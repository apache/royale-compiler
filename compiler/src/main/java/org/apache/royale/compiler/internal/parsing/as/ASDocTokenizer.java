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

package org.apache.royale.compiler.internal.parsing.as;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;

public class ASDocTokenizer
{
    private boolean fromMXML = false;

    private List<ICompilerProblem> problems;

    private RawASDocTokenizer tokenizer;

    public ASDocTokenizer(boolean fromMXML)
    {
        super();
        this.fromMXML = fromMXML;
        problems = new ArrayList<ICompilerProblem>(2);

    }

    public void setReader(Reader input)
    {
        tokenizer = new RawASDocTokenizer(input);
        tokenizer.setFromMXML(fromMXML);
    }

    public ASDocToken next()
    {
        ASDocToken token = null;
        boolean needToken = true;
        while (needToken)
        {
            try
            {
                token = tokenizer.nextToken();
                needToken = false;
            }
            catch (Exception e)
            {
                needToken = true;
                problems.add(new InternalCompilerProblem(e));
            }
        }
        if (token == null)
            return null;

        switch (token.getType())
        {
            case ASTokenTypes.TOKEN_ASDOC_TAG:
            case ASTokenTypes.TOKEN_ASDOC_TEXT:
                return token;
        }
        return null;
    }

    public void close() throws IOException
    {
        if (tokenizer != null)
        {
            tokenizer.reset();
            tokenizer.yyclose(); //close the reader
        }
    }
}
