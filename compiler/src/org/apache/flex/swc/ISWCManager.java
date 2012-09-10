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

package org.apache.flex.swc;

import java.io.File;

import org.apache.flex.compiler.internal.caches.AssetTagCache;
import org.apache.flex.compiler.internal.caches.CSSDocumentCache;
import org.apache.flex.compiler.internal.caches.FileScopeCache;
import org.apache.flex.compiler.internal.caches.SWFCache;

/**
 * {@code ISWCManager} is a repository of SWC library models. It has all the SWC
 * libraries shared within a workspace.
 */
public interface ISWCManager
{
    /**
     * Get the SWC model of the given SWC file. If the manager has a valid model
     * for the file, it returns it. Otherwise, it will parse the SWC file, store
     * the model and return it.
     * 
     * @param file SWC file
     * @return SWC model
     */
    ISWC get(File file);

    /**
     * Removes an existing SWC file from the cache
     * 
     * @param file SWC file to be removed
     */
    void remove(File file);

    /**
     * Get SWF cache.
     * 
     * @return {@link SWFCache}
     */
    SWFCache getSWFCache();

    /**
     * Get file scope cache.
     * 
     * @return {@link FileScopeCache}
     */
    FileScopeCache getFileScopeCache();

    /**
     * Get asset tags cache.
     * 
     * @return {@link AssetTagCache}
     */
    AssetTagCache getAssetTagCache();

    /**
     * @return Cache for CSS models from a CSS file or a default CSS in a SWC
     * library.
     */
    CSSDocumentCache getCSSDocumentCache();
}
