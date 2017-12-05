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

import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.swf.types.RGB;

/**
 * This interface provides information about the {@link ITarget}
 */
public interface ITargetReport
{
    /**
     * Gets the name of all the sources that are involved in the {@link ITarget}.
     *
     * @return A collection of source names.
     */
    Set<String> getSourceNames();

    /**
     * Gets the names of all the assets that are involved in the {@link ITarget}.
     *
     * @return A collection of asset names.
     */
    Set<String> getAssetNames();

    /**
     * Gets the name of all the libraries that are involved in the
     * {@link ITarget}.
     *
     * @return A collection of library names.
     */
    Set<String> getLibraryNames();

    /**
     * Gets the list of all the top-level, externally-visible definitions in the
     * {@link ITarget}. The sequence represents the order in which the
     * definitions are exported to the frame.
     *
     * If the compilation did not generate a movie, this method returns an
     * empty collection.
     *
     * @return A collection of definition names.
     */
    Set<String> getDefinitionNames();

    /**
     * Gets the list of all RSLs required to run this application.
     * The list is in the order the RSLs will be loaded.
     * This list does not include legacy RSLs specified using
     * -runtime-shared-libraries. Those RSLs are always required.
     * 
     * @return list of RSLs required by the target. 
     */
    List<RSLSettings> getRequiredRSLs();
    
    /**
     * Gets the background color.
     *
     * @return An RGB value.
     */
    RGB getBackgroundColor();

    /**
     * Gets the user-defined width.
     * 
     * @return Width of the application, in pixels. <code>0</code> if it was not
     * specified.
     */
    int getWidth();

    /**
     * Gets the user-defined height.
     * 
     * @return Height of the application, in pixels. <code>0</code> if it was
     * not specified.
     */
    int getHeight();

    /**
     * Gets the user-defined width percentage.
     * 
     * @return Width percentage; <code>0.0</code> if it was not specified.
     */
    double getWidthPercent();

    /**
     * Gets the user-defined height percentage.
     * 
     * @return Height percentage; <code>0.0</code> if it was not specified.
     */
    double getHeightPercent();

    /**
     * Gets the page title.
     * 
     * @return Page title; <code>null</code> if it was not specified.
     */
    String getPageTitle();
}
