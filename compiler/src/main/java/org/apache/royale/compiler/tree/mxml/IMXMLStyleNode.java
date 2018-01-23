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

package org.apache.royale.compiler.tree.mxml;

import java.util.Collection;

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * This AST node represents an MXML <code>&lt;Style&gt;</code> tag.
 * <p>
 * An {@link IMXMLStyleNode} has no child nodes, but it has a reference to a
 * {@link ICSSDocument} object.
 */
public interface IMXMLStyleNode extends IMXMLNode
{
    /**
     * Get the CSS model from the {@code <fx:Style>} tag. The CSS text isn't
     * parsed until this method is called.
     * 
     * @param problems Compiler problem collection.
     * @return An {@link ICSSDocument} object.
     */
    ICSSDocument getCSSDocument(Collection<ICompilerProblem> problems);
}
