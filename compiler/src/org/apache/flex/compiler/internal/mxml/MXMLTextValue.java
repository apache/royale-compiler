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

package org.apache.flex.compiler.internal.mxml;

import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.flex.compiler.mxml.IMXMLTagAttributeData;
import org.apache.flex.compiler.mxml.IMXMLTextValue;
import org.apache.flex.compiler.mxml.IMXMLTextData.TextType;

public class MXMLTextValue extends MXMLTagAttributeValue implements
        IMXMLTextValue
{
    /**
     * Constructor.
     */
    MXMLTextValue(MXMLToken textToken, IMXMLTagAttributeData parent)
    {
        super(parent);
        setStart(textToken.getStart());
        setEnd(textToken.getEnd());
        setColumn(textToken.getColumn());
        setLine(textToken.getLine());
        text = textToken.getText();
    }

    private String text;

    //
    // Object overrides
    //

    /**
     * For debugging only. This format is nice in the Eclipse debugger.
     */
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
    // IMXMLTextData implementations
    //

    @Override
    public String getContent()
    {
        return text;
    }

    @Override
    public TextType getTextType()
    {
        return TextType.TEXT;
    }
}
