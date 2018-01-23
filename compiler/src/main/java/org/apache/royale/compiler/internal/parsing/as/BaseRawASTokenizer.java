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

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.problems.ASDocNotClosedProblem;
import org.apache.royale.compiler.problems.CDataNotClosedProblem;
import org.apache.royale.compiler.problems.CommentNotClosedProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.StringLiteralMustBeTerminatedBeforeLineBreakProblem;
import org.apache.royale.compiler.problems.StringLiteralNotClosedProblem;

/**
 * Base class for RawActionScriptTokenizer. Pulling out code into Java backing
 * class so it's easier to deal with. Uses a token pool to avoid object
 * creation. The token pool will keep a buffer of 10 tokens in memory, and it is
 * generally unsafe to hold on to tokens passed the point they are needed. If
 * tokens are to be preserved, their copy constructor should be used
 */
public abstract class BaseRawASTokenizer extends BaseRawTokenizer<ASToken>
{
    /**
     * Tracks if we are in a close tag. Used in the generated code
     */
    protected boolean isInCloseTag = false;

    /**
     * The depth of open tags, used to determine if we're still in E4X content.
     * Each time we see a tag open, this count is incremented. When we see any
     * kind of tag close (</ or />) we decrement the counter.
     */
    protected int e4xTagDepth = 0;

    /**
     * The depth of the braces in E4X content. Used to determine when to pop out
     * of E4X databindings and back to E4X content
     */
    protected int e4xBraceBalance = 0;

    /**
     * State to return to from E4X. Used in the generated code
     */
    protected int e4xReturnState = RawASTokenizer.E4X;

    /**
     * Depth of typed collection constructs. Used in the generated code
     */
    protected int typedDepth = 0;

/**
     * Nested '<' bracket level (for <!DOCTYPE et al)
     */
    protected int docTypeLevel;

    /**
     * Flag to indicate we should collect comments
     */
    protected boolean collectComments = false;

    /**
     * token that we may need to return for rules that may return more than one
     * token
     */
    protected ASToken bufferToken;

    @Override
    protected void continueAggregate()
    {
        if (!hasAggregateContents())
            super.startAggregate();
        else
            super.continueAggregate();
    }

    /**
     * Convert the unicode escape sequence and append the unicode character to
     * text buffer. The input <b>must</b> have the leading escape character "\".
     * 
     * @param escapeSequence Unicode escape sequence like {@code \u00FF} or
     * {@code \xFF}.
     */
    protected final void aggregateEscapedUnicodeChar(final String escapeSequence)
    {
        final int unicode = decodeEscapedUnicode(escapeSequence);
        //check to make sure we are in valid unicode range
        if (!Character.isValidCodePoint(unicode))
            addBadCharacterProblem(yytext());
        else
            continueAggregate(Character.toChars(unicode));
    }

    /**
     * Build a token from the aggregation buffer.
     * 
     * @param type Type of the new token.
     * @return New token.
     */
    @Override
    public final ASToken buildAggregateToken(final int type)
    {
        final ASToken token = fetchToken(
                type,
                aggregateStart,
                getOffset() + (markedPosition() - readStart()),
                aggregateStartLine,
                aggregateStartColumn,
                super.aggregateContents);
        aggregateContents = null;
        setLastToken(token);
        return token;
    }

    /**
     * Build an e4x text token from a given entity type.
     */
    protected final ASToken buildE4XTextToken(final int type)
    {
        final ASToken token = buildToken(type);
        if (hasAggregateContents())
        {
            bufferToken = token;
            final int ttype;
            if (((RawASTokenizer)this).yystate() == RawASTokenizer.E4XTEXTVALUE)
                ttype = ASTokenTypes.TOKEN_E4X_TEXT;
            else
                ttype = ASTokenTypes.TOKEN_E4X_STRING;
            return buildAggregateToken(ttype);
        }
        else
        {
            aggregateContents = null;
            return token;
        }
    }

    @Override
    protected final void fillBuffer(StringBuilder builder)
    {
        builder.append(buffer(), readStart(), markedPosition() - readStart());
    }

    /**
     * returns the start of the current read
     * 
     * @return a non-negative int
     */
    protected abstract int readStart();

    /**
     * returns the marked position in the current read
     * 
     * @return a non-negative int
     */
    protected abstract int markedPosition();

    /**
     * returns the end of the current read
     * 
     * @return a non-negative int
     */
    protected abstract int readEnd();

    /**
     * returns the current active buffer. this will change and is not constant
     * 
     * @return the current buffer
     */
    protected abstract char[] buffer();

    /**
     * True if we want to collect comments, besides ASDoc. ASDoc comments will
     * always be returned
     * 
     * @param collect <code>true</code to collect non-ASDoc comments,
     * <code>false</code> to ignore them.
     */
    public void setCollectComments(final boolean collect)
    {
        collectComments = collect;
    }

    /**
     * in E4X, <code>&lt;</code> is allowed in char and string literals.
     * 
     * @param allow true if <code>&lt;</code> is allowed
     */
    protected abstract void setAllowLTInE4XStringLiterals(boolean allow);

    @Override
    protected ASToken[] initTokenPool()
    {
        return new ASToken[10];
    }

    /**
     * @return true if we are still parsing XML content
     */
    public boolean isInXML()
    {
        return e4xTagDepth > 0 && e4xBraceBalance == 0;
    }

    /**
     * @return true if we are parsing inside of an E4x databinding expression
     */
    public boolean isInE4XDatabinding()
    {
        return e4xBraceBalance > 0;
    }

    /**
     * begins the current state in the lexer
     * 
     * @param state the state to begin
     */
    protected abstract void yybegin(int state);

    /**
     * returns a char at the given offset into the internal char buffer
     * 
     * @param pos the offset into the buffer
     * @return a char
     */
    protected abstract char yycharat(int pos);

    /**
     * returns the length of the current read
     * 
     * @return a non-negative int
     */
    protected abstract int yylength();

    @Override
    protected final ASToken newToken(final int type, final int start, final int end, final int line, final int column, final CharSequence text)
    {
        return new ASToken(type, start, end, line, column, text);
    }

    /**
     * @return true if we are collecting comments
     */
    public final boolean isCollectingComments()
    {
        return collectComments;
    }

    public final ASToken getBufferToken()
    {
        final ASToken retVal = bufferToken.clone();
        bufferToken = null;
        return retVal;
    }

    public final boolean hasBufferToken()
    {
        return bufferToken != null;
    }

    @Override
    public void reset()
    {
        super.reset();
        e4xBraceBalance = 0;
        e4xTagDepth = 0;
    }

    /**
     * Matches escaped unicode sequence like {@code \u00FF}.
     */
    static final String PATTERN_U4 = "\\\\u[a-fA-F0-9]{4}";

    /**
     * Matches escaped unicode sequence like {@code \xFF}.
     */
    private static final String PATTERN_X2 = "\\\\x[a-fA-F0-9]{2}";

    /**
     * Matches either {@link #PATTERN_U4} or {@link #PATTERN_X2}.
     */
    private static final String PATTERN_UNICODE = String.format("(%s)|(%s)", PATTERN_U4, PATTERN_X2);

    /**
     * Convert escaped unicode sequence such as {@code \u00FF} and {@code \xFF}
     * to HTML entities like {@code &#xFF}.
     * 
     * @param escapedUnicode Escaped unicode sequence in the form of either
     * {@code \u0000} or {@code \xFF}.
     * @return Encoded HTML entity string.
     */
    protected String escapedUnicodeToHtmlEntity(final String escapedUnicode)
    {
        final int unicode = decodeEscapedUnicode(escapedUnicode);
        return String.format("&#x%H;", unicode);
    }

    /**
     * Report unexpected line terminators in a string literal.
     */
    protected final void reportInvalidLineTerminatorInStringLiteral()
    {
        final ISourceLocation location = getCurrentSourceLocation(0);
        final ICompilerProblem problem = new StringLiteralMustBeTerminatedBeforeLineBreakProblem(location);
        getProblems().add(problem);
    }

    /**
     * Report syntax error: input ended before reaching the closing quotation
     * mark for a string literal.
     */
    protected final void reportUnclosedStringLiteral()
    {
        final ISourceLocation location = getCurrentSourceLocation(0);
        final ICompilerProblem problem = new StringLiteralNotClosedProblem(location);
        getProblems().add(problem);
    }

    /**
     * Report syntax error: input ended before ASDoc is closed.
     */
    protected final void reportUnclosedASDoc()
    {
        final ISourceLocation location = getCurrentSourceLocation(0);
        final ICompilerProblem problem = new ASDocNotClosedProblem(location);
        getProblems().add(problem);
    }

    /**
     * Report syntax error: input ended before Comment is closed.
     */
    protected final void reportUnclosedComment()
    {
        final ISourceLocation location = getCurrentSourceLocation(0);
        final ICompilerProblem problem = new CommentNotClosedProblem(location);
        getProblems().add(problem);
    }

    /**
     * Report syntax error: input ended before CDATA is closed.
     */
    protected final void reportUnclosedCDATA()
    {
        final ISourceLocation location = getCurrentSourceLocation(0);
        final ICompilerProblem problem = new CDataNotClosedProblem(location);
        getProblems().add(problem);
    }

    /**
     * Convert escaped unicode sequence such as {@code \u00FF} and {@code \xFF}
     * to unicode code point.
     * 
     * @param escapedUnicode Escaped unicode sequence in the form of either
     * {@code \u0000} or {@code \xFF}.
     * @return Unicode number.
     */
    protected static int decodeEscapedUnicode(final String escapedUnicode)
    {
        if (escapedUnicode == null)
            throw new IllegalArgumentException("Escape sequence can't be null");

        if (!escapedUnicode.matches(PATTERN_UNICODE))
            throw new IllegalStateException("Only call this method from a lexer rule that matches unicode sequence pattern.");

        return Integer.parseInt(escapedUnicode.substring(2), 16);
    }

}
