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

package org.apache.royale.abc;

/**
 * ABCReader contains the byte-level methods that read ABC bytecode.
 */
class ABCReader
{
    /**
     * Construct a new ABCReader.
     * @param pos - the starting offset in the ABC. 
     * @param abc - the ABC bytecode.
     */
    ABCReader(int pos, byte[] abc)
    {
        this.pos = pos;
        this.abc = abc;
    }

    byte[] abc;

    int pos;

    int readU8()
    {
        return 255 & abc[pos++];
    }

    int readU16()
    {
        return readU8() | readU8() << 8;
    }

    int readS24()
    {
        return readU16() | ((byte)readU8()) << 16;
    }

    int readU30()
    {
        int result = readU8();
        if (0 == (result & 0x00000080))
            return result;
        
        result = result & 0x0000007f | readU8() << 7;
        if (0 == (result & 0x00004000))
            return result;
        
        result = result & 0x00003fff | readU8() << 14;
        if (0 == (result & 0x00200000))
            return result;
        
        result = result & 0x001fffff | readU8() << 21;
        if (0 == (result & 0x10000000))
            return result;
        
        return result & 0x0fffffff | readU8() << 28;
    }

    double readDouble()
    {
        return Double.longBitsToDouble(readU16() | ((long)readU16()) << 16 |
                                       ((long)readU16()) << 32 | ((long)readU16()) << 48);
    }

}
