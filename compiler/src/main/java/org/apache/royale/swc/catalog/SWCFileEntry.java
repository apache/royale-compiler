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

package org.apache.royale.swc.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.io.SWCReader;

/**
 * A file in a SWC archive.
 */
class SWCFileEntry implements ISWCFileEntry
{
    public SWCFileEntry(String containingSWCPath, String path, long mod)
    {
        this.containingSWCPath = containingSWCPath;
        this.path = path;
        this.mod = mod;
    }

    private final String containingSWCPath;
    private final String path;
    private final long mod;

    @Override
    public String getContainingSWCPath()
    {
        return containingSWCPath;
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public long getLastModified()
    {
        return mod;
    }

    @Override
    public InputStream createInputStream() throws IOException
    {
        ZipFile swcFile = new ZipFile(containingSWCPath);
        InputStream inputStream = SWCReader.getInputStream(swcFile, path);
        return inputStream;
    }
}
