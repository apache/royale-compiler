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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFileSpecification implements IZipFileSpecification {
	
	private class IteratorImplementation implements Iterator<String> {
		
		private Enumeration<? extends ZipEntry> enumeration;

		public IteratorImplementation() {
			enumeration = zipFile.entries();
		}
		
		@Override
        public void remove() {
			//do nothing
		}

		@Override
        public String next() {
			ZipEntry element = enumeration.nextElement();
			return element.getName();
		}

		@Override
        public boolean hasNext() {
			return enumeration.hasMoreElements();
		}
	}

	private ZipFile zipFile;
	private long lastModified;
	private String path;

	public ZipFileSpecification(File file) throws ZipException, IOException {
		zipFile = new ZipFile(file);
		lastModified = file.lastModified();
		path = file.getPath();
	}
	
	@Override
    public Iterator<String> getEntries() {
		return new IteratorImplementation();
	}

	@Override
    public InputStream getEntryStream(String name) throws IOException {
		if(zipFile != null) {
			ZipEntry entry = zipFile.getEntry(name);
			if(entry != null)
				return zipFile.getInputStream(entry);
			//try with leading slash as a fallback
			entry = zipFile.getEntry("/" + name);
			if(entry != null)
				return zipFile.getInputStream(entry);
			
		}
		throw new IOException("File not found");
	}

	@Override
    public void close() throws IOException {
		if(zipFile == null)
			return;
		zipFile.close();
		zipFile = null;
	}

	@Override
    public boolean hasEntry(String entryName) {
		ZipEntry entry = zipFile.getEntry(entryName);
		if(entry == null) //check for leading slashes
			return zipFile.getEntry("/" + entryName) != null;
		return true;
	}

	@Override
    public Reader createReader() throws FileNotFoundException {
		return null;
	}

	@Override
    public long getLastModified() {
		return lastModified;
	}

	@Override
    public String getPath() {
		return path;
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
