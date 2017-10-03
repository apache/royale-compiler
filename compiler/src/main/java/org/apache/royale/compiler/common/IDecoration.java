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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.tree.as.IDefinitionNode;



/**
 * A {@link IDecoration} is a type of parse tree element that decorates an {@link IDefinitionNode}.  An {@link IDecoration} is not a child of
 * an {@link IDefinitionNode} node, rather it helps change the signature of the {@link IDefinitionNode}
 * These include:
 * -modifiers
 * -namespaces
 * -metadata
 */
public interface IDecoration
{
	/**
	 * Returns the parent that is being decorated by this {@link IDecoration}
	 * @return the {@link IDefinitionNode} representing the parent
	 */
	IDefinitionNode getDecoratedDefinitionNode(); 
}
