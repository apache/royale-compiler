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

import java.io.File;
import java.util.*;

import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.RSLSettings.RSLAndPolicyFileURLPair;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.config.CompilerProblemSettings;
import org.apache.royale.compiler.internal.config.DefaultsConfigurator;
import org.apache.royale.compiler.internal.config.FileConfigurator;
import org.apache.royale.compiler.internal.config.ICompilerSettings;
import org.apache.royale.compiler.internal.config.IConfigurator;
import org.apache.royale.compiler.internal.config.RuntimeSharedLibraryPathInfo;
import org.apache.royale.compiler.internal.config.SystemPropertyConfigurator;
import org.apache.royale.compiler.internal.config.TargetSettings;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.config.localization.ResourceBundleLocalizer;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.FileUtils;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.Trace;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A class that allows a client change compiler settings and to 
 * configure projects and targets from those settings.
 */
public class Configurator implements ICompilerSettings, IConfigurator, ICompilerSettingsConstants, Cloneable
{

    /**
     * Marker class for RSLSettings because RSLSettings need special handling
     * in getOptions(). 
     */
    static class RSLSettingsList extends ArrayList<RSLSettings>
    {
        public RSLSettingsList(int size)
        {
            super(size);
        }
        
        public RSLSettingsList(Collection<? extends RSLSettings> c)
        {
            super(c);
        }

        private static final long serialVersionUID = 0L;        
    }
    
    /**
     *  Ditto for conditional compilation
     */
    static class CompilerDefinitionMap extends TreeMap<String, String>
    {
        private static final long serialVersionUID = 0L;
    }
    
    static class ApplicationDomainsList extends ArrayList<String[]>
    {
        private static final long serialVersionUID = 0L;
    }
    
    static class MXMLNamespaceMappingList extends ArrayList<IMXMLNamespaceMapping>
    {
        public MXMLNamespaceMappingList(int size)
        {
            super(size);
        }
        
        public MXMLNamespaceMappingList(Collection<? extends IMXMLNamespaceMapping> namespaceMappings)
        {
            super(namespaceMappings);
        }

        private static final long serialVersionUID = 0L;
        
    }

    static class ServicesContextRoot
    {
        public ServicesContextRoot(String path, String contextRoot)
        {
            this.path = path;
            this.contextRoot = contextRoot;
        }
        
        public String path;
        public String contextRoot;
    }

    /**
     *  Ditto for conditional compilation
     */
    static class FrameLabelMap extends TreeMap<String, List<String>>
    {
        private static final long serialVersionUID = 0L;
    }
    
    /**
     * Convert file path strings to {@code File} objects. Null values are
     * discarded.
     * 
     * @param paths List of file paths
     * @return List of File objects. No null values will be returned.
     */
    public static List<File> toFiles(final List<String> paths)
    {
        final List<File> result = new ArrayList<File>();
        for (final String path : paths)
        {
            if (path != null)
                result.add(new File(path));
        }
        return result;
    }

    /**
     * Convert file path strings to {@code File} objects. Null values are
     * discarded.
     * 
     * @param paths List of file paths
     * @return Array of File objects. No null values will be returned.
     */
    public static List<File> toFileList(final List<String> paths)
    {
        final List<File> result = new ArrayList<File>();
        for (final String path : paths)
        {
            if (path != null)
                result.add(FilenameNormalization.normalize(new File(path)));
        }
        return result;
    }

    /**
     * Convert {@code File} objects to {@code String}, where each {@code String} is 
     * the absolute file path of the file. Null values are discarded.
     * 
     * @param files file specifications
     * @return Array of File objects. No null values will be returned.
     */
    public static String[] toPaths(File[] files)
    {
        final List<String> result = new ArrayList<String>();
        for (final File file : files)
        {
            if (file != null)
                result.add(file.getAbsolutePath());
        }
        return result.toArray(new String[0]);
    }

    /**
     * Resolve a list of normalized paths to {@link IFileSpecification} objects
     * from the given {@code workspace}.
     * 
     * @param paths A list of normalized paths.
     * @param workspace IWorkspace.
     * @return A list of file specifications.
     */
    public static List<IFileSpecification> toFileSpecifications(
            final List<String> paths,
            final IWorkspace workspace)
    {
        return Lists.transform(paths, new Function<String, IFileSpecification>()
        {
            @Override
            public IFileSpecification apply(final String path)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
            		System.out.println("Configurator waiting for lock in toFileSpecifications");
            	IFileSpecification fs = workspace.getFileSpecification(path);
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
            		System.out.println("Configurator done with lock in toFileSpecifications");
            	return fs;
            }
        });
    }

    // Used to generate the command line
    private static final String EQUALS_STRING = "=";
    private static final String PLUS_EQUALS_STRING = "+=";
    private static final String COMMA_STRING =  ",";
    private static final String PLUS_STRING =  "+";
    private static final Set<String> excludes = new HashSet<String>();

    static
    {
        excludes.add("output");
        excludes.add("warnings");
        excludes.add("compiler.debug");
        excludes.add("compiler.profile");
        excludes.add("compiler.accessible");
        excludes.add("compiler.strict");
        excludes.add("compiler.show-actionscript-warnings");        
        excludes.add("compiler.show-unused-type-selector-warnings");
        excludes.add("compiler.show-deprecation-warnings");
        excludes.add("compiler.show-shadowed-device-font-warnings");
        excludes.add("compiler.show-binding-warnings");
        excludes.add("compiler.verbose-stacktraces");
        excludes.add("royale");
    }
    
    /**
     * Constructor
     */
    public Configurator()
    {
        this(Configuration.class);
    }
    
    /**
     * Constructor
     */
    public Configurator(Class<? extends Configuration> configurationClass)
    {
        this.configurationClass = configurationClass;
        args = new LinkedHashMap<String, Object>();
        more = new LinkedHashMap<String, Object>();
        tokens = new TreeMap<String, String>();
        
        keepLinkReport = false;
        keepSizeReport = false;
        keepConfigurationReport = false;
        reportMissingLibraries = true;
        warnOnRoyaleOnlyOptionUsage = false;
        isConfigurationDirty = true;
        configurationDefaultVariable = ICompilerSettingsConstants.FILE_SPECS_VAR; // the default variable of the configuration.
        configurationPathResolver = new ConfigurationPathResolver(System.getProperty("user.dir"));
        configurationProblems = new ArrayList<ICompilerProblem>();

        // initialize the localization manager.
        LocalizationManager.get().addLocalizer(new ResourceBundleLocalizer());
    }

    private ConfigurationBuffer cfgbuf;
    protected Configuration configuration;
    private Class<? extends Configuration> configurationClass;
    
    private Map<String, Object> args, more;
    private String[] extras;
    private String configurationDefaultVariable;
    private boolean keepLinkReport, keepSizeReport, keepConfigurationReport;
    private String mainDefinition;
    private boolean reportMissingLibraries;
    private boolean warnOnRoyaleOnlyOptionUsage;
    private List<String> loadedConfigFiles;
    private List<String> missingConfigFiles;
    
    private Map<String, String> tokens;
    
    private boolean isConfigurationDirty;
    private boolean configurationSuccess;
    protected Collection<ICompilerProblem> configurationProblems;
    private boolean extrasRequireDefaultVariable;
    private IPathResolver configurationPathResolver;
    
    protected ICompilerProject project;

    // 
    // IConfigurator related methods
    //
    
    @Override
    public List<String> getLoadedConfigurationFiles()
    {
        return loadedConfigFiles != null ? loadedConfigFiles :
            Collections.<String>emptyList();
    }
    
    @Override
    public List<String> getMissingConfigurationFiles()
    {
        return missingConfigFiles != null ? missingConfigFiles :
            Collections.<String>emptyList();
    }
    
    @Override
    public boolean applyToProject(ICompilerProject project)
    {
    	this.project = project;
        final IWorkspace workspace = project.getWorkspace();
        boolean success = processConfiguration();
        workspace.startIdleState();
        try
        {
            if (configuration == null)
                return false;
            
            applyConfiguration();
        }
        finally
        {
            workspace.endIdleState(IWorkspace.NIL_COMPILATIONUNITS_TO_UPDATE);
        }
        return success;
    }
    
    protected boolean applyConfiguration()
    {
        return false; // don't call this, just override it
    }

    protected String computeQNameForTargetFile()
    {
        List<String> computedSourcePath = new ArrayList<String>();
        applySourcePathRules(computedSourcePath);
        String targetSourceFileName = configuration.getTargetFile();
        if (targetSourceFileName == null)
            return null;
        final File targetSourceFile = FilenameNormalization.normalize(new File(configuration.getTargetFile()));
        return targetSourceFile.getName();
    }
    

    @Override
    public ITargetSettings getTargetSettings(TargetType targetType)
    {
        boolean wasConfigurationDirty = isConfigurationDirty;
        
        if (!processConfiguration())
            return null;

        // Special handling for dump config. If dump config was specified then
        // dump it out again if the configuration was not dirty. The config is
        // dumped as a side-effect of validating the configuration if it gets
        // processed.
        try
        {
            if (!wasConfigurationDirty)
            {
                if (cfgbuf.getVar(ICompilerSettingsConstants.DUMP_CONFIG_VAR) != null)
                {
                    // The file may have already been dumped as a side-effect of 
                    // applyToProject() so only dump if it does not exist.
                    String dumpConfigPath = configuration.getDumpConfig();
                    if (dumpConfigPath != null && !(new File(dumpConfigPath).exists()))
                        configuration.validateDumpConfig(cfgbuf);
                }
            }
            
            if (mainDefinition != null)
            {
                configuration.setMainDefinition(mainDefinition);
            }
            else if ((configuration.getMainDefinition() == null) && (targetType == TargetType.SWF))
            {
                String computedQName = computeQNameForTargetFile();
                if (computedQName != null)
                    configuration.setMainDefinition(computedQName);
            }

            if (targetType == TargetType.SWC)
                validateSWCConfiguration();
            else
                Configuration.validateNoCompcOnlyOptions(cfgbuf);
            
        }
        catch (ConfigurationException e)
        {
            reportConfigurationException(e);
            return null;
        }
        
        return instantiateTargetSettings();
    }

    protected ITargetSettings instantiateTargetSettings()
    {
        return new TargetSettings(configuration, project);
    }
    
    @Override    
    public ICompilerProblemSettings getCompilerProblemSettings()
    {
        processConfiguration();
        
        return new CompilerProblemSettings(configuration);
    }

    @Override
    public Collection<ICompilerProblem> validateConfiguration(String[] args, TargetType targetType)
    {
        if (args == null)
            throw new NullPointerException("args may not be null");
        
        List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        ConfigurationBuffer configurationBuffer = createConfigurationBuffer(configurationClass);
        
        try
        {
            CommandLineConfigurator.parse(configurationBuffer, null, args);

            // verify SWC-only args are not used to for a SWF target.
            if (targetType == TargetType.SWF)
                Configuration.validateNoCompcOnlyOptions(configurationBuffer);
        }
        catch (ConfigurationException e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(e);
            problems.add(problem);
        }
        
        return problems;
    }
    
    @Override
    public Collection<ICompilerProblem> getConfigurationProblems()
    {
        assert configuration != null : 
            "Get the configuration problems after calling applyToProject() or getTargetSettings()";
        
        List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(configurationProblems.size() +
                                            configuration.getConfigurationProblems().size());
        problems.addAll(configurationProblems);
        problems.addAll(configuration.getConfigurationProblems()); 
        return problems;
    }

    @Override
    public void setConfigurationPathResolver(IPathResolver pathResolver)
    {
        if (pathResolver == null)
            throw new NullPointerException("pathResolver may not be null");
        
        this.configurationPathResolver = pathResolver;
    }

    // Needed by MXMLC for now.
    public Configuration getConfiguration()
    {
        return configuration;
    }
    
    // Needed by MXMLC for now.
    public ConfigurationBuffer getConfigurationBuffer()
    {
        return cfgbuf;
    }

    /**
     * Create a new configuration instance. The Configurator will need to 
     * create a new configuration for each new configuration. For example,
     * creating a new Configurator and getting the target settings will create
     * a new configuration. If later on, the configuration is modified by calling
     * any of the setter methods on the Configurator, then a new configuration 
     * will be created the next time applyToProject() or getTargetSettings() is called.
     * 
     * The method may be overriden to allow for greater control when creating a 
     * custom configuration that extends the built-in configuration.
     * 
     * @return a new configuration instance. If the custom configuration class
     * cannot be created, the default configuration class will be created instead.
     */
    protected Configuration createConfiguration()
    {
        try
        {
            return configurationClass.newInstance();
        }
        catch (Exception e)
        {
            // If there is a problem initializing the configuration, then
            // throw a ConfigurationException.
            reportConfigurationException(new ConfigurationException.CouldNotInstantiate(configurationClass.getName()));

            // Create the default configuration so we can report configuration
            // problems.
            try
            {
                return Configuration.class.newInstance();                
            }
            catch (Exception e2)
            {
                // this should never fail
                assert(false);
                return null;
            }
        }
    }
    
    /**
     * Initialize the configuration and the configuration buffer.
     */
    protected void initializeConfiguration()
    {
        // Create a clean configuration and configuration buffer
        configuration = createConfiguration();
        cfgbuf = createConfigurationBuffer(configuration.getClass());
        assert configurationPathResolver != null : "No configuration path resolver was set.";
        configuration.setPathResolver(configurationPathResolver);
        configuration.setReportMissingCompilerLibraries(reportMissingLibraries);
        configuration.setWarnOnRoyaleOnlyOptionUsage(warnOnRoyaleOnlyOptionUsage);
    }

    /**
     * Create a configuration buffer.
     * @param configurationClass    The Configuration object
     * @return          the configuration buffer to use
     */
    protected ConfigurationBuffer createConfigurationBuffer(
            Class<? extends Configuration> configurationClass)
    {
        return new ConfigurationBuffer(
                configurationClass, Configuration.getAliases());
    }

    /**
     * Apply the follow source-path rules:
     * 
     * 1. If source-path is empty, the target file's directory will be added to 
     *    source-path.
     * 2. If source-path is not empty and if the target file's directory is a 
     *    sub-directory of one of the directories in source-path, source-path 
     *    remains unchanged.
     * 3. If source-path is not empty and if the target file's directory is not
     *    a sub-directory of any one of the directories in source-path, the target
     *    file's directory is prepended to source-path.
     *  
     * @param sourcePath the source path to apply the rules to.
     */
    protected void applySourcePathRules(List<String> sourcePath)
    {
        String targetFileDirectory = configuration.getTargetFileDirectory();
        List<String> configuredSourcePath = configuration.getCompilerSourcePath();
        
        if (targetFileDirectory != null)
        {
            
            // This method is called with an empty sourcePath so any additions will
            // have the effect of prepending to the sourcePath.
            if (configuredSourcePath.isEmpty() || 
                (!configuredSourcePath.contains(targetFileDirectory) &&
                 !isSubdirectoryOf(targetFileDirectory, configuredSourcePath)))
            {
                sourcePath.add(targetFileDirectory);
            }
        }
        
        // The configuration system provides additional source paths.
        sourcePath.addAll(configuredSourcePath);
    }
    
    /**
     * Check whether the provided path is a subdirectory of the list of 
     * directories and vice versa.
     * 
     * @param path - absolute path name of directory to test.
     * @param directories - {@link Iterable} of directories to test.
     * @return true if path is a subdirectory, false otherwise
     */
    private static boolean isSubdirectoryOf(String path, Iterable<String> directories)
    {
        final File pathFile = FileUtils.canonicalFile(new File(path));

        for (String directoryName : directories)
        {
            final File dirFile = FileUtils.canonicalFile(new File(directoryName));
            final long dirFilenameLength = dirFile.getAbsolutePath().length();

            File parentFile = pathFile.getParentFile();
            while (parentFile != null)
            {
                if (dirFile.equals(parentFile))
                    return true;

                // if the dirFilenameLength is greater than the parentFilename length
                // then break out and try the next path, rather than going all the
                // way up to the root, as it can't be a sub directory.
                if (dirFilenameLength > parentFile.getAbsolutePath().length())
                    break;

                parentFile = parentFile.getParentFile();
            }
        }

        return false;
    }

    public static List<RSLSettings> getRSLSettingsFromConfiguration(Configuration configuration)
    {
        List<RuntimeSharedLibraryPathInfo> infoList = configuration.getRslPathInfo();

        if (infoList == null || infoList.size() == 0)
        {
            return Collections.emptyList();
        }

        boolean verifyDigests = configuration.getVerifyDigests();
        List<RSLSettings> rslSettingsList = new ArrayList<RSLSettings>(infoList.size());
        for (RuntimeSharedLibraryPathInfo info : infoList)
        {
            // For each loop, convert an RSL and its failovers into
            // the RSLSettings class.
            RSLSettings rslSettings = new RSLSettings(info.getSWCFile());
            List<String> rslURLs = info.getRSLURLs();
            List<String> policyFileURLs = info.getPolicyFileURLs();

            int n = info.getRSLURLs().size();
            for (int i = 0; i < n; i++)
            {
                rslSettings.addRSLURLAndPolicyFileURL(rslURLs.get(i), policyFileURLs.get(i));
            }

            rslSettings.setVerifyDigest(verifyDigests);
            
            String swcPath = info.getSWCFile().getPath();
            rslSettings.setApplicationDomain(configuration.getApplicationDomain(swcPath));
            rslSettings.setForceLoad(configuration.getForceRsls().contains(swcPath));
            
            // Add an RSL to the list of RSL settings.
            rslSettingsList.add(rslSettings);
        }

        return rslSettingsList;
    }

    /**
     * Wrapper around the real processConfiguration.
     * 
     * @return true if success, false otherwise.
     */
    protected boolean processConfiguration()
    {
        boolean success = true;
        
        if (isConfigurationDirty)
        {
            configurationProblems.clear();
            
            try
            {
                success = processConfiguration(getOptions(args, more, processExtras(extras)));
            }
            catch (ConfigurationException e)
            {
                reportConfigurationException(e);
                success = false;
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            }
        }
        else
        {
            success = configurationSuccess;
        }
        
        isConfigurationDirty = false;
        configurationSuccess = success;
        return success;
    }

    /**
     * Does all the work to set the command line arguments info the 
     * configuration object.
     *  
     * @param argsArray - command line arguments
     * 
     * @return true if successful, false otherwise.
     */
    protected boolean  processConfiguration(String[] argsArray)
    {
        initializeConfiguration();
        
        boolean success = true;

        try
        {
            loadDefaults(cfgbuf);

            // TODO This is needed until we can defer
            // the default style loading in loadDefaults().  
            byPassConfigurationsRequiringFlexSDK();
            
            SystemPropertyConfigurator.load(cfgbuf, "royale");

            // Parse the command line a first time, to peak at stuff like
            // "royalelib" and "load-config".  The first parse is thrown
            // away after that and we intentionally parse a second time
            // below.  See note below.
            CommandLineConfigurator.parse(cfgbuf, configurationDefaultVariable, argsArray);

            overrideDefaults();

            // Return if "-version" is present so the command line can print the 
            // version.
            if (cfgbuf.getVar("version") != null)
                return false;

            // Return so the command line can print help if "-help" is present.
            final List<ConfigurationValue> helpVar = cfgbuf.getVar("help");
            if (helpVar != null)
                return false;
            
            // Load configurations from files.
            if (!loadConfig())
                success = false;
            
            if (!loadProjectConfig())
                success = false;
            
            // The command line needs to take precedence over all defaults and config files.
            // By simply re-merging the command line back on top,
            // we will get the behavior we want.
            cfgbuf.clearSourceVars(CommandLineConfigurator.source);
            CommandLineConfigurator.parse(cfgbuf, configurationDefaultVariable, argsArray);

            // commit() reports problems instead of throwing an exception. This 
            // allows us to process all the options in a configuration that
            // are correct in the hopes that it will be enough to configure a
            // project.
            if (!cfgbuf.commit(configuration, configurationProblems))
                success = false;
            
            configuration.validate(cfgbuf);
        }
        catch (ConfigurationException e)
        {
            reportConfigurationException(e);
            success = false;
        }
        
        return success;
    }

    /**
     * Load the default values into the passed in config buffer
     * @param cfgbuf                    the config buffer to set the default values in
     * @throws ConfigurationException
     */
    protected void loadDefaults (ConfigurationBuffer cfgbuf) throws ConfigurationException
    {
        DefaultsConfigurator.loadDefaults(cfgbuf);
    }

    /**
     * Do the validatation that the old COMPCConfiguration used to do.
     */
    protected void validateSWCConfiguration() throws ConfigurationException
    {
        validateSWCInputs();
        
        // verify that if -include-inheritance-dependencies is set that
        // -include-classes is not null
        if (configuration.getIncludeInheritanceDependenciesOnly() &&
            configuration.getIncludeClasses().size() == 0)
        {
            throw new ConfigurationException.MissingIncludeClasses();
        }
    }

    /**
     * Basic validation of compc options. This code used to be
     * in the old compiler's CompcConfiguration.
     * 
     * @throws ConfigurationException
     */
    protected void validateSWCInputs() throws ConfigurationException
    {
        if (configuration.getIncludeSources().isEmpty() && 
            configuration.getIncludeClasses().isEmpty() &&
            configuration.getIncludeNamespaces().isEmpty() &&
            ((configuration.getCompilerIncludeLibraries() == null) || 
             (configuration.getCompilerIncludeLibraries().size() == 0)) &&
             configuration.getIncludeFiles().isEmpty() && 
             configuration.getIncludeResourceBundles().isEmpty())
        {
            throw new ConfigurationException.NoSwcInputs( null, null, -1 );
        }
    }
    
    /**
     * By-pass the configurations that requires Flex SDK.
     */
    protected void byPassConfigurationsRequiringFlexSDK() throws ConfigurationException
    {
        if (System.getProperty("royalelib") == null &&
            System.getProperty("application.home") == null)
        {
            cfgbuf.clearVar("load-config", null, -1);
            cfgbuf.clearVar("compiler.theme", null, -1);
        }
        
    }

    /**
     * Override default values.
     */
    protected void overrideDefaults() throws ConfigurationException
    {
        String royalelib = cfgbuf.getToken("royalelib");
        if (royalelib == null)
        {
            final String appHome = System.getProperty("application.home");
            if (appHome == null)
                cfgbuf.setToken("royalelib", ".");
            else
                cfgbuf.setToken("royalelib", appHome + File.separator + "frameworks");
        }

        // Framework Type: halo, gumbo, interop...
        String framework = cfgbuf.getToken("framework");
        if (framework == null)
            cfgbuf.setToken("framework", "halo");

        String configname = cfgbuf.getToken("configname");
        if (configname == null)
            cfgbuf.setToken("configname", "royale");

        String buildNumber = cfgbuf.getToken("build.number");
        if (buildNumber == null)
        {
            if ("".equals(VersionInfo.getBuild()))
                buildNumber = "workspace";
            else
                buildNumber = VersionInfo.getBuild();
            cfgbuf.setToken("build.number", buildNumber);
        }   
    }

    /**
     * Load configuration XML file specified in {@code -load-config} option on
     * command-line.
     * 
     * @return true if successful, false otherwise.
     */
    protected boolean loadConfig()
    {
        boolean success = true;
        
        List<ConfigurationValue> configs;
        try
        {
            configs = cfgbuf.peekConfigurationVar("load-config");
            if (configs != null)
            {
                for (ConfigurationValue cv : configs)
                {
                    for (String path : cv.getArgs())
                    {
                        File configFile = configurationPathResolver.resolve(path);
                        if (!configFile.exists() && path.contains("royale-config"))
                        {
                        	File configFile2 = configurationPathResolver.resolve(path.replace("royale-config", "flex-config"));
                        	if (configFile2.exists())
                        		configFile = configFile2;
                        }
                        if (!configFile.exists())
                        {
                            success = false;
                            if (missingConfigFiles == null)
                                missingConfigFiles = new ArrayList<String>();
                            
                            missingConfigFiles.add(path);
                        }
                        else 
                        {
                           if (!loadConfigFromFile(
                                    cfgbuf,
                                    configFile,
                                    new File(configFile.getPath()).getParent(),
                                    "royale-config",
                                    false))
                           {
                               success = false;
                           }
                        }
                    }
                }
            }
        }
        catch (ConfigurationException e)
        {
            reportConfigurationException(e);
            success = false;
        }

        return success;
    }

    /**
     * Load a configuration from file. {@code FileConfigurator.load()} is
     * wrapped in this method because we want to print a message after loading
     * using MXMLC#println(String).
     * 
     * @return true if successful, false otherwise.
     */
    protected final boolean loadConfigFromFile(final ConfigurationBuffer buffer,
                                          final File fileSpec,
                                          final String context,
                                          final String rootElement,
                                          final boolean ignoreUnknownItems)
    {
        boolean success = true;
        
        try
        {
            FileConfigurator.load(buffer, 
                    new FileSpecification(fileSpec.getAbsolutePath()),
                    context, rootElement, ignoreUnknownItems);
        }
        catch (ConfigurationException e)
        {
            // record exception
            reportConfigurationException(e);
            success = false;
            
        }

        if (loadedConfigFiles == null)
            loadedConfigFiles = new ArrayList<String>();
        
        loadedConfigFiles.add(fileSpec.getPath());
        
        return success;
    }

    /**
     * Convert conifguration exceptions to problems and collect them for 
     * reporting.
     * 
     * @param e
     */
    protected void reportConfigurationException(ConfigurationException e)
    {
        final ICompilerProblem problem = new ConfigurationProblem(e);
        configurationProblems.add(problem);
    }

    /**
     * Load project specific configuration. The configuration XML file is at the
     * project root with naming convention of [project name]-config.xml.
     * 
     * @return true if successful, false otherwise.
     */
    protected boolean loadProjectConfig()
    {
        boolean success = true;
        
        // Load project file, if any...
        List<ConfigurationValue> fileValues = cfgbuf.getVar(ICompilerSettingsConstants.FILE_SPECS_VAR);
        if ((fileValues != null) && (fileValues.size() > 0))
        {
            ConfigurationValue cv = fileValues.get(fileValues.size() - 1);
            if (cv.getArgs().size() > 0)
            {
                String val = cv.getArgs().get(cv.getArgs().size() - 1);
                int index = val.lastIndexOf('.');
                if (index != -1)
                {
                    String project = val.substring(0, index) + "-config.xml";
                    File projectFile = configurationPathResolver.resolve(project);
                    if (projectFile.exists())
                    {
                        if (!loadConfigFromFile(
                                cfgbuf,
                                projectFile,
                                new File(project).getParent(),
                                "royale-config",
                                false))
                        {
                            success = false;
                        }
                    }
                }
            }
        }
        
        return success;
    }
    
    //
    // Configuration related methods
    //
    protected String[] getOptions(Map<String, Object> args, Map<String, Object> more, 
            String[] extras)
    {
        ArrayList<String> buffer = new ArrayList<String>();
        
        for (Map.Entry<String, String> tokenEntry : tokens.entrySet())
        {
            buffer.add(PLUS_STRING + tokenEntry.getKey() + EQUALS_STRING + tokenEntry.getValue());
        }
        
        for (Map.Entry<String, Object> arg : args.entrySet())
        {
            String key = arg.getKey();
            Object value = arg.getValue();

            if (value instanceof Boolean)
            {
                buffer.add(key + EQUALS_STRING + value);
            }
            else if (value instanceof Number)
            {
                buffer.add(key);
                buffer.add(value.toString());
            }
            else if (COMPILER_CONTEXT_ROOT.equals(key) && value instanceof String)
            {               
                buffer.add(key);
                buffer.add((String)value);
            }
            else if (value instanceof String)
            {               
                if (!"".equals(value))
                {
                    buffer.add(key);
                    buffer.add((String)value);
                }
                else
                {
                    buffer.add(key + EQUALS_STRING);
                }
            }
            else if (value instanceof File)
            {
                String p = ((File) value).getPath();
                if (!"".equals(p))
                {
                    buffer.add(key);
                    buffer.add(p);
                }
                else
                {
                    buffer.add(key + EQUALS_STRING);
                }
            }
            else if (value instanceof java.util.Date)
            {
                buffer.add(key);
                buffer.add(value.toString());
            }
            else if (value instanceof MXMLNamespaceMappingList)
            {
                addNamespaceMappingsToBuffer(buffer, (MXMLNamespaceMappingList)value, false);
            }
            else if (value instanceof RSLSettingsList)
            {
                addRSLSettingsToBuffer(buffer, (RSLSettingsList)value, false);
            }
            else if (value instanceof CompilerDefinitionMap)
            {
                final CompilerDefinitionMap defs = (CompilerDefinitionMap)value;
                for (Map.Entry<String, String> entry : defs.entrySet())
                {
                    // String.valueOf will help turn null into "null"
                    String name = entry.getKey();
                    String val  = entry.getValue();
                    
                    // handle empty-string values
                    
                    // technically, name should not ever be empty length (value can be),
                    // but we don't want to do error handling, CompilerConfiguration.cfgDefine()
                    // will do it for us later
                    if (name.length() == 0)
                    {
                        name = "\"\"";
                    }
                    
                    if (val.length() == 0)
                    {
                        val = "\"\"";
                    }
                    
                    /* note '+=': defines from all royale-config.xmls will be collected (just '=' would
                     * always ignore all but the most recent definitions), hopefully in a meaningful
                     * order (royale-config, user-config, commandline) since we now allow re-definitions.
                     */
                    buffer.add(COMPILER_DEFINE + PLUS_EQUALS_STRING + name + COMMA_STRING + val);
                }
            }
            else if (value instanceof Map)
            {
                @SuppressWarnings("unchecked")
                Map<String, ?> m = (Map<String, ?>) value;
                for (Map.Entry<String, ?>entry : m.entrySet())
                {
                    String k = entry.getKey();
                    Object v = entry.getValue();
                    
                    if (v instanceof String)
                    {
                        buffer.add(key);
                        buffer.add(k);
                        buffer.add((String)v);
                    }
                    else if (v instanceof File)
                    {
                        buffer.add(key);
                        buffer.add(k);
                        buffer.add(((File) v).getPath());
                    }
                    else if (v instanceof Collection)
                    {
                        buffer.add(key);
                        buffer.add(k);
                        Collection<?> list = (Collection<?>)v;
                        for (Object next : list)
                        {
                            if (next != null)
                                buffer.add(next.toString());
                        }
                    }
                    else if (v != null)
                    {
                        assert false;
                    }
                }
            }
            else if (value instanceof int[])
            {
                int[] a = (int[]) value;
                buffer.add(key);
                buffer.add(String.valueOf(a[0]));
                buffer.add(String.valueOf(a[1]));
            }
            else if (value instanceof Collection)
            {
                Collection<Object> list = new LinkedList<Object>((Collection<?>)args.get(key));
                
                int length = list.size();
                if (length > 0)
                {
                    buffer.add(key);
                }
                else if (!LOAD_CONFIG.equals(key))
                {
                    buffer.add(key + EQUALS_STRING);
                }
                for (Object obj : list)
                {
                    if (obj instanceof String)
                    {
                        buffer.add((String)obj);
                    }
                    else if (obj instanceof File)
                    {
                        buffer.add(((File)obj).getPath());
                    }
                }
            }
            else if (value != null)
            {
                assert false;
            }
            else
            {
                // System.err.println("unprocessed compiler options: " + key + EQUALS_STRING + value);
            }
        }
        
        for (Map.Entry<String, Object> moreEntry : more.entrySet())
        {
            String key = moreEntry.getKey();
            Object value = moreEntry.getValue();

            if (value instanceof Collection)
            {
                buffer.add(key + PLUS_EQUALS_STRING + toCommaSeparatedString((Collection<?>)value));
            }
            else if (value instanceof Map)
            {
                @SuppressWarnings("unchecked")
                Map<String, ?> m = (Map<String, ?>) value;
                for (Map.Entry<String, ?>entry : m.entrySet())
                {
                    String k = entry.getKey();
                    Object v = entry.getValue();
                    
                    if (v instanceof Collection)
                    {
                        buffer.add(key + PLUS_EQUALS_STRING + k + COMMA_STRING +
                                toCommaSeparatedString((Collection<?>)v));
                    }
                    else if (v != null)
                    {
                        assert false;
                    }
                }
            }
            else if (value instanceof MXMLNamespaceMappingList)
            {
                addNamespaceMappingsToBuffer(buffer, (MXMLNamespaceMappingList)value, true);
            }
            else if (value instanceof RSLSettingsList)
            {
                addRSLSettingsToBuffer(buffer, (RSLSettingsList)value, true);
            }
            else if (value != null)
            {
                assert false;
            }
            else
            {
                // System.err.println("unprocessed compiler options: " + key + EQUALS_STRING + value);
            }
        }

        // Append extra command line args to the buffer.
        if (extras != null && extras.length > 0)
        {
            for (int i = 0, length = extras == null ? 0 : extras.length; i < length; i++)
            {
                if (extras[i] != null)
                {
                    buffer.add(extras[i]);
                }
            }            
        }
        
        String[] options = new String[buffer.size()];
        buffer.toArray(options);
    
        if (Trace.config)
            Trace.trace("Configurator: options = " + buffer.toString());
        
        return options;
    }

    /**
     * Do special case handling of extra arguments for Flash Builder. FB needs 
     * special handling for some arguments because it sets them using the 
     * Configurator API and they can also be entered by a user in 
     * "additional compiler arguments". When both are entered the 
     * "additional" arguments win. This code isn't pretty but it mimics the behavior
     * of the old compiler.
     * 
     * @param extraOptions
     * 
     * @return new array of extra arguments to use. Returns null if
     * extraOptions is null. 
     * @throws ConfigurationException 
     */
    protected String[] processExtras(String[] extraOptions) throws ConfigurationException
    {
        // If the extraOptions have default variables then don't process them 
        // because stripping out options can introduce problems parsing the
        // options. One specific case is 
        // "-source-path . -output comps.swc MyComboBox.mxml MyButton.mxml"
        // This is not ambiguous. Stripping out the "output" option leaves
        // us with -source-path . MyComboBox.mxml MyButton.mxml" which is
        // ambiguous.
        if (extraOptions == null || extrasRequireDefaultVariable)
            return extraOptions;
        
        List<Object> newArgs = new ArrayList<Object>();
        ConfigurationBuffer extrasBuffer = createConfigurationBuffer(configurationClass);

        CommandLineConfigurator.parse(extrasBuffer, null, extraOptions);
        
        List<Object[]> positions = extrasBuffer.getPositions();
        for (int i = 0, length = positions.size(); i < length; i++)
        {
            Object[] a = positions.get(i);
            String var = (String) a[0];
            
            if ("link-report".equals(var))
            {
                keepLinkReport(true);
            }
            else if ("compiler.debug".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    String debugPassword = extrasBuffer.peekSimpleConfigurationVar("debug-password");
                    if ("true".equals(value))
                    {
                        enableDebugging(true, debugPassword);
                    }
                    else if ("false".equals(value))
                    {
                        enableDebugging(false, debugPassword);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.verbose-stacktraces".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        enableVerboseStacktraces(true);
                    }
                    else if ("false".equals(value))
                    {
                        enableVerboseStacktraces(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }                           
            }
            else if ("compiler.accessible".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        enableAccessibility(true);
                    }
                    else if ("false".equals(value))
                    {
                        enableAccessibility(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.strict".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        enableStrictChecking(true);
                    }
                    else if ("false".equals(value))
                    {
                        enableStrictChecking(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("output".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    setOutput(new File(value));
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("size-report".equals(var))
            {
                keepSizeReport(true);
            }
            else if ("warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showActionScriptWarnings(true);
                        showBindingWarnings(true);
                        showDeprecationWarnings(true);
                        showUnusedTypeSelectorWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showActionScriptWarnings(false);
                        showBindingWarnings(false);
                        showDeprecationWarnings(false);
                        showUnusedTypeSelectorWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.show-actionscript-warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showActionScriptWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showActionScriptWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.show-deprecation-warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showDeprecationWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showDeprecationWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.show-binding-warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showBindingWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showBindingWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.show-unused-type-selector-warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showUnusedTypeSelectorWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showUnusedTypeSelectorWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("compiler.show-multiple-definition-warnings".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        showMultipleDefinitionWarnings(true);
                    }
                    else if ("false".equals(value))
                    {
                        showMultipleDefinitionWarnings(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            else if ("royale".equals(var))
            {
                try
                {
                    String value = extrasBuffer.peekSimpleConfigurationVar(var);
                    if ("true".equals(value))
                    {
                        setRoyale(true);
                    }
                    else if ("false".equals(value))
                    {
                        setRoyale(false);
                    }
                }
                catch (ConfigurationException ex)
                {
                }
            }
            if (!excludes.contains(var))
            {
                // keep this variable
                int iStart = ((Integer) a[1]).intValue();
                int iEnd = ((Integer) a[2]).intValue();
                for (int j = iStart; j < iEnd; j++)
                {
                    newArgs.add(extraOptions[j]);
                }
            }
        }

        extraOptions = new String[newArgs.size()];
        newArgs.toArray(extraOptions);
        return extraOptions;
    }

    /**
     * Add namespace mappings to the command line buffer.
     * 
     * @param buffer
     * @param valueList
     * @param append true to use append the command in the buffer, otherwise set the command.
     */
    private void addNamespaceMappingsToBuffer(ArrayList<String> buffer, MXMLNamespaceMappingList valueList, 
            boolean append)
    {
        if (valueList.isEmpty())
        {
            StringBuilder sb = new StringBuilder(COMPILER_NAMESPACES_NAMESPACE);
            sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);            
            buffer.add(sb.toString());
            return;
        }
        
        for (IMXMLNamespaceMapping value : valueList) 
        {
            StringBuilder sb = new StringBuilder(COMPILER_NAMESPACES_NAMESPACE);
            sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);
            sb.append(value.getURI());
            sb.append(COMMA_STRING);
            sb.append(value.getManifestFileName());
            
            buffer.add(sb.toString());
        }
        
        
    }

    /**
     * Append RSLSettings to the command line buffer.
     * 
     * @param buffer
     * @param value
     * @param append true if the values should be appended to existing RSL settings ("+="), false
     * for override existing RSL settings ("=").
     */
    private void addRSLSettingsToBuffer(ArrayList<String> buffer, RSLSettingsList valueList, 
            boolean append)
    {
        // if the list is empty, override any config files.
        if (valueList.isEmpty())
        {
            StringBuilder sb = new StringBuilder(RUNTIME_SHARED_LIBRARY_PATH);
            sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);
            buffer.add(sb.toString());
            return;
        }
        
        // runtime-shared-library-path=path-element,rsl-url,policy-file-url,...
        for (RSLSettings settings : valueList) 
        {
            StringBuilder sb = new StringBuilder(RUNTIME_SHARED_LIBRARY_PATH);
            sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);
            
            sb.append(settings.getLibraryFile().getPath());
            for (RSLAndPolicyFileURLPair urls : settings.getRSLURLs())
            {
                sb.append(COMMA_STRING);
                sb.append(urls.getRSLURL());
                sb.append(COMMA_STRING);
                sb.append(urls.getPolicyFileURL());
            }

            buffer.add(sb.toString());
            
            // application-domain=path-element,application-domain-target
            if (settings.getApplicationDomain() != ApplicationDomainTarget.DEFAULT)
            {
                sb = new StringBuilder(RUNTIME_SHARED_LIBRARY_SETTINGS_APPLICATION_DOMAIN);
                sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);
                sb.append(settings.getLibraryFile().getPath());
                sb.append(COMMA_STRING);
                sb.append(settings.getApplicationDomain().getApplicationDomainValue());                

                buffer.add(sb.toString());
            }

            // force-rsls=path-element
            if (settings.isForceLoad())
            {
                sb = new StringBuilder(RUNTIME_SHARED_LIBRARY_SETTINGS_FORCE_RSLS);
                sb.append(append ? PLUS_EQUALS_STRING : EQUALS_STRING);
                sb.append(settings.getLibraryFile().getPath());

                buffer.add(sb.toString());                
            }
        }
        
    }

    /**
     * Enables accessibility in the application.
     * This is equivalent to using <code>mxmlc/compc --compiler.accessible</code>.<p>
     * By default, this is disabled.
     * 
     * @param b boolean value
     */
    @Override
    public void enableAccessibility(boolean b)
    {
        args.put(COMPILER_ACCESSIBLE, b ? Boolean.TRUE : Boolean.FALSE);

        isConfigurationDirty = true;
    }
    
    /**
     * Sets the ActionScript file encoding. The compiler will use this encoding to read
     * the ActionScript source files.
     * This is equivalent to using <code>mxmlc/compc --compiler.actionscript-file-encoding</code>.<p>
     * By default, the encoding is <code>UTF-8</code>.
     * 
     * @param encoding charactere encoding, e.g. <code>UTF-8</code>, <code>Big5</code>
     */
    @Override
    public void setActionScriptFileEncoding(String encoding)
    {
        args.put(COMPILER_ACTIONSCRIPT_FILE_ENCODING, encoding);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Allows some source path directories to be subdirectories of the other.
     * This is equivalent to using <code>mxmlc/compc --compiler.allow-source-path-overlap</code>.<p>
     * By default, this is disabled.<p>
     * 
     * In some J2EE settings, directory overlapping should be allowed. For example,
     * 
     * <pre>
     * wwwroot/MyAppRoot
     * wwwroot/WEB-INF/flex/source_path1
     * </pre>
     * 
     * @param b boolean value
     */
    @Override
    public void allowSourcePathOverlap(boolean b)
    {
        args.put(COMPILER_ALLOW_SOURCE_PATH_OVERLAP, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets the context root path so that the compiler can replace <code>{context.root}</code> tokens for
     * service channel endpoints. This is equivalent to using the <code>compiler.context-root</code> option
     * for the mxmlc or compc compilers.
     * 
     * <p>
     * By default, this value is undefined.
     * 
     * @param path An instance of String.
     */
    @Override
    public void setContextRoot(String path)
    {
        args.put(COMPILER_CONTEXT_ROOT, path);
        
        isConfigurationDirty = true;
    }

    /**
     * Enables debugging in the application.
     * This is equivalent to using <code>mxmlc/compc --compiler.debug</code> and <code>--debug-password</code>.<p>
     * By default, debug is <code>false</code> and the debug password is "".
     * 
     * @param b boolean value
     * @param debugPassword a password that is embedded in the application.
     */
    @Override
    public void enableDebugging(boolean b, String debugPassword)
    {
        args.put(COMPILER_DEBUG, b ? Boolean.TRUE : Boolean.FALSE);
        args.put(DEBUG_PASSWORD, debugPassword);
        
        isConfigurationDirty = true;
    }

    /**
     * Enable or disable runtime shared libraries in the application.
     * 
     * This is equivalent to using <code>mxmlc --static-link-runtime-shared-libraries</code>.<p>
     * 
     * @param b boolean value
     */
    public void setStaticLinkRuntimeSharedLibraries(boolean b)
    {
        args.put(STATIC_LINK_RUNTIME_SHARED_LIBRARIES, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }

    /**
     * Sets the location of the default CSS file.
     * This is equivalent to using <code>mxmlc/compc --compiler.defaults-css-url</code>.
     * 
     * @param url an instance of <code>java.io.File</code>.
     */
    public void setDefaultCSS(File url)
    {
        args.put(COMPILER_DEFAULTS_CSS_URL, url);

        isConfigurationDirty = true;
    }
    
    /**
     * Sets the list of SWC files or directories to compile against but to omit from linking.
     * This is equivalent to using <code>mxmlc/compc --compiler.external-library-path</code>.
     * 
     * @param paths <code>File.isDirectory()</code> should return <code>true</code> or <code>File</code> instances should represent SWC files.
     */
    @Override
    public void setExternalLibraryPath(Collection<File> paths)
    {
        removeNativeJSLibrariesIfNeeded(paths);

        args.put(COMPILER_EXTERNAL_LIBRARY_PATH, paths);
        more.remove(COMPILER_EXTERNAL_LIBRARY_PATH);

        isConfigurationDirty = true;
    }

    private void removeNativeJSLibrariesIfNeeded(Collection<File> paths) {
        Iterator<File> fileIterator = paths.iterator();

        while (fileIterator.hasNext()) {
            final File file = fileIterator.next();
            final boolean isNativeJS = file.getAbsolutePath().contains("js/libs");
            boolean excludeNativeJS = false;

            try {
                excludeNativeJS = cfgbuf.peekSimpleConfigurationVar("exclude-native-js-libraries").equals(Boolean.TRUE.toString());
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }

            if (isNativeJS && excludeNativeJS) {
                fileIterator.remove();
            }
        }
    }

    /**
     * Adds to the existing list of SWC files.
     * 
     * @see #setExternalLibraryPath
     * @param paths <code>File.isDirectory()</code> should return <code>true</code> or <code>File</code> instances should represent SWC files.
     */
    @Override
    public void addExternalLibraryPath(Collection<File> paths)
    {
        removeNativeJSLibrariesIfNeeded(paths);

        addFiles(COMPILER_EXTERNAL_LIBRARY_PATH, paths);

        isConfigurationDirty = true;
    }

    /**
     * Sets the AS3 metadata the compiler should keep in the SWF.
     * This is equivalent to using <code>mxmlc --compiler.keep-as3-metadata</code>.
     * 
     * <p>
     * The default value is <code>{Bindable, Managed, ChangeEvent, NonCommittingChangeEvent, Transient}</code>.
     * 
     * @param md an array of AS3 metadata names
     */
    public void setASMetadataNames(String[] md)
    {
        args.put(COMPILER_KEEP_AS3_METADATA, md);
        more.remove(COMPILER_KEEP_AS3_METADATA);

        isConfigurationDirty = true;
    }
    
    /**
     * Adds the list of AS3 metadata names to the existing list of AS3 metadata the compiler should
     * keep in the SWF.
     * 
     * @param md an array of AS3 metadata names
     */
    public void addASMetadataNames(Collection<String> md)
    {
        addStrings(COMPILER_KEEP_AS3_METADATA, md);

        isConfigurationDirty = true;
    }

    /**
     * Disables the pruning of unused type selectors.
     * This is equivalent to using <code>mxmlc/compc --compiler.keep-all-type-selectors</code>.
     * By default, it is set to <code>false</code>.
     * 
     * @param b boolean value
     */
    public void keepAllTypeSelectors(boolean b)
    {
        args.put(COMPILER_KEEP_ALL_TYPE_SELECTORS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
}
    
    /**
     * Instructs the linker to keep a report of the content that is included in the application.
     * Callers may use <code>Report.writeLinkReport()</code> to retrieve the linker report.
     * 
     * @param b boolean value
     */
    public void keepLinkReport(boolean b)
    {
        keepLinkReport = b;
        
        isConfigurationDirty = true;
    }
    
    public boolean keepLinkReport()
    {
        return keepLinkReport;
    }
    
    /**
     * Instructs the linker to keep a SWF size report.
     * Callers may use <code>Report.writeSizeReport()</code> to retrieve the size report.
     * 
     * @param b boolean value
     */
    public void keepSizeReport(boolean b)
    {
        keepSizeReport = b;
        
        isConfigurationDirty = true;
    }
    
    public boolean keepSizeReport()
    {
        return keepSizeReport;
    }
    
    /**
     * Instructs the compiler to keep a report of the compiler configuration settings.
     * Callers may use <code>Report.writeConfigurationReport()</code> to retrieve the configuration report.
     * 
     * @param b boolean value
     */
    public void keepConfigurationReport(boolean b)
    {
        keepConfigurationReport = b;
        
        isConfigurationDirty = true;
    }
    
    public boolean keepConfigurationReport()
    {
        return keepConfigurationReport;
    }


    /**
     * Instructs the compiler to report missing Libraries as a configuration error.
     * 
     * @param b boolean value
     */
    public void reportMissingsLibraries(boolean b)
    {
        reportMissingLibraries = b;

        isConfigurationDirty = true;
    }

    /**
     * Controls if the compiler warns when "Royale only" configuration options
     * are used in the compiler.
     * 
     * @param value True to enable warnings, false to disable warnings. The
     * default is to not warn.
     */
    public void setWarnOnRoyaleOnlyOptionUsage(boolean value)
    {
        this.warnOnRoyaleOnlyOptionUsage = value;
        
        isConfigurationDirty = true;
    }
    
    /**
     * Includes a list of libraries (SWCs) to completely include in the application
     * This is equivalent to using <code>mxmlc/compc --compiler.include-libraries</code>.
     * 
     * @param libraries a collection of <code>java.io.File</code> (<code>File.isDirectory()</code> should return <code>true</code> or instances must represent SWC files).
     */
    @Override
    public void setIncludeLibraries(Collection<File> libraries)
    {
        args.put(COMPILER_INCLUDE_LIBRARIES, libraries);
        
        isConfigurationDirty = true;
    }

    /**
     * Sets a list of resource bundles to include in the swf.
     * This is equivalent to using <code>mxmlc/compc --include-resource-bundle</code>.
     * 
     * @param bundles an array of <code>java.lang.String</code>
    */
    @Override
    public void setIncludeResourceBundles(Collection<String> bundles)
    {
        args.put(INCLUDE_RESOURCE_BUNDLES, bundles);        
        
        isConfigurationDirty = true;
    }   

    /**
     * Adds a list of resource bundles to the existing list.
     * 
     * @see #setIncludeResourceBundles
     * @param bundles an array of <code>java.lang.String</code>
     */
    public void addIncludeResourceBundles(Collection<String> bundles)
    {
        addStrings(INCLUDE_RESOURCE_BUNDLES, bundles);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of SWC files or directories that contain SWC files.
     * This is equivalent to using <code>mxmlc/compc --compiler.library-path</code>.
     * 
     * @param paths an array of <code>File</code>. <code>File.isDirectory()</code> should return <code>true</code> or instances must represent SWC files.
     */
    @Override
    public void setLibraryPath(Collection<File> paths)
    {
        removeNativeJSLibrariesIfNeeded(paths);

        args.put(COMPILER_LIBRARY_PATH, paths);
        more.remove(COMPILER_LIBRARY_PATH);
        
        isConfigurationDirty = true;
    }

    /**
     * Adds a list of SWC files or directories to the default library path.
     * 
     * @param paths an array of <code>File</code>. <code>File.isDirectory()</code> should return <code>true</code> or instances must represent SWC files.
     * @see #setLibraryPath
     */
    public void addLibraryPath(Collection<File> paths)
    {
        removeNativeJSLibrariesIfNeeded(paths);

        addFiles(COMPILER_LIBRARY_PATH, paths);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets the locales that the compiler would use to replace <code>{locale}</code> tokens that appear in some configuration values.
     * This is equivalent to using <code>mxmlc/compc --compiler.locale</code>.
     * For example,
     * 
     * <pre>
     * addSourcePath(new File[] { "locale/{locale}" });
     * addLocale(new String[] { "en_US" });
     * </pre>
     * 
     * The <code>locale/en_US</code> directory will be added to the source path.
     * @since 3.0
     */
    public void setLocale(String[] locales)
    {
        args.put(COMPILER_LOCALE, locales);
        
        isConfigurationDirty = true;
    }
    
    @Override
    public void setNamespaceMappings(List<? extends IMXMLNamespaceMapping> namespaceMappings)
    {
        args.put(COMPILER_NAMESPACES_NAMESPACE, 
                namespaceMappings != null ? new MXMLNamespaceMappingList(namespaceMappings) : null);
        more.remove(COMPILER_NAMESPACES_NAMESPACE);
        
        isConfigurationDirty = true;
    }

    @Override
    public void addNamespaceMappings(Collection<IMXMLNamespaceMapping> namespaceMappings)
    {
        MXMLNamespaceMappingList currentSettings = (MXMLNamespaceMappingList)more.get(COMPILER_NAMESPACES_NAMESPACE);
        if (currentSettings == null)
            currentSettings = new MXMLNamespaceMappingList(namespaceMappings.size());
        
        currentSettings.addAll(namespaceMappings);

        isConfigurationDirty = true;
    }

    /**
     * Enables post-link optimization. This is equivalent to using <code>mxmlc/compc --compiler.optimize</code>.
     * Application sizes are usually smaller with this option enabled.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void optimize(boolean b)
    {
        args.put(COMPILER_OPTIMIZE, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    @Override
    public void compress(boolean b)
    {
        args.put(COMPILER_COMPRESS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }

    @Override
    public void setServicesXMLPath(String path, String contextRoot)
    {
        if (path == null || contextRoot == null)
            throw new NullPointerException("path and contenxtRoot may not be null.");
        
        args.put(COMPILER_SERVICES, new ServicesContextRoot(path, contextRoot));
        
        isConfigurationDirty = true;
    }
    
    /**
     * Runs the ActionScript compiler in a mode that detects legal but potentially incorrect code.
     * This is equivalent to using <code>mxmlc/compc --compiler.show-actionscript-warnings</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     * @see #checkActionScriptWarning(int, boolean)
     */
    @Override
    public void showActionScriptWarnings(boolean b)
    {
        args.put(COMPILER_SHOW_ACTIONSCRIPT_WARNINGS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Toggles whether warnings generated from data binding code are displayed.
     * This is equivalent to using <code>mxmlc/compc --compiler.show-binding-warnings</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void showBindingWarnings(boolean b)
    {
        args.put(COMPILER_SHOW_BINDING_WARNINGS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }

    /**
     * Toggles whether the use of deprecated APIs generates a warning.
     * This is equivalent to using <code>mxmlc/compc --compiler.show-deprecation-warnings</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void showDeprecationWarnings(boolean b)
    {
        args.put(COMPILER_SHOW_DEPRECATION_WARNINGS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }

    /**
     * Toggles whether warnings generated from unused type selectors are displayed.
     * This is equivalent to using <code>mxmlc/compc --compiler.show-unused-type-selector-warnings</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void showUnusedTypeSelectorWarnings(boolean b)
    {
        args.put(COMPILER_SHOW_UNUSED_TYPE_SELECTOR_WARNINGS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    

    @Override
    public void showMultipleDefinitionWarnings(boolean b)
    {
        args.put(COMPILER_SHOW_MULTIPLE_DEFINITION_WARNINGS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    

    /**
     * Sets a list of path elements that form the roots of ActionScript class hierarchies.
     * This is equivalent to using <code>mxmlc/compc --compiler.source-path</code>.
     * 
     * @param paths an array of <code>java.io.File</code> (<code>File.isDirectory()</code> must return <code>true</code>).
     */
    @Override
    public void setSourcePath(List<File> paths)
    {
        args.put(COMPILER_SOURCE_PATH, paths);
        more.remove(COMPILER_SOURCE_PATH);
        
        isConfigurationDirty = true;
    }

    /**
     * Adds a list of path elements to the existing source path list.
     * 
     * @param paths an array of <code>java.io.File</code> (<code>File.isDirectory()</code> must return <code>true</code>).
     * @see #setSourcePath
     */
    @Override
    public void addSourcePath(Collection<File> paths)
    {
        addFiles(COMPILER_SOURCE_PATH, paths);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Runs the ActionScript compiler in strict error checking mode.
     * This is equivalent to using <code>mxmlc/compc --compiler.strict</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void enableStrictChecking(boolean b)
    {
        args.put(COMPILER_STRICT, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of CSS or SWC files to apply as a theme.
     * This is equivalent to using <code>mxmlc/compc --compiler.theme</code>.
     * 
     * @param files an array of <code>java.io.File</code>
     */
    @Override
    public void setTheme(List<File> files)
    {
        args.put(COMPILER_THEME, files);
        more.remove(COMPILER_THEME);
        
        isConfigurationDirty = true;
    }

    /**
     * Adds a list of CSS or SWC files to the existing list of theme files.
     * 
     * @param files an array of <code>java.io.File</code>
     * @see #setTheme
     */
    @Override
    public void addTheme(List<File> files)
    {
        addFiles(COMPILER_THEME, files);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Determines whether resources bundles are included in the application.
     * This is equivalent to using <code>mxmlc/compc --compiler.use-resource-bundle-metadata</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    @Override
    public void useResourceBundleMetaData(boolean b)
    {
        args.put(COMPILER_USE_RESOURCE_BUNDLE_METADATA, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Generates bytecodes that include line numbers. When a run-time error occurs,
     * the stacktrace shows these line numbers. Enabling this option generates larger SWF files.
     * This is equivalent to using <code>mxmlc/compc --compiler.verbose-stacktraces</code>.
     * By default, it is set to <code>false</code>.
     * 
     * @param b boolean value
     */
    public void enableVerboseStacktraces(boolean b)
    {
        args.put(COMPILER_VERBOSE_STACKTRACES, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }

    /**
     * Enables the removal of RSLs associated with libraries
     * that are not used by an application.
     * This is equivalent to using the
     * <code>remove-unused-rsls</code> option of the mxmlc compiler.
     * 
     * <p>
     * The default value is <code>false</code>.
     * 
     * @param b Boolean value that enables or disables the removal.
     *    
     * @since 4.5
     */
    @Override
    public void removeUnusedRuntimeSharedLibraryPaths(boolean b)
    {
        args.put(REMOVE_UNUSED_RSLS, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Enables checking of ActionScript warnings. They are:
     * 
     * <pre>
     * --compiler.warn-array-tostring-changes
     * --compiler.warn-assignment-within-conditional
     * --compiler.warn-bad-array-cast
     * --compiler.warn-bad-bool-assignment
     * --compiler.warn-bad-date-cast
     * --compiler.warn-bad-es3-type-method
     * --compiler.warn-bad-es3-type-prop
     * --compiler.warn-bad-nan-comparison
     * --compiler.warn-bad-null-assignment
     * --compiler.warn-bad-null-comparison
     * --compiler.warn-bad-undefined-comparison
     * --compiler.warn-boolean-constructor-with-no-args
     * --compiler.warn-changes-in-resolve
     * --compiler.warn-class-is-sealed
     * --compiler.warn-const-not-initialized
     * --compiler.warn-constructor-returns-value
     * --compiler.warn-deprecated-event-handler-error
     * --compiler.warn-deprecated-function-error
     * --compiler.warn-deprecated-property-error
     * --compiler.warn-duplicate-argument-names
     * --compiler.warn-duplicate-variable-def
     * --compiler.warn-for-var-in-changes
     * --compiler.warn-import-hides-class
     * --compiler.warn-instance-of-changes
     * --compiler.warn-internal-error
     * --compiler.warn-level-not-supported
     * --compiler.warn-missing-namespace-decl
     * --compiler.warn-negative-uint-literal
     * --compiler.warn-no-constructor
     * --compiler.warn-no-explicit-super-call-in-constructor
     * --compiler.warn-no-type-decl
     * --compiler.warn-number-from-string-changes
     * --compiler.warn-scoping-change-in-this
     * --compiler.warn-slow-text-field-addition
     * --compiler.warn-unlikely-function-value
     * --compiler.warn-xml-class-has-changed
     * --compiler.warn-this-within-closure
     * </pre>
     * 
     * @param warningCode warning code
     * @param b boolean value
     * @see #WARN_ARRAY_TO_STRING_CHANGES
     * @see #WARN_ASSIGNMENT_WITHIN_CONDITIONAL
     * @see #WARN_BAD_ARRAY_CAST
     * @see #WARN_BAD_BOOLEAN_ASSIGNMENT
     * @see #WARN_BAD_DATE_CAST
     * @see #WARN_BAD_ES3_TYPE_METHOD
     * @see #WARN_BAD_ES3_TYPE_PROP
     * @see #WARN_BAD_NAN_COMPARISON
     * @see #WARN_BAD_NULL_ASSIGNMENT
     * @see #WARN_BAD_NULL_COMPARISON
     * @see #WARN_BAD_UNDEFINED_COMPARISON
     * @see #WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS
     * @see #WARN_CHANGES_IN_RESOLVE
     * @see #WARN_CLASS_IS_SEALED
     * @see #WARN_CONST_NOT_INITIALIZED
     * @see #WARN_CONSTRUCTOR_RETURNS_VALUE
     * @see #WARN_DEPRECATED_EVENT_HANDLER_ERROR
     * @see #WARN_DEPRECATED_FUNCTION_ERROR
     * @see #WARN_DEPRECATED_PROPERTY_ERROR
     * @see #WARN_DUPLICATE_ARGUMENT_NAMES
     * @see #WARN_DUPLICATE_VARIABLE_DEF
     * @see #WARN_FOR_VAR_IN_CHANGES
     * @see #WARN_IMPORT_HIDES_CLASS
     * @see #WARN_INSTANCEOF_CHANGES
     * @see #WARN_INTERNAL_ERROR
     * @see #WARN_LEVEL_NOT_SUPPORTED
     * @see #WARN_MISSING_NAMESPACE_DECL
     * @see #WARN_NEGATIVE_UINT_LITERAL
     * @see #WARN_NO_CONSTRUCTOR
     * @see #WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR
     * @see #WARN_NO_TYPE_DECL
     * @see #WARN_NUMBER_FROM_STRING_CHANGES
     * @see #WARN_SCOPING_CHANGE_IN_THIS
     * @see #WARN_SLOW_TEXTFIELD_ADDITION
     * @see #WARN_UNLIKELY_FUNCTION_VALUE
     * @see #WARN_XML_CLASS_HAS_CHANGED
     * @see #WARN_THIS_WITHIN_CLOSURE
     */
    @Override
    public void checkActionScriptWarning(int warningCode, boolean b)
    {
        String key = null;
        
        switch (warningCode)
        {
        case WARN_ARRAY_TO_STRING_CHANGES:
            key = COMPILER_WARN_ARRAY_TOSTRING_CHANGES;
            break;
        case WARN_ASSIGNMENT_WITHIN_CONDITIONAL:
            key = COMPILER_WARN_ASSIGNMENT_WITHIN_CONDITIONAL;
            break;
        case WARN_BAD_ARRAY_CAST:
            key = COMPILER_WARN_BAD_ARRAY_CAST;
            break;
        case WARN_BAD_BOOLEAN_ASSIGNMENT:
            key = COMPILER_WARN_BAD_BOOL_ASSIGNMENT;
            break;
        case WARN_BAD_DATE_CAST:
            key = COMPILER_WARN_BAD_DATE_CAST;
            break;
        case WARN_BAD_ES3_TYPE_METHOD:
            key = COMPILER_WARN_BAD_ES3_TYPE_METHOD;
            break;
        case WARN_BAD_ES3_TYPE_PROP:
            key = COMPILER_WARN_BAD_ES3_TYPE_PROP;
            break;
        case WARN_BAD_NAN_COMPARISON:
            key = COMPILER_WARN_BAD_NAN_COMPARISON;
            break;
        case WARN_BAD_NULL_ASSIGNMENT:
            key = COMPILER_WARN_BAD_NULL_ASSIGNMENT;
            break;
        case WARN_BAD_NULL_COMPARISON:
            key = COMPILER_WARN_BAD_NULL_COMPARISON;
            break;
        case WARN_BAD_UNDEFINED_COMPARISON:
            key = COMPILER_WARN_BAD_UNDEFINED_COMPARISON;
            break;
        case WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS:
            key = COMPILER_WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS;
            break;
        case WARN_CHANGES_IN_RESOLVE:
            key = COMPILER_WARN_CHANGES_IN_RESOLVE;
            break;
        case WARN_CLASS_IS_SEALED:
            key = COMPILER_WARN_CLASS_IS_SEALED;
            break;
        case WARN_CONST_NOT_INITIALIZED:
            key = COMPILER_WARN_CONST_NOT_INITIALIZED;
            break;
        case WARN_CONSTRUCTOR_RETURNS_VALUE:
            key = COMPILER_WARN_CONSTRUCTOR_RETURNS_VALUE;
            break;
        case WARN_DEPRECATED_EVENT_HANDLER_ERROR:
            key = COMPILER_WARN_DEPRECATED_EVENT_HANDLER_ERROR;
            break;
        case WARN_DEPRECATED_FUNCTION_ERROR:
            key = COMPILER_WARN_DEPRECATED_FUNCTION_ERROR;
            break;
        case WARN_DEPRECATED_PROPERTY_ERROR:
            key = COMPILER_WARN_DEPRECATED_PROPERTY_ERROR;
            break;
        case WARN_DUPLICATE_ARGUMENT_NAMES:
            key = COMPILER_WARN_DUPLICATE_ARGUMENT_NAMES;
            break;
        case WARN_DUPLICATE_VARIABLE_DEF:
            key = COMPILER_WARN_DUPLICATE_VARIABLE_DEF;
            break;
        case WARN_FOR_VAR_IN_CHANGES:
            key = COMPILER_WARN_FOR_VAR_IN_CHANGES;
            break;
        case WARN_IMPORT_HIDES_CLASS:
            key = COMPILER_WARN_IMPORT_HIDES_CLASS;
            break;
        case WARN_INSTANCEOF_CHANGES:
            key = COMPILER_WARN_INSTANCE_OF_CHANGES;
            break;
        case WARN_INTERNAL_ERROR:
            key = COMPILER_WARN_INTERNAL_ERROR;
            break;
        case WARN_LEVEL_NOT_SUPPORTED:
            key = COMPILER_WARN_LEVEL_NOT_SUPPORTED;
            break;
        case WARN_MISSING_NAMESPACE_DECL:
            key = COMPILER_WARN_MISSING_NAMESPACE_DECL;
            break;
        case WARN_NEGATIVE_UINT_LITERAL:
            key = COMPILER_WARN_NEGATIVE_UINT_LITERAL;
            break;
        case WARN_NO_CONSTRUCTOR:
            key = COMPILER_WARN_NO_CONSTRUCTOR;
            break;
        case WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR:
            key = COMPILER_WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR;
            break;
        case WARN_NO_TYPE_DECL:
            key = COMPILER_WARN_NO_TYPE_DECL;
            break;
        case WARN_NUMBER_FROM_STRING_CHANGES:
            key = COMPILER_WARN_NUMBER_FROM_STRING_CHANGES;
            break;
        case WARN_SCOPING_CHANGE_IN_THIS:
            key = COMPILER_WARN_SCOPING_CHANGE_IN_THIS;
            break;
        case WARN_SLOW_TEXTFIELD_ADDITION:
            key = COMPILER_WARN_SLOW_TEXT_FIELD_ADDITION;
            break;
        case WARN_UNLIKELY_FUNCTION_VALUE:
            key = COMPILER_WARN_UNLIKELY_FUNCTION_VALUE;
            break;
        case WARN_XML_CLASS_HAS_CHANGED:
            key = COMPILER_WARN_XML_CLASS_HAS_CHANGED;
            break;
        case WARN_THIS_WITHIN_CLOSURE:
            key = COMPILER_WARN_THIS_WITHIN_CLOSURE;
            break;
        }
        
        if (key != null)
        {
            args.put(key, b ? Boolean.TRUE : Boolean.FALSE);
            
            isConfigurationDirty = true;
        }
    }
    
    /**
     * Sets the default background color (may be overridden by the application code).
     * This is equivalent to using <code>mxmlc/compc --default-background-color</code>.
     * The default value is <code>0x869CA7</code>.
     * 
     * @param color RGB value
     */
    public void setDefaultBackgroundColor(int color)
    {
        args.put(DEFAULT_BACKGROUND_COLOR, new Integer(color));
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets the default frame rate to be used in the application.
     * This is equivalent to using <code>mxmlc/compc --default-frame-rate</code>.
     * The default value is <code>24</code>.
     * 
     * @param rate frames per second
     */
    public void setDefaultFrameRate(int rate)
    {
        args.put(DEFAULT_FRAME_RATE, new Integer(rate));
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets the default script execution limits (may be overridden by root attributes).
     * This is equivalent to using <code>mxmlc/compc --default-script-limits</code>.
     * The default maximum recursion depth is <code>1000</code>.
     * The default maximum execution time is <code>60</code>.
     * 
     * @param maxRecursionDepth recursion depth
     * @param maxExecutionTime execution time in seconds. 
     */
    public void setDefaultScriptLimits(int maxRecursionDepth, int maxExecutionTime)
    {
        args.put(DEFAULT_SCRIPT_LIMITS, new int[] { maxRecursionDepth, maxExecutionTime });
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets the default window size.
     * This is equivalent to using <code>mxmlc/compc --default-size</code>.
     * The default width is <code>500</code>.
     * The default height is <code>375</code>.
     * 
     * @param width width in pixels
     * @param height height in pixels
     */
    public void setDefaultSize(int width, int height)
    {
        args.put(DEFAULT_SIZE, new int[] { width, height });
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of definitions to omit from linking when building an application.
     * This is equivalent to using <code>mxmlc/compc --externs</code>.
     * 
     * @param definitions An array of definitions (e.g. classes, functions, variables, namespaces, etc.)
     */
    @Override
    public void setExterns(Collection<String> definitions)
    {
        args.put(EXTERNS, definitions);
        more.remove(EXTERNS);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Adds a list of definitions to the existing list of definitions.
     *
     * @see #setExterns
     * @param definitions an array of definitions (e.g. classes, functions, variables, namespaces, etc.)
     */
    public void addExterns(Collection<String> definitions)
    {
        addStrings(EXTERNS, definitions);
        
        isConfigurationDirty = true;
    }

    /**
     * Loads a file containing configuration options. The file format follows the format of <code>royale-config.xml</code>.
     * This is equivalent to using <code>mxmlc/compc --load-config</code>.
     * 
     * @param file an instance of <code>java.io.File</code>
     */
    public void setConfiguration(File file)
    {
        args.put(LOAD_CONFIG, file);
        more.remove(LOAD_CONFIG);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Adds a file to the existing list of configuration files.
     * 
     * @see #setConfiguration(File)
     * @param file a configuration file
     */
    public void addConfiguration(File file)
    {
        addFiles(LOAD_CONFIG, Collections.singleton(file));
        
        isConfigurationDirty = true;
    }

    /**
     * Sets the configuration parameters. The input should be valid <code>mxmlc/compc</code> command-line arguments.<p>
     * 
     * @param args <code>mxmlc/compc</code> command-line arguments
     * @param defaultVariable the default variable of the configuration.
     */
    public void setConfiguration(String[] args, String defaultVariable)
    {
        setConfiguration(args, defaultVariable, true);
    }
    
    /**
     * Sets the configuration parameters. The input should be valid
     * <code>mxmlc/compc</code> command-line arguments.
     * <p>
     * 
     * @param args <code>mxmlc/compc</code> command-line arguments
     * @param defaultVariable the default variable of the configuration.
     * @param argsRequireDefaultVariable true if the default variable must be
     * set in order to parse the args, false otherwise.
     */
    public void setConfiguration(String[] args, String defaultVariable, 
            boolean argsRequireDefaultVariable)
    {
        extras = args;
        extrasRequireDefaultVariable = argsRequireDefaultVariable;
        configurationDefaultVariable = defaultVariable;
        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of definitions to omit from linking when building an application.
     * This is equivalent to using <code>mxmlc/compc --load-externs</code>.
     * This option is similar to <code>setExterns(String[])</code>. The following is an example of
     * the file format:
     * 
     * <pre>
     * &lt;script>
     *     &lt;!-- use 'dep', 'pre' or 'dep' to specify a definition to be omitted from linking. -->
     *     &lt;def id="org.apache.royale.core:UIBase"/>
     *     &lt;pre id="org.apache.royale.core:IUIBase"/>
     *     &lt;dep id="String"/>
     * &lt;/script>
     * </pre>
     * 
     * @param files an array of <code>java.io.File</code>
     */
    public void setLoadExterns(Collection<File> files)
    {
        args.put(LOAD_EXTERNS, files);
        more.remove(LOAD_EXTERNS);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Adds a list of files to the existing list of definitions to be omitted from linking.
     * 
     * @see #setLoadExterns
     * @see #setExterns
     * @param files an array of <code>java.io.File</code>.
     */
    public void addLoadExterns(Collection<File> files)
    {
        addFiles(LOAD_EXTERNS, files);
        
        isConfigurationDirty = true;
    }

    /**
     * Sets a SWF frame label with a sequence of classnames that will be linked onto the frame.
     * This is equivalent to using <code>mxmlc/compc --frames.frame</code>.
     * 
     * @param label A string
     * @param classNames a collection of class names
     */
    public void setFrameLabel(String label, Collection<String> classNames)
    {
        if (!args.containsKey(FRAMES_FRAME))
        {
            args.put(FRAMES_FRAME, new TreeMap<String, Collection<String>>());
        }
        
        // I am ONLY doing this because we set it three lines above
        @SuppressWarnings("unchecked")
        Map<String, Collection<String>> map = (Map<String, Collection<String>>) args.get(FRAMES_FRAME);
        map.put(label, classNames);

        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of definitions to always link in when building an application.
     * This is equivalent to using <code>mxmlc/compc --includes</code>.
     * 
     * @param definitions an array of definitions (e.g. classes, functions, variables, namespaces, etc).
     */
    public void setIncludes(Collection<String> definitions)
    {
        args.put(INCLUDES, definitions);
        more.remove(INCLUDES);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Adds a list of definitions to the existing list of definitions.
     *
     * @see #setIncludes
     * @param definitions an array of definitions (e.g. classes, functions, variables, namespaces, etc.)
     */
    public void addIncludes(Collection<String> definitions)
    {
        addStrings(INCLUDES, definitions);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Specifies the licenses that the compiler has to validate before compiling.
     * This is equivalent to using <code>mxmlc/compc --licenses.license</code>
     * 
     * @param productName a string
     * @param serialNumber a serial number
     */
    public void setLicense(String productName, String serialNumber)
    {
        if (!args.containsKey(LICENSES_LICENSE))
        {
            args.put(LICENSES_LICENSE, new TreeMap<String, String>());
        }
        
        // I am ONLY doing this because we set it three lines above
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) args.get(LICENSES_LICENSE);
        map.put(productName, serialNumber);     
        
        isConfigurationDirty = true;
    }

    /**
     * Sets the metadata section of the application SWF. This option is equivalent to using the following <code>mxmlc/compc</code>
     * command-line options:
     * 
     * <pre>
     * --metadata.contributor
     * --metadata.creator
     * --metadata.date
     * --metadata.description
     * --metadata.language
     * --metadata.localized-description
     * --metadata.localized-title
     * --metadata.publisher
     * --metadata.title
     * </pre>
     * 
     * The valid fields and types of value are specified below:
     * 
     * <pre>
     * CONTRIBUTOR      java.lang.String
     * CREATOR          java.lang.String
     * DATE             java.util.Date
     * DESCRIPTION      java.util.Map<String, String>
     * TITLE            java.util.Map<String, String>
     * LANGUAGE         java.lang.String
     * PUBLISHER        java.lang.String
     * </pre>
     * 
     * For example,
     * 
     * <pre>
     * Map titles = new HashMap();
     * titles.put("EN", "Apache Royale 1.0.0 Application");
     * 
     * Map descriptions = new HashMap();
     * descriptions.put("EN", "http://royale.apache.org");
     * 
     * setSWFMetaData(Configuration.LANGUAGE, "EN");
     * setSWFMetaData(Configuration.TITLE, titles);
     * setSWFMetaData(Configuration.DESCRIPTION, descriptions);
     * </pre>
     * 
     * @param field CONTRIBUTOR, CREATOR, DATE, DESCRIPTION, TITLE, LANGUAGE, PUBLISHER
     * @param value String, Date or Map
     * @see #CONTRIBUTOR
     * @see #CREATOR
     * @see #DATE
     * @see #DESCRIPTION
     * @see #TITLE
     * @see #LANGUAGE
     * @see #PUBLISHER
     */
    @Override
    public void setSWFMetadata(int field, Object value)
    {
        switch (field)
        {
        case CONTRIBUTOR:
            args.put(METADATA_CONTRIBUTOR, value);
            break;
        case CREATOR:
            args.put(METADATA_CREATOR, value);
            break;
        case DATE:
            args.put(METADATA_DATE, value);
            break;
        case DESCRIPTION:
            args.put(METADATA_LOCALIZED_DESCRIPTION, value);
            break;
        case TITLE:
            args.put(METADATA_LOCALIZED_TITLE, value);
            break;
        case LANGUAGE:
            args.put(METADATA_LANGUAGE, value);
            break;
        case PUBLISHER:
            args.put(METADATA_PUBLISHER, value);
            break;
        }

        args.remove(RAW_METADATA);
        
        isConfigurationDirty = true;
    }

    /**
     * Sets the metadata section of the application SWF.
     * This is equivalent to using <code>mxmlc/compc --raw-metadata</code>.
     * This option overrides everything set by the <code>setSWFMetaData</code> method.
     * 
     * @see #setSWFMetadata(int, Object)
     * @param xml a well-formed XML fragment
     */
    @Override
    public void setSWFMetadata(String xml)
    {
        args.put(RAW_METADATA, xml);
        
        args.remove(METADATA_CONTRIBUTOR);
        args.remove(METADATA_CREATOR);
        args.remove(METADATA_DATE);
        args.remove(METADATA_LOCALIZED_DESCRIPTION);
        args.remove(METADATA_LOCALIZED_TITLE);
        args.remove(METADATA_LANGUAGE);
        args.remove(METADATA_PUBLISHER);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Sets a list of runtime shared library URLs to be loaded before the application starts.
     * This is equivalent to using <code>mxmlc/compc --runtime-shared-libraries</code>.
     * 
     * @param libraries an array of <code>java.lang.String</code>.
     */
    @Override
    public void setRuntimeSharedLibraries(List<String> libraries)
    {
        args.put(RUNTIME_SHARED_LIBRARIES, libraries);
        more.remove(RUNTIME_SHARED_LIBRARIES);
        
        isConfigurationDirty = true;
    }

    /**
     * Adds a list of runtime shared library URLs to the existing list.
     * 
     * @see #setRuntimeSharedLibraries
     * @param libraries an array of <code>java.lang.String</code>
     */
    public void addRuntimeSharedLibraries(List<String> libraries)
    {
        addStrings(RUNTIME_SHARED_LIBRARIES, libraries);
        
        isConfigurationDirty = true;
    }

    /**
     * Toggles whether the application SWF is flagged for access to network resources.
     * This is equivalent to using <code>mxmlc/compc --use-network</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    public void useNetwork(boolean b)
    {
        args.put(USE_NETWORK, b ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    /**
     * Defines a token. mxmlc and compc support token substitutions. For example,
     * 
     * <pre>
     * mxmlc +royalelib=path1 +foo=bar --var=${foo}
     * </pre>
     * 
     * <code>var=bar</code> after the substitution of <code>${foo}</code>.
     * 
     * @param name The name of the token.
     * @param value The value of the token.
     */
    @Override
    public void setToken(String name, String value)
    {
        tokens.put(name, value);
        
        isConfigurationDirty = true;
    }

    /**
     * Set the class name of the main definition
     * 
     * @param name of the main definition
     */
    public void setMainDefinition(String name)
    {
        mainDefinition = name;
        
        isConfigurationDirty = true;
    }

    /**
     * 
     * @param key
     * @param files
     */
    private void addFiles(String key, Collection<File> files)
    {
        addFiles(more, key, files);
    }

    /**
     * 
     * @param more
     * @param key
     * @param files
     */
    @SuppressWarnings("unchecked")
    private void addFiles(Map<String, Object> more, String key, Collection<File> files)
    {
        Collection<File> existing = null;
        
        if (more.containsKey(key))
        {
            Object obj = more.get(key);
            existing = (Collection<File>) obj;
        }
        
        if (existing != null)
        {
            existing.addAll(files);
            files = existing;
        }
        
        more.put(key, files);
    }
    
    /**
     * 
     * @param key
     * @param strings
     */
    private void addStrings(String key, Collection<String> strings)
    {
        addStrings(more, key, strings);
    }
    
    /**
     * 
     * @param more
     * @param key
     * @param strings
     */
    @SuppressWarnings("unchecked")
    private void addStrings(Map<String, Object> more, String key, Collection<String> strings)
    {
        Collection<String> existing = null;
        
        if (more.containsKey(key))
        {
            existing = (Collection<String>) more.get(key);
        }
        
        if (existing != null)
        {
            existing.addAll(strings);
            strings = existing;
        }
        
        more.put(key, strings);
    }

    /**
     * 
     */
    @Override
    public String toString()
    {
        String[] options;
        try
        {
            options = getOptions(args, more, processExtras(extras));
        }
        catch (ConfigurationException e)
        {
            options = new String[0];
        }
        
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < options.length; i++)
        {
            b.append(options[i]);
            b.append(' ');
        }
        return b.toString();
    }
    
    private String toCommaSeparatedString(Collection<?> values)
    {
        StringBuilder b = new StringBuilder();
        int length = values.size();
        int i = 0;
        for (Object value : values)
        {
            String valueString = null;
            
            if (value instanceof String)
            {
                valueString = (String)value;
            }
            else if (value instanceof File)
            {
                valueString = ((File)value).getPath();
            }
            
            if (valueString != null)
                b.append(valueString);
            
            if (i++ < length - 1)
            {
                b.append(COMMA_STRING);
            }
        }
        return b.toString();
    }

    @Override
    public void setTargetPlayer(int major, int minor, int revision)
    {
        args.put(TARGET_PLAYER, major + "." + minor + "." + revision);
        
        isConfigurationDirty = true;
    }

    @Override
    public void setCompatibilityVersion(int major, int minor, int revision)
    {
        if (!(major == 0 && minor == 0 && revision == 0))
        {
            args.put(COMPILER_MXML_COMPATIBILITY, major + "." + minor + "." + revision);
        }
        
        isConfigurationDirty = true;
    }

    @Override
    public void enableDigestVerification(boolean verify)
    {
        args.put(VERIFY_DIGESTS, verify ? Boolean.TRUE : Boolean.FALSE);    
        
        isConfigurationDirty = true;
    }

    @Override
    public void setDefineDirectives(Map<String, String> defines)
    {
        if (defines == null)
        {
            args.remove(COMPILER_DEFINE);
        }
        else
        {
            final CompilerDefinitionMap defs = new CompilerDefinitionMap();
            defs.putAll(defines);
            args.put(COMPILER_DEFINE, defs);
        }
        
        isConfigurationDirty = true;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getExtensions() {
        if( !args.containsKey( COMPILER_EXTENSIONS ) ) {
            args.put( COMPILER_EXTENSIONS, new LinkedHashMap<String, List<String>>() );
        }
        return (Map<String, List<String>>) args.get( COMPILER_EXTENSIONS );
    }

    public void addExtensionLibraries( File extension, List<String> parameter )
    {
        getExtensions().put( extension.getAbsolutePath(), parameter );
        
        isConfigurationDirty = true;
    }

    @Override
    public void setExtensionLibraries( Map<File, List<String>> extensions)
    {
        getExtensions().clear();
        Set<File> keys = extensions.keySet();
        for ( File key : keys )
        {
            addExtensionLibraries( key, extensions.get( key ) );
        }
        
        isConfigurationDirty = true;
    }
    
    @Override
    public Configurator clone()
    {
        Configurator cloneConfig;
        
        try
        {
            cloneConfig = (Configurator) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException(e);//wont happen
        }

        cloneConfig.args = new LinkedHashMap<String, Object>(args);
        cloneConfig.more = new LinkedHashMap<String, Object>(more);
        cloneConfig.tokens = new LinkedHashMap<String, String>(tokens);
        cloneConfig.configurationClass = configurationClass;
        cloneConfig.keepLinkReport = keepLinkReport;
        cloneConfig.keepSizeReport = keepSizeReport;
        cloneConfig.keepConfigurationReport = keepConfigurationReport;
        cloneConfig.reportMissingLibraries = reportMissingLibraries;
        cloneConfig.warnOnRoyaleOnlyOptionUsage = warnOnRoyaleOnlyOptionUsage;
        cloneConfig.mainDefinition = mainDefinition;
        cloneConfig.isConfigurationDirty = true;
        
        if (extras != null)
        {
            cloneConfig.extras = new String[extras.length];
            System.arraycopy(extras, 0, cloneConfig.extras, 0, extras.length);
        }

        return cloneConfig;
    }
    
    @Override
    public void setRuntimeSharedLibraryPath(List<RSLSettings> rslSettings)
    {
        args.put(RUNTIME_SHARED_LIBRARY_PATH, 
                rslSettings != null ? new RSLSettingsList(rslSettings) : null);
        more.remove(RUNTIME_SHARED_LIBRARY_PATH);
        
        isConfigurationDirty = true;
    }

    @Override
    public void addRuntimeSharedLibraryPath(List<RSLSettings> rslSettings)
    {
        RSLSettingsList currentSettings = (RSLSettingsList)more.get(RUNTIME_SHARED_LIBRARY_PATH);
        if (currentSettings == null)
            currentSettings = new RSLSettingsList(rslSettings.size());
        
        currentSettings.addAll(rslSettings);
        
        isConfigurationDirty = true;
    }

    @Override
    public void setLocales(Collection<String> locales)
    {
        args.put(COMPILER_LOCALE, locales);
        more.remove(COMPILER_LOCALE);
        
        isConfigurationDirty = true;
    }

    @Override
    public void addLocales(Collection<String> locales)
    {
        addStrings(COMPILER_LOCALE, locales);
        
        isConfigurationDirty = true;
    }

    @Override
    public void setOutput(File output)
    {
        args.put(OUTPUT, output);
        
        isConfigurationDirty = true;
    }

    // 
    // Library Settings
    //
    
    @Override
    public void setIncludeClasses(Collection<String> classes)
    {
        args.put(INCLUDE_CLASSES, classes);                
        
        isConfigurationDirty = true;
    }

    @Override
    public void setIncludeFiles(Map<String, File> files)
    {
        args.put(INCLUDE_FILE, files);                
        
        isConfigurationDirty = true;
    }

    @Override
    public void setIncludeNamespaces(Collection<String> namespaces)
    {
        args.put(INCLUDE_NAMESPACES, namespaces);        
        
        isConfigurationDirty = true;
    }

    @Override
    public void setIncludeSources(Collection<File> sources)
    {
        args.put(INCLUDE_SOURCES, sources);                
        
        isConfigurationDirty = true;
    }

    @Override
    public void setIncludeStyleSheet(Map<String, File> styleSheets)
    {
        args.put(INCLUDE_STYLESHEET, styleSheets);                        
        
        isConfigurationDirty = true;
    }

    @Override
    public void enableIncludeLookupOnly(boolean include)
    {
        args.put(INCLUDE_LOOKUP_ONLY, include ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;
    }
    
    @Override
    public void setRoyale(boolean value)
    {
        args.put(ROYALE, value ? Boolean.TRUE : Boolean.FALSE);
        
        isConfigurationDirty = true;        
    }

    @Override
    public void setExcludeNativeJSLibraries(boolean value)
    {
        args.put(EXCLUDE_NATIVE_JS_LIBRARIES, value ? Boolean.TRUE : Boolean.TRUE);
    }

    //
    // End of Configuration settings.
    //

    
}
