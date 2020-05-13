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

package org.apache.royale.compiler.config;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.*;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.config.*;
import org.apache.royale.compiler.internal.config.annotations.*;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.problems.*;
import org.apache.royale.swc.catalog.XMLFormatter;
import org.apache.royale.utils.FileUtils;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The model for all the configuration options supported by the compiler. Each option is stored in a private field. It
 * usually has a getter method, a setter method and a configuration method (started with "cfg"). The configuration
 * methods are used by the configurators to set the values of a given option name.
 * <p>
 * This class is currently being reviewed and refactored. See CMP-471 as the master bug for the tasks.
 * <p>
 * Steps to refactor:
 * <ol>
 * <li>Check "mxmlc" manual (http://adobe.ly/bffN8A) and copy documents to Javadoc on setters of options.</li>
 * <li>Check "configuration support" WIKI (http://bit.ly/oAI8gj) to see if an option should be deprecated.</li>
 * <li>Merge {@code cfgXXX} to {@code setXXX}, and annotate the setter.</li>
 * <li>Remove {@code getXXXInfo()} static method.</li>
 * </ol>
 * 
 * @see "http://bugs.adobe.com/jira/browse/CMP-471"
 */
public class Configuration
{
    private static final int DEFAULT_HEIGHT_MAX = 4096;
    private static final int DEFAULT_HEIGHT_MIN = 1;
    private static final int DEFAULT_WIDTH_MAX = 4096;
    private static final int DEFAULT_WIDTH_MIN = 1;

    public static final String DEFAULT_OUTPUT_DIRECTORY_TOKEN = "org.apache.royale.default.output.directory";

    public static final String SWC_AIRGLOBAL = "airglobal.swc";

    /**
     * Singleton empty string list. All getters returning a list of string should return this object when the field is
     * null.
     */
    private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();

    private static final List<String> compcOnlyOptions = new ArrayList<String>(9);

    static
    {
        compcOnlyOptions.add("directory");
        compcOnlyOptions.add("include-classes");
        compcOnlyOptions.add("include-file");
        compcOnlyOptions.add("include-lookup-only");
        compcOnlyOptions.add("include-namespaces");
        compcOnlyOptions.add("include-sources");
        compcOnlyOptions.add("include-stylesheet");
        compcOnlyOptions.add("include-inheritance-dependencies-only");
    }

    /**
     * Validate configuration options values.
     * 
     * @param configurationBuffer Configuration buffer.
     * @throws ConfigurationException Error.
     */
    public void validate(ConfigurationBuffer configurationBuffer) throws ConfigurationException
    {
        // process the merged configuration buffer. right, can't just process the args.
        processDeprecatedAndRemovedOptions(configurationBuffer);

        validateDumpConfig(configurationBuffer);
    }

    private String[] removeNativeJSLibrariesIfNeeded(String[] libraryPaths)
    {
        List<String> libraryPathList = new ArrayList<String>(Arrays.asList(libraryPaths));
        String appHome = System.getProperty("application.home");
        if (appHome == null)
            return libraryPaths;
        appHome = appHome.replace("\\", "/");

        if (isExcludeNativeJSLibraries)
        {
            Iterator<String> pathIterator = libraryPathList.iterator();

            while (pathIterator.hasNext())
            {
                final String path = pathIterator.next().replace("\\", "/");
                final boolean isNativeJS = path.contains(appHome + "/js/libs")
                        && path.lastIndexOf(".swc") == path.length() - ".swc".length();

                if (isNativeJS)
                {
                    pathIterator.remove();
                }
            }
        }

        return libraryPathList.toArray(new String[libraryPathList.size()]);
    }

    /**
     * Validate that no compc-only options are used in a given configuration buffer. Use this method to verify no
     * compc-only args are used in mxmlc.
     * 
     * @param configurationBuffer the configuration buffer to check for compc-only options.
     * 
     * @throws ConfigurationException if a compc-only option is found in the configuration buffer.
     */
    public static void validateNoCompcOnlyOptions(ConfigurationBuffer configurationBuffer) throws ConfigurationException
    {
        for (String option : compcOnlyOptions)
        {
            List<ConfigurationValue> values = configurationBuffer.getVar(option);
            if (values != null && values.size() > 0)
                throw new ConfigurationException.UnknownVariable(values.get(0).getVar(), values.get(0).getSource(),
                        values.get(0).getLine());
        }
    }

    /**
     * The path of a given file name based on the context of the configuration value or the default output directory
     * token.
     * 
     * @param cv
     * @param fileName
     * @return the full path of the file.
     */
    protected String getOutputPath(ConfigurationValue cv, String fileName)
    {
        String result = fileName;

        if (fileName != null)
        {
            File file = new File(fileName);
            if (!FileUtils.isAbsolute(file))
            {
                String directory = cv.getBuffer().getToken(DEFAULT_OUTPUT_DIRECTORY_TOKEN);

                // if no default output directory, then use the configuration context.
                if (directory == null)
                {
                    directory = cv.getContext();
                }

                if (directory != null)
                {
                    result = FileUtils.addPathComponents(directory, fileName, File.separatorChar);
                }
            }
        }

        return pathResolver.resolve(result).getAbsolutePath();
    }

    private static Map<String, String> aliases = null;

    public static Map<String, String> getAliases()
    {
        if (aliases == null)
        {
            aliases = new HashMap<String, String>();

            aliases.put("l", "compiler.library-path");
            aliases.put("el", "compiler.external-library-path");
            aliases.put("fb", "use-flashbuilder-project-files");
            aliases.put("is", "include-sources");
            aliases.put("sp", "compiler.source-path");
            aliases.put("rsl", "runtime-shared-libraries");
            aliases.put("keep", "compiler.keep-generated-actionscript");
            aliases.put("o", "output");
            aliases.put("rslp", "runtime-shared-library-path");
            aliases.put("static-rsls", "static-link-runtime-shared-libraries");
        }
        return aliases;
    }

    //
    // PathResolver
    //
    private IPathResolver pathResolver;

    /**
     * Set a path resolver to resolver files relative to a configuration. Files inside of configuration files are
     * resolved relative to those configuration files and files on the command line are resolved relative to the root
     * directory of the compile.
     * 
     * @param pathResolver a path resolver for this configuration. May not be null.
     */
    public void setPathResolver(IPathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }

    //
    // mainDefinition
    //
    private String mainDefinition;

    /**
     * Main definition is the root class of a SWF. {@code mxmlc} only takes one file in the source list. The main
     * definition name is the main source file name.
     * 
     * @return main definition
     */
    public String getMainDefinition()
    {
        return mainDefinition;
    }

    public void setMainDefinition(String mainDefinition)
    {
        assert mainDefinition != null : "main definition can't be null";
        assert!"".equals(mainDefinition) : "main definition can't be empty";

        this.mainDefinition = mainDefinition;
    }

    //
    // 'benchmark' option
    //
    @Config(removed = true)
    @Mapping("benchmark")
    public void setBenchmark(ConfigurationValue cv, boolean b)
    {
    }

    //
    // 'debug-password' option
    //

    private String debugPassword;

    /**
     * Lets you engage in remote debugging sessions with the Flash IDE.
     */
    public String getDebugPassword()
    {
        return debugPassword;
    }

    @Config(advanced = true)
    @Mapping("debug-password")
    @DefaultArgumentValue("")
    public void setDebugPassword(ConfigurationValue cv, String debugPassword)
    {
        this.debugPassword = debugPassword;
    }

    //
    // 'default-background-color' option
    //

    private int backgroundColor = 0x50727E;

    public int getDefaultBackgroundColor()
    {
        return this.backgroundColor;
    }

    @Config(advanced = true)
    @Mapping("default-background-color")
    public void setDefaultBackgroundColor(ConfigurationValue cv, int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    //
    // 'default-frame-rate' option
    //

    private int frameRate = 24;

    public int getDefaultFrameRate()
    {
        return frameRate;
    }

    @Config(advanced = true)
    @Mapping("default-frame-rate")
    public void setDefaultFrameRate(ConfigurationValue cv, int rate) throws ConfigurationException
    {
        if (rate <= 0)
            throw new ConfigurationException.GreaterThanZero(cv.getVar(), cv.getSource(), cv.getLine());
        frameRate = rate;
    }

    //
    // 'default-script-limits' option
    //

    private int scriptLimit = 60;
    private int scriptRecursionLimit = 1000;
    private boolean scriptLimitsSet = false;

    public int getScriptTimeLimit()
    {
        return scriptLimit;
    }

    public int getScriptRecursionLimit()
    {
        return scriptRecursionLimit;
    }

    public boolean scriptLimitsSet()
    {
        return scriptLimitsSet;
    }

    @Config(advanced = true)
    @Mapping("default-script-limits")
    @Arguments({ "max-recursion-depth", "max-execution-time" })
    public void setDefaultScriptLimits(ConfigurationValue cv, int scriptLimit, int scriptRecursionLimit)
            throws ConfigurationException
    {
        if (scriptLimit <= 0)
            throw new ConfigurationException.GreaterThanZero(cv.getVar(), cv.getSource(), cv.getLine());

        if (scriptRecursionLimit <= 0)
            throw new ConfigurationException.GreaterThanZero(cv.getVar(), cv.getSource(), cv.getLine());

        this.scriptLimitsSet = true;
        this.scriptLimit = scriptRecursionLimit;
        this.scriptRecursionLimit = scriptLimit;
    }

    //
    // 'default-size' option
    //

    private int defaultWidth = 500;
    private int defaultHeight = 375;

    public int getDefaultWidth()
    {
        return defaultWidth;
    }

    public int getDefaultHeight()
    {
        return defaultHeight;
    }

    @Config(advanced = true)
    @Arguments({ "width", "height" })
    @Mapping("default-size")
    public void setDefaultSize(ConfigurationValue cv, int width, int height) throws ConfigurationException
    {
        if (width < DEFAULT_WIDTH_MIN || width > DEFAULT_WIDTH_MAX || height < DEFAULT_HEIGHT_MIN
                || height > DEFAULT_HEIGHT_MAX)
            throw new ConfigurationException.IllegalDimensions(width, height, cv.getVar(), cv.getSource(),
                    cv.getLine());

        this.defaultWidth = width;
        this.defaultHeight = height;
    }

    //
    // 'externs' option
    //

    private final Set<String> externs = new LinkedHashSet<String>();

    public Set<String> getExterns()
    {
        return externs;
    }

    /**
     * Sets a list of classes to exclude from linking when compiling a SWF file. This option provides compile-time link
     * checking for external references that are dynamically linked.
     */
    @Config(advanced = true, allowMultiple = true)
    @Mapping("externs")
    @Arguments("symbol")
    @InfiniteArguments
    public void setExterns(ConfigurationValue cfgval, List<String> vals)
    {
        externs.addAll(QNameNormalization.normalize(vals));
    }

    //
    // 'includes' option
    //

    private final Set<String> includes = new LinkedHashSet<String>();

    public Set<String> getIncludes()
    {
        return includes;
    }

    /**
     * Links one or more classes to the resulting application SWF file, whether or not those classes are required at
     * compile time. To link an entire SWC file rather than individual classes, use the include-libraries option.
     */
    @Config(allowMultiple = true, advanced = true)
    @Mapping("includes")
    @Arguments("symbol")
    @InfiniteArguments
    public void setIncludes(ConfigurationValue cfgval, List<String> vals)
    {
        includes.addAll(QNameNormalization.normalize(vals));
    }

    //
    // 'framework' option
    //
    //
    @Config(allowMultiple = true, advanced = true, removed = true)
    @Mapping("framework")
    public void setFramework(ConfigurationValue cfgval, String value)
    {
    }

    // 'link-report' option
    //

    private String linkReportFileName = null;

    public File getLinkReport()
    {
        return linkReportFileName != null ? new File(linkReportFileName) : null;
    }

    /**
     * Prints linking information to the specified output file. This file is an XML file that contains {@code def} tags,
     * {@code pre} tags and {@code ext} tags showing linker dependencies in the final SWF file. The file format output
     * by this command can be used to write a file for input to the {@code load-externs} option.
     */
    @Config(advanced = true)
    @Mapping("link-report")
    @Arguments("filename")
    public void setLinkReport(ConfigurationValue cv, String filename)
    {
        this.linkReportFileName = getOutputPath(cv, filename);
    }

    //
    // 'size-report' option
    //

    private String sizeReportFileName = null;

    public File getSizeReport()
    {
        return sizeReportFileName != null ? new File(sizeReportFileName) : null;
    }

    @Config(advanced = true)
    @Mapping("size-report")
    @Arguments("filename")
    public void setSizeReport(ConfigurationValue cv, String filename)
    {
        this.sizeReportFileName = getOutputPath(cv, filename);
    }

    // 'api-report' option
    //

    private String apiReportFileName = null;

    public File getApiReport()
    {
        return apiReportFileName != null ? new File(apiReportFileName) : null;
    }

    /**
     * Prints api usage information to the specified output file. This file is an text file that contains every method and property
     * resolved by the compiler.
     */
    @Config(advanced = true)
    @Mapping("api-report")
    @Arguments("filename")
    public void setApiReport(ConfigurationValue cv, String filename)
    {
        this.apiReportFileName = getOutputPath(cv, filename);
    }

    //
    // 'load-externs' option
    //

    /**
     * Specifies the location of an XML file that contains def, pre, and ext symbols to omit from linking when compiling
     * a SWF file. The XML file uses the same syntax as the one produced by the link-report option.
     * <p>
     * This option provides compile-time link checking for external components that are dynamically linked.
     */
    @Config(allowMultiple = true, advanced = true)
    @Mapping("load-externs")
    @Arguments("filename")
    public void setLoadExterns(ConfigurationValue cfgval, String filename) throws ConfigurationException
    {
        final String path = resolvePathStrict(filename, cfgval);
        final FileSpecification f = new FileSpecification(path);
        final List<String> externsFromFile = LoadExternsParser.collectExterns(cfgval, f);
        externs.addAll(externsFromFile);
    }

    //
    // 'raw-metadata' option
    //

    private String metadata = null;

    public String getRawMetadata()
    {
        if (metadata != null)
            return metadata;

        return generateMetadata();
    }

    private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String DC_URI = "http://purl.org/dc/elements/1.1";

    /**
     * @return Metadata XML string.
     */
    private final String generateMetadata()
    {
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        assert xmlOutputFactory != null : "Expect XMLOutputFactory implementation.";
        final StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xmlWriter = null;

        try
        {
            xmlWriter = new XMLFormatter(xmlOutputFactory.createXMLStreamWriter(stringWriter));
            xmlWriter.writeStartDocument();

            xmlWriter.writeStartElement("rdf", "RDF", RDF_URI);
            xmlWriter.setPrefix("rdf", RDF_URI);
            xmlWriter.writeNamespace("rdf", RDF_URI);

            // write rdf:Description
            xmlWriter.writeStartElement(RDF_URI, "Description");
            xmlWriter.setPrefix("dc", DC_URI);
            xmlWriter.setPrefix(VersionInfo.COMPILER_NAMESPACE_PREFIX, VersionInfo.COMPILER_NAMESPACE_URI);
            xmlWriter.writeNamespace("dc", DC_URI);
            xmlWriter.writeNamespace(VersionInfo.COMPILER_NAMESPACE_PREFIX, VersionInfo.COMPILER_NAMESPACE_URI);

            // write dc:format
            xmlWriter.writeStartElement(DC_URI, "format");
            xmlWriter.writeCharacters("application/x-shockwave-flash");
            xmlWriter.writeEndElement();

            if (isRoyale())
            {
                // write localizedTitles
                writeMap(xmlWriter, DC_URI, "description", localizedDescriptions);

                // write localizedDescription
                writeMap(xmlWriter, DC_URI, "title", localizedTitles);

                // write publisher
                writeCollection(xmlWriter, DC_URI, "publisher", publishers);

                // write creators
                writeCollection(xmlWriter, DC_URI, "creator", creators);

                // write contributor
                writeCollection(xmlWriter, DC_URI, "contributor", contributors);

                // write language
                writeCollection(xmlWriter, DC_URI, "language", langs);

                // write date
                writeDate(xmlWriter);
            }

            // write compiledBy
            writeCompiledBy(xmlWriter);

            // write
            xmlWriter.writeEndElement(); // Description
            xmlWriter.writeEndDocument();
        }
        catch (XMLStreamException e)
        {
            return "";
        }

        return stringWriter.toString();
    }

    /**
     * Write information about the compiler that compiled the swf.
     * 
     * @param writer
     * @throws XMLStreamException
     */
    private void writeCompiledBy(XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeEmptyElement(VersionInfo.COMPILER_NAMESPACE_URI, VersionInfo.COMPILER_ELEMENT);
        writer.writeAttribute(VersionInfo.COMPILER_NAME_ATTRIBUTE, VersionInfo.getCompilerName());
        writer.writeAttribute(VersionInfo.COMPILER_VERSION_ATTRIBUTE, VersionInfo.getCompilerVersion());
        writer.writeAttribute(VersionInfo.COMPILER_BUILD_ATTRIBUTE, VersionInfo.getCompilerBuild());

        writer.writeEndElement(); // compiledBy
    }

    /**
     * Write the data to rdf/xml
     * 
     * @param writer
     * @throws XMLStreamException
     */
    private void writeDate(XMLStreamWriter writer) throws XMLStreamException
    {
        if (date == null)
        {
            date = DateFormat.getDateInstance().format(new Date());
        }

        writer.writeStartElement(DC_URI, "date");
        writer.writeCharacters(date);
        writer.writeEndElement();
    }

    /**
     * Write a map of values to rdf/xml
     * 
     * @param writer
     * @param namespaceURI
     * @param localName
     * @param mapData
     * @throws XMLStreamException
     */
    private void writeMap(XMLStreamWriter writer, String namespaceURI, String localName, Map<String, String> mapData)
            throws XMLStreamException
    {
        if (mapData.size() > 0)
        {
            writer.writeStartElement(namespaceURI, localName);
            if ((mapData.size() == 1) && (mapData.get("x-default") != null))
            {
                String data = mapData.get("x-default");
                writer.writeCharacters(data);
            }
            else
            {
                writer.writeStartElement(RDF_URI, "Alt");
                for (final String key : mapData.keySet())
                {
                    final String value = mapData.get(key);
                    writer.writeStartElement(RDF_URI, "li");
                    writer.writeAttribute("xml", "", "lang", key);
                    writer.writeCharacters(value);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

    }

    /**
     * Write a collection values to rdf/xml.
     * 
     * @param writer
     * @param namespaceURI
     * @param localName
     * @param values
     * @throws XMLStreamException
     */
    private void writeCollection(XMLStreamWriter writer, String namespaceURI, String localName,
            Collection<String> values) throws XMLStreamException
    {
        if (values.isEmpty())
            return;

        writer.writeStartElement(namespaceURI, localName);

        if (values.size() > 1)
            writer.writeStartElement(RDF_URI, "Bag");

        for (String value : values)
        {
            writer.writeCharacters(value);
        }

        if (values.size() > 1)
            writer.writeEndElement();

        writer.writeEndElement();

    }

    /**
     * Defines the metadata for the resulting SWF file. The value of this option overrides any metadata-related compiler
     * options such as contributor, creator, date, and description.
     */
    @Config(advanced = true)
    @Mapping("raw-metadata")
    @Arguments("text")
    public void setRawMetadata(ConfigurationValue cv, String xml) throws ConfigurationException
    {
        if (metadata != null)
        {
            throw new ConfigurationException.BadMetadataCombo(cv.getVar(), cv.getSource(), cv.getLine());
        }

        this.metadata = xml;
    }

    //
    // 'resource-bundle-list' option
    //

    private String rbListFileName = null;

    public String getResourceBundleList()
    {
        return rbListFileName;
    }

    //
    // 'include-resource-bundles' option
    //

    /**
     * include-resource-bundles [...]
     */
    private List<String> includeResourceBundles = new ArrayList<String>();

    /**
     * @return a list of resource bundles to include in the swc
     */
    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    /**
     * Sets the resource bundles that should be included in this SWC or SWF. This list can have locale independent
     * qualified name for property files (with the name not including the suffix) or qualified name for classes that
     * extend ResourceBundle.
     * <p>
     * Qualified name of a properties file is determined by its relative path to its parent source folder. Such as:
     * <p>
     * Source path: locale/{locale}
     * <p>
     * Path of properties file 1: locale/en_US/A.properties Qualified name of properties file 1: A
     * <p>
     * Path of properties file 2: locale/en_US/com/resources/B.properties Qualified name of properties file 1:
     * com.resources.B
     * <p>
     * Note: Source folders of all the properties files passed using this argument should be in the project's source
     * path list.
     * 
     * @param cv configuration value objects
     * @param values list of resource bundles to include in the swc or swf
     */
    @Config(allowMultiple = true)
    @Mapping("include-resource-bundles")
    @Arguments("bundle")
    @RoyaleOnly
    public void setIncludeResourceBundles(ConfigurationValue cv, List<String> values)
    {
        includeResourceBundles.addAll(values);
    }

    /**
     * Prints a list of resource bundles that are used by the current application to a file named with the filename
     * argument. You then use this list as input that you specify with the include-resource-bundles option to create a
     * resource module.
     */
    @Config(advanced = true)
    @Mapping("resource-bundle-list")
    @Arguments("filename")
    @RoyaleOnly
    public void setResourceBundleList(ConfigurationValue cv, String filename)
    {
        this.rbListFileName = getOutputPath(cv, filename);
    }

    //
    // 'runtime-shared-libraries' option
    //

    private List<String> rslList = new LinkedList<String>();

    public List<String> getRuntimeSharedLibraries()
    {
        return rslList;
    }

    /**
     * Specifies a list of runtime shared libraries (RSLs) to use for this application. RSLs are dynamically-linked at
     * run time. The compiler externalizes the contents of the application that you are compiling that overlap with the
     * RSL.
     * <p>
     * You specify the location of the SWF file relative to the deployment location of the application. For example, if
     * you store a file named library.swf file in the web_root/libraries directory on the web server, and the
     * application in the web root, you specify libraries/library.swf.
     */
    @Config(allowMultiple = true)
    @Mapping("runtime-shared-libraries")
    @Arguments("url")
    @InfiniteArguments
    @RoyaleOnly
    public void setRuntimeSharedLibraries(ConfigurationValue cfgval, List<String> urls) throws ConfigurationException
    {
        rslList.addAll(urls);
    }

    //
    // 'use-network' option
    //

    private boolean useNetwork = true;

    public boolean getUseNetwork()
    {
        return useNetwork;
    }

    /**
     * Specifies that the current application uses network services.
     * <p>
     * The default value is true.
     * <p>
     * When the use-network property is set to false, the application can access the local filesystem (for example, use
     * the XML.load() method with file: URLs) but not network services. In most circumstances, the value of this
     * property should be true.
     */
    @Config
    @Mapping("use-network")
    public void setUseNetwork(ConfigurationValue cv, boolean b)
    {
        this.useNetwork = b;
    }

    //
    // runtime-shared-library-path
    //

    private List<RuntimeSharedLibraryPathInfo> rslPathInfoList;

    /**
     * @return List of of all the -runtime-shared-libraries-path options. Each-runtime-shared-libraries-path option
     *         supplied results in a RslPathInfo object. Each object in the list is of type RslPathInfo.
     *         <p>
     *         The list will be empty if -static-link-runtime-shared-libraries=true.
     *         <p>
     *         TODO Verify if this is still true and make the code do what it says.
     */
    public List<RuntimeSharedLibraryPathInfo> getRslPathInfo()
    {
        if (rslPathInfoList == null)
            return Collections.emptyList();
        else
            return rslPathInfoList;
    }

    public List<File> getRslExcludedLibraries()
    {
        if (rslPathInfoList == null || getStaticLinkRsl())
            return Collections.emptyList();

        return Lists.transform(rslPathInfoList, new Function<RuntimeSharedLibraryPathInfo, File>()
        {
            @Override
            public File apply(RuntimeSharedLibraryPathInfo info)
            {
                return info.getSWCFile();
            }
        });
    }

    /**
     * Specifies the location of a runtime shared library (RSL). The compiler externalizes the contents of the
     * application that you are compiling that overlap with the RSL.
     * <p>
     * The path-element argument is the location of the SWC file or open directory to compile against. For example,
     * c:\flexsdk\frameworks\libs\framework.swc. This is the equivalent of the using the external-library-path option
     * when compiling against an RSL using the runtime-shared-libraries option.
     * <p>
     * The rsl-url argument is the URL of the RSL that will be used to load the RSL at runtime. The compiler does not
     * verify the existence of the SWF file at this location at compile time. It does store this string in the
     * application, however, and uses it at run time. As a result, the SWF file must be available at run time but
     * necessarily not at compile time.
     * <p>
     * The policy-file-url is the location of the crossdomain.xml file that gives permission to read the RSL from the
     * server. This might be necessary because the RSL can be on a separate server as the application. For example,
     * http://www.mydomain.com/rsls/crossdomain.xml.
     * <p>
     * The failover-url and second policy-file-url arguments specify the location of the secondary RSL and
     * crossdomain.xml file if the first RSL cannot be loaded. This most commonly happens when the client Player version
     * does not support cross-domain RSLs. You can add any number of failover RSLs, but must include a policy file URL
     * for each one.
     * <p>
     * Do not include spaces between the comma-separated values. The following example shows how to use this option:
     * 
     * <pre>
     * mxmlc -o=../lib/app.swf -runtime-shared-library-path=../lib/mylib.swc,../bin/myrsl.swf Main.mxml
     * </pre>
     * 
     * You can specify more than one library file to be used as an RSL. You do this by adding additional
     * runtime-shared-library-path options.
     * <p>
     * You can also use the runtime-shared-libraries command to use RSLs with your applications. However, the
     * runtime-shared-library-path option lets you also specify the location of the policy file and failover RSL.
     * <p>
     */
    // NOTE: if the annotations are modified, then also modify the annotations
    // in COMPCConfiguration.
    @Config(allowMultiple = true)
    @Mapping({ "runtime-shared-library-path" })
    @SoftPrerequisites({ "static-link-runtime-shared-libraries" })
    @ArgumentNameGenerator(RSLArgumentNameGenerator.class)
    @InfiniteArguments
    @RoyaleOnly
    public void setRuntimeSharedLibraryPath(ConfigurationValue cfgval, List<String> urls) throws ConfigurationException
    {

        if (urls.isEmpty())
            return;

        // Usage rule: if you use -rslp on the command line
        // it will take effect unless you also specify -static-rsls=true on the command line.
        if (CommandLineConfigurator.SOURCE_COMMAND_LINE.equals(cfgval.getSource()))
        {
            setOverrideStaticLinkRsl(false);
        }

        // ignore rsl if told to
        if (getStaticLinkRsl())
        {
            return;
        }

        if (urls.size() < 2)
        {
            // insufficent arguments
            throw new ConfigurationException.MissingArgument("rsl-url", "runtime-shared-library-path",
                    cfgval.getSource(), cfgval.getLine());
        }

        RuntimeSharedLibraryPathInfo info = new RuntimeSharedLibraryPathInfo();

        // validate the first argument, the swc or open directory, required.
        String include = resolvePathStrict(urls.get(0), cfgval, true);
        info.setSWCPath(urls.get(0));
        info.setSWCFile(new File(include));

        // the rest of the args are: rsl-url, policy-file-url, rsl-url, policy-file-url,... 
        for (int i = 1; i < urls.size(); ++i)
        {
            final String url = urls.get(i);
            if ((i + 1) % 2 == 0)
            {
                if ("".equals(url.length()))
                {
                    // rsl urls is required
                    throw new ConfigurationException.MissingArgument("rsl-url", "runtime-shared-library-path",
                            cfgval.getSource(), cfgval.getLine());
                }
                info.addRSLURL(url);
            }
            else
            {
                info.addPolicyFileURL(url);
            }
        }

        // if the last policy file was not specified, then add an empty one so
        // there are always the same number of rsls and policy files.
        if ((urls.size() % 2) == 0)
        {
            info.addPolicyFileURL("");
        }

        // take local variables and add to overall arguments.
        if (rslPathInfoList == null)
        {
            rslPathInfoList = new ArrayList<RuntimeSharedLibraryPathInfo>();
        }

        rslPathInfoList.add(info);
    }

    //
    // 'static-link-runtime-shared-libraries' option
    // 

    private boolean staticLinkRsl = true;
    private String staticLinkRslSource;

    /**
     * @return true if -cd-rsl option should be used. False otherwise.
     */
    public boolean getStaticLinkRsl()
    {
        return staticLinkRsl;
    }

    /**
     * Allow another option, namely -rslp to override the value of static-rsls. But you can not override a -static-rsls
     * option that came from the command line.
     * 
     * @param staticLinkRsl
     */
    protected void setOverrideStaticLinkRsl(boolean staticLinkRsl)
    {
        if (CommandLineConfigurator.SOURCE_COMMAND_LINE.equals(staticLinkRslSource))
        {
            return;
        }

        this.staticLinkRsl = staticLinkRsl;
    }

    /**
     * Determines whether to compile against libraries statically or use RSLs. Set this option to true to ignore the
     * RSLs specified by the runtime-shared-library-path option. Set this option to false to use the RSLs.
     * <p>
     * This option is useful so that you can quickly switch between a statically and dynamically linked application
     * without having to change the runtime-shared-library-path option, which can be verbose, or edit the configuration
     * files.
     */
    @Config
    @Mapping("static-link-runtime-shared-libraries")
    @RoyaleOnly
    public void setStaticLinkRuntimeSharedLibraries(ConfigurationValue cv, boolean b)
    {
        staticLinkRsl = b;
        staticLinkRslSource = cv.getSource();
    }

    //
    // 'use-flashbuilder-project-files' option
    //
    private Boolean useFlashBuilderProjectFiles = false;

    public Boolean getUseFlashBuilderProjectFiles()
    {
        return useFlashBuilderProjectFiles;
    }

    @Config
    @Mapping({ "use-flashbuilder-project-files" })
    @RoyaleOnly
    public void setUseFlashBuilderProjectFiles(ConfigurationValue cv, Boolean useFiles) throws ConfigurationException
    {
        useFlashBuilderProjectFiles = useFiles;
    }

    //
    // 'verify-digests' options
    // 

    private boolean verifyDigests = true;

    /**
     * @return true if digest information associated with the -cd-rsl option is used by the application at runtime.
     *         False otherwise.
     */
    public boolean getVerifyDigests()
    {
        return verifyDigests;
    }

    /**
     * Instructs the application to check the digest of the RSL SWF file against the digest that was compiled into the
     * application at compile time. This is a security measure that lets you load RSLs from remote domains or different
     * sub-domains. It also lets you enforce versioning of your RSLs by forcing an application's digest to match the
     * RSL's digest. If the digests are out of sync, you must recompile your application or load a different RSL SWF
     * file.
     */
    @Config(advanced = true)
    @RoyaleOnly
    public void setVerifyDigests(ConfigurationValue cv, boolean b)
    {
        verifyDigests = b;
    }

    //
    // 'remove-unused-rsls' option
    // 

    private boolean removeUnusedRSLs = false;

    /**
     * @return true if the user wants to remove unused RSLs. Otherwise false.
     */
    public boolean getRemoveUnusedRsls()
    {
        return removeUnusedRSLs;
    }

    @Config(advanced = true)
    @RoyaleOnly
    public void setRemoveUnusedRsls(ConfigurationValue cv, boolean b)
    {
        removeUnusedRSLs = b;
    }

    //
    // '-include-inheritance-dependencies-only' option
    // 

    private boolean includeInheritanceDependenciesOnly = false;

    /**
     * @return true if the user want to include inheritance dependencies only.
     */
    public boolean getIncludeInheritanceDependenciesOnly()
    {
        return includeInheritanceDependenciesOnly;
    }

    @Config(advanced = true)
    public void setIncludeInheritanceDependenciesOnly(ConfigurationValue cv, boolean b)
    {
        includeInheritanceDependenciesOnly = b;
    }

    //
    // 'target-player' option
    // 

    // targeted player version (also set in DefaultsConfigurator)
    private int majorVersionTarget = 11;
    private int minorVersionTarget = 1;
    private int revisionTarget = 0;

    /**
     * The major part the earliest player version that this compiler can target. The code generator generates bytecode
     * which will not pass verification on players earlier than 10.1.
     */
    public static final int TARGET_PLAYER_MAJOR_VERSION_MIN = 10;

    /**
     * The minor part the earliest player version that this compiler can target. The code generator generates bytecode
     * which will not pass verification on players earlier than 10.1.
     */
    public static final int TARGET_PLAYER_MINOR_VERSION_MIN = 1;

    /**
     * @return The major version of the player targeted by this application. The returned value will be greater to or
     *         equal to 9.
     */
    public int getTargetPlayerMajorVersion()
    {
        return majorVersionTarget;
    }

    /**
     * @return The minor version of the player targeted by this application. The returned value will be greater to or
     *         equal to 0.
     */
    public int getTargetPlayerMinorVersion()
    {
        return minorVersionTarget;
    }

    /**
     * @return The revision of the player targeted by this application. The returned value will be greater to or equal
     *         to 0.
     */
    public int getTargetPlayerRevision()
    {
        return revisionTarget;
    }

    /**
     * Specifies the version of Flash Player that you want to target with the application. Features requiring a later
     * version of Flash Player are not compiled into the application.
     * <p>
     * The player_version parameter has the following format:<br>
     * <code>major_version.minor_version.revision</code>
     * <p>
     * The major_version is required while minor_version and revision are optional. The minimum value is 10.0.0. If you
     * do not specify the minor_version or revision, then the compiler uses zeros.
     * <p>
     * The value of major_version is also used by the {targetPlayerMajorVersion} token in the royale-config.xml file. This
     * token can be used in any <path-element> element.
     * <p>
     * If you do not explicitly set the value of this option, the compiler uses the default from the royale-config.xml
     * file. The value in royale-config.xml is the version of Flash Player that shipped with the SDK.
     * <p>
     * This option is useful if your application's audience has a specific player and cannot upgrade. You can use this
     * to "downgrade" your application for that audience.
     */
    @Config
    @Arguments("version")
    public void setTargetPlayer(ConfigurationValue cv, String version) throws ConfigurationException
    {
        if (version == null || version.equals(""))
            return;

        final String[] results = Iterables.toArray(Splitter.on(".").omitEmptyStrings().trimResults().split(version),
                String.class);

        // major.minor.revision
        // major is required, minor and revision are optional
        if (results.length < 1 || results.length > 3)
            throw new ConfigurationException.BadVersion(version, "target-player");

        final String majorVersion = results[0];

        final String minorVersion;
        if (results.length > 1)
            minorVersion = results[1];
        else
            minorVersion = "0";

        final String revision;
        if (results.length > 2)
            revision = results[2];
        else
            revision = "0";

        try
        {
            majorVersionTarget = Integer.parseInt(majorVersion);
            minorVersionTarget = Integer.parseInt(minorVersion);
            revisionTarget = Integer.parseInt(revision);
        }
        catch (NumberFormatException e)
        {
            throw new ConfigurationException.BadVersion(version, "target-player");
        }

        if (majorVersionTarget < TARGET_PLAYER_MAJOR_VERSION_MIN
                || majorVersionTarget == TARGET_PLAYER_MAJOR_VERSION_MIN
                        && minorVersionTarget < TARGET_PLAYER_MINOR_VERSION_MIN)
        {
            throw new ConfigurationException.BadVersion(version, "target-player");
        }
    }

    //
    // 'swf-version' option
    //

    // swf version 13 is what shipped with player 11, and is the min version for
    // LZMA compression
    private static final Map<String, Integer> targetPlayerToSWFVersionMap = getSwfVersionMap();

    private static Map<String, Integer> getSwfVersionMap()
    {
        // Player 9 and below are not supported.
        // 10.0 -> 10
        // 10.1 -> 10
        // 10.2 -> 11
        // 10.3 -> 12
        // 11.0 -> 13
        // 11.1 -> 14
        // 11.2 -> 15
        // 11.3 -> 16
        // 11.4 -> 17 
        // 11.5 -> 18
        // 11.6 -> 19
        // 11.7 -> 20
        // 11.8 -> 21
        // 11.9 -> 22
        // 12.0 -> 23
        // 13.0 -> 24
        // 14.0 -> 25
        // 15.0 -> 26
        // 16.0 -> 27
        // 17.0 -> 28
        // 18.0 -> 29
        // 19.0 -> 30
        // 20.0 -> 31
        // 21.0 -> 32
        // 22.0 -> 33
        // 23.0 -> 34
        // 24.0 -> 35
        // 25.0 -> 36
        // 26.0 -> 37
        // 27.0 -> 38
        // 28.0 -> 39
        // 29.0 -> 40
        // 30.0 -> 41
        // 31.0 -> 42
        // 32.0 -> 43

        Map<String, Integer> map = new HashMap<String, Integer>(10);

        map.put("10.0", 10);
        map.put("10.1", 10);
        map.put("10.2", 11);
        map.put("10.3", 12);
        map.put("11.0", 13);
        map.put("11.1", 14);
        map.put("11.2", 15);
        map.put("11.3", 16);
        map.put("11.4", 17);
        map.put("11.5", 18);
        map.put("11.6", 19);
        map.put("11.7", 20);
        map.put("11.8", 21);
        map.put("11.9", 22);
        map.put("12.0", 23);
        map.put("13.0", 24);
        map.put("14.0", 25);
        map.put("15.0", 26);
        map.put("16.0", 27);
        map.put("17.0", 28);
        map.put("18.0", 29);
        map.put("19.0", 30);
        map.put("20.0", 31);
        map.put("21.0", 32);
        map.put("22.0", 33);
        map.put("23.0", 34);
        map.put("24.0", 35);
        map.put("25.0", 36);
        map.put("26.0", 37);
        map.put("27.0", 38);
        map.put("28.0", 39);
        map.put("29.0", 40);
        map.put("30.0", 41);
        map.put("31.0", 42);
        map.put("32.0", 43);

        return map;
    }

    private int lookupSwfVersion()
    {
        int swfVersion = DEFAULT_SWF_VERSION;
        Integer lookupVersion = targetPlayerToSWFVersionMap.get(Integer.toString(getTargetPlayerMajorVersion()) + "."
                + Integer.toString(getTargetPlayerMinorVersion()));
        if (lookupVersion != null)
            swfVersion = lookupVersion;

        return swfVersion;
    }

    private final int UNSET_SWF_VERSION = -1;
    private final int DEFAULT_SWF_VERSION = 14; // matches default target-player
    private final int MINIMUM_SWF_VERSION = 10; // matches minimum target-player

    private int swfVersion = UNSET_SWF_VERSION;

    public int getSwfVersion()
    {
        if (swfVersion == UNSET_SWF_VERSION)
            swfVersion = lookupSwfVersion();
        return swfVersion;
    }

    @Config
    @Mapping("swf-version")
    public void setSwfVersion(ConfigurationValue cv, int version) throws ConfigurationException
    {
        if (version < MINIMUM_SWF_VERSION)
            throw new ConfigurationException.BadVersion(Integer.toString(version), "swf-version");

        swfVersion = version;
    }

    //
    // 'use-direct-blit' option
    //

    private boolean useDirectBlit = true;

    public boolean getUseDirectBlit()
    {
        return useDirectBlit;
    }

    @Config
    public void setUseDirectBlit(ConfigurationValue cv, boolean value)
    {
        useDirectBlit = value;
    }

    //
    // 'use-gpu' option
    //

    private boolean useGpu = true;

    public boolean getUseGpu()
    {
        return useGpu;
    }

    @Config
    public void setUseGpu(ConfigurationValue cv, boolean value)
    {
        useGpu = value;
    }

    //
    // 'tools-locale' options
    // 

    private Locale toolsLocale = null;

    /**
     * @return locale to use when reporting compile time errors, or <code>null</code> if not specified. In that case,
     *         system's locale is used.
     */
    public Locale getToolsLocale()
    {
        return toolsLocale;
    }

    /**
     * Configures the LocalizationManager's locale, which is used when reporting compile time errors, warnings, and
     * info.
     * 
     * @param toolsLocale A locale in Java format. For example, "en" or "ja_JP".
     * @throws ConfigurationException When the specified toolsLocale is not available a ToolsLocaleNotAvailable error is
     *         reported.
     */
    @Config
    @Mapping("tools-locale")
    public void setToolsLocale(ConfigurationValue cv, String toolsLocale) throws ConfigurationException
    {
        Locale[] locales = Locale.getAvailableLocales();

        for (int i = 0; i < locales.length; i++)
        {
            if (locales[i].toString().equals(toolsLocale))
            {
                this.toolsLocale = locales[i];

                LocalizationManager.get().setLocale(locales[i]);
                return;
            }
        }

        throw new ConfigurationException.ToolsLocaleNotAvailable(cv.getVar(), cv.getSource(), cv.getLine());
    }

    //
    // 'compiler.accessible' option
    //

    private boolean accessible = false;

    public boolean getCompilerAccessible()
    {
        return accessible;
    }

    /**
     * Enables accessibility features when compiling the application or SWC file.
     */
    @Config
    @Mapping({ "compiler", "accessible" })
    @RoyaleOnly
    public void setCompilerAccessible(ConfigurationValue cv, boolean accessible)
    {
        this.accessible = accessible;
    }

    //
    // 'compiler.allow-private-name-conflicts' option
    //

    private boolean allowPrivateNameConflicts = true;

    public boolean getCompilerAllowPrivateNameConflicts()
    {
        return allowPrivateNameConflicts;
    }

    /**
     * Whether the compiler will allow a subclass to have a variable/property/method with the
     * same name as a private variable/property/method in the base classes.  The compiler
     * will always report an error if the subclass has an API that conflicts with public/protected
     * APIs in the base classes.
     */
    @Config
    @Mapping({ "compiler", "allow-private-name-conflicts" })
    @RoyaleOnly
    public void setCompilerAllowPrivateNameConflicts(ConfigurationValue cv, boolean allow)
    {
        this.allowPrivateNameConflicts = allow;
    }

    //
    // 'compiler.allow-import-aliases' option
    //

    private boolean allowImportAliases = false;

    public boolean getCompilerAllowImportAliases()
    {
        return allowImportAliases;
    }

    /**
     * Whether the compiler will allow imports to include an optional alias for
     * the definition name.
     */
    @Config
    @Mapping({ "compiler", "allow-import-aliases" })
    @RoyaleOnly
    public void setCompilerAllowImportAliases(ConfigurationValue cv, boolean allow)
    {
        this.allowImportAliases = allow;
    }

    //
    // 'compiler.allow-abstract-classes' option
    //

    private boolean allowAbstractClasses = false;

    public boolean getCompilerAllowAbstractClasses()
    {
        return allowAbstractClasses;
    }

    /**
     * Whether the compiler will allow classes to be abstract.
     */
    @Config
    @Mapping({ "compiler", "allow-abstract-classes" })
    @RoyaleOnly
    public void setCompilerAllowAbstractClasses(ConfigurationValue cv, boolean allow)
    {
        this.allowAbstractClasses = allow;
    }

    //
    // 'compiler.allow-private-constructors' option
    //

    private boolean allowPrivateConstructors = false;

    public boolean getCompilerAllowPrivateConstructors()
    {
        return allowPrivateConstructors;
    }

    /**
     * Whether the compiler will allow constructors to be private.
     */
    @Config
    @Mapping({ "compiler", "allow-private-constructors" })
    @RoyaleOnly
    public void setCompilerAllowPrivateConstructors(ConfigurationValue cv, boolean allow)
    {
        this.allowPrivateConstructors = allow;
    }

    //
    // 'compiler.strict-identifier-names' option
    //

    private boolean strictIdentifierNames = true;

    public boolean getCompilerStrictIdentifierNames()
    {
        return strictIdentifierNames;
    }

    /**
     * Whether the compiler will enforce strict identifier naming. If enabled,
     * certain keywords and reserved words cannot be used as identifier names.
     */
    @Config
    @Mapping({ "compiler", "strict-identifier-names" })
    public void setCompilerStrictIdentifierNames(ConfigurationValue cv, boolean strict)
    {
        this.strictIdentifierNames = strict;
    }

    //
    // 'compiler.actionscript-file-encoding' option
    //

    private String actionscriptFileEncoding = null;

    public String getCompilerActionscriptFileEncoding()
    {
        return actionscriptFileEncoding;
    }

    /**
     * Sets the file encoding for ActionScript files.
     */
    @Config
    @Mapping({ "compiler", "actionscript-file-encoding" })
    public void setCompilerActionscriptFileEncoding(ConfigurationValue cv, String encoding)
    {
        actionscriptFileEncoding = encoding;
    }

    //
    // 'compiler.adjust-opdebugline' option (hidden)
    //

    private boolean adjustOpDebugLine = true;

    public boolean getAdjustOpDebugLine()
    {
        return adjustOpDebugLine;
    }

    /**
     * For internal use only. Set it to false so that debugging mxmlc auto-generated code is easier.
     */
    @Config(advanced = true, hidden = true)
    public void setCompilerAdjustOpdebugline(ConfigurationValue cv, boolean b)
    {
        adjustOpDebugLine = b;
    }

    //
    // 'compiler.allow-source-path-overlap' option
    //

    private boolean allowSourcePathOverlap = false;

    public boolean getAllowSourcePathOverlap()
    {
        return allowSourcePathOverlap;
    }

    /**
     * Checks if a source-path entry is a sub-directory of another source-path entry. It helps make the package names of
     * MXML components unambiguous.
     */
    @Config(advanced = true)
    public void setCompilerAllowSourcePathOverlap(ConfigurationValue cv, boolean b)
    {
        allowSourcePathOverlap = b;
    }

    //
    // 'compiler.binding-value-change-event' option
    //

    private String bindingValueChangeEvent = "mx.events.PropertyChangeEvent";

    public String getBindingValueChangeEvent()
    {
        return bindingValueChangeEvent;
    }

    /**
     * The change event class for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingValueChangeEvent(ConfigurationValue cv, String b)
    {
        bindingValueChangeEvent = b;
    }

    //
    // 'compiler.binding-value-change-event-kind' option
    //

    private String bindingValueChangeEventKind = "mx.events.PropertyChangeEventKind";

    public String getBindingValueChangeEventKind()
    {
        return bindingValueChangeEventKind;
    }

    /**
     * The change event kind for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingValueChangeEventKind(ConfigurationValue cv, String b)
    {
        bindingValueChangeEventKind = b;
    }

    //
    // 'compiler.binding-value-change-event-type' option
    //

    private String bindingValueChangeEventType = "propertyChange";

    public String getBindingValueChangeEventType()
    {
        return bindingValueChangeEventType;
    }

    /**
     * The change event type for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingValueChangeEventType(ConfigurationValue cv, String b)
    {
        bindingValueChangeEventType = b;
    }

    //
    // 'compiler.binding-event-handler-event' option
    //

    private String bindingEventHandlerEvent = "flash.events.Event";

    public String getBindingEventHandlerEvent()
    {
        return bindingEventHandlerEvent;
    }

    /**
     * The event handler event for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingEventHandlerEvent(ConfigurationValue cv, String b)
    {
        bindingEventHandlerEvent = b;
    }

    //
    // 'compiler.binding-event-handler-class' option
    //

    private String bindingEventHandlerClass = "flash.events.EventDispatcher";

    public String getBindingEventHandlerClass()
    {
        return bindingEventHandlerClass;
    }

    /**
     * The event handler class for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingEventHandlerClass(ConfigurationValue cv, String b)
    {
        bindingEventHandlerClass = b;
    }

    //
    // 'compiler.binding-event-handler-interface' option
    //

    private String bindingEventHandlerInterface = "flash.events.IEventDispatcher";

    public String getBindingEventHandlerInterface()
    {
        return bindingEventHandlerInterface;
    }

    /**
     * The event handler interface for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerBindingEventHandlerInterface(ConfigurationValue cv, String b)
    {
        bindingEventHandlerInterface = b;
    }

    //
    // 'compiler.byte-array-embed-class' option
    //

    private String byteArrayEmbedClass = "mx.core.ByteArrayAsset";

    public String getByteArrayEmbedClass()
    {
        return byteArrayEmbedClass;
    }

    /**
     * The class for embedded byte arrays
     */
    @Config(advanced = true)
    public void setCompilerByteArrayEmbedClass(ConfigurationValue cv, String b)
    {
    	byteArrayEmbedClass = b;
    }

    //
    // 'compiler.states-class' option
    //

    private String statesClass = "mx.states.State";

    public String getStatesClass()
    {
        return statesClass;
    }

    /**
     * The class for states
     */
    @Config(advanced = true)
    public void setCompilerStatesClass(ConfigurationValue cv, String b)
    {
        statesClass = b;
    }

    //
    // 'compiler.states-instance-override-class' option
    //

    private String statesInstanceOverrideClass = "mx.states.AddItems";

    public String getStatesInstanceOverrideClass()
    {
        return statesInstanceOverrideClass;
    }

    /**
     * The class for state-dependent instances
     */
    @Config(advanced = true)
    public void setCompilerStatesInstanceOverrideClass(ConfigurationValue cv, String b)
    {
        statesInstanceOverrideClass = b;
    }

    //
    // 'compiler.states-property-override-class' option
    //

    private String statesPropertyOverrideClass = "mx.states.SetProperty";

    public String getStatesPropertyOverrideClass()
    {
        return statesPropertyOverrideClass;
    }

    /**
     * The class for state-dependent properties
     */
    @Config(advanced = true)
    public void setCompilerStatesPropertyOverrideClass(ConfigurationValue cv, String b)
    {
        statesPropertyOverrideClass = b;
    }

    //
    // 'compiler.states-event-override-class' option
    //

    private String statesEventOverrideClass = "mx.states.SetEventHandler";

    public String getStatesEventOverrideClass()
    {
        return statesEventOverrideClass;
    }

    /**
     * The class for state-dependent events
     */
    @Config(advanced = true)
    public void setCompilerStatesEventOverrideClass(ConfigurationValue cv, String b)
    {
        statesEventOverrideClass = b;
    }

    //
    // 'compiler.states-style-override-class' option
    //

    private String statesStyleOverrideClass = "mx.states.SetStyle";

    public String getStatesStyleOverrideClass()
    {
        return statesStyleOverrideClass;
    }

    /**
     * The class for state-dependent styles
     */
    @Config(advanced = true)
    public void setCompilerStatesStyleOverrideClass(ConfigurationValue cv, String b)
    {
        statesStyleOverrideClass = b;
    }

    //
    // 'compiler.proxy-base-class' option
    //

    private String proxyBaseClass = "org.apache.royale.utils.Proxy";

    public String getProxyBaseClass()
    {
        return proxyBaseClass;
    }

    /**
     * The class for proxy code generation
     */
    @Config(advanced = true)
    public void setCompilerProxyBaseClass(ConfigurationValue cv, String b)
    {
        proxyBaseClass = b;
    }

    //
    // 'compiler.component-factory-class' option
    //

    private String componentFactoryClass = "mx.core.ClassFactory";

    public String getComponentFactoryClass()
    {
        return componentFactoryClass;
    }

    /**
     * The class for inline component factories
     */
    @Config(advanced = true)
    public void setCompilerComponentFactoryClass(ConfigurationValue cv, String b)
    {
        componentFactoryClass = b;
    }

    //
    // 'compiler.component-factory-interface' option
    //

    private String componentFactoryInterface = "mx.core.IFactory";

    public String getComponentFactoryInterface()
    {
        return componentFactoryInterface;
    }

    /**
     * The interface for inline component factories
     */
    @Config(advanced = true)
    public void setCompilerComponentFactoryInterface(ConfigurationValue cv, String b)
    {
        componentFactoryInterface = b;
    }

    //
    // 'compiler.fxg-base-class' option
    //

    private String fxgBaseClass = "spark.core.SpriteVisualElement";

    public String getFxgBaseClass()
    {
        return fxgBaseClass;
    }

    /**
     * The base class for generated binding code
     */
    @Config(advanced = true)
    public void setCompilerFxgBaseClass(ConfigurationValue cv, String b)
    {
    	fxgBaseClass = b;
    }

    /**
     * Syntax:<br/>
     * <code>-define=&lt;name&gt;,&lt;value&gt;</code> where name is <code>NAMESPACE::name</code> and value is a legal
     * definition value (e.g. <code>true</code> or <code>1</code> or <code>!CONFIG::debugging</code>)
     *
     * Example: <code>-define=CONFIG::debugging,true</code>
     *
     * In <code>royale-config.xml</code>:<br/>
     * 
     * <pre>
     * <royale-config>
     *    <compiler>
     *       <define>
     *          <name>CONFIG::debugging</name>
     *          <value>true</value>
     *       </define>
     *       ...
     *    </compile>
     * </royale-config>
     * </pre>
     *
     * Values:<br/>
     * Values are ActionScript expressions that must coerce and evaluate to constants at compile-time. Effectively, they
     * are replaced in AS code, verbatim, so <code>-define=TEST::oneGreaterTwo,"1>2"</code> will getCompiler coerced and
     * evaluated, at compile-time, to <code>false</code>.
     *
     * It is good practice to wrap values with double-quotes, so that MXMLC correctly parses them as a single argument:
     * <br/>
     * <code>-define=TEST::oneShiftRightTwo,"1 >> 2"</code>
     *
     * Values may contain compile-time constants and other configuration values:<br/>
     * <code>-define=CONFIG::bool2,false -define=CONFIG::and1,"CONFIG::bool2 && false" TestApp.mxml</code>
     *
     * String values on the command-line <i>must</i> be surrounded by double-quotes, and either escape-quoted (
     * <code>"\"foo\""</code> or <code>"\'foo\'"</code>) or single-quoted (<code>"'foo'"</code>).
     *
     * String values in configuration files need only be single- or double- quoted:<br/>
     * 
     * <pre>
     * <royale-config>
     *    <compiler>
     *       <define>
     *          <name>NAMES::Organization</name>
     *          <value>'Apache Software Foundation'</value>
     *       </define>
     *       <define>
     *          <name>NAMES::Application</name>
     *          <value>"Flex 4.8.0"</value>
     *       </define>
     *       ...
     *    </compile>
     * </royale-config>
     * </pre>
     *
     * Empty strings <i>must</i> be passed as <code>"''"</code> on the command-line, and <code>''</code> or
     * <code>""</code> in configuration files.
     * 
     * Finally, if you have existing definitions in a configuration file, and you would like to add to them with the
     * command-line (let's say most of your build setCompilertings are in the configuration, and that you are adding one
     * temporarily using the command-line), you use the following syntax: <code>-define+=TEST::temporary,false</code>
     * (noting the plus sign)
     * 
     * Note that definitions can be overridden/redefined if you use the append ("+=") syntax (on the commandline or in a
     * user config file, for instance) with the same namespace and name, and a new value.
     * 
     * Definitions cannot be removed/undefined. You can undefine ALL existing definitions from (e.g. from
     * royale-config.xml) if you do not use append syntax ("=" or append="false").
     * 
     * IMPORTANT FOR FLEXBUILDER If you are using "Additional commandline arguments" to "-define", don't use the
     * following syntax though I suggest it above: -define+=CONFIG::foo,"'value'" The trouble is that FB parses the
     * double quotes incorrectly as <"'value'> -- the trailing double-quote is dropped. The solution is to avoid inner
     * double-quotes and put them around the whole expression: -define+="CONFIG::foo,'value'"
     */
    private Map<String, String> configVars;

    /**
     * @return A list of ConfigVars
     */

    public Map<String, String> getCompilerDefine()
    {
        return configVars;
    }

    @Config(advanced = true, allowMultiple = true)
    @Arguments({ "name", "value" })
    public void setCompilerDefine(ConfigurationValue cv, String name, String value) throws ConfigurationException
    {
        if (configVars == null)
            configVars = new LinkedHashMap<String, String>();

        configVars.put(name, value);
    }
    
    //
    // 'compiler.conservative' option (hidden)
    //

    private boolean useConservativeAlgorithm = false;

    public boolean useConservativeAlgorithm()
    {
        return useConservativeAlgorithm;
    }

    @Config(advanced = true, hidden = true)
    @Mapping({ "compiler", "conservative" })
    public void setCompilerConservative(ConfigurationValue cv, boolean c)
    {
        useConservativeAlgorithm = c;
    }

    //
    // 'compiler.context-root' option
    //

    private String contextRoot = null;

    public String getCompilerContextRoot()
    {
        return contextRoot;
    }

    /**
     * "Context root" is used to resolve {context.root} tokens in services configuration files to improve portability.
     */
    @Config
    @Mapping({ "compiler", "context-root" })
    @Arguments("context-path")
    @RoyaleOnly
    public void setCompilerContextRoot(ConfigurationValue cv, String contextRoot)
    {
        this.contextRoot = contextRoot;
    }

    //
    // 'compiler.debug' option
    //

    private boolean generateDebugTags = false;

    public boolean isDebuggingEnabled()
    {
        return generateDebugTags;
    }

    protected void setDebug(boolean value)
    {
        generateDebugTags = value;
    }

    @Config
    @Mapping({ "compiler", "debug" })
    public void setCompilerDebug(ConfigurationValue cv, boolean generateDebugTags)
    {
        this.generateDebugTags = generateDebugTags;
    }

    //
    // 'compiler.defaults-css-url' option
    //

    /**
     * Location of defaults stylesheet.
     */
    private String defaultsCssUrl;

    public String getCompilerDefaultsCssUrl()
    {
        return defaultsCssUrl;
    }

    /**
     * Defines the location of the default style sheet. Setting this option overrides the implicit use of the
     * defaults.css style sheet in the framework.swc file.
     */
    @Config(advanced = true)
    @Mapping({ "compiler", "defaults-css-url" })
    @RoyaleOnly
    public void setCompilerDefaultsCssUrl(ConfigurationValue cv, String defaultsCssUrlPath) throws CannotOpen
    {
        defaultsCssUrl = resolvePathStrict(defaultsCssUrlPath, cv);
    }

    //
    // 'compiler.doc' option (hidden)
    //

    private boolean doc = false;

    public boolean getCompilerDoc()
    {
        return this.doc;
    }

    @Config(advanced = true, hidden = true)
    @Mapping({ "compiler", "doc" })
    public void setCompilerDoc(ConfigurationValue cv, boolean doc)
    {
        this.doc = doc;
    }

    //
    // 'compiler.external-library-path' option
    //

    private final List<String> externalLibraryPath = new ArrayList<String>();

    public List<String> getCompilerExternalLibraryPath()
    {
        return externalLibraryPath;
    }

    private boolean compilingForAIR = false;

    /**
     * @return True if AIR libraries are included in the {@code external-library-path}.
     */
    public boolean getCompilingForAIR()
    {
        return compilingForAIR;
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "external-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @SoftPrerequisites({ "target-player", "exclude-native-js-libraries" })
    @InfiniteArguments
    public void setCompilerExternalLibraryPath(ConfigurationValue cv, String[] pathlist) throws ConfigurationException
    {
        pathlist = removeNativeJSLibrariesIfNeeded(pathlist);

        final ImmutableList<String> pathElements = ImmutableList.copyOf(pathlist);
        final ImmutableList<String> resolvedPaths = expandTokens(pathElements, locales, cv,
                !reportMissingCompilerLibraries);
        externalLibraryPath.addAll(resolvedPaths);

        // TODO: Review usages of "compilingForAIR", because only looking at path elements
        // on library path isn't enough. There might be a folder on the library path that
        // contains AIR libraries.
        compilingForAIR = containsAIRLibraries(pathElements);
    }

    /**
     * Returns true if there's AIR libraries on the library paths.
     * 
     * @param libraryPaths Library paths.
     * @return True if there's AIR libraries on the library paths.
     */
    private static boolean containsAIRLibraries(final ImmutableList<String> libraryPaths)
    {
        for (final String path : libraryPaths)
        {
            if (path.equals(SWC_AIRGLOBAL) || path.endsWith("/" + SWC_AIRGLOBAL) || path.endsWith("\\" + SWC_AIRGLOBAL))
            {
                return true;
            }
        }
        return false;
    }

    //
    // 'compiler.swf-external-library-path' option
    //

    private final List<String> swfExternalLibraryPath = new ArrayList<String>();

    public List<String> getCompilerSwfExternalLibraryPath()
    {
        return swfExternalLibraryPath;
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "swf-external-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @SoftPrerequisites({ "target-player", "exclude-native-js-libraries" })
    @InfiniteArguments
    public void setCompilerSwfExternalLibraryPath(ConfigurationValue cv, String[] pathlist) throws ConfigurationException
    {
        pathlist = removeNativeJSLibrariesIfNeeded(pathlist);

        final ImmutableList<String> pathElements = ImmutableList.copyOf(pathlist);
        final ImmutableList<String> resolvedPaths = expandTokens(pathElements, locales, cv,
                !reportMissingCompilerLibraries);
        swfExternalLibraryPath.addAll(resolvedPaths);

        // TODO: Review usages of "compilingForAIR", because only looking at path elements
        // on library path isn't enough. There might be a folder on the library path that
        // contains AIR libraries.
        compilingForAIR = containsAIRLibraries(pathElements);
    }
    
    //
    // 'compiler.generated-directory' option
    // This can only be configured using getter and setter.
    //

    private String generatedDir = null;

    public String getCompilerGeneratedDirectory()
    {
        return generatedDir;
    }

    public void setCompilerGeneratedDirectory(String generatedDir)
    {
        this.generatedDir = generatedDir;
    }

    //
    // 'compiler.headless-server' option
    //

    private boolean headlessServer;

    public boolean isHeadlessServer()
    {
        return headlessServer;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "headless-server" })
    @RoyaleOnly
    public void setCompilerHeadlessServer(ConfigurationValue cv, boolean headlessServer)
    {
        this.headlessServer = headlessServer;
    }

    //
    // 'compiler.include-libraries' option
    //

    private final List<String> includeLibraries = new ArrayList<String>();

    public List<String> getCompilerIncludeLibraries()
    {
        return includeLibraries;
    }

    /**
     * Links all classes inside a SWC file to the resulting application SWF file, regardless of whether or not they are
     * used.
     * <p>
     * Contrast this option with the library-path option that includes only those classes that are referenced at compile
     * time.
     * <p>
     * To link one or more classes whether or not they are used and not an entire SWC file, use the includes option.
     * <p>
     * This option is commonly used to specify resource bundles.
     */
    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "include-libraries" })
    @Arguments("library")
    @InfiniteArguments
    public void setCompilerIncludeLibraries(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        final ImmutableList<String> resolvedPaths = expandTokens(Arrays.asList(pathlist), locales, cv,
                !reportMissingCompilerLibraries);
        includeLibraries.addAll(resolvedPaths);
    }

    //
    // 'compiler.incremental' option
    //

    @Config(removed = true)
    @Mapping({ "compiler", "incremental" })
    public void setCompilerIncremental(ConfigurationValue cv, boolean b)
    {
    }

    //
    // 'compiler.keep-all-type-selectors' option.  

    /**
     * This was initially used by Flex Builder when building design view, but they no longer use it.
     */
    private boolean keepAllTypeSelectors;

    public boolean keepAllTypeSelectors()
    {
        return keepAllTypeSelectors;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "keep-all-type-selectors" })
    @RoyaleOnly
    public void setCompilerKeepAllTypeSelectors(ConfigurationValue cv, boolean keepAllTypeSelectors)
    {
        this.keepAllTypeSelectors = keepAllTypeSelectors;
    }

    //
    // 'compiler.keep-as3-metadata' option
    //

    private Set<String> as3metadata = null;

    public Set<String> getCompilerKeepAs3Metadata()
    {
        return as3metadata == null ? Collections.<String> emptySet() : as3metadata;
    }

    @Config(advanced = true, allowMultiple = true)
    @Mapping({ "compiler", "keep-as3-metadata" })
    @Arguments("name")
    @InfiniteArguments
    public void setCompilerKeepAs3Metadata(ConfigurationValue cv, List<String> values)
    {
        if (as3metadata == null)
            as3metadata = new HashSet<String>();
        as3metadata.addAll(values);
    }

    //
    // 'compiler.keep-generated-actionscript' option (removed)
    //

    @Config(removed = true)
    @Mapping({ "compiler", "keep-generated-actionscript" })
    public void setCompilerKeepGeneratedActionscript(ConfigurationValue cv, boolean keep)
    {
    }

    //
    // 'compiler.keep-generated-signatures' option (removed)
    //

    @Config(removed = true)
    @Mapping({ "compiler", "keep-generated-signatures" })
    public void setCompilerKeepGeneratedSignatures(ConfigurationValue cv, boolean keep)
    {
    }

    //
    // 'compiler.enable-runtime-design-layers' option
    //

    private boolean enableRuntimeDesignLayers = true;

    public boolean getEnableRuntimeDesignLayers()
    {
        return enableRuntimeDesignLayers;
    }

    @Config
    @Mapping({ "compiler", "enable-runtime-design-layers" })
    @RoyaleOnly
    public void setCompilerEnableRuntimeDesignLayers(ConfigurationValue cv, boolean enable)
    {
        enableRuntimeDesignLayers = enable;
    }

    //
    // 'compiler.enable-swc-version-filtering' option
    //

    private boolean enableSwcVersionFiltering = true;

    public boolean getEnableSwcVersionFiltering()
    {
        return enableSwcVersionFiltering;
    }

    @Config(advanced = true, hidden = true)
    @Mapping({ "compiler", "enable-swc-version-filtering" })
    public void setCompilerEnableSwcVersionFiltering(ConfigurationValue cv, boolean enable)
    {
        this.enableSwcVersionFiltering = enable;
    }

    //
    // 'compiler.library-path' option
    //

    private final List<String> libraryPath = new ArrayList<String>();
    protected boolean reportMissingCompilerLibraries = true;

    /**
     * Sets whether to report missing libraries in the configuration. If this is false any missing libraries will not be
     * warned about, and the filename will also be added to list of libraries in the project when it doesn't exist. If
     * reportMissingCompilerLibraries is true, any missing libraries will not be added to the project.
     * 
     * @param reportMissingCompilerLibraries true to report missing libraries
     */
    public void setReportMissingCompilerLibraries(boolean reportMissingCompilerLibraries)
    {
        this.reportMissingCompilerLibraries = reportMissingCompilerLibraries;
    }

    public List<String> getCompilerLibraryPath()
    {
        return libraryPath;
    }

    /**
     * Links SWC files to the resulting application SWF file. The compiler only links in those classes for the SWC file
     * that are required. You can specify a directory or individual SWC files.
     */
    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @SoftPrerequisites({ "locale", "target-player", "exclude-native-js-libraries" })
    public void setCompilerLibraryPath(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        pathlist = removeNativeJSLibrariesIfNeeded(pathlist);
        final ImmutableList<String> resolvedPaths = expandTokens(Arrays.asList(pathlist), locales, cv,
                !reportMissingCompilerLibraries);
        libraryPath.addAll(resolvedPaths);
    }

    //
    // 'compiler.swf-library-path' option
    //

    private final List<String> swfLibraryPath = new ArrayList<String>();
    protected boolean reportMissingCompilerSwfLibraries = true;

    /**
     * Sets whether to report missing libraries in the configuration. If this is false any missing libraries will not be
     * warned about, and the filename will also be added to list of libraries in the project when it doesn't exist. If
     * reportMissingCompilerLibraries is true, any missing libraries will not be added to the project.
     * 
     * @param reportMissingCompilerLibraries true to report missing libraries
     */
    public void setReportMissingCompilerSwfLibraries(boolean reportMissingCompilerLibraries)
    {
        this.reportMissingCompilerLibraries = reportMissingCompilerLibraries;
    }

    public List<String> getCompilerSwfLibraryPath()
    {
        return swfLibraryPath;
    }

    /**
     * Links SWC files to the resulting application SWF file. The compiler only links in those classes for the SWC file
     * that are required. You can specify a directory or individual SWC files.
     */
    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "swf-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @SoftPrerequisites({ "locale", "target-player", "exclude-native-js-libraries" })
    public void setCompilerSwfLibraryPath(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        pathlist = removeNativeJSLibrariesIfNeeded(pathlist);
        final ImmutableList<String> resolvedPaths = expandTokens(Arrays.asList(pathlist), locales, cv,
                !reportMissingCompilerLibraries);
        swfLibraryPath.addAll(resolvedPaths);
    }

    //
    // 'compiler.locale' option
    //

    protected final List<String> locales = new ArrayList<String>();

    public List<String> getCompilerLocales()
    {
        return locales;
    }

    /**
     * Specifies one or more locales to be compiled into the SWF file. If you do not specify a locale, then the compiler
     * uses the default locale from the royale-config.xml file. The default value is en_US. You can append additional
     * locales to the default locale by using the += operator. If you remove the default locale from the royale-config.xml
     * file, and do not specify one on the command line, then the compiler will use the machine's locale.
     */
    @Config(allowMultiple = true)
    @Mapping({ "compiler", "locale" })
    @Arguments("locale-element")
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerLocale(ConfigurationValue cv, String[] newLocales)
    {
        locales.addAll(Arrays.asList(newLocales));
    }

    //
    // 'compiler.metadata-export' option (incomplete)
    //

    private boolean metadataExport;

    public boolean metadataExport()
    {
        return metadataExport;
    }

    // metadataExport does not have the normal configuration setCompilerter because it is not
    // a normal configuration value but rather something setCompiler by the compiler
    public void setCompilerMetadataExport(boolean metadataExport)
    {
        this.metadataExport = metadataExport;
    }

    //
    // 'compiler.mxml.children-as-data' option
    //
    private Boolean childrenAsData = false;

    public Boolean getCompilerMxmlChildrenAsData()
    {
        return childrenAsData;
    }

    @Config
    @Mapping({ "compiler", "mxml", "children-as-data" })
    @RoyaleOnly
    public void setCompilerMxmlChildrenAsData(ConfigurationValue cv, Boolean asData) throws ConfigurationException
    {
        childrenAsData = asData;
    }

    //
    // 'compiler.info.flex' option
    // used to suppress some of info() fields
    //
    private Boolean infoFlex = true;

    public Boolean getCompilerInfoFlex()
    {
        return infoFlex;
    }

    @Config
    @Mapping({ "compiler", "info", "royale" })
    @RoyaleOnly
    public void setCompilerInfoFlex(ConfigurationValue cv, Boolean asData) throws ConfigurationException
    {
    	infoFlex = asData;
    }

    //
    // 'compiler.allow-subclass-overrides' option
    //
    private Boolean allowSubclassOverrides = false;

    public Boolean getCompilerAllowSubclassOverrides()
    {
        return allowSubclassOverrides;
    }

    @Config
    @Mapping({ "compiler", "allow-subclass-overrides" })
    @RoyaleOnly
    public void setCompilerAllowSubclassOverrides(ConfigurationValue cv, Boolean allow) throws ConfigurationException
    {
        allowSubclassOverrides = allow;
    }

    //
    // 'compiler.mxml.implicitImports' option
    //
    private String[] implicitImports;

    public String[] getCompilerMxmlImplicitImports()
    {
        return implicitImports;
    }

    @Config(allowMultiple = true)
    @Mapping({ "compiler", "mxml", "imports" })
    @Arguments("implicit-import")
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerMxmlImplicitImports(ConfigurationValue cv, String[] imports) throws ConfigurationException
    {
        implicitImports = imports;
    }

    //
    // 'compiler.mxml.compatibility-version' option
    //

    public String getCompilerCompatibilityVersionString()
    {
        return getCompilerMxmlCompatibilityVersionString();
    }

    public int getCompilerCompatibilityVersion()
    {
        return getCompilerMxmlCompatibilityVersion();
    }

    //
    // 'compiler.mxml.minimum-supported-version' option
    //

    public String getCompilerMinimumSupportedVersionString()
    {
        return getCompilerMxmlMinimumSupportedVersionString();
    }

    public int getCompilerMinimumSupportedVersion()
    {
        return getCompilerMxmlMinimumSupportedVersion();
    }

    @Config
    @Mapping({ "compiler", "minimum-supported-version" })
    @RoyaleOnly
    public void setCompilerMinimumSupportedVersion(ConfigurationValue cv, String version) throws ConfigurationException
    {
        setCompilerMxmlMinimumSupportedVersion(cv, version);
    }

    //
    // 'qualified-type-selectors' option
    //
    @Config(advanced = true, removed = true)
    @Mapping({ "compiler", "mxml", "qualified-type-selectors" })
    public void setCompilerMxmlQualifiedTypeSelectors(ConfigurationValue cv, boolean b)
    {
    }

    //
    // 'compiler.omit-trace-statements' option
    //

    private boolean omitTraceStatements = true;

    public boolean omitTraceStatements()
    {
        return omitTraceStatements;
    }

    @Config
    @Mapping({ "compiler", "omit-trace-statements" })
    public void setCompilerOmitTraceStatements(ConfigurationValue cv, boolean b)
    {
        omitTraceStatements = b;
    }

    //
    // 'compiler.optimize' option
    //

    private boolean optimize = false;

    public boolean optimize()
    {
        return optimize;
    }

    public boolean getCompilerOptimize()
    {
        return optimize;
    }

    @Config
    @Mapping({ "compiler", "optimize" })
    public void setCompilerOptimize(ConfigurationValue cv, boolean b)
    {
        optimize = b;
    }

    //
    // 'compiler.preloader' option
    //

    private String preloader = null;

    /**
     * 
     * @return Returns the preloader class configured by the user. If the user did not configure a preloader, the
     *         "mx.preloader.DownloaderProgressBar" preloader will be returned if the compatibility version is less than
     *         4.0. Otherwise the "mx.preloaders.SparkDownloadProgressBar" preloader will be returned.
     */
    public String getPreloader()
    {
        if (preloader != null)
            return preloader;

        if (getCompilerMxmlCompatibilityVersion() < MXML_VERSION_4_0)
            return IMXMLTypeConstants.DownloadProgressBar;
        else
            return IMXMLTypeConstants.SparkDownloadProgressBar;
    }

    public String getCompilerPreloader()
    {
        return preloader;
    }

    @Config
    @Mapping({ "compiler", "preloader" })
    @RoyaleOnly
    public void setCompilerPreloader(ConfigurationValue cv, String value)
    {
        preloader = value;
    }

    //
    // 'compiler.services' option
    //

    private File servicesConfigFile;

    //protected ServicesDependencies servicesDependencies;

    public File getCompilerServices()
    {
        return servicesConfigFile;
    }

    /**
     * Used by the compiler to record the client dependencies from the Flex Data Services configuration file.
     */
    /*
     * public ServicesDependencies getCompilerServicesDependencies() { if
     * (servicesDependencies == null && servicesConfigFile != null) { String
     * servicesPath = servicesConfigFile.getName(); servicesDependencies = new
     * ServicesDependencies(servicesPath, null, getCompilerContextRoot()); }
     * return servicesDependencies; } public void
     * setCompilerServicesDependencies(ServicesDependencies deps) {
     * servicesDependencies = deps; }
     */

    @Config
    @Mapping({ "compiler", "services" })
    @Arguments("filename")
    @RoyaleOnly
    public void setCompilerServices(ConfigurationValue cv, String servicesPath) throws ConfigurationException
    {
        try
        {
            servicesConfigFile = new File(resolvePathStrict(servicesPath, cv));
        }
        catch (Exception e)
        {
            throw new ConfigurationException.CannotOpen(servicesPath, cv.getVar(), cv.getSource(), cv.getLine());
        }
    }

    //
    // 'compiler.show-actionscript-warnings' option
    //

    /**
     * Enable asc -warnings
     */
    private boolean ascWarnings;

    public boolean warnings()
    {
        return this.ascWarnings;
    }

    @Config
    @Mapping({ "compiler", "show-actionscript-warnings" })
    public void setCompilerShowActionscriptWarnings(ConfigurationValue cv, boolean ascWarnings)
    {
        this.ascWarnings = ascWarnings;
    }

    //
    // 'compiler.show-binding-warnings' option
    //

    /**
     * Controls whether binding warnings are displayed.
     */
    private boolean showBindingWarnings = true;

    public boolean showBindingWarnings()
    {
        return showBindingWarnings;
    }

    @Config
    @Mapping({ "compiler", "show-binding-warnings" })
    public void setCompilerShowBindingWarnings(ConfigurationValue cv, boolean show)
    {
        this.showBindingWarnings = show;
    }

    @Config
    @Mapping({ "compiler", "show-multiple-definition-warnings" })
    public void setCompilerShowMultipleDefinitionWarnings(ConfigurationValue cv, boolean show)
    {
        this.showMultipleDefinitionWarnings = show;
    }
    //
    // 'compiler.show-dependency-warnings' option (hidden)
    //

    private boolean showDependencyWarnings = false;

    public boolean showDependencyWarnings()
    {
        return showDependencyWarnings;
    }

    @Config(advanced = true, hidden = true)
    @Mapping({ "compiler", "show-dependency-warnings" })
    public void setCompilerShowDependencyWarnings(ConfigurationValue cv, boolean show)
    {
        this.showDependencyWarnings = show;
    }

    //
    // 'compiler.report-invalid-styles-as-warnings' option
    //

    /**
     * Controls whether invalid styles are report as errors or warnings.
     */
    private boolean reportInvalidStylesAsWarnings = false;

    /**
     * Get value of {@code compiler.report-invalid-styles-as-warnings} option value.
     * <p>
     * <h2>What's "invalid styles"?</h2> The term "invalid style" only applies to MXML style specifier (a.k.a. inline
     * style). If a style of a component is defined with "theme" attribute, the style is only effective with such theme.
     * If a theme-specific style is used in an application who doesn't use the required theme, the style is considered
     * invalid.
     * <p>
     * For example, style "fooStyle" is defined to used only with theme called "fooTheme":
     * 
     * <pre>
     * [Style(name="fooStyle", type="uint", format="Color", inherit="yes", theme="fooTheme")]
     * public class MyComponent extends UIComponent
     * </pre>
     * 
     * If "fooTheme" isn't used by the current application, the following style specifier is considered "invalid styles"
     * . <br>
     * {@code <local:MyComponent fooStyle="white" />}
     * 
     * @return True if invalid styles are reported as warnings instead of errors.
     */
    public boolean getReportInvalidStylesAsWarnings()
    {
        return reportInvalidStylesAsWarnings;
    }

    @Config
    @Mapping({ "compiler", "report-invalid-styles-as-warnings" })
    @RoyaleOnly
    public void setCompilerReportInvalidStylesAsWarnings(ConfigurationValue cv, boolean show)
    {
        this.reportInvalidStylesAsWarnings = show;
    }

    //
    // 'compiler.report-missing-required-skin-parts-as-warnings' option
    //

    private boolean reportMissingRequiredSkinPartsAsWarnings = false;

    /**
     * Allow the user to configure whether it should be considered an error to not create a required skin part or if it
     * should just be a warning.
     */
    public boolean reportMissingRequiredSkinPartsAsWarnings()
    {
        return reportMissingRequiredSkinPartsAsWarnings;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "report-missing-required-skin-parts-as-warnings" })
    @RoyaleOnly
    public void setCompilerReportMissingRequiredSkinPartsAsWarnings(ConfigurationValue cv, boolean b)
    {
        reportMissingRequiredSkinPartsAsWarnings = b;
    }

    //
    // 'compiler.show-invalid-css-property-warnings' option
    //

    private boolean showInvalidCSSPropertyWarnings = true;

    /**
     * Controls whether warnings are displayed when styles, which don't apply to the current theme(s), are used in CSS.
     * <p>
     * See {@link #getReportInvalidStylesAsWarnings()} for definition of "invalid style".
     * <p>
     * This option applies to <i>invalid styles</i> in a {@code <fx:Style>} block.
     */
    public boolean getShowInvalidCSSPropertyWarnings()
    {
        return showInvalidCSSPropertyWarnings;
    }

    @Config
    @Mapping({ "compiler", "show-invalid-css-property-warnings" })
    @RoyaleOnly
    public void setShowInvalidCssPropertyWarnings(ConfigurationValue cv, boolean show)
    {
        this.showInvalidCSSPropertyWarnings = show;
    }

    //
    // 'compiler.show-deprecation-warnings' option
    //

    /**
     * Controls whether warnings are displayed when a deprecated API is used.
     */
    private boolean showDeprecationWarnings = false;

    public boolean showDeprecationWarnings()
    {
        return showDeprecationWarnings;
    }

    @Config(advanced = true, hidden = true)
    @Mapping({ "compiler", "show-deprecation-warnings" })
    public void setCompilerShowDeprecationWarnings(ConfigurationValue cv, boolean show)
    {
        this.showDeprecationWarnings = show;
    }

    //
    // 'compiler.show-shadowed-device-font-warnings' option
    //
    @Config
    @Mapping({ "compiler", "show-shadowed-device-font-warnings" })
    @RoyaleOnly
    public void setCompilerShowShadowedDeviceFontWarnings(ConfigurationValue cv, boolean show)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    //
    // 'compiler.show-unused-type-selector-warnings' option
    //

    private boolean showUnusedTypeSelectorWarnings = true;

    public boolean showUnusedTypeSelectorWarnings()
    {
        return showUnusedTypeSelectorWarnings;
    }

    //
    // 'compiler.show-multiple-definition-warnings' option
    //

    private boolean showMultipleDefinitionWarnings = true;

    public boolean showMultipleDefinitionWarnings()
    {
        return showMultipleDefinitionWarnings;
    }

    @Config
    @Mapping({ "compiler", "show-unused-type-selector-warnings" })
    @RoyaleOnly
    public void setCompilerShowUnusedTypeSelectorWarnings(ConfigurationValue cv, boolean show)
    {
        this.showUnusedTypeSelectorWarnings = show;
    }

    //
    // 'compiler.source-path' option
    //

    /**
     * Source path elements searched for ActionScript class files, possibly containing a {locale} token.
     */
    private final List<String> unexpandedSourcePath = new ArrayList<String>();

    /**
     * Directories searched for ActionScript class files. The specified compiler.source-path can have path elements
     * which contain a special {locale} token. If you compile for a single locale, this token is replaced by the
     * specified locale. If you compile for multiple locales, any path element with the {locale} token is ignored,
     * because we do not support compiling, for example, both en_US and ja_JP versions of MyComponent into the same SWF.
     * A path element with {locale} is similarly ignored if you compile for no locale.
     */
    private final List<String> sourcePath = new ArrayList<String>();

    /** Context object for "source-path" option. */
    private ConfigurationValue sourcePathContext = null;

    public List<String> getCompilerSourcePath()
    {
        return sourcePath;
    }

    /**
     * Get the source paths computed from the given {@code locale}. The locale must be included in the configuration.
     * 
     * @param locale Locale name.
     * @return Source paths computed from the given {@code locale}.
     * @throws CannotOpen Error resolving one of the paths from this locale.
     */
    public ImmutableList<String> getCompilerResourceBundlePathForLocale(String locale) throws CannotOpen
    {
        assert locales.contains(locale) : "Locale is not configured: " + locale;
        return expandTokens(unexpandedSourcePath, ImmutableSet.of(locale), sourcePathContext,
                !reportMissingCompilerLibraries);
    }

    @Config(allowMultiple = true)
    @Arguments(Arguments.PATH_ELEMENT)
    @SoftPrerequisites("locale")
    public void setCompilerSourcePath(ConfigurationValue cv, String[] paths) throws ConfigurationException
    {
        final List<String> pathList = Arrays.asList(paths);
        unexpandedSourcePath.addAll(pathList);

        final ImmutableList<String> resolvedSourcePaths = expandTokens(pathList, locales, cv);
        assertThatAllPathsAreDirectories(resolvedSourcePaths, cv);
        sourcePath.addAll(resolvedSourcePaths);

        sourcePathContext = cv;
    }

    /**
     * Check that all paths in the path list are directories.
     * 
     * @param paths A list of paths.
     * @param cv Context.
     * @throws NotDirectory Path is not a directory exception.
     */
    public static void assertThatAllPathsAreDirectories(final List<String> paths, final ConfigurationValue cv)
            throws NotDirectory
    {
        assert paths != null : "Expected path list.";
        assert cv != null : "Expected ConfigurationValue as context.";

        for (final String path : paths)
        {
            final File file = new File(path);
            if (!file.isDirectory())
                throw new NotDirectory(path, cv.getVar(), cv.getSource(), cv.getLine());
        }
    }

    public static ConfigurationInfo getCompilerSourcePathInfo()
    {
        return new ConfigurationInfo(-1, new String[] { "path-element" })
        {
            @Override
            public boolean allowMultiple()
            {
                return true;
            }

            @Override
            public boolean isPath()
            {
                return true;
            }
        };
    }

    //
    // 'compiler.strict' option
    //

    /**
     * Run the AS3 compiler in strict mode
     */
    private boolean strict;

    public boolean strict()
    {
        return this.strict;
    }

    @Config
    @Mapping({ "compiler", "strict" })
    public void setCompilerStrict(ConfigurationValue cv, boolean strict)
    {
        this.strict = strict;
    }

    //
    // 'compiler.suppress-warnings-in-incremental' option (incomplete)
    //

    // for Zorn
    //
    // When doing incremental compilation, the compiler doesn't recompile codes that are previously compiled with
    // warnings. It only outputs the warning messages so as to remind users of the warnings.
    //
    // The command-line tool and Zorn work differently in that Zorn keeps the warning logger while the commnad-line
    // tool, of course, can't keep the warning logger alive...
    //
    // Zorn needs this flag to tell the compiler not to output warnings again in incremental compilations because
    // it keeps its own log.
    private boolean suppressWarningsInIncremental = false;

    public boolean suppressWarningsInIncremental()
    {
        return suppressWarningsInIncremental;
    }

    public void setCompilerSuppressWarningsInIncremental(boolean b)
    {
        suppressWarningsInIncremental = b;
    }

    //
    // 'compiler.theme' option
    //

    private List<String> themeFiles = null;

    /**
     * Get normalized theme file paths. If a the compiler is in "Flex 3 compatibility" mode and only "Spark" theme is
     * used, it will be replaced with the legacy "Halo" theme.
     * 
     * @return A list of normalized paths to the theme files.
     */
    public List<String> getCompilerThemeFiles()
    {
        if (themeFiles == null)
            return EMPTY_STRING_LIST;

        final boolean isVersion3OrEarlier = getCompilerMxmlCompatibilityVersion() <= MXML_VERSION_3_0;
        final boolean hasOnlyOneThemeFile = themeFiles.size() == 1;
        if (isVersion3OrEarlier && hasOnlyOneThemeFile)
        {
            // Swap in the default Flex 3 theme of Halo.
            final String path = FilenameUtils.normalize(themeFiles.get(0), true);
            final String sparkPath = "/themes/Spark/spark.css";

            if (path.endsWith(sparkPath))
            {
                int index = path.indexOf(sparkPath);
                final String haloPath = path.substring(0, index) + "/themes/Halo/halo.swc";
                themeFiles.set(0, FilenameNormalization.normalize(haloPath));
            }
        }
        return themeFiles;
    }

    @Config(allowMultiple = true)
    @Mapping({ "compiler", "theme" })
    @Arguments("filename")
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerTheme(ConfigurationValue cv, List<String> paths) throws CannotOpen
    {
        // Use "resolvePathsStrict()" if invalid theme file can't be ignored.
        final ImmutableList<String> resolved = resolvePathsStrict(ImmutableList.copyOf(paths), cv);

        if (themeFiles == null)
            themeFiles = new ArrayList<String>();
        themeFiles.addAll(resolved);
    }

    //
    // 'compiler.defaults-css-files' option
    //

    private Deque<String> defaultsCSSFiles = new ArrayDeque<String>();

    /**
     * List of filenames of defaults style stylesheets (css only).
     * <p>
     * <b>For example:</b><br>
     * <code>-defaults-css-files=[A, B, C]</code><br>
     * Then, 'A' should have precedence over 'B', then 'C', then SWCs defaultsCssFiles should have the order: SWCS, C,
     * B, A
     * 
     * @see #setDefaultsCSSFiles
     */
    public Deque<String> getDefaultsCSSFiles()
    {
        return defaultsCSSFiles;
    }

    /**
     * Inserts CSS files into the output the same way that a per-SWC defaults.css file works, but without having to
     * re-archive the SWC file to test each change.
     * <p>
     * CSS files included in the output with this option have a higher precedence than default CSS files in existing
     * SWCs. For example, a CSS file included with this option overrides definitions in framework.swc's defaults.css
     * file, but it has the same overall precedence as other included CSS files inside the SWC file.
     * <p>
     * This option does not actually insert the CSS file into the SWC file; it simulates it. When you finish developing
     * the CSS file, you should rebuild the SWC file with the new integrated CSS file.
     * <p>
     * This option takes one or more files. The precedence for multiple CSS files included with this option is from
     * first to last.
     */
    @Config(allowMultiple = true, advanced = true)
    @Mapping({ "compiler", "defaults-css-files" })
    @Arguments("filename")
    @InfiniteArguments
    @RoyaleOnly
    public void setDefaultsCSSFiles(ConfigurationValue cv, List<String> paths) throws CannotOpen
    {
        final ImmutableList<String> resolved = resolvePathsStrict(ImmutableList.copyOf(paths), cv);
        for (final String path : resolved)
        {
            defaultsCSSFiles.addFirst(path);
        }
    }

    //
    // 'compiler.exclude-defaults-css-files' option
    //

    private Deque<String> excludeDefaultsCSSFiles = new ArrayDeque<String>();

    /**
     * List of filenames to exclude from list of defaults style stylesheets (css only).
     * For defaults.css files in a SWC, use HTML.swc:defaults.css where HTML is the
     * name of a SWC.
     * <p>
     * <b>For example:</b><br>
     * <code>-exclude-defaults-css-files=[A, B, C]</code><br>
     * Then, 'A' should have precedence over 'B', then 'C', then SWCs defaultsCssFiles should have the order: SWCS, C,
     * B, A
     * 
     * @see #setExcludeDefaultsCSSFiles
     */
    public Deque<String> getExcludeDefaultsCSSFiles()
    {
        return excludeDefaultsCSSFiles;
    }

    /**
     * Excludes CSS files from the output.
     * <p>
     * This option takes one or more files. The precedence for multiple CSS files included with this option is from
     * first to last.
     */
    @Config(allowMultiple = true, advanced = true)
    @Mapping({ "compiler", "exclude-defaults-css-files" })
    @Arguments("filename")
    @InfiniteArguments
    @RoyaleOnly
    public void setExcludeDefaultsCSSFiles(ConfigurationValue cv, List<String> paths) throws CannotOpen
    {
        for (final String path : paths)
        {
            excludeDefaultsCSSFiles.addFirst(path);
        }
    }

    /**
     * Location of theme style stylesheets (css only, configured via themefiles above).
     */
    private List<IFileSpecification> themeCssFiles = new LinkedList<IFileSpecification>();

    public List<IFileSpecification> getCompilerThemeCssFiles()
    {
        return themeCssFiles;
    }

    public void addThemeCssFiles(List<IFileSpecification> files)
    {
        themeCssFiles.addAll(files);
    }

    //
    // 'compiler.use-resource-bundle-metadata' option
    //

    @Config(removed = true)
    @Mapping({ "compiler", "use-resource-bundle-metadata" })
    public void setCompilerUseResourceBundleMetadata(ConfigurationValue cv, boolean b)
    {
    }

    //
    // 'compiler.verbose-stacktraces' option
    //

    private boolean verboseStacktraces;

    // from as3 and mxml configuration interface
    public boolean debug()
    {
        // the debug() in as3 and mxml configuration maps to stacktraceLineNumbers
        return generateDebugTags;
    }

    public boolean release() {
        return !generateDebugTags;
    }

    @Config
    @Mapping({ "compiler", "verbose-stacktraces" })
    public void setCompilerVerboseStacktraces(ConfigurationValue cv, boolean verboseStacktraces)
    {
        if (generateDebugTags)
        {
            this.verboseStacktraces = true;
        }
        else
        {
            this.verboseStacktraces = verboseStacktraces;
        }
    }

    //
    // 'compiler.warn-array-tostring-changes' option
    //

    private boolean warn_array_tostring_changes = false;

    public boolean warn_array_tostring_changes()
    {
        return warn_array_tostring_changes;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-array-tostring-changes" })
    public void setCompilerWarnArrayTostringChanges(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_array_tostring_changes)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-assignment-within-conditional' option
    //

    private boolean warn_assignment_within_conditional = true;

    public boolean warn_assignment_within_conditional()
    {
        return warn_assignment_within_conditional;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-assignment-within-conditional" })
    public void setCompilerWarnAssignmentWithinConditional(ConfigurationValue cv, boolean b)
    {
        warn_assignment_within_conditional = b;
    }

    //
    // 'compiler.warn-bad-array-cast' option
    //

    private boolean warn_bad_array_cast = true;

    public boolean warn_bad_array_cast()
    {
        return warn_bad_array_cast;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-array-cast" })
    public void setCompilerWarnBadArrayCast(ConfigurationValue cv, boolean b)
    {
        warn_bad_array_cast = b;
    }

    //
    // 'compiler.warn-bad-bool-assignment' option
    //

    private boolean warn_bad_bool_assignment = true;

    public boolean warn_bad_bool_assignment()
    {
        return warn_bad_bool_assignment;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-bool-assignment" })
    public void setCompilerWarnBadBoolAssignment(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_bad_bool_assignment)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-bad-date-cast' option
    //

    private boolean warn_bad_date_cast = true;

    public boolean warn_bad_date_cast()
    {
        return warn_bad_date_cast;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-date-cast" })
    public void setCompilerWarnBadDateCast(ConfigurationValue cv, boolean b)
    {
        warn_bad_date_cast = b;
    }

    //
    // 'compiler.warn-bad-es3-type-method' option
    //

    private boolean warn_bad_es3_type_method = true;

    public boolean warn_bad_es3_type_method()
    {
        return warn_bad_es3_type_method;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-es3-type-method" })
    public void setCompilerWarnBadEs3TypeMethod(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_bad_es3_type_method)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-bad-es3-type-prop' option
    //

    private boolean warn_bad_es3_type_prop = true;

    public boolean warn_bad_es3_type_prop()
    {
        return warn_bad_es3_type_prop;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-es3-type-prop" })
    public void setCompilerWarnBadEs3TypeProp(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_bad_es3_type_prop)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-bad-nan-comparison' option
    //

    private boolean warn_bad_nan_comparison = true;

    public boolean warn_bad_nan_comparison()
    {
        return warn_bad_nan_comparison;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-nan-comparison" })
    public void setCompilerWarnBadNanComparison(ConfigurationValue cv, boolean b)
    {
        warn_bad_nan_comparison = b;
    }

    //
    // 'compiler.warn-bad-null-assignment' option
    //

    private boolean warn_bad_null_assignment = true;

    public boolean warn_bad_null_assignment()
    {
        return warn_bad_null_assignment;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-null-assignment" })
    public void setCompilerWarnBadNullAssignment(ConfigurationValue cv, boolean b)
    {
        warn_bad_null_assignment = b;
    }

    //
    // 'compiler.warn-bad-null-comparison' option
    //

    private boolean warn_bad_null_comparison = true;

    public boolean warn_bad_null_comparison()
    {
        return warn_bad_null_comparison;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-null-comparison" })
    public void setCompilerWarnBadNullComparison(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_bad_null_comparison)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-bad-undefined-comparison' option
    //

    private boolean warn_bad_undefined_comparison = true;

    public boolean warn_bad_undefined_comparison()
    {
        return warn_bad_undefined_comparison;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-bad-undefined-comparison" })
    public void setCompilerWarnBadUndefinedComparison(ConfigurationValue cv, boolean b)
    {
        warn_bad_undefined_comparison = b;
    }

    //
    // 'compiler.warn-boolean-constructor-with-no-args' option
    //

    private boolean warn_boolean_constructor_with_no_args = false;

    public boolean warn_boolean_constructor_with_no_args()
    {
        return warn_boolean_constructor_with_no_args;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-boolean-constructor-with-no-args" })
    public void setCompilerWarnBooleanConstructorWithNoArgs(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_boolean_constructor_with_no_args)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-changes-in-resolve' option
    //

    private boolean warn_changes_in_resolve = false;

    public boolean warn_changes_in_resolve()
    {
        return warn_changes_in_resolve;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-changes-in-resolve" })
    public void setCompilerWarnChangesInResolve(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_changes_in_resolve)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-class-is-sealed' option
    //

    private boolean warn_class_is_sealed = true;

    public boolean warn_class_is_sealed()
    {
        return warn_class_is_sealed;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-class-is-sealed" })
    public void setCompilerWarnClassIsSealed(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_class_is_sealed)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-const-not-initialized' option
    //

    private boolean warn_const_not_initialized = true;

    public boolean warn_const_not_initialized()
    {
        return warn_const_not_initialized;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-const-not-initialized" })
    public void setCompilerWarnConstNotInitialized(ConfigurationValue cv, boolean b)
    {
        warn_const_not_initialized = b;
    }

    //
    // 'compiler.warn-constructor-returns-value' option
    //

    private boolean warn_constructor_returns_value = false;

    public boolean warn_constructor_returns_value()
    {
        return warn_constructor_returns_value;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-constructor-returns-value" })
    public void setCompilerWarnConstructorReturnsValue(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_constructor_returns_value)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-deprecated-event-handler-error' option
    //

    private boolean warn_deprecated_event_handler_error = false;

    public boolean warn_deprecated_event_handler_error()
    {
        return warn_deprecated_event_handler_error;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-deprecated-event-handler-error" })
    public void setCompilerWarnDeprecatedEventHandlerError(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_deprecated_event_handler_error)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-deprecated-function-error' option
    //

    private boolean warn_deprecated_function_error = true;

    public boolean warn_deprecated_function_error()
    {
        return warn_deprecated_function_error;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-deprecated-function-error" })
    public void setCompilerWarnDeprecatedFunctionError(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_deprecated_function_error)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-deprecated-property-error' option
    //

    private boolean warn_deprecated_property_error = true;

    public boolean warn_deprecated_property_error()
    {
        return warn_deprecated_property_error;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-deprecated-property-error" })
    public void setCompilerWarnDeprecatedPropertyError(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_deprecated_property_error)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-duplicate-argument-names' option
    //

    private boolean warn_duplicate_argument_names = true;

    public boolean warn_duplicate_argument_names()
    {
        return warn_duplicate_argument_names;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-duplicate-argument-names" })
    public void setCompilerWarnDuplicateArgumentNames(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_duplicate_argument_names)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-duplicate-variable-def' option
    //

    private boolean warn_duplicate_variable_def = true;

    public boolean warn_duplicate_variable_def()
    {
        return warn_duplicate_variable_def;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-duplicate-variable-def" })
    public void csetCompilerWarnDuplicateVariableDef(ConfigurationValue cv, boolean b)
    {
        warn_duplicate_variable_def = b;
    }

    //
    // 'compiler.warn-for-var-in-changes' option
    //

    private boolean warn_for_var_in_changes = false;

    public boolean warn_for_var_in_changes()
    {
        return warn_for_var_in_changes;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-for-var-in-changes" })
    public void setCompilerWarnForVarInChanges(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_for_var_in_changes)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-import-hides-class' option
    //

    private boolean warn_import_hides_class = true;

    public boolean warn_import_hides_class()
    {
        return warn_import_hides_class;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-import-hides-class" })
    public void setCompilerWarnImportHidesClass(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_import_hides_class)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-instance-of-changes' option
    //

    private boolean warn_instance_of_changes = true;

    public boolean warn_instance_of_changes()
    {
        return warn_instance_of_changes;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-instance-of-changes" })
    public void setCompilerWarnInstanceOfChanges(ConfigurationValue cv, boolean b)
    {
        warn_instance_of_changes = b;
    }

    //
    // 'compiler.warn-internal-error' option
    //

    private boolean warn_internal_error = true;

    public boolean warn_internal_error()
    {
        return warn_internal_error;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-internal-error" })
    public void setCompilerWarnInternalError(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_internal_error)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-level-not-supported' option
    //

    private boolean warn_level_not_supported = true;

    public boolean warn_level_not_supported()
    {
        return warn_level_not_supported;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-level-not-supported" })
    public void setCompilerWarnLevelNotSupported(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_level_not_supported)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-missing-namespace-decl' option
    //

    private boolean warn_missing_namespace_decl = true;

    public boolean warn_missing_namespace_decl()
    {
        return warn_missing_namespace_decl;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-missing-namespace-decl" })
    public void setCompilerWarnMissingNamespaceDecl(ConfigurationValue cv, boolean b)
    {
        warn_missing_namespace_decl = b;
    }

    //
    // 'compiler.warn-negative-uint-literal' option
    //

    private boolean warn_negative_uint_literal = true;

    public boolean warn_negative_uint_literal()
    {
        return warn_negative_uint_literal;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-negative-uint-literal" })
    public void setCompilerWarnNegativeUintLiteral(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_negative_uint_literal)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-no-constructor' option
    //

    private boolean warn_no_constructor = false;

    public boolean warn_no_constructor()
    {
        return warn_no_constructor;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-no-constructor" })
    public void setCompilerWarnNoConstructor(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_no_constructor)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-no-explicit-super-call-in-constructor' option
    //

    private boolean warn_no_explicit_super_call_in_constructor = false;

    public boolean warn_no_explicit_super_call_in_constructor()
    {
        return warn_no_explicit_super_call_in_constructor;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-no-explicit-super-call-in-constructor" })
    public void setCompilerWarnNoExplicitSuperCallInConstructor(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_no_explicit_super_call_in_constructor)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-no-type-decl' option
    //

    private boolean warn_no_type_decl = true;

    public boolean warn_no_type_decl()
    {
        return warn_no_type_decl;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-no-type-decl" })
    public void setCompilerWarnNoTypeDecl(ConfigurationValue cv, boolean b)
    {
        warn_no_type_decl = b;
    }

    //
    // 'compiler.warn-number-from-string-changes' option
    //

    private boolean warn_number_from_string_changes = false;

    public boolean warn_number_from_string_changes()
    {
        return warn_number_from_string_changes;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-number-from-string-changes" })
    public void setCompilerWarnNumberFromStringChanges(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_number_from_string_changes)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-scoping-change-in-this' option
    //

    private boolean warn_scoping_change_in_this = false;

    public boolean warn_scoping_change_in_this()
    {
        return warn_scoping_change_in_this;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-scoping-change-in-this" })
    public void setCompilerWarnScopingChangeInThis(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_scoping_change_in_this)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-slow-text-field-addition' option
    //

    private boolean warn_slow_text_field_addition = true;

    public boolean warn_slow_text_field_addition()
    {
        return warn_slow_text_field_addition;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-slow-text-field-addition" })
    public void setCompilerWarnSlowTextFieldAddition(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_slow_text_field_addition)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-unlikely-function-value' option
    //

    private boolean warn_unlikely_function_value = true;

    public boolean warn_unlikely_function_value()
    {
        return warn_unlikely_function_value;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-unlikely-function-value" })
    public void setCompilerWarnUnlikelyFunctionValue(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_unlikely_function_value)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-xml-class-has-changed' option
    //

    private boolean warn_xml_class_has_changed = false;

    public boolean warn_xml_class_has_changed()
    {
        return warn_xml_class_has_changed;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-xml-class-has-changed" })
    public void setCompilerWarnXmlClassHasChanged(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != warn_xml_class_has_changed)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // 'compiler.warn-this-within-closure' option
    //

    private boolean warn_this_within_closure = true;

    public boolean warn_this_within_closure()
    {
        return warn_this_within_closure;
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "warn-this-within-closure" })
    public void setCompilerWarnThisWithinClosure(ConfigurationValue cv, boolean b)
    {
        warn_this_within_closure = b;
    }

    //
    // compiler.generate-abstract-syntax-tree
    //

    private boolean generateAbstractSyntaxTree = true;

    @Config(hidden = true)
    @Mapping({ "compiler", "generate-abstract-syntax-tree" })
    public void setCompilerGenerateAbstractSyntaxTree(ConfigurationValue cv, boolean b)
    {
        generateAbstractSyntaxTree = b;
    }

    public boolean getCompilerGenerateAbstractSyntaxTree()
    {
        return generateAbstractSyntaxTree;
    }

    //
    // 'compiler.isolateStyles' option
    //

    /**
     * Allow the user to decide if the compiled application/module should have its own style manager. Turn off isolate
     * styles for compatibility less than 4.0.
     */
    private boolean isolateStyles = true;

    public boolean getCompilerIsolateStyles()
    {
        return isolateStyles && (getCompilerCompatibilityVersion() >= MXML_VERSION_4_0);
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "isolate-styles" })
    @RoyaleOnly
    public void setCompilerIsolateStyles(ConfigurationValue cv, boolean isolateStyles)
    {
        this.isolateStyles = isolateStyles;
    }

    //
    // 'compiler.compress' option (default is true)
    //

    private boolean useCompression = true;

    @Config
    @Mapping({ "compiler", "compress" })
    public void setCompress(ConfigurationValue cv, boolean useCompression)
    {
        this.useCompression = useCompression;
    }

    /**
     * Setting {@code -compiler.compress=false} will force compiler not to compress the output SWF.
     */
    public boolean useCompression()
    {
        return this.useCompression;
    }

    // ATTENTION: Please set default values in DefaultsConfigurator.

    private static final String LOCALE_TOKEN = "{locale}";
    private static final String TARGET_PLAYER_MAJOR_VERSION_TOKEN = "{targetPlayerMajorVersion}";
    private static final String TARGET_PLAYER_MINOR_VERSION_TOKEN = "{targetPlayerMinorVersion}";
    private static final String TARGET_PLAYER_MAJOR_VERSION_TOKEN_REGEX_ESCAPED = Pattern.quote(
            TARGET_PLAYER_MAJOR_VERSION_TOKEN);
    private static final String TARGET_PLAYER_MINOR_VERSION_TOKEN_REGEX_ESCAPED = Pattern.quote(
            TARGET_PLAYER_MINOR_VERSION_TOKEN);

    // Special Case for Apache.  These are not currently exposed with command line options.
    public static final String PLAYERGLOBAL_HOME_TOKEN = "{playerglobalHome}";
    public static final String AIR_HOME_TOKEN = "{airHome}";
    public static final String FLEX_VERSION_TOKEN = "{royaleVersion}";

    public static final String STRICT = "compiler.strict";
    public static final String AS3 = "compiler.as3";
    public static final String ES = "compiler.es";

    public ImmutableList<String> expandTokens(final Iterable<String> pathElements, final Iterable<String> locales,
            final ConfigurationValue configurationValue)
    {
        return expandTokens(pathElements, locales, configurationValue, false);
    }

    /**
     * All path-tokens get expanded from this method, as of now, {locale} and {targetPlayerMajorVersion} Replaces
     * instances of "{targetPlayerMajorVersion}" and "{targetPlayerMinorVersion}" with configured value. Expands the
     * {locale} token in a list of path elements for the source-path or library-path. The treatment of a path element
     * containing "{locale}" depends on whether we are processing a source-path or a library-path, and on whether we are
     * compiling for a single locale, multiple locales, or no locale:
     * 
     * <pre>
     * -source-path=foo,bar/{locale},baz -locale=en_US 
     * -> foo,bar/en_US,baz 
     * 
     * -source-path=foo,bar/{locale},baz -locale=en_US,ja_JP
     * -> foo,bar/en_US,bar/ja_JP,baz 
     * 
     * -source-path=foo,bar/{locale},baz -locale=
     * -> foo,baz 
     * 
     * -library-path=foo,bar/{locale},baz -locale=en_US 
     * -> foo,bar/en_US,baz 
     * 
     * -library-path=foo,bar/{locale},baz -locale=en_US,ja_JP
     * -> foo,bar/en_US,bar/ja_JP,baz 
     * 
     * -library-path=foo,bar/{locale},baz -locale=
     * -> foo,baz
     * </pre>
     * 
     * @param pathElements A list of unprocessed paths from configuration values.
     * @param locales A set of locales.
     * @param configurationValue Context.
     * @param returnMissingFiles controls whether or not files that do not exist are included in the list of expanded
     *        files. Pass true to include files that do not exist, false otherwise.
     * @return A list of normalized and resolved file paths.
     * @throws CannotOpen
     */
    protected ImmutableList<String> expandTokens(final Iterable<String> pathElements, final Iterable<String> locales,
            final ConfigurationValue configurationValue, final boolean returnMissingFiles)
    {
        assert pathElements != null : "Expected path list.";
        assert locales != null : "Expected locales.";
        assert configurationValue != null : "Expected ConfigurationValue as a context.";

        String targetPlayerMajorVersion = String.valueOf(getTargetPlayerMajorVersion());
        String targetPlayerMinorVersion = String.valueOf(getTargetPlayerMinorVersion());

        // Expand target player and locale tokens.
        final ImmutableList.Builder<String> resolvedPaths = new ImmutableList.Builder<String>();
        for (String pathElement : pathElements)
        {
            pathElement = expandRuntimeTokens(pathElement, configurationValue.getBuffer());
            String royaleVersion =  getClass().getPackage().getImplementationVersion();
            if (royaleVersion != null)
            {
                pathElement = pathElement.replace(FLEX_VERSION_TOKEN, royaleVersion);
            }

            String playerExpandedPath = pathElement.replaceAll(TARGET_PLAYER_MAJOR_VERSION_TOKEN_REGEX_ESCAPED,
                    targetPlayerMajorVersion).replaceAll(TARGET_PLAYER_MINOR_VERSION_TOKEN_REGEX_ESCAPED,
                            targetPlayerMinorVersion);

            try
            {
                if (playerExpandedPath.contains(LOCALE_TOKEN))
                {
                    for (final String locale : locales)
                    {
                        final String expandedPath = playerExpandedPath.replace(LOCALE_TOKEN, locale);
                        String resolvedPath = resolvePathStrict(expandedPath, configurationValue);
                        resolvedPaths.add(resolvedPath);

                        //Add this to the locale dependent sources map
                        localeDependentSources.put(resolvedPath, locale);
                    }
                }
                else
                {
                    String resolvedPath = resolvePathStrict(playerExpandedPath, configurationValue, returnMissingFiles);
                    resolvedPaths.add(resolvedPath);
                }
            }
            catch (CannotOpen e)
            {
                // Making an exception here and catching this fatal error.
                // This is an exception because library paths come thru this
                // code path and we don't want to throw all of the libraries 
                // out because one of the paths is bad. We want to load as 
                // many libraries as we can an report the ones we couldn't load.
                configurationProblems.add(new ConfigurationProblem(e));
            }
        }

        return resolvedPaths.build();
    }

    /**
     * Replaces instances of {playerglobalHome} and {airHome}. Values can come from either ../env.properties (relative
     * to jar file) or environment variables. The property file values have precedence. The pairs are
     * env.PLAYERGLOBAL_HOME and PLAYERGLOBAL_HOME, and, env.AIR_HOME and AIR_HOME.
     */
    private String expandRuntimeTokens(String pathElement, ConfigurationBuffer buffer)
    {
        // Look at property file first, if it exists, and see if the particular property
        // is defined.  If not found, then look for the environment variable.
        // If there is neither use libs/player
        Properties envProperties = loadEnvPropertyFile();

        String playerglobalHome = envProperties != null
                ? envProperties.getProperty("env.PLAYERGLOBAL_HOME", System.getenv("PLAYERGLOBAL_HOME"))
                : System.getenv("PLAYERGLOBAL_HOME");

        if (playerglobalHome == null)
            playerglobalHome = buffer.getToken("env.PLAYERGLOBAL_HOME");
        if (playerglobalHome == null)
            playerglobalHome = "libs/player";

        String airHome = envProperties != null ? envProperties.getProperty("env.AIR_HOME", System.getenv("AIR_HOME"))
                : System.getenv("AIR_HOME");

        if (airHome == null)
            airHome = buffer.getToken("env.AIR_HOME");
        if (airHome == null)
            airHome = "..";

        pathElement = pathElement.replace(PLAYERGLOBAL_HOME_TOKEN, playerglobalHome);
        pathElement = pathElement.replace(AIR_HOME_TOKEN, airHome);

        return pathElement;
    }

    /**
     * Load the env.properties file from the classpath.
     * 
     * @return null if env.properties does not exist in classpath or could not be loaded
     */
    private Properties loadEnvPropertyFile()
    {
        Properties properties = null;
        InputStream in = null;

        try
        {
            in = getClass().getClassLoader().getResourceAsStream("env.properties");
            if (in == null)
            {
                try
                {
                    File f = new File("../env.properties");
                    in = new FileInputStream(f);
                    properties = new Properties();
                    properties.load(in);
                    in.close();
                    return properties;
                }
                catch (FileNotFoundException e)
                {
                    try
                    {
                        File f = new File("unittest.properties");
                        in = new FileInputStream(f);
                        properties = new Properties();
                        properties.load(in);
                        in.close();
                        properties.setProperty("env.PLAYERGLOBAL_HOME", properties.getProperty("PLAYERGLOBAL_HOME"));
                        properties.setProperty("env.AIR_HOME", properties.getProperty("AIR_HOME"));
                        properties.setProperty("env.PLAYERGLOBAL_VERSION", properties.getProperty("PLAYERGLOBAL_VERSION"));
                        return properties;
                    }
                    catch (FileNotFoundException e1)
                    {
                        return null;
                    }
                    catch (IOException e1)
                    {
                        return null;
                    }
                }
                catch (IOException e)
                {
                    return null;
                }
            }

            properties = new Properties();
            properties.load(in);
            in.close();
        }
        catch (Exception e)
        {
        }

        return properties;
    }

    private Map<String, String> localeDependentSources = new HashMap<String, String>();

    /**
     * Returns a map that stores locale dependent files. For each item in this map, key is the path of the resource and
     * value id the locale it belongs to.
     */
    public Map<String, String> getLocaleDependentSources()
    {
        return localeDependentSources;
    }

    /**
     * 
     * @param path A path to resolve.
     * @param cv Configuration context.
     * @return A single normalized resolved file. If the path could be expanded into more than one path, then use
     *         {@link resolvePathsStrict}
     * @throws CannotOpen
     */
    protected String resolvePathStrict(final String path, final ConfigurationValue cv) throws CannotOpen
    {
        return resolvePathStrict(path, cv, false);
    }

    /**
     * Resolve a single path. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param path A path to resolve.
     * @param cv Configuration context.
     * @param returnMissingFiles Determines if the CannotOpen exception is thrown if a file does not exist. Pass true to
     *        disable exceptions and return files that do not exist. Pass false to throw exceptions.
     * @return A single normalized resolved file. If the path could be expanded into more than one path, then use
     *         {@link resolvePathsStrict}.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private String resolvePathStrict(final String path, final ConfigurationValue cv, final boolean returnMissingFiles)
            throws CannotOpen
    {
        ImmutableList<String> singletonPath = ImmutableList.of(path);
        ImmutableList<String> results = resolvePathsStrict(singletonPath, cv, returnMissingFiles);
        return results.get(0);
    }

    /**
     * Resolve a list of paths. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param paths A list of paths to resolve.
     * @param cv Configuration context.
     * @return A list of normalized resolved file paths.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private ImmutableList<String> resolvePathsStrict(final ImmutableList<String> paths, final ConfigurationValue cv)
            throws CannotOpen
    {
        return resolvePathsStrict(paths, cv, false);
    }

    /**
     * Resolve a list of paths. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param paths A list of paths to resolve.
     * @param cv Configuration context.
     * @param returnMissingFiles Determines if the CannotOpen exception is thrown if a file does not exist. Pass true to
     *        disable exceptions and return files that do not exist. Pass false to throw exceptions.
     * @return A list of normalized resolved file paths.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private ImmutableList<String> resolvePathsStrict(final ImmutableList<String> paths, final ConfigurationValue cv,
            final boolean returnMissingFiles) throws CannotOpen
    {
        assert paths != null : "Expected paths";
        assert cv != null : "Require ConfigurationValue as context.";

        final ImmutableList.Builder<String> resolvedPathsBuilder = new ImmutableList.Builder<String>();
        for (String processedPath : paths)
        {
            if (cv.getContext() != null)
            {
                boolean isAbsolute = new File(processedPath).isAbsolute();
                if (!isAbsolute)
                    processedPath = new File(cv.getContext(), processedPath).getAbsolutePath();
            }
            if (processedPath.contains("*"))
            {
                // if contains wild card, just prove the part before the wild card is valid
                int c = processedPath.lastIndexOf(File.separator, processedPath.indexOf("*"));
                if (c != -1)
                    processedPath = processedPath.substring(0, c);
            }
            if (!processedPath.contains(".swc:"))
            {
	            final File fileSpec = pathResolver.resolve(processedPath);
	            if (!returnMissingFiles && !fileSpec.exists())
	            {
	                throw new CannotOpen(FilenameNormalization.normalize(processedPath), cv.getVar(), cv.getSource(),
	                        cv.getLine());
	            }
	            resolvedPathsBuilder.add(fileSpec.getAbsolutePath());
            }
            else
                resolvedPathsBuilder.add(processedPath);
        }
        return resolvedPathsBuilder.build();
    }

    //////////////////////////////////////////////////////////////////////////
    // compiler.extensions.*
    //////////////////////////////////////////////////////////////////////////

    //
    // 'compiler.extensions.extension' option
    //

    /**
     * Configures a list of many extensions mapped to a single Extension URI.
     * <extension> <extension>something-extension.jar</extension>
     * <parameters>version=1.1,content=1.2</parameters> </extension>
     * 
     * @param cv The configuration value context.
     * @param pathlist A List of values for the Extension element, with the first item expected to be the uri and the
     *        remaining are extension paths.
     * @throws CannotOpen When no arg is provided or when the jar does not exist.
     */
    @Config(allowMultiple = true, removed = true)
    @Mapping({ "compiler", "extensions", "extension" })
    @Arguments({ "extension", "parameters" })
    @InfiniteArguments
    public void setExtension(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
    }

    //////////////////////////////////////////////////////////////////////////
    // compiler.fonts.*
    //////////////////////////////////////////////////////////////////////////

    @Config
    @Mapping({ "compiler", "fonts", "advanced-anti-aliasing" })
    @RoyaleOnly
    public void setCompilerFontsAdvancedAntiAliasing(ConfigurationValue cv, boolean val)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config(allowMultiple = true, advanced = true)
    @Mapping({ "compiler", "fonts", "languages", "language-range" })
    @Arguments({ "lang", "range" })
    @RoyaleOnly
    public void setCompilerFontsLanguagesLanguageRange(ConfigurationValue cv, String lang, String range)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config
    @Mapping({ "compiler", "fonts", "local-fonts-snapshot" })
    @RoyaleOnly
    public void setCompilerFontsLocalFontsSnapshot(ConfigurationValue cv, String localFontsSnapshotPath)
            throws CannotOpen
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config
    @Mapping({ "compiler", "fonts", "local-font-paths" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerFontsLocalFontPaths(ConfigurationValue cv, List<String> list) throws CannotOpen
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config(advanced = true)
    @Mapping({ "compiler", "fonts", "managers" })
    @Arguments("manager-class")
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerFontsManagers(ConfigurationValue cv, List<String> list)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config
    @Mapping({ "compiler", "fonts", "max-cached-fonts" })
    @RoyaleOnly
    public void setCompilerFontsMaxCachedFonts(ConfigurationValue cv, String val)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    @Config
    @Mapping({ "compiler", "fonts", "max-glyphs-per-face" })
    @RoyaleOnly
    public void setCompilerFontsMaxGlyphsPerFace(ConfigurationValue cv, String val)
    {
        // intentionally do nothing here as feature removed, but don't annotate as removed
        // as to not generate warnings for royale-config's which still set this options
    }

    //////////////////////////////////////////////////////////////////////////
    // compiler.namespaces
    //////////////////////////////////////////////////////////////////////////

    private List<MXMLNamespaceMapping> manifestMappings;

    public List<MXMLNamespaceMapping> getCompilerNamespacesManifestMappings()
    {
        return manifestMappings;
    }

    /**
     * Configures a list of many manifests mapped to a single namespace URI.
     * <namespace> <uri>library:adobe/flex/something</uri> <manifest>something-manifest.xml</manifest>
     * <manifest>something-else-manifest.xml</manifest> ... </namespace>
     * 
     * @param cfgval The configuration value context.
     * @param args A List of values for the namespace element, with the first item expected to be the uri and the
     *        remaining are manifest paths.
     */
    @Config(allowMultiple = true)
    @Mapping({ "compiler", "namespaces", "namespace" })
    @Arguments({ "uri", "manifest" })
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerNamespacesNamespace(ConfigurationValue cfgval, List<String> args)
            throws ConfigurationException
    {
        if (args == null)
            throw new ConfigurationException.CannotOpen(null, cfgval.getVar(), cfgval.getSource(), cfgval.getLine());

        // allow -compiler.namespaces.namespace= which means don't add
        // anything, which matches the behavior of things like -compiler.library-path
        // which don't throw an error in this case either.
        if (args.isEmpty())
            return;

        if (args.size() < 2)
            throw new ConfigurationException.NamespaceMissingManifest("namespace", cfgval.getSource(),
                    cfgval.getLine());

        if (args.size() % 2 != 0)
            throw new ConfigurationException.IncorrectArgumentCount(args.size() + 1, args.size(), cfgval.getVar(),
                    cfgval.getSource(), cfgval.getLine());

        if (manifestMappings == null)
            manifestMappings = new ArrayList<MXMLNamespaceMapping>();

        for (int i = 0; i < args.size() - 1; i += 2)
        {
            final String uri = args.get(i);
            final String manifestFile = args.get(i + 1);
            final String path = resolvePathStrict(manifestFile, cfgval);
            manifestMappings.add(new MXMLNamespaceMapping(uri, path));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // metadata.*
    ///////////////////////////////////////////////////////////////////////////

    //
    // 'metadata.contributor' option
    //

    private final Set<String> contributors = new TreeSet<String>();

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "contributor" })
    @Arguments("name")
    public void setMetadataContributor(ConfigurationValue cv, String name)
    {
        contributors.add(name);
    }

    //
    // 'metadata.creator' option
    //

    private final Set<String> creators = new TreeSet<String>();

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "creator" })
    @Arguments("name")
    public void setMetadataCreator(ConfigurationValue cv, String name)
    {
        creators.add(name);
    }

    //
    // 'metadata.date' option
    //

    public String date = null;
    
    public String getMetadataDate()
    {
    	return date;
    }

    @Config
    @Mapping({ "metadata", "date" })
    @Arguments("text")
    public void setMetadataDate(ConfigurationValue cv, String text)
    {
        date = text;
    }

    //
    // 'metadata.dateFormat' option
    //

    public String dateFormat = null;
    
    public String getMetadataDateFormat()
    {
    	return dateFormat;
    }

    @Config
    @Mapping({ "metadata", "dateFormat" })
    @Arguments("text")
    public void setMetadataDateFormat(ConfigurationValue cv, String text)
    {
        dateFormat = text;
    }

    //
    // 'metadata.description' option
    //

    private final Map<String, String> localizedDescriptions = new LinkedHashMap<String, String>();

    @Config
    @Mapping({ "metadata", "description" })
    @Arguments("text")
    public void setMetadataDescription(ConfigurationValue cv, String text)
    {
        localizedDescriptions.put("x-default", text);
    }

    //
    // 'metadata.language' option
    //

    public final Set<String> langs = new TreeSet<String>();

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "language" })
    @Arguments("code")
    public void setMetadataLanguage(ConfigurationValue cv, String code)
    {
        langs.add(code);
    }

    //
    // 'metadata.localized-description' option
    //

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "localized-description" })
    @Arguments({ "text", "lang" })
    public void setMetadataLocalizedDescription(ConfigurationValue cv, String text, String lang)
    {
        localizedDescriptions.put(lang, text);
    }

    //
    // 'metadata.localized-title' option
    //

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "localized-title" })
    @Arguments({ "title", "lang" })
    public void setMetadataLocalizedTitle(ConfigurationValue cv, String title, String lang)
    {
        localizedTitles.put(lang, title);
    }

    //
    // 'metadata.publisher' option
    //

    private final Set<String> publishers = new TreeSet<String>();

    @Config(allowMultiple = true)
    @Mapping({ "metadata", "publisher" })
    @Arguments("name")
    public void setMetadataPublisher(ConfigurationValue cv, String name)
    {
        publishers.add(name);
    }

    //
    // 'metadata.title' option
    //

    private final Map<String, String> localizedTitles = new LinkedHashMap<String, String>();

    @Config
    @Mapping({ "metadata", "title" })
    @Arguments("text")
    public void setMetadataTitle(ConfigurationValue cv, String title)
    {
        localizedTitles.put("x-default", title);
    }

    //////////////////////////////////////////////////////////////////////////
    // runtime-shared-library-settings
    //////////////////////////////////////////////////////////////////////////

    // 
    // 'force-rsl' option
    //
    private Set<String> forceRsls;

    /**
     * Get the array of SWCs that should have their RSLs loaded, even if the compiler detects no classes being used from
     * the SWC.
     * 
     * @return Array of SWCs that should have their RSLs loaded.
     */
    public Set<String> getForceRsls()
    {
        if (forceRsls == null)
        {
            return Collections.emptySet();
        }

        return forceRsls;
    }

    @Config(advanced = true, allowMultiple = true)
    @Mapping({ "runtime-shared-library-settings", "force-rsls" })
    @SoftPrerequisites({ "runtime-shared-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @RoyaleOnly
    public void setForceRsls(ConfigurationValue cfgval, String[] args) throws ConfigurationException
    {
        if (forceRsls == null)
        {
            forceRsls = new HashSet<String>();
        }

        // Add swc to the forceRsls set.
        for (String arg : args)
        {
            // path-element parameter (swc)
            // verify path exists and the swc has an
            // existing -rslp option specified.
            String swcPath = resolvePathStrict(arg, cfgval);

            // verify the swc is used in an the RSL configuration.
            if (!doesSwcHaveRSLInfo(swcPath))
            {
                throw new ConfigurationException.SwcDoesNotHaveRslData(swcPath, cfgval.getVar(), cfgval.getSource(),
                        cfgval.getLine());
            }

            forceRsls.add(swcPath);
        }
    }

    // 
    // 'application-domain' option
    //

    /*
     * Key: swc file path; Value: application domain target
     */
    private HashMap<String, ApplicationDomainTarget> applicationDomains;

    /**
     * Get the application domain an RSL should be loaded into. The default is the current application domain but the
     * user can override this setting.
     * 
     * @param swcPath The full path of the swc file.
     * 
     * @return The application domain the RSL should be loaded into. If the swc is not found, then 'default' is
     *         returned.
     */
    public ApplicationDomainTarget getApplicationDomain(String swcPath)
    {
        if (applicationDomains == null || swcPath == null)
        {
            return ApplicationDomainTarget.DEFAULT;
        }

        for (Map.Entry<String, ApplicationDomainTarget> entry : applicationDomains.entrySet())
        {
            if (entry.getKey().equals(swcPath))
            {
                return entry.getValue();
            }
        }

        return ApplicationDomainTarget.DEFAULT;
    }

    @Config(advanced = true, allowMultiple = true)
    @SoftPrerequisites({ "runtime-shared-library-path" })
    @Mapping({ "runtime-shared-library-settings", "application-domain" })
    @Arguments({ "path-element", "application-domain-target" })
    @InfiniteArguments
    @RoyaleOnly
    // TODO: need to create an argument name generator for the args.
    public void setApplicationDomain(ConfigurationValue cfgval, String[] args) throws ConfigurationException
    {
        // ignore the force option if we are static linking
        if (getStaticLinkRsl())
            return;

        if (applicationDomains == null)
        {
            applicationDomains = new HashMap<String, ApplicationDomainTarget>();
        }

        // Add swc and application domain target to the map.
        // The args are: swc file path, application domain type, ...
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i++];

            // path-element parameter (swc)
            // verify path exists and the swc has an
            // existing -rslp option specified.
            String swcPath = resolvePathStrict(arg, cfgval);

            // verify the swc is used in an the RSL configuration.
            if (!doesSwcHaveRSLInfo(swcPath))
            {
                throw new ConfigurationException.SwcDoesNotHaveRslData(swcPath, cfgval.getVar(), cfgval.getSource(),
                        cfgval.getLine());
            }

            // Verify the application domain target is valid.
            arg = args[i];
            ApplicationDomainTarget adTarget = getApplicationDomainTarget(arg);
            if (adTarget == null)
            {
                // throw a configuration exception that the application domain 
                // type is incorrect.
                throw new ConfigurationException.BadApplicationDomainValue(swcPath, arg, cfgval.getVar(),
                        cfgval.getSource(), cfgval.getLine());
            }

            applicationDomains.put(swcPath, adTarget);
        }
    }

    /**
     * Test if the specified parameter is a valid application domain type. If it is then return the corresponding enum.
     * 
     * @param arg String value representing an ApplicationDomainTarget.
     * @return An ApplicationDomainTarget enum if the parameter is a valid application domain target, null otherwise.
     */
    private ApplicationDomainTarget getApplicationDomainTarget(String arg)
    {
        for (ApplicationDomainTarget appDomain : ApplicationDomainTarget.values())
        {
            if (appDomain.getApplicationDomainValue().equals(arg))
                return appDomain;
        }

        return null;
    }

    /**
     * Check if the SWC has any RSL info associated with it.
     * 
     * @param swcPath
     * @return true if the swc has RSL info, false otherwise.
     */
    private boolean doesSwcHaveRSLInfo(String swcPath)
    {
        if (swcPath == null)
            return false;

        List<RuntimeSharedLibraryPathInfo> rslInfoList = getRslPathInfo();
        for (RuntimeSharedLibraryPathInfo rslInfo : rslInfoList)
        {
            if (swcPath.equals(rslInfo.getSWCFile().getPath()))
                return true;
        }

        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    // compiler.mxml
    //////////////////////////////////////////////////////////////////////////

    //
    // 'compiler.mxml.compatibility-version' option
    //

    public static final int MXML_VERSION_4_7 = 0x04070000;
    public static final int MXML_VERSION_4_6 = 0x04060000;
    public static final int MXML_VERSION_4_5 = 0x04050000;
    public static final int MXML_VERSION_4_0 = 0x04000000;
    public static final int MXML_VERSION_3_0 = 0x03000000;
    public static final int MXML_VERSION_2_0_1 = 0x02000001;
    public static final int MXML_VERSION_2_0 = 0x02000000;
    public static final int MXML_CURRENT_VERSION = MXML_VERSION_4_7;
    public static final int MXML_EARLIEST_MAJOR_VERSION = 3;
    public static final int MXML_LATEST_MAJOR_VERSION = 4;
    public static final int MXML_LATEST_MINOR_VERSION = 7;

    private int mxml_major = MXML_LATEST_MAJOR_VERSION;
    private int mxml_minor = MXML_LATEST_MINOR_VERSION;
    private int mxml_revision;

    private int mxmlMinMajor = MXML_EARLIEST_MAJOR_VERSION;
    private int mxmlMinMinor;
    private int mxmlMinRevision;

    public int getCompilerMxmlMajorCompatibilityVersion()
    {
        return mxml_major;
    }

    public int getCompilerMxmlMinorCompatibilityVersion()
    {
        return mxml_minor;
    }

    public int getCompilerMxmlRevisionCompatibilityVersion()
    {
        return mxml_revision;
    }

    /*
     * Unlike the framework's RoyaleVersion.compatibilityVersionString, this
     * returns null rather than a string like "3.0.0" for the current version.
     * But if a -compatibility-version was specified, this string will always be
     * of the form N.N.N. For example, if -compatibility-version=2, this string
     * is "2.0.0", not "2".
     */
    public String getCompilerMxmlCompatibilityVersionString()
    {
        return (mxml_major == 0 && mxml_minor == 0 && mxml_revision == 0) ? null
                : mxml_major + "." + mxml_minor + "." + mxml_revision;
    }

    /*
     * This returns an int that can be compared with version constants such as
     * MxmlConfiguration.VERSION_3_0.
     */
    public int getCompilerMxmlCompatibilityVersion()
    {
        int version = (mxml_major << 24) + (mxml_minor << 16) + mxml_revision;
        return version != 0 ? version : MXML_CURRENT_VERSION;
    }

    @Config
    @Mapping({ "compiler", "mxml", "compatibility-version" })
    @Arguments("version")
    @RoyaleOnly
    public void setCompilerMxmlCompatibilityVersion(ConfigurationValue cv, String version) throws ConfigurationException
    {
        if (version == null)
        {
            return;
        }

        String[] results = version.split("\\.");

        if (results.length == 0)
        {
            throw new ConfigurationException.BadVersion(version, "compatibility-version");

        }

        // Set minor and revision numbers to zero in case only a major number 
        // was specified.
        this.mxml_minor = 0;
        this.mxml_revision = 0;

        for (int i = 0; i < results.length; i++)
        {
            int versionNum = 0;

            try
            {
                versionNum = Integer.parseInt(results[i]);
            }
            catch (NumberFormatException e)
            {
                throw new ConfigurationException.BadVersion(version, "compatibility-version");
            }

            if (i == 0)
            {
                if (versionNum >= MXML_EARLIEST_MAJOR_VERSION && versionNum <= MXML_LATEST_MAJOR_VERSION)
                {
                    this.mxml_major = versionNum;
                }
                else
                {
                    throw new ConfigurationException.BadVersion(version, "compatibility-version");
                }
            }
            else
            {
                if (versionNum >= 0)
                {
                    if (i == 1)
                    {
                        this.mxml_minor = versionNum;
                    }
                    else
                    {
                        this.mxml_revision = versionNum;
                    }
                }
                else
                {
                    throw new ConfigurationException.BadVersion(version, "compatibility-version");
                }
            }
        }
    }

    /*
     * Minimum supported SDK version for this library. This string will always
     * be of the form N.N.N. For example, if -minimum-supported-version=2, this
     * string is "2.0.0", not "2".
     */
    public String getCompilerMxmlMinimumSupportedVersionString()
    {
        return (mxmlMinMajor == 0 && mxmlMinMinor == 0 && mxmlMinRevision == 0) ? null
                : mxmlMinMajor + "." + mxmlMinMinor + "." + mxmlMinRevision;
    }

    /*
     * This returns an int that can be compared with version constants such as
     * MxmlConfiguration.VERSION_3_0.
     */
    public int getCompilerMxmlMinimumSupportedVersion()
    {
        int version = (mxmlMinMajor << 24) + (mxmlMinMinor << 16) + mxmlMinRevision;
        return version != 0 ? version : (MXML_EARLIEST_MAJOR_VERSION << 24);
    }

    public void setCompilerMxmlMinimumSupportedVersion(int version)
    {
        mxmlMinMajor = version >> 24 & 0xFF;
        mxmlMinMinor = version >> 16 & 0xFF;
        mxmlMinRevision = version & 0xFF;
    }

    @Config
    @Mapping({ "compiler", "mxml", "minimum-supported-version" })
    @RoyaleOnly
    public void setCompilerMxmlMinimumSupportedVersion(ConfigurationValue cv, String version)
            throws ConfigurationException
    {
        if (version == null)
        {
            return;
        }

        String[] results = version.split("\\.");

        if (results.length == 0)
        {
            throw new ConfigurationException.BadVersion(version, "minimum-supported-version");

        }

        for (int i = 0; i < results.length; i++)
        {
            int versionNum = 0;

            try
            {
                versionNum = Integer.parseInt(results[i]);
            }
            catch (NumberFormatException e)
            {
                throw new ConfigurationException.BadVersion(version, "minimum-supported-version");
            }

            if (i == 0)
            {
                if (versionNum >= MXML_EARLIEST_MAJOR_VERSION && versionNum <= MXML_LATEST_MAJOR_VERSION)
                {
                    this.mxmlMinMajor = versionNum;
                }
                else
                {
                    throw new ConfigurationException.BadVersion(version, "minimum-supported-version");
                }
            }
            else
            {
                if (versionNum >= 0)
                {
                    if (i == 1)
                    {
                        mxmlMinMinor = versionNum;
                    }
                    else
                    {
                        mxmlMinRevision = versionNum;
                    }
                }
                else
                {
                    throw new ConfigurationException.BadVersion(version, "minimum-supported-version");
                }
            }
        }

        isMinimumSupportedVersionConfigured = true;
    }

    private boolean isMinimumSupportedVersionConfigured = false;

    public boolean isCompilerMxmlMinimumSupportedVersionConfigured()
    {
        return isMinimumSupportedVersionConfigured;
    }

    //
    // 'compiler.mobile' option
    //

    private boolean mobile = false;

    /**
     * @return determines whether the target runtime is a mobile device. This may alter the features available, such as
     *         certain blend-modes when compiling FXG.
     */
    public boolean getMobile()
    {
        return mobile;
    }

    @Config()
    @Mapping({ "compiler", "mobile" })
    public void setMobile(ConfigurationValue cv, boolean b)
    {
        mobile = b;
    }

    ////////////////////////////////////////////////////////////////////////////
    // licenses.*
    ////////////////////////////////////////////////////////////////////////////

    //
    //  'license' option
    //

    @Config(allowMultiple = true, displayed = false, removed = true)
    @Mapping({ "licenses", "license" })
    @Arguments({ "product", "serial-number" })
    public void setLicensesLicense(ConfigurationValue cfgval, String product, String serialNumber)
            throws ConfigurationException
    {
    }

    ////////////////////////////////////////////////////////////////////////////
    // frames.*
    ////////////////////////////////////////////////////////////////////////////

    private List<FrameInfo> frameList = new LinkedList<FrameInfo>();

    public List<FrameInfo> getFrameList()
    {
        return frameList;
    }

    @Config(advanced = true, allowMultiple = true)
    @Mapping({ "frames", "frame" })
    @Arguments({ "label", "classname" })
    @InfiniteArguments
    public void setFramesFrame(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
        // "args" are [label, qname, qname, qname, ...]

        final FrameInfo info = new FrameInfo();

        if (args.size() < 2)
            throw new ConfigurationException.BadFrameParameters(cv.getVar(), cv.getSource(), cv.getLine());

        for (final String next : args)
        {
            if (info.getLabel() == null)
            {
                info.setLabel(next);
            }
            else
            {
                info.getFrameClasses().add(next);
            }
        }

        frameList.add(info);
    }

    //
    // -as3
    //

    private boolean as3 = true;

    @Config(advanced = true)
    @Mapping({ "compiler", "as3" })
    @DeprecatedConfig
    public void setAS3(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != as3)
            addRemovedConfigurationOptionProblem(cv);
    }

    //
    // -es
    //

    private boolean es = false;

    @Config(advanced = true)
    @Mapping({ "compiler", "es" })
    @DeprecatedConfig
    public void setES(ConfigurationValue cv, boolean b)
    {
        // This option is set in royale-config.xml so only warn
        // if the user sets a non-default value.
        if (b != es)
            addRemovedConfigurationOptionProblem(cv);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Compc Options
    ////////////////////////////////////////////////////////////////////////////

    //
    // compute-digest=true|false
    //

    /**
     * Writes a digest to the catalog.xml of a library. Use this when the library will be used as a cross-domain RSL or
     * when you want to enforce the versioning of RSLs. The default value is true.
     */
    @Config(compcOnly = true, removed = true)
    @Mapping("compute-digest")
    public void setComputeDigest(ConfigurationValue cv, boolean value)
    {
    }

    /**
     * directory=false|true
     */
    private boolean outputSwcAsDirectory = false;

    /**
     * Outputs the SWC file in an open directory format rather than a SWC file. You use this option with the output
     * option to specify a destination directory, as the following example shows:
     * 
     * <pre>
     * compc -directory=true -output=destination_directory
     * </pre>
     */
    @Config(compcOnly = true)
    @Mapping("directory")
    public void setOutputSwcAsDirectory(ConfigurationValue cv, boolean value)
    {
        outputSwcAsDirectory = value;
    }

    /**
     * @return True if the compiler will build the SWC file in an open directory format rather than a SWC file.
     */
    public boolean getOutputSwcAsDirectory()
    {
        return outputSwcAsDirectory;
    }

    /**
     * include-classes class [...]
     */
    private final List<String> includeClasses = new ArrayList<String>();

    /**
     * Specifies classes to include in the SWC file. You provide the class name (for example, MyClass) rather than the
     * file name (for example, MyClass.as) to the file for this option. As a result, all classes specified with this
     * option must be in the compiler's source path. You specify this by using the source-path compiler option.
     * <p>
     * You can use packaged and unpackaged classes. To use components in namespaces, use the include-namespaces option.
     * <p>
     * If the components are in packages, ensure that you use dot-notation rather than slashes to separate package
     * levels.
     * <p>
     * This is the default option for the component compiler.
     */
    @Config(compcOnly = true, allowMultiple = true)
    @Arguments(Arguments.CLASS)
    @Mapping("include-classes")
    public void setIncludeClasses(ConfigurationValue cv, List<String> values)
    {
        includeClasses.addAll(values);
    }

    /**
     * @return A list of class names to be included in the target.
     */
    public List<String> getIncludeClasses()
    {
        return includeClasses;
    }

    /**
     * include-file name path [...]
     */
    public final Map<String, String> includeFilesNamePath = new LinkedHashMap<String, String>();

    /**
     * Adds the file to the SWC file. This option does not embed files inside the library.swf file. This is useful for
     * adding graphics files, where you want to add non-compiled files that can be referenced in a style sheet or
     * embedded as assets in MXML files.
     * <p>
     * If you add a stylesheet that references compiled resources such as programmatic skins, use the include-stylesheet
     * option.
     * <p>
     * If you use the [Embed] syntax to add a resource to your application, you are not required to use this option to
     * also link it into the SWC file.
     */
    @Config(compcOnly = true, allowMultiple = true)
    @Mapping("include-file")
    @Arguments({ "name", "path" })
    public void setIncludeFiles(ConfigurationValue cv, List<String> values)
            throws IncorrectArgumentCount, CannotOpen, RedundantFile
    {
        // Expect name-path pairs in the arguments.
        final int size = values.size();
        if (size % 2 != 0)
            throw new IncorrectArgumentCount(size + 1, size, cv.getVar(), cv.getSource(), cv.getLine());

        for (int nameIndex = 0; nameIndex < size - 1; nameIndex += 2)
        {
            final String name = values.get(nameIndex);
            final String path = resolvePathStrict(values.get(nameIndex + 1), cv);

            if (includeFilesNamePath.containsKey(name))
            {
                throw new ConfigurationException.RedundantFile(name, cv.getVar(), cv.getSource(), cv.getLine());
            }

            includeFilesNamePath.put(name, path);
        }
    }

    /**
     * @return A map of included files. The keys are file entry names; The values are file paths.
     */
    public Map<String, String> getIncludeFiles()
    {
        return includeFilesNamePath;
    }

    /**
     * include-lookup-only=false|true
     */
    private boolean includeLookupOnly = false;

    /**
     * If true, only manifest entries with lookupOnly=true are included in the SWC catalog.
     */
    @Config(compcOnly = true, advanced = true)
    @Mapping("include-lookup-only")
    @RoyaleOnly
    public void setIncludeLookupOnly(ConfigurationValue cv, boolean value)
    {
        includeLookupOnly = value;
    }

    /**
     * @return If true, only manifest entries with lookupOnly=true are included in the SWC catalog.
     */
    public boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    /**
     * include-namespaces uri [...]
     */
    private final List<String> includeNamespaces = new ArrayList<String>();

    /**
     * Specifies namespace-style components in the SWC file. You specify a list of URIs to include in the SWC file. The
     * uri argument must already be defined with the namespace option.
     * <p>
     * To use components in packages, use the include-classes option.
     */
    @Config(compcOnly = true, allowMultiple = true)
    @Mapping("include-namespaces")
    @Arguments({ "uri" })
    @RoyaleOnly
    public void setIncludeNamespaces(ConfigurationValue cv, List<String> values)
    {
        includeNamespaces.addAll(values);
    }

    /**
     * @return A list of URIs to include in the SWC file.
     */
    public List<String> getIncludeNamespaces()
    {
        return includeNamespaces;
    }

    /**
     * include-sources path-element
     */
    private final List<String> includeSources = new ArrayList<String>();

    /**
     * Specifies classes or directories to add to the SWC file. When specifying classes, you specify the path to the
     * class file (for example, MyClass.as) rather than the class name itself (for example, MyClass). This lets you add
     * classes to the SWC file that are not in the source path. In general, though, use the include-classes option,
     * which lets you add classes that are in the source path.
     * <p>
     * If you specify a directory, this option includes all files with an MXML or AS extension, and ignores all other
     * files.
     * <p>
     * If you use this option to include MXML components that are in a non-default package, you must include the source
     * folder in the source path.
     */
    @Config(compcOnly = true, allowMultiple = true)
    @Mapping("include-sources")
    @Arguments(Arguments.PATH_ELEMENT)
    public void setIncludeSources(ConfigurationValue cv, List<String> values) throws NotAFile
    {
        fillListWithResolvedPaths(values, includeSources, cv);
    }

    /**
     * @return Normalized file paths of the included source files.
     */
    public List<String> getIncludeSources()
    {
        return includeSources;
    }

    /**
     * Add resolved file paths from {@code source} list to {@code target} list.
     * 
     * @param source Source list with un-resolved file paths.
     * @param target Target list.
     * @param cv Context.
     * @throws CannotOpen
     */
    private void fillListWithResolvedPaths(final List<String> source, final List<String> target,
            final ConfigurationValue cv) throws NotAFile
    {
        for (final String path : source)
        {
            String resolvedPath;
            try
            {
                resolvedPath = resolvePathStrict(path, cv);
                target.add(resolvedPath);
            }
            catch (CannotOpen e)
            {
                throw new ConfigurationException.NotAFile(path, cv.getVar(), cv.getSource(), cv.getLine());
            }
        }
    }

    /**
     * include-stylesheet namepath [...]
     */
    private final List<String> includeStyleSheets = new ArrayList<String>();

    /**
     * Specifies stylesheets to add to the SWC file. This option compiles classes that are referenced by the stylesheet
     * before including the stylesheet in the SWC file.
     * <p>
     * You do not need to use this option for all stylesheets; only stylesheets that reference assets that need to be
     * compiled such as programmatic skins or other class files. If your stylesheet does not reference compiled assets,
     * you can use the include-file option.
     * <p>
     * This option does not compile the stylesheet into a SWF file before including it in the SWC file. You compile a
     * CSS file into a SWF file when you want to load it at run time.
     */
    @Config(compcOnly = true, allowMultiple = true)
    @Mapping("include-stylesheet")
    @Arguments({ "name", "path" })
    @RoyaleOnly
    public void setIncludeStyleSheets(ConfigurationValue cv, List<String> values) throws NotAFile
    {
        fillListWithResolvedPaths(values, includeStyleSheets, cv);
    }

    /**
     * @return A list of the normalized file path of stylesheets to add to the SWC file.
     */
    public List<String> getIncludeStyleSheets()
    {
        return includeStyleSheets;
    }

    private File dependencyGraphOutput;

    /**
     * Specifies a file name that a graphml version of the dependency graph should be written to.
     * 
     */
    @Config(advanced = true)
    @Mapping("dependency-graph")
    @Arguments("filename")
    public void setDependencyGraphOutput(ConfigurationValue cv, String fileName)
    {
        dependencyGraphOutput = new File(getOutputPath(cv, fileName));
    }

    /**
     * Gets the location the graphml version of the dependency graph should be written to, null if no dependecy graph
     * should be written.
     * 
     * @return The location the dependency graph should be written to.
     */
    public File getDependencyGraphOutput()
    {
        return dependencyGraphOutput;
    }

    //
    // 'output' option
    //

    private String output;

    public String getOutput()
    {
        return output;
    }

    @Config
    @Arguments("filename")
    public void setOutput(ConfigurationValue val, String output) throws ConfigurationException
    {
        this.output = getOutputPath(val, output);
    }

    //
    // 'swf-debugfile-alias' option
    //

    private String swfDebugfileAlias;

    public String getSwfDebugfileAlias()
    {
        return swfDebugfileAlias;
    }

    @Config
    @Arguments("filename")
    public void setSwfDebugfileAlias(ConfigurationValue val, String output) throws ConfigurationException
    {
        this.swfDebugfileAlias = output;
    }

    //
    // 'dump-config-file' option from ToolsConfiguration
    //

    private String dumpConfigFile = null;

    /**
     * @return filename of the configuration dump
     */
    public String getDumpConfig()
    {
        return dumpConfigFile;
    }

    @Config(advanced = true, displayed = false)
    @Arguments("filename")
    @Mapping("dump-config")
    public void setDumpConfig(ConfigurationValue cv, String filename)
    {
        dumpConfigFile = getOutputPath(cv, filename);
    }

    //
    // 'warnings' option from ToolsConfiguration
    //

    private boolean warnings = true;

    public boolean getWarnings()
    {
        return warnings;
    }

    @Config
    @Mapping("warnings")
    public void setWarnings(ConfigurationValue cv, boolean b)
    {
        warnings = b;
    }

    //
    // 'error-problems'
    //

    private Collection<Class<ICompilerProblem>> errorClasses;

    /**
     * Get the collection of user specified problem classes that should be treated as errors.
     * 
     * @return list of problem classes that should be treated as errors.
     */
    public Collection<Class<ICompilerProblem>> getErrorProblems()
    {
        return errorClasses != null ? errorClasses : Collections.<Class<ICompilerProblem>> emptyList();
    }

    @Config(allowMultiple = true)
    @Arguments(Arguments.CLASS)
    @InfiniteArguments
    public void setErrorProblems(ConfigurationValue cv, List<String> classNames) throws ConfigurationException
    {
        if (errorClasses == null)
            errorClasses = new HashSet<Class<ICompilerProblem>>();

        // Convert string to a class and save.
        for (String className : classNames)
        {
            Class<ICompilerProblem> resolvedClass = resolveProblemClassName(className);
            if (resolvedClass == null)
            {
                throw new ConfigurationException.CompilerProblemClassNotFound(className, cv.getVar(), cv.getSource(),
                        cv.getLine());
            }

            errorClasses.add(resolvedClass);
        }
    }

    //
    // 'warning-problems'
    //

    private Collection<Class<ICompilerProblem>> warningClasses;

    /**
     * Get the collection of user specified problem classes that should be treated as warnings.
     * 
     * @return list of problem classes that should be treated as warnings.
     */
    public Collection<Class<ICompilerProblem>> getWarningProblems()
    {
        return warningClasses != null ? warningClasses : Collections.<Class<ICompilerProblem>> emptyList();
    }

    @Config(allowMultiple = true)
    @Arguments(Arguments.CLASS)
    @InfiniteArguments
    public void setWarningProblems(ConfigurationValue cv, List<String> classNames) throws ConfigurationException
    {
        if (warningClasses == null)
            warningClasses = new HashSet<Class<ICompilerProblem>>();

        // Convert string to a class and save.
        for (String className : classNames)
        {
            Class<ICompilerProblem> resolvedClass = resolveProblemClassName(className);
            if (resolvedClass == null)
            {
                throw new ConfigurationException.CompilerProblemClassNotFound(className, cv.getVar(), cv.getSource(),
                        cv.getLine());
            }

            warningClasses.add(resolvedClass);
        }
    }

    //
    // 'ignore-problems'
    //

    private Collection<Class<ICompilerProblem>> ignoreClasses;

    /**
     * Get the collection of user specified problem classes that should be ignored.
     * 
     * @return list of problem classes that should be ignored.
     */
    public Collection<Class<ICompilerProblem>> getIgnoreProblems()
    {
        return ignoreClasses != null ? ignoreClasses : Collections.<Class<ICompilerProblem>> emptyList();
    }

    @Config(allowMultiple = true)
    @Arguments(Arguments.CLASS)
    @InfiniteArguments
    public void setIgnoreProblems(ConfigurationValue cv, List<String> classNames) throws ConfigurationException
    {
        if (ignoreClasses == null)
            ignoreClasses = new HashSet<Class<ICompilerProblem>>();

        // Convert string to a class and save.
        for (String className : classNames)
        {
            Class<ICompilerProblem> resolvedClass = resolveProblemClassName(className);
            if (resolvedClass == null)
            {
                throw new ConfigurationException.CompilerProblemClassNotFound(className, cv.getVar(), cv.getSource(),
                        cv.getLine());
            }

            ignoreClasses.add(resolvedClass);
        }
    }

    //
    // 'legacy-message-format'
    //

    private boolean legacyMessageFormat = true;

    public boolean useLegacyMessageFormat()
    {
        return legacyMessageFormat;
    }

    @Config(hidden = true)
    public void setLegacyMessageFormat(ConfigurationValue cv, boolean value) throws ConfigurationException
    {
        legacyMessageFormat = value;
    }

    //
    // 'create-target-with-errors'
    //

    private boolean createTargetWithErrors = false;

    public boolean getCreateTargetWithErrors()
    {
        return createTargetWithErrors;
    }

    @Config(hidden = true)
    public void setCreateTargetWithErrors(ConfigurationValue cv, boolean value) throws ConfigurationException
    {
        createTargetWithErrors = value;
    }

    //
    // 'flex'
    //
    private boolean isRoyale = false;

    public boolean isRoyale()
    {
        return isRoyale;
    }

    /**
     * Option to enable or prevent various Royale compiler behaviors. This is currently used to enable/disable the
     * generation of a root class for library swfs and generation of Royale specific code for application swfs.
     */
    @Config(hidden = true)
    public void setRoyale(ConfigurationValue cv, boolean value) throws ConfigurationException
    {
        isRoyale = value;
    }

    private boolean isExcludeNativeJSLibraries = false;

    public boolean isExcludeNativeJSLibraries()
    {
        return isExcludeNativeJSLibraries;
    }

    /**
     * Option to remove the Native JS libraries from external-library-path and library-path as they shouldn't be any
     * when compiling SWFs / SWCs.
     */
    @Config()
    @Mapping("exclude-native-js-libraries")
    public void setExcludeNativeJSLibraries(ConfigurationValue cv, boolean value) throws ConfigurationException
    {
        isExcludeNativeJSLibraries = value;
    }

    /**
     * Resolve a problem class name to a Java Class.
     * 
     * @param className May be fully qualified. If the class name is not fully qualified, it is assumed to live in the
     *        "org.apache.royale.compiler.problems" package.
     * 
     * @return A class corresponding to the className or null if the class name was not found.
     */
    @SuppressWarnings("unchecked")
    private Class<ICompilerProblem> resolveProblemClassName(String className)
    {
        if (className == null)
            return null;

        Class<?> resolvedClass = null;

        try
        {
            resolvedClass = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }

        if (ICompilerProblem.class.isAssignableFrom(resolvedClass))
            return (Class<ICompilerProblem>) resolvedClass;
        else
            return null;
    }

    //
    // 'file-specs' option
    //

    private List<String> fileSpecs = new ArrayList<String>();

    /**
     * Get target file path. Target file is the last file in the {@link #getFileSpecs()}.
     * FIXME: Calling this target file is a bit misleading as it's sort of the "main" source file
     */
    public String getTargetFile()
    {
        if (fileSpecs.isEmpty())
            return null;
        else
            return Iterables.getLast(fileSpecs);
    }

    /**
     * @return Path of the target's parent directory.
     */
    public String getTargetFileDirectory()
    {
        final String targetFile = getTargetFile();
        if (targetFile == null)
            return null;

        final String normalizedTargetFile = FilenameNormalization.normalize(targetFile);

        return FilenameUtils.getFullPathNoEndSeparator(normalizedTargetFile);
    }

    /**
     * This has been added so that unit tests can change the target file. It isn't normally called when running the
     * command line compiler.
     */
    public void setTargetFile(String mainFile)
    {
        fileSpecs.clear();
        fileSpecs.add(mainFile);
    }

    /**
     * @return A list of filespecs. It's the default variable for command line.
     */
    public List<String> getFileSpecs()
    {
        return fileSpecs;
    }

    @Config(allowMultiple = true, hidden = true)
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @SoftPrerequisites("royale")
    public void setFileSpecs(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
    	
        this.fileSpecs.addAll(args);

        checkForMxmlFiles(args);
    }

    private void checkForMxmlFiles(List<String> paths)
    {
        // If there are mxml or css files then we are compiling a flex 
        // application so enable "royale".
        for (String path : paths)
        {
            if (path.endsWith(".mxml") || path.endsWith(".css"))
                isRoyale = true;
        }
    }

    //
    // 'help' option from CommandLineConfiguration
    //

    /**
     * dummy, just a trigger for help text
     */
    @Config(displayed = false, greedy = true)
    @Arguments("keyword")
    @InfiniteArguments
    public void setHelp(ConfigurationValue cv, String[] keywords)
    {

    }

    //
    // 'load-config' option from CommandLineConfiguration
    //

    private String configFile = null;

    /**
     * @return Normalized path to a Flex configuration file.
     */
    public String getLoadConfig()
    {
        return configFile;
    }

    /**
     * Since {@link ConfigurationBuffer} loads the "load-config" files, the value of this configuration option isn't
     * intersting to the rest part of the compiler.
     */
    @Config(allowMultiple = true)
    @Arguments("filename")
    public void setLoadConfig(ConfigurationValue cv, String filename) throws ConfigurationException
    {
    	try {
            configFile = resolvePathStrict(filename, cv);    		
    	} 
    	catch (ConfigurationException.CannotOpen e)
    	{
    		if (filename.contains("royale-config"))
    		{
    			try {
    				filename = filename.replace("royale-config", "flex-config");
        			configFile = resolvePathStrict(filename, cv);
        			cv.getArgs().remove(0);
        			cv.getArgs().add(filename);
    			}
    			catch (ConfigurationException.CannotOpen e2)
    			{
    				throw e;
    			}
    		}
    		else
    			throw e;
    	}
    }

    //
    // 'version' option from CommandLineConfiguration
    //

    /**
     * Dummy option. Just a trigger for version info.
     */
    @Config
    public void setVersion(ConfigurationValue cv, boolean value)
    {
    }

    //
    // 'verbose' option from CommandLineConfiguration
    //

    private boolean verbose = false;

    public boolean isVerbose()
    {
        return verbose;
    }

    @Config(hidden = true)
    public void setVerbose(ConfigurationValue cfgval, boolean b)
    {
        verbose = b;
    }

    //
    // 'verbose' option from CommandLineConfiguration
    //

    private int diagnostics = 0;

    public int getDiagnosticsLevel()
    {
        return diagnostics;
    }

    @Config(hidden = true)
    public void setDiagnostics(ConfigurationValue cfgval, int b)
    {
        diagnostics = b;
    }

    //
    // 'dump-ast' option from CommandLineConfiguration
    //

    private boolean dumpAst;

    public boolean isDumpAst()
    {
        return dumpAst;
    }

    @Config(hidden = true)
    public void setDumpAst(ConfigurationValue cfgval, boolean b)
    {
        dumpAst = b;
    }

    //
    // 'enable-inlining' option from CommandLineConfiguration
    //
    private boolean enableInlining = false;

    /**
     * @return true if function inlining has been enabled.
     */
    public boolean isInliningEnabled()
    {
        return enableInlining;
    }

    /**
     * Enable or disable function inlining.
     * 
     * @param cfgval The configuration value context.
     * @param b true to enable inlining, false otherwise.
     */
    @Config(hidden = true)
    @Mapping({ "compiler", "inline" })
    public void setEnableInlining(ConfigurationValue cfgval, boolean b)
    {
        enableInlining = b;
    }

    //
    // 'remove-dead-code' option from CommandLineConfiguration
    //
    private boolean removeDeadCode = false;

    /**
     * @return true if dead code removal has been enabled.
     */
    public boolean getRemoveDeadCode()
    {
        return this.removeDeadCode;
    }

    /**
     * Enable or disable dead code removal.
     * 
     * @param cfgval the configuration value context.
     * @param b true to enable dead code removal, false to disable.
     */
    @Config(advanced = true)
    @Mapping({ "compiler", "remove-dead-code" })
    public void setRemoveDeadCode(ConfigurationValue cfgval, boolean b)
    {
        this.removeDeadCode = b;
    }

    //
    // Validation methods from ToolsConfiguration
    //

    void validateDumpConfig(ConfigurationBuffer configurationBuffer) throws ConfigurationException
    {
        if (dumpConfigFile != null)
        {
            final String text = FileConfigurator.formatBuffer(configurationBuffer, "royale-config",
                    LocalizationManager.get(), "flex2.configuration");
            try
            {
                final Writer writer = new FileWriter(dumpConfigFile);
                IOUtils.write(text, writer);
                IOUtils.closeQuietly(writer);
            }
            catch (IOException e)
            {
                throw new ConfigurationException.IOError(dumpConfigFile);
            }
        }
    }

    /**
     * Collection of fatal and non-fatal configuration problems.
     */
    private Collection<ICompilerProblem> configurationProblems = new ArrayList<ICompilerProblem>();

    /**
     * Get the configuration problems. This should be called after the configuration has been processed.
     * 
     * @return a collection of fatal and non-fatal configuration problems.
     */
    public Collection<ICompilerProblem> getConfigurationProblems()
    {
        return configurationProblems;
    }

    private boolean warnOnRoyaleOnlyOptionUsage = false;

    /**
     * @return True if warnings are generated when "Flex only" options are used, false otherwise.
     */
    public boolean getWarnOnRoyaleOnlyOptionUsage()
    {
        return warnOnRoyaleOnlyOptionUsage;
    }

    /**
     * Controls if the compiler warns when "Flex only" configuration options are used in the compiler.
     * 
     * @param value True to enable warnings, false to disable warnings. The default is to not warn.
     */
    public void setWarnOnRoyaleOnlyOptionUsage(boolean value)
    {
        this.warnOnRoyaleOnlyOptionUsage = value;
    }

    private boolean enableTelemetry = false;

    /**
     *
     * @return True if telemetry is enabled, false otherwise.
     */
    public boolean isEnableTelemetry()
    {
        return enableTelemetry;
    }

    /**
     * Controls if the flash runtime should allow providing advanced telemetry options to external tools.
     *
     * @param enableTelemetry True to enable telemetry, false to disable. The default ist to disable.
     */
    public void setEnableTelemetry(boolean enableTelemetry)
    {
        this.enableTelemetry = enableTelemetry;
    }

    /**
     * Turns on the advanced telemetry options of the flash runtime to allow clients like scout to connect.
     *
     * Remark: Internally and in the spec this option is called "enable telemetry" but by tools and the commandline it's
     * referenced by advanced-telemetry.
     */
    @Config(advanced = true)
    @Mapping({ "compiler", "advanced-telemetry" })
    @RoyaleOnly
    public void setEnableTelemetry(ConfigurationValue cv, boolean enableTelemetry) throws CannotOpen
    {
        this.enableTelemetry = enableTelemetry;
    }

    private void processDeprecatedAndRemovedOptions(ConfigurationBuffer configurationBuffer)
    {
        for (final String var : configurationBuffer.getVars())
        {
            ConfigurationInfo info = configurationBuffer.getInfo(var);
            List<ConfigurationValue> values = configurationBuffer.getVar(var);
            if (values != null)
            {
                for (final ConfigurationValue cv : values)
                {
                    if (info.isRemoved())
                    {
                        //addRemovedConfigurationOptionProblem(cv);
                    }
                    else if (info.isDeprecated() && configurationBuffer.getVar(var) != null)
                    {
                        String replacement = info.getDeprecatedReplacement();
                        String since = info.getDeprecatedSince();
                        DeprecatedConfigurationOptionProblem problem = new DeprecatedConfigurationOptionProblem(var,
                                replacement, since, cv.getSource(), cv.getLine());
                        configurationProblems.add(problem);
                    }
                    else if (warnOnRoyaleOnlyOptionUsage && info.isRoyaleOnly())
                    {
                        RoyaleOnlyConfigurationOptionNotSupported problem = new RoyaleOnlyConfigurationOptionNotSupported(
                                var, cv.getSource(), cv.getLine());
                        configurationProblems.add(problem);
                    }
                }
            }
        }
    }

    /**
     * Add a RemovedConfigurationOptionProblem to the list of configuration problems.
     * 
     * @param cv
     */
    private void addRemovedConfigurationOptionProblem(ConfigurationValue cv)
    {
        RemovedConfigurationOptionProblem problem = new RemovedConfigurationOptionProblem(cv.getVar(), cv.getSource(),
                cv.getLine());
        configurationProblems.add(problem);
    }

    private boolean strictXML = false;

    /**
     *
     * @return True if strictXML is enabled, false otherwise.
     */
    public boolean isStrictXML()
    {
        return strictXML;
    }

    /**
     * Controls if the compiler should try to resolve XML methods.  Enabling this makes it
     * possible to write new XML implementations but causes more warnings.  Default is false.
     *
     * @param strictXML True to enable strict XML checking, false to disable. The default is to disable.
     */
    public void setStrictXML(boolean strictXML)
    {
        this.strictXML = strictXML;
    }

    /**
     * Turns on the strict XML checking in the compiler.  Enabling this makes it
     * possible to write new XML implementations but causes more warnings.
     *
     */
    @Config(advanced = true)
    @Mapping({ "compiler", "strict-xml" })
    @RoyaleOnly
    public void setStrictXML(ConfigurationValue cv, boolean strictXML) throws CannotOpen
    {
        this.strictXML = strictXML;
    }

}
