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

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;

public class XMLLiteralNode extends BaseLiteralContainerNode
{
    /**
     * Constructor.
     */
    public XMLLiteralNode()
    {
        super(new LiteralNode(LiteralType.XML, ""));
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected XMLLiteralNode(XMLLiteralNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.XMLContentID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return project.getBuiltinType(BuiltinType.XML);
    }

    @Override
    protected XMLLiteralNode copy()
    {
        return new XMLLiteralNode(this);
    }

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return true;
    }

    //
    // BaseLiteralContainerNode overrides
    //

    @Override
    public LiteralType getLiteralType()
    {
        return LiteralType.XML;
    }
    
    //
    // Other methods
    //

    /**
     * This method is for debugging and testing purposes only.
     * 
     * @return XML literal text.
     */
    public final String getText()
    {
        final StringBuilder result = new StringBuilder();
        
        final ContainerNode contents = getContentsNode();
        for (int i = 0; i < contents.getChildCount(); i++)
        {
            final IASNode element = contents.getChild(i);
            
            if (element instanceof LiteralNode)
                result.append(((LiteralNode)element).getValue());

            else if (element instanceof IIdentifierNode)
                result.append("{").append(((IIdentifierNode)element).getName()).append("}");

            else if (element instanceof IMemberAccessExpressionNode)
                result.append("{").append(((IMemberAccessExpressionNode)element).getDisplayString()).append("}");

        }
        
        return result.toString();
    }
}
