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

package org.apache.royale.compiler.css;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.css.CSSModelTreeType;

/**
 * All CSS DOM node classes implement this interface to adapt to JBurg's
 * {@code DefaultInodeAdapter}.
 */
public interface ICSSNode extends ISourceLocation
{
    /**
     * @return Tree structure in text.
     */
    String toStringTree();

    /**
     * Get the node's child count. This method will be used by JBurg's
     * {@code DefaultAdapter}.
     * 
     * @return Child count.
     */
    int getArity();

    /**
     * Get the node's nth child. This method will be used by JBurg's
     * {@code DefaultAdapter}.
     * 
     * @param index Child index.
     * @return The nth child node.
     */
    ICSSNode getNthChild(int index);

    /**
     * Get the node's type. This method will be used by JBurg's
     * {@code DefaultAdapter}.
     * 
     * @return Node type.
     */
    CSSModelTreeType getOperator();
}
