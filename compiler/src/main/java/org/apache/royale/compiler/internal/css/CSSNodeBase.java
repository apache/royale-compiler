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

package org.apache.royale.compiler.internal.css;

import static com.google.common.collect.Collections2.transform;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.css.ICSSNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * Base class for all the CSS DOM objects.
 */
class CSSNodeBase extends SourceLocation implements ICSSNode
{

    private static final int NOT_SET = -1;

    /**
     * Initialize the node with source location information.
     * 
     * @param tree AST
     * @param tokenStream token stream used to compute source location
     * @param type CSS DOM tree node type
     */
    protected CSSNodeBase(final CommonTree tree, final TokenStream tokenStream, final CSSModelTreeType type)
    {
        // Setup start, end, line, column, source location data.
        if (tree != null && tokenStream != null)
            setSourceLocation(tree, tokenStream);

        // Tree node type.
        this.type = type;

        // Children nodes.
        this.children = new ArrayList<ICSSNode>();
    }

    /**
     * CSS node type.
     */
    private final CSSModelTreeType type;

    /**
     * A list of children nodes. The sub-classes can add nodes to this list in
     * their constructors.
     */
    protected final List<ICSSNode> children;

    /**
     * Parent node.
     */
    protected ICSSNode parent;

    /**
     * Initialize source location information.
     * 
     * @param tree AST node.
     * @param tokenStream Token stream used to compute source location.
     */
    private void setSourceLocation(final CommonTree tree, TokenStream tokenStream)
    {
        int line = NOT_SET;
        int column = NOT_SET;
        int endLine = NOT_SET;
        int endColumn = NOT_SET;
        int start = NOT_SET;
        int stop = NOT_SET;

        // compute source location
        final int tokenStartIndex = tree.getTokenStartIndex();
        if (tokenStartIndex >= 0)
        {
            final CommonToken startToken =
                    (CommonToken)tokenStream.get(tokenStartIndex);
            start = startToken.getStartIndex();
            line = startToken.getLine();
            column = startToken.getCharPositionInLine();
        }

        final int tokenStopIndex = tree.getTokenStopIndex();
        if (tokenStopIndex >= 0)
        {
            final CommonToken stopToken =
                    (CommonToken)tokenStream.get(tokenStopIndex);
            stop = stopToken.getStopIndex() + 1;
            endLine = stopToken.getLine();
            endColumn = stopToken.getCharPositionInLine() + stopToken.getText().length();
        }

        if (endLine == NOT_SET)
        {
            endLine = line;
            if (endColumn == NOT_SET)
            {
                endColumn = column;
            }
        }

        setStart(start);
        setEnd(stop);
        setLine(line);
        setColumn(column);
        setEndLine(endLine);
        setEndColumn(endColumn);
        setSourcePath(tokenStream.getSourceName());
    }

    /**
     * Recursively print out the text representation of the tree.
     * 
     * @return Tree structure in text.
     */
    @Override
    public String toStringTree()
    {
        if (children.isEmpty())
        {
            return type.name();
        }
        else
        {
            return new StringBuilder()
                    .append("( ")
                    .append(type.name())
                    .append(" ")
                    .append(Joiner.on(" ").skipNulls().join(transform(children, NODE_TO_TREE_TEXT)))
                    .append(" )")
                    .toString();
        }
    }

    /**
     * Function to transform an {@link ICSSNode} to its
     * {@link ICSSNode#toStringTree()} representation.
     */
    private static final Function<ICSSNode, String> NODE_TO_TREE_TEXT = new Function<ICSSNode, String>()
    {
        @Override
        public String apply(ICSSNode node)
        {
            return node.toStringTree();
        }
    };

    @Override
    public int getArity()
    {
        return children.size();
    }

    @Override
    public ICSSNode getNthChild(int index)
    {
        return children.get(index);
    }

    @Override
    public CSSModelTreeType getOperator()
    {
        return type;
    }

    @Override
    public ICSSNode getParent()
    {
        return parent;
    }

    /**
     * Set the parent node. Used during parsing.
     * 
     * @param parent parent node
     */
    public void setParent(CSSNodeBase parent)
    {
        this.parent = parent;
    }
}
