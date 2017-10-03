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

import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;

/**
 * An AST node representing an embedded asset.
 * <p>
 * When a variable has <code>[Embed]</code> metadata,
 * an <code>IEmbedNode</code> is created to represent the initial value
 * of the variable.
 * <p>
 * For example,
 * <pre>
 * [Embed("flag.jpg")]
 * public var flag:Class;
 * </pre>
 * is represented by
 * <pre>
 * IVariableNode
 *   IMetaTagsNode
 *     IMetaTagNode "Embed"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "var"
 *   IIdentifierNode "flag"
 *   IIdentifierNode "Class"
 *   IEmbedNode
 * </pre>
 * This node has no children.
 */
public interface IEmbedNode extends IExpressionNode
{
    /**
     * Gets the attributes associated with the IEmbedNode
     * 
     * @return an array of IMetaTagAttributes
     */
    IMetaTagAttribute[] getAttributes();
}
