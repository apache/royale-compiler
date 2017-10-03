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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCDigest;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.SWCDigest;
import org.apache.royale.swc.catalog.StAXCatalogWriter;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.Header.Compression;
import org.apache.royale.swf.io.ISWFWriterFactory;

/**
 * Base class for serializing a SWC model.
 */
abstract class SWCWriterBase implements ISWCWriter
{

    /**
     * File name of "catalog.xml".
     */
    protected static final String CATALOG_XML = "catalog.xml";

    /**
     * Constructor
     * 
     * @param compressLibrarySWF - true if the library will be built compressed,
     * false otherwise.
     * @param enableDebug - true if the library should be build with debug
     * enabled, false otherwise.
     * @param enableTelemetry - true if the library should be build with telemetry
     * enabled, false otherwise.
     * @param swfWriterFactory - factory for creating swf writers.
     */
    protected SWCWriterBase(boolean compressLibrarySWF,
            boolean enableDebug, boolean enableTelemetry,
            ISWFWriterFactory swfWriterFactory)
    {
        assert swfWriterFactory != null;
        this.compressLibrarySWF = compressLibrarySWF;
        this.enableDebug = enableDebug;
        this.enableTelemetry = enableTelemetry;
        this.swfWriterFactory = swfWriterFactory;
    }
    
    private final boolean compressLibrarySWF;   // true if the library is built compressed
    protected final boolean enableDebug;
    protected final boolean enableTelemetry;
    protected final ISWFWriterFactory swfWriterFactory;
    
    /**
     * @return true if the library is built compressed, false otherwise.
     */
    public boolean compressLibrarySWF()
    {
        return compressLibrarySWF;
    }
    
    public Header.Compression getLibrarySWFCompression()
    {
        // NOTE: this does not support LZMA in SWCS (obviously).
        return compressLibrarySWF() ? Compression.ZLIB : Compression.NONE;
    }

    /**
     * This method defines the serialization order.
     */
    @Override
    public void write(final ISWC swc) throws IOException
    {
        if (swc == null)
            throw new NullPointerException("SWC model can't be null.");

        prepare(swc);

        for (final ISWCLibrary library : swc.getLibraries())
        {
            writeLibrary(library);
        }

        // Write the catalog after the library has been written
        // in case the library added a digest.
        writeCatalog(swc);

        for (final ISWCFileEntry fileEntry : swc.getFiles().values())
        {
            writeFile(fileEntry);
        }

        finish(swc);
    }

    /**
     * Before writing SWC contents.
     * 
     * @param swc SWC model
     */
    abstract void prepare(ISWC swc) throws IOException;

    /**
     * Write "catalog.xml" to the target SWC.
     * 
     * @param swc SWC model.
     */
    abstract void writeCatalog(ISWC swc) throws IOException;

    /**
     * Add a library to the target SWC.
     * 
     * @param swc SWC library.
     */
    abstract void writeLibrary(ISWCLibrary swc) throws IOException;

    /**
     * Add a file entry to the target SWC.
     * 
     * @param swc SWC library.
     */
    abstract void writeFile(ISWCFileEntry swc) throws IOException;

    /**
     * Clean up resources after writing out the SWC.
     * 
     * @param swc SWC model.
     * @throws IOException
     */
    abstract void finish(ISWC swc) throws IOException;

    /**
     * Serialize the SWC model's catalog.xml to a writer.
     * 
     * @param swc model
     */
    protected final void writeCatalogXML(ISWC swc, Writer writer)
    {
        try
        {
            final StAXCatalogWriter xmlWriter = new StAXCatalogWriter(swc, writer);
            xmlWriter.write();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a digest stream output if the library needs to create a digest
     * for the library.swf.
     * 
     * @param library 
     * 
     * @return a digest stream if a digest is needed, null otherwise.
     */
    protected DigestOutputStream getDigestOutputStream(ISWCLibrary library, OutputStream outputStream)
    {
        if (!hasUnsignedDigest(library))
        {
            try
            {
                MessageDigest messageDigest = MessageDigest.getInstance(SWCDigest.SHA_256);
                return new DigestOutputStream(outputStream, messageDigest);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Eat the exception, this should never happen in a
                // production environment.
            }  
        }
            
        return null;
    }
    
    /**
     * Add the digest from the digestStream to the ISWCLibrary.
     * 
     * @param digestStream may be null. If null no digest is created.
     * @param library The library to update. May not be null.
     * @throws NullPointerException if library is null.
     */
    protected void addDigestToLibrary(DigestOutputStream digestStream, 
            ISWCLibrary library)
    {
        if (library == null) {
            throw new NullPointerException("library may not be null");
        }

        if (digestStream != null)
        {
            SWCDigest swcDigest = new SWCDigest();
            swcDigest.setType(SWCDigest.SHA_256);
            swcDigest.setValue(digestStream.getMessageDigest().digest());
            library.addDigest(swcDigest);
        }
    }
    
    /**
     * Determine if the library has an unsigned digest.
     * 
     * @param library
     * @return true if the library has an unsigned digest, false
     * otherwise
     */
    private boolean hasUnsignedDigest(ISWCLibrary library)
    {
        List<ISWCDigest> digests = library.getDigests();
        
        for (ISWCDigest digest : digests)
        {
            if (!digest.isSigned()) {
                return true;
            }
        }
        return false;
    }

    
}
