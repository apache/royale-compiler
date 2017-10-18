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

/**
 * Qualified element name (XML namespace and local name). This does not contain
 * information about the namespace prefix that may be used on the element in a
 * given document-- that's a transient syntactic detail; clients should rely on
 * the namespace URI to disambiguate elements.
 */
public class XMLName implements Comparable<XMLName>
{
    public static final XMLName NULL_NAME = new XMLName("");

    /**
     * Constructor
     * 
     * @param xmlNamespace XML namespace for element (e.g.
     * http://foo.adobe.com/2006/mxml or shared.*)
     * @param name element name without prefix (e.g. Button)
     */
    public XMLName(String xmlNamespace, String name)
    {
        this.name = (name == null) ? "" : name;
        this.xmlNamespace = (xmlNamespace == null) ? "" : xmlNamespace;
    }

    /**
     * Constructor that assumes an empty namespace.
     * 
     * @param name element name without prefix (e.g. Button)
     */
    public XMLName(String name)
    {
        this.name = (name == null) ? "" : name;
        this.xmlNamespace = "";
    }

    /**
     * Constructor for use by our subclasses
     */
    protected XMLName()
    {
        // This stub function is the default constructor, only used by our subclasses.
    }

    /**
     * XML namespace
     */
    protected String xmlNamespace;

    /**
     * Local element name (unqualified)
     */
    protected String name;

    /**
     * Get the element's unqualified local name
     * 
     * @return local name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the XML namespace for the element (e.g.
     * http://foo.adobe.com/2006/mxml or shared.*)
     * 
     * @return XML namespace
     */
    public String getXMLNamespace()
    {
        return xmlNamespace;
    }

    /**
     * Dump the XMLName as a string (for debugging)
     * 
     * @return string representation of the name
     */
    @Override
    public String toString()
    {
        String retVal = null;
        if (!xmlNamespace.equals(""))
            retVal = xmlNamespace + ":" + name;
        else
            retVal = name;

        return retVal;
    }

    @Override
    public int hashCode()
    {
        // use better hashcode impl to avoid all the unneeded recomputation and storage of additional field
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((xmlNamespace == null) ? 0 : xmlNamespace.hashCode());
        return result;
    }

    /**
     * Does this element name equal the one that's passed in?
     * 
     * @return true if the element names are equal
     */
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof XMLName))
            return false;
        XMLName xmlOther = (XMLName)other;

        // All the code in XMLName and PrefixedXMLName makes sure that fName and fXMLNamespace
        // are both non-null, so we don't have to check for null here.
        if (!name.equals(xmlOther.name))
            return false;
        return xmlNamespace.equals(xmlOther.xmlNamespace);
    }

    @Override
    public int compareTo(XMLName o)
    {
        return toString().compareTo(o.toString());
    }
}
