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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * This interface represents the information content of one metadata annotation
 * and is shared by {@link IMetaTagNode} and {@link org.apache.royale.compiler.definitions.metadata.IMetaTag}.
 */
public interface IMetaInfo
{
    /**
     * Gets the name of this metadata annotation (like Event or IconFile)
     * @return meta attribute name
     */
    String getTagName();
    
    /**
     * Returns an in-order list of all of the attributes contained within this {@link IMetaInfo}
     * @return an array of {@link IMetaTagAttribute} objects or an empty array, never null.
     */
    IMetaTagAttribute[] getAllAttributes();

    /**
     * Gets the {@link IMetaTagAttribute} found on the tag for the given key
     * @param key the attribute key we are looking for, or IMetaTagNode.SINGLE_VALUE if the attribute does not have a key
     * @return the {@link IMetaTagAttribute}, or null
     */
    IMetaTagAttribute getAttribute(String key);

    /**
     * Gets the value of the given attribute found on the tag
     * @param key the attribute key we are looking for, or IMetaTagNode.SINGLE_VALUE if the attribute does not have a key.
     * @return the value of said attribute, or null
     */
    String getAttributeValue(String key);
}
