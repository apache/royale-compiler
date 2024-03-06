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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.royale.compiler.mxml.IMXMLManifestManager;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ManifestInconsistentComponentEntriesProblem;
import org.apache.royale.compiler.problems.ManifestParsingProblem;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.swc.ISWCComponent;
import org.apache.royale.swc.ISWC;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Each {@code RoyaleProject} has an {@code MXMLManifestManager} to resolve MXML tags to ActionScript classes,
 * using the <component> tags inside SWCs' catalog.xml files and any manifest files associated
 * with the project.
 * This manager must be recreated whenever the library path, or a manifest file, changes.
 */
public class MXMLManifestManager implements IMXMLManifestManager
{
    /**
     * Helper method to get the class name from the
     * class info. Takes care of checking if the class
     * info is null.
     * 
     * @param classInfo may be null.
     * @return The name of the class if classInfo is not null,
     * null otherwise.
     */
    static String getClassName(ClassInfo classInfo)
    {
        return classInfo != null ? classInfo.className : null;
    }
    
    /**
     * Constructor.
     * 
     * @param project The {@code RoyaleProject} for which this manifest manager
     * provides MXML-tag-to-ActionScript-classname mappings.
     */
    public MXMLManifestManager(RoyaleProject project)
    {
        // Loop over all the SWCs on the library path.
        for (ISWC swc : project.getLibraries())
        {
            addSWC(swc);
        }
        
        // Loop over all the manifest files that MXML namespace URIs are mapped to.
        for (IMXMLNamespaceMapping namespaceMapping : project.getNamespaceMappings())
        {
            addManifest(project, namespaceMapping.getURI(), namespaceMapping.getManifestFileName());
        }
    }
    
    // Maps an MXML tag name to a fully-qualified classname
    // such as "spark.components.Button"; null values in this map
    // indicate that there were inconsistent manifest entries
    // for the tag name.
    private Map<XMLName, ClassInfo> lookupMap = new HashMap<XMLName, ClassInfo>();
    
    // Maps a tag name to a fully-qualified class name. This map only contains
    // manifest entries where 'lookupOnly' is true. This is only really needed
    // for manifests specified in the -include-namespace option.
    private Map<XMLName, String> lookupOnlyMap = new HashMap<XMLName, String>();
    
    /**
     * Maps a fully qualified classname such as "spark.components.Button" to
     * an MXML tag name such as "&lt;s:Button&gt;".
     */
    private SetMultimap<String, XMLName> reverseLookupMap = HashMultimap.<String, XMLName>create();

    private Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
    
    //
    // Object overrides
    //
    
    /**
     * For debugging only.
     * Lists all of the MXML-tag-to-ActionScript-classname mappings,
     * in sorted order. Useful for debugging manifest problems.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        TreeSet<XMLName> keys = new TreeSet<XMLName>(lookupMap.keySet()); 
        for (XMLName key : keys)
        {
            sb.append(key);
            sb.append(" -> ");
            sb.append(lookupMap.get(key));
            sb.append(", lookupOnly = ");
            sb.append(isLookupOnly(key));
            sb.append('\n');
        }
        
        return sb.toString();
    }
    
    //
    // IMXMLManifestManager implementations
    //
    
    @Override
    public String resolve(XMLName tagName)
    {
        return getClassName(lookupMap.get(tagName));
    }
    
    @Override
    public boolean isLookupOnly(XMLName tagName)
    {
        return lookupOnlyMap.get(tagName) != null;
    }
    
    @Override
    public Collection<XMLName> getTagNamesForClass(String className)
    {
        Collection<XMLName> result = reverseLookupMap.get(className);
        if (result == null)
            return Collections.emptySet();
        else
            return Collections.unmodifiableCollection(result);
    }
    
    @Override
    public Collection<String> getQualifiedNamesForNamespaces(Set<String> namespaceURIs, 
            boolean manifestEntriesOnly)
    {
        HashSet<String> qualifiedNames = new HashSet<String>();
        for (Map.Entry<XMLName, ClassInfo> entry : lookupMap.entrySet())
        {
            if (namespaceURIs.contains(entry.getKey().getXMLNamespace()))
            {
                ClassInfo classInfo = entry.getValue();
                if (classInfo != null && 
                    (!manifestEntriesOnly || 
                     (manifestEntriesOnly && classInfo.fromManifest)))
                {
                    qualifiedNames.add(classInfo.className);                                            
                }
            }
        }
        return qualifiedNames;
    }
    
    //
    // Other methods
    //
    
    private void addSWC(ISWC swc)
    {
        File swcFile = swc.getSWCFile();
        
        // Loop over all the <component> tags in the catalog.xml file
        // inside each SWC.
        for (ISWCComponent component : swc.getComponents())
        {
            String uri = component.getURI();
            String name = component.getName();
            XMLName tagName = new XMLName(uri, name);
            String qname = component.getQName();
            
            // Add the mapping info in the <component> tag
            // to the maps of this manifest manager.
            add(tagName, qname, uri, swcFile.getAbsolutePath(), false);
        }        
    }
    
    private void addManifest(RoyaleProject project, String uri, String manifestFileName)
    {
        Document manifestDocument = null;
        
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLManifestManager waiting for lock in addManifest");
        IFileSpecification manifestFileSpec = project.getWorkspace().getFileSpecification(manifestFileName);
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLManifestManager done with lock in addManifest");
        
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);
            documentBuilderFactory.setCoalescing(true);
            documentBuilderFactory.setIgnoringComments(true);
            manifestDocument = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(manifestFileSpec.createReader()));
        }
        catch (SAXException e)
        {
            ManifestParsingProblem problem = new ManifestParsingProblem(e.getMessage(), uri, manifestFileName);
            problems.add(problem);
            return;
        }
        catch (Exception e)
        {
            ManifestParsingProblem problem = new ManifestParsingProblem("Unknown parsing problem", uri, manifestFileName);
            problems.add(problem);
            return;
        }

        if (manifestDocument != null)
        {
            NodeList components = manifestDocument.getElementsByTagName("component");
            for (int i = 0; i < components.getLength(); i++)
            {
                Element component = (Element)components.item(i);
                if (component != null)
                {
                    String id = component.getAttribute("id");
                    if (id != null)
                    {
                        // TODO Why are we checking for dots in the tag name?
                        int lastDot = id.lastIndexOf(".");
                        if (lastDot != -1)
                            id = id.substring(lastDot + 1);
                    }
                    
                    XMLName tagName = new XMLName(uri, id);
                    
                    String className = component.getAttribute("class");
                    if (className != null)
                        className = className.replaceAll("/", ".");
                    
                    String lookupOnlyStr = component.getAttribute("lookupOnly");
                    boolean lookupOnly = lookupOnlyStr == null ? false : Boolean.valueOf(lookupOnlyStr).booleanValue();
                    
                    if (id != null && className != null)
                    {
                        add(tagName, className, uri, manifestFileName, true);

                        if (lookupOnly)
                            addLookupOnly(tagName, className);
                    }
                }
            }
        }
    }
    
    /**
     * Adds a mapping to this manifest manager.
     * 
     * @param tagName An {@code XMLName} for an MXML tag.
     * 
     * @param className The fully-qualified ActionScript classname
     * to which this tag maps.
     * 
     * @param file The SWC file in which the mapping was declared.
     */
    private void add(XMLName tagName, String className, String uri,
                     String fileName, boolean fromManifest)
    {
        if (!lookupMap.containsKey(tagName))
        {
            // The first manifest entry associating a className
            // with this tagName is being added.
            // The ClassInfo keeps track of whether it came
            // from the catalog of a SWC or from a manifest file.
            lookupMap.put(tagName, new ClassInfo(className, fromManifest));
            reverseLookupMap.put(className, tagName);
            return;
        }
        else
        {
            // A particular mapping might come first from a SWC and later
            // from a manifest. In that case, change the fromManifest flag to true;
            // otherwise getQualifiedNamesForNamespaces() won't return the
            // right names and COMPC won't link in all the classes that
            // were in manifests.
            ClassInfo classInfo = lookupMap.get(tagName);
            if (fromManifest)
            {
                boolean wasFromManifest = classInfo.fromManifest;
                classInfo.fromManifest = true;
                if (!wasFromManifest)
                {
                    // manifest takes precedence over SWC, and a manifest uses a
                    // different class name than a SWC for the same URI, that is
                    // not treated as an error.
                    // however, if two manifests have different classes names
                    // for the same URI, that will be treated as an error.
                    classInfo.className = className;
                    return;
                }
            }
        }
        
        // If subsequent classNames added for this tagName aren't consistent,
        // null out the className in this map so that the tag won't
        // resolve to a class.
        String oldClassName = getClassName(lookupMap.get(tagName)); 
        if (className.equals(oldClassName))
        {
            return;
        }
        
        lookupMap.put(tagName, null);
        reverseLookupMap.remove(oldClassName, tagName);
        
        ICompilerProblem problem = new ManifestInconsistentComponentEntriesProblem(tagName.getName(), uri, fileName);
        problems.add(problem);
    }
    
    /**
     * Adds a 'lookupOnly' mapping to this manifest manager.
     * 
     * @param tagName An {@code XMLName} for an MXML tag.
     * 
     * @param className The fully-qualified ActionScript classname
     * to which this tag maps.
     */
    private void addLookupOnly(XMLName tagName, String className)
    {
        if (!lookupOnlyMap.containsKey(tagName))
        {
            // The first manifest entry associating a className
            // with this tagName is being added.
            lookupOnlyMap.put(tagName, className);
        }
    }

    public void collectProblems(Collection<ICompilerProblem> problems)
    {
        problems.addAll(getProblems());
    }
    
    /**
     * Looks for inconsistent manifest mappings and returns
     * a collection of compiler problems for them.
     * 
     * @return A collection of {@code ICompilerProblem} objects.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }
    
    /**
     * This inner class stores information about a class in a namespace mapping.
     */
    private static class ClassInfo
    {
        /**
         * Constructor.
         * 
         * @param className fully qualified class name.
         * @param fromManifest true if the class name came from a manifest
         * file entry, false otherwise.
         */
        ClassInfo(String className, boolean fromManifest)
        {
            this.className = className;
            this.fromManifest = fromManifest;
        }
        
        public String className;
        public boolean fromManifest;
    }
}
