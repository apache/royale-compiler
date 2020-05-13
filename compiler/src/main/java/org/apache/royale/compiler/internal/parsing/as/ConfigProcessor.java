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

package org.apache.royale.compiler.internal.parsing.as;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import antlr.Token;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.parsing.as.ConfigCompilationUnit.ConfigFileNode;
import org.apache.royale.compiler.internal.parsing.as.ConfigProcessor.ConfigProject.ConfigProjectScope;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.ConfigConstNode;
import org.apache.royale.compiler.internal.tree.as.ConfigExpressionNode;
import org.apache.royale.compiler.internal.tree.as.ConfigNamespaceNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.CannotResolveConfigExpressionProblem;
import org.apache.royale.compiler.problems.CannotResolveProjectLevelConfigExpressionProblem;
import org.apache.royale.compiler.problems.ConflictingNameInNamespaceProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.NonConstantConfigInitProblem;
import org.apache.royale.compiler.problems.UndefinedConfigNamespaceProblem;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.Iterables;

/**
 * Processor handles config information found by the parser and that is set on
 * the current project
 */
public class ConfigProcessor
{
    /**
     * Used as a fake project to facilitate type lookup
     */
    final class ConfigProject extends org.apache.royale.compiler.internal.projects.ASProject
    {
        /**
         * Scope that allows direct addition of IDefinitions without adding
         * compilation units
         */
        final class ConfigProjectScope extends ASProjectScope
        {
            /**
             * @param project
             */
            private ConfigProjectScope(CompilerProject project)
            {
                super(project);
            }

            public void addConfigDefinition(IDefinition definition)
            {
                addDefinitionToStore(definition);
            }
        }

        private ConfigProject(IWorkspace w)
        {
            super((Workspace)w, true);
            getSourceCompilationUnitFactory().addHandler(new ISourceFileHandler()
            {

                @Override
                public String[] getExtensions()
                {
                    return new String[] {"config"};
                }

                @Override
                public ICompilationUnit createCompilationUnit(CompilerProject project, String path,
                        BasePriority priority, int order, String qname, String locale)
                {
                    return new ConfigCompilationUnit(backingProject, path);
                }

                @Override
                public boolean needCompilationUnit(CompilerProject project, String path, String qname, String locale)
                {
                    return true;
                }

                @Override
                public boolean canCreateInvisibleCompilationUnit()
                {
                    return false;
                }
            });
        }

        @Override
        protected ASProjectScope initProjectScope(CompilerProject project)
        {
            return new ConfigProjectScope(project);
        }

        @Override
        public ConfigProjectScope getScope()
        {
            return (ConfigProjectScope)super.getScope();
        }

        public void removeCU(ConfigCompilationUnit cu)
        {
            removeCompilationUnits(Collections.<ICompilationUnit> singletonList(cu));

        }

		@Override
		public boolean getAllowPrivateNameConflicts() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getAllowImportAliases() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getAllowAbstractClasses() {
			// TODO Auto-generated method stub
			return false;
		}

        @Override
        public boolean getAllowPrivateConstructors() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean getStrictIdentifierNames() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    /**
     * Scope block that we add all of our config information to
     */
    private ScopedBlockNode configScope;

    private BaseASParser parser;

    /**
     * Flag to keep track of if we should report errors or not If we're
     * transferring constants from the project to an individual config
     * processor, then we don't need to report the errors as they were already
     * reported as errors on the project.
     */
    private boolean transferringConstants;

    /**
     * Fake project we use to faciliate type lookup for expressions
     */
    private ConfigProject backingProject;

    /**
     * Project variables
     */
    private IProjectConfigVariables variables;

    /**
     * Our fake config compilation unit we use to make the type/project system
     * happy
     */
    private ConfigCompilationUnit configUnit;

    private HashSet<String> configNames;

    private final IWorkspace workspace;

    ConfigProcessor(IWorkspace workspace, BaseASParser parser)
    {
        this.parser = parser;
        configNames = new HashSet<String>();
        configNames.add(IASLanguageConstants.DEFAULT_CONFIG_NAME);
        this.workspace = workspace;
    }

    /**
     * Sets the {@link IProjectConfigVariables} that will be used to alter the
     * shape of the tree we are building
     * 
     * @param variables {@link IProjectConfigVariables} or null
     */
    public void connect(IProjectConfigVariables variables)
    {
        this.variables = variables;
        if (variables != null)
        {
            List<String> namespaces = variables.getConfigNamespaceNames();
            for (String namespace : namespaces)
            {
                configNames.add(namespace);
            }
        }
    }

    /**
     * Disconnects us from the parser, cleaning up any state that was built
     */
    public void disconnect()
    {
        if (configUnit != null)
            backingProject.removeCU(configUnit);

        if (backingProject != null)
            backingProject = null;

        parser = null;
    }

    public final boolean isConfigNamespace(final String name)
    {
        return configNames.contains(name);
    }

    private final void initConfigStructures()
    {
        if (backingProject == null)
        {
            backingProject = new ConfigProject(workspace);

            String configFileName = FilenameNormalization.normalize("config" + Integer.toString(hashCode()) + ".config");
            try
            {
                backingProject.setIncludeSources(new File[] {new File(configFileName)});
            }
            catch (InterruptedException e1)
            {
                //ignore this
            }
            Collection<ICompilationUnit> units = backingProject.getCompilationUnits(configFileName);
            assert units.size() == 1 && Iterables.getOnlyElement(units) instanceof ConfigCompilationUnit;
            try
            {
                transferringConstants = true;

                ISyntaxTreeRequestResult treeResult = Iterables.getOnlyElement(units).getSyntaxTreeRequest().get();
                configScope = ((ConfigFileNode)treeResult.getAST()).getTargetConfigScope();
                addConditionalCompilationNamespace(new ConfigNamespaceNode(new IdentifierNode(IASLanguageConstants.DEFAULT_CONFIG_NAME, (Token)null)));
                if (variables != null)
                {
                    if (variables != null)
                    {
                        List<IDefinition> definitions = variables.getRequiredDefinitions();
                        ConfigProjectScope scope = backingProject.getScope();
                        for (IDefinition def : definitions)
                        {
                            if (def != null && scope.getLocalDefinitionSetByName(def.getQualifiedName()) == null)
                            {
                                scope.addConfigDefinition(def);
                            }
                        }
                    }
                    List<ConfigNamespaceNode> namespaces = variables.getConfigNamespaces();
                    for (ConfigNamespaceNode ns : namespaces)
                    {
                        addConditionalCompilationNamespace(ns);
                    }
                    List<ConfigConstNode> vars = variables.getConfigVariables();
                    for (ConfigConstNode var : vars)
                    {
                        addConfigConstNode(var);
                    }
                }
            }
            catch (InterruptedException e)
            {
                ICompilerProblem problem = new InternalCompilerProblem2(parser.getFilename(), e, "ConfigProcessor");
                addProblem(problem);
            }
            finally
            {
                transferringConstants = false;
            }
        }
    }

    /**
     * Returns any children created by this config processor
     * 
     * @return children, or an empty array
     */
    public IASNode[] getConfigChildren()
    {
        if (configScope != null)
        {
            final int childCount = configScope.getChildCount();
            ArrayList<IASNode> children = new ArrayList<IASNode>(childCount);
            for (int i = 0; i < childCount; i++)
            {
                NodeBase child = (NodeBase)configScope.getChild(i);
                ((NodeBase)child).setParent(null);
                if (child instanceof ConfigConstNode)
                {
                    ((ConfigConstNode)child).reset();
                }
                children.add(child);
            }
            return children.toArray(new IASNode[0]);
        }
        return new IASNode[0];
    }

    /**
     * Adds a name that is recognized as a config name for conditional
     * compilation
     */
    public boolean addConditionalCompilationNamespace(NamespaceNode node)
    {
        initConfigStructures();
        IDefinitionSet name = configScope.getASScope().getLocalDefinitionSetByName(node.getName());
        if (name != null)
        {
            //allow redefinition of config namespace - matches ASC
            return true;
        }
        else
        {
            node.normalize(true);
            configScope.addItemAfterNormalization(node);
            configScope.getASScope().addDefinition(node.getDefinition());
            configNames.add(node.getName());
            return true;
        }
    }

    /**
     * Adds a {@link ConfigConstNode} to our internal tree. Will return true if
     * this item is unique
     * 
     * @param node the {@link ConfigConstNode} that we have encountered while
     * parsing.
     * @return true if this is not a redefinition of a config name
     */
    public boolean addConfigConstNode(ConfigConstNode node)
    {
        initConfigStructures();
        node.normalize(true);
        IdentifierNode configNamespaceNode = (IdentifierNode)node.getNamespaceNode();
        IDefinitionSet set = configScope.getASScope().getLocalDefinitionSetByName(configNamespaceNode.getName());
        if (set == null)
        {
            IIdentifierNode namespaceNode = (IdentifierNode)node.getNamespaceNode();
            ICompilerProblem problem = new UndefinedConfigNamespaceProblem(namespaceNode, namespaceNode.getName());
            addProblem(problem);
            return false;
        }

        configScope.addItemAfterNormalization(node);
        DefinitionBase constDef = node.getDefinition();
        configScope.getASScope().addDefinition(constDef);
        if (constDef instanceof ConstantDefinition)
        {
            ConstantDefinition def = (ConstantDefinition)constDef;
            Object value = def.resolveValue(backingProject);
            if (value == ConfigConstNode.UNKNOWN_VALUE)
            {
                if (def instanceof ConfigConstNode.ConfigDefinition)
                {
                	ConfigConstNode.ConfigDefinition cdef = (ConfigConstNode.ConfigDefinition)def;
    	        	IExpressionNode initializer = cdef.getInitializer();
    	        	if (initializer.getNodeID() != ASTNodeID.MemberAccessExpressionID &&
    	        			initializer.getNodeID() != ASTNodeID.IdentifierID)
    	        	{
    	                // Get the real source node for the problem.
    	                // If there isn't one, then don't make a problem - assume 
    	                // someone else already found the cause and logged it.
    	                IASNode problemLocationNode = node.getAssignedValueNode();
    	                if (problemLocationNode != null)
    	                {
    	                    ICompilerProblem problem = new NonConstantConfigInitProblem(
    	                            problemLocationNode);
    	                    addProblem(problem);
    	                }
    	        	}
            	}
            }
        }
        // Check for redeclaration
        // Config vars don't care about MultiDefinitionType.MANY vs. AMBIGUOUS
        // and it's not possible for them to shadow params, so we only have to check if
        // the multi type is not NONE
        if (SemanticUtils.getMultiDefinitionType(constDef, backingProject) != SemanticUtils.MultiDefinitionType.NONE)
        {
            addProblem(new ConflictingNameInNamespaceProblem(node, constDef.getBaseName(), node.getNamespace()));
            return false;
        }
        return true;
    }

    /**
     * Helper method to add a problem
     * 
     * @param problem Problem to add
     */
    private void addProblem(ICompilerProblem problem)
    {
        // If we're transferring the constants to a particular parser, then
        // the problems have already been reported via the project
        // don't report them again to avoid dupes
        if (!transferringConstants)
            parser.addProblem(problem);
    }

    /**
     * turns a config expression node into a Java Interger, Boolean, etc...
     * creates a compiler problem is unable to evaluate
     */
    protected Object evaluateConstNodeExpressionToJavaObject(ConfigExpressionNode node)
    {
        initConfigStructures();
        node.normalize(true);
        configScope.addItemAfterNormalization(node);
        Object result = node.resolveConfigValue(backingProject);
        if (result == null || result == ConfigConstNode.UNKNOWN_VALUE)
        {
        	// try to allow simple memberaccessexpressions as well (a.b)
            IDefinition definition = node.resolve(backingProject);
            if (definition instanceof ConfigConstNode.ConfigDefinition)
            {
            	ConfigConstNode.ConfigDefinition def = (ConfigConstNode.ConfigDefinition)definition;
	        	IExpressionNode initializer = def.getInitializer();
	        	if (initializer.getNodeID() == ASTNodeID.MemberAccessExpressionID ||
		        	initializer.getNodeID() == ASTNodeID.IdentifierID)
	        		return initializer;
        	}
        	
            // If we can't get a value, log an error. If we are are processing System variables, then don't
            // use the problem type that requires a "site", as we don't know what it is
            ICompilerProblem problem = isFromProjectConfigVariables() ?
                    new CannotResolveProjectLevelConfigExpressionProblem(node.getConfigValue()) :
                    new CannotResolveConfigExpressionProblem(node, node.getConfigValue());
            addProblem(problem);
        }
        return result;
    }

    /**
     * Returns true if we are this config processor was is helping to parse
     * project variables, like those from the command line.
     */
    private boolean isFromProjectConfigVariables()
    {
        return this.parser.isParsingProjectConfigVariables();
    }

    /**
     * turns a config expression into a synthesize LiteralNode
     */
    protected IASNode evaluateConstNodeExpression(ConfigExpressionNode node)
    {

        final Object result = evaluateConstNodeExpressionToJavaObject(node);

        if (result instanceof Boolean)
        {
            LiteralNode literalNode = new LiteralNode(LiteralType.BOOLEAN, ((Boolean)result).toString());
            literalNode.setSynthetic(true);
            return literalNode;
        }
        else if (result instanceof String)
        {
            LiteralNode literalNode = new LiteralNode(LiteralType.STRING, (String)result);
            literalNode.setSynthetic(true);
            return literalNode;
        }
        else if (result instanceof Double ||
                 result instanceof Integer ||
                 result instanceof Long)
        {
            LiteralNode literalNode = new NumericLiteralNode(result.toString());
            literalNode.setSynthetic(true);
            return literalNode;
        }
        else if (result == ABCConstants.NULL_VALUE)
        {
            // Handle 'null'
            LiteralNode literalNode = new LiteralNode(LiteralType.NULL, IASLanguageConstants.Null);
            literalNode.setSynthetic(true);
            return literalNode;
        }
        else if (result instanceof MemberAccessExpressionNode)
        {
        	MemberAccessExpressionNode mae = (MemberAccessExpressionNode)result;
        	return mae;
        }
        else if (result instanceof IdentifierNode)
        {
        	IdentifierNode id = (IdentifierNode)result;
        	return id;
        }
        else if (result instanceof Namespace)
        {
        	Namespace ns = (Namespace)result;
        	String nsName = ns.getName();
        	if (nsName.length() == 0)
        		nsName = "public";
        	
        	return new IdentifierNode(nsName);
        }
        return null;
    }

    /**
     * Bind the configurations to a parser.
     * 
     * @param parser AS parser.
     */
    public final void setParser(BaseASParser parser)
    {
        this.parser = parser;
    }

    /**
     * Detach the parser if it's currently bound to this processor.
     * 
     * @param parser AS parser.
     */
    public final void detachParser(BaseASParser parser)
    {
        if (this.parser == parser)
            this.parser = null;
    }
}
