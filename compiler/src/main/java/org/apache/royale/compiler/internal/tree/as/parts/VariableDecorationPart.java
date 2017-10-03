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

package org.apache.royale.compiler.internal.tree.as.parts;

import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.KeywordNode;

public class VariableDecorationPart extends DecorationPart
{
    private byte assignedValueId;

    /**
     * a pointer to the keyword for either const or var
     */
    private byte keywordNodeId;

    public VariableDecorationPart()
    {
        super();
        assignedValueId = -1;
        keywordNodeId = -1;
    }

    public void setAssignedValue(ExpressionNodeBase node)
    {
        assignedValueId = insert(node);
    }

    public void setKeywordNode(KeywordNode node)
    {
        keywordNodeId = insert(node);
    }

    public ExpressionNodeBase getAssignedValue()
    {
        Object object = getFromStore(assignedValueId);
        if (object instanceof ExpressionNodeBase)
            return (ExpressionNodeBase)object;
        return null;
    }

    public KeywordNode getKeywordValue()
    {
        Object object = getFromStore(keywordNodeId);
        if (object instanceof KeywordNode)
            return (KeywordNode)object;
        return null;
    }

    @Override
    protected void insertUnknownObject(Object entry, int offset)
    {
        if (entry instanceof ExpressionNodeBase)
        {
            assignedValueId = (byte)offset;
        }
        else if (entry instanceof KeywordNode)
        {
            keywordNodeId = (byte)offset;
        }
    }
}
