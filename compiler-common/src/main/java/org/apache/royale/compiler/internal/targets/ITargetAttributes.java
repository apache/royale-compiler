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

import java.util.Map;

/**
 * Attributes that changes the target output, such as "frame rate", "SWF width"
 * and "Use GPU". The values come from either special attributes on the main
 * MXML document or the {@code [SWF]} metadata tag in the main ActionScript
 * class.
 */
public interface ITargetAttributes
{
    /**
     * @return SWF width, or (@code null} if not set.
     */
    Float getWidth();
    
    /**
     * @return SWF height, or (@code null} if not set.
     */
    Float getHeight();
    
    /**
     * @return SWF width, or (@code null} if not set.
     */
    Double getWidthPercentage();

    /**
     * @return SWF height, or (@code null} if not set.
     */
    Double getHeightPercentage();

    /**
     * @return SWF frame rate, or {@code null} if not set.
     */
    Float getFrameRate();

    /**
     * @return Preloader class name, or {@code null} if not set.
     */
    String getPreloaderClassName();

    /**
     * @return Runtime DPI provider's class name, or {@code null} if not set.
     */
    String getRuntimeDPIProviderClassName();

    /**
     * @return Splash screen image attribute value, or {@code null} if not set.
     */
    String getSplashScreenImage();

    /**
     * @return page title attribute value, or {@code null} if not set.
     */
    String getPageTitle();

    /**
     * @return Script recursion limit value, or {@code null} if not set.
     */
    Integer getScriptRecursionLimit();

    /**
     * @return Script time limit value, or {@code null} if not set.
     */
    Integer getScriptTimeLimit();

    /**
     * @return UseGPU attribute value, or {@code null} if not set.
     */
    Boolean getUseGPU();

    /**
     * @return UseDirectBlit attribute value, or {@code null} if not set.
     */
    Boolean getUseDirectBlit();

    /**
     * @return UsePreloader attribute value, or {@code null} if not set.
     */
    Boolean getUsePreloader();

    /**
     * @return Raw BackgroundColor attribute value, or
     * {@code null} if not set.
     */
    String getBackgroundColor();

    /**
     * Get all the key/value pairs that can be added to the frame-1 "info"
     * object without special processing.
     * 
     * @return A map of all attribute key/value pairs.
     */
    Map<String, String> getRootInfoAttributes();

}
