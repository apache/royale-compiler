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
 * A definition representing a <code>function get</code> or
 * <code>function set</code> declaration.
 * <p>
 * This is a base interface for <code>IGetterDefinition</code>
 * and <code>ISetterDefinition</code>.
 * An <code>IAccessorDefinition</code> is created from an <code>IGetterNode</code>
 * or an <code>ISetterNode</code>.
 */
public interface IAccessorDefinition extends IFunctionDefinition, IVariableDefinition
{
    IAccessorDefinition resolveCorrespondingAccessor(ICompilerProject project);
    
    boolean isProblematic();
    void setIsProblematic(boolean value);
}
