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

import org.apache.royale.compiler.definitions.ITypeDefinition;

/**
 * This AST node represents an MXML <code>@Resource(...)</code> compiler
 * directive.
 */
public interface IMXMLResourceNode extends IMXMLCompilerDirectiveNodeBase
{
    /**
     * Returns the string representation of the resource bundle name as found in
     * compiler directive
     * 
     * @return resource bundle name.
     */
    String getBundleName();

    /**
     * Returns the string representation of the value of key as found in
     * compiler directive
     * 
     * @return resource bundle name.
     */
    String getKey();

    /**
     * Type of the identifier which its value is set with this compiler
     * directive.
     * 
     * @return left hand identifier type
     */
    ITypeDefinition getType();
}
