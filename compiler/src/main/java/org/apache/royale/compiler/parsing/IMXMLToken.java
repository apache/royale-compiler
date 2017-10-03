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

/**
 * A token returned back by an {@link IMXMLTokenizer} created when we scan text
 */
public interface IMXMLToken extends ICMToken
{
	/**
	 * Types of tokens within the MXML language
	 */
	enum MXMLTokenKind
	{
		/**
		 * an open tag start token: &lt; 
		 */
		TAG_OPEN_START, 
		
		/**
		 * a close tag start token: &lt; 
		 */
		TAG_CLOSE_START, 
		
		/**
		 * a tag close token: &gt; 
		 */
		TAG_END,
		
		/**
		 * an empty tag close token: /&gt; 
		 */
		EMPTY_TAG_END, 
		
		/**
		 * Name as matched by the XML identifier rule
		 */
		NAME, 
		
		/**
		 * Equals char
		 */
		EQUALS, 
		
		/**
		 * String literal
		 */
		STRING, 
		
		/**
		 * Textual content
		 */
		TEXT, 
		
		/**
		 * An XML comment
		 */
		COMMENT,
		
		/**
		 * A CDATA block
		 */
		CDATA, 
		
		/**
		 * An ActionScript token
		 */
		ACTIONSCRIPT,
		
		/**
         * A whitespace token
         */
		WHITESPACE,
		
		/**
		 * An untyped or unknown token kind.  Should be ignored
		 */
		UNKNOWN
	}
	
	/**
	 * Returns the kind of token that we represent.
	 * @return a token kind, seen in {@link MXMLTokenKind}
	 */
	MXMLTokenKind getMXMLTokenKind();
}
