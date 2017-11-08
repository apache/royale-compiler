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

package org.apache.royale.swc.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.internal.config.QNameNormalization;
import org.apache.royale.compiler.problems.ComponentTagWithoutURIProblem;
import org.apache.royale.compiler.problems.FileInLibraryIOProblem;
import org.apache.royale.swc.ISWCVersion;
import org.apache.royale.swc.SWC;
import org.apache.royale.swc.SWCComponent;
import org.apache.royale.swc.SWCDigest;
import org.apache.royale.swc.SWCLibrary;
import org.apache.royale.swc.SWCScript;
import org.apache.royale.swc.io.SWCReader;
import org.apache.royale.utils.FilenameNormalization;

/**
 * A StAX implementation to parse catalog.xml in a SWC.
 */
public class StAXCatalogReader implements ICatalogXMLConstants
{
    public StAXCatalogReader(final InputStream in, final SWC swc) throws XMLStreamException
    {
        if (swc == null)
            throw new NullPointerException("SWC model can't be null");
        if (in == null)
            throw new NullPointerException("InputStream can't be null.");

        this.swc = swc;
        this.timeStamps = new HashMap<String, String>();
        this.componentIndex = new HashMap<String, SWCComponent>();

        // A filter only keeps start and end XML elements.
        final StreamFilter filter = new StreamFilter()
        {
            @Override
            public boolean accept(XMLStreamReader reader)
            {
                return reader.isStartElement();
            }
        };

        // Configure the XML factory.
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        assert factory != null : "Null XMLInputFactory";

        // Create Stream XML reader.
        final XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(in);
        reader = factory.createFilteredReader(xmlStreamReader, filter);

    }

    private final XMLStreamReader reader;
    private final SWC swc;
    private final Map<String, String> timeStamps;
    private final Map<String, SWCComponent> componentIndex;
    private SWCLibrary currentLibrary;
    private SWCScript currentScript;

    /**
     * Close the StAX parser and the underlying {@code InputStream}.
     */
    public void close() throws IOException
    {
        try
        {
            reader.close();
        }
        catch (XMLStreamException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public void parse()
    {
        final String containingSWCFile = FilenameNormalization.normalize(swc.getSWCFile().getAbsolutePath());

        try
        {
            while (reader.hasNext())
            {
                final int next = reader.next();
                if (next != XMLStreamConstants.START_ELEMENT)
                    continue;

                final String tagName = reader.getName().getLocalPart();
                if (tagName.equals(TAG_LIBRARY))
                {
                    final String path = reader.getAttributeValue(null, ATTR_PATH);
                    currentLibrary = new SWCLibrary(path);
                    swc.addLibrary(currentLibrary);
                }
                else if (tagName.equals(TAG_COMPONENT))
                {
                    String className = reader.getAttributeValue(null, ATTR_CLASS_NAME);
                    className = QNameNormalization.normalize(className);                    
                    final String componentName = reader.getAttributeValue(null, ATTR_NAME);
                    final String uri = reader.getAttributeValue(null, ATTR_URI);
                    
                    if (uri == null || uri.isEmpty())
                    {
                        swc.addProblem(new ComponentTagWithoutURIProblem(componentName, SWCReader.CATALOG_XML, containingSWCFile));
                        continue;
                    }
                    
                    final String icon = reader.getAttributeValue(null, ATTR_ICON);
                    final String preview = reader.getAttributeValue(null, ATTR_PREVIEW);
                    final SWCComponent component = new SWCComponent();
                    if (className != null)
                        component.setQName(className.intern());
                    if (componentName != null)
                        component.setName(componentName.intern());
                    
                    // uri is null-checked already
                    component.setURI(uri.intern());
                    
                    if (icon != null)
                        component.setIcon(icon.intern());
                    if (preview != null)
                        component.setPreview(preview.intern());
                    swc.addComponent(component);
                    componentIndex.put(className, component);
                }
                else if (tagName.equals(TAG_SCRIPT))
                {
                    String name = reader.getAttributeValue(null, ATTR_NAME);
                    String mod = reader.getAttributeValue(null, ATTR_MOD);
                    String signatureChecksum = reader.getAttributeValue(null, ATTR_SIGNATURE_CHECKSUM);
                    currentScript = new SWCScript();
                    if (name != null)
                        currentScript.setName(name.intern());
                    if (mod != null)
                        currentScript.setLastModified(Long.parseLong(mod));
                    if (signatureChecksum != null)
                        currentScript.setSignatureChecksum(signatureChecksum);
                    currentLibrary.addScript(currentScript);
                    timeStamps.put(name, mod);
                }
                else if (tagName.equals(TAG_ROYALE))
                {
                    final String royaleVersion = reader.getAttributeValue(null, ATTR_VERSION);
                    final String minSupportedVersion = reader.getAttributeValue(null, ATTR_MINIMUM_SUPPORTED_VERSION);
                    final String royaleBuild = reader.getAttributeValue(null, ATTR_BUILD);
                    ISWCVersion swcVersion = swc.getVersion();
                    swcVersion.setRoyaleVersion(royaleVersion);
                    swcVersion.setRoyaleMinSupportedVersion(minSupportedVersion);
                    swcVersion.setRoyaleBuild(royaleBuild);
                }
                else if (tagName.equals(TAG_SWC))
                {
                    final String swcVersion = reader.getAttributeValue(null, ATTR_VERSION);
                    swc.getVersion().setSWCVersion(swcVersion);
                }
                else if (tagName.equals(TAG_DEF))
                {
                    String id = reader.getAttributeValue(null, ATTR_ID);
                    id = QNameNormalization.normalize(id);
                    currentScript.addDefinition(id);

                    final SWCComponent component = componentIndex.get(id);
                    if (component != null)
                        component.setScript(currentScript);
                }
                else if (tagName.equals(TAG_DEP))
                {
                    String id = reader.getAttributeValue(null, ATTR_ID);
                    id = QNameNormalization.normalize(id);
                    final String type = reader.getAttributeValue(null, ATTR_TYPE);
                    assert type.length() == 1;
                    currentScript.addDependency(id, DependencyType.get(type.charAt(0)));
                }
                else if (tagName.equals(TAG_FILE))
                {
                    String path = reader.getAttributeValue(null, ATTR_PATH);
                    String modString = reader.getAttributeValue(null, ATTR_MOD);

                    long mod = 0;
                    if (modString != null)
                        mod = Long.parseLong(modString);

                    SWCFileEntry fileEntry = new SWCFileEntry(containingSWCFile, path, mod);
                    swc.addFile(fileEntry);
                }
                else if (tagName.equals(TAG_DIGEST))
                {
                    String type = reader.getAttributeValue(null, ATTR_TYPE);
                    String value = reader.getAttributeValue(null, ATTR_VALUE);
                    boolean isSigned = Boolean.parseBoolean(reader.getAttributeValue(null, ATTR_SIGNED));
                    SWCDigest swcDigest = new SWCDigest();
                    swcDigest.setType(type);
                    swcDigest.setValue(value);
                    swcDigest.setSigned(isSigned);
                    
                    currentLibrary.addDigest(swcDigest);
                }
                else if (tagName.equals(TAG_METADATA))
                {
                    String metadataName = reader.getAttributeValue(null, ATTR_NAME);
                    currentLibrary.addNameToKeepAS3MetadataSet(metadataName);
                }
                else if (tagName.equals(TAG_COMPILER))
                {
                    final String compilerName = reader.getAttributeValue(null, ATTR_NAME);
                    final String compilerVersion = reader.getAttributeValue(null, ATTR_VERSION);
                    final String compilerBuild = reader.getAttributeValue(null, ATTR_BUILD);
                    ISWCVersion swcVersion = swc.getVersion();
                    swcVersion.setCompilerName(compilerName);
                    swcVersion.setCompilerVersion(compilerVersion);
                    swcVersion.setCompilerBuild(compilerBuild);
                }
            }
        }
        catch (XMLStreamException e)
        {
            swc.addProblem(new FileInLibraryIOProblem(SWCReader.CATALOG_XML,
                    containingSWCFile,
                    e.getLocalizedMessage()));
        }
    }
    
}
