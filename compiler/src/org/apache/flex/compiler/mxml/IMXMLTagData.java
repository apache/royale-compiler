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

package org.apache.flex.compiler.mxml;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.common.PrefixMap;
import org.apache.flex.compiler.common.XMLName;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;

/**
 * Encapsulation of an open tag, a close tag, or an empty tag in MXML.
 */
public interface IMXMLTagData extends ISourceLocation
{
    IFileSpecification getSource();
        
    MXMLDialect getMXMLDialect();
    
    IMXMLData getParent();
    
    IMXMLTagData getParentTag();
    
    boolean isEmptyTag();
    
    boolean isCloseTag();
    
    String getPrefix();
    
    PrefixMap getPrefixMap();
    
    PrefixMap getCompositePrefixMap();
    
    String getURI();
    
    String getName();
    
    String getShortName();
    
    XMLName getXMLName();
    
    String getStateName();
    
    IMXMLTagAttributeData[] getAttributeDatas();
    
    IMXMLTagAttributeData getTagAttributeData(String attributeName);
    
    String getRawAttributeValue(String attributeName);
    
    ISourceLocation getLocationOfChildUnits();
    
    MXMLUnitData getFirstChildUnit();
    
    IMXMLTagData getFirstChild(boolean includeEmptyTags);
    
    IMXMLTagData getNextSibling(boolean includeEmptyTags);
    
    IMXMLTagData findMatchingEndTag();
    
    String getCompilableText();
}
