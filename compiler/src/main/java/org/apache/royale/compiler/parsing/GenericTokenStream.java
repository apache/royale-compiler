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

import java.util.List;

import org.apache.royale.compiler.internal.parsing.TokenBase;

import antlr.Token;
import antlr.TokenStream;

/**
 * Implementation of TokenStream that allows us to feed lists of ASTokens to ASTreeAssembler.
 */
public class GenericTokenStream implements TokenStream
{
	/**
	 * Constructor
	 * @param tokens	list of ASTokens (or any subclass of antlr.Token)
	 */
	public GenericTokenStream(List<?extends ICMToken> tokens)
	{
		this.tokens = tokens.toArray(new TokenBase[0]);
		size = this.tokens.length;
		position = -1;
		eofToken = new Token(Token.EOF_TYPE);
	}
	
	/**
	 * Constructor
	 * @param tokens	list of ASTokens (or any subclass of antlr.Token)
	 */
	public GenericTokenStream(TokenBase[] tokens)
	{
		this.tokens = tokens;
		size = tokens.length;
		position = -1;
		eofToken = new Token(Token.EOF_TYPE);
	}
	
    private TokenBase[] tokens;
    private int size;
    private int position;
    private Token eofToken;

	/**
	 * Retrieves next token
	 * @return			returns next token from token iterator
	 */
	@Override
    public Token nextToken()
	{
		position++;
		
		if (position < size)
			return tokens[position];
		
		return eofToken;
	}
}
