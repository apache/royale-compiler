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

package org.apache.royale.compiler.filespecs;

import java.io.File;

import org.apache.royale.utils.FilenameNormalization;

/**
 * Base class for all IFileSpecifications, which has a file path and knows
 * how to perform basic operations with it.
 */
public abstract class BaseFileSpecification implements IFileSpecification
{
	/**
	 * The file path
	 */
	protected String path;

	/**
	 * Constructor
	 * 
	 * @param path the file path
	 */
	public BaseFileSpecification(String path) {
		this.path = FilenameNormalization.normalize(path);
	}

	/**
	 * Returns the path of the file.
	 * 
	 * @return the file path
	 */
	@Override
    public String getPath() {
		return path;
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Returns the file handle.
	 * 
	 * @return the file handle
	 */
	public File getFileHandle() {
		return new File(getPath());
	}

	/**
	 * Get the last modified timestamp of the file
	 * 
	 * @return the last modified timestamp
	 */
	@Override
    public long getLastModified() {
		File file = new File(getPath());
		if (file.exists())
			return file.lastModified();
		// If the file doesn't exist any more, we should DEFINITELY not
		// still be using any old cached parse trees for it.  Returning
		// MAX_VALUE-1 here makes sure it looks newer than anything in our
		// cache (since its timestamp is more recent).
		return Long.MAX_VALUE - 1;
	}

	@Override
    public boolean isOpenDocument()
	{
		return false;
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
