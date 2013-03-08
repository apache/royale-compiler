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

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.flex.compiler.mxml.IMXMLTextData.TextType;
import org.apache.flex.compiler.parsing.IASToken;

/**
 * Represents a databinding expression found within an attribute value
 */
public class MXMLDatabindingValue extends MXMLTagAttributeValue implements IMXMLDatabindingValue
{
    /**
     * Constructor.
     */
    MXMLDatabindingValue(MXMLToken start, ListIterator<MXMLToken> iterator, IMXMLTagAttributeData parent)
    {
        super(parent);
        
        setStart(start.getStart());
        setColumn(start.getColumn());
        setLine(start.getLine());
        
        while (iterator.hasNext())
        {
            MXMLToken token = iterator.next();
            
            setEnd(token.getEnd());
            
            if (token.isASToken())
                tokens.add(token);
            else
                break;
         }
    }

    /**
     * Constructor.
     */
    MXMLDatabindingValue(MXMLToken start, ListIterator<MXMLToken> iterator)
    {
        this(start, iterator, null);
    }

    private ArrayList<IASToken> tokens = new ArrayList<IASToken>(5);
    
    //
    // IMXMLTextData implementations
    //
    
    @Override
    public String getContent()
    {
        StringBuilder builder = new StringBuilder();
        
        final int size = tokens.size();
        IASToken lastToken = null;
        for (int i = 0; i < size; i++)
        {
            IASToken currentToken = tokens.get(i);
            if (lastToken != null)
            {
                int spaces = currentToken.getStart() - lastToken.getEnd();
                for (int s = 0; s < spaces; s++)
                    builder.append(" ");
            }
            builder.append(currentToken.getText());
            lastToken = currentToken;
        }
        
        return builder.toString();
    }

    @Override
    public TextType getTextType()
    {
        return TextType.DATABINDING;
    }

    //
    // IMXMLDatabindingData implementations
    //

    @Override
    public IASToken[] getDatabindingContent()
    {
        return tokens.toArray(new IASToken[0]);
    }
}
