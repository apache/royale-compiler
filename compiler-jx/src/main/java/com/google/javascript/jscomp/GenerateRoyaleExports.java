/*
 * Copyright 2008 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp;

import java.util.Set;

import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;

public class GenerateRoyaleExports extends AbstractPostOrderCallback {

	private final AbstractCompiler compiler;
	private Set<String> extraSymbolNamesToExport;

	public GenerateRoyaleExports(AbstractCompiler compiler) {
	  this.compiler = compiler;
	}

	public void process(Node externs, Node root, Set<String> extraSymbolNamesToExport) {
		this.extraSymbolNamesToExport = extraSymbolNamesToExport;
		NodeTraversal.traverse(compiler, root, this);
	}

	@Override
	public void visit(NodeTraversal t, Node n, Node parent) {
		JSDocInfo docInfo = n.getJSDocInfo();
		if(docInfo == null || docInfo.isExport()) {
			// if no jsdoc or already exported, we can skip
			return;
		}

		switch(n.getToken()) {
			case STRING_KEY:{
				Node parentNode = n.getParent();
				if(parentNode == null) {
					return;
				}
		
				Node gpNode = parentNode.getParent();
				if(gpNode == null || gpNode.getToken() != Token.CALL) {
					return;
				}
				
				Node objNode = gpNode.getChildAtIndex(1);
				if (!objNode.isQualifiedName()) {
					return;
				}

				String accessorQualifiedName = objNode.getQualifiedName() + "." + n.getString();
				if(!extraSymbolNamesToExport.contains(accessorQualifiedName)) {
					return;
				}

				// we found an accessor that needs to be exported
				// accessors are defined in Object.defineProperties() calls
				addExtern(n.getString());
				return;
			}
			case ASSIGN: {
				Node firstChild = n.getFirstChild();
				if (!firstChild.isQualifiedName()) {
					return;
				}
				String qualifiedName = firstChild.getQualifiedName();
				if(!extraSymbolNamesToExport.contains(qualifiedName)) {
					return;
				}
		
				Node parentNode = n.getParent();
				if(parentNode == null) {
					return;
				}
		
				Node gpNode = parentNode.getParent();
				if(gpNode == null || !gpNode.isScript()) {
					return;
				}
		
				// we found a variable or constant that needs to be exported
				addExportSymbolCall(qualifiedName, n);
				return;
			}
			default:
				return;
		}
	}

	private void addExtern(String export) {
	  Node objectPrototype = NodeUtil.newQName(compiler, "Object.prototype");
	  JSType objCtor = compiler.getTypeRegistry().getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE);
	  objectPrototype.getFirstChild().setJSType(objCtor);
	  Node propstmt = IR.exprResult(IR.getprop(objectPrototype, IR.string(export)));
	  propstmt.useSourceInfoFromForTree(getSynthesizedExternsRoot());
	  propstmt.setOriginalName(export);
	  getSynthesizedExternsRoot().addChildToBack(propstmt);
	  compiler.reportChangeToEnclosingScope(propstmt);
	}

	/** Lazily create a "new" externs root for undeclared variables. */
	private Node getSynthesizedExternsRoot() {
	  return  compiler.getSynthesizedExternsInput().getAstRoot(compiler);
	}

	private void addExportSymbolCall(String export, Node context) {
	  Node call =
		  IR.call(
			  NodeUtil.newQName(
				  compiler, compiler.getCodingConvention().getExportSymbolFunction(),
				  context, export),
			  IR.string(export),
			  NodeUtil.newQName(
				  compiler, export,
				  context, export));
  
	  Node expression = IR.exprResult(call).useSourceInfoIfMissingFromForTree(context);
	  annotate(expression);
  
	  addStatement(context, expression);
	}

	private void addStatement(Node context, Node stmt) {
	  CodingConvention convention = compiler.getCodingConvention();
  
	  Node n = context;
	  Node exprRoot = n;
	  while (!NodeUtil.isStatementBlock(exprRoot.getParent())) {
		exprRoot = exprRoot.getParent();
	  }
  
	  // It's important that any class-building calls (goog.inherits)
	  // come right after the class definition, so move the export after that.
	  while (true) {
		Node next = exprRoot.getNext();
		if (next != null
			&& NodeUtil.isExprCall(next)
			&& convention.getClassesDefinedByCall(next.getFirstChild()) != null) {
		  exprRoot = next;
		} else {
		  break;
		}
	  }
  
	  Node block = exprRoot.getParent();
	  block.addChildAfter(stmt, exprRoot);
	  compiler.reportChangeToEnclosingScope(stmt);
	}

	private void annotate(Node node) {
	  NodeTraversal.traverse(
		  compiler, node, new PrepareAst.PrepareAnnotations());
	}
}