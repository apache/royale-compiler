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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.targets.LinkageChecker;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildReportProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;

/**
 * Class to write a {@link DependencyGraph} as a yed-format graphml file.
 * <p>
 * This graph will walk through the graph from the {@link ICompilationUnit} stored in roots 
 * and construct a graphml report from the edges.
 * <p>
 * In yed, the edges will be visually distinguishable from each other:
 * <ul>
 * <li>If a dependency contains inheritance, the edge in the graphml report will be solid</li>
 * <li>If a dependency is signature/namespace but is not an inheritance relationship, 
 *  the edge in the graphml report will be dashed</li>
 * <li>If a dependency only contains an expression dependency, the edge will be dotted</li>
 * </ul>
 */
public class GraphMLWriter extends XMLGraphWriter implements IReportWriter
{
    
    /**
     * GraphMLWriter constructor
     * 
     * @param graph A {@link DependencyGraph} that this class will report on
     * @param roots A list of {@link ICompilationUnit} that the graph walker will start on. 
     *  Only units that are reachable from these units will be reported on.
     * @param linkageChecker class to check the linkage of compilation units.  
     * May not be null.
     */
    public GraphMLWriter(DependencyGraph graph, Collection<ICompilationUnit> roots, 
            boolean useExternalDependencies,
            LinkageChecker linkageChecker)
    {
        super(graph, roots);
        
        assert linkageChecker != null : "linkageChecker may not be null";
        
        this.useExternalDependencies = useExternalDependencies;
        this.linkageChecker = linkageChecker;
    }
    
    private boolean useExternalDependencies;
    private String yNSuri;
    private LinkageChecker linkageChecker;

    /**
     * A helper function that will read the {@link DependencyGraph} that was stored in this class and construct
     * a {@link Element} of type "graph" by walking through the graph from the root nodes.
     * <p>
     * Do not call this while threads are still running. This function reads the bytes from each {@link ICompilationUnit}
     * in order to find the ABC byte size of each unit. If the compilation threads are still running, you might get an
     * exception or an invalid byte size.
     * 
     * @return a {@link Element} of type "graph" that represents the contents of the {@link DependencyGraph}
     */
    private Element readGraph() throws InterruptedException {
        //reads a graph and its set of edges and stores them in a few arraylists
        
        Element graphTag = doc.createElement("graph");
        Map<ICompilationUnit, Integer> bytesChanged = InvalidationBytesCalculator.calculateBytesChanged(roots);
        Map<ICompilationUnit, Integer> totalBytesChanged = InvalidationBytesCalculator.calculateTotalInvalidatedBytesChanged(roots);
        
        
        ArrayList<ICompilationUnit> visibleVertices;
        if(!useExternalDependencies)
        {
            visibleVertices = new ArrayList<ICompilationUnit>(readVisibleInternalVertices(roots));    
        }
        else
        {
            visibleVertices = new ArrayList<ICompilationUnit>(readVisibleExternalVertices(roots));
        }
        int nodeIndex = 0;
        
        /**
         * Wad Class just to sort the edges and dependencies
         */
        class EdgePair {
            int fromIndex;
            int toIndex;
            DependencyTypeSet typeSet;
            public EdgePair(int fromIndex, int toIndex, DependencyTypeSet typeSet)
            {
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
                this.typeSet = typeSet;
            }
        }
        
        List<EdgePair> edges = new ArrayList<EdgePair>();
        
        for (int i = 0; i < visibleVertices.size(); i++)
        {
            ICompilationUnit vertex = visibleVertices.get(i);
            Element nodeTag = doc.createElement("node");
            nodeTag.setAttribute("id", "n" + Integer.toString(nodeIndex));

            String labelName = vertex.getName();
            String label = labelName + "[" + bytesChanged.get(vertex) + " bytes | " + totalBytesChanged.get(vertex) + " total invalidated bytes]";

            nodeTag.appendChild(createNodeDataTag(label, isExternal(vertex)));
            graphTag.appendChild(nodeTag);
            if (useExternalDependencies || !isExternal(vertex))
            {
                Collection<ICompilationUnit> dependentUnits = graph.getDirectDependencies(vertex);
                for (ICompilationUnit toVertex : dependentUnits)
                {
                    edges.add(new EdgePair(i, visibleVertices.indexOf(toVertex), graph.getDependencyTypes(vertex, toVertex)));
                }
            }
            nodeIndex++;
        }
        
        int edgeIndex = 0;
        for (EdgePair edge : edges)
        {
            int from = edge.fromIndex;
            int to = edge.toIndex;
            assert from > -1;
            assert to > -1;
            
            Element edgeTag = doc.createElement("edge");
            edgeTag.setAttribute("id", "e" + Integer.toString(edgeIndex));
            edgeTag.setAttribute("source", "n" + Integer.toString(from));
            edgeTag.setAttribute("target", "n" + Integer.toString(to));
            
            edgeTag.appendChild(createEdgeDataTag(edge.typeSet));
            graphTag.appendChild(edgeTag);
            edgeIndex++;
        }
        
        //tags.addAll(readGraphEdges(level, visibleVertices));*/
        return graphTag;
    }

    /**
     * Test is the compilation unit is external or should be linked into the
     * application.
     * 
     * @param vertex the compilation unit.
     * @return true if the compilation should NOT be linked into the application,
     * false otherwise.
     * @throws InterruptedException
     */
    private boolean isExternal(ICompilationUnit vertex) throws InterruptedException
    {
        if (vertex.getCompilationUnitType() == UnitType.SWC_UNIT &&
            linkageChecker.isExternal(vertex))
        {
            return true;
        }
        
        return false;
    }

    /**
     * Helper function to create a {@link Element} representing a node of {@link ICompilationUnit} in the {@link DependencyGraph}.
     * 
     * @param label A {@link String} representing the compilation unit. This will be displayed on the node in yed when the report is opened.
     * @param external A {@link boolean} representing whether this node is an external compilation unit or an internal one. The color of the node
     *  depends on this.
     * @return A {@link Element} representing a node of {@link ICompilationUnit} in the {@link DependencyGraph}
     */
    private Element createNodeDataTag(String label, boolean external)
    {
        Element dataTag = doc.createElement("data");
        dataTag.setAttribute("key", "d0");
        Element shapeNodeTag = doc.createElementNS(yNSuri, "y:ShapeNode");
        
        String colorString = external ? "#FFCC00": "#CCCCFF";
        
        Element fillTag = doc.createElementNS(yNSuri, "y:Fill");
        fillTag.setAttribute("color", colorString);
        fillTag.setAttribute("transparent", "false");
        
        Element labelTag = doc.createElementNS(yNSuri, "y:NodeLabel");
        labelTag.setTextContent(label);
        
        shapeNodeTag.appendChild(fillTag);
        shapeNodeTag.appendChild(labelTag);
        dataTag.appendChild(shapeNodeTag);
        return dataTag;
    }
    
    /**
     * Helper function to create a {@link Element} representing a dependency
     * based on a a {@link DependencyTypeSet} in the {@link DependencyGraph}.
     * 
     * @param edge An {@link DependencyTypeSet} that will be encoded in a graphml edge tag.
     * @return A {@link Element} representing a dependency in the {@link DependencyGraph}
     */
    private Element createEdgeDataTag(DependencyTypeSet typeSet)
    {
        Element dataTag = doc.createElement("data");
        dataTag.setAttribute("key", "d1");
        Element polyLineEdgeTag = doc.createElementNS(yNSuri, "y:PolyLineEdge");
        
        String lineStyle;
        if (DependencyType.INHERITANCE.existsIn(typeSet))
        {
            lineStyle = "solid";
        } 
        else if(DependencyType.NAMESPACE.existsIn(typeSet) || DependencyType.SIGNATURE.existsIn(typeSet))
        {
            lineStyle = "dashed";
        }
        else
        {
            lineStyle = "dotted";
        }
        
        Element lineStyleTag = doc.createElementNS(yNSuri, "y:LineStyle");
        lineStyleTag.setAttribute("type", lineStyle);
        
        Element arrowsTag = doc.createElementNS(yNSuri, "y:Arrows");
        arrowsTag.setAttribute("target", "standard");
        arrowsTag.setAttribute("source", "none");
        
        Element labelTag = doc.createElementNS(yNSuri, "y:EdgeLabel");
        labelTag.setTextContent(DependencyType.getTypeString(typeSet));
        
        polyLineEdgeTag.appendChild(lineStyleTag);
        polyLineEdgeTag.appendChild(arrowsTag);
        polyLineEdgeTag.appendChild(labelTag);
        dataTag.appendChild(polyLineEdgeTag);
        return dataTag;
    }

    /**
     * A helper function that will walk through the {@link DependencyGraph} and return a Set of all {@link ICompilationUnit}
     * that is visible from the roots.
     * @param roots The The {@link Collection} of root {@link ICompilationUnit} that this function will walk through
     * @return A {@link Set} of visible {@link ICompilationUnit} from the roots
     */
    private Set<ICompilationUnit> readVisibleExternalVertices(Collection<ICompilationUnit> roots)
    {
        Set<ICompilationUnit> dependentVertices = new HashSet<ICompilationUnit>();
        Stack<ICompilationUnit> unsearched = new Stack<ICompilationUnit>();
        unsearched.addAll(roots);
        
        while (!unsearched.isEmpty())
        {
            unsearched.addAll(readDependencies(dependentVertices, unsearched.pop()));
        }
        
        return dependentVertices;
    }
    
    /**
     * A helper function that will walk through the {@link DependencyGraph} and return a Set of {@link ICompilationUnit}
     * that are either internal and visible from the root units OR external and a direct dependency of a visible internal node.
     * 
     * @param roots The {@link Collection} of root {@link ICompilationUnit} that this function will walk through
     * @return A {@link Set} of visible internal {@link ICompilationUnit} or direct external dependencies of one
     * @throws InterruptedException 
     */
    private Set<ICompilationUnit> readVisibleInternalVertices(Collection<ICompilationUnit> roots) throws InterruptedException
    {
        Set<ICompilationUnit> dependentVertices = new HashSet<ICompilationUnit>();
        List<ICompilationUnit> internalVertices = new ArrayList<ICompilationUnit>();
        for (ICompilationUnit vertex : roots)
        {
            if(!isExternal(vertex))
                internalVertices.add(vertex);
        }
        for (ICompilationUnit vertex : internalVertices)
        {
            dependentVertices.addAll(readDependencies(dependentVertices, vertex));
        }
        return dependentVertices;
    }
    
    
    /**
     * A helper function that will return a set of {@link ICompliationUnit} are direct dependents of the vertex that are not
     * in the closed set dependentVertices
     * @param dependentVertices A closed {@link Set} of {@link ICompilationUnit}
     * @param vertex The {@link ICompilationUnit} that this function will find direct depedencies on
     * @return A {@link Set} of new {@link ICompilationUnit} that are direct dependencies of vertex but not in the closed set.
     */
    private Set<ICompilationUnit> readDependencies(Set<ICompilationUnit> dependentVertices, ICompilationUnit vertex)
    {
        dependentVertices.add(vertex);
        Set<ICompilationUnit> newDependencies = new HashSet<ICompilationUnit>();
        Set<ICompilationUnit> dependencies = graph.getDirectDependencies(vertex);
        for (ICompilationUnit dependency : dependencies)
        {
            if(!dependentVertices.contains(dependency))
            {
                newDependencies.add(dependency);
            }
        }
        return newDependencies;
    }
    
    @Override
    public void writeToStream(OutputStream outStream, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        String uri_xmlns = "http://graphml.graphdrawing.org/xmlns";
        String defaultns = "http://www.w3.org/2000/xmlns/";
        String uri_xsi = "http://www.w3.org/2001/XMLSchema-instance";
        String uri_yed = "http://www.yworks.com/xml/yed/3";
        String uri_schema = "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd";
        yNSuri = "http://www.yworks.com/xml/graphml";
        
        Element graphmlTag = doc.createElement("graphml");
        graphmlTag.setAttribute("xmlns", uri_xmlns);
        graphmlTag.setAttributeNS(defaultns, "xmlns:xsi", uri_xsi);
        graphmlTag.setAttributeNS(defaultns, "xmlns:y", yNSuri);
        graphmlTag.setAttributeNS(defaultns, "xmlns:yed", uri_yed);
        graphmlTag.setAttributeNS(uri_xsi, "xsi:schemaLocation", uri_schema);
        
        Element key0 = doc.createElement("key");
        key0.setAttribute("for", "node");
        key0.setAttribute("id", "d0");
        key0.setAttribute("yfiles.type", "nodegraphics");
        
        Element key1 = doc.createElement("key");
        key1.setAttribute("for", "edge");
        key1.setAttribute("id", "d1");
        key1.setAttribute("yfiles.type", "edgegraphics");
        
        graphmlTag.appendChild(key0);
        graphmlTag.appendChild(key1);
        
        graphmlTag.appendChild(readGraph());
        doc.appendChild(graphmlTag);
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
