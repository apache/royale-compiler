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

package org.apache.royale.compiler.internal.tree.mxml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.IProjectConfigVariables;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * This class scans character-by-character across the logical text of multiple
 * source fragments (coming from attribute values or text units), looking for
 * databinding expressions using the same simple brace-matching algorithm as the
 * old compiler. It breaks fragments at each <code>{</code> that begins a
 * databinding and each <code>}</code> that ends one.
 * <p>
 * We have to deal with fragments because physical MXML constructs such as
 * entities, CDATA blocks, and MXML comments cannot be presented to the
 * ActionScript parser, but the logical ActionScript code that does get parsed
 * must produce ActionScript nodes that have the correct physical source
 * locations within the file.
 */
class MXMLDataBindingParser
{
    /**
     * An MXML databinding starts with this character.
     */
    private static final char LEFT_BRACE = '{';

    /**
     * An MXML databinding ends with this character.
     */
    private static final char RIGHT_BRACE = '}';

    /**
     * This character can be used to escape the '{' so that it doesn't indicate
     * databinding.
     */
    private static final char BACKSLASH = '\\';

    /**
     * Parses source fragments looking for databinding expressions.
     * <p>
     * If none are found, a String formed by concatenating the logical text of
     * the fragments is returned. If one databinding is found, with no
     * surrounding text, an {@code IMXMLDataBindingNode} representing it is
     * returned. If one or more databindings, with surrounding or interspersed
     * text is found, an {@code IMXMLConcatenatedDataBindingNode} is returned.
     */
    public static Object parse(IMXMLNode parent,
                               ISourceLocation sourceLocation,
                               ISourceFragment[] fragments,
                               Collection<ICompilerProblem> problems,
                               Workspace workspace,
                               MXMLDialect mxmlDialect,
                               ICompilerProject project)
    {
        assert fragments != null : "Expected an array of source fragments";

        // Find pairs of '{' and '}' in the fragments that represent databindings.
        ListMultimap<ISourceFragment, Integer> scanResult = scan(fragments);

        // A null result means there are no databindings.
        // Return the concatenated logical text.
        if (scanResult == null)
            return SourceFragmentsReader.concatLogicalText(fragments);

        // Split the fragments as necessary around the '{' and '}' that
        // begin and end databindings and produce a list of subfragment lists.
        // Each entry in the top-level list is either a DataBindingFragmentList
        // containing subfragments inside a databinding or a NonDataBindingFragmentList
        // containing subfragments outside a databinding.
        List<FragmentList> splitResult = split(fragments, scanResult);

        // Create an MXMLConcatenatedDataBindingNode with children.
        // Each DataBindingFragmentList creates a child MXMLDataBindingNode.
        // Each NonDataBindingFragmentList creates a child LiteralNode of type STRING.
        return createNode(parent, sourceLocation, splitResult, problems, workspace, mxmlDialect, project);
    }

    /**
     * Scans character-by-character across the logical text of each source
     * fragment, doing brace-matching and building up a list (in
     * dataBindingRanges) of DataBindingRange objects that keep track of where
     * each databinding starts and ends.
     */
    private static ListMultimap<ISourceFragment, Integer> scan(ISourceFragment[] fragments)
    {
        // We'll return a map mapping a source fragment to a list
        // of character indexes where the '{' and '}' for databindings
        // are located.
        ListMultimap<ISourceFragment, Integer> result = null;

        // This counter is incremented by each unescaped '{'
        // and decremented by each '}'.
        int nesting = 0;

        // These keep track of where we found a '{' that starts
        // a databinding, while we look for the matching '}'.
        ISourceFragment leftBraceFragment = null;
        int leftBraceCharIndex = -1;

        // This flag keeps track of whether we've seen a backslash,
        // which escapes the databinding meaning of '{'.
        boolean escape = false;

        // Iterate over each input fragment.
        for (ISourceFragment fragment : fragments)
        {
            // Iterate over each character of logical text in the current fragment.
            String text = fragment.getLogicalText();
            int n = text.length();
            for (int i = 0; i < n; i++)
            {
                switch (text.charAt(i))
                {
                    default:
                    {
                        escape = false;
                        break;
                    }

                    case LEFT_BRACE:
                    {
                        // If this '{' isn't nested and isn't escaped,
                        // it might start a databinding, so remember where it is.
                        if (nesting == 0 & !escape)
                        {
                            leftBraceFragment = fragment;
                            leftBraceCharIndex = i;
                        }
                        nesting++;
                        escape = false;
                        break;
                    }

                    case RIGHT_BRACE:
                    {
                        nesting--;
                        if (nesting == 0)
                        {
                            // We've found the matching '}' for the '{'.
                            // Record where the '{' and '}' are.
                            if (result == null)
                                result = LinkedListMultimap.<ISourceFragment, Integer> create();
                            result.put(leftBraceFragment, leftBraceCharIndex);
                            result.put(fragment, i);
                        }
                        escape = false;
                        break;
                    }

                    case BACKSLASH:
                    {
                        escape = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Builds a list of DataBindingFragmentList and NonDataBindingFragmentList
     * objects that organizes the fragments that are inside databindings and
     * outside databindings.
     */
    private static List<FragmentList> split(
            ISourceFragment[] fragments,
            ListMultimap<ISourceFragment, Integer> scanResult)
    {
        List<FragmentList> result = new ArrayList<FragmentList>();

        boolean inDataBinding = false;
        FragmentList currentList = null;

        for (ISourceFragment fragment : fragments)
        {
            // Get the character indexes where '{' and '}' for databindings are.
            List<Integer> braceIndices = scanResult.get(fragment);

            if (braceIndices == null)
            {
                if (currentList == null)
                    currentList = newFragmentList(result, inDataBinding);

                // If there is no '{' or '}' in this fragment,
                // add the fragment to the current fragment list.
                currentList.add(fragment);
            }
            else
            {
                // Otherwise, we'll have to break up the fragment.
                // Add an end-of-fragment index at the end of the list
                // so that we don't miss the last subfragment.
                braceIndices.add(fragment.getLogicalText().length());

                // Break up the fragment into subfragments around
                // the '{' and '}' characters.
                // Add each subfragment to the appropriate list.
                int beginIndex = 0;
                for (int braceIndex : braceIndices)
                {
                    ISourceFragment subfragment =
                            ((SourceFragment)fragment).subfragment(beginIndex, braceIndex);
                    if (subfragment != null)
                    {
                        currentList = newFragmentList(result, inDataBinding);
                        currentList.add(subfragment);
                    }
                    beginIndex = braceIndex + 1;
                    inDataBinding = !inDataBinding;
                }
            }
        }

        return result;
    }

    private static FragmentList newFragmentList(List<FragmentList> result, boolean inDataBinding)
    {
        FragmentList list = inDataBinding ?
                            new DataBindingFragmentList() :
                            new NonDataBindingFragmentList();

        result.add(list);

        return list;
    }

    private static IASNode createNode(IMXMLNode parent,
                                      ISourceLocation sourceLocation,
                                      List<FragmentList> listOfFragmentLists,
                                      Collection<ICompilerProblem> problems,
                                      Workspace workspace,
                                      MXMLDialect mxmlDialect,
                                      ICompilerProject project)
    {
        MXMLConcatenatedDataBindingNode node = new MXMLConcatenatedDataBindingNode((NodeBase)parent);

        node.setLocation(sourceLocation.getSourcePath(),
                         sourceLocation.getAbsoluteStart(), sourceLocation.getAbsoluteEnd(),
                         sourceLocation.getLine(), sourceLocation.getColumn(),
                         sourceLocation.getEndLine(), sourceLocation.getEndColumn());

        // Build a list of children for the MXMLConcatenatedDataBindingNode.
        List<IASNode> children = new ArrayList<IASNode>();

        for (List<ISourceFragment> fragmentList : listOfFragmentLists)
        {
            if (fragmentList instanceof DataBindingFragmentList)
            {
                // For each DataBindingFragmentList, add an IMXMLDataBindingNode
                // containing an IExpressionNode created by the ActionScript parser.
                children.add(createDataBindingNode(node, sourceLocation, fragmentList, problems, workspace, project));
            }
            else if (fragmentList instanceof NonDataBindingFragmentList)
            {
                // For each NonDataBindingFragmentList, add an ILiteralNode
                // of type STRING from the concatenated logical text.
                children.add(createStringLiteralNode(node, sourceLocation, fragmentList));
            }
        }

        // If no nodes were built, we probably had an empty databinding ( "{}" )
        // But we must make a node here, otherwise the tree will be malformed or
        // invalid. So let's create an empty one
        if (children.isEmpty())
        {
            assert listOfFragmentLists.isEmpty(); // should only happen if we
                                                  // were passed no frags
            children.add(createEmptyDatabindingNode(node, sourceLocation));
        }

        // If the leading/trailing child is a whitespace string literal node, remove it.
        trim(children, mxmlDialect);

        node.setChildren(children.toArray(new IASNode[0]));

        // If the only thing inside is a single IMXMLDataBindingNode,
        // return that, because we're not concatenating anything.
        if (node.getChildCount() == 1)
        {
            IASNode child = node.getChild(0);
            if (child instanceof IMXMLSingleDataBindingNode)
                return child;
        }

        // Otherwise, return the IConcatenatedDataBindingNode.
        return node;
    }

    private static ILiteralNode createStringLiteralNode(
            MXMLConcatenatedDataBindingNode parent,
            ISourceLocation sourceLocation,
            List<ISourceFragment> fragmentList)
    {
        ISourceFragment[] fragments = fragmentList.toArray(new ISourceFragment[0]);
        String text = SourceFragmentsReader.concatLogicalText(fragments);

        // LiteralNode automatically strips out quote characters at the
        // beginning and end of the string.
        // with that in mind, if the original text starts or ends with a quote,
        // that will get stripped if we don't wrap it manually.
        // apache/royale-compiler#49
        if(text.indexOf("\"") != -1)
        {
            text = "'" + text + "'";
        }
        else
        {
            text = "\"" + text + "\"";
        }
        LiteralNode stringLiteralNode = new LiteralNode(LiteralType.STRING, text);
        stringLiteralNode.setParent(parent);

        ISourceFragment firstFragment = fragments[0];
        ISourceFragment lastFragment = fragments[fragments.length - 1];
        stringLiteralNode.setSourcePath(sourceLocation.getSourcePath());
        stringLiteralNode.setStart(firstFragment.getPhysicalStart());
        stringLiteralNode.setEnd(lastFragment.getPhysicalStart() + lastFragment.getPhysicalText().length());
        stringLiteralNode.setLine(firstFragment.getPhysicalLine());
        stringLiteralNode.setColumn(firstFragment.getPhysicalColumn());

        return stringLiteralNode;
    }

    private static IMXMLSingleDataBindingNode createDataBindingNode(
            IMXMLNode parent,
            ISourceLocation sourceLocation,
            List<ISourceFragment> fragments,
            Collection<ICompilerProblem> problems,
            Workspace workspace,
            ICompilerProject project)
    {
        MXMLSingleDataBindingNode result = new MXMLSingleDataBindingNode((NodeBase)parent);

        // Set location information for the MXMLDataBindingNode.
        ISourceFragment firstFragment = fragments.get(0);
        ISourceFragment lastFragment = fragments.get(fragments.size() - 1);
        result.setSourcePath(sourceLocation.getSourcePath());
        result.setStart(firstFragment.getPhysicalStart() - 1);
        result.setEnd(lastFragment.getPhysicalStart() + lastFragment.getPhysicalText().length() + 1);
        result.setLine(firstFragment.getPhysicalLine());
        result.setColumn(firstFragment.getPhysicalColumn() - 1);

        // Parse the fragments inside the databinding expression.
        Reader reader = new SourceFragmentsReader(sourceLocation.getSourcePath(), fragments.toArray(new ISourceFragment[0]));
        // IExpressionNode expressionNode = ASParser.parseDataBinding(workspace, reader, problems);
        IProjectConfigVariables projectConfigVariables =
            ((RoyaleProject)project).getProjectConfigVariables();
        IExpressionNode expressionNode = ASParser.parseExpression(workspace, reader, problems, 
                            projectConfigVariables, sourceLocation);

        // If the parse of the databinding expression failed,
        // substitute an empty string literal node
        // (which is the result of the empty databinding expression {}).
        if (expressionNode == null)
            expressionNode = new LiteralNode(LiteralType.STRING, "");

        // ASParser creates the ExpressionNodeBase as a child of a FileNode.
        // Make it a child of the MXMLDataBindingNode.
        ((ExpressionNodeBase)expressionNode).setParent(result);
        result.setExpressionNode(expressionNode);

        // double check that the node tree has its children's parent chain set up
        validateParents((NodeBase)expressionNode);
        return result;
    }

    private static void validateParents(NodeBase expressionNode)
    {
        for (int i = 0; i < expressionNode.getChildCount(); i++)
        {
            IASNode child = expressionNode.getChild(i);
            if (child instanceof NodeBase)
            {
                if (child.getParent() == null)
                    ((NodeBase)child).setParent((ExpressionNodeBase)expressionNode);
                validateParents((NodeBase)child);
            }
        }
    }
    
    // Makes a databinding node whose expression is just an empty string
    private static IMXMLSingleDataBindingNode createEmptyDatabindingNode(IMXMLNode parent, ISourceLocation sourceLocation)
    {
        MXMLSingleDataBindingNode result = new MXMLSingleDataBindingNode((NodeBase)parent);

        // Since we are empty, we can't set our source location based on our children.
        // But in the special case we can set our location to the same thing as our parent.
        result.setSourceLocation((NodeBase)parent);

        IExpressionNode expressionNode = new LiteralNode(LiteralType.STRING, "");
        ((ExpressionNodeBase)expressionNode).setParent(result);
        result.setExpressionNode(expressionNode);
        return result;
    }

    private static void trim(List<IASNode> children, MXMLDialect mxmlDialect)
    {
        assert (children != null && !children.isEmpty()); // function as written requires at least one child

        IASNode firstChild = children.get(0);
        removeIfWhitespace(children, firstChild, mxmlDialect);

        int n = children.size();
        IASNode lastChild = children.get(n - 1);
        removeIfWhitespace(children, lastChild, mxmlDialect);
    }

    private static void removeIfWhitespace(List<IASNode> children,
                                           IASNode child,
                                           MXMLDialect mxmlDialect)
    {
        if (child instanceof ILiteralNode)
        {
            String text = ((ILiteralNode)child).getValue();
            if (mxmlDialect.isWhitespace(text))
                children.remove(child);
        }
        assert !children.isEmpty(); // don't delete all the children
    }

    /**
     * This is a typedef-like class to improve readability by avoiding nested
     * parameterized types.
     */
    @SuppressWarnings("serial")
    private static abstract class FragmentList extends ArrayList<ISourceFragment>
    {
    }

    /**
     * This class is used to store sequences of {@code ISourceFragment}s that
     * are inside databinding expressions.
     */
    @SuppressWarnings("serial")
    private static class DataBindingFragmentList extends FragmentList
    {
    }

    /**
     * This class is used to store sequences of {@code ISourceFragment}s that
     * are outside databinding expressions.
     */
    @SuppressWarnings("serial")
    private static class NonDataBindingFragmentList extends FragmentList
    {
    }
}
