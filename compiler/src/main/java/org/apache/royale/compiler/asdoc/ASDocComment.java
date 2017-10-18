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

package org.apache.royale.compiler.asdoc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.royale.compiler.internal.parsing.as.ASDocToken;
import org.apache.royale.compiler.internal.parsing.as.ASDocTokenizer;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

import antlr.Token;

public class ASDocComment implements IASDocComment
{

    private ASToken token;

    @SuppressWarnings("unused")
    private IDocumentableDefinitionNode node;

    private String description;

    Map<String, List<IASDocTag>> tags = new TreeMap<String, List<IASDocTag>>();

    public ASDocComment(ASToken token, IDocumentableDefinitionNode node)
    {
        this.token = token;
        this.node = node;
    }

    /**
     * @param token The asdoc token.
     */
    public ASDocComment(Token token)
    {
        this.token = (ASToken)token;
    }

    @Override
    public void compile()
    {
        if (token == null)
            return;

        String data = token.getText();
        ASDocTokenizer tokenizer = new ASDocTokenizer(false);
        tokenizer.setReader(new StringReader(data));
        ASDocToken tok = tokenizer.next();
        boolean foundDescription = false;
        ASDocTag pendingTag = null;

        try
        {
            while (tok != null)
            {
                if (!foundDescription && tok.getType() == ASTokenTypes.TOKEN_ASDOC_TEXT)
                {
                    description = tok.getText();
                }
                else
                {
                    // do tags
                    if (tok.getType() == ASTokenTypes.TOKEN_ASDOC_TAG)
                    {
                        if (pendingTag != null)
                        {
                            addTag(pendingTag);
                        }
                        pendingTag = new ASDocTag(tok.getText().substring(1));
                    }
                    else if (tok.getType() == ASTokenTypes.TOKEN_ASDOC_TEXT)
                    {
                        if(pendingTag != null) {
                            pendingTag.setDescription(tok.getText());
                            addTag(pendingTag);
                            pendingTag = null;
                        }
                    }
                }

                foundDescription = true;

                tok = tokenizer.next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addTag(ASDocTag tag)
    {
        List<IASDocTag> list = tags.get(tag.getName());
        if (list == null)
        {
            list = new ArrayList<IASDocTag>();
            tags.put(tag.getName(), list);
        }
        list.add(tag);
    }

    @Override
    public String toString()
    {
        return "ASASDocComment [description=" + description + "]";
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    //--------------------------------------------------------------------------
    // ASDocTag
    //--------------------------------------------------------------------------

    class ASDocTag implements IASDocTag
    {
        private String name;
        private String description;

        public ASDocTag(String name)
        {
            if (name.indexOf('@') == 0)
                name = name.substring(1);
            this.setName(name);
        }

        public ASDocTag(String name, String description)
        {
            this(name);
            setDescription(description);
        }

        @Override
        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        @Override
        public boolean hasDescription()
        {
            return description != null && !description.equals("");
        }
    }

    @Override
    public boolean hasTag(String name)
    {
        List<IASDocTag> list = tags.get(name);
        if (list == null || list.size() == 0)
            return false;
        return true;
    }

    @Override
    public IASDocTag getTag(String name)
    {
        List<IASDocTag> list = tags.get(name);
        if (list == null || list.size() == 0)
            return null;
        return list.get(0);
    }

    @Override
    public Map<String, List<IASDocTag>> getTags()
    {
        // TODO return unmodifiable
        return tags;
    }

    @Override
    public List<IASDocTag> getTagsByName(String name)
    {
        List<IASDocTag> result = new ArrayList<IASDocTag>();
        List<IASDocTag> list = tags.get(name);
        if (list == null)
            return result;

        for (IASDocTag tag : list)
        {
            result.add(tag);
        }
        return result;
    }

    @Override
    public void paste(IASDocComment source)
    {
        // 
        description = source.getDescription();
        IASDocTag tag;
        if (source.hasTag("return"))
        {
            tag = source.getTag("return");
            addTag(new ASDocTag(tag.getName(), tag.getDescription()));
        }
        if (source.hasTag("param"))
        {
            Collection<IASDocTag> tags = source.getTagsByName("param");
            for (IASDocTag ptag : tags)
            {
                addTag(new ASDocTag(ptag.getName(), ptag.getDescription()));
            }
        }
    }

}
