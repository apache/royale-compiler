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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLObjectNode;

/**
 * This AST node represents an MXML &lt;Object&gt; tag. Although
 * {@code MXMLObjectNode} has the same API as {@code MXMLInstanceNode}, it gets
 * codegen'd differently in {@code MXMLDocumentDirectoveProcessor}, to use the
 * <code>newobject</code> opcode.
 */
class MXMLObjectNode extends MXMLInstanceNode implements IMXMLObjectNode
{
    MXMLObjectNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLObjectID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Object;
    }
}
