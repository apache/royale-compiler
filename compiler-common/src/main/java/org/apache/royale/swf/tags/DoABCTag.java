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
 * Represents a <code>DoABC</code> tag in a SWF file.
 * <p>
 * DoABC tag defines a series of bytecodes to be executed. The bytecodes
 * contained within the DoABC tag run in the ActionScript 3.0 virtual machine.
 */
public final class DoABCTag extends Tag
{
    /**
     * Constructor.
     */
    public DoABCTag()
    {
        super(TagType.DoABC);
    }

    /**
     * Constructor and initializer.
     */
    public DoABCTag(long flags, String name, byte[] abcData)
    {
        this();
        this.flags = flags;
        this.name = name;
        this.abcData = abcData;
    }

    private long flags;
    private String name;
    private byte[] abcData;

    /**
     * A 32-bit flags value, which may contain the following bits set:
     * <ul>
     * <li><b>kDoABCLazyInitializeFlag = 1</b> Indicates that the ABC block
     * should not be executed immediately, but only parsed. A later finddef may
     * cause its scripts to execute.</li>
     * </ul>
     * 
     * @return flag value
     */
    public long getFlags()
    {
        return flags;
    }

    /**
     * Set the flag value.
     * 
     * @param flags flag value
     */
    public void setFlags(long flags)
    {
        this.flags = flags;
    }

    /**
     * The name assigned to the bytecode.
     * 
     * @return bytecode's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the bytecode's name.
     * 
     * @param name bytecode's name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * A block of .abc bytecode to be parsed by the ActionScript 3.0 virtual
     * machine, up to the end of the tag.
     * 
     * @return ABC bytes
     */
    public byte[] getABCData()
    {
        return abcData;
    }

    /**
     * Set the ABC bytecode.
     * 
     * @param abcData bytecode
     */
    public void setABCData(byte[] abcData)
    {
        this.abcData = abcData;
    }

    @Override
    protected String description()
    {
        return String.format("\"%s\", %.2f kb", name, abcData.length / 1024f);
    }

}
