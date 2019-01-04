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

package org.apache.royale.swc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.royale.swc.io.SWCReader;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFReader;

/**
 * Implementation of library model in a SWC.
 */
public class SWCLibrary implements ISWCLibrary
{
    /**
     * Use this constructor for writing a SWC library.
     * 
     * @param path relative path of the library in the SWC archive
     * @param swf model for the library SWF
     */
    public SWCLibrary(String path, ISWF swf)
    {
        this.scripts = new LinkedHashMap<String, ISWCScript>();
        this.keepAS3MetadataSet = new HashSet<String>();
        this.digests = new ArrayList<ISWCDigest>();
        this.path = path;
        this.librarySWF = swf;
    }

    /**
     * Use this constructor for reading a SWC library.
     * 
     * @param path relative path of the library in the SWC archive
     */
    public SWCLibrary(String path)
    {
        this(path, null);
    }

    private final Map<String, ISWCScript> scripts;
    private final Set<String> keepAS3MetadataSet;
    private final List<ISWCDigest> digests;
    private final String path;
    private final ISWF librarySWF;

    @Override
    public void addScript(ISWCScript script)
    {
        assert script != null;
        final String name = script.getName();
        assert !scripts.containsKey(name) : "Adding duplicated script: " + name;
        scripts.put(name, script);
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public List<ISWCScript> getScripts()
    {
        return new ArrayList<ISWCScript>(scripts.values());
    }

    @Override
    public Set<String> getKeepAS3MetadataSet()
    {
        return keepAS3MetadataSet;
    }
    
    @Override
    public void addNameToKeepAS3MetadataSet(String name)
    {
        keepAS3MetadataSet.add(name);
    }


    @Override
    public void readSWFInputStream(ISWFReader swfReader, ISWC swc)
    {
        ZipFile swcFile = null;
        try
        {
            swcFile = new ZipFile(swc.getSWCFile());
            InputStream swcFileInputStream = SWCReader.getInputStream(swcFile, path);
            if(swcFileInputStream != null) {
                final InputStream inputStream = new BufferedInputStream(swcFileInputStream);
                swfReader.readFrom(inputStream, SWCReader.getReportingPath(
                        swc.getSWCFile().getAbsolutePath(), path));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (swcFile != null)
            {
                try
                {
                    swcFile.close();
                }
                catch (IOException e)
                {
                    // Ignore this.
                }
            }
        }
    }

    @Override
    public ISWCScript getScript(String name)
    {
        return scripts.get(name);
    }

    @Override
    public List<ISWCDigest> getDigests()
    {
        return digests;
    }

    @Override
    public ISWF getSWF()
    {
        return librarySWF;
    }

    /**
     * Add a new digest to the library.
     * 
     * @param digest The digest to add. May not be null.
     */
    @Override
    public void addDigest(ISWCDigest digest)
    {
        digests.add(digest);
    }

    /**
     * Remove an existing digest from the library.
     * 
     * @param digest The digest to add. May not be null.
     */
    public void removeDigest(ISWCDigest digest)
    {
        digests.remove(digest);
    }
    
    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return getPath();
    }
}
