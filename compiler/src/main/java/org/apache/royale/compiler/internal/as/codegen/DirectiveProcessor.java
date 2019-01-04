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

package org.apache.royale.compiler.internal.as.codegen;

import java.util.Collection;



import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.InterfaceNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;

import static org.apache.royale.abc.ABCConstants.TRAIT_Getter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Setter;

import org.apache.royale.compiler.problems.BURMDiagnosticNotAllowedHereProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;


/**
 * A DirectiveProcessor is the outer shell of the code generator;
 * the DirectiveProcessor contains logic to navigate the directives'
 * ASTs, and subclasses implement "builder" methods to translate
 * specific directives into ABC terms.
 */
class DirectiveProcessor
{
    private Collection<ICompilerProblem> problems = null;

    protected DirectiveProcessor(Collection<ICompilerProblem> problems)
    {
        this.problems = problems;
    }

    /**
     * Translate a ClassNode AST into ABC.
     * Subclasses should override this if
     * they can process class definitions.
     * @param c - the class' AST.
     */
    void declareClass(ClassNode c)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(c));
    }
    
    /**
     * Translate an InterfaceNode AST into ABC.
     * Subclasses should override this if
     * they can process interface definitions.
     * @param in - the interface's AST.
     */
    void declareInterface(InterfaceNode in)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(in));
    }
    
    /**
     * Translate a FunctionNode AST into ABC.
     * Subclasses should override this if
     * they can process function definitions.
     * @param f - the function's AST.
     */
    void declareFunction(FunctionNode f)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(f));
    }
    
    /**
     * Translate a PackageNode AST into ABC.
     * Subclasses should override this if
     * they can process packages.
     * @param p - the package's AST.
     */
    void declarePackage(PackageNode p)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(p));
    }
    
    /**
     * Translate a VaribleNode AST into ABC.
     * Subclasses should override this if
     * they can process variable definitions.
     * @param var - the variable's AST.
     * @param modifiers - static, const
     */
    void declareVariable(VariableNode var)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(var));
    }

    /**
     * Translate a VaribleNode AST into ABC, and adding bindable support.
     * Subclasses should override this if
     * they can process bindable variable definitions.
     * @param var - the variable's AST.
     * @param modifiers - static, const
     */
    void declareBindableVariable(VariableNode var)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(var));
    }

    /**
     * Translate an MXMLDocumentNode AST into ABC.
     * Subclasses should override this if
     * they can process document nodes.
     * @param c - the document's AST.
     */
    void declareMXMLDocument(IMXMLDocumentNode d)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(d));
    }

    /**
     * Translate other directives' ASTs into ABC.
     * Subclasses should override this if
     * they can process ad-hoc directives.
     * @param n - the directive's AST.
     */
    void processDirective(IASNode n)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(n));
    }
    
    /**
     * Translate a NamespaceIdentifierNode AST into ABC.
     * Subclasses should override this if
     * they can process namespace identifiers.
     * @param ns - the namespace identifier's AST.
     */
    void processNamespaceIdentifierDirective(NamespaceIdentifierNode ns)
    {
        problems.add(new BURMDiagnosticNotAllowedHereProblem(ns));
    }
    
    /**
     * Translate a ImportNode AST into ABC.
     * Subclasses should override this if
     * they can process imports.
     * @param imp - the import's AST.
     */
    void processImportDirective(ImportNode imp)
    {
        processDirective(imp);
    }

    /**
     * Look through a CONFIG block marker into its contents.
     * @param n - the CONFIG block marker.
     */
    void processConfigBlock(IASNode n)
    {
        traverse(n);
    }
    
    /**
     * Traverse the children of a root node and process them.
     * @param root - the root node.  The root is not processed.
     */
    void traverse(IASNode root)
    {
        for ( int i = 0; root != null && i < root.getChildCount(); i++ )
        {
            processNode(root.getChild(i));
        }
    }
    
    /**
     * Process an individual directive.
     * @param n - the directive's AST.
     */
    void processNode(IASNode n)
    {
        // In malformed trees, children can be null.
        // For example, an MXMLFileNode may not have an MXMLDocumentNode.
        if (n == null)
            return;
        
        switch ( n.getNodeID() )
        {
            case ClassID:
                declareClass((ClassNode)n);
                break;
            case InterfaceID:
                declareInterface((InterfaceNode)n);
                break;
            case FunctionID:
            case GetterID:
            case SetterID:
                declareFunction((FunctionNode)n);
                break;
            case ImportID:
                processImportDirective((ImportNode)n);
                break;
            case NamespaceIdentifierID:
                processNamespaceIdentifierDirective((NamespaceIdentifierNode)n);
                break;
            case PackageID:
                declarePackage((PackageNode)n);
                break;
            case VariableID:
                declareVariable((VariableNode)n);
                break;
            case BindableVariableID:
                declareBindableVariable((VariableNode)n);
                break;
            case MXMLDocumentID:
                declareMXMLDocument((IMXMLDocumentNode)n);
                break;
            case MetaTagsID:
                break;
            case ConfigBlockID:
                processConfigBlock(n);
                break;
            default:
            	// hack to allow override as a separate keyword in a conditional compile block.
            	// the AST thinks it is a property on the class named override.
            	// This allows less coding when in SWF the base class has a method that needs
            	// overriding but the JS base class does not.  Instead of writing
            	// COMPILE::SWF
            	// override public function foo() {
            	//   method body
            	// }
            	// COMPILE::JS
            	// public function foo() {
            	//   an exact copy of method body
            	// }
            	// we want to allow:
            	// COMPILE::SWF { override }
                // public function foo() {
            	//   method body
            	// }
            	if (n.getNodeID() == ASTNodeID.IdentifierID)
            	{
            		IdentifierNode node = (IdentifierNode)n;
            		if (node.getName().equals("override"))
            		{
            			IASNode parent = node.getParent();
            			if (parent.getNodeID() == ASTNodeID.ConfigBlockID)
            			{
            				IASNode parentOfMethods = parent.getParent();
            				int functionCount = parentOfMethods.getChildCount();
            				for (int i = 0; i < functionCount; i++)
            				{
            					IASNode child = parentOfMethods.getChild(i);
            					if (child == parent)
            					{
            						// examine the next node
            						child = parentOfMethods.getChild(i + 1);
            						if (child instanceof IFunctionNode)
            						{
            							// convince the compiler that this is now an override
            							IFunctionNode fnode = (IFunctionNode)child;
            							FunctionDefinition fdef = (FunctionDefinition)fnode.getDefinition();
            							fdef.setOverride();
            							return;
            						}    						
            					}
            				}
            			}
            		}
            	}
                processDirective(n);
        }
    }
    
    /**
     *  Getter/Setter functions need to be declared with a specific
     *  trait kind; do so here.
     *  @param func - a FunctionNode.
     *  @param default_kind - the trait kind to use if the function
     *    is not a getter or setter.  Varies depending on caller's context.
     *  @return the trait kind to use to declare the input function.
     */
    public static int functionTraitKind(FunctionNode func, int default_kind)
    {
        switch(func.getNodeID())
        {
            case GetterID:
                return TRAIT_Getter;
            case SetterID:
                return TRAIT_Setter;
            default:
                return default_kind;
        }
    }
}
