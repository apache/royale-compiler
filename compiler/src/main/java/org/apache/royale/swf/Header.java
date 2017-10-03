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

package org.apache.royale.swf;

import java.util.Arrays;

import org.apache.royale.swf.types.Rect;

/**
 * Represents the header of a SWF file.
 */
public class Header
{
    public static final char[] SIGNATURE_COMPRESSED_ZLIB = new char[] {'C', 'W', 'S'};
    public static final char[] SIGNATURE_COMPRESSED_LZMA = new char[] {'Z', 'W', 'S'};
    public static final char[] SIGNATURE_UNCOMPRESSED = new char[] {'F', 'W', 'S'};

    /**
     * Constructor.
     */
    public Header()
    {
    }
    
    private char[] signature;
    private int version;
    private long length;
    private long compressedLength;
    private Rect frameSize = new Rect(0, 0, 0, 0); // so that buildEmptySWF() doesn't NPE
    private float frameRate;
    private int frameCount;
    
    public enum Compression
    {
        NONE, ZLIB, LZMA
    }

    /**
     * SWF signature:
     * <ul>
     * <li>CWS - compressed</li>
     * <li>FWS - uncompressed</li>
     * </ul>
     * 
     * @return signature string
     */
    public char[] getSignature()
    {
        return signature;
    }

    /**
     * Determine if the signature is valid.
     * 
     * @param signature The signature to be checked.
     * @return true if the signature is valid, false otherwise.
     */
    public boolean isSignatureValid(char[] signature)
    {
        return (Arrays.equals(signature, SIGNATURE_COMPRESSED_ZLIB) ||
                Arrays.equals(signature, SIGNATURE_COMPRESSED_LZMA) ||
                Arrays.equals(signature, SIGNATURE_UNCOMPRESSED));
    }

    public void setSignature(char[] signature)
    {
        if (isSignatureValid(signature))
            this.signature = signature;
        else
            throw new IllegalArgumentException("Invalid SWF signature: " + new String(signature));
    }

    /**
     * SWF file version.
     * 
     * @return version
     */
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    /**
     * Length of entire file in bytes. If the SWF file is compressed, the length
     * is the size after decompression.
     * 
     * @return file length
     */
    public long getLength()
    {
        return length;
    }

    /**
     * Length is the LZMA "compressedLength" from the header
     */
    public long getCompressedLength()
    {
        assert this.getCompression() == Compression.LZMA; // this field only valid for LZMA
        return this.compressedLength;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    public void setCompressedLength(long length)
    {
        this.compressedLength = length;
    }

    /**
     * Frame size in twips.
     * 
     * @return frame size
     */
    public Rect getFrameSize()
    {
        return frameSize;
    }

    public void setFrameSize(Rect frameSize)
    {
        this.frameSize = frameSize;
    }

    /**
     * Get frame rate.
     * 
     * @return frame rate
     */
    public float getFrameRate()
    {
        return frameRate;
    }

    public void setFrameRate(float frameRate)
    {
        this.frameRate = frameRate;
    }

    /**
     * Get frame count.
     * 
     * @return frame count
     */
    public int getFrameCount()
    {
        return frameCount;
    }

    public void setFrameCount(int frameCount)
    {
        this.frameCount = frameCount;
    }

    /**
     * Check if the SWF file uses compression.
     * 
     * @return true if compressed
     */
    public Compression getCompression()
    {
        if (Arrays.equals(signature, SIGNATURE_COMPRESSED_LZMA))
        {
            return Compression.LZMA;
        }
        else if (Arrays.equals(signature, SIGNATURE_COMPRESSED_ZLIB))
        {
            return Compression.ZLIB;
        }
        else
        {
            assert Arrays.equals(signature, SIGNATURE_UNCOMPRESSED);
            return Compression.NONE;
        }
    }

    /**
     * Based on desire for compression, debug, and swf version, pick the
     * appropriate compression type
     */
    public static Compression decideCompression(boolean compressed, int swfVersion, boolean debug)
    {
        Compression ret = Compression.NONE;
        if (compressed)
        {
            if (debug)
            {
                // Use zlib for debug, as it is much, much faster
                ret = Compression.ZLIB;
            }
            else
            {
                // LZMA is best - use it if the SWF version supports it.
                // Otherwise fall back to ZLIB
                ret = (swfVersion >= 13) ? Compression.LZMA : Compression.ZLIB;
            }
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return String.format(
                "[SWF HEADER]\n" +
                        "  Signature: %s\n" +
                        "  Version: %d\n" +
                        "  Length: %d\n" +
                        "  Frame size: %s\n" +
                        "  Frame rate: %.2f\n" +
                        "  Frame count: %d\n",
                new String(signature), version, length, frameSize, frameRate, frameCount);
    }
}
