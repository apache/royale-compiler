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

package org.apache.flex.compiler.asdoc.flexjs;

import org.apache.flex.compiler.asdoc.IASDocComment;

import antlr.Token;

public class ASDocComment implements IASDocComment
{

    public ASDocComment(Token t)
    {
        token = t;
    }
    
    Token token;
    
    public String commentNoEnd()
    {
        String s = token.getText();
        String[] lines = s.split("\n");
        StringBuilder sb = new StringBuilder();
        sb.append(lines[0]);
        sb.append("\n");
        int n = lines.length;
        for (int i = 1; i < n - 1; i++)
        {
            String line = lines[i];
            int star = line.indexOf("*");
            sb.append(" ");
            if (star > -1)
                sb.append(line.substring(star));
            sb.append("\n");
        }
        return sb.toString();
    }
}
