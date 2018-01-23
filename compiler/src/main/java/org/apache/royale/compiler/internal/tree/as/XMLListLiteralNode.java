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

public class XMLListLiteralNode extends BaseLiteralContainerNode
{
    /**
     * Constructor.
     */
    public XMLListLiteralNode()
    {
        super(new LiteralNode(LiteralType.XMLLIST, ""));
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected XMLListLiteralNode(XMLListLiteralNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.XMLListContentID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return project.getBuiltinType(BuiltinType.XMLLIST);
    }

    @Override
    protected XMLListLiteralNode copy()
    {
        return new XMLListLiteralNode(this);
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
        return LiteralType.XMLLIST;
    }
}
