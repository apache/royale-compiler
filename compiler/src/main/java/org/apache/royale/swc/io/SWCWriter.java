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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.filespecs.FileSpecification;
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
    public SWCWriter(final String filename, String swcDate, String swcDateFormat) throws FileNotFoundException
    {
        this(filename, true, true, false, swcDate, swcDateFormat, SizeReportWritingSWFWriter.getSWFWriterFactory(null));
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
            String metadataDate, String metadataFormat,
            ISWFWriterFactory swfWriterFactory) throws FileNotFoundException
    {
        super(compressLibrarySWF, enableDebug, enableTelemetry, swfWriterFactory);
        
        // Ensure that the directory for the SWC exists.
        File outputFile = new File(filename);
        File outputDirectory = new File(outputFile.getAbsoluteFile().getParent());
        outputDirectory.mkdirs();
        
    	if (metadataDate != null)
    	{
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
    			Date d = sdf.parse(metadataDate);
    			Calendar cal = new GregorianCalendar();
    			cal.setTime(d);
    			ZonedDateTime zdt = ZonedDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
    									cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0, ZoneId.systemDefault());
    			fileDate = zdt.toInstant().toEpochMilli();
    		} catch (ParseException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IllegalArgumentException e1) {
    			e1.printStackTrace();
    		}
    	}
    	
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
    }

    /**
     * Target SWC output stream.
     */
    private final ZipOutputStream zipOutputStream;
    
    private long fileDate = System.currentTimeMillis();

    @Override
    void writeCatalog(final ISWC swc) throws IOException
    {
    	ZipEntry ze = new ZipEntry(CATALOG_XML);
    	ze.setTime(fileDate);
    	ze.setMethod(ZipEntry.STORED);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Writer catalogXMLWriter = new OutputStreamWriter(baos);
        writeCatalogXML(swc, catalogXMLWriter);
        catalogXMLWriter.flush();
        ze.setSize(baos.size());
        ze.setCompressedSize(baos.size());
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(baos.toByteArray());
        ze.setCrc(crc.getValue());
        zipOutputStream.putNextEntry(ze);
        
        baos.writeTo(zipOutputStream);
        
        zipOutputStream.closeEntry();
    }

    @Override
    void writeLibrary(final ISWCLibrary library) throws IOException
    {
        final ISWF swf = library.getSWF();
        final String path = library.getPath();
        assert swf != null : "Expect SWF model";
        assert path != null : "Expect SWF path";

        ZipEntry ze = new ZipEntry(path);
    	ze.setTime(fileDate);
    	ze.setMethod(ZipEntry.STORED);
    	
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ISWFWriter swfWriter = swfWriterFactory.createSWFWriter(swf,
                getLibrarySWFCompression(), enableDebug, enableTelemetry);
        swfWriter.writeTo(baos);
        swfWriter.close();
        ze.setSize(baos.size());
        ze.setCompressedSize(baos.size());
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(baos.toByteArray());
        ze.setCrc(crc.getValue());
        zipOutputStream.putNextEntry(ze);        

        final DigestOutputStream digestStream = getDigestOutputStream(library, zipOutputStream);
        baos.writeTo(digestStream != null ? digestStream : zipOutputStream);
        
        zipOutputStream.closeEntry();
        
        if (digestStream != null) {
            addDigestToLibrary(digestStream, library);
        }
    }

    @Override
    void writeFile(final ISWCFileEntry fileEntry) throws IOException
    {
    	ZipEntry ze = new ZipEntry(fileEntry.getPath());
    	ze.setTime(fileDate);        
    	ze.setMethod(ZipEntry.STORED);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InputStream fileInputStream = fileEntry.createInputStream();
        String name = fileEntry.getPath();
        if (name.endsWith(".css")) // add other text files here
        {
        	FileSpecification.NoCRLFInputStream filteredInputStream = 
        			new FileSpecification.NoCRLFInputStream(fileInputStream);
        	IOUtils.copy(filteredInputStream, baos);
        	filteredInputStream.close();
        }
        else
        {
        	IOUtils.copy(fileInputStream, baos);
        	fileInputStream.close();
        }
        
        ze.setSize(baos.size());
        ze.setCompressedSize(baos.size());
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(baos.toByteArray());
        ze.setCrc(crc.getValue());
        zipOutputStream.putNextEntry(ze);
        
        baos.writeTo(zipOutputStream);
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
