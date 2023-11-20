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
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.PropertyNodes;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.GetterNode;
import org.apache.royale.compiler.internal.tree.as.SetterNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

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
        RoyaleJSProject project = (RoyaleJSProject)getWalker().getProject();
        boolean emitExports = true;
        if (project != null && project.config != null)
            emitExports = project.config.getExportPublicSymbols();

        if (!getModel().getPropertyMap().isEmpty())
        {
            writeProperties(definition, emitExports);
        }
        if (!getModel().getPropertyMap().isEmpty() && !getModel().isExterns)
        {
            writeDefineProperties(definition);
        }
        if (!getModel().getStaticPropertyMap().isEmpty())
        {
            writeStaticProperties(definition, emitExports);
        }
        if (!getModel().getStaticPropertyMap().isEmpty() && !getModel().isExterns)
        {
            writeStaticDefinePropreties(definition);
        }
    }

	public void emitGet(IGetterNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        boolean suppress = getModel().suppressExports || ASNodeUtils.hasExportSuppressed(node);
;
        if (suppress) getModel().suppressedExportNodes.add(node);
				
        IDefinition def = node.getDefinition();
        ModifiersSet modifierSet = def.getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
		if (!isStatic && def != null && def.isPrivate() && getProject().getAllowPrivateNameConflicts())
			name = fjs.formatPrivateName(def.getParent().getQualifiedName(), name);

		String uri = null;
		String key = name;
		if (!def.getNamespaceReference().isLanguageNamespace()) {
			//we need to include it in the name mappings
			uri = ((INamespaceDefinition) ((GetterNode) node).getActualNamespaceNode().resolve(getProject())).getURI();
			//make sure the key includes the uri to avoid clashing with other equivalent base names
			key =  uri + "::" + name;
		}
        boolean emitExports = true;
        boolean exportProtected = false;
        boolean exportInternal = false;
        boolean preventRenamePublicSymbols = true;
        boolean preventRenameProtectedSymbols = true;
        boolean preventRenameInternalSymbols = true;
        RoyaleJSProject project = (RoyaleJSProject) getWalker().getProject();
        if (project != null && project.config != null)
        {
            emitExports = project.config.getExportPublicSymbols();
            exportProtected = project.config.getExportProtectedSymbols();
            exportInternal = project.config.getExportInternalSymbols();
            preventRenamePublicSymbols = project.config.getPreventRenamePublicSymbols();
            preventRenameProtectedSymbols = project.config.getPreventRenameProtectedSymbols();
            preventRenameInternalSymbols = project.config.getPreventRenameInternalSymbols();
        }
		
		PropertyNodes p = map.get(key);
        if (p == null)
        {
            p = new PropertyNodes();
			//track name and uri separately:
			p.name = name;
			p.originalName = node.getName();
			p.uri = uri;
            map.put(key, p);
        }
        if(uri != null || def.isPublic())
        {
            p.resolvedExport = p.resolvedExport || emitExports;
            p.preventRename = p.preventRename || preventRenamePublicSymbols;
        }
        else if(def.isInternal())
        {
            p.resolvedExport = p.resolvedExport || exportInternal;
            p.preventRename = p.preventRename || preventRenameInternalSymbols;
        }
        else if(def.isProtected())
        {
            p.resolvedExport = p.resolvedExport || exportProtected;
            p.preventRename = p.preventRename || preventRenameProtectedSymbols;
        }
        p.getter = node;
		if (!p.suppressExport) p.suppressExport = suppress;
        if (p.type == null && project != null)
        	p.type = node.getDefinition().resolveReturnType(project);
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());
    }

    public void emitSet(ISetterNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        JSRoyaleDocEmitter doc = (JSRoyaleDocEmitter) fjs.getDocEmitter();
		boolean suppress = getModel().suppressExports || ASNodeUtils.hasExportSuppressed(node);

		if (suppress) getModel().suppressedExportNodes.add(node);

        IFunctionDefinition def = node.getDefinition();
        ModifiersSet modifierSet = def.getModifiers();
        boolean isStatic = (modifierSet != null && modifierSet
                .hasModifier(ASModifier.STATIC));
        HashMap<String, PropertyNodes> map = isStatic ? getModel()
                .getStaticPropertyMap() : getModel().getPropertyMap();
        String name = node.getName();
    		if (!isStatic && def != null && def.isPrivate() && getProject().getAllowPrivateNameConflicts())
    			name = fjs.formatPrivateName(def.getParent().getQualifiedName(), name);
    	String uri = null;
    	String key = name;
		if (!def.getNamespaceReference().isLanguageNamespace()) {
			//we need to include it in the name mappings
			uri = ((INamespaceDefinition) ((SetterNode) node).getActualNamespaceNode().resolve(getProject())).getURI();
			//make sure the key includes the uri to avoid clashing with other equivalent base names
			key =  uri + "::" + name;
		}
        boolean emitExports = true;
        boolean exportProtected = false;
        boolean exportInternal = false;
        boolean preventRenamePublicSymbols = true;
        boolean preventRenameProtectedSymbols = true;
        boolean preventRenameInternalSymbols = true;
        RoyaleJSProject project = (RoyaleJSProject) getWalker().getProject();
        if (project != null && project.config != null)
        {
            emitExports = project.config.getExportPublicSymbols();
            exportProtected = project.config.getExportProtectedSymbols();
            exportInternal = project.config.getExportInternalSymbols();
            preventRenamePublicSymbols = project.config.getPreventRenamePublicSymbols();
            preventRenameProtectedSymbols = project.config.getPreventRenameProtectedSymbols();
            preventRenameInternalSymbols = project.config.getPreventRenameInternalSymbols();
        }
		
        PropertyNodes p = map.get(key);
        if (p == null)
        {
            p = new PropertyNodes();
            //track name and uri separately:
            p.name = name;
            p.originalName = node.getName();
            p.uri = uri;
            map.put(key, p);
        }
        if(uri != null || def.isPublic())
        {
            p.resolvedExport = p.resolvedExport || emitExports;
            p.preventRename = p.preventRename || preventRenamePublicSymbols;
        }
        else if(def.isInternal())
        {
            p.resolvedExport = p.resolvedExport || exportInternal;
            p.preventRename = p.preventRename || preventRenameInternalSymbols;
        }
        else if(def.isProtected())
        {
            p.resolvedExport = p.resolvedExport || exportProtected;
            p.preventRename = p.preventRename || preventRenameProtectedSymbols;
        }
        p.setter = node;
        if (!p.suppressExport) p.suppressExport = suppress;
        if (p.type == null && project != null)
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
			writeNewline();
        }
    }

    private void writeProperties(IClassDefinition definition, boolean emitExports)
    {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        Set<String> propertyNames = getModel().getPropertyMap().keySet();
        for (String propName : propertyNames)
        {
            PropertyNodes p = getModel().getPropertyMap().get(propName);
            IGetterNode getterNode = p.getter;
            ISetterNode setterNode = p.setter;
            String baseName = p.name;
            if (getModel().isExterns)
            {
                IAccessorNode node = (getterNode != null) ? getterNode : setterNode;
                writeNewline();
                writeNewline();
                writeNewline();
                writeNewline("/**");
                if (emitExports)
                    writeNewline(" * @export");
                if (p.type != null)
                    writeNewline(" * @type {"+ JSRoyaleDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "}");
                writeNewline(" */");
                write(getEmitter().formatQualifiedName(qname));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(baseName);
                }
                write(ASEmitterTokens.SEMICOLON);
            }
            else
            {
                IAccessorNode accessorNode = (getterNode != null) ? getterNode : setterNode;
                if(!accessorNode.getDefinition().isOverride())
                {
                    // start by writing out the instance accessors as regular variables
                    // because Closure Compiler doesn't properly analyze calls to
                    // defineProperties() alone.
                    // since there's no analysis, Closure assumes that getters/setters
                    // have no side effects, which results in important get/set calls
                    // being removed as dead code.
                    // defining the accessors as variables first convinces Closure to
                    // handle them more intelligently while not preventing them from
                    // being real accessors.
                    // Source: https://developers.google.com/closure/compiler/docs/limitations
                    writeNewline();
                    writeNewline();
                    writeNewline();
                    writeNewline("/**");
                    if (p.preventRename)
                        writeNewline(" * @nocollapse");
                    if (p.resolvedExport && !p.suppressExport)
                        writeNewline(" * @export");
                    if (p.type != null)
                        writeNewline(" * @type {" + JSRoyaleDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "}"); 
                    writeNewline(" */");
                    write(getEmitter().formatQualifiedName(qname));
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSEmitterTokens.PROTOTYPE);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    if (p.uri != null)
                    {
                        INamespaceDecorationNode ns = ((FunctionNode) accessorNode).getActualNamespaceNode();
                        INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                        fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                        write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, false));
                    }
                    else
                        write(baseName);
                    write(ASEmitterTokens.SEMICOLON);
                }

                if (getterNode != null)
                {
                    writeGetterNode(p, definition);
                }
                if (setterNode != null)
                {
                    writeSetterNode(p, definition);
                }
            }
        }
    }
    
    private void writeStaticProperties(IClassDefinition definition, boolean emitExports)
    {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        Set<String> propertyNames = getModel().getStaticPropertyMap().keySet();
        for (String propName : propertyNames)
        {
            PropertyNodes p = getModel().getStaticPropertyMap().get(propName);
            IGetterNode getterNode = p.getter;
            ISetterNode setterNode = p.setter;
            String baseName = p.name;
            if (getModel().isExterns)
            {
                IAccessorNode node = (getterNode != null) ? getterNode : setterNode;
                writeNewline();
                writeNewline();
                writeNewline();
                writeNewline("/**");
                if (emitExports)
                    writeNewline(" * @export");
                if (p.type != null)
                    writeNewline(" * @type {" + JSRoyaleDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "}");
                writeNewline(" */");
                write(getEmitter().formatQualifiedName(qname));
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(baseName);
                }
                write(ASEmitterTokens.SEMICOLON);                	
            }
            else
            {
                // start by writing out the static accessors as regular variables
                // because Closure Compiler doesn't properly analyze calls to
                // defineProperties() alone.
                // since there's no analysis, Closure assumes that getters/setters
                // have no side effects, which results in important get/set calls
                // being removed as dead code.
                // defining the accessors as variables first convinces Closure to
                // handle them more intelligently while not preventing them from
                // being real accessors.
                // Source: https://developers.google.com/closure/compiler/docs/limitations
                writeNewline();
                writeNewline();
                writeNewline();
                writeNewline("/**");
                // like instance accessors, we should have if (p.preventRename)
                // here, but while Closure compiler seems to properly handle
                // renaming of instance accessors, it fails when we try the same
                // trick with static accessors, unless there's a nocollapse.
                // when we allow renaming, we don't want to break anything that
                // isn't dynamic access, so we always need nocollapse here.
                writeNewline(" * @nocollapse");
                if (p.resolvedExport && !p.suppressExport)
                    writeNewline(" * @export");
                if (p.type != null)
                    writeNewline(" * @type {" + JSRoyaleDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "}"); 
                writeNewline(" */");
                write(getEmitter().formatQualifiedName(qname));
                write(ASEmitterTokens.MEMBER_ACCESS);
                if (p.uri != null)
                {
                    IAccessorNode node = (getterNode != null) ? getterNode : setterNode;
                    INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, false));
                }
                else
                    write(baseName);
                write(ASEmitterTokens.SEMICOLON);

                if (getterNode != null)
                {
                    writeStaticGetterNode(p, definition);
                }
                if (setterNode != null)
                {
                    writeStaticSetterNode(p, definition);
                }
            }
        }
    }

    private void writeDefineProperties(IClassDefinition definition)
    {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
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
            String baseName = p.name;
            IGetterNode getterNode = p.getter;
            ISetterNode setterNode = p.setter;
            writeNewline("/**");
            if (p.type != null)
            {
                String typeName = p.type.getBaseName();
                if (getModel().isInternalClass(typeName))
                    typeName = getModel().getInternalClasses().get(typeName);
                writeNewline(" * @type {" + JSRoyaleDocEmitter.convertASTypeToJSType(typeName, p.type.getPackageName()) + "}");
            }
            writeNewline(" */");
            FunctionNode fnNode = getterNode != null ? (FunctionNode) getterNode : (FunctionNode) setterNode;
            if (p.uri != null)
            {
                INamespaceDecorationNode ns = fnNode.getActualNamespaceNode();
                INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                //String s = nsDef.getURI();
                write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, false));
            }
            else
                write(baseName);
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
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                    //String s = nsDef.getURI();
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatGetter(baseName), true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(fjs.formatGetter(baseName));
                }
                wroteGetter = true;
            }
            else if (setterNode != null /* && setterNode.getDefinition().isOverride()*/)
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
                    if (p.uri != null)
                    {
                        INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
                        INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                        fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                        //String s = nsDef.getURI();
                        write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatGetter(baseName), true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(fjs.formatGetter(baseName));
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
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                    //String s = nsDef.getURI();
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatSetter(baseName), true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(fjs.formatSetter(baseName));
                }
            }
            else if (getterNode != null/* && getterNode.getDefinition().isOverride()*/)
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
                    if (p.uri != null)
                    {
                        INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
                        INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                        fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                        //String s = nsDef.getURI();
                        write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatSetter(baseName), true));
                    }
                    else
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(fjs.formatSetter(baseName));
                    }
                }
            }
            write(ASEmitterTokens.BLOCK_CLOSE);
        }
        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }

    private void writeStaticDefinePropreties(IClassDefinition definition) {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

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
            String baseName = p.name;
            writeNewline("/**");
            if (p.type != null)
                writeNewline(" * @type {" + JSRoyaleDocEmitter.convertASTypeToJSType(p.type.getBaseName(), p.type.getPackageName()) + "}");
            writeNewline(" */");
            FunctionNode fnNode = getterNode != null ? (FunctionNode) getterNode : (FunctionNode) setterNode;
            if (p.uri != null)
            {
                INamespaceDecorationNode ns = fnNode.getActualNamespaceNode();
                INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                //String s = nsDef.getURI();
                write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, baseName, false));
            }
            else
                write(baseName);
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
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                    //String s = nsDef.getURI();
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatGetter(baseName), true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(fjs.formatGetter(baseName));
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
                if (p.uri != null)
                {
                    INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
                    INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
                    fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
                    //String s = nsDef.getURI();
                    write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatSetter(baseName), true));
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(fjs.formatSetter(baseName));
                }
            }
            write(ASEmitterTokens.BLOCK_CLOSE);
        }
        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }

    private void writeGetterNode(PropertyNodes p, IClassDefinition definition) {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        String baseName = p.name;
        IGetterNode getterNode = p.getter;
        writeNewline();
        writeNewline();
        writeNewline();
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        if (p.uri != null)
        {
            INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
            write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatGetter(baseName), true));
        }
        else
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(fjs.formatGetter(baseName));
        }
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.FUNCTION);
        fjs.emitParameters(getterNode.getParametersContainerNode());

        fjs.emitDefinePropertyFunction(getterNode);
                            
        write(ASEmitterTokens.SEMICOLON);
    }

    private void writeSetterNode(PropertyNodes p, IClassDefinition definition)
    {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        String baseName = p.name;
        IGetterNode getterNode = p.getter;
        ISetterNode setterNode = p.setter;
        boolean isClassBindable = BindableHelper.isClassCodeGenBindable(definition);

        boolean isBindable = false;                
        IAccessorDefinition setterDef = (IAccessorDefinition)setterNode.getDefinition();
        IAccessorDefinition getterDef = null;
        if (getterNode != null)
            getterDef = (IAccessorDefinition)getterNode.getDefinition();
        if ((getterDef != null && (setterDef.isBindable() || getterDef.isBindable())))
        {
            boolean foundExplicitBindableTag = false;
            if (setterDef.isBindable())
            {

                isBindable = BindableHelper.isCodeGenBindableMember(setterDef, isClassBindable);
                foundExplicitBindableTag = BindableHelper.hasExplicitBindable(setterDef);
            }
            if (getterDef.isBindable())
            {

                isBindable = isBindable || BindableHelper.isCodeGenBindableMember(getterDef, isClassBindable);
                foundExplicitBindableTag = foundExplicitBindableTag || BindableHelper.hasExplicitBindable(getterDef);

            }

            if (isClassBindable) {
                //if we 'foundExplicitBindableTag' such as [Bindable(event='someEvent')], then nothing else matters, even another [Bindable] tag is ignored (Flex)
                isBindable = !foundExplicitBindableTag;
            }
        }
        writeNewline();
        writeNewline();
        writeNewline();
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        if (p.uri != null)
        {
            INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
            write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatSetter(baseName), true));
        }
        else
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            if (isBindable) {
                write(JSRoyaleEmitterTokens.BINDABLE_PREFIX);
                write(fjs.formatSetter(fjs.formatPrivateName(definition.getQualifiedName(), baseName, true)));
            } else {
                write(fjs.formatSetter(baseName));
            }
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
            write(fjs.formatSetter(baseName));
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
            write(fjs.formatGetter(baseName));
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
            write(fjs.formatSetter(fjs.formatPrivateName(definition.getQualifiedName(), baseName, true)));
            write(ASEmitterTokens.PAREN_OPEN);
            write("value");
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
            writeNewline("    this.dispatchEvent("+fjs.formatQualifiedName(BindableEmitter.VALUECHANGE_EVENT_QNAME)+".createUpdateEvent(");
            writeNewline("         this, \"" + p.originalName + "\", oldValue, value));");
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SEMICOLON);                           
        }
    }

    private void writeStaticGetterNode(PropertyNodes p, IClassDefinition definition) {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        String baseName = p.name;
        IGetterNode getterNode = p.getter;

        writeNewline();
        writeNewline();
        writeNewline();
        write(getEmitter().formatQualifiedName(qname));
        if (p.uri != null)
        {
            INamespaceDecorationNode ns = ((FunctionNode)getterNode).getActualNamespaceNode();
            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            //String s = nsDef.getURI();
            write(JSRoyaleEmitter.formatNamespacedProperty(p.uri , fjs.formatGetter(baseName), true));
        }
        else
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(fjs.formatGetter(baseName));
        }
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.FUNCTION);
        fjs.emitParameters(getterNode.getParametersContainerNode());

        fjs.emitDefinePropertyFunction(getterNode);
        
        write(ASEmitterTokens.SEMICOLON);
    }

    private void writeStaticSetterNode(PropertyNodes p, IClassDefinition definition) {
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = definition.getQualifiedName();
        String baseName = p.name;
        ISetterNode setterNode = p.setter;

        writeNewline();
        writeNewline();
        writeNewline();
        write(getEmitter().formatQualifiedName(qname));
        if (p.uri != null)
        {
            INamespaceDecorationNode ns = ((FunctionNode)setterNode).getActualNamespaceNode();
            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(getWalker().getProject());
            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
            //String s = nsDef.getURI();
            write(JSRoyaleEmitter.formatNamespacedProperty(p.uri, fjs.formatSetter(baseName), true));
        }
        else
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(fjs.formatSetter(baseName));
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
