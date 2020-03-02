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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.royale.compiler.internal.as.codegen.ICodeGenerator;
import org.apache.royale.compiler.internal.as.codegen.ICodeGenerator.IConstantValue;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

import java.util.Collection;

/**
 * Instances of this class represent definitions of ActionScript variables and
 * constants in the symbol table.
 * <p>
 * After a variable definition is in the symbol table, it should always be
 * accessed through the read-only <code>IVariableDefinition</code> interface.
 */
public class VariableDefinition extends DefinitionBase implements IVariableDefinition
{
    /**
     * Constructor.
     * 
     * @param name The name of the variable.
     */
    public VariableDefinition(String name)
    {
        super(name);
    }

    /**
     * Constructor.
     * 
     * @param name The name of the variable.
     * @param initialValue The initial value of the variable.
     */
    public VariableDefinition(String name, Object initialValue)
    {
        this(name);
        this.initValue = initialValue;
    }

    /**
     * The initial value of this VariableDefinition, if it is known this is used
     * for variables that come from an ABC with a value specified in the traits
     * entry.
     */
    protected Object initValue;

    @Override
    public VariableClassification getVariableClassification()
    {
        // Determine whether or not a variable is local
        // by looking at its containing scope.
        //
        // Local variables don't always have a containing
        // function definition ( MXML event specifiers is one
        // example ).
        ASScope containingScope = getContainingASScope();
        if (containingScope instanceof FunctionScope)
            return VariableClassification.LOCAL;
        if (containingScope instanceof CatchScope)
            return VariableClassification.LOCAL;

        IDefinition parent = getParent();

        if (parent instanceof IClassDefinition)
            return VariableClassification.CLASS_MEMBER;
        if (parent instanceof IPackageDefinition)
            return VariableClassification.PACKAGE_MEMBER;
        if (parent == null)
        {
            if (inPackageNamespace())
                return VariableClassification.PACKAGE_MEMBER;

            return VariableClassification.FILE_MEMBER;
        }

        assert false;
        return null;
    }

    // TODO Remove everything below here when Royale has been integrated into Fb and Fc.

    /**
     * Gets the {@link DependencyType} that should be used when resolving the
     * type of this variable definition.
     * <p>
     * This method is intended to be overridden by sub-classes.
     * 
     * @return The {@link DependencyType} that should be used when resolving the
     * type of this variable definition
     */
    protected DependencyType getTypeDependencyType()
    {
        DependencyType dt = DependencyType.EXPRESSION;
        if (getVariableClassification() != VariableClassification.LOCAL)
            dt = DependencyType.SIGNATURE;
        return dt;
    }

    private boolean mxmlDeclared = false;
    public void setMxmlDeclared(){
        mxmlDeclared = true;
    }

    public boolean isMXMLDeclared() {
        return mxmlDeclared;
    }

    @Override
    public IVariableNode getNode()
    {
        return (IVariableNode)super.getNode();
    }

    /**
     * Get the node that produced this definition if it has not yet been collected.
     * This will return null if the node has been collected, and won't do any work to get the
     * node back (like reparsing the file).
     * @return  The Variable Node that produced this definition, or null if it does not exist
     */
    private IVariableNode getNodeIfExists()
    {
        return (IVariableNode)nodeRef.getNodeIfExists();
    }
    
    @Override
    public IVariableNode getVariableNode()
    {
        return getNode();
    }

    @Override
    public boolean matches(DefinitionBase node)
    {
        boolean matches = super.matches(node);
        if (!matches)
            return false;
        if (node == this)
            return true;
        VariableDefinition vNode = (VariableDefinition)node;

        VariableClassification classification = vNode.getVariableClassification();
        if (classification != getVariableClassification())
            return false;

        // Along with local, file member and parameter, name offsets needs to be compared for class/interface members also.
        // This is required to differentiate members having same name belonging to different class/interface
        // within the same AS file - See FBG-3494 for an example.
        if (classification == VariableClassification.LOCAL || classification == VariableClassification.FILE_MEMBER || classification == VariableClassification.PARAMETER
                || classification == VariableClassification.CLASS_MEMBER || classification == VariableClassification.INTERFACE_MEMBER)
        {
            if (vNode.getNameStart() != getNameStart() || vNode.getNameEnd() != getNameEnd())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * For debugging only. Produces a string such as
     * <code>public var i:int</code>.
     */
    @Override
    protected void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');
        
        if (isStatic())
        {
            sb.append(IASKeywordConstants.STATIC);
            sb.append(' ');
        }

        sb.append(IASKeywordConstants.VAR);
        sb.append(' ');

        sb.append(getBaseName());

        String type = getTypeAsDisplayString();
        if (!type.isEmpty())
        {
            sb.append(':');
            sb.append(type);
        }
    }

    public IExpressionNode getInitializer() {
    	return initializer;
	}
    
    @SuppressWarnings("incomplete-switch")
	public void setInitializer(IExpressionNode initExpr)
    {
        if( initExpr != null )
        {
            if( initExpr instanceof LiteralNode )
            {
                // Do some quick an dirty evaluation if the initializer is just a literal
                // no point in copying the expression and running it through the burm later
                switch(initExpr.getNodeID() )
                {
                    case LiteralBooleanID:
                        initValue = SemanticUtils.getBooleanContent(initExpr);
                        return;
                    case LiteralDoubleID:
                        initValue = SemanticUtils.getDoubleContent(initExpr);
                        return;
                    case LiteralStringID:
                        initValue = SemanticUtils.getStringLiteralContent(initExpr);
                        return;
                    case LiteralIntegerID:
                        initValue = SemanticUtils.getIntegerContent(initExpr);
                        return;
                    case LiteralNullID:
                        initValue = ABCConstants.NULL_VALUE;
                        return;
                    case LiteralUintID:
                        initValue = SemanticUtils.getUintContent(initExpr);
                        return;
                }
            }
            setHasInit();
            // If we're a local then we can depend on the weak reference back to the original
            // AST - if that AST is ever collected then we shouldn't be able to resolve back
            // to the definition of a local.
            if( getVariableClassification() != VariableClassification.LOCAL )
                this.initializer = initExpr.copyForInitializer( new DefinitionScopedNode() );
        }
    }
    private void setHasInit()
    {
        flags |= FLAG_HAS_INIT;
    }
    
    boolean hasInit()
    {
        return (flags & FLAG_HAS_INIT) != 0;
    }

    /**
     * IScopedNode impmlementation to make the containing scope available to the
     * copied initializer expression
     */
    private class DefinitionScopedNode extends NodeBase implements IScopedNode
    {
        public IASScope getScope ()
        {
            return getContainingASScope();
        }

        public void getAllImports (Collection<String> imports)
        {
        }

        public void getAllImportNodes (Collection<IImportNode> imports)
        {
        }

        public ASTNodeID getNodeID ()
        {
            return ASTNodeID.UnknownID;
        }
    }
    
    /**
     * Try to calculate the initial value for this VariableDefinition.
     * 
     * @param project the project to use to resolve the intializer
     * @return the initial value of this definition, or null if one can't be
     * determined.
     */
    @Override
    public Object resolveInitialValue(ICompilerProject project)
    {
        if (initValue != null)
            return initValue;

        IExpressionNode initExpr = getInitExpression();
        if (initExpr != null)
        {
            ICodeGenerator codeGenerator = CodeGeneratorManager.getCodeGenerator();
            IConstantValue constantValue = codeGenerator.generateConstantValue(initExpr, project);
            if (constantValue != null)
                return constantValue.getValue();
        }

        return null;
    }

    private IExpressionNode initializer;

    protected IExpressionNode getInitExpression()
    {
        IExpressionNode init = null;
        if( hasInit() )
        {
            if( this.getVariableClassification() == VariableClassification.LOCAL )
            {
                IVariableNode n = getNodeIfExists();
                assert n != null : "The AST for a local var should still be in memory!";
                init = n.getAssignedValueNode();
            }
            else
            {
                init = initializer;
            }
        }
        return init;
    }

    @Override
    public boolean isSkinPart()
    {
        return getSkinPart() != null;
    }

    @Override
    public boolean isRequiredSkinPart()
    {
        IMetaTag skinPart = getSkinPart();
        if (skinPart == null)
            return false;

        return isRequiredSkinPart(skinPart);
    }

    /**
     * Tell this definition whether it was declared in a control flow block
     * or not.  Default setting is false.
     *
     * @param b true if this definition was declared in control flow, otherwise false.
     */
    public void setDeclaredInControlFlow(boolean b)
    {
        if (b)
            flags |= FLAG_DECLARED_IN_CONTROL_FLOW;
        else
            flags &= ~FLAG_DECLARED_IN_CONTROL_FLOW;
    }

    /**
     * Was this variable declared inside a control flow construct?
     * <p>
     * This is used in constant-evaluating the value of the variable.
     *
     * @return true if the Definition was declared inside a control-flow block
     */
    public boolean declaredInControlFlow()
    {
        return (flags & FLAG_DECLARED_IN_CONTROL_FLOW) != 0;
    }
}
