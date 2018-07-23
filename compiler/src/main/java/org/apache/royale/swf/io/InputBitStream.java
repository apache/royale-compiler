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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.swf.Header;
import org.apache.royale.utils.DAByteArrayOutputStream;

/**
 * Implementation of {@link InputBitStream}. This implementation allows you to
 * swap the underlying {@code InputStream} source with another one.
 * <p>
 * {@code InputBitStream} doesn't buffer the source InputStream internally. It
 * has one byte buffer for reading SWF bit-values. The buffer is filled 8 bits
 * at a time. All the bit value methods read from this buffer.
 * <p>
 * If a SWF file is compressed, InputBitStream uses an
 * {@link InflaterInputStream} to decompress the source input.
 */
public class InputBitStream extends InputStream implements IInputBitStream
{
    // source
    private InputStream in;

    // bit value cache
    private int bitPos = 0;

    private int bitBuf = 0;
    private long offset = 0;
    private long readBoundary = 0;

    /**
     * Create an {@code InputBitStream}.
     * 
     * @param in source {@code InputStream}
     */
    public InputBitStream(InputStream in)
    {
        this.in = in;
    }

    public InputBitStream(byte[] bytes)
    {
        this.in = new ByteArrayInputStream(bytes);
    }

    /**
     * Discard the data left in the bit value cache. Always call this method
     * after reading bit values and before reading other byte-aligned data.
     */
    @Override
    public void byteAlign()
    {
        bitPos = 0;
    }

    @Override
    public int read() throws IOException
    {
        return readByte();
    }

    @Override
    public byte[] read(int length)
    {
        final byte[] data = new byte[length];
        try
        {
            read(data);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public boolean readBit()
    {
        return readBits(1) != 0;
    }

    /**
     * Read multiple bits as a padded int.
     * 
     * @param length number of bits to read
     * @return The value that was read.
     */
    protected int readBits(int length)
    {
        if (length == 0)
        {
            return 0;
        }

        int bitsLeft = length;
        int result = 0;

        if (bitPos == 0) // no value in the buffer - read a byte
        {
            bitBuf = readUI8();
            bitPos = 8;
        }

        while (true)
        {
            int shift = bitsLeft - bitPos;
            if (shift > 0)
            {
                // Consume the entire buffer
                result |= bitBuf << shift;
                bitsLeft -= bitPos;

                // Get the next byte from the input stream
                bitBuf = readUI8();
                bitPos = 8;
            }
            else
            {
                // Consume a portion of the buffer
                result |= bitBuf >> -shift;
                bitPos -= bitsLeft;
                bitBuf &= 0xff >> (8 - bitPos); // mask off the consumed bits
                return result;
            }
        }
    }

    // The following are SWF primitive type decoder methods.

    /**
     * This helps to mute the {@code IOException}. If there's no data in the
     * source input stream, calling this method will raise an runtime exception.
     * <p>
     * All the methods implementing {@code IInputBitStream} should call this
     * method when consuming the next byte from the input stream.
     * 
     * @return next byte in the input stream
     */
    protected int readByte()
    {
        byteAlign();
        try
        {
            if (offset >= readBoundary)
            {
                throw new RuntimeException(String.format("About to read over or reading over the boundary: %d -> %d.", offset, readBoundary));
            }

            final int n = in.read();
            offset++;
            if (-1 == n)
            {
                throw new RuntimeException("No more data to read.");
            }
            else
            {
                return n;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double readDOUBLE()
    {
        return Double.longBitsToDouble(readSI64());
    }

    @Override
    public long readEncodedU32()
    {
        long decoded = 0;
        for (int i = 0; i < 5; i++)
        {
            final int nextByte = readByte();
            decoded = (decoded << 7) | (nextByte & 0x7f);
            if ((nextByte & 0x80) == 0)
            {
                break;
            }
        }
        return decoded & 0x000000ff << 24 |
               decoded & 0x0000ff00 << 8 |
               decoded & 0x00ff0000 >>> 8 |
               decoded & 0xff000000 >>> 24;
    }

    @Override
    public float readFB(int length)
    {
        // Convert bits to x.16 FIXED point number
        return readSB(length) / (float)0x10000;
    }

    @Override
    public float readFIXED()
    {
        // Convert 32-bit int to 16.16 FIXED point number
        return readSI32() / (float)0x10000;
    }

    @Override
    public float readFIXED8()
    {
        // Convert 16-bit int to 8.8 FIXED point number
        return (short)readSI16() / (float)0x100;
    }

    @Override
    public float readFLOAT()
    {
        return Float.intBitsToFloat(readSI32());
    }

    @Override
    public int readSB(int length)
    {
        int bits = readBits(length);
        return bits << 32 - length >> 32 - length;
    }

    @Override
    public short readSI16()
    {
        return (short)(readByte() | readByte() << 8);
    }

    @Override
    public int readSI32()
    {
        return readByte() |
               readByte() << 8 |
               readByte() << 16 |
               readByte() << 24;
    }

    @Override
    public long readSI64()
    {
        return (readUI32() & 0xFFFFFFFFL) | (readUI32() << 32);
    }

    @Override
    public byte readSI8()
    {
        return (byte)readByte();
    }

    @Override
    public String readString()
    {
        final DAByteArrayOutputStream buffer = new DAByteArrayOutputStream();
        for (int nextByte = readUI8(); nextByte != 0; nextByte = readUI8())
        {
            buffer.write(nextByte);
        }

        try
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("InputBitStream waiting for lock in readString");
            String ret = new String(buffer.getDirectByteArray(), 0, buffer.size(), "UTF-8");
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("InputBitStream done with lock in readString");
            return ret;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(buffer);
        }
    }

    @Override
    public int readUB(int length)
    {
        return readBits(length);
    }

    @Override
    public int readUI16()
    {
        return 0xFFFF & readSI16();
    }

    @Override
    public int readUI24()
    {
        return 0xFFFFFF & (readByte() | readByte() << 8 | readByte() << 16);
    }

    @Override
    public long readUI32()
    {
        return 0xFFFFFFFFl & readSI32();
    }

    @Override
    public short readUI8()
    {
        return (short)(0xFF & readSI8());
    }

    /**
     * Set if the InputStream is a compressed SWF stream.
     */
    public void setCompress(Header.Compression compression) throws IOException
    {
        switch (compression)
        {
            case NONE:
                break;
            case ZLIB:
                this.in = new BufferedInputStream(new InflaterInputStream(in));
                break;
            case LZMA:
                this.in = new LZMAInputStream(in);
                break;
            default:
                assert false;
        }
    }

    @Override
    public byte[] readToBoundary()
    {
        assert readBoundary > 0 : "Must set boundary before readToBoundary";

        // The conversion is safe because a tag length is SI32.
        final int len = (int)(readBoundary - offset);
        final byte[] result = this.read(len);
        return result;
    }

    @Override
    public long getOffset()
    {
        return offset;
    }

    @Override
    public void setReadBoundary(long offset)
    {
        assert offset > 0 : "Read boundary must > 0.";
        this.readBoundary = offset;
    }

    @Override
    public long getReadBoundary()
    {
        return this.readBoundary;
    }
}
