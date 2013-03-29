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

import org.apache.flex.compiler.parsing.IMXMLToken;

public class MXMLInstructionData extends MXMLUnitData implements
        IMXMLInstructionData
{
    /**
     * Constructor.
     */
    MXMLInstructionData(IMXMLToken token)
    {
        instructionText = token.getText();

        setOffsets(token.getStart(), token.getEnd());
        setColumn(token.getColumn());
        setLine(token.getLine());
    }

    private String instructionText;

    //
    // Object overrides
    //

    // For debugging only. This format is nice in the Eclipse debugger.
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append('|');
        sb.append(getInstructionText());
        sb.append('|');

        sb.append(' ');

        // Display line, column, start, and end as "17:5 160-188".
        sb.append(super.toString());

        return sb.toString();
    }

    //
    // MXMLUnitData overrides
    //

    // For debugging only. This format is nice in a text file.
    @Override
    public String toDumpString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toDumpString());

        sb.append('\t');

        sb.append('"');
        sb.append(getInstructionText());
        sb.append('"');

        return sb.toString();
    }

    //
    // IMXMLInstructionData implementations
    //

    /**
     * Returns the raw processing instruction. It is up to clients to parse
     * this.
     */
    public String getInstructionText()
    {
        return instructionText;
    }
}
