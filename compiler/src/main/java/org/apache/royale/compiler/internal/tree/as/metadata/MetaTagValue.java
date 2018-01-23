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

package org.apache.royale.compiler.internal.tree.as.metadata;

import org.apache.royale.compiler.internal.definitions.metadata.MetaTagAttribute;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * One name/value pair from the meta attribute. Subclass of
 * {@link MetaTagAttribute} to add unit test capabilities for trees
 */
class MetaTagValue extends MetaTagAttribute
{

    public MetaTagValue(String key, String value)
    {
        super(key, value);
    }

    public MetaTagValue(String value)
    {
        super(null, value);
    }

    @Override
    public String toString()
    {
        if (this.getKey() != null && this.getValue() != null)
        {
            return this.getKey() + "=" + this.getValue();
        }
        return IMetaTagNode.SINGLE_VALUE;
    }
}
