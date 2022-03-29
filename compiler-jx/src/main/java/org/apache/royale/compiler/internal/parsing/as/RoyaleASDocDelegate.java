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




import antlr.Token;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.asdoc.IASDocDelegate;
import org.apache.royale.compiler.asdoc.IASParserASDocDelegate;
import org.apache.royale.compiler.asdoc.IMetadataParserASDocDelegate;
import org.apache.royale.compiler.asdoc.IPackageDITAParser;
import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.internal.codegen.js.utils.DocEmitterUtils;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.metadata.BasicMetaTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;
import org.apache.royale.compiler.utils.DefinitionUtils;

/**
 * Default implementation of {@link IASDocDelegate} that does not have any code
 * model or eclipse dependencies.
 */
public final class RoyaleASDocDelegate implements IASDocDelegate
{
    private static final RoyaleASDocDelegate INSTANCE = new RoyaleASDocDelegate();

    /**
     * Gets the single instance of this delegate.
     * 
     * @return The single instance of this delegate.
     */
    public static IASDocDelegate get()
    {
        return INSTANCE;
    }

    private boolean noDoc = false;

    public RoyaleASDocDelegate()
    {
    }

    /**
     * Passing true as constructor argument allows for processing of comments, without returning
     * the parsed comment for storage.
     * This option is included to provide outward behavior similar to NilASDocDelegate, whilst
     * still allowing its use as a 'hook' in the 'afterDefinition' method, to 'process' comments
     * during initial processing of AST nodes.
     *
     * @param noDocReturn
     */
    public RoyaleASDocDelegate(Boolean noDocReturn)
    {
        super();
        this.noDoc = noDocReturn;
    }

    @Override
    public IASParserASDocDelegate getASParserASDocDelegate()
    {
        return new ASDelegate(noDoc);
    }

    @Override
    public IASDocComment createASDocComment(ISourceLocation location, IDocumentableDefinition definition)
    {
        return null;
    }

    @Override
    public IPackageDITAParser getPackageDitaParser()
    {
        return IPackageDITAParser.NIL_PARSER;
    }

    private static final class ASDelegate implements IASParserASDocDelegate, IMetadataParserASDocDelegate
    {
        @SuppressWarnings("unused")
		static final ASDelegate INSTANCE = new ASDelegate(false);

        private boolean noDoc = false;
        public ASDelegate(Boolean noDocReturn)
        {
            this.noDoc = noDocReturn;
        }

        @Override
        public void beforeVariable()
        {
        }

        @Override
        public void afterVariable()
        {
        }

        Token currentToken;
        
        @Override
        public void setCurrentASDocToken(Token asDocToken)
        {
            currentToken = asDocToken;
        }

        @Override
        public IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode)
        {
            if (currentToken == null)
                return null;
            
            ASDocComment comment = new ASDocComment(currentToken);
            if (definitionNode instanceof BaseDefinitionNode && DocEmitterUtils.hasSuppressExport(comment.commentNoEnd())) {
                MetaTagsNode tags = (MetaTagsNode)definitionNode.getMetaTags();
                if (tags == null) {
                    tags = new MetaTagsNode();
                    ((BaseDefinitionNode) definitionNode).setMetaTags(tags);
                }
                if (!tags.hasTagByName(DefinitionUtils.JSROYALE_SUPPRESS_EXPORT)) {
                    tags.addTag(new BasicMetaTagNode(DefinitionUtils.JSROYALE_SUPPRESS_EXPORT));
                }
           }
            currentToken = null;
            return !noDoc ? comment : null;
        }

        @Override
        public IMetadataParserASDocDelegate getMetadataParserASDocDelegate()
        {
        	// ASDelegate is also MetadataDelegate because when metadata like
        	// event metadata has asdoc, the parser sees the asdoc token before
        	// seeing the metadata tokens so it tells the ASDelegate about
        	// the token but then asks the metadata delegate after the
        	// definition.  Sharing the token between the two types of
        	// delegates seems to fix the problem.
            return this;
        }

        @Override
        public void clearMetadataComment(String metaDataTagName)
        {
        }

        @Override
        public void afterMetadata(int metaDataEndOffset)
        {
        }
    }
}
