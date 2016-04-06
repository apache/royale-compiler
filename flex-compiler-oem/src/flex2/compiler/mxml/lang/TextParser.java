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

package flex2.compiler.mxml.lang;

/**
 * MXML text parser, used to parse attribute values and text
 * content. Some utility functionality is also exposed in static
 * methods.
 */
public abstract class TextParser
{
    /**
     * valid percentage expressions are: [whitespace] positive-whole-or-decimal-number [whitespace] % [whitespace]
     */
    //private static final Pattern percentagePattern = Pattern.compile("\\s*((\\d+)(.(\\d)+)?)\\s*%\\s*");

    /**
     * valid qualified names are series of 1 or more leading-alpha-or-_-followed-by-alphanumerics words, separated by dots
     */
    //private static final Pattern qualifiedNamePattern = Pattern.compile("([a-zA-Z_]\\w*)(\\.([a-zA-Z_]\\w*))*");

    /**
     * valid AS RegExps are: / 0-or-more-of-anything / 0-or-more-flag chars. We leave pattern validation to ASC.
     */
    //private static final Pattern regExpPattern = Pattern.compile("/.*/[gimsx]*");

    //  error codes
    public final static int Ok = 0;
    public final static int ErrTypeNotEmbeddable = 1;       //  @Embed in a bad spot
    public final static int ErrInvalidTextForType = 2;      //  can't make text work as a serialized instance of type
    public final static int ErrInvalidPercentage = 3;       //  malformed percentage expression
    public final static int ErrTypeNotSerializable = 4;     //  type doesn't have a text representation at all
    public final static int ErrPercentagesNotAllowed = 5;   //  percentage not allowed here
    public final static int ErrTypeNotContextRootable = 6;  //  @ContextRoot in a bad spot
    public final static int ErrUnrecognizedAtFunction = 7;  //  @huh?()
    public final static int ErrUndefinedContextRoot = 8;    //  context-root not defined
    public final static int ErrInvalidTwoWayBind  = 9;      //  malformed two-way binding expression
    
    //  processing flags
    public final static int FlagInCDATA = 1;
    public final static int FlagCollapseWhiteSpace = 2;
    public final static int FlagConvertColorNames = 4;
    public final static int FlagAllowPercentages = 8;
    public final static int FlagIgnoreBinding = 16;
    public final static int FlagIgnoreAtFunction = 32;
    public final static int FlagIgnoreArraySyntax = 64;
    public final static int FlagIgnoreAtFunctionEscape = 128;
    public final static int FlagRichTextContent = 256;


    /**
     * test if this is a valid identifier, and is not an actionscript keyword.
     */
    public static boolean isValidIdentifier(String id)
    {
        if (id.length() == 0 || !isIdentifierFirstChar(id.charAt(0)))
        {
            return false;
        }

        for (int i=1; i < id.length(); i++)
        {
            if (!isIdentifierChar(id.charAt(i)))
            {
                return false;
            }
        }

        if (StandardDefs.isReservedWord(id))
        {
            return false;
        }

        return true;
    }
    
    /**
     * Used to detect scoped attributes.
     */
    public static boolean isScopedName(String name)
    {
        return name.indexOf('.') != -1;
    }
	
    /**
     * Helper used to decompose a scoped name.
     */
    public static String[] analyzeScopedName(String name)
    {
        String[] results = name.split("\\.");                           
        return (results.length != 2) ? null : results;
    }

    /**
     *
     */
    private static boolean isIdentifierFirstChar(char ch)
    {
        return Character.isJavaIdentifierStart(ch);
    }

    /**
     *
     */
    private static boolean isIdentifierChar(int ch)
    {
        return ch != -1 && Character.isJavaIdentifierPart((char)ch);
    }

    /**
     *
    private static boolean isQualifiedName(String text)
    {
        return qualifiedNamePattern.matcher(text).matches() && !StandardDefs.isReservedWord(text);
    }
     */
}
