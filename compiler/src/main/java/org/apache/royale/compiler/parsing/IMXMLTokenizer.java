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
 * An interface for tokenizing MXML 3/4 source code.  For a given reader, we will return
 * an array of tokens that we've determined to be correct for the given range
 */
public interface IMXMLTokenizer
{
    /**
     * Sets a flag to indicate whether this tokenizer should try to repair its token stream
     * @param isRepairing
     */
    void setIsRepairing(boolean isRepairing);
    
    /**
	 * Returns an array of {@link IMXMLToken} objects for a given range of text represented by a {@link Reader}
	 * @param reader a Java {@link Reader}
	 * @return an array of {@link IMXMLToken} objects
	 */
	IMXMLToken[] getTokens(Reader reader);
	
	/**
	 * Returns an array of {@link IMXMLToken} objects for a given range of text represented by a {@link String}
	 * @return an array of {@link IMXMLToken} objects
	 */
	IMXMLToken[] getTokens(String range);
}
