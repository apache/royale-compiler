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
import org.apache.royale.compiler.tree.as.IParameterNode;

/**
 * A definition representing a parameter declaration.
 * <p>
 * An <code>IParameterDefinition</code> is created from an <code>IParameterNode</code>.
 * <p>
 * For example, the function declaration
 * <pre>public function f(i:int):void;</pre>
 * creates one parameter definition whose base name is <code>"i"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is named <code>"int"</code>.
 */
public interface IParameterDefinition extends IVariableDefinition
{
    /**
     * Returns true if this argument is a rest-style argument, signified by ...
     * 
     * @return true if we are a restful argument
     */
    boolean isRest();

    boolean hasDefaultValue();

    /**
     * Attempt to resolve the default value assigned to this parameter. This may
     * cause the AST for the definition to get reloaded, so could be slow.
     * 
     * @param project the Project to resolve things in
     * @return The constant value of the default value, if one can be
     * determined, or null if it can't be determined. This will be a String,
     * int, double, boolean, or Namespace depending on what the initial value
     * was. The value could also be
     * org.apache.royale.abc.ABCConstants.UNDEFINED_VALUE if the initial
     * value was the undefined constant value Callers will need to use
     * instanceof to see what type the value is.
     */
    Object resolveDefaultValue(ICompilerProject project);

    /**
     * Returns the {@link IParameterNode} from which this definition was
     * created, if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    IParameterNode getNode();
}
