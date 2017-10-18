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

import antlr.Token;

import org.apache.royale.compiler.parsing.IASBalancingScanner;
import org.apache.royale.utils.NonLockingStringReader;

/**
 * Scanner that uses the ActionScript partitioner to determine whether {} braces
 * are balanced over a particular span of the document
 */
public class ASBalancingScanner implements IASBalancingScanner
{
    private int countBraces(Reader input) throws IOException
    {
        RawASTokenizer tokenizer = new RawASTokenizer(input);
        Token token;
        int counter = 0;
        do
        {
            token = tokenizer.nextToken();
            if (token != null)
            {
                switch (token.getType())
                {
                    case ASTokenTypes.TOKEN_BLOCK_CLOSE:
                        counter--;
                        break;
                    case ASTokenTypes.TOKEN_BLOCK_OPEN:
                        counter++;
                        break;
                    default:
                        // Ignore other token types.
                        break;
                }
            }
        }
        while (token != null);

        return counter;
    }

    @Override
    public boolean areBracesBalanced(Reader input)
    {
        int braceCount = 0;
        try
        {
            braceCount = countBraces(input);
        }
        catch (IOException e)
        {
            // Ignore
        }
        return (braceCount == 0);
    }

    @Override
    public boolean areBracesBalanced(String range)
    {
        return areBracesBalanced(new NonLockingStringReader(range));
    }

    @Override
    public boolean areBracesBalancedOrOverbalanced(Reader input)
    {
        int braceCount = 0;
        try
        {
            braceCount = countBraces(input);
        }
        catch (IOException e)
        {
            // Ignore
        }
        return (braceCount <= 0);
    }

    @Override
    public boolean areBracesBalancedOrOverbalanced(String range)
    {
        return areBracesBalancedOrOverbalanced(new NonLockingStringReader(range));
    }
}
