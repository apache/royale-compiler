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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCComponent;
import org.apache.royale.swc.ISWCDigest;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swc.ISWCVersion;
import com.google.common.collect.Iterables;

/**
 * Use {@link XMLStreamWriter} API to serialize an {@link ISWC} model to XML.
 */
public class StAXCatalogWriter implements ICatalogXMLConstants
{

    /**
     * Create an catalog.xml writer.
     * 
     * @param swc SWC model
     * @param writer writer
     * @throws XMLStreamException XML error
     * @throws FactoryConfigurationError factory error
     */
    public StAXCatalogWriter(final ISWC swc, final Writer writer) throws XMLStreamException, FactoryConfigurationError
    {
        assert swc != null : "expect SWC model";
        assert writer != null : "expect Writer";

        this.swc = swc;

        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        assert xmlOutputFactory != null : "Expect XMLOutputFactory implementation.";
        this.xmlWriter = new XMLFormatter(xmlOutputFactory.createXMLStreamWriter(writer));
    }

    private final XMLStreamWriter xmlWriter;
    private final ISWC swc;

    /**
     * Write serialized XML to output.
     * 
     * @throws XMLStreamException error
     */
    public void write() throws XMLStreamException
    {
        xmlWriter.writeStartDocument(); // start XML

        {
            xmlWriter.writeStartElement(TAG_SWC);
            xmlWriter.writeAttribute(ATTR_XMLNS, XMLNS_SWC_CATALOG_9);
            writeVersions();
            writeFeatures();
            writeComponents();
            writeLibraries();
            writeFiles();
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndDocument(); // end XML

        xmlWriter.flush();
        xmlWriter.close();
    }

    private static final Comparator<ISWCComponent> COMPONENT_COMPARATOR =
        new Comparator<ISWCComponent>() {

            @Override
            public int compare(ISWCComponent o1, ISWCComponent o2)
            {
                return o1.getQName().compareTo(o2.getQName());
            }
        };
    
    private void writeComponents() throws XMLStreamException
    {
        Collection<ISWCComponent> componentsList = swc.getComponents();
        if (componentsList.isEmpty())
            return;
        xmlWriter.writeStartElement(TAG_COMPONENTS);
        ISWCComponent[] components = componentsList.toArray(new ISWCComponent[componentsList.size()]);
        Arrays.sort(components, COMPONENT_COMPARATOR);
        for (final ISWCComponent component : components)
        {
            xmlWriter.writeEmptyElement(TAG_COMPONENT);
            if (component.getQName() != null)
                xmlWriter.writeAttribute(ATTR_CLASS_NAME, dottedQNameToColonQName(component.getQName()));
            if (component.getName() != null)
                xmlWriter.writeAttribute(ATTR_NAME, component.getName());
            if (component.getURI() != null)
                xmlWriter.writeAttribute(ATTR_URI, component.getURI());
            if (component.getIcon() != null)
                xmlWriter.writeAttribute(ATTR_ICON, component.getIcon());
            if (component.getPreview() != null)
                xmlWriter.writeAttribute(ATTR_PREVIEW, component.getPreview());
        }
        xmlWriter.writeEndElement();
    }

    private static final Comparator<ISWCFileEntry> FILE_COMPARATOR =
        new Comparator<ISWCFileEntry>()
        {

            @Override
            public int compare(ISWCFileEntry o1, ISWCFileEntry o2)
            {
                return o1.getPath().compareTo(o2.getPath());
            }
        
        };
    
    private void writeFiles() throws XMLStreamException
    {
        Collection<ISWCFileEntry> filesCollection = swc.getFiles().values();
        if (filesCollection.isEmpty())
            return;
        xmlWriter.writeStartElement(TAG_FILES);
        ISWCFileEntry[] files = filesCollection.toArray(new ISWCFileEntry[filesCollection.size()]);
        Arrays.sort(files, FILE_COMPARATOR);
        for (final ISWCFileEntry file : files)
        {
            xmlWriter.writeEmptyElement(TAG_FILE);
            xmlWriter.writeAttribute(ATTR_PATH, file.getPath());
            xmlWriter.writeAttribute(ATTR_MOD, String.valueOf(file.getLastModified()));
        }
        xmlWriter.writeEndElement();
    }

    private static final Comparator<ISWCLibrary> LIBRARY_COMPARATOR =
        new Comparator<ISWCLibrary>() {

            @Override
            public int compare(ISWCLibrary o1, ISWCLibrary o2)
            {
                return o1.getPath().compareTo(o2.getPath());
            }
        };
        
    private static final Comparator<ISWCScript> SCRIPT_COMPARATOR =
        new Comparator<ISWCScript>() {

            @Override
            public int compare(ISWCScript o1, ISWCScript o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        };
        
    private static final Comparator<ISWCDigest> DIGEST_COMPARATOR =
        new Comparator<ISWCDigest>() {

            @Override
            public int compare(ISWCDigest o1, ISWCDigest o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        };
    
    private void writeLibraries() throws XMLStreamException
    {
        xmlWriter.writeStartElement(TAG_LIBRARIES);

        final Collection<ISWCLibrary> librariesCollection = swc.getLibraries();
        final ISWCLibrary[] libraries = librariesCollection.toArray(new ISWCLibrary[librariesCollection.size()]);
        Arrays.sort(libraries, LIBRARY_COMPARATOR);
        
        for (final ISWCLibrary library : libraries)
        {
            xmlWriter.writeStartElement(TAG_LIBRARY);
            xmlWriter.writeAttribute(ATTR_PATH, library.getPath());
            {
                final Collection<ISWCScript> scriptsList = library.getScripts();
                final ISWCScript[] scripts = scriptsList.toArray(new ISWCScript[scriptsList.size()]);
                Arrays.sort(scripts, SCRIPT_COMPARATOR);
                for (final ISWCScript script : scripts)
                {
                    writeLibraryScript(script);
                }

                final Collection<String> keepAS3MetadataList = library.getKeepAS3MetadataSet();
                if (!keepAS3MetadataList.isEmpty())
                {
                    xmlWriter.writeStartElement(TAG_KEEP_AS3_METADATA);
                    
                    final String[] keepAS3Metadata = keepAS3MetadataList.toArray(new String[keepAS3MetadataList.size()]);
                    Arrays.sort(keepAS3Metadata);
                    for (final String metadata : keepAS3Metadata)
                    {
                        xmlWriter.writeEmptyElement(TAG_METADATA);
                        xmlWriter.writeAttribute(ATTR_NAME, metadata);
                    }
                    xmlWriter.writeEndElement(); // end of <keep-as3-metadata>
                }

                xmlWriter.writeStartElement(TAG_DIGESTS);
                
                final Collection<ISWCDigest> digestsList = library.getDigests();
                final ISWCDigest[] digests = digestsList.toArray(new ISWCDigest[digestsList.size()]);
                Arrays.sort(digests, DIGEST_COMPARATOR);
                for (final ISWCDigest digest : digests)
                {
                    xmlWriter.writeEmptyElement(TAG_DIGEST);
                    xmlWriter.writeAttribute(ATTR_TYPE, digest.getType());
                    xmlWriter.writeAttribute(ATTR_SIGNED, digest.isSigned() ? "true" : "false"); 
                    xmlWriter.writeAttribute(TAG_VALUE, digest.getValue());
                }
                xmlWriter.writeEndElement(); // end of <digests>
            }
            xmlWriter.writeEndElement(); // end of <library>
        }

        xmlWriter.writeEndElement(); // end of <libraries>
    }

    private static String dottedQNameToColonQName(String qname)
    {
        int lastDotIndex = qname.lastIndexOf('.');
        if (lastDotIndex == -1)
            return qname;
        final String packageName = qname.substring(0, lastDotIndex);
        final String baseName = qname.substring(lastDotIndex + 1);
        return packageName + ":" + baseName; 
    }
    
    private static final Comparator<Map.Entry<String, DependencyType>> DEPENDENCY_MAP_ENTRY_COMPARATOR =
        new Comparator<Map.Entry<String, DependencyType>>()
        {

            @Override
            public int compare(Entry<String, DependencyType> arg0, Entry<String, DependencyType> arg1)
            {
                int result = arg0.getKey().compareTo(arg1.getKey());
                if (result != 0)
                    return result;
                return arg0.getValue().ordinal() - arg1.getValue().ordinal();
            }
        
        };
    
    private void writeLibraryScript(final ISWCScript script) throws XMLStreamException
    {
        xmlWriter.writeStartElement(TAG_SCRIPT);
        xmlWriter.writeAttribute(ATTR_NAME, script.getName());
        xmlWriter.writeAttribute(ATTR_MOD, String.valueOf(script.getLastModified()));
        if (script.getSignatureChecksum() != null)
            xmlWriter.writeAttribute(ATTR_SIGNATURE_CHECKSUM, script.getSignatureChecksum());

        final Set<String> definitionsSet = script.getDefinitions();
        final String[] definitions = definitionsSet.toArray(new String[definitionsSet.size()]);
        Arrays.sort(definitions);
        for (final String def : definitions)
        {
            xmlWriter.writeEmptyElement(TAG_DEF);
            xmlWriter.writeAttribute(ATTR_ID, dottedQNameToColonQName(def));
        }

        final Set<Map.Entry<String, DependencyType>> dependenciesSet = script.getDependencies().entries();
        final ArrayList<Map.Entry<String, DependencyType>> dependencies =
            new ArrayList<Map.Entry<String, DependencyType>>(dependenciesSet.size());
        Iterables.addAll(dependencies, dependenciesSet);
        Collections.sort(dependencies, DEPENDENCY_MAP_ENTRY_COMPARATOR);
        
        
        for (final Map.Entry<String, DependencyType> dep : dependencies)
        {
            xmlWriter.writeEmptyElement(TAG_DEP);
            xmlWriter.writeAttribute(ATTR_ID, dottedQNameToColonQName(dep.getKey()));
            xmlWriter.writeAttribute(ATTR_TYPE, String.valueOf(dep.getValue().getSymbol()));
        }

        xmlWriter.writeEndElement();
    }

    private void writeFeatures() throws XMLStreamException
    {
        xmlWriter.writeStartElement(TAG_FEATURES);
        {
            // Like the pre-Royale compiler,
            // Royale unconditionally writes out script dependencies.
            xmlWriter.writeEmptyElement(TAG_FEATURE_SCRIPT_DEPS);
            
            if (!swc.getComponents().isEmpty())
                xmlWriter.writeEmptyElement(TAG_FEATURE_COMPONENTS);
            
            if (!swc.getFiles().isEmpty())
                xmlWriter.writeEmptyElement(TAG_FEATURE_FILES);
        }
        xmlWriter.writeEndElement();
    }

    private void writeVersions() throws XMLStreamException
    {
        final ISWCVersion version = swc.getVersion();
        if (version == null)
            return;

        xmlWriter.writeStartElement(TAG_VERSIONS);

        // SWC version
        final String swcVersion = version.getSWCVersion();
        if (swcVersion != null)
        {
            xmlWriter.writeEmptyElement(TAG_SWC);
            xmlWriter.writeAttribute(ATTR_VERSION, swcVersion);
        }

        // Royale version
        final String royaleVersion = version.getRoyaleVersion();
        if (royaleVersion != null)
        {
            xmlWriter.writeEmptyElement(TAG_ROYALE);
            xmlWriter.writeAttribute(ATTR_VERSION, royaleVersion);

            final String royaleBuild = version.getRoyaleBuild();
            if (royaleBuild != null)
                xmlWriter.writeAttribute(ATTR_BUILD, royaleBuild);

            final String royaleMinSupportedVersion = version.getRoyaleMinSupportedVersion();
            if (royaleMinSupportedVersion != null)
                xmlWriter.writeAttribute(ATTR_MINIMUM_SUPPORTED_VERSION, royaleMinSupportedVersion);
        }

        // Compiler version
        final String compilerVersion = version.getCompilerVersion();
        if (compilerVersion != null)
        {
            xmlWriter.writeEmptyElement(TAG_COMPILER);
            
            final String compilerName = version.getCompilerName();
            if (compilerName != null)
                xmlWriter.writeAttribute(ATTR_NAME, compilerName);
            
            xmlWriter.writeAttribute(ATTR_VERSION, compilerVersion);

            final String compilerBuild = version.getCompilerBuild();
            if (compilerBuild != null)
                xmlWriter.writeAttribute(ATTR_BUILD, compilerBuild);
        }

        xmlWriter.writeEndElement();
    }
}
