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

package org.apache.royale.compiler.tree.metadata;

import org.apache.royale.compiler.common.IDecoration;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * An AST node representing a single metadata annotation on a declaration.
 * <p>
 * This node has no children.
 * <p>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public var i:int;
 * </pre>
 * is represented as
 * <pre>
 * IVariableNode
 *   IMetaTagsNode
 *     IMetaTagNode "Foo"
 *     IMetaTagNode "Bar"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "var"
 *   IIdentifierNode "i"
 *   IIdentifierNode "int"
 * </pre>
 */
public interface IMetaTagNode extends IASNode, IDecoration, IMetaInfo
{
    static final String SINGLE_VALUE = "single".intern();
}
