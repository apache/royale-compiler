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

package org.apache.royale.compiler.clients;

import org.apache.royale.compiler.Messages;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.CommandLineConfigurator;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.ConfigurationPathResolver;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.IConfigurationFilter;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.targets.AppSWFTarget;
import org.apache.royale.compiler.internal.targets.Target;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.swf.io.ISWFWriterFactory;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFWriter;
import org.apache.royale.swf.io.SWFReader;
import org.apache.royale.swf.io.SizeReportWritingSWFWriter;
import com.google.common.collect.ImmutableSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.flex.tools.FlexTool;

/**
 * Command line optimizer - can read in a swf, apply the optimizations usually done during swf linking,
 * and write out the swf again.
 */
public class Optimizer implements FlexTool
{
    static final String NEWLINE = System.getProperty("line.separator");
    private static final String DEFAULT_VAR = "input";
    private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";

    /**
     * Entry point for <code>optimizer</code> tool.
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
        final Optimizer optimizer = new Optimizer();
        return optimizer.mainNoExit(args);
    }

    @Override
    public String getName() {
        return FLEX_TOOL_OPTIMIZER;
    }

    @Override
    public int execute(String[] args) {
        return mainNoExit(args);
    }

    public int mainNoExit(final String[] args)
    {
        int result = 0;
        long startTime = System.nanoTime();
        
        if( configure(args) )
        {
            ITargetSettings targetSettings = getTargetSettings();

            final File inputFile = new File(getInputFilePath());
            File outputFile = new File(config.getOutput());
            
            InputStream in = null;
            try
            {
                if( inputFile.exists() )
                {
                    // Read in the SWF
                    in = new BufferedInputStream(new FileInputStream(inputFile));
                    SWFReader reader = new SWFReader();
                    try
                    {
                        ISWF swf = reader.readFrom(in, getInputFilePath());
                        // record any problems encountered reading the swf
                        problems.addAll(reader.getProblems());

                        List<ICompilerProblem> linkProblems = new ArrayList<ICompilerProblem>();
    
                        // do the optimization
                        OptimizerSWFTarget target = new OptimizerSWFTarget(swf, project, targetSettings, null);
                        ISWF optimizedSWF = target.build(linkProblems);

                        // record any problems found
                        problems.addAll(linkProblems);

                        if( !problems.hasErrors() )
                        {
                            Header.Compression compression = Header.decideCompression(true, swf.getVersion(), false);
    
                            final ISWFWriterFactory writerFactory = SizeReportWritingSWFWriter.getSWFWriterFactory(
                                    targetSettings.getSizeReport());
                            final ISWFWriter writer = writerFactory.createSWFWriter(optimizedSWF, compression,
                                    targetSettings.isDebugEnabled(), targetSettings.isTelemetryEnabled());
                            int swfSize = writer.writeTo(outputFile);
            
                            long endTime = System.nanoTime();
                            String seconds = String.format("%5.3f", (endTime - startTime) / 1e9);
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("byteCount", swfSize);
                            params.put("path", outputFile.getCanonicalPath());
                            params.put("seconds", seconds);
                            System.out.println(Messages.getString(
                                    "MXMLC.bytes_written_to_file_in_seconds_format",
                                    params));
                        }
                    }
                    finally
                    {
                        IOUtils.closeQuietly(reader);
                    }   
                }
                else
                {
                    problems.add(new FileNotFoundProblem(inputFile.getAbsolutePath()));
                }
            }
            catch(IOException ioe)
            {
                final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, ioe.getMessage());
                problems.add(problem);
            }
            finally
            {
                if ( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch(IOException ioe)
                    {
                        final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, ioe.getMessage());
                        problems.add(problem);
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

    public Optimizer()
    {
        workspace = new Workspace();
        project = new RoyaleProject(workspace);
        problems = new ProblemQuery();
    }

    protected boolean configure(String[] args)
    {
        projectConfigurator = createConfigurator();

        try
        {
            // Print brief usage if no arguments provided.
            if (args.length == 0)
            {
                final String usage = CommandLineConfigurator.brief(
                        getProgramName(),
                        DEFAULT_VAR,
                        LocalizationManager.get(),
                        L10N_CONFIG_PREFIX);
                System.out.println(getStartMessage());
                if (usage != null)
                    System.out.println(usage);

                // Create a default configuration so we can exit gracefully.
                config = new OptimizerConfiguration();
                return false;
            }

            ConfigurationPathResolver resolver = new ConfigurationPathResolver(System.getProperty("user.dir")); 
            projectConfigurator.setConfigurationPathResolver(resolver);
            projectConfigurator.setConfiguration(args, getConfigurationDefaultVariable());

            projectConfigurator.applyToProject(project);
            config = (OptimizerConfiguration)projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();
            config.setCompilerOptimize(null, true);

            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null)
            {
                System.out.println(VersionInfo.buildMessage());
                return false;
            }

            // Print help if "-help" is present.
            final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
            if (helpVar != null)
            {
                processHelp(helpVar);
                return false;
            }
            
            if( problems.hasErrors() )
                return false;
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, e.getMessage());
            problems.add(problem);
            return false;
        }

        return true;

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
        System.out.println(getStartMessage());
        System.out.println(usages);
    }
    
    /**
     * Get the start up message that contains the program name 
     * with the copyright notice.
     * 
     * @return
     */
    private String getStartMessage()
    {
        // This must not be localized.
        String message = "Apache ActionScript Compiler (optimizer)" + NEWLINE +
            VersionInfo.buildMessage() + NEWLINE;
        return message;
    }

    /**
     * @return the input file path
     */
    private String getInputFilePath()
    {
        return config.getInput();
    }

    /**
     * @return  the target settings specified by the configuration
     */
    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(getTargetType());

        return targetSettings;
    }

    private Workspace workspace;
    private RoyaleProject project;
    private Configurator projectConfigurator;
    private OptimizerConfiguration config;
    private ITargetSettings targetSettings;
    private ProblemQuery problems;
    protected ConfigurationBuffer configBuffer;

    protected String getConfigurationDefaultVariable()
    {
        return "input";
    }

    protected Configurator createConfigurator()
    {
        return new OptimizerConfigurator(OptimizerConfiguration.class);
    }


    /**
     * @return always "optimizer"
     */
    protected String getProgramName()
    {
        return "optimizer";
    }

    /**
     */
    protected Target.TargetType getTargetType()
    {
        return Target.TargetType.SWF;
    }

    /**
     * Configuration class - adds the input option
     */
    public static class OptimizerConfiguration extends Configuration
    {
        //
        // 'input' option
        //

        private String input;

        public String getInput()
        {
            return input;
        }

        @Config(isRequired=true)
        @Arguments("filename")
        public void setInput(ConfigurationValue val, String output) throws ConfigurationException
        {
            this.input = getOutputPath(val, output);
        }
        
        //
        // 'output' option
        //
        
        private String output = "output.swf";
        
        @Override
        public String getOutput()
        {
            return output;
        }

        @Override
        @Config
        @Arguments("filename")        
        public void setOutput(ConfigurationValue val, String output) throws ConfigurationException
        {
            this.output = output;
        }
        
    }

    /**
     * Optimizer configurator - provides overrides to create the right config buffer,
     * and defaults for the optimizer
     */
    public static class OptimizerConfigurator extends Configurator
    {
        public OptimizerConfigurator(Class<? extends Configuration> configurationClass)
        {
            super(configurationClass);
        }

        /**
         * Set of vars that can be set from the command line
         */
        private static Set<String> configVars = ImmutableSet.<String>of("help", "version", "load-config", "input", "output",
                "compiler.keep-as3-metadata", "compiler.debug");

        /**
         * Create a config filter that will filter out parameters that the optimizer doesn't accept
         */
        protected IConfigurationFilter createConfigFilter()
        {
            return new IConfigurationFilter()
            {
                @Override
                public boolean select(String name)
                {
                    return configVars.contains(name);
                }
            };
        }

        /**
         * Create the config buffer with the right filter
         */
        @Override
        protected ConfigurationBuffer createConfigurationBuffer(Class<? extends Configuration> configClass)
        {
            return new ConfigurationBuffer(configClass, Collections.<String, String>emptyMap(), createConfigFilter());
        }

        /**
         * Load default values
         */
        @Override
        protected void loadDefaults (ConfigurationBuffer cfgbuf) throws ConfigurationException
        {
        }

        /**
         * By-pass the configurations that requires Flex SDK.
         *
         * This method is needed here because the baseclass version tries to set some config vars
         * that don't exist in an optimizer config.
         */
        @Override
        protected void byPassConfigurationsRequiringFlexSDK() throws ConfigurationException
        {
        }
    }

    /**
     * A SWF target that will start with a SWF, and simply link it to build it
     */
    public static class OptimizerSWFTarget extends AppSWFTarget
    {
        /**
         * The swf to optimize
         */
        private ISWF swf;

        @Override
        public ISWF build(Collection<ICompilerProblem> problems)
        {
            return linkSWF(swf);
        }

        public OptimizerSWFTarget (ISWF swf, CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
        {
            super(project, targetSettings, progressMonitor);
            this.swf = swf;
        }
    }
}
