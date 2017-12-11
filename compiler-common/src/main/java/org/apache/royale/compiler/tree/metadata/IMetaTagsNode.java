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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * An AST node representing the collection of metadata annotations on a declaration.
 * <p>
 * The general shape of this node is:
 * <pre>
 * IMetaTagsNode
 *   IMetaTagNode <-- getChild(0)
 *   IMetaTagNode <-- getChild(1)
 *   ...
 * <pre>
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
public interface IMetaTagsNode extends /* IAdaptable, */IASNode
{
    /**
     * Gets all the {@link IMetaTagNode} objects that match the given name
     * 
     * @param name the name to match, such as Event, Style, IconFile, etc
     * @return an array of {@link IMetaTagNode} objects or null
     */
    IMetaTagNode[] getTagsByName(String name);

    /**
     * Returns all the {@link IMetaTagNode} objects as an array
     * 
     * @return an array of objects, or an empty array
     */
    IMetaTagNode[] getAllTags();

    /**
     * Determines if a specific {@link IMetaTagNode} exists in this collection
     * 
     * @param name the name of the tag
     * @return true if it exists
     */
    boolean hasTagByName(String name);

    /**
     * Returns the first {@link IMetaTagNode} matching the given name
     * 
     * @param name the name to search for
     * @return an {@link IMetaTagNode} or null
     */
    IMetaTagNode getTagByName(String name);
}
