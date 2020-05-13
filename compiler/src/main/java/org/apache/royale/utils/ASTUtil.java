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

package org.apache.royale.utils;


import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;

public class ASTUtil {
    
    public static void processFunctionNode(FunctionNode funcNode, ICompilerProject project) {
        if (project instanceof IRoyaleProject) {
            if (funcNode.isImplicit()) {
                //we don't care about implicit nodes (e.g. generated constructors)
                //and they may not have all the 'real' definition data populated anyway, such as contained ASScope
                return;
            }
            ArrayLikeUtil.preProcessGetterSetters(project, funcNode.getScopedNode(), null);
            if (funcNode.getDefinition().getContainedScope().getHasLoopCheck()) {
                //pre-process for 'ArrayLike' for-each mutations
                ArrayLikeUtil.preProcessLoopChecks(funcNode.getDefinition().getContainedScope(), (IRoyaleProject) project);
            }
        }
    }
    
    
}
