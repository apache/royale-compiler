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

import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.internal.projects.ASProject;

/**
 * Delegate interface used by {@link ASProject} to delegate
 * queries of the ASDoc bundle files to clients.
 * <p>
 * ASDoc bundles are resource bundle SWCs that contain DITA files
 * with localized ASDoc information.
 */
public interface IASDocBundleDelegate
{
    /**
     * A default implementation of {@link IASDocBundleDelegate}.
     */
    static final IASDocBundleDelegate NIL_DELEGATE = new IASDocBundleDelegate()
    {
        @Override
        public IASDocComment getComment(IDocumentableDefinition def, String containingSWCFileName)
        {
            return null;
        }
    };
    
    /**
     * Retrieves the localized {@link IASDocComment} for the
     * specified {@link IDocumentableDefinition} in the specified
     * SWC file.
     * 
     * @param definition The {@link IDocumentableDefinition} whose comment
     * is to be returned.
     * @param containingSWCFileName The file name of the SWC containing the
     * specified definition.
     * @return A {@link IASDocComment} for the specified definition
     * in the specified SWC file, localized for the current locale of the JVM,
     * or <code>null</code> if there is no comment.
     */
    IASDocComment getComment(IDocumentableDefinition definition, String containingSWCFileName);
}
