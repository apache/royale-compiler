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

package flex2.tools.oem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.annotation.Annotation;
import java.net.URI;

import org.apache.royale.compiler.clients.COMPJSC;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

import flex2.compiler.CompilerException;
import flex2.compiler.Source;
import flex2.compiler.SourceList;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.io.FileUtil;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.util.Benchmark;
import flex2.compiler.util.CompilerControl;
import flex2.compiler.util.CompilerMessage;
import flex2.compiler.util.MimeMappings;
import flex2.compiler.util.PerformanceData;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.linker.SimpleMovie;
import flex2.tools.oem.internal.ApplicationCompilerConfiguration;
import flex2.tools.oem.internal.LibraryCompilerConfiguration;
import flex2.tools.oem.internal.OEMConfiguration;
import flex2.tools.oem.internal.OEMReport;
import flex2.tools.oem.internal.OEMUtil;

/**
 * The <code>Library</code> class represents a SWC archive or a RSL. It implements the <code>Builder</code> interface
 * which allows for building the library incrementally. The following example defines a SWC archive or RSL:
 *
 * <pre>
 * Library lib = new Library();
 * </pre>
 *
 * You can add components to the <code>Library</code> object in the following ways:
 *
 * <pre>
 * 1. String              - Specify a fully-qualified name.
 * 2. File                - Specify a source file.
 * 3. VirtualLocalFile    - Specify an in-memory source object.
 * 4. URI                 - Specify a namespace URI.
 * </pre>
 *
 * <p>
 * To add resource bundles to the <code>Library</code>, you can use the <code>addResourceBundle()</code> method,
 * as the following example shows:
 *
 * <pre>
 * lib.addResourceBundle("mx.controls"));
 * </pre>
 *
 * <p>
 * To add archive files to the <code>Library</code>, you can use the <code>addArchiveFile()</code> method, as the following
 * example shows:
 *
 * <pre>
 * lib.addArchiveFile("defaults.css", new File("path1/myStyle.css"));
 * </pre>
 *
 * Before you can compile with a <code>Library</code> object, you must configure it. The following
 * four methods are the most common methods you use to configure the <code>Library</code> object:
 *
 * <pre>
 * 1. setLogger()        - Use this to set a Logger so that the client can be notified of events that occurred during the compilation.
 * 2. setConfiguration() - Optional. Use this to specify compiler options.
 * 3. setOutput()        - Optional. Use this to specify an output file name.
 * 4. setDirectory()     - Optional. Use this to specify an RSL output directory.
 * </pre>
 *
 * You must implement the <code>flex2.tools.oem.Logger</code> interface and use the implementation as the <code>Logger</code>
 * for the compilation. The following is an example <code>Logger</code> implementation:
 *
 * <pre>
 * lib.setLogger(new flex2.tools.oem.Logger()
 * {
 *     public void log(Message message, int errorCode, String source)
 *     {
 *         System.out.println(message);
 *     }
 * });
 * </pre>
 *
 * To specify compiler options for the <code>Library</code> object, you
 * must get a <code>Configuration</code> object that is populated with default values. Then, you set
 * compiler options programmatically using methods of the <code>Configuration</code> class.
 *
 * <p>
 * The <code>setOutput()</code> method lets you specify where the <code>Library</code> object writes
 * the output to. If you call the <code>setOutput()</code> method, the <code>build(boolean)</code> method
 * writes directly to the specified location; for example:
 *
 * <pre>
 * lib.setOutput(new File("MyLib.swc"));
 * lib.build(true);
 * </pre>
 *
 * If you do not call the <code>setOutput()</code> method, you can use the <code>build(OutputStream, boolean)</code>
 * method. This requires that you provide a buffered output stream; for example:
 *
 * <pre>
 * lib.build(new BufferedOutputStream(new FileOutputStream("MyLib.swc")), true);
 * </pre>
 *
 * The <code>setDirectory()</code> method lets you output RSLs to the specified directory; for example:
 *
 * <pre>
 * lib.setDirectory(new File("dir1"));
 * lib.build(true);
 * </pre>
 *
 * You can save the <code>Library</code> object compilation
 * data for reuse. You do this using the <code>save(OutputStream)</code> method. Subsequent compilations can use
 * the <code>load(OutputStream)</code> method to get the old data into the <code>Library</code> object; for example:
 *
 * <pre>
 * lib.save(new BufferedOutputStream(new FileOutputStream("MyLib.incr")));
 * </pre>
 *
 * When a cache file (for example, <code>MyLib.incr</code>) from a previous compilation is available before the
 * compilation, you can call the <code>load(OutputStream)</code> method before you call the <code>build()</code> method; for example:
 *
 * <pre>
 * lib.load(new BufferedInputStream(FileInputStream("MyLib.incr")));
 * lib.build(true);
 * </pre>
 *
 * The <code>build(false)</code> and <code>build(OutputStream, false)</code> methods always rebuild the library.
 * The first time you build the <code>Library</code>
 * object, the <code>build(true)/build(OutputStream, true)</code> methods do a complete build, which
 * is equivalent to the <code>build(false)/build(OutputStream, false)</code> methods, respectively. After you call the
 * <code>clean()</code> method, the <code>Library</code> object always does a full build.
 *
 * <p>
 * The <code>clean()</code> method cleans up compilation data in the <code>Library</code> object the output
 * file, if the <code>setOutput()</code> method was called.
 *
 * <p>
 * You can use the <code>Library</code> class to build a library from a combination of source
 * files in the file system and in-memory, dynamically-generated source objects. You
 * must use the <code>addComponent(VirtualLocalFile)</code>, <code>addResourceBundle(VirtualLocalFile)</code>, and
 * <code>addArchiveFile(String, VirtualLocalFile)</code> methods to use in-memory objects.
 *
 * <p>
 * The <code>Library</code> class can be part of a <code>Project</code>.
 *
 * @see flex2.tools.oem.Configuration
 * @see flex2.tools.oem.Project
 * @version 2.0.1
 * @author Clement Wong
 */
public class Library implements Builder, Cloneable
{
    static
    {
        // This "should" trigger the static initialization of Application which locates
        // flex-compiler-oem.jar and set application.home correctly.
        try
        {
            // in Java 1.4, simply saying Application.class would load the class
            // Java 1.5 is much smarter, and you have to coax the JVM to actually load it
            Class.forName("flex2.tools.oem.Application");
        }
        catch (ClassNotFoundException e)
        {
            // I guess it didn't work *shrug*
            e.printStackTrace();
            assert false;
        }
    }

    /**
     * Constructor.
     */
    public Library()
    {
        sources = new TreeSet<VirtualFile>(new Comparator<VirtualFile>()
        {
            public int compare(VirtualFile f0, VirtualFile f1)
            {
                return f0.getName().compareTo(f1.getName());
            }
        });
        classes = new TreeSet<String>();
        namespaces = new TreeSet<URI>();
        resourceBundles = new TreeSet<String>();
        files = new TreeMap<String, VirtualFile>();
        stylesheets = new TreeMap<String, VirtualFile>();

        oemConfiguration = null;
        logger = null;
        output = null;
        directory = null;
        mimeMappings = new MimeMappings();
        meter = null;
        resolver = null;
        cc = new CompilerControl();

        //data = null;
        cacheName = null;
        configurationReport = null;
        messages = new ArrayList<Message>();
    }

    private Set<VirtualFile> sources;
    private Set<String> classes, resourceBundles;
    private Set<URI> namespaces;
    private Map<String, VirtualFile> files, stylesheets;
    private OEMConfiguration oemConfiguration;
    private Logger logger;
    private File output, directory;
    private MimeMappings mimeMappings;
    private ProgressMeter meter;
    protected PathResolver resolver;
    private CompilerControl cc;
    private ApplicationCache applicationCache;
    private LibraryCache libraryCache;
    
    private List<Source> compiledSources;
    private SourceList sourceList;


    // clean() would null out the following variables
    //LibraryData data;
    private String cacheName, configurationReport;
    private List<Message> messages;
    private HashMap<String, PerformanceData[]> compilerBenchmarks;
    private Benchmark benchmark;

    /**
     * Adds a class, function, variable, or namespace to this <code>Library</code> object.
     *
     * This is the equilvalent of the <code>include-classes</code> option of the compc compiler.
     *
     * @param includeClass A fully-qualified name.
     */
    public void addComponent(String includeClass)
    {
        classes.add(includeClass);
    }

    /**
     * Adds a component to this <code>Library</code> object.
     * This is the equilvalent of the <code>include-sources</code> option of the compc compiler.
     *
     * @param includeSource A source file.
     */
    public void addComponent(File includeSource)
    {
        sources.add(new LocalFile(includeSource));
    }

    /**
     * Adds a component to this <code>Library</code> object.
     *
     * This is equilvalent to the <code>include-sources</code> option of the compc compiler.
     *
     * @param includeSource An in-memory source object.
     */
    public void addComponent(VirtualLocalFile includeSource)
    {
        sources.add(includeSource);
    }

    /**
     * Adds a list of components to this <code>Library</code> object.
     *
     * This is equilvalent to the <code>include-namespaces</code> option of the compc compiler.
     *
     * @param includeNamespace A namespace URI.
     */
    public void addComponent(URI includeNamespace)
    {
        namespaces.add(includeNamespace);
    }

    /**
     * Removes the specified component from this <code>Library</code> object.
     * The name can be a class, a function, a variable, or a namespace.
     *
     * @param includeClass A fully-qualified name.
     */
    public void removeComponent(String includeClass)
    {
        classes.remove(includeClass);
    }

    /**
     * Removes the specified component from this <code>Library</code> object.
     *
     * @param includeSource A source file.
     */
    public void removeComponent(File includeSource)
    {
        sources.remove(new LocalFile(includeSource));
    }

    /**
     * Removes the specified component from this <code>Library</code> object.
     *
     * @param includeSource An in-memory source object.
     */
    public void removeComponent(VirtualLocalFile includeSource)
    {
        sources.remove(includeSource);
    }

    /**
     * Removes the specified list of components from this <code>Library</code> object. The input argument is a namespace URI.
     *
     * @param includeNamespace A namespace URI.
     */
    public void removeComponent(URI includeNamespace)
    {
        namespaces.remove(includeNamespace);
    }

    /**
     * Removes all the components from this <code>Library</code> object.
     */
    public void removeAllComponents()
    {
        sources.clear();
        classes.clear();
        namespaces.clear();
    }

    /**
     * Adds a resource bundle to this <code>Library</code> object.
     *
     * This is equilvalent to the <code>include-resource-bundles</code> option of the compc compiler.
     *
     * @param resourceBundle A resource bundle name.
     */
    public void addResourceBundle(String resourceBundle)
    {
        resourceBundles.add(resourceBundle);
    }

    /**
     * Removes the specified resource bundle name from this <code>Library</code> object.
     *
     * @param resourceBundle A resource bundle name.
     */
    public void removeResourceBundle(String resourceBundle)
    {
        resourceBundles.remove(resourceBundle);
    }

    /**
     * Removes all the resource bundles from this <code>Library</code> object.
     *
     */
    public void removeAllResourceBundles()
    {
        resourceBundles.clear();
    }

    /**
     * Adds a file to this <code>Library</code> object. This is equilvalent to the <code>include-file</code> option of the compc compiler.
     *
     * @param name The name in the archive.
     * @param file The file to be added.
     */
    public void addArchiveFile(String name, File file)
    {
        files.put(name, new LocalFile(file));
    }

    /**
     * Adds an in-memory source object to this <code>Library</code> object. This is equilvalent to the <code>
     * include-file</code> option of the compc compiler.
     *
     * @param name The name in the archive.
     * @param file The in-memory source object to be added.
     */
    public void addArchiveFile(String name, VirtualLocalFile file)
    {
        files.put(name, file);
    }

    /**
     * Removes the specified file from this <code>Library</code> object.
     *
     * @param name The name in the archive.
     */
    public void removeArchiveFile(String name)
    {
        files.remove(name);
    }

    /**
     * Removes all the archive files from this <code>Library</code> object.
     */
    public void removeAllArchiveFiles()
    {
        files.clear();
    }

    /**
     * Adds a CSS stylesheet to this <code>Library</code> object. This is equilvalent to the <code>include-stylesheet</code> option of the compc compiler.
     *
     * @param name The name in the archive.
     * @param file The file to be added.
     * @since 3.0
     */
    public void addStyleSheet(String name, File file)
    {
        stylesheets.put(name, new LocalFile(file));
    }

    /**
     * Adds an in-memory CSS stylesheet object to this <code>Library</code> object. This is equilvalent to the <code>
     * include-stylesheet</code> option of the compc compiler.
     *
     * @param name The name in the archive.
     * @param file The in-memory source object to be added.
     * @since 3.0
     */
    public void addStyleSheet(String name, VirtualLocalFile file)
    {
        stylesheets.put(name, file);
    }

    /**
     * Removes the specified CSS stylesheet from this <code>Library</code> object.
     *
     * @param name The name in the archive.
     * @since 3.0
     */
    public void removeStyleSheet(String name)
    {
        stylesheets.remove(name);
    }

    /**
     * Removes all the CSS stylesheets from this <code>Library</code> object.
     * @since 3.0
     */
    public void removeAllStyleSheets()
    {
        stylesheets.clear();
    }

    /**
     * @inheritDoc
     */
    public void setConfiguration(Configuration configuration)
    {
        oemConfiguration = (OEMConfiguration) configuration;
    }

    /**
     * @inheritDoc
     */
    public Configuration getDefaultConfiguration()
    {
        return getDefaultConfiguration(false);
    }

    /**
     *
     * @param processDefaults
     * @return
     */
    private Configuration getDefaultConfiguration(boolean processDefaults)
    {
        return OEMUtil.getLibraryConfiguration(constructCommandLine(null), false, false,
                                               OEMUtil.getLogger(logger, messages), resolver,
                                               mimeMappings, processDefaults);
    }

    /**
     * @inheritDoc
     */
    public HashMap<String, PerformanceData[]> getCompilerBenchmarks()
    {
        return compilerBenchmarks;
    }

    /**
     * @inheritDoc
     */
    public Benchmark getBenchmark()
    {
        return benchmark;
    }

    /**
     * @inheritDoc
     */
    public Configuration getConfiguration()
    {
        return oemConfiguration;
    }

    /**
     * @inheritDoc
     */
    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * @inheritDoc
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * @inheritDoc
     */
    public void setSupportedFileExtensions(String mimeType, String[] extensions)
    {
        mimeMappings.set(mimeType, extensions);
    }

    /**
     * Sets the output destination. This method is necessary if you use the <code>build(boolean)</code> method.
     * If you use the <code>build(OutputStream, boolean)</code> method, there is no need to use this method.
     *
     * @param output An instance of the <code>java.io.File</code> class.
     */
    public void setOutput(File output)
    {
        this.output = output;
    }

    /**
     * Gets the output destination. This method returns <code>null</code> if you did not call the
     * <code>setOutput()</code> method.
     *
     * @return An instance of the <code>java.io.File</code> class, or <code>null</code> if you did not
     * call the <code>setOutput()</code> method.
     */
    public File getOutput()
    {
        return output;
    }

    /**
     * Sets the RSL output directory.
     *
     * @param directory An RSL directory.
     */
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }

    /**
     * Gets the RSL output directory.
     *
     * @return A <code>java.io.File</code>, or <code>null</code> if you did not call the <code>setDirectory()</code> method.
     */
    public File getDirectory()
    {
        return directory;
    }

    /**
     * @inheritDoc
     */
    public void setProgressMeter(ProgressMeter meter)
    {
        this.meter = meter;
    }

    /**
     * @inheritDoc
     */
    public void setPathResolver(PathResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * @inheritDoc
     */
    // IMPORTANT: If you make changes here, you probably want to mirror them in Application.build()
    public long build(boolean incremental) throws IOException
    {
        // I know that directory is not referenced anywhere in here...
        // if you setDirectory but do not setOutput, then output==null but dirctory!=null
        // so this silly looking IF needs to be like this...
        if (output != null || directory != null)
        {
            long size = 0;

            //TODO PERFORMANCE: A lot of unnecessary recopying and buffering here
            try
            {
                @SuppressWarnings("unused")
                int result = compile(incremental);

                return size;
            }
            finally
            {                
                //runExtensions();
                
                clean(false /* cleanData */,
                      false /* cleanCache */,
                      false /* cleanOutput */,
                      true /* cleanConfig */,
                      false /* cleanMessages */,
                      true /* cleanThreadLocals */);
            }
        }
        else
        {
            return 0;
        }
    }

    /*
    private void runExtensions()
    {
        if (oemConfiguration != null)
        {
            Set<ILibraryExtension> extensions = ExtensionManager.getLibraryExtensions( oemConfiguration.getExtensions() );

            for ( ILibraryExtension extension : extensions )
            {
                if (ThreadLocalToolkit.errorCount() == 0)
                {
                    extension.run( this.clone(), oemConfiguration.clone() );
                }
            }
        }
    }
    */
    
    /**
     * @inheritDoc
     * 
     * Note: If the OutputStream is written to a File,
     * refreshLastModified() should be called to update the timestamp
     * in the SwcCache.  Otherwise, subsequent builds in this Project
     * will think the Library has been externally updated and will
     * force a reload.
     */
    public long build(OutputStream out, boolean incremental) throws IOException
    {
        try
        {
            @SuppressWarnings("unused")
            int result = compile(incremental);

            /*
            if (result == SKIP || result == LINK || result == OK)
            {
                return link(out);
            }
            else
            {
            */
                return 0;
            //}
        }
        finally
        {
            //runExtensions();
            
            clean(false /* cleanData */,
                  false /* cleanCache */,
                  false /* cleanOutput */,
                  true /* cleanConfig */,
                  false /* cleanMessages */,
                  true /* cleanThreadLocals */);
        }
    }

    /**
     * @inheritDoc
     */
    public void stop()
    {
        cc.stop();
    }

    /**
     * @inheritDoc
     */
    public void clean()
    {
        clean(true /* cleanData */,
              true /* cleanCache */,
              true /* cleanOutput */,
              true /* cleanConfig */,
              true /* cleanMessages */,
              true /* cleanThreadLocals */);
    }

    /**
     * @inheritDoc
     */
    public void load(InputStream in) throws IOException
    {
    }

    /**
     * @inheritDoc
     */
    public long save(OutputStream out) throws IOException
    {
        return 1;
    }

    /**
     * @inheritDoc
     */
    public Report getReport()
    {
        //OEMUtil.setupLocalizationManager();
        return new OEMReport(compiledSources,
                             null,
                             null,
                             sourceList,
                             configurationReport,
                             messages, files);
    }

    /**
     *
     * @param c
     * @return
     */
    private String[] constructCommandLine(OEMConfiguration localOEMConfiguration)
    {
        String[] commandLine = (localOEMConfiguration != null) ? localOEMConfiguration.getCompilerOptions() : 
                                                                 new String[0];
        
        if (output != null)
        {
            // add output parameter
            String[] outputCommandLine = new String[commandLine.length + 1];
            System.arraycopy(commandLine, 0, outputCommandLine, 0, commandLine.length);
            outputCommandLine[commandLine.length] = "-output=" + output.getAbsolutePath();
            commandLine = outputCommandLine;
        }
        
        // Translate "classes" into "-include-classes" so the CompcConfiguration can
        // properly validate the configuration.
        if (classes.size() > 0)
        {
            StringBuilder buffer = new StringBuilder("-include-classes=");

            for (Iterator<String> iter = classes.iterator(); iter.hasNext();)
            {
                String className = iter.next();
                buffer.append(className);
                if (iter.hasNext())
                {
                    buffer.append(",");
                }
            }
            
            String[] newCommandLine = new String[commandLine.length + 1];
            System.arraycopy(commandLine, 0, newCommandLine, 0, commandLine.length);
            newCommandLine[commandLine.length] = buffer.toString();
            
            return newCommandLine;
        }
        // Translate "sources" into "-include-sources" so the CompcConfiguration can
        // properly validate the configuration.
        if (sources.size() > 0)
        {
            StringBuilder buffer = new StringBuilder("-include-sources=");

            for (Iterator<VirtualFile> iter = sources.iterator(); iter.hasNext();)
            {
                String className = iter.next().getName();
                buffer.append(className);
                if (iter.hasNext())
                {
                    buffer.append(",");
                }
            }
            
            String[] newCommandLine = new String[commandLine.length + 1];
            System.arraycopy(commandLine, 0, newCommandLine, 0, commandLine.length);
            newCommandLine[commandLine.length] = buffer.toString();
            
            return newCommandLine;
        }

        return commandLine;
    }


    /**
     * Compiles the <code>Library</code>. This method does not link the <code>Library</code>.
     *
     * @param incremental If <code>true</code>, build incrementally; if <code>false</code>, rebuild.
     * @return  {@link Builder#OK} if this method call resulted in compilation of some/all parts of the application;
     *          {@link Builder#LINK} if this method call did not compile anything in the application but advise the caller to link again;
     *          {@link Builder#SKIP} if this method call did not compile anything in the application;
     *          {@link Builder#FAIL} if this method call encountered errors during compilation.
     */
    protected int compile(boolean incremental)
    {
        try 
        {
        messages.clear();

        // if there is no configuration, use the default... but don't populate this.configuration.
        OEMConfiguration tempOEMConfiguration;

        if (oemConfiguration == null)
        {
            tempOEMConfiguration = (OEMConfiguration) getDefaultConfiguration(true);
        }
        else
        {
            tempOEMConfiguration = OEMUtil.getLibraryConfiguration(constructCommandLine(oemConfiguration),
                                                                   oemConfiguration.keepLinkReport(),
                                                                   oemConfiguration.keepSizeReport(),
                                                                   OEMUtil.getLogger(logger, messages),
                                                                   resolver, mimeMappings);
        }

        // if c is null, which indicates problems, this method will return.
        if (tempOEMConfiguration == null)
        {
            clean(false /* cleanData */, false /* cleanCache */, false /* cleanOutput */);
            return FAIL;
        }
        else if (oemConfiguration != null && oemConfiguration.keepConfigurationReport())
        {
            configurationReport = OEMUtil.formatConfigurationBuffer(tempOEMConfiguration.cfgbuf);
        }

        if (oemConfiguration != null)
        {
            oemConfiguration.cfgbuf = tempOEMConfiguration.cfgbuf;
        }

        // add archive files to the link checksum
        for (Map.Entry<String, VirtualFile>entry : files.entrySet())
        {
            tempOEMConfiguration.cfgbuf.calculateLinkChecksum(entry.getKey(), entry.getValue().getLastModified());
        }            

        // initialize some ThreadLocal variables...
        cc.run();
        OEMUtil.init(OEMUtil.getLogger(logger, messages), mimeMappings, meter, resolver, cc);

        // if there is any problem getting the licenses, this method will return.
        //Map licenseMap = OEMUtil.getLicenseMap(tempOEMConfiguration.configuration);

        // if there are no SWC inputs, output an error and return -1
        VirtualFile[] includeLibs = (tempOEMConfiguration.configuration == null) ? null : tempOEMConfiguration.configuration.getCompilerConfiguration().getIncludeLibraries();
        if (sources.size() == 0 && classes.size() == 0 && namespaces.size() == 0 &&
            resourceBundles.size() == 0 && files.size() == 0 && stylesheets.size() == 0 &&
            (includeLibs == null || includeLibs.length == 0))
        {
            ThreadLocalToolkit.log(new ConfigurationException.NoSwcInputs( null, null, -1 ));
            clean(false /* cleanData */, false /* cleanCache */, false /* cleanOutput */);
            return FAIL;
        }


        CompilerConfiguration compilerConfig = tempOEMConfiguration.configuration.getCompilerConfiguration();
        compilerConfig.setMetadataExport(true);


        clean(true /* cleanData */,
              false /* cleanCache */,
              false /* cleanOutput */,
              true /* cleanConfig */,
              false /* cleanMessages */,
              false /* cleanThreadLocals */);
        COMPJSC compc = new COMPJSC();
        int returnValue = compc.mainNoExit(constructCommandLine(oemConfiguration), null, true);
        if (returnValue == 0 || returnValue == 2)
            returnValue = OK;
        else
            returnValue = FAIL;

        LibraryCompilerConfiguration acc = ((LibraryCompilerConfiguration)tempOEMConfiguration.configuration);
        VirtualFile[] sourcePaths = acc.getCompilerConfiguration().getSourcePath();

        compiledSources = new ArrayList<Source>();
        List<String> sourceFiles = compc.getSourceList();
        String mainFile = compc.getMainSource();
        VirtualFile mainVirtualFile = null;
        if (sourceFiles != null)
        {
	        for (String sourceFile : sourceFiles)
	        {
	            for (VirtualFile sourcePath : sourcePaths)
	            {
	                String pathName = sourcePath.getName();
	                if (sourceFile.indexOf(pathName) == 0)
	                {
	                    String relPath = sourceFile.substring(pathName.length());
	                    int lastSep = relPath.lastIndexOf(File.separator);
	                    String shortName = relPath.substring(lastSep + 1);
	                    relPath = relPath.substring(0, lastSep);
	                    boolean isRoot = sourceFile.equals(mainFile);
	                    Source source = new Source(sourcePath, relPath, shortName, null, false, isRoot);
	                    compiledSources.add(source);
	                    if (mainFile != null && pathName.equals(mainFile))
	                    	mainVirtualFile = sourcePath;
	                }
	            }
	        }
	        try {
				sourceList = new SourceList(new ArrayList<VirtualFile>(), sourcePaths, mainVirtualFile, new String[0]);
			} catch (CompilerException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
        }
        convertMessages(compc.getProblemQuery());
        
        clean(returnValue != OK, false, false);
        return returnValue;

        }
        finally
        {
            // clean thread locals
            OEMUtil.clean();
        }
    }
        
    
    public long link(OutputStream output)
    {
        try
        {
            FileInputStream inStream = new FileInputStream(this.output);
            byte[] b = new byte[(int) this.output.length()];
            inStream.read(b);
            output.write(b);
            inStream.close();
            return this.output.length();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     *
     * @param cleanData
     * @param cleanCache
     * @param cleanOutput
     */
    private void clean(boolean cleanData, boolean cleanCache, boolean cleanOutput)
    {
        clean(cleanData,
              cleanCache,
              cleanOutput,
              true /* cleanConfig */,
              false /* cleanMessages */,
              false /* cleanThreadLocals */);
    }

    /**
     *
     * @param cleanData
     * @param cleanCache
     * @param cleanOutput
     * @param cleanConfig
     * @param cleanMessages
     * @param cleanThreadLocals
     */
    private void clean(boolean cleanData, boolean cleanCache, boolean cleanOutput,
                       boolean cleanConfig, boolean cleanMessages, boolean cleanThreadLocals)
    {
        if (cleanThreadLocals)
        {
            OEMUtil.clean();
        }
        
        if (oemConfiguration != null && cleanConfig)
        {
            oemConfiguration.reset();
        }

        if (cleanData)
        {
            //data = null;
            configurationReport = null;
        }

        if (cleanCache)
        {
            if (cacheName != null)
            {
                File dead = FileUtil.openFile(cacheName);
                if (dead != null && dead.exists())
                {
                    dead.delete();
                }
                cacheName = null;
            }
        }

        if (cleanOutput)
        {
            if (output != null && output.exists())
            {
                output.delete();
            }
        }

        if (cleanMessages)
        {
            messages.clear();
        }
    }

    /**
     *
     * @param s1
     * @param s2
     * @return
    private <T> boolean isDifferent(Collection<T> s1, Collection<T> s2)
    {
        for (Iterator<T> i = s2.iterator(); i.hasNext(); )
        {
            if (!s1.contains(i.next()))
            {
                return true;
            }
        }

        return s1.size() > s2.size();
    }
     */

    
    /**
     * Returns the cache of sources in the source list and source
     * path.  After building this Application object, the cache may be
     * used to compile another Application object with common sources.
     *
     * @return The active cache. May be null.
     *
     * @since 4.5
     */
    public ApplicationCache getApplicationCache()
    {
        return applicationCache;
    }

    /**
     * Sets the cache for sources in the source list and source path.
     * After compiling an Application object, the cache may be reused
     * to build another Application object with common sources.
     *
     * @param applicationCache A reference to the application cache.
     *
     * @since 4.5
     */
    public void setApplicationCache(ApplicationCache applicationCache)
    {
        this.applicationCache = applicationCache;
    }

    // TODO: deprecate getSwcCache() and setSwcCache(), then add
    // getLibraryCache() and setLibraryCache().
    /**
     * Get the cache of swcs in the library path. After building this Application
     * object the cache may be saved and used to compile another Application object
     * that uses the same library path.
     *
     * @return The active cache. May be null.
     *
     * @since 3.0
     */
    public LibraryCache getSwcCache()
    {
        return libraryCache;
    }

    /**
     * Set the cache for swcs in the library path. After compiling an
     * Application object the cache may be reused to build another Application
     * object that uses the same library path.
     *
     * @param swcCache A reference to an allocated swc cache.
     *
     * @since 3.0
     */
    public void setSwcCache(LibraryCache libraryCache)
    {
        this.libraryCache = libraryCache;
    }

    @Override
    public Library clone()
    {
        Library clone;
        try
        {
            clone = (Library) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e ); //wont happen
        }
        clone.oemConfiguration = oemConfiguration.clone();
        return clone;
    }

    public void refreshLastModified()
    {
    }
    
    public void convertMessages(ProblemQuery pq)
    {
        List<ICompilerProblem> probs = pq.getProblems();
        for (ICompilerProblem prob : probs)
        {
            Class aClass = prob.getClass();
            Annotation[] annotations = aClass.getAnnotations();

            for(Annotation annotation : annotations){
                if(annotation instanceof DefaultSeverity){
                    DefaultSeverity myAnnotation = (DefaultSeverity) annotation;
                    CompilerProblemSeverity cps = myAnnotation.value();
                    String level;
                    if (cps.equals(CompilerProblemSeverity.ERROR))
                        level = Message.ERROR;
                    else if (cps.equals(CompilerProblemSeverity.WARNING))
                        level = Message.WARNING;
                    else
                        break; // skip if IGNORE?
                    CompilerMessage msg = new CompilerMessage(level, 
                                                    prob.getSourcePath(), 
                                                    prob.getLine() + 1, 
                                                    prob.getColumn());
                    try
                    {
                        String errText = ProblemFormatter.DEFAULT_FORMATTER.format(prob);
                        msg.setMessage(errText);
                    }
                    catch (IllegalArgumentException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    catch (SecurityException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    
                    messages.add(msg);
                    try
                    {
                        logger.log(msg, aClass.getField("errorCode").getInt(null), prob.getSourcePath());
                    }
                    catch (IllegalArgumentException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (SecurityException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (NoSuchFieldException e)
                    {
                        try
                        {
                            logger.log(msg, aClass.getField("warningCode").getInt(null), prob.getSourcePath());
                        }
                        catch (IllegalArgumentException e1)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (SecurityException e1)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (IllegalAccessException e1)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (NoSuchFieldException e1)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            
        }

    }
}
