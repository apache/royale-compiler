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

/**
 * This AST node represents an MXML <code>&lt;Reparent&gt;</code> tag.
 * <p>
 * An {@link IMXMLReparentNode} has no child nodes.
 */
public interface IMXMLReparentNode extends IMXMLNode
{
    /**
     * The states (or state groups) in which this reparenting operation applies.
     * 
     * @return An array of Strings which are the names of states or state
     * groups.
     */
    String[] getIncludeIn();

    /**
     * The states (or state groups) in which this reparenting operation does not
     * apply.
     * 
     * @return An array of Strings which are the names of states or state
     * groups.
     */
    String[] getExcludeFrom();

    /**
     * The target of the reparenting operation.
     * 
     * @return A String which is the id of another component in the document.
     */
    String getTarget();
}
