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

package org.apache.flex.compiler.clients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.clients.MXMLJSC.JSOutputType;
import org.apache.flex.compiler.clients.MXMLJSC.JSTargetType;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationValue;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.flex.compiler.internal.config.annotations.Arguments;
import org.apache.flex.compiler.internal.config.annotations.Config;
import org.apache.flex.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.flex.compiler.internal.config.annotations.Mapping;
import org.apache.flex.compiler.internal.config.annotations.SoftPrerequisites;

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
    @Arguments("type")
    @InfiniteArguments
    public void setCompilerTargets(ConfigurationValue cv, String[] targetlist)
    {
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
    // 'compiler.js-external-library-path' option
    //

    private final List<String> jsexternalLibraryPath = new ArrayList<String>();

    @Override
    public List<String> getCompilerExternalLibraryPath()
    {
    	if (jsexternalLibraryPath.size() > 0)
    		return jsexternalLibraryPath;
    	return super.getCompilerExternalLibraryPath();
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

    @Override
    public List<String> getCompilerLibraryPath()
    {
    	if (jslibraryPath.size() > 0)
    		return jslibraryPath;
    	return super.getCompilerLibraryPath();
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
    	if (jsconfigVars.size() > 0)
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

    @Override
    protected String overrideDefinedValue(String name, String value)
    {
    	if (name.equals("COMPILE::SWF") && value.equals("AUTO"))
    		return "false";
    	if (name.equals("COMPILE::JS") && value.equals("AUTO"))
    		return "true";
    	return value;
    }

}
