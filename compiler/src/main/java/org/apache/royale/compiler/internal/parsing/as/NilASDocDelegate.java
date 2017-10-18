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
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * Default implementation of {@link IASDocDelegate} that does not have any code
 * model or eclipse dependencies.
 */
public final class NilASDocDelegate implements IASDocDelegate
{
    private static final NilASDocDelegate INSTANCE = new NilASDocDelegate();

    /**
     * Gets the single instance of this delegate.
     * 
     * @return The single instance of this delegate.
     */
    public static IASDocDelegate get()
    {
        return INSTANCE;
    }

    private NilASDocDelegate()
    {
    }

    @Override
    public IASParserASDocDelegate getASParserASDocDelegate()
    {
        return ASDelegate.INSTANCE;
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

    private static final class ASDelegate implements IASParserASDocDelegate
    {
        static final ASDelegate INSTANCE = new ASDelegate();

        @Override
        public void beforeVariable()
        {
        }

        @Override
        public void afterVariable()
        {
        }

        @Override
        public void setCurrentASDocToken(Token asDocToken)
        {
        }

        @Override
        public IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode)
        {
            return null;
        }

        @Override
        public IMetadataParserASDocDelegate getMetadataParserASDocDelegate()
        {
            return MetadataDelegate.INSTANCE;
        }

    }

    private static final class MetadataDelegate implements IMetadataParserASDocDelegate
    {
        static final MetadataDelegate INSTANCE = new MetadataDelegate();

        @Override
        public void setCurrentASDocToken(Token asDocToken)
        {
        }

        @Override
        public IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode)
        {
            return null;
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
