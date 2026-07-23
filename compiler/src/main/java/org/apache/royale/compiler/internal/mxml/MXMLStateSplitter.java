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

import java.util.Collection;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLStateSyntaxNotAllowedProblem;

/**
 * Simple utility for parsing attribute.state phrases.
 */
public class MXMLStateSplitter
{
    /**
     * Constructor.
     */
    public MXMLStateSplitter(IMXMLToken nameToken, MXMLDialect mxmlDialect, Collection<ICompilerProblem> problems, IFileSpecification fileSpec)
    {
        String name = nameToken.getText();
        
        // Is there a dot in the name?
        int dotIndex = name.lastIndexOf('.');
        boolean dotIsStateSyntax = false;
        if (dotIndex != -1)
        {
            // a namespace prefix is allowed to include the "." character
            switch (nameToken.getType())
            {
                case MXMLTokenTypes.TOKEN_NAME:
                    dotIsStateSyntax = !name.startsWith("xmlns:");
                    break;
                case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
                case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
                    int nsIndex = name.indexOf(":");
                    dotIsStateSyntax = nsIndex < dotIndex;
                    break;
                default:
                    dotIsStateSyntax = true;
            }
        }
        if (dotIsStateSyntax)
        {
            if (mxmlDialect != null && mxmlDialect.isEqualToOrAfter(MXMLDialect.MXML_2009))
            {
                baseName = name.substring(0, dotIndex);
                stateName = name.substring(dotIndex + 1);
                stateNameOffset = dotIndex + 1;
            }
            else
            {
                stateNameOffset = -1;
                baseName = name;
                stateName = null;

                if (problems != null && fileSpec != null)
                {
                    problems.add(new MXMLStateSyntaxNotAllowedProblem(nameToken, name));
                }
            }
        }
        else
        {
            // no dot, or a dot that is allowed in the namespace prefix
            baseName = name;
            stateNameOffset = -1;
            stateName = null;
        }
    }

    private final String baseName;

    private final String stateName;

    private final int stateNameOffset;
    
    /**
     * Gets the part of the name before the dot, or the whole name if no dot.
     */
    public String getBaseName()
    {
        return baseName;
    }
    
    /**
     * Gets the part of the name after the first dot, or null if no dot.
     */
    public String getStateName()
    {
        return stateName;
    }
    
    /**
     * Gets the offset of the state name, where zero is the first character in the name.
     */
    public int getStateNameOffset()
    {
        return stateNameOffset;
    }
}
