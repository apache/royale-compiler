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

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.constants.INamespaceConstants;
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
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.internal.tree.as.ConfigConditionBlockNode;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.ParameterNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.utils.DefinitionUtils;
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
            else if (child.getNodeID() == ASTNodeID.ConfigBlockID)
            {
            	ConfigConditionBlockNode configNode = (ConfigConditionBlockNode)child;
            	if (configNode.getChildCount() > 0)
            	{
            		child = configNode.getChild(0);
                    if (child instanceof ITypeNode)
                        return (ITypeNode) child;
            	}
            }
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
                boolean isLocalFunction = false;
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
                    {
                        return true;
                    }
                    else if (classification == FunctionClassification.LOCAL)
                    {
                        isLocalFunction = true;
                    }
                }
                return parentNodeId == ASTNodeID.FunctionCallID
                        && !(nodeDef instanceof AccessorDefinition)
                        && !identifierIsMemberAccess
                        && !isFileOrPackageMember
                        && !isLocalFunction;
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
        //support alternate ordering of checks, e.g. listItem.(3 == @id) as well as the normal listItem.(@id == 3)
        if (parentNode instanceof IBinaryOperatorNode
            && node == parentNode.getChild(1)) {
            return !(parentNode instanceof IMemberAccessExpressionNode);
        }
        
        return false;
    }
    
   /* public static ArrayList<String> amendE4XFilterComparison(ICompilerProject project,
                                                     JSSessionModel model, IBinaryOperatorNode node, boolean isLeft) {
        if (!model.inE4xFilter) return null;
        IExpressionNode leftNode = node.getLeftOperandNode();
        IDefinition left = null;
        IDefinition right = null;
        boolean leftNodeMember = false;
        boolean leftNodeQName = false;
        boolean rightNodeMember = false;
        boolean rightNodeQName = false;
        if (writeE4xFilterNode(project, model, leftNode)) {
            leftNodeMember = true;
            if (leftNode  instanceof IFunctionCallNode &&
                    ((IFunctionCallNode) leftNode).getNameNode() != null){
                leftNodeQName = ((IFunctionCallNode) leftNode).getNameNode().equals("name");
            }
        } else {
            left = node.getLeftOperandNode().resolveType(project);
        }
        IExpressionNode rightNode = node.getRightOperandNode();
        if (writeE4xFilterNode(project, model, rightNode)) {
            rightNodeMember = true;
            if (rightNode  instanceof IFunctionCallNode &&
                    ((IFunctionCallNode) rightNode).getNameNode() != null){
                rightNodeQName = ((IFunctionCallNode) rightNode).getNameNode().equals("name");
            }
        } else {
            right = node.getRightOperandNode().resolveType(project);
        }
        if (!leftNodeMember && !rightNodeMember) return null;
        ArrayList<String> s = null;
        if (leftNodeMember) {
            //left side is a node.something() function
            if (right!= null && leftNodeQName) {
                //node.name() == right
                if (right.getQualifiedName().equals("String")) {
                    if (isLeft) {
                        s=new ArrayList<String>();
                        s.add(".toString()");
                    } //else make no change for right side
                } else if (rightNodeQName || right.getQualifiedName().equals("QName")) {
                    //use qname.equals(otherQName)
                    if (isLeft) {
                        s.add(".equals(");
                    } else {
                        s.add(")"); //close the equals
                    }
                }
            }
        }
        if (s == null && rightNodeMember) {
            //right side is a node.something() function that has not already been addressed
            if (left!= null && rightNodeQName) {
                //left == node.name()
                if (left.getQualifiedName().equals("String")) {
                    if (!isLeft) {
                        //node.name().toString()
                        s=new ArrayList<String>();
                        s.add(".toString()");
                    } //else make no change for right side
                } else if (rightNodeQName || right.getQualifiedName().equals("QName")) {
                    //use qname.equals(otherQName)
                    if (isLeft) {
                        s.add(".equals(");
                    } else {
                        s.add(")"); //close the equals
                    }
                }
            }
        }
       
        
        return s;
    }*/

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

    public static boolean needsDefaultValue(IVariableNode node, boolean defaultInitializers, ICompilerProject project)
    {
        if (node == null)
        {
            return false;
        }
        if (node instanceof IParameterNode)
        {
            return false;
        }
        if (node instanceof IAccessorNode)
        {
            return false;
        }
        IExpressionNode assignedValueNode = node.getAssignedValueNode();
        if (assignedValueNode != null)
        {
            //already has an assigned value, so it doesn't need to be
            //hoisted
            return false;
        }
        IASNode parentNode = node.getParent();
        if (parentNode instanceof IVariableExpressionNode)
        {
            //ignore for-in loops
            return false;
        }
        IExpressionNode variableTypeNode = node.getVariableTypeNode();
        if (variableTypeNode == null)
        {
            return false;
        }
        IDefinition varTypeDef = variableTypeNode.resolve(project);
        if (varTypeDef == null)
        {
            return false;
        }
        if (IASLanguageConstants.ANY_TYPE.equals(varTypeDef.getQualifiedName()))
        {
            return false;
        }
        if (project.getBuiltinType(BuiltinType.ANY_TYPE).equals(varTypeDef)
                || project.getBuiltinType(BuiltinType.ANY_TYPE).equals(varTypeDef)
                || project.getBuiltinType(BuiltinType.ANY_TYPE).equals(varTypeDef))
        {
            return false;
        }
        if (project.getBuiltinType(BuiltinType.INT).equals(varTypeDef)
                || project.getBuiltinType(BuiltinType.UINT).equals(varTypeDef))
        {
            //always true, regardless of -js-default-initializers
            return true;
        }
        return defaultInitializers;
    }
    
    
    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public static boolean isXMLish(IExpressionNode obj, ICompilerProject project )
    {
        // See if the left side is XML or XMLList
        IDefinition leftDef = obj.resolveType(project);
        if (leftDef == null && obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        {
            return isXMLish(((MemberAccessExpressionNode)obj).getLeftOperandNode(), project);
        }
        else if (leftDef != null && leftDef.getBaseName().equals("*") && obj instanceof DynamicAccessNode) {
            return isXMLish(((DynamicAccessNode)obj).getLeftOperandNode(), project);
        }
        return SemanticUtils.isXMLish(leftDef, project);
    }
    
    
    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * We want to know not just whether the node is of type XML,
     * but whether it is a property of a property of type XML.
     * For example, this.foo might be XML or XMLList, but since
     * 'this' isn't also XML, we return false.  That's because
     * assignment to this.foo shouldn't use setChild() but
     * just do an assignment.
     * @param obj
     * @return
     */
    public static boolean isXMLList(IMemberAccessExpressionNode obj, ICompilerProject project)
    {
        IExpressionNode leftNode = obj.getLeftOperandNode();
        IExpressionNode rightNode = obj.getRightOperandNode();
        ASTNodeID rightID = rightNode.getNodeID();
        if (rightID == ASTNodeID.IdentifierID || (rightID == ASTNodeID.NamespaceAccessExpressionID && rightNode.getChild(1).getNodeID() == ASTNodeID.IdentifierID))
        {
            IDefinition rightDef = rightNode.resolveType(project);
            if (rightDef != null)
            {
                if (SemanticUtils.isXMLish(rightDef, project))
                {
                    return isLeftNodeXMLish(leftNode, project);
                }
                return false;
            }
            return isLeftNodeXMLish(leftNode, project);
        }
        else if (rightID == ASTNodeID.Op_AtID)
            return true;
        else if (rightNode instanceof IDynamicAccessNode && ((IDynamicAccessNode) rightNode).getLeftOperandNode().getNodeID() == ASTNodeID.Op_AtID)
            return true;
        return false;
    }

    public static boolean isLeftNodeXMLList(IExpressionNode leftNode, ICompilerProject project) {
        boolean isXMLList = false;
        if (isLeftNodeXMLish(leftNode, project)) {
            //it is not XMLList if it is a DynamicAccessNode with numeric index.
            //this is limited analysis, because ["0"] would also be the same as [0], but perhaps best we can do without more runtime support
            if (leftNode instanceof IDynamicAccessNode) { //DynamicAccessNode
                IExpressionNode dynAccess = ((IDynamicAccessNode) leftNode).getRightOperandNode();
                IDefinition accessDef = dynAccess.resolveType(project);
                if (SemanticUtils.isNumericType(accessDef, project)) {
                    //assume we are XML, not XMLList
                    isXMLList = false;
                }
            } else
                isXMLList = true;

        }
        return isXMLList;
    }

    public static boolean isLeftNodeXML(IExpressionNode leftNode, ICompilerProject project) {
        boolean isXML = false;
        if (isLeftNodeXMLish(leftNode, project)) {
            //it is not XMLList if it is a DynamicAccessNode with numeric index.
            //this is limited analysis, because ["0"] would also be the same as [0], but perhaps best we can do without more runtime support
            if (leftNode instanceof IDynamicAccessNode) { //DynamicAccessNode
                IExpressionNode dynAccess = ((IDynamicAccessNode) leftNode).getRightOperandNode();
                IDefinition accessDef = dynAccess.resolveType(project);
                if (SemanticUtils.isNumericType(accessDef, project)) {
                    //assume we are XML, not XMLList
                    isXML = true;
                }
            } else
                isXML = false;

        }
        return isXML;
    }
    
    
    public static boolean isLeftNodeXMLish(IExpressionNode leftNode, ICompilerProject project)
    {
        ASTNodeID leftID = leftNode.getNodeID();
        if (leftID == ASTNodeID.IdentifierID)
        {
            IDefinition leftDef = leftNode.resolveType(project);
            if (leftDef != null)
                return SemanticUtils.isXMLish(leftDef, project);
        }
        else if (leftID == ASTNodeID.MemberAccessExpressionID || leftID == ASTNodeID.Op_DescendantsID)
        {
            MemberAccessExpressionNode maen = (MemberAccessExpressionNode)leftNode;
            IExpressionNode rightNode = maen.getRightOperandNode();
            ASTNodeID rightID = rightNode.getNodeID();
            if (rightID == ASTNodeID.IdentifierID)
            {
                IDefinition rightDef = rightNode.resolveType(project);
                if (rightDef != null && rightDef != project.getBuiltinType(BuiltinType.ANY_TYPE))
                {
                    return SemanticUtils.isXMLish(rightDef, project);
                }
            }
            leftNode = maen.getLeftOperandNode();
            return isLeftNodeXMLish(leftNode, project);
        }
        else if (leftID == ASTNodeID.FunctionCallID)
        {
            FunctionCallNode fcn = (FunctionCallNode)leftNode;
            String fname = fcn.getFunctionName();
            if (fname.equals("XML") || fname.equals("XMLList"))
                return true;
        }
        else if (leftID == ASTNodeID.Op_AsID)
        {
            BinaryOperatorAsNode boan = (BinaryOperatorAsNode)leftNode;
            String fname = ((IdentifierNode)boan.getChild(1)).getName();
            if (fname.equals("XML") || fname.equals("XMLList"))
                return true;
        }
        else if (leftID == ASTNodeID.ArrayIndexExpressionID)
        {
            leftNode = (IExpressionNode)(leftNode.getChild(0));
            IDefinition leftDef = leftNode.resolveType(project);
            if (leftDef != null)
                return SemanticUtils.isXMLish(leftDef, project);
            
        }
        else if (leftID == ASTNodeID.E4XFilterID)
            return true;
        return false;
    }
    
    public static boolean needsXMLQNameArgumentsPatch(IFunctionCallNode node, ICompilerProject project) {
        if (node.getNameNode() instanceof MemberAccessExpressionNode
        && ((MemberAccessExpressionNode)node.getNameNode()).getRightOperandNode() instanceof IdentifierNode) {
            String methodName = ((IdentifierNode)((MemberAccessExpressionNode)node.getNameNode()).getRightOperandNode()).getName();
            if ("child".equals(methodName) || "descendants".equals(methodName) || "attribute".equals(methodName)) {
                //double check it is not a method with the same name on a non-XMLish class
                IASNode leftNode = ((MemberAccessExpressionNode)node.getNameNode()).getLeftOperandNode();
                boolean isXML = leftNode instanceof MemberAccessExpressionNode
                        && isLeftNodeXMLish((MemberAccessExpressionNode) leftNode, project);
                if (!isXML) {
                    isXML = leftNode instanceof IExpressionNode && isXMLish((IExpressionNode)leftNode, project);
                }
                if (isXML) {
                    //check argumentsNode
                    if (node.getArgumentsNode().getChildCount() == 1) {
                        IDefinition def = node.getArgumentNodes()[0].resolveType(project);
                        if (def != null && def.getQualifiedName().equals("QName")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static void createDefaultNamespaceArg(ContainerNode argsNode, int position, IExpressionNode defaultNamespace) {
        argsNode.addChild((NodeBase) defaultNamespace, position);
        ((NodeBase) defaultNamespace).setParent(argsNode);
    }
    
    public static boolean isCustomNamespace(String ns) {
        if (ns != null)
        {
            return (!(  ns.equals(IASKeywordConstants.PRIVATE) ||
                        ns.equals(IASKeywordConstants.PROTECTED) ||
                        ns.equals(IASKeywordConstants.INTERNAL) ||
                        ns.equals(INamespaceConstants.AS3URI) ||
                        ns.equals(IASKeywordConstants.PUBLIC)
                    ));
        }
        return false;
    }

    public static final String getClassDepthNameBase(String base, IClassDefinition definition, ICompilerProject project) {
        return base + "_" + DefinitionUtils.deltaFromObject(definition, project) +"_";
    }

}
