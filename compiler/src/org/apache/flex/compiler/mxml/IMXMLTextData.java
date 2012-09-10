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

package org.apache.flex.compiler.mxml;

/**
 * Represents a block of some form of text found in MXML content
 */
public interface IMXMLTextData
{
	/**
	 * Represents different kinds of text elements
	 */
	enum TextType
	{
		/**
		 * A CDATA block
		 */
		CDATA,
		
		/**
		 * A comment block
		 */
		COMMENT,
		
		/**
		 * Text
		 */
		TEXT,
		
		/**
		 * An ASDoc comment block
		 */
		ASDOC,
		
		/**
		 * Whitespace found in the MXML
		 */
		WHITESPACE,
		
		/**
		 * Represents the contents of a databinding expression
		 */
		DATABINDING,
		
		/**
		 * An MXML entity
		 */
		ENTITY,
		
		/**
		 * An unknown text type
		 */
		OTHER
	}
	
	String getContent();
	
	/**
	 * Returns the type of text this data represents
	 * @return a {@link TextType} vaue
	 */
	TextType getTextType();
}
