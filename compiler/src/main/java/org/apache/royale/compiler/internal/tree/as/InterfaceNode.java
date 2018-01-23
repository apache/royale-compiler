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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;

/**
 * ActionScript parse tree node representing an interface definition
 */
public class InterfaceNode extends MemberedNode implements IInterfaceNode
{
    /**
     * Constructor.
     * 
     * @param name The node holding the interface name.
     */
    public InterfaceNode(ExpressionNodeBase name)
    {
        init(name);
    }

    /**
     * The class keyword
     */
    protected KeywordNode interfaceKeywordNode;

    /**
     * The extends keyword (if one is present)
     */
    protected KeywordNode extendsKeywordNode;

    /**
     * The collection of base interfaces
     */
    protected TransparentContainerNode baseInterfacesNode;

    /**
     * Generated FunctionNode to represent cast function
     */
    protected FunctionNode castFunctionNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.InterfaceID;
    }
    
    @Override
    public int getSpanningStart()
    {
        return getNodeStartForTooling();
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(interfaceKeywordNode, fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(extendsKeywordNode, fillInOffsets);
        addChildInOrder(contentsNode, fillInOffsets);
        if (extendsKeywordNode != null || baseInterfacesNode.getChildCount() > 0)
            addChildInOrder(baseInterfacesNode, fillInOffsets);
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            InterfaceDefinition definition = buildDefinition();
            setDefinition(definition);
            scope.addDefinition(definition);

            TypeScope typeScope = new TypeScope(scope, contentsNode, definition);
            definition.setContainedScope(typeScope);
            scope = typeScope;

            setupCastFunction(set, definition, scope);
        }
        
        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            reconnectDef(scope);
            scope = this.getDefinition().getContainedScope();
            contentsNode.reconnectScope(scope);
        }

        // Recurse on the interface block.
        contentsNode.analyze(set, scope, problems);
    }

    /*
     * For debugging only. Builds a string such as <code>"IFoo"</code> from the
     * name of the interface being defined.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }
    
    //
    // TreeNode overrides
    //
    
    @Override
    protected int getInitialChildCount()
    {
        return 2;
    }

    //
    // BaseDefinitionNode overrides
    //

    @Override
    protected void init(ExpressionNodeBase nameNode)
    {
        super.init(nameNode);
        
        extendsKeywordNode = null;
        baseInterfacesNode = new TransparentContainerNode();
        castFunctionNode = null;
    }
    
    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return false;
    }
    
    @Override
    public InterfaceDefinition getDefinition()
    {
        return (InterfaceDefinition)super.getDefinition();
    }

    @Override
    protected void setDefinition(IDefinition definition)
    {
        assert definition instanceof InterfaceDefinition;
        super.setDefinition(definition);
    }

    //
    // IInterfaceNode implementations
    //
    
    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public String getQualifiedName()
    {
        IImportTarget importTarget = ASImportTarget.buildImportFromPackageName(getWorkspace(), getPackageName());
        String qualifiedName = importTarget.getQualifiedName(getName());
        if (qualifiedName == null)
            return getShortName();
        return qualifiedName;
    }

    @Override
    public String getShortName()
    {
        String name = getName();
        int lastDot = name.lastIndexOf(".");
        if (lastDot != -1)
            name = name.substring(lastDot + 1);
        return name;
    }
    
    @Override
    public InterfaceClassification getInterfaceClassification()
    {
        if (getParent() instanceof FileNode)
            return InterfaceClassification.INNER_INTERFACE;
 
        return InterfaceClassification.PACKAGE_MEMBER;
    }
    
    @Override
    public IExpressionNode[] getExtendedInterfaceNodes()
    {
        ArrayList<IExpressionNode> interfaceNodeList = new ArrayList<IExpressionNode>();
        
        int childCount = baseInterfacesNode.getChildCount();
        if (baseInterfacesNode != null && childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                IASNode child = baseInterfacesNode.getChild(i);
                if (child instanceof IIdentifierNode)
                {
                    interfaceNodeList.add(((IIdentifierNode)child));
                }
            }
        }
        
        return interfaceNodeList.toArray(new IExpressionNode[0]);
    }
    
    @Override
    public String[] getExtendedInterfaces()
    {
        ArrayList<String> interfaceNodeList = new ArrayList<String>();
        
        int childCount = baseInterfacesNode.getChildCount();
        if (baseInterfacesNode != null && childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                IASNode child = baseInterfacesNode.getChild(i);
                if (child instanceof IIdentifierNode)
                {
                    interfaceNodeList.add(((IIdentifierNode)child).getName());
                }
            }
        }
        
        return interfaceNodeList.toArray(new String[0]);
    }

    //
    // Other methods
    //

    /**
     * Sets the interface keyword. Used during parsing.
     * 
     * @param interfaceKeyword token containing the keyword
     */
    public void setInterfaceKeyword(IASToken interfaceKeyword)
    {
        interfaceKeywordNode = new KeywordNode(interfaceKeyword);
    }

    /**
     * Get the node containing the class keyword
     * 
     * @return the node containing class
     */
    public KeywordNode getClassKeywordNode()
    {
        return interfaceKeywordNode;
    }

    /**
     * Get the node containing the extends keyword, if one exists
     * 
     * @return the node containing extends
     */
    public KeywordNode getExtendsKeywordNode()
    {
        return extendsKeywordNode;
    }

    /**
     * Sets the extends keyword if one is present. Used during parsing.
     * 
     * @param extendsKeyword token containing the keyword
     */
    public void setExtendsKeyword(IASToken extendsKeyword)
    {
        extendsKeywordNode = new KeywordNode(extendsKeyword);
    }

    /**
     * Add an implemented interface to this class. Used during parsing.
     * 
     * @param interfaceName node containing the interface name
     */
    public void addBaseInterface(ExpressionNodeBase interfaceName)
    {
        baseInterfacesNode.addChild(interfaceName);
    }

    /**
     * Get the container of base interfaces for this interfaces
     * 
     * @return the node containing the base interfaces
     */
    public ContainerNode getBaseInterfacesNode()
    {
        return baseInterfacesNode;
    }

    private void setupCastFunction(EnumSet<PostProcessStep> set, InterfaceDefinition def, ASScope scope)
    {
        // Unlike ClassNode, InterfaceNode doesn't create implicit definitions
        // for "this" or "super" for interfaces; but it does create an implicit
        // function that helps us resolve casting expressions such as IFoo(foo).
        FunctionDefinition castFunc = new FunctionDefinition(getName());
        castFunc.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());
        castFunc.setReturnTypeReference(ReferenceFactory.resolvedReference(def));
        castFunc.setCastFunction();
        castFunc.setImplicit();

        // Add this definition to the interface scope.
        scope.addDefinition(castFunc);
    }

    /**
     * Get the dummy node representing the cast function
     * 
     * @return cast function
     */
    public FunctionNode getCastFunctionNode()
    {
        return castFunctionNode;
    }

    InterfaceDefinition buildDefinition()
    {
        String definitionName = nameNode.computeSimpleReference();
        InterfaceDefinition definition = new InterfaceDefinition(definitionName);
        definition.setNode(this);

        fillInNamespaceAndModifiers(definition);
        fillInMetadata(definition);

        // Set the interfaces that this interface extends.
        if (baseInterfacesNode != null)
        {
            int n = baseInterfacesNode.getChildCount();
            IReference[] baseInterfaces = new IReference[n];
            for (int i = 0; i < n; i++)
            {
                IASNode child = baseInterfacesNode.getChild(i);
                if (child instanceof ExpressionNodeBase)
                    baseInterfaces[i] = ((ExpressionNodeBase)child).computeTypeReference();
            }
            definition.setExtendedInterfaceReferences(baseInterfaces);
        }

        return definition;
    }
}
