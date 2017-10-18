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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Base class for {@link ITargetAttributes}. Subclasses need to implement
 * several {@code parseXXX()} methods in order to adapt the backing container to
 * the interface APIs.
 */
abstract class TargetAttributeBase implements ITargetAttributes
{
    /**
     * valid percentage expressions are: [whitespace] positive-whole-or-decimal-number [whitespace] % [whitespace]
     */
    private static final Pattern percentagePattern = Pattern.compile("\\s*((\\d+)(.(\\d)+)?)\\s*%\\s*");

    
    @Override
    public Float getWidth()
    {
        if (isPercentage(ATTRIBUTE_WIDTH))
            return null;

        return parseFloat(ATTRIBUTE_WIDTH);
    }

    @Override
    public Float getHeight()
    {
        if (isPercentage(ATTRIBUTE_HEIGHT))
            return null;

        return parseFloat(ATTRIBUTE_HEIGHT);
    }

    @Override
    public Double getWidthPercentage()
    {
        if (!isPercentage(ATTRIBUTE_WIDTH))
            return null;

        return parsePercentage(ATTRIBUTE_WIDTH);
    }

    @Override
    public Double getHeightPercentage()
    {
        if (!isPercentage(ATTRIBUTE_HEIGHT))
            return null;

        return parsePercentage(ATTRIBUTE_HEIGHT);
    }

    @Override
    public String getBackgroundColor()
    {
        return parseString(ATTRIBUTE_BACKGROUND_COLOR);
    }

    @Override
    public Float getFrameRate()
    {
        return parseFloat(ATTRIBUTE_FRAME_RATE);
    }

    @Override
    public String getPreloaderClassName()
    {
        return parseString(ATTRIBUTE_PRELOADER);
    }

    @Override
    public String getRuntimeDPIProviderClassName()
    {
        return parseString(ATTRIBUTE_RUNTIME_DPI_PROVIDER);
    }

    @Override
    public Integer getScriptRecursionLimit()
    {
        return parseInteger(ATTRIBUTE_SCRIPT_RECURSION_LIMIT);
    }

    @Override
    public Integer getScriptTimeLimit()
    {
        return parseInteger(ATTRIBUTE_SCRIPT_TIME_LIMIT);
    }

    @Override
    public String getSplashScreenImage()
    {
        return parseString(ATTRIBUTE_SPLASH_SCREEN_IMAGE);
    }

    @Override
    public String getPageTitle()
    {
        return parseString(ATTRIBUTE_PAGE_TITLE);
    }

    @Override
    public Boolean getUseDirectBlit()
    {
        return parseBoolean(ATTRIBUTE_USE_DIRECT_BLIT);
    }

    @Override
    public Boolean getUseGPU()
    {
        return parseBoolean(ATTRIBUTE_USE_GPU);
    }

    @Override
    public Boolean getUsePreloader()
    {
        return parseBoolean(ATTRIBUTE_USE_PRELOADER);
    }

    /**
     * Parse the value of the given value and check if it's a percentage value.
     * 
     * @param value value string
     * @return true if a percentage value, or false if not
     */
    final boolean isPercentageValue(final String value)
    {
        assert (value != null) : "isPercentageValue called on a null value";

        if (value.indexOf('%') >= 0)
            return true;
        else
            return false;
    }

    /**
     * Parse the value of the given key as a percentage value.
     * 
     * @param value value string
     * @return percentage value or null not a valid percentage
     */
    final Double parsePercentageValue(final String value)
    {
        assert (value != null) : "parsePercentageValue called on a null value";

        Matcher m = percentagePattern.matcher(value);

        if (m.matches())
            return Double.valueOf(m.group(1));
        else
            return null;
    }

    /**
     * Parse the value of the given key as a Boolean.
     * 
     * @param key Attribute name.
     * @return Boolean value or null if {@code key} isn't in the map.
     */
    abstract Boolean parseBoolean(final String key);

    /**
     * Parse the value of the given key as a Float.
     * 
     * @param key Attribute name.
     * @return Float value or null if {@code key} isn't in the map.
     */
    abstract Float parseFloat(final String key);

    /**
     * Parse the value of the given key as a Integer.
     * 
     * @param key Attribute name.
     * @return Integer value or null if {@code key} isn't in the map.
     */
    abstract Integer parseInteger(final String key);

    /**
     * Parse the value of the given key as a String.
     * 
     * @param key Attribute name.
     * @return String value or null if {@code key} isn't in the map.
     */
    abstract String parseString(final String key);

    /**
     * Parse the value of the given key as a Percentage.
     * 
     * @param key Attribute name.
     * @return Percentage value or null if {@code key} isn't in the map.
     */
    abstract Double parsePercentage(final String key);

    /**
     * Parse the value of the given key and check if it's a percentage value.
     * 
     * @param key Attribute name.
     * @return true if a percentage value, or false if not or if {@code key}
     * isn't in the map.
     */
    abstract boolean isPercentage(final String key);
}
