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
 * Creates a scanner that determines if various structures are balanced across a given input
 */
public interface IASBalancingScanner
{
	/**
	 * Are the braces balanced in the ActionScript code contained in the reader?
	 * @param input 			reader to access a segment of ActionScript code
	 * @return					true if {} braces are balanced in that segment
	 */
	boolean areBracesBalanced(Reader input);

    /**
     * Are the braces balanced in the ActionScript code contained in the range?
     * @return                  true if {} braces are balanced in that segment
     */
	boolean areBracesBalanced(String range);

	/**
     * Return true if there are the same number of open/close braces, or if
     * there are more close braces than open.
     */
    boolean areBracesBalancedOrOverbalanced(Reader input);

    /**
     * Return true if there are the same number of open/close braces, or if
     * there are more close braces than open.
     */
    boolean areBracesBalancedOrOverbalanced(String range);
}
