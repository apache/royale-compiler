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

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.IDecorationPart;
import org.apache.royale.compiler.internal.tree.as.parts.VariableDecorationPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * Base class for variables, including both traditional variables as well as
 * arguments
 */
public abstract class BaseVariableNode extends BaseTypedDefinitionNode implements IVariableNode, IInitializableDefinitionNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The node containing the name of the variable/argument.
     */
    public BaseVariableNode(IdentifierNode nameNode)
    {
        super();
        init(nameNode);
    }

    /**
     * Constructor.
     * 
     * @param nameNode The node containing the name of the variable/argument.
     * @param typeNode The node containing the type of the variable/argument.
     */
    public BaseVariableNode(IdentifierNode nameNode, ExpressionNodeBase typeNode)
    {
        super();
        init(nameNode);
        this.typeNode = typeNode;
    }

    /**
     * Offset at which the equals operator ("=") starts
     */
    protected int equalsOperatorStart;

    protected boolean isConst;
    
    //
    // NodeBase overrides
    //

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(((VariableDecorationPart)getDecorationPart()).getKeywordValue(), fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        ensureTypeNode();
        addChildInOrder(typeNode, fillInOffsets);
        addChildInOrder(getAssignedValueNode(), fillInOffsets);
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            if (needsBindableDefinition())
            {
                DefinitionBase[] bindingDefs = buildBindableDefinitions(scope);

                // Set the definition to the first "binding" definition - it will be a getter or setter,
                // so you can always get to the corresponding getter/setter from it.
                setDefinition(bindingDefs[0]);

                scope.addDefinition(bindingDefs[0]);
                scope.addDefinition(bindingDefs[1]);
            }
            else
            {
                DefinitionBase definition = buildDefinition();
                setDefinition(definition);
                scope.addDefinition(definition);
            }
        }

        super.analyze(set, scope, problems);
    }
    
    /*
     * For debugging only. Build a string such as <code>i:int</code> from the
     * name and type of the variable being defined.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append(getName());

        String type = getVariableType();
        if (!type.isEmpty())
        {
            sb.append(':');
            sb.append(type);
        }

        return true;
    }

    //
    // BaseDefinitionNode overrides
    //
    
    @Override
    protected void init(ExpressionNodeBase node)
    {
        super.init(node);
        
        equalsOperatorStart = -1;
        isConst = false;
    }

    @Override
    protected IDecorationPart createDecorationPart()
    {
        return new VariableDecorationPart();
    }
    
    @Override
    // TODO Remove unnecessary override.
    public DefinitionBase getDefinition()
    {
        return super.getDefinition();
    }

    //
    // IVariableNode implementations
    //
    
    @Override
    public IExpressionNode getVariableTypeNode()
    {
        return getTypeNode();
    }
    
    @Override
    public String getVariableType()
    {
        return getTypeName();
    }

    @Override
    public boolean isConst()
    {
        return isConst;
    }
    
    @Override
    public int getDeclarationEnd()
    {
        return getEnd();
    }

    //
    // IInitializableDefinitionNode implementations
    //
    
    @Override
    public final ExpressionNodeBase getAssignedValueNode()
    {
        return ((VariableDecorationPart)getDecorationPart()).getAssignedValue();
    }

    @Override
    // TODO Setter should not be in interface
    public void setAssignedValue(IASToken equalsOperator, ExpressionNodeBase value)
    {
        if (equalsOperator != null)
            equalsOperatorStart = equalsOperator.getStart();

        ((VariableDecorationPart)getDecorationPart()).setAssignedValue(value);
    }

    //
    // Other methods
    //

    public void setKeyword(IASToken keyword)
    {
        if (keyword != null) //keyword can be null in some cases
            ((VariableDecorationPart)getDecorationPart()).setKeywordNode(new KeywordNode(keyword));
    }

    public void setKeyword(KeywordNode keyword)
    {
        if (keyword != null)
            ((VariableDecorationPart)getDecorationPart()).setKeywordNode(keyword);
    }

    /**
     * Sets a flag to indicate that this node is a constant
     * 
     * @param isConst true if we are a constant
     */
    public void setIsConst(boolean isConst)
    {
        this.isConst = isConst;
    }

    /**
     * Ensures the that this variable will have a type as a child. Subclasses
     * can override this method to provide different values for the type
     */
    protected void ensureTypeNode()
    {
        //if we don't have a type node, drop in a default identifier that is implicit
        if (typeNode == null)
            typeNode = LanguageIdentifierNode.buildAnyType();
    }

    /**
     * Build the VariableDefinition or ConstantDefinition that represents this
     * Node.
     * 
     * @return the Definition representing this Node.
     */
    DefinitionBase buildDefinition()
    {
        String definitionName = nameNode.computeSimpleReference();

        VariableDefinition definition;
        definition = isConst() ?
                     new ConstantDefinition(definitionName) :
                     new VariableDefinition(definitionName);

        fillinDefinition(definition);

        definition.setDeclaredInControlFlow(isInControlFlow());

        definition.setInitializer(this.getAssignedValueNode());

        return definition;
    }

    /**
     * Determine if this var decl was inside a control flow construct
     * Basically looks up the parent chain until it gets to it's containing definition,
     * if we hit anything other than a block or ChainedVariableNode before then we know we
     * are inside some control flow.
     * @return  true if this variable is declared inside a control flow construct
     */
    private boolean isInControlFlow()
    {
        boolean result = false;
        IASNode n = getParent();
        while( n !=null )
        {
            if( n instanceof BlockNode || // OK to be nested in empty blocks
                n instanceof ChainedVariableNode )// OK to be nested in a chained variable
            {
                n = n.getParent();
            }
            else if( n instanceof BaseDefinitionNode )
            {
                // If we hit a DefinitionNode, then we made it to our containing definition
                // we can stop looking now
                n = null;
            }
            else
            {
                // Found something between the decl and the containing def, must be something
                // control flowish.
                result = true;
                n = null;
            }
        }
        return result;
    }

    /**
     * Does this variable node need to construct a special definition this is
     * true for variables that have bindable metadata and no attributes, or are
     * "public" and inside of a ClassNode with bindable metadata with no
     * attributes. If it has [Bindable] metadata with an attribute, such as
     * [Bindable(event="MyEvent")], then no special handling is needed and we
     * can just construct a normal VariableDefinition
     * 
     * @return true if this represents a Bindable variable that needs special
     * handling
     */
    private boolean needsBindableDefinition()
    {
        if (hasEmptyBindableMetadata(getMetaTagsNode()))
            return true;

        INamespaceDecorationNode nsNode = getNamespaceNode();
        if (nsNode != null && nsNode.getName() == INamespaceConstants.public_)
        {
            ClassNode cls = (ClassNode)getAncestorOfType(ClassNode.class);
            if (cls != null && hasEmptyBindableMetadata(cls.getMetaTags()))
                return true;
        }
        return false;

    }

    /**
     * Helper method to determine if the IMetaTagsNode passed in has at least 1
     * Bindable tag with no attributes
     */
    private boolean hasEmptyBindableMetadata(IMetaTagsNode metaTagsNode)
    {
        boolean hasEmptyBindable = false;
        if (metaTagsNode != null)
        {
            IMetaTagNode[] bindableTags = metaTagsNode.getTagsByName(IMetaAttributeConstants.ATTRIBUTE_BINDABLE);

            for (IMetaTagNode node : bindableTags)
            {
                if (node.getAllAttributes().length == 0)
                {
                    hasEmptyBindable = true;
                    break;
                }
            }
        }
        return hasEmptyBindable;
    }

    public boolean hasEqualsOperator()
    {
        return equalsOperatorStart != -1;
    }

    /**
     * Get the offset at which the equals operator ("=") starts
     * 
     * @return start offset for equals operator
     */
    public int getEqualsOperatorStart()
    {
        return equalsOperatorStart;
    }

    /**
     * Get the offset at which the equals operator ("=") ends
     * 
     * @return end offset for equals operator
     */
    public int getEqualsOperatorEnd()
    {
        return equalsOperatorStart + 1;
    }

    public final KeywordNode getKeywordNode()
    {
        return ((VariableDecorationPart)getDecorationPart()).getKeywordValue();
    }
}
