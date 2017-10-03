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

package org.apache.royale.utils;

import java.io.File;

/**
 * A reliable way to identify and compare directories.
 * Makes it impossible to do a case-sensitive comparison.
 */
public final class DirectoryID extends FileID
{
    /**
     * Constructor.
     * 
     * @param file must be an directory that exists on the file system
     */
    public DirectoryID(File file)
    {
        super(file);
        assert file.isDirectory();
    }

    /**
     * @return true if file is a child of "this"
     */
    public boolean isParentOf(File file)
    {
       return (fileToLowerCaseString(file)).startsWith(this.getLowerCasePath());
    }
}
