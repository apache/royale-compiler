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

package org.apache.royale.compiler.parsing;

import java.io.Reader;

/**
 * An interface for tokenizing ActionScript 3 source code.  For a given reader, we will return
 * an array of tokens that we've determined to be correct for the given range
 */
public interface IASTokenizer
{
    /**
     * Sets the path to the file this tokenizer is scanning
     * @param path a file path
     */
    void setPath(String path);
    
    /**
     * Sets whether the tokenizer should collect single-line and multi-line comments as it
     * scans for tokens
     * @param collect true if we should return comments
     */
    void setCollectComments(boolean collect);
    
    /**
     * Sets whether we follow include statements, including their tokens. Default is <code>true</code>
     * @param followIncludes true if we should follow includes
     */
    void setFollowIncludes(final boolean followIncludes);
    
    /**
	 * Returns an array of {@link IASToken} objects for a given range of text represented by a {@link Reader}
	 * @param reader a Java {@link Reader}
	 * @return an array of {@link IASToken} objects
	 */
	IASToken[] getTokens(Reader reader);
	
	/**
	 * Returns an array of {@link IASToken} objects for a given range of text represented by a {@link String}
	 * @return an array of {@link IASToken} objects
	 */
	IASToken[] getTokens(String range);
}
