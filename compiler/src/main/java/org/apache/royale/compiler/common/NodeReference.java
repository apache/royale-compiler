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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

import java.lang.ref.WeakReference;

/**
 * Helper class to abstract the details of how we get back to Nodes from Definitions and NamespaceReferences.
 *
 * This class holds a WeakRef to the IASNode, and is able to regenerate the IASNode if needed after it has been collected.
 */
public class NodeReference
{
    /**
     * Construct a NodeReference to the given node
     * @param node  the node to references
     */
    public NodeReference(IASNode node)
    {
        absoluteStart = node.getAbsoluteStart();
        nodeRef = new WeakReference<IASNode>(node);
        fileSpec = node.getFileSpecification();
    }
    
    /**
     * Construct a NodeReference to a node in the specified file at the
     * specified offset. This constructor is used from the MXMLScopeBuilder,
     * where we only have temporary tree nodes.
     * 
     * @param containingFileSpec File that contains the node this reference
     * refers to.
     * @param absoluteStart The offset at which the node this reference refers to
     * starts.
     */
    public NodeReference(IFileSpecification containingFileSpec, int absoluteStart)
    {
        this.absoluteStart = absoluteStart;
        nodeRef = new WeakReference<IASNode>(null);
        fileSpec = containingFileSpec;
    }

    // The node that produced this definition, if this definition came from source.
    // This is stored as a weak reference so that symbol tables don't prevent ASTs
    // from being garbage-collected.
    // If the reference is lost, getNode() restores it by using
    // 'fileSpec' to reparse the AST and 'start' to find the correct node in that AST.
    private WeakReference<IASNode> nodeRef;

    // The {@link IFileSpecification} from which this definition came.
    private IFileSpecification fileSpec;

    // The absolute starting offset of this definition.
    private int absoluteStart = ISourceLocation.UNKNOWN;

    /**
     * Get the file spec for this Node
     */
    public IFileSpecification getFileSpecification()
    {
        return fileSpec;
    }

    /**
     * Get the start offset for the node
     */
    public int getAbsoluteStart()
    {
        return absoluteStart;
    }
    
    /**
     * Finds the node closest to the root whose start offset is exactly the
     * specified offset.
     * <p>
     * This method differs from
     * {@link IASNode#getContainingNode(int)}
     * in that it considers the start of a node to be contained by the node.
     * 
     * @param root Node to start the search at.
     * @param absoluteOffset Start offset of the node to find.
     * @return The node closest to the specified root node whose start offset is
     * the specified offset, or null if no such node exists.
     */
    private static IASNode findNode(IASNode root, int absoluteOffset)
    {
        IASNode current = root;
        assert root.getAbsoluteEnd() >= absoluteOffset;
        
        // These two nested while loops rely on the fact that
        // the start and end offset of a node enclose the start
        // offset of the nodes first child and the end offset of the
        // nodes last child.
        while ((current != null) && (current.getAbsoluteStart() != absoluteOffset))
        {
            assert current.getAbsoluteStart() < absoluteOffset;
            assert current.getAbsoluteEnd() >= absoluteOffset;
            int childCount = current.getChildCount();
            int i = 0;
            IASNode next = null;
            // This inner loop computes which child node to descend to.
            // We want to descend to the child contains the
            // start offset.  The child nodes are always in file order,
            // and their source ranges do not overlap.
            while ((i < childCount) && (next == null))
            {
                IASNode currChild = current.getChild(i);
                // Once we find a child that ends after the offset
                // we are looking for, descend to that child.
                if (currChild != null && currChild.getAbsoluteEnd() >= absoluteOffset)
                    next = currChild; 
                ++i;
            }
            current = next;
        }
        return current;
    }

    /**
     * Get a strong reference to the Node.  If necessary this will reparse the file to find the correct Node.
     * This method may return null, such as when document is so malformed
     * that we can't produce a file node for it.
     * (Note that MXMLScopeBuilder can produce definitions with NodeReferences into a malformed document.)
     * @param workspace     the Workspace to use to parse the file
     * @param scope         the containing scope of the definition we want the node for - this is used
     *                      to find the ASFileScope it's contained in to reconnect the definitions and Nodes
     *                      if the file needs to be reparsed
     */
    public IASNode getNode(IWorkspace workspace, ASScope scope)
    {
        // If this definition didn't come from source, return null.
        if (fileSpec == null || absoluteStart == ISourceLocation.UNKNOWN)
            return null;

        // If the definition node is still weakly attached, return it.
        IASNode node = nodeRef.get();
        if (node != null)
            return node;


        // Reparse the file from which this definition came
        // to create a new AST.
        // TODO Detect whether the file has changed since the definition was created.
        if( workspace != null )
        {
            ASScope s = scope;
            ASFileScope fileScope = null;
            // Get the file scope, and reconnect the AST to the definitions/scopes that
            // already exist
            while( s != null )
            {
                if( s instanceof ASFileScope )
                {
                    fileScope = (ASFileScope)s;
                }
                s = s.getContainingScope();
            }
            if( fileScope != null) {

            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.NODE_REFERENCES) == CompilerDiagnosticsConstants.NODE_REFERENCES)
            		System.out.println("NodeReference getting lock for " + fileSpec.getPath());
                // Grab the lock
                synchronized (this)
                {
                    // Check again in case another thread already updated the node reference
                    node = nodeRef.get();
                    if(node != null )
                        return node;

                    // Get the file node from the file scope
                    IASNode fileNode = fileScope.getNode();

                    if (fileNode != null)
                    {
                        // Find the node that produced this definition,
                        // using the starting offset stored in this definition.
                        node = findNode(fileNode, absoluteStart);

                        // Reattach a weak reference to the node.
                        nodeRef = new WeakReference<IASNode>(node);
                    }
                }
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.NODE_REFERENCES) == CompilerDiagnosticsConstants.NODE_REFERENCES)
            		System.out.println("NodeReference done with lock for " + fileSpec.getPath());
            }
        }
        return node;
    }

    /**
     * Get the node from the underlying weak reference.  This will not do any parsing
     * or any other work to recover the Node if it has been collected
     * @return  the IASNode this reference refers to, or null if the Node no longer exists
     */
    public IASNode getNodeIfExists()
    {
        return nodeRef != null ? nodeRef.get() : null;
    }
    
    /**
     * Explicitly re-connect this node reference to the specified node.
     * @param node The {@link IASNode} to re-connect this node reference to.
     */
    public void reconnectNode(IASNode node)
    {
        assert node.contains(absoluteStart);
        nodeRef = new WeakReference<IASNode>(node);
    }

    /**
     * static that represents no NodeReference (e.g. for defs that came from an ABC)
     */
    public static NodeReference noReference = new NodeReference();

    /**
     * Private ctor only used by the noReference instance.
     */
    private NodeReference()
    {
        this.nodeRef = null;
        this.fileSpec = null;
        this.absoluteStart = ISourceLocation.UNKNOWN;
    }
}
