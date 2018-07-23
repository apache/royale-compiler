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
import java.io.OutputStream;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.utils.DAByteArrayOutputStream;

import SevenZip.Compression.LZMA.Encoder;

public class LZMACompressor
{
    public LZMACompressor()
    {
        // init the encoder to it is ready for work
        encoder = new Encoder();

        // Note: algorithm doesn't do anything at this time.
        /// value 1 is supposed to be "max"
        if (!encoder.SetAlgorithm(1))
            assert false;

        // Dictionary size
        // This is the default value from the 7Zip example (1 << 21)
        // It is not obvious that making it bigger will give better results. Be aware that the
        // implementation seems to allocate 2X ints, so 8Xbytes.
        int dictionarySize = 1 << 21;
        if (!encoder.SetDictionarySize(dictionarySize))
            assert false;

        // set number of fast bytes - [5, 273], default: 128\n" +
        if (!encoder.SetNumFastBytes(128))
            assert false;

        // -mf{MF_ID}: set Match Finder: [bt2, bt4], default: bt4\n" +
        if (!encoder.SetMatchFinder(1))
            assert false;

        //"  -lc{N}: set number of literal context bits - [0, 8], default: 3\n" +
        //"  -lp{N}: set number of literal pos bits - [0, 4], default: 0\n" +
        //"  -pb{N}: set number of pos bits - [0, 4], default: 2\n" +
        int lc = 3;
        int lp = 0;
        int pb = 2;

        if (!encoder.SetLcLpPb(lc, lp, pb))
            assert false;

        // set marker mode to true, so that we write out 
        // an explicit "end of stream" marker, as per the 
        // SFW LZMA spec
        encoder.SetEndMarkerMode(true);
    }

    // We will compress all of our data into this data structure,
    // so that we can easily stream it out later
    private DAByteArrayOutputStream byteArrayOutputStream = null;

    private final Encoder encoder;

    /**
     * This simple wrapper make an IOutputBitStream look like in InputSteam.
     * LZMA library needs an Input stream, but our SWF infrastructure is already
     * using IOutputBitStream.
     */
    public static class StreamAdapter extends InputStream
    {
        public StreamAdapter(IOutputBitStream outputBitStream)
        {
            // Fetch the raw bytes from outputBitStream, and
            // save them so we can serve them up later
            bytes = outputBitStream.getBytes();
            totalByteCount = outputBitStream.size();
        }

        private final byte[] bytes;
        private final long totalByteCount;
        private int position = 0;

        /**
         * Implemeint InputStream.read()
         */
        @Override
        public int read() throws IOException
        {
            int ret = -1;
            if (position >= totalByteCount)
                ret = -1;
            else
            {
                ret = bytes[position++];
                if (ret < 0)
                    ret += 256; // Java signed byte -> unsigned
            }
            return ret;
        }

        /**
         * @return the number of bytes in the input stream
         */
        long getCount()
        {
            return totalByteCount;
        }
    }

    /**
     * Compresses all of the data in outputBitStream into
     * this.byteArrayOutputStream Must be called before any of the write
     * methods.
     */
    public void compress(IOutputBitStream outputBitStream) throws IOException
    {
        assert byteArrayOutputStream == null;
        byteArrayOutputStream = new DAByteArrayOutputStream();
        StreamAdapter is = new StreamAdapter(outputBitStream);
        encoder.Code(is, byteArrayOutputStream, -1, -1, null);
    }

    /**
     * Write the LZMA compression properties to the output. These are part the
     * the SWF header
     * 
     * @param outputStream The output stream.
     */
    public void writeLZMAProperties(OutputStream outputStream) throws IOException
    {
        encoder.WriteCoderProperties(outputStream);
    }

    /**
     * Write the actual compressed payload, and the EOS bytes at the end
     */
    public void writeDataAndEnd(OutputStream outputStream) throws IOException
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("LZMACompressor waiting for lock in writeDataAndEnd");
        byte[] data = byteArrayOutputStream.getDirectByteArray();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("LZMACompressor waiting for lock in writeDataAndEnd");
        outputStream.write(data, 0, data.length);
        outputStream.flush();
    }

    /**
     * @return the length of the LZMA data, and the final EOS marker
     */
    public long getLengthOfCompressedPayload()
    {
        assert byteArrayOutputStream != null;
        return byteArrayOutputStream.size();
    }

}
