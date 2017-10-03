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

package org.apache.royale.compiler.internal.tree.as.metadata;

import antlr.Token;

import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.QualifiedNameExpressionNode;
import org.apache.royale.compiler.tree.metadata.ITypedTagNode;

public class TypedTagNode extends MetaTagNode implements ITypedTagNode
{
    protected IdentifierNode type;

    public TypedTagNode(String tagName)
    {
        super(tagName);
    }

    public void setTypeName(String attributeName, Token type)
    {
        this.type = new QualifiedNameExpressionNode((TokenBase)type);
        this.type.setParent(this);
        addToMap(attributeName, getTypeName());
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(type, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 1;
    }

    private String getTypeName()
    {
        return type != null ? type.getName() : "";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TypedTagNode)
        {
            if (!equals(((TypedTagNode)obj).type, type))
                return false;

        }
        else
        {
            return false;
        }
        return super.equals(obj);
    }
}
