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
import com.google.javascript.rhino.Node;

import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;

public class FindRoyaleMXMLPropertyNamesToKeep extends AbstractPostOrderCallback {

	private final AbstractCompiler compiler;
	private Set<String> propertyNamesToKeep;

	public FindRoyaleMXMLPropertyNamesToKeep(AbstractCompiler compiler) {
	  this.compiler = compiler;
	}

	public void process(Node externs, Node root, Set<String> propertyNamesToKeep) {
		this.propertyNamesToKeep = propertyNamesToKeep;
		NodeTraversal.traverse(compiler, root, this);
	}

	@Override
	public void visit(NodeTraversal t, Node n, Node parent) {
		checkForGenerateMXMLAttributes(t, n, parent);
		checkForMXMLDescriptor(t, n, parent);
		checkForBindings(t, n, parent);
	}

	private void checkForMXMLDescriptor(NodeTraversal t, Node n, Node parent) {
		if(!n.isArrayLit()) {
			return;
		}

		if(parent == null || !parent.isName()) {
			return;
		}

		if(!"mxmldd".equals(parent.getQualifiedName())) {
			return;
		}

		Node mxmlDescriptorNode = null;
		if(parent.getGrandparent() != null
				&& parent.getGrandparent().getGrandparent() != null
				&& parent.getGrandparent().getGrandparent().getGrandparent() != null) {
			mxmlDescriptorNode = parent.getGrandparent().getGrandparent().getGrandparent().getGrandparent();
		}
		if(mxmlDescriptorNode == null || !mxmlDescriptorNode.isStringKey() || mxmlDescriptorNode.getChildCount() == 0) {
			return;
		}
		if(!"MXMLDescriptor".equals(mxmlDescriptorNode.getString())) {
			return;
		}
		Node descriptorParent = mxmlDescriptorNode.getParent();
		if(descriptorParent == null || !descriptorParent.isObjectLit()) {
			return;
		}
		Node descriptorGP = mxmlDescriptorNode.getGrandparent();
		if(descriptorGP == null) {
			return;
		}
		if(descriptorGP.getIndexOfChild(descriptorParent) != 2) {
			return;
		}
		Node prototype = descriptorGP.getChildBefore(descriptorParent);
		String qualifiedName = prototype.getQualifiedName();
		if(!qualifiedName.endsWith(".prototype")) {
			return;
		}
		qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 10);
		Node defineProps = descriptorGP.getChildBefore(prototype);
		if(!"Object.defineProperties".equals(defineProps.getQualifiedName())) {
			return;
		}
		traverseMXMLAttributes(n, null);
	}

	private void checkForGenerateMXMLAttributes(NodeTraversal t, Node n, Node parent) {
		if(!n.isGetProp()) {
			return;
		}
		if(parent == null || !parent.isCall()) {
			return;
		}
		if(!"this.generateMXMLAttributes".equals(n.getQualifiedName())) {
			return;
		}
		if(parent.getChildCount() != 2) {
			return;
		}
		Node arrayArg = parent.getChildAtIndex(1);
		if(!arrayArg.isArrayLit()) {
			return;
		}
		if(arrayArg.getChildCount() == 0) {
			return;
		}

		Node functionNode = null;
		if(parent.getGrandparent() != null) {
			functionNode = parent.getGrandparent().getParent();
		}
		if(functionNode == null) {
			return;
		}
		if(!functionNode.isFunction()) {
			return;
		}
		String constructorName = functionNode.getOriginalName();
		traverseMXMLAttributes(arrayArg, constructorName);
	}

	private void traverseMXMLAttributes(Node arrayArg, String qualifiedName) {
		if(arrayArg.getChildCount() == 0) {
			return;
		}
		boolean alreadyHadQualifiedName = qualifiedName != null;
		int i = 0;
		while(i < arrayArg.getChildCount()) {
			if(qualifiedName == null) {
				Node nameChild = arrayArg.getChildAtIndex(i);
				i++;
				if(!nameChild.isGetProp() && !nameChild.isName()) {
					throwParseException(nameChild);
				}
				qualifiedName = nameChild.getQualifiedName();
			}
			Node numPropsChild = arrayArg.getChildAtIndex(i);
			i++;
			if(!numPropsChild.isNumber()) {
				throwParseException(numPropsChild);
			}
			int numProps = (int) numPropsChild.getDouble();
			int minNumPropsChildren = i + numProps * 3;
			if(arrayArg.getChildCount() < minNumPropsChildren) {
				throwParseException(arrayArg);
			}
			while(i < minNumPropsChildren) {
				Node nameNode = arrayArg.getChildAtIndex(i);
				i++;
				String propName = null;
				if (nameNode.isString()) {
					//no parse exception if it's not a string because non-string
					//values are allowed when using -mxml-reflect-object-property=true
					propName = nameNode.getString();
					propertyNamesToKeep.add(propName);
				}
				Node propTypeNode = arrayArg.getChildAtIndex(i);
				i++;
				if((IMXMLLanguageConstants.ATTRIBUTE_ID.equals(propName) || "_id".equals(propName)) && propTypeNode.isTrue()) {
					Node valueNode = arrayArg.getChildAtIndex(i);
					if(valueNode.isString()) {
						propertyNamesToKeep.add(valueNode.getString());
					}
				}
				if(!propTypeNode.isTrue()) {
					Node valueNode = arrayArg.getChildAtIndex(i);
					i++;
					if(valueNode.isArrayLit()) {
						traverseMXMLAttributes(valueNode, null);
					}
				}
				else {
					i++; //skip value
				}
			}
			if(i >= arrayArg.getChildCount()) {
				throwParseException(arrayArg);
			}
			Node numStylesChild = arrayArg.getChildAtIndex(i);
			i++;
			if(!numStylesChild.isNumber()) {
				throwParseException(numStylesChild);
			}
			int numStyles = (int) numStylesChild.getDouble();
			if(numStyles != 0) {
				throwParseException(numStylesChild);
			}
			if(i >= arrayArg.getChildCount()) {
				throwParseException(arrayArg);
			}
			Node numEventsChild = arrayArg.getChildAtIndex(i);
			i++;
			if(!numEventsChild.isNumber()) {
				throwParseException(numEventsChild);
			}
			int numEvents = (int) numEventsChild.getDouble();
			i += numEvents * 2;
			if(!alreadyHadQualifiedName) {
				if(i >= arrayArg.getChildCount()) {
					throwParseException(arrayArg);
				}
				Node childrenChild = arrayArg.getChildAtIndex(i);
				i++;
				if(!childrenChild.isNull() && !childrenChild.isArrayLit()) {
					throwParseException(childrenChild);
				}
				if(childrenChild.isArrayLit()) {
					traverseMXMLAttributes(childrenChild, null);
				}
				qualifiedName = null;
			}
		}
	}

	private void traverseWatchers(Node n, int startIndex) {
		if(n.isNull()) {
			//this is fine, just skip it
			return;
		}
		if(!n.isArrayLit()) {
			throwParseException(n);
		}

		int i = startIndex;
		while(i < n.getChildCount()) {
			Node indexNode = n.getChildAtIndex(i);
			i++;
			if(indexNode.isNull()) {
				//this is the last child watcher
				return;
			}
			if(!indexNode.isNumber()) {
				throwParseException(indexNode);
			}
			Node watcherTypeNode = n.getChildAtIndex(i);
			i++;
			if(!watcherTypeNode.isNumber()) {
				throwParseException(watcherTypeNode);
			}
			int watcherType = (int) watcherTypeNode.getDouble();
			switch(watcherType) {
				case 0: { //function
					Node nameNode = n.getChildAtIndex(i);
					i++;
					if(!nameNode.isString()) {
						throwParseException(nameNode);
					}
					String propName = nameNode.getString();
					propertyNamesToKeep.add(propName);
					i++; //skip function
					i++; //skip event names
					i++; //skip binding indices
					break;
				}
				case 1: { //static property
					Node nameNode = n.getChildAtIndex(i);
					i++;
					if(!nameNode.isString()) {
						throwParseException(nameNode);
					}
					String propName = nameNode.getString();
					propertyNamesToKeep.add(propName);
					i++; //skip event names
					i++; //skip binding indices
					i++; //skip property getter function
					i++; //skip mname
					break;
				}
				case 2: { //property
					Node nameNode = n.getChildAtIndex(i);
					i++;
					if(!nameNode.isString()) {
						throwParseException(nameNode);
					}
					String propName = nameNode.getString();
					propertyNamesToKeep.add(propName);
					i++; //skip event names
					i++; //skip binding indices
					i++; //skip property getter function
					break;
				}
				case 3: { //xml
					Node nameNode = n.getChildAtIndex(i);
					i++;
					if(!nameNode.isString()) {
						throwParseException(nameNode);
					}
					String propName = nameNode.getString();
					propertyNamesToKeep.add(propName);
					i++; //skip binding indices\
					break;
				}
				default: { //unknown!
					throwParseException(watcherTypeNode);
				}
			}
			Node watcherChildren = n.getChildAtIndex(i);
			i++;
			traverseWatchers(watcherChildren, 0);
		}
	}

	private void throwParseException(Node unexpectedNode) {
		throw new RuntimeException("Find MXML property names to keep parse failure: " + unexpectedNode);
	}

	private void checkForBindings(NodeTraversal t, Node n, Node parent) {
		if(!n.isGetProp()) {
			return;
		}
		if(parent == null || !parent.isAssign() || parent.getChildCount() < 2) {
			return;
		}
		String qualifiedName = n.getQualifiedName();
		if(qualifiedName == null) {
			return;
		}
		if(!qualifiedName.endsWith(".prototype._bindings")) {
			return;
		}
		Node bindingsNode = parent.getChildAtIndex(1);
		if(!bindingsNode.isArrayLit()) {
			throwParseException(bindingsNode);
		}
		int i = 0;
		Node firstChild = bindingsNode.getChildAtIndex(i);
		if(firstChild.isGetProp()) {
			i++; //skip inherited bindings
		}
		Node numBindingsChild = bindingsNode.getChildAtIndex(i);
		i++;
		if(!numBindingsChild.isNumber()) {
			throwParseException(numBindingsChild);
		}
		int numBindings = (int) numBindingsChild.getDouble();
		int minNumBindingsChildren = i + numBindings * 3;
		if(bindingsNode.getChildCount() < minNumBindingsChildren) {
			throwParseException(bindingsNode);
		}
		while(i < minNumBindingsChildren) {
			Node sourceNode = bindingsNode.getChildAtIndex(i);
			i++;
			if(sourceNode.isString()) {
				String propName = sourceNode.getString();
				propertyNamesToKeep.add(propName);
			}
			else if(sourceNode.isArrayLit()) {
				for(int j = 0; j < sourceNode.getChildCount(); j++) {
					Node child = sourceNode.getChildAtIndex(j);
					if(child.isString()) {
						String propName = child.getString();
						if(!propName.contains(".")) {
							propertyNamesToKeep.add(propName);
						}
					}
				}
			}
			i++; //skip event
			Node targetNode = bindingsNode.getChildAtIndex(i);
			i++;
			if(targetNode.isString()) {
				String propName = targetNode.getString();
				propertyNamesToKeep.add(propName);
			}
			else if(targetNode.isArrayLit()) {
				for(int j = 0; j < sourceNode.getChildCount(); j++) {
					Node child = sourceNode.getChildAtIndex(j);
					if(child.isString()) {
						String propName = child.getString();
						if(!propName.contains(".")) {
							propertyNamesToKeep.add(propName);
						}
					}
				}
			}
		}
		traverseWatchers(bindingsNode, i);
	}
}