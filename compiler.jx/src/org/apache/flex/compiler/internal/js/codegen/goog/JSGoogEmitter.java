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

package org.apache.flex.compiler.internal.js.codegen.goog;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.js.codegen.JSEmitter;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.js.codegen.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.js.codegen.goog.IJSGoogEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 */
public class JSGoogEmitter extends JSEmitter implements IJSGoogEmitter
{
    public static final String GOOG_BASE = "goog.base";
    public static final String GOOG_INHERITS = "goog.inherits";
    public static final String GOOG_PROVIDE = "goog.provide";
    public static final String GOOG_REQUIRE = "goog.require";

    private List<String> propertyNames = new ArrayList<String>();
    
    IJSGoogDocEmitter getDoc()
    {
        return (IJSGoogDocEmitter) getDocEmitter();
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageNode node)
    {
        ITypeNode type = findTypeNode(node);
        if (type == null)
            return;

        /* goog.provide('x');\n\n */
        write(GOOG_PROVIDE);
        write(PARENTHESES_OPEN);
        write(SINGLE_QUOTE);
        write(type.getQualifiedName());
        write(SINGLE_QUOTE);
        write(PARENTHESES_CLOSE);
        write(SEMICOLON);
        writeNewline();
        writeNewline();
    }

    @Override
    public void emitPackageHeaderContents(IPackageNode node)
    {
        ITypeNode type = findTypeNode(node);
        if (type == null)
            return;

        IPackageDefinition parent = (IPackageDefinition) node.getDefinition();
        ArrayList<String> list = new ArrayList<String>();
        parent.getContainedScope().getScopeNode().getAllImports(list);
        for (String imp : list)
        {
            if (imp.indexOf(AS3) != -1)
                continue;
            
            /* goog.require('x');\n */
            write(GOOG_REQUIRE);
            write(PARENTHESES_OPEN);
            write(SINGLE_QUOTE);
            write(imp);
            write(SINGLE_QUOTE);
            write(PARENTHESES_CLOSE);
            write(SEMICOLON);
            writeNewline();
        }
        
        // (erikdebruin) only write 'closing' line break when there are 
        //               actually imports...
        if (list.size() > 1 || 
        	(list.size() == 1 && list.get(0).indexOf(AS3) == -1))
        {
            writeNewline();
        }
    }

    @Override
    public void emitPackageContents(IPackageNode node)
    {
        ITypeNode type = findTypeNode(node);
        if (type == null)
            return;

        IClassNode cnode = (IClassNode) type;
        
        emitClass(cnode);
    }

    @Override
    public void emitPackageFooter(IPackageNode node)
    {
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();

        // constructor
        emitMethod((IFunctionNode) definition.getConstructor().getNode());
        write(SEMICOLON);

        IDefinitionNode[] dnodes = node.getAllMemberNodes();
        for (IDefinitionNode dnode : dnodes)
        {
            if (dnode.getNodeID() == ASTNodeID.VariableID)
            {
                writeNewline();
                writeNewline();
                emitField((IVariableNode) dnode);
                write(SEMICOLON);
            }
            else if (dnode.getNodeID() == ASTNodeID.FunctionID)
            {
                if (!((IFunctionNode) dnode).isConstructor())
                {
                    writeNewline();
                    writeNewline();
                    emitMethod((IFunctionNode) dnode);
                    write(SEMICOLON);
                }
            }
            else if (dnode.getNodeID() == ASTNodeID.GetterID || 
            		 dnode.getNodeID() == ASTNodeID.SetterID)
            {
                writeNewline();
                writeNewline();
            	emitAccessors((IAccessorNode) dnode);
                write(SEMICOLON);
            }
        }
    }
    
    @Override
    public void emitField(IVariableNode node)
    {
        IClassDefinition definition = getClassDefinition(node);

        getDoc().emitFieldDoc(node);

        /* x.prototype.y = z */
        
        String root = "";
        if (!node.isConst())
        {
        	root = PROTOTYPE;
        	root += PERIOD;
        }
        write(definition.getQualifiedName() + PERIOD + root + node.getName());
        
        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
        	write(SPACE);
            write(EQUALS);
            write(SPACE);
            getWalker().walk(vnode);
        }

        if (!(node instanceof ChainedVariableNode))
        {
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    write(SEMICOLON);
                    writeNewline();
                    writeNewline();
                    emitField((IVariableNode) child);
                }
            }
        }
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        if (!(node instanceof ChainedVariableNode) && !node.isConst())
        {
            emitMemberKeyword(node);
        }

        IExpressionNode avnode = node.getAssignedValueNode();
    	
        if (avnode != null)
        {
	        String opCode = avnode.getNodeID().getParaphrase();
	        if (opCode != "AnonymousFunction")
	        	getDoc().emitVarDoc(node);
        }
        else
        {
        	getDoc().emitVarDoc(node);
        }
        
        emitDeclarationName(node);
        emitAssignedValue(avnode);

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    write(COMMA);
                    write(SPACE);
                    emitVarDeclaration((IVariableNode) child);
                }
            }
        }
    }
    
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        emitObjectDefineProperty(node);
    }
    
    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        emitObjectDefineProperty(node);
    }
    
    private void emitAccessors(IAccessorNode node)
    {
    	if (!propertyNames.contains(node.getName()))
    	{
    		emitField(node);
            write(SEMICOLON);
            writeNewline();
            writeNewline();
            
    		propertyNames.add(node.getName());
    	}
    	
    	if (node.getNodeID() == ASTNodeID.GetterID)
        {
        	emitGetAccessor((IGetterNode) node);
        }
        else if (node.getNodeID() == ASTNodeID.SetterID)
        {
        	emitSetAccessor((ISetterNode) node);
        }
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        ICompilerProject project = getWalker().getProject();
        
        getDoc().emitMethodDoc(node, project);
        
        boolean isConstructor = node.isConstructor();
        
        String qname = getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(qname);
            if (!isConstructor)
            {
            	write(PERIOD);
            	if (!fn.hasModifier(ASModifier.STATIC))
	            {
	                write(PROTOTYPE);
	            	write(PERIOD);
	            }
            }
        }

        if (!isConstructor)
    		emitMemberName(node);
        
        write(SPACE);
        write(EQUALS);
        write(SPACE);
        write(FUNCTION);
        
        emitParamters(node.getParameterNodes());

        if (isConstructor)
        {
            boolean hasSuperClass = hasSuperClass(node, project);

            if (node.getScopedNode().getChildCount() > 0 || hasSuperClass)
            {
            	emitMethodScope(node.getScopedNode());
            }
            else
            {
            	write(SPACE);
            	write(CURLYBRACE_OPEN);
                writeNewline();
            	write(CURLYBRACE_CLOSE);
            }
            
            if (hasSuperClass)
            {
                /* \ngoog.inherits(x, y) */
                writeNewline();
                write(GOOG_INHERITS);
                write(PARENTHESES_OPEN);
                write(qname);
                write(COMMA);
                write(SPACE);
                String sname = getSuperClassDefinition(node, project).getQualifiedName();
                write(sname);
                write(PARENTHESES_CLOSE);
            }
            
            return;
        }

        emitMethodScope(node.getScopedNode());
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
    	emitSuperCallCodeBlock(node);
        
        emitRestParameterCodeBlock(node);

        emitDefaultParameterCodeBlock(node);
    }

    private void emitSuperCallCodeBlock(IFunctionNode node)
    {
        IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
    	
        IExpressionNode bnode = cnode.getBaseClassExpressionNode();
        if (bnode != null && node.isConstructor())
        {
            if (!hasBody(node))
                write(INDENT);
            
        	// TODO (erikdebruin) handle arguments when calling super
            write(GOOG_BASE);
            write(PARENTHESES_OPEN);
            write(IASKeywordConstants.THIS);
            write(PARENTHESES_CLOSE);
            write(SEMICOLON);
            writeNewline();
        }
    }

    private void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();
        if (pnodes.length == 0)
            return;

        Map<Integer, IParameterNode> defaults = getDefaults(pnodes);

        if (defaults != null)
        {
            final StringBuilder code = new StringBuilder();

            if (!hasBody(node))
            {
                indentPush();
                write(INDENT);
            }

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());

            for (int i = 0, n = parameters.size(); i < n; i++)
            {
            	IParameterNode pnode = parameters.get(i);
            	
                if (pnode != null)
                {
                    code.setLength(0);
                    
                    /* x = typeof y !== 'undefined' ? y : z;\n */
                    code.append(pnode.getName());
                    code.append(SPACE);
                    code.append(EQUALS);
                    code.append(SPACE);
                    code.append(IASKeywordConstants.TYPEOF);
                    code.append(SPACE);
                    code.append(pnode.getName()); 
                    code.append(SPACE);
                    code.append(ASTNodeID.Op_StrictNotEqualID.getParaphrase());
                    code.append(SPACE);
                    code.append(SINGLE_QUOTE);
                    code.append(IASLanguageConstants.UNDEFINED);
                    code.append(SINGLE_QUOTE);
                    code.append(SPACE);
                    code.append(ASTNodeID.TernaryExpressionID.getParaphrase());
                    code.append(SPACE);
                    code.append(pnode.getName()); 
                    code.append(SPACE); 
                    code.append(COLON); 
                    code.append(SPACE); 
                    code.append(pnode.getDefaultValue());
                    code.append(SEMICOLON); 

                    write(code.toString());
                    
                    if (i == n - 1 && !hasBody(node))
                        indentPop();
                    
                    writeNewline();
                }
            }
        }
    }

    private void emitRestParameterCodeBlock(IFunctionNode node)
    {
        IParameterNode[] pnodes = node.getParameterNodes();

        IParameterNode rest = getRest(pnodes);
        if (rest != null)
        {
            final StringBuilder code = new StringBuilder();

            /* x = Array.prototype.slice.call(arguments, y);\n */
            code.append(rest.getName());
            code.append(SPACE);
            code.append(EQUALS);
            code.append(SPACE);
            code.append(IASLanguageConstants.Array);
            code.append(PERIOD);
        	code.append(PROTOTYPE);
        	code.append(PERIOD);
        	code.append(SLICE);
        	code.append(PERIOD);
        	code.append(CALL);
        	code.append(PARENTHESES_OPEN);
        	code.append(IASLanguageConstants.arguments);
        	code.append(COMMA);
        	code.append(SPACE);
        	code.append(String.valueOf(pnodes.length - 1));
        	code.append(PARENTHESES_CLOSE);
        	code.append(SEMICOLON);

            write(code.toString());
            
            writeNewline();
        }
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    public JSGoogEmitter(FilterWriter out)
    {
        super(out);
    }

    private Map<Integer, IParameterNode> getDefaults(IParameterNode[] nodes)
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

    private IParameterNode getRest(IParameterNode[] nodes)
    {
        for (IParameterNode node : nodes)
        {
            if (node.isRest())
                return node;
        }

        return null;
    }

    private static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    private static IClassDefinition getClassDefinition(IDefinitionNode node)
    {
        IClassNode tnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
        return tnode.getDefinition();
    }

    private static IClassDefinition getSuperClassDefinition(IDefinitionNode node, ICompilerProject project)
    {
        IClassDefinition parent = (IClassDefinition) node.getDefinition().getParent();
        IClassDefinition superClass = parent.resolveBaseClass(project);
        return superClass;
    }

    private static boolean hasSuperClass(IDefinitionNode node, ICompilerProject project)
    {
    	IClassDefinition superClassDefinition = getSuperClassDefinition(node, project);
    	String qname = superClassDefinition.getQualifiedName();
    	return superClassDefinition != null && !qname.equals(IASLanguageConstants.Object);
    }

    private static boolean hasBody(IFunctionNode node)
    {
        IScopedNode scope = node.getScopedNode();
        return scope.getChildCount() > 0;
    }

    private void emitObjectDefineProperty(IAccessorNode node)
    {
        /*
        Object.defineProperty(
            A.prototype, 
            'foo', 
            {get: function() {return -1;}, 
            configurable: true}
         );
        */
    	
        // head
        write(IASLanguageConstants.Object);
        write(PERIOD);
        write(DEFINE_PROPERTY);
        write(PARENTHESES_OPEN);
        indentPush();
        writeNewline();

        // Type
        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();
        write(type.getQualifiedName());
        write(PERIOD);
        write(PROTOTYPE);
        write(COMMA);
        write(SPACE);
        writeNewline();

        // name
        write(SINGLE_QUOTE);
        write(definition.getBaseName());
        write(SINGLE_QUOTE);
        write(COMMA);
        write(SPACE);
        writeNewline();

        // info object
        // declaration
        write(CURLYBRACE_OPEN);
        write(node.getNodeID() == 
        		ASTNodeID.GetterID ? 
        		IASKeywordConstants.GET : 
        		IASKeywordConstants.SET);
        write(COLON);
        write(FUNCTION);
        emitParamters(node.getParameterNodes());

        // ToDo (erikdebruin/mschmalle) fix 'empty' function body when testing 
        //                              accessors in the context of an entire
        //                              class instead of only function definition
        emitMethodScope(node.getScopedNode());
        
        write(COMMA);
        write(SPACE);
        write(CONFIGURABLE);
        write(COLON);
        write(IASKeywordConstants.TRUE);
        write(CURLYBRACE_CLOSE);
        indentPop();
        writeNewline();

        // tail, no colon; parent container will add it
        write(PARENTHESES_CLOSE);
    }

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
    	ASTNodeID id = node.getNodeID();
    	
    	if (id == ASTNodeID.Op_AsID || id == ASTNodeID.Op_IsID)
        {
        	// TODO (erikdebruin) replace: this is a placeholder for the 
    		//                    eventual implementation
            write((id == ASTNodeID.Op_AsID) ? "as" : "is");
            write(PARENTHESES_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            write(COMMA);
            write(SPACE);
            getWalker().walk(node.getRightOperandNode());
            write(PARENTHESES_CLOSE);
        }
        else
        {
        	getWalker().walk(node.getLeftOperandNode());

            if (id != ASTNodeID.Op_CommaID)
                write(SPACE);
            
            // (erikdebruin) rewrite 'a &&= b' to 'a = a && b'
            if (id == ASTNodeID.Op_LogicalAndAssignID || id == ASTNodeID.Op_LogicalOrAssignID)
            {
            	IIdentifierNode lnode = (IIdentifierNode) node.getLeftOperandNode();
            	
                write(EQUALS);
                write(SPACE);
                write(lnode.getName());
                write(SPACE);
                write((id == ASTNodeID.Op_LogicalAndAssignID) ? 
                		     ASTNodeID.Op_LogicalAndID.getParaphrase() : 
                		     ASTNodeID.Op_LogicalOrID.getParaphrase());
            }
            else
            {
            	write(node.getOperator().getOperatorText());
            }
            
            write(SPACE);
            
            getWalker().walk(node.getRightOperandNode());
        }
    }
}
