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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.parsing.ICMToken;
import org.apache.royale.compiler.problems.ASDocNotClosedProblem;
import org.apache.royale.compiler.problems.BadCharacterProblem;
import org.apache.royale.compiler.problems.CommentNotClosedProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLUnclosedTagProblem;

import antlr.CommonToken;
import antlr.Token;

/**
 * Base class for JFlex-based tokenizers (RawScriptTokenizer, RawTagTokenizer,
 * RawCSSTokenizer, RawMetadataTokenizer). Tokenizers should use %extends
 * BaseRawTokenizer and provide a definitino for getColumn(). yytext()) will be
 * generated automatically.
 */
public abstract class BaseRawMXMLTokenizer
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

    private Token lastToken = null;

    protected String sourcePath = null;

    public BaseRawMXMLTokenizer()
    {
        // no-arg constructor
    }

    /**
     * Gets the text of the current token. This is implemented by JFlex.
     * 
     * @return The text of current token
     */
    public abstract String yytext();

    /**
     * Gets the current offset of the tokenizer into the file buffer. This
     * should be implemented in the grammar file by returning JFlex's zzchar.
     * 
     * @return The current offset.
     */
    public abstract int getOffset();

    /**
     * Gets the current line number of the tokenizer. Line numbers start at 1,
     * not 0. This should be implemented in the grammar file by returning
     * JFlex's zzline.
     * 
     * @return The current line.
     */
    public abstract int getLine();

    /**
     * Gets the current column number of the tokenizer. Column numbers start at
     * 1, not 0. This should be implemented in the grammar file by returning
     * JFlex's zzcolumn.
     * 
     * @return The current column.
     */
    public abstract int getColumn();

    /*
     * Grammar files may override this to build various subclasses of Token such
     * as ASToken. The default is to build an ANTLR CommonToken.
     */
    protected Token buildToken(int type, int start, int end, int line, int column, String text)
    {
        CommonToken token = new CommonToken(type, text);
        token.setLine(line);
        token.setColumn(column);
        token.setFilename(sourcePath);
        lastToken = token;
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
    protected Token buildToken(int type, String text)
    {
        int start = getOffset();
        int end = start + text.length();
        int line = getLine();
        int column = getColumn();
        return buildToken(type, start, end, line, column, text);
    }

    /**
     * Build a token of the specified type, using the current yytext(),
     * getOffset(), getLine(), and getColumn(). The grammar file cannot override
     * this.
     * 
     * @param type token type (based on the appropriate XxxTokenTypes interface)
     * @return new token
     */
    protected Token buildToken(int type)
    {
        return buildToken(type, yytext());
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

    public void setSourcePath(String sourcePath)
    {
        this.sourcePath = sourcePath;
    }

    protected void setLastToken(Token token)
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

    protected void fillBuffer(StringBuilder builder)
    {
        builder.append(yytext());
    }

    protected final boolean hasAggregateContents()
    {
        return aggregateContents != null;
    }

    /**
     * Build a token from the current aggregated text and the given type
     * 
     * @param type token type (based on the appropriate XxxTokenTypes interface)
     * @return new token
     */
    protected Token buildAggregateToken(int type)
    {
        if (aggregateContents == null)
        {
            return null;
        }
        String contents = aggregateContents.toString();
        aggregateContents = null;
        return buildToken(type, aggregateStart, aggregateStart + contents.length(),
                          aggregateStartLine, aggregateStartColumn, contents);
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

    /**
     * Report an unclosed entity problem
     * 
     * @param The token
     */
    protected void reportUnclosedCDATA(MXMLToken token)
    {
        getProblems().add(new MXMLUnclosedTagProblem((ASToken)token, "CDATA"));
    }
    
    /**
     * Report an unclosed entity problem
     * 
     * @param The token
     */
    protected void reportUnclosedComment(MXMLToken token)
    {
        getProblems().add(new CommentNotClosedProblem((ASToken)token));
    }
    
    /**
     * Report an unclosed entity problem
     * 
     * @param The token
     */
    protected void reportUnclosedASDocComment(MXMLToken token)
    {
        getProblems().add(new ASDocNotClosedProblem((ASToken)token));
    }

    protected void reportBadCharacterProblem(String badChar)
    {
        ISourceLocation location = getCurrentSourceLocation(badChar.length());
        ICompilerProblem problem = new BadCharacterProblem(location, badChar);
        getProblems().add(problem);
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
                sourcePath,
                getOffset(),
                getOffset() + tokenLength,
                getLine(),
                getColumn());
    }
    
    protected List<ICompilerProblem> problems = null;

    /**
     * @return true if we encountered errors while tokenizing
     */
    public boolean hasProblems()
    {
        return problems != null && problems.size() > 0;
    }

    /**
     * @return any problems we encountered while parsing
     */
    public List<ICompilerProblem> getProblems()
    {
        if (problems == null)
            problems = new ArrayList<ICompilerProblem>(0);
        return problems;
    }

    public boolean hasBufferToken()
    {
        return false;
    }

    public ASToken getBufferToken()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isInE4XDatabinding()
    {
        return false;
    }
}
