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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.royale.compiler.parsing.IASToken;

/**
 * The token buffer used by IDE features such as code hinting and syntax
 * highlighting.
 */
public class RepairingTokenBuffer extends BaseRepairingTokenBuffer implements IRepairingTokenBuffer
{
    private final ASToken[] tokens;
    private final int size;

    /**
     * A sorted set of offsets where optional semicolon tokens are inserted.
     */
    private final LinkedHashSet<Integer> semicolonInsertions;

    /**
     * Constructor
     * 
     * @param tokens list of ASTokens (or any subclass of antlr.Token)
     */
    public RepairingTokenBuffer(ASToken[] tokens)
    {
        super(null);
        this.tokens = tokens;
        size = tokens.length;
        semicolonInsertions = new LinkedHashSet<Integer>();
    }

    @Override
    public final boolean insertSemicolon(final boolean isNextToken)
    {
        if (!insertSemis)
            return false;
        final boolean isAdded = semicolonInsertions.add(position - 1);
        if (isAdded && isNextToken)
            onSemicolonInserted();
        return isAdded;
    }

    @Override
    public final void rewind(int position)
    {
        this.position = position;
    }

    @Override
    public final void consume()
    {
        if (nextIsSemicolon)
            nextIsSemicolon = false;
        else
            position++;
    }

    @Override
    protected final ASToken lookAheadSkipInsertedSemicolon(int i)
    {
        // Position of the look-ahead token in the token buffer.
        final int lookAheadPos = position + i - 1;
        if (lookAheadPos < size)
            return tokens[lookAheadPos];
        else
            return eofToken;
    }

    /**
     * Get all the tokens in this buffer.
     * 
     * @param includeInserted True if automatically inserted semicolons are
     * included in the result.
     * @return All the tokens in the buffer.
     */
    public IASToken[] getTokens(boolean includeInserted)
    {
        if (!includeInserted)
            return tokens;

        final List<ASToken> tokens = new ArrayList<ASToken>(Arrays.asList(this.tokens));
        int adjust = 0;

        // "pos" is the index of the token after the to-be-inserted semicolon
        for (final Integer pos : semicolonInsertions)
        {
            final ASToken tokenBeforeSemicolon = tokens.get(pos);
            final ASToken semicolon = new ASToken(
                    ASTokenTypes.TOKEN_SEMICOLON,
                    tokenBeforeSemicolon.getEnd(),
                    tokenBeforeSemicolon.getEnd(),
                    tokenBeforeSemicolon.getLine(),
                    tokenBeforeSemicolon.getColumn() + 1,
                    ";");
            adjust++;
            final int insertIndex = pos + adjust;
            tokens.add(insertIndex, semicolon);
        }
        return tokens.toArray(new ASToken[0]);
    }

    @Override
    public ASToken previous()
    {
        final int prevPos = position - 2;
        if (prevPos < size && prevPos > 0)
            return tokens[prevPos];
        else
            return eofToken;
    }

    /**
     * If we don't have the optional semicolon, always insert it. This
     * implementation is much looser than
     * {@link StreamingTokenBuffer#matchOptionalSemicolon()} in order to appease
     * code model.
     * 
     * @return Always true.
     */
    @Override
    public boolean matchOptionalSemicolon()
    {
        if (LA(1) == ASTokenTypes.TOKEN_SEMICOLON)
            consume();
        else
            insertSemicolon(false);
        return true;
    }
}
