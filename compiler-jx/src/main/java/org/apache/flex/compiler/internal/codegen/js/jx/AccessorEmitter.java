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

package org.apache.flex.compiler.internal.codegen.js.jx;

import java.util.HashMap;
import java.util.Set;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSDocEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel.PropertyNodes;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.SetterNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.INamespaceDecorationNode;
import org.apache.flex.compiler.tree.as.ISetterNode;

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
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        if (!getModel().getPropertyMap().isEmpty())
        {
            String qname = definition.getQualifiedName();
            Set<String> propertyNames = getModel().getPropertyMap().keySet();
            for (String propName : propertyNames)
            {
                PropertyNodes p = getModel().getPropertyMap().get(propName);
                IGetterNode getterNode = p.getter;
                ISetterNode setterNode = p.setter;
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.GETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.GETTER_PREFIX);
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
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSEmitterTokens.PROTOTYPE);
                    if (fjs.isCustomNamespace((FunctionNode)setterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.SETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.SETTER_PREFIX);
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
        if (!getModel().getPropertyMap().isEmpty())
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
                writeNewline("/** @export */");
                FunctionNode fnNode = getterNode != null ? (FunctionNode) getterNode : (FunctionNode) setterNode;
                if (fjs.isCustomNamespace(fnNode))
                {
        			INamespaceDecorationNode ns = fnNode.getActualNamespaceNode();
                    ICompilerProject project = getWalker().getProject();
        			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
        			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
        			String s = nsDef.getURI();
        			write("\"" + s + "::" + propName + "\"");
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.GETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.GETTER_PREFIX);
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
                            ICompilerProject project = getWalker().getProject();
                			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
                			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                			String s = nsDef.getURI();
                			write("[\"" + s + "::" + JSFlexJSEmitterTokens.GETTER_PREFIX.getToken() + propName + "\"]");
                        }
                        else
                        {
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSFlexJSEmitterTokens.GETTER_PREFIX);
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.SETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.SETTER_PREFIX);
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
                            ICompilerProject project = getWalker().getProject();
                			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
                			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                			String s = nsDef.getURI();
                			write("[\"" + s + "::" + JSFlexJSEmitterTokens.SETTER_PREFIX.getToken() + propName + "\"]");
                        }
                        else
                        {
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSFlexJSEmitterTokens.SETTER_PREFIX);
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
                if (getterNode != null)
                {
                    writeNewline();
                    writeNewline();
                    writeNewline();
                    write(getEmitter().formatQualifiedName(qname));
                    if (fjs.isCustomNamespace((FunctionNode)getterNode))
                    {
            			INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.GETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.GETTER_PREFIX);
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.SETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.SETTER_PREFIX);
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
        if (!getModel().getStaticPropertyMap().isEmpty())
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
                writeNewline("/** @export */");
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.GETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.GETTER_PREFIX);
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
                        ICompilerProject project = getWalker().getProject();
            			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
            			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            			String s = nsDef.getURI();
            			write("[\"" + s + "::" + JSFlexJSEmitterTokens.SETTER_PREFIX.getToken() + propName + "\"]");
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.SETTER_PREFIX);
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
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        ModifiersSet modifierSet = node.getDefinition().getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
        PropertyNodes p = map.get(name);
        if (p == null)
        {
            p = new PropertyNodes();
            map.put(name, p);
        }
        p.getter = node;
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());
    }

    public void emitSet(ISetterNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();
        JSFlexJSDocEmitter doc = (JSFlexJSDocEmitter) fjs.getDocEmitter();

        ModifiersSet modifierSet = node.getDefinition().getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
        PropertyNodes p = map.get(name);
        if (p == null)
        {
            p = new PropertyNodes();
            map.put(name, p);
        }
        p.setter = node;
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
