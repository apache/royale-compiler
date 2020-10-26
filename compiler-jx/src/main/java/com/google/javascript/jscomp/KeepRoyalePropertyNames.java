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

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;

public class KeepRoyalePropertyNames implements CompilerPass {
	
	private final AbstractCompiler compiler;
	private Set<String> propertyNamesToKeep;

	public KeepRoyalePropertyNames(AbstractCompiler compiler, Set<String> propertyNamesToKeep) {
	  this.compiler = compiler;
	  this.propertyNamesToKeep = propertyNamesToKeep;
	}

	@Override
	public void process(Node externs, Node root) {
	  for(String nameToKeep : propertyNamesToKeep) {
		addExtern(nameToKeep);
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
}
