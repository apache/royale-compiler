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
import java.util.List;

import antlr.Token;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.parsing.ICMToken;
import org.apache.royale.compiler.problems.BadCharacterProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Base class for JFlex-based tokenizers (RawScriptTokenizer, RawTagTokenizer,
 * RawCSSTokenizer, RawMetadataTokenizer). Tokenizers should use %extends
 * BaseRawTokenizer and provide a definitino for getColumn(). yytext()) will be
 * generated automatically.
 */
public abstract class BaseRawTokenizer<T extends TokenBase>
{
    /**
     * Start offset of aggregate
     */
    protected int aggregateStart;

    /**
     * Line number of aggregate
     */
    protected int aggregateStartLine;

    /**
     * Column number of aggregate
     */
    protected int aggregateStartColumn;

    /**
     * Contents of aggregate
     */
    protected StringBuilder aggregateContents;

    private T lastToken = null;

    private final List<ICompilerProblem> problems;

    /**
     * Token pool to cut down on object creation.
     */
    private final T[] tokenPool = initTokenPool();
    /**
     * Offset into the token pool
     */
    private int tokenPoolIndex = 5; //buffer room for tokens set to 5

    public BaseRawTokenizer()
    {
        problems = new ArrayList<ICompilerProblem>();
    }

    protected abstract T[] initTokenPool();

    /**
     * Gets the text of the current token. This is implemented by JFlex.
     * 
     * @return The text of current token
     */
    protected abstract String yytext();

    private String sourcePath;

    /**
     * Gets the source path for the file being tokenized. This source path is
     * used when reporting problems.
     */
    public String getSourcePath()
    {
        return sourcePath;
    }

    /**
     * Sets the source path for the file being tokenized. This source path is
     * used when reporting problems.
     */
    public void setSourcePath(String sourcePath)
    {
        this.sourcePath = sourcePath;
    }

    /**
     * Gets the current offset of the tokenizer into the file buffer. This
     * should be implemented in the grammar file by returning JFlex's zzchar.
     * 
     * @return The current offset.
     */
    protected abstract int getOffset();

    /**
     * Gets the current line number of the tokenizer. Lines numbers start at 0,
     * not 1. This should be implemented in the grammar file by returning
     * JFlex's zzline.
     * 
     * @return The current line.
     */
    protected abstract int getLine();

    /**
     * Gets the current column number of the tokenizer. Column numbers start at
     * 0, not 1. This should be implemented in the grammar file by returning
     * JFlex's zzcolumn.
     * 
     * @return The current column.
     */
    protected abstract int getColumn();

    protected void addBadCharacterProblem(String badChar)
    {
        ISourceLocation location = getCurrentSourceLocation(badChar.length());
        ICompilerProblem problem = new BadCharacterProblem(location, badChar);
        problems.add(problem);
    }

    /**
     * Create a {@code ISourceLocation} object based on the current lexer state.
     * 
     * @param tokenLength Length of the problematic input.
     * @return Current source location used to report a syntax problem.
     */
    protected final ISourceLocation getCurrentSourceLocation(int tokenLength)
    {
        return new SourceLocation(
                getSourcePath(),
                getOffset(),
                getOffset() + tokenLength,
                getLine(),
                getColumn());
    }

    /**
     * @return true if we encountered errors while tokenizing
     */
    public boolean hasProblems()
    {
        return problems.size() > 0;
    }

    /**
     * @return any problems we encountered while parsing
     */
    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }

    protected T buildToken(int type, int start, int end, int line, int column, CharSequence text)
    {
        final T token = fetchToken(type, start, start + text.length(), getLine(), getColumn(), text);
        setLastToken(token);
        return token;
    }

    public void reset()
    {
        lastToken = null;
        aggregateContents = null;
        aggregateStart = -1;
        aggregateStartLine = -1;
        aggregateStartColumn = -1;
    }

    public final int getLastTokenType()
    {
        return lastToken != null ? lastToken.getType() : -1;
    }

    public final String getLastTokenText()
    {
        return lastToken != null ? lastToken.getText() : "";
    }

    protected void setLastToken(T token)
    {
        lastToken = token;
    }

    /**
     * Initialize a new aggregate with the current yytext() and position
     */
    protected final void startAggregate()
    {
        aggregateStart = getOffset();
        aggregateStartLine = getLine();
        aggregateStartColumn = getColumn();
        aggregateContents = new StringBuilder();
        fillBuffer(aggregateContents);
    }

    protected final void startAggregate(Token token)
    {
        aggregateStart = ((ICMToken)token).getStart();
        aggregateStartLine = ((ICMToken)token).getLine();
        aggregateStartColumn = ((ICMToken)token).getColumn();
        aggregateContents = new StringBuilder();
        aggregateContents.append(token.getText());
    }

    /**
     * Add the current yytext() to the current aggregate
     */
    protected void continueAggregate()
    {
        if (aggregateContents != null)
            fillBuffer(aggregateContents);
    }

    protected final void continueAggregate(Token token)
    {
        if (aggregateContents != null)
        {
            aggregateContents.append(token.getText());
        }
    }

    protected final void continueAggregate(String text)
    {
        if (aggregateContents != null)
        {
            aggregateContents.append(text);
        }
    }

    protected final void continueAggregate(char c)
    {
        if (aggregateContents != null)
        {
            aggregateContents.append(c);
        }
    }

    protected final void continueAggregate(char chars[])
    {
        if (aggregateContents != null)
        {
            aggregateContents.append(chars);
        }
    }

    protected abstract void fillBuffer(StringBuilder builder);

    protected final boolean hasAggregateContents()
    {
        return aggregateContents != null;
    }

    /**
     * signals that we should reuse the last token in the token pool without
     * filling our buffer
     */
    public final void setReuseLastToken()
    {
        if (tokenPoolIndex > 0)
            tokenPoolIndex--;
    }

    /**
     * Builds a token, or reuses a token, based on the underlying token pool
     * 
     * @param type the type of token to build
     * @param start the token start
     * @param end the token end
     * @param line the token line
     * @param column the token column
     * @param text the token text
     * @return a token from the passed in parameters. This token is not safe to
     * hold on to, and should be used and discarded before further tokens are
     * queried
     */
    protected final T fetchToken(final int type, final int start, final int end, final int line, final int column, final CharSequence text)
    {
        //no token poll.  remove this potentially
        if (tokenPool.length == 0)
        {
            return newToken(type, start, end, line, column, text);
        }
        //if the pool is full, and we are passed the length of the pool, reset our index to zero
        if (tokenPoolIndex >= tokenPool.length)
        {
            tokenPoolIndex = 0;
        }
        //try to use the new token.  Accept it if the token is not null, and it is not locked.  
        final T potential = tokenPool[tokenPoolIndex];
        if (potential != null && !potential.isLocked())
        {
            potential.reuse(type, start, end, line, column, text);
        }
        else
        //build a new token
        {
            tokenPool[tokenPoolIndex] = newToken(type, start, end, line, column, text);
        }
        return tokenPool[tokenPoolIndex++];
    }

    protected abstract T newToken(int type, int start, int end, int line, int column, CharSequence text);

    /**
     * Build a token from the current aggregated text and the given type
     * 
     * @param type token type (based on the appropriate XxxTokenTypes interface)
     * @return new token
     */
    public T buildAggregateToken(final int type)
    {
        if (aggregateContents == null)
        {
            return null;
        }
        final T token = fetchToken(type, aggregateStart, aggregateStart + aggregateContents.length(),
                          aggregateStartLine, aggregateStartColumn, aggregateContents.toString());
        aggregateContents = null;
        setLastToken(token);
        return token;
    }

    /**
     * Builds a token with the specified type and text, using the current
     * getOffset(), getLine(), and getColumn(). The grammar file should not
     * override this.
     * 
     * @param type token type (based on the appropriate XxxTokenTypes interface)
     * @return new token
     */
    public final T buildToken(final int type)
    {
        final String text = yytext();
        final int start = getOffset();
        final T token = fetchToken(type, start, start + text.length(), getLine(), getColumn(), text);
        setLastToken(token);
        return token;
    }

    /**
     * Build a token of the specified type, using the current yytext(),
     * getOffset(), getLine(), and getColumn(). The grammar file cannot override
     * this.
     * 
     * @param type token type (based on the appropriate XxxTokenTypes interface)
     * @return new token
     */
    public T buildToken(final int type, final String text)
    {
        final int start = getOffset();
        final T token = fetchToken(type, start, start + text.length(), getLine(), getColumn(), text);
        setLastToken(token);
        return token;
    }

    /**
     * Get the current context as a string (to help with debugging)
     * 
     * @param line current line number
     * @return description of current context
     */
    protected String getContext(int line)
    {
        return yytext() + " (" + line + ")";
    }
}
