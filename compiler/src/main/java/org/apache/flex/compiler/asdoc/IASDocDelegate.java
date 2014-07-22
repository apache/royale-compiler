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

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.definitions.IDocumentableDefinition;
import org.apache.flex.compiler.internal.parsing.as.ASParser;
import org.apache.flex.swc.io.SWCReader;


/**
 * Delegate interface used by the {@link ASParser} to record
 * ASDoc information encountered while parsing ActionScript
 * or meta-data tags.
 */
public interface IASDocDelegate
{
    /**
     * Get's an implementation of {@link IASParserASDocDelegate} that can be
     * used by the {@link ASParser} to record information about ASDoc comments
     * encountered while parsing ActionScript.
     * <p>
     * Implementations of the {@link IASParserASDocDelegate} interface that
     * record ASDoc data are stateful and can not be shared between
     * {@link ASParser} instances.
     * 
     * @return an implementation of {@link IASParserASDocDelegate} that can be
     * used by the {@link ASParser} to recored information about ASDoc comments
     */
    IASParserASDocDelegate getASParserASDocDelegate();
    
    /**
     * Called by MXML tree building code to create an {@link IASDocComment} for
     * classes defined by MXML files.
     * @param location The location of the ASDoc text in an MXML file.
     * @param definition The {@link org.apache.flex.compiler.definitions.IDefinition} the ASDoc documents
     * @return A new {@link IASDocComment} or null.
     */
    IASDocComment createASDocComment(ISourceLocation location, IDocumentableDefinition definition);
    
    /**
     * Get's an implementation of {@link IPackageDITAParser} that can be used by
     * the {@link SWCReader} to parse DITA information found in a SWC.
     * @return An implementation of {@link IPackageDITAParser}.
     */
    IPackageDITAParser getPackageDitaParser();
}
