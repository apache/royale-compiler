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

package org.apache.royale.compiler.internal.codegen.js.jx;

import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.*;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.TooFewFunctionParametersProblem;
import org.apache.royale.compiler.problems.TooManyFunctionParametersProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IFileScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.utils.NativeUtils;

public class FunctionCallEmitter extends JSSubEmitter implements ISubEmitter<IFunctionCallNode>
{

    public FunctionCallEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IFunctionCallNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        IASNode cnode = node.getChild(0);

        if (cnode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            cnode = cnode.getChild(0);
        String postCallAppend = null;
        ASTNodeID id = cnode.getNodeID();
        if (id != ASTNodeID.SuperID)
        {
            IDefinition def = null;
            IExpressionNode nameNode = node.getNameNode();
            def = nameNode.resolve(getProject());

            boolean isClassCast = false;
            boolean wrapResolve = false;
            if (node.isNewExpression())
            {
                boolean omitNew = false;
                if (nameNode instanceof IdentifierNode
                        && (((IdentifierNode) nameNode).getName().equals(IASLanguageConstants.String)
                        || ((IdentifierNode) nameNode).getName().equals(IASLanguageConstants.Boolean)
                        || ((IdentifierNode) nameNode).getName().equals(IASLanguageConstants.Number)
                        || (
                            (
                                ((IdentifierNode) nameNode).getName().equals(IASLanguageConstants.QName) ||
                                ((IdentifierNode) nameNode).getName().equals(IASLanguageConstants.XML)
                            )
                            //use an alternate 'constructor' if there is an alternate default namespace
                            && getModel().defaultXMLNamespaceActive
                            && node.getContainingScope().getScope() instanceof FunctionScope
                            && getModel().getDefaultXMLNamespace((FunctionScope)node.getContainingScope().getScope()) != null
                        )
                    )
                )
                {
                    omitNew = true;
                }
                
                if (!((node.getChild(1) instanceof VectorLiteralNode)))
                {
                    if (!omitNew
                            && ((def == null
                                || !(def.getBaseName().equals(IASGlobalFunctionConstants._int) || def.getBaseName().equals(IASGlobalFunctionConstants.uint)))
                                && !(def instanceof AppliedVectorDefinition && (
                                        ((RoyaleJSProject) getProject()).config.getJsVectorEmulationClass()!= null
                                        && ((RoyaleJSProject) getProject()).config.getJsVectorEmulationClass().equals("Array"))
                                ))
                        )
                    {
	                    if (getProject() instanceof RoyaleJSProject
                                && nameNode.resolveType(getProject()) != null
                                && nameNode.resolveType(getProject()).getQualifiedName().equals("Class")) {
        
	                        wrapResolve = shouldResolveUncertain(nameNode, false);
                            
                            if (wrapResolve) {
                                ((RoyaleJSProject) getProject()).needLanguage = true;
                                getModel().needLanguage = true;
                                write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                                write(ASEmitterTokens.MEMBER_ACCESS);
                                write("resolveUncertain");
                                write(ASEmitterTokens.PAREN_OPEN);
                            }
                        }
                        startMapping(node.getNewKeywordNode());
	                    writeToken(ASEmitterTokens.NEW);
	                    endMapping(node.getNewKeywordNode());
                    }
                }
                else
                {
                    VectorLiteralNode vectorLiteralNode = (VectorLiteralNode) node.getChild(1);
                    String vectorEmulationClass = (((RoyaleJSProject)fjs.getWalker().getProject()).config.getJsVectorEmulationClass());
                    SourceLocation mappingLocation;
                    String elementClassName;
                    IDefinition elementClass = (((AppliedVectorDefinition)def).resolveElementType(getWalker().getProject()));
                    elementClassName = getEmitter().formatQualifiedName(elementClass.getQualifiedName());
                    if (vectorEmulationClass != null)
                    {
                        if (!vectorEmulationClass.equals("Array")) {
                            //Explanation:
                            //this was how it was originally set up, but it assumes the constructor of the emulation
                            //class can handle first argument being an Array or numeric value...
                            writeToken(ASEmitterTokens.NEW);
                            write(vectorEmulationClass);
                            write(ASEmitterTokens.PAREN_OPEN);
                        }// otherwise.... if 'Array' is the emulation class, then just use the literal content
                    } else {
                        //no 'new' output in this case, just coercion, so map from the start of 'new'
                        startMapping(node);
                        write(JSRoyaleEmitterTokens.SYNTH_VECTOR);
                        write(ASEmitterTokens.PAREN_OPEN);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        //the element type of the Vector:
                        write(elementClassName);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(ASEmitterTokens.PAREN_CLOSE);
                        write(ASEmitterTokens.SQUARE_OPEN);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write("coerce");
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(ASEmitterTokens.SQUARE_CLOSE);
                        mappingLocation = new SourceLocation(vectorLiteralNode.getCollectionTypeNode());
                        mappingLocation.setEndColumn(mappingLocation.getEndColumn() + 1);
                        endMapping(mappingLocation);
                        write(ASEmitterTokens.PAREN_OPEN);
                        if (getProject() instanceof RoyaleJSProject)
                            ((RoyaleJSProject)getProject()).needLanguage = true;
                        getEmitter().getModel().needLanguage = true;
                      
                    }
                    mappingLocation = new SourceLocation(vectorLiteralNode.getContentsNode());
                    if (mappingLocation.getColumn()>0) mappingLocation.setColumn(mappingLocation.getColumn() -1);
                    mappingLocation.setEndColumn(mappingLocation.getColumn()+1);
                    startMapping(mappingLocation);
                    write("[");
                    
                    endMapping(mappingLocation);
                    ContainerNode contentsNode = vectorLiteralNode.getContentsNode();
                    int len = contentsNode.getChildCount();
                    for (int i = 0; i < len; i++)
                    {
                        getWalker().walk(contentsNode.getChild(i));
                        if (i < len - 1)
                        {
                            writeToken(ASEmitterTokens.COMMA);
                        }
                    }
                    mappingLocation = new SourceLocation(vectorLiteralNode.getContentsNode());
                    mappingLocation.setLine(vectorLiteralNode.getContentsNode().getEndLine());
                    mappingLocation.setColumn(vectorLiteralNode.getContentsNode().getEndColumn());
                    mappingLocation.setEndColumn(mappingLocation.getColumn() + 1);
                    startMapping(mappingLocation);
                    write("]");
                    endMapping(mappingLocation);
                    if (vectorEmulationClass != null)
                    {
                        if (!vectorEmulationClass.equals("Array")) {
                            writeToken(ASEmitterTokens.COMMA);
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(elementClassName);
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(ASEmitterTokens.PAREN_CLOSE);
                        }
                    } else {
                        write(ASEmitterTokens.PAREN_CLOSE);
                    }
                    return;
                }
            }
            else
            {
                def = node.getNameNode().resolve(getProject());

                isClassCast = def != null && (def instanceof ClassDefinition
                        || def instanceof InterfaceDefinition
                        || ( def instanceof VariableDefinition && ((VariableDefinition) def).resolveType(getProject()).getBaseName().equals("Class")))
                        && !(NativeUtils.isJSNative(def.getBaseName()))
                        && !def.getBaseName().equals(IASLanguageConstants.XML)
                        && !def.getBaseName().equals(IASLanguageConstants.XMLList);
                
            }
            
            if (node.isNewExpression())
            {
                def = node.resolveCalledExpression(getProject());
                // all new calls to a class should be fully qualified names
                if (def instanceof ClassDefinition)
                {
                    startMapping(nameNode);
                    boolean isInt = def.getBaseName().equals(IASGlobalFunctionConstants._int);
                    if (isInt || def.getBaseName().equals(IASGlobalFunctionConstants.uint))
                    {
                        ICompilerProject project = this.getProject();
                        if (project instanceof RoyaleJSProject)
                            ((RoyaleJSProject) project).needLanguage = true;
                        getEmitter().getModel().needLanguage = true;
                        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        if (isInt)
                            write(JSRoyaleEmitterTokens.UNDERSCORE);
                        write(def.getQualifiedName());
                        endMapping(nameNode);
                    } else if( def.getQualifiedName().equals("QName")
                            && getModel().defaultXMLNamespaceActive
                            //is the execution context relevant
                            && node.getContainingScope().getScope() instanceof FunctionScope
                            && getModel().getDefaultXMLNamespace(((FunctionScope)node.getContainingScope().getScope()))!=null
                    ) {
    
                        IExpressionNode alternateDefaultNS = getModel().getDefaultXMLNamespace(((FunctionScope)node.getContainingScope().getScope()));
                        
                        write("QName.createWithDefaultNamespace");
                        endMapping(nameNode);
                        startMapping(node.getArgumentsNode());
                        write(ASEmitterTokens.PAREN_OPEN);
                        endMapping(node.getArgumentsNode());
                        write("/* compiler-added default namespace: */ ");
                        getEmitter().getWalker().walk(alternateDefaultNS);
                        
                        int argCount = node.getArgumentsNode().getChildCount();
                        if (argCount>0)
                            write(",");
                        
                        ((FunctionCallArgumentsEmitter) ((JSRoyaleEmitter) getEmitter()).functionCallArgumentsEmitter).emitContents(node.getArgumentsNode());
    
                        startMapping(node.getArgumentsNode());
                        write(ASEmitterTokens.PAREN_CLOSE);
                        endMapping(node.getArgumentsNode());
                        return;
                    } else if( def.getQualifiedName().equals("XML")
                            && node.getArgumentsNode().getChildCount() == 1
                            && getModel().defaultXMLNamespaceActive
                            //is the execution context relevant
                            && node.getContainingScope().getScope() instanceof FunctionScope
                            && getModel().getDefaultXMLNamespace(((FunctionScope)node.getContainingScope().getScope()))!=null
                    ){
                        write("XML.constructWithDefaultXmlNS");
                        //patch in the defaultNS arg (at position 1.
                        EmitterUtils.createDefaultNamespaceArg(node.getArgumentsNode(),1,getModel().getDefaultXMLNamespace(((FunctionScope)node.getContainingScope().getScope())));
                        getEmitter().emitArguments(node.getArgumentsNode());
                        return;
                    }
                    else
                    {
	                    write(getEmitter().formatQualifiedName(def.getQualifiedName()));
	                    endMapping(nameNode);
                    }
                }
                else
                {
                    // wrap "new someFunctionCall(args)" in parens so the
                    // function call gets parsed and evaluated before new
                    // otherwise it just looks like any other "new function"
                    // in JS.
                    if (nameNode.hasParenthesis())
                        write(ASEmitterTokens.PAREN_OPEN);
                    // I think we still need this for "new someVarOfTypeClass"
                    getEmitter().getWalker().walk(nameNode);
                    if (nameNode.hasParenthesis())
                        write(ASEmitterTokens.PAREN_CLOSE);
                }
                if ( def instanceof AppliedVectorDefinition
                        && (fjs.getWalker().getProject() instanceof RoyaleJSProject)
                        && (((RoyaleJSProject)fjs.getWalker().getProject()).config.getJsVectorEmulationClass() != null))
                {
                    ContainerNode args = node.getArgumentsNode();
                    String vectorEmulationClass = ((RoyaleJSProject)fjs.getWalker().getProject()).config.getJsVectorEmulationClass();
                    if (args.getChildCount() == 0)
                    {
                        if (vectorEmulationClass.equals("Array")) {
                            write(ASEmitterTokens.SQUARE_OPEN);
                            write(ASEmitterTokens.SQUARE_CLOSE);
                        } else {
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(ASEmitterTokens.SQUARE_OPEN);
                            write(ASEmitterTokens.SQUARE_CLOSE);
                            write(ASEmitterTokens.COMMA);
                            write(ASEmitterTokens.SPACE);
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(((AppliedVectorDefinition)def).resolveElementType(getWalker().getProject()).getQualifiedName());
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(ASEmitterTokens.PAREN_CLOSE);
                        }
                    } else {
                        if (vectorEmulationClass.equals("Array")) {
                            if (getProject() instanceof RoyaleJSProject)
                                ((RoyaleJSProject) getProject()).needLanguage = true;
                            getEmitter().getModel().needLanguage = true;
                            write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            startMapping(node.getNameNode());
                            write("arrayAsVector");
                            endMapping(node.getNameNode());
                        }
                        startMapping(node);
                        write(ASEmitterTokens.PAREN_OPEN);
                        endMapping(node);
                        getWalker().walk(args.getChild(0));
                        write(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.SPACE);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(((AppliedVectorDefinition) def).resolveElementType(getWalker().getProject()).getQualifiedName());
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        if (args.getChildCount() == 2 && !vectorEmulationClass.equals("Array")) {
                            IASNode second = args.getChild(1);
                            if (second instanceof IExpressionNode) {
                                ITypeDefinition secondType =
                                        ((IExpressionNode) second).resolveType(fjs.getWalker().getProject());
                                if (fjs.getWalker().getProject().getBuiltinType(BuiltinType.BOOLEAN).equals(secondType)) {
                                    write(ASEmitterTokens.COMMA);
                                    write(ASEmitterTokens.SPACE);
                                    getWalker().walk(second);
                                }
                            }
                        }
                        write(ASEmitterTokens.PAREN_CLOSE);
                    }
                } else {
                    getEmitter().emitArguments(node.getArgumentsNode());
                }
                //end wrap resolve
                if (wrapResolve) {
                    write(ASEmitterTokens.PAREN_CLOSE);
                }
            }
            else if (!isClassCast)
            {
                if (def != null)
                {
                    boolean isInt = def.getBaseName().equals(IASGlobalFunctionConstants._int);
                    boolean isTrace = def.getParent() == null && def.getBaseName().equals(IASGlobalFunctionConstants.trace);
                    if (isInt || isTrace
                            || def.getBaseName().equals(IASGlobalFunctionConstants.uint))
                    {
                        ICompilerProject project = this.getProject();
                        if (project instanceof RoyaleJSProject)
                            ((RoyaleJSProject) project).needLanguage = true;
                        getEmitter().getModel().needLanguage = true;
                        startMapping(node.getNameNode());
                        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        if (isInt)
                            write(JSRoyaleEmitterTokens.UNDERSCORE);
                        endMapping(node.getNameNode());
                    }
                    else if (def.getBaseName().equals("sortOn"))
                	{
                		if (def.getParent() != null &&
                    		def.getParent().getQualifiedName().equals("Array"))
                		{
                            ICompilerProject project = this.getProject();
                            if (project instanceof RoyaleJSProject)
                                ((RoyaleJSProject) project).needLanguage = true;
                            getEmitter().getModel().needLanguage = true;
                            write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write("sortOn");
                            IContainerNode newArgs = EmitterUtils.insertArgumentsBefore(node.getArgumentsNode(), cnode);
                            fjs.emitArguments(newArgs);
                            return;
            			}
            		}
                    else if (def.getBaseName().equals("sort"))
                	{
                	    if (def.getParent() != null) {
                	        if (def.getParent().getQualifiedName().equals("Array")
                                || (node.getNameNode() instanceof MemberAccessExpressionNode
                                    && (((MemberAccessExpressionNode) node.getNameNode()).getLeftOperandNode().resolveType(getProject()) instanceof AppliedVectorDefinition)
                                    &&  getProject() instanceof RoyaleJSProject
                                    && (((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass() == null
                                    || ((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass().equals("Array"))))
                	        {
                                IExpressionNode args[] = node.getArgumentNodes();
                                if (args.length > 0)
                                {
                                    IExpressionNode optionsParamCheck = args.length == 1 ? args[0] : args[1];
                                    ICompilerProject project = this.getProject();
                                    IDefinition paramCheck = optionsParamCheck.resolveType(project);
            
                                    if (paramCheck.getBaseName().equals(IASLanguageConstants._int)
                                            || paramCheck.getBaseName().equals(IASLanguageConstants.uint)
                                            || paramCheck.getBaseName().equals(IASLanguageConstants.Number))
                                    {
                                        //deal with specific numeric option argument variations
                                        //either: Array.sort(option:uint) or Array.sort(compareFunction:Function, option:uint)
                                        //use our Language sort implementation to support these actionscript-specific method signatures
                                        if (project instanceof RoyaleJSProject)
                                            ((RoyaleJSProject) project).needLanguage = true;
                                        getEmitter().getModel().needLanguage = true;
                                        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                                        write(ASEmitterTokens.MEMBER_ACCESS);
                                        write("sort(");
                                        // can't use parameter emitter because the parameter types would be for
                                        // Array.sort instead of Language.sort
                                        IContainerNode newArgs = EmitterUtils.insertArgumentsBefore(node.getArgumentsNode(), cnode);
                                        for (int i = 0; i < newArgs.getChildCount(); i++)
                                        {
                                        	IExpressionNode arg = (IExpressionNode)newArgs.getChild(i);
                                        	IDefinition paramTypeDef;
                                        	if (i == 0)
                                        	{
                                        		paramTypeDef = project.resolveQNameToDefinition(IASLanguageConstants.Array);
                                                getEmitter().emitAssignmentCoercion(arg, paramTypeDef);
                                        	}
                                        	else if (i == 1)
                                        	{
                                        		write(ASEmitterTokens.COMMA);
                                        		write(ASEmitterTokens.SPACE);
                                        		if (args.length == 1)
                                        			paramTypeDef = project.resolveQNameToDefinition(IASLanguageConstants.Number);
                                        		else
                                        			paramTypeDef = project.resolveQNameToDefinition(IASLanguageConstants.Function);
                                                getEmitter().emitAssignmentCoercion(arg, paramTypeDef);
                                        	}
                                        	else if (i == 2)
                                        	{
                                        		write(ASEmitterTokens.COMMA);
                                        		write(ASEmitterTokens.SPACE);
                                        		paramTypeDef = project.resolveQNameToDefinition(IASLanguageConstants.Number);
                                                getEmitter().emitAssignmentCoercion(arg, paramTypeDef);
                                        	}
                                        }
                                        write(")");
                                        return;
                                    }
                                }
                            }
                        }
                	    
            		}
                    else if ((def.getBaseName().equals("insertAt")
                                || def.getBaseName().equals("removeAt"))
                                &&  def.getParent() instanceof AppliedVectorDefinition
                                && ((getProject() instanceof RoyaleJSProject) && (
                                    ((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass() == null
                                    || ((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass().equals("Array")))
                           ) {

                            if ((((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass() != null)
                                && ((RoyaleJSProject)getProject()).config.getJsVectorEmulationClass().equals("Array")) {
                                //use a similar approach to regular 'Array' insertAt/removeAt
                                //for Array Vector emulation only (not for other custom classes)
                                //replace the insertAt/removeAt method with 'splice'
                                IdentifierNode splice = new IdentifierNode("splice");
                                splice.setSourceLocation(((MemberAccessExpressionNode)node.getNameNode()).getRightOperandNode());
                                splice.setParent((MemberAccessExpressionNode)node.getNameNode());
                                ((MemberAccessExpressionNode)node.getNameNode()).setRightOperandNode(splice);
                                NumericLiteralNode spliceArg;
                                if (def.getBaseName().equals("insertAt")) {
                                    //insertAt
                                    spliceArg = new NumericLiteralNode("0");
                                    //This works like 'insertAt' itself, pushing the insertee to 3rd position (correct position):
                                    node.getArgumentsNode().addChild(spliceArg, 1);
                                } else {
                                    //removeAt
                                    spliceArg = new NumericLiteralNode("1");
                                    node.getArgumentsNode().addChild(spliceArg, 1);
                                    postCallAppend = "[0]";
                                }
                            } else {
                                //default Vector implementation
                                //unlike Array implementation of these methods, the synthetic Vector implementation supports these methods at runtime,
                                //and they behave differently with fixed length vectors compared to the native 'splice' method output which is used to
                                //support them in Array, however they are not protected from GCL renaming in release builds by any actual class definition,
                                //so we explicitly 'protect' them here by using DynamicAccess instead of MemberAccess
                                ExpressionNodeBase leftSide = (ExpressionNodeBase)(((BinaryOperatorNodeBase) (node.getNameNode())).getLeftOperandNode());
                                LiteralNode dynamicName = new LiteralNode(ILiteralNode.LiteralType.STRING, "'" + def.getBaseName() + "'");
                                dynamicName.setSourceLocation(((BinaryOperatorNodeBase) (node.getNameNode())).getRightOperandNode());
                                DynamicAccessNode replacement = new DynamicAccessNode(leftSide);
                                leftSide.setParent(replacement);
                                replacement.setSourceLocation(node.getNameNode());
                                replacement.setRightOperandNode(dynamicName);
                                dynamicName.setParent(replacement);
    
                                FunctionCallNode replacer = new FunctionCallNode(replacement)   ;
                                replacement.setParent(replacer);
                                IExpressionNode[] args = node.getArgumentNodes();
                                for (IExpressionNode arg : args) {
                                    replacer.getArgumentsNode().addItem((NodeBase) arg);
                                }
                                replacer.getArgumentsNode().setParent(replacer);
                                replacer.getArgumentsNode().setSourceLocation(node.getArgumentsNode());
                                replacer.setParent((NodeBase) node.getParent());
                                //swap it out
                                node = replacer;
                            }
                    }
                    else if (def instanceof AppliedVectorDefinition)
                    {
                        IExpressionNode[] argumentNodes = node.getArgumentNodes();
                        int len = argumentNodes.length;
                    	if (len == 0)
                    	{
                    		getWalker().getProject().getProblems().add(new TooFewFunctionParametersProblem(node, 1));
                    	}
                    	else if (len > 1)
                    	{
                    		getWalker().getProject().getProblems().add(new TooManyFunctionParametersProblem(node, 1));
                    	}
                    	else
                    	{
                            String elementClassName = getEmitter().formatQualifiedName(((TypedExpressionNode)nameNode).getTypeNode().resolve(getProject()).getQualifiedName());
                    	    if (getProject() instanceof RoyaleJSProject
                                && ((RoyaleJSProject) getProject()).config.getJsVectorEmulationClass()!= null) {
                    	        String vectorEmulationClass = ((RoyaleJSProject) getProject()).config.getJsVectorEmulationClass();
                    	        if (vectorEmulationClass.equals("Array")) {
                    	            //just do a slice copy of the array which is the first argument
                                    getWalker().walk(node.getArgumentsNode().getChild(0));
                                    write(ASEmitterTokens.MEMBER_ACCESS);
                                    write("slice");
                                    write(ASEmitterTokens.PAREN_OPEN);
                                    write(ASEmitterTokens.PAREN_CLOSE);
                                } else {
                    	            //assume the emulation class can handle an array or numeric value for first constructor arg...
                                    writeToken(ASEmitterTokens.NEW);
                                    startMapping(node.getNameNode());
                                    write(vectorEmulationClass);
                                    endMapping(node.getNameNode());
                                    write(ASEmitterTokens.PAREN_OPEN);
                                    getWalker().walk(node.getArgumentsNode().getChild(0));
                                    writeToken(ASEmitterTokens.COMMA);
                                    write(ASEmitterTokens.SINGLE_QUOTE);
                                    //the element type of the Vector:
                                    write(elementClassName);
                                    write(ASEmitterTokens.SINGLE_QUOTE);
                                    write(ASEmitterTokens.PAREN_CLOSE);
                                }
                            } else {
                    	        //default Vector implementation
                                startMapping(node.getNameNode());
                                write(JSRoyaleEmitterTokens.SYNTH_VECTOR);
                                write(ASEmitterTokens.PAREN_OPEN);
                                write(ASEmitterTokens.SINGLE_QUOTE);
                                //the element type of the Vector:
                                write(elementClassName);
                                write(ASEmitterTokens.SINGLE_QUOTE);
                                write(ASEmitterTokens.PAREN_CLOSE);
                                write(ASEmitterTokens.SQUARE_OPEN);
                                write(ASEmitterTokens.SINGLE_QUOTE);
                                write("coerce");
                                write(ASEmitterTokens.SINGLE_QUOTE);
                                write(ASEmitterTokens.SQUARE_CLOSE);
                                endMapping(node.getNameNode());
        
                                getEmitter().emitArguments(node.getArgumentsNode());
                                if (getProject() instanceof RoyaleJSProject)
                                    ((RoyaleJSProject)getProject()).needLanguage = true;
                                getEmitter().getModel().needLanguage = true;
                            }
                        }
                        return;
                    }
                    else if (def.getBaseName().equals(IASLanguageConstants.XML))
                    {
                    	write("XML.conversion");
                    	if (getModel().defaultXMLNamespaceActive) {
                    	    //add the default namespace as a second param
                            if (node.getContainingScope().getScope() instanceof FunctionScope) {
                                IExpressionNode defaultNS =  getModel().getDefaultXMLNamespace((FunctionScope)(node.getContainingScope().getScope()));
                                if (defaultNS != null) {
                                    //append the 'default' namespace as a second argument in the conversion function. Only applies if parsing is required.
                                    EmitterUtils.createDefaultNamespaceArg(node.getArgumentsNode(), 1, defaultNS);
                                }
                            }
                        }
                        getEmitter().emitArguments(node.getArgumentsNode());
                    	return;
                    }
                    else if (def.getBaseName().equals(IASLanguageConstants.XMLList))
                    {
                        write("XMLList.conversion");
                        getEmitter().emitArguments(node.getArgumentsNode());
                        return;
                    }
                    else if (def.getQualifiedName().equals(IASLanguageConstants.Object)) {
                        //'resolveUncertain' always output here
                        //unless a) there are no arguments
                        //or b) it is *explicitly* suppressed for 'Object'
                        if (node.getArgumentNodes().length > 0) {
                            if (shouldResolveUncertain(nameNode, true)) {
                                wrapResolve = true;
                                ((RoyaleJSProject) getProject()).needLanguage = true;
                                getModel().needLanguage = true;
                                write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                                write(ASEmitterTokens.MEMBER_ACCESS);
                                write("resolveUncertain");
                                write(ASEmitterTokens.PAREN_OPEN);
                            }
                        }
                    }
                    else if (nameNode.getNodeID() == ASTNodeID.NamespaceAccessExpressionID && def instanceof FunctionDefinition)
                    {
                    	if (fjs.isCustomNamespace((FunctionDefinition)def))
                    	{
                    		write(ASEmitterTokens.THIS);
                    		NamespaceIdentifierNode nin = (NamespaceIdentifierNode)nameNode.getChild(0);
                    		NamespaceDefinition nsDef = (NamespaceDefinition)nin.resolve(getProject());
                    		IdentifierNode idNode = (IdentifierNode)nameNode.getChild(1);
                    		String propName = idNode.getName();
                			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                			String s = nsDef.getURI();
                			write(JSRoyaleEmitter.formatNamespacedProperty(s, propName, true));
                            getEmitter().emitArguments(node.getArgumentsNode());
                            return;
                    	}
                    }
                }
                else if (nameNode.getNodeID() == ASTNodeID.MemberAccessExpressionID && ((JSRoyaleEmitter)getEmitter()).isProxy(((MemberAccessExpressionNode)nameNode).getLeftOperandNode()) && def == null)
                {
                	MemberAccessExpressionNode mae = (MemberAccessExpressionNode)nameNode;
                	getWalker().walk(mae.getLeftOperandNode());
                    write(".callProperty('");
                    getWalker().walk(mae.getRightOperandNode());
                    write("'");
                    IExpressionNode[] args = node.getArgumentNodes();
                    int n = args.length;
                    if (n > 0)
                    {
	                    for (int i = 0; i < n; i++)
	                    {
		                    write(", ");
	                        getWalker().walk(args[i]);
	                    }
                    }
                    write(ASEmitterTokens.PAREN_CLOSE);
                    return;
                }

            	if (def != null
                        && node.getNameNode() instanceof IMemberAccessExpressionNode
                        && def.getContainingScope() instanceof IFileScope) {
                    //this code branch covers a scenario where fully qualified imported functions had their package elements
                    //swapped to dynamic-access when js-dynamic-access-unknown-members=true, which should not happen.
                    //This was observed when the imported function call was being used 'fully qualified' inside
                    //a locally defined function with the same name (where fully qualified access was of course needed)
                    //example:
                    // import org.apache.royale.test.asserts.assertTrue; //import original function
                    //
                    //  public function assertTrue(message:String, condition:Boolean):void{
                    //    org.apache.royale.test.asserts.assertTrue(condition,message); // without this branch in the compiler it was being expressed as org["apache"]["royale"]...etc
                    //  }

            	    startMapping(node.getNameNode());
            	    write(def.getQualifiedName());
            	    endMapping(node.getNameNode());
                } else {
                    getWalker().walk(node.getNameNode());
                }


                getEmitter().emitArguments(node.getArgumentsNode());
    
                if (postCallAppend != null) {
                    write(postCallAppend);
                }
                //end wrap resolve
                if (wrapResolve) {
                    write(ASEmitterTokens.PAREN_CLOSE);
                }
            }
            else //function-style cast
            {
                fjs.emitIsAs(node, node.getArgumentNodes()[0], node.getNameNode(), ASTNodeID.Op_AsID, true);
            }
        }
        else
        {
            fjs.emitSuperCall(node, JSSessionModel.SUPER_FUNCTION_CALL);
        }
    }
    
    
    private boolean shouldResolveUncertain(IExpressionNode nameNode, boolean forceExplicit) {
        //default if not avoided globally
        boolean should = ((RoyaleJSProject)getProject()).config.getJsResolveUncertain();
        //just in case:
        if (!(getProject() instanceof RoyaleJSProject)) return false;

        IDocEmitter docEmitter = getEmitter().getDocEmitter();
        if (docEmitter instanceof JSRoyaleDocEmitter)
        {
            JSRoyaleDocEmitter royaleDocEmitter = (JSRoyaleDocEmitter) docEmitter;
            //look for local boolean toggle, unless forceExplicit is set
            boolean suppress = !forceExplicit && royaleDocEmitter.getLocalSettingAsBoolean(
                    JSRoyaleEmitterTokens.SUPPRESS_RESOLVE_UNCERTAIN, !should);
            //if it is still on, look for sepcific/named 'off' setting based on name node
            if (!suppress && nameNode !=null) {
                //check to suppress for indvidual named node
                if (nameNode instanceof IdentifierNode) {
                    suppress = royaleDocEmitter.getLocalSettingIncludesString(JSRoyaleEmitterTokens.SUPPRESS_RESOLVE_UNCERTAIN, ((IdentifierNode) nameNode).getName());
                }
            }
            should = !suppress;
        }
        return should;
    }

}
