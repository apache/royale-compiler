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

import static org.apache.royale.compiler.common.ISourceLocation.UNKNOWN;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.problems.SyntaxProblem;

import antlr.Token;

/**
 * Base class for repairing token buffers. This handles insertion of semicolons,
 * and also tracks errors.
 */
public abstract class BaseRepairingTokenBuffer implements IRepairingTokenBuffer
{
    protected int position;
    protected ASToken eofToken;
    protected final List<SyntaxProblem> errors;
    protected boolean nextIsSemicolon;
    protected boolean insertSemis;
    protected static final ASToken SEMICOLON = new ASToken(ASTokenTypes.TOKEN_SEMICOLON, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, ";");

    public BaseRepairingTokenBuffer(String sourcePath)
    {
        position = 0;
        eofToken = new ASToken(Token.EOF_TYPE, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "");
        eofToken.setSourcePath(sourcePath);
        errors = new ArrayList<SyntaxProblem>();
        nextIsSemicolon = false;
        insertSemis = true;
    }

    @Override
    public void setEnableSemicolonInsertion(boolean enable)
    {
        insertSemis = enable;
    }

    /**
     * This method is called when an optional semicolon is inserted and is going
     * to be returned as the next token. It is triggered in
     * {@link #insertSemicolon(boolean)}.
     */
    protected final void onSemicolonInserted()
    {
        nextIsSemicolon = true;
    }

    @Override
    public void addError(SyntaxProblem error)
    {
        errors.add(error);
    }

    @Override
    public final int mark()
    {
        return position;
    }

    @Override
    public ASToken LT(int i)
    {
        if (nextIsSemicolon)
            return SEMICOLON;

        ASToken token = lookAheadSkipInsertedSemicolon(i);
        assert token.verify() : "token failed verification: " + token.toString();
        return token;
    }

    /**
     * Look-ahead "i" tokens and ignore {@link #insertSemis} flag. Subclasses
     * provide implementations specific to how they access the token buffer.
     */
    abstract ASToken lookAheadSkipInsertedSemicolon(int i);

    @Override
    public ASToken lookAheadSkipInsertedSemicolon()
    {
        return lookAheadSkipInsertedSemicolon(1);
    }

    @Override
    public final int LA(final int i)
    {
        return LT(i).getType();
    }
}
