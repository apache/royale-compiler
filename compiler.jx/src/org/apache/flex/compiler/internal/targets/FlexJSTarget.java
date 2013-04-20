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

import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.IJSTarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;

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
