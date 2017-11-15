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

import antlr.Token;

import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * Interface used by the MetadataParser to recored information about
 * ASDoc comments encountered while parsing meta-data.
 * <p>
 * Implementations of this interface that record ASDoc data are stateful and can
 * not be shared between MetadataParser instances.
 */
public interface IMetadataParserASDocDelegate
{
    /**
     * Called by the MetadataParser whenever a {@link Token} containing an ASDoc
     * comment is encountered.
     * @param asDocToken A {@link Token} containing an ASDoc comment.
     */
    void setCurrentASDocToken(Token asDocToken);
    
    /**
     * Called by the MetadataParser after an {@link IDocumentableDefinitionNode} has been constructed
     * and fully parsed.
     * @param definitionNode {@link IDocumentableDefinitionNode} that has been parsed.
     * @return An {@link IASDocComment} that should be attached to the {@link IDocumentableDefinitionNode}.
     */
    IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode);
    
    /**
     * Called by the MetadataParser after parsing a meta-data tag
     * that should prevent the current ASDoc comment from attaching to any
     * subsequent meta-data tag.
     * 
     * @param metaDataTagName The name of the meta-data tag prevents the
     * current ASDoc comment from attaching to any subsequent meta-data tag.
     */
    void clearMetadataComment(String metaDataTagName);
    
    /**
     * Called by the MetadataParser any time a meta-data tag is
     * parsed.
     * 
     * @param metaDataEndOffset The end offset of the meta-data tag that has
     * been parsed.
     */
    void afterMetadata(int metaDataEndOffset);
}
