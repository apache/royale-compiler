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

import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;

/**
 * An MXML blob is a large chunk of MXML data that was passed over during
 * tokenization. A blob, for example could be the contents of an fx:Private tag.
 */
public class MXMLTagBlobData extends MXMLUnitData
{
    /**
     * Constructor.
     * 
     * @param token
     */
    MXMLTagBlobData(MXMLToken token)
    {
        String text = token.getText();
        int length = text.length();
        int pos = 1;
        while (pos < length)
        {
            if (Character.isWhitespace(text.charAt(pos)))
                break;
            pos++;
        }
        name = text.substring(1, pos - 1);
        
        setStart(token.getStart());
        setEnd(token.getEnd());
        setLine(token.getLine());
        setColumn(token.getColumn());
    }

    private String name;
    
    //
    // Other methods
    //

    public String getName()
    {
        return name;
    }
}
