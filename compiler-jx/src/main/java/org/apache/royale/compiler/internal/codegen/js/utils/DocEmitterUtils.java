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

package org.apache.royale.compiler.internal.codegen.js.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;

public class DocEmitterUtils
{
    public static List<String> loadImportIgnores(JSRoyaleEmitter emitter, String doc)
    {
        ArrayList<String> ignoreList = new ArrayList<String>();
        String ignoreToken = JSRoyaleEmitterTokens.IGNORE_IMPORT.getToken();
        int index = doc.indexOf(ignoreToken);
        while (index != -1)
        {
            String ignorable = doc.substring(index + ignoreToken.length());
            int endIndex = ignorable.indexOf("\n");
            ignorable = ignorable.substring(0, endIndex);
            ignorable = ignorable.trim();
            ignoreList.add(ignorable);
            index = doc.indexOf(ignoreToken, index + endIndex);
        }
        
        // TODO (mschmalle)
        ((JSRoyaleDocEmitter)emitter.getDocEmitter()).setClassIgnoreList(ignoreList);
        return ignoreList;
    }
    
    
    public static Boolean hasSuppressExport(JSRoyaleEmitter emitter, String doc)
    {

        String ignoreToken = JSRoyaleEmitterTokens.SUPPRESS_EXPORT.getToken();
        int index = doc.indexOf(ignoreToken);
        return index != -1;
    }
}
