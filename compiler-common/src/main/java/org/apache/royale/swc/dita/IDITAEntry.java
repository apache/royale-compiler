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

package org.apache.royale.swc.dita;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.definitions.IDefinition;

/**
 * An IDITAEntry represents a category of DITA data stored within in a SWC for a specific package
 */
public interface IDITAEntry
{
    /**
     * Returns the package name that this IDITAEntry represents
     */
    String getPackageName();
    
    /**
     * Returns the comment for the specific {@link IDefinition} if it exists.  If nothing exists, this method will return null
     * @param defintion the {@link IDefinition} whose comment we want to find
     * @return an {@link IASDocComment} or null
     * @throws Exception
     */
    IASDocComment getComment(IDefinition defintion) throws Exception;
}
