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

import java.io.ByteArrayOutputStream;

/**
 * ByteArratOuputStream that supports directly accessing the byte array inside the stream
 */
public class DAByteArrayOutputStream extends ByteArrayOutputStream {
	
	public DAByteArrayOutputStream() {
		super();
	}

	public DAByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * Get the byte array contained within this stream.  Note: flush may need to be called
	 * @return the byte array
	 */
	public synchronized byte[] getDirectByteArray() {
	    // only return the buffer directly if the length of the buffer
	    // is the same as the count, as the buffer can be bigger than the
	    // count, and in this case, we return an array which is larger than
	    // expected, so we need to truncate it with a copy.
	    if (buf.length == count)
	        return buf;
	    else
	        return super.toByteArray();
	}
}
