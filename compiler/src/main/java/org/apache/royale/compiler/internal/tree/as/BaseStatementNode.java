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

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IStatementNode;

/**
 * Base class for statements that have a conditional, with contents
 */
public abstract class BaseStatementNode extends FixedChildrenNode implements IStatementNode
{
    /**
     * Constructor.
     */
    public BaseStatementNode()
    {
        contentsNode = initBlockNode();
    }

    /**
     * Block node that contains the contents of this statement
     */
    protected BlockNode contentsNode;

    //
    // IStatementNode implementations
    //

    @Override
    public IASNode getStatementContentsNode()
    {
        return contentsNode;
    }

    //
    // Other methods
    //

    protected BlockNode initBlockNode()
    {
        return new BlockNode();
    }

    // TODO Unnecessary given getStatementContentsNode()
    public BlockNode getContentsNode()
    {
        return contentsNode;
    }
}
