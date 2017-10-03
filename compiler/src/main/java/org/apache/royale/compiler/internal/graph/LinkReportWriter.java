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

package org.apache.royale.compiler.internal.graph;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.targets.LinkageChecker;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildReportProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;

/**
 * Class to write a {@link DependencyGraph} as a XML link report.
 * <p>
 * This graph will walk through the graph from the {@link ICompilationUnit} stored in roots 
 * and construct a XML link report.
 * <p>
 * This has mostly been kept consistent with the link report from the old flex compiler, 
 * however a few differences remain. We may link different things from the framework, 
 * or the old compiler might have failed to find certain dependencies.
 * <p>
 * Without a concrete spec, this class is still a work in progress
 */
public class LinkReportWriter extends XMLGraphWriter implements IReportWriter
{   
    /**
     * A {@link Comparator} that sorts qnames {@link String} based on alphabetical order.
     * <p>
     * This will put qnames with a package definition before ones that don't.
     * Otherwise, the ordering will be alphabetical.
     */
    public static class QNameComparator implements Comparator<String>
    {
        @Override
        public int compare(String a, String b)
        {
            boolean aSingleNamed = (a.lastIndexOf('.')) == -1;
            boolean bSingleNamed = (b.lastIndexOf('.')) == -1;
            
            if (aSingleNamed == bSingleNamed)
                return a.compareTo(b);
            else
            {
                return aSingleNamed ? 1 : -1;
            }
        }
    }

    public LinkReportWriter(DependencyGraph graph, List<ICompilationUnit> roots,
            LinkageChecker linkageChecker)
    {
        super(graph, roots);
        this.linkageChecker = linkageChecker;
    }
    
    private final LinkageChecker linkageChecker;
    
    /**
     * A helper function that adds the dependencies to a script tag in the link report
     * <p>
     * Prerequist dependencies using tag "&lt;pre&gt;" (those that contain inheritances) 
     * will be written before other dependencies using tag "&lt;dep&gt;".
     * 
     * @param script The script that the dependencies will be added to
     * @param dependencies A {@link Map} of current dependencies to an {@link Integer} representing the {@link DependencyType}
     */

    private void addScriptDependencies(Element script, Map<String, DependencyTypeSet> dependencies)
    {
        List<Element> prereqNodes = new ArrayList<Element>();
        List<Element> dependencyNodes = new ArrayList<Element>();
        
        List<String> dependencyQNames = new ArrayList<String>(dependencies.keySet());
        Collections.sort(dependencyQNames, new QNameComparator());
        for (String qname: dependencyQNames)
        {
            if (qname.isEmpty())
            {
                continue;
            }
            String xmlStyleQName = formatXMLStyleQName(qname);
            DependencyTypeSet dependencySet = dependencies.get(qname);
            String typeString = DependencyType.getTypeString(dependencySet);
            if(DependencyType.INHERITANCE.existsIn(dependencySet))
            {
                Element preNode = doc.createElement("pre");
                preNode.setAttribute("id", xmlStyleQName);
                preNode.setAttribute("type", typeString);

                prereqNodes.add(preNode);
            }
            else
            {
                Element preNode = doc.createElement("dep");
                preNode.setAttribute("id", xmlStyleQName);
                preNode.setAttribute("type", typeString);

                dependencyNodes.add(preNode);
            }
        }
        
        for (Element tag : prereqNodes)
        {
            script.appendChild(tag);
        }
        for (Element tag : dependencyNodes)
        {
            script.appendChild(tag);
        }
    }

    /**
     * A helper function that creates a {@link Element} of type "script" representing an internal {@link ICompilationUnit} 
     * with its definitions and dependencies.
     * <p>
     * The dependencies tags will come before the definitions of tags "&lt;def&gt;"
     * <p>
     * This should only be called after the threads are done. The function gets definition names from a 
     * compilation unit by requesting a {@link IFileScopeRequestResult} from it and using the filescopes's
     * externally visible definitions.
     * @param bytecodeSize The size of the ABC byte code of this {@link ICompilationUnit} in number of bytes.
     * @param cu The {@link ICompilationUnit} to be encoded as a script tag
     * @return A {@link Element} that encodes the compilation unit and its definitions and dependencies.
     * @throws InterruptedException
     */
    
    private Element createScriptTag(int bytecodeSize, ICompilationUnit cu) throws InterruptedException
    {
        Element script = doc.createElement("script");
        script.setAttribute("name", cu.getName());
        
        script.setAttribute("mod", String.valueOf(cu.getSyntaxTreeRequest().get().getLastModified()));
        script.setAttribute("size", Integer.toString(bytecodeSize));
        
        Collection<IDefinition> definitions = cu.getFileScopeRequest().get().getExternallyVisibleDefinitions();
        List<String> defQNames = new ArrayList<String>();
        
        for (IDefinition definition : definitions)
        {
            String xmlStyleQName = formatXMLStyleQName(definition.getQualifiedName());
            defQNames.add(xmlStyleQName);
        }
        
        Collections.sort(defQNames, new QNameComparator());
        
        for (String definitionQName : defQNames)
        {
            Element definition = doc.createElement("def");
            script.appendChild(definition);
            definition.setAttribute("id", definitionQName);
        }
        return script;
    }

    /**
     * A helper function that reads an {@link ICompilationUnit}'s dependencies.
     * <p>
     * External definitions will be added to a set given in its parameters
     * @param externalDefs A {@link Set} that external definitions will be added to
     * @param cu The {@link ICompilationUnit} that is being read
     * @return A {@link Map} from the qnames of the dependencies to the dependency types
     * @throws InterruptedException 
     */
    private Map<String, DependencyTypeSet> readEdges(Set<String> externalDefs, ICompilationUnit cu) throws InterruptedException
    {
        Map<String, DependencyTypeSet> dependencies = new HashMap<String, DependencyTypeSet>();
        Collection<ICompilationUnit> dependentUnits = graph.getDirectDependencies(cu);
        for (ICompilationUnit dependentUnit: dependentUnits)
        {
            Map<String, DependencyTypeSet> edgeDeps = graph.getDependencySet(cu, dependentUnit);
            dependencies.putAll(edgeDeps);
            
            Collection<String> edgeQNames = edgeDeps.keySet();
            
            if (isLinkageExternal(dependentUnit))
            {
                externalDefs.addAll(edgeQNames);                    
            }
        }
        return dependencies;
    }
    
    /**
     * A helper function that will sort the root nodes in a reverse topological order and 
     * filter out all the internal {@link ICompilationUnit} that are visible.
     * @return A sorted {@link List} of {@link ICompilationUnit} that are both visible from the 
     * root nodes and internal to the project.
     * @throws InterruptedException 
     */
    private List<ICompilationUnit> extractSortedInternalUnits() throws InterruptedException
    {
        List<ICompilationUnit> internalUnits = new ArrayList<ICompilationUnit>();
        
        List<ICompilationUnit> vertices = graph.topologicalSort(roots);
        Collections.reverse(vertices);
        
        for (ICompilationUnit vertex : vertices)
        {
            if (!isLinkageExternal(vertex))
            {
                internalUnits.add(vertex);
            }
        }
        return internalUnits;
    }

    /**
     * Determine if a compilation unit should be linked into the target.
     * 
     * @param cu The compilation unit to test.
     * @param linkageChecker class to check the linkage of compilation units.  
     * @return true if the compilation unit's linkage is external, false
     * otherwise.
     * @throws InterruptedException 
     */
    private boolean isLinkageExternal(ICompilationUnit cu) throws InterruptedException
    {
        return (cu.getCompilationUnitType() == UnitType.SWC_UNIT &&
                linkageChecker.isExternal(cu));
    }
    
    @Override
    public void writeToStream(OutputStream outStream, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        List<ICompilationUnit> internalUnits = extractSortedInternalUnits();
        Map<ICompilationUnit, Integer> bytecodeSizes = InvalidationBytesCalculator.calculateBytesChanged(internalUnits);
        Set<String> externalDefs = new HashSet<String>();
        
        Element scripts = doc.createElement("scripts");
        
        for (ICompilationUnit cu : internalUnits)
        {
            Element script = createScriptTag(bytecodeSizes.get(cu), cu);            
            addScriptDependencies(script, readEdges(externalDefs, cu));
            scripts.appendChild(script);
        }
        
        Element externalDefinitions = doc.createElement("external-defs");
        List<String> extDefinitionList = new ArrayList<String>(externalDefs);
        Collections.sort(extDefinitionList, new QNameComparator());
        
        for (String ext: extDefinitionList)
        {
            Element extNode = doc.createElement("ext");
            extNode.setAttribute("id", formatXMLStyleQName(ext));
            externalDefinitions.appendChild(extNode);
        }
        
        Element report = doc.createElement("report");
        report.appendChild(scripts);
        report.appendChild(externalDefinitions);
        doc.appendChild(report);
        try 
        {
            writeReport(outStream);
        }
        catch (TransformerException e)
        {
            problems.add(new UnableToBuildReportProblem(e));
        }
    }
    
}
