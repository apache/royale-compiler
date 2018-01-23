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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition.ClassClassification;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;

/**
 * This interface contains the APIs that are available for both ActionScript and
 * MXML class nodes.
 * <p>
 * {@code IClassNode} extends this with APIs specific to AS trees and
 * {@code IMXMLClassDefinitionNode} extends this with APIs specific to MXML
 * trees.
 */
public interface ICommonClassNode extends ITypeNode, IDocumentableDefinitionNode
{
    /**
     * Returns the class definition corresponding to this class node.
     */
    @Override
    IClassDefinition getDefinition();

    /**
     * Returns the name of this class' base class, if it exists.
     * 
     * @return the name of the base class, or null
     */
    String getBaseClassName();

    /**
     * Returns the names of any interfaces that this class declares it
     * implements. This does not walk up the inheritance chain.
     * 
     * @return an array of interface names, or an empty array
     */
    String[] getImplementedInterfaces();

    /**
     * Retrieve all of the meta attributes (e.g. [Event("")]) that match the
     * given name in the metadata for this class. This does not walk up the
     * inehritance chain.
     * 
     * @param name name of meta attributes to search for (e.g. Event or
     * IconFile)
     * @return array of meta attributes matching that name (or empty array)
     */
    IMetaTag[] getMetaTagsByName(String name);

    /**
     * Returns the classification of this ActionScript class
     * 
     * @return the {@link ClassClassification} object
     */
    ClassClassification getClassClassification();
}
