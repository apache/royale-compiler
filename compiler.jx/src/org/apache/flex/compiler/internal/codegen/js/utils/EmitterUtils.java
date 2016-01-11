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

package org.apache.flex.compiler.internal.codegen.js.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.scopes.TypeScope;
import org.apache.flex.compiler.internal.tree.as.ParameterNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.NativeUtils;

/**
 * Various static methods used in shared emitter logic.
 */
public class EmitterUtils
{
    public static ITypeNode findTypeNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof ITypeNode)
                return (ITypeNode) child;
        }
        return null;
    }

    public static ITypeDefinition findType(Collection<IDefinition> definitions)
    {
        for (IDefinition definition : definitions)
        {
            if (definition instanceof ITypeDefinition)
                return (ITypeDefinition) definition;
        }
        return null;
    }

    public static IFunctionDefinition findFunction(Collection<IDefinition> definitions)
    {
        for (IDefinition definition : definitions)
        {
            if (definition instanceof IFunctionDefinition)
                return (IFunctionDefinition) definition;
        }
        return null;
    }

    public static IFunctionNode findFunctionNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof IFunctionNode)
                return (IFunctionNode) child;
        }
        return null;
    }

    public static IVariableNode findVariableNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof IVariableNode)
                return (IVariableNode) child;
        }
        return null;
    }

    public static IVariableDefinition findVariable(Collection<IDefinition> definitions)
    {
        for (IDefinition definition : definitions)
        {
            if (definition instanceof IVariableDefinition)
                return (IVariableDefinition) definition;
        }
        return null;
    }

    public static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        if (tnode != null)
        {
            return (ITypeDefinition) tnode.getDefinition();
        }
        return null;
    }

    public static boolean isSameClass(IDefinition pdef, IDefinition thisClass,
            ICompilerProject project)
    {
        if (pdef == thisClass)
            return true;

        IDefinition cdef = ((ClassDefinition) thisClass)
                .resolveBaseClass(project);
        while (cdef != null)
        {
            // needs to be a loop
            if (cdef == pdef)
                return true;
            cdef = ((ClassDefinition) cdef).resolveBaseClass(project);
        }
        return false;
    }

    public static boolean hasSuperClass(ICompilerProject project,
            IDefinitionNode node)
    {
        IClassDefinition superClassDefinition = getSuperClassDefinition(node,
                project);
        // XXX (mschmalle) this is nulling for MXML super class, figure out why
        if (superClassDefinition == null)
            return false;
        String qname = superClassDefinition.getQualifiedName();
        return superClassDefinition != null
                && !qname.equals(IASLanguageConstants.Object);
    }

    public static boolean hasSuperCall(IScopedNode node)
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

    public static boolean hasBody(IFunctionNode node)
    {
        IScopedNode scope = node.getScopedNode();
        return scope.getChildCount() > 0;
    }

    public static IClassDefinition getSuperClassDefinition(
            IDefinitionNode node, ICompilerProject project)
    {
        IDefinition parent = node.getDefinition().getParent();
        if (parent instanceof IClassDefinition)
        {
            IClassDefinition parentClassDef = (IClassDefinition) parent;
            IClassDefinition superClass = parentClassDef.resolveBaseClass(project);
            return superClass;
        }
        return null;
    }

    public static List<String> resolveImports(ITypeDefinition type)
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

    public static IClassDefinition getClassDefinition(IDefinitionNode node)
    {
        IClassNode tnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return (tnode != null) ? tnode.getDefinition() : null;
    }

    public static IParameterNode getRest(IParameterNode[] nodes)
    {
        for (IParameterNode node : nodes)
        {
            if (node.isRest())
                return node;
        }

        return null;
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

    public static boolean writeThis(ICompilerProject project,
            JSSessionModel model, IIdentifierNode node)
    {
        IClassNode classNode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        IDefinition nodeDef = node.resolve(project);

        IASNode parentNode = node.getParent();
        ASTNodeID parentNodeId = parentNode.getNodeID();

        IASNode firstChild = parentNode.getChild(0);

        final IClassDefinition thisClass = model.getCurrentClass();

        boolean identifierIsMemberAccess = parentNodeId == ASTNodeID.MemberAccessExpressionID;

        if (nodeDef instanceof ParameterDefinition)
            return false;
        
        if (classNode == null) // script in MXML and AS interface definitions
        {
            if (nodeDef instanceof VariableDefinition)
            {
                IDefinition pdef = ((VariableDefinition) nodeDef).getParent();

                if (thisClass == null || !isSameClass(pdef, thisClass, project))
                    return false;

                if (identifierIsMemberAccess)
                    return node == firstChild;

                return parentNodeId == ASTNodeID.ContainerID
                        || !(parentNode instanceof ParameterNode);
            }
            else if (nodeDef instanceof AccessorDefinition)
            {
                IDefinition pdef = ((AccessorDefinition) nodeDef).getParent();

                if (thisClass == null || !isSameClass(pdef, thisClass, project))
                    return false;

                if (identifierIsMemberAccess)
                    return node == firstChild;

                return true;
            }
            else if (parentNodeId == ASTNodeID.ContainerID
                    && nodeDef instanceof FunctionDefinition)
            {
                return ((FunctionDefinition) nodeDef)
                        .getFunctionClassification() == FunctionClassification.CLASS_MEMBER; // for 'goog.bind'
            }
            else
            {
                return parentNodeId == ASTNodeID.FunctionCallID
                        && !(nodeDef instanceof AccessorDefinition)
                        && !identifierIsMemberAccess;
            }
        }
        else
        {
            if (nodeDef != null && !nodeDef.isInternal()
                    && isClassMember(project, nodeDef, classNode))
            {
                if (identifierIsMemberAccess)
                {
                    return node == firstChild;
                }
                else
                {
                    boolean identifierIsLocalFunction = nodeDef instanceof FunctionDefinition
                            && !(nodeDef instanceof AccessorDefinition)
                            && ((FunctionDefinition) nodeDef)
                                    .getFunctionClassification() == IFunctionDefinition.FunctionClassification.LOCAL;

                    return !identifierIsLocalFunction;
                }
            }
        }

        return false;
    }

    public static boolean isClassMember(ICompilerProject project,
            IDefinition nodeDef, IClassNode classNode)
    {
        TypeScope cscope = (TypeScope) classNode.getDefinition()
                .getContainedScope();

        Set<INamespaceDefinition> nsSet = cscope.getNamespaceSet(project);
        Collection<IDefinition> defs = new HashSet<IDefinition>();

        cscope.getAllPropertiesForMemberAccess((CompilerProject) project, defs,
                nsSet);

        Iterator<IDefinition> visiblePropertiesIterator = defs.iterator();
        while (visiblePropertiesIterator.hasNext())
        {
            if (nodeDef.getQualifiedName().equals(
                    visiblePropertiesIterator.next().getQualifiedName()))
                return true;
        }

        return false;
    }
    
    public static boolean isScalar(IExpressionNode node)
    {
    	ASTNodeID id = node.getNodeID();
        if (id == ASTNodeID.LiteralBooleanID ||
        		id == ASTNodeID.LiteralIntegerID ||
        		id == ASTNodeID.LiteralIntegerZeroID ||
        		id == ASTNodeID.LiteralDoubleID ||
        		id == ASTNodeID.LiteralNullID ||
        		id == ASTNodeID.LiteralNumberID ||
        		id == ASTNodeID.LiteralRegexID ||
        		id == ASTNodeID.LiteralStringID ||
        		id == ASTNodeID.LiteralUintID)
        	return true;
        if (id == ASTNodeID.IdentifierID)
        {
        	IIdentifierNode idnode = (IIdentifierNode)node;
        	String idname = idnode.getName();
        	if (idname.equals(NativeUtils.NativeASType.Infinity.name()) ||
        		idname.equals(NativeUtils.NativeASType.undefined.name()) ||
        		idname.equals(NativeUtils.NativeASType.NaN.name()))
        		return true;
        }
        // special case -Infinity
        if (id == ASTNodeID.Op_SubtractID &&
        		node.getChildCount() == 1)
        {
        	IASNode child = node.getChild(0);
        	if (child.getNodeID() == ASTNodeID.IdentifierID)
        	{
            	IIdentifierNode idnode = (IIdentifierNode)child;
            	String idname = idnode.getName();
            	if (idname.equals(NativeUtils.NativeASType.Infinity.name()))
            		return true;        		
        	}
        }
        return false;
    }

}
