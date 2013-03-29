/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.apache.flex.compiler.mxml;

import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;

public class MXMLEntityData extends MXMLTextData implements IMXMLEntityData
{
    /**
     * Constructor.
     */
    MXMLEntityData(MXMLToken textToken)
    {
        super(textToken);

        type = textToken.getType();
    }

    private int type;

    //
    // Object overrides
    //

    // This method is only used for debugging.
    @Override
    public String toString()
    {
        String s = getCompilableText();

        s = s.replaceAll("\n", "\\\\n");
        s = s.replaceAll("\r", "\\\\r");
        s = s.replaceAll("\t", "\\\\t");

        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" \"");
        sb.append(s);
        sb.append("\"");
        return sb.toString();
    }

    //
    // MXMLTextData overrides
    // 

    @Override
    public String getCompilableText()
    {
        return getDecodedContent();
    }

    //
    // IMXMLEntityData implementations
    //

    @Override
    public String getDecodedContent()
    {
        return MXMLEntityValue.getDecodedContent(getContents(), type);
    }
}
