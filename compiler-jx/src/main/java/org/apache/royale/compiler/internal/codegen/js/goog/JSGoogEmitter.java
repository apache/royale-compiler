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

package org.apache.royale.compiler.internal.codegen.js.goog;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants.BuiltinType;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitter;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition.INamepaceDeclarationDirective;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.VariableUsedBeforeDeclarationProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IEmbedNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSGoogEmitter extends JSEmitter implements IJSGoogEmitter
{
    protected List<String> propertyNames = new ArrayList<String>();

    // TODO (mschmalle) Remove this (not used in JSRoyaleEmitter and JSGoogEmitter anymore)
    public ICompilerProject project;

    private JSGoogDocEmitter docEmitter;
    
    // TODO (mschmalle) Fix; this is not using the backend doc strategy for replacement
    @Override
    public IJSGoogDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSGoogDocEmitter(this);
        return docEmitter;
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return name;
    }

    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    // XXX DEAD
    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        /* goog.provide('x');\n\n */
        write(JSGoogEmitterTokens.GOOG_PROVIDE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(type.getQualifiedName());
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
    }

    // XXX DEAD
    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();

        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        List<String> list = EmitterUtils.resolveImports(type);
        for (String imp : list)
        {
            if (imp.indexOf(JSGoogEmitterTokens.AS3.getToken()) != -1)
                continue;

            /* goog.require('x');\n */
            write(JSGoogEmitterTokens.GOOG_REQUIRE);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(imp);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
        }

        // (erikdebruin) only write 'closing' line break when there are 
        //               actually imports...
        if (list.size() > 1
                || (list.size() == 1 && list.get(0).indexOf(
                        JSGoogEmitterTokens.AS3.getToken()) == -1))
        {
            writeNewline();
        }
    }

    // XXX DEAD
    @Override
    public void emitPackageContents(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        
        ITypeDefinition type = EmitterUtils.findType(containedScope.getAllLocalDefinitions());
        if (type != null)
        {
            ITypeNode tnode = EmitterUtils.findTypeNode(definition.getNode());
            if (tnode != null)
            {
            	getModel().primaryDefinitionQName = formatQualifiedName(type.getQualifiedName());
                getWalker().walk(tnode); // IClassNode | IInterfaceNode
            }
            return;
        }
        
        IFunctionDefinition func = EmitterUtils.findFunction(containedScope.getAllLocalDefinitions());
        if (func != null)
        {
            IFunctionNode fnode = EmitterUtils.findFunctionNode(definition.getNode());
            if (fnode != null)
            {
                getWalker().walk(fnode);
            }
            return;
        }

        IVariableDefinition variable = EmitterUtils.findVariable(containedScope.getAllLocalDefinitions());
        if (variable != null)
        {
            IVariableNode vnode = EmitterUtils.findVariableNode(definition.getNode());
            if (vnode != null)
            {
                getWalker().walk(vnode);
            }
        }
        
    	INamepaceDeclarationDirective ns = EmitterUtils.findNamespace(containedScope
                .getAllLocalDefinitions());
        if(ns != null)
        {
        	INamespaceNode nsNode = EmitterUtils.findNamespaceNode(definition.getNode());
        	if (nsNode != null)
        	{
        		getWalker().walk(nsNode);
        	}
        }
    }

    // XXX DEAD
    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    // XXX DEAD
    @Override
    public void emitClass(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        getModel().setCurrentClass(definition);

        IFunctionDefinition ctorDefinition = definition.getConstructor();

        // Static-only (Singleton) classes may not have a constructor
        if (ctorDefinition != null)
        {
            IFunctionNode ctorNode = (IFunctionNode) ctorDefinition.getNode();
            if (ctorNode != null)
            {
                // constructor
                emitMethod(ctorNode);
                write(ASEmitterTokens.SEMICOLON);
            }
            else
            {
                String qname = definition.getQualifiedName();
                if (qname != null && !qname.equals(""))
                {
                    write(qname);
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.FUNCTION);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                    write(ASEmitterTokens.SEMICOLON);
                }
            }
        }

        IDefinitionNode[] dnodes = node.getAllMemberNodes();
        for (IDefinitionNode dnode : dnodes)
        {
            if (dnode.getNodeID() == ASTNodeID.VariableID)
            {
                writeNewline();
                writeNewline();
                emitField((IVariableNode) dnode);
                write(ASEmitterTokens.SEMICOLON);
            }
            else if (dnode.getNodeID() == ASTNodeID.FunctionID)
            {
                if (!((IFunctionNode) dnode).isConstructor())
                {
                    writeNewline();
                    writeNewline();
                    emitMethod((IFunctionNode) dnode);
                    write(ASEmitterTokens.SEMICOLON);
                }
            }
            else if (dnode.getNodeID() == ASTNodeID.GetterID
                    || dnode.getNodeID() == ASTNodeID.SetterID)
            {
                writeNewline();
                writeNewline();
                emitAccessors((IAccessorNode) dnode);
                write(ASEmitterTokens.SEMICOLON);
            }
        }
    }

    // XXX Dead [InterfaceEmitter]
    @Override
    public void emitInterface(IInterfaceNode node)
    {
        ICompilerProject project = getWalker().getProject();

        getDocEmitter().emitInterfaceDoc(node, project);

        String qname = node.getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(formatQualifiedName(qname));
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
            boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                    || mnode.getNodeID() == ASTNodeID.SetterID;

            if (!isAccessor || !propertyNames.contains(qname))
            {
                writeNewline();

                write(formatQualifiedName(qname));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(mnode.getQualifiedName());

                if (isAccessor && !propertyNames.contains(qname))
                {
                    propertyNames.add(qname);
                }
                else
                {
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.FUNCTION);

                    emitParameters(((IFunctionNode) mnode).getParametersContainerNode());

                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                }

                write(ASEmitterTokens.SEMICOLON);
            }
        }
    }

    // XXX Dead
    @Override
    public void emitField(IVariableNode node)
    {
        IClassDefinition definition = EmitterUtils.getClassDefinition(node);

        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
        if (enode != null)
            def = enode.resolveType(getWalker().getProject());

        getDocEmitter().emitFieldDoc(node, def, getWalker().getProject());

        /* x.prototype.y = z */

        ModifiersSet modifierSet = node.getDefinition().getModifiers();
        String root = "";
        if (modifierSet != null && !modifierSet.hasModifier(ASModifier.STATIC))
        {
            root = JSEmitterTokens.PROTOTYPE.getToken();
            root += ASEmitterTokens.MEMBER_ACCESS.getToken();
        }
        write(definition.getQualifiedName()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + root
                + node.getName());

        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            getWalker().walk(vnode);
        }

        if (!(node instanceof ChainedVariableNode))
        {
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeNewline(ASEmitterTokens.SEMICOLON);
                    writeNewline();
                    emitField((IVariableNode) child);
                }
            }
        }
    }

    // XXX Dead [VarDeclarationEmitter]
    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        if (!(node instanceof ChainedVariableNode) && !node.isConst())
        {
            emitMemberKeyword(node);
        }

        IExpressionNode avnode = node.getAssignedValueNode();
        if (avnode != null)
        {
            IDefinition def = avnode.resolveType(getWalker().getProject());

            String opcode = avnode.getNodeID().getParaphrase();
            if (opcode != "AnonymousFunction")
                getDocEmitter().emitVarDoc(node, def, getWalker().getProject());
        }
        else
        {
            getDocEmitter().emitVarDoc(node, null, getWalker().getProject());
        }

        emitDeclarationName(node);
        if (avnode != null && !(avnode instanceof IEmbedNode))
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            emitAssignedValue(avnode);
        }

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeToken(ASEmitterTokens.COMMA);
                    emitVarDeclaration((IVariableNode) child);
                }
            }
        }
    }

    // XXX Dead 
    public void emitAccessors(IAccessorNode node)
    {
        String qname = node.getQualifiedName();
        if (!propertyNames.contains(qname))
        {
            emitField(node);
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
            writeNewline();

            propertyNames.add(qname);
        }

        if (node.getNodeID() == ASTNodeID.GetterID)
        {
            emitGetAccessor((IGetterNode) node);
        }
        else if (node.getNodeID() == ASTNodeID.SetterID)
        {
            emitSetAccessor((ISetterNode) node);
        }
    }

    // XXX Dead 
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        emitObjectDefineProperty(node);
    }

    // XXX Dead 
    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        emitObjectDefineProperty(node);
    }

    // XXX Dead [MethodEmitter]
    @Override
    public void emitMethod(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        ICompilerProject project = getWalker().getProject();

        getDocEmitter().emitMethodDoc(node, project);

        boolean isConstructor = node.isConstructor();

        String qname = EmitterUtils.getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(formatQualifiedName(qname));
            if (!isConstructor)
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                if (!fn.hasModifier(ASModifier.STATIC))
                {
                    write(JSEmitterTokens.PROTOTYPE);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                }
            }
        }

        if (!isConstructor)
            emitMemberName(node);

        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);

        emitParameters(node.getParametersContainerNode());

        boolean hasSuperClass = EmitterUtils.hasSuperClass(project, node);

        if (isConstructor && node.getScopedNode().getChildCount() == 0)
        {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            if (hasSuperClass)
                emitSuperCall(node, JSSessionModel.CONSTRUCTOR_EMPTY);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }

        if (!isConstructor || node.getScopedNode().getChildCount() > 0)
            emitMethodScope(node.getScopedNode());

        if (isConstructor && hasSuperClass)
        {
            writeNewline(ASEmitterTokens.SEMICOLON);
            write(JSGoogEmitterTokens.GOOG_INHERITS);
            write(ASEmitterTokens.PAREN_OPEN);
            write(formatQualifiedName(qname));
            writeToken(ASEmitterTokens.COMMA);
            String sname = EmitterUtils.getSuperClassDefinition(node, project)
                    .getQualifiedName();
            write(formatQualifiedName(sname));
            write(ASEmitterTokens.PAREN_CLOSE);
        }
    }

    // XXX Dead
    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        IASNode cnode = node.getChild(0);

        if (cnode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            cnode = cnode.getChild(0);

        ASTNodeID id = cnode.getNodeID();
        if (id != ASTNodeID.SuperID)
        {
            if (node.isNewExpression())
            {
                writeToken(ASEmitterTokens.NEW);
            }

            getWalker().walk(node.getNameNode());
            
            emitArguments(node.getArgumentsNode());
        }
        else
        {
            emitSuperCall(node, JSSessionModel.SUPER_FUNCTION_CALL);
        }
    }

    // XXX Dead
    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        ICompilerProject project = getWalker().getProject();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        IDefinition def = node.resolve(project);

        ITypeDefinition type = node.resolveType(project);

        IASNode pnode = node.getParent();
        ASTNodeID inode = pnode.getNodeID();

        boolean writeSelf = false;
        if (cnode != null)
        {
            IDefinitionNode[] members = cnode.getAllMemberNodes();
            for (IDefinitionNode mnode : members)
            {
                if ((type != null && type.getQualifiedName().equalsIgnoreCase(
                        IASLanguageConstants.Function))
                        || (def != null && def.getQualifiedName()
                                .equalsIgnoreCase(mnode.getQualifiedName())))
                {
                    if (!(pnode instanceof FunctionNode)
                            && inode != ASTNodeID.MemberAccessExpressionID)
                    {
                        writeSelf = true;
                        break;
                    }
                    else if (inode == ASTNodeID.MemberAccessExpressionID
                            && !def.isStatic())
                    {
                        String tname = type.getQualifiedName();
                        writeSelf = !tname.equalsIgnoreCase(cnode
                                .getQualifiedName())
                                && !tname.equals(IASLanguageConstants.Function);
                        break;
                    }
                }
            }
        }

        boolean isRunningInTestMode = cnode != null
                && cnode.getQualifiedName().equalsIgnoreCase("RoyaleTest_A");
        if (writeSelf && !isRunningInTestMode)
        {
            write(JSGoogEmitterTokens.SELF);
            write(ASEmitterTokens.MEMBER_ACCESS);
        }
        else
        {
            String pname = (type != null) ? type.getPackageName() : "";
            if (cnode != null && pname != ""
                    && !pname.equalsIgnoreCase(cnode.getPackageName())
                    && inode != ASTNodeID.ArgumentID
                    && inode != ASTNodeID.VariableID
                    && inode != ASTNodeID.TypedExpressionID)
            {
                write(pname);
                write(ASEmitterTokens.MEMBER_ACCESS);
            }
        }

        super.emitIdentifier(node);
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        ASDocComment asDoc = (ASDocComment) node
                .getASDocComment();
        if (asDoc != null)
        {
            String asDocString = asDoc.commentNoEnd();
            String debugToken = JSRoyaleEmitterTokens.DEBUG_COMMENT
                    .getToken();
            int emitIndex = asDocString.indexOf(debugToken);
            if(emitIndex != -1)
            {
                IParameterNode[] pnodes = node.getParameterNodes();

                IParameterNode rest = EmitterUtils.getRest(pnodes);
                if (rest != null)
                {
                    final StringBuilder code = new StringBuilder();
                    code.append(rest.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(rest.getName());
                    code.append(ASEmitterTokens.SEMICOLON.getToken());
                    write(code.toString());
                }
                write(JSRoyaleEmitterTokens.DEBUG_RETURN);
                writeNewline();
            }
        }
        IDefinition def = node.getDefinition();
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;
        boolean isLocal = false;
        if (node.getFunctionClassification() == IFunctionDefinition.FunctionClassification.LOCAL)
            isLocal = true;
        boolean isPackage = false;
        if (node.getFunctionClassification() == IFunctionDefinition.FunctionClassification.PACKAGE_MEMBER)
        isPackage = true;
        if (EmitterUtils.hasBody(node) && !isStatic && !isLocal && !isPackage)
            emitSelfReference(node);

        if (node.isConstructor()
                && EmitterUtils.hasSuperClass(getWalker().getProject(), node)
                && !EmitterUtils.hasSuperCall(node.getScopedNode()))
            emitSuperCall(node, JSSessionModel.CONSTRUCTOR_FULL);

        if (!getModel().isExterns)
        	emitRestParameterCodeBlock(node);

        if (!getModel().isExterns)
        	emitDefaultParameterCodeBlock(node);
    }

    // XXX Dead
    protected void emitSelfReference(IFunctionNode node)
    {
        writeToken(ASEmitterTokens.VAR);
        writeToken(JSGoogEmitterTokens.SELF);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.THIS);
        writeNewline(ASEmitterTokens.SEMICOLON);
    }

    // XXX Dead
    protected void emitSuperCall(IASNode node, String type)
    {
        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        if (type == JSSessionModel.CONSTRUCTOR_EMPTY)
        {
            indentPush();
            writeNewline();
            indentPop();
        }
        else if (type == JSSessionModel.SUPER_FUNCTION_CALL)
        {
            if (fnode == null)
                fnode = (IFunctionNode) fcnode
                        .getAncestorOfType(IFunctionNode.class);
        }

        if (fnode.isConstructor()
                && !EmitterUtils.hasSuperClass(getWalker().getProject(), fnode))
            return;

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        if (cnode == null)
        {
            IDefinition cdef = getModel().getCurrentClass();
            write(formatQualifiedName(cdef.getQualifiedName()));
        }
        else
            write(formatQualifiedName(cnode.getQualifiedName()));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);

        if (fnode.isConstructor())
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(JSGoogEmitterTokens.GOOG_CONSTRUCTOR);
            write(ASEmitterTokens.SINGLE_QUOTE);
        }

        if (fnode != null && !fnode.isConstructor())
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(fnode.getName());
            write(ASEmitterTokens.SINGLE_QUOTE);
        }

        IASNode[] anodes = null;
        boolean writeArguments = false;
        if (fcnode != null)
        {
            anodes = fcnode.getArgumentNodes();

            writeArguments = anodes.length > 0;
        }
        else if (fnode.isConstructor())
        {
            anodes = fnode.getParameterNodes();

            writeArguments = (anodes != null && anodes.length > 0);
        }

        if (writeArguments)
        {
            int len = anodes.length;
            for (int i = 0; i < len; i++)
            {
                writeToken(ASEmitterTokens.COMMA);

                getWalker().walk(anodes[i]);
            }
        }

        write(ASEmitterTokens.PAREN_CLOSE);

        if (type == JSSessionModel.CONSTRUCTOR_FULL)
        {
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }
        else if (type == JSSessionModel.CONSTRUCTOR_EMPTY)
        {
            write(ASEmitterTokens.SEMICOLON);
        }
    }

    protected void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();
        if (pnodes.length == 0)
            return;

        Map<Integer, IParameterNode> defaults = EmitterUtils
                .getDefaults(pnodes);

        if (defaults != null)
        {
            final StringBuilder code = new StringBuilder();

            if (!EmitterUtils.hasBody(node))
            {
                indentPush();
                writeIndent();
            }

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());

            for (int i = 0, n = parameters.size(); i < n; i++)
            {
                IParameterNode pnode = parameters.get(i);

                if (pnode != null)
                {
                    code.setLength(0);

                    /* x = typeof y !== 'undefined' ? y : z;\n */
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.TYPEOF.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.STRICT_NOT_EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                    code.append(ASEmitterTokens.UNDEFINED.getToken());
                    code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.TERNARY.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.COLON.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    
                    IExpressionNode assignedValueNode = pnode.getAssignedValueNode();
                    code.append(stringifyNode(assignedValueNode));
                    code.append(ASEmitterTokens.SEMICOLON.getToken());

                    write(code.toString());

                    if (i == n - 1 && !EmitterUtils.hasBody(node))
                        indentPop();

                    writeNewline();
                }
            }
        }
    }

    protected void emitRestParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();

        IParameterNode rest = EmitterUtils.getRest(pnodes);
        if (rest != null)
        {
            final StringBuilder code = new StringBuilder();

            /* x = Array.prototype.slice.call(arguments, y);\n */
            code.append(rest.getName());
            code.append(ASEmitterTokens.SPACE.getToken());
            code.append(ASEmitterTokens.EQUAL.getToken());
            code.append(ASEmitterTokens.SPACE.getToken());
            code.append(BuiltinType.ARRAY.getName());
            code.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
            code.append(JSEmitterTokens.PROTOTYPE.getToken());
            code.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
            code.append(JSEmitterTokens.SLICE.getToken());
            code.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
            code.append(JSEmitterTokens.CALL.getToken());
            code.append(ASEmitterTokens.PAREN_OPEN.getToken());
            code.append(JSEmitterTokens.ARGUMENTS.getToken());
            code.append(ASEmitterTokens.COMMA.getToken());
            code.append(ASEmitterTokens.SPACE.getToken());
            code.append(String.valueOf(pnodes.length - 1));
            code.append(ASEmitterTokens.PAREN_CLOSE.getToken());
            code.append(ASEmitterTokens.SEMICOLON.getToken());

            write(code.toString());

            writeNewline();
        }
    }

    @Override
    public void emitAssignedValue(IExpressionNode node)
    {
        if (node == null)
        {
            return;
        }
        IDefinition definition = node.resolve(getWalker().getProject());
        if (node.getNodeID() == ASTNodeID.IdentifierID && node.getParent().getNodeID() == ASTNodeID.VariableID)
        {
        	if (definition instanceof VariableDefinition && (!(definition.getParent() instanceof ClassDefinition)))
        	{
        		IFileSpecification defFile = ((VariableDefinition)definition).getFileSpecification();
        		IFileSpecification varFile = node.getFileSpecification();
        		if (defFile != null && varFile != null && varFile.equals(defFile))
        		{
        			if (node.getAbsoluteStart() < definition.getAbsoluteStart())
        			{
        				getProblems().add(new VariableUsedBeforeDeclarationProblem(node, definition.getBaseName()));
        			}
        		}
        	}
        }
        if (node.getNodeID() == ASTNodeID.ClassReferenceID)
        {
            write(definition.getQualifiedName());
        }
        else
        {
            if (definition instanceof IFunctionDefinition &&
            		node.getNodeID() == ASTNodeID.NamespaceAccessExpressionID)
            {
            	// can't do normal walk.  Need to generate closure
            	// so walk just the right operand
            	getWalker().walk(node.getChild(1));
            }
            else
            	getWalker().walk(node);
        }
    }

    // XXX Dead
    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        write(JSGoogEmitterTokens.GOOG_ARRAY_FOREACH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.COMMA);
        writeToken(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        if (childNode instanceof IVariableExpressionNode)
            write(((IVariableNode) childNode.getChild(0)).getName());
        else
            write(((IIdentifierNode) childNode).getName());
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        if (isImplicit(xnode))
            write(ASEmitterTokens.BLOCK_OPEN);
        getWalker().walk(node.getStatementContentsNode());
        if (isImplicit(xnode))
        {
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    public JSGoogEmitter(FilterWriter out)
    {
        super(out);
    }

    // XXX Dead
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        /*
        Object.defineProperty(
            A.prototype, 
            'foo', 
            {get: function() {return -1;}, 
            configurable: true}
         );
        */

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        // head
        write(JSGoogEmitterTokens.OBJECT);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTY);
        writeNewline(ASEmitterTokens.PAREN_OPEN, true);

        // Type
        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();
        write(type.getQualifiedName());
        if (!node.hasModifier(ASModifier.STATIC))
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
        }
        writeToken(ASEmitterTokens.COMMA);
        writeNewline();

        // name
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(definition.getBaseName());
        write(ASEmitterTokens.SINGLE_QUOTE);
        writeToken(ASEmitterTokens.COMMA);
        writeNewline();

        // info object
        // declaration
        write(ASEmitterTokens.BLOCK_OPEN);
        write(node.getNodeID() == ASTNodeID.GetterID ? ASEmitterTokens.GET
                : ASEmitterTokens.SET);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.FUNCTION);
        emitParameters(node.getParametersContainerNode());

        emitDefinePropertyFunction(node);

        writeToken(ASEmitterTokens.COMMA);
        write(JSEmitterTokens.CONFIGURABLE);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.TRUE);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE, false);

        // tail, no colon; parent container will add it
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    // XXX Dead
    protected void emitDefinePropertyFunction(IAccessorNode node)
    {
        emitMethodScope(node.getScopedNode());
    }

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    @Override
    public void emitNamespaceAccessExpression(
            INamespaceAccessExpressionNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(ASEmitterTokens.MEMBER_ACCESS);
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitAsOperator(IBinaryOperatorNode node)
    {
        emitBinaryOperator(node);
    }

    @Override
    public void emitIsOperator(IBinaryOperatorNode node)
    {
        emitBinaryOperator(node);
    }

    // XXX Dead
    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);

        ASTNodeID id = node.getNodeID();

        if (id == ASTNodeID.Op_IsID)
        {
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else if (id == ASTNodeID.Op_AsID)
        {
            // (is(a, b) ? a : null)
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            writeToken(ASEmitterTokens.PAREN_CLOSE);
            writeToken(ASEmitterTokens.TERNARY);
            getWalker().walk(node.getLeftOperandNode());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.NULL);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else
        {
            getWalker().walk(node.getLeftOperandNode());

            if (id != ASTNodeID.Op_CommaID)
                write(ASEmitterTokens.SPACE);

            // (erikdebruin) rewrite 'a &&= b' to 'a = a && b'
            if (id == ASTNodeID.Op_LogicalAndAssignID
                    || id == ASTNodeID.Op_LogicalOrAssignID)
            {
                IExpressionNode lnode = node
                        .getLeftOperandNode();

                writeToken(ASEmitterTokens.EQUAL);
                getWalker().walk(lnode);
                write(ASEmitterTokens.SPACE);
                write((id == ASTNodeID.Op_LogicalAndAssignID) ? ASEmitterTokens.LOGICAL_AND
                        : ASEmitterTokens.LOGICAL_OR);
            }
            else
            {
                write(node.getOperator().getOperatorText());
            }

            write(ASEmitterTokens.SPACE);

            getWalker().walk(node.getRightOperandNode());
        }

        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    protected void emitClosureStart(FunctionNode node)
    {
        write(JSGoogEmitterTokens.GOOG_BIND);
        write(ASEmitterTokens.PAREN_OPEN);
    }
    
    protected void emitClosureEnd(FunctionNode node, IDefinition nodeDef)
    {
        write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    @Override
    public void emitContainer(IContainerNode node)
    {
    	// saw this in a for loop with multiple initializer statements: for (var i = 0, j = 0; ...
        int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = node.getChild(i);
            if (i == 0)
            	getWalker().walk(child);
            else
            {
            	String s = stringifyNode(child);
            	if (s.startsWith("var "))
            	{
            		s = s.substring(4);
            		write(s);
            	}
            }
            if (i < len - 1)
            	write(", ");
        }    	
    }
}
