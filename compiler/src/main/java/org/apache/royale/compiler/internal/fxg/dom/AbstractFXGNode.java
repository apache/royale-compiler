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

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGChildNodeNotSupportedProblem;
import org.apache.royale.compiler.problems.FXGInvalidChildNodeProblem;
import org.apache.royale.compiler.problems.FXGInvalidNodeAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A helper class that serves as the base implementation of IFXGNode. Subclasses
 * can delegate to this class to handle unknown attributes or children.
 */
public abstract class AbstractFXGNode implements IFXGNode
{   
    protected IFXGNode documentNode;
    protected String uri;
    protected int startLine;
    protected int startColumn;
    protected int endLine;
    protected int endColumn;

    public static final double ALPHA_MIN_INCLUSIVE = 0.0;
    public static final double ALPHA_MAX_INCLUSIVE = 1.0;
    public static final int COLOR_BLACK = 0xFF000000;
    public static final int COLOR_WHITE = 0xFFFFFFFF;
    public static final int COLOR_RED = 0xFFFF0000;
    public static final int GRADIENT_ENTRIES_MAX_INCLUSIVE = 15;
    public static final double EPSILON = 0.00001;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an FXG child node to this node.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
    	//Exception:Child node {0} is not supported by node {1}.
        if (child == null) 
        {
            problems.add(new FXGChildNodeNotSupportedProblem(getDocumentPath(), getStartLine(), getStartColumn(), getNodeName()));
        }
        else
        {
            problems.add(new FXGInvalidChildNodeProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn(), child.getNodeName(), getNodeName()));            
        }
    }
    
    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        return children;
    }
    /**
     * Sets an FXG attribute on this FXG node.
     * 
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
    	if (isVersionGreaterThanCompiler())
        {
            // Warning: Minor version of this FXG file is greater than minor
            // version supported by this compiler. Log a warning for an unknown
            // attribute or an attribute with values out of range.
    	    
    	    //TODO FXGLOG
    	    //FXGLog.getLogger().log(IFXGLogger.WARN, "UnknownNodeAttribute", null, getDocumentName(), startLine, startColumn);
        }
        else
        {
            // Exception:Attribute {0} not supported by node {1}.
            problems.add(new FXGInvalidNodeAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn(), name, getNodeName()));
        }
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
     * @return - the path of the FXG file being processed.
     */
    @Override
    public String getDocumentPath()
    {
        return ((GraphicNode)this.getDocumentNode()).getDocumentPath();
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
    
    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    /**
     * @return - true if version of the FXG file is greater than the compiler
     * version. false otherwise.
     */
    public boolean isVersionGreaterThanCompiler()
    {
        return ((GraphicNode)this.documentNode).isVersionGreaterThanCompiler();
    }

    /**
     * 
     * @return the file version
     */
    public FXGVersion getFileVersion()
    {
        return ((GraphicNode)this.documentNode).getVersion();
    }
   
}
