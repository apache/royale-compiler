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

package org.apache.royale.compiler.internal.config;

import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;

/**
 * An interface to configure projects and targets. This interface is 
 * implemented by an object that stores both project and target settings.
 */
public interface IConfigurator
{

    /**
     * Apply the current configuration settings to a given project. If any 
     * configuration problems have a severity of
     * {@linkplain org.apache.royale.compiler.problems.CompilerProblemSeverity#ERROR}, then the project will be 
     * setup with the existing configuration but the project may not be 
     * correct.
     * Call {@linkplain #getConfigurationProblems()} to get a list of
     * problems encountered while processing the configuration.
     * 
     * @param project The project to apply the configuration settings to.
     * 
     * @return true if the configuration contains no errors and the project was
     * successfully configured, false if the configuration contains errors.
     */
    boolean applyToProject(ICompilerProject project);

    /**
     * Get the settings that apply to a given type of target based on the
     * current state of the configuration. If any of the problems have a
     * severity of {@linkplain org.apache.royale.compiler.problems.CompilerProblemSeverity#ERROR}, then null will be
     * returned instead of the target settings. Call {@linkplain
     * #getConfigurationProblems()} to get a list of problems encountered while
     * processing the configuration.
     * 
     * @param targetType
     * @return The configuration settings that are applicable to the target
     * type, null if the configuration contains errors.
     */
    ITargetSettings getTargetSettings(TargetType targetType);

    /**
     * Get the settings that control how compiler problems are reported. These
     * settings control what warnings are reported and how problems are
     * classified into errors and warnings.
     * 
     * @return The configuration settings that affect problem reporting.
     */
    ICompilerProblemSettings getCompilerProblemSettings();
    
    /**
     * Get the list of configuration files that were loaded while processing
     * compiler settings.
     * 
     * @return The file names of loaded configuration files.
     */
    List<String> getLoadedConfigurationFiles();
    
    /**
     * Get the list of configuration files that were specified to be loaded but not found
     * while processing compiler settings.
     * 
     * @return The file names of missing configuration files.
     */
    List<String> getMissingConfigurationFiles();

    /**
     * Validates that the specified compiler options are syntactically correct.
     * The options are specified in the same format as the user specifies
     * configuration options to the command line tools. 
     * 
     * @param args An array of compiler options. May not be null.
     * @param targetType the target for which the configuration will be applied.
     * @return a list of problems found in the configuration arguments. If no
     * problems are found an empty list is returned.
     * @throws NullPointerException if args is null.
     */
    public Collection<ICompilerProblem> validateConfiguration(String[] args, TargetType targetType);

    /**
     * Get the configuration problems. This method should only be called after
     * the configuration has been processed, which means after either
     * applyToProject() or getTargetSettings() has been called.
     * <p>
     * The problems in this list contain both errors and warnings.
     * </p>
     * 
     * @return a collection of non-fatal configuration problems.
     */
    Collection<ICompilerProblem> getConfigurationProblems();
 
    /**
     * Set a path resolver for files used in the command line options and
     * configuration files.
     * 
     * @param pathResolver a path resolver for configuration files.
     */
    void setConfigurationPathResolver(IPathResolver pathResolver);
}
