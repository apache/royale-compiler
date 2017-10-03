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
import org.apache.royale.compiler.internal.config.annotations.FlexOnly;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
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
    }

    //
    // 'compiler.targets' option
    //

    protected final List<String> targets = new ArrayList<String>();

    public List<String> getCompilerTargets()
    {
    	if (targets.size() == 0)
    		targets.add(JSTargetType.JS_FLEX.getText());
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

    private boolean jsDefaultInitializers = false;

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
     * In <code>flex-config.xml</code>:<br/>
     * 
     * <pre>
     * <flex-config>
     *    <compiler>
     *       <define>
     *          <name>CONFIG::debugging</name>
     *          <value>true</value>
     *       </define>
     *       ...
     *    </compile>
     * </flex-config>
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
     * <flex-config>
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
     * </flex-config>
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
     * flex-config.xml) if you do not use append syntax ("=" or append="false").
     * 
     * IMPORTANT FOR FLEXBUILDER If you are using "Additional commandline arguments" to "-define", don't use the
     * following syntax though I suggest it above: -define+=CONFIG::foo,"'value'" The trouble is that FB parses the
     * double quotes incorrectly as <"'value'> -- the trailing double-quote is dropped. The solution is to avoid inner
     * double-quotes and put them around the whole expression: -define+="CONFIG::foo,'value'"
     */
    private Map<String, String> jsconfigVars;

    /**
     * @return A list of ConfigVars
     */
    @Override
    public Map<String, String> getCompilerDefine()
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
    @FlexOnly
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

}
