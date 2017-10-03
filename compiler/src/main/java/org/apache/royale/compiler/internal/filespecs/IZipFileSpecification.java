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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.royale.compiler.filespecs.IFileSpecification;


/**
 * Interface for dealing with zip files.  Since SWCs are actually zip files,this provides an additional way to deal with
 * files that doesn't involve mxmlc
 */
public interface IZipFileSpecification extends IFileSpecification, Closeable{
	
	/**
	 * Creates a stream to a given entry within the SWC file
	 * @param name the name of the entry we want
	 * @return an {@link InputStream} or null
	 * @throws IOException 
	 */
	InputStream getEntryStream(String name) throws IOException;
	
	/**
	 * Returns an iterator to the names of all the entries in this zip file
	 * @return an {@link Iterator}
	 */
	Iterator<String> getEntries();
	
	/**
	 * Closes this archive
	 * @throws IOException
	 */
	@Override
    void close() throws IOException;
	
	/**
	 * Determines if the given entry exists within this archive
	 * @param entryName the name of the entry
	 * @return true if it exists
	 */
	boolean hasEntry(String entryName);
}
