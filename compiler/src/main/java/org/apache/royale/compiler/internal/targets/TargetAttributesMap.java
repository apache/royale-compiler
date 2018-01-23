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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * An implementation of {@link ITargetAttributes} based on a Java {@link Map}.
 */
public class TargetAttributesMap extends TargetAttributeBase
{
    /**
     * These attributes are either excluded from the "info" object or require
     * extra processing before added to the map.
     */
    private static ImmutableSet<String> SPECIAL_ATTRIBUTES = ImmutableSet.of(
            ATTRIBUTE_PRELOADER,
            ATTRIBUTE_RUNTIME_DPI_PROVIDER,
            ATTRIBUTE_SPLASH_SCREEN_IMAGE,
            ATTRIBUTE_IMPLEMENTS,
            // The following attributes are included in the "info" object but require extra processing.
            ATTRIBUTE_USE_PRELOADER,
            ATTRIBUTE_BACKGROUND_COLOR);

    /**
     * The backing map.
     */
    private final ImmutableMap<String, String> map;

    /**
     * Root info object attributes.
     */
    private final ImmutableMap<String, String> rootInfoAttributes;

    /**
     * Initialize from a map.
     * 
     * @param map Backing map.
     */
    public TargetAttributesMap(final Map<String, String> map)
    {
        this.map = ImmutableMap.copyOf(map);

        // Select attributes for root info object.
        final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        for (final String key : map.keySet())
        {
            if (!SPECIAL_ATTRIBUTES.contains(key))
                builder.put(key, map.get(key));
        }
        
        this.rootInfoAttributes = builder.build();
    }

    /**
     * Parse the value of the given key as a Boolean.
     * 
     * @param key Attribute name.
     * @return Boolean value or null if {@code key} isn't in the map.
     */
    @Override
    Boolean parseBoolean(final String key)
    {
        final String value = map.get(key);
        if (value == null)
            return null;
        else
            return Boolean.parseBoolean(value);
    }

    /**
     * Parse the value of the given key as a Float.
     * 
     * @param key Attribute name.
     * @return Float value or null if {@code key} isn't in the map.
     */
    @Override
    Float parseFloat(final String key)
    {
        final String value = map.get(key);
        if (value == null)
            return null;
        else
            return Float.parseFloat(value);
    }

    /**
     * Parse the value of the given key as a Integer.
     * 
     * @param key Attribute name.
     * @return Integer value or null if {@code key} isn't in the map.
     */
    @Override
    Integer parseInteger(final String key)
    {
        final String value = map.get(key);
        if (value == null)
            return null;
        else
            return Integer.parseInt(value);
    }

    /**
     * Parse the value of the given key as a String.
     * 
     * @param key Attribute name.
     * @return String value or null if {@code key} isn't in the map.
     */
    @Override
    String parseString(final String key)
    {
        return map.get(key);
    }

    @Override
    Double parsePercentage(final String key)
    {
        final String value = map.get(key);
        if (value == null)
            return null;
        else
            return super.parsePercentageValue(value);
    }

    @Override
    boolean isPercentage(final String key)
    {
        final String value = map.get(key);
        if (value == null)
            return false;
        else
            return super.isPercentageValue(value);
    }

    @Override
    public Map<String, String> getRootInfoAttributes()
    {
        return rootInfoAttributes;
    }
}
