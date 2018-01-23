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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;

/**
 * Base container node for a literal that is made up of other literals, like XML
 * or arrays
 */
public interface ILiteralContainerNode extends ILiteralNode
{
    /**
     * @return The node that represents this node's type, Note that this is not
     * considered a child node.
     */
    LiteralNode getBaseTypeNode();

    /**
     * @return The container node that holds the {@link LiteralNode}.
     */
    ContainerNode getContentsNode();
}
