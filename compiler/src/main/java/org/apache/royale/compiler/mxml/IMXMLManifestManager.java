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

package org.apache.royale.compiler.mxml;

import java.util.Collection;
import java.util.Set;

import org.apache.royale.compiler.common.XMLName;

/**
 * 
 */
public interface IMXMLManifestManager
{
    /**
     * Uses the manifest information to map an MXML tag
     * to a ActionScript classname.
     * 
     * @param tagName An {@code XMLName} representing an MXML tag,
     * such as an {@code <s:Button>} tag.
     * 
     * @return A fully-qualified ActionScript classname,
     * such as <code>"spark.controls.Button"</code>
     */
    String resolve(XMLName tagName);
    
    /**
     * Determines if a manifest entry is "lookupOnly" or not.
     * 
     * @param tagName An {@code XMLName} representing an MXML tag.

     * @return <code>true</code> if tag name's manifest entry has its <code>lookupOnly</code>
     * property set to <code>true</code>, and <code>false</code> otherwise. 
     */
    boolean isLookupOnly(XMLName tagName);
    
    /**
     * Uses the manifest information to find all the MXML tags that map to a
     * specified fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * 
     * @param className Fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * @return A collection of {@link XMLName}'s representing a MXML tags, such
     * as a <code>"Button"</code> tag in the namespace
     * <code>"library://ns.adobe.com/flex/spark"</code>.
     */
    Collection<XMLName> getTagNamesForClass(String className);
    
    /**
     * Find all the qualified names in the manifest information that have a
     * corresponding MXML tag whose namespace is in the specified set of
     * namespaces.
     * 
     * @param namespaceURIs Set set of MXML tag namespace URIs such as:
     * <code>"library://ns.adobe.com/flex/spark"</code>
     * @param manifestEntriesOnly determines if all the qualified names are 
     * returned or if only the entries from manifest files are returned.
     * @return A collection of qualified names (
     * <code>spark.components.Button</code> for example ) that have correponding
     * MXML tags in the manifest information whose namespaces are in the
     * specified set of namespaces.
     */
    Collection<String> getQualifiedNamesForNamespaces(Set<String> namespaceURIs, boolean manifestEntriesOnly);
}
