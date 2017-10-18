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
 * This output bit stream can serialize SWF primitive data types.
 * 
 * @see SWF file format spec v10 - "Basic Data Types"
 */
interface IOutputBitStream extends Closeable
{
    /**
     * Get the value of the current bit position
     * 
     * @return current bit position
     */
    int getBitPos();

    /**
     * Write the bit values in bit buffer onto the output stream. Always call
     * this method in after writing bit values and before writing other
     * byte-aligned data types.
     */
    void byteAlign();

    /**
     * Flush piped output stream. Calling this method automatically flushes bit
     * buffer.
     */
    void flush();

    /**
     * Get the direct byte buffer. The buffer size may be larger than the actual
     * number of bytes buffered. Call {@code size()} to get the effective byte
     * array size.
     * 
     * @return all the bytes in the buffer.
     */
    byte[] getBytes();

    /**
     * Remove all data in the buffer.
     */
    void reset();

    /**
     * Get size of the current buffer.
     * 
     * @return number of bytes in the current buffer.
     */
    int size();

    /**
     * Writes {@code data.length} bytes from the specified byte array to this
     * stream.
     * 
     * @param data raw bytes
     */
    void write(byte[] data);

    /**
     * Writes {@code len} bytes from the specified byte array starting at
     * {@code offset} off to this output stream.
     * 
     * @param data raw bytes
     * @param off offset
     * @param len length
     */
    void write(byte[] data, int off, int len);

    /**
     * Write a boolean value as UB[1].
     * 
     * @param value boolean value
     */
    void writeBit(boolean value);

    /**
     * Write double-precision (64-bit) IEEE Standard 754 compatible. The IEEE
     * 754 standard defines a double as: Sign bit: 1 bit Exponent width: 11 bits
     * Significant precision: 52 bits (53 implicit)
     * 
     * @param value number
     */
    void writeDOUBLE(double value);

    /**
     * Write variable length encoded 32-bit unsigned integer.
     * 
     * @param value integer number
     */
    void writeEncodedU32(long value);

    /**
     * Write fixed-point bit value.
     * 
     * @param value float number
     * @param length number of bits used
     */
    void writeFB(double value, int length);

    /**
     * Write 32-bit 16.16 fixed-point number.
     * 
     * @param value float number
     */
    void writeFIXED(double value);

    /**
     * Write 16-bit 8.8 fixed-point number.
     * 
     * @param value number
     */
    void writeFIXED8(double value);

    /**
     * Write single-precision (32-bit) IEEE Standard 754 compatible. The IEEE
     * 754 standard specifies a binary32 as having: Sign bit: 1 bit Exponent
     * width: 8 bits Significant precision: 24 (23 explicitly stored)
     * 
     * @param value number
     */
    void writeFLOAT(float value);

    /**
     * Write signed bit value.
     * 
     * @param value integer number
     * @param length number of bits used
     */
    void writeSB(int value, int length);

    /**
     * Write signed 16-bit integer value.
     * 
     * @param value integer
     */
    void writeSI16(int value);

    /**
     * Write signed 32-bit integer value.
     * 
     * @param value integer
     */
    void writeSI32(int value);

    /**
     * Write signed 64-bit integer value.
     * 
     * @param value integer
     */
    void writeSI64(long value);

    /**
     * Write signed 8-bit integer value.
     * 
     * @param value integer
     */
    void writeSI8(int value);

    /**
     * Write a null-terminated character string.
     * 
     * @param value string
     */
    void writeString(String value);

    /**
     * Write unsigned bit value.
     * 
     * @param value integer number
     * @param length number of bits used
     */
    void writeUB(int value, int length);

    /**
     * Write unsigned 16-bit integer value.
     * 
     * @param value integer
     */
    void writeUI16(int value);

    /**
     * Write unsigned 24-bit integer value.
     * 
     * @param value integer
     */
    void writeUI24(long value);

    /**
     * Write unsigned 32-bit integer value.
     * 
     * @param value Use signed 64-bit long. The high 32 bits are ignored.
     */
    void writeUI32(long value);

    /**
     * Write unsigned 8-bit integer value.
     * 
     * @param value integer
     */
    void writeUI8(int value);

}
