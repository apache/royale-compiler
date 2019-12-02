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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.definitions.SyntheticBindableGetterDefinition;
import org.apache.royale.compiler.internal.definitions.SyntheticBindableSetterDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.ITypedNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;

/**
 * Base class for definitions that have a type associated with them
 */
public abstract class BaseTypedDefinitionNode extends BaseDefinitionNode implements ITypedNode
{
    /**
     * Constructor.
     */
    public BaseTypedDefinitionNode()
    {
        super();
    }

    /**
     * Offset at which the type operator (":") starts
     */
    protected int typeOperatorStart;

    /**
     * The type of the variable
     */
    protected ExpressionNodeBase typeNode;

    //
    // BaseDefinitionNode overrides
    //

    @Override
    protected void init(ExpressionNodeBase nameNode)
    {
        super.init(nameNode);
        
        typeOperatorStart = -1;
        typeNode = null;
    }
    
    //
    // ITypedNode implementations
    //
    
    @Override
    public ExpressionNodeBase getTypeNode()
    {
        return typeNode;
    }

    @Override
    public boolean hasTypeOperator()
    {
        return typeOperatorStart != -1;
    }

    @Override
    public int getTypeOperatorStart()
    {
        return typeOperatorStart;
    }

    @Override
    public int getTypeOperatorEnd()
    {
        return typeOperatorStart + 1;
    }

    //
    // Other methods
    //

    /**
     * Determines if this typed definition has an explicit type specification
     * 
     * @return true if we have an actual type
     */
    public boolean hasExplicitType()
    {
        if (typeNode instanceof IdentifierNode)
            return !((IdentifierNode)typeNode).isImplicit();

        return typeNode != null;
    }

    /**
     * Returns the type of this node in String form
     * 
     * @return the types name, or an empty string
     */
    public String getTypeName()
    {
        if(hasExplicitType())
        {
            IIdentifierNode identifierNode = null;
            if(typeNode instanceof IIdentifierNode)
            {
                identifierNode = (IIdentifierNode) typeNode;
            }
            else if(typeNode instanceof INamespaceAccessExpressionNode)
            {
                INamespaceAccessExpressionNode namespaceAccess = (INamespaceAccessExpressionNode) typeNode;
                IExpressionNode rightOperandNode = namespaceAccess.getRightOperandNode();
                if (rightOperandNode instanceof IIdentifierNode) 
                {
                    identifierNode = (IIdentifierNode) rightOperandNode;
                }
            }
            if (identifierNode != null)
            {
                return identifierNode.getName();
            }
        }
        return "";
    }

    public boolean isVoidType()
    {
        return typeNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode)typeNode).getKind() == LanguageIdentifierKind.VOID;
    }

    public boolean isAnyType()
    {
        return typeNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode)typeNode).getKind() == LanguageIdentifierKind.ANY_TYPE;
    }

    /**
     * Set the type node. Used during parsing.
     * 
     * @param typeOperator ASToken containing the type operator (":")
     * @param variableType node containing the variable type
     */
    public void setType(IASToken typeOperator, ExpressionNodeBase variableType)
    {
        if (typeOperator != null)
            typeOperatorStart = typeOperator.getStart();

        typeNode = variableType;
    }
    
    /**
     * Helper method to fill in a definition with appropriate metadata &
     * namespace stuff. Used by both buildDefinition and
     * buildBindableDefinitions
     * 
     * @param definition the definition to "fill in"
     */
    protected void fillinDefinition(DefinitionBase definition)
    {
        definition.setNode(this);

        fillInNamespaceAndModifiers(definition);
        fillInMetadata(definition);

        // Set the variable's type. If a type annotation doesn't appear in the source,
        // the variable type in the definition will be null.
        IReference typeRef = hasExplicitType() ? typeNode.computeTypeReference() : null;
        definition.setTypeReference(typeRef);
    }

    public DefinitionBase buildBindableGetter(String definitionName)
    {
        GetterDefinition getter = new SyntheticBindableGetterDefinition(definitionName);
        fillinDefinition(getter);
        
        // set up the return type for the getter
        IReference typeRef = getter.getTypeReference();
        getter.setReturnTypeReference(typeRef);

        return getter;
    }
    
    public DefinitionBase buildBindableSetter(String definitionName, ASScope containingScope, IReference typeRef)
    {
        SetterDefinition setter = new SyntheticBindableSetterDefinition(definitionName);

        fillinDefinition(setter);

        // Set up the params for the setter
        ParameterDefinition param = new ParameterDefinition("");
        param.setTypeReference(typeRef);
        setter.setParameters(new ParameterDefinition[] {param});
        setter.setTypeReference(typeRef);
        ASScope setterContainedScope = new FunctionScope(containingScope);
        setter.setContainedScope(setterContainedScope);
        setterContainedScope.addDefinition(param);
        setter.setReturnTypeReference(ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.VOID));

        return setter;
    }
    
    /**
     * Build the definitions for a bindable variable that needs a generated
     * getter/setter.
     * 
     * @return An Array with all of the definitions built for this Node.
     */
    DefinitionBase[] buildBindableDefinitions(ASScope containingScope)
    {
        String definitionName = nameNode.computeSimpleReference();

        DefinitionBase[] definitions = new DefinitionBase[2];
        definitions[0] = buildBindableGetter(definitionName);        
        definitions[1] = buildBindableSetter(definitionName, containingScope, definitions[0].getTypeReference());

        return definitions;
    }


}
