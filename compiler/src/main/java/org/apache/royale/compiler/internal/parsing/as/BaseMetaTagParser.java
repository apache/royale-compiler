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
import java.util.List;

import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.asdoc.IMetadataParserASDocDelegate;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.metadata.BaseDefinitionMetaTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.parsing.GenericTokenStream;
import org.apache.royale.compiler.parsing.IMetadataParser;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

public class BaseMetaTagParser extends LLkParser implements IMetadataParser
{
    protected IMetadataParserASDocDelegate asDocDelegate;

    public BaseMetaTagParser(int arg0)
    {
        super(arg0);
        asDocDelegate = NilASDocDelegate.get().getASParserASDocDelegate().getMetadataParserASDocDelegate();
    }

    public BaseMetaTagParser(ParserSharedInputState arg0, int arg1)
    {
        super(arg0, arg1);
        asDocDelegate = NilASDocDelegate.get().getASParserASDocDelegate().getMetadataParserASDocDelegate();
    }

    public BaseMetaTagParser(TokenBuffer arg0, int arg1)
    {
        super(arg0, arg1);
        asDocDelegate = NilASDocDelegate.get().getASParserASDocDelegate().getMetadataParserASDocDelegate();
    }

    public BaseMetaTagParser(TokenStream arg0, int arg1)
    {
        super(arg0, arg1);
        asDocDelegate = NilASDocDelegate.get().getASParserASDocDelegate().getMetadataParserASDocDelegate();
    }

    protected void handleComment(Token docToken)
    {
        asDocDelegate.setCurrentASDocToken(docToken);
    }

    protected void resetComments(String name)
    {
        asDocDelegate.clearMetadataComment(name);
    }

    protected void afterTag(MetaTagNode tag, Token openBrace, Token closeBrace, Token closeParen) throws TokenStreamException
    {
        if (tag != null && openBrace != null)
        {
            // ']' is the best match for 'end of a metadata'.
            // ')' is the optional choice.
            // 'LT(0)' is the last seen token, which is the fall back for terminating the metadata tag.
            if (closeBrace != null)
                tag.span(openBrace, closeBrace);
            else if (closeParen != null)
                tag.span(openBrace, closeParen);
            else
                tag.span(openBrace, LT(0));
        }
        if (tag != null)
            asDocDelegate.afterMetadata(tag.getAbsoluteEnd());
    }

    @Override
    public void reportError(RecognitionException ex)
    {
        //Suppress errors for now
    }

    protected final String getText(Token token)
    {
        return token != null ? token.getText() : "";
    }

    protected final IdentifierNode build(Token token)
    {
        IdentifierNode name = new IdentifierNode(getText(token));
        name.span(token);
        return name;
    }

    @Override
    public IMetaTagsNode parse(Reader reader)
    {
        MetadataTokenizer tokenizer = new MetadataTokenizer(reader);
        try
        {
            List<MetadataToken> tokens = tokenizer.parseTokens();
            MetaTagsNode node = new MetaTagsNode();
            new MetadataParser(new GenericTokenStream(tokens)).meta(node);
            return node;
        }
        catch (ANTLRException e)
        {
            // ignore parser errors
            return null;
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                //do nothing
            }
        }
    }

    /**
     * Apply any comments to the metadata node passed in. Callers should ensure
     * that the metadata type should have comments bound to it
     * 
     * @param node the metadata to bind the comment to
     */
    void applyComment(BaseDefinitionMetaTagNode node)
    {
        IASDocComment asDoc = asDocDelegate.afterDefinition(node);
        if (asDoc != null)
            node.setASDocComment(asDoc);
    }

    public void setASDocDelegate(IMetadataParserASDocDelegate asDocDelegate)
    {
        this.asDocDelegate = asDocDelegate;
    }
}
