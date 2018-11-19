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

package org.apache.royale.compiler.internal.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.IProjectConfigVariables;
import org.apache.royale.compiler.internal.tree.as.ConfigConstNode;
import org.apache.royale.compiler.internal.tree.as.ConfigNamespaceNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Manager handles storing config variables passed in from the command line or from other project properties.
 * 
 * The manager also returns an {@link IProjectConfigVariables} class that contains information to be used by the parser to properly
 * take config variables into consideration.
 */
public final class ConfigManager
{
    /**
     * Internal impl of {@link IProjectConfigVariables}
     */
    private final class ProjectVariables implements IProjectConfigVariables
    {
        private CompilerProject project;
        private Map<String, String> configValues;
        private ArrayList<IDefinition> definitions;
        private ArrayList<ConfigConstNode> constNodes;
        private ArrayList<ConfigNamespaceNode> configNamespaces;
        private ArrayList<String> nsNames;
        private ArrayList<ICompilerProblem> problems;

        public ProjectVariables(CompilerProject project, Map<String, String> configValues) {
            this.project = project;
            this.configValues = configValues;
        }
        
        @Override
        public List<IDefinition> getRequiredDefinitions()
        {
            if(definitions == null) {
                //add required builtin definitions
                definitions = new ArrayList<IDefinition>(5);
                definitions.add(project.getScope().findDefinitionByName(IASLanguageConstants.Boolean));
                definitions.add(project.getScope().findDefinitionByName(IASLanguageConstants.String));
                definitions.add(project.getScope().findDefinitionByName(IASLanguageConstants.uint));
                definitions.add(project.getScope().findDefinitionByName(IASLanguageConstants._int));
                definitions.add(project.getScope().findDefinitionByName(IASLanguageConstants.Number));
            }
            return definitions;
        }
        
        /**
         * loads the config data when needed 
         */
        
        private synchronized void loadConfigData() {
            problems = new ArrayList<ICompilerProblem>();
            HashSet<String> nsNames = new HashSet<String>();
            constNodes = new ArrayList<ConfigConstNode>();
            configNamespaces = new ArrayList<ConfigNamespaceNode>();
            
            
            
            //------------------------------------------
            // First, build up a big string that is an actionscript
            // encoding of all the members of fConfigValues. These cannot be parsed
            // one at a time, becuase the value of one may refer to the value of another
            StringBuilder cn = new StringBuilder();
            Iterator<String> it = configValues.keySet().iterator();
            while(it.hasNext()) {
                String next = it.next();
                int q = next.indexOf("::");
                if(q == -1) {
                    continue; //log an error
                }
                //builds the AS3 code that corresponds to the config key value pair
                String subSequence = next.substring(0, q);
               // StringBuilder cn = new StringBuilder();
                cn.append(IASKeywordConstants.CONFIG);
                cn.append(" ");
                cn.append(IASKeywordConstants.NAMESPACE);
                cn.append(" ");
                cn.append(subSequence);
                cn.append(";");
                cn.append(subSequence);
                cn.append(" ");
                cn.append(IASKeywordConstants.CONST);
                cn.append(" ");
                cn.append(next.substring(q+2));
                cn.append("=");
                cn.append(configMapping.get(next));
                cn.append(";");
            }
            
            // Now that cn contains all the AS3 code for the config, we can parse it
            IASNode[] nodes = ASParser.parseProjectConfigVariables(project.getWorkspace(), cn.toString(), problems);
            for(IASNode node: nodes) {
                {
                if(node instanceof ConfigConstNode) {
                        constNodes.add((ConfigConstNode)node);
                    } else if(node instanceof ConfigNamespaceNode) {
                        if (!nsNames.contains( ((ConfigNamespaceNode)node).getName())) {
                            configNamespaces.add((ConfigNamespaceNode)node);
                            nsNames.add(((ConfigNamespaceNode)node).getName());
                        }
                    } else {
                        //log error
                    }
                }
            }
           
            this.nsNames = new ArrayList<String>(nsNames.size());
            this.nsNames.addAll(nsNames);
        }
        
        @Override
        public boolean equals(IProjectConfigVariables other)
        {
            if(other instanceof ProjectVariables) {
                return configValues.hashCode() == ((ProjectVariables)other).hashCode();
            }
            return other == this;
        }

        @Override
        public List<ConfigConstNode> getConfigVariables()
        {
            if(constNodes == null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager waiting for lock for loadConfigData");
                loadConfigData();
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager done with lock for loadConfigData");
            }
            return constNodes;
        }

        @Override
        public List<ConfigNamespaceNode> getConfigNamespaces()
        {
            if(configNamespaces == null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager waiting for lock for loadConfigData");
                loadConfigData();
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager done with lock for loadConfigData");
            }
            return configNamespaces;
        }

        @Override
        public List<String> getConfigNamespaceNames()
        {
            if(nsNames == null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager waiting for lock for loadConfigData");
                loadConfigData();
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager done with lock for loadConfigData");
            }
            return nsNames;
        }

        @Override
        public Collection<ICompilerProblem> getProblems()
        {
            if(problems == null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager waiting for lock for loadConfigData");
                loadConfigData();
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CONFIG_MANAGER) == CompilerDiagnosticsConstants.CONFIG_MANAGER)
            		System.out.println("ConfigManager done with lock for loadConfigData");
            }
            return problems;
        }

    }
    
    /**
     * List of config mappings
     */
    private LinkedHashMap<String, String> configMapping;
    /**
     * 
     * Flag indicating if we've collected errors on the project config variables
     */
    private boolean validated = false;

    public ConfigManager() {
        configMapping = new LinkedHashMap<String, String>();
    }
    
    /**
     * Adds a config variable and expression to this mapping
     * @param namespace The config variable.
     * @param expression The expression.
     */
    public void addConfigVariable(String namespace, String expression) {
        configMapping.put(namespace, expression);
        validated = false;
    }
    
    /**
     * Syntax:<br/>
     * <code>-define=&lt;name&gt;,&lt;value&gt;</code>
     * where name is <code>NAMESPACE::name</code> and value is a legal definition value
     * (e.g. <code>true</code> or <code>1</code> or <code>!CONFIG::debugging</code>)
     *
     * Example: <code>-define=CONFIG::debugging,true</code>
     *
     * In <code>royale-config.xml</code>:<br/>
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
     * Values are ActionScript expressions that must coerce and evaluate to constants at compile-time.
     * Effectively, they are replaced in AS code, verbatim, so <code>-define=TEST::oneGreaterTwo,"1>2"</code>
     * will getCompiler coerced and evaluated, at compile-time, to <code>false</code>.
     *
     * It is good practice to wrap values with double-quotes,
     * so that MXMLC correctly parses them as a single argument:<br/>
     * <code>-define=TEST::oneShiftRightTwo,"1 >> 2"</code>
     *
     * Values may contain compile-time constants and other configuration values:<br/>
     * <code>-define=CONFIG::bool2,false -define=CONFIG::and1,"CONFIG::bool2 && false" TestApp.mxml</code>
     *
     * String values on the command-line <i>must</i> be surrounded by double-quotes, and either
     * escape-quoted (<code>"\"foo\""</code> or <code>"\'foo\'"</code>) or single-quoted
     * (<code>"'foo'"</code>).
     *
     * String values in configuration files need only be single- or double- quoted:<br/>
     * <pre>
     * <royale-config>
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
     * </royale-config>
     * </pre>
     *
     * Empty strings <i>must</i> be passed as <code>"''"</code> on the command-line, and
     * <code>''</code> or <code>""</code> in configuration files.
     * 
     * Finally, if you have existing definitions in a configuration file, and you would
     * like to add to them with the command-line (let's say most of your build setCompilertings
     * are in the configuration, and that you are adding one temporarily using the
     * command-line), you use the following syntax:
     * <code>-define+=TEST::temporary,false</code> (noting the plus sign)
     * 
     * Note that definitions can be overridden/redefined if you use the append ("+=") syntax
     * (on the commandline or in a user config file, for instance) with the same namespace
     * and name, and a new value.
     * 
     * Definitions cannot be removed/undefined. You can undefine ALL existing definitions
     * from (e.g. from royale-config.xml) if you do not use append syntax ("=" or append="false").
     * 
     * IMPORTANT FOR FLEXBUILDER
     * If you are using "Additional commandline arguments" to "-define", don't use the following
     * syntax though I suggest it above:
     *     -define+=CONFIG::foo,"'value'"
     * The trouble is that FB parses the double quotes incorrectly as <"'value'> -- the trailing
     * double-quote is dropped. The solution is to avoid inner double-quotes and put them around the whole expression:
     *    -define+="CONFIG::foo,'value'"
     * @param variables the mapping of config names to expressions
     */
    public void addConfigVariables(Map<String, String> variables) {
        configMapping.putAll(variables);
        validated = false;
    }
    
    /**
     * Returns the {@link IProjectConfigVariables} object for the given project.  Each value returned will be a new object, but could possibly share
     * the same signature as other variables
     * @param project the {@link ICompilerProject} to pull our config data from
     * @return an {@link IProjectConfigVariables} object
     */
    public IProjectConfigVariables getProjectConfig(CompilerProject project) {
        LinkedHashMap<String, String> configValues = new LinkedHashMap<String, String>();
        configValues.putAll(configMapping);
        ProjectVariables vars = new ProjectVariables(project, configValues);
        if(!validated) {
            vars.getProblems();
            validated = true;
        }
        return vars;
    }
}
