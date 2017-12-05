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

package org.apache.royale.compiler.asdoc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * Opaque interface used by {@link IDocumentableDefinitionNode}s and
 * {@link IDocumentableDefinition}s to refer to ASDoc comments. As more ASDoc
 * functionality is ported into the compiler code base, methods can be added to
 * this interface to allow easier access to ASDoc information.
 */
public interface IASDocComment
{
    String getDescription();

    void compile();

    boolean hasTag(String name);

    IASDocTag getTag(String name);

    Map<String, List<IASDocTag>> getTags();

    Collection<IASDocTag> getTagsByName(String name);

    void paste(IASDocComment source);
}
