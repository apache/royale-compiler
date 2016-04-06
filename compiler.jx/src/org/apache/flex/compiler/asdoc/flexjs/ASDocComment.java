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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.asdoc.IASDocComment;
import org.apache.flex.compiler.asdoc.IASDocTag;

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
        int n = lines.length;
        if (n == 1)
        {
        	int c = lines[0].indexOf("*/");
        	if (c != -1)
        		lines[0] = lines[0].substring(0, c);
        }
        sb.append(lines[0]);
        sb.append("\n");
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

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public void compile()
    {
    }

    @Override
    public boolean hasTag(String name)
    {
        return false;
    }

    @Override
    public IASDocTag getTag(String name)
    {
        return null;
    }

    @Override
    public Map<String, List<IASDocTag>> getTags()
    {
        return null;
    }

    @Override
    public Collection<IASDocTag> getTagsByName(String string)
    {
        return null;
    }

    @Override
    public void paste(IASDocComment source)
    {
    }

}
