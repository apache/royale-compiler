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

package org.apache.royale.compiler.clients;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.clients.MXMLJSC.JSTargetType;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.RoyaleOnly;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.internal.config.annotations.SoftPrerequisites;
import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;

import com.google.common.collect.ImmutableList;

/**
 * The {@link JSConfiguration} class holds all compiler arguments needed for
 * compiling ActionScript to JavaScript.
 * <p>
 * Specific flags are implemented here for the configuration to be loaded by the
 * configure() method of {@link MXMLJSC}.
 * <p>
 * This class inherits all compiler arguments from the MXMLC compiler.
 *
 * @author Michael Schmalle
 */
public class JSConfiguration extends Configuration
{
    public JSConfiguration()
    {
        //for Royale projects, we want to make some changes to the compiler
        //defaults. we do that here in JSConfiguration instead of Configuration.
        //despite its name, JSConfiguration is used for SWF compilation too.

        //we try to keep the defaults of the Configuration base class backwards
        //compatible with other compilers, like the one in the Flex SDK. this
        //policy helps IDEs to use the Royale compiler for code intelligence
        //with other SDKs without requiring the IDEs to "undo" Royale's changes
        //to defaults.
        setCompilerAllowAbstractClasses(null, true);
        setCompilerAllowPrivateConstructors(null, true);
        setCompilerAllowImportAliases(null, true);
        setCompilerStrictIdentifierNames(null, false);
    }

    //
    // 'compiler.targets' option
    //

    protected final List<String> targets = new ArrayList<String>();

    public List<String> getCompilerTargets()
    {
    	if (targets.size() == 0)
    		targets.add(JSTargetType.JS_ROYALE.getText());
        return targets;
    }

    /**
     * The list of compiler outputs to generate
     */
    @Config(allowMultiple = true, isPath = false)
    @Mapping({ "compiler", "targets" })
    @Arguments("target")
    @InfiniteArguments
    public void setCompilerTargets(ConfigurationValue cv, String[] targetlist)
    {
        targets.clear();
    	for (String target : targetlist)
    		targets.add(target);
    }

    //
    // 'js-output-type'
    //

    @Config
    @Mapping("js-output-type")
    public void setJSOutputType(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
    	// ignore if set via compiler.targets
    	if (targets.size() > 0) return;
        targets.clear();
        targets.add(value);
    }

    //
    // 'source-map'
    //

    private boolean sourceMap = false;

    public boolean getSourceMap()
    {
        return sourceMap;
    }

    @Config
    @Mapping("source-map")
    public void setSourceMap(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        sourceMap = value;
    }

    //
    // 'js-default-initializers'
    //

    private boolean jsDefaultInitializers = true;

    public boolean getJsDefaultInitializers()
    {
        return jsDefaultInitializers;
    }

    @Config
    @Mapping("js-default-initializers")
    public void setJsDefaultInitializers(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        jsDefaultInitializers = value;
    }

    //
    // 'js-dynamic-access-unknown-members'
    //

    private boolean jsDynamicAccessUnknownMembers = false;

    public boolean getJsDynamicAccessUnknownMembers()
    {
        return jsDynamicAccessUnknownMembers;
    }

    /**
     * If the definition of a member cannot be resolved, emit dynamic access
     * instead of normal member access. Ensures that dynamic members aren't
     * renamed.
     *
     * <code>myObject.memberAccess</code> becomes <code>myObject["memberAccess"]</code>
     */
    @Config
    @Mapping("js-dynamic-access-unknown-members")
    public void setJsDynamicAccessUnknownMembers(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        jsDynamicAccessUnknownMembers = value;
    }

    //
    // 'compiler.js-external-library-path' option
    //

    private final List<String> jsexternalLibraryPath = new ArrayList<String>();

    public List<String> getCompilerJsExternalLibraryPath()
    {
    	return jsexternalLibraryPath;
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "js-external-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setCompilerJsExternalLibraryPath(ConfigurationValue cv, String[] pathlist) throws ConfigurationException
    {
        final ImmutableList<String> pathElements = ImmutableList.copyOf(pathlist);
        final ImmutableList<String> resolvedPaths = expandTokens(pathElements, locales, cv,
                !reportMissingCompilerLibraries);
        jsexternalLibraryPath.addAll(resolvedPaths);
    }

    //
    // 'compiler.js-library-path' option
    //

    private final List<String> jslibraryPath = new ArrayList<String>();

    public List<String> getCompilerJsLibraryPath()
    {
    	return jslibraryPath;
    }

    /**
     * Links SWC files to the resulting application SWF file. The compiler only links in those classes for the SWC file
     * that are required. You can specify a directory or individual SWC files.
     */
    @Config(allowMultiple = true, isPath = true)
    @Mapping({ "compiler", "js-library-path" })
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    @SoftPrerequisites({ "locale", "target-player", "exclude-native-js-libraries" })
    public void setCompilerJsLibraryPath(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        final ImmutableList<String> resolvedPaths = expandTokens(Arrays.asList(pathlist), locales, cv,
                !reportMissingCompilerLibraries);
        jslibraryPath.addAll(resolvedPaths);
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
     *          <value>"Royale 4.8.0"</value>
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
     * IMPORTANT FOR FLASH BUILDER If you are using "Additional commandline arguments" to "-define", don't use the
     * following syntax though I suggest it above: -define+=CONFIG::foo,"'value'" The trouble is that FB parses the
     * double quotes incorrectly as <"'value'> -- the trailing double-quote is dropped. The solution is to avoid inner
     * double-quotes and put them around the whole expression: -define+="CONFIG::foo,'value'"
     */
    private Map<String, String> jsconfigVars;

    /**
     * @return A list of ConfigVars
     */
    public Map<String, String> getJsCompilerDefine()
    {
    	if (jsconfigVars != null)
    		return jsconfigVars;
    	return super.getCompilerDefine();
    }

    @Config(advanced = true, allowMultiple = true)
    @Arguments({ "name", "value" })
    public void setJsCompilerDefine(ConfigurationValue cv, String name, String value) throws ConfigurationException
    {
        if (jsconfigVars == null)
        	jsconfigVars = new LinkedHashMap<String, String>();

        jsconfigVars.put(name, value);
    }

    //
    // 'output' option
    //

    private String jsoutput;

    @Override
    public String getOutput()
    {
    	if (jsoutput != null)
    		return jsoutput;
    	return super.getOutput();
    }

    @Config
    @Arguments("filename")
    public void setJsOutput(ConfigurationValue val, String output) throws ConfigurationException
    {
        this.jsoutput = getOutputPath(val, output);
    }

    /**
     * @return JS equivalent of -load-config
     */
    public String getJsLoadConfig()
    {
    	return null;
    }

    //
    // 'module-output' option
    //

    private String moduleoutput;

    /**
     * if used, the js-debug and js-release folders are calculated by removing
     * the folders specified from the output folder.  This is useful in some
     * cases when using module that are in the same source path as the main app
     * as opposed to being in separate projects.  For example in TourDeFlex,
     * the main app is in the src folder, and a module example may be in
     * src/mx/controls/ such as mx/controls/ButtonExample.mxml.
     * Without this options, the output might end up in
     * src/mx/controls/bin/js-debug and src/mx/controls/bin/js-release when
     * it would be better if the output was relative to the main app and go
     * in bin/js-debug/mx/controls and bin/js-release/mx/controls.  Even
     * specifying js-output doesn't work as setting it to the main app's
     * bin folder would result in the output .JS going in the same folder
     * as the main app instead of being nested in mx/controls.  So, by
     * setting this option to mx/controls, the compiler will calculate the desired
     * folder structure.
     */
    public String getModuleOutput()
    {
    	if (moduleoutput != null && moduleoutput.equals("/"))
    		return null;
    	return moduleoutput == null ? null : moduleoutput.replace("/", File.separator);
    }

    @Config
    @Arguments("filename")
    public void setModuleOutput(ConfigurationValue val, String output) throws ConfigurationException
    {
        this.moduleoutput = output;
    }

    /**
     * Placeholder.  MXMLJSC picks off these values and changes them to load-config for the JS compilers
     */
    @Config(allowMultiple = true)
    @Arguments("filename")
    public void setJsLoadConfig(ConfigurationValue cv, String filename) throws ConfigurationException
    {
    
    }
    
    //////////////////////////////////////////////////////////////////////////
    // compiler.js-namespaces
    //////////////////////////////////////////////////////////////////////////

    private List<MXMLNamespaceMapping> jsmanifestMappings;

    public List<MXMLNamespaceMapping> getCompilerJsNamespacesManifestMappings()
    {
        return jsmanifestMappings;
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
    @Mapping({ "compiler", "js-namespaces", "namespace" })
    @Arguments({ "uri", "manifest" })
    @InfiniteArguments
    @RoyaleOnly
    public void setCompilerJsNamespacesNamespace(ConfigurationValue cfgval, List<String> args)
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

        if (jsmanifestMappings == null)
            jsmanifestMappings = new ArrayList<MXMLNamespaceMapping>();

        for (int i = 0; i < args.size() - 1; i += 2)
        {
            final String uri = args.get(i);
            final String manifestFile = args.get(i + 1);
            final String path = resolvePathStrict(manifestFile, cfgval);
            jsmanifestMappings.add(new MXMLNamespaceMapping(uri, path));
        }
    }

    //
    // 'js-vector-emulation-class' option
    //

    private String jsVectorEmulationClass = null;

    public String getJsVectorEmulationClass()
    {
        return jsVectorEmulationClass;
    }

    /**
     * The class to use instead of default Vector implementation for handling Vector.
     */
    @Config(advanced = true)
    public void setJsVectorEmulationClass(ConfigurationValue cv, String b)
    {
    	jsVectorEmulationClass = b;
    }
    
    
    //
    // 'js-complex-implicit-coercions'
    //
    
    private boolean jsComplexImplicitCoercions = true;
    
    public boolean getJsComplexImplicitCoercions()
    {
        return jsComplexImplicitCoercions;
    }
    
    /**
     * Support for including/avoiding more complex implicit assignment coercions
     * example
     * var array:Array = [new MyClass()];
     * var myOtherClass:MyOtherClass = array[0];
     *
     * In the above example, the compiler will (by default) output an implicit coercion
     * that is equivalent in actionscript to:
     * var myOtherClass:MyOtherClass = MyOtherClass(array[0]);
     *
     * By setting this configuration option to false, the implicit coercion code in situations similar to the above
     * is not generated (other primitive implicit coercions, such as int/uint/Number/String and Boolean coercions remain)
     * This is a global setting for the current source code being compiled, it is possible to leave it on and specifically avoid it via doc
     * settings. The doc comment compiler directive for that is: @royalesuppresscompleximplicitcoercion
     * Another option is to add the explicit coercions in code and then avoid their output
     * via specific @royaleignorecoercion doc comment directives. Doing so however may add extra unwanted output
     * in other compiler targets (for example, swf bytecode) if the same source code is shared between targets.
     */
    @Config(advanced = true)
    @Mapping("js-complex-implicit-coercions")
    public void setJsComplexImplicitCoercions(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        jsComplexImplicitCoercions = value;
    }
    
    //
    // 'js-resolve-uncertain'
    //
    
    private boolean jsResolveUncertain = true;
    
    public boolean getJsResolveUncertain()
    {
        return jsResolveUncertain;
    }
    
    /**
     * Support for avoiding more overhead of resolving instantiations from
     * unknown constructors
     * example
     * var myClass:Class = String;
     * var myString:* = new myClass("test");
     *
     * In the above example, the compiler will (by default) output
     * a call to a Language.resolveUncertain method which wraps the 'new myClass("test")'
     *
     *
     * This normalizes the return value for some primitive constructors, so that (for example)
     * strict equality and inequality operators provide the same results between compiler
     * targets.
     * In situations where it is certain that the resolveUncertain method is not needed,
     * this option provides a way to switch it off 'globally' for the current source code being compiled.
     * It can also be switched off or on locally using the '@royalesuppressresolveuncertain'
     * doc comment compiler directive.
     */
    @Config(advanced = true)
    @Mapping("js-resolve-uncertain")
    public void setJsResolveUncertain(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        jsResolveUncertain = value;
    }
    
    //
    // 'js-vector-index-checks'
    //
    
    private boolean jsVectorIndexChecks = true;
    
    public boolean getJsVectorIndexChecks()
    {
        return jsVectorIndexChecks;
    }
    
    /**
     * Support for avoiding more overhead of adding checks into
     * assignments via Vector index access
     * example
     * var myVector:Vector.<int> = new Vector.<int>();
     * myVector[0] = 42;
     *
     * In the above example, the compiler will (by default) wrap
     * the '0' inside myVector[0] with a method call on the vector instance
     * that checks to see if the index is valid for the Vector it is being used against
     *
     * This check will throw an error if the index is out of range, and the
     * range checking differs if the Vector is 'fixed' or non-'fixed'
     *
     * In situations where it is certain that the index will always be valid for Vector instance
     * being targeted, or where all cases in a given codebase are certain to be valid, it is possible
     * to avoid the overhead of this check. This is especially important in loops.
     * This config setting affects the global setting for the current source code being compiled.
     * It can be adjusted locally within code, using the '@royalesuppressvectorindexcheck'
     * doc comment compiler  directive.
     */
    @Config(advanced = true)
    @Mapping("js-vector-index-checks")
    public void setJsVectorIndexChecks(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        jsVectorIndexChecks = value;
    }
    
    
    //
    // 'jsx-factory'
    //
    
    private String jsxFactory = "React.createElement";
    
    public String getJsxFactory()
    {
        return jsxFactory;
    }
    
    /**
     * Customize the factory to use for JSX. Defaults to React.createElement
     */
    @Config(advanced = true)
    @Mapping("jsx-factory")
    public void setJsxFactory(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
        jsxFactory = value;
    }
    
}
