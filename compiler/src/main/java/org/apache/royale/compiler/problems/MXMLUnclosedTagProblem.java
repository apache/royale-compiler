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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.problems.annotations.ProblemClassification;

/**
 * Problem generated when an unterminated entity is found.
 */
@ProblemClassification(CompilerProblemClassification.SYNTAX_ERROR)
public final class MXMLUnclosedTagProblem extends ParserProblem
{
    public static final String DESCRIPTION =
        "'${tokenText}' tag (or tag before this tag, or non-tag inside this tag) is unclosed or tag not allowed here.";

    public static final int errorCode = 1552;
    
    public MXMLUnclosedTagProblem(ISourceLocation site, String text)
    {
        super(site);
        tokenText = text;
    }
    
    public MXMLUnclosedTagProblem(ASToken site, String text) 
    {
        super(site);
        tokenText = text;
    }

    public MXMLUnclosedTagProblem(ASToken token) 
    {
        this(token, token.getText());
    }

    public final String tokenText;

}
