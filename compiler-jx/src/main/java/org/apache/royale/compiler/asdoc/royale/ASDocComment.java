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

package org.apache.royale.compiler.asdoc.royale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.asdoc.IASDocTag;

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

    private String description = null;
    private Map<String, List<IASDocTag>> tagMap;
    
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void compile()
    {
    	compile(true);
    }
    
    public void compile(boolean trimlines)
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
        // clip off asdoc slash-star-star
        String line = lines[0].substring(3);
        if (trimlines)
        	sb.append(line.trim());
        else
        	sb.append(line + "\n");
        boolean inPre = false;
        for (int i = 1; i < n - 1; i++)
        {
            line = lines[i];
            int star = line.indexOf("*");
            int at = line.indexOf("@");
            if (at == -1)
            {
            	if (line.contains("<pre>"))
            		inPre = true;
            	if (line.contains("</pre>"))
            		inPre = false;
	            sb.append(" ");
	            if (star > -1)
	            {
	            	if (trimlines)
	            		sb.append(line.substring(star + 1).trim());
	            	else
	            		sb.append(line.substring(star + 1) + "\n");
	            }
	            if (inPre)
	            	sb.append("\\n");
            }
            else
            {
            	if (tagMap == null)
            		tagMap = new HashMap<String, List<IASDocTag>>();
            	
            	int after = line.indexOf(" ", at + 1);
            	int tabAfter = line.indexOf("\t", at + 1);
            	if (tabAfter != -1 && after != -1 && tabAfter < after)
            		after = tabAfter;
            	if (after == -1)
            	{
            		tagMap.put(line.substring(at + 1), null);
            	}
            	else
            	{
            		String tagName = line.substring(at + 1, after);
            		List<IASDocTag> tags = tagMap.get(tagName);
            		if (tags == null)
            		{
            			tags = new ArrayList<IASDocTag>();
            			tagMap.put(tagName, tags);
            		}
            		tags.add(new ASDocTag(tagName, line.substring(after + 1).trim()));
            	}            		
            }
        }
        if (trimlines)
        	description = sb.toString().trim().replace("\"", "\\\"");
        else
        	description = sb.toString();
    }

    @Override
    public boolean hasTag(String name)
    {
    	if (tagMap == null)	
    		return false;
    	return (tagMap.containsKey(name));
    }

    @Override
    public IASDocTag getTag(String name)
    {
    	if (tagMap == null)	
    		return null;
    	List<IASDocTag> tags = tagMap.get(name);
    	if (tags == null)
    		return null;
        return tags.get(0);
    }

    @Override
    public Map<String, List<IASDocTag>> getTags()
    {
        return tagMap;
    }

    @Override
    public Collection<IASDocTag> getTagsByName(String string)
    {
        return tagMap.get(string);
    }

    @Override
    public void paste(IASDocComment source)
    {
    }

    class ASDocTag implements IASDocTag
    {
    	public ASDocTag(String name, String desc)
    	{
    		this.name = name;
    		this.desc = desc;
    	}

    	private String name;
    	private String desc;
    	
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return name;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return desc;
		}

		@Override
		public boolean hasDescription() {
			// TODO Auto-generated method stub
			return desc != null;
		}
    	
    }
}
