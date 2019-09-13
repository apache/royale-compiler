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

package org.apache.royale.compiler.internal.tree.as;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import antlr.Token;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.common.Counter;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Base class for ActionScript parse tree nodes
 */
public abstract class NodeBase extends SourceLocation implements IASNode
{
    /**
     * Used in calls to CheapArray functions. It represents the type of objects
     * that appear in fSymbols.
     */
    protected static final IASNode[] emptyNodeArray = new IASNode[0];
    

    /**
     * Combine the attributes from the base class with the attributes specific
     * to this class
     * 
     * @param superAttributes The attributes of the base class.
     * @param myAttributes The attributes of this class.
     * @return attribute value
     */
    public static String[] combineAttributes(String[] superAttributes, String[] myAttributes)
    {
        String[] combinedAttributes = new String[superAttributes.length + myAttributes.length];
        for (int i = 0; i < superAttributes.length; i++)
        {
            combinedAttributes[i] = superAttributes[i];
        }
        for (int i = 0; i < myAttributes.length; i++)
        {
            combinedAttributes[superAttributes.length + i] = myAttributes[i];
        }
        return combinedAttributes;
    }

    /**
     * Constructor.
     */
    public NodeBase()
    {
        super();
        parent = null;
        
        if (Counter.COUNT_NODES)
            countNodes();
    }

    /**
     * Parent node.
     * <p>
     * Methods (even in NodeBase itself) should use getParent() instead of
     * accessing this member directly. getParent() is overridden in FileNode to
     * do something special when the FileNode represents a shared file.
     */
    protected IASNode parent;

    @Override
    public IASNode getParent()
    {
        return parent;
    }

    @Override
    public int getChildCount()
    {
        return 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return null;
    }

    @Override
    public IFileSpecification getFileSpecification()
    {
        // TODO Make sure this works with include processing!!!
        ASFileScope fileScope = getFileScope();
        IWorkspace w = fileScope.getWorkspace();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("NodeBase waiting for lock in getFileSpecification");
        IFileSpecification fs = w.getFileSpecification(fileScope.getContainingPath());
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("NodeBase done with lock in getFileSpecification");
        return fs;
    }

    @Override
    public int getSpanningStart()
    {
        return getStart();
    }

    /**
     * Get the node type as a string. For example, this will return
     * "IdentifierNode" for an IdentifierNode.
     * 
     * @return the node type
     */
    public String getNodeKind()
    {
        return getClass().getSimpleName();
    }

    @Override
    public boolean contains(int offset)
    {
        return getAbsoluteStart() < offset && getAbsoluteEnd() >= offset;
    }

    /**
     * Determine whether the offset "loosely" fits within this node. With normal
     * nodes, this will return true if the offset matches fLocation.fStart or
     * fLocation.fEnd or anything in between. With bogus nodes (which have
     * fStart == fEnd), this will return true if the offset comes after fStart
     * and before any other nodes in the parse tree
     * 
     * @param offset the offset to test
     * @return true if the offset is "loosely" contained within this node
     */
    public boolean looselyContains(int offset)
    {
        if (getAbsoluteStart() == getAbsoluteEnd())
        {
            // This is a bogus placeholder node (generally an identifier that's been inserted to complete an expression).
            // We'll pretend that it extends all the way to the start offset of the next node.  Now to find that next node...
            if (getAbsoluteStart() <= offset)
            {
                NodeBase containingNode = (NodeBase)getParent();
                // Step 1: Find a node that extends beyond the target offset
                while (containingNode != null && containingNode.getAbsoluteEnd() <= getAbsoluteEnd())
                    containingNode = (NodeBase)containingNode.getParent();
                // If no such node exists, we know that there aren't any tokens in the parse
                // tree that end after this bogus token.  So we can go ahead and pretend this node
                // extends all the way out.
                if (containingNode == null)
                    return true;
                // Step 2: Find a node that starts after the target offset
                IASNode succeedingNode = containingNode.getSucceedingNode(getAbsoluteEnd());
                // If no such node exists, we know that there aren't any tokens in the parse
                // tree that start after this bogus token.  So we can go ahead and pretend this
                // node extends all the way out.
                if (succeedingNode == null)
                    return true;
                // Otherwise, pretend this node extends all the way to the next token.
                if (succeedingNode.getAbsoluteStart() >= offset)
                    return true;
            }
        }
        else
        {
            // This is a real token.  See if the test offset fits within (including the boundaries, since we're looking
            // at "loose" containment.
            return getAbsoluteStart() <= offset && getAbsoluteEnd() >= offset;
        }
        return false;
    }

    @Override
    public IASNode getSucceedingNode(int offset)
    {
        // This node ends before the offset is even reached.  This is hopeless.
        if (getAbsoluteEnd() <= offset)
            return null;

        // See if one of our children starts after the offset
        for (int i = 0; i < getChildCount(); i++)
        {
            IASNode child = getChild(i);
            
            if (child.getAbsoluteStart() > offset)
                return child;
            else if (child.getAbsoluteEnd() > offset)
                return child.getSucceedingNode(offset);
        }
        
        return null;
    }

    /**
     * Determine whether this node is transparent. If a node is transparent, it
     * can never be returned from getContainingNode... the offset will be
     * considered part of the parent node instead.
     * 
     * @return true if the node is transparent
     */
    public boolean isTransparent()
    {
        return false;
    }

    @Override
    public IASNode getContainingNode(int offset)
    {
        // This node doesn't even contain the offset.  This is hopeless.
        if (!contains(offset))
        {
            return null;
        }
        IASNode containingNode = this;
        int childCount = getChildCount();
        // See if the offset is contained within one of our children.
        for (int i = 0; i < childCount; i++)
        {
            IASNode child = getChild(i);
            if (child.getAbsoluteStart() <= offset)
            {
                if (child.contains(offset))
                {
                    containingNode = child.getContainingNode(offset);
                    if (child instanceof NodeBase && ((NodeBase)child).canContinueContainmentSearch(containingNode, this, i, true))
                        continue;
                    break;
                }
            }
            else
            {
                if (child instanceof NodeBase && ((NodeBase)child).canContinueContainmentSearch(containingNode, this, i, false))
                    continue;
                // We've passed this offset without finding a child that contains it.  This is
                // the nearest enclosing node.
                break;
            }
        }
        // Make sure we don't return a transparent node
        while (containingNode != null && containingNode instanceof NodeBase && ((NodeBase)containingNode).isTransparent())
        {
            containingNode = containingNode.getParent();
        }
        return containingNode;
    }

    @Override
    public boolean isTerminal()
    {
        return false;
    }

    @Override
    public IScopedNode getContainingScope()
    {
        return (IScopedNode)getAncestorOfType(IScopedNode.class);
    }

    // TODO Can probably eliminate this.
    protected boolean canContinueContainmentSearch(IASNode containingNode, IASNode currentNode, int childOffset, boolean offsetsStillValid)
    {
        return false;
    }

    @Override
    public IASNode getAncestorOfType(Class<? extends IASNode> nodeType)
    {
        IASNode current = getParent();
        while (current != null && !(nodeType.isInstance(current)))
            current = current.getParent();
        if (current != null)
            return current;
        return null;
    }

    @Override
    public String getPackageName()
    {
        // Starting with this node's parent, walk up the parent chain
        // looking for a package node or an MXML class definition node.
        // These two types of nodes understand what their package is.
        IASNode current = getParent();
        while (current != null &&
               !(current instanceof IPackageNode ||
                 current instanceof IMXMLClassDefinitionNode))
        {
            current = current.getParent();
        }

        if (current instanceof IPackageNode)
            return ((IPackageNode)current).getPackageName();
        else if (current instanceof IMXMLClassDefinitionNode)
            return ((IMXMLClassDefinitionNode)current).getPackageName();

        return null;
    }

    /**
     * Set the start offset of the node to fall just after the token. Used
     * during parsing.
     * 
     * @param token start this node after this token
     */
    public void startAfter(Token token)
    {
        if (token instanceof ISourceLocation)
        {
            startAfter((ISourceLocation) token);
        }
    }
    
    /**
     * Set the start offset of the node to fall just after the token. Used
     * during parsing.
     * 
     * @param location start this node after this token
     */
    public final void startAfter(ISourceLocation location)
    {
        final int end = location.getEnd();
        if (end != UNKNOWN)
        {
            setSourcePath(location.getSourcePath());
            setStart(end);
            setLine(location.getEndLine());
            setColumn(location.getEndColumn());
        }
    }

    /**
     * Set the start offset of the node to fall just before the token. Used
     * during parsing.
     * 
     * @param token start this node before this token
     */
    public final void startBefore(Token token)
    {
        if (token instanceof ISourceLocation)
            startBefore((ISourceLocation)token);
    }

    /**
     * Set the start offset of the node to fall just before the token. Used
     * during parsing.
     * 
     * @param location start this node before this token
     */
    public final void startBefore(ISourceLocation location)
    {
        final int start = location.getStart();
        if (start != UNKNOWN)
        {
            setSourcePath(location.getSourcePath());
            setStart(start);
            setLine(location.getLine());
            setColumn(location.getColumn());
        }
    }

    /**
     * Set the end offset of the node to fall just after the token. Used during
     * parsing.
     * 
     * @param token end this node after this token
     */
    public final void endAfter(Token token)
    {
        if (token instanceof ISourceLocation)
            endAfter((ISourceLocation)token);
    }

    /**
     * Set the end offset of the node to fall just after the token. Used during
     * parsing.
     * 
     * @param location end this node after this token
     */
    public final void endAfter(ISourceLocation location)
    {
        final int end = location.getEnd();
        if (end != UNKNOWN)
        {
            setEnd(end);
            setEndLine(location.getEndLine());
            setEndColumn(location.getEndColumn());
        }
    }

    /**
     * Set the end offset of the node to fall just before the token. Used during
     * parsing.
     * 
     * @param token start this node before this token
     */
    public final void endBefore(Token token)
    {
        if (token instanceof ISourceLocation)
            endBefore((ISourceLocation)token);
    }

    /**
     * Set the end offset of the node to fall just before the token. Used during
     * parsing.
     * 
     * @param location start this node before this token
     */
    public final void endBefore(ISourceLocation location)
    {
        final int start = location.getStart();
        if (start != UNKNOWN)
        {
            setEnd(start);
            setEndLine(location.getLine());
            setEndColumn(location.getColumn());
        }
    }

    /**
     * Set the start and end offsets of the node to coincide with the token's
     * offsets. Used during parsing.
     * 
     * @param token the token to take the offsets from
     */
    public final void span(Token token)
    {
        if (token instanceof ISourceLocation)
            span((ISourceLocation)token);
    }

    /**
     * Set the start and end offsets of the node to coincide with the tokens'
     * offsets. Used during parsing.
     * 
     * @param firstToken the token to take the start offset and line number from
     * @param lastToken the token to take the end offset from
     */
    public final void span(Token firstToken, Token lastToken)
    {
        if (firstToken instanceof ISourceLocation && lastToken instanceof ISourceLocation)
            span((ISourceLocation)firstToken, (ISourceLocation)lastToken);
    }

    /**
     * Set the start and end offsets of the node. Used during parsing.
     * 
     * @param start start offset for the node
     * @param end end offset for the node
     * @param line line number for the node
     * @deprecated Use span(int,int,int,int,int,int) instead so that endLine and endColumn are included
     */
    public final void span(int start, int end, int line, int column)
    {
        span(start, end, line, column, -1, -1);
    }

    /**
     * Set the start and end offsets of the node. Used during parsing.
     * 
     * @param start start offset for the node
     * @param end end offset for the node
     * @param line line number for the node
     */
    public final void span(int start, int end, int line, int column, int endLine, int endColumn)
    {
        setStart(start);
        setEnd(end);
        setLine(line);
        setColumn(column);
        setEndLine(endLine);
        setEndColumn(endColumn);
    }

    public Collection<ICompilerProblem> runPostProcess(EnumSet<PostProcessStep> set, ASScope containingScope)
    {
        ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(10);
        normalize(set.contains(PostProcessStep.CALCULATE_OFFSETS));
        if (set.contains(PostProcessStep.POPULATE_SCOPE) || set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
            analyze(set, containingScope, problems);
        return problems;
    }

    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        int childrenSize = getChildCount();
        // Populate this scope with definitions found among the children
        for (int i = 0; i < childrenSize; i++)
        {
            IASNode child = getChild(i);
            if (child instanceof NodeBase)
            {
                if (child.getParent() == null)
                    ((NodeBase)child).setParent(this);
                ((NodeBase)child).analyze(set, scope, problems);
            }
        }
    }

    /**
     * Changes the position of two children in our tree. Neither child can be
     * null and both must have the same parent
     * 
     * @param child the child to replace target
     * @param target the child to replace child
     */
    protected void swapChildren(NodeBase child, NodeBase target)
    {
        //no op in this impl
    }

    /**
     * Replaces the child with the given target. The child will be removed from
     * the tree, and its parentage will be updated
     * 
     * @param child the {@link NodeBase} to replace
     * @param target the {@link NodeBase} to replace the replaced
     */
    protected void replaceChild(NodeBase child, NodeBase target)
    {
        //no op
    }

    /**
     * Normalize the tree. Move custom children into the real child list and
     * fill in missing offsets so that offset lookup will work. Used during
     * parsing.
     */
    public void normalize(boolean fillInOffsets)
    {
        // The list of children doesn't change, so the child count should be constant throughout the loop
        int childrenSize = getChildCount();
        // Normalize the regular children
        for (int i = 0; i < childrenSize; i++)
        {
            IASNode child = getChild(i);
            if (child instanceof NodeBase)
            {
                ((NodeBase)child).setParent(this);
                ((NodeBase)child).normalize(fillInOffsets);
            }
        }
        // Add the special children (which get normalized as they're added)
        if (childrenSize == 0)
            setChildren(fillInOffsets);
        // Fill in offsets based on the child nodes
        if (fillInOffsets)
            fillInOffsets();
        // fill in any dangling ends
        //fillEndOffsets();
        // get rid of unused child space
    }

    /**
     * Allow various subclasses to do special kludgy things like identify
     * mx.core.Application.application
     */
    protected void connectedToProjectScope()
    {
        // The list of children doesn't change, so the child count should be constant throughout the loop
        int childrenSize = getChildCount();
        for (int i = 0; i < childrenSize; i++)
        {
            IASNode child = getChild(i);
            if (child instanceof NodeBase)
                ((NodeBase)child).connectedToProjectScope();
        }
    }

    /**
     * If this node has custom children (names, arguments, etc), shove them into
     * the list of children. Used during parsing.
     */
    protected void setChildren(boolean fillInOffsets)
    {
        //nothing in this class
    }

    /**
     * If the start and end offsets haven't been set explicitly, fill them in
     * based on the offsets of the children. Used during parsing. If the end
     * offset is less than any of the kids' offsets, change fLocation.fEnd to
     * encompass kids.
     */
    protected void fillInOffsets()
    {
        int numChildren = getChildCount();
        if (numChildren > 0)
        {
            int start = getAbsoluteStart();
            int end = getAbsoluteEnd();
            if (start == -1)
            {
                for (int i = 0; i < numChildren; i++)
                {
                    IASNode child = getChild(i);
                    int childStart = child.getAbsoluteStart();
                    if (childStart != -1)
                    {
                        if (getSourcePath() == null)
                            setSourcePath(child.getSourcePath());
                        setStart(childStart);
                        setLine(child.getLine());
                        setColumn(child.getColumn());
                        break;
                    }
                }
            }
            for (int i = numChildren - 1; i >= 0; i--)
            {
                IASNode child = getChild(i);
                int childEnd = child.getAbsoluteEnd();
                if (childEnd != -1)
                {
                    if (end < childEnd)
                    {
                        setEnd(childEnd);
                        setEndLine(child.getEndLine());
                        setEndColumn(child.getEndColumn());
                    }
                    break;
                }
            }
        }
    }

    /**
     * Set the parent node. Used during parsing.
     * 
     * @param parent parent node
     */
    public void setParent(NodeBase parent)
    {
        this.parent = parent;
    }

    /**
     * Get the nearest containing scope for this node. Used during type
     * decoration.
     * 
     * @return nearest containing scope for this node
     */
    // TODO Make this more efficient using overrides on BlockNode and FileNode.
    public IScopedNode getScopeNode()
    {
        if (this instanceof IScopedNode && ((IScopedNode)this).getScope() != null)
            return (IScopedNode)this;
        IASNode parent = getParent();
        if (parent != null && parent instanceof NodeBase)
            return ((NodeBase)parent).getScopeNode();
        return null;
    }

    /**
     * Get the path of the file in which this parse tree node resides.
     * <p>
     * The tree builder can set a node's origin source file using
     * {@link #setSourcePath(String)}. If the source path was set, return the
     * source path; Otherwise, use root {@link FileNode}'s path.
     * 
     * @return file path that contains this node
     */
    public String getContainingFilePath()
    {
        String path = getSourcePath();
        if (path != null)
            return path;

        final FileNode fileNode = (FileNode)getAncestorOfType(FileNode.class);
        if (fileNode != null)
            return fileNode.getSourcePath();
        return null;
    }

    /**
     * For debugging only. Displays this node and its children in a form like
     * this:
     * 
     * <pre>
     * FileNode "D:\tests\UIComponent.as" 0:0 0-467464  D:\tests\UIComponent.as
     *   PackageNode "mx.core" 12:1 436-465667 D:\tests\UIComponent.as
     *     FullNameNode "mx.core" 0:0 444-451 null
     *       IdentifierNode "mx" 12:9 444-446 D:\tests\UIComponent.as
     *       IdentifierNode "core" 12:12 447-451 D:\tests\UIComponent.as
     *     ScopedBlockNode 13:1 454-465667 D:\tests\UIComponent.as
     *       ImportNode "flash.accessibility.Accessibility" 14:1 457-497 D:\tests\UIComponent.as
     *       FullNameNode "flash.accessibility.Accessibility" 0:0 464-497 null
     *         FullNameNode "flash.accessibility" 0:0 464-483 null
     *           IdentifierNode "flash" 14:8 464-469 D:\tests\UIComponent.as
     *           IdentifierNode "accessibility" 14:14 470-483 D:\tests\UIComponent.as
     *         IdentifierNode "Accessibility" 14:28 484-497 D:\tests\UIComponent.as
     *       ...
     * </pre>
     * <p>
     * Subclasses may not override this, because we want to maintain regularity
     * for how all nodes display.
     */
    @Override
    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        buildStringRecursive(sb, 0, false);
        return sb.toString();
    }

    /**
     * For debugging only. Called by {@code toString()}, and recursively by
     * itself, to display this node on one line and each descendant node on
     * subsequent indented lines.
     * 
     * * */
    public void buildStringRecursive(StringBuilder sb, int level, boolean skipSrcPath)
    {
        // Indent two spaces for each nesting level.
        for (int i = 0; i < level; i++)
        {
            sb.append("  ");
        }

        // Build the string that represents this node.
        buildOuterString(sb, skipSrcPath);
      
        sb.append('\n');
        
        //To test scopes in ParserSuite
        if(skipSrcPath && (this instanceof IScopedNode)) {
            for (int i = 0; i < level+1; i++)
                sb.append("  ");
            sb.append("[Scope]");
            sb.append("\n");
            IScopedNode scopedNode = (IScopedNode)this;
            IASScope scope = scopedNode.getScope();
            Collection<IDefinition> definitions = scope.getAllLocalDefinitions();
            for(IDefinition def : definitions) {
                for (int i = 0; i < level+2; i++)
                    sb.append("  ");
                ((DefinitionBase)def).buildString(sb, false);
                sb.append('\n');
            }
        }

        // Recurse over the child nodes.
        int n = getChildCount();
        for (int i = 0; i < n; i++)
        {
            NodeBase child = (NodeBase)getChild(i);
            // The child can be null if we're toString()'ing
            // in the debugger during tree building.
            if (child != null)
                child.buildStringRecursive(sb, level + 1, skipSrcPath);
        }
    }

    /**
     * For debugging only. Called by {@code buildStringRecursive()} to display
     * information for this node only.
     * <p>
     * An example is
     * 
     * <pre>
     * PackageNode "mx.core" 12:1 436-465667 D:\tests\UIComponent.as
     * </pre>.
     * <p>
     * The type of node (PackageNode) is displayed first, followed by
     * node-specific information ("mx.core") followed by location information in
     * the form
     * 
     * <pre>
     * line:column start-end sourcepath
     * </pre>
     * 
     */
    private void buildOuterString(StringBuilder sb, boolean skipSrcPath)
    {
        // First display the type of node, as represented by 
        // the short name of the node class.
        sb.append(getNodeKind());

        sb.append("(").append(getNodeID().name()).append(")");
        
        sb.append(' ');

        // Display optional node-specific information in the middle.
        if (buildInnerString(sb))
            sb.append(' ');

        // The SourceLocation superclass handles producing a string such as
        // "17:5 160-188" C:/test.as".
        if(skipSrcPath) 
            sb.append(getOffsetsString());
        else
            sb.append(super.toString());
    }

    /**
     * For debugging only. This method is called by {@code buildOuterString()}.
     * It is overridden by subclasses to display optional node-specific
     * information in the middle of the string, between the node type and the
     * location information.
     */
    protected boolean buildInnerString(StringBuilder sb)
    {
        return false;
    }
    
    /**
     * For debugging only.
     */
    public String getInnerString()
    {
        StringBuilder sb = new StringBuilder();
        buildInnerString(sb);
        return sb.toString();
    }

    public ASFileScope getFileScope()
    {
        ASScope scope = getASScope();
        assert scope != null;
        while (!(scope instanceof ASFileScope))
        {
            scope = scope.getContainingScope();
            assert scope != null;
        }
        return (ASFileScope)scope;
    }

    /**
     * @return {@link OffsetLookup} object for the current tree or null.
     */
    protected final OffsetLookup tryGetOffsetLookup()
    {
        // Try FileNode.getOffsetLookup()
        final IASNode fileNode = getAncestorOfType(IFileNode.class);
        if (fileNode != null)
            return ((IFileNode)fileNode).getOffsetLookup();
        else
            return null;
    }

    /**
     * Get's the {@link IWorkspace} in which this {@link NodeBase} lives.
     * 
     * @return The {@link IWorkspace} in which this {@link NodeBase} lives.
     */
    public IWorkspace getWorkspace()
    {
        return getFileScope().getWorkspace();
    }

    /**
     * Get the scope this Node uses for name resolution as an ASScope.
     * 
     * @return the ASScope for this node, or null if there isn't one.
     */
    public ASScope getASScope()
    {
        IScopedNode scopeNode = getContainingScope();
        IASScope scope = scopeNode != null ? scopeNode.getScope() : null;

        // If the ScopedNode had a null scope, keep looking up the tree until we
        // find one with a non-null scope.
        // TODO: Is it a bug that an IScopedNode returns null for it's scope?
        // TODO: this seems like a leftover from block scoping - for example, a
        // TODO: ForLoopNode is an IScopedNode, but it doesn't really have a scope
        while (scope == null && scopeNode != null)
        {
            scopeNode = scopeNode.getContainingScope();
            scope = scopeNode != null ? scopeNode.getScope() : null;
        }

        if (scope instanceof TypeScope)
        {
            TypeScope typeScope = (TypeScope)scope;
            if (this instanceof BaseDefinitionNode)
            {
                if (((BaseDefinitionNode)this).hasModifier(ASModifier.STATIC))
                    scope = typeScope.getStaticScope();
                else
                    scope = typeScope.getInstanceScope();
            }
            else
            {
                // Do we need the class or instance scope?
                BaseDefinitionNode bdn = (BaseDefinitionNode)this.getAncestorOfType(BaseDefinitionNode.class);
                if (bdn instanceof ClassNode)
                {
                    // We must be loose code in a class
                    scope = typeScope.getStaticScope();
                }
                else
                {
                    if (bdn != null && bdn.hasModifier(ASModifier.STATIC)
                            // Namespaces are always static
                            || bdn instanceof NamespaceNode)
                        scope = typeScope.getStaticScope();
                    else
                        scope = typeScope.getInstanceScope();
                }
            }
        }
        ASScope asScope = scope instanceof ASScope ? (ASScope)scope : null;
        return asScope;
    }

    /**
     * Get the node's local start offset.
     */
    @Override
    public int getStart()
    {
        final OffsetLookup offsetLookup = tryGetOffsetLookup();
        if (offsetLookup != null)
        {
            // to handle start offset in an included file
            int absoluteOffset = getAbsoluteStart();
            final String pathBeforeCaret = offsetLookup.getFilename(absoluteOffset - 1);
            // if the offset is the first offset in the included file return without adjustment
            if (pathBeforeCaret != null && pathBeforeCaret.equals(getSourcePath()))
                return offsetLookup.getLocalOffset(absoluteOffset-1)+1;
            
            return offsetLookup.getLocalOffset(absoluteOffset);
        }
        
        return super.getStart();
    }

    /**
     * Get the node's local end offset.
     */
    @Override
    public int getEnd()
    {
        final OffsetLookup offsetLookup = tryGetOffsetLookup();
        return offsetLookup != null ?
                // to handle end offset in an included file
                offsetLookup.getLocalOffset(super.getEnd() - 1) + 1 :
                super.getEnd();
    }

    /**
     * @return The node's absolute start offset.
     */
    @Override
    public int getAbsoluteStart()
    {
        return super.getStart();
    }

    /**
     * @return The node's absolute end offset.
     */
    @Override
    public int getAbsoluteEnd()
    {
        return super.getEnd();
    }
    
    /**
     * Collects the import nodes that are descendants of this node
     * but which are not contained within a scoped node.
     * <p>
     * This is a helper method for {@link IScopedNode#getAllImportNodes}().
     * Since that method walks up the chain of scoped nodes, we don't want
     * to look inside scoped nodes that were already processed.
     * 
     * @param importNodes The collection of import nodes being built.
     */
    public void collectImportNodes(Collection<IImportNode> importNodes)
    {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i)
        {
            IASNode child = getChild(i);
            
            if (child instanceof IImportNode)
                importNodes.add((IImportNode)child);

            else if (!(child instanceof IScopedNode))
                ((NodeBase)child).collectImportNodes(importNodes);
        }
    }

    /**
     * Used only in asserts.
     */
    public boolean verify()
    {
        // Verify the id.
        ASTNodeID id = getNodeID();
        assert id != null &&
               id != ASTNodeID.InvalidNodeID &&
               id != ASTNodeID.UnknownID : "Definition has bad id";

        // TODO: Verify the source location eventually.
        //      assert getSourcePath() != null : "Node has null source path:\n" + toString();
        //      assert getStart() != UNKNOWN : "Node has unknown start:\n" + toString();
        //      assert getEnd() != UNKNOWN : "Node has unknown end:\n" + toString();
        //      assert getLine() != UNKNOWN : "Node has unknown line:\n" + toString();
        //      assert getColumn() != UNKNOWN : "Node has unknown column:\n" + toString();

        // Verify the children.
        int n = getChildCount();
        for (int i = 0; i < n; i++)
        {
            // Any null children?
            NodeBase child = (NodeBase)getChild(i);
            assert child != null : "Node has null child";

            // Does each child have this node as its parent?
            // (Note: Two node classes override getParent() to not return the parent,
            // so exclude these for now.)
            if (!(child instanceof NamespaceIdentifierNode || child instanceof QualifiedNamespaceExpressionNode))
            {
                assert child.getParent() == this : "Child node has bad parent";
            }

            // Recurse on each child.
            child.verify();
        }

        return true;
    }
    
    /**
     * Counts various types of nodes that are created,
     * as well as the total number of nodes.
     */
    private void countNodes()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("ASScopeBase incrementing counter for " + getClass().getSimpleName());
        Counter counter = Counter.getInstance();
        counter.incrementCount(getClass().getSimpleName());
        counter.incrementCount("nodes");
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("ASScopeBase done incrementing counter for " + getClass().getSimpleName());
    }
}
