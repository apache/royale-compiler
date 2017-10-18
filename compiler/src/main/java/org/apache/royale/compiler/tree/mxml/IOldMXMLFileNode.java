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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFileNode;


/**
 * Represents an {@link IFileNode} that is an MXML file
 */
public interface IOldMXMLFileNode extends IFileNode {

	/**
	 * Returns the {@link IClassDefinition} that this MXML file represents.  Note: this will be the main IClassDefinition that is public, matching
	 * the name of the MXML file.  This is a convince method that is similar to calling: getAllTopLevelDefinitions(true, true) and 
	 * finding the first {@link IDefinitionNode} that is an {@link IClassDefinition}
	 * @return an {@link IClassDefinition}
	 */
	public IClassDefinition getIClass();
}
