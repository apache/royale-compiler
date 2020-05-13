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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.node.NodeEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.common.JSModuleRequireDescription;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition.INamepaceDeclarationDirective;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.utils.NativeUtils;

public class PackageHeaderEmitter extends JSSubEmitter implements
        ISubEmitter<IPackageDefinition>
{

    public PackageHeaderEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IPackageDefinition definition)
    {
        RoyaleJSProject project = (RoyaleJSProject) getProject();
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = EmitterUtils.findType(containedScope
                .getAllLocalDefinitions());
        String qname = null;
        boolean isExterns = false;
        if (type != null)
        {
            qname = type.getQualifiedName();
            ITypeNode typeNode = type.getNode();
            if (typeNode instanceof IClassNode)
            {
                IClassNode classNode = (IClassNode) typeNode;
                ASDocComment asDoc = (ASDocComment) classNode.getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    isExterns = asDocString.contains(JSRoyaleEmitterTokens.EXTERNS.getToken());
                    getEmitter().getModel().isExterns = isExterns;
                }
            }
            else if (typeNode instanceof IInterfaceNode)
            {
            	IInterfaceNode interfaceNode = (IInterfaceNode) typeNode;
                ASDocComment asDoc = (ASDocComment) interfaceNode.getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    isExterns = asDocString.contains(JSRoyaleEmitterTokens.EXTERNS.getToken());
                    getEmitter().getModel().isExterns = isExterns;
                }
            }
        }
        if (qname == null)
        {
            IFunctionDefinition fn = EmitterUtils.findFunction(containedScope
                    .getAllLocalDefinitions());
            if(fn != null)
            {
                qname = fn.getQualifiedName();
                IFunctionNode functionNode = (IFunctionNode) fn.getNode();
                ASDocComment asDoc = (ASDocComment) functionNode.getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    isExterns = asDocString.contains(JSRoyaleEmitterTokens.EXTERNS.getToken());
                    getEmitter().getModel().isExterns = isExterns;
                }
            }
        }
        if (qname == null)
        {
            IVariableDefinition variable = EmitterUtils.findVariable(containedScope
                    .getAllLocalDefinitions());
            if(variable != null)
            {
                qname = variable.getQualifiedName();
                IVariableNode variableNode = (IVariableNode) variable.getNode();
                ASDocComment asDoc = (ASDocComment) variableNode.getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    isExterns = asDocString.contains(JSRoyaleEmitterTokens.EXTERNS.getToken());
                    getEmitter().getModel().isExterns = isExterns;
                }
            }
        }
        if (qname == null)
        {
        	INamepaceDeclarationDirective ns = EmitterUtils.findNamespace(containedScope
                    .getAllLocalDefinitions());
            if(ns != null)
            {
                qname = ns.getQualifiedName();
            }
        }
        if (qname == null)
        {
            return;
        }

        List<File> sourcePaths = project.getSourcePath();
        String sourceName = definition.getSourcePath();
        for (File sourcePath : sourcePaths)
        {
            if (sourceName.startsWith(sourcePath.getAbsolutePath()))
            {
                sourceName = sourceName.substring(sourcePath.getAbsolutePath().length() + 1);
            }
        }

        writeNewline("/**");
        writeNewline(" * Generated by Apache Royale Compiler from " + sourceName.replace('\\', '/'));
        writeNewline(" * " + qname);
        writeNewline(" *");
        writeNewline(" * @fileoverview");
        if (isExterns)
        	writeNewline(" * @externs");
        writeNewline(" *");
        // need to suppress access controls so access to protected/private from defineProperties
        // doesn't generate warnings.
        writeNewline(" * @suppress {checkTypes|accessControls}");
        writeNewline(" */");
        writeNewline();

        if (!isExterns)
        {
	        /* goog.provide('x');\n\n */
	        write(JSGoogEmitterTokens.GOOG_PROVIDE);
	        write(ASEmitterTokens.PAREN_OPEN);
	        write(ASEmitterTokens.SINGLE_QUOTE);
	        write(((JSRoyaleEmitter)getEmitter()).formatQualifiedName(qname, true));
	        write(ASEmitterTokens.SINGLE_QUOTE);
	        write(ASEmitterTokens.PAREN_CLOSE);
	        writeNewline(ASEmitterTokens.SEMICOLON);
	        
	        HashMap<String, String> internalClasses = getEmitter().getModel().getInternalClasses();
	        if (internalClasses.size() > 0)
	        {
	        	ArrayList<String> classesInOrder = new ArrayList<String>();
	        	for (String internalClass : internalClasses.keySet())
	        	{
	        		classesInOrder.add(internalClass);
	        	}
	        	Collections.sort(classesInOrder);
	        	for (String internalClass : classesInOrder)
	        	{
	        	       /* goog.provide('x');\n\n */
	                write(JSGoogEmitterTokens.GOOG_PROVIDE);
	                write(ASEmitterTokens.PAREN_OPEN);
	                write(ASEmitterTokens.SINGLE_QUOTE);
	                write(((JSRoyaleEmitter)getEmitter()).formatQualifiedName(internalClass, true));
	                write(ASEmitterTokens.SINGLE_QUOTE);
	                write(ASEmitterTokens.PAREN_CLOSE);
	                writeNewline(ASEmitterTokens.SEMICOLON);
	        	}
	        }
        }
        else
        {
        	String pkgName = definition.getQualifiedName();
        	if (pkgName.length() > 0)
        	{
        		String[] parts = pkgName.split("\\.");
        		String current = "";
        		boolean firstOne = true;
        		for (String part : parts)
        		{
        			current += part;
    				writeNewline("/** @const */");
        			if (firstOne)
        			{
        				write("var ");
        				firstOne = false;
        			}
        			write(current);
        			write(" = {}");
	                writeNewline(ASEmitterTokens.SEMICOLON);
	                current += ".";
        		}
        	}
        }
        writeNewline();

    }

    public void emitContents(IPackageDefinition definition)
    {
        if (getEmitter().getModel().isExterns) return;

        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        PackageScope containedScope = (PackageScope) definition
                .getContainedScope();

        ArrayList<String> writtenRequires = new ArrayList<String>();

        Collection<IDefinition> localDefinitions = containedScope.getAllLocalDefinitions();
        ITypeDefinition type = EmitterUtils.findType(localDefinitions);
        IDefinition otherMainDefinition = null;
        if (type == null)
        {
            if (localDefinitions.isEmpty())
                return;
            // function or variable definition
            otherMainDefinition = localDefinitions.iterator().next();
        }
        else
        {
            ITypeNode typeNode = type.getNode();
            if (typeNode instanceof IClassNode)
            {
                IClassNode classNode = (IClassNode) typeNode;
                ASDocComment asDoc = (ASDocComment) classNode.getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    String ignoreToken = JSRoyaleEmitterTokens.IGNORE_IMPORT
                            .getToken();
                    int ignoreIndex = asDocString.indexOf(ignoreToken);
                    while (ignoreIndex != -1)
                    {
                        String ignorable = asDocString.substring(ignoreIndex
                                + ignoreToken.length());
                        int endIndex = ignorable.indexOf("\n");
                        ignorable = ignorable.substring(0, endIndex);
                        ignorable = ignorable.trim();
                        // pretend we've already written the goog.requires for this
                        writtenRequires.add(ignorable);
                        ignoreIndex = asDocString.indexOf(ignoreToken,
                                ignoreIndex + ignoreToken.length());
                    }
                }
            }
        }

        RoyaleJSProject royaleProject = (RoyaleJSProject) getProject();
        ASProjectScope projectScope = (ASProjectScope) royaleProject.getScope();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(type != null ? type : otherMainDefinition);
        ArrayList<String> requiresList = royaleProject.getRequires(cu);
        ArrayList<String> interfacesList = royaleProject.getInterfaces(cu);
        ArrayList<JSModuleRequireDescription> externalRequiresList = royaleProject.getExternalRequires(cu);

        String cname = (type != null) ? type.getQualifiedName() : otherMainDefinition.getQualifiedName();
        writtenRequires.add(cname); // make sure we don't add ourselves


        if (type instanceof IClassDefinition) {
            ((JSRoyaleEmitter) getEmitter()).processBindableSupport((IClassDefinition) type, requiresList);
        }

        boolean emitsRequires = emitRequires(requiresList, writtenRequires, cname, royaleProject);
        boolean emitsInterfaces = emitInterfaces(interfacesList, writtenRequires);

        // erikdebruin: Add missing language feature support, with e.g. 'is' and
        //              'as' operators. We don't need to worry about requiring
        //              this in every project: ADVANCED_OPTIMISATIONS will NOT
        //              include any of the code if it is not used in the project.
        boolean makingSWC = royaleProject.getSWFTarget() != null &&
                royaleProject.getSWFTarget().getTargetType() == TargetType.SWC;
        boolean isMainCU = royaleProject.mainCU != null
                && cu.getName().equals(royaleProject.mainCU.getName());
        if (isMainCU || makingSWC)
        {
            ICompilerProject project = this.getProject();
            if (project instanceof RoyaleJSProject)
            {
                if (((RoyaleJSProject)project).needLanguage)
                {
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);
                }
            }
        }

        boolean emitsExternalRequires = emitExternalRequires(externalRequiresList, writtenRequires);

        if (emitsRequires || emitsInterfaces || emitsExternalRequires || isMainCU)
        {
            writeNewline();
        }

        writeNewline();
        writeNewline();
    }

    private boolean emitRequires(List<String> requiresList, List<String> writtenRequires, String cname, RoyaleJSProject project)
    {
        boolean emitsRequires = false;
        if (requiresList != null)
        {
            Collections.sort(requiresList);
            for (String imp : requiresList)
            {
                if (imp.contains(JSGoogEmitterTokens.AS3.getToken()))
                    continue;

                if (imp.equals(JSGoogEmitterTokens.GOOG_BIND.getToken()))
                    continue;

                if (imp.equals(cname))
                    continue;
                
                if (NativeUtils.isNative(imp))
                {
                    if (!(imp.equals("QName") || imp.equals("Namespace") || imp.equals("XML") || imp.equals("XMLList") || imp.equals("isXMLName")))
                        continue;
                }

                if (NativeUtils.isJSNative(imp))
                {
                    continue;
                }

                if(!project.isGoogProvided(imp))
                {
                    continue;
                }

                if (writtenRequires.indexOf(imp) == -1)
                {
                    /* goog.require('x');\n */
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(((JSRoyaleEmitter)getEmitter()).formatQualifiedName(imp, true));
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);

                    writtenRequires.add(imp);

                    emitsRequires = true;
                }
            }
        }
        return emitsRequires;
    }

    private boolean emitInterfaces(List<String> interfacesList, List<String> writtenRequires)
    {
        boolean emitsInterfaces = false;
        if (interfacesList != null)
        {
            Collections.sort(interfacesList);
            for (String imp : interfacesList)
            {
                if (writtenRequires.indexOf(imp) == -1)
                {
                    write(JSGoogEmitterTokens.GOOG_REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(((JSRoyaleEmitter)getEmitter()).formatQualifiedName(imp, true));
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);

                    emitsInterfaces = true;
                }
            }
        }
        return emitsInterfaces;
    }

    private boolean emitExternalRequires(List<JSModuleRequireDescription> externalRequiresList, List<String> writtenRequires)
    {
        boolean emitsExternalRequires = false;
        if (externalRequiresList != null)
        {
            Collections.sort(externalRequiresList);
            for (JSModuleRequireDescription m : externalRequiresList)
            {
                // use the qname, if the definition is not in a package
                String variableName = m.qname;
                int firstDot = variableName.indexOf('.');
                if(firstDot != -1)
                {
                    // otherwise, use the first part of the package name
                    variableName = variableName.substring(0, firstDot);
                }
                if (writtenRequires.indexOf(m.moduleName) == -1)
                {
                    /* var xyz = require('xyz');\n */
                    /* var someModule = require('some-module');\n */
                    write(ASEmitterTokens.VAR);
                    write(ASEmitterTokens.SPACE);
                    write(variableName);
                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.SPACE);
                    write(NodeEmitterTokens.REQUIRE);
                    write(ASEmitterTokens.PAREN_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(m.moduleName);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    writeNewline(ASEmitterTokens.SEMICOLON);

                    writtenRequires.add(m.moduleName);

                    emitsExternalRequires = true;
                }
            }
        }
        return emitsExternalRequires;
    }

}
