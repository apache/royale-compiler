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

import antlr.Token;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.internal.common.Counter;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.parsing.ICMToken;

/**
 * Base class of ASToken, MXMLToken, CSSToken
 */
public abstract class TokenBase extends Token implements ICMToken, ISourceLocation
{
    /**
     * Constructor
     * 
     * @param tokenType type of token
     * @param start location location information
     * @param end location location information
     * @param line location location information
     * @param column location location information
     * @param text actual text represented by token
     */
    public TokenBase(int tokenType, int start, int end, int line, int column, CharSequence text)
    {
        type = tokenType;
        localStart = this.start = start;
        localEnd = this.end = end;
        this.line = line;
        this.column = column;
        this.text = text;
        this.endLine = line;
        this.endColumn = column + end - start;

        if (Counter.COUNT_TOKENS)
            countTokens();
    }

    /**
     * Copy constructor
     * 
     * @param o token to copy
     */
    public TokenBase(TokenBase o)
    {
        type = o.type;
        start = o.start;
        end = o.end;
        line = o.line;
        column = o.column;
        endLine = o.endLine;
        endColumn = o.endColumn;
        text = o.text;

        localStart = o.localStart;
        localEnd = o.localEnd;
        sourcePath = o.sourcePath;

        if (Counter.COUNT_TOKENS)
            countTokens();
    }

    /**
     * Text represented by this token
     */
    private CharSequence text;

    /**
     * Start of this token
     */
    private int start;

    /**
     * End offset of this token
     */
    private int end;

    /**
     * Line of this token
     */
    private int line;

    /**
     * Column of this token
     */
    private int column;

    /**
     * End line of this token
     */
    private int endLine;

    /**
     * End column of this token
     */
    private int endColumn;

    /**
     * Flag to determine if this token is locked
     */
    private boolean locked;

    /**
     * Local start offset.
     */
    private int localStart;

    /**
     * Local end offset.
     */
    private int localEnd;

    protected abstract String getTypeString();

    /**
     * @return Local start offset.
     */
    public final int getLocalStart()
    {
        return localStart;
    }

    /**
     * @return Local end offset.
     */
    public final int getLocalEnd()
    {
        return localEnd;
    }

    public final void reuse(final int tokenType, final int start, final int end,
            final int line, final int column, final CharSequence text)
    {
        type = tokenType;
        this.start = start;
        this.end = end;
        this.line = line;
        this.column = column;
        this.endLine = line;
        this.endColumn = column + end - start;
        this.text = text;
    }

    /**
     * Locks this token. When locked, if this token is in a token pool, it will
     * not be overwritten
     */
    public void lock()
    {
        locked = true;
    }

    /**
     * Returns whether this token can be overwritten when it is a member of a
     * token pool
     * 
     * @return true if we are locked
     */
    public boolean isLocked()
    {
        return locked;
    }

    /**
     * Get the text represented by this token
     * 
     * @return text represented by this token
     * @see antlr.Token#getText()
     */
    @Override
    public String getText()
    {
        //we're either going to be a String or a StringBuilder
        //String.toString returns itself
        //StringBuilder toString returns a new String
        if (text != null)
            return text.toString();
        return "";
    }

    /**
     * Returns the underlying CharSequence that represents the contents of this
     * token
     * 
     * @return a {@link CharSequence} or null
     */
    public CharSequence getCharSequence()
    {
        return text;
    }

    /**
     * Set the text represented by this token
     * 
     * @param text text represented by this token
     * @see antlr.Token#setText(java.lang.String)
     */
    @Override
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Set the CharSequence represented by this token
     * 
     * @param text text represented by this token
     */
    public void setText(CharSequence text)
    {
        this.text = text;
    }

    public void setLocation(int start, int end, int line, int column)
    {
        this.start = start;
        this.end = end;
        this.line = line;
        this.column = column;
        this.endLine = line;
        this.endColumn = column + end - start;
    }

    @Override
    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    @Override
    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    @Override
    public int getLine()
    {
        return line;
    }

    @Override
    public void setLine(int line)
    {
        this.line = line;
    }

    public final boolean matchesLine(final TokenBase other)
    {
        return other != null && other.line == line;
    }

    @Override
    public int getColumn()
    {
        return column;
    }

    @Override
    public void setColumn(int column)
    {
        this.column = column;
    }

    @Override
    public int getEndLine()
    {
        return endLine;
    }

    public void setEndLine(int line)
    {
        endLine = line;
    }

    @Override
    public int getEndColumn()
    {
        return endColumn;
    }

    public void setEndColumn(int column)
    {
        endColumn = column;
    }

    /**
     * Determine whether or not this token is bogus (i.e. the start and end
     * offsets are the same, which implies that it was inserted from an included
     * file or during token fixup)
     * 
     * @return true iff the token is bogus
     */
    @Override
    public boolean isImplicit()
    {
        return start == end;
    }

    /**
     * For debugging only.
     */
    private String getEscapedText()
    {
        String text = getText();

        text = text.replaceAll("\n", "\\\\n");
        text = text.replaceAll("\r", "\\\\r");
        text = text.replaceAll("\t", "\\\\t");

        return text;
    }

    /**
     * For debugging only. This format is nice in the Eclipse debugger.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append('|');
        sb.append(getEscapedText());
        sb.append('|');

        sb.append(' ');

        sb.append(getTypeString());

        sb.append(' ');

        if (locked)
            sb.append("locked ");

        int line = getLine();
        if (line != UNKNOWN)
            sb.append(line + 1);
        else
            sb.append('?');
        sb.append(':');
        int column = getColumn();
        if (column != UNKNOWN)
            sb.append(column + 1);
        else
            sb.append('?');

        sb.append(' ');

        int start = getStart();
        if (start != UNKNOWN)
            sb.append(start);
        else
            sb.append('?');
        sb.append('-');
        int end = getEnd();
        if (end != UNKNOWN)
            sb.append(end);
        else
            sb.append('?');

        sb.append(' ');
        String sourcePath = getSourcePath();
        if (sourcePath != null)
        {
            sb.append('"');
            sb.append(sourcePath);
            sb.append('"');
        }
        else
        {
            sb.append('?');
        }

        return sb.toString();
    }

    /**
     * For debugging only. This format is nice in a text file.
     */
    public String toDumpString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getLine() + 1);
        sb.append('\t');
        sb.append(getColumn() + 1);
        sb.append('\t');
        sb.append(getStart());
        sb.append('\t');
        sb.append(getEnd());
        sb.append('\t');

        String typeString = getTypeString();
        sb.append(typeString);
        int n = 28 - typeString.length();
        for (int i = 0; i < n; i++)
            sb.append(' ');
        sb.append('\t');

        sb.append('|');
        sb.append(getEscapedText());
        sb.append('|');

        return sb.toString();
    }

    /**
     * Reduce the span of the token by removing characters from the beginning
     * and end. This is used to remove quote characters and such from tokens.
     * 
     * @param trimLeft number of characters to remove from the left of the token
     * @param trimRight number of characters to remove from the right of the
     * token
     */
    public void truncate(int trimLeft, int trimRight)
    {
        String text = getText();
        if (trimLeft + trimRight <= text.length())
        {
            text = text.substring(trimLeft, text.length() - trimRight);
            setText(text);
            start += trimLeft;
            end -= trimRight;
        }
    }

    /**
     * Adjust all associated offsets by the adjustment amount
     * 
     * @param offsetAdjustment amount to add to offsets
     */
    public void adjustOffsets(int offsetAdjustment)
    {
        start += offsetAdjustment;
        end += offsetAdjustment;
    }

    /**
     * Adjust all associated offsets by the adjustment amount
     * 
     * @param offsetAdjustment amount to add to offsets
     * @param lineAdjustment amount to add to the line number
     * @param columnAdjustment amount to add to the column number
     */
    public void adjustLocation(int offsetAdjustment, int lineAdjustment, int columnAdjustment)
    {
        start += offsetAdjustment;
        end += offsetAdjustment;
        line += lineAdjustment;
        column += columnAdjustment;
        endLine += lineAdjustment;
        endColumn += columnAdjustment;
    }

    /**
     * Capture the current start/end offsets as this token's local offsets. This
     * method is called in {@code StreamingASTokenizer#nextTokenFromReader()}
     * after the token is initialized, and before being updated by
     * {@link IncludeHandler#onNextToken}.
     */
    public final void storeLocalOffset()
    {
        this.localStart = start;
        this.localEnd = end;
    }

    private String sourcePath;

    @Override
    public final String getSourcePath()
    {
        return sourcePath;
    }

    public final void setSourcePath(String path)
    {
        this.sourcePath = path;
    }

    /**
     * Verifies that this token has its type and location information set.
     * <p>
     * This is used only in asserts.
     */
    public boolean verify()
    {
        // Verify the token type.
        int type = getType();
        assert type != 0 : "Token has no type: " + toString();

        // Verify the source location (except for EOF tokens,
        // which are special and don't have a source location).
        if (type != ASTokenTypes.EOF)
        {
            assert getStart() != UNKNOWN : "Token has unknown start: " + toString();
            assert getEnd() != UNKNOWN : "Token has unknown end: " + toString();
            assert getLine() != UNKNOWN : "Token has an unknown line: " + toString();
            assert getColumn() != UNKNOWN : "Token has an unknown column: " + toString();
            assert getEndLine() != UNKNOWN : "Token has an unknown end line: " + toString();
            assert getEndColumn() != UNKNOWN : "Token has an unknown end column: " + toString();
        }

        return true;
    }

    /**
     * Counts various types of tokens that are created, as well as the total
     * number of tokens.
     */
    private void countTokens()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("TokenBase incrementing counter for " + getClass().getSimpleName());
        Counter counter = Counter.getInstance();
        counter.incrementCount(getClass().getSimpleName());
        counter.incrementCount("tokens");
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("TokenBase done incrementing counter for " + getClass().getSimpleName());
    }

    @Override
    public int getAbsoluteEnd()
    {
        return getEnd();
    }

    @Override
    public int getAbsoluteStart()
    {
        return getStart();
    }
}
