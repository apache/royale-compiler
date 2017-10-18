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

package org.apache.royale.compiler.internal.codegen.js.amd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * These tools need to be refactored into utility classes.
 * 
 * @author Michael Schmalle
 */
public class TempTools
{

    public static void fillStaticStatements(IClassNode node,
            List<IASNode> list, boolean excludeFields)
    {
        int len = node.getScopedNode().getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = node.getScopedNode().getChild(i);
            if (child instanceof IExpressionNode)
                list.add(child);
            else if (child instanceof IDefinitionNode)
            {
                if (!excludeFields
                        && ((IDefinitionNode) child)
                                .hasModifier(ASModifier.STATIC)
                        && child instanceof IVariableNode)
                    list.add(child);
            }
        }
    }

    public static void fillInstanceMembers(IDefinitionNode[] members,
            List<IDefinitionNode> list)
    {
        for (IDefinitionNode node : members)
        {
            if (node instanceof IFunctionNode
                    && ((IFunctionNode) node).isConstructor())
                continue;

            if (!node.hasModifier(ASModifier.STATIC))
            {
                list.add(node);
            }
        }
    }

    public static void fillStaticMembers(IDefinitionNode[] members,
            List<IDefinitionNode> list, boolean excludeFields,
            boolean excludeFunctions)
    {
        for (IDefinitionNode node : members)
        {
            if (node.hasModifier(ASModifier.STATIC))
            {
                if (!excludeFields && node instanceof IVariableNode)
                    list.add(node);
                else if (!excludeFunctions && node instanceof IFunctionNode)
                    list.add(node);
            }
        }
    }

    public static List<IVariableDefinition> getFields(
            IClassDefinition definition, boolean excludePrivate)
    {
        ArrayList<IVariableDefinition> result = new ArrayList<IVariableDefinition>();
        Collection<IDefinition> definitions = definition.getContainedScope()
                .getAllLocalDefinitions();
        for (IDefinition member : definitions)
        {
            if (!member.isImplicit() && member instanceof IVariableDefinition)
            {
                IVariableDefinition vnode = (IVariableDefinition) member;
                if (!member.isStatic()
                        && (member.isPublic() || member.isProtected()))
                    result.add(vnode);
                // TODO FIX the logic here, this won't add twice though
                if (!excludePrivate && member.isPrivate())
                    result.add(vnode);
            }
        }
        return result;
    }

    public static boolean isVariableAParameter(IVariableDefinition node,
            IParameterDefinition[] parameters)
    {
        for (IParameterDefinition parameter : parameters)
        {
            if (node.getBaseName().equals(parameter.getBaseName()))
                return true;
        }
        return false;
    }

    public static Map<Integer, IParameterNode> getDefaults(
            IParameterNode[] nodes)
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

    public static boolean injectThisArgument(FunctionCallNode node,
            boolean allowMembers)
    {
        // if super isSuper checks the nameNode
        if (node.isSuperExpression() && !node.isNewExpression())
            return true;

        ExpressionNodeBase base = node.getNameNode();
        if (base.getNodeID() == ASTNodeID.IdentifierID)
            return false;

        if (allowMembers && base instanceof IMemberAccessExpressionNode)
        {
            //  foo.super()
            IMemberAccessExpressionNode mnode = (IMemberAccessExpressionNode) base;
            if (mnode.getLeftOperandNode().getNodeID() == ASTNodeID.SuperID)
                return true;
        }

        return false;
    }

    public static String toInitialValue(IVariableDefinition field,
            ICompilerProject project)
    {
        Object value = field.resolveInitialValue(project);
        if (value != null)
            return value.toString();
        IReference reference = field.getTypeReference();
        if (reference == null)
            return "undefined";
        if (reference.getName().equals("int")
                || reference.getName().equals("uint")
                || reference.getName().equals("Number"))
            return "0";
        return "null";
    }

    public static boolean isBinding(IIdentifierNode node,
            ICompilerProject project)
    {
        IDefinition resolve = node.resolve(project);

        if (resolve != null && resolve.isPrivate() && !isField(resolve))
        {
            //if (resolve instanceof IFunctionDefinition)
            IExpressionNode rightSide = getNode(node, true, project);
            IBinaryOperatorNode parent = (IBinaryOperatorNode) node
                    .getAncestorOfType(IBinaryOperatorNode.class);
            if (isThisLeftOf(node))
                parent = (IBinaryOperatorNode) parent
                        .getAncestorOfType(IBinaryOperatorNode.class);

            IVariableNode vparent = (IVariableNode) node
                    .getAncestorOfType(IVariableNode.class);
            if (vparent != null)
            {
                IExpressionNode indentFromThis = getIndentFromThis(node);
                if (vparent.getAssignedValueNode() == node
                        || ((IBinaryOperatorNode) vparent
                                .getAssignedValueNode()).getRightOperandNode() == indentFromThis)
                    return true;
            }

            if (rightSide == node && parent != null/*|| isThisLeftOf(node)*/)
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isField(IDefinition node)
    {
        return !(node instanceof IFunctionDefinition);
    }

    public static boolean isValidThis(IIdentifierNode node,
            ICompilerProject project)
    {
        // added super.foo(), wanted to 'this' behind foo
        if (node.getParent() instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode mnode = (IMemberAccessExpressionNode) node
                    .getParent();
            if (mnode.getLeftOperandNode().getNodeID() == ASTNodeID.SuperID)
                return false;

            IExpressionNode indentFromThis = getIndentFromThis(node);
            if (node == indentFromThis)
                return true;

            // test that this is the base expression
            ExpressionNodeBase enode = (ExpressionNodeBase) node;
            ExpressionNodeBase baseExpression = enode.getBaseExpression();
            if (indentFromThis == null && baseExpression != null
                    && baseExpression != node)
                return false;

            // check to see if the left is a type
            ITypeDefinition type = mnode.getLeftOperandNode().resolveType(
                    project);

            // A.{foo} : Left is a Type
            // XXX going to have to test packgeName to com.acme.A
            if (type instanceof ClassTraitsDefinition
                    && mnode.getLeftOperandNode() == node)
            {
                return false;
            }
            // this.{foo} : explicit 'this', in js we are ignoring explicit this identifiers
            // because we are inserting all of them with the emitter
            else if (indentFromThis == null)
            {
                //return false;
            }

        }

        IDefinition definition = node.resolve(project);
        if (definition == null)
            return false; // Is this correct?
        if (definition instanceof IParameterDefinition)
            return false;
        if (definition.getParent() instanceof IMemberAccessExpressionNode)
            return false;
        if (!(definition.getParent() instanceof IClassDefinition))
            return false;

        if (definition instanceof IVariableDefinition)
        {
            IVariableDefinition variable = (IVariableDefinition) definition;
            if (variable.isStatic())
                return false;
        }
        if (definition instanceof IFunctionDefinition)
        {
            IFunctionDefinition function = (IFunctionDefinition) definition;
            if (function.isStatic())
                return false;
        }

        return true;
    }

    private static boolean isThisLeftOf(IIdentifierNode node)
    {
        if (node.getParent() instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode parent = (IMemberAccessExpressionNode) node
                    .getParent();
            if (parent.getLeftOperandNode() instanceof ILanguageIdentifierNode
                    && ((ILanguageIdentifierNode) parent.getLeftOperandNode())
                            .getKind() == LanguageIdentifierKind.THIS)
                return true;
        }
        return false;
    }

    public static IExpressionNode getNode(IASNode iNode, Boolean toRight,
            ICompilerProject project)
    {
        try
        {
            IASNode node = iNode;
            while (node != null)
            {
                if (node instanceof IBinaryOperatorNode
                        && !(node instanceof MemberAccessExpressionNode))
                {
                    if (toRight)
                        node = ((IBinaryOperatorNode) node)
                                .getRightOperandNode();
                    else
                        node = ((IBinaryOperatorNode) node)
                                .getLeftOperandNode();
                }
                else if (node instanceof IFunctionCallNode)
                    node = ((IFunctionCallNode) node).getNameNode();
                else if (node instanceof IDynamicAccessNode)
                    node = ((IDynamicAccessNode) node).getLeftOperandNode();
                else if (node instanceof IUnaryOperatorNode)
                    node = ((IUnaryOperatorNode) node).getOperandNode();
                else if (node instanceof IForLoopNode)
                    node = ((IForLoopNode) node).getChild(0).getChild(0);
                else if (node instanceof IVariableNode)
                {
                    if (toRight)
                        node = ((IVariableNode) node).getAssignedValueNode();
                    else
                        node = ((IVariableNode) node).getVariableTypeNode();
                }
                else if (node instanceof IExpressionNode)
                {
                    //                    IDefinition def = ((IExpressionNode) node).resolve(project);
                    //                    if (def instanceof VariableDefinition)
                    //                    {
                    //                        final VariableDefinition variable = (VariableDefinition) def;
                    //                        def = variable.resolveType(project);
                    //                    }
                    //                    else if (def instanceof FunctionDefinition)
                    //                    {
                    //                        final FunctionDefinition functionDef = (FunctionDefinition) def;
                    //                        final IReference typeRef = functionDef
                    //                                .getReturnTypeReference();
                    //                        if (typeRef != null)
                    //                            def = typeRef.resolve(project,
                    //                                    (ASScope) getScopeFromNode(iNode),
                    //                                    DependencyType.INHERITANCE, false);
                    //                    }
                    //                    else if (def instanceof IGetterDefinition)
                    //                    {
                    //                        final ITypeDefinition returnType = ((IGetterDefinition) def)
                    //                                .resolveReturnType(project);
                    //                        //                        def = m_sharedData.getDefinition(returnType
                    //                        //                                .getQualifiedName());
                    //                        def = returnType; // XXX figure out
                    //                    }
                    //
                    //                    if (def != null && def instanceof ClassDefinition)
                    //                    {
                    //                        return def;
                    //                    }
                    return (IExpressionNode) node;
                }
                else
                {
                    node = null;
                }
            }
        }
        catch (Exception e)
        {
            // getDefinitionForNode(iNode,toRight);

            // getDefinition() sometimes crashes, e.g. when looking at a cast to an interface in some cases,
            // FunctionDefinition.getParameters() returns null and ExpressionNodeBase.determineIfFunction() chokes on it
            //           printWarning(iNode, "getDefinitionForNode() failed for" + iNode);
        }
        return null;
    }

    private static IExpressionNode getIndentFromThis(IIdentifierNode node)
    {
        if (node.getParent() instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode parent = (IMemberAccessExpressionNode) node
                    .getParent();
            if (parent.getLeftOperandNode() instanceof ILanguageIdentifierNode
                    && ((ILanguageIdentifierNode) parent.getLeftOperandNode())
                            .getKind() == LanguageIdentifierKind.THIS)
                return parent.getRightOperandNode();
        }
        return null;
    }

    public static String toPackageName(String name)
    {
        if (!name.contains("."))
            return name;
        final String stem = name.substring(0, name.lastIndexOf("."));
        return stem;
    }

    public static String toBaseName(String name)
    {
        if (!name.contains("."))
            return name;
        final String basename = name.substring(name.lastIndexOf(".") + 1);
        return basename;
    }

}
