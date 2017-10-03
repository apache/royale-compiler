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
import org.apache.royale.compiler.tree.as.ILiteralNode;

/**
 * Represents a literal node array that contains nodes within an array literal
 * statement: [ foo, bar, baz ]
 */
public class ArrayLiteralNode extends BaseLiteralContainerNode
{
    /**
     * Constructor.
     */
    public ArrayLiteralNode()
    {
        super(new LiteralNode(LiteralType.ARRAY, ""));
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected ArrayLiteralNode(ArrayLiteralNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ArrayLiteralID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return project.getBuiltinType(BuiltinType.ARRAY);
    }

    @Override
    protected ArrayLiteralNode copy()
    {
        return new ArrayLiteralNode(this);
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
        return LiteralType.ARRAY;
    }

    @Override
    public String getValue()
    {
        return getValue(false);
    }

    @Override
    public String getValue(boolean rawValue)
    {
        StringBuilder builder = new StringBuilder();
        ContainerNode contents = getContentsNode();
        
        int childcount = contents.getChildCount();
        for (int i = 0; i < childcount; i++)
        {
            IASNode child = contents.getChild(i);
           
            if (child instanceof ILiteralNode)
                builder.append(((ILiteralNode)child).getValue(rawValue));
            else if (child instanceof IIdentifierNode)
                builder.append(((IIdentifierNode)child).getName());

            if (i + 1 < (childcount))
                builder.append(",");
        }
        
        return "[" + builder.toString() + "]";
    }
}
