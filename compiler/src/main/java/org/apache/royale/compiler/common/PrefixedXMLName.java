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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.constants.IMXMLCoreConstants;
import org.apache.royale.compiler.internal.mxml.MXMLData;

/**
 * A subclass of XMLName used by clients that need to manipulate the actual
 * syntactic form of the element name (including its prefix). Most clients of
 * codemodel/mxmlmodel should use XMLName instead, and rely on the full
 * namespace URI; the specific namespace prefix that's on a given element in a
 * given document has no meaning outside that document. In particular, no one
 * should ever assume that "mx:" is the prefix being used for the Flex
 * namespace.
 */
public class PrefixedXMLName extends XMLName
{
    /**
     * Constructor This version will parse the containingDocument on the fly (or
     * use an existing parse, if there is already one in the MXMLDataProvider).
     * 
     * @param rawName full element name (e.g. mx:Button)
     * @param mxmlData MXMLData containing the element
     */
    public PrefixedXMLName(String rawName, MXMLData mxmlData)
    {
        name = splitName(rawName);
        assert name != null;
        xmlNamespace = resolvePrefix(prefix, mxmlData);
        if (xmlNamespace == null)
            xmlNamespace = IMXMLCoreConstants.emptyString;
    }

    /**
     * Constructor
     * 
     * @param rawName full element name (e.g. mx:Button)
     * @param prefixMap map of element prefixes to XML namespaces
     */
    public PrefixedXMLName(String rawName, PrefixMap prefixMap)
    {
        name = splitName(rawName);
        assert name != null;
        xmlNamespace = prefixMap.getNamespaceForPrefix(prefix);
        if (xmlNamespace == null)
            xmlNamespace = IMXMLCoreConstants.emptyString;
    }

    public PrefixedXMLName(String rawName, String uri)
    {
        name = splitName(rawName);
        xmlNamespace = uri;
    }

    /**
     * Constructor used when we already know the specific namespace and prefix
     * being used by a particular instance of an XML element.
     * 
     * @param xmlNamespace the namespace URI
     * @param name the unprefixed element name
     * @param rawName the prefixed element name (e.g. mx:Button)
     */
    public PrefixedXMLName(String xmlNamespace, String name, String rawName)
    {
        splitName(rawName);
        this.name = name;
        assert name != null;
        this.xmlNamespace = (xmlNamespace == null) ? IMXMLCoreConstants.emptyString : xmlNamespace;
    }

    /**
     * Prefix from the intial lookup. Only accurate when in the context of a
     * document.
     */
    private String prefix;

    /**
     * Cached value of the full name. It's ok to cache this because we can't
     * ever change the name, namespace, or prefix after this object is
     * constructed.
     */
    private String prefixedName;

    /**
     * Split a raw tag name (e.g. mx:Button) into a prefix (mx) and short name
     * (Button). The prefix is stored in fPrefix, and the short name is
     * returned. For use in constructors.
     * 
     * @param rawName full element name (e.g. mx:Button)
     * @return short name (e.g. Button)
     */
    private String splitName(String rawName)
    {
        String name;

        if (rawName == null)
        {
            prefix = IMXMLCoreConstants.emptyString;
            return IMXMLCoreConstants.emptyString;
        }

        int colonIndex = rawName.indexOf(IMXMLCoreConstants.colon);
        if (colonIndex > -1)
        {
            prefix = rawName.substring(0, colonIndex);
            name = rawName.substring(colonIndex + 1);
        }
        else
        {
            prefix = IMXMLCoreConstants.emptyString;
            name = rawName;
        }

        return name;
    }

    /**
     * Determine which XML namespace is appropriate for the given prefix in the
     * given document
     * 
     * @param prefix element prefix
     * @param mxmlData containing MXMLData
     * @return XML namespaces
     */
    protected String resolvePrefix(String prefix, MXMLData mxmlData)
    {
        return resolvePrefix(prefix, mxmlData.getRootTagPrefixMap());
    }

    /**
     * Determine which XML namespace is appropriate for the given prefix in the
     * given prefix map
     */
    protected String resolvePrefix(String prefix, PrefixMap prefixMap)
    {
        if (prefixMap.containsPrefix(prefix))
            return prefixMap.getNamespaceForPrefix(prefix);

        return IMXMLCoreConstants.emptyString;
    }

    /**
     * Get the prefix for the element
     * 
     * @return the prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Return the prefixed version of the name.
     */
    public String getPrefixedName()
    {
        if (prefixedName != null)
            return prefixedName;
        
        if (prefix != null && !prefix.equals(IMXMLCoreConstants.emptyString))
            prefixedName = (prefix + IMXMLCoreConstants.colon + getName()).intern();
        else
            prefixedName = getName();
        
        return prefixedName;
    }
}
