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

package org.apache.royale.compiler.internal.parsing.as;

import java.util.List;

import org.apache.royale.compiler.internal.parsing.TokenBase;

public abstract class BaseTokenizerWithFakeCharacters
{
    /**
     * Build a token of the appropriate type (used when we need to truncate a
     * token to strip out fake characters in removeFakeTokens)
     * 
     * @param tokenType type of token
     * @param start start of token
     * @param end end of token
     * @param line line of token
     * @param tokenText text of token
     * @return token of the appropriate type (ASToken, CSSToken, MXMLToken,
     * etc.)
     */
    protected abstract TokenBase buildToken(int tokenType, int start, int end, int line, int column, String tokenText);

    /**
     * Remove any dummy tokens that we created using the fake characters we
     * appended to the end of the stream. If there is a token that contains both
     * real characters and fake characters, we'll truncate that token to only
     * include real characters
     * 
     * @param lastRealIndex index of last real characters
     * @param tokenList complete token list
     */
    protected void removeFakeTokens(int lastRealIndex, List<TokenBase> tokenList)
    {
        // Remove any dummy tokens we created using the fake characters we appended to
        // the end of the stream
        for (int tokenIndex = tokenList.size() - 1; tokenIndex >= 0; tokenIndex--)
        {
            TokenBase token = (TokenBase)tokenList.get(tokenIndex);
            if (token.getEnd() <= lastRealIndex)
                break;
            else if (token.getStart() < lastRealIndex &&
                     token.getEnd() > lastRealIndex)
            {
                // The end of this token is in the fake characters.  Truncate it.
                int actualLength = lastRealIndex - token.getStart();
                if (actualLength > token.toString().length())
                {
                    actualLength = token.toString().length();
                }
                tokenList.set(tokenIndex, buildToken(token.getType(), token.getStart(), lastRealIndex, -1, -1, token.toString().substring(0, actualLength)));
            }
            else
            {
                // This entire token is in the fake characters.  Discard it.
                tokenList.remove(tokenIndex);
            }
        }
    }
}
