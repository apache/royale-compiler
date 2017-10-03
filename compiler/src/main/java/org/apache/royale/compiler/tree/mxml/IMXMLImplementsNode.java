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

import org.apache.royale.compiler.tree.as.IIdentifierNode;

/**
 * This AST node represents an <code>implements</code> attribute on a class
 * definition tag in MXML.
 * <p>
 * It has N children, each of which is an {@link IIdentifierNode} representing a
 * single interface such as <code>IMyInterface</code> or
 * <code>flash.events.IEventDispatcher</code>.
 */
public interface IMXMLImplementsNode extends IMXMLNode
{
    /**
     * Gets the identifier nodes representing the implemented interfaces; these
     * are the children of this node.
     * 
     * @return An array of {@link IIdentifierNode} objects representing the
     * implemented interfaces.
     */
    IIdentifierNode[] getInterfaceNodes();
}
