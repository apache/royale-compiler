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

package org.apache.royale.compiler.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;

import org.apache.royale.compiler.ant.config.ConfigAppendString;
import org.apache.royale.compiler.ant.config.ConfigBoolean;
import org.apache.royale.compiler.ant.config.ConfigInt;
import org.apache.royale.compiler.ant.config.ConfigString;
import org.apache.royale.compiler.ant.config.ConfigVariable;
import org.apache.royale.compiler.ant.config.NestedAttributeElement;
import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;
import org.apache.royale.compiler.ant.types.DefaultScriptLimits;
import org.apache.royale.compiler.ant.types.DefaultSize;
import org.apache.royale.compiler.ant.types.FlexFileSet;
import org.apache.royale.compiler.ant.types.FlexSWCFileSet;
import org.apache.royale.compiler.ant.types.Fonts;
import org.apache.royale.compiler.ant.types.Metadata;
import org.apache.royale.compiler.ant.types.RuntimeSharedLibraryPath;

/**
 * Implements the {@code <mxmlc>} Ant task.
 * <p>
 * For example:
 * <pre>
 * &lt;mxmlc file="${bug}.mxml"
 *         debug="false"
 *         keep="true"
 *         verbose-stacktraces="false"
 *         incremental="false"
 *         strict="true"
 *         benchmark="true"
 *         report-invalid-styles-as-warnings="true"
 *         show-invalid-css-property-warnings="false"
 *         tools-locale="de_DE"
 *         fork="false"&gt;
 *     &lt;source-path path-element="${ROYALE_HOME}/frameworks/projects/framework/src"/&gt;
 * &lt;/mxmlc&gt;
 * </pre>
 *
 * All the simple mxmlc configuration parameters are supported as tag
 * attributes.  Complex configuration options, like
 * -compiler.namespaces.namespace, are implemented as child tags.  For
 * example:
 * <p>
 * <code>
 * &lt;namespace uri="http://www.adobe.com/2006/mxml" manifest="${basedir}/manifest.xml"/&gt;
 * </code>
 */
public final class MXMLCTask extends FlexTask implements DynamicConfigurator
{
    /*=======================================================================*
     * Constants                                     
     *=======================================================================*/

	private static final String TASK_NAME = "mxmlc";
	
	private static final String TOOL_JAR_FILE_NAME = "jsc.jar";

	private static final String TOOL_CLASS_NAME = "org.apache.royale.compiler.clients.MXMLJSC";

	private static final String TOOL_METHOD_NAME = "staticMainNoExit";
	
	private static final String TOOL_FAILURE_METHOD_NAME = "isFatalFailure";
	
	private static final ConfigVariable[] CONFIG_VARIABLES =
	{
        // Basic Boolean options
        new ConfigBoolean(new OptionSpec("benchmark")),
        new ConfigBoolean(new OptionSpec("compiler.accessible")),
        new ConfigBoolean(new OptionSpec("compiler.debug")),
        new ConfigBoolean(new OptionSpec("compiler.incremental")),
        new ConfigBoolean(new OptionSpec("compiler.mobile")),
        new ConfigBoolean(new OptionSpec("compiler.optimize")),
        new ConfigBoolean(new OptionSpec("compiler.report-invalid-styles-as-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.report-missing-required-skin-parts-as-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.show-actionscript-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.show-binding-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.show-deprecation-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.show-invalid-css-property-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.show-unused-type-selector-warnings")),
        new ConfigBoolean(new OptionSpec("compiler.strict")),
        new ConfigBoolean(new OptionSpec("compiler.use-resource-bundle-metadata")),
        new ConfigBoolean(new OptionSpec("remove-unused-rsls")),
        new ConfigBoolean(new OptionSpec("use-network")),
        new ConfigBoolean(new OptionSpec("warnings")),
        
        // Advanced Boolean options
        new ConfigBoolean(new OptionSpec("compiler.allow-source-path-overlap")),
        new ConfigBoolean(new OptionSpec("compiler.as3")),
        new ConfigBoolean(new OptionSpec("compiler.doc")),
        new ConfigBoolean(new OptionSpec("compiler.es")),
        new ConfigBoolean(new OptionSpec("compiler.generate-abstract-syntax-tree")),
        new ConfigBoolean(new OptionSpec("compiler.headless-server")),
        new ConfigBoolean(new OptionSpec("compiler.isolate-styles")),
        new ConfigBoolean(new OptionSpec("compiler.keep-all-type-selectors")),
        new ConfigBoolean(new OptionSpec("compiler.keep-generated-actionscript", "keep")),
        new ConfigBoolean(new OptionSpec("compiler.verbose-stacktraces")),
        new ConfigBoolean(new OptionSpec("compiler.warn-array-tostring-changes")),
        new ConfigBoolean(new OptionSpec("compiler.warn-assignment-within-conditional")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-array-cast")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-bool-assignment")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-date-cast")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-es3-type-method")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-es3-type-prop")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-nan-comparison")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-null-assignment")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-null-comparison")),
        new ConfigBoolean(new OptionSpec("compiler.warn-bad-undefined-comparison")),
        new ConfigBoolean(new OptionSpec("compiler.warn-boolean-constructor-with-no-args")),
        new ConfigBoolean(new OptionSpec("compiler.warn-changes-in-resolve")),
        new ConfigBoolean(new OptionSpec("compiler.warn-class-is-sealed")),
        new ConfigBoolean(new OptionSpec("compiler.warn-const-not-initialized")),
        new ConfigBoolean(new OptionSpec("compiler.warn-constructor-returns-value")),
        new ConfigBoolean(new OptionSpec("compiler.warn-deprecated-event-handler-error")),
        new ConfigBoolean(new OptionSpec("compiler.warn-deprecated-function-error")),
        new ConfigBoolean(new OptionSpec("compiler.warn-deprecated-property-error")),
        new ConfigBoolean(new OptionSpec("compiler.warn-duplicate-argument-names")),
        new ConfigBoolean(new OptionSpec("compiler.warn-duplicate-variable-def")),
        new ConfigBoolean(new OptionSpec("compiler.warn-for-var-in-changes")),
        new ConfigBoolean(new OptionSpec("compiler.warn-import-hides-classes")),
        new ConfigBoolean(new OptionSpec("compiler.warn-instance-of-changes")),
        new ConfigBoolean(new OptionSpec("compiler.warn-internal-error")),
        new ConfigBoolean(new OptionSpec("compiler.warn-level-not-supported")),
        new ConfigBoolean(new OptionSpec("compiler.warn-missing-namespace-decl")),
        new ConfigBoolean(new OptionSpec("compiler.warn-negative-uint-literal")),
        new ConfigBoolean(new OptionSpec("compiler.warn-no-constructor")),
        new ConfigBoolean(new OptionSpec("compiler.warn-no-explicit-super-call-in-constructor")),
        new ConfigBoolean(new OptionSpec("compiler.warn-no-type-decl")),
        new ConfigBoolean(new OptionSpec("compiler.warn-number-from-string-changes")),
        new ConfigBoolean(new OptionSpec("compiler.warn-scoping-change-in-this")),
        new ConfigBoolean(new OptionSpec("compiler.warn-slow-text-field-addition")),
        new ConfigBoolean(new OptionSpec("compiler.warn-unlikely-function-value")),
        new ConfigBoolean(new OptionSpec("compiler.warn-xml-class-has-changed")),
        new ConfigBoolean(new OptionSpec("static-link-runtime-shared-libraries", "static-rsls")),
        new ConfigBoolean(new OptionSpec("skip-transpile")),
        new ConfigBoolean(new OptionSpec("verify-digests")),
        new ConfigBoolean(new OptionSpec("use-direct-blit")),
        new ConfigBoolean(new OptionSpec("use-gpu")),
        new ConfigBoolean(new OptionSpec("royale")),
        
        // String options
        new ConfigString(new OptionSpec("compiler.actionscript-file-encoding")),
        new ConfigString(new OptionSpec("compiler.context-root")),
        new ConfigString(new OptionSpec("compiler.defaults-css-url")),
        new ConfigString(new OptionSpec("compiler.locale")),
        new ConfigString(new OptionSpec("compiler.mxml.compatibility-version")),
        new ConfigString(new OptionSpec("compiler.services")),
        new ConfigString(new OptionSpec("compiler.targets")),
        new ConfigString(new OptionSpec("closure-lib")),
        new ConfigString(new OptionSpec("debug-password")),
        new ConfigString(new OptionSpec("dump-config")),
        new ConfigString(new OptionSpec("link-report")),
        new ConfigString(new OptionSpec("load-externs")),
        new ConfigString(new OptionSpec("output", "o")),
        new ConfigString(new OptionSpec("raw-metadata")),
        new ConfigString(new OptionSpec("resource-bundle-list")),
        new ConfigString(new OptionSpec("size-report")),
        new ConfigString(new OptionSpec("target-player")),
        new ConfigString(new OptionSpec("tools-locale")),
        new ConfigAppendString(new OptionSpec("configname")),
        
        // Int options
        new ConfigInt(new OptionSpec("default-background-color")),
        new ConfigInt(new OptionSpec("default-frame-rate")),
        new ConfigInt(new OptionSpec("swf-version"))		
	};
	
    private static final OptionSpec NAMESPACE =
    	new OptionSpec("compiler.namespaces.namespace");
    
    private static final OptionSpec LICENSE =
    	new OptionSpec("licenses.license");
    
    private static final OptionSpec EXTERNS =
    	new OptionSpec("externs");
    
    private static final OptionSpec INCLUDES =
    	new OptionSpec("includes");
    
    private static final OptionSpec INCLUDE_RESOURCE_BUNDLES =
    	new OptionSpec("include-resource-bundles", "ir");
    
    private static final OptionSpec RUNTIME_SHARED_LIBRARIES =
    	new OptionSpec("runtime-shared-libraries", "rsl");
    
    private static final OptionSpec FRAME =
    	new OptionSpec("frames.frame");

    private static final OptionSpec DEFINE =
    	new OptionSpec("compiler.define");
    
    private static final OptionSpec EXTERNAL_LIBRARY_PATH =
    	new OptionSpec("compiler.external-library-path", "el");
    
    private static final OptionSpec JS_EXTERNAL_LIBRARY_PATH =
    new OptionSpec("compiler.js-external-library-path");
    
    private static final OptionSpec SWF_EXTERNAL_LIBRARY_PATH =
    new OptionSpec("compiler.swf-external-library-path");
    
    private static final OptionSpec INCLUDE_LIBRARIES =
    	new OptionSpec("compiler.include-libraries");
    
    private static final OptionSpec LIBRARY_PATH =
    	new OptionSpec("compiler.library-path", "l");
    
    private static final OptionSpec JS_LIBRARY_PATH =
    new OptionSpec("compiler.js-library-path", "l");
    
    private static final OptionSpec SWF_LIBRARY_PATH =
    new OptionSpec("compiler.swf-library-path");
    
    private static final OptionSpec SOURCE_PATH =
    	new OptionSpec("compiler.source-path", "sp");
    
    private static final OptionSpec THEME =
    	new OptionSpec("compiler.theme");
    
    private static final OptionSpec LOAD_CONFIG =
    	new OptionSpec("load-config");
    
    private static final OptionSpec KEEP_AS3_METADATA =
    	new OptionSpec("compiler.keep-as3-metadata");
    
    private static final OptionSpec FORCE_RSLS = new OptionSpec(
    	"runtime-shared-library-settings.force-rsls");
    
    private static final OptionSpec APPLICATION_DOMAIN = new OptionSpec(
    	"runtime-shared-library-settings.application-domain", "rsl-domain");

    /*=======================================================================*
     * Variables
     *=======================================================================*/

    private String file;
    private String output;

    private Metadata metadata;
    private Fonts fonts;
    private DefaultScriptLimits defaultScriptLimits;
    private DefaultSize defaultSize;

    private final List<IOptionSource> nestedFileSets;

    /*=======================================================================*
     * Constructor.                                                          *
     *=======================================================================*/

    /**
     * Constructor.
     */
    public MXMLCTask()
    {
        super(TASK_NAME, CONFIG_VARIABLES, TOOL_JAR_FILE_NAME,
        	  TOOL_CLASS_NAME, TOOL_METHOD_NAME, TOOL_FAILURE_METHOD_NAME);
        
        nestedAttribs = new ArrayList<IOptionSource>();
        nestedFileSets = new ArrayList<IOptionSource>();
    }

    /*=======================================================================*
     * Required Attributes                                                   *
     *=======================================================================*/

    // file="..."
    
    public void setFile(String file)
    {
        this.file = file;
    }
    
    /*=======================================================================*
     * Other Attributes                                                      *
     *=======================================================================*/
    
    // output="..."
    
    /*
     * Necessary to override inherited setOutput method since ant gives
     * priority to parameter types more specific than String.
     */
    public void setOutput(File o)
    {
        setOutput(o.getAbsolutePath());
    }
    
    public void setOutput(String o)
    {
        this.output = o;
    }

    /*=======================================================================*
     *  Child Elements                                                       *
     *=======================================================================*/
    
    // <metadata>

    public Metadata createMetadata() 
    {
        if (metadata == null)
            return metadata = new Metadata();
        else
            throw new BuildException("Only one nested <metadata> element is allowed in an " + TASK_NAME + " task.");
    }
    
    // <fonts>

    public Fonts createFonts()
    {
        if (fonts == null)
            return fonts = new Fonts(this);
        else
            throw new BuildException("Only one nested <fonts> element is allowed in an " + TASK_NAME + " task.");
    }
    
    // <namespace>

    public NestedAttributeElement createNamespace()
    {
        return createElem(new String[] { "uri", "manifest" }, NAMESPACE);
    }
    
    // <license>

    public NestedAttributeElement createLicense()
    {
        return createElem(new String[] { "product", "serial-number" }, LICENSE);
    }
    
    // <externs>

    public NestedAttributeElement createExterns()
    {
        return createElem("symbol", EXTERNS);
    }
    
    // <includes>

    public NestedAttributeElement createIncludes()
    {
        return createElem("symbol", INCLUDES);
    }
    
    // <frame>

    public NestedAttributeElement createFrame()
    {
        return createElem(new String[] { "label", "classname" }, FRAME);
    }
    
    // other child elements

    public Object createDynamicElement(String name)
    {
        if (KEEP_AS3_METADATA.matches(name))
        {
            return createElem("name", KEEP_AS3_METADATA);            
        }
        else if (DEFINE.matches(name))
        {
            return createElem(new String[] { "name", "value" }, DEFINE);
        }
        else if (RUNTIME_SHARED_LIBRARIES.matches(name))
        {
            return createElem("url", RUNTIME_SHARED_LIBRARIES);
        }
        else if (RUNTIME_SHARED_LIBRARY_PATH.matches(name))
        {
            RuntimeSharedLibraryPath runtimeSharedLibraryPath = new RuntimeSharedLibraryPath();
            nestedAttribs.add(runtimeSharedLibraryPath);
            return runtimeSharedLibraryPath;
        }
        else if (LOAD_CONFIG.matches(name))
        {
        	return createElemAllowAppend(new String[] {"filename"} , LOAD_CONFIG);
        }
        else if (SOURCE_PATH.matches(name))
        {
            return createElem("path-element", SOURCE_PATH);
        }
        else if (DefaultScriptLimits.DEFAULT_SCRIPT_LIMITS.matches(name))
        {
            if (defaultScriptLimits == null)
                return defaultScriptLimits = new DefaultScriptLimits();
            else
                throw new BuildException("Only one nested <default-script-limits> element is allowed in an " + TASK_NAME + " task.");
        }
        else if (DefaultSize.DEFAULT_SIZE.matches(name))
        {
            if (defaultSize == null)
                return defaultSize = new DefaultSize();
            else
                throw new BuildException("Only one nested <default-size> element is allowed in an " + TASK_NAME + " task.");
        }
        else if (EXTERNAL_LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(EXTERNAL_LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (JS_EXTERNAL_LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(JS_EXTERNAL_LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (SWF_EXTERNAL_LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(SWF_EXTERNAL_LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (INCLUDE_LIBRARIES.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(INCLUDE_LIBRARIES, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (JS_LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(JS_LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (SWF_LIBRARY_PATH.matches(name))
        {
            FlexFileSet fs = new FlexSWCFileSet(SWF_LIBRARY_PATH, true);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (THEME.matches(name))
        {
            FlexFileSet fs = new FlexFileSet(THEME);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (INCLUDE_RESOURCE_BUNDLES.matches(name))
        {
            return createElem("bundle", INCLUDE_RESOURCE_BUNDLES);
        }
        else if (FORCE_RSLS.matches(name))
        {
            FlexFileSet fs = new FlexFileSet(FORCE_RSLS);
            nestedFileSets.add(fs);
            return fs;
        }
        else if (APPLICATION_DOMAIN.matches(name))
        {
            return createElem(new String[] { "path-element", "application-domain-target" }, APPLICATION_DOMAIN);            
        }

        return super.createDynamicElement(name);
    }

    /*=======================================================================*
     *  Execute and Related Functions                                        *
     *=======================================================================*/

    protected void prepareCommandline() throws BuildException
    {
        for (int i = 0; i < configVariables.length; i++)
        {
            configVariables[i].addToCommandline(cmdline);
        }

        if (metadata != null)
            metadata.addToCommandline(cmdline);

        if(fonts != null)
            fonts.addToCommandline(cmdline);

        if (defaultScriptLimits != null)
            defaultScriptLimits.addToCommandline(cmdline);

        if (defaultSize != null)
            defaultSize.addToCommandline(cmdline);

        Iterator<IOptionSource> it = nestedAttribs.iterator();

        while (it.hasNext())
        {
            ((IOptionSource)it.next()).addToCommandline(cmdline);
        }

        it = nestedFileSets.iterator();

        while (it.hasNext())
            ((IOptionSource)it.next()).addToCommandline(cmdline);
        
        if (output != null)
            (new ConfigString(new OptionSpec("output", "o"), output)).addToCommandline(cmdline);
        
        // end of arguments
        cmdline.createArgument().setValue("--");

        // file-DEFAULT_SCRIPT_LIMITS may not be specified if building, e.g. a resource bundle SWF
        if (file != null)
            cmdline.createArgument().setValue(file);
    }
}
