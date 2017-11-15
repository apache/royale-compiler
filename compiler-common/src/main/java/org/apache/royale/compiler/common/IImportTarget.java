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

import org.apache.royale.compiler.definitions.INamespaceDefinition;

/**
 * Represents the target of an import statement (e.g. import mx.controls.Button or import mx.controls.*)
 * and encapsulates the logic involved in combining a reference (Button or mx.controls.Button) with
 * the import target to get a fully qualified name. You can create import targets from the import
 * target string (with the constructor), from a package name, or from an XML namespace. 
 */
public interface IImportTarget {

	/**
	 * Interpret the reference based on this import target, and try to build a qualified
	 * class name for it.  If no logical path can be constructed from the two (because the
	 * reference contains package information that doesn't match the import target, or
	 * because the reference class doesn't match the import target class), this method
	 * returns null.  The one exception to this rule (for convenience) is the import target
	 * "*", which will always return the reference itself
	 * Examples (import target + reference):
	 * 		mx.controls.* + Button => mx.controls.Button
	 * 		mx.core.* + Button => mx.core.Button
	 * 		mx.controls.Button + Button => mx.controls.Button
	 * 		mx.controls.RadioButton + Button => (null)
	 * 		* + Button => Button
	 * 		mx.controls.* + mx.controls.Button => mx.controls.Button
	 * 		mx.core.* + mx.controls.Button => (null)
	 * 		mx.controls.Button + mx.controls.Button => mx.controls.Button
	 * 		mx.controls.RadioButton + mx.controls.Button => (null)
	 * 		* + mx.controls.Button => mx.controls.Button
	 * @param reference		the reference as it appears in the document
	 * @return				a qualified name for the reference if it makes sense; null if it doesn't
	 */
	String getQualifiedName(String reference);

	/**
	 * Returns the name of the target package we are targeting
	 * @return the target package
	 */
	String getTargetPackage();

	/**
	 * True if this import is a wild card
	 * @return true if we are a wild card
	 */
	boolean isWildcard();

	/**
	 * Gets the name of the target in our import.  In flash.events.EventDispatcher, it would be EventDispatcher
	 * @return the target name.
	 */
	String getTargetName();
	
	/**
	 * Gets the {@link INamespaceDefinition} for the imported package's public namespace.
	 * @return The {@link INamespaceDefinition} for the imported package's public namespace.
	 */
	INamespaceDefinition getNamespace();
}
