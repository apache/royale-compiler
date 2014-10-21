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

package org.apache.flex.compiler.internal.codegen.js.goog;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.codegen.IASGlobalFunctionConstants.BuiltinType;
import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitter;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.scopes.PackageScope;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IEmbedNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSGoogEmitter extends JSEmitter implements IJSGoogEmitter
{

    protected static final String CONSTRUCTOR_EMPTY = "emptyConstructor";
    protected static final String CONSTRUCTOR_FULL = "fullConstructor";
    protected static final String SUPER_FUNCTION_CALL = "replaceSuperFunction";

    protected List<String> propertyNames = new ArrayList<String>();

    protected ICompilerProject project;

    protected IJSGoogDocEmitter getDoc()
    {
        return (IJSGoogDocEmitter) getDocEmitter();
    }

    @Override
    public IDocEmitter getDocEmitter()
    {
        return new JSGoogDocEmitter(this);
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

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

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();

        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        List<String> list = resolveImports(type);
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

    @Override
    public void emitPackageContents(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        ITypeNode tnode = findTypeNode(definition.getNode());
        if (tnode != null)
        {
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();

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

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        ICompilerProject project = getWalker().getProject();

        getDoc().emitInterfaceDoc(node, project);

        String qname = node.getQualifiedName();
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

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
            boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                    || mnode.getNodeID() == ASTNodeID.SetterID;

            if (!isAccessor || !propertyNames.contains(qname))
            {
                writeNewline();

                write(qname);
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

                    emitParameters(((IFunctionNode) mnode).getParameterNodes());

                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                }

                write(ASEmitterTokens.SEMICOLON);
            }
        }
    }

    @Override
    public void emitField(IVariableNode node)
    {
        IClassDefinition definition = getClassDefinition(node);

        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
        if (enode != null)
            def = enode.resolveType(getWalker().getProject());

        getDoc().emitFieldDoc(node, def);

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
                getDoc().emitVarDoc(node, def);
        }
        else
        {
            getDoc().emitVarDoc(node, null);
        }

        emitDeclarationName(node);
        if (!(avnode instanceof IEmbedNode))
        	emitAssignedValue(avnode);

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

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        emitObjectDefineProperty(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        emitObjectDefineProperty(node);
    }

    protected void emitAccessors(IAccessorNode node)
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

    @Override
    public void emitMethod(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        ICompilerProject project = getWalker().getProject();

        getDoc().emitMethodDoc(node, project);

        boolean isConstructor = node.isConstructor();

        String qname = getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(qname);
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

        emitParameters(node.getParameterNodes());

        boolean hasSuperClass = hasSuperClass(node);

        if (isConstructor && node.getScopedNode().getChildCount() == 0)
        {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            if (hasSuperClass)
                emitSuperCall(node, CONSTRUCTOR_EMPTY);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }

        if (!isConstructor || node.getScopedNode().getChildCount() > 0)
            emitMethodScope(node.getScopedNode());

        if (isConstructor && hasSuperClass)
        {
            writeNewline();
            write(JSGoogEmitterTokens.GOOG_INHERITS);
            write(ASEmitterTokens.PAREN_OPEN);
            write(qname);
            writeToken(ASEmitterTokens.COMMA);
            String sname = getSuperClassDefinition(node, project)
                    .getQualifiedName();
            write(sname);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
    }

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

            write(ASEmitterTokens.PAREN_OPEN);
            walkArguments(node.getArgumentNodes());
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else
        {
            emitSuperCall(node, SUPER_FUNCTION_CALL);
        }
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        ICompilerProject project = getWalker().getProject();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        IDefinition def = ((IIdentifierNode) node).resolve(project);

        ITypeDefinition type = ((IIdentifierNode) node).resolveType(project);

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
                && cnode.getQualifiedName().equalsIgnoreCase("FalconTest_A");
        if (writeSelf && !isRunningInTestMode)
        {
            write(JSGoogEmitterTokens.SELF);
            write(ASEmitterTokens.MEMBER_ACCESS);
        }
        else
        {
            String pname = (type != null) ? type.getPackageName() : "";
            if (cnode != null
                    && pname != ""
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
        IDefinition def = node.getDefinition();
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;
        boolean isLocal = false;
        if (node.getFunctionClassification() == IFunctionDefinition.FunctionClassification.LOCAL)
            isLocal = true;
        if (hasBody(node) && !isStatic && !isLocal)
            emitSelfReference(node);

        if (node.isConstructor()
                && hasSuperClass(node) && !hasSuperCall(node.getScopedNode()))
            emitSuperCall(node, CONSTRUCTOR_FULL);

        emitRestParameterCodeBlock(node);

        emitDefaultParameterCodeBlock(node);
    }

    protected void emitSelfReference(IFunctionNode node)
    {
        writeToken(ASEmitterTokens.VAR);
        writeToken(JSGoogEmitterTokens.SELF);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.THIS);
        writeNewline(ASEmitterTokens.SEMICOLON);
    }

    protected void emitSuperCall(IASNode node, String type)
    {
        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        if (type == CONSTRUCTOR_EMPTY)
        {
            indentPush();
            writeNewline();
            indentPop();
        }
        else if (type == SUPER_FUNCTION_CALL)
        {
            if (fnode == null)
                fnode = (IFunctionNode) fcnode
                        .getAncestorOfType(IFunctionNode.class);
        }

        if (fnode.isConstructor() && !hasSuperClass(fnode))
            return;

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        write(cnode.getQualifiedName());
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

        if (type == CONSTRUCTOR_FULL)
        {
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }
        else if (type == CONSTRUCTOR_EMPTY)
        {
            write(ASEmitterTokens.SEMICOLON);
        }
    }

    protected void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();
        if (pnodes.length == 0)
            return;

        Map<Integer, IParameterNode> defaults = getDefaults(pnodes);

        if (defaults != null)
        {
            final StringBuilder code = new StringBuilder();

            if (!hasBody(node))
            {
                indentPush();
                write(ASEmitterTokens.INDENT);
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
                    code.append(pnode.getDefaultValue());
                    code.append(ASEmitterTokens.SEMICOLON.getToken());

                    write(code.toString());

                    if (i == n - 1 && !hasBody(node))
                        indentPop();

                    writeNewline();
                }
            }
        }
    }

    private void emitRestParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();

        IParameterNode rest = getRest(pnodes);
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
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    @Override
    protected void emitAssignedValue(IExpressionNode node)
    {
        if (node != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            if (node.getNodeID() == ASTNodeID.ClassReferenceID)
            {
                IDefinition definition = node.resolve(getWalker().getProject());
                write(definition.getQualifiedName());
            }
            else
            {
                getWalker().walk(node);
            }
        }
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        getWalker().walk(node.getCollectionNode());
    }

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

    protected Map<Integer, IParameterNode> getDefaults(IParameterNode[] nodes)
    {
        Map<Integer, IParameterNode> result = new HashMap<Integer, IParameterNode>();
        int i = 0;
        boolean hasDefaults = false;
        for (IParameterNode node : nodes)
        {
            if (node.hasDefaultValue())
            {
                hasDefaults = true;
                result.put(i, node);
            }
            else
            {
                result.put(i, null);
            }
            i++;
        }

        if (!hasDefaults)
            return null;

        return result;
    }

    private IParameterNode getRest(IParameterNode[] nodes)
    {
        for (IParameterNode node : nodes)
        {
            if (node.isRest())
                return node;
        }

        return null;
    }

    private static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    protected static IClassDefinition getClassDefinition(IDefinitionNode node)
    {
        IClassNode tnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return (tnode != null) ? tnode.getDefinition() : null;
    }

    private static IClassDefinition getSuperClassDefinition(
            IDefinitionNode node, ICompilerProject project)
    {
        IClassDefinition parent = (IClassDefinition) node.getDefinition()
                .getParent();
        IClassDefinition superClass = parent.resolveBaseClass(project);
        return superClass;
    }

    protected boolean hasSuperClass(IDefinitionNode node)
    {
        ICompilerProject project = getWalker().getProject();
        IClassDefinition superClassDefinition = getSuperClassDefinition(node,
                project);
        // XXX (mschmalle) this is nulling for MXML super class, figure out why
        if (superClassDefinition == null)
            return false;
        String qname = superClassDefinition.getQualifiedName();
        return superClassDefinition != null
                && !qname.equals(IASLanguageConstants.Object);
    }

    private boolean hasSuperCall(IScopedNode node)
    {
        for (int i = node.getChildCount() - 1; i > -1; i--)
        {
            IASNode cnode = node.getChild(i);
            if (cnode.getNodeID() == ASTNodeID.FunctionCallID
                    && cnode.getChild(0).getNodeID() == ASTNodeID.SuperID)
                return true;
        }

        return false;
    }

    protected static boolean hasBody(IFunctionNode node)
    {
        IScopedNode scope = node.getScopedNode();
        return scope.getChildCount() > 0;
    }

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
        emitParameters(node.getParameterNodes());

        emitMethodScope(node.getScopedNode());

        writeToken(ASEmitterTokens.COMMA);
        write(JSEmitterTokens.CONFIGURABLE);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.TRUE);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE, false);

        // tail, no colon; parent container will add it
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    @Override
    public void emitNamespaceAccessExpression(INamespaceAccessExpressionNode node)
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
                IIdentifierNode lnode = (IIdentifierNode) node
                        .getLeftOperandNode();

                writeToken(ASEmitterTokens.EQUAL);
                writeToken(lnode.getName());
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

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    private List<String> resolveImports(ITypeDefinition type)
    {
        ArrayList<String> list = new ArrayList<String>();
        IScopedNode scopeNode = type.getContainedScope().getScopeNode();
        if (scopeNode != null)
        {
            scopeNode.getAllImports(list);
        }
        else
        {
            // MXML
            ClassDefinition cdefinition = (ClassDefinition) type;
            String[] implicitImports = cdefinition.getImplicitImports();
            for (String imp : implicitImports)
            {
                list.add(imp);
            }
        }
        return list;
    }
}
