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

import org.apache.royale.compiler.problems.SyntaxProblem;

/**
 * Interface for the stream of tokens that will be passed to the parser. This
 * stream will support optional semicolon instructions from the parser
 */
public interface IRepairingTokenBuffer
{
    /**
     * Enables or disables the insertion of semicolons
     * 
     * @param enable true if we should insert semicolons into the token stream
     */
    void setEnableSemicolonInsertion(boolean enable);

    /**
     * Inserts a semicolon into the token stream
     * 
     * @param isNextToken true if the semicolon should be the next token
     * @return true if the semicolon was successfully inserted.
     */
    boolean insertSemicolon(boolean isNextToken);

    /**
     * Adds a syntax error that we encountered during parsing
     * 
     * @param error the {@link SyntaxProblem} to add
     */
    void addError(SyntaxProblem error);

    /**
     * Marks the position in the underlying token buffer. Token index starts
     * from 0. If we haven't consumed any token from the buffer, the "mark" will
     * be "0". If the first token in the buffer was just consumed, the "mark"
     * will be "1".
     * 
     * @return Index of the LT(1) token in the buffer.
     */
    int mark();

    /**
     * Rewinds the token buffer to the given position. This is optional
     * 
     * @param position the non-negative position
     */
    void rewind(int position);

    /**
     * Consumes the current token in the buffer
     */
    void consume();

    /**
     * Looks ahead an arbitrary distance and returns back the token type. If no
     * token exists, it will return EOF
     * 
     * @param i the offset to look ahead
     * @return the token type, or EOF
     */
    int LA(int i);

    /**
     * Looks ahead an arbitrary distance and returns back the token. If no token
     * exists, it will return EOF
     * 
     * @param i the offset to look ahead
     * @return the token, or EOF
     */
    ASToken LT(int i);

    /**
     * Get the first non-fix-up-semicolon from the look-ahead. For example, if
     * the buffer.isNextSemicolon=true and the look-ahead buffer is [A, B, C,
     * ...], LA(1) will return the fix-up semicolon, but this method will return
     * "A". This is useful for error reporting, because we don't want to treat
     * invalid fix-up semicolons as source code errors.
     * 
     * @return The first non-fix-up-semicolon from the look-ahead.
     */
    ASToken lookAheadSkipInsertedSemicolon();

    /**
     * Returns the previous token the buffer was looking at
     * 
     * @return the previous token, or EOF
     */
    ASToken previous();

    /**
     * Match an optional semicolon.
     * 
     * @return True if either a semicolon token or a virtual semicolon is
     * matched.
     */
    boolean matchOptionalSemicolon();

}
