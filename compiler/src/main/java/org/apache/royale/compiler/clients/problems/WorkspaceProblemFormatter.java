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

package org.apache.royale.compiler.clients.problems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

import org.apache.commons.io.input.NullReader;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.CompilerProblemClassification;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Problem formatter class that reports more detailed readable description of a
 * problem. In addition to the problem message, this class reports the text
 * where the problem occurred if any. This class requires a Workspace to access
 * problem's file and the necessary line information. 
 * 
 * All the command line tools such as MXMLC, ASC should use this class for 
 * problem reporting as we would like to give as much detail as possible to 
 * the users in command line.
 */
public final class WorkspaceProblemFormatter extends ProblemFormatter
{
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final int MAX_CACHED_LINES_PER_FILE = 10;

    // The ProblemLocalizer appends these id and strings after the 
    // problem descriptions in a generated properties file.
    private static String ERROR_FORMAT_ID = "ErrorFormat";       
    private static String WARNING_FORMAT_ID = "WarningFormat"; 
    private static String SYNTAXERROR_FORMAT_ID = "SyntaxErrorFormat";        
    private static String INTERNALERROR_FORMAT_ID = "InternalErrorFormat";       
    private static String LOCATION_FORMAT_ID = "LocationFormat";
    private static String LOCATION_FORMAT_STRING = "%s(%d): col: %d";    
    
    private final Workspace workspace;
    private final LoadingCache<String, FileLineInfo> readers;
    private final CompilerProblemCategorizer problemCategorizer;
    
    /**
     * Constructor.
     * 
     * @param workspace workspace that contains files associated with specified 
     * problems.
     */
    public WorkspaceProblemFormatter(final Workspace workspace)
    {
        this(workspace, null);
    }
    
    /**
     * Constructor.
     * 
     * @param workspace workspace that contains files associated with specified 
     * problems.
     * @param problemCategorizer a class the categorizes problems into one of the
     * values in {@link CompilerProblemSeverity}. Adds "Error:" and "Warning:" into
     * the format of a problem. If null, the problem categories will not be added 
     * to the output. 
     */
    public WorkspaceProblemFormatter(final Workspace workspace,
            CompilerProblemCategorizer problemCategorizer)
    {
        super();
        
        this.workspace = workspace;
        this.problemCategorizer = problemCategorizer;
        
        readers = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .softValues()
            .build(
            new CacheLoader<String, FileLineInfo>()
            {
                @Override
                public FileLineInfo load(String fileName)
                {
                    return new FileLineInfo(fileName);
                }
            });
    }

    @Override
    public String format(ICompilerProblem problem)
    {   
        StringBuffer buffer = new StringBuffer();
        
        final String locationString = getLocationString(problem.getSourcePath(), problem.getLine(), problem.getColumn());
        if (!locationString.isEmpty()) 
        {
            buffer.append(locationString);
            buffer.append(" ");
        }
        
        String description = super.format(problem);
        
        if (problemCategorizer != null)
        {
            // Prepend the classification on the front (ex: "Error: ")
            description = String.format(getProblemFormat(problem), description);
        }
        
        assert description != null;
        buffer.append(description);
        buffer.append(NEW_LINE);
        buffer.append(NEW_LINE);

        final String lineText = getLineText(problem);
        if (lineText != null)
        {
            buffer.append(lineText);
            buffer.append(NEW_LINE);
            buffer.append(getLinePointer(lineText, problem.getColumn()));
            buffer.append(NEW_LINE);
        }
        
        return buffer.toString();
    }
    
    /**
     * 
     * @param problem the problem whose formatter we want
     * @return an "sprints format string", like "Error: %s"
     */
    private String getProblemFormat(ICompilerProblem problem)
    {
        CompilerProblemSeverity severity = problemCategorizer.getProblemSeverity(problem);
        CompilerProblemClassification classification = problemCategorizer.getProblemClassification(problem);
        
        String formatString=null;
        if (classification == CompilerProblemClassification.DEFAULT)
        {
            switch(severity)
            {
                case ERROR:
                    formatString = getErrorFormat();
                    break;
                case WARNING:
                    formatString = getWarningFormat();
                    break;
                default:
                    assert false;
                    formatString = getErrorFormat();
            }
        }
        else
        {
            switch (classification)
            {
                case SYNTAX_ERROR:
                    formatString = getSyntaxErrorFormat();
                    break;
                case INTERNAL_ERROR:
                    formatString = getInternalErrorFormat();
                    break;     
                default:
                    assert false;
            }
        }
        
        assert formatString != null;
        return formatString;
    }
    
    /**
     * Gets the text of the line specified problem occured or 
     * <code>null</code> if there is no line information. 
     * 
     * @param problem problem to process
     * @return the text of the line or <code>null</code> if there 
     * is no line information
     */
    protected String getLineText(ICompilerProblem problem)
    {
        String filePath = problem.getSourcePath();
        if (filePath == null)
            return null;

        int lineNumber = problem.getLine();
        if (lineNumber < 0)
            return null;

        FileLineInfo fileLineInfo = readers.getUnchecked(filePath);
        return fileLineInfo.getLineText(lineNumber);
    }


    /*
     * Helper method to display a caret marking a single character 
     * within the line of source text.
     */
     private String getLinePointer(final String lineText, int column)
    {
        if (lineText == null || column == -1)
            return "";

            final StringBuilder b = new StringBuilder(column);
            final int len = lineText.length();
            for (int i = 0; i < column; i++)
            {
                if (i < len && lineText.charAt(i) == '\t')
                    b.append('\t');
                else
                    b.append(' ');
            }
            b.append('^');
            return b.toString();
    }

    /*
     * Helper method to display the file and line number.
     * <p>
     * Never returns null.
     */
     private String getLocationString(String filePath, int line, int col)
     {
         if (filePath == null)
             return "";

         String location = filePath;
         if (line != -1)
             location = String.format(getLocationFormat(), location, (line + 1), (col + 1));
         assert location != null;
         return location;
     }
     
     private class FileLineInfo
     {
         FileLineInfo(String fileName)
         {
             this.fileName = fileName;
             this.reader = createReader();

             cachedLines = CacheBuilder.newBuilder()
                 .concurrencyLevel(1)
                 .softValues()
                 .maximumSize(MAX_CACHED_LINES_PER_FILE)
                 .build();
         }

         private LineNumberReader createReader()
         {
             IFileSpecification fileSpec = workspace.getFileSpecification(fileName);
             Reader reader;
             try
             {
                 reader = fileSpec.createReader();
             }
             catch (FileNotFoundException e)
             {
                 reader = new NullReader(0);
             }

             return new LineNumberReader(reader);
         }

         final String fileName;
         LineNumberReader reader;
         final Cache<Integer, String> cachedLines;

         String getLineText(int lineNumber)
         {
             String result = cachedLines.getIfPresent(lineNumber);
             if (result != null)
                 return result;


             if (reader.getLineNumber() > lineNumber)
                 reader = createReader();

             assert reader.getLineNumber() <= lineNumber;
             try
             {
                 while (reader.getLineNumber() < lineNumber)
                 {
                     final String lineText = reader.readLine();
                     if (lineText == null)
                         return null;
                 }

                 result = reader.readLine();
                 if (result == null)
                     return null;

                 cachedLines.put(lineNumber, result);
                 return result;
             }
             catch (IOException e)
             {
                 return null;
             }
         }
     }

     /**
      * @return the string used to format messages categorized as errors. 
      */
     private String getErrorFormat()
     {
        return getMessage(ERROR_FORMAT_ID);
     }

     /**
      * @return the string used to format messages categorized as warnings. 
      */
     private String getWarningFormat()
     {
         return getMessage(WARNING_FORMAT_ID);
     }
     
     /**
      * @return the string used to format messages categorized as syntax errors. 
      */
     private String getSyntaxErrorFormat()
     {
         return getMessage(SYNTAXERROR_FORMAT_ID);
     }

     /**
      * @return the string used to format messages categorized as internal errors. 
      */
     private String getInternalErrorFormat()
     {
         return getMessage(INTERNALERROR_FORMAT_ID);
     }

     /**
      * Format of the line number the problem was found.
      * Example) foo:7
      * The problem was found on line 7 of foo.
      * 
      * @return the string used to format line number of the problem in a file. 
      */
     private String getLocationFormat()
     {
         String format = getMessage(LOCATION_FORMAT_ID);
         if (format != null)
             return format;
         
         return LOCATION_FORMAT_STRING;
     }
}
