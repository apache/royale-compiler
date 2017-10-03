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

package org.apache.royale.compiler.internal.fxg.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGInvalidNodeAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A special kind of relationship node that delegates the addition of child
 * nodes to another parent node (instead of adding them to itself). An example
 * of a delegate node is the fill child of a Rect component. In the snippet
 * below, a SolidColor fill is added directly to the Rect - the parent of the
 * fill property node.
 * 
 * <pre>
 * &lt;Rect width="20" height="20"&gt;
 *     &lt;fill&gt;
 *         &lt;SolidColor color="#FFCC00" /&gt;
 *     &lt;/fill&gt;
 * &lt;/Rect&gt;
 * </pre>
 */
public class DelegateNode implements IFXGNode
{
    protected String name;
    protected IFXGNode delegate;
    protected IFXGNode documentNode;
    protected String uri;
    protected int startLine;
    protected int startColumn;
    protected int endLine;
    protected int endColumn;

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getNodeName()
    {
        return name;
    }

    public void setDelegate(IFXGNode delegate, Collection<ICompilerProblem> problems)
    {
        this.delegate = delegate;
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an FXG child node to the delegate node.
     * 
     * @param child - a child FXG node to be added to the delegate node.
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        delegate.addChild(child, problems);
    }
    
    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        
        children.addAll(delegate.getChildren());
        return children;
    }

    /**
     * Sets an FXG attribute on the delegate node.
     * 
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        //Attribute {0} not supported by node {1}
        problems.add(new FXGInvalidNodeAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn(), name, getNodeName()));
    }

    /**
     * @return The root node of the FXG document.
     */
    @Override
    public IFXGNode getDocumentNode()
    {
        return documentNode;
    }

    /**
     * Establishes the root node of the FXG document containing this node.
     * @param root - the root node of the FXG document.
     */
    @Override
    public void setDocumentNode(IFXGNode root)
    {
        documentNode = root;
    }

    /**
     * return the namespace URI of this node.
     */
    @Override
    public String getNodeURI()
    {
        return uri;
    }

    /**
     * @param uri - the namespace URI of this node.
     */
    public void setNodeURI(String uri)
    {
        this.uri = uri;
    }

    /**
     * @return the line on which the node declaration started.
     */
    @Override
    public int getStartLine()
    {
        return startLine;
    }

    /**
     * @param line - the line on which the node declaration started.
     */
    @Override
    public void setStartLine(int line)
    {
        startLine = line;
    }

    /**
     * @return - the column on which the node declaration started.
     */
    @Override
    public int getStartColumn()
    {
        return startColumn;
    }

    /**
     * @param column - the line on which the node declaration started.
     */
    @Override
    public void setStartColumn(int column)
    {
        startColumn = column;
    }

    /**
     * @return the line on which the node declaration ended.
     */
    @Override
    public int getEndLine()
    {
        return endLine;
    }

    /**
     * @param line - the line on which the node declaration ended.
     */
    @Override
    public void setEndLine(int line)
    {
        endLine = line;
    }

    /**
     * @return - the column on which the node declaration ended.
     */
    @Override
    public int getEndColumn()
    {
        return endColumn;
    }

    /**
     * @param column the column on which the node declaration ended.
     */
    @Override
    public void setEndColumn(int column)
    {
        endColumn = column;
    }
    
    /**
     * @return - the path of the FXG file being processed.
     */
    @Override
    public String getDocumentPath()
    {
        return ((GraphicNode)this.getDocumentNode()).getDocumentPath();
    }
}
