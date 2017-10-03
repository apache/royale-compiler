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

import java.util.Collections;
import java.util.Map;

/**
 * Nil implementation of {@link ITargetAttributes}.
 */
public class NilTargetAttributes implements ITargetAttributes
{
    private NilTargetAttributes() {}
    public static NilTargetAttributes INSTANCE = new NilTargetAttributes();
    
    @Override
    public Float getWidth()
    {
        return null;
    }

    @Override
    public Float getHeight()
    {
        return null;
    }

    @Override
    public Double getWidthPercentage()
    {
        return null;
    }

    @Override
    public Double getHeightPercentage()
    {
        return null;
    }

    @Override
    public Float getFrameRate()
    {
        return null;
    }

    @Override
    public String getPreloaderClassName()
    {
        return null;
    }

    @Override
    public String getRuntimeDPIProviderClassName()
    {
        return null;
    }

    @Override
    public String getSplashScreenImage()
    {
        return null;
    }

    @Override
    public String getPageTitle()
    {
        return null;
    }

    @Override
    public Integer getScriptRecursionLimit()
    {
        return null;
    }

    @Override
    public Integer getScriptTimeLimit()
    {
        return null;
    }

    @Override
    public Boolean getUseGPU()
    {
        return null;
    }

    @Override
    public Boolean getUseDirectBlit()
    {
        return null;
    }

    @Override
    public Boolean getUsePreloader()
    {
        return null;
    }

    @Override
    public String getBackgroundColor()
    {
        return null;
    }

    @Override
    public Map<String, String> getRootInfoAttributes()
    {
        return Collections.emptyMap();
    }
}
