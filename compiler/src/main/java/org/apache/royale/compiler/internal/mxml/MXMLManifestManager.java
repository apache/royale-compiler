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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.royale.compiler.mxml.IMXMLManifestManager;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ManifestProblem;
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
     * class info. Takes are of checking if the class
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
    
    // Maps an MXML tag name to a list of (qname, path) duples,
    // for reporting inconsistencies or duplications between manifests.
    private HashMap<XMLName, ArrayList<ProblemEntry>> problemMap =
        new HashMap<XMLName, ArrayList<ProblemEntry>>();
    
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
            add(tagName, qname, swcFile.getAbsolutePath(), false);
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
        catch (Exception e)
        {
            // TODO Report a problem.
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
                        add(tagName, className, manifestFileName, true);

                        if (lookupOnly)
                            addLookupOnly(tagName, className);
                    }
                }
            }
        }
        else
            System.out.println("Unable to parse " + manifestFileName);
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
    private void add(XMLName tagName, String className, String fileName,
                     boolean fromManifest)
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
            if (fromManifest)
                lookupMap.get(tagName).fromManifest = true;
        }
        
        // If subsequent classNames added for this tagName aren't consistent,
        // null out the className in this map so that the tag won't
        // resolve to a class.
        String oldClassName = getClassName(lookupMap.get(tagName)); 
        if (className.equals(oldClassName))
            return;
        
        lookupMap.put(tagName, null);
        reverseLookupMap.remove(oldClassName, tagName);
        
        // 
        ProblemEntry entry = new ProblemEntry(className, fileName);
        ArrayList<ProblemEntry> list = problemMap.get(tagName);
        if (list == null)
        {
            list = new ArrayList<ProblemEntry>();
            problemMap.put(tagName, list);
        }
        list.add(entry);
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
    
    /**
     * Looks for inconsistent manifest mappings and returns
     * a collection of compiler problems for them.
     * 
     * @return A collection of {@code ICompilerProblem} objects.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        
        // Search the lookupMap for null values, which indicate
        // an inconsistent tagName->className mapping.
        for (XMLName key : lookupMap.keySet())
        {
            if (lookupMap.get(key) == null)
            {
                // The corresponding entry in the problemMap
                // has information about all the mapping of that tagName.
                List<ProblemEntry> list = problemMap.get(key);
                ICompilerProblem problem = new ManifestProblem(list);
                problems.add(problem);
            }
        }
        
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
    
    /**
     * This inner class is a simple duple struct used to keep track
     * of all the manifest mappings for a particular tag.
     * For example, <whatever:Foo> might map to a.b.Foo in
     * X.swc and Y.swc but c.d.Foo in Z.swc.
     * We keep track of all of this so that we can create compiler
     * problems describing where the inconsistencies are.
     */
    private static class ProblemEntry
    {
        ProblemEntry(String className, String fileName)
        {
            this.className = className;
            this.fileName = fileName;
        }
        
        @SuppressWarnings("unused")
        public String className;
        
        @SuppressWarnings("unused")
        public String fileName;
    }
}
