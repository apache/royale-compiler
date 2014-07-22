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

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;

/**
 * Abstract base class that contains information about code that uses the flex
 * framework that is needed by the compiler to generate code in the first frame
 * of library.swfs in flex SWCs.
 * <p>
 * This information is collected by looking at all the {@link ICompilationUnit}s
 * that will be built into a flex SWC.
 */
class FlexLibraryFrame1Info extends FlexFrame1Info
{

    FlexLibraryFrame1Info(FlexProject flexProject,
                          ImmutableSet<ICompilationUnit> compilationUnitsToBuild) throws InterruptedException
    {
        super(flexProject);

        collectFromCompilationUnits(compilationUnitsToBuild);
    }
}
