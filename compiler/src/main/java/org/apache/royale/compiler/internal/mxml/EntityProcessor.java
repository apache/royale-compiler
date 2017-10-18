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

package org.apache.royale.compiler.internal.mxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLInvalidEntityProblem;
import org.apache.royale.compiler.problems.MXMLUnterminatedEntityProblem;

/**
 * {@code EntityProcessor} is an all-static utility class used in parsing MXML.
 * It handles replacing character entity references
 * (such as <code>&lt;</code>, <code>&#8482;</code>, and <code>&#x2122;</code>)
 * in XML text, and in attribute values, with the actual Unicode characters
 * that they represent.
 * <p>
 * The only named character entities that are supported are
 * <code>amp</code>, <code>apos</code>, <code>gt</code>, <code>lt</code>,
 * and <code>quot</code>.
 * <p>
 * Non-character entity references are not supported.
 * <p>
 * An {@code EntityProblem} is reported for each entity that cannot be replaced.
 * If any problems are reported, the output text is <code>null</code>.
 */
// TODO We probably want to do something smarter than call
// EntityProcessor.replaceEntities() on every MXML text unit.
// Maybe the lexer/parser should mark text units that contain entities.
// Maybe it should make every entity a separate text unit.
// We also have to deal with the issue of reporting offsets after an entity.
public class EntityProcessor
{
    private static final char AMPERSAND = '&';
    
    private static final char SEMICOLON = ';';
    
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("#(\\d+)");
        
    private static final Pattern HEX_PATTERN = Pattern.compile("#x([A-Fa-f\\d]+)");
    
    /**
     * Replaces all character entity references in a string.
     * <p>
     * @param s The input string.
     * @param problems A collection of problems, to which {@code EntityProblem}s
     * are added if the entities in the input string are not recognized.
     * @return The output string, or <code>null</code> if there were entity problems.
     */
    public static String parseAsString(String s, ISourceLocation location,
                                          MXMLDialect mxmlDialect,
                                          Collection<ICompilerProblem> problems)
    {
        StringBuilder sb = new StringBuilder();
        
        // If the input string doesn't contain an ampersand character,
        // then there are no entities to replace; just return a single
        // fragment corresponding to the input string.
        int ampersandIndex = s.indexOf(AMPERSAND);
        if (ampersandIndex == -1)
        {
            return s;
        }
        else
        {
            // This variable will keep track of where we find the semicolon
            // that ends the entity. By initializing it to the position
            // before the beginning of the string, we can avoid having
            // special logic to pick up the part of the string before
            // the first entity; it becomes the same logic as picking up
            // the part of the string between the first semicolon and the
            // second ampersand.
            int semicolonIndex = -1;
            
            while (true)
            {
                // We've found an ampersand.
                // Build a fragment containing the text from the previous
                // semicolon (or the beginning) to the this ampersand.
                if (ampersandIndex > semicolonIndex + 1)
                {
                    String text = s.substring(semicolonIndex + 1, ampersandIndex);
                    sb.append(text);
                }

                // Since we found an ampersand that starts an entity,
                // look for a subsequent semicolon that ends it.
                // If it doesn't exist, report a problem.
                semicolonIndex = s.indexOf(SEMICOLON, ampersandIndex + 1);
                if (semicolonIndex == -1)
                {
                    ICompilerProblem problem = new MXMLUnterminatedEntityProblem(location);
                    problems.add(problem);
                    break; // we can't do any further processing
                }
    
                // Extract and convert the entity between the ampersand and the semicolon.
                String physicalText = s.substring(ampersandIndex, semicolonIndex + 1);
                String entityName = s.substring(ampersandIndex + 1, semicolonIndex);
                int c = convertEntity(entityName, mxmlDialect);
                if (c == -1)
                {
                    // If it doesn't convert to a character, create a problem and return null.
                    ICompilerProblem problem = new MXMLInvalidEntityProblem(location, physicalText);
                    problems.add(problem);
                }
                else
                {
                    // If it does convert, add a fragment for the entity.
                    String logicalText = String.copyValueOf(new char[] { (char)c });
                    sb.append(logicalText);
                }
                
                // Find the next ampersand after the semicolon.
                ampersandIndex = s.indexOf(AMPERSAND, semicolonIndex + 1);
                
                // If there isn't one, we're done.
                // Add a final fragment for the text after the last semicolon.
                if (ampersandIndex == -1)
                {
                    if (semicolonIndex + 1 < s.length())
                    {
                        String text = s.substring(semicolonIndex + 1);
                        sb.append(text);
                    }
                    break;
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * Replaces all character entity references in a string.
     * <p>
     * @param s The input string.
     * @param problems A collection of problems, to which {@code EntityProblem}s
     * are added if the entities in the input string are not recognized.
     * @return The output string, or <code>null</code> if there were entity problems.
     */
    public static ISourceFragment[] parse(String s, ISourceLocation location,
                                          MXMLDialect mxmlDialect,
                                          Collection<ICompilerProblem> problems)
    {
        List<ISourceFragment> fragmentList = new ArrayList<ISourceFragment>();
        
        ISourceFragment fragment;
        
        // If the input string doesn't contain an ampersand character,
        // then there are no entities to replace; just return a single
        // fragment corresponding to the input string.
        int ampersandIndex = s.indexOf(AMPERSAND);
        if (ampersandIndex == -1)
        {
            if (s.length() > 0)
            {
                fragment = new SourceFragment(s, location);
                fragmentList.add(fragment);
            }
        }
        else
        {
            // This variable will keep track of where we find the semicolon
            // that ends the entity. By initializing it to the position
            // before the beginning of the string, we can avoid having
            // special logic to pick up the part of the string before
            // the first entity; it becomes the same logic as picking up
            // the part of the string between the first semicolon and the
            // second ampersand.
            int semicolonIndex = -1;
            
            int start = location.getStart();
            int line = location.getLine();
            int column = location.getColumn();
            
            while (true)
            {
                // We've found an ampersand.
                // Build a fragment containing the text from the previous
                // semicolon (or the beginning) to the this ampersand.
                if (ampersandIndex > semicolonIndex + 1)
                {
                    String text = s.substring(semicolonIndex + 1, ampersandIndex);
                    fragment = new SourceFragment(text, text,
                        start + semicolonIndex + 1, line, column + semicolonIndex + 1);
                    fragmentList.add(fragment);
                }

                // Since we found an ampersand that starts an entity,
                // look for a subsequent semicolon that ends it.
                // If it doesn't exist, report a problem.
                semicolonIndex = s.indexOf(SEMICOLON, ampersandIndex + 1);
                if (semicolonIndex == -1)
                {
                    ICompilerProblem problem = new MXMLUnterminatedEntityProblem(location);
                    problems.add(problem);
                    break; // we can't do any further processing
                }
    
                // Extract and convert the entity between the ampersand and the semicolon.
                String physicalText = s.substring(ampersandIndex, semicolonIndex + 1);
                String entityName = s.substring(ampersandIndex + 1, semicolonIndex);
                int c = convertEntity(entityName, mxmlDialect);
                if (c == -1)
                {
                    // If it doesn't convert to a character, create a problem and return null.
                    ICompilerProblem problem = new MXMLInvalidEntityProblem(location, physicalText);
                    problems.add(problem);
                }
                else
                {
                    // If it does convert, add a fragment for the entity.
                    String logicalText = String.copyValueOf(new char[] { (char)c });
                    fragment = new SourceFragment(physicalText, logicalText,
                        start + ampersandIndex, line, column + ampersandIndex);
                    fragmentList.add(fragment);
                }
                
                // Find the next ampersand after the semicolon.
                ampersandIndex = s.indexOf(AMPERSAND, semicolonIndex + 1);
                
                // If there isn't one, we're done.
                // Add a final fragment for the text after the last semicolon.
                if (ampersandIndex == -1)
                {
                    if (semicolonIndex + 1 < s.length())
                    {
                        String text = s.substring(semicolonIndex + 1);
                        fragment = new SourceFragment(text, text,
                            start + semicolonIndex + 1, line, column + semicolonIndex + 1);
                        fragmentList.add(fragment);
                    }
                    break;
                }
            }
        }
        
        return fragmentList.toArray(new ISourceFragment[0]);
    }
    
    private static int convertEntity(String entityName, MXMLDialect mxmlDialect)
    {
        Character ch = mxmlDialect.getNamedEntity(entityName);
        if (ch != null)
            return ch.charValue();
        
        // TODO What happens with Unicode characters
        // outside the BMP, such as &#x12345; ?
        
        Matcher m = HEX_PATTERN.matcher(entityName);
        if (m.matches())
            return Integer.parseInt(m.group(1), 16);        
        
        m = DECIMAL_PATTERN.matcher(entityName);
        if (m.matches())
            return Integer.parseInt(m.group(1));
        
        return -1;
    }
}
