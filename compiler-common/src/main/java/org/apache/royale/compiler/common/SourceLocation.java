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

package org.apache.royale.compiler.common;

import org.apache.royale.utils.FilenameNormalization;

/**
 * Common class to store file/location information across all source types
 * such as AS, CSS etc
 */
public class SourceLocation implements ISourceLocation
{
    /**
     * Constructor for a known source location.
     */
    public SourceLocation(String sourcePath, int start, int end,
                          int line, int column, int endLine, int endColumn)
    {
        this.sourcePath = sourcePath;
        this.start = start;
        this.end = end;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    /**
     * For backwards compatibility
     */
    public SourceLocation(String sourcePath, int start, int end, int line, int column)
    {
        this(sourcePath, start, end, line, column, UNKNOWN, UNKNOWN);
    }
    
    /**
     * Copy Constructor for a known source location.
     */
    public SourceLocation(ISourceLocation location)
    {
        this(location.getSourcePath(),
             location.getStart(),
             location.getEnd(),
             location.getLine(),
             location.getColumn(),
             location.getEndLine(),
             location.getEndColumn());
    }

    /**
     * Constructor for an unknown source location.
     */
    public SourceLocation()
    {
        this(null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);
    }

    /**
     * Source path.
     */
    private String sourcePath;

    /**
     * Zero-based starting offset.
     * <p>
     * This class does not distinguish between local and absolute offsets,
     * but subclasses may. If so, they should store the absolute starting
     * offset in this field and use it to compute the local starting offset.
     */
    private int start;
    
    /**
     * Zero-based ending offset.
     * <p>
     * This class does not distinguish between local and absolute offsets,
     * but subclasses may. If so, they should store the absolute ending
     * offset in this field and use it to compute the local ending offset.
     */
    private int end;
    
    /**
     * Zero-based line number.
     * Corresponds to start, not end.
     */
    private int line;
    
    /**
     * Zero-based column number.
     * Corresponds to start, not end.
     */
    private int column;

    /**
     * Zero-based line number that corresponds to end.
     */
    private int endLine;

    /**
     * Zero-based column number that corresponds to end.
     */
    private int endColumn;
    
    /**
     * Copies source location information from another instance
     * into this instance.
     */
    public final void setSourceLocation(ISourceLocation src)
    {
        assert src != null : "source location can't be null";
        
        this.start = src.getStart();
        this.end = src.getEnd();
        this.line = src.getLine();
        this.column = src.getColumn();
        this.endLine = src.getEndLine();
        this.endColumn = src.getEndColumn();
        this.sourcePath = src.getSourcePath();
    }

    /**
     * @return The local start offset
     */
    @Override
    public int getStart()
    {
        assert start >= 0 || start == UNKNOWN : "Invalid value for start: " + start;
        return start;
    }

    /**
     * @return The absolute start offset.
     */
    @Override
    public int getAbsoluteStart()
    {
        assert start >= 0 || start == UNKNOWN : "Invalid value for start: " + start;
        return start;
    }
    
    /**
     * @return The absolute end offset.
     */
    @Override
    public int getAbsoluteEnd()
    {
        assert end >= 0 || end == UNKNOWN : "Invalid value for end: " + end;
        return end;
    }
    
    /**
     * Set the absolute offset where this node starts.
     */
    public void setStart(int start)
    {
        if (start != UNKNOWN)
            this.start = start;
    }

    /**
     * @return The local end offset.
     */
    @Override
    public int getEnd()
    {
        assert end >= 0 || end == UNKNOWN : "Invalid value for end: " + end;
        return end;
    }

    /**
     * Set the absolute offset where this node ends.
     */
    public void setEnd(int end)
    {
        if (end != UNKNOWN)
            this.end = end;
    }

    /**
     * Get the line number where this node starts.
     * Line numbers start at 0, not 1.
     * @return The line number
     */
    @Override
    public int getLine()
    {
        assert line >= 0 || line == UNKNOWN : "Invalid value for line: " + line;
        return line;
    }

    /**
     * Set the line number where this node starts.
     * Column numbers start at 0, not 1.
     * @param line The line number
     */
    public void setLine(int line)
    {
        if (line != UNKNOWN)
            this.line = line;
    }

    /**
     * Get the column number where this node starts.
     * @return The column number
     */
    @Override
    public int getColumn()
    {
        assert column >= 0 || column == UNKNOWN : "Invalid value for column: " + column;
        return column;
    }

    /**
     * Set the column number where this node starts.
     * @param column The column number
     */
    public void setColumn(int column)
    {
        if (column != UNKNOWN)
            this.column = column;
    }

    /**
     * Get the line number where this node ends.
     * Line numbers start at 0, not 1.
     * @return The line number
     */
    public int getEndLine()
    {
        return endLine;
    }

    /**
     * Set the line number where this node ends.
     * Column numbers start at 0, not 1.
     * @param line The line number
     */
    public void setEndLine(int line)
    {
        this.endLine = line;
    }

    /**
     * Get the column number where this node ends.
     * @return The column number
     */
    public int getEndColumn()
    {
        return endColumn;
    }

    /**
     * Set the column number where this node ends.
     * @param column The column number
     */
    public void setEndColumn(int column)
    {
        this.endColumn = column;
    }

    /**
     * Get the source path for this node.
     * @return The source path for this node
     */
    @Override
    public final String getSourcePath()
    {
        // null means the source is unknown.
        // "" means the source is a buffer that hasn't yet been saved to a file.
        // Something like "framework.swc:defaults.css" means the source is a file inside a SWC.
        // TODO Shouldn't the part before the colon be normalized?
        // Anything else should be a normalized path to a source file.
        assert sourcePath == null ||
               sourcePath.isEmpty() ||
               sourcePath.contains(".swc:") ||
               FilenameNormalization.isNormalized(sourcePath)  :
               "Invalid value for sourcePath: " + sourcePath;
        return sourcePath;
    }

    /**
     * Set the source path of the node.
     * @param sourcePath The source path
     */
    public final void setSourcePath(String sourcePath)
    {
        this.sourcePath = sourcePath;
    }
    
    /**
     * Displays line, column, start, end, and sourcepath in a format such as 
     * <pre>
     * "17:5 160-188 C:\test.as"
     * </pre>
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getLineColumnString());
        sb.append(getOffsetsString());
        sb.append(getSourcePathString());
        return sb.toString();
    }

    /**
     * Displays Line and Column numbers
     */
    protected String getLineColumnString()
    {
        StringBuilder sb = new StringBuilder();
        int line = getLine();
        if (line != UNKNOWN)
            sb.append(line);
        else
            sb.append('?');
        sb.append(':');
        int column = getColumn();
        if (column != UNKNOWN)
            sb.append(column);
        else
            sb.append('?');
        
        sb.append(' ');
        return sb.toString();
    }

    /**
     * Displays sourcepath
     * 
     */
    protected String getSourcePathString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(' ');
        
        String sourcePath = getSourcePath();
        if (sourcePath != null)
            sb.append(sourcePath);
        else
            sb.append('?');
        return sb.toString();
    }

    /**
     * Displays line, column, start, end
     * 
     */
    protected String getOffsetsString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("loc: ");
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

        sb.append("abs: ");
        int absoluteStart = getAbsoluteStart();
        if (absoluteStart != UNKNOWN)
            sb.append(absoluteStart);
        else
            sb.append('?');
        sb.append('-');
        int absoluteEnd = getAbsoluteEnd();
        if (absoluteEnd != UNKNOWN)
            sb.append(absoluteEnd);
        else
            sb.append('?');
        return sb.toString();
    }
   
    
    /**
     * Span the location range from {@code start} to {@code end}.
     * 
     * @param start Start location.
     * @param end End location.
     */
    public final void span(ISourceLocation start, ISourceLocation end)
    {
        setSourcePath(start.getSourcePath());
        setStart(start.getStart());
        setEnd(end.getEnd());
        setLine(start.getLine());
        setColumn(start.getColumn());
        setEndLine(end.getEndLine());
        setEndColumn(end.getEndColumn());
    }
    
    /**
     * Span the location range from {@code start} to {@code end}.
     * 
     * @param location The location
     */
    public final void span(ISourceLocation location)
    {
        if (location != null)
            setSourceLocation(location);
    }
}
