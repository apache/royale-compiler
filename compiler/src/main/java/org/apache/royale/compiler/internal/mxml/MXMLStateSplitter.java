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
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;

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
        // Is there a dot in the name?
        String name = nameToken.getText();
        int i = name.indexOf('.');
        if (i >= 0)
        {
            if (mxmlDialect != null && mxmlDialect.isEqualToOrAfter(MXMLDialect.MXML_2009))
            {
                baseName = name.substring(0, i);
                stateName = name.substring(i + 1);
                stateNameOffset = i + 1;
            }
            else
            {
                stateNameOffset = -1;
                baseName = name;
                stateName = null;

                // TODO: I don't think is going to make the right kind of "problem"
                // This is how the old code worked, but I think it will give a strange message
                if (problems != null && fileSpec != null)
                    problems.add(new SyntaxProblem((MXMLToken)nameToken, "Spark state overrides not supported by current language version"));
            }
        }
        else
        {
            // no dot.
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
