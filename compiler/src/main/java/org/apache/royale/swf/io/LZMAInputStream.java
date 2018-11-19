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
import java.io.InputStream;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.utils.DAByteArrayOutputStream;

import SevenZip.Compression.LZMA.Decoder;

/**
 * Takes an input stream of LZMA compressed bytes, provides a stream on
 * uncompressed data.
 */
public class LZMAInputStream extends InputStream
{

    /**
     * At the point this function is called, we should have read the 12 byte SWF
     * Header already.
     */
    public LZMAInputStream(InputStream inputStream) throws IOException
    {
        decoder = new Decoder();
        this.inputStream = inputStream;
        initDecode();
    }

    private final Decoder decoder;
    private final InputStream inputStream;
    private byte[] buffer = null;
    private int readIndex;

    @Override
    public int read() throws IOException
    {
        if (readIndex >= buffer.length)
            return -1;
        else
        {
            int ret = buffer[readIndex++];
            if (ret < 0) // convert signed byte to unsigned byte
                ret += 256;
            return ret;
        }
    }

    /**
     * set up the LZMA decoder, then decompress the entire file into memory
     * 
     * @throws IOException
     */
    private void initDecode() throws IOException
    {
        int propertiesSize = 5;
        byte[] properties = new byte[propertiesSize];

        if (inputStream.read(properties, 0, propertiesSize) != propertiesSize)
            throw new IOException("input .lzma file is too short");

        if (!decoder.SetDecoderProperties(properties))
            throw new IOException("Incorrect stream properties");

        /* swf omits the len field */

        DAByteArrayOutputStream os = new DAByteArrayOutputStream();

        long outSize = -1;
        if (!decoder.Code(inputStream, os, outSize))
            throw new IOException("Error in data stream");

        os.flush();
        readIndex = 0;
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("LZMAInputStream waiting for lock in initDecode");
        buffer = os.getDirectByteArray();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("LZMAInputStream waiting for lock in initDecode");
    }
}
