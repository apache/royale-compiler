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

package org.apache.royale.swc.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.DigestOutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFWriter;
import org.apache.royale.swf.io.ISWFWriterFactory;
import org.apache.royale.swf.io.SizeReportWritingSWFWriter;

/**
 * Write a SWC model to an open directory. {@code compc} can be configured to
 * write a SWC to an open directory by:
 * 
 * <pre>
 * compc -directory=true -output=destination_directory
 * </pre>
 */
public class SWCDirectoryWriter extends SWCWriterBase
{

    /**
     * Write a SWC to an open directory at {@code path}.
     * The library.swf will be debuggable and compressed.
     * 
     * @param path Target path.
     */
    public SWCDirectoryWriter(String path)
    {
        this(path, true, true, false, SizeReportWritingSWFWriter.getSWFWriterFactory(null));
    }

    /**
     * Write a SWC to an open directory at {@code path}.
     * 
     * @param path Target path.
     * @param compressLibrarySWF - true if the library will be built compressed,
     * false otherwise.
     * @param enableDebug - true if the library should be build with debug
     * enabled, false otherwise.
     * @param swfWriterFactory - factory for creating swf writers.
     */
    public SWCDirectoryWriter(String path,
            boolean compressLibrarySWF,
            boolean enableDebug,
            boolean enableTelemetry,
            ISWFWriterFactory swfWriterFactory)
    {
        super(compressLibrarySWF, enableDebug, enableTelemetry, swfWriterFactory);
        this.directory = new File(path);
    }

    /**
     * Target path.
     */
    private final File directory;

    @Override
    void writeCatalog(ISWC swc) throws IOException
    {
        final Writer writer = new BufferedWriter(new FileWriter(new File(directory, CATALOG_XML)));
        writeCatalogXML(swc, writer);
        writer.flush();
        writer.close();
    }

    @Override
    void writeLibrary(ISWCLibrary library) throws IOException
    {
        final ISWF swf = library.getSWF();
        final String path = library.getPath();
        assert swf != null : "Expect SWF model";
        assert path != null : "Expect SWF path";

        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(directory, path)));

        final DigestOutputStream digestStream = getDigestOutputStream(library, outputStream);

        ISWFWriter swfWriter = swfWriterFactory.createSWFWriter(swf,
                getLibrarySWFCompression(), enableDebug, enableTelemetry);
        swfWriter.writeTo(digestStream != null ? digestStream : outputStream);
        swfWriter.close();
        outputStream.close();

        if (digestStream != null) {
            addDigestToLibrary(digestStream, library);
        }
    }

    @Override
    void writeFile(ISWCFileEntry fileEntry) throws IOException
    {
        final File file = new File(directory, fileEntry.getPath()).getAbsoluteFile();
        final File parentFolder = file.getParentFile();
        if (!parentFolder.isDirectory()) {
            parentFolder.mkdirs();
        }
        file.createNewFile();
        final OutputStream outputStream = new FileOutputStream(file);
        final InputStream fileInputStream = fileEntry.createInputStream();
        IOUtils.copy(fileInputStream, outputStream);
        fileInputStream.close();
        outputStream.close();
    }

    @Override
    void prepare(ISWC swc) throws IOException
    {
        if (!directory.exists())
        {
            if (!directory.mkdir()) {
                throw new FileNotFoundException(directory.getAbsolutePath());
            }
        }
    }

    @Override
    void finish(ISWC swc) throws IOException
    {
    }

}
