/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.swc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.royale.compiler.Messages;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.CommandLineConfigurator;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.ConfigurationPathResolver;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.internal.config.annotations.SoftPrerequisites;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.LibraryDependencyGraph;
import org.apache.royale.compiler.internal.targets.SWFTarget;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.FileIOProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.catalog.XMLFormatter;

/**
 * swcdepends command line utility.
 * 
 * Use this utility to see the dependency order of all SWCs in your configuration.
 * You can also see:
 * 
 * - the swcs a swc is dependent on (on by default).
 * - the scripts that are causing the dependency (off by default).
 * - the dependency types of the scripts (off by default).
 * 
 */
public class SWCDepends
{

    // XML names
    public static final String SWC_DEPENDENCY_ORDER_ELEMENT = "swc-dependency-order";
    public static final String SWC_DEPENDENCIES_ELEMENT = "swc-dependencies";
    public static final String SWC_ELEMENT = "swc";
    public static final String DEFINITION_DEPENDENCIES_ELEMENT = "definition-dependencies";
    public static final String DEFINITION_ELEMENT = "def";
    public static final String PATH_ATTRIBUTE = "path";
    public static final String ID_ATTRIBUTE = "id";
    
    static final String NEWLINE = System.getProperty("line.separator");
    private static final String DEFAULT_VAR = "no-default-arg";
    private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";

    protected Workspace workspace;
    protected RoyaleProject project;
    protected DependencyConfiguration config;
    protected ProblemQuery problems;
    protected ConfigurationBuffer configBuffer;

    protected Configurator projectConfigurator;

    protected ICompilationUnit mainCU;
    protected SWFTarget target;
    protected ITargetSettings targetSettings;
    
    private List<String> dependencyOrder;     // of all discovered swcs
    
    /**
     * Entry point for <code>swcdepends</code> tool.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        int exitCode = staticMainNoExit(args);
        System.exit(exitCode);
    }


    public static int staticMainNoExit(final String[] args)
    {
        final SWCDepends swcDepends = new SWCDepends();
        return swcDepends.mainNoExit(args);
    }

    public SWCDepends()
    {
        workspace = new Workspace();
        project = new RoyaleProject(workspace);
        problems = new ProblemQuery();
    }

    public int mainNoExit(final String[] args)
    {
        int result = 0;
        
        if (configure(args))
        {
            XMLStreamWriter xmlWriter = null;
            try
            {
                List<String> showSwcs = config.getShowSwcs();
                DependencyTypeSet dependencyTypes = getDependencyTypeSet();
                LibraryDependencyGraph graph = project.createLibraryDependencyGraph(dependencyTypes);
                
                
                xmlWriter = new XMLFormatter(getXMLWriter());
                xmlWriter.writeStartDocument();
                
                // write SWCDependencyOrder
                xmlWriter.writeStartElement(SWC_DEPENDENCY_ORDER_ELEMENT);

                dependencyOrder = graph.getDependencyOrder();
                for (String swcName : dependencyOrder)
                {
                    if (!showSwc(swcName, showSwcs))
                        continue;
                    
                    println(swcName + ":");

                    xmlWriter.writeStartElement(SWC_ELEMENT);
                    xmlWriter.writeAttribute(PATH_ATTRIBUTE, swcName);

                    // Show the dependencies of this swc.
                    printDependencies(swcName, xmlWriter, graph);
                    
                    xmlWriter.writeEndElement(); // SWC
                }
                
                xmlWriter.writeEndElement(); // SWC_DEPENDENCY_ORDER
                xmlWriter.writeEndDocument();
                xmlWriter.flush();
                xmlWriter.close();
            }
            catch (IOException e)
            {
                final FileIOProblem problem = new FileIOProblem(e);
                problems.add(problem);
            }
            catch (Exception e)
            {
                final ICompilerProblem problem = new InternalCompilerProblem(e);
                problems.add(problem);
            }
            finally
            {
                if (xmlWriter != null)
                {
                    try
                    {
                        xmlWriter.close();
                    }
                    catch (XMLStreamException e)
                    {
                        // ignore
                    }
                }
            }
        }
        else
        {
            result = 1;
        }

        // Print out any errors we may have encountered
        ProblemFormatter formatter = new WorkspaceProblemFormatter(workspace, null);

        ProblemPrinter printer = new ProblemPrinter(formatter, System.err);
        printer.printProblems(problems.getFilteredProblems());

        if( problems.hasErrors() )
            result = 1;

        return result;
    }
    
    /**
     * @param swcName name of swc
     * @param swcs list of swcs to show. An empty list means show all.
     * @return true if the swc should be shown, false otherwise.
     */
    private boolean showSwc(String swcName, List<String>swcs)
    {
        // filter the swcs that are shown
        boolean show = true;
        if (swcs.size() != 0)
        {
            show = false;
            for (String showSwc : swcs)
            {
                if (swcName.endsWith(showSwc))
                {
                    show = true;
                    break;
                }
            }
        }
      
        return show;
    }


    /**
     * Print the dependencies of a SWC.
     * 
     * @param xmlWriter
     * @param graph
     * @throws XMLStreamException 
     */
    private void printDependencies(String swcName, XMLStreamWriter xmlWriter, LibraryDependencyGraph graph) throws XMLStreamException
    {
        // show swc dependencies
        if (config.getShowDependencyList())
        {
            Set<String> dependencies = graph.getDependencies(swcName);
            
            if (dependencies.size() > 0)
            {
                xmlWriter.writeStartElement(SWC_DEPENDENCIES_ELEMENT);
                
                // Sort by dependency order so the swcs are always
                // in the same order.
                List<String> swcDependencies = new ArrayList<String>(graph.getDependencies(swcName));
                Collections.sort(swcDependencies, 
                        new Comparator<String>()
                        {
                            @Override
                            public int compare(String o1, String o2)
                            {
                                return dependencyOrder.indexOf(o1) - dependencyOrder.indexOf(o2);
                            }
                            
                        });
                
                List<String> swcFilter = config.getShowDependentSwcs();
                for (String swcDependency : swcDependencies)
                {
                    if (!showSwc(swcDependency, swcFilter))
                        continue;
                    
                    println("\t" + swcDependency);
                    
                    xmlWriter.writeStartElement(SWC_ELEMENT);
                    xmlWriter.writeAttribute(PATH_ATTRIBUTE, swcDependency);

                    printExterns(swcName, swcDependency, xmlWriter, graph);
                    
                    xmlWriter.writeEndElement(); // SWC
                }
                xmlWriter.writeEndElement(); // SWC_DEPENDENCY_ORDER
            }
        }
        
    }

    /**
     * Print the external scripts that are causing the dependency.
     * 
     * @param swcDependency
     * @param xmlWriter
     * @param graph
     * @throws XMLStreamException 
     */
    private void printExterns(String swcName, String swcDependency, 
            XMLStreamWriter xmlWriter, LibraryDependencyGraph graph) 
            throws XMLStreamException
    {
        // list the external scripts that caused the dependencies between
        // swcLocation and swcDepName.
        if (config.getShowExterns())
        {
            xmlWriter.writeStartElement(DEFINITION_DEPENDENCIES_ELEMENT);
            
            Map<String, DependencyTypeSet> dependencyMap = graph.getDependencySet(swcName, swcDependency); 
            for (Map.Entry<String, DependencyTypeSet> entry : dependencyMap.entrySet()) 
            {
                xmlWriter.writeStartElement(DEFINITION_ELEMENT);
                xmlWriter.writeAttribute(ID_ATTRIBUTE, entry.getKey());

                if (config.getShowTypes())
                {
                    System.out.print("\t\t" + entry.getKey() + "\t");
                    
                    StringBuilder sb = new StringBuilder();
                    for (Iterator<DependencyType>iter = entry.getValue().iterator(); 
                         iter.hasNext();)
                    {
                        DependencyType type = iter.next();
                        
                        System.out.print(type.getSymbol());
                        sb.append(type.getSymbol());
                        
                        if (iter.hasNext())
                        {
                            sb.append(",");
                            System.out.print(" ");
                        }
                    }
                    
                    println("");
                    xmlWriter.writeAttribute("types", sb.toString());
                }
                else
                {
                    println("\t\t" + entry.getKey());
                }
                
                xmlWriter.writeEndElement(); // SCRIPT_NAME
            }
            
            xmlWriter.writeEndElement(); // DEPENDENT_SCRIPTS
        }
    }

    /**
     * @return A writer for the xml-based dependency report.
     * 
     * @throws XMLStreamException 
     * @throws IOException 
     */
    private XMLStreamWriter getXMLWriter() throws XMLStreamException, IOException
    {
        Writer writer = null;
        if (config.getDependencyReport() != null)
            writer = new FileWriter(config.getDependencyReport());
        else
            writer = new StringWriter();    // throw away output

        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        assert xmlOutputFactory != null : "Expect XMLOutputFactory implementation.";

        return xmlOutputFactory.createXMLStreamWriter(writer);
    }


    /**
     * @return the dependency set requested by the user. The default to 
     * to show all dependency types.
     */
    private DependencyTypeSet getDependencyTypeSet()
    {
        List<String> desiredDependencies = config.getDesiredScriptDependencyTypes();
        if (desiredDependencies != null)
        {
            DependencyTypeSet dependencySet = DependencyTypeSet.noneOf();
            List<String> validDependencies = new ArrayList<String>();
            for (DependencyType type : DependencyTypeSet.allOf())
            {
                validDependencies.add(String.valueOf(type.getSymbol()));
            }
            
            // convert strings to a enum set.
            for (String desiredDependency : desiredDependencies)
            {
                if (validDependencies.contains(desiredDependency))
                    dependencySet.add(DependencyType.get(desiredDependency.charAt(0)));
            }
            
            return dependencySet;
        }
        
        return DependencyTypeSet.allOf();
    }

    /**
     * Load configurations from all the sources.
     * 
     * @param args command line arguments
     * @return True if mxmlc should continue with compilation.
     */
    protected boolean configure(final String[] args)
    {
        projectConfigurator = createConfigurator();
        
        try
        {
            ConfigurationPathResolver resolver = new ConfigurationPathResolver(System.getProperty("user.dir")); 
            projectConfigurator.setConfigurationPathResolver(resolver);
            projectConfigurator.setConfiguration(args, getConfigurationDefaultVariable());
            projectConfigurator.applyToProject(project);
            problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());
            
            // Get the configuration and configBuffer which are now initialized.
            config = (DependencyConfiguration)projectConfigurator.getConfiguration();
            Messages.setLocale(config.getToolsLocale());
            configBuffer = projectConfigurator.getConfigurationBuffer();
            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null)
            {
                println(VersionInfo.buildMessage());
                return false;
            }

            // Print help if "-help" is present.
            final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
            if (helpVar != null)
            {
                processHelp(helpVar);
                return false;
            }
            
            for (String fileName : projectConfigurator.getLoadedConfigurationFiles())
            {
                println(Messages.getString("MXMLC.Loading_configuration_format", 
                        Collections.<String,Object>singletonMap("configurationName", fileName)));                
            }
            
            // Add a blank line between the configuration list and the rest of 
            // the output to make the start of the output easier to detect.
            println(""); 
            
            // If we have configuration errors then exit before trying to 
            // validate the target.
            if (problems.hasErrors())
                return false;
            
            return true;
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, e.getMessage());
            problems.add(problem);
            return false;
        }
    }
    
    /**
     * Print detailed help information if -help is provided.
     */
    private void processHelp(final List<ConfigurationValue> helpVar)
    {
        final Set<String> keywords = new LinkedHashSet<String>();
        for (final ConfigurationValue val : helpVar)
        {
            for (final Object element : val.getArgs())
            {
                String keyword = (String)element;
                while (keyword.startsWith("-"))
                    keyword = keyword.substring(1);
                keywords.add(keyword);
            }
        }

        if (keywords.size() == 0)
            keywords.add("help");

        final String usages = CommandLineConfigurator.usage(
                    getProgramName(),
                    DEFAULT_VAR,
                    configBuffer,
                    keywords,
                    LocalizationManager.get(),
                    L10N_CONFIG_PREFIX);
        println(getStartMessage());
        println(usages);
    }
    
    /**
     * Print a message.
     * 
     * @param msg Message text.
     */
    protected void println(final String msg)
    {
        System.out.println(msg);
    }

    /**
     * Get the start up message that contains the program name with the
     * copyright notice.
     * 
     * @return the start up message.
     */
    private String getStartMessage()
    {
        // This must not be localized.
        String message = "SWC Dependency utility (swcdepends)" + NEWLINE +
            VersionInfo.buildMessage() + NEWLINE;
        return message;
    }

    private String getConfigurationDefaultVariable()
    {
        return DEFAULT_VAR;
    }

    private Configurator createConfigurator()
    {
        return new Configurator(DependencyConfiguration.class);
    }


    /**
     * @return always "swcdepends"
     */
    private String getProgramName()
    {
        return "swcdepends";
    }

    /**
     * dependency.* configuration options
     *
     */
    public static class DependencyConfiguration extends Configuration
    {

        //
        // 'show-external-classes' option
        //
        //  Should we show the external scripts
        //
        
        private boolean showExterns = false;

        public boolean getShowExterns()
        {
            return showExterns;
        }

        @Config
        @Mapping({"dependency", "show-external-classes"})
        public void setShowExternalClasses(ConfigurationValue cv, boolean showExterns) throws ConfigurationException
        {
            this.showExterns = showExterns;
        }

        //
        // 'show-dependency-list' option
        //
        //  Should swc dependencies be shown in addition to the dependency 
        //  order?
        //
        
        private boolean showDependencyList = true;

        public boolean getShowDependencyList()
        {
            return showDependencyList;
        }

        @Config
        @Mapping({"dependency", "show-dependency-list"})
        public void setShowDependencyList(ConfigurationValue cv, boolean showSwcDependencies) throws ConfigurationException
        {
            this.showDependencyList = showSwcDependencies;
        }

        //
        // 'show-types' option
        //
        //  Should we show the external scripts
        //
        
        private boolean showTypes = false;

        public boolean getShowTypes()
        {
            return showTypes;
        }

        @Config
        @Mapping({"dependency", "show-types"})
        @SoftPrerequisites("show-external-classes")
        public void setShowTypes(ConfigurationValue cv, boolean showTypes) throws ConfigurationException
        {
            this.showTypes = showTypes;
            
            // if showTypes is set, then turn on show-external-classes show the types will be seen.
            if (showTypes)
                showExterns = true;
        }

        //
        // 'types' option
        //
        
        private List<String> desiredTypes = new LinkedList<String>();
        
        public List<String> getDesiredScriptDependencyTypes()
        {
            return desiredTypes;
        }
        
        @Config(allowMultiple=true)
        @Mapping({"dependency", "types"})
        @Arguments("type")
        @InfiniteArguments
        public void setTypes( ConfigurationValue cfgval, String[] types ) throws ConfigurationException
        {
            for (int i = 0; i < types.length; ++i)
            {
                desiredTypes.add( types[i] );
            }
        }

        //
        // 'show-swcs' option
        //
        //  Filter which SWCs to show in the dependency order.
        //
        
        private List<String> showSwcs = new LinkedList<String>();
        
        public List<String> getShowSwcs()
        {
            return showSwcs;
        }
        
        @Config(allowMultiple = true)
        @Mapping({"dependency", "show-swcs"})
        @Arguments("swc-name")
        @InfiniteArguments
        public void setShowSwcs(ConfigurationValue cfgval, String[] swcs) throws ConfigurationException
        {
            for (int i = 0; i < swcs.length; ++i)
            {
                showSwcs.add(swcs[i]);
            }
        }

        //
        // 'show-swcs' option
        //
        //  Filter which SWCs to show in the "dependent SWCs" list. 
        //
        
        private List<String> showDependentSwcs = new LinkedList<String>();
        
        public List<String> getShowDependentSwcs()
        {
            return showDependentSwcs;
        }
        
        @Config(allowMultiple = true)
        @Mapping({"dependency", "show-dependent-swcs"})
        @Arguments("swc-name")
        @InfiniteArguments
        public void setShowDependentSwcs(ConfigurationValue cfgval, String[] swcs) throws ConfigurationException
        {
            for (int i = 0; i < swcs.length; ++i)
            {
                showDependentSwcs.add(swcs[i]);
            }
        }

        //
        // 'minimize-dependency-set' option
        //
        //  Removes a SWC from the dependency set if the scripts resolved in a SWC are a subset of the scripts resolved in another dependent SWC.
        //  No longer supported, the dependency set is always minimized.
        //
        
        @Config(removed=true)
        @Mapping({"dependency", "minimize-dependency-set"})
        public void setMinimizeDependencySet(ConfigurationValue cv, boolean minimumSet) throws ConfigurationException
        {
            // leave configuration option here so old scripts do not error.
        }
        
        // 
        // -dependency-report option
        //
        // Takes a path that specifies a filename where an xml version of the report
        // is written.
        
        private String dependencyReportFileName;
        
        public File getDependencyReport()
        {
            return dependencyReportFileName != null ? new File(dependencyReportFileName) : null;
        }

        @Config()
        @Mapping({"dependency", "dependency-report"})
        @Arguments("filename")
        public void setDependencyReport(ConfigurationValue cv, String filename)
        {
            this.dependencyReportFileName = getOutputPath(cv, filename);
        }
        
    }

}
