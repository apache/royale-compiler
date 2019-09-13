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
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.DuplicateImportAliasProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IImportNode;

/**
 * ActionScript parse tree node representing an import statement
 */
public class ImportNode extends FixedChildrenNode implements IImportNode
{
    /**
     * Builds an ImportNode for a qname.
     */
    public static ImportNode buildImportNode(String qname)
    {
        Deque<IASNode> nodeList = new LinkedList<IASNode>();
        for (String s : qname.split("\\."))
        {
            nodeList.add(new IdentifierNode(s));
        }
        IASToken dotToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_MEMBER_ACCESS, -1, -1, -1, -1, ".");
        while (nodeList.size() > 1)
        {
            ExpressionNodeBase first = (ExpressionNodeBase)nodeList.removeFirst();
            ExpressionNodeBase second = (ExpressionNodeBase)nodeList.removeFirst();
            nodeList.addFirst(new FullNameNode(first, dotToken, second));
        }
        ImportNode importNode = new ImportNode((ExpressionNodeBase)nodeList.getFirst());
        return importNode;
    }

    /**
     * Turns a qualified name such as <code>"flash.display.Sprite"</code>
     * into a wildcard name like <code>"flash.display.*"</code>.
     */
    public static String makeWildcardName(String name)
    {
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1)
            name = name.substring(0, lastDot) + ".*";
        return name;
    }

    /**
     * Constructor.
     * 
     * @param targetImportNode package to import
     */
    public ImportNode(ExpressionNodeBase targetImportNode)
    {
        this.setImportTarget(targetImportNode);
        importKind = ImportKind.AS_SCOPED_IMPORT;
    }

    /**
     * Package to import
     */
    protected ExpressionNodeBase targetImportNode;

    protected ImportKind importKind;
    
    protected String importAlias;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ImportID;
    }

    @Override
    public int getChildCount()
    {
        return targetImportNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return targetImportNode;
        
        return null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (targetImportNode != null)
        {
            targetImportNode.normalize(fillInOffsets);
            targetImportNode.setParent(this);
        }
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        setChildren(fillInOffsets);
        
        super.normalize(fillInOffsets);
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            if (importAlias != null)
            {
                if (scope.hasImportAlias(importAlias))
                {
                    //we can't have duplicates, or it will be ambiguous -JT
                    FileNode fileNode = (FileNode) getAncestorOfType(FileNode.class);
                    fileNode.addProblem(new DuplicateImportAliasProblem(this, this.getImportAlias()));
                }
                else
                {
                    scope.addImport(this.getImportName(), this.getImportAlias());
                }
            }
            else
            {
                scope.addImport(this.getImportName());
            }
        }
    }
    
    /*
     * For debugging only. Builds a string such as
     * <code>"flash.display.Sprite"</code> from the import name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getImportName());
        sb.append('"');

        return true;
    }
    
    //
    // IImportNode implementations
    //

    @Override
    public ImportKind getImportKind()
    {
        if (getAbsoluteStart() == getAbsoluteEnd())
            return ImportKind.IMPLICIT_IMPORT;
        
        return importKind;
    }
    
    @Override
    public IExpressionNode getImportNameNode()
    {
        return targetImportNode;
    }

    @Override
    public String getImportName()
    {
        return targetImportNode instanceof IIdentifierNode ? ((IIdentifierNode)targetImportNode).getName() : "";
    }

    @Override
    public IImportTarget getImportTarget()
    {
        return ASImportTarget.get(getWorkspace(), getImportName());
    }

    @Override
    public boolean isWildcardImport()
    {
        String targetPackage = getImportName();
        return (targetPackage.endsWith(".") || targetPackage.endsWith("*"));
    }
    
    @Override
    public IDefinition resolveImport(ICompilerProject project)
    {
        if (isWildcardImport())
            return null;
        
        String importName = getImportName();
        IReference importReference = ReferenceFactory.packageQualifiedReference(
            project.getWorkspace(), importName);
        ASScope scope = (ASScope)getScopeNode().getScope();
        return importReference.resolve(project, scope, null, true);
    }

    //
    // Other methods
    //

    /**
     * Sets the type of import we have encountered. Used during parsing,
     * defaults to AS_SCOPED_IMPORT
     * 
     * @param importKind The type of import.
     */
    public void setImportKind(ImportKind importKind)
    {
        this.importKind = importKind;
    }
    
    /**
     * Sets the import target. Used during parsing.
     * 
     * @param targetImportNode The node representing the import target.
     */
    public void setImportTarget(ExpressionNodeBase targetImportNode)
    {
        this.targetImportNode = targetImportNode;

        if (targetImportNode != null)
        {
            setEnd(targetImportNode.getAbsoluteEnd());
            setEndLine(targetImportNode.getEndLine());
            setEndColumn(targetImportNode.getEndColumn());
        }
    }

    /**
     * Gets the optional alias of the import.
     */
    public String getImportAlias()
    {
        return this.importAlias;
    }

    /**
     * Sets the optional alias of the import.
     *
     * @param importAlias The alias of the import.
     */
    public void setImportAlias(String importAlias)
    {
        this.importAlias = importAlias;
    }
}
