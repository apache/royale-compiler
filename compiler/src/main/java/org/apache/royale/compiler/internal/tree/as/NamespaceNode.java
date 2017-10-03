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

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition.NamespaceClassification;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * AST node for namespace definition.
 */
public class NamespaceNode extends BaseDefinitionNode implements INamespaceNode, IInitializableDefinitionNode
{
    private static final String EMPTY_URI = "";

    /**
     * Create a {@code NamespaceNode} from the namespace name identifier.
     * 
     * @param nameNode Namespace name.
     */
    public NamespaceNode(IdentifierNode nameNode)
    {
        super();
        
        init(nameNode);
    }

    /**
     * Initialized namespace URI value.
     */
    private ExpressionNodeBase uriNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public final ASTNodeID getNodeID()
    {
        return ASTNodeID.NamespaceID;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        if (uriNode != null)
            addChildInOrder(uriNode, fillInOffsets);
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            // Make sure we respect the static modifier even for code outside of a class,
            // as FB still needs lookups to work on broken code.
            boolean is_static = this.hasModifier(ASModifier.STATIC);

            // Namespace definitions inside of a class are always static
            if (scope.getContainingDefinition() instanceof IClassDefinition)
                is_static = true;

            NamespaceDefinition definition = buildDefinition(is_static);
            setDefinition(definition);
            scope.addDefinition(definition);
        }

        super.analyze(set, scope, problems);
    }
    
    //
    // BaseDefinitionNode overrides
    //
    
    @Override
    public NamespaceDefinition getDefinition()
    {
        return (NamespaceDefinition)super.getDefinition();
    }

    //
    // INamespaceNode implementations
    //

    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public String getQualifiedName()
    {
        String qualifiedName = null;
        
        if (getNamespaceClassification() == NamespaceClassification.PACKAGE_MEMBER)
        {
            IImportTarget importTarget = ASImportTarget.buildImportFromPackageName(getWorkspace(), getPackageName());
            qualifiedName = importTarget.getQualifiedName(getName());
        }
        
        if (qualifiedName == null)
            qualifiedName = getName();
        
        return qualifiedName;
    }

    @Override
    public String getShortName()
    {
        return getName();
    }
    
    @Override
    public NamespaceClassification getNamespaceClassification()
    {
        IScopedNode scopedNode = getScopeNode();
        IASNode node = scopedNode;
        
        if (node.getParent() instanceof PackageNode)
            return NamespaceClassification.PACKAGE_MEMBER;
        
        if (node instanceof ICommonClassNode || node.getParent() instanceof ICommonClassNode)
            return NamespaceClassification.CLASS_MEMBER;
        
        if (node instanceof FileNode)// this is an include
            return NamespaceClassification.FILE_MEMBER;
        
        return NamespaceClassification.LOCAL;
    }

    @Override
    public ExpressionNodeBase getNamespaceURINode()
    {
        return uriNode;
    }

    @Override
    public String getURI()
    {
        return uriNode instanceof LiteralNode ? ((LiteralNode)uriNode).getValue() : EMPTY_URI;
    }
    
    //
    // IInitializableDefinitionNode implementations
    //
    
    @Override
    public void setAssignedValue(IASToken eq, ExpressionNodeBase value)
    {
        uriNode = value;
    }

    
    //
    // Other methods
    //

    /**
     * @return URI string or null.
     */
    private String getURILiteral()
    {
        return uriNode instanceof LiteralNode ? ((LiteralNode)uriNode).getValue() : null;
    }

    /**
     * Build a namespace definition for this node.
     * 
     * @param is_static true if this should be treated as a "static" namespace -
     * all namespaces in a class are static, though they won't contain the
     * static modifier, which is why we have this flag rather than just checking
     * the modifier
     * @return the NamespaceDefinition representing this namespace in the symbol
     * table
     */
    NamespaceDefinition buildDefinition(boolean is_static)
    {
        String definitionName = getName();

        INamespaceReference namespaceReference = NamespaceDefinition.createNamespaceReference(
                getASScope(), getNamespaceNode(), is_static);

        ExpressionNodeBase initExpr = getNamespaceURINode();
        
        NamespaceDefinition definition =
                NamespaceDefinition.createNamespaceDefintionDirective(namespaceReference, getScope(), definitionName, getURILiteral(), initExpr != null ? initExpr.computeNamespaceReference() : null);
        definition.setNode(this);

        fillInModifiers(definition);
        fillInMetadata(definition);

        return definition;
    }

    private IASScope getScope()
    {
        IASScope scope = getScopeNode().getScope();
        if (scope instanceof TypeScope)
        {
            // Namespaces in a class are always static
            scope = ((TypeScope)scope).getStaticScope();
        }
        return scope;
    }
}
