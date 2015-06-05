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

package org.apache.flex.compiler.asdoc;

import java.io.StringReader;

import org.apache.flex.compiler.asdoc.IASDocComment;
import org.apache.flex.compiler.internal.parsing.as.ASDocToken;
import org.apache.flex.compiler.internal.parsing.as.ASDocTokenizer;
import org.apache.flex.compiler.internal.parsing.as.ASTokenTypes;

import antlr.Token;

public class ASDocComment implements IASDocComment
{

    public ASDocComment(Token t)
    {
        token = t;
        String data = token.getText();
        ASDocTokenizer tokenizer = new ASDocTokenizer(false);
        tokenizer.setReader(new StringReader(data));
        ASDocToken tok = tokenizer.next();
        boolean foundDescription = false;

        try
        {
            while (tok != null)
            {
                if (!foundDescription
                        && tok.getType() == ASTokenTypes.TOKEN_ASDOC_TEXT)
                {
                    System.out.println("ASDOC Text: " + tok.getText() );
                    foundDescription = true;
                }
                else
                {
                    // do tags
                    if (tok.getType() == ASTokenTypes.TOKEN_ASDOC_TAG)
                    {
                        System.out.println("ASDOC Tag: " + tok.getText() );
                    }
                    else if (tok.getType() == ASTokenTypes.TOKEN_ASDOC_TEXT)
                    {
                        System.out.println("ASDOC Description: " + tok.getText() );
                    }
                }

                tok = tokenizer.next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    Token token;
    
    public Token getToken()
    {
        return token;
    }
}
