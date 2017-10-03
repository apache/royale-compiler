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

import java.util.ArrayList;

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IASToken.ASTokenKind;

/**
 * Buffer used that supports streaming of tokens, instead of a pre-computed
 * list. This token buffer is used by the ActionScript compiler.
 */
public final class StreamingTokenBuffer extends BaseRepairingTokenBuffer implements IRepairingTokenBuffer
{
    /**
     * The "rewind buffer size" is the limit of the number of tokens allowed in
     * a single syntactic predicate.
     */
    private static final int REWIND_BUFFER_SIZE = 10;

    private final StreamingASTokenizer tokenizer;
    private final ArrayList<ASToken> buffer;
    private int bufferSize;
    private ASToken previousToken;

    public StreamingTokenBuffer(final StreamingASTokenizer tokens)
    {
        super(tokens.getSourcePath());
        tokenizer = tokens;
        buffer = new ArrayList<ASToken>();
        for (int i = 0; i < REWIND_BUFFER_SIZE; i++)
        {
            buffer.add(eofToken);
        }
        bufferSize = 0;
        previousToken = eofToken;
    }

    /**
     * @return Path of the token source.
     */
    public String getSourcePath()
    {
        return tokenizer.getSourcePath();
    }

    @Override
    public final boolean insertSemicolon(final boolean isNextToken)
    {
        if (!insertSemis)
            return false;
        if (isNextToken)
            onSemicolonInserted();
        return true;
    }

    private final void fill(final int distance)
    {
        for (int pos = 0; pos < distance; pos++)
        {
            final ASToken next = tokenizer.next();
            buffer.add(next);
            bufferSize++;
        }
    }

    @Override
    public void rewind(final int position)
    {
        final int backSteps = this.position - position;
        if (backSteps > REWIND_BUFFER_SIZE)
        {
            throw new IllegalStateException(String.format(
                    "Token buffer can't rewind that far. Max rewind is %d, but got %d.",
                    REWIND_BUFFER_SIZE,
                    backSteps));
        }

        for (int i = 0; i < backSteps; i++)
        {
            // Left-pad the buffer with EOF tokens to push the look-ahead tokens further.
            buffer.add(0, eofToken);
            bufferSize++;
        }
        this.position = position;
    }

    @Override
    public final void consume()
    {
        if (nextIsSemicolon)
        {
            nextIsSemicolon = false;
            previousToken = SEMICOLON;
        }
        else
        {
            position++;
            // "fBufferSize" is the number of tokens, not including 
            // the last EOF in the buffer, or the rewind buffer tokens. 
            if (bufferSize > 0)
            {
                assert previousToken != null;
                previousToken = buffer.get(REWIND_BUFFER_SIZE);
                buffer.remove(1);
                bufferSize--;
                assert bufferSize >= 0 : "fBufferSize can not be negative";
            }
        }
    }

    @Override
    protected ASToken lookAheadSkipInsertedSemicolon(int i)
    {
        assert bufferSize + REWIND_BUFFER_SIZE == buffer.size() : "buffer size out-of-sync";
        if (bufferSize < i)
        {
            fill(i - bufferSize);
        }
        final ASToken result = buffer.get(REWIND_BUFFER_SIZE - 1 + i);
        if (result != null)
            result.lock();
        return result != null ? result : eofToken;
    }

    /**
     */
    public IASToken[] getTokens(final boolean includeInserted)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ASToken previous()
    {
        return previousToken != null ? previousToken : eofToken;
    }

    /**
     * Match optional semicolon.
     * <p>
     * This function implements the first 2 optional semicolon insertion rules
     * in the ECMA specification.
     * 
     * @see "ECMA 2.6.2 Chapter 7.9.1 Rules of Automatic Semicolon Insertion"
     */
    @Override
    public boolean matchOptionalSemicolon()
    {
        final ASToken nextToken = LT(1);
        if (nextToken == null)
        {
            // Pass -- end of file
        }
        else if (nextToken.getType() == ASTokenTypes.EOF)
        {
            // Pass -- end of file
        }
        else if (nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON)
        {
            // Found the semicolon.
            consume();
        }
        else if (nextToken.getTokenKind() == ASTokenKind.SCOPE_CLOSE)
        {
            // Pass - the "offending token" is a "}".
        }
        else if (nextToken.getType() == ASTokenTypes.TOKEN_KEYWORD_ELSE)
        {
            // Pass - the "offending token" is "else".            
        }
        else if (nextToken.getLine() > previous().getLine())
        {
            // Insert - the "offending token" is on another line.
            insertSemicolon(false);
        }
        else if (!nextToken.getSourcePath().equals(previous().getSourcePath()))
        {
            // Insert - the "offending token" is in another file.
            // The previous token is in an included file.
            insertSemicolon(false);
        }
        else
        {
            // Failed to insert a virtual semicolon.
            return false;
        }
        return true;
    }
}
