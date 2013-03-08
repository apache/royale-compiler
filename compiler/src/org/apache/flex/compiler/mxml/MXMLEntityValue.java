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

package org.apache.flex.compiler.mxml;

import java.util.HashMap;
import java.util.Map;

import org.apache.flex.compiler.mxml.IMXMLTextData.TextType;
import org.apache.flex.compiler.parsing.IMXMLToken;
import org.apache.flex.compiler.parsing.MXMLTokenTypes;

public class MXMLEntityValue extends MXMLTagAttributeValue implements IMXMLEntityValue
{
    private static final Map<String, String> NAMED_ENTITIES;

    static
    {
        NAMED_ENTITIES = new HashMap<String, String>(5);

        NAMED_ENTITIES.put("&amp;", "&");
        NAMED_ENTITIES.put("&apos;", "'");
        NAMED_ENTITIES.put("&gt;", ">");
        NAMED_ENTITIES.put("&lt;", "<");
        NAMED_ENTITIES.put("&quot;", "\"");

        // TODO HTML 4 supports about 250 named characters
        // HTML 5 supports about 2500. How many should MXML support
        // beyond these required 5? What did Xerces/mxmlc support?
    }

    static final String getDecodedContent(String content, int type)
    {
        switch (type)
        {
            case MXMLTokenTypes.TOKEN_ENTITY:
            {
                return NAMED_ENTITIES.get(content);
            }
            case MXMLTokenTypes.TOKEN_HEX_ENTITY:
            {
                //thanks to the lexer, we are always: &#x00000;
                //strip off the first 3 chars and the trailing semicolon
                return String.valueOf(Integer.parseInt(content.substring(3, content.length() - 1), 16));
            }
            case MXMLTokenTypes.TOKEN_DECIMAL_ENTITY:
            {
                //thanks to the lexer, we are always: &#\21267;
                //strip off the first 3 chars and the trailing semicolon
                return String.valueOf(Integer.parseInt(content.substring(3, content.length() - 1)));
            }
        }
        
        return null;
    }

    /**
     * Constructor.
     */
    MXMLEntityValue(IMXMLToken token, IMXMLTagAttributeData parent)
    {
        super(parent);

        content = token.getText();
        type = token.getType();

        setStart(token.getStart());
        setEnd(token.getEnd());
        setColumn(token.getColumn());
        setLine(token.getLine());
    }

    private final String content;

    /**
     * Token type which tells us what kind of entity we are dealing with
     */
    private final int type;

    //
    // Object overrides
    //

    // For debugging only. This format is nice in the Eclipse debugger.
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("|");
        sb.append(getContent());
        sb.append("| ");
        sb.append(getLine());
        sb.append(" ");
        sb.append(getColumn());

        return sb.toString();
    }

    //
    // IMXMLTagAttributeValue implementations
    //

    @Override
    public String getContent()
    {
        return content;
    }

    @Override
    public TextType getTextType()
    {
        return TextType.ENTITY;
    }

    //
    // IMXMLEntityValue implementations
    //

    @Override
    public String getDecodedContent()
    {
        return getDecodedContent(content, type);
    }
}
