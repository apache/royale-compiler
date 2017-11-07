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

package org.apache.royale.compiler.internal.driver.wast;

import java.io.IOException;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.targets.ITarget.TargetType;

public class WASTCompilationUnit extends ASCompilationUnit {

	/**
     * Create a compilation unit from an ABC file.
     * 
     * @param project compiler project
     * @param path ABC file path
     * @throws IOException error
     */
    public WASTCompilationUnit(CompilerProject project, String path)
            throws IOException
    {
        this(project, path, DefinitionPriority.BasePriority.LIBRARY_PATH);
    }

    public WASTCompilationUnit(CompilerProject project, String path,
            DefinitionPriority.BasePriority basePriority)
    {
        super(project, path, basePriority);
    }

    public WASTCompilationUnit(CompilerProject project, String path,
            DefinitionPriority.BasePriority basePriority, String qname)
    {
        super(project, path, basePriority, 0, qname);
    }
    
    public Boolean addDependency(String className, DependencyType dt)
    {
        return true;
    }

    @Override
    public void startBuildAsync(TargetType targetType)
    {
        getSyntaxTreeRequest();
        getFileScopeRequest();
        getOutgoingDependenciesRequest();
    }

}
