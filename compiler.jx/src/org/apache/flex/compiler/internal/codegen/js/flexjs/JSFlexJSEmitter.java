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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSDocEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.PackageScope;
import org.apache.flex.compiler.internal.scopes.TypeScope;
import org.apache.flex.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.GetterNode;
import org.apache.flex.compiler.internal.tree.as.ParameterNode;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.internal.tree.as.SetterNode;
import org.apache.flex.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.ASNodeUtils;
import org.apache.flex.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    public JSFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    public IDefinition thisClass;

    class PropertyNodes
    {
    	public IGetterNode getter;
    	public ISetterNode setter;
    }
    
    HashMap<String, PropertyNodes> propertyMap = new HashMap<String, PropertyNodes>();
    ArrayList<String> bindableVars = new ArrayList<String>();
    
    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    protected void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public void emitClass(IClassNode node)
    {
        thisClass = node.getDefinition();

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && MXMLJSC.keepASDoc)
        	loadImportIgnores(asDoc.commentNoEnd());
        
        project = getWalker().getProject();

        IClassDefinition definition = node.getDefinition();

        IFunctionDefinition ctorDefinition = definition.getConstructor();

        // Static-only (Singleton) classes may not have a constructor
        if (ctorDefinition != null)
        {
            IFunctionNode ctorNode = (IFunctionNode) ctorDefinition.getNode();
            if (ctorNode != null)
            {
                // constructor
                emitMethod(ctorNode);
                write(ASEmitterTokens.SEMICOLON);
            }
            else
            {
                String qname = definition.getQualifiedName();
                if (qname != null && !qname.equals(""))
                {
                    write(formatQualifiedName(qname));
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.FUNCTION);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                    write(ASEmitterTokens.SEMICOLON);
                }
            }
        }

        IDefinitionNode[] dnodes = node.getAllMemberNodes();
        for (IDefinitionNode dnode : dnodes)
        {
            if (dnode.getNodeID() == ASTNodeID.VariableID)
            {
                writeNewline();
                writeNewline();
                writeNewline();
                emitField((IVariableNode) dnode);
                write(ASEmitterTokens.SEMICOLON);
            }
            else if (dnode.getNodeID() == ASTNodeID.FunctionID)
            {
                if (!((IFunctionNode) dnode).isConstructor())
                {
                    writeNewline();
                    writeNewline();
                    writeNewline();
                    emitMethod((IFunctionNode) dnode);
                    write(ASEmitterTokens.SEMICOLON);
                }
            }
            else if (dnode.getNodeID() == ASTNodeID.GetterID
                    || dnode.getNodeID() == ASTNodeID.SetterID)
            {
                writeNewline();
                writeNewline();
                writeNewline();
                emitAccessors((IAccessorNode) dnode);
                write(ASEmitterTokens.SEMICOLON);
            }
            else if (dnode.getNodeID() == ASTNodeID.BindableVariableID)
            {
                writeNewline();
                writeNewline();
                writeNewline();
                emitField((IVariableNode) dnode);
                write(ASEmitterTokens.SEMICOLON);
            }
        }
        
        emitBindableVariables(node.getDefinition());
        
        emitASGettersAndSetters(node.getDefinition());
    }
    
    public void emitASGettersAndSetters(IClassDefinition definition)
    {
        if (!propertyMap.isEmpty())
        {
            write(JSGoogEmitterTokens.OBJECT);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            String qname = definition.getQualifiedName();
            write(formatQualifiedName(qname));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);        	
            write(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SPACE);
            write("/** @lends {" + formatQualifiedName(qname) + ".prototype} */ ");
            writeNewline(ASEmitterTokens.BLOCK_OPEN);
            
	        Set<String> propertyNames = propertyMap.keySet();
	        boolean firstTime = true;
	        for (String propName : propertyNames)
	        {
	        	if (firstTime)
	        		firstTime = false;
	        	else
	                writeNewline(ASEmitterTokens.COMMA);
	        		
	        	PropertyNodes p = propertyMap.get(propName);
	            writeNewline("/** @expose */");
	        	write(propName);
	        	write(ASEmitterTokens.COLON);
	            write(ASEmitterTokens.SPACE);
	            writeNewline(ASEmitterTokens.BLOCK_OPEN);
	            if (p.getter != null)
	            {
	            	write(ASEmitterTokens.GET);
		        	write(ASEmitterTokens.COLON);
		            write(ASEmitterTokens.SPACE);
		            write(JSDocEmitterTokens.JSDOC_OPEN);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.ATSIGN);
		            write(ASEmitterTokens.THIS);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.BLOCK_OPEN);
		            write(formatQualifiedName(qname));
		            write(ASEmitterTokens.BLOCK_CLOSE);
		            write(ASEmitterTokens.SPACE);
		            write(JSDocEmitterTokens.JSDOC_CLOSE);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.FUNCTION);
		            emitParameters(p.getter.getParameterNodes());

		            emitDefinePropertyFunction(p.getter);
	            }
	            if (p.setter != null)
	            {
	            	if (p.getter != null)
	                    writeNewline(ASEmitterTokens.COMMA);
	            		
	            	write(ASEmitterTokens.SET);
		        	write(ASEmitterTokens.COLON);
		            write(ASEmitterTokens.SPACE);
		            write(JSDocEmitterTokens.JSDOC_OPEN);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.ATSIGN);
		            write(ASEmitterTokens.THIS);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.BLOCK_OPEN);
		            write(formatQualifiedName(qname));
		            write(ASEmitterTokens.BLOCK_CLOSE);
		            write(ASEmitterTokens.SPACE);
		            write(JSDocEmitterTokens.JSDOC_CLOSE);
		            write(ASEmitterTokens.SPACE);
		            write(ASEmitterTokens.FUNCTION);
		            emitParameters(p.setter.getParameterNodes());

		            emitDefinePropertyFunction(p.setter);
	            }
	            write(ASEmitterTokens.BLOCK_CLOSE);	            
	        }
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }
    }

    private void loadImportIgnores(String doc) 
    {
    	ArrayList<String> ignoreList = new ArrayList<String>();
    	String ignoreToken = JSFlexJSEmitterTokens.IGNORE_IMPORT.getToken();
    	int index = doc.indexOf(ignoreToken);
    	while (index != -1)
    	{
        	String ignorable = doc.substring(index + ignoreToken.length());
        	int endIndex = ignorable.indexOf("\n");
        	ignorable = ignorable.substring(0, endIndex);
        	ignorable = ignorable.trim();
    		ignoreList.add(ignorable);
    		index = doc.indexOf(ignoreToken, index + endIndex);
    	}
    	this.getDocEmitter();
    	docEmitter.classIgnoreList = ignoreList;
	}

    /*
	@Override
    public void emitInterface(IInterfaceNode node)
    {
        ICompilerProject project = getWalker().getProject();

        getDoc().emitInterfaceDoc(node, project);

        String qname = node.getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(formatQualifiedName(qname));
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }

        
        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
            boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                    || mnode.getNodeID() == ASTNodeID.SetterID;

            writeNewline();
            writeNewline();
            writeNewline();

            getDoc().emitInterfaceMemberDoc((IFunctionNode) mnode, project);
            
            write(formatQualifiedName(qname));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
            write(ASEmitterTokens.MEMBER_ACCESS);
            if (isAccessor)
            {
                writeGetSetPrefix(mnode.getNodeID() == ASTNodeID.GetterID);
            }
            write(mnode.getQualifiedName());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            emitParameters(((IFunctionNode) mnode).getParameterNodes());
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }
    }
	*/
    
    @Override
    public void emitField(IVariableNode node)
    {
        IDefinition definition = getClassDefinition(node);

        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
        if (enode != null)
        {
            if (project == null)
                project = getWalker().getProject();

            def = enode.resolveType(project);
        }

        getDoc().emitFieldDoc(node, def);

        IDefinition ndef = node.getDefinition();

        ModifiersSet modifierSet = ndef.getModifiers();
        String root = "";
        if (modifierSet != null && !modifierSet.hasModifier(ASModifier.STATIC))
        {
            root = JSEmitterTokens.PROTOTYPE.getToken();
            root += ASEmitterTokens.MEMBER_ACCESS.getToken();
        }

        if (definition == null)
            definition = ndef.getContainingScope().getDefinition();

        write(formatQualifiedName(definition.getQualifiedName())
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + root
                + node.getName());

        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
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
                    writeNewline(ASEmitterTokens.SEMICOLON);
                    writeNewline();
                    emitField((IVariableNode) child);
                }
            }
        }
        if (node.getNodeID() == ASTNodeID.BindableVariableID)
        {
        	bindableVars.add(node.getName());            
        }
    }

    private void emitBindableVariables(IClassDefinition cdef)
    {
    	if (bindableVars.size() > 0)
    	{
            write(JSGoogEmitterTokens.OBJECT);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            String qname = cdef.getQualifiedName();
            write(formatQualifiedName(qname));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);        	
            write(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SPACE);
            write("/** @lends {" + formatQualifiedName(qname) + ".prototype} */ ");
            writeNewline(ASEmitterTokens.BLOCK_OPEN);
            
	        boolean firstTime = true;
	        for (String varName : bindableVars)
	        {
	        	if (firstTime)
	        		firstTime = false;
	        	else
	                write(ASEmitterTokens.COMMA);
	            
	        	emitBindableVarDefineProperty(varName, cdef);
	        }
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);

    	}
    }
    
    private void emitBindableVarDefineProperty(String name, IClassDefinition cdef)
    {
	    // 'PropName': {
	    writeNewline("/** @expose */");
	    writeNewline(name + 
	    				ASEmitterTokens.COLON.getToken() +
	    				ASEmitterTokens.SPACE.getToken() +
	    				ASEmitterTokens.BLOCK_OPEN.getToken());
	    indentPush();
	    writeNewline("/** @this {" + formatQualifiedName(cdef.getQualifiedName()) + "} */");
	    writeNewline(ASEmitterTokens.GET.getToken() + ASEmitterTokens.COLON.getToken()
	            + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.FUNCTION.getToken()
	            + ASEmitterTokens.PAREN_OPEN.getToken() + ASEmitterTokens.PAREN_CLOSE.getToken()
	            + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.BLOCK_OPEN.getToken());
	    writeNewline(ASEmitterTokens.RETURN.getToken() + ASEmitterTokens.SPACE.getToken()
	            + ASEmitterTokens.THIS.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken()
	            + name + "_" + ASEmitterTokens.SEMICOLON.getToken());
	    indentPop();
	    writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
	    writeNewline();
	    writeNewline("/** @this {" + formatQualifiedName(cdef.getQualifiedName()) + "} */");
	    writeNewline(ASEmitterTokens.SET.getToken() + ASEmitterTokens.COLON.getToken()
	            + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.FUNCTION.getToken()
	            + ASEmitterTokens.PAREN_OPEN.getToken() + "value" + ASEmitterTokens.PAREN_CLOSE.getToken()
	            + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.BLOCK_OPEN.getToken());
	    writeNewline("if (value != " + ASEmitterTokens.THIS.getToken()
	            + ASEmitterTokens.MEMBER_ACCESS.getToken() + name + "_) {");
	    writeNewline("    var oldValue = "
	            + ASEmitterTokens.THIS.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken()
	            + name + "_" + ASEmitterTokens.SEMICOLON.getToken());
	    writeNewline("    " + ASEmitterTokens.THIS.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken()
	            + name + "_ = value;");
	    writeNewline("    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(");
	    writeNewline("         this, \"" + name + "_\", oldValue, value));");
	    writeNewline("}");
	    write(ASEmitterTokens.BLOCK_CLOSE.getToken());
	    write(ASEmitterTokens.BLOCK_CLOSE.getToken());
    }

    /*
    @Override
    protected void emitAccessors(IAccessorNode node)
    {
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
        fn.parseFunctionBody(getProblems());

        ICompilerProject project = getWalker().getProject();

        getDoc().emitMethodDoc(node, project);

        boolean isConstructor = node.isConstructor();

        String qname = getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(formatQualifiedName(qname));
            if (!isConstructor)
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                if (!fn.hasModifier(ASModifier.STATIC))
                {
                    write(JSEmitterTokens.PROTOTYPE);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                }
            }
        }

        if (!isConstructor)
            emitMemberName(node);

        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);

        emitParameters(node.getParameterNodes());

        boolean hasSuperClass = hasSuperClass(node);

        if (isConstructor && node.getScopedNode().getChildCount() == 0)
        {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            if (hasSuperClass)
                emitSuperCall(node, CONSTRUCTOR_EMPTY);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }

        if (!isConstructor || node.getScopedNode().getChildCount() > 0)
            emitMethodScope(node.getScopedNode());

        if (isConstructor && hasSuperClass)
        {
            writeNewline(ASEmitterTokens.SEMICOLON);
            write(JSGoogEmitterTokens.GOOG_INHERITS);
            write(ASEmitterTokens.PAREN_OPEN);
            write(formatQualifiedName(qname));
            writeToken(ASEmitterTokens.COMMA);
            String sname = getSuperClassDefinition(node, project)
                    .getQualifiedName();
            write(formatQualifiedName(sname));
            write(ASEmitterTokens.PAREN_CLOSE);
        }
    }
    */

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        IASNode cnode = node.getChild(0);

        if (cnode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            cnode = cnode.getChild(0);

        ASTNodeID id = cnode.getNodeID();
        if (id != ASTNodeID.SuperID)
        {
            ICompilerProject project = null;
            IDefinition def = null;

            boolean isClassCast = false;

            if (node.isNewExpression())
            {
                writeToken(ASEmitterTokens.NEW);
            }
            else
            {
                if (project == null)
                    project = getWalker().getProject();

                def = node.getNameNode().resolve(project);

                isClassCast = (def instanceof ClassDefinition ||
                        def instanceof InterfaceDefinition) && 
                        !(NativeUtils.isJSNative(def.getBaseName()));
            }

            if (node.isNewExpression())
            {
                if (project == null)
                    project = getWalker().getProject();

                def = node.resolveCalledExpression(project);
                // all new calls to a class should be fully qualified names
                if (def instanceof ClassDefinition)
                    write(formatQualifiedName(def.getQualifiedName()));
                else
                    // I think we still need this for "new someVarOfTypeClass"
                    getWalker().walk(node.getNameNode());
                write(ASEmitterTokens.PAREN_OPEN);
                walkArguments(node.getArgumentNodes());
                write(ASEmitterTokens.PAREN_CLOSE);
            }
            else if (!isClassCast)
            {
                if (def != null)
                {
                    boolean isInt = def.getBaseName().equals(IASGlobalFunctionConstants._int);
                    if (isInt ||
                        def.getBaseName().equals(IASGlobalFunctionConstants.trace) ||
                        def.getBaseName().equals(IASGlobalFunctionConstants.uint))
                    {
                        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        if (isInt)
                            write(JSFlexJSEmitterTokens.UNDERSCORE);                        
                    }
                }
                getWalker().walk(node.getNameNode());
                write(ASEmitterTokens.PAREN_OPEN);
                walkArguments(node.getArgumentNodes());
                write(ASEmitterTokens.PAREN_CLOSE);
            }
            else
            {
                emitIsAs(node.getArgumentNodes()[0], node.getNameNode(), ASTNodeID.Op_AsID, true);
            }
        }
        else
        {
            emitSuperCall(node, SUPER_FUNCTION_CALL);
        }
    }

    //--------------------------------------------------------------------------

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        // we don't want 'var self = this;' in FlexJS
    	// unless there are anonymous functions
    	if (node.containsAnonymousFunctions())
    		super.emitSelfReference(node);
    }

    private boolean writeThis(IIdentifierNode node)
    {
        IClassNode classNode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        IDefinition nodeDef = ((IIdentifierNode) node).resolve(project);

        IASNode parentNode = node.getParent();
        ASTNodeID parentNodeId = parentNode.getNodeID();

        IASNode firstChild = parentNode.getChild(0);

        boolean identifierIsMemberAccess = parentNodeId == ASTNodeID.MemberAccessExpressionID;

        if (classNode == null) // script in MXML and AS interface definitions
        {
            if (nodeDef instanceof ParameterDefinition)
                return false;
            
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
                return parentNodeId == ASTNodeID.FunctionCallID
                        && !(nodeDef instanceof AccessorDefinition)
                        && !identifierIsMemberAccess;
            }
        }
        else
        {
            if (nodeDef != null
                    && !nodeDef.isInternal()
                    && isClassMember(nodeDef, classNode))
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

    private boolean isClassMember(IDefinition nodeDef, IClassNode classNode)
    {
        TypeScope cscope = (TypeScope) classNode.getDefinition()
                .getContainedScope();

        Set<INamespaceDefinition> nsSet = cscope.getNamespaceSet(project);
        Collection<IDefinition> defs = new HashSet<IDefinition>();

        cscope.getAllPropertiesForMemberAccess((CompilerProject) project, defs,
                nsSet);

        Iterator<IDefinition> visiblePropertiesIterator = defs.iterator();
        while (visiblePropertiesIterator.hasNext())
        {
            if (nodeDef.getQualifiedName().equals(
                    visiblePropertiesIterator.next().getQualifiedName()))
                return true;
        }

        return false;
    }

    private boolean isSameClass(IDefinition pdef, IDefinition thisClass,
            ICompilerProject project)
    {
        if (pdef == thisClass)
            return true;

        IDefinition cdef = ((ClassDefinition) thisClass).resolveBaseClass(project);
        while (cdef != null)
        {
            // needs to be a loop
            if (cdef == pdef)
                return true;
            cdef = ((ClassDefinition) cdef).resolveBaseClass(project);
        }
        return false;
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        if (project == null)
            project = getWalker().getProject();

        IDefinition nodeDef = ((IIdentifierNode) node).resolve(project);

        IASNode parentNode = node.getParent();
        ASTNodeID parentNodeId = parentNode.getNodeID();

        boolean identifierIsAccessorFunction = nodeDef instanceof AccessorDefinition;
        boolean identifierIsPlainFunction = nodeDef instanceof FunctionDefinition
                && !identifierIsAccessorFunction;

        boolean emitName = true;

        if (nodeDef != null
                && nodeDef.isStatic())
        {
            String sname = nodeDef.getParent().getQualifiedName();
            if (sname.length() > 0)
            {
                write(formatQualifiedName(sname));
                write(ASEmitterTokens.MEMBER_ACCESS);
            }
        }
        else if (!NativeUtils.isNative(node.getName()))
        {
            // an instance method as a parameter or
            // a local function
            boolean useGoogBind = (parentNodeId == ASTNodeID.ContainerID
                    && identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                    .getFunctionClassification() == FunctionClassification.CLASS_MEMBER)
                    || (identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                            .getFunctionClassification() == FunctionClassification.LOCAL);

            if (useGoogBind)
            {
                write(JSGoogEmitterTokens.GOOG_BIND);
                write(ASEmitterTokens.PAREN_OPEN);
            }

            if (writeThis(node))
            {
                IFunctionObjectNode functionObjectNode = (IFunctionObjectNode) node.getParent()
                		.getAncestorOfType(IFunctionObjectNode.class);

                if (functionObjectNode != null)
                	write(JSGoogEmitterTokens.SELF);
                else
                	write(ASEmitterTokens.THIS);

                write(ASEmitterTokens.MEMBER_ACCESS);
            }

            if (useGoogBind)
            {
                write(node.getName());

                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.THIS);
                write(ASEmitterTokens.PAREN_CLOSE);

                emitName = false;
            }
        }

        //IDefinition parentDef = (nodeDef != null) ? nodeDef.getParent() : null;
        //boolean isNative = (parentDef != null)
        //        && NativeUtils.isNative(parentDef.getBaseName());
        if (emitName)
        {
            if (nodeDef != null)    
                write(formatQualifiedName(nodeDef.getQualifiedName()));
            else
                write(node.getName());
        }
    }

    //--------------------------------------------------------------------------
    @Override
    protected void emitSuperCall(IASNode node, String type)
    {
        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        if (type == SUPER_FUNCTION_CALL)
        {
            if (fnode == null)
                fnode = (IFunctionNode) fcnode
                        .getAncestorOfType(IFunctionNode.class);

            if (fnode != null && fnode.isConstructor() && !hasSuperClass(fnode))
                return;

	        IClassNode cnode = (IClassNode) node
	                .getAncestorOfType(IClassNode.class);
	
	        // ToDo (erikdebruin): add VF2JS conditional -> only use check during full SDK compilation
	        if (cnode == null)
	            return;
	        
	        if (fnode != null && (fnode.getNodeID() == ASTNodeID.GetterID
	                || fnode.getNodeID() == ASTNodeID.SetterID))
	        {
		        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
		        write(ASEmitterTokens.MEMBER_ACCESS);
		        if (fnode.getNodeID() == ASTNodeID.GetterID)
		        	write(JSFlexJSEmitterTokens.SUPERGETTER);
		        else
		        	write(JSFlexJSEmitterTokens.SUPERSETTER);
		        write(ASEmitterTokens.PAREN_OPEN);
		        write(formatQualifiedName(cnode.getQualifiedName()));
	            writeToken(ASEmitterTokens.COMMA);
		        write(ASEmitterTokens.THIS);
	            writeToken(ASEmitterTokens.COMMA);
	            write(ASEmitterTokens.SINGLE_QUOTE);
	            write(fnode.getName());
	            write(ASEmitterTokens.SINGLE_QUOTE);
	        	        
		        IASNode[] anodes = null;
		        boolean writeArguments = false;
		        if (fcnode != null)
		        {
		            anodes = fcnode.getArgumentNodes();
		
		            writeArguments = anodes.length > 0;
		        }
		        else if (fnode != null && fnode.isConstructor())
		        {
		            anodes = fnode.getParameterNodes();
		
		            writeArguments = (anodes != null && anodes.length > 0);
		        }
		        else if (fnode == null && node instanceof BinaryOperatorAssignmentNode)
		        {
		            BinaryOperatorAssignmentNode bnode = (BinaryOperatorAssignmentNode) node;
		            
		            IFunctionNode pnode = (IFunctionNode) bnode.getAncestorOfType(IFunctionNode.class);
		            
		            if (pnode.getNodeID() == ASTNodeID.SetterID)
		            {
		                writeToken(ASEmitterTokens.COMMA);
		                getWalker().walk(bnode.getRightOperandNode());
		            }
		        }
		
		        if (writeArguments)
		        {
		            int len = anodes.length;
		            for (int i = 0; i < len; i++)
		            {
		                writeToken(ASEmitterTokens.COMMA);
		
		                getWalker().walk(anodes[i]);
		            }
		        }
		
		        write(ASEmitterTokens.PAREN_CLOSE);
		        return;
	        }
        }
        super.emitSuperCall(node, type);
    }
    
    /*
    @Override
    protected void emitDefaultParameterCodeBlock(IFunctionNode node)
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
                write(JSFlexJSEmitterTokens.INDENT);
            }

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());

            for (int i = 0, n = parameters.size(); i < n; i++)
            {
                IParameterNode pnode = parameters.get(i);

                if (pnode != null)
                {
                    code.setLength(0);

                    // x = typeof y !== 'undefined' ? y : z;\n 
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.TYPEOF.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.STRICT_NOT_EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                    code.append(ASEmitterTokens.UNDEFINED.getToken());
                    code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.TERNARY.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.COLON.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getDefaultValue());
                    code.append(ASEmitterTokens.SEMICOLON.getToken());

                    write(code.toString());

                    if (i == n - 1 && !hasBody(node))
                        indentPop();

                    writeNewline();
                }
            }
        }
    }
    */

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        ASTNodeID id = node.getNodeID();
        /*
        if (id == ASTNodeID.Op_InID
                || id == ASTNodeID.Op_LogicalAndAssignID
                || id == ASTNodeID.Op_LogicalOrAssignID)
        {
            super.emitBinaryOperator(node);
        }
        else */ if (id == ASTNodeID.Op_IsID || id == ASTNodeID.Op_AsID)
        {
            emitIsAs(node.getLeftOperandNode(), node.getRightOperandNode(), id, false);
        }
        else if (id == ASTNodeID.Op_InstanceOfID)
        {
            getWalker().walk(node.getLeftOperandNode());

            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.INSTANCEOF);
            
            IDefinition dnode = (node.getRightOperandNode()).resolve(project);
            if (dnode != null)
                write(formatQualifiedName(dnode.getQualifiedName()));
            else
                getWalker().walk(node.getRightOperandNode());
        }
        else
        {
            IExpressionNode leftSide = node.getLeftOperandNode();
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode lnode = leftSide.getChild(0);
                IASNode rnode = leftSide.getChild(1);
                IDefinition rnodeDef = ((IIdentifierNode) rnode).resolve(getWalker().getProject());
                if (lnode.getNodeID() == ASTNodeID.SuperID && 
                		rnodeDef instanceof AccessorDefinition)
                {
                    String op = node.getOperator().getOperatorText();
                    boolean isAssignment = op.contains("=") && !op.contains("==") && 
                    										!(op.startsWith("<") || 
                    												op.startsWith(">") || 
                    												op.startsWith("!"));
                	if (isAssignment)
                	{
                        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
        		        write(JSFlexJSEmitterTokens.SUPERSETTER);
        		        write(ASEmitterTokens.PAREN_OPEN);
        		        IClassNode cnode = (IClassNode) node
        		        	.getAncestorOfType(IClassNode.class);
        		        write(formatQualifiedName(cnode.getQualifiedName()));
        	            writeToken(ASEmitterTokens.COMMA);
        		        write(ASEmitterTokens.THIS);
        	            writeToken(ASEmitterTokens.COMMA);
        	            write(ASEmitterTokens.SINGLE_QUOTE);
        	            write(rnodeDef.getBaseName());
        	            write(ASEmitterTokens.SINGLE_QUOTE);
        	            writeToken(ASEmitterTokens.COMMA);

                		if (op.length() > 1) // += and things like that
                		{
                            write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                            write(ASEmitterTokens.MEMBER_ACCESS);
            		        write(JSFlexJSEmitterTokens.SUPERSETTER);
            		        write(ASEmitterTokens.PAREN_OPEN);
            		        write(formatQualifiedName(cnode.getQualifiedName()));
            	            writeToken(ASEmitterTokens.COMMA);
            		        write(ASEmitterTokens.THIS);
            	            writeToken(ASEmitterTokens.COMMA);
            	            write(ASEmitterTokens.SINGLE_QUOTE);
            	            write(rnodeDef.getBaseName());
            	            write(ASEmitterTokens.SINGLE_QUOTE);
            		        write(ASEmitterTokens.PAREN_CLOSE);
            		        write(op.substring(0, 1));
                		}

                        getWalker().walk(node.getRightOperandNode());
        		        write(ASEmitterTokens.PAREN_CLOSE);
                        return;
                	}
                }
            }
        	
            super.emitBinaryOperator(node);
            /*
            IExpressionNode leftSide = node.getLeftOperandNode();

            IExpressionNode property = null;
            int leftSideChildCount = leftSide.getChildCount();
            if (leftSideChildCount > 0)
            {
                IASNode childNode = leftSide.getChild(leftSideChildCount - 1);
                if (childNode instanceof IExpressionNode)
                    property = (IExpressionNode) childNode;
                else
                    property = leftSide;
            }
            else
                property = leftSide;

            IDefinition def = null;
            if (property instanceof IIdentifierNode)
                def = ((IIdentifierNode) property).resolve(getWalker()
                        .getProject());

            boolean isSuper = false;
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode cnode = leftSide.getChild(0);
                ASTNodeID cId = cnode.getNodeID();

                isSuper = cId == ASTNodeID.SuperID;
            }

            String op = node.getOperator().getOperatorText();
            boolean isAssignment = op.contains("=") && !op.contains("==") && 
            										!(op.startsWith("<") || 
            												op.startsWith(">") || 
            												op.startsWith("!"));

            if (def instanceof AccessorDefinition && isAssignment)
            {
            	// this will make the set_foo call
                getWalker().walk(leftSide);
            }
            else if (isSuper) 
            {
                emitSuperCall(node, "");
            }
            else
            {
                if (ASNodeUtils.hasParenOpen(node))
                    write(ASEmitterTokens.PAREN_OPEN);

                getWalker().walk(leftSide);

                if (node.getNodeID() != ASTNodeID.Op_CommaID)
                    write(ASEmitterTokens.SPACE);

                writeToken(node.getOperator().getOperatorText());

                getWalker().walk(node.getRightOperandNode());

                if (ASNodeUtils.hasParenClose(node))
                    write(ASEmitterTokens.PAREN_CLOSE);
            }
            */
        }
    }

    private void emitIsAs(IExpressionNode left, IExpressionNode right, 
            ASTNodeID id, boolean coercion)
    {
    	// project is null in unit tests
        IDefinition dnode = project != null ? (right).resolve(project) : null;
    	if (id != ASTNodeID.Op_IsID && dnode != null)
    	{
    		// find the function node
            IFunctionNode functionNode = (IFunctionNode) left
            	.getAncestorOfType(IFunctionNode.class);
            if (functionNode != null) // can be null in synthesized binding code
            {
	            ASDocComment asDoc = (ASDocComment) functionNode.getASDocComment();
	            if (asDoc != null)
	            {
		            String asDocString = asDoc.commentNoEnd();
		            String ignoreToken = JSFlexJSEmitterTokens.IGNORE_COERCION.getToken();
		            boolean ignore = false;
		            int ignoreIndex = asDocString.indexOf(ignoreToken);
		            while (ignoreIndex != -1)
		            {
		            	String ignorable = asDocString.substring(ignoreIndex + ignoreToken.length());
		            	int endIndex = ignorable.indexOf("\n");
		            	ignorable = ignorable.substring(0, endIndex);
		            	ignorable = ignorable.trim();
		            	String rightSide = dnode.getQualifiedName();
		            	if (ignorable.equals(rightSide))
		            	{
		                    ignore = true;
		            		break;
		            	}
		            	ignoreIndex = asDocString.indexOf(ignoreToken, ignoreIndex + ignoreToken.length());
		            }
		            if (ignore)
		            {
		                getWalker().walk(left);
		                return;
		            }
	            }
            }
    	}
        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
        write(ASEmitterTokens.MEMBER_ACCESS);
        if (id == ASTNodeID.Op_IsID)
            write(ASEmitterTokens.IS);
        else
            write(ASEmitterTokens.AS);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(left);
        writeToken(ASEmitterTokens.COMMA);

        if (dnode != null)
            write(formatQualifiedName(dnode.getQualifiedName()));
        else
            getWalker().walk(right);
        
        if (coercion) 
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.TRUE);
        }
        
        write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);
        
        IASNode leftNode = node.getLeftOperandNode();
        IASNode rightNode = node.getRightOperandNode();

        if (project == null)
            project = getWalker().getProject();

        IDefinition def = node.resolve(project);
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;

        boolean continueWalk = true;
        if (!isStatic)
        {
            if (!(leftNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) leftNode)
                        .getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS))
            {
            	if (rightNode instanceof UnaryOperatorAtNode)
                {
            		// ToDo (erikdebruin): properly handle E4X
            		
                    write(ASEmitterTokens.THIS);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    getWalker().walk(node.getLeftOperandNode());
                    write(ASEmitterTokens.SQUARE_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write("E4XOperator");
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.SQUARE_CLOSE);
                    continueWalk = false;
                }
            	else if (node.getNodeID() == ASTNodeID.Op_DescendantsID)
            	{
            		// ToDo (erikdebruin): properly handle E4X
            		
                    write(ASEmitterTokens.THIS);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    getWalker().walk(node.getLeftOperandNode());
                    write(ASEmitterTokens.SQUARE_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write("E4XSelector");
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.SQUARE_CLOSE);
                    continueWalk = false;
            	}
            	else if (leftNode.getNodeID() != ASTNodeID.SuperID)
                {
                    getWalker().walk(node.getLeftOperandNode());
                    write(node.getOperator().getOperatorText());
                }
            	else if (leftNode.getNodeID() == ASTNodeID.SuperID &&
            			rightNode.getNodeID() == ASTNodeID.GetterID)
            	{
            		// setter is handled in binaryOperator
                    write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                    write(ASEmitterTokens.MEMBER_ACCESS);
    		        write(JSFlexJSEmitterTokens.SUPERGETTER);
    		        write(ASEmitterTokens.PAREN_OPEN);
    		        IClassNode cnode = (IClassNode) node
    		        	.getAncestorOfType(IClassNode.class);
    		        write(formatQualifiedName(cnode.getQualifiedName()));
    	            writeToken(ASEmitterTokens.COMMA);
    		        write(ASEmitterTokens.THIS);
    	            writeToken(ASEmitterTokens.COMMA);
    	            write(ASEmitterTokens.SINGLE_QUOTE);
    	            write(((GetterNode)rightNode).getName());
    	            write(ASEmitterTokens.SINGLE_QUOTE);
                    continueWalk = false;            		
            	}
            }
            else
            {
                write(ASEmitterTokens.THIS);
                write(node.getOperator().getOperatorText());
            }
        
        }
        
        if (continueWalk)
        	getWalker().walk(node.getRightOperandNode());
        
        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    /*
    private static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    private static IClassDefinition getSuperClassDefinition(
            IDefinitionNode node, ICompilerProject project)
    {
        IClassDefinition parent = (IClassDefinition) node.getDefinition()
                .getParent();
        IClassDefinition superClass = parent.resolveBaseClass(project);
        return superClass;
    }
	*/
    
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
    	String name = node.getName();
    	PropertyNodes p = propertyMap.get(name);
    	if (p == null)
    	{
    		p = new PropertyNodes();
    		propertyMap.put(name, p);
    	}
    	p.getter = node;
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());        
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
    	String name = node.getName();
    	PropertyNodes p = propertyMap.get(name);
    	if (p == null)
    	{
    		p = new PropertyNodes();
    		propertyMap.put(name, p);
    	}
    	p.setter = node;
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());
        
        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
	        IMetaInfo[] metaInfos = null;
	        metaInfos = node.getMetaInfos();
	        for (IMetaInfo metaInfo : metaInfos)
	        {
	            name = metaInfo.getTagName();
	            if (name.equals("Bindable") && metaInfo.getAllAttributes().length == 0)
	            {
	                isBindableSetter = true;
	                break;
	            }
	        }
        }
        if (isBindableSetter)
        {
            IFunctionDefinition definition = node.getDefinition();
            ITypeDefinition type = (ITypeDefinition) definition.getParent();
            getDoc().emitMethodDoc(fn, project);
            write(formatQualifiedName(type.getQualifiedName()));
            if (!node.hasModifier(ASModifier.STATIC))
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
            }

            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__");
            writeToken(node.getName());
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            emitParameters(node.getParameterNodes());
            //writeNewline();
            emitMethodScope(node.getScopedNode());
        }
    }
   
    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
    	//TODO: ajh  is this method needed anymore?
    	
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();

        // ToDo (erikdebruin): add VF2JS conditional -> only use check during full SDK compilation
        if (type == null)
            return;
        
        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
	        IMetaInfo[] metaInfos = null;
	        metaInfos = node.getMetaInfos();
	        for (IMetaInfo metaInfo : metaInfos)
	        {
	            String name = metaInfo.getTagName();
	            if (name.equals("Bindable") && metaInfo.getAllAttributes().length == 0)
	            {
	                isBindableSetter = true;
	                break;
	            }
	        }
        }
        if (isBindableSetter)
        {
            getDoc().emitMethodDoc(fn, project);
            write(formatQualifiedName(type.getQualifiedName()));
            if (!node.hasModifier(ASModifier.STATIC))
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
            }

            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__");
            writeToken(node.getName());
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            emitParameters(node.getParameterNodes());
            //writeNewline();
            emitMethodScope(node.getScopedNode());
        }
        super.emitObjectDefineProperty(node);
    }
    
    @Override
    protected void emitDefinePropertyFunction(IAccessorNode node)
    {
        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
	        IMetaInfo[] metaInfos = null;
	        metaInfos = node.getMetaInfos();
	        for (IMetaInfo metaInfo : metaInfos)
	        {
	            String name = metaInfo.getTagName();
	            if (name.equals("Bindable") && metaInfo.getAllAttributes().length == 0)
	            {
	                isBindableSetter = true;
	                break;
	            }
	        }
        }
        if (isBindableSetter)
        {
            //write(ASEmitterTokens.FUNCTION);
            //emitParameters(node.getParameterNodes());
            write(ASEmitterTokens.SPACE);
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(node.getName());
            //write(ASEmitterTokens.PAREN_OPEN);
            //write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
            
            // add change check
            write(ASEmitterTokens.IF);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.PAREN_OPEN);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.STRICT_EQUAL);
            write(ASEmitterTokens.SPACE);
            IParameterNode[] params = node.getParameterNodes();
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.RETURN);
            writeNewline(ASEmitterTokens.SEMICOLON);

            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__" + node.getName());
            write(ASEmitterTokens.PAREN_OPEN);
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
            
        	// add dispatch of change event
            writeNewline("    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(");
            writeNewline("         this, \"" + node.getName() + "\", oldValue, " + params[0].getName() + "));");
            write(ASEmitterTokens.BLOCK_CLOSE);
            //writeNewline(ASEmitterTokens.SEMICOLON);
            writeNewline();
            writeNewline();
        }
        else
        	super.emitDefinePropertyFunction(node);
    }
        
    /*
    private void writeGetSetPrefix(boolean isGet)
    {
        if (isGet)
            write(ASEmitterTokens.GET);
        else
            write(ASEmitterTokens.SET);
        write("_");
    }
	*/
    
    private JSFlexJSDocEmitter docEmitter = null;
    
    @Override
    public IDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
        	docEmitter = new JSFlexJSDocEmitter(this);
        return docEmitter;
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        writeNewline("/**");
        writeNewline(" * " + type.getQualifiedName());
        writeNewline(" *");
        writeNewline(" * @fileoverview");
        writeNewline(" *");
        writeNewline(" * @suppress {checkTypes}");
        writeNewline(" */");
        writeNewline();
        
        /* goog.provide('x');\n\n */
        write(JSGoogEmitterTokens.GOOG_PROVIDE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(formatQualifiedName(type.getQualifiedName()));
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();
        
        ArrayList<String> writtenRequires = new ArrayList<String>();

        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        ITypeNode typeNode = type.getNode();
        if (typeNode instanceof ClassNode)
        {
	        ClassNode classNode = (ClassNode) typeNode;
	        if (classNode != null)
	        {
	            ASDocComment asDoc = (ASDocComment) classNode.getASDocComment();
	            if (asDoc != null)
	            {
		            String asDocString = asDoc.commentNoEnd();
		            String ignoreToken = JSFlexJSEmitterTokens.IGNORE_IMPORT.getToken();
		            int ignoreIndex = asDocString.indexOf(ignoreToken);
		            while (ignoreIndex != -1)
		            {
		            	String ignorable = asDocString.substring(ignoreIndex + ignoreToken.length());
		            	int endIndex = ignorable.indexOf("\n");
		            	ignorable = ignorable.substring(0, endIndex);
		            	ignorable = ignorable.trim();
		            	// pretend we've already written the goog.requires for this
		            	writtenRequires.add(ignorable);
		            	ignoreIndex = asDocString.indexOf(ignoreToken, ignoreIndex + ignoreToken.length());
		            }
	            }
	        }
        }
        
        if (project == null)
            project = getWalker().getProject();

        FlexJSProject flexProject = (FlexJSProject) project;
        ASProjectScope projectScope = (ASProjectScope) flexProject.getScope();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(type);
        ArrayList<String> requiresList = flexProject.getRequires(cu);
        ArrayList<String> interfacesList = flexProject.getInterfaces(cu);

        String cname = type.getQualifiedName();
        writtenRequires.add(cname); // make sure we don't add ourselves

        boolean emitsRequires = false;
        if (requiresList != null)
        {
            for (String imp : requiresList)
            {
                if (imp.indexOf(JSGoogEmitterTokens.AS3.getToken()) != -1)
                    continue;

                if (imp.equals(cname))
                    continue;

                if (NativeUtils.isNative(imp))
                    continue;

                if (writtenRequires.indexOf(imp) == -1)
                {

                    /* goog.require('x');\n */
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(formatQualifiedName(imp));
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);
                    
                    writtenRequires.add(imp);
                    
                    emitsRequires = true;
                }
            }
        }
        
        boolean emitsInterfaces = false;
        if (interfacesList != null)
        {
            for (String imp : interfacesList)
            {
                if (writtenRequires.indexOf(imp) == -1)
                {
	                write(JSGoogEmitterTokens.GOOG_REQUIRE);
	                write(ASEmitterTokens.PAREN_OPEN);
	                write(ASEmitterTokens.SINGLE_QUOTE);
	                write(formatQualifiedName(imp));
	                write(ASEmitterTokens.SINGLE_QUOTE);
	                write(ASEmitterTokens.PAREN_CLOSE);
	                writeNewline(ASEmitterTokens.SEMICOLON);
	                
	                emitsInterfaces = true;
                }
            }
        }
        
        // erikdebruin: Add missing language feature support, with e.g. 'is' and 
        //              'as' operators. We don't need to worry about requiring
        //              this in every project: ADVANCED_OPTIMISATIONS will NOT
        //              include any of the code if it is not used in the project.
        boolean isMainCU = flexProject.mainCU != null && 
                    cu.getName().equals(flexProject.mainCU.getName());
        if (isMainCU)
        {
            write(JSGoogEmitterTokens.GOOG_REQUIRE);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
        }

        if (emitsRequires || emitsInterfaces || isMainCU)
        {
            writeNewline();
        }

        writeNewline();
        writeNewline();
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        ITypeNode tnode = findTypeNode(definition.getNode());
        if (tnode != null)
        {
            /*
             * Metadata
             * 
             * @type {Object.<string, Array.<Object>>}
             */
            writeNewline();
            writeNewline();
            writeNewline();
            getDoc().begin();
            writeNewline(" * Metadata");
            writeNewline(" *");
            writeNewline(" * @type {Object.<string, Array.<Object>>}");
            getDoc().end();

            // a.B.prototype.AFJS_CLASS_INFO = {  };
            write(formatQualifiedName(type.getQualifiedName()));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
            write(ASEmitterTokens.MEMBER_ACCESS);
            writeToken(JSFlexJSEmitterTokens.FLEXJS_CLASS_INFO);
            writeToken(ASEmitterTokens.EQUAL);
            writeToken(ASEmitterTokens.BLOCK_OPEN);
            
            // names: [{ name: '', qName: '' }]
            write(JSFlexJSEmitterTokens.NAMES);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.SQUARE_OPEN);
            writeToken(ASEmitterTokens.BLOCK_OPEN);
            write(JSFlexJSEmitterTokens.NAME);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(tnode.getName());
            write(ASEmitterTokens.SINGLE_QUOTE);
            writeToken(ASEmitterTokens.COMMA);
            write(JSFlexJSEmitterTokens.QNAME);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(formatQualifiedName(tnode.getQualifiedName()));
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SQUARE_CLOSE);

            IExpressionNode[] enodes;
            if (tnode instanceof IClassNode)
                enodes = ((IClassNode) tnode).getImplementedInterfaceNodes();
            else
                enodes = ((IInterfaceNode) tnode).getExtendedInterfaceNodes();
            
            if (enodes.length > 0)
            {
                writeToken(ASEmitterTokens.COMMA);

                // interfaces: [a.IC, a.ID]
                write(JSFlexJSEmitterTokens.INTERFACES);
                writeToken(ASEmitterTokens.COLON);
                write(ASEmitterTokens.SQUARE_OPEN);
                int i = 0;
                for (IExpressionNode enode : enodes)
                { 
                    write(formatQualifiedName(enode.resolve(project).getQualifiedName()));
                    if (i < enodes.length - 1)
                        writeToken(ASEmitterTokens.COMMA);
                    i++;
                }
                write(ASEmitterTokens.SQUARE_CLOSE);
            }

            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
        }
    }

    private int foreachLoopCounter = 0;

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        String iterName = "foreachiter"
                + new Integer(foreachLoopCounter).toString();
        foreachLoopCounter++;

        write(ASEmitterTokens.FOR);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.VAR);
        write(ASEmitterTokens.SPACE);
        write(iterName);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.IN);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        if (childNode instanceof IVariableExpressionNode)
        {
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName());
        }
        else
            write(((IIdentifierNode) childNode).getName());
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        write(ASEmitterTokens.SQUARE_OPEN);
        write(iterName);
        write(ASEmitterTokens.SQUARE_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
        writeNewline();
        getWalker().walk(node.getStatementContentsNode());
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();

    }

    /*
    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        write(ASEmitterTokens.TRY);
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        
        write(JSGoogEmitterTokens.GOOG_ARRAY_FOREACH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.COMMA);
        writeToken(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        if (childNode instanceof IVariableExpressionNode)
        	write(((IVariableNode) childNode.getChild(0)).getName());
        else
        	write(((IIdentifierNode) childNode).getName());
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        if (isImplicit(xnode))
            write(ASEmitterTokens.BLOCK_OPEN);
        getWalker().walk(node.getStatementContentsNode());
        if (isImplicit(xnode))
        {
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        write(ASEmitterTokens.CATCH);
        write(ASEmitterTokens.PAREN_OPEN);
        write("foreachbreakerror");
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
    	// look for break in foreach and throw error instead
    	if (node.getKind() == IIterationFlowNode.IterationFlowKind.BREAK)
    	{
    		IASNode pNode = node.getParent();
    		while (pNode != null)
    		{
    			ASTNodeID id = pNode.getNodeID();
    			if (id == ASTNodeID.ForEachLoopID)
    			{
    				write(ASEmitterTokens.THROW);
    				write(ASEmitterTokens.SPACE);
    				write(ASEmitterTokens.NEW);
    				write(ASEmitterTokens.SPACE);
    				write(JSGoogEmitterTokens.ERROR);
    				write(ASEmitterTokens.PAREN_OPEN);
    				write(ASEmitterTokens.PAREN_CLOSE);
    				write(ASEmitterTokens.SEMICOLON);
    				return;
    			}
    			else if (id == ASTNodeID.ForLoopID ||
    					id == ASTNodeID.DoWhileLoopID ||
    					id == ASTNodeID.WhileLoopID)
    				break;
    			pNode = pNode.getParent();
    		}
    	}
        write(node.getKind().toString().toLowerCase());
        IIdentifierNode lnode = node.getLabelNode();
        if (lnode != null)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(lnode);
        }
    }
    */

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        boolean isWritten = false;
        
        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
                // ToDo (erikdebruin): VF2JS -> handle XML output properly...
            	
                write("'" + s + "'");
                
                isWritten = true;
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", "\\\\n");
        }
        
        if (!isWritten)
        {
            write(s);
        }
    }
    
    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
    	// ToDo (erikdebruin): implement E4X replacement !?!
        write(ASEmitterTokens.SINGLE_QUOTE);
        write("E4XFilter");
        write(ASEmitterTokens.SINGLE_QUOTE);
    }
    
    /*
    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (node.getNodeID() == ASTNodeID.Op_PreIncrID
                || node.getNodeID() == ASTNodeID.Op_PreDecrID
                || node.getNodeID() == ASTNodeID.Op_PostIncrID
                || node.getNodeID() == ASTNodeID.Op_PostDecrID)
        {
            IExpressionNode opNode = node.getOperandNode();
            String getString = stringifyNode(opNode);
            int index = getString.lastIndexOf("get_");
            if (index != -1)
            {
                write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                write(ASEmitterTokens.MEMBER_ACCESS);
            	if (node.getNodeID() == ASTNodeID.Op_PreIncrID)
            		write(JSFlexJSEmitterTokens.PREINCREMENT);
            	else if (node.getNodeID() == ASTNodeID.Op_PostIncrID)
            		write(JSFlexJSEmitterTokens.POSTINCREMENT);
            	else if (node.getNodeID() == ASTNodeID.Op_PreDecrID)
            		write(JSFlexJSEmitterTokens.PREDECREMENT);
            	else
            		write(JSFlexJSEmitterTokens.POSTDECREMENT);
                write(ASEmitterTokens.PAREN_OPEN);
                String obj = getString.substring(0, index - 1);
                write(obj);
                write(ASEmitterTokens.COMMA);
            	String prop = getString.substring(index + 4);            	
            	int endIndex = prop.indexOf(ASEmitterTokens.PAREN_OPEN.getToken());
            	prop = prop.substring(0, endIndex);
                write(ASEmitterTokens.DOUBLE_QUOTE);
            	write(prop);
                write(ASEmitterTokens.DOUBLE_QUOTE);
                write(ASEmitterTokens.PAREN_CLOSE);
                return;
            }
            else
            {
            	IASNode parentNode = node.getParent();
            	if (parentNode.getNodeID() == ASTNodeID.MemberAccessExpressionID &&
            			((MemberAccessExpressionNode)parentNode).getLeftOperandNode() == node)
            	{
            		// GCC wanted parens around foo++.toString().  As in (foo++).toString();
	                write(ASEmitterTokens.PAREN_OPEN);
	                super.emitUnaryOperator(node);
	                write(ASEmitterTokens.PAREN_CLOSE);
	                return;
            	}
            }

        }
        super.emitUnaryOperator(node);
    }
    */
    
    @Override
    protected String formatQualifiedName(String name)
    {
    	if (name.contains("goog.") || name.startsWith("Vector."))
    		return name;
    	name = name.replaceAll("\\.", "_");
    	return name;
    }
}
