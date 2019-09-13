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

package org.apache.royale.compiler.internal.tree.as;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.VariableDecorationPart;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * ActionScript parse tree node representing one argument (e.g. str:String) in a
 * function definition
 */
public final class ParameterNode extends BaseVariableNode implements IParameterNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The identifier node representing the name of the parameter.
     */
    public ParameterNode(IdentifierNode nameNode)
    {
        super(nameNode);
    }

    /**
     * Constructor.
     * 
     * @param nameNode The identifier node representing the name of the parameter.
     * @param typeNode The expression node reperesenting the type of teh parameter.
     */
    public ParameterNode(IdentifierNode nameNode, ExpressionNodeBase typeNode)
    {
        super(nameNode, typeNode);
    }

    private boolean isRest = false;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        if (!isRest())
            return ASTNodeID.ArgumentID;
        else
            return ASTNodeID.ArgumentRestID;
    }

    @Override
    public IScopedNode getScopeNode()
    {
        IASNode ancestor = getAncestorOfType(FunctionNode.class);
       
        if (ancestor != null && ancestor instanceof FunctionNode)
        {
            FunctionNode function = (FunctionNode)ancestor;
            if (function.getScopedNode() != null)
                return function.getScopedNode();
        }
        
        // We should never get here, but just in case...
        if (getParent() != null)
            return getParent().getContainingScope();
        
        return null;
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (isCatchParameter(scope))
        {
            if (set.contains(PostProcessStep.POPULATE_SCOPE) ||
                set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
            {
                ParameterDefinition definition = buildDefinition();
                setDefinition(definition);
                // Don't hoist the catch scope's parameter definition
                ((CatchScope)scope).setParameterDefinition(definition);
            }
        }
        else
        {
            super.analyze(set, scope, problems);
        }
    }
    
    //
    // TreeNode overrides
    //
    
    @Override
    protected int getInitialChildCount()
    {
        return 3;
    }
    
    //
    // BaseDefinitionNode overrides
    //

    @Override
    public IMetaTagsNode getMetaTags()
    {
        return null;
    }

    @Override
    public String getNamespace()
    {
        return null;
    }

    @Override
    public boolean hasNamespace(String namespace)
    {
        return false;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return false;
    }
    
    //
    // BaseVariableNode overrides
    //
    
    @Override
    protected void ensureTypeNode()
    {
        if (typeNode == null && isRest())
        {
            typeNode = new IdentifierNode(IASLanguageConstants.Array);
            typeNode.span(-1, -1, -1, -1, -1, -1);
            return;
        }
        
        super.ensureTypeNode();
    }

    /**
     * Build a Definition for this argument
     * 
     * @return the Definition for this argument
     */
    @Override
    ParameterDefinition buildDefinition()
    {
        String definitionName = getName();
        ParameterDefinition definition = new ParameterDefinition(definitionName);
        definition.setNode(this);
        fillInNamespaceAndModifiers(definition);

        setDefinition(definition);

        // Set the type of the parameter. If a type annotation doesn't appear in the source,
        // the parameter type in the definition will be null.
        IReference typeRef = hasExplicitType() ? typeNode.computeTypeReference() : null;

        // If there is no type anno, and we're the rest parameter, we're typed as Array
        if (typeRef == null && this.isRest())
            typeRef = ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ARRAY);

        definition.setTypeReference(typeRef);

        // Tell the def we have a default value
        if (hasDefaultValue())
        {
            definition.setHasDefault();
            definition.setInitializer(this.getAssignedValueNode());
        }

        // Is it ... ?
        if (isRest())
            definition.setRest();

        return definition;
    }
    
    @Override
    public DefinitionBase getDefinition()
    {
        return super.getDefinition();
    }

    @Override
    protected void setDefinition(IDefinition definition)
    {
        assert definition instanceof ParameterDefinition;
        super.setDefinition(definition);
    }

    //
    // IParameterNode implementations
    //

    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public String getQualifiedName()
    {
        return getName();
    }

    @Override
    public String getShortName()
    {
        return getName();
    }

    @Override
    public VariableClassification getVariableClassification()
    {
        if (getAncestorOfType(CatchNode.class) != null)
            return VariableClassification.LOCAL;
        
        return VariableClassification.PARAMETER;
    }

    @Override
    public boolean isRest()
    {
        return isRest;
    }
    
    @Override
    public boolean hasDefaultValue()
    {
        return ((VariableDecorationPart)getDecorationPart()).getAssignedValue() != null;
    }
    
    @Override
    public String getDefaultValue()
    {
        ExpressionNodeBase assignedValueNode = ((VariableDecorationPart)getDecorationPart()).getAssignedValue();
        
        if (assignedValueNode != null)
        {
            if (assignedValueNode instanceof ILiteralNode)
                return ((ILiteralNode)assignedValueNode).getValue(true);
            
            else if (assignedValueNode instanceof IIdentifierNode)
                return ((IIdentifierNode)assignedValueNode).getName();
            
            else if (assignedValueNode instanceof MemberAccessExpressionNode)
                return ((MemberAccessExpressionNode)assignedValueNode).getDisplayString();
        }
        
        return null;
    }
    
    //
    // Other methods
    //

    public void setIsRestParameter(boolean isRest)
    {
        this.isRest = isRest;
    }

    private boolean isCatchParameter(ASScope scope)
    {
        return scope instanceof CatchScope &&
               this.getParent() instanceof CatchNode;
    }
}
