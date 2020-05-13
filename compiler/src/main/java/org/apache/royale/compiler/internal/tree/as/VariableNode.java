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
import java.util.Iterator;
import java.util.List;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.as.decorators.IVariableTypeDecorator;
import org.apache.royale.compiler.tree.as.decorators.SymbolDecoratorProvider;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * ActionScript parse tree node representing a variable definition (e.g. var
 * str:String)
 */
public class VariableNode extends BaseVariableNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The identifier node representing the name of the variable.
     * @param typeNode The expression node representing the type of the variable.
     */
    public VariableNode(IdentifierNode nameNode, ExpressionNodeBase typeNode)
    {
        super(nameNode, typeNode);
    }

    /**
     * Constructor.
     * 
     * @param nameNode The identifier node representing the name of the variable.
     */
    public VariableNode(IdentifierNode nameNode)
    {
        super(nameNode);
    }
    
    private List<ChainedVariableNode> chainedVariableNodes = null;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        ASTNodeID nodeID = ASTNodeID.VariableID;

        DefinitionBase varDef = this.getDefinition();
        if (varDef != null && varDef.isBindable())
        {
            // Bindable vars with a user-specified event class work the same as normal variables
            // the user is responsible for dispatching the event.

            //note: the above statement was inconsistent with Flex if the class itself is also marked [Bindable]
            //in that case, binding is related to both codegen events and the specified event.
            if (varDef.isClassBindable()) {
                //we know that isBindable() must be because of the class [Bindable], regardless of any specific eventNames
                nodeID = ASTNodeID.BindableVariableID;
            } else {
                //as original, only consider to be [Bindable] if eventNames is empty:
                List<String> eventNames = varDef.getBindableEventNames();
                if (eventNames.size() == 0)
                    nodeID = ASTNodeID.BindableVariableID;
            }
        }

        return nodeID;
    }
    
    @Override
    public int getSpanningStart()
    {
        return getNodeStartForTooling();
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        super.setChildren(fillInOffsets);
        
        if (chainedVariableNodes != null)
        {
            // If we have any chained vars, add them to our children now.  We don't do this in addChainedVariableNode
            // because if call addChildInOrder too early, then setChildren will never get called, and other children won't
            // be set up correctly.
            for (int i = 0, l = chainedVariableNodes.size(); i < l; ++i)
            {
                addChildInOrder(chainedVariableNodes.get(i), fillInOffsets);
            }
            // Don't need these anymore - they are now children of this node
            chainedVariableNodes = null;
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
    // BaseVariableNode overrides
    //

    @Override
    public String getVariableType()
    {
        IDefinition definition = getDefinition();
        
        List<IVariableTypeDecorator> list = SymbolDecoratorProvider.getProvider().getVariableTypeDecorators(definition);
        if (list.size() > 0)
        {
            Iterator<IVariableTypeDecorator> it = list.iterator();
            while (it.hasNext())
            {
                IDefinition type = it.next().decorateVariableType(definition);
                if (type != null)
                    return type.getQualifiedName();
            }
        }

        return super.getVariableType();
    }
    
    //
    // IVariableNode implementations
    //
    
    @Override
    public boolean isImplicit()
    {
        return nameNode instanceof ILanguageIdentifierNode &&
               (((ILanguageIdentifierNode)nameNode).getKind() == LanguageIdentifierKind.THIS ||
                ((ILanguageIdentifierNode)nameNode).getKind() == LanguageIdentifierKind.SUPER);
    }

    @Override
    public String getQualifiedName()
    {
        String qualifiedName = null;
        
        if (getVariableClassification() == VariableClassification.PACKAGE_MEMBER)
        {
            IImportTarget importTarget = ASImportTarget.buildImportFromPackageName(getWorkspace(), getPackageName());
            qualifiedName = importTarget.getQualifiedName(getName());
        }
        
        if (qualifiedName == null)
            qualifiedName = getName();
        
        return qualifiedName;
    }

    @Override
    public String getShortName()
    {
        return getName();
    }

    @Override
    public VariableClassification getVariableClassification()
    {
        IScopedNode scopedNode = getScopeNode();
        IASNode node = scopedNode;
        
        if (node != null)
        {
            if (node instanceof ICommonClassNode || node.getParent() instanceof ICommonClassNode)
                return VariableClassification.CLASS_MEMBER;
            
            if (node.getParent() instanceof IInterfaceNode)
                return VariableClassification.INTERFACE_MEMBER;
            
            if (node.getParent() instanceof PackageNode)
                return VariableClassification.PACKAGE_MEMBER;
            
            if (node instanceof FileNode) //this is an include
                return VariableClassification.FILE_MEMBER;
        }
        
        return VariableClassification.LOCAL;
    }
    
    @Override
    public int getDeclarationEnd()
    {
        int varEnd = getEnd();
        
        // handle multiple-variables here
        // only the first variable in the multiple-variable statement needs to be handled specially
        // as its end offset is the end of the entire statement
        // we'll check if the parent is not a variable and then go through the children
        // when we encounter a variable node, we'll take the end offset of the previous node
        if (!(getParent() instanceof IVariableNode))
        {
            int numChildren = getChildCount();
            for (int i = 0; i < numChildren; i++)
            {
                IASNode child = getChild(i);
                if (child instanceof IVariableNode && i > 0)
                { // we got a chained variable
                    varEnd = getChild(i - 1).getEnd();
                    break;
                }
            }
        }
        
        return varEnd;
    }

    //
    // Other methods
    //

    public void addChainedVariableNode(ChainedVariableNode node)
    {
        if (chainedVariableNodes == null)
            chainedVariableNodes = new ArrayList<ChainedVariableNode>();
        
        chainedVariableNodes.add(node);
    }
}
