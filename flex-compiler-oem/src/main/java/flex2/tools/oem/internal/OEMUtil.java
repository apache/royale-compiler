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

package flex2.tools.oem.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.ConfigurationPathResolver;
import flex2.compiler.common.DefaultsConfigurator;
import flex2.compiler.config.CommandLineConfigurator;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.config.FileConfigurator;
import flex2.compiler.config.SystemPropertyConfigurator;
import flex2.compiler.util.CompilerControl;
import flex2.compiler.util.MimeMappings;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.CommandLineConfiguration;
import flex2.tools.CompcConfiguration;
import flex2.tools.ToolsConfiguration;
import flex2.tools.oem.*;

import org.apache.royale.compiler.clients.COMPJSC;
import org.apache.royale.compiler.clients.MXMLJSC;

/**
 * A collection of utility methods used by classes in flex2.tools.oem.
 *
 * @version 2.0.1
 * @author Clement Wong
 */
public class OEMUtil
{
	/**
	 * 
    public static final LocalizationManager setupLocalizationManager()
    {
        LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

        if (l10n == null)
        {
            // set up for localizing messages
            l10n = new LocalizationManager();
            l10n.addLocalizer(new ResourceBundleLocalizer());
            ThreadLocalToolkit.setLocalizationManager(l10n);
        }

        return l10n;
    }
     */

	/**
	 * 
	 * @param logger
	 * @param mimeMappings
	 */
	public static final void init(Logger logger,
								  MimeMappings mimeMappings,
								  ProgressMeter meter,
								  PathResolver resolver,
								  CompilerControl cc)
	{
        //CompilerAPI.useAS3();
        //CompilerAPI.usePathResolver(resolver != null ? new OEMPathResolver(resolver) : null);
        //setupLocalizationManager();
        ThreadLocalToolkit.setLogger(new OEMLogAdapter(logger));
        ThreadLocalToolkit.setMimeMappings(mimeMappings);
        ThreadLocalToolkit.setProgressMeter(meter);
        ThreadLocalToolkit.setCompilerControl(cc);
	}

	/**
	 * 
	 *
	 */
	public static final void clean()
	{
		//CompilerAPI.removePathResolver();
		ThreadLocalToolkit.setLogger(null);
		//ThreadLocalToolkit.setLocalizationManager(null);
		ThreadLocalToolkit.setMimeMappings(null);
		ThreadLocalToolkit.setProgressMeter(null);
		ThreadLocalToolkit.setCompilerControl(null);
        ThreadLocalToolkit.setCompatibilityVersion(null);
	}


	/**
	 * 
	 * @param args
	 * @param logger
	 * @param mimeMappings
	 * @return
	 */
	public static final OEMConfiguration getApplicationConfiguration(String[] args, boolean keepLinkReport, 
			                                                         boolean keepSizeReport, Logger logger, PathResolver resolver,
																	 MimeMappings mimeMappings)
	{
		return getApplicationConfiguration(args, keepLinkReport, keepSizeReport, logger, resolver, mimeMappings, true);
	}

	/**
	 * 
	 * @param args
	 * @param logger
	 * @param mimeMappings
	 * @param processDefaults
	 * @return
	 */
	public static final OEMConfiguration getApplicationConfiguration(String[] args, boolean keepLinkReport, boolean keepSizeReport, 
			                                                         Logger logger, PathResolver resolver, MimeMappings mimeMappings,
																	 boolean processDefaults)
	{
		if (!processDefaults)
		{
			return new OEMConfiguration(null, null);
		}
		
		// expect args to have --file-specs because we need it to find app-specific-config.xml.
		OEMUtil.init(logger, mimeMappings, null, resolver, null);
		
        try
		{
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(ApplicationCompilerConfiguration.class,
            													 ApplicationCompilerConfiguration.getAliases());
            cfgbuf.setDefaultVar("--file-specs" /*Mxmlc.FILE_SPECS*/);            
            DefaultsConfigurator.loadDefaults(cfgbuf);
            MXMLJSC mxmlc = new MXMLJSC();
            mxmlc.configure(args);
            ApplicationCompilerConfiguration configuration = processMXMLCConfiguration(mxmlc.config);
            
            configuration.keepLinkReport(keepLinkReport);
            configuration.keepSizeReport(keepSizeReport);
            
            return new OEMConfiguration(cfgbuf, configuration);
		}
		catch (ConfigurationException ex)
		{
			//Mxmlc.processConfigurationException(ex, "oem");
			return null;
		}
		/*
		catch (IOException ex)
		{
			ThreadLocalToolkit.logError(ex.getMessage());
			return null;
		}
		*/
		catch (RuntimeException ex)
		{
			Class c;
			try
			{
				c = Class.forName("flex.messaging.config.ConfigurationException");
				if (c.isInstance(ex))
				{
					ThreadLocalToolkit.logError(ex.getMessage());
				}
			}
			catch (ClassNotFoundException ex2)
			{
				
			}
			return null;
		}
	}

	private static ApplicationCompilerConfiguration processMXMLCConfiguration(org.apache.royale.compiler.config.Configuration config)
	{
	    ApplicationCompilerConfiguration acc = new ApplicationCompilerConfiguration();
        ConfigurationPathResolver resolver = new ConfigurationPathResolver(); 
	    acc.setConfigPathResolver(resolver);
	    acc.setBackgroundColor(config.getDefaultBackgroundColor());
	    acc.setDebug(config.debug());
	    acc.setFrameRate(config.getDefaultFrameRate());
	    acc.setHeight(Integer.toString(config.getDefaultHeight()));
        acc.setWidth(Integer.toString(config.getDefaultWidth()));
        acc.setSwfVersion(config.getSwfVersion());
        acc.setScriptRecursionLimit(config.getScriptRecursionLimit());
        acc.setScriptTimeLimit(config.getScriptTimeLimit());
        CompilerConfiguration cc = acc.getCompilerConfiguration();
        cc.setAccessible(config.getCompilerAccessible());
        List<String> externalLibraries = config.getCompilerExternalLibraryPath();
        String[] extlibs = new String[externalLibraries.size()];
        externalLibraries.toArray(extlibs);
        try
        {
            cc.cfgExternalLibraryPath(null, extlibs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> libraries = config.getCompilerLibraryPath();
        String[] libs = new String[libraries.size()];
        libraries.toArray(libs);
        try
        {
            cc.cfgLibraryPath(null, libs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> sources = config.getCompilerSourcePath();
        String[] srcs = new String[sources.size()];
        sources.toArray(srcs);
        try
        {
            cc.cfgSourcePath(null, srcs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    return acc;
	}
	
    private static LibraryCompilerConfiguration processCOMPCCConfiguration(org.apache.royale.compiler.config.Configuration config)
    {
        LibraryCompilerConfiguration acc = new LibraryCompilerConfiguration();
        ConfigurationPathResolver resolver = new ConfigurationPathResolver(); 
	    acc.setConfigPathResolver(resolver);
        acc.setBackgroundColor(config.getDefaultBackgroundColor());
        acc.setDebug(config.debug());
        acc.setFrameRate(config.getDefaultFrameRate());
        acc.setHeight(Integer.toString(config.getDefaultHeight()));
        acc.setWidth(Integer.toString(config.getDefaultWidth()));
        acc.setSwfVersion(config.getSwfVersion());
        acc.setScriptRecursionLimit(config.getScriptRecursionLimit());
        acc.setScriptTimeLimit(config.getScriptTimeLimit());
        CompilerConfiguration cc = acc.getCompilerConfiguration();
        
        List<String> externalLibraries = config.getCompilerExternalLibraryPath();
        String[] extlibs = new String[externalLibraries.size()];
        externalLibraries.toArray(extlibs);
        try
        {
            cc.cfgExternalLibraryPath(null, extlibs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> libraries = config.getCompilerLibraryPath();
        String[] libs = new String[libraries.size()];
        libraries.toArray(libs);
        try
        {
            cc.cfgLibraryPath(null, libs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> sources = config.getCompilerSourcePath();
        String[] srcs = new String[sources.size()];
        sources.toArray(srcs);
        try
        {
            cc.cfgSourcePath(null, srcs);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
        return acc;
    }
	
	/**
	 * 
	 * @param args
	 * @param logger
	 * @param mimeMappings
	 * @return
	 */
	public static final OEMConfiguration getLibraryConfiguration(String[] args, boolean keepLinkReport, 
			                                                     boolean keepSizeReport, Logger logger, PathResolver resolver,
																 MimeMappings mimeMappings)
	{
		return getLibraryConfiguration(args, keepLinkReport, keepSizeReport, logger, resolver, mimeMappings, true);
	}

	/**
	 * 
	 * @param args
	 * @param logger
	 * @param mimeMappings
	 * @param processDefaults
	 * @return
	 */
	public static final OEMConfiguration getLibraryConfiguration(String[] args, boolean keepLinkReport, 
			                                                     boolean keepSizeReport, Logger logger,
																 PathResolver resolver, MimeMappings mimeMappings,
																 boolean processDefaults)
	{
		if (!processDefaults)
		{
			return new OEMConfiguration(null, null);
		}
		
		// expect no SWC inputs in args.
		OEMUtil.init(logger, mimeMappings, null, resolver, null);
		
        try
		{
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(LibraryCompilerConfiguration.class,
            													 LibraryCompilerConfiguration.getAliases());
	        DefaultsConfigurator.loadOEMCompcDefaults( cfgbuf );
            COMPJSC compc = new COMPJSC();
            compc.configure(args);
            LibraryCompilerConfiguration configuration = processCOMPCCConfiguration(compc.config);
            configuration.keepLinkReport(keepLinkReport);
            configuration.keepSizeReport(keepSizeReport);
            
            return new OEMConfiguration(cfgbuf, configuration);
		}
		catch (ConfigurationException ex)
		{
			//Mxmlc.processConfigurationException(ex, "oem");
			return null;
		}
		/*
		catch (IOException ex)
		{
			ThreadLocalToolkit.logError(ex.getMessage());
			return null;
		}
		*/
		catch (RuntimeException ex)
		{
			Class c;
			try
			{
				c = Class.forName("flex.messaging.config.ConfigurationException");
				if (c.isInstance(ex))
				{
					ThreadLocalToolkit.logError(ex.getMessage());
				}
			}
			catch (ClassNotFoundException ex2)
			{
				
			}
			return null;
		}
	}

	/**
	 * 
	 * @param args
	 * @param logger
	 * @param mimeMappings
	 * @return
	 */
	public static final OEMConfiguration getLinkerConfiguration(String[] args, boolean keepLinkReport, boolean keepSizeReport,
																Logger logger, MimeMappings mimeMappings,
																PathResolver resolver,
																flex2.compiler.common.Configuration c,
																Set newLinkerOptions, Set<String> includes, Set<String> excludes)
	{
		OEMUtil.init(logger, mimeMappings, null, resolver, null);
		
        try
		{
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(ApplicationCompilerConfiguration.class,
                    ApplicationCompilerConfiguration.getAliases());
            cfgbuf.setDefaultVar("--file-specs" /*Mxmlc.FILE_SPECS*/);            
            DefaultsConfigurator.loadDefaults(cfgbuf);
            MXMLJSC mxmlc = new MXMLJSC();
            mxmlc.configure(args);
            ApplicationCompilerConfiguration configuration = processMXMLCConfiguration(mxmlc.config);
            
            configuration.keepLinkReport(keepLinkReport);
            configuration.keepSizeReport(keepSizeReport);
            
            return new OEMConfiguration(cfgbuf, configuration);
		}
		catch (ConfigurationException ex)
		{
			//Mxmlc.processConfigurationException(ex, "oem");
			return null;
		}
		/*
		catch (IOException ex)
		{
			ThreadLocalToolkit.logError(ex.getMessage());
			return null;
		}
		*/
		catch (RuntimeException ex)
		{
			Class cls;
			try
			{
				cls = Class.forName("flex.messaging.config.ConfigurationException");
				if (cls.isInstance(ex))
				{
					ThreadLocalToolkit.logError(ex.getMessage());
				}
			}
			catch (ClassNotFoundException ex2)
			{
				
			}
			return null;
		}
	}

	public static final ConfigurationBuffer getCommandLineConfigurationBuffer(Logger logger, PathResolver resolver, String[] args)
	{
		ConfigurationBuffer cfgbuf = null;
		
        try
        {
			OEMUtil.init(logger, null, null, resolver, null);
			
	        cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, CommandLineConfiguration.getAliases());
	        SystemPropertyConfigurator.load( cfgbuf, "flex" );
        	CommandLineConfigurator.parse( cfgbuf, null, args);
        }
        catch (ConfigurationException ex)
        {
        	ThreadLocalToolkit.log(ex);
        	cfgbuf = null;
        }
        
        return cfgbuf;
	}
	
	public static final ConfigurationBuffer getCompcConfigurationBuffer(Logger logger, PathResolver resolver, String[] args)
	{
		ConfigurationBuffer cfgbuf = null;
		
        try
        {
			OEMUtil.init(logger, null, null, resolver, null);
			
	        cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
	        SystemPropertyConfigurator.load( cfgbuf, "flex" );
        	CommandLineConfigurator.parse( cfgbuf, null, args);
        }
        catch (ConfigurationException ex)
        {
        	ThreadLocalToolkit.log(ex);
        	cfgbuf = null;
        }
        
        return cfgbuf;
	}
	
	public static String[] trim(String[] args, ConfigurationBuffer cfgbuf, Set excludes)
	{
		List<Object[]> positions = cfgbuf.getPositions();
        List<Object> newArgs = new ArrayList<Object>();
		for (int i = 0, length = positions.size(); i < length; i++)
		{
			Object[] a = positions.get(i);
			String var = (String) a[0];
			int iStart = ((Integer) a[1]).intValue(), iEnd = ((Integer) a[2]).intValue();
			if (!excludes.contains(var))
			{
				for (int j = iStart; j < iEnd; j++)
				{
					newArgs.add(args[j]);
				}
			}
		}
		args = new String[newArgs.size()];
		newArgs.toArray(args);
		return args;
	}
	
	public static final Logger getLogger(Logger logger, List<Message> messages)
	{
		return new BuilderLogger(logger == null ? new OEMConsole() : logger, messages);
	}
	
	public static final String formatConfigurationBuffer(ConfigurationBuffer cfgbuf)
	{
		return FileConfigurator.formatBuffer(cfgbuf, "royale-config",
											 /*OEMUtil.setupLocalizationManager(),*/ "flex2.configuration");
	}

	/**
	 * 
	 * @param configuration
	 * @return
	 */
	public static final Map getLicenseMap(ToolsConfiguration configuration)
	{
		return configuration.getLicensesConfiguration().getLicenseMap();
	}

}
