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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.asdoc.IASDocComment;

/**
 * The base class for definitions which can have ASDoc comments.
 */
public interface IDocumentableDefinition extends IDefinition
{
    /**
     * Gets the {@link IASDocComment} for the ASDoc comment attached to
     * this {@link IDocumentableDefinition}.
     * @return The {@link IASDocComment} for the ASDoc comment attached to
     * this {@link IDocumentableDefinition}.
     */
    IASDocComment getExplicitSourceComment();
    
    /**
     * Determines if this node has an explicit comment. Since comments can
     * inherit, the value from getComment might not always be defined on the
     * element itself
     * 
     * @return true if we have an explicit comment
     */
    boolean hasExplicitComment();
}
