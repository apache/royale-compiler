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

package org.apache.royale.compiler.internal.driver.js.goog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationInfo;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;

/**
 * A Configuration to override some behaviors of the default configuration.
 */
public class ASDocConfiguration extends JSGoogConfiguration
{
    public ASDocConfiguration()
    {
        super();
        
        // Override MXMLC defaults
        setDebug(true);
    }

	public static Map<String, String> getAliases()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put( "o", "output" );
        map.put( "dc", "doc-classes" );
        map.put( "dn", "doc-namespaces" );
        map.put( "ds", "doc-sources" );
	    map.putAll(Configuration.getAliases());
		return map;
    }

	//
	// 'doc-classes' option
	//
	
    private List<String> classes = new ArrayList<String>();

    public List<String> getClasses()
    {
        return classes;
    }

    public void cfgDocClasses(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
        classes.addAll( args );
    }

    public static ConfigurationInfo getDocClassesInfo()
    {
        return new ConfigurationInfo( -1, new String[] { "class" } )
        {
            public boolean allowMultiple()
            {
                return true;
            }
        };
    }

    /**
     * @return Normalized file paths of the included source files.
     */
    @Override
    public List<String> getIncludeClasses()
    {
        return classes;
    }


	//
	// 'doc-namespaces' option
	//
	
    private List<String> namespaces = new ArrayList<String>();

    public List<String> getNamespaces()
	{
	    return namespaces;
	}

	public void cfgDocNamespaces(ConfigurationValue val, List<String> DocNamespaces)
	{
	    namespaces.addAll(DocNamespaces);
	}

	public static ConfigurationInfo getDocNamespacesInfo()
	{
	    return new ConfigurationInfo( -1, new String[] { "uri" } )
	    {
	        public boolean allowMultiple()
	        {
	            return true;
	        }
	    };
	}

    /**
     * @return Normalized file paths of the included source files.
     */
    @Override
    public List<String> getIncludeNamespaces()
    {
        return namespaces;
    }

	//
	// 'doc-sources' option
	//
	
    private List<String> sources = new ArrayList<String>();

    public List<String> getDocSources()
    {
        return sources;
    }

    public void cfgDocSources(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
        sources.addAll( args );
    }

    public static ConfigurationInfo getDocSourcesInfo()
    {
        return new ConfigurationInfo( -1, new String[] { "path-element" } )
        {
            public boolean allowMultiple()
            {
                return true;
            }

            public boolean isPath()
            {
                return true;
            }
        };
    }

    /**
     * @return Normalized file paths of the included source files.
     */
    @Override
    public List<String> getIncludeSources()
    {
        return sources;
    }


	//
	// 'examples-path' option
	//
	
	private String examplesPath;

	public String getExamplesPath()
	{
		return examplesPath;
	}

	public void cfgExamplesPath(ConfigurationValue cv, String str) throws ConfigurationException
	{
		File file = new File(str);
		if (! file.isDirectory())
		{
			throw new ConfigurationException.NotDirectory( str, cv.getVar(), cv.getSource(), cv.getLine() );
		}
		examplesPath = file.getAbsolutePath().replace('\\', '/');
	}

	//
	// 'exclude-classes' option
	//
	
	private List<String> excludeClasses = new ArrayList<String>();

	public List<String> getExcludeClasses()
	{
	    return excludeClasses;
	}

	public void cfgExcludeClasses(ConfigurationValue cv, List<String> args) throws ConfigurationException
	{
	    excludeClasses.addAll( args );
	}

	public static ConfigurationInfo getExcludeClassesInfo()
	{
	    return new ConfigurationInfo( -1, new String[] { "class" } )
	    {
	        public boolean allowMultiple()
	        {
	            return true;
	        }
	    };
	}

	//
	// 'exclude-dependencies' option
	//
	
	private boolean excludeDependencies;

	public boolean excludeDependencies()
	{
		return excludeDependencies;
	}

	public void cfgExcludeDependencies(ConfigurationValue val, boolean bool)
	{
	    this.excludeDependencies = bool;
	}

	//
	// 'footer' option
	//
	
	private String footer;

	public String getFooter()
	{
		return footer;
	}

	public void cfgFooter(ConfigurationValue cv, String str) throws ConfigurationException
	{
		 footer = str;
	}

	//
	// 'help' option
	//
	
    // dummy, just a trigger for help text
    public void cfgHelp(ConfigurationValue cv, String[] keywords)
    {
        // intercepted upstream in order to allow help text to be printed even when args are otherwise bad
    }
    
    public static ConfigurationInfo getHelpInfo()
    {
        return new ConfigurationInfo( -1, "keyword" )
        {
            public boolean isGreedy()
			{
				return true;
			}

            public boolean isDisplayed()
			{
				return false;
			}
        };
    }
	//
	// 'keep-xml' option
	//
	
	private boolean keepXML;

	public boolean keepXml()
	{
		return keepXML;
	}

	public void cfgKeepXml(ConfigurationValue cv, boolean b) throws ConfigurationException
	{
		keepXML = b;
	}

	public static ConfigurationInfo getKeepXmlInfo()
	{
		return new ConfigurationInfo()
		{
		    public boolean isHidden()
		    {
			    return true;
		    }
		};
	}

	//
	// 'left-frameset-width' option
	//
	
	private int leftFramesetWidth;

	public int getLeftFramesetWidth()
	{
		return leftFramesetWidth;
	}

	public void cfgLeftFramesetWidth(ConfigurationValue val, int left)
	{
		this.leftFramesetWidth = left;
	}

	//
	// 'load-config' p[topm
	//
	
    // dummy, ignored - pulled out of the buffer
    public void cfgLoadConfig(ConfigurationValue cv, String filename) throws ConfigurationException
    {
    }

    public static ConfigurationInfo getLoadConfigInfo()
    {
        return new ConfigurationInfo( 1, "filename" )
        {
            public boolean allowMultiple()
            {
                return true;
            }
        };
    }

	//
	// 'main-title' option
	//
	
	private String mainTitle;

	public String getMainTitle()
	{
		return mainTitle;
	}

	public void cfgMainTitle(ConfigurationValue cv, String str) throws ConfigurationException
	{
		 mainTitle = str;
	}

	//
	// 'package-description-file' option
	//
	private String packageDescriptionFile;

	public String getPackageDescriptionFile()
	{
		return packageDescriptionFile;
	}

	public void cfgPackageDescriptionFile(ConfigurationValue cv, String str) throws ConfigurationException
	{
		File file = new File(str);
		if (!file.exists() || file.isDirectory())
		{
			throw new ConfigurationException.NotAFile( str, cv.getVar(), cv.getSource(), cv.getLine() );
		}
		packageDescriptionFile = file.getAbsolutePath().replace('\\', '/');
	}	

	//
	// 'skip-xsl' option
	//
	
	private boolean skipXSL;

	public boolean skipXsl()
	{
		return skipXSL;
	}

	public void cfgSkipXsl(ConfigurationValue cv, boolean b) throws ConfigurationException
	{
		skipXSL = b;
	}

	public static ConfigurationInfo getSkipXslInfo()
	{
		return new ConfigurationInfo()
		{
		    public boolean isHidden()
		    {
			    return true;
		    }
		};
	}

	//
	// 'templates-path' option
	//
	
	private String templatesPath;

	public String getTemplatesPath()
    {
        return templatesPath;
    }

    public void cfgTemplatesPath(ConfigurationValue val, String basedir) throws ConfigurationException
    {
        this.templatesPath = basedir;
    }

	//
	// 'version' option
	//
	
    // dummy, just a trigger for version info
    public void cfgVersion(ConfigurationValue cv, boolean dummy)
    {
        // intercepted upstream in order to allow version into to be printed even when required args are missing
    }

	//
	// 'window-title' option
	//
	
	private String windowTitle;

	public String getWindowTitle()
	{
		return windowTitle;
	}

	public void cfgWindowTitle(ConfigurationValue cv, String str) throws ConfigurationException
	{
		 windowTitle = str;
	}
	
    //
    // 'restore-builtin-classes' option
    //
    
    private boolean restoreBuiltinClasses;

    public boolean restoreBuiltinClasses()
    {
        return restoreBuiltinClasses;
    }

    public void cfgRestoreBuiltinClasses(ConfigurationValue cv, boolean b) throws ConfigurationException
    {
        restoreBuiltinClasses = b;
    }

    // restore-builtin-classes is only for internal use
    public static ConfigurationInfo getRestoreBuiltinClassesInfo()
    {
        return new ConfigurationInfo()
        {
            public boolean isHidden()
            {
                return true;
            }
        };
    }
    
    
    //
    // 'lenient' option
    //
    
    private boolean lenient;

    public boolean isLenient()
    {
        return lenient;
    }

    public void cfgLenient(ConfigurationValue cv, boolean b) throws ConfigurationException
    {
        lenient = b;
    }
    
    //
    // 'exclude-sources' option
    //
    
    private List<String> excludeSources = new ArrayList<String>();

    public List<String> getExcludeSources()
    {
        return excludeSources;
    }

    public void cfgExcludeSources(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
        for (String arg : args)
        {
            arg = arg.replace("\\", "/");
            excludeSources.add(arg);
        }
    }

    public static ConfigurationInfo getExcludeSourcesInfo()
    {
        return new ConfigurationInfo( -1, new String[] { "path-element" } )
        {
            public boolean allowMultiple()
            {
                return true;
            }

            public boolean isPath()
            {
                return true;
            }
        };
    }

    //
    // 'date-in-footer' option
    //
    
    private boolean dateInFooter = true;

    public boolean getDateInFooter()
    {
        return dateInFooter;
    }

    public void cfgDateInFooter(ConfigurationValue cv, boolean b) throws ConfigurationException
    {
        dateInFooter = b;
    }
    
    //
    // 'include-all-for-asdoc' option
    //
    private boolean includeAllForAsdoc;

    public boolean isIncludeAllForAsdoc()
    {
        return includeAllForAsdoc;
    }

    public void cfgIncludeAllForAsdoc(ConfigurationValue cv, boolean b) throws ConfigurationException
    {
    	includeAllForAsdoc = b;
    }

    // include-all-only is only for internal use
    public static ConfigurationInfo getIncludeAllForAsdoc()
    {
        return new ConfigurationInfo()
        {
            public boolean isHidden()
            {
                return true;
            }
        };
    }
}
