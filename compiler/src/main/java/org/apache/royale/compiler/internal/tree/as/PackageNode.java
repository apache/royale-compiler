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

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.IDecorationPart;
import org.apache.royale.compiler.internal.tree.as.parts.SparseDecorationPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * ActionScript parse tree node representing a package definition
 */
public class PackageNode extends MemberedNode implements IPackageNode
{
    /**
     * Constructor.
     * 
     * @param packageNameNode node holding this package name
     */
    public PackageNode(ExpressionNodeBase packageNameNode, IASToken packageKeyword)
    {
        super();
        init(packageNameNode);
        this.packageNameNode = packageNameNode;
        contentsNode = new ScopedBlockNode();
        if (packageKeyword != null)
            packageKeywordStart = packageKeyword.getStart();
        else
            packageKeywordStart = -1;
    }

    /**
     * Start offset for the package keyword
     */
    protected int packageKeywordStart;

    /**
     * The name of this package
     */
    protected ExpressionNodeBase packageNameNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.PackageID;
    }
    
    @Override
    public String getPackageName()
    {
        return getName();
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (packageNameNode == null)
            packageNameNode = new NilNode();
        addChildInOrder(packageNameNode, fillInOffsets);
        addChildInOrder(contentsNode, fillInOffsets);
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            PackageDefinition definition = buildDefinition();
            setDefinition(definition);
            /*
             * This could be improved because neither code model or the
             * compiler will ever dig package definitions out of there by name.
             * CM won't get them out of a file scope by name because of this
             * check here. The compiler won't ever get them out of a file scope
             * by name because the namespace specifier on all package
             * definitions is the CM implicit namespace. I opted to leave the
             * package definitions in the file scope for two reasons: #1. We
             * need to pin the package scopes otherwise they and their contents
             * will get GC'd. #2. There is code that enumerates the contents of
             * the file scope and that code wants to find package definitions in
             * there. This could be cleaned up somewhat by having a list of
             * package scopes on every file scope and then changing things such
             * that package scopes are not added the file scope's symbol table.
             */
            scope.addDefinition(definition);
            PackageScope packageScope = new PackageScope(scope, definition.getQualifiedName(), contentsNode);
            definition.setContainedScope(packageScope);
            scope = packageScope;
        }

        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            reconnectDef(scope);
            scope = this.getDefinition().getContainedScope();
            contentsNode.reconnectScope(scope);
        }

        // Recurse on the package block.
        contentsNode.analyze(set, scope, problems);
    }
    
    /*
     * For debugging only. Builds a string such as <code>"mx.core"</code> from
     * the package name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }
    
    //
    // TreeNode overrides
    //
    
    @Override
    protected int getInitialChildCount()
    {
        return 2;
    }

    //
    // BaseDefinitionNode overrides
    //
    
    @Override
    protected IDecorationPart createDecorationPart()
    {
        return SparseDecorationPart.EMPTY_DECORATION_PART;
    }
    
    @Override
    public IMetaTagsNode getMetaTags()
    {
        return null;
    }
    
    @Override
    public String getNamespace()
    {
        return null;
    }
    
    @Override
    public boolean hasNamespace(String namespace)
    {
        return false;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return false;
    }

    @Override
    public ExpressionNodeBase getNameExpressionNode()
    {
        return packageNameNode;
    }

    @Override
    public String getName()
    {
        return (packageNameNode instanceof IIdentifierNode) ? ((IIdentifierNode)packageNameNode).getName() : "";
    }

    @Override
    public int getNameStart()
    {
        return packageNameNode instanceof IIdentifierNode ? packageNameNode.getStart() : -1;
    }

    @Override
    public int getNameEnd()
    {
        return packageNameNode instanceof IIdentifierNode ? packageNameNode.getEnd() : -1;
    }

    @Override
    public int getNameAbsoluteStart()
    {
        return packageNameNode instanceof IIdentifierNode ? packageNameNode.getAbsoluteStart() : -1;
    }

    @Override
    public int getNameAbsoluteEnd()
    {
        return packageNameNode instanceof IIdentifierNode ? packageNameNode.getAbsoluteEnd() : -1;
    }
    
    @Override
    public PackageDefinition getDefinition()
    {
        return (PackageDefinition)super.getDefinition();
    }
    
    //
    // IPackageNode implementations
    //
    
    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public String getQualifiedName()
    {
        return getName();
    }

    @Override
    public String getShortName()
    {
        return getName();
    }
    
    @Override
    public PackageKind getPackageKind()
    {
        return PackageKind.CONCRETE;
    }

    //
    // Other methods
    //

    /**
     * Sets the name of this package. Should only be used during parsing.
     * 
     * @param packageNameNode the name of the package
     */
    public void setPackageName(ExpressionNodeBase packageNameNode)
    {
        this.packageNameNode = packageNameNode;
    }

    /**
     * Determine whether or not this node has a package keyword
     * 
     * @return true if the node has a package keyword
     */
    public boolean hasPackageKeyword()
    {
        return packageKeywordStart != -1;
    }

    /**
     * Get the start offset for the package keyword
     * 
     * @return start offset for "package"
     */
    public int getPackageKeywordStart()
    {
        return packageKeywordStart;
    }

    /**
     * Get the end offset for the package keyword
     * 
     * @return end offset for "package"
     */
    public int getPackageKeywordEnd()
    {
        return packageKeywordStart + 7;
    }

    PackageDefinition buildDefinition()
    {
        String definitionName = getName();
        PackageDefinition definition = new PackageDefinition(definitionName);
        definition.setNode(this);
        this.setDefinition(definition);

        return definition;
    }
}
