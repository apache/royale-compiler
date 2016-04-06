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

package flex2.linker;

import flash.swf.Movie;
 
/**
 * Represents a simple single frame Movie.  It's currently not
 * instantiated directly.  Instead, it's subclasses for special
 * purposes like an application SWF or a library SWF.
 *
 * @author Clement Wong
 */
public class SimpleMovie extends Movie
{
	/**
	 * MD5 password is no longer needed for Flash Player. 
	 * Use this dummy password instead of generating one.
	 * @see http://bugs.adobe.com/jira/browse/SDK-27210
	 */
	//private static final String NO_PASSWORD = "NO-PASSWORD";
	
	public SimpleMovie(LinkerConfiguration configuration)
    {
	    /*
        if (configuration.width() != null)
	    {
		    try
		    {
			    width = Integer.parseInt(configuration.width());
		    }
		    catch(NumberFormatException nfe)
		    {
			    ThreadLocalToolkit.log(new PreLink.CouldNotParseNumber(configuration.width(), "width"));
		    }
	        userSpecifiedWidth = true;
	    }
	    else if (configuration.widthPercent() != null)
	    {
		    width = configuration.defaultWidth();
	        widthPercent = configuration.widthPercent();
	    }
		else
	    {
		    width = configuration.defaultWidth();
	    }

	    if (configuration.height() != null)
	    {
		    try
		    {
			    height = Integer.parseInt(configuration.height());
		    }
		    catch(NumberFormatException nfe)
		    {
			    ThreadLocalToolkit.log(new PreLink.CouldNotParseNumber(configuration.height(), "height"));
		    }
	        userSpecifiedHeight = true;
	    }
	    else if (configuration.heightPercent() != null)
	    {
		    height = configuration.defaultHeight();
	        heightPercent = configuration.heightPercent();
	    }
		else
	    {
		    height = configuration.defaultHeight();
	    }

		size = new Rect(width * 20, height * 20);

        if ((configuration.scriptLimitsSet()))
        {
            scriptLimits = new ScriptLimits( configuration.getScriptRecursionLimit(),
                                             configuration.getScriptTimeLimit() );
        }

        framerate = configuration.getFrameRate();
		version = configuration.getSwfVersion();
		bgcolor = new SetBackgroundColor(configuration.backgroundColor());
		if (configuration.debug())
		{
			enableDebugger = new EnableDebugger(NO_PASSWORD);
			uuid = new FlashUUID();
		}

        // SWF 8 File Attributes Support
        if (version >= 8)
        {
            fileAttributes = new FileAttributes();
            enableTelemetry = new EnableTelemetry();
            fileAttributes.actionScript3 = (version >= 9);    

            if (configuration.useNetwork())
            {
                fileAttributes.useNetwork = true;
	            fileAttributes.actionScript3 = (version >= 9);
            }
            
            if (configuration.getAdvancedTelemetry()) {
            	enableTelemetry.enabled = true;
            }
            
            fileAttributes.useDirectBlit = configuration.getUseDirectBlit();
            fileAttributes.useGPU = configuration.getUseGpu();
            
	        String metadataStr = configuration.getMetadata();
            if (metadataStr != null)
            {
                metadata = new Metadata();
                metadata.xml = metadataStr;
                fileAttributes.hasMetadata = true;
            }
        }

        long build = 0;
        byte majorVersion = 0;
        byte minorVersion = 0;
        
        try
        {
            majorVersion = (byte)Integer.parseInt(VersionInfo.FLEX_MAJOR_VERSION);
            minorVersion = (byte)Integer.parseInt(VersionInfo.FLEX_MINOR_VERSION);
            build = Long.parseLong( VersionInfo.getBuild() );
        }
        catch (NumberFormatException numberFormatException)
        {
            // preilly: for now just ignore empty or bogus build numbers.
        }

        
        productInfo = new ProductInfo(ProductInfo.ABOBE_FLEX_PRODUCT, 6,
                                        majorVersion, minorVersion, build, 
                                        System.currentTimeMillis());
		pageTitle = configuration.pageTitle();

		lazyInit = configuration.lazyInit();
        rootClassName = formatSymbolClassName( configuration.getRootClassName() );

        if (rootClassName == null)
            rootClassName = formatSymbolClassName( configuration.getMainDefinition() );
        
        exportedUnits = new LinkedHashMap<CompilationUnit, Frame>();
        
        includeInheritanceDependenciesOnly = configuration.getIncludeInheritanceDependenciesOnly();
        */
	}
	
    protected String linkReport, sizeReport;

    public String getLinkReport()
    {
    	return linkReport;
    }
    
    public String getSizeReport()
    {
    	return sizeReport;
    }
    
    public void setSizeReport(String report)
    {
    	sizeReport = report;
    }
    
    /*
    public String getRBList()
    {
    	return rbList;
    }
    
    public boolean getInheritanceDependenciesOnly()
    {
        return includeInheritanceDependenciesOnly;
    }
    */
}
