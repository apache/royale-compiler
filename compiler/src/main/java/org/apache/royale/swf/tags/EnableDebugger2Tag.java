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

package org.apache.royale.swf.tags;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>EnableDebugger2</code> tag in a SWF file.
 * <p>
 * The {@code EnableDebugger2} tag enables debugging. The Password field is
 * encrypted by using the MD5 algorithm.
 * <p>
 * Note that, the old "swfutils" always set the "Reserved" field to a constant:
 * 
 * <pre>
 * // This corresponds to the constant used in the player,
 * // core/splay.cpp, in ScriptThread::EnableDebugger().
 * tagw.writeUI16(0x1975);
 * </pre>
 */
public class EnableDebugger2Tag extends Tag implements IManagedTag
{
    public static final int RESERVED_FIELD_VALUE = 0x1975;

    /**
     * Constructor.
     */
    public EnableDebugger2Tag()
    {
        super(TagType.EnableDebugger2);
    }

    /**
     * Constructor with initialization.
     */
    public EnableDebugger2Tag(String password)
    {
        this();
        this.password = password;
    }

    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
