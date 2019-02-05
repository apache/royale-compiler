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

package org.apache.royale.compiler.internal.codegen.js.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition.INamepaceDeclarationDirective;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.ParameterNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.utils.NativeUtils;

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

    public static INamepaceDeclarationDirective findNamespace(Collection<IDefinition> definitions)
    {
        for (IDefinition definition : definitions)
        {
            if (definition instanceof INamepaceDeclarationDirective)
                return (INamepaceDeclarationDirective) definition;
        }
        return null;
    }
    
    public static INamespaceNode findNamespaceNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof INamespaceNode)
                return (INamespaceNode) child;
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
        if (nodeDef instanceof InterfaceDefinition)
            return false;
        if (nodeDef instanceof ClassDefinition)
            return false;
        
        if (classNode == null) // script in MXML and AS interface definitions
        {
        	if (parentNodeId == ASTNodeID.FunctionCallID && model.inE4xFilter)
        	{
        		// instance methods must be qualified with 'this'?
        		// or maybe we need to test if identifier exists on XML/XMLList
        		return false;
        	}
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
                boolean isFileOrPackageMember = false;
                if(nodeDef instanceof FunctionDefinition)
                {
                    FunctionClassification classification = ((FunctionDefinition) nodeDef).getFunctionClassification();
                    if(classification == FunctionClassification.FILE_MEMBER ||
                            classification == FunctionClassification.PACKAGE_MEMBER)
                    {
                        isFileOrPackageMember = true;
                    }
                    else if (!identifierIsMemberAccess && classification == FunctionClassification.CLASS_MEMBER &&
                    		isClassMember(project, nodeDef, thisClass))
                    	return true;
                }
                return parentNodeId == ASTNodeID.FunctionCallID
                        && !(nodeDef instanceof AccessorDefinition)
                        && !identifierIsMemberAccess
                        && !isFileOrPackageMember;
            }
        }
        else
        {
        	if (parentNodeId == ASTNodeID.FunctionCallID && model.inE4xFilter)
        	{
        		// instance methods must be qualified with 'this'?
        		// or maybe we need to test if identifier exists on XML/XMLList
        		return false;
        	}
            if (nodeDef != null
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
        IDefinition parentDef = nodeDef.getParent();
    	if (nodeDef.isInternal() && (!(parentDef instanceof ClassDefinition)))
    		return false;
    	
        IClassDefinition cdef = classNode.getDefinition();
        return parentDef == cdef || (parentDef instanceof ClassDefinition && cdef.isInstanceOf((ClassDefinition)parentDef, project));
    }
    
    public static boolean isClassMember(ICompilerProject project,
            IDefinition nodeDef, IClassDefinition classDef)
    {
        IDefinition parentDef = nodeDef.getParent();
    	if (nodeDef.isInternal() && (!(parentDef instanceof ClassDefinition)))
    		return false;
    	
        return parentDef == classDef || (parentDef instanceof ClassDefinition && ((ClassDefinition)parentDef).isInstanceOf(classDef, project));
    }
    
    public static boolean writeE4xFilterNode(ICompilerProject project,
            JSSessionModel model, IExpressionNode node)
    {
    	if (!model.inE4xFilter) return false;
    	
        IDefinition nodeDef = node.resolve(project);

        IASNode parentNode = node.getParent();
//        ASTNodeID parentNodeId = parentNode.getNodeID();

        IASNode firstChild = parentNode.getChild(0);

//        final IClassDefinition thisClass = model.getCurrentClass();

//        boolean identifierIsMemberAccess = parentNodeId == ASTNodeID.MemberAccessExpressionID;

        if (parentNode instanceof IUnaryOperatorNode)
        	return false;
        if (nodeDef instanceof ParameterDefinition)
            return false;
        if (nodeDef instanceof InterfaceDefinition)
            return false;
        if (nodeDef instanceof ClassDefinition)
            return false;
        if (nodeDef instanceof VariableDefinition)
        {
        		List<IVariableNode> list = model.getVars();
        		for (IVariableNode element : list) {
        		    if(element.getQualifiedName().equals(((IIdentifierNode)node).getName()))
        		    		return false;
        		}
        }
        
        if (node == firstChild) 
        		return true;

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

    // return true if the node is an expression that may not work
    // as the initial value of a static var at
    // static initialization time.  Such as a function call to
    // another static method in the class.
    // Non-static initializers have different rules: even simple object
    // and arrays need to be created for each instance, but for statics
    // simple objects and arras are ok.
    public static boolean needsStaticInitializer(String node, String className)
    {
    	return node.contains(className);
    }

    public static IContainerNode insertArgumentsBefore(IContainerNode argumentsNode, IASNode... nodes)
    {
        int originalLength = argumentsNode.getChildCount();
        int extraLength = nodes.length;
        ContainerNode result = new ContainerNode(originalLength + extraLength);
        result.setSourcePath(argumentsNode.getSourcePath());
        result.span(argumentsNode);
        result.setParent((NodeBase) argumentsNode.getParent());
        for (int i = 0; i < extraLength; i++)
        {
            NodeBase node = (NodeBase) nodes[i];
            node.setSourcePath(argumentsNode.getSourcePath());
            result.addItem(node);
        }
        for (int i = 0; i < originalLength; i++)
        {
            result.addItem((NodeBase) argumentsNode.getChild(i));
        }
        return result;
    }

    public static IContainerNode insertArgumentsAfter(IContainerNode argumentsNode, IASNode... nodes)
    {
        int originalLength = argumentsNode.getChildCount();
        int extraLength = nodes.length;
        ContainerNode result = new ContainerNode(originalLength + extraLength);
        result.setSourcePath(argumentsNode.getSourcePath());
        result.span(argumentsNode);
        result.setParent((NodeBase) argumentsNode.getParent());
        for (int i = 0; i < originalLength; i++)
        {
            result.addItem((NodeBase) argumentsNode.getChild(i));
        }
        for (int i = 0; i < extraLength; i++)
        {
            NodeBase node = (NodeBase) nodes[i];
            node.setSourcePath(argumentsNode.getSourcePath());
            result.addItem(node);
        }
        return result;
    }

    public static IContainerNode insertArgumentsAt(IContainerNode argumentsNode, int index, IASNode... nodes)
    {
        int originalLength = argumentsNode.getChildCount();
        int extraLength = nodes.length;
        ContainerNode result = new ContainerNode(originalLength + extraLength);
        result.setSourcePath(argumentsNode.getSourcePath());
        result.span(argumentsNode);
        result.setParent((NodeBase) argumentsNode.getParent());
        for (int i = 0; i < originalLength; i++)
        {
            if(i < index)
            {
                result.addItem((NodeBase) argumentsNode.getChild(i));
            }
            else
            {
            	if (i == index)
                {
                    for (IASNode node : nodes)
                    {
                    	NodeBase n = (NodeBase) node;
                    	n.setSourcePath(argumentsNode.getSourcePath());
                    	result.addItem(n);
                    }
                }
                result.addItem((NodeBase) argumentsNode.getChild(i));
            }
        }
        return result;
    }

    public static boolean isImplicit(IContainerNode node)
    {
        return node.getContainerType() == IContainerNode.ContainerType.IMPLICIT
                || node.getContainerType() == IContainerNode.ContainerType.SYNTHESIZED;
    }

}
