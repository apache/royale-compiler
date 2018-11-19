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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.utils.DAByteArrayOutputStream;

/**
 * The output stream that can write SWF primitive data types. It contains an
 * in-memory buffer. The buffer is optionally compressed.
 */
public class OutputBitStream implements IOutputBitStream
{

    // optional filter for compression
    private final OutputStream filteredOutput;

    // final byte stream
    private final DAByteArrayOutputStream flatOutputBuffer;

    // Bit buffer pointer. Must start as a full byte with value of 8
    private int bitPos = 8;

    // Bit buffer.
    private byte currentByte = 0x00;

    // True if the output SWF stream is compressed.
    private final boolean useCompression;

    /**
     * Create an uncompressed {@code OutputBitStream}.
     */
    public OutputBitStream()
    {
        this(false);
    }

    /**
     * Create an {@code OutputBitStream}.
     * 
     * @param useCompression true if the output stream is compressed.
     */
    public OutputBitStream(boolean useCompression)
    {
        this.useCompression = useCompression;
        flatOutputBuffer = new DAByteArrayOutputStream();
        if (useCompression)
        {
            filteredOutput = new DeflaterOutputStream(flatOutputBuffer);
        }
        else
        {
            // skip compression filter
            filteredOutput = this.flatOutputBuffer;
        }
    }

    @Override
    public int getBitPos()
    {
        return bitPos;
    }

    @Override
    public void byteAlign()
    {
        if (bitPos != 8)
        {
            writeByte(currentByte);
            currentByte = 0;
            bitPos = 8;
        }
    }

    /**
     * Close internal output buffer.
     */
    @Override
    public void close() throws IOException
    {
        filteredOutput.close();
    }

    /**
     * Flush piped output stream. Calling this method automatically flushes bit
     * buffer.
     */
    @Override
    public void flush()
    {
        byteAlign();
        try
        {
            if (useCompression)
            {
                ((DeflaterOutputStream)filteredOutput).finish();
            }
            filteredOutput.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getBytes()
    {
        flush();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("OutputBitStream waiting for lock in getBytes");
        byte[] b = flatOutputBuffer.getDirectByteArray();
	   	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
			System.out.println("OutputBitStream waiting for lock in getBytes");
		return b;
    }

    @Override
    public void reset()
    {
        flatOutputBuffer.reset();
    }

    @Override
    public int size()
    {
        return flatOutputBuffer.size();
    }

    /**
     * Get the bytes in the final output stream. This method create a copy of
     * the buffer.
     * 
     * @return a copy of buffered bytes in the output stream.
     */
    public byte[] toByteArray()
    {
        return flatOutputBuffer.toByteArray();
    }

    @Override
    public void write(byte[] data)
    {
        try
        {
            filteredOutput.write(data);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] data, int off, int len)
    {
        try
        {
            filteredOutput.write(data, off, len);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBit(boolean data)
    {
        writeUB(data ? 1 : 0, 1);
    }

    private void writeBits(int data, int size)
    {
        while (size > 0)
        {
            if (size > bitPos)
            {
                //more bits left to write than shift out what will fit
                currentByte |= data << (32 - size) >>> (32 - bitPos);

                // shift all the way left, then right to right
                // justify the data to be or'ed in
                writeByte(currentByte);
                size -= bitPos;
                currentByte = 0;
                bitPos = 8;
            }
            else
            {
                currentByte |= data << (32 - size) >>> (32 - bitPos);
                bitPos -= size;
                size = 0;

                if (bitPos == 0)
                {
                    // current byte is filled
                    writeByte(currentByte);
                    currentByte = 0;
                    bitPos = 8;
                }
            }
        }
    }

    /**
     * Write the lower 8 bits of a 32-bit integer as a byte onto the output
     * stream. This function mute the IOException.
     * 
     * @param value byte value
     */
    private void writeByte(int value)
    {
        try
        {
            filteredOutput.write(value);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void writeByte(long value)
    {
        writeByte((int)value);
    }

    @Override
    public void writeDOUBLE(double value)
    {
        writeSI64(Double.doubleToLongBits(value));
    }

    @Override
    public void writeEncodedU32(long value)
    {
        value &= 0xffffffffL;
        do
        {
            byte fragment = (byte)(value & 0x7f);
            value >>= 7;
            if (value > 0)
            {
                fragment |= 0x80;
            }
            writeByte(fragment);
        }
        while (value > 0);

    }

    @Override
    public void writeFB(double data, int size)
    {
        final int bits = (int)(data * 0x10000);
        writeSB(bits, size);
    }

    @Override
    public void writeFIXED(double value)
    {
        final int bytes = (int)(value * 0x010000) & 0xffffffff;
        writeUI32(bytes);
    }

    @Override
    public void writeFIXED8(double value)
    {
        final int bytes = (int)(value * 0x0100) & 0xffff;
        writeUI16(bytes);
    }

    @Override
    public void writeFLOAT(float value)
    {
        writeSI32(Float.floatToIntBits(value));
    }

    @Override
    public void writeSB(int data, int size)
    {
        assert (data >= -(1 << (size - 1)) && data <= (1 << (size - 1)) - 1);
        writeBits(data, size);
    }

    @Override
    public void writeSI16(int value)
    {
        writeByte(value);
        writeByte(value >> 8);
    }

    @Override
    public void writeSI32(int value)
    {
        writeByte(value);
        writeByte(value >> 8);
        writeByte(value >> 16);
        writeByte(value >> 24);
    }

    @Override
    public void writeSI64(long value)
    {
        writeSI32((int)value);
        writeSI32((int)(value >> 32));
    }

    @Override
    public void writeSI8(int value)
    {
        writeByte(value);
    }

    @Override
    public void writeString(String value)
    {
        try
        {
            filteredOutput.write(value.getBytes("UTF-8"));
            filteredOutput.write(0);

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeUB(int data, int size)
    {
        assert (data >= 0 && data <= (1 << size) - 1);
        writeBits(data, size);
    }

    @Override
    public void writeUI16(int value)
    {
        writeSI16(value);
    }

    @Override
    public void writeUI24(long value)
    {
        writeByte(value);
        writeByte(value >> 8);
        writeByte(value >> 16);
    }

    @Override
    public void writeUI32(long value)
    {
        writeByte(value);
        writeByte(value >> 8);
        writeByte(value >> 16);
        writeByte(value >> 24);
    }

    @Override
    public void writeUI8(int value)
    {
        writeByte(value);
    }

}
