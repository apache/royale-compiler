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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.io.FilterWriter;

import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.ParameterNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.ASNodeUtils;
import org.apache.flex.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    public JSFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        ICompilerProject project = getWalker().getProject();

        IDefinition definition = getClassDefinition(node);

        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
        if (enode != null)
            def = enode.resolveType(project);

        getDoc().emitFieldDoc(node, def);

        IDefinition ndef = node.getDefinition();

        ModifiersSet modifierSet = ndef.getModifiers();
        String root = "";
        if (modifierSet != null && !modifierSet.hasModifier(ASModifier.STATIC))
        {
            root = JSEmitterTokens.PROTOTYPE.getToken();
            root += ASEmitterTokens.MEMBER_ACCESS.getToken();
        }

        if (definition == null)
            definition = ndef.getContainingScope().getDefinition();

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
    protected void emitAccessors(IAccessorNode node)
    {
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
    public void emitFunctionCall(IFunctionCallNode node)
    {
        IASNode cnode = node.getChild(0);

        if (cnode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            cnode = cnode.getChild(0);

        ASTNodeID id = cnode.getNodeID();
        if (id != ASTNodeID.SuperID)
        {
            ICompilerProject project = null;
            IDefinition def = null;

            boolean isClassCast = false;

            if (node.isNewExpression())
            {
                writeToken(ASEmitterTokens.NEW);
            }
            else
            {
                project = getWalker().getProject();
                def = ((IExpressionNode) cnode).resolve(project);

                isClassCast = def instanceof ClassDefinition
                        && !(NativeUtils.isNative(def.getBaseName()));
            }

            if (node.isNewExpression())
            {
            	def = node.resolveCalledExpression(getWalker().getProject());
            	// all new calls to a class should be fully qualified names
            	if (def instanceof ClassDefinition)
            		write(def.getQualifiedName());
            	else // I think we still need this for "new someVarOfTypeClass"
            		getWalker().walk(node.getNameNode());
                write(ASEmitterTokens.PAREN_OPEN);
                walkArguments(node.getArgumentNodes());
                write(ASEmitterTokens.PAREN_CLOSE);
            }
            else if (!isClassCast)
            {
                getWalker().walk(node.getNameNode());
                write(ASEmitterTokens.PAREN_OPEN);
                walkArguments(node.getArgumentNodes());
                write(ASEmitterTokens.PAREN_CLOSE);
            }
            else
            {
                walkArguments(node.getArgumentNodes());

                write("/** Cast to " + def.getQualifiedName() + " */");
            }
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
        boolean writeThis = false;
        if (cnode != null && !(def instanceof ParameterDefinition))
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
        else if (cnode == null && !(type instanceof ClassTraitsDefinition))
        {
            // (erikdebruin) the sequence of these conditions matters, leave
            //               well enough alone!
            if (def instanceof VariableDefinition)
            {
                if (inode == ASTNodeID.MemberAccessExpressionID)
                {
                    writeSelf = true;
                }
                else if (inode == ASTNodeID.ContainerID)
                {
                    writeSelf = true;
                    writeThis = true;
                }
                else if (!(pnode instanceof ParameterNode))
                {
                    writeSelf = true;
                }
            }
            else if (inode == ASTNodeID.ContainerID)
            {
                writeSelf = true;
            }
            else if (inode == ASTNodeID.FunctionCallID
                    && !(def instanceof AccessorDefinition)
                    && inode != ASTNodeID.MemberAccessExpressionID)
            {
                writeSelf = true;
                writeThis = true;
            }
        }

        boolean emitName = true;

        // FIXME (erikdebruin) I desperately needed a way to bypass the addition
        //                   of the 'self' prefix when running the tests... Or 
        //                   I'd have to put the prefix in ~150 asserts!
        boolean isRunningInTestMode = cnode != null
                && cnode.getQualifiedName().equalsIgnoreCase("A");

        if (writeSelf && !isRunningInTestMode)
        {
            boolean useGoogBind = inode == ASTNodeID.ContainerID && !writeThis;

            if (useGoogBind)
            {
                write("goog.bind(");
            }

            if (writeThis)
                write(ASEmitterTokens.THIS);
            else
                write(JSGoogEmitterTokens.SELF);
            write(ASEmitterTokens.MEMBER_ACCESS);

            if (useGoogBind)
            {
                write(node.getName());
                write(", self)");

                emitName = false;
            }
        }

        if (def instanceof AccessorDefinition)
        {
            IASNode anode = node
                    .getAncestorOfType(BinaryOperatorAssignmentNode.class);

            boolean isLeftSide = anode != null
                    && (pnode.equals(anode.getChild(0)) || node.equals(anode
                            .getChild(0)));

            write((anode != null && isLeftSide) ? "set_" : "get_");
            write(node.getName());
            write(ASEmitterTokens.PAREN_OPEN);

            IExpressionNode rightSide = null;

            if (anode != null)
            {
                if (isLeftSide)
                {
                    rightSide = ((BinaryOperatorAssignmentNode) anode)
                            .getRightOperandNode();

                    getWalker().walk(rightSide);
                }
                else
                {
                    rightSide = ((IBinaryOperatorNode) pnode)
                            .getRightOperandNode();
                }
            }

            write(ASEmitterTokens.PAREN_CLOSE);

            if (anode != null
                    && !isLeftSide && pnode instanceof IBinaryOperatorNode
                    && !(pnode instanceof IMemberAccessExpressionNode))
            {
                rightSide = ((IBinaryOperatorNode) pnode).getRightOperandNode();

                if (rightSide != null)
                {
                    write(ASEmitterTokens.SPACE);

                    writeToken(((IBinaryOperatorNode) pnode).getOperator()
                            .getOperatorText());

                    getWalker().walk(rightSide);
                }
            }
        }
        else if (emitName)
        {
            write(node.getName());
        }
    }

    @Override
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

        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);

        if (fnode != null && !fnode.isConstructor())
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            if (fnode.getNodeID() == ASTNodeID.GetterID)
                write("get_");
            else if (fnode.getNodeID() == ASTNodeID.SetterID)
                write("set_");

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

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        ASTNodeID id = node.getNodeID();
        if (id == ASTNodeID.Op_IsID
                || id == ASTNodeID.Op_AsID || id == ASTNodeID.Op_InID
                || id == ASTNodeID.Op_LogicalAndAssignID
                || id == ASTNodeID.Op_LogicalOrAssignID)
        {
            super.emitBinaryOperator(node);
        }
        else
        {
            IExpressionNode leftSide = node.getLeftOperandNode();

            IExpressionNode property = null;
            int leftSideChildCount = leftSide.getChildCount();
            if (leftSideChildCount > 0)
                property = (IExpressionNode) leftSide
                        .getChild(leftSideChildCount - 1);
            else
                property = leftSide;

            IDefinition def = null;
            if (property instanceof IIdentifierNode)
                def = ((IIdentifierNode) property).resolve(getWalker()
                        .getProject());

            if (def instanceof AccessorDefinition)
            {
                getWalker().walk(leftSide);
            }
            else
            {
                if (ASNodeUtils.hasParenOpen(node))
                    write(ASEmitterTokens.PAREN_OPEN);

                getWalker().walk(leftSide);

                if (node.getNodeID() != ASTNodeID.Op_CommaID)
                    write(ASEmitterTokens.SPACE);

                writeToken(node.getOperator().getOperatorText());

                getWalker().walk(node.getRightOperandNode());

                if (ASNodeUtils.hasParenClose(node))
                    write(ASEmitterTokens.PAREN_CLOSE);
            }
        }
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        IASNode leftNode = node.getLeftOperandNode();

        if (!(leftNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) leftNode)
                .getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS))
        {
            getWalker().walk(node.getLeftOperandNode());
            write(node.getOperator().getOperatorText());
        }
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        /*
        Class.prototype.get_property = function()
        {
            // body;
        };
        */

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(problems);

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();
        write(type.getQualifiedName());
        if (!node.hasModifier(ASModifier.STATIC))
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
        }

        write(ASEmitterTokens.MEMBER_ACCESS);
        write((node instanceof IGetterNode) ? "get_" : "set_");
        writeToken(node.getName());
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        emitParamters(node.getParameterNodes());
        //writeNewline();
        emitMethodScope(node.getScopedNode());
    }
}
