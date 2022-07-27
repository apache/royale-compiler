////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.linter.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check that symbols in package or class scopes have a namespace.
 */
public class MissingNamespaceRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ClassID, (node, tokenQuery, problems) -> {
			checkClassNode((IClassNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.InterfaceID, (node, tokenQuery, problems) -> {
			checkInterfaceNode((IInterfaceNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.VariableID, (node, tokenQuery, problems) -> {
			checkVariableNode((IVariableNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkClassNode(IClassNode classNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode scopedNode = classNode.getContainingScope();
		IASNode possiblePackage = scopedNode.getParent();
		if (!(possiblePackage instanceof IPackageNode)) {
			return;
		}
		if (hasNamespace(classNode)) {
			return;
		}
		problems.add(new MissingNamespaceOnClassLinterProblem(classNode));
	}

	private void checkInterfaceNode(IInterfaceNode interfaceNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode scopedNode = interfaceNode.getContainingScope();
		IASNode possiblePackage = scopedNode.getParent();
		if (!(possiblePackage instanceof IPackageNode)) {
			return;
		}
		if (hasNamespace(interfaceNode)) {
			return;
		}
		problems.add(new MissingNamespaceOnInterfaceLinterProblem(interfaceNode));
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode scopedNode = functionNode.getContainingScope();
		IASNode possiblePackageOrClass = scopedNode.getParent();
		if (possiblePackageOrClass instanceof IPackageNode) {
			if (hasNamespace(functionNode) || functionNode.getParent() instanceof IFunctionObjectNode) {
				return;
			}
			problems.add(new MissingNamespaceOnPackageFunctionLinterProblem(functionNode));
			return;
		}
		if (possiblePackageOrClass instanceof IClassNode) {
			if (hasNamespace(functionNode) || functionNode.getParent() instanceof IFunctionObjectNode) {
				return;
			}
			problems.add(new MissingNamespaceOnMethodLinterProblem(functionNode));
			return;
		}
	}

	private void checkVariableNode(IVariableNode variableNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode scopedNode = variableNode.getContainingScope();
		IASNode possiblePackageOrClass = scopedNode.getParent();
		if (possiblePackageOrClass instanceof IPackageNode) {
			if (hasNamespace(variableNode)) {
				return;
			}
			problems.add(new MissingNamespaceOnPackageVariableLinterProblem(variableNode));
			return;
		}
		if (possiblePackageOrClass instanceof IClassNode) {
			if (hasNamespace(variableNode)) {
				return;
			}
			problems.add(new MissingNamespaceOnFieldLinterProblem(variableNode));
			return;
		}
	}

	private boolean hasNamespace(IDefinitionNode node) {
		if (node instanceof BaseDefinitionNode) {
			BaseDefinitionNode baseDefNode = (BaseDefinitionNode) node;
			INamespaceDecorationNode nsNode = baseDefNode.getNamespaceNode();
			if (nsNode == null) {
				return false;
			}
			if (nsNode instanceof IdentifierNode) {
				IdentifierNode identifierNode = (IdentifierNode) nsNode;
				if (identifierNode.isImplicit()) {
					return false;
				}
			}
			return true;
		}
		String ns = node.getNamespace();
		return ns != null;
	}

	public static class MissingNamespaceOnClassLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on class '${className}'";

		public MissingNamespaceOnClassLinterProblem(IClassNode node)
		{
			super(node);
			className = node.getName();
		}

		public String className;
	}

	public static class MissingNamespaceOnInterfaceLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on interface '${interfaceName}'";

		public MissingNamespaceOnInterfaceLinterProblem(IInterfaceNode node)
		{
			super(node);
			interfaceName = node.getName();
		}

		public String interfaceName;
	}

	public static class MissingNamespaceOnPackageFunctionLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on package function '${functionName}'";

		public MissingNamespaceOnPackageFunctionLinterProblem(IFunctionNode node)
		{
			super(node);
			functionName = node.getQualifiedName();
		}

		public String functionName;
	}

	public static class MissingNamespaceOnMethodLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on method '${functionName}'";

		public MissingNamespaceOnMethodLinterProblem(IFunctionNode node)
		{
			super(node);
			functionName = node.getName();
		}

		public String functionName;
	}

	public static class MissingNamespaceOnPackageVariableLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on package variable '${variableName}'";

		public MissingNamespaceOnPackageVariableLinterProblem(IVariableNode node)
		{
			super(node);
			variableName = node.getQualifiedName();
		}

		public String variableName;
	}

	public static class MissingNamespaceOnFieldLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Missing namespace on field '${variableName}'";

		public MissingNamespaceOnFieldLinterProblem(IVariableNode node)
		{
			super(node);
			variableName = node.getName();
		}

		public String variableName;
	}
}
