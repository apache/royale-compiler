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

import java.io.InputStream;

import org.apache.royale.swc.dita.IDITAList;

/**
 * Interface used by SWCReader to parse DITA files containing ASDoc
 * information in SWCs.
 */
public interface IPackageDITAParser
{
    /**
     * Nil implementation of {@link IPackageDITAParser}.
     */
    static final IPackageDITAParser NIL_PARSER = new IPackageDITAParser()
    {
        @Override
        public IDITAList parse(String swcFilePath, InputStream stream)
        {
            return null;
        }
    };
    
    /**
     * Parses DITA information from the specified {@link InputStream}.
     * @param swcFilePath The name of the SWC file that contains the DITA information to parse.
     * @param stream The {@link InputStream} that reads the DITA information.
     * @return A new {@link IDITAList} or null.
     */
    IDITAList parse(String swcFilePath, InputStream stream);
}
