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

package org.apache.flex.compiler.problems;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.RecognitionException;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.common.SourceLocation;

/**
 * CSS parser problem.
 */
public final class CSSParserProblem extends CompilerProblem
{
    public static String DESCRIPTION =
        "${reason}";
    
    public static final int errorCode = 1324;

    private CSSParserProblem(ISourceLocation site, String reason,
                             Class<? extends BaseRecognizer> type)
    {
        super(site);
        this.reason = reason;
        this.type = type;
    }

    public final String reason;

    public final Class<? extends BaseRecognizer> type;
    
    /**
     * Create a {@code CSSParserProblem} from ANTLR's
     * {@link BaseRecognizer#displayRecognitionError}.
     * 
     * @param baseRecognizer Lexer, parser or tree walker.
     * @param tokenNames token names
     * @param e exception
     * @return {@code CSSParserProblem}
     */
    public static CSSParserProblem create(BaseRecognizer baseRecognizer,
                                          String[] tokenNames,
                                          RecognitionException e)
    {
        final String messageHeader = baseRecognizer.getErrorHeader(e);
        final String messageBody = baseRecognizer.getErrorMessage(e, tokenNames);
        final String reason = String.format("%s %s", messageHeader, messageBody);

        final ISourceLocation location = new SourceLocation(
            e.input.getSourceName(),
            UNKNOWN, UNKNOWN, // TODO Need start and end info from CSS
            e.line, e.charPositionInLine);

        return new CSSParserProblem(location, reason, baseRecognizer.getClass());
    }
}
