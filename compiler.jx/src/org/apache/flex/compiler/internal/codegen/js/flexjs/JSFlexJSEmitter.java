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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.DefinePropertyFunctionEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.InterfaceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.LiteralEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MethodEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ObjectDefinePropertyEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SelfReferenceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.VarDeclarationEmitter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.BinaryOperatorAsNode;
import org.apache.flex.compiler.internal.tree.as.BlockNode;
import org.apache.flex.compiler.internal.tree.as.DynamicAccessNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.TernaryOperatorNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ILiteralContainerNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

import com.google.common.base.Joiner;

/**
 * Concrete implementation of the 'FlexJS' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    private JSFlexJSDocEmitter docEmitter = null;

    private PackageHeaderEmitter packageHeaderEmitter;
    private PackageFooterEmitter packageFooterEmitter;

    private BindableEmitter bindableEmitter;

    private ClassEmitter classEmitter;
    private InterfaceEmitter interfaceEmitter;

    private FieldEmitter fieldEmitter;
    private VarDeclarationEmitter varDeclarationEmitter;
    private AccessorEmitter accessorEmitter;
    private MethodEmitter methodEmitter;

    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private ForEachEmitter forEachEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;
    private LiteralEmitter literalEmitter;

    private AsIsEmitter asIsEmitter;
    private SelfReferenceEmitter selfReferenceEmitter;
    private ObjectDefinePropertyEmitter objectDefinePropertyEmitter;
    private DefinePropertyFunctionEmitter definePropertyFunctionEmitter;

    public ArrayList<String> usedNames = new ArrayList<String>();
    
    @Override
    public String postProcess(String output)
    {
    	String[] lines = output.split("\n");
    	ArrayList<String> finalLines = new ArrayList<String>();
    	boolean sawRequires = false;
    	boolean stillSearching = true;
    	for (String line : lines)
    	{
    		if (stillSearching)
    		{
	            int c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (c > -1)
	            {
	                int c2 = line.indexOf(")");
	                String s = line.substring(c + 14, c2 - 1);
	    			sawRequires = true;
	    			if (!usedNames.contains(s))
	    				continue;
	    		}
	    		else if (sawRequires)
	    			stillSearching = false;
    		}
    		finalLines.add(line);
    	}
    	return Joiner.on("\n").join(finalLines);
    }
    
    public BindableEmitter getBindableEmitter()
    {
        return bindableEmitter;
    }

    public ClassEmitter getClassEmiter()
    {
        return classEmitter;
    }

    public AccessorEmitter getAccessorEmitter()
    {
        return accessorEmitter;
    }

    public PackageFooterEmitter getPackageFooterEmitter()
    {
        return packageFooterEmitter;
    }

    // TODO (mschmalle) Fix; this is not using the backend doc strategy for replacement
    @Override
    public IJSGoogDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSFlexJSDocEmitter(this);
        return docEmitter;
    }

    public JSFlexJSEmitter(FilterWriter out)
    {
        super(out);

        packageHeaderEmitter = new PackageHeaderEmitter(this);
        packageFooterEmitter = new PackageFooterEmitter(this);

        bindableEmitter = new BindableEmitter(this);

        classEmitter = new ClassEmitter(this);
        interfaceEmitter = new InterfaceEmitter(this);

        fieldEmitter = new FieldEmitter(this);
        varDeclarationEmitter = new VarDeclarationEmitter(this);
        accessorEmitter = new AccessorEmitter(this);
        methodEmitter = new MethodEmitter(this);

        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        forEachEmitter = new ForEachEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
        literalEmitter = new LiteralEmitter(this);

        asIsEmitter = new AsIsEmitter(this);
        selfReferenceEmitter = new SelfReferenceEmitter(this);
        objectDefinePropertyEmitter = new ObjectDefinePropertyEmitter(this);
        definePropertyFunctionEmitter = new DefinePropertyFunctionEmitter(this);
        
    }

    @Override
    protected void writeIndent()
    {
        write(JSFlexJSEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    public void emitLiteralContainer(ILiteralContainerNode node)
    {
        super.emitLiteralContainer(node);
    }

    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
		IFunctionNode fnNode = (IFunctionNode)node.getAncestorOfType(IFunctionNode.class);
    	if (fnNode.getEmittingLocalFunctions())
    	{
    		super.emitLocalNamedFunction(node);
    	}
    }
    
    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
    	node.setEmittingLocalFunctions(true);
    	super.emitFunctionBlockHeader(node);
    	if (node.isConstructor())
    	{
            IClassNode cnode = (IClassNode) node
            .getAncestorOfType(IClassNode.class);
            emitComplexInitializers(cnode);
    	}
        if (node.containsLocalFunctions())
        {
            List<IFunctionNode> anonFns = node.getLocalFunctions();
            int n = anonFns.size();
            for (int i = 0; i < n; i++)
            {
                IFunctionNode anonFn = anonFns.get(i);
                if (anonFn.getParent().getNodeID() == ASTNodeID.AnonymousFunctionID)
                {
                    write("var /** @type {Function} */ __localFn" + Integer.toString(i) + "__ = ");
                	getWalker().walk(anonFn.getParent());
                }
                else
                {
                	getWalker().walk(anonFn);
                	write(ASEmitterTokens.SEMICOLON);
                }
                this.writeNewline();
            }
        }
    	node.setEmittingLocalFunctions(false);
    }
    
    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
		IFunctionNode fnNode = (IFunctionNode)node.getAncestorOfType(IFunctionNode.class);
    	if (fnNode.getEmittingLocalFunctions())
    	{
    		super.emitFunctionObject(node);
    	}
    	else
    	{
            List<IFunctionNode> anonFns = fnNode.getLocalFunctions();
            int i = anonFns.indexOf(node.getFunctionNode());
            if (i < 0)
            	System.out.println("missing index for " + node.toString());
            else
            	write("__localFn" + Integer.toString(i) + "__");
    	}
    }
    
    @Override
    public void emitMemberKeyword(IDefinitionNode node)
    {
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(ASEmitterTokens.VAR);
        }
    }

    @Override
    public void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public String formatQualifiedName(String name, boolean isDoc)
    {
        /*
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        */
    	if (getModel().isInternalClass(name))
    		return getModel().getInternalClasses().get(name);
    	if (name.startsWith("window."))
    		name = name.substring(7);
    	else if (!isDoc)
    	{
    		if (!usedNames.contains(name))
    			usedNames.add(name);
    	}
        return name;
    }
    
    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
    	IPackageNode packageNode = definition.getNode();
    	IFileNode fileNode = (IFileNode) packageNode.getAncestorOfType(IFileNode.class);
        int nodeCount = fileNode.getChildCount();
        String mainClassName = null;
        for (int i = 0; i < nodeCount; i++)
        {
	        IASNode pnode = fileNode.getChild(i);
	        
	        if (pnode instanceof IPackageNode)
	        {
	        	IScopedNode snode = ((IPackageNode)pnode).getScopedNode();
	            int snodeCount = snode.getChildCount();
	            for (int j = 0; j < snodeCount; j++)
	            {
	    	        IASNode cnode = snode.getChild(j);
	    	        if (cnode instanceof IClassNode)
	    	        {
	    	        	mainClassName = ((IClassNode)cnode).getQualifiedName();
	    	        	break;
	    	        }
	            }
	        }
	        else if (pnode instanceof IClassNode)
	        {
	        	String className = ((IClassNode)pnode).getQualifiedName();
	        	getModel().getInternalClasses().put(className, mainClassName + "." + className);
	        }
	        else if (pnode instanceof IInterfaceNode)
	        {
	        	String className = ((IInterfaceNode)pnode).getQualifiedName();
	        	getModel().getInternalClasses().put(className, mainClassName + "." + className);
	        }
            else if (pnode instanceof IFunctionNode)
            {
                String className = ((IFunctionNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
            }
            else if (pnode instanceof IVariableNode)
            {
                String className = ((IVariableNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
            }
        }

        packageHeaderEmitter.emit(definition);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        packageHeaderEmitter.emitContents(definition);
        usedNames.clear();
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        packageFooterEmitter.emit(definition);
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        interfaceEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        varDeclarationEmitter.emit(node);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        accessorEmitter.emit(node);
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        accessorEmitter.emitGet(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        accessorEmitter.emitSet(node);
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        methodEmitter.emit(node);
    }

    public void emitComplexInitializers(IClassNode node)
    {
    	classEmitter.emitComplexInitializers(node);
    }
    
    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        forEachEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        memberAccessEmitter.emit(node);
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
    	getWalker().walk(node.getLeftOperandNode());
    	write(".filter(function(node){return (node.");
    	String s = stringifyNode(node.getRightOperandNode());
    	if (s.startsWith("(") && s.endsWith(")"))
    		s = s.substring(1, s.length() - 1);
    	write(s);
    	write(")})");
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        identifierEmitter.emit(node);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        literalEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Specific
    //--------------------------------------------------------------------------

    public void emitIsAs(IExpressionNode left, IExpressionNode right, ASTNodeID id, boolean coercion)
    {
        asIsEmitter.emitIsAs(left, right, id, coercion);
    }

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        selfReferenceEmitter.emit(node);
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        objectDefinePropertyEmitter.emit(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        definePropertyFunctionEmitter.emit(node);
    }
    
    public String stringifyDefineProperties(IClassDefinition cdef)
    {
    	setBufferWrite(true);
    	accessorEmitter.emit(cdef);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        setBufferWrite(false);
        return result;
    }

    @Override
	public void emitClosureStart()
    {
        ICompilerProject project = getWalker().getProject();;
        if (project instanceof FlexJSProject)
        	((FlexJSProject)project).needLanguage = true;
        write(JSFlexJSEmitterTokens.CLOSURE_FUNCTION_NAME);
        write(ASEmitterTokens.PAREN_OPEN);
    }

    @Override
	public void emitClosureEnd(IASNode node)
    {
    	write(ASEmitterTokens.COMMA);
    	write(ASEmitterTokens.SPACE);
    	write(ASEmitterTokens.SINGLE_QUOTE);
    	if (node.getNodeID() == ASTNodeID.IdentifierID)
    		write(((IIdentifierNode)node).getName());
    	else if (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    		writeChainName(node);
    	else
    		System.out.println("unexpected node in emitClosureEnd");
    	write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    @Override
    public void emitStatement(IASNode node)
    {
    	// don't emit named local functions as statements
    	// they are emitted as part of the function block header
    	if (node.getNodeID() == ASTNodeID.FunctionID)
    	{
    		return;
    	}
    	super.emitStatement(node);
    }
    private void writeChainName(IASNode node)
    {
    	while (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    	{    		
    		node = ((IMemberAccessExpressionNode)node).getRightOperandNode();
    	}
    	if (node.getNodeID() == ASTNodeID.IdentifierID)
    		write(((IdentifierNode)node).getName());
    	else
    		System.out.println("unexpected node in emitClosureEnd");
    }
    
    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (node.getNodeID() == ASTNodeID.Op_DeleteID)
        {
        	if (node.getChild(0).getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        	{
        		if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
        		{
        			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0).getChild(0));
        			if (isXMLList(obj))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);
        		        
        	            getWalker().walk(obj);
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChildAt(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        		}
        		else if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.IdentifierID)
        		{
        			if (isXML((IdentifierNode)(node.getChild(0).getChild(0))))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);
        		        
        	            getWalker().walk(node.getChild(0).getChild(0));
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChild(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;        				
        			}
        		}

        	}
        	else if (node.getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
    		{
    			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0));
    			if (isXMLList(obj))
    			{
    		        if (ASNodeUtils.hasParenOpen(node))
    		            write(ASEmitterTokens.PAREN_OPEN);
    		        
    	            String s = stringifyNode(obj.getLeftOperandNode());
    	            write(s);
    	            write(".removeChild('");
    	            s = stringifyNode(obj.getRightOperandNode());
    	            write(s);
    	            write("')");
    		        if (ASNodeUtils.hasParenClose(node))
    		            write(ASEmitterTokens.PAREN_CLOSE);
    		        return;
    			}
    		}

        }
        else if (node.getNodeID() == ASTNodeID.Op_AtID)
        {
        	write("attribute('");
            getWalker().walk(node.getOperandNode());
        	write("')");
        	return;
        }

        super.emitUnaryOperator(node);

    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isXMLList(MemberAccessExpressionNode obj)
    {
    	IExpressionNode leftNode = obj.getLeftOperandNode();
    	IExpressionNode rightNode = obj.getRightOperandNode();
    	ASTNodeID rightID = rightNode.getNodeID();
		if (rightID == ASTNodeID.IdentifierID)
		{
			IDefinition rightDef = rightNode.resolveType(getWalker().getProject());
			if (rightDef != null)
				return IdentifierNode.isXMLish(rightDef, getWalker().getProject());
		}
    	ASTNodeID leftID = leftNode.getNodeID();
		if (leftID == ASTNodeID.IdentifierID)
		{
			IDefinition leftDef = leftNode.resolveType(getWalker().getProject());
			if (leftDef != null)
				return IdentifierNode.isXMLish(leftDef, getWalker().getProject());
		}
		else if (leftID == ASTNodeID.MemberAccessExpressionID)
		{
			return isXMLList((MemberAccessExpressionNode)leftNode);
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
    	return false;
    }
    
    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isXML(IExpressionNode obj)
    {
		// See if the left side is XML or XMLList
		IDefinition leftDef = obj.resolveType(getWalker().getProject());
		return IdentifierNode.isXMLish(leftDef, getWalker().getProject());
    }
    
    public MemberAccessExpressionNode getLastMAEInChain(MemberAccessExpressionNode node)
    {
    	while (node.getRightOperandNode() instanceof MemberAccessExpressionNode)
    		node = (MemberAccessExpressionNode)node.getRightOperandNode();
    	return node;
    }
    
    @Override
    public void emitLabelStatement(LabeledStatementNode node)
    {
    	BlockNode innerBlock = node.getLabeledStatement();
    	if (innerBlock.getChildCount() == 1 && innerBlock.getChild(0).getNodeID() == ASTNodeID.ForEachLoopID)
    	{        
    		getWalker().walk(node.getLabeledStatement());
    		return; // for each emitter will emit label in the right spot
    	}
    	super.emitLabelStatement(node);
    }
}
