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

package org.apache.royale.compiler.utils;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * @author Michael Schmalle
 */
public class DefinitionUtils
{
    public static final boolean isMemberDefinition(IDefinition definition)
    {
        return definition != null
                && (definition.getParent() instanceof IClassDefinition || definition
                        .getParent() instanceof IInterfaceDefinition);
    }


    public static final int deltaFromObject(IClassDefinition definition, ICompilerProject project) {
        int ret = -1;
        if (definition != null) {
            return definition.resolveAncestry(project).length - 1;
        }
        return ret;
    }


}
