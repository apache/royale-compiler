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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSFontFace;
import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import org.apache.royale.compiler.css.ICSSNode;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.problems.CSSParserProblem;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Implementation of a CSS model.
 */
public class CSSDocument extends CSSNodeBase implements ICSSDocument
{
    /** The short name for the default namespace is an empty string. */
    private static final String DEFAULT_NAMESPACE_SHORT_NAME = "";
    private static final int NOT_SET = -1;

    /**
     * Parse a CSS document into {@link ICSSDocument} model.
     * 
     * @param input ANTLR input stream. The {@code CharStream#getSourceName()}
     * must be implemented in order to make source location work.
     * @param problems Parsing problems will be aggregated in this collection.
     * @return CSS DOM object.
     */
    public static CSSDocument parse(final CharStream input, final Collection<ICompilerProblem> problems)
    {
        assert input != null : "CSS input can't be null";
        assert problems != null : "Problem collection can't be null";

        try
        {
            // parse and build tree
            final CSSLexer lexer = new CSSLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final CSSParser parser = new CSSParser(tokens);
            final CSSParser.stylesheet_return stylesheet = parser.stylesheet();
            // both lexer and parser woon't have problems until after
            // parser.stylesheet() returns
            problems.addAll(lexer.problems);
            problems.addAll(parser.problems);
            if (!hasErrors(lexer.problems) && !hasErrors(parser.problems))
            {
                CommonTree ast = (CommonTree)stylesheet.getTree();
                if (ast == null)
                {
                    // may be null if the input contains only comments -JT
                    // apache/royale-compiler#1218
                    ast = new CommonTree();
                }
                final CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);
                nodes.setTokenStream(tokens);

                // walk the tree and build definitions
                final CSSTree treeWalker = new CSSTree(nodes);
                treeWalker.stylesheet();

                problems.addAll(treeWalker.problems);

                // definition models
                return treeWalker.model;
            }
            return null;
        }
        catch (RecognitionException e)
        {
            assert false : "RecognitionException must be collected as ICompilerProblem.";
            problems.add(new UnexpectedExceptionProblem(e));
            return null;
        }
    }

    /**
     * Create a root CSS definition.
     * 
     * @param rules CSS rules
     * @param namespaces {@code @namespace} statements
     * @param fontFaces {@code @font-face} statements
     * @param tree root of the AST
     */
    protected CSSDocument(final List<CSSRule> rules,
                          final List<CSSNamespaceDefinition> namespaces,
                          final List<CSSFontFace> fontFaces,
                          final CommonTree tree,
                          final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.DOCUMENT);

        assert rules != null : "Rules can't be null.";
        assert namespaces != null : "Namespace definitions can't be null";
        assert fontFaces != null : "Font face definitions can't be null";

        this.rules = new ImmutableList.Builder<ICSSRule>().addAll(rules).build();
        this.namespaces = new ImmutableList.Builder<ICSSNamespaceDefinition>().addAll(namespaces).build();
        this.fontFaces = new ImmutableList.Builder<ICSSFontFace>().addAll(fontFaces).build();
        
        Map<String, CSSNamespaceDefinition> namespaceMap = new HashMap<String, CSSNamespaceDefinition>();
        for (CSSNamespaceDefinition namespace : namespaces)
        {
            final String prefix = namespace.getPrefix();
            final String key = prefix != null ? prefix : DEFAULT_NAMESPACE_SHORT_NAME;
            namespaceMap.put(key, namespace);
        }
        this.namespacesLookup = ImmutableMap.copyOf(namespaceMap);

        // setup tree
        children.add(new CSSTypedNode(CSSModelTreeType.NAMESPACE_LIST, this.namespaces));
        children.add(new CSSTypedNode(CSSModelTreeType.FONT_FACE_LIST, this.fontFaces));
        children.add(new CSSTypedNode(CSSModelTreeType.RULE_LIST, this.rules));

        List<ICSSNode> allNodes = new ArrayList<ICSSNode>();
        allNodes.addAll(this.namespaces);
        allNodes.addAll(this.fontFaces);
        allNodes.addAll(this.rules);
        int line = NOT_SET;
        int column = NOT_SET;
        int endLine = NOT_SET;
        int endColumn = NOT_SET;
        int start = NOT_SET;
        int end = NOT_SET;
        for (ICSSNode node : allNodes)
        {
            if (start == NOT_SET || start > node.getStart())
            {
                start = node.getStart();
            }
            if (end == NOT_SET || end < node.getStart())
            {
                end = node.getEnd();
            }
            if (line == NOT_SET || line > node.getLine())
            {
                line = node.getLine();
                column = node.getColumn();
            }
            else if (line == node.getLine() && column > node.getColumn())
            {
                column = node.getColumn();
            }
            if (endLine == NOT_SET || endLine < node.getEndLine())
            {
                endLine = node.getEndLine();
                endColumn = node.getEndColumn();
            }
            else if (endLine == node.getEndLine() && endColumn < node.getEndColumn())
            {
                endColumn = node.getEndColumn();
            }
        }
        setStart(start);
        setEnd(end);
        setLine(line);
        setColumn(column);
        setEndLine(endLine);
        setEndColumn(endColumn);
        if (tokenStream != null)
        {
            setSourcePath(tokenStream.getSourceName());
        }
    }

    private final ImmutableList<ICSSRule> rules;
    private final ImmutableList<ICSSNamespaceDefinition> namespaces;
    private final ImmutableList<ICSSFontFace> fontFaces;
    private final ImmutableMap<String, CSSNamespaceDefinition> namespacesLookup;

    @Override
    public ImmutableList<ICSSRule> getRules()
    {
        return rules;
    }

    @Override
    public ImmutableList<ICSSNamespaceDefinition> getAtNamespaces()
    {
        return namespaces;
    }

    @Override
    public String toString()
    {
        return Joiner.on("\n").join(
                Joiner.on("\n").join(namespaces),
                Joiner.on("\n").join(fontFaces),
                Joiner.on("\n").join(rules));
    }

    @Override
    public ImmutableList<ICSSFontFace> getFontFaces()
    {
        return fontFaces;
    }

    @Override
    public ICSSNamespaceDefinition getNamespaceDefinition(String prefix)
    {
        return namespacesLookup.get(prefix);
    }

    @Override
    public ICSSNamespaceDefinition getDefaultNamespaceDefinition()
    {
        return namespacesLookup.get(DEFAULT_NAMESPACE_SHORT_NAME);
    }

	private static boolean hasErrors(Collection<ICompilerProblem> problems) {
		CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(null);
		for (ICompilerProblem problem : problems) {
			CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem);
			if (CompilerProblemSeverity.ERROR.equals(severity)) {
				return true;
			}
		}
		return false;
	}

}
