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

package org.apache.royale.compiler.tree.mxml;

import org.apache.royale.compiler.mxml.IMXMLTagData;

/**
 * This AST node represents an MXML <code>&lt;XML&gt;</code> tag.
 * <p>
 * An {@link IMXMLXMLNode} has no child nodes, but it stores a reference to an
 * {@link IMXMLTagData} object which represents the single XML tag inside the
 * <code>&lt;XML&gt;</code> tag.
 */
public interface IMXMLXMLNode extends IMXMLInstanceNode
{
    // name used to lookup type for format="xml"
    String XML_NODE_NAME = "flash.xml.XMLNode";

    /**
     * Gets the XML that this node represents as a string. This will trim out
     * all the bindable parts, as those parts will be set programmatically when
     * their bindings fire.
     * 
     * @return A String representation of the XML object
     */
    String getXMLString();

    static enum XML_TYPE
    {
        E4X, // this node should create an E4X style XML object
        OLDXML
        // this node should create an old XMLNode object
    }

    /**
     * What type of xml object should this node create.
     */
    XML_TYPE getXMLType();
}
