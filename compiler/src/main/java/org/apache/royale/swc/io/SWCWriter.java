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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFWriter;
import org.apache.royale.swf.io.ISWFWriterFactory;
import org.apache.royale.swf.io.SizeReportWritingSWFWriter;

/**
 * Implementation for serializing a SWC model to a *.swc library file.
 */
public class SWCWriter extends SWCWriterBase
{
    
    /**
     * Create a SWC where the library.swf is build with
     * debug and is compressed.
     * 
     * @param filename path to write the file to.
     */
    public SWCWriter(final String filename) throws FileNotFoundException
    {
        this(filename, true, true, false, SizeReportWritingSWFWriter.getSWFWriterFactory(null));
    }
    
    /**
     * @param filename path to write the file to.
     * @param compressLibrarySWF - true if the library will be built compressed,
     * false otherwise.
     * @param enableDebug - true if the library should be build with debug
     * enabled, false otherwise.
     * @param swfWriterFactory - factory for creating swf writers.
     */
    public SWCWriter(final String filename, 
            boolean compressLibrarySWF,
            boolean enableDebug,
            boolean enableTelemetry,
            ISWFWriterFactory swfWriterFactory) throws FileNotFoundException
    {
        super(compressLibrarySWF, enableDebug, enableTelemetry, swfWriterFactory);
        
        // Ensure that the directory for the SWC exists.
        File outputFile = new File(filename);
        File outputDirectory = new File(outputFile.getAbsoluteFile().getParent());
        outputDirectory.mkdirs();
        
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
    }

    /**
     * Target SWC output stream.
     */
    private final ZipOutputStream zipOutputStream;

    @Override
    void writeCatalog(final ISWC swc) throws IOException
    {
        zipOutputStream.putNextEntry(new ZipEntry(CATALOG_XML));
        final Writer catalogXMLWriter = new OutputStreamWriter(zipOutputStream);
        writeCatalogXML(swc, catalogXMLWriter);
        catalogXMLWriter.flush();
        zipOutputStream.closeEntry();
    }

    @Override
    void writeLibrary(final ISWCLibrary library) throws IOException
    {
        final ISWF swf = library.getSWF();
        final String path = library.getPath();
        assert swf != null : "Expect SWF model";
        assert path != null : "Expect SWF path";

        zipOutputStream.putNextEntry(new ZipEntry(path));

        final DigestOutputStream digestStream = getDigestOutputStream(library, zipOutputStream);

        ISWFWriter swfWriter = swfWriterFactory.createSWFWriter(swf,
                getLibrarySWFCompression(), enableDebug, enableTelemetry);
        swfWriter.writeTo(digestStream != null ? digestStream : zipOutputStream);
        swfWriter.close();
        zipOutputStream.closeEntry();
        
        if (digestStream != null) {
            addDigestToLibrary(digestStream, library);
        }
    }

    @Override
    void writeFile(final ISWCFileEntry fileEntry) throws IOException
    {
        zipOutputStream.putNextEntry(new ZipEntry(fileEntry.getPath()));
        final InputStream fileInputStream = fileEntry.createInputStream();
        IOUtils.copy(fileInputStream, zipOutputStream);
        fileInputStream.close();
        zipOutputStream.closeEntry();
    }

    @Override
    void prepare(ISWC swc)
    {
    }

    @Override
    void finish(ISWC swc) throws IOException
    {
        zipOutputStream.flush();
        zipOutputStream.close();
    }

}
