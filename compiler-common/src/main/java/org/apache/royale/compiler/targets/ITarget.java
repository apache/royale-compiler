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

package org.apache.royale.compiler.targets;

import java.util.Set;

import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * An ITarget is a promise to build a particular output from the
 * CompilationUnits in an IProject. All ITargets are capable of traversing the
 * dependency graph in their owning project in a manner sufficient to fulfill
 * their process to build a particular output. ITargets are created by IProjects
 * with a specification of which ICompilationUnits and their dependencies should
 * be compiled and added to the output. Each type of IProject may define its own
 * ways of specifying which ICompilationUnits are part of a ITarget.
 * 
 * @see org.apache.royale.compiler.projects.ICompilerProject
 * @see org.apache.royale.compiler.units.ICompilationUnit
 */
public interface ITarget
{
    /**
     * Type constants for each target compiler supports
     */
    enum TargetType
    {
        SWC
        {
            @Override
            public String getExtension()
            {
                return ".swc";
             }
        },
        SWF
        {
            @Override
            public String getExtension()
            {
                return ".swf";
            }
        };
    
        public abstract String getExtension();
    }

    /**
     * Returns the type of this target.
     * 
     * @return type of this target
     */
    TargetType getTargetType();

    /**
     * Returns the settings specific to this target.
     * 
     * @return settings for this target
     */
    ITargetSettings getTargetSettings();

    /**
     * Returns mixins.
     * 
     * @return settings for this target
     */
    Set<ICompilationUnit> getIncludesCompilationUnits() throws InterruptedException;

    /**
     * Returns a report specific to this target.  If the target has been created
     * but not built before getTargetReport() has been called, this method will
     * return null.  If the Target could not be built, null will be returned.
     *
     * @return report on this target
     */
    ITargetReport getTargetReport() throws InterruptedException;

    /**
     * Get the set of metadata names that will be preserved in the target or
     * recorded in the SWC target.
     * 
     * @return The set of metadata names.
     */
    Set<String> getASMetadataNames();
}
