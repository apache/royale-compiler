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

package org.apache.royale.compiler.internal.parsing;

import java.io.IOException;
import java.io.Reader;

import org.apache.royale.utils.NonLockingStringReader;

/**
 * Reader that spoofs adding several characters to the end of the existing Reader.   Reading from the
 * FakingReader returns characters from the original Reader until they run out, at which point it returns
 * characters from its buffer of fake characters
 */
public class FakingReader extends Reader
{
	/**
	 * Constructor
	 * @param reader		the original reader
	 * @param fakeCharacters		the fake characters to "append" to the end
	 */
	public FakingReader(Reader reader, String fakeCharacters)
	{
		super();
		this.reader = reader;
		fakeReader = new NonLockingStringReader(fakeCharacters);
		readingFakeCharacters = false;
		
//      See the comments on mungeDifficultCharacters(), below
		
//		fMungeDifficultCharacters = mungeDifficultCharacters;
//		fOffset = 0;
//		fDifficultCharacters = new ArrayList();
//		fDifficultCharacterOffsets = new ArrayList();
	}
	
	/**
     * The original Reader that contains the real characters
     */
    protected Reader reader;
    
    /**
     * The StringReader that contains the fake characters
     */
    protected NonLockingStringReader fakeReader;
    
    /**
     * Flag indicating which reader is currently in use
     */
    protected boolean readingFakeCharacters;
    
	/**
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException
	{
		reader.close();
	}
	
//	/**
//	 * Flag indicating that we should munge difficult characters to prepare for JLex 
//	 */
//	protected boolean fMungeDifficultCharacters;
//	/**
//	 * Current offset into the stream
//	 */
//	protected int fOffset;
//	/**
//	 * List of difficult characters that we've had to munge before sending to JLex
//	 */
//	protected ArrayList fDifficultCharacters;
//	/**
//	 * List of offsets at which we've had to munge characters before sending to JLex
//	 */
//	protected ArrayList fDifficultCharacterOffsets;
//	/**
//	 * Locate any identifier-legal characters that we know we'll have trouble
//	 * identifying in the tokenizer, and replace them with simpler characters.
//	 * This is designed to be called after reading characters (from the real
//	 * stream) and read()
//	 * 
//	 * @param cbuf		buffer of characters just filled in read()
//	 * @param off		the offset at which read() just put the characters
//	 * @param len		the number of character successfully read
//	 */
//	protected void mungeDifficultCharacters(char[] cbuf, int off, int len)
//	{
//		// The following are all legal identifier characters in ECMAScript
//		// 		Uppercase letter (Lu)
//		// 		Lowercase letter (Ll)
//		// 		Titlecase letter (Lt)
//		// 		Modifier letter (Lm)
//		// 		Other letter (Lo)
//		//		Letter number (Nl) *
//		//		$ and _
//		//		Unicode escape sequence (\uFFFF)
//		// 		Non-spacing mark (Mn) *
//		//		Combining spacing mark (Mc) *
//		//		Decimal number (Nd)
//		//		Connector punctuation (Pc) *
//		// 
//		// In JLex, we can use the builtin character classes
//		// 		[:letter:] = Character.isLetter() = Lu + Ll + Lt + Lm + Lo
//		// 		[:digit:] = Character.isDigit() = Nd
//		// and we can use simple regular expressions to handle $ and _
//		// and the Unicode escape sequence (\uFFFF).  The other classes
//		// (Nl, Mn, Mc, Nd, Pc) are not easy to identify in JLex, since there's
//		// no generic character class support.  We'll identify the characters
//		// here and replace them with something more easily managed, then we'll
//		// unmunge the tokens in ASTokenizer before we do anything else with them.
//		
//		for (int i = off; i < off + len; i++)
//		{
//			if (Character.getType(cbuf[i]) == Character.LETTER_NUMBER)
//			{
//				// This is a valid starting character for an identifier, so
//				// "a" is a suitable equivalent
//				fDifficultCharacterOffsets.add(new Integer(fOffset + i - off));
//				fDifficultCharacters.add(new Character(cbuf[i]));
//				cbuf[i] = 'a';
//			}
//			else if (cbuf[i] != '_' &&
//					(Character.getType(cbuf[i]) == Character.NON_SPACING_MARK ||
//					Character.getType(cbuf[i]) == Character.COMBINING_SPACING_MARK ||
//					Character.getType(cbuf[i]) == Character.CONNECTOR_PUNCTUATION))
//			{
//				// This is not a valid starting character for an identifier, but
//				// is a valid following character for an identifier, so "0" is a
//				// suitable equivalent
//				fDifficultCharacterOffsets.add(new Integer(fOffset + i - off));
//				fDifficultCharacters.add(new Character(cbuf[i]));
//				cbuf[i] = '0';
//			}
//		}
//	}
//	/**
//	 * Undo any character transformation that we accomplished in mungeDifficultCharacters
//	 * @param tokenString	the token string that came back from the tokenizer
//	 * 						(possibly containing transformed characters)
//	 * @param startOffset	the start offset of that string
//	 * @return				the same string, with the original characters put back
//	 */
//	public String unmungeDifficultCharacters(String tokenString, int startOffset)
//	{
//		int endOffset = startOffset + tokenString.length();
//		char [] tokenChars = null;
//		for (int i = 0; i < fDifficultCharacterOffsets.size(); i++)
//		{
//			int difficultCharacterOffset = ((Integer)fDifficultCharacterOffsets.get(i)).intValue();
//			if (difficultCharacterOffset >= startOffset &&
//					difficultCharacterOffset <= endOffset)
//			{
//				if (tokenChars == null)
//					tokenChars = tokenString.toCharArray();
//				tokenChars[difficultCharacterOffset - startOffset] = 
//					((Character)fDifficultCharacters.get(i)).charValue();
//			}
//			else if (difficultCharacterOffset > endOffset)
//			{
//				break;
//			}
//		}
//		if (tokenChars != null)
//			tokenString = new String(tokenChars);
//		return tokenString;
//	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if (readingFakeCharacters)
		{
			// We're already past the end of the real stream and in the fake characters
			return fakeReader.read(cbuf, off, len);
		}
		
		int charactersRead = reader.read(cbuf, off, len);
		if (charactersRead == -1)
		{
			// We're past the end of the real stream and in the fake characters
			readingFakeCharacters = true;
			return fakeReader.read(cbuf, off, len);
		}
		
		// See the comments on mungeDifficultCharacters(), above
		//				if (fMungeDifficultCharacters)
		//				{
		//					mungeDifficultCharacters(cbuf, off, charactersRead);
		//					fOffset += charactersRead;
		//				}
		// We're still reading characters from the real stream.
		return charactersRead;
	}
}
