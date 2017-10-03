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

package org.apache.royale.swf.io;

import java.io.Closeable;

/**
 * Read SWF primitive data types from a byte stream.
 */
interface IInputBitStream extends Closeable
{
    /**
     * Discard the data left in the bit value cache. Always call this method
     * after reading bit values and before reading other byte-aligned data.
     */
    void byteAlign();

    /**
     * Get the offset of the read cursor.
     * 
     * @return offset
     */
    long getOffset();

    /**
     * Set the read boundary. Reading beyond the boundary will raise exception.
     * <p>
     * The read boundary can move forward and backward. This is because
     * DefineSprite has nested tags. The read boundary for a DefineSprite tag is
     * at the end of all the nested tags, but it moves backwards when starting
     * to read the nested ones.
     * 
     * @param offset boundary offset to the beginning of the stream.
     */
    void setReadBoundary(long offset);

    /**
     * Get the read boundary.
     * 
     * @return read boundary
     */
    long getReadBoundary();

    /**
     * Read all the bytes from the current position up to the read boundary.
     * This is useful for reading SWF tags with variable length field at the
     * end, like DoABC.
     * 
     * @return byte array
     */
    byte[] readToBoundary();

    /**
     * Read raw bytes.
     * 
     * @param length number of bytes to read
     * @return array of bytes
     */
    byte[] read(int length);

    /**
     * Read 1-bit boolean value.
     * 
     * @return boolean
     */
    boolean readBit();

    /**
     * Read 64-bit floating-point number
     * 
     * @return double
     */
    double readDOUBLE();

    /**
     * Read encoded unsigned 32-bit integer
     * 
     * @return integer
     */
    long readEncodedU32();

    /**
     * Read fixed-point bit values. Fixed-point bit values are 32-bit 16.16
     * signed, fixed-point numbers. That is, the high 16 bits represent the
     * number before the decimal point, and the low 16 bits represent the number
     * after the decimal point. A fixed-point bit value is identical to a
     * signed-bit value, but the interpretation is different. For example, a
     * 19-bit, signed-bit value of 0x30000 is interpreted as 196608 decimal. The
     * 19-bit, fixed-point bit value 0x30000 is interpreted as 3.0. The format
     * of this value is effectively 3.16 rather than 16.16.
     * 
     * @param length number of bits to read
     * @return integer
     */
    float readFB(int length);

    /**
     * Read 32-bit 16.16 fixed point number.
     * 
     * @return fixed point number
     */
    float readFIXED();

    /**
     * Read 16-bit 8.8 fixed point number
     * 
     * @return fixed point number
     */
    float readFIXED8();

    /**
     * Read 32-bit floating-point number
     * 
     * @return float
     */
    float readFLOAT();

    /**
     * Read signed bit values.
     * 
     * @param length number of bits to read
     * @return integer
     */
    int readSB(int length);

    /**
     * Read 16-bit signed integer.
     * 
     * @return integer
     */
    short readSI16();

    /**
     * Read 32-bit signed integer.
     * 
     * @return integer
     */
    int readSI32();

    /**
     * Read 64-bit signed integer.
     * 
     * @return integer
     */
    long readSI64();

    /**
     * Read 8-bit signed integer.
     * 
     * @return integer
     */
    byte readSI8();

    /**
     * Read string.
     * 
     * @return string
     */
    String readString();

    /**
     * Read unsigned bit values.
     * 
     * @param length number of bits to read
     * @return integer
     */
    int readUB(int length);

    /**
     * Read 16-bit unsigned integer.
     * 
     * @return integer
     */
    int readUI16();

    /**
     * Read 24-bit unsigned integer.
     * 
     * @return integer
     */
    int readUI24();

    /**
     * Read 32-bit unsigned integer.
     * 
     * @return integer
     */
    long readUI32();

    /**
     * Read 8-bit unsigned integer.
     * 
     * @return integer
     */
    short readUI8();

}
