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
import java.util.ArrayList;

import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.PackageScope;
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
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.units.ICompilationUnit;
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

    public IDefinition thisClass;

    @Override
    protected void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public void emitClass(IClassNode node)
    {
        thisClass = node.getDefinition();

        super.emitClass(node);
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
                def = node.getNameNode().resolve(project);

                isClassCast = def instanceof ClassDefinition
                        && !(NativeUtils.isNative(def.getBaseName()));
            }

            if (node.isNewExpression())
            {
                def = node.resolveCalledExpression(getWalker().getProject());
                // all new calls to a class should be fully qualified names
                if (def instanceof ClassDefinition)
                    write(def.getQualifiedName());
                else
                    // I think we still need this for "new someVarOfTypeClass"
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

    private boolean isSameClass(IDefinition pdef, IDefinition thisClass2,
            ICompilerProject project)
    {
        if (pdef == thisClass2)
            return true;

        // needs to be a loop
        if (((ClassDefinition) thisClass2).resolveBaseClass(project) == pdef)
            return true;

        return false;
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
                        if (def instanceof FunctionDefinition)
                        {
                            if (((FunctionDefinition) def)
                                    .getFunctionClassification() != IFunctionDefinition.FunctionClassification.LOCAL)
                                writeSelf = true;
                        }
                        else
                            writeSelf = true;
                        break;
                    }
                    else if (inode == ASTNodeID.MemberAccessExpressionID
                            && !def.isStatic()
                            && (pnode.getChild(0) == node || (pnode.getChild(0) instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) pnode
                                    .getChild(0)).getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)))
                    {
                        // we are in a member access expression and it isn't a static
                        // and we are the left node, or the left node is 'this'
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
                VariableDefinition vardef = (VariableDefinition) def;
                IDefinition pdef = vardef.getParent();
                if (inode == ASTNodeID.MemberAccessExpressionID)
                {
                    if (pdef == thisClass && pnode.getChild(0) == node)
                        writeSelf = true;
                }
                else if (inode == ASTNodeID.ContainerID)
                {
                    if (isSameClass(pdef, thisClass, project))
                    {
                        writeSelf = true;
                        writeThis = true;
                    }
                }
                else if (!(pnode instanceof ParameterNode))
                {
                    if (pdef == thisClass)
                        writeSelf = true;
                }
            }
            else if (inode == ASTNodeID.ContainerID)
            {
                if (def instanceof FunctionDefinition)
                {
                    if (((FunctionDefinition) def).getFunctionClassification() != IFunctionDefinition.FunctionClassification.LOCAL)
                        writeSelf = true;
                }
                else
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
        //                     of the 'self' prefix when running the tests... Or 
        //                     I'd have to put the prefix in ~150 asserts!
        boolean isRunningInTestMode = cnode != null
                && cnode.getQualifiedName().equalsIgnoreCase("A");

        if (writeSelf && !isRunningInTestMode)
        {
            boolean useGoogBind = inode == ASTNodeID.ContainerID
                    && !writeThis && def instanceof FunctionDefinition
                    && !def.isStatic();

            if (useGoogBind)
            {
                write(JSGoogEmitterTokens.GOOG_BIND);
                write(ASEmitterTokens.PAREN_OPEN);
            }

            if (writeThis)
                write(ASEmitterTokens.THIS);
            else if (def.isStatic())
            {
                String sname = def.getParent().getQualifiedName();
                write(sname);
            }
            else
                write(JSGoogEmitterTokens.SELF);
            write(ASEmitterTokens.MEMBER_ACCESS);

            if (useGoogBind)
            {
                write(node.getName());

                writeToken(ASEmitterTokens.COMMA);
                write(JSGoogEmitterTokens.SELF);
                write(ASEmitterTokens.PAREN_CLOSE);

                emitName = false;
            }
        }
        else if (def != null && def.isStatic())
        {
            String sname = def.getParent().getQualifiedName();
            write(sname);
            write(ASEmitterTokens.MEMBER_ACCESS);
        }

        IDefinition parentDef = (def != null) ? def.getParent() : null;
        boolean isNative = (parentDef != null)
                && NativeUtils.isNative(parentDef.getBaseName());
        if ((def instanceof AccessorDefinition && !isNative)
                || (def instanceof VariableDefinition && ((VariableDefinition) def)
                        .isBindable()))
        {
            IASNode anode = node
                    .getAncestorOfType(BinaryOperatorAssignmentNode.class);

            boolean isAssignment = false;
            if (anode != null)
            {
                IASNode leftNode = anode.getChild(0);
                if (anode == pnode)
                {
                    if (node == leftNode)
                        isAssignment = true;
                }
                else
                {
                    IASNode parentNode = pnode;
                    IASNode thisNode = node;
                    while (anode != parentNode)
                    {
                        if (parentNode instanceof IMemberAccessExpressionNode)
                        {
                            if (thisNode != parentNode.getChild(1))
                            {
                                // can't be an assignment because 
                                // we're on the left side of a memberaccessexpression
                                break;
                            }
                        }
                        if (parentNode == leftNode)
                        {
                            isAssignment = true;
                        }
                        thisNode = parentNode;
                        parentNode = parentNode.getParent();
                    }
                }
            }

            writeGetSetPrefix(!isAssignment);
            write(node.getName());
            write(ASEmitterTokens.PAREN_OPEN);

            IExpressionNode rightSide = null;

            if (anode != null)
            {
                if (isAssignment)
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
                    && !isAssignment && pnode instanceof IBinaryOperatorNode
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
            if (fnode.getNodeID() == ASTNodeID.GetterID
                    || fnode.getNodeID() == ASTNodeID.SetterID)
                writeGetSetPrefix(fnode.getNodeID() == ASTNodeID.GetterID);
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
        else if (fnode != null && fnode.isConstructor())
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
            {
                IASNode childNode = leftSide.getChild(leftSideChildCount - 1);
                if (childNode instanceof IExpressionNode)
                    property = (IExpressionNode) childNode;
                else
                    property = leftSide;
            }
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

        ICompilerProject project = getWalker().getProject();
        IDefinition def = node.resolve(project);
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;

        if (!isStatic
                && !(leftNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) leftNode)
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
        ICompilerProject project = getWalker().getProject();

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(problems);

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();
        getDoc().emitMethodDoc(fn, project);
        write(type.getQualifiedName());
        if (!node.hasModifier(ASModifier.STATIC))
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
        }

        write(ASEmitterTokens.MEMBER_ACCESS);
        writeGetSetPrefix(node instanceof IGetterNode);
        writeToken(node.getName());
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        emitParameters(node.getParameterNodes());
        //writeNewline();
        emitMethodScope(node.getScopedNode());
    }

    private void writeGetSetPrefix(boolean isGet)
    {
        if (isGet)
            write(ASEmitterTokens.GET);
        else
            write(ASEmitterTokens.SET);
        write("_");
    }

    @Override
    public IDocEmitter getDocEmitter()
    {
        return new JSFlexJSGoogDocEmitter(this);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();

        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        FlexJSProject project = (FlexJSProject) getWalker().getProject();
        ASProjectScope projectScope = (ASProjectScope) project.getScope();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(type);
        ArrayList<String> list = project.getRequires(cu);

        String cname = type.getQualifiedName();
        ArrayList<String> writtenInstances = new ArrayList<String>();
        writtenInstances.add(cname); // make sure we don't add ourselves

        if (list != null)
        {
            for (String imp : list)
            {
                if (imp.indexOf(JSGoogEmitterTokens.AS3.getToken()) != -1)
                    continue;

                if (imp.equals(cname))
                    continue;

                if (imp.equals("Array"))
                    continue;
                if (imp.equals("Boolean"))
                    continue;
                if (imp.equals("decodeURI"))
                    continue;
                if (imp.equals("decodeURIComponent"))
                    continue;
                if (imp.equals("encodeURI"))
                    continue;
                if (imp.equals("encodeURIComponent"))
                    continue;
                if (imp.equals("Error"))
                    continue;
                if (imp.equals("Function"))
                    continue;
                if (imp.equals("JSON"))
                    continue;
                if (imp.equals("Number"))
                    continue;
                if (imp.equals("int"))
                    continue;
                if (imp.equals("Object"))
                    continue;
                if (imp.equals("RegExp"))
                    continue;
                if (imp.equals("String"))
                    continue;
                if (imp.equals("uint"))
                    continue;

                if (writtenInstances.indexOf(imp) == -1)
                {

                    /* goog.require('x');\n */
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(imp);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);
                    writtenInstances.add(imp);
                }
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
    }

    private int foreachLoopCounter = 0;

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        String iterName = "foreachiter"
                + new Integer(foreachLoopCounter).toString();
        foreachLoopCounter++;

        write(ASEmitterTokens.FOR);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.VAR);
        write(ASEmitterTokens.SPACE);
        write(iterName);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.IN);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        if (childNode instanceof IVariableExpressionNode)
        {
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName());
        }
        else
            write(((IIdentifierNode) childNode).getName());
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        write(ASEmitterTokens.SQUARE_OPEN);
        write(iterName);
        write(ASEmitterTokens.SQUARE_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
        writeNewline();
        getWalker().walk(node.getStatementContentsNode());
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();

    }

    /*
    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        write(ASEmitterTokens.TRY);
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        
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
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        write(ASEmitterTokens.CATCH);
        write(ASEmitterTokens.PAREN_OPEN);
        write("foreachbreakerror");
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
    	// look for break in foreach and throw error instead
    	if (node.getKind() == IIterationFlowNode.IterationFlowKind.BREAK)
    	{
    		IASNode pNode = node.getParent();
    		while (pNode != null)
    		{
    			ASTNodeID id = pNode.getNodeID();
    			if (id == ASTNodeID.ForEachLoopID)
    			{
    				write(ASEmitterTokens.THROW);
    				write(ASEmitterTokens.SPACE);
    				write(ASEmitterTokens.NEW);
    				write(ASEmitterTokens.SPACE);
    				write(JSGoogEmitterTokens.ERROR);
    				write(ASEmitterTokens.PAREN_OPEN);
    				write(ASEmitterTokens.PAREN_CLOSE);
    				write(ASEmitterTokens.SEMICOLON);
    				return;
    			}
    			else if (id == ASTNodeID.ForLoopID ||
    					id == ASTNodeID.DoWhileLoopID ||
    					id == ASTNodeID.WhileLoopID)
    				break;
    			pNode = pNode.getParent();
    		}
    	}
        write(node.getKind().toString().toLowerCase());
        IIdentifierNode lnode = node.getLabelNode();
        if (lnode != null)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(lnode);
        }
    }
    */

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

}
