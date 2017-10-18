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

package org.apache.royale.compiler.internal.targets;

import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;

/**
 * Abstract base class that contains information about code that uses the flex
 * framework that is needed by the compiler to generate code in the first frame
 * of library.swfs in royale SWCs.
 * <p>
 * This information is collected by looking at all the {@link ICompilationUnit}s
 * that will be built into a royale SWC.
 */
class RoyaleLibraryFrame1Info extends RoyaleFrame1Info
{

    RoyaleLibraryFrame1Info(RoyaleProject royaleProject,
                          ImmutableSet<ICompilationUnit> compilationUnitsToBuild) throws InterruptedException
    {
        super(royaleProject);

        collectFromCompilationUnits(compilationUnitsToBuild);
    }
}
