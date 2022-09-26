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

package org.apache.royale.linter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.config.localization.ResourceBundleLocalizer;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.linter.internal.config.FileConfigurator;
import org.apache.royale.linter.internal.config.SystemPropertyConfigurator;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.Trace;

/**
 * A class that allows a client change compiler settings and to 
 * configure projects and targets from those settings.
 */
public class Configurator implements Cloneable
{
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

    // Used to generate the command line
    private static final String EQUALS_STRING = "=";
    private static final String PLUS_EQUALS_STRING = "+=";
    private static final String COMMA_STRING =  ",";
    private static final String PLUS_STRING =  "+";
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
        
        isConfigurationDirty = true;
        configurationDefaultVariable = ILinterSettingsConstants.FILES; // the default variable of the configuration.
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
    private List<String> loadedConfigFiles;
    private List<String> missingConfigFiles;
    
    private Map<String, String> tokens;
    
    private boolean isConfigurationDirty;
    private boolean configurationSuccess;
    protected Collection<ICompilerProblem> configurationProblems;
    private IPathResolver configurationPathResolver;

    // 
    // IConfigurator related methods
    //
    
    public List<String> getLoadedConfigurationFiles()
    {
        return loadedConfigFiles != null ? loadedConfigFiles :
            Collections.<String>emptyList();
    }
    
    public List<String> getMissingConfigurationFiles()
    {
        return missingConfigFiles != null ? missingConfigFiles :
            Collections.<String>emptyList();
    }

    public Collection<ICompilerProblem> validateConfiguration(String[] args)
    {
        if (args == null)
            throw new NullPointerException("args may not be null");
        
        List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        ConfigurationBuffer configurationBuffer = createConfigurationBuffer(configurationClass);
        
        try
        {
            CommandLineConfigurator.parse(configurationBuffer, null, args);
        }
        catch (ConfigurationException e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(e);
            problems.add(problem);
        }
        
        return problems;
    }
    
    public Collection<ICompilerProblem> getConfigurationProblems()
    {
        assert configuration != null : 
            "Get the configuration problems after calling getConfiguration()";
        
        List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(configurationProblems.size() +
                                            configuration.getConfigurationProblems().size());
        problems.addAll(configurationProblems);
        problems.addAll(configuration.getConfigurationProblems()); 
        return problems;
    }

    public void setConfigurationPathResolver(IPathResolver pathResolver)
    {
        if (pathResolver == null)
            throw new NullPointerException("pathResolver may not be null");
        
        this.configurationPathResolver = pathResolver;
    }

    public Configuration getConfiguration()
    {
        processConfiguration();
        return configuration;
    }
    
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
            
            if (!loadLocalConfig())
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
     * Override default values.
     */
    protected void overrideDefaults() throws ConfigurationException
    {
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
     * Load project specific configuration. The configuration XML file is at the
     * project root with naming convention of aslint-config.xml.
     * 
     * @return true if successful, false otherwise.
     */
    protected boolean loadLocalConfig()
    {
        boolean success = true;
        
        String project = "aslint-config.xml";
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

	protected String[] processExtras(String[] extraOptions) throws ConfigurationException
    {
		return extraOptions;
	}

    /**
     * Sets the configuration parameters. The input should be valid <code>mxmlc/compc</code> command-line arguments.<p>
     * 
     * @param args <code>mxmlc/compc</code> command-line arguments
     * @param defaultVariable the default variable of the configuration.
     */
    public void setConfiguration(String[] args, String defaultVariable)
    {
        extras = args;
        configurationDefaultVariable = defaultVariable;
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
    public void setToken(String name, String value)
    {
        tokens.put(name, value);
        
        isConfigurationDirty = true;
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
        cloneConfig.isConfigurationDirty = true;
        
        if (extras != null)
        {
            cloneConfig.extras = new String[extras.length];
            System.arraycopy(extras, 0, cloneConfig.extras, 0, extras.length);
        }

        return cloneConfig;
    }
}
