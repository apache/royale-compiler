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
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.clients.JSConfiguration;
import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.RoyaleOnly;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;

/**
 * The {@link JSGoogCompcConfiguration} class holds all compiler arguments needed for
 * compiling ActionScript to JavaScript the 'goog' way.
 * <p>
 * Specific flags are implemented here for the configuration to be loaded by the
 * configure() method of {@link MXMLJSC}.
 * <p>
 * This class inherits all compiler arguments from the MXMLC compiler.
 * 
 * @author Erik de Bruin
 */
public class JSGoogCompcConfiguration extends JSConfiguration
{
    public JSGoogCompcConfiguration()
    {
    	setDebug(true);
    }

    //
    // 'closure-lib'
    //

    protected String closureLib = "";

    public boolean isClosureLibSet() {
        return !closureLib.isEmpty();
    }

    public String getClosureLib()
    {
        try
        {
            if (closureLib.equals(""))
            {
                return getAbsolutePathFromPathRelativeToMXMLC(
                        "../../js/lib/google/closure-library");
            }
        }
        catch (Exception e) { /* better to try and fail... */ }
        
        return closureLib;
    }

    @Config
    @Mapping("closure-lib")
    public void setClosureLib(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
        if (value != null)
            closureLib = value;
    }

    //
    // Override 'compiler.binding-value-change-event-type'
    //

    private String bindingValueChangeEventType = "valueChange";

    @Override
    public String getBindingValueChangeEventType()
    {
        return bindingValueChangeEventType;
    }

    @Override
    @Config(advanced = true)
    public void setCompilerBindingValueChangeEventType(ConfigurationValue cv, String b)
    {
        bindingValueChangeEventType = b;
    }

    //
    // Override 'compiler.mxml.children-as-data'
    //
    
    private Boolean childrenAsData = true;
    
    @Override
    public Boolean getCompilerMxmlChildrenAsData()
    {
        return childrenAsData;
    }

    @Override
    @Config
    @Mapping({"compiler", "mxml", "children-as-data"})
    @RoyaleOnly
    public void setCompilerMxmlChildrenAsData(ConfigurationValue cv, Boolean asData) throws ConfigurationException
    {
        childrenAsData = asData;
    }

    //
    // 'marmotinni'
    //

    private String marmotinni;

    public String getMarmotinni()
    {
        return marmotinni;
    }

    @Config
    @Mapping("marmotinni")
    public void setMarmotinni(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
        marmotinni = value;
    }

    //
    // 'sdk-js-lib'
    //

    protected List<String> sdkJSLib = new ArrayList<String>();

    public List<String> getSDKJSLib()
    {
        if (sdkJSLib.size() == 0)
        {
            try
            {
                String path = getAbsolutePathFromPathRelativeToMXMLC(
                            "../../frameworks/js/Royale/src");

                sdkJSLib.add(path);
            }
            catch (Exception e) { /* better to try and fail... */ }
        }
        
        return sdkJSLib;
    }

    @Config(allowMultiple = true)
    @Mapping("sdk-js-lib")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setSDKJSLib(ConfigurationValue cv, List<String> value)
            throws ConfigurationException
    {
        sdkJSLib.addAll(value);
    }

    //
    // 'external-js-lib'
    //

    private List<String> externalJSLib = new ArrayList<String>();

    public List<String> getExternalJSLib()
    {
        return externalJSLib;
    }

    @Config(allowMultiple = true)
    @Mapping("external-js-lib")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setExternalJSLib(ConfigurationValue cv, List<String> value)
            throws ConfigurationException
    {
        externalJSLib.addAll(value);
    }

    //
    // 'strict-publish'
    //

    private boolean strictPublish = true;

    public boolean getStrictPublish()
    {
        return strictPublish;
    }

    @Config
    @Mapping("strict-publish")
    public void setStrictPublish(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        strictPublish = value;
    }

    //
    // 'keep-asdoc'
    //

    private boolean keepASDoc = true;

    public boolean getKeepASDoc()
    {
        return keepASDoc;
    }

    @Config
    @Mapping("keep-asdoc")
    public void setKeepASDoc(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	keepASDoc = value;
    }

    
    
    //
    // 'remove-circulars'
    //

    private boolean removeCirculars = false;

    public boolean getRemoveCirculars()
    {
        return removeCirculars;
    }

    @Config
    @Mapping("remove-circulars")
    public void setRemoveCirculars(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	removeCirculars = value;
    }

    
    //
    // 'skip-transpile'
    //

    private boolean skipTranspile = false;

    public boolean getSkipTranspile()
    {
        return skipTranspile;
    }

    @Config
    @Mapping("skip-transpile")
    public void setSkipTranspile(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	skipTranspile = value;
    }
    
    
    
    protected String getAbsolutePathFromPathRelativeToMXMLC(String relativePath)
        throws IOException
    {
        String mxmlcURL = MXMLJSC.class.getProtectionDomain().getCodeSource()
                .getLocation().getPath();

        File mxmlc = new File(URLDecoder.decode(mxmlcURL, "utf-8"));
        
        return new File(mxmlc.getParent() + File.separator + relativePath)
                .getCanonicalPath();
    }

    //
    // 'js-compiler-option'
    //

    protected List<String> jsCompilerOptions = new ArrayList<String>();

    public List<String> getJSCompilerOptions()
    {
        return jsCompilerOptions;
    }

    @Config(allowMultiple = true)
    @Mapping("js-compiler-option")
    @Arguments("option")
    @InfiniteArguments
    public void setJSCompilerOptions(ConfigurationValue cv, List<String> value)
            throws ConfigurationException
    {
    	jsCompilerOptions.addAll(value);
    }

    //
    // 'js-output-optimization'
    //

    protected List<String> jsOutputOptimizations = new ArrayList<String>();

    public List<String> getJSOutputOptimizations()
    {
        return jsOutputOptimizations;
    }

    @Config(allowMultiple = true)
    @Mapping("js-output-optimization")
    @Arguments("optimization")
    @InfiniteArguments
    public void setJSOutputOptimizations(ConfigurationValue cv, List<String> value)
            throws ConfigurationException
    {
    	jsOutputOptimizations.addAll(value);
    }

    // 'html-template' option
    //

    private String htmlTemplateFileName = null;

    public File getHtmlTemplate()
    {
        return htmlTemplateFileName != null ? new File(htmlTemplateFileName) : null;
    }

    /**
     * Specify an HTML template with tokens to replace with application-specific values.
     * If not specified a standard template is generated.
     */
    @Config(advanced = true)
    @Mapping("html-template")
    @Arguments("filename")
    public void setHtmlTemplate(ConfigurationValue cv, String filename)
    {
        this.htmlTemplateFileName = getOutputPath(cv, filename);
    }

    // 'html-output-filename' option
    //

    private String htmlOutputFileName = "index.html";

    public String getHtmlOutputFileName()
    {
        return htmlOutputFileName;
    }

    /**
     * Specify the name of the HTML file that goes in the output folder.  Default is index.html.
     */
    @Config(advanced = true)
    @Mapping("html-output-filename")
    @Arguments("filename")
    public void setHtmlOutputFileName(ConfigurationValue cv, String filename)
    {
        this.htmlOutputFileName = filename;
    }

    //
    // 'compiler.keep-code-with-metadata' option
    //

    private Set<String> keepCodeWithMetadata = null;

    public Set<String> getCompilerKeepCodeWithMetadata()
    {
        return keepCodeWithMetadata == null ? Collections.<String> emptySet() : keepCodeWithMetadata;
    }

    @Config(advanced = true, allowMultiple = true)
    @Mapping({ "compiler", "keep-code-with-metadata" })
    @Arguments("name")
    @InfiniteArguments
    public void setCompilerKeepCodeWithMetadata(ConfigurationValue cv, List<String> values)
    {
        if (keepCodeWithMetadata == null)
        	keepCodeWithMetadata = new HashSet<String>();
        keepCodeWithMetadata.addAll(values);
    }

    //
    // 'export-public-symbols'
    //

    private boolean exportPublicSymbols = true;

    public boolean getExportPublicSymbols()
    {
        return exportPublicSymbols;
    }

    @Config
    @Mapping("export-public-symbols")
    public void setExportPublicSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	exportPublicSymbols = value;
    }

    //
    // 'export-protected-symbols'
    //

    private boolean exportProtectedSymbols = false;

    public boolean getExportProtectedSymbols()
    {
        return exportProtectedSymbols;
    }

    @Config
    @Mapping("export-protected-symbols")
    public void setExportProtectedSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	exportProtectedSymbols = value;
    }

    //
    // 'export-internal-symbols'
    //

    private boolean exportInternalSymbols = false;

    public boolean getExportInternalSymbols()
    {
        return exportInternalSymbols;
    }

    @Config
    @Mapping("export-internal-symbols")
    public void setExportInternalSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	exportInternalSymbols = value;
    }

    
    //
    // 'warn-public-vars'
    //

    private boolean warnPublicVars = false;

    public boolean getWarnPublicVars()
    {
        return warnPublicVars;
    }

    @Config
    @Mapping("warn-public-vars")
    public void setWarnPublicVars(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	warnPublicVars = value;
    }

    //
    // 'inline-constants'
    //

    private boolean inlineConstants = false;

    public boolean getInlineConstants()
    {
        return inlineConstants;
    }

    @Config
    @Mapping("inline-constants")
    public void setInlineConstants(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	inlineConstants = value;
    }

}
