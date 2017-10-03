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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * A definition representing a <code>const</code> declaration.
 * <p>
 * An <code>IConstantDefinition</code is created from an <code>IVariableNode</code>
 * for which <code>isConst()</code> is <code>true</code>.
 * <p>
 * For example, the declaration
 * <pre>public const N:int = 1;</pre>
 * creates a constant definition whose base name is <code>"N"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is named <code>"int"</code>.
 * <p>
 * A constant definition is contained within a file scope, a package scope,
 * a class scope, an interface scope, or a function scope;
 * it does not contain a scope.
 */
public interface IConstantDefinition extends IVariableDefinition
{
    /**
     * Try to calculate the constant value for this ConstantDefinition.
     * 
     * @param project the project to use to resolve the intializer
     * @return the constant value of this definition, or null if one can't be
     * determined.
     */
    Object resolveValue(ICompilerProject project);
}
