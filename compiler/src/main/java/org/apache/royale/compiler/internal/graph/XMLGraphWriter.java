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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;

/** 
 * The base class of {@link IReportWriter} that transforms a {@link DependencyGraph} to output in XML
 */
public abstract class XMLGraphWriter implements IReportWriter
{
    protected DependencyGraph graph;
    protected Collection<ICompilationUnit> roots;
    protected Document doc;
    
    @Override
    public abstract void writeToStream(OutputStream outStream, Collection<ICompilerProblem> problems) throws InterruptedException;
    
    /**
     * Writes this class's report {@link Document} to the {@link OutputStream}
     * 
     * @param outStream The {@link OutputStream} that will be written to.
     * @throws IOException
     */
    protected void writeReport(OutputStream outStream) throws TransformerException
    {
        try
        {
            TransformerFactory tranFactory = TransformerFactory.newInstance(); 
            Transformer aTransformer = tranFactory.newTransformer(); 
            aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Source src = new DOMSource(doc);
            Result dest = new StreamResult(outStream); 
            aTransformer.transform(src, dest);
        }
        catch(TransformerConfigurationException e)
        {
            assert false : "Programmer Error";
        }
    }
    
    /**
     * Helper function to help format a qname {@link String} into the XML style of prefix:name that
     * the old compiler used.
     * <p>
     * This currently just transforms the last '.' into a ':' if one exists.
     * @param qname A {@link String} qname of format "package.name"
     * @return A {@link String} qname of format "package:name"
     */
    protected String formatXMLStyleQName(String qname)
    {
        StringBuilder sb = new StringBuilder(qname);
        int lastDotIndex = qname.lastIndexOf('.');
        if(lastDotIndex > -1)
        {
            sb.setCharAt(lastDotIndex, ':');
        }
        
        return sb.toString();
    }
    
    /**
     * Constructor
     * @param graph A dependency graph for this graph writer
     * @param roots The root compilation nodes to be written
     */
    public XMLGraphWriter(DependencyGraph graph, Collection<ICompilationUnit> roots) 
    {
        this.graph = graph;
        this.roots = roots;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        DocumentBuilder parser;
        
        try
        {
            parser = factory.newDocumentBuilder();
            //Create blank DOM Document
            doc = parser.newDocument();
        }
        catch (ParserConfigurationException e)
        {
            assert false : "Programmer error";
        }
    }
}
