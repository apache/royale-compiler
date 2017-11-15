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

import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;

/**
 * This singleton class represents the 2009 dialect of MXML,
 * with the language namespace <code>"http://ns.adobe.com/mxml/2009"</code>.
 * <p>
 * In addition to the special language tags of the 2006 dialect,
 * this dialect supports {@code <Declarations>},  {@code <Definition>}, 
 * {@code <Library>}, {@code <Private>},  and {@code <Reparent>}.
 */
public class MXMLDialect2009 extends MXMLDialect2006
{
    // The singleton instance of this class.
    private static final MXMLDialect INSTANCE =
        new MXMLDialect2009(IMXMLLanguageConstants.NAMESPACE_MXML_2009, 2009);
    
    /**
     * Gets the singleton instance of this class.
     */
    public static MXMLDialect getInstance()
    {
        return INSTANCE;
    }
    
    // Protected constructor
    protected MXMLDialect2009(String languageNamespace, int year)
    {
        super(languageNamespace, year);
        
        declarationsXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.DECLARATIONS);
        definitionXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.DEFINITION);
        libraryXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.LIBRARY);
        privateXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.PRIVATE);
        reparentXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.REPARENT);
    }
}
