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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLLiteralNode;

/**
 * Implementation of {@link IMXMLLiteralNode}.
 */
class MXMLLiteralNode extends MXMLNodeBase implements IMXMLLiteralNode
{
    /**
     * Constructor.
     */
    public MXMLLiteralNode(NodeBase parent, Object value)
    {
        super(parent);

        this.value = value;
    }

    /**
     * An object of type Boolean, Integer, Long, Number, or String; or
     * <code>null</code> to represent the null String.
     */
    private Object value;

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLLiteralID;
    }

    @Override
    public String getName()
    {
        return "Literal";
    }

    /**
     * For debugging only.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        super.buildInnerString(sb);

        Object value = getValue();
        if (value instanceof String)
            sb.append('"');
        sb.append(value);
        if (value instanceof String)
            sb.append('"');

        return true;
    }
}
