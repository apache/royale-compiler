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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.royale.compiler.asdoc.IPackageDITAParser;
import org.apache.royale.compiler.problems.LibraryNotFoundProblem;
import org.apache.royale.compiler.problems.FileInLibraryIOProblem;
import org.apache.royale.compiler.problems.FileInLibraryNotFoundProblem;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.SWC;
import org.apache.royale.swc.catalog.StAXCatalogReader;
import org.apache.royale.swc.dita.IDITAList;

/**
 * Read a SWC file using Java ZIP utilities. The {@code SWCReader} owns the
 * result {@linkplain ISWC} object.
 */
public class SWCReader implements ISWCReader
{
    public static final String CATALOG_XML = "catalog.xml";
    private static final String DITA_MANIFEST = "docs/packages.dita";
    private static final String ANE_EXTENSION_XML = "META-INF/AIR/extension.xml";

    /**
     * Create a SWCReader from a file path.
     * 
     * @param filename file path
     */
    public SWCReader(String filename)
    {
        this(new File(filename), IPackageDITAParser.NIL_PARSER);
    }
    
    /**
     * Create a SWCReader from a file object
     * 
     * @param swcFile input SWC file. The file must exist or a 
     * FileNotFoundException is thrown.
     */
    public SWCReader(File swcFile)
    {
        this(swcFile, IPackageDITAParser.NIL_PARSER);
    }

    /**
     * Create a SWCReader from a file object
     * 
     * @param swcFile input SWC file. The file must exist or a
     * FileNotFoundException is thrown.
     * @param packageDitaParser {@link IPackageDITAParser} that will be used to
     * parse DITA information found in the SWC.
     */
    public SWCReader(File swcFile, IPackageDITAParser packageDitaParser)
    {
        this.swcFile = swcFile;
        this.swc = new SWC(swcFile);

        if (!swcFile.exists() || !swcFile.isFile())
        {
            swc.addProblem(new LibraryNotFoundProblem(swcFile.getAbsolutePath()));
            return;
        }

        ZipFile zipFile = null;
        catalogReader = null;
        try
        {
            try
            {
                zipFile = new ZipFile(swcFile, ZipFile.OPEN_READ);
                final InputStream catalogInputStream = getInputStream(zipFile, CATALOG_XML);
                if (catalogInputStream == null)
                {
                    swc.addProblem(new FileInLibraryNotFoundProblem(swcFile.getAbsolutePath(), CATALOG_XML));
                    return;
                }                
                catalogReader = new StAXCatalogReader(new BufferedInputStream(catalogInputStream), swc);
                catalogReader.parse();
                catalogReader.close();
                catalogReader = null;
            }
            catch (Exception e)
            {
                swc.addProblem(new FileInLibraryIOProblem(CATALOG_XML,
                        swcFile.getAbsolutePath(),
                        e.getLocalizedMessage()));
                return;
            }
            
            try
            {
                //might not exist, so wrap in a try catch
                final InputStream ditaInputStream = getInputStream(zipFile, DITA_MANIFEST);
                if(ditaInputStream != null)
                {
                    try
                    {
                        IDITAList list = packageDitaParser.parse(swcFile.getAbsolutePath(), ditaInputStream);
                        if (list != null)
                            swc.setDITAList(list);
                    }
                    finally
                    {
                        ditaInputStream.close();
                    }
                }
            }
            catch (IOException e)
            {
                //ignore this
            }
            
            // The swc is an considered an ANE if it contains a 
            // META-INF/AIR/extension.xml file.
            swc.setIsANE(zipFile.getEntry(ANE_EXTENSION_XML) != null);
            
        }
        finally
        {
            try
            {
                if (catalogReader != null)
                    catalogReader.close();
                
                if (zipFile != null)
                    zipFile.close();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
        
    }

    private final File swcFile;
    private StAXCatalogReader catalogReader;
    private final SWC swc;

    @Override
    public File getFile()
    {
        return swcFile;
    }

    @Override
    public ISWC getSWC()
    {
        return swc;
    }

    /**
     * Get the {@code InputStream} of a file in the SWC archive. If the file
     * does not exist in the zip file, return null.
     * 
     * @param zipFile Zip file.
     * @param filename Name of the file in the zip archive.
     * @return InputStream of the file in the zip archive, or null.
     * @throws IOException Error reading file from zip archive.
     */
    public static InputStream getInputStream(ZipFile zipFile, String filename) throws IOException
    {
        ZipEntry zipEntry = null;
        for (final Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();)
        {
            final ZipEntry entry = entryEnum.nextElement();
            if (entry.getName().equals(filename))
            {
                zipEntry = entry;
                break;
            }
        }

        if (zipEntry == null)
            return null;
        else
            return zipFile.getInputStream(zipEntry);
    }

    /**
     * Create a single path that is a combined path of the SWC and the library
     * inside. This path should only be used for error reporting. It should not
     * be used to open the file.
     * 
     * @param swcPath the path of the SWC.
     * @param libraryPath the path of the library withing the SWC. 
     * @return combined path for SWC and library file.
     */
    public static String getReportingPath(String swcPath, String libraryPath)
    {
        assert swcPath != null && libraryPath != null;
        
        StringBuilder sb = new StringBuilder();
        sb.append(swcPath).append("(").append(libraryPath).append(")");
        return sb.toString();
    }
}
