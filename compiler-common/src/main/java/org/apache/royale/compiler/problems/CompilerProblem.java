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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;
import org.apache.royale.compiler.problems.annotations.ProblemClassification;
import org.apache.royale.utils.FilenameNormalization;

/**
 * CompilerProblem is the base class for all error and warning classes in the compiler,
 * which are collectively called "problems".
 * 
 * Each problem has
 * 1) source location information (file/start/end/line/column);
 * 2) a description with named placeholders which get filled in
 *    from the public fields of the problem class;
 * 3) the ability to compare itself to other problems,
 *    to produce a nicely sorted list of problems;
 * 4) the ability to toString() itself for command-line output such as
 * 
 *    C:\Faramir\compiler\trunk\tests\resources\milestones\m1\M1.as:4
 *    Syntax error: '+' is not allowed here
 *    import flash.display.+Sprite;
 *                         ^
 *    where a caret marks the offending spot in the offending line.
 */
@ProblemClassification(CompilerProblemClassification.DEFAULT)
@DefaultSeverity(CompilerProblemSeverity.ERROR)
public abstract class CompilerProblem implements ICompilerProblem
{
    /**
     * Constructor.
     *
     * @param sourcePath The path of the file in which the problem occurred.
     * @param start The offset within the source buffer at which the problem starts.
     * @param end The offset within the source buffer at which the problem ends.
     * @param line The line number within the source buffer at which the problem starts.
     * @param column The column number within the source buffer at which the problem starts.
     * @param endLine The line number within the source buffer at which the problem ends.
     * @param endColumn The column number within the source buffer at which the problem ends.
     * @param normalizeFilePath true if the path can be normalized. This is needed 
     * by configuration problems that have the "command line" as there source.
     */
    public CompilerProblem(String sourcePath, int start, int end,
                           int line, int column, int endLine, int endColumn,
                           boolean normalizeFilePath)
    {
        if (sourcePath != null && normalizeFilePath)
            sourcePath = FilenameNormalization.normalize(sourcePath);

        this.sourcePath = sourcePath;
        this.start = start;
        this.end = end;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }
    /**
     * Constructor.
     * 
     * @param sourcePath The path of the file in which the problem occurred.
     * @param start The offset within the source buffer at which the problem starts.
     * @param end The offset within the source buffer at which the problem ends.
     * @param line The line number within the source buffer at which the problem starts.
     * @param column The column number within the source buffer at which the problem starts.
     * @param normalizeFilePath true if the path can be normalized. This is needed 
     * by configuration problems that have the "command line" as there source.
     */
    public CompilerProblem(String sourcePath, int start, int end, int line, int column, 
                           boolean normalizeFilePath)
    {
        this(sourcePath, start, end, line, column, line, column, normalizeFilePath);
    }
        
    /**
     * Constructor.
     * 
     * @param sourcePath The normalized path of the file in which the problem occurred.
     * @param start The offset within the source buffer at which the problem starts.
     * @param end The offset within the source buffer at which the problem ends.
     * @param line The line number within the source buffer at which the problem starts.
     * @param column The column number within the source buffer at which the problem starts.
     */
    public CompilerProblem(String sourcePath, int start, int end, int line, int column)
    {
        this(sourcePath, start, end, line, column, true);
    }

    /**
     * Constructor.
     *
     * @param sourcePath The normalized path of the file in which the problem occurred.
     * @param start The offset within the source buffer at which the problem starts.
     * @param end The offset within the source buffer at which the problem ends.
     * @param line The line number within the source buffer at which the problem starts.
     * @param column The column number within the source buffer at which the problem starts.
     * @param endLine The line number within the source buffer at which the problem ends.
     * @param endColumn The column number within the source buffer at which the problem ends.
     */
    public CompilerProblem(String sourcePath, int start, int end, int line, int column, int endLine, int endColumn)
    {
        this(sourcePath, start, end, line, column, endLine, endColumn, true);
    }
    
    /**
     * Constructor for a problem whose only source-location information
     * is a source path.
     * 
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     */
    public CompilerProblem(String sourcePath)
    {
        this(sourcePath, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, true);
    }
    
    /**
     * Constructor for a problem with no source-location information.
     */
    public CompilerProblem()
    {
        this(null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, true);
    }
    
    /**
     * Constructor for a problem associated with an object
     * implementing {@link ISourceLocation}.
     * 
     * @param site The {@link ISourceLocation} where the problem occurred.
     */
    public CompilerProblem(ISourceLocation site)
    {
        this(site.getSourcePath(),
             site.getStart(), site.getEnd(),
             site.getLine(), site.getColumn(),
             site.getEndLine(), site.getEndColumn());
    }
    
    /**
     * Constructor for a problem associated with an {@link IDefinition}.
     * 
     * @param site The {@link IDefinition} where the problem occurred.
     */
    public CompilerProblem(IDefinition site)
    {
        this(site.getSourcePath(),
             site.getNameStart(), site.getNameEnd(),
             site.getNameLine(), site.getNameColumn(),
             site.getNameLine(),
             site.getNameColumn() + site.getNameEnd() - site.getNameStart());
    }

    /**
     * Constructor for a problem associated with an {@link IASToken}.
     * 
     * @param site The {@link IASToken} where the problem occurred.
     */
    public CompilerProblem(IASToken site)
    {
        this(site.getSourcePath(),
             site.getLocalStart(), site.getLocalEnd(),
             site.getLine(), site.getColumn(),
             site.getEndLine(), site.getEndColumn());
    }

    private final String sourcePath;
    private final int start;
    private final int end;
    private final int line;
    private final int column;
    private final int endLine;
    private final int endColumn;
    
    @Override
    public String getID()
    {
        // Return the fully-qualified classname of the CompilerProblem subclass
        // as a String to identify the type of problem that occurred.
        return getClass().getName();
    }
    
    @Override
    public String getSourcePath()
    {
        return sourcePath;
    }
    
    @Override
    public int getStart()
    {
        return start;
    }
    
    @Override
    public int getEnd()
    {
        return end;
    }
    
    @Override
    public int getLine()
    {
        return line;
    }
    
    @Override
    public int getColumn()
    {
        return column;
    }

    @Override
    public int getEndLine()
    {
        return endLine;
    }

    @Override
    public int getEndColumn()
    {
        return endColumn;
    }

    @Override
    public int getAbsoluteStart()
    {
        return start;
    }

    @Override
    public int getAbsoluteEnd()
    {
        return end;
    }
    
    /**
     * Returns a String for displaying this compiler problem in a console window.
     * 
     * This is for debugging purposes, therefore no non-test code should call this.
     * 
     * There are typically four lines output for each problem:
     *  location (file and line number)
     *  description
     *  
     * For example:
     * 
     * C:\Faramir\compiler\trunk\tests\resources\milestones\m1\M1.as:4
     * Syntax error: '+' not allowed here.
     */
    @Override
    public String toString()
    {
        return ProblemFormatter.DEFAULT_FORMATTER.format(this);
    }
}
