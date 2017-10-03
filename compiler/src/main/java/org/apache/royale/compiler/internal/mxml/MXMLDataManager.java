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

package org.apache.royale.compiler.internal.mxml;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.caches.MXMLDataCache;
import org.apache.royale.compiler.mxml.IMXMLDataManager;

/**
 * The {@code MXMLDataManager} of the {@code IWorkspace}
 * maintains a cache of DOM-like {@code MXMLData} objects for MXML files.
 * This cache supports concurrent access by multiple threads.
 * <p>
 * If an MXML file is used in multiple projects, its {@code MXMLData}
 * can be shared, but its symbol table and parse tree cannot,
 * because the meaning of various MXML tags could be different
 * in different projects.
 */
public class MXMLDataManager implements IMXMLDataManager
{
    /**
     * Constructor.
     */
    public MXMLDataManager()
    {
        mxmlDataCache = new MXMLDataCache();
    }
    
    // This cache supports concurrent access by multiple threads.
    private final MXMLDataCache mxmlDataCache;
    
    @Override
    public MXMLData get(IFileSpecification fileSpec)
    {
        MXMLData mxmlData = mxmlDataCache.get(MXMLDataCache.createKey(fileSpec));
        assert mxmlData.verify() : "MXMLData failed verification";
        return mxmlData;
    }
    
    @Override
    public void invalidate(IFileSpecification fileSpec)
    {
        mxmlDataCache.remove(MXMLDataCache.createKey(fileSpec));
    }
}
