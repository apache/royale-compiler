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

package org.apache.royale.compiler.internal.filespecs;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.utils.FilenameNormalization;

/**
 * A {@link IFileSpecification} implementation from a file path and text string. 
 */
public class StringFileSpecification implements IFileSpecification
{

    public StringFileSpecification(String name, String content)
    {
        this(name, content, 0);
    }
    
    public StringFileSpecification(String name, String content, long lastModified)
    {
        this.name = FilenameNormalization.normalize(name);
        this.content = content;
        this.lastModified = lastModified;
    }

    public StringFileSpecification(String content)
    {
        this("", content, 0);
    }

    private final String content;
    private final String name;
    private final long lastModified;

    @Override
    public String getPath()
    {
        return name;
    }

    @Override
    public Reader createReader() throws FileNotFoundException
    {
        return new StringReader(content);
    }

    @Override
    public long getLastModified()
    {
        return lastModified;
    }

    @Override
    public boolean isOpenDocument()
    {
        return false;
    }

	@Override
	public void setLastModified(long fileDate) {
		// TODO Auto-generated method stub
		
	}

}
