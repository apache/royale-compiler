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

package org.apache.royale.compiler.definitions.metadata;

import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * One IMetaTag corresponds to a single metadata annoation (such as
 * <code>[Event(name="click", type="flash.events.MouseEvent")])
 * that applies to a given class, interface, variable, or method.
 */
public interface IMetaTag extends IMetaInfo, ISourceLocation
{
    // TODO Eliminate this.
    static final String SINGLE_VALUE = "single";

    /**
     * The special metadata name that indicates source location of a definition.
     */
    static final String GO_TO_DEFINITION_HELP = "__go_to_definition_help";
    static final String GO_TO_DEFINITION_HELP_POS = "pos";

    /**
     * Gets the definition to which this metadata annotation is attached.
     */
    IDefinition getDecoratedDefinition();

    /**
     * If the metadata annotation specifies a single keyless value, this method
     * returns that value. Otherwise, it returns <code>null</code>.
     * 
     * @return The keyless value.
     */
    String getValue();

    /**
     * Gets the {@code IMetaTagNode} that produced this {@code IMetaTag}.
     */
    IMetaTagNode getTagNode();
}
