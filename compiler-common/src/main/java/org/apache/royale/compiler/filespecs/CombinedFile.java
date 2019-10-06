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

package org.apache.royale.compiler.filespecs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.royale.utils.FilenameNormalization;

/**
 * This is an {@link IFileSpecification} that include multiple source files into
 * one. JFlex-generated tokenizers can skip BOM at the beginning of a file, but
 * not elsewhere. This class skips BOM header in each source file so that the
 * merged file won't have a BOM in the middle of a file.
 */
public class CombinedFile implements IFileSpecification
{

    /**
     * Create a combined file by concatenating {@code includedFilenames}
     * together and append {@code sourceFilename} at the end.
     * 
     * @param includedFilenames included files
     * @param sourceFilename source file
     */
    public CombinedFile(final List<String> includedFilenames, final String sourceFilename)
    {
        this.combinedSource = null;
        this.sourceFilename = FilenameNormalization.normalize(sourceFilename);
        this.fileList = includedFilenames;
        this.fileList.add(sourceFilename);
    }

    private StringBuilder combinedSource;
    private final String sourceFilename;
    private final List<String> fileList;

    /**
     * The path of the combined file is the path of the source file.
     */
    @Override
    public String getPath()
    {
        return sourceFilename;
    }

    /**
     * Create a {@link Reader} from the combined source text.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Reader createReader() throws FileNotFoundException
    {
        if (combinedSource == null)
            combineFile();
        return new StringReader(combinedSource.toString());
    }

    /**
     * Get the time stamp of the most recent modified file among the source
     * files.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public long getLastModified()
    {
        long lastModified = 0;

        for (final String file : fileList)
        {
            final long timestamp = new File(file).lastModified();
            lastModified = Math.max(lastModified, timestamp);
        }

        return lastModified;
    }

    /**
     * Always return false because the combined file is cached in memory.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isOpenDocument()
    {
        return false;
    }

    /**
     * Concatenate source files together. The main source file is always at the
     * end.
     * 
     * @throws FileNotFoundException error
     */
    private void combineFile() throws FileNotFoundException
    {
        assert combinedSource == null : "Do not call combineFile() twice.";

        combinedSource = new StringBuilder();

        for (final String filename : fileList)
        {
            Reader reader = null;
            try
            {
                final BufferedInputStream strm = getStreamAndSkipBOM(filename);
                reader = new InputStreamReader(strm);
                combinedSource.append(IOUtils.toString(reader));
                combinedSource.append(IOUtils.LINE_SEPARATOR);
            }
            catch (FileNotFoundException e)
            {
                throw e;
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    /**
     * BOM patterns.
     * 
     * @see <a href="http://www.unicode.org/faq/utf_bom.html#BOM">Unicode BOM spec</a>
     */
    public static enum BOM
    {
        NONE("UTF-8"),
        UTF_8("UTF-8", (byte)0xEF, (byte)0xBB, (byte)0xBF),
        UTF_16_LE("UTF-16LE", (byte)0xFF, (byte)0xFE),
        UTF_16_BE("UTF-16BE", (byte)0xFE, (byte)0xFF),
        UTF_32_LE("UTF-32LE", (byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00),
        UTF_32_BE("UTF-32BE", (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF);

        private BOM(final String charsetName, final byte... pattern)
        {
            if (charsetName == null || !Charset.isSupported(charsetName))
                this.charset = Charset.defaultCharset();
            else
                this.charset = Charset.forName(charsetName);
            this.pattern = pattern;
        }

        /**
         * BOM pattern in byte array.
         */
        public final byte pattern[];

        /**
         * The Java {@link Charset} for this BOM header.
         */
        public final Charset charset;
    }

    /**
     * Get the {@link BufferedInputStream} of a file, skipping the BOM.
     * 
     * @param filename The path to the file.
     * @return BufferedInputStream
     */
    public static BufferedInputStream getStreamAndSkipBOM(String filename) throws IOException
    {
        final File file = new File(filename);
        BufferedInputStream strm = new BufferedInputStream(new FileInputStream(file));
        final BOM bom = getBOM(strm);
        strm.skip(bom.pattern.length);

        return strm;
    }

    /**
     * Get the BOM tag of a stream.
     * 
     * @param strm BufferedInputStream to be checked.
     * @return {@link BOM} type.
     * @throws IOException Error.
     */
    public static BOM getBOM(BufferedInputStream strm) throws IOException
    {
        assert (strm.markSupported()) : "getBOM call on stream which does not support mark";

        // Peek the first 4 bytes.
        final byte[] peek = new byte[4];
        strm.mark(4);
        strm.read(peek);
        strm.reset();

        // Try matching 4-byte BOM tags.
        final byte[] quadruplet = Arrays.copyOf(peek, 4);
        if (Arrays.equals(BOM.UTF_32_BE.pattern, quadruplet))
            return BOM.UTF_32_BE;
        else if (Arrays.equals(BOM.UTF_32_LE.pattern, quadruplet))
            return BOM.UTF_32_LE;

        // Try matching 3-byte BOM tags.
        final byte[] triplet = Arrays.copyOf(peek, 3);
        if (Arrays.equals(BOM.UTF_8.pattern, triplet))
            return BOM.UTF_8;

        // Try matching 2-byte BOM tags.
        final byte[] twin = Arrays.copyOf(peek, 2);
        if (Arrays.equals(BOM.UTF_16_BE.pattern, twin))
            return BOM.UTF_16_BE;
        else if (Arrays.equals(BOM.UTF_16_LE.pattern, twin))
            return BOM.UTF_16_LE;

        // No BOM tag.
        return BOM.NONE;
    }

	@Override
	public void setLastModified(long fileDate) {
		// TODO Auto-generated method stub
		
	}
}
