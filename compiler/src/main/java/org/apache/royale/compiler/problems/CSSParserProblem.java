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

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.RecognitionException;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;

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
        final String reason = String.format("%s %s", messageHeader, makeNice(messageBody));

        final ISourceLocation location = new SourceLocation(
            e.input.getSourceName(),
            UNKNOWN, UNKNOWN, // TODO Need start and end info from CSS
            e.line, e.charPositionInLine);

        return new CSSParserProblem(location, reason, baseRecognizer.getClass());
    }
    
    private static String makeNice(String s)
    {
    	if (s.contains("mismatched tree node: <mismatched token"))
    	{
    		int c = s.indexOf("expecting");
    		if (c != -1)
    		{
    			String expecting = s.substring(c);
    			expecting = expecting.replace("I_DECL", "CSS property name");
        		c = s.indexOf("resync");
        		if (c != -1)
        		{
        			s = s.substring(0, c);
        		}
        		String token = s.replaceFirst("mismatched tree node: <mismatched token: \\[\\@([^=]+)=([^,]+),<([^>]+)>.*$", "unexpected token $2");
        		s = token + " " + expecting;
    		}
    	}
    	return s;
    }
}
