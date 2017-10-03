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

package org.apache.royale.swf.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.royale.swf.ISWF;

/**
 * Read a SWF file and construct in-memory model.
 */
public interface ISWFReader extends Closeable
{
    /**
     * Read a SWF file from the input stream.
     * 
     * @param input source.
     * @param path path of input source, used for error reporting. May not be
     * null.
     * @return SWF model
     * @throws IOException error
     */
    ISWF readFrom(InputStream input, String path) throws IOException;
}
