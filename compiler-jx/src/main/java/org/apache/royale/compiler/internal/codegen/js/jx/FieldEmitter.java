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

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleDocEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.utils.NativeUtils;

/**
 * Static or member variables of a class. For local variables in a function, see
 * VarDeclarationEmitter. For accessors, see AccessorEmitter.
 */
public class FieldEmitter extends JSSubEmitter implements
        ISubEmitter<IVariableNode>
{
    public FieldEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public boolean hasComplexStaticInitializers = false;
    
    private boolean isComplex(IExpressionNode vnode, IDefinition definition)
    {
    	if (EmitterUtils.isScalar(vnode))
    		return false;
    	
    	IClassDefinition cdef = (IClassDefinition)definition;
    	
    	// walk the tree of nodes looking for IdentifierNodes
    	// and see if they resolve to external dependencies
    	return isExternalReference(vnode, cdef);
    }
    
    private boolean isExternalReference(IExpressionNode vnode, IClassDefinition cdef)
    {
    	if (vnode.getNodeID() == ASTNodeID.IdentifierID)
    	{
    		IDefinition def = vnode.resolve(getProject());
    		if (def == null)  // saw this for a package reference (org in org.apache)
    			return false;
    		String qname = def.getQualifiedName();
    		if (NativeUtils.isJSNative(qname))
    			return false;
    		if (def instanceof IClassDefinition)
    			return !(qname.contentEquals(cdef.getQualifiedName()));
    		def = def.getParent();
    		if (def != null)
    		{
    			qname = def.getQualifiedName();
                if (NativeUtils.isJSNative(qname))
                    return false;
    			return !(qname.contentEquals(cdef.getQualifiedName()));
    		}
    	}
    	int n = vnode.getChildCount();
    	for (int i = 0; i < n; i++)
    	{
    		IASNode childNode = vnode.getChild(i);
    		if (childNode instanceof IExpressionNode)
    		{
    			if (isExternalReference((IExpressionNode)childNode, cdef))
    				return true;
    		}
    		//if  childNode is a ContainerNode (e.g. argumentsNode of FunctionCallNode),
            //should we be checking the arguments as well? Doing so (*and* returning true because one of the argument nodes is an external reference)
            //seems to cause issues, so avoiding this for now.
    	}
    	return false;
    }
    
    @Override
    public void emit(IVariableNode node)
    {
        IExpressionNode vnode = node.getAssignedValueNode();
        boolean isBindable = (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst());
        IDefinition ndef = node.getDefinition();
        IDefinition definition = EmitterUtils.getClassDefinition(node);
        if (definition == null && ndef != null)
        {
        	definition = ndef.getParent();
        }
        boolean isComplexInitializedStatic = vnode != null && ndef.isStatic() && !isBindable && isComplex(vnode, definition);
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();
        if (enode != null)
        {
            def = enode.resolveType(getProject());
        }
        IExpressionNode nameNode = node.getNameExpressionNode();

        // TODO (mschmalle)
        if (getEmitter().getDocEmitter() instanceof IJSRoyaleDocEmitter && !isComplexInitializedStatic)
        {
            ((IJSRoyaleDocEmitter) getEmitter().getDocEmitter()).emitFieldDoc(node, def, getProject());
        }


        String className = null;
        String root = "";
        IVariableDefinition.VariableClassification classification = node.getVariableClassification();
        boolean isPackageOrFileMember = classification == IVariableDefinition.VariableClassification.PACKAGE_MEMBER ||
                classification == IVariableDefinition.VariableClassification.FILE_MEMBER;
        if (isPackageOrFileMember)
        {
            String qualifiedName = node.getQualifiedName();
            if (fjs.getModel().isExterns && node.getName().equals(qualifiedName))
            {
                writeToken(ASEmitterTokens.VAR);
            }
            write(fjs.formatQualifiedName(qualifiedName));
        }
        else
        {
            ModifiersSet modifierSet = ndef.getModifiers();
            if (modifierSet != null && !modifierSet.hasModifier(ASModifier.STATIC))
            {
                root = JSEmitterTokens.PROTOTYPE.getToken();
                root += ASEmitterTokens.MEMBER_ACCESS.getToken();
            }

            if (definition == null)
                definition = ndef.getContainingScope().getDefinition();

            className = getEmitter().formatQualifiedName(definition.getQualifiedName());
            if (isComplexInitializedStatic)
            {
                emitComplexInitializedStatic(node, className, def);
            }
            else
            {
                startMapping(nameNode);
	            write(className);
                write(ASEmitterTokens.MEMBER_ACCESS.getToken());
                write(root);
                endMapping(nameNode);
                String nodeName = node.getName();
	            String qname = nodeName;
	            IDefinition nodeDef = node.getDefinition();
	            if (nodeDef != null && !nodeDef.isStatic() && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
                {
                    qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
                }
	    
	            if (EmitterUtils.isCustomNamespace(node.getNamespace()))
                {
	                INamespaceDecorationNode ns = ((VariableNode) node).getNamespaceNode();
	                INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getProject());
	                fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
	                String s = nsDef.getURI();
                    startMapping(nameNode, node.getName());
	                write(JSRoyaleEmitter.formatNamespacedProperty(s, qname, false));
                    endMapping(nameNode);
	            }
	            else
                {
                    String symbolName = null;
                    if (!qname.equals(nodeName))
                    {
                        symbolName = nodeName;
                    }
                    startMapping(nameNode, symbolName);
                    write(qname);
                    endMapping(nameNode);
                }
            }
        }

        if (isBindable)
        {
            // add an underscore to convert this var to be the
            // backing var for the get/set pair that will be generated later.
            write("_");
        }
        if (vnode != null && !isComplexInitializedStatic)
        {
        	getModel().inStaticInitializer = ndef.isStatic() || isPackageOrFileMember;
            String vnodeString = getEmitter().stringifyNode(vnode);
            if (ndef.isStatic() && vnode instanceof FunctionCallNode)
            {
                FunctionCallNode fcn = (FunctionCallNode)vnode;
            	if (fcn.getNameNode() instanceof IdentifierNode)
            	{
            		IDefinition d = fcn.getNameNode().resolve(getProject());
            		// assume this is a call to static method in the class
            		// otherwise it would be a memberaccessexpression?
            		if (d instanceof FunctionDefinition)
            		{
            			FunctionDefinition fd = (FunctionDefinition)d;
                		IASNode m = fd.getNode();
                		if (m != null)
                		{
    	            		// re-emit it to collect static initializer class references in usedNames
    	            		getEmitter().stringifyNode(m);
                		}
                    }
                    //it could also be a constructor
                    else if (d instanceof IClassDefinition)
                    {
                        IClassDefinition classDef = (IClassDefinition) d;
                        IFunctionDefinition constructorDef = classDef.getConstructor();
                        if (constructorDef != null)
                        {
                            IASNode m = constructorDef.getNode();
                            if (m != null)
                            {
                                // re-emit it to collect static initializer class references in usedNames
                                getEmitter().stringifyNode(m);
                            }
                        }
                    }
            	}
            }
        	getModel().inStaticInitializer = false;
        	if ((ndef.isStatic() && !EmitterUtils.needsStaticInitializer(vnodeString, className)) ||
        			(!ndef.isStatic() && EmitterUtils.isScalar(vnode)) ||
        			isPackageOrFileMember)
	        {
                IExpressionNode beforeNode = enode;
                if (beforeNode.getAbsoluteStart() == -1)
                {
                    beforeNode = nameNode;
                }
	            startMapping(node, beforeNode);
	            write(ASEmitterTokens.SPACE);
	            writeToken(ASEmitterTokens.EQUAL);
	            endMapping(node);
                getEmitter().emitAssignmentCoercion(vnode, node.getVariableTypeNode().resolve(getProject()));
	        }
	        else if (ndef.isStatic() && EmitterUtils.needsStaticInitializer(vnodeString, className))
	        {
	        	hasComplexStaticInitializers = true;
	        }
	        
	        if (!isPackageOrFileMember  && !ndef.isStatic() && !EmitterUtils.isScalar(vnode)
                    && getProject() instanceof RoyaleJSProject
                    && ((RoyaleJSProject) getProject()).config != null
                    && ((RoyaleJSProject) getProject()).config.getJsDefaultInitializers()
            )
	        {
	            //this value will actually be initialized inside the constructor.
                //but if default initializers is set, we define it on the prototype with null value first.
	            //Why?: this needs to be defined on the prototype to support reflection
                //otherwise the constructor initializers will create the new property value on 'this' and
                //there is no runtime clue to separate what is 'dynamic' and what is 'inherited'
                //these clues throughout the prototype chain are important for runtime identification
                //of dynamic fields.
                //runtime checks will only work accurately using this technique if the entire inheritance chain
                //for the reflection target is compiled with default js initializers, because it permits
                //inspection of the prototype chain to determine all the sealed members, and isolate them
                //from whatever else is defined as 'own' properties on the instance (which can be assumed to be
                // 'dynamic' properties).
                write(ASEmitterTokens.SPACE);
                writeToken(ASEmitterTokens.EQUAL);
                write(ASEmitterTokens.NULL);
            }
        }
        if (vnode == null && def != null)
        {
            emitDefaultInitializerForDefinition(def);
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
                    getEmitter().emitField((IVariableNode) child);
                }
            }
        }
        if (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst())
        {
            putBindableVariable(node, def);
        }
    }
    
    private String getFieldName(IVariableNode node, JSRoyaleEmitter fjs)
    {
        String qname = node.getName();
        IDefinition nodeDef = node.getDefinition();
        if (nodeDef != null && !nodeDef.isStatic() && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
    			qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);

        if (EmitterUtils.isCustomNamespace(node.getNamespace())) {
            INamespaceDecorationNode ns = ((VariableNode) node).getNamespaceNode();
            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getProject());
            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
            String s = nsDef.getURI();
            return JSRoyaleEmitter.formatNamespacedProperty(s, qname, false);
        }
        return qname;
    }

    public boolean emitFieldInitializer(IVariableNode node)
    {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        IDefinition definition = EmitterUtils.getClassDefinition(node);

        IDefinition ndef = node.getDefinition();
        String className = null;

        IVariableDefinition.VariableClassification classification = node.getVariableClassification();
        boolean isPackageOrFileMember = classification == IVariableDefinition.VariableClassification.PACKAGE_MEMBER ||
                classification == IVariableDefinition.VariableClassification.FILE_MEMBER;
        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
            String vnodeString = getEmitter().stringifyNode(vnode);
            if (definition == null)
                definition = ndef.getContainingScope().getDefinition();
            className = getEmitter().formatQualifiedName(definition.getQualifiedName());
        	if (ndef.isStatic() && EmitterUtils.needsStaticInitializer(vnodeString, className) && !isPackageOrFileMember)
	        {
                writeNewline();
                write(className);
                write(ASEmitterTokens.MEMBER_ACCESS.getToken());

                if (EmitterUtils.isCustomNamespace(node.getNamespace())) {
	                INamespaceDecorationNode ns = ((VariableNode) node).getNamespaceNode();
	                INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getProject());
	                fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
	                String s = nsDef.getURI();
	                write(JSRoyaleEmitter.formatNamespacedProperty(s, node.getName(), false));
	            }
	            else write(node.getName());
	
	            if (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst())
	            {
	                // add an underscore to convert this var to be the
	                // backing var for the get/set pair that will be generated later.
	                write("_");
	            }
	            write(ASEmitterTokens.SPACE);
	            writeToken(ASEmitterTokens.EQUAL);
	            write(vnodeString);
	            write(ASEmitterTokens.SEMICOLON);
                return true;
            }
        }

        return false;
    }

    private void emitComplexInitializedStatic(IVariableNode node, String className, IDefinition variableTypeExprDef) {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        IExpressionNode vnode = node.getAssignedValueNode();
        IExpressionNode nameNode = node.getNameExpressionNode();
        write(className);
        write(ASEmitterTokens.MEMBER_ACCESS.getToken());
        write(fjs.formatGetter(getFieldName(node, fjs)));
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        String vnodeString = getEmitter().stringifyNode(vnode);
        writeToken(ASEmitterTokens.VAR);
        writeToken("value");
        writeToken(ASEmitterTokens.EQUAL);
        startMapping(vnode);
        write(vnodeString);
        endMapping(vnode);
        writeNewline(ASEmitterTokens.SEMICOLON);
        write(IASLanguageConstants.Object);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTIES);
        write(ASEmitterTokens.PAREN_OPEN);
        write(className);
        writeToken(ASEmitterTokens.COMMA);
        writeToken(ASEmitterTokens.BLOCK_OPEN);
        write(getFieldName(node, fjs));
        writeToken(ASEmitterTokens.COLON);
        if (node.isConst())
            write("{ value: value, writable: false }");
        else
            write("{ value: value, writable: true }");
        write(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeToken(ASEmitterTokens.RETURN);
        write("value");
        indentPop();
        writeNewline(ASEmitterTokens.SEMICOLON);
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        if (!node.isConst())
        {
            write(className);
            write(ASEmitterTokens.MEMBER_ACCESS.getToken());
            write(fjs.formatSetter(getFieldName(node, fjs)));
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            write(ASEmitterTokens.PAREN_OPEN);
            write("value");
            writeToken(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
            write(IASLanguageConstants.Object);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            write(className);
            writeToken(ASEmitterTokens.COMMA);
            writeToken(ASEmitterTokens.BLOCK_OPEN);
            write(getFieldName(node, fjs));
            writeToken(ASEmitterTokens.COLON);
            write("{ value: value, writable: true }");
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            indentPop();
            writeNewline(ASEmitterTokens.SEMICOLON);
            write(ASEmitterTokens.BLOCK_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
        }
        //Fix for references to the target : the following empty declaration is required for @lends to work in Object.defineProperties below
        //otherwise references elsewhere in code to the target can be renamed (and therefore do not work)
        if (getEmitter().getDocEmitter() instanceof IJSRoyaleDocEmitter)
        {
            ((IJSRoyaleDocEmitter) getEmitter().getDocEmitter()).emitFieldDoc(node, variableTypeExprDef, getProject());
        }
        startMapping(node);
        write(className);
        write(ASEmitterTokens.MEMBER_ACCESS);
        endMapping(node);
        String symbolName = null;
        String nodeName = node.getName();
        String fieldName = getFieldName(node, fjs);
        if (!fieldName.equals(nodeName))
        {
            symbolName = nodeName;
        }
        startMapping(nameNode, symbolName);
        write(fieldName);
        endMapping(nameNode);
        startMapping(node);
        write(ASEmitterTokens.SEMICOLON);
        endMapping(node);
        writeNewline();
        writeNewline();
        write(IASLanguageConstants.Object);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTIES);
        write(ASEmitterTokens.PAREN_OPEN);
        write(className);
        writeToken(ASEmitterTokens.COMMA);
        write("/** @lends {" + className
                + "} */ ");
        writeNewline(ASEmitterTokens.BLOCK_OPEN);
        // TODO (mschmalle)
        if (getEmitter().getDocEmitter() instanceof IJSRoyaleDocEmitter)
        {
            ((IJSRoyaleDocEmitter) getEmitter().getDocEmitter()).emitFieldDoc(node, variableTypeExprDef, getProject());
        }
        write(getFieldName(node, fjs));
        writeToken(ASEmitterTokens.COLON);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        write(ASEmitterTokens.GET);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SPACE);
        write(className);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(fjs.formatGetter(getFieldName(node, fjs)));
        if (!node.isConst())
        {
            writeNewline(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SET);
            write(ASEmitterTokens.COLON);
            write(ASEmitterTokens.SPACE);
            write(className);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(fjs.formatSetter(getFieldName(node, fjs)));
        }
        writeNewline(ASEmitterTokens.COMMA);
        write("configurable: true");
        write(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        indentPop();
    }

    private void emitDefaultInitializerForDefinition(IDefinition def) {

        String defName = def.getQualifiedName();
        if (defName.equals("int") || defName.equals("uint"))
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write("0");
        }
        else
        {
            boolean defaultInitializers = false;
            ICompilerProject project = getProject();
            if(project instanceof RoyaleJSProject)
            {
                RoyaleJSProject fjsProject = (RoyaleJSProject) project;
                if(fjsProject.config != null)
                {
                    defaultInitializers = fjsProject.config.getJsDefaultInitializers();
                }
            }
            if (defaultInitializers)
            {
                if (defName.equals("Number"))
                {
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(IASKeywordConstants.NA_N);
                }
                else if (defName.equals("Boolean"))
                {
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(IASKeywordConstants.FALSE);
                    
                } else if (defName.equals("*")) {
                    //setting the value to *undefined* is needed  to create the field
                    //on the prototype - this is important for reflection purposes
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.UNDEFINED);
                }
                else
                {
                    //everything else should default to null
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(IASKeywordConstants.NULL);
                }
            }
        }
    }

    private void putBindableVariable(IVariableNode node, IDefinition variableTypeExprDef) {
        if (getModel().getBindableVars().get(node.getName()) == null) {
            BindableVarInfo bindableVarInfo = new BindableVarInfo();
            bindableVarInfo.isStatic = node.hasModifier(ASModifier.STATIC);;
            bindableVarInfo.namespace = node.getNamespace();
            bindableVarInfo.type = variableTypeExprDef.getQualifiedName();
            getModel().getBindableVars().put(node.getName(), bindableVarInfo);
            IMetaTagsNode metaTags = node.getMetaTags();
            if (metaTags != null) {
                IMetaTagNode[] tags = metaTags.getAllTags();
                if (tags.length > 0)
                    bindableVarInfo.metaTags = tags;
            }
        }
    }
}
