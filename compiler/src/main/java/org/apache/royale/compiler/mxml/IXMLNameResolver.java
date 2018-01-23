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

import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;

/**
 * Interface to handle resolving an XML name to a fully-qualified ActionScript
 * name or to a definition.
 */
public interface IXMLNameResolver
{
    /**
     * Resolves an XML name, such as
     * <code>("library://ns.adobe.com/flex/spark", "Button")</code> for an
     * {@code <s:Button>} tag, to a fully-qualified ActionScript class name such
     * as "spark.components.Button" that the manifest information has associated
     * with the XML name.
     * <p>
     * This method handles both manifest namespaces (such as in the above
     * example) and package namespaces such as <d:Sprite
     * xmlns:d="flash.display.*">. <b>Note:</b> This method should only be
     * called when resolving a tag outside the context of an MXML file. Normally
     * {@link org.apache.royale.compiler.internal.scopes.MXMLFileScope#resolveTagToQualifiedName(IMXMLTagData)}
     * should be used instead.
     * 
     * @param tagXMLName An {@link XMLName} to resolve to a fully-qualified
     * ActionScript class name.
     * @param mxmlDialect The {@link MXMLDialect} of the document in which the
     * specified {@link XMLName} was encountered.
     * @return The qname of the ActionScript class, or <code>null</code> if the
     * tag has a manifest namespace and isn't found in the MXMLManifestManager.
     */
    String resolveXMLNameToQualifiedName(XMLName tagXMLName, MXMLDialect mxmlDialect);

    /**
     * Resolves an {@link XMLName}, such as
     * <code>("library://ns.adobe.com/flex/spark", "Button")</code> for an
     * {@code <s:Button>} tag, to a class definition that the manifest
     * information has associated with the XML name.
     * <p>
     * This method handles both manifest namespaces (such as in the above
     * example) and package namespaces such as <d:Sprite
     * xmlns:d="flash.display.*">.
     * 
     * @param tagXMLName {@link XMLName} of a tag.
     * @param mxmlDialect The {@link MXMLDialect} of the document in which the
     * specified {@link XMLName} was encountered.
     * @return The definition of the ActionScript class, or <code>null</code> if
     * the tag has a manifest namespace and isn't found in the
     * MXMLManifestManager.
     */
    IDefinition resolveXMLNameToDefinition(XMLName tagXMLName, MXMLDialect mxmlDialect);
}
