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

/**
 * This interface represents definition which are themselves defined by
 * metadata, such as definitions for events, styles, and effects.
 */
public interface IMetadataDefinition extends IDocumentableDefinition
{
    /**
     * Gets the definition that this metadata is decorating.
     * 
     * @return An {@code IDefinition} object.
     */
    IDefinition getDecoratedDefinition();

    /**
     * Gets the value of the <code>deprecatedSince</code> attribute.
     * 
     * @return The attribute value as a String, or <code>null</code> if there is
     * no such attribute.
     */
    String getDeprecatedSince();

    /**
     * Gets the value of the <code>deprecatedReplacement</code> attribute.
     * 
     * @return The attribute value as a String, or <code>null</code> if there is
     * no such attribute.
     */
    String getDeprecatedReplacement();

    /**
     * Gets the value of the <code>deprecatedMessage</code> attribute.
     * 
     * @return The attribute value as a String, or <code>null</code> if there is
     * no such attribute.
     */
    String getDeprecatedMessage();
}
