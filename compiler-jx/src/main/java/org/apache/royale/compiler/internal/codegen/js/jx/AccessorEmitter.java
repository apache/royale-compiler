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

import java.util.HashMap;
import java.util.Set;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.PropertyNodes;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.SetterNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.ISetterNode;

public class AccessorEmitter extends JSSubEmitter implements
        ISubEmitter<IAccessorNode>
{

    public AccessorEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IAccessorNode node)
    {
        if (node.getNodeID() == ASTNodeID.GetterID)
        {
            emitGet((IGetterNode) node);
        }
        else if (node.getNodeID() == ASTNodeID.SetterID)
        {
            emitSet((ISetterNode) node);
        }
    }

    public void emit(IClassDefinition definition)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        RoyaleJSProject project = (RoyaleJSProject)getWalker().getProject();
        boolean emitExports = true;
        if (project != null && project.config != null)
        	emitExports = project.config.getExportPublicSymbols();

        if (!getModel().getPropertyMap().isEmpty())
        {
            String qname = definition.getQualifiedName();
            Set<String> propertyNames = getModel().getPropertyMap().keySet();
            for (String propName : propertyNames)
            {
                PropertyNodes p = getModel().getPropertyMap().get(propName);
                IGetterNode getterNode = p.getter;
                ISetterNode setterNode = p.setter;
                if (getModel().isExterns)
                {
                	IAccessorNode node = (getterNode != null) ? getterNode : setterNode;
                    writeNewline();
                    writeNewline();
                    writeNewline();
                	writeNewline("/**");
                    if (emitExports)
                    	writeNewline("  * @export");
                    if (p.type != null)
                    	writeNewline("  * @type {"+ JSGoogDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "} */");
                    else
                    	writeNewline("  */");
                    write(getEmitter().formatQualifiedName(qname));
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSEmitterTokens.PROTOTYPE);
                    if (fjs.isCustomNamespace((FunctionNode)node))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                    	write(propName);
                    }
                    write(ASEmitterTokens.SEMICOLON);                	
                	
                }
                else
                {
	                if (getterNode != null)
	                {
	                    writeNewline();
	                    writeNewline();
	                    writeNewline();
	                    write(getEmitter().formatQualifiedName(qname));
	                    write(ASEmitterTokens.MEMBER_ACCESS);
	                    write(JSEmitterTokens.PROTOTYPE);
	                    if (fjs.isCustomNamespace((FunctionNode)getterNode))
	                    {
	            			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
	            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
	            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
	            			String s = nsDef.getURI();
	            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.GETTER_PREFIX.getToken() + propName, true));
	                    }
	                    else
	                    {
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSRoyaleEmitterTokens.GETTER_PREFIX);
	                    	write(propName);
	                    }
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.EQUAL);
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.FUNCTION);
	                    fjs.emitParameters(getterNode.getParametersContainerNode());
	
	                    fjs.emitDefinePropertyFunction(getterNode);
	                                        
	                    write(ASEmitterTokens.SEMICOLON);
	                }
	                if (setterNode != null)
	                {
	                	boolean isBindable = false;                
	                	IAccessorDefinition setterDef = (IAccessorDefinition)setterNode.getDefinition();
	                	IAccessorDefinition getterDef = null;
	                	if (getterNode != null)
	                		getterDef = (IAccessorDefinition)getterNode.getDefinition();
	                	if (setterDef.isBindable() || (getterDef != null && getterDef.isBindable()))
	                	{
	                		if (setterDef.isBindable())
	                		{
	                			IMetaTag[] tags = setterDef.getMetaTagsByName("Bindable");
	                			if (tags.length > 1)
	                        		isBindable = true;
	                			else if (tags.length == 1)
	                			{
	                				if (tags[0].getAllAttributes().length == 0)
	                					isBindable = true;
	                			}
	                		}
	                		else if (getterDef != null && getterDef.isBindable())
	                		{
	                			IMetaTag[] tags = getterDef.getMetaTagsByName("Bindable");
	                			if (tags.length > 1)
	                        		isBindable = true;
	                			else if (tags.length == 1)
	                			{
	                				if (tags[0].getAllAttributes().length == 0)
	                					isBindable = true;
	                			}                			
	                		}
	                	}
	                    writeNewline();
	                    writeNewline();
	                    writeNewline();
	                    write(getEmitter().formatQualifiedName(qname));
	                    write(ASEmitterTokens.MEMBER_ACCESS);
	                    write(JSEmitterTokens.PROTOTYPE);
	                    if (fjs.isCustomNamespace((FunctionNode)setterNode))
	                    {
	            			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
	            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
	            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
	            			String s = nsDef.getURI();
	            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.SETTER_PREFIX.getToken() + propName, true));
	                    }
	                    else
	                    {
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        if (isBindable)
	                        	write(JSRoyaleEmitterTokens.BINDABLE_PREFIX);
	                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
	                    	write(propName);
	                    }
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.EQUAL);
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.FUNCTION);
	                    fjs.emitParameters(setterNode.getParametersContainerNode());
	
	                    fjs.emitDefinePropertyFunction(setterNode);
	                    
	                    write(ASEmitterTokens.SEMICOLON);
	                    
	                    if (isBindable)
	                    {
	                    	writeNewline();
	                    	writeNewline();
	                    	writeNewline();
	                        write(getEmitter().formatQualifiedName(qname));
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSEmitterTokens.PROTOTYPE);
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
	                    	write(propName);
	                        write(ASEmitterTokens.SPACE);
	                        write(ASEmitterTokens.EQUAL);
	                        write(ASEmitterTokens.SPACE);
	                        write(ASEmitterTokens.FUNCTION);
	                        write(ASEmitterTokens.PAREN_OPEN);
	                        write("value");
	                        write(ASEmitterTokens.PAREN_CLOSE);
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
	                        write(JSRoyaleEmitterTokens.GETTER_PREFIX);
	                    	write(propName);
	                        write(ASEmitterTokens.PAREN_OPEN);
	                        write(ASEmitterTokens.PAREN_CLOSE);
	                        writeNewline(ASEmitterTokens.SEMICOLON);
	                        write(ASEmitterTokens.IF);
	                        write(ASEmitterTokens.SPACE);
	                        write(ASEmitterTokens.PAREN_OPEN);
	                        write("oldValue != value");
	                        write(ASEmitterTokens.PAREN_CLOSE);
	                        write(ASEmitterTokens.SPACE);
	                        writeNewline(ASEmitterTokens.BLOCK_OPEN);
	                        write(ASEmitterTokens.THIS);
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSRoyaleEmitterTokens.BINDABLE_PREFIX);
	                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
	                    	write(propName);
	                        write(ASEmitterTokens.PAREN_OPEN);
	                        write("value");
	                        write(ASEmitterTokens.PAREN_CLOSE);
	                        writeNewline(ASEmitterTokens.SEMICOLON);
	                        writeNewline("    this.dispatchEvent("+fjs.formatQualifiedName(BindableEmitter.VALUECHANGE_EVENT_QNAME)+".createUpdateEvent(");
	                        writeNewline("         this, \"" + propName + "\", oldValue, value));");
	                        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
	                        write(ASEmitterTokens.BLOCK_CLOSE);
	                        write(ASEmitterTokens.SEMICOLON);                        
	                        
	                    }
	                }
                }
            }
        }
        if (!getModel().getPropertyMap().isEmpty() && !getModel().isExterns)
        {
            writeNewline();
            writeNewline();
            writeNewline();
            write(JSGoogEmitterTokens.OBJECT);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            String qname = definition.getQualifiedName();
            write(getEmitter().formatQualifiedName(qname));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
            write(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SPACE);
            write("/** @lends {" + getEmitter().formatQualifiedName(qname)
                    + ".prototype} */ ");
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            Set<String> propertyNames = getModel().getPropertyMap().keySet();
            boolean firstTime = true;
            for (String propName : propertyNames)
            {
                if (firstTime)
                    firstTime = false;
                else
                    writeNewline(ASEmitterTokens.COMMA);

                boolean wroteGetter = false;
                PropertyNodes p = getModel().getPropertyMap().get(propName);
                IGetterNode getterNode = p.getter;
                ISetterNode setterNode = p.setter;
            	writeNewline("/**");
                if (emitExports)
                	writeNewline("  * @export");
                if (p.type != null)
                {
                	String typeName = p.type.getBaseName();
                	if (getModel().isInternalClass(typeName))
    					typeName = getModel().getInternalClasses().get(typeName);
					writeNewline("  * @type {" + JSGoogDocEmitter.convertASTypeToJSType(typeName, p.type.getPackageName()) + "} */");
                }
                else
                	writeNewline("  */");
                FunctionNode fnNode = getterNode != null ? (FunctionNode) getterNode : (FunctionNode) setterNode;
                if (fjs.isCustomNamespace(fnNode))
                {
        			INamespaceDecorationNode ns = fnNode.getActualNamespaceNode();
        			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
        			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
        			String s = nsDef.getURI();
        			write(JSRoyaleEmitter.formatNamespacedProperty(s, propName, false));
                }
                else
                	write(propName);
                write(ASEmitterTokens.COLON);
                write(ASEmitterTokens.SPACE);
                write(ASEmitterTokens.BLOCK_OPEN);
                writeNewline();
                if (getterNode != null)
                {
                    write(ASEmitterTokens.GET);
                    write(ASEmitterTokens.COLON);
                    write(ASEmitterTokens.SPACE);
                    write(getEmitter().formatQualifiedName(qname));
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSEmitterTokens.PROTOTYPE);
                    if (fjs.isCustomNamespace((FunctionNode)getterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.GETTER_PREFIX.getToken() + propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSRoyaleEmitterTokens.GETTER_PREFIX);
                    	write(propName);
                    }
                    wroteGetter = true;
                }
                else if (setterNode != null && setterNode.getDefinition().isOverride())
                {
                	// see if there is a getter on a base class.  If so, we have to 
                	// generate a call to the super from this class because 
                	// Object.defineProperty doesn't allow overriding just the setter.
                	// If there is no getter defineProp'd the property will seen as
                	// write-only.
                	IAccessorDefinition other = (IAccessorDefinition)SemanticUtils.resolveCorrespondingAccessor(p.setter.getDefinition(), getProject());
                	if (other != null)
                	{
                        write(ASEmitterTokens.GET);
                        write(ASEmitterTokens.COLON);
                        write(ASEmitterTokens.SPACE);
                        
                        write(getEmitter().formatQualifiedName(other.getParent().getQualifiedName()));
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSEmitterTokens.PROTOTYPE);
                        if (fjs.isCustomNamespace((FunctionNode)setterNode))
                        {
                			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
                			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
                			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                			String s = nsDef.getURI();
                			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.GETTER_PREFIX.getToken() + propName, true));
                        }
                        else
                        {
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSRoyaleEmitterTokens.GETTER_PREFIX);
                        	write(propName);
                        }
                        wroteGetter = true;
                	}
                }
                if (setterNode != null)
                {
                    if (wroteGetter)
                        writeNewline(ASEmitterTokens.COMMA);

                    write(ASEmitterTokens.SET);
                    write(ASEmitterTokens.COLON);
                    write(ASEmitterTokens.SPACE);
                    write(getEmitter().formatQualifiedName(qname));
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSEmitterTokens.PROTOTYPE);
                    if (fjs.isCustomNamespace((FunctionNode)setterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.SETTER_PREFIX.getToken() + propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
                    	write(propName);
                    }
                }
                else if (getterNode != null && getterNode.getDefinition().isOverride())
                {
                	// see if there is a getter on a base class.  If so, we have to 
                	// generate a call to the super from this class because 
                	// Object.defineProperty doesn't allow overriding just the getter.
                	// If there is no setter defineProp'd the property will seen as
                	// read-only.
                	IAccessorDefinition other = (IAccessorDefinition)SemanticUtils.resolveCorrespondingAccessor(p.getter.getDefinition(), getProject());
                	if (other != null)
                	{
                        if (wroteGetter)
                            writeNewline(ASEmitterTokens.COMMA);

                        write(ASEmitterTokens.SET);
                        write(ASEmitterTokens.COLON);
                        write(ASEmitterTokens.SPACE);
                        write(getEmitter().formatQualifiedName(other.getParent().getQualifiedName()));
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSEmitterTokens.PROTOTYPE);
                        if (fjs.isCustomNamespace((FunctionNode)getterNode))
                        {
                			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
                			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
                			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                			String s = nsDef.getURI();
                			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.SETTER_PREFIX.getToken() + propName, true));
                        }
                        else
                        {
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSRoyaleEmitterTokens.SETTER_PREFIX);
                        	write(propName);
                        }
                	}
                }
                write(ASEmitterTokens.BLOCK_CLOSE);
            }
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }
        if (!getModel().getStaticPropertyMap().isEmpty())
        {
            String qname = definition.getQualifiedName();
            Set<String> propertyNames = getModel().getStaticPropertyMap().keySet();
            for (String propName : propertyNames)
            {
                PropertyNodes p = getModel().getStaticPropertyMap().get(propName);
                IGetterNode getterNode = p.getter;
                ISetterNode setterNode = p.setter;
                if (getModel().isExterns)
                {
                	IAccessorNode node = (getterNode != null) ? getterNode : setterNode;
                    writeNewline();
                    writeNewline();
                    writeNewline();
                	writeNewline("/**");
                    if (emitExports)
                    	writeNewline("  * @export");
                    if (p.type != null)
                    	writeNewline("  * @type {" + JSGoogDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "} */");
                    else
                    	writeNewline("  */");
                    write(getEmitter().formatQualifiedName(qname));
                    if (fjs.isCustomNamespace((FunctionNode)node))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                    	write(propName);
                    }
                    write(ASEmitterTokens.SEMICOLON);                	
                }
                else
                {
	                if (getterNode != null)
	                {
	                    writeNewline();
	                    writeNewline();
	                    writeNewline();
	                    write(getEmitter().formatQualifiedName(qname));
	                    if (fjs.isCustomNamespace((FunctionNode)getterNode))
	                    {
	            			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
	            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
	            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
	            			String s = nsDef.getURI();
	            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.GETTER_PREFIX.getToken() + propName, true));
	                    }
	                    else
	                    {
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSRoyaleEmitterTokens.GETTER_PREFIX);
	                    	write(propName);
	                    }
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.EQUAL);
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.FUNCTION);
	                    fjs.emitParameters(getterNode.getParametersContainerNode());
	
	                    fjs.emitDefinePropertyFunction(getterNode);
	                    
	                    write(ASEmitterTokens.SEMICOLON);
	                }
	                if (setterNode != null)
	                {
	                    writeNewline();
	                    writeNewline();
	                    writeNewline();
	                    write(getEmitter().formatQualifiedName(qname));
	                    if (fjs.isCustomNamespace((FunctionNode)setterNode))
	                    {
	            			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
	            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
	            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
	            			String s = nsDef.getURI();
	            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.SETTER_PREFIX.getToken() + propName, true));
	                    }
	                    else
	                    {
	                        write(ASEmitterTokens.MEMBER_ACCESS);
	                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
	                    	write(propName);
	                    }
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.EQUAL);
	                    write(ASEmitterTokens.SPACE);
	                    write(ASEmitterTokens.FUNCTION);
	                    fjs.emitParameters(setterNode.getParametersContainerNode());
	
	                    fjs.emitDefinePropertyFunction(setterNode);
	                    
	                    write(ASEmitterTokens.SEMICOLON);
	                }
                }
            }
        }
        if (!getModel().getStaticPropertyMap().isEmpty() && !getModel().isExterns)
        {
            writeNewline();
            writeNewline();
            writeNewline();
            write(JSGoogEmitterTokens.OBJECT);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            String qname = definition.getQualifiedName();
            write(getEmitter().formatQualifiedName(qname));
            write(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SPACE);
            write("/** @lends {" + getEmitter().formatQualifiedName(qname)
                    + "} */ ");
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            Set<String> propertyNames = getModel().getStaticPropertyMap()
                    .keySet();
            boolean firstTime = true;
            for (String propName : propertyNames)
            {
                if (firstTime)
                    firstTime = false;
                else
                    writeNewline(ASEmitterTokens.COMMA);

                PropertyNodes p = getModel().getStaticPropertyMap().get(
                        propName);
                IGetterNode getterNode = p.getter;
                ISetterNode setterNode = p.setter;
            	writeNewline("/**");
                if (emitExports)
                	writeNewline("  * @export");
                if (p.type != null)
                	writeNewline("  * @type {" + JSGoogDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "} */");
                else
                	writeNewline("  */");
                write(propName);
                write(ASEmitterTokens.COLON);
                write(ASEmitterTokens.SPACE);
                write(ASEmitterTokens.BLOCK_OPEN);
                writeNewline();
                if (getterNode != null)
                {
                    write(ASEmitterTokens.GET);
                    write(ASEmitterTokens.COLON);
                    write(ASEmitterTokens.SPACE);
                    write(getEmitter().formatQualifiedName(qname));
                    if (fjs.isCustomNamespace((FunctionNode)getterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.GETTER_PREFIX.getToken() + propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSRoyaleEmitterTokens.GETTER_PREFIX);
                    	write(propName);
                    }
                }
                if (setterNode != null)
                {
                    if (p.getter != null)
                        writeNewline(ASEmitterTokens.COMMA);

                    write(ASEmitterTokens.SET);
                    write(ASEmitterTokens.COLON);
                    write(ASEmitterTokens.SPACE);
                    write(getEmitter().formatQualifiedName(qname));
                    if (fjs.isCustomNamespace((FunctionNode)setterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write(JSRoyaleEmitter.formatNamespacedProperty(s, JSRoyaleEmitterTokens.SETTER_PREFIX.getToken() + propName, true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSRoyaleEmitterTokens.SETTER_PREFIX);
                    	write(propName);
                    }
                }
                write(ASEmitterTokens.BLOCK_CLOSE);
            }
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }
    }

	public void emitGet(IGetterNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        IDefinition def = node.getDefinition();
        ModifiersSet modifierSet = def.getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
    		if (!isStatic && def != null && def.isPrivate() && getProject().getAllowPrivateNameConflicts())
    			name = fjs.formatPrivateName(def.getParent().getQualifiedName(), name);
        PropertyNodes p = map.get(name);
        if (p == null)
        {
            p = new PropertyNodes();
            map.put(name, p);
        }
        p.getter = node;
        ICompilerProject project = (ICompilerProject)getWalker().getProject();
        if (project != null)
        	p.type = node.getDefinition().resolveReturnType(project);
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());
    }

    public void emitSet(ISetterNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        JSRoyaleDocEmitter doc = (JSRoyaleDocEmitter) fjs.getDocEmitter();

        IFunctionDefinition def = node.getDefinition();
        ModifiersSet modifierSet = def.getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
    		if (!isStatic && def != null && def.isPrivate() && getProject().getAllowPrivateNameConflicts())
    			name = fjs.formatPrivateName(def.getParent().getQualifiedName(), name);
        PropertyNodes p = map.get(name);
        if (p == null)
        {
            p = new PropertyNodes();
            map.put(name, p);
        }
        p.setter = node;
        ICompilerProject project = (ICompilerProject)getWalker().getProject();
        if (project != null)
        {
        	IParameterDefinition[] params = def.getParameters();
        	p.type = params[0].resolveType(project);
        }
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());

        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
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
            doc.emitMethodDoc(fn, getProject());
            write(fjs.formatQualifiedName(type.getQualifiedName()));
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
            fjs.emitParameters(node.getParametersContainerNode());
            //writeNewline();
            fjs.emitMethodScope(node.getScopedNode());
        }
    }
}
