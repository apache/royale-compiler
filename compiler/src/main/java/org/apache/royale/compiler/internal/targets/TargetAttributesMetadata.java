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

import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import com.google.common.base.Strings;

/**
 * An implementation of {@link ITargetAttributes} based on a
 * {@link IMetaTagNode}.
 */
public class TargetAttributesMetadata extends TargetAttributeBase
{

    /**
     * Initialize from a {@code IMetaTagNode}.
     * 
     * @param metaTagNode Metadata tag node.
     */
    public TargetAttributesMetadata(final IMetaTagNode metaTagNode)
    {
        assert metaTagNode != null : "meta tag node can't be null";
        this.metaTagNode = metaTagNode;
    }

    /**
     * The metadata tag node that contains the attribute values.
     */
    private final IMetaTagNode metaTagNode;

    /**
     * Parse the value of the given key as a Float.
     * 
     * @param key Attribute name.
     * @return Float value or null if {@code key} isn't in the map.
     */
    @Override
    Float parseFloat(final String key)
    {
        final String value = metaTagNode.getAttributeValue(key);
        if (Strings.isNullOrEmpty(value))
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
        final String value = metaTagNode.getAttributeValue(key);
        if (Strings.isNullOrEmpty(value))
            return null;
        else
            return Integer.parseInt(value);
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
        final String value = metaTagNode.getAttributeValue(key);
        if (Strings.isNullOrEmpty(value))
            return null;
        else
            return Boolean.parseBoolean(value);
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
        return metaTagNode.getAttributeValue(key);
    }

    @Override
    Double parsePercentage(final String key)
    {
        final String value = metaTagNode.getAttributeValue(key);
        if (value == null)
            return null;
        else
            return super.parsePercentageValue(value);
    }

    @Override
    boolean isPercentage(final String key)
    {
        final String value = metaTagNode.getAttributeValue(key);
        if (value == null)
            return false;
        else
            return super.isPercentageValue(value);
    }

    /**
     * {@link IMetaTagNode} doesn't have API to enumerate all attribute keys. If
     * this method is ever needed, we will need to create an implementation of
     * {@link IMetaTagNode} for {@code [SWF]} metadata.
     */
    @Override
    public Map<String, String> getRootInfoAttributes()
    {
        throw new UnsupportedOperationException();
    }
}
