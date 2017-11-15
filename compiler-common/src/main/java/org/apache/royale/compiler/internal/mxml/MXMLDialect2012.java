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

import java.util.EnumSet;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;

/**
 * This singleton class represents the experimental 2012 dialect of MXML,
 * with the language namespace <code>"http://ns.adobe.com/mxml/2012"</code>.
 * <p>
 * Compared with the 2009 dialect, this dialect support no new
 * special language tags, but it slightly changes the whitespace
 * and parsing rules for better consistency with XML and ActionScript.
 */
public class MXMLDialect2012 extends MXMLDialect2009
{
    // The singleton instance of this class.
    private static final MXMLDialect INSTANCE =
        new MXMLDialect2012(IMXMLLanguageConstants.NAMESPACE_MXML_2012, 2012);
    
    /**
     * Gets the singleton instance of this class.
     */
    public static MXMLDialect getInstance()
    {
        return INSTANCE;
    }
    
    // Protected constructor
    protected MXMLDialect2012(String languageNamespace, int year)
    {
        super(languageNamespace, year);
    }
    
    @Override
    public boolean isWhitespace(char c)
    {
        // In XML, and therefore in MXML 2012, only four characters
        // are considered to be whitespace.
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }
    
    @Override
    public String trim(String s)
    {
        // TODO Maybe it would be faster to trim
        // using a Java regular expression?
        
        int n = s.length();
        
        // Determine the index at which non-whitespace starts.
        int i;
        for (i = 0; i < n; i++)
        {
            if (!isWhitespace(s.charAt(i)))
                break;
        }
        
        // Determine index at which non-whitespace ends.
        int j;
        for (j = n - 1; j >= i; j--)
        {
            if (!isWhitespace(s.charAt(j)))
                break;
        }
        
        return i > 0 || j < n - 1 ? s.substring(i, j + 1) : s;
    }
    
    @Override
    public Boolean parseBoolean(ICompilerProject project, String s, EnumSet<TextParsingFlags> flags)
    {
        // Unlike 2006 and 2009, 2012 recognizes only lowercase true and false.
        
        if (s.equals(IASLanguageConstants.TRUE))
            return Boolean.TRUE;
        
        else if (s.equals(IASLanguageConstants.FALSE))
            return Boolean.FALSE;
        
        else
            return null;
    }
}
