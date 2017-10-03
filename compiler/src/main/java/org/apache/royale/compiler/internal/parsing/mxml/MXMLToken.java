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

package org.apache.royale.compiler.internal.parsing.mxml;

import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.parsing.ICMToken;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;

/**
 * MXML token (output unit of MXMLTokenizer, input unit of MXMLData constructor).
 */
public class MXMLToken extends ASToken implements IMXMLToken
{
    /**
	 * Constructor
	 * @param tokenType		token type from MXMLTokenTypes
	 * @param start start offset of the token
	 * @param end end of token
	 * @param line line of token
	 * @param tokenText		token string 
	 */
	public MXMLToken(int tokenType, int start, int end, int line, int column, CharSequence tokenText)
	{
		super(tokenType, start, end, line, column, tokenText);
	}
	
	/**
	 * Copy constructor
	 * @param other			the MXMLToken to copy
	 */
	public MXMLToken(MXMLToken other)
	{
		super(other);
	}
	
	public MXMLToken(ASToken other)
    {
        super(other);
    }

    @Override
    public ICMToken changeType(int type)
    {
        return new MXMLToken(type, getStart(), getEnd(), getLine(), getColumn(), getText());
    }
    
    @Override
    public MXMLToken clone() {
        return new MXMLToken(this);
    }
    
    /**
     * @return true if this token represents an ActionScript construct
     */
    public final boolean isASToken() {
        return !isE4X(type);
    }
    
 	@Override
    public MXMLTokenKind getMXMLTokenKind() {
		switch(getType()) {
			case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
				return MXMLTokenKind.TAG_OPEN_START;
			case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
				return MXMLTokenKind.TAG_CLOSE_START;
			case MXMLTokenTypes.TOKEN_TAG_END:
				return MXMLTokenKind.TAG_END;
			case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
				return MXMLTokenKind.EMPTY_TAG_END;
			case MXMLTokenTypes.TOKEN_NAME:
				return MXMLTokenKind.NAME;
			case MXMLTokenTypes.TOKEN_EQUALS:
				return MXMLTokenKind.EQUALS;
			case MXMLTokenTypes.TOKEN_STRING:
				return MXMLTokenKind.STRING;
			case MXMLTokenTypes.TOKEN_TEXT:
				return MXMLTokenKind.TEXT;
			case MXMLTokenTypes.TOKEN_COMMENT:
				return MXMLTokenKind.COMMENT;
			case MXMLTokenTypes.TOKEN_CDATA:
				return MXMLTokenKind.CDATA;
			case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
				return MXMLTokenKind.COMMENT;
			case MXMLTokenTypes.TOKEN_WHITESPACE:
			    return MXMLTokenKind.WHITESPACE;
			default:
			    if(isASToken())
			        return MXMLTokenKind.ACTIONSCRIPT;
				return MXMLTokenKind.UNKNOWN;
		}
	}
	
	/**
     * @return true if this token represents the start of an MXML tag (open or close)
     */
	public static final boolean isTagStart(int type) {
		switch(type) {
			case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
			case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
				return true;
		}
		return false;
	}
	

	/**
     * @return true if this token represents the start of an MXML tag (open or close)
     */
	public final boolean isTagStart() {
		return isTagStart(getType());
	}
	
	/**
	 * @param type the type of token to check
     * @return true if this token represents the end of an MXML tag (open or close)
     */
	public static final boolean isTagEnd(final int type) {
		switch(type) {
			case MXMLTokenTypes.TOKEN_TAG_END:
			case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
				return true;
		}
		return false;
	}
	
	/**
     * @return true if this token represents the end of an MXML tag (open or close)
     */
	public final boolean isTagEnd() {
		return isTagEnd(getType());
	}
	
	/**
	 * @param type the type of token to check
     * @return true if this token represents an MXML tag (open or close, start or end)
     */
	public static final boolean isStructureTag(final int type) {
		switch(type) {
			case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
			case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
			case MXMLTokenTypes.TOKEN_TAG_END:
			case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
				return true;
		}
		return false;
	}
	
	/**
     * @return true if this token represents an MXML tag (open or close, start or end)
     */
	public final boolean isStructureTag() {
		return isStructureTag(getType());
	}
	
	/**
	 * @param type the type of token to check
     * @return true if this token can legally follow a close tag 
     */
	public static final boolean canFollowCloseTag(final int type) {
		switch(type) {
			case MXMLTokenTypes.TOKEN_WHITESPACE:
			case MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION:
			case MXMLTokenTypes.TOKEN_COMMENT:
			case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
			case MXMLTokenTypes.TOKEN_TEXT:
			case MXMLTokenTypes.TOKEN_CDATA:
			case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
			case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
				return true;
		}
		return false;
	}
	
	/**
     * @return true if this token can legally follow a close tag 
     */
	public final boolean canFollowCloseTag() {
		return canFollowCloseTag(getType());
	}

	/**
	 * Get the display string for the token type
	 * @return				display string for the token type
	 */
	@Override
	public String getTypeString()
	{
		switch (getType())
		{
			case MXMLTokenTypes.TOKEN_WHITESPACE:
				return "TOKEN_WHITESPACE";
			case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
				return "TOKEN_OPEN_TAG_START";
			case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
				return "TOKEN_CLOSE_TAG_START";
			case MXMLTokenTypes.TOKEN_TAG_END:
				return "TOKEN_TAG_END";
			case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
				return "TOKEN_EMPTY_TAG_END";
			case MXMLTokenTypes.TOKEN_NAME:
				return "TOKEN_NAME";
			case MXMLTokenTypes.TOKEN_EQUALS:
				return "TOKEN_EQUALS";
			case MXMLTokenTypes.TOKEN_STRING:
				return "TOKEN_STRING";
			case MXMLTokenTypes.TOKEN_TEXT:
				return "TOKEN_TEXT";
			case MXMLTokenTypes.TOKEN_CDATA:
				return "TOKEN_CDATA";
			case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
				return "TOKEN_ASDOC_COMMENT";
			case MXMLTokenTypes.TOKEN_COMMENT:
				return "TOKEN_COMMENT";
            case MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION:
            	return "TOKEN_PROCESSING_INSTRUCTION";
			default:
				return super.getTypeString();
		}
	}
}
