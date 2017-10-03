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

import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.problems.annotations.ProblemClassification;
import org.apache.royale.compiler.common.ISourceLocation;

@ProblemClassification(CompilerProblemClassification.SYNTAX_ERROR)
public class SyntaxProblem extends ParserProblem implements ICompilerProblem
{
    public static final String DESCRIPTION =
        "'${tokenText}' is not allowed here";
    
    public static final int errorCode = 1510;
    public SyntaxProblem(ISourceLocation site, String text)
    {
        super(site);
        tokenText = text;
    }
    
    public SyntaxProblem(ASToken site, String text) 
    {
        super(site);
        tokenText = text;
    }
    
    public SyntaxProblem(ASToken token)
    {
        this(token, token.getText());
    }
    
    public final String tokenText;
}
