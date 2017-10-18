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

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.internal.tree.as.ModifiersContainerNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;

public interface IDecorationPart
{
    void compact();

    /**
     * Sets the {@link IASDocComment} field to point to the specified {@link IASDocComment}.
     * @param comment The {@link IASDocComment} to point at.
     */
    void setASDocComment(IASDocComment comment);

    /**
     * @return Node that represents the comment range
     */
    IASDocComment getASDocComment();

    /**
     * @return The metadata attributes associated with this node (e.g.
     * [Event("foo")]. These attributes aren't contained within the node itself,
     * but are a preceding sibling (since it's possible for one set of metadata
     * attributes to apply to more than one variable, or to both a getter and
     * setter.
     */
    MetaTagsNode getMetadata();

    /**
     * @return The modifiers associated with this node. These modifiers aren't
     * contained within the node itself, but are a preceding sibling (since it's
     * possible for one set of modifiers to apply to more than one variable.
     */
    ModifiersContainerNode getModifiers();

    /**
     * @return The namespace associated with this node.
     */
    INamespaceDecorationNode getNamespace();

    /**
     * set the modifiers
     * 
     * @param set
     */
    void setModifiers(ModifiersContainerNode set);

    void setMetadata(MetaTagsNode meta);

    void setNamespace(INamespaceDecorationNode ns);
}
