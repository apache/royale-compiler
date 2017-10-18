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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.types.RGB;

import flash.swf.tags.SetBackgroundColor;
import flex2.compiler.CompilerException;
import flex2.compiler.Source;
import flex2.compiler.SourceList;
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
import flex2.tools.ToolsConfiguration;
import flex2.tools.oem.internal.ApplicationCompilerConfiguration;
import flex2.tools.oem.internal.OEMConfiguration;
import flex2.tools.oem.internal.OEMReport;
import flex2.tools.oem.internal.OEMUtil;

/**
 * The <code>Application</code> class represents a Flex application. It implements the <code>Builder</code> interface
 * which allows for building the application incrementally. There are many ways to define
 * a Flex application. The most common way is specify the location of the target source file
 * on disk:
 *
 * <pre>
 * Application app = new Application(new File("MyApp.mxml"));
 * </pre>
 *
 * Before the <code>Application</code> object starts the compilation, it must be configured. The most common methods that the client
 * calls are <code>setLogger()</code>, <code>setConfiguration()</code>, and <code>setOutput()</code>.
 *
 * A logger must implement <code>flex2.tools.oem.Logger</code> and use the implementation as the Logger
 * for the compilation. The following is an example <code>Logger</code> implementation:
 *
 * <pre>
 * app.setLogger(new flex2.tools.oem.Logger()
 * {
 *     public void log(Message message, int errorCode, String source)
 *     {
 *         System.out.println(message);
 *     }
 * });
 * </pre>
 *
 * To specify compiler options for the <code>Application</code> object, the client
 * must get a <code>Configuration</code> object populated with default values. Then, the client can set
 * compiler options programmatically.
 *
 * The <code>setOutput()</code> method lets clients specify where the <code>Application</code> object should write
 * the output to. If you call the <code>setOutput()</code> method, the <code>build(boolean)</code> method builds and
 * writes directly to the location specified by the <code>setOutput()</code> method. For example:
 *
 * <pre>
 * app.setOutput(new File("MyApp.swf"));
 * app.build(true);
 * </pre>
 *
 * If you do not call the <code>setOutput()</code> method, the client can use the <code>build(OutputStream, boolean)</code> method
 * which requires the client to provide a buffered output stream. For example:
 *
 * <pre>
 * app.build(new BufferedOutputStream(new FileOutputStream("MyApp.swf")), true);
 * </pre>
 *
 * Before the <code>Application</code> object is thrown away, it is possible to save the compilation
 * data for reuse by using the <code>save(OutputStream)</code> method. Subsequent compilations can use the
 * <code>load(OutputStream)</code> method to get the old data into the <code>Application</code> object.
 *
 * <pre>
 * app.save(new BufferedOutputStream(new FileOutputStream("MyApp.incr")));
 * </pre>
 *
 * When a cache file (such as <code>MyApp.incr</code>) is available from a previous compilation, the client can
 * call the <code>load(OutputStream)</code> method before calling the <code>build(boolean)</code> method. For example:
 *
 * <pre>
 * app.load(new BufferedInputStream(FileInputStream("MyApp.incr")));
 * app.build();
 * </pre>
 *
 * The <code>build(false)</code> and <code>build(OutputStream, false)</code> methods always rebuild the application. If the <code>Application</code>
 * object is new, the first <code>build(true)/build(OutputStream, true)</code> method call performs a full build, which
 * is equivalent to <code>build(false)/build(OutputStream, false)</code>, respectively. After a call to the <code>clean()</code> method,
 * the <code>Application</code> object always performs a full build.
 *
 * <p>
 * The <code>clean()</code> method not only cleans up compilation data in the <code>Application</code> object, but also the output
 * file if the <code>setOutput()</code> method was called.
 *
 * <p>
 * The <code>Application</code> class also supports building applications from a combination of source
 * files from the file system and in-memory, dynamically-generated source objects. The client
 * must use the <code>Application(String, VirtualLocalFile)</code> or <code>Application(String, VirtualLocalFile[])</code> constructors.
 *
 * <p>
 * The <code>Application</code> class can be part of a <code>Project</code>. For more information, see the <code>Project</code> class's description.
 *
 * @see flex2.tools.oem.Configuration
 * @see flex2.tools.oem.Project
 * @version 2.0.1
 * @author Clement Wong
 */
public class Application implements Builder
{
    static
    {
        // find all the compiler temp files.
        File[] list = null;
        try
        {
            File tempDir = File.createTempFile("Flex2_", "").getParentFile();
            list = tempDir.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.startsWith("Flex2_");
                }
            });
        }
        catch (Exception e)
        {
        }

        // get rid of compiler temp files.
        for (int i = 0, len = list == null ? 0 : list.length; i < len; i++)
        {
            try { list[i].delete(); } catch (Exception t) {}
        }

        // use the protection domain to find the location of flex-compiler-oem.jar.
        URL url = Application.class.getProtectionDomain().getCodeSource().getLocation();
        try
        {
            File f = new File(new URI(url.toExternalForm()));
            if (f.getAbsolutePath().endsWith("flex-compiler-oem.jar"))
            {
                // use the location of flex-compiler-oem.jar to set application.home
                // assume that the jar file is in <application.home>/lib/flex-compiler-oem.jar
                String applicationHome = f.getParentFile().getParent();
                System.setProperty("application.home", applicationHome);
            }
        }
        catch (URISyntaxException ex)
        {
        }
        catch (IllegalArgumentException ex)
        {
        }
    }

    /**
     * Constructor.
     *
     * @param file The target source file.
     * @throws FileNotFoundException Thrown when the specified source file does not exist.
     */
    public Application(File file) throws FileNotFoundException
    {
        this(file, null);
    }

    /**
     * Constructor.
     *
     * @param file The target source file.
     * @param libraryCache A reference to a LibraryCache object. After
     *        building this Application object the cache may be saved
     *        and used to compile another Application object that uses
     *        a similar library path.
     * @throws FileNotFoundException Thrown when the specified source file does not exist.
     * @since 3.0
     */
    public Application(File file, LibraryCache libraryCache) throws FileNotFoundException
    {
        if (file.exists())
        {
            init(new VirtualFile[] { new LocalFile(FileUtil.getCanonicalFile(file)) });
        }
        else
        {
            throw new FileNotFoundException(FileUtil.getCanonicalPath(file));
        }

        this.libraryCache = libraryCache;
    }

    /**
     * Constructor.
     *
     * @param file An in-memory source object.
     */
    public Application(VirtualLocalFile file)
    {
        init(new VirtualFile[] { file });
    }

    /**
     * Constructor.
     *
     * @param files An array of in-memory source objects. The last element in the array is the target source object.
     */
    public Application(VirtualLocalFile[] files)
    {
        init(files);
    }

    /**
     * Constructor.  Use to build resource modules which don't have a target
     * source file.
     *
     */
    public Application()
    {
         init(new VirtualFile[0]);
    }

    /**
     *
     * @param files
     */
    @SuppressWarnings("unchecked")
    private void init(VirtualFile[] files)
    {        
        this.files = new ArrayList(files.length);
        for (int i = 0, length = files.length; i < length; i++)
        {
            this.files.add(files[i]);
        }
        oemConfiguration = null;
        logger = null;
        output = null;
        mimeMappings = new MimeMappings();
        meter = null;
        resolver = null;
        cc = new CompilerControl();
        //isGeneratedTargetFile = false;

        //data = null;
        cacheName = null;
        configurationReport = null;
        messages = new ArrayList<Message>();
    }

    private List<VirtualFile> files;
    private OEMConfiguration oemConfiguration;
    private Logger logger;
    private File output;
    private MimeMappings mimeMappings;
    private ProgressMeter meter;
    protected PathResolver resolver;
    private CompilerControl cc;
    //private boolean isGeneratedTargetFile;
    private ApplicationCache applicationCache;
    private LibraryCache libraryCache;

    // clean() would null out the following variables.
    private String cacheName, configurationReport;
    private List<Message> messages;
    private boolean setOutputCalled;
    private int loaded = 0;

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
        return OEMUtil.getApplicationConfiguration(constructCommandLine(null), false, false,
                                                   OEMUtil.getLogger(logger, messages), resolver,
                                                   mimeMappings, processDefaults);
    }

    /**
     * @inheritDoc
     */
    public Map<String, PerformanceData[]> getCompilerBenchmarks()
    {
        return null;
    }

    /**
     * @inheritDoc
     */
    public Benchmark getBenchmark()
    {
        return null;
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
     * Sets the location of the compiler's output. This method is necessary if you call the <code>build(boolean)</code> method.
     * If you use the <code>build(OutputStream, boolean)</code> method, in which an output stream
     * is provided, there is no need to use this method.
     *
     * @param output An instance of the <code>java.io.File</code> class.
     */
    public void setOutput(File output)
    {
    	setOutputCalled = true;
        this.output = output;
    }

    /**
     * Gets the output destination. This method returns <code>null</code> if the <code>setOutput()</code> method was not called.
     *
     * @return An instance of the <code>java.io.File</code> class, or <code>null</code> if you did not call the <code>setOutput()</code> method.
     */
    public File getOutput()
    {
        return output;
    }

    /**
     * @inheritDoc
     */
    public void setSupportedFileExtensions(String mimeType, String[] extensions)
    {
        mimeMappings.set(mimeType, extensions);
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
     * @since 3.0
     */
    public void setPathResolver(PathResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * @inheritDoc
     */
    // IMPORTANT: If you make changes here, you probably want to mirror them in Library.build()
    public long build(boolean incremental) throws IOException
    {
        if (output != null)
        {
            InputStream tempIn = null;
            ByteArrayOutputStream tempOut = null;
            OutputStream out = null;
            long size = 0;

            //TODO PERFORMANCE: A lot of unnecessary recopying and buffering here
            try
            {
                @SuppressWarnings("unused")
                int result = compile(incremental);

                return size;
            }
            catch (Exception e)
            {
                ThreadLocalToolkit.logError(e.getLocalizedMessage());
                return 0;
            }
            finally
            {
                if (tempIn != null) { try { tempIn.close(); } catch (Exception ex) {} }
                if (tempOut != null) { try { tempOut.close(); } catch (Exception ex) {} }
                if (out != null) { try { out.close(); } catch (Exception ex) {} }

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
            Set<IApplicationExtension> extensions = ExtensionManager.getApplicationExtensions(oemConfiguration.getExtensions());

            for ( IApplicationExtension extension : extensions )
            {
                if (ThreadLocalToolkit.errorCount() == 0)
                {
                    extension.run( (Configuration) oemConfiguration.clone() );
                }
            }
        }
    }
    */
    
    /**
     * @inheritDoc
     */
    public long build(OutputStream out, boolean incremental) throws IOException
    {
        try
        {
            @SuppressWarnings("unused")
            int result = compile(incremental);
            /*
            if (result == OK || result == LINK)
            {
                runExtensions();
                return link(out);
            }
            else if (result == SKIP)
            {
                runExtensions();
                return encode(out);
            }
            else
            {
            */
                return 0;
           // }
        }
        finally
        {
            
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
    	// assuming FB takes care of deleting bin-release and bin-debug, we want to delete bin.
    	// but this also gets called when quitting FB.
    	setOutputCalled = false;
    }

    /**
     * @inheritDoc
     */
    public void load(InputStream in) throws IOException
    {
    	loaded++;
    }

    /**
     * @inheritDoc
     */
    public long save(OutputStream out) throws IOException
    {
    	loaded--;
        return 1;
    }

    /**
     * @inheritDoc
     */
    public Report getReport()
    {
        //OEMUtil.setupLocalizationManager();
        return new OEMReport( sources,
                              movie,
                              null, 
                              sourceList,
                             configurationReport,
                             messages);
    }


    /**
     * Compiles the <code>Application</code> object. This method does not link the <code>Application</code>.
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
                tempOEMConfiguration = OEMUtil.getApplicationConfiguration(constructCommandLine(oemConfiguration),
                                                                           oemConfiguration.keepLinkReport(),
                                                                           oemConfiguration.keepSizeReport(),
                                                                           OEMUtil.getLogger(logger, messages),
                                                                           resolver, mimeMappings);
            }
    
            // if c is null, which indicates problems, this method will return.
            if (tempOEMConfiguration == null)
            {
                clean(false /* cleanData */,
                      false /* cleanCache */,
                      false /* cleanOutput */,
                      true /* cleanConfig */,
                      false /* cleanMessages */,
                      false /* cleanThreadLocals */);
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
        
            // initialize some ThreadLocal variables...
            cc.run();
            OEMUtil.init(OEMUtil.getLogger(logger, messages), mimeMappings, meter, resolver, cc);
    
            //Map licenseMap = OEMUtil.getLicenseMap(tempOEMConfiguration.configuration);
    
            mxmljsc = new MXMLJSC();
            mxmljsc.noLink = true;
            //int returnValue = mxmlc.mainCompileOnly(constructCommandLine2(tempOEMConfiguration.configuration), null);
            int returnValue = mxmljsc.mainNoExit(constructCommandLine(oemConfiguration), null, true);
            if (returnValue == 0 || returnValue == 2)
                returnValue = OK;
            else
                returnValue = FAIL;
            
            processMXMLCReport(mxmljsc, tempOEMConfiguration);
            
            clean(returnValue == FAIL /* cleanData */,
                  false /* cleanCache */,
                  false /* cleanOutput */,
                  true /* cleanConfig */,
                  false /* cleanMessages */,
                  false /* cleanThreadLocals */);
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
        return mxmljsc.writeSWF(output);
    }
    
    private MXMLJSC mxmljsc = new MXMLJSC();
    private List<Source> sources;
    private SimpleMovie movie;
    private SourceList sourceList;
    
    void processMXMLCReport(MXMLJSC mxmljsc, OEMConfiguration config)
    {
        ApplicationCompilerConfiguration acc = ((ApplicationCompilerConfiguration)config.configuration);
        sources = new ArrayList<Source>();
        VirtualFile[] sourcePaths = acc.getCompilerConfiguration().getSourcePath();

        List<String> sourceFiles = mxmljsc.getSourceList();
        String mainFile = mxmljsc.getMainSource();
        VirtualFile mainVirtualFile = null;
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
                    sources.add(source);
                    if (pathName.equals(mainFile))
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
        ProblemQuery pq = mxmljsc.getProblemQuery();
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
        ISWF swf = mxmljsc.getSWFTarget();
        movie = new SimpleMovie(null);
        org.apache.royale.swf.types.Rect r = swf.getFrameSize();
        flash.swf.types.Rect fr = new flash.swf.types.Rect();
        fr.xMin = r.xMin();
        fr.yMin = r.yMin();
        fr.xMax = r.xMax();
        fr.yMax = r.yMax();
        movie.size = fr;
        RGB bg = swf.getBackgroundColor();
        int red = bg.getRed();
        red = red << 16;
        int green = bg.getGreen();
        green = green << 8;
        int blue = bg.getBlue();
        movie.bgcolor = new SetBackgroundColor(red + green + blue);
        movie.topLevelClass = swf.getTopLevelClass();
        
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
     * @param localOEMConfiguration
     * @return
     */
    private String[] constructCommandLine(OEMConfiguration localOEMConfiguration)
    {
        String[] options = (localOEMConfiguration != null) ? localOEMConfiguration.getCompilerOptions() : new String[0];
        String[] args = new String[options.length + files.size() + 1];
        System.arraycopy(options, 0, args, 0, options.length);
        args[options.length] = "--file-specs";// + Mxmlc.FILE_SPECS;
        for (int i = 0, size = files.size(); i < size; i++)
        {
            args[options.length + 1 + i] = files.get(i).getName();
        }

        return args;
    }

    /**
    *
    * @param localOEMConfiguration
    * @return
    */
   private String[] constructCommandLine2(ToolsConfiguration localToolsConfiguration)
   {
       String[] options = (localToolsConfiguration != null) ? processToolsConfig(localToolsConfiguration): new String[0];
       String[] args = new String[options.length + files.size() + 1];
       System.arraycopy(options, 0, args, 0, options.length);
       args[options.length] = "--file-specs";// + Mxmlc.FILE_SPECS;
       for (int i = 0, size = files.size(); i < size; i++)
       {
           args[options.length + 1 + i] = files.get(i).getName();
       }

       return args;
   }
   
   private String[] processToolsConfig(ToolsConfiguration tc)
   {
       String[] results = new String[1];
       results[0] = "-debug=" + (tc.debug() ? "true" : "false");
       return results;
   }
   
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
}
