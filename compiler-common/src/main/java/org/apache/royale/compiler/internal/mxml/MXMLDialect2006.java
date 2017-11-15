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
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.ICompilerProjectWithNamedColor;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;

/**
 * This singleton class represents the 2006 dialect of MXML,
 * with the language namespace <code>"http://www.adobe.com/2006/mxml"</code>.
 * <p>
 * The special language tags of this dialect are {@code <Binding>}, 
 * {@code <Component>},  {@code <Metadata>}, {@code <Model>}, 
 * {@code <Script>}, and {@code <Style>}.
 */
public class MXMLDialect2006 extends MXMLDialect
{
    // The singleton instance of this class.
    private static final MXMLDialect INSTANCE =
        new MXMLDialect2006(IMXMLLanguageConstants.NAMESPACE_MXML_2006, 2006);
    
    /**
     * Gets the singleton instance of this class.
     */
    public static MXMLDialect getInstance()
    {
        return INSTANCE;
    }
    
    // Protected constructor
    protected MXMLDialect2006(String languageNamespace, int year)
    {
        super(languageNamespace, year);
        
        bindingXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.BINDING);
        componentXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.COMPONENT);
        metadataXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.METADATA);
        modelXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.MODEL);
        scriptXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.SCRIPT);
        styleXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.STYLE);
    }
    
    @Override
    public boolean isWhitespace(char c)
    {
        // This definition corresponds to the characters
        // that Java's trim() method trims.
        return c <= ' ';
    }
    
    @Override
    public boolean isWhitespace(String s)
    {
        int n = s.length();
        for (int i = 0; i < n ; i++)
        {
            char c = s.charAt(i);
            if (!isWhitespace(c))
                return false;
        }
        return true;
    }
    
    @Override
    public String collapseWhitespace(String s, char replacementChar)
    {
        StringBuilder sb = new StringBuilder();
        
        boolean lastWasSpace = true;
        int n = s.length();
        int i = 0;
        while (i < n)
        {
            char c = s.charAt(i++);
            boolean ws = Character.isWhitespace(c);
            if (ws)
            {
                if (lastWasSpace)
                    ; // consume the character
                else
                    sb.append(replacementChar);
                lastWasSpace = true;
            }
            else
            {
                sb.append(c);
                lastWasSpace = false;
            }
        }
        
        return trim(sb.toString());
    }
    
    @Override
    public String trim(String s)
    {
        return s.trim();
    }
    
    @Override
    public String[] splitAndTrim(String s)
    {
        // first make sure it isn't in array format
        int c = s.indexOf('[');
        if (c != -1)
            s = s.substring(c + 1);
        c = s.indexOf(']');
        if (c != -1)
            s = s.substring(0, c);
        
        //check for quotes
        s = s.replace("'", "");       

        String[] a = s.split(",");
        
        if (a == null)
            return null;
        
        int n = a.length;
        for (int i = 0; i < n; i++)
        {
            a[i] = trim(a[i]);
        }
        
        return a;
    }
    
    @Override
    public Boolean parseBoolean(ICompilerProject project, String s,
                                EnumSet<TextParsingFlags> flags)
    {
        s = trim(s);
        
        s = s.toLowerCase();
        
        if (s.equals(IASLanguageConstants.FALSE))
            return Boolean.FALSE;
        else if (s.equals(IASLanguageConstants.TRUE))
            return Boolean.TRUE;
        
        return null;
    }
    
    @Override
    public Integer parseInt(ICompilerProject project, String s,
                            EnumSet<TextParsingFlags> flags)
    {
        s = trim(s);
        
        // Don't parse ints with leading zeros, which are not octal.
        // For example, a MA zip code, 02127.
        if (hasLeadingZeros(s))
            return null;

        Integer value = null;
        try
        {
            value = Integer.decode(s);
            if (value != null)
                return value;
        }
        catch (NumberFormatException e)
        {
        }
        
        if (flags != null && flags.contains(TextParsingFlags.ALLOW_COLOR_NAME))
        {
            value = ((ICompilerProjectWithNamedColor)project).getNamedColor(s);
            if (value != null)
                return value;
        }
        
        return null;
    }
    
    @Override
    public Long parseUint(ICompilerProject project, String s,
                          EnumSet<TextParsingFlags> flags)
    {
        s = trim(s);
        
        // Don't parse uint's with leading zeros, which are not octal.
        // For example, a MA zip code, 02127.
        if (hasLeadingZeros(s))
            return null;

        Long value = null;
        try
        {
            value = Long.decode(s);
            long longValue = value.longValue();
            // TODO I don't understand the purpose of the following logic,
            // which comes from the old compiler. It seems like it should be
            // enforcing the positivity of the uint, but doesn't appear to do that.
            return (longValue == Math.abs(longValue) && longValue <= 0xFFFFFFFFL) ? value : longValue;
        }
        catch (NumberFormatException e)
        {
        }
        
        if (flags != null && flags.contains(TextParsingFlags.ALLOW_COLOR_NAME))
        {
            Integer colorValue = ((ICompilerProjectWithNamedColor)project).getNamedColor(s);
            if (colorValue != null)
                return colorValue.longValue();
        }
        
        return null;
    }
    
    @Override
    public Number parseNumber(ICompilerProject project, String s,
                              EnumSet<TextParsingFlags> flags)
    {
        // Don't parse Numbers with leading zeros, which are not octal.
        // For example, a MA zip code, 02127.
        if (hasLeadingZeros(s))
            return null;

        Integer value = parseInt(project, s, flags);
        if (value != null)
            return value;

        try
        {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
        }
        
        return null;
    }
    
    @Override
    public String parseString(ICompilerProject project, String s,
                              EnumSet<TextParsingFlags> flags)
    {
        if (flags != null && flags.contains(TextParsingFlags.COLLAPSE_WHITE_SPACE))
            s = collapseWhitespace(s, ' ');
        
        return s;
    }
    
    @Override
    public List<Object> parseArray(ICompilerProject project, String s,
                                   EnumSet<TextParsingFlags> flags)
    {
        if (flags != null && flags.contains(TextParsingFlags.ALLOW_ARRAY))
        {
            String trimmed = trim(s);
    
            if (!isArray(trimmed))
                return null;

            List<Object> list = new ArrayList<Object>();
            
            if (isEmptyArray(trimmed))
                return list;
            
            StringBuilder buffer = new StringBuilder();
            char quoteChar = '\'';
            boolean inQuotes = false;
    
            int n = trimmed.length();
            for (int i = 1; i < n; i++)
            {
                char c = trimmed.charAt(i);
                switch (c)
                {
                    case '[':
                    {
                        if (inQuotes)
                        {
                            buffer.append(c);
                        }
                        else
                        {
                            // The old compiler did not support nested arrays,
                            // and in fact behaves rather strangely when you
                            // write them.
                        }
                        break;
                    }
                    
                    case '"':
                    case '\'':
                    {
                        if (inQuotes)
                        {
                            if (quoteChar == c)
                                inQuotes = false;
                            else
                                buffer.append(c);
                        }
                        else
                        {
                            inQuotes = true;
                            quoteChar = c;
                        }
                        break;
                    }
                    
                    case ',':
                    case ']':
                    {
                        if (inQuotes)
                        {
                            buffer.append(c);
                        }
                        else
                        {
                            String elementText = trim(buffer.toString());
                            buffer = new StringBuilder();
        
                            // NOTE: Clear any special-processing flags, on the interpretation
                            // that they only apply to top-level scalars.
                            // NOTE: The old compiler did not support nested arrays.
                            Object element = parseObject(project, elementText, null);
                            if (element != null)
                                list.add(element);
                            else
                                return null;
                        }
                        break;
                    }
                    
                    default:
                    {
                        buffer.append(c);
                        break;
                    }
                }
            }
            
            return list;
        }
    
        return null;
    }
    
    @Override
    public Object parseObject(ICompilerProject project, String s,
                              EnumSet<TextParsingFlags> flags)
    {
        String trimmed = trim(s);
        
        Object result;
        
        result = parseBoolean(project, trimmed, flags);
        if (result != null)
            return result;
        
        result = parseArray(project, trimmed, flags);
        if (result != null)
            return result;

        result = parseNumber(project, trimmed, flags);
        if (result != null)
            return result;
        
        return s;
    }
    
    //
    // Other methods
    //
    
    private boolean hasLeadingZeros(String s)
    {
        boolean result = false;
        
        int n = s.length();
        
        if (n > 1 && s.charAt(0) == '0' &&
            !(s.startsWith("0x") || s.startsWith("0X") || s.startsWith("0.")))
        {
            result = true;
        }
        
        return result;
    }
    
    protected boolean isArray(String s)
    {
        assert s.equals(trim(s));
        
        int n = s.length();
        return n >= 2 && s.charAt(0) == '[' && s.charAt(n - 1) == ']';
    }

    private boolean isEmptyArray(String s)
    {
        assert s.equals(trim(s));

        boolean result = false;

        if (isArray(s) && s.substring(1, s.length() - 1).trim().length() == 0)
            result = true;

        return result;
    }
}
