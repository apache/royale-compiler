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

package org.apache.flex.compiler.internal.targets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.flex.compiler.driver.js.IJSApplication;
import org.apache.flex.compiler.exceptions.BuildCanceledException;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.driver.js.JSApplication;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.IJSTarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetReport;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class FlexJSTarget extends JSTarget implements IJSTarget
{
    /**
     * Initialize a JS target with the owner project and root compilation units.
     * 
     * @param project the owner project
     */
    public FlexJSTarget(IASProject project, ITargetSettings targetSettings,
            ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
    }

}
