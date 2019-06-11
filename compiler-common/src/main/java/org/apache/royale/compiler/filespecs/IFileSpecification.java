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

import java.io.FileNotFoundException;
import java.io.Reader;


/**
 * The interface for file specifications.
 */
public interface IFileSpecification
{
	/**
	 * Returns the path of the file.
	 * @return The path of the file.
	 */
	String getPath();

	/**
	 * Creates a new Reader for the contents of file.  Important: The caller
	 * should call Reader.close() when it is done reading.
	 * 
	 * @return a Reader for the contents of the file.
	 * @throws FileNotFoundException 
	 */
	Reader createReader() throws FileNotFoundException;
	
	/**
	 * Get the last modified timestamp of the file
	 * @return	the last modified timestamp
	 */
	long getLastModified();
	

	/**
	 * Set the last modified timestamp of the file
	 * @param fileDate	the last modified timestamp
	 */
	void setLastModified(long fileDate);
	
    /**
     * @return true if this file specification refers to an open document and
     * the {@link #createReader()} method returns a reader that reads the live
     * document buffer.
     */
	boolean isOpenDocument();
}
