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
 * Interface used by the ASParser to record ASDoc information.
 * <p>
 * Implementations of this interface that record ASDoc data are stateful and can
 * not be shared between ASParser instances.
 */
public interface IASParserASDocDelegate
{
    /**
     * Called by the ASParser before a variable is parsed.
     * <p>
     * This is here to handle cases like this:
     * <pre>
     * /**
     *  * Documentation for variable a.
     *  &#x2a;/
     * var a:*,b:* = function () {
     * 
     *  var var_that_is_not_documented : *;
     *  }
     * </pre>
     * <p>
     * The easiest way to handle the above case is to push the current ASDoc state on to a 
     * stack and pop it off that stack when {@link #afterVariable()} is called.
     */
    void beforeVariable();
    
    /**
     * @see #beforeVariable()
     */
    void afterVariable();
    
    /**
     * Called by the ASParser whenever a {@link Token} containing an ASDoc
     * comment is encountered.
     * @param asDocToken A {@link Token} containing an ASDoc comment.
     */
    void setCurrentASDocToken(Token asDocToken);
    
    /**
     * Called by the ASParser after an {@link IDocumentableDefinitionNode} has been constructed
     * and fully parsed.
     * @param definitionNode {@link IDocumentableDefinitionNode} that has been parsed.
     * @return An {@link IASDocComment} that should be attached to the {@link IDocumentableDefinitionNode}.
     */
    IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode);
    
    /**
     * Called by the ASParser to get an implementation of
     * {@link IMetadataParserASDocDelegate} that will be used by the
     * MetadataParser to record information about ASDoc comments
     * encountered while parsing ActionScript meta-data.
     * <p>
     * Implementations of the {@link IMetadataParserASDocDelegate} interface
     * that record ASDoc data are stateful and can not be shared between
     * MetadataParser instances.
     * 
     * @return An implementation of {@link IMetadataParserASDocDelegate} that
     * will be used by the MetadataParser to record information about
     * ASDoc comments encountered while parsing ActionScript meta-data.
     */
    IMetadataParserASDocDelegate getMetadataParserASDocDelegate();
}
