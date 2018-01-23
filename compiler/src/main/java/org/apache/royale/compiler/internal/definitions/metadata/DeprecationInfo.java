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

package org.apache.royale.compiler.internal.definitions.metadata;

import org.apache.royale.compiler.definitions.metadata.IDeprecationInfo;

/**
 * Implementation of the {@link IDeprecationInfo} interface.
 */
public class DeprecationInfo implements IDeprecationInfo
{
    /**
     * Constructor.
     */
    public DeprecationInfo(String replacement, String since, String message)
    {
        this.replacement = replacement;
        this.since = since;
        this.message = message;
    }

    private final String replacement;
    private final String since;
    private final String message;

    @Override
    public String getReplacement()
    {
        return replacement;
    }

    @Override
    public String getSince()
    {
        return since;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}
