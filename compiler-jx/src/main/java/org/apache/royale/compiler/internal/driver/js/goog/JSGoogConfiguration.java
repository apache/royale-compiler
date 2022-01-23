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
 * The {@link JSGoogConfiguration} class holds all compiler arguments needed for
 * compiling ActionScript to JavaScript the 'goog' way.
 * <p>
 * Specific flags are implemented here for the configuration to be loaded by the
 * configure() method of {@link MXMLJSC}.
 * <p>
 * This class inherits all compiler arguments from the MXMLC compiler.
 *
 * @author Erik de Bruin
 */
public class JSGoogConfiguration extends JSConfiguration
{
    public JSGoogConfiguration()
    {
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
            	if (System.getenv("GOOG_HOME") != null)
            		closureLib = System.getenv("GOOG_HOME");
            	if (closureLib.equals(""))
            	{
            		return getAbsolutePathFromPathRelativeToMXMLC(
                        "../../js/lib/google/closure-library");
            	}
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

    private boolean removeCirculars = true;

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
    	String externs = "--externs ";
    	for (int i = 0; i < value.size(); i++)
    	{
    		String item = value.get(i);
    		if (item.startsWith(externs))
    		{
    			String filePath = item.substring(externs.length());
    			filePath = resolvePathStrict(filePath, cv);
    			item = externs + filePath;
    			value.set(i,  item);
    		}
    	}
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
    // 'prevent-rename-mxml-symbol-references'
    //

    private boolean preventRenameMxmlSymbolReferences = true;

    public boolean getPreventRenameMxmlSymbolReferences()
    {
        return preventRenameMxmlSymbolReferences;
    }

    @Config
    @Mapping("prevent-rename-mxml-symbol-references")
    public void setPreventRenameMxmlSymbolReferences(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameMxmlSymbolReferences = value;
    }

    //
    // 'prevent-rename-public-symbols'
    //

    private boolean preventRenamePublicSymbols = true;

    public boolean getPreventRenamePublicSymbols()
    {
        return preventRenamePublicSymbols;
    }

    @Config
    @Mapping("prevent-rename-public-symbols")
    public void setPreventRenamePublicSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicSymbols = value;
    }

    //
    // 'prevent-rename-public-instance-methods'
    //

    private boolean preventRenamePublicInstanceMethods = true;

    public boolean getPreventRenamePublicInstanceMethods()
    {
        return preventRenamePublicInstanceMethods;
    }

    @Config
    @Mapping("prevent-rename-public-instance-methods")
    public void setPreventRenamePublicInstanceMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicInstanceMethods = value;
    }

    //
    // 'prevent-rename-public-static-methods'
    //

    private boolean preventRenamePublicStaticMethods = true;

    public boolean getPreventRenamePublicStaticMethods()
    {
        return preventRenamePublicStaticMethods;
    }

    @Config
    @Mapping("prevent-rename-public-static-methods")
    public void setPreventRenamePublicStaticMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicStaticMethods = value;
    }

    //
    // 'prevent-rename-public-instance-variables'
    //

    private boolean preventRenamePublicInstanceVariables = true;

    public boolean getPreventRenamePublicInstanceVariables()
    {
        return preventRenamePublicInstanceVariables;
    }

    @Config
    @Mapping("prevent-rename-public-instance-variables")
    public void setPreventRenamePublicInstanceVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicInstanceVariables = value;
    }

    //
    // 'prevent-rename-public-static-variables'
    //

    private boolean preventRenamePublicStaticVariables = true;

    public boolean getPreventRenamePublicStaticVariables()
    {
        return preventRenamePublicStaticVariables;
    }

    @Config
    @Mapping("prevent-rename-public-static-variables")
    public void setPreventRenamePublicStaticVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicStaticVariables = value;
    }

    //
    // 'prevent-rename-public-instance-accessors'
    //

    private boolean preventRenamePublicInstanceAccessors = true;

    public boolean getPreventRenamePublicInstanceAccessors()
    {
        return preventRenamePublicInstanceAccessors;
    }

    @Config
    @Mapping("prevent-rename-public-instance-accessors")
    public void setPreventRenamePublicInstanceAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicInstanceAccessors = value;
    }

    //
    // 'prevent-rename-public-static-accessors'
    //

    private boolean preventRenamePublicStaticAccessors = true;

    public boolean getPreventRenamePublicStaticAccessors()
    {
        return preventRenamePublicStaticAccessors;
    }

    @Config
    @Mapping("prevent-rename-public-static-accessors")
    public void setPreventRenamePublicStaticAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenamePublicStaticAccessors = value;
    }

    //
    // 'prevent-rename-protected-symbols'
    //

    private boolean preventRenameProtectedSymbols = true;

    public boolean getPreventRenameProtectedSymbols()
    {
        return preventRenameProtectedSymbols;
    }

    @Config
    @Mapping("prevent-rename-protected-symbols")
    public void setPreventRenameProtectedSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedSymbols = value;
    }

    //
    // 'prevent-rename-protected-instance-methods'
    //

    private boolean preventRenameProtectedInstanceMethods = true;

    public boolean getPreventRenameProtectedInstanceMethods()
    {
        return preventRenameProtectedInstanceMethods;
    }

    @Config
    @Mapping("prevent-rename-protected-instance-methods")
    public void setPreventRenameProtectedInstanceMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedInstanceMethods = value;
    }

    //
    // 'prevent-rename-protected-static-methods'
    //

    private boolean preventRenameProtectedStaticMethods = true;

    public boolean getPreventRenameProtectedStaticMethods()
    {
        return preventRenameProtectedStaticMethods;
    }

    @Config
    @Mapping("prevent-rename-protected-static-methods")
    public void setPreventRenameProtectedStaticMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedStaticMethods = value;
    }

    //
    // 'prevent-rename-protected-instance-variables'
    //

    private boolean preventRenameProtectedInstanceVariables = true;

    public boolean getPreventRenameProtectedInstanceVariables()
    {
        return preventRenameProtectedInstanceVariables;
    }

    @Config
    @Mapping("prevent-rename-protected-instance-variables")
    public void setPreventRenameProtectedInstanceVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedInstanceVariables = value;
    }

    //
    // 'prevent-rename-protected-static-variables'
    //

    private boolean preventRenameProtectedStaticVariables = true;

    public boolean getPreventRenameProtectedStaticVariables()
    {
        return preventRenameProtectedStaticVariables;
    }

    @Config
    @Mapping("prevent-rename-protected-static-variables")
    public void setPreventRenameProtectedStaticVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedStaticVariables = value;
    }

    //
    // 'prevent-rename-protected-instance-accessors'
    //

    private boolean preventRenameProtectedInstanceAccessors = true;

    public boolean getPreventRenameProtectedInstanceAccessors()
    {
        return preventRenameProtectedInstanceAccessors;
    }

    @Config
    @Mapping("prevent-rename-protected-instance-accessors")
    public void setPreventRenameProtectedInstanceAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedInstanceAccessors = value;
    }

    //
    // 'prevent-rename-protected-static-accessors'
    //

    private boolean preventRenameProtectedStaticAccessors = true;

    public boolean getPreventRenameProtectedStaticAccessors()
    {
        return preventRenameProtectedStaticAccessors;
    }

    @Config
    @Mapping("prevent-rename-protected-static-accessors")
    public void setPreventRenameProtectedStaticAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameProtectedStaticAccessors = value;
    }

    //
    // 'prevent-rename-internal-symbols'
    //

    private boolean preventRenameInternalSymbols = true;

    public boolean getPreventRenameInternalSymbols()
    {
        return preventRenameInternalSymbols;
    }

    @Config
    @Mapping("prevent-rename-internal-symbols")
    public void setPreventRenameInternalSymbols(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalSymbols = value;
    }

    //
    // 'prevent-rename-internal-instance-methods'
    //

    private boolean preventRenameInternalInstanceMethods = true;

    public boolean getPreventRenameInternalInstanceMethods()
    {
        return preventRenameInternalInstanceMethods;
    }

    @Config
    @Mapping("prevent-rename-internal-instance-methods")
    public void setPreventRenameInternalInstanceMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalInstanceMethods = value;
    }

    //
    // 'prevent-rename-internal-static-methods'
    //

    private boolean preventRenameInternalStaticMethods = true;

    public boolean getPreventRenameInternalStaticMethods()
    {
        return preventRenameInternalStaticMethods;
    }

    @Config
    @Mapping("prevent-rename-internal-static-methods")
    public void setPreventRenameInternalStaticMethods(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalStaticMethods = value;
    }

    //
    // 'prevent-rename-internal-instance-variables'
    //

    private boolean preventRenameInternalInstanceVariables = true;

    public boolean getPreventRenameInternalInstanceVariables()
    {
        return preventRenameInternalInstanceVariables;
    }

    @Config
    @Mapping("prevent-rename-internal-instance-variables")
    public void setPreventRenameInternalInstanceVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalInstanceVariables = value;
    }

    //
    // 'prevent-rename-internal-static-variables'
    //

    private boolean preventRenameInternalStaticVariables = true;

    public boolean getPreventRenameInternalStaticVariables()
    {
        return preventRenameInternalStaticVariables;
    }

    @Config
    @Mapping("prevent-rename-internal-static-variables")
    public void setPreventRenameInternalStaticVariables(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalStaticVariables = value;
    }

    //
    // 'prevent-rename-internal-instance-accessors'
    //

    private boolean preventRenameInternalInstanceAccessors = true;

    public boolean getPreventRenameInternalInstanceAccessors()
    {
        return preventRenameInternalInstanceAccessors;
    }

    @Config
    @Mapping("prevent-rename-internal-instance-accessors")
    public void setPreventRenameInternalInstanceAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalInstanceAccessors = value;
    }

    //
    // 'prevent-rename-internal-static-accessors'
    //

    private boolean preventRenameInternalStaticAccessors = true;

    public boolean getPreventRenameInternalStaticAccessors()
    {
        return preventRenameInternalStaticAccessors;
    }

    @Config
    @Mapping("prevent-rename-internal-static-accessors")
    public void setPreventRenameInternalStaticAccessors(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	preventRenameInternalStaticAccessors = value;
    }

    
    //
    // 'prevent-rename-object-keys'
    //

    private List<String> preventRenameObjectKeys = new ArrayList<String>();

    public List<String> getPreventRenameObjectKeys()
    {
        return preventRenameObjectKeys;
    }

    @Config(allowMultiple = true, isPath = false)
    @Mapping("prevent-rename-object-keys")
    @Arguments("key")
    @InfiniteArguments
    public void setPreventRenameObjectKeys(ConfigurationValue cv, String[] keys)
            throws ConfigurationException
    {
    	preventRenameObjectKeys.clear();
    	for (String key : keys)
    		preventRenameObjectKeys.add(key);
    }

    //
    // 'allow-dynamic-bindings'
    //

    private boolean allowDynamicBindings = true;

    public boolean getAllowDynamicBindings()
    {
        return allowDynamicBindings;
    }

    @Config
    @Mapping("allow-dynamic-bindings")
    public void setAllowDynamicBindings(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	allowDynamicBindings = value;
    }

    //
    // 'mxml-reflect-object-property'
    //

    private boolean mxmlReflectObjectProperty = false;

    public boolean getMxmlReflectObjectProperty()
    {
        return mxmlReflectObjectProperty;
    }

    @Config
    @Mapping("mxml-reflect-object-property")
    public void setMxmlReflectObjectProperty(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
    	mxmlReflectObjectProperty = value;
    }

    
    //
    // 'warn-public-vars'
    //

    private boolean warnPublicVars = true;

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

    // 'externs-report' option
    //

    private String externsReportFileName = null;

    public File getExternsReport()
    {
        return externsReportFileName != null ? new File(externsReportFileName) : null;
    }

    /**
     * Prints externs information to the specified output file. This file is an Google Closure Compiler externs file that contains
     * all of the public and protected APIs in the final SWF file. The file format output
     * by this command can be used to write a file for input to the {@code -js-compiler-options="--externs <path-to-this-file>"} option.
     */
    @Config(advanced = true)
    @Mapping("externs-report")
    @Arguments("filename")
    public void setExternsReport(ConfigurationValue cv, String filename)
    {
        this.externsReportFileName = getOutputPath(cv, filename);
    }
    
    
    /**
     * Support for reflection data output to represent selected config options
     * that were used when compiling
     * @return an integer representation of bit flags representing
     */
    public int getReflectionFlags() {
        int ret = 0;
        final int WITH_DEFAULT_INITIALIZERS = 1;
        final int HAS_KEEP_AS3_METADATA = 2;
        final int HAS_KEEP_CODE_WITH_METADATA = 4;
        final int HAS_EXPORT_PUBLIC_SYMBOLS = 8;
        final int EXPORT_PROTECTED_SYMBOLS = 16;
        final int EXPORT_INTERNAL_SYMBOLS = 32;
    
        if (getJsDefaultInitializers()) ret |= WITH_DEFAULT_INITIALIZERS;
        if (getCompilerKeepAs3Metadata().size() > 0) ret |= HAS_KEEP_AS3_METADATA;
        if (getCompilerKeepCodeWithMetadata().size() > 0) ret |= HAS_KEEP_CODE_WITH_METADATA;
        if (getExportPublicSymbols()) ret |= HAS_EXPORT_PUBLIC_SYMBOLS;
        if (getExportProtectedSymbols()) ret |= EXPORT_PROTECTED_SYMBOLS;
        if (getExportInternalSymbols()) ret |= EXPORT_INTERNAL_SYMBOLS;
        
        return ret;
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

    //
    // 'js-getter-prefix'
    //

    protected String jsGetterPrefix = "";

    public String getJsGetterPrefix()
    {
        return jsGetterPrefix;
    }

    @Config
    @Mapping("js-getter-prefix")
    public void setJsGetterPrefix(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
        jsGetterPrefix = value;
    }

    //
    // 'js-setter-prefix'
    //

    protected String jsSetterPrefix = "";

    public String getJsSetterPrefix()
    {
        return jsSetterPrefix;
    }

    @Config
    @Mapping("js-setter-prefix")
    public void setJsSetterPrefix(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
        jsSetterPrefix = value;
    }

}
