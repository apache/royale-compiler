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

package org.apache.royale.compiler.internal.resourcebundles;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.tree.as.ClassReferenceNode;
import org.apache.royale.compiler.internal.tree.as.EmbedNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.tree.properties.ResourceBundleEntryNode;
import org.apache.royale.compiler.internal.tree.properties.ResourceBundleFileNode;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.ParserProblem;
import org.apache.royale.compiler.problems.ResourceBundleMalformedEncodingProblem;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Properties parser that reads a properties file in Unicode. 
 */
public class PropertiesFileParser
{
    public static final Pattern CLASS_REFERENCE_REGEX = Pattern.compile("ClassReference\\((.*)\\)");
    public static final Pattern EMBED_REGEX = Pattern.compile(IMetaAttributeConstants.ATTRIBUTE_EMBED + "\\(.*\\)");
    
    /**
     * Characters we skip in certain places
     */
    private static final String WHITESPACE = " \t\n\r\f";

    /**
     * Characters that terminate a key
     */
    private static String SPLITTERS = "=: \t";

    /**
     * Characters that terminate a value
     */
    private static final String TERMINATORS = "\n\r\f";
    
    /**
     * Path of the file that is parsed.
     */
    private String filePath;
    
    /**
     * File node for the properties file being parsed
     */
    private ResourceBundleFileNode fileNode;
    
    /**
     * Collection that is used to store problems that occur during parsing.
     */
    private Collection<ICompilerProblem> problems;
    
    private IWorkspace workspace;

    /**
     * Constructor
     */
    public PropertiesFileParser(IWorkspace workspace) {
        this.workspace = workspace;
    }

    /**
     * This method attempts to parse a .properties file
     * using the same rules as Java, except that the file
     * is assumed to have UTF-8 encoding.
     * 
     * Let <ow> indicates optional whitespace and <rw> required whitespace.
     * 
     * Comment lines have the form <ow>#<comment> or <ow>!<comment>
     * If # or ! isn't the first non-whitespace character on a line,
     * it doesn't start a comment.
     * 
     * Key/value pairs have the form <ow>key<ow>=<ow>value
     * or <ow>key<ow>:<ow>value or <ow>key<rw>value
     * In other words, you can use an equal sign, a colon,
     * or just whitespace to separate the key from the value.
     * 
     * Trailing whitespace is not stripped from the value.
     * 
     * You can use standard escape sequences
     * like \n, \r, \t, \u1234, and \\.
     * 
     * Backslash-space is an escape sequence for a space;
     * for example, if a value needs to start with a space
     * you must write it as backslash-space or it will be
     * interpreted as optional whitespace preceding the value.
     * However, you don't need to escape spaces within a value.
     *   
     * You can continue a line by ending it with a backslash.
     * Leading whitespace on the next line is stripped.
     *      
     * Backslashes that aren't part of an escape sequence are removed.
     * For example, \A is just A.
     *      
     * You don't need to escape a double-quote or a single-quote
     * (but it doesn't hurt to do so).
     * 
     * @param filePath path of the properties file to parse.
     * @param locale locale of the file if it is locale dependent, otherweise <code>null</code>
     * @param reader reader that wraps an open stream to the file to parse.
     * @param problems collection that is used to store problems that occur
     * during parsing
     */
    public ResourceBundleFileNode parse(String filePath, String locale, 
            Reader reader, Collection<ICompilerProblem> problems) {
        this.filePath = filePath;
        this.problems = problems;

        try
        {
            fileNode = new ResourceBundleFileNode(workspace, filePath, locale);
            
            parse(new BufferedReader(reader));
                        
            return fileNode;
        }
        catch(FileNotFoundException ex) 
        {
            ICompilerProblem problem = new FileNotFoundProblem(filePath);
            problems.add(problem);
        }
        catch (IOException ex)
        {
            ICompilerProblem problem = new InternalCompilerProblem2(filePath, ex, "PropertiesFileParser");
            problems.add(problem);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
        
        return null;
    }

    /**
     * Parses the properties file.
     * 
     * @param br buffered reader to read the properties file.
     * @throws IOException
     */
    private void parse(BufferedReader br) throws IOException
    {
        String line;
        StringBuilder buffer = new StringBuilder(100);
        int lineNumber = 0;
        int comment_length=0;
        String sep = System.getProperty("line.separator");
        int sep_len = sep.length();
        int offset = 0;

        while((line=br.readLine())!=null) {
            lineNumber++;

            int len = line.length();
            offset+=len;
            int start=0;

            // TODO: Clean this up some day by using: 
            // http://commons.apache.org/io/api-release/org/apache/commons/io/input/BOMInputStream.html
            // skip the Unicode BOM; UTF-8 is indicated by the byte sequence
            // EF BB BF, which is the UTF-8 encoding of the character U+FEFF)
            if (lineNumber == 1 && len > 0 && line.charAt(0) == '\uFEFF') {
                line = line.substring(1);
                len = line.length();
                offset = len;
            }

            // find first non-whitespace char
            for(;start<len && WHITESPACE.indexOf(line.charAt(start))!=-1;start++);

            if (line.trim().length() == 0) {
                buffer.append(sep);
                comment_length+=sep_len;
                continue;
            }

            // if lines starts with !, # or only contains whitespace
            // add it to the buffer and start over with a new line
            if(len==0 || line.charAt(start)=='!' || line.charAt(start)=='#' ||
                    WHITESPACE.indexOf(line.charAt(start))!=-1) {
                buffer.append(line);
                buffer.append(sep);
                comment_length+=len+sep_len;
                continue;
            }

            // done with comment save it
            if(comment_length!=0) {
                buffer.setLength(comment_length);
            }

            buffer.setLength(0);

            // put start of name=value piece into beginning of buffer
            buffer.append(line.substring(start));
            offset+=start;

            // a line ending with a backslash is continued onto the following line
            while(line != null && line.length() > 1 && line.charAt(line.length()-1)=='\\') {
                buffer.setLength(buffer.length()-1); // remove the backslash
                line=br.readLine();
                if(line!=null) {
                    int new_start = 0;
                    len = line.length();
                    // find first non-whitespace char

                    for(;new_start < len &&
                    WHITESPACE.indexOf(line.charAt(new_start))!=-1;
                    new_start++);

                    // add to buffer
                    buffer.append(line.substring(new_start));
                }
            }

            String propLine = buffer.toString();
            String com_key = loadProperty(propLine, lineNumber, offset, start);

            if(comment_length!=0 && com_key != null) {
                comment_length=0;
            }

            buffer.setLength(0);
        }
    }

    /**
     * Parses a line in a property file.
     * 
     * @param prop
     * @param lineNumber
     * @return
     */
    private String loadProperty(String property, int lineNumber, int startOffset, int column)
    {
        String key;
        String value;
        int prop_len=property.length();
        int prop_index=0;

        // key
        for(; prop_index<prop_len; prop_index++) {
            char current = property.charAt(prop_index);
            if(current == '\\')
                prop_index++;
            else if(SPLITTERS.indexOf(current) != -1)
                break;
        }

        key = property.substring(0, prop_index);
        key = unescape(key, startOffset, column, lineNumber, property);
        key = key.trim();

        // got key now go to first non-whitespace
        for(; prop_index<property.length() &&
        WHITESPACE.indexOf(property.charAt(prop_index))!=-1;
        prop_index++);

        try {
            // also skip : or =
            if(property.charAt(prop_index)==':' || property.charAt(prop_index)=='=') {
                prop_index++;
                // skip any more whitespace
                for(; prop_index<property.length() &&
                WHITESPACE.indexOf(property.charAt(prop_index))!=-1;
                prop_index++);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            return null;
        }

        int value_start=prop_index;

        // read value
        for(;prop_index<property.length(); prop_index++) {
            char current = property.charAt(prop_index);
            if(current == '\\')
                prop_index++;
            else if(TERMINATORS.indexOf(current) != -1)
                break;
        }

        value = property.substring(value_start,prop_index);
        value = unescape(value, startOffset+value_start, value_start, lineNumber, property);
        
        SourceLocation keyLocation = new SourceLocation(filePath, startOffset, 
                startOffset+key.length(), lineNumber, column);
        SourceLocation valueLocation = new SourceLocation(filePath, startOffset+value_start, 
                startOffset+value_start+value.length(), lineNumber, value_start);
        
        process(key, value, keyLocation, valueLocation, problems);

        return key;
    }
    
    /**
     * Do opposite of escape on a given string.
     * 
     * @param string string that contains characters we want to un-escape
     * @return un-escaped string
     */
    private String unescape(String string, int start, int column, int line, String lineText)
    {
        if(string==null)
            return null;

        StringBuilder buffer = new StringBuilder(string.length());
        int string_index=0;

        while(string_index < string.length()) {
            char add = string.charAt(string_index++);
            if(add == '\\') {
                add = string.charAt(string_index++);
                // handle unicode chars, else escaped single chars
                if(add == 'u') {
                    // Read the xxxx
                    int unicode=0;
                    for (int i=0; i<4; i++) {
                        add = string.charAt(string_index++);
                        switch (add) {
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                                unicode = (unicode << 4) + add - '0';
                                break;

                            case 'a': case 'b': case 'c':
                            case 'd': case 'e': case 'f':
                                unicode = (unicode << 4) + 10 + add - 'a';
                                break;

                            case 'A': case 'B': case 'C':
                            case 'D': case 'E': case 'F':
                                unicode = (unicode << 4) + 10 + add - 'A';
                                break;

                            default:
                            {
                                ISourceLocation location = new SourceLocation(filePath,
                                    start, start + string.length(), line, column);
                                ICompilerProblem problem = new ResourceBundleMalformedEncodingProblem(location, string);
                                problems.add(problem);
                            }
                        }
                    }
                    add = (char) unicode;
                } else {
                    // add escaped char to value
                    switch(add) {
                        case 't':
                            add = '\t';
                            break;
                        case 'n':
                            add = '\n';
                            break;
                        case 'r':
                            add = '\r';
                            break;
                        case 'f':
                            add = '\f';
                            break;
                    }
                }
                buffer.append(add);
            } else
                buffer.append(add);
        }
        return buffer.toString();
    }
    
    /**
     * Process a key-value pair extracted from a properties file while parsing.
     * 
     * @param key key of the property
     * @param value value of the property
     * @param keySource location information of key
     * @param valueSource location information of value
     * @param problems collection storing problems that occur during parsing
     */
    private void process(String key, String value, SourceLocation keySource, 
            SourceLocation valueSource, Collection<ICompilerProblem> problems)
    {
        LiteralNode keyNode = new LiteralNode(LiteralType.STRING, key, keySource);
        
        ExpressionNodeBase valueNode = null; 
        Matcher matcher;
        if ((matcher = CLASS_REFERENCE_REGEX.matcher(value)).matches())
        {
            valueNode = processClassReference(matcher, valueSource, problems);
        }
        else if ((matcher = EMBED_REGEX.matcher(value)).matches())
        {
            valueNode = processEmbed(value, valueSource, problems);
        }
        else
        {
            valueNode = new LiteralNode(LiteralType.STRING, value, valueSource);
        }
        
        if(valueNode != null)
            fileNode.addItem(new ResourceBundleEntryNode(keyNode, valueNode));  
    }

    /**
     * Process a ClassReference directive that occurs in a properties file.
     * 
     * @param matcher matcher that has already identified a ClassReference directive in a value.
     * @param sourceLocation location where this directive occurred in the file
     * @param problems collection to add problems if encountered during
     * processing.
     * @return a {@link ClassReferenceNode} instance that represents this
     * occurrence or <code>null</code> if any problem occurs.
     */
    private ClassReferenceNode processClassReference(Matcher matcher, SourceLocation sourceLocation,
            Collection<ICompilerProblem> problems)
    {        
        try
        {
            String qName = matcher.group(1).trim();
            if (qName.equals("null"))
            {
                return new ClassReferenceNode(null, sourceLocation);
            }

            if ((qName.charAt(0) == '"') && (qName.indexOf('"', 1) == qName.length() - 1))
            {
                qName = qName.substring(1, qName.length() - 1);
                return new ClassReferenceNode(qName, sourceLocation);
            }
        } 
        catch(Exception e)
        {
            //do nothing, problem will be reported next.
        }
               
        ParserProblem problem = new ParserProblem(sourceLocation);
        problems.add(problem);
            
        return null;
    }
    
    /**
     * Process a Embed directive that occurs in a properties file.
     * 
     * @param value Embed directive to process
     * @param sourceLocation location where this directive occurred in the file
     * @param problems collection to add problems if encountered during
     * processing.
     * @return a {@link EmbedNode} instance that represents this
     * occurrence or <code>null</code> if any problem occurs.
     */
    private EmbedNode processEmbed(String value, SourceLocation sourceLocation,
                                   Collection<ICompilerProblem> problems)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(value);
        sb.append("]");
        
        MetaTagsNode metaTagsNode =  ASParser.parseMetadata(
            workspace, sb.toString(), sourceLocation.getSourcePath(), sourceLocation.getAbsoluteStart(), 
            sourceLocation.getLine(), sourceLocation.getColumn(), problems);
        
        if (metaTagsNode == null)
            return null;

        IMetaTagNode embedMetaTagNode = metaTagsNode.getTagByName(IMetaAttributeConstants.ATTRIBUTE_EMBED);
        if (embedMetaTagNode == null)
            return null;
        
        EmbedNode embedNode = new EmbedNode(filePath, embedMetaTagNode, fileNode);
        embedNode.setSourceLocation(sourceLocation);
        
        return embedNode;
    }
 
}
