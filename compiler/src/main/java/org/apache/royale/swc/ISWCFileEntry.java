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

import java.io.IOException;
import java.io.InputStream;

/**
 * A file in a SWC archive.
 */
public interface ISWCFileEntry
{
    /**
     * Get the path of the SWC in which this file is contained
     * 
     * @return relative path
     */
    String getContainingSWCPath();

    /**
     * Get the path of the file relative to the root of the package.
     * 
     * @return relative path
     */
    String getPath();

    /**
     * Get the time-stamp of the file. The time-stamp value is read from
     * {@code <file>} tag in catalog.xml file.
     * 
     * @return time stamp in milliseconds from epoch time.
     */
    long getLastModified();

    /**
     * Get the {@code InputStream} of the file entry.
     * 
     * @return file input stream
     */
    InputStream createInputStream() throws IOException;
}
