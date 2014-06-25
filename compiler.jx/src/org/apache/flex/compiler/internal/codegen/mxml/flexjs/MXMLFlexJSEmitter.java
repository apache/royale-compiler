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

package org.apache.flex.compiler.internal.codegen.mxml.flexjs;


import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.mxml.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.databinding.BindingDatabase;
import org.apache.flex.compiler.internal.codegen.databinding.BindingInfo;
import org.apache.flex.compiler.internal.codegen.databinding.FunctionWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.PropertyWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.StaticPropertyWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.WatcherInfoBase;
import org.apache.flex.compiler.internal.codegen.databinding.WatcherInfoBase.WatcherType;
import org.apache.flex.compiler.internal.codegen.databinding.XMLWatcherInfo;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassNode;
import org.apache.flex.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStateNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.NativeUtils;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * @author Erik de Bruin
 */
public class MXMLFlexJSEmitter extends MXMLEmitter implements
        IMXMLFlexJSEmitter
{

    private ArrayList<MXMLDescriptorSpecifier> currentInstances;
    private ArrayList<MXMLDescriptorSpecifier> currentPropertySpecifiers;
    private ArrayList<MXMLDescriptorSpecifier> descriptorTree;
    private MXMLDescriptorSpecifier propertiesTree;
    private ArrayList<MXMLEventSpecifier> events;
    private ArrayList<MXMLDescriptorSpecifier> instances;
    private ArrayList<MXMLScriptSpecifier> scripts;
    //private ArrayList<MXMLStyleSpecifier> styles;

    private int eventCounter;
    private int idCounter;

    private boolean inMXMLContent;
    private boolean inStatesOverride;
    
    private StringBuilder subDocuments = new StringBuilder();
    private ArrayList<String> subDocumentNames = new ArrayList<String>();

    public MXMLFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDocument(IMXMLDocumentNode node)
    {
        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();
        propertiesTree = new MXMLDescriptorSpecifier();

        events = new ArrayList<MXMLEventSpecifier>();
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        scripts = new ArrayList<MXMLScriptSpecifier>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        eventCounter = 0;
        idCounter = 0;

        // visit MXML
        IClassDefinition cdef = node.getClassDefinition();
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        ((JSFlexJSEmitter) asEmitter).thisClass = cdef;

        // visit tags
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i));
        }

        String cname = node.getFileNode().getName();

        emitHeader(node);

        write(subDocuments.toString());
        writeNewline();

        emitClassDeclStart(cname, node.getBaseClassName(), false);

        emitPropertyDecls();
        
        emitClassDeclEnd(cname, node.getBaseClassName());

        emitMetaData(cdef);

        emitScripts();

        emitEvents(cname);

        emitPropertyGetterSetters(cname);

        emitMXMLDescriptorFuncs(cname);

        emitBindingData(cname, cdef);

        emitEncodedCSS(cname);
        
    }

    public void emitSubDocument(IMXMLComponentNode node)
    {
        ArrayList<MXMLDescriptorSpecifier> oldDescriptorTree;
        MXMLDescriptorSpecifier oldPropertiesTree;
        ArrayList<MXMLEventSpecifier> oldEvents;
        ArrayList<MXMLScriptSpecifier> oldScripts;
        ArrayList<MXMLDescriptorSpecifier> oldCurrentInstances;
        ArrayList<MXMLDescriptorSpecifier> oldCurrentPropertySpecifiers;
        int oldEventCounter;
        int oldIdCounter;
        
        oldDescriptorTree = descriptorTree;
        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();
        oldPropertiesTree = propertiesTree;
        propertiesTree = new MXMLDescriptorSpecifier();

        oldEvents = events;
        events = new ArrayList<MXMLEventSpecifier>();
        // we don't save these.  We want all requires to be generated at the top of the file
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        oldScripts = scripts;
        scripts = new ArrayList<MXMLScriptSpecifier>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        oldCurrentInstances = currentInstances;
        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        oldCurrentPropertySpecifiers = currentPropertySpecifiers;
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        oldEventCounter = eventCounter;
        eventCounter = 0;
        oldIdCounter = idCounter;
        idCounter = 0;

        // visit MXML
        IClassDefinition cdef = node.getContainedClassDefinition();
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        ((JSFlexJSEmitter) asEmitter).thisClass = cdef;

        IASNode classNode = node.getContainedClassDefinitionNode();
        // visit tags
        final int len = classNode.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(classNode.getChild(i));
        }

        String cname = cdef.getQualifiedName();
        subDocumentNames.add(cname);
        String baseClassName = cdef.getBaseClassAsDisplayString();

        emitClassDeclStart(cname, baseClassName, false);

        emitPropertyDecls();
        
        emitClassDeclEnd(cname, baseClassName);

        emitMetaData(cdef);

        emitScripts();

        emitEvents(cname);

        emitPropertyGetterSetters(cname);

        emitMXMLDescriptorFuncs(cname);

        emitBindingData(cname, cdef);

        emitEncodedCSS(cname);

        descriptorTree = oldDescriptorTree;
        propertiesTree = oldPropertiesTree;
        events = oldEvents;
        scripts = oldScripts;
        currentInstances = oldCurrentInstances;
        currentPropertySpecifiers = oldCurrentPropertySpecifiers;
        eventCounter = oldEventCounter;
        idCounter = oldIdCounter;

    }

    //--------------------------------------------------------------------------

    protected void emitClassDeclStart(String cname, String baseClassName,
            boolean indent)
    {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @constructor");
        writeNewline(" * @extends {" + baseClassName + "}");
        writeNewline(" */");
        writeToken(cname);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        if (indent)
            indentPush();
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        write(cname);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);
        writeToken(ASEmitterTokens.COMMA);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(JSGoogEmitterTokens.GOOG_CONSTRUCTOR);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
    }

    //--------------------------------------------------------------------------

    protected void emitClassDeclEnd(String cname, String baseClassName)
    {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @private");
        writeNewline(" * @type {Array}");
        writeNewline(" */");
        writeNewline("this.mxmldd;");

        writeNewline();
        writeNewline("/**");
        writeNewline(" * @private");
        writeNewline(" * @type {Array}");
        writeNewline(" */");

        indentPop();

        writeNewline("this.mxmldp;");

        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        write(JSGoogEmitterTokens.GOOG_INHERITS);
        write(ASEmitterTokens.PAREN_OPEN);
        write(cname);
        writeToken(ASEmitterTokens.COMMA);
        write(baseClassName);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
        writeNewline();
    }

    //--------------------------------------------------------------------------

    protected void emitMetaData(IClassDefinition cdef)
    {
        String cname = cdef.getQualifiedName();
        
        writeNewline("/**");
        writeNewline(" * Metadata");
        writeNewline(" *");
        writeNewline(" * @type {Object.<string, Array.<Object>>}");
        writeNewline(" */");
        write(cname + ".prototype.FLEXJS_CLASS_INFO = { names: [{ name: '");
        write(cdef.getBaseName());
        write("', qName: '");
        write(cname);
        writeNewline("' }] };");
        writeNewline();
        writeNewline();
    }

    //--------------------------------------------------------------------------

    protected void emitPropertyDecls()
    {
        for (MXMLDescriptorSpecifier instance : instances)
        {
            writeNewline();
            writeNewline("/**");
            writeNewline(" * @private");
            writeNewline(" * @type {" + instance.name + "}");
            writeNewline(" */");
            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(instance.id);
            writeNewline(ASEmitterTokens.SEMICOLON);
        }
    }

    //--------------------------------------------------------------------------

    protected void emitBindingData(String cname, IClassDefinition cdef)
    {
        BindingDatabase bd = BindingDatabase.bindingMap.get(cdef);
        if (bd == null)
            return;
        if (bd.getBindingInfo().isEmpty())
            return;

        outputBindingInfoAsData(cname, bd);
    }

    private void outputBindingInfoAsData(String cname, BindingDatabase bindingDataBase)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        .getASEmitter();

        writeNewline("/**");
        writeNewline(" * @expose");
        writeNewline(" */");
        writeNewline(cname
                + ".prototype._bindings = [");
        
        Set<BindingInfo> bindingInfo = bindingDataBase.getBindingInfo();
        writeNewline(bindingInfo.size() + ","); // number of bindings
        
        for (BindingInfo bi : bindingInfo)
        {
            String s;
            s = bi.getSourceString();
            if (s == null)
                s = getSourceStringFromGetter(bi.getExpressionNodesForGetter());
            if (s.contains("."))
            {
                String[] parts = s.split("\\.");
                write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() + 
                        parts[0] + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                int n = parts.length;
                for (int i = 1; i < n; i++)
                {
                    String part = parts[i];
                    write(", " +  ASEmitterTokens.DOUBLE_QUOTE.getToken() + part + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                }
                writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
            }
            else if (s == null || s.length() == 0)
            {
                List<IExpressionNode> getterNodes = bi.getExpressionNodesForGetter();
                StringBuilder sb = new StringBuilder();
                sb.append("function() { return ");
                for (IExpressionNode getterNode : getterNodes)
                {
                    sb.append(asEmitter.stringifyNode(getterNode));
                }
                sb.append("; },");
                writeNewline(sb.toString());
            }
            else
                writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + s + 
                        ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            
            IExpressionNode destNode = bi.getExpressionNodeForDestination();
            if (destNode != null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(asEmitter.stringifyNode(destNode));
                writeNewline(sb.toString());
            }
            else
                writeNewline(ASEmitterTokens.NULL.getToken() + ASEmitterTokens.COMMA.getToken());
            
            s = bi.getDestinationString();
            if (s.contains("."))
            {
                String[] parts = s.split("\\.");
                write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() + 
                        parts[0] + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                int n = parts.length;
                for (int i = 1; i < n; i++)
                {
                    String part = parts[i];
                    write(", " + ASEmitterTokens.DOUBLE_QUOTE.getToken() + part + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                }
                writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
            }
            else
                writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + s +
                        ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        Set<Entry<Object, WatcherInfoBase>> watcherChains = bindingDataBase.getWatcherChains();
        for (Entry<Object, WatcherInfoBase> entry : watcherChains)
        {
            WatcherInfoBase watcherInfoBase = entry.getValue();
            encodeWatcher(watcherInfoBase);
        }
        // add a trailing null for now so I don't have to have logic where the watcher figures out not to add
        // a comma
        writeNewline("null" + ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.SEMICOLON.getToken());
    }

    private void encodeWatcher(WatcherInfoBase watcherInfoBase)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        .getASEmitter();

        writeNewline(watcherInfoBase.getIndex() + ASEmitterTokens.COMMA.getToken());
        WatcherType type = watcherInfoBase.getType();
        if (type == WatcherType.FUNCTION)
        {
            writeNewline("0" + ASEmitterTokens.COMMA.getToken());

            FunctionWatcherInfo functionWatcherInfo = (FunctionWatcherInfo)watcherInfoBase;
           
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + functionWatcherInfo.getFunctionName() + 
                    ASEmitterTokens.DOUBLE_QUOTE.getToken());
            IExpressionNode params[] = functionWatcherInfo.params;
            StringBuilder sb = new StringBuilder();
            sb.append("function() { return [");
            boolean firstone = true;
            for (IExpressionNode param : params)
            {
                if (firstone)
                    firstone = false;
                sb.append(ASEmitterTokens.COMMA.getToken());
                sb.append(asEmitter.stringifyNode(param));   
            }
            sb.append("]; },");
            outputEventNames(functionWatcherInfo.getEventNames());
            outputBindings(functionWatcherInfo.getBindings());
        }
        else if ((type == WatcherType.STATIC_PROPERTY) || (type == WatcherType.PROPERTY))
        {
            writeNewline((type == WatcherType.STATIC_PROPERTY ? "1" : "2") + 
                    ASEmitterTokens.COMMA.getToken());

            PropertyWatcherInfo propertyWatcherInfo = (PropertyWatcherInfo)watcherInfoBase;
           
            boolean makeStaticWatcher = (watcherInfoBase.getType() == WatcherType.STATIC_PROPERTY);
            
            // round up the getter function for the watcher, or null if we don't need one
            MethodInfo propertyGetterFunction = null;
            if (watcherInfoBase.isRoot && !makeStaticWatcher)
            {
                // TODO: figure out what this looks like
                // propertyGetterFunction = this.propertyGetter;
                // assert propertyGetterFunction != null;
            }
            else if (watcherInfoBase.isRoot && makeStaticWatcher)
            {
                 // TODO: implement getter func for static watcher.
            }
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + propertyWatcherInfo.getPropertyName() +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            outputEventNames(propertyWatcherInfo.getEventNames());
            outputBindings(propertyWatcherInfo.getBindings());
            if (propertyGetterFunction == null)
                writeNewline("null" + ASEmitterTokens.COMMA.getToken()); // null is valid
            if (type == WatcherType.STATIC_PROPERTY)
            {
                StaticPropertyWatcherInfo pwinfo = (StaticPropertyWatcherInfo)watcherInfoBase;
                Name classMName = pwinfo.getContainingClass(getMXMLWalker().getProject());
                writeNewline(nameToString(classMName));
            }
        }
        else if (type == WatcherType.XML)
        {
            writeNewline("3" + ASEmitterTokens.COMMA.getToken());

            XMLWatcherInfo xmlWatcherInfo = (XMLWatcherInfo)watcherInfoBase;
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + xmlWatcherInfo.getPropertyName() +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            outputBindings(xmlWatcherInfo.getBindings());
        }
        else assert false;     

        // then recurse into children
        Set<Entry<Object, WatcherInfoBase>> children = watcherInfoBase.getChildren();
        if (children != null)
        {
            writeNewline(ASEmitterTokens.SQUARE_OPEN.getToken());
            for ( Entry<Object, WatcherInfoBase> ent : children)
            {
                encodeWatcher(ent.getValue());
            }
            writeNewline("null" + ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        else
        {
            writeNewline("null" + ASEmitterTokens.COMMA.getToken());
        }
    }
    
    private String getSourceStringFromMemberAccessExpressionNode(MemberAccessExpressionNode node)
    {
        String s = "";
        
        IExpressionNode left = node.getLeftOperandNode();
        if (left instanceof FunctionCallNode) //  probably a cast
        {
            IASNode child = ((FunctionCallNode)left).getArgumentsNode().getChild(0);
            if (child instanceof IdentifierNode)
                s = getSourceStringFromIdentifierNode((IdentifierNode)child);
            else if (child instanceof MemberAccessExpressionNode)
                s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)child);
        }
        else if (left instanceof MemberAccessExpressionNode)
            s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)left);
        else if (left instanceof IdentifierNode)
            s = getSourceStringFromIdentifierNode((IdentifierNode)left);
        else
            System.out.println("expected binding member access left node" + node.toString());
        s += ".";
        
        IExpressionNode right = node.getRightOperandNode();
        if (right instanceof FunctionCallNode) //  probably a cast
        {
            IASNode child = ((FunctionCallNode)right).getArgumentsNode().getChild(0);
            if (child instanceof IdentifierNode)
                s += getSourceStringFromIdentifierNode((IdentifierNode)child);
            else if (child instanceof MemberAccessExpressionNode)
                s += getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)child);
        }
        else if (right instanceof MemberAccessExpressionNode)
            s += getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)right);
        else if (right instanceof IdentifierNode)
            s += getSourceStringFromIdentifierNode((IdentifierNode)right);
        else
            System.out.println("expected binding member access right node" + node.toString());
        
        return s;
    }
    
    private String getSourceStringFromIdentifierNode(IdentifierNode node)
    {
        return node.getName();
    }
    
    private String getSourceStringFromGetter(List<IExpressionNode> nodes)
    {
        String s = "";
        IExpressionNode node = nodes.get(0);
        if (node instanceof MemberAccessExpressionNode)
        {
            s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)node);
        }
        return s;
    }
    
    private void outputEventNames(List<String> events)
    {
        if (events.size() > 1)
        {
            int n = events.size();
            write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() +
                    events.get(0) + ASEmitterTokens.DOUBLE_QUOTE.getToken());
            for (int i = 1; i < n; i++)
            {
                String event = events.get(i);
                write(ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() + 
                        event + ASEmitterTokens.DOUBLE_QUOTE.getToken());
            }
            writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        else if (events.size() == 1)
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + events.get(0) +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
        else
            writeNewline("null" + ASEmitterTokens.COMMA.getToken());
    }
    
    private void outputBindings(List<BindingInfo> bindings)
    {
        if (bindings.size() > 1)
        {
            int n = bindings.size();
            write(ASEmitterTokens.SQUARE_OPEN.getToken() + bindings.get(0).getIndex());
            for (int i = 1; i < n; i++)
            {
                BindingInfo binding = bindings.get(i);
                write(ASEmitterTokens.COMMA.getToken() + binding.getIndex());
            }
            writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        else if (bindings.size() == 1)
            writeNewline(bindings.get(0).getIndex() + ASEmitterTokens.COMMA.getToken());
        else
            writeNewline("null" + ASEmitterTokens.COMMA.getToken());
        
    }

    //--------------------------------------------------------------------------    

    protected void emitScripts()
    {
        for (MXMLScriptSpecifier script : scripts)
        {
            String output = script.output();

            if (!output.equals(""))
            {
                writeNewline(output);
            }
        }
    }

    //--------------------------------------------------------------------------    

    protected void emitEvents(String cname)
    {
        for (MXMLEventSpecifier event : events)
        {
            writeNewline("/**");
            writeNewline(" * @expose");
            writeNewline(" * @param {" + event.type + "} event");
            writeNewline(" */");
            writeNewline(cname
                    + ".prototype." + event.eventHandler + " = function(event)");
            writeNewline(ASEmitterTokens.BLOCK_OPEN, true);

            writeNewline(event.value + ASEmitterTokens.SEMICOLON.getToken(),
                    false);

            write(ASEmitterTokens.BLOCK_CLOSE);
            writeNewline(";");
            writeNewline();
            writeNewline();
        }
    }

    //--------------------------------------------------------------------------    

    protected void emitPropertyGetterSetters(String cname)
    {
        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (!instance.id.startsWith(MXMLFlexJSEmitterTokens.ID_PREFIX
                    .getToken()))
            {
                writeNewline("/**");
                writeNewline(" * @expose");
                writeNewline(" * @return {" + instance.name + "}");
                writeNewline(" */");
                writeNewline(cname
                        + ".prototype.get_" + instance.id + " = function()");
                indentPush();
                writeNewline("{");
                indentPop();
                writeNewline("return this." + instance.id + ";");
                writeNewline("};");
                writeNewline();
                writeNewline();
                writeNewline("/**");
                writeNewline(" * @expose");
                writeNewline(" * @param {" + instance.name + "} value");
                writeNewline(" */");
                writeNewline(cname
                        + ".prototype.set_" + instance.id
                        + " = function(value)");
                indentPush();
                writeNewline("{");
                indentPush();
                writeNewline("if (value != this." + instance.id + ")");
                indentPop();
                indentPop();
                writeNewline("this." + instance.id + " = value;");
                writeNewline("};");
                writeNewline();
                writeNewline();
            }
        }
    }

    //--------------------------------------------------------------------------    

    protected void emitMXMLDescriptorFuncs(String cname)
    {
        // top level is 'mxmlContent', skip it...
        if (descriptorTree.size() > 0)
        {
            MXMLDescriptorSpecifier root = descriptorTree.get(0);
            root.isTopNode = false;
    
            writeNewline("/**");
            writeNewline(" * @override");
            writeNewline(" * @return {Array} the Array of UI element descriptors.");
            writeNewline(" */");
            writeNewline(cname + ".prototype.get_MXMLDescriptor = function()");
            indentPush();
            writeNewline("{");
            writeNewline("if (this.mxmldd == undefined)");
            indentPush();
            writeNewline("{");
            writeNewline("/** @type {Array} */");
            writeNewline("var arr = " + cname + ".base(this, 'get_MXMLDescriptor');");
            writeNewline("/** @type {Array} */");
            indentPop();
            indentPop();
            writeNewline("var data = [");
    
            writeNewline(root.output(true));
    
            indentPush();
            writeNewline("];");
            indentPush();
            writeNewline("");
            indentPush();
            writeNewline("if (arr)");
            indentPop();
            writeNewline("this.mxmldd = arr.concat(data);");
            indentPush();
            writeNewline("else");
            indentPop();
            indentPop();
            writeNewline("this.mxmldd = data;");
            writeNewline("}");
            indentPop();
            writeNewline("return this.mxmldd;");
            writeNewline("};");
            writeNewline();
        }
        
        if (propertiesTree.propertySpecifiers.size() > 0 ||
                propertiesTree.eventSpecifiers.size() > 0)
        {
            writeNewline("/**");
            writeNewline(" * @override");
            writeNewline(" * @return {Array} the Array of UI element descriptors.");
            writeNewline(" */");
            writeNewline(cname + ".prototype.get_MXMLProperties = function()");
            indentPush();
            writeNewline("{");
            writeNewline("if (this.mxmldp == undefined)");
            indentPush();
            writeNewline("{");
            writeNewline("/** @type {Array} */");
            writeNewline("var arr = " + cname + ".base(this, 'get_MXMLProperties');");
            writeNewline("/** @type {Array} */");
            indentPop();
            indentPop();
            writeNewline("var data = [");
    
            MXMLDescriptorSpecifier root = propertiesTree;
            root.isTopNode = true;
            writeNewline(root.output(true));
    
            indentPush();
            writeNewline("];");
            indentPush();
            writeNewline("");
            indentPush();
            writeNewline("if (arr)");
            indentPop();
            writeNewline("this.mxmldp = arr.concat(data);");
            indentPush();
            writeNewline("else");
            indentPop();
            indentPop();
            writeNewline("this.mxmldp = data;");
            writeNewline("}");
            indentPop();
            writeNewline("return this.mxmldp;", false);
            writeNewline("};");
        }
    }

    //--------------------------------------------------------------------------    

    @Override
    public void emitEventSpecifier(IMXMLEventSpecifierNode node)
    {
        IDefinition cdef = node.getDefinition();

        MXMLDescriptorSpecifier currentDescriptor = getCurrentDescriptor("i");

        MXMLEventSpecifier eventSpecifier = new MXMLEventSpecifier();
        eventSpecifier.eventHandler = MXMLFlexJSEmitterTokens.EVENT_PREFIX
                .getToken() + eventCounter++;
        eventSpecifier.name = cdef.getBaseName();
        eventSpecifier.type = node.getEventParameterDefinition()
                .getTypeAsDisplayString();

        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        StringBuilder sb = null;
        int len = node.getChildCount();
        if (len > 0)
        {
            sb = new StringBuilder();
            for (int i = 0; i < len; i++)
            {
                sb.append(getIndent((i > 0) ? 1 : 0)
                        + asEmitter.stringifyNode(node.getChild(i)));
                if (i < len - 1)
                {
                    sb.append(ASEmitterTokens.SEMICOLON.getToken());
                    sb.append(ASEmitterTokens.NEW_LINE.getToken());
                }
            }
        }
        eventSpecifier.value = sb.toString();

        if (currentDescriptor != null)
            currentDescriptor.eventSpecifiers.add(eventSpecifier);
        else  // in theory, if no currentdescriptor must be top tag event
            propertiesTree.eventSpecifiers.add(eventSpecifier);

        events.add(eventSpecifier);
    }

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        if (isStateDependent(node) && !inStatesOverride)
            return;
        
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");

        String id = node.getID();
        if (id == null)
            id = node.getEffectiveID();
        if (id == null)
            id = MXMLFlexJSEmitterTokens.ID_PREFIX.getToken() + idCounter++;

        MXMLDescriptorSpecifier currentInstance = new MXMLDescriptorSpecifier();
        currentInstance.isProperty = false;
        currentInstance.id = id;
        currentInstance.name = cdef.getQualifiedName();
        currentInstance.parent = currentPropertySpecifier;

        if (currentPropertySpecifier != null)
            currentPropertySpecifier.propertySpecifiers.add(currentInstance);
        else if (inMXMLContent)
            descriptorTree.add(currentInstance);
        else
        {
            currentInstance.parent = propertiesTree;
            propertiesTree.propertySpecifiers.add(currentInstance);
        }

        instances.add(currentInstance);

        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            moveDown(false, currentInstance, null);

            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                getMXMLWalker().walk(pnode); // Property Specifier
            }

            moveUp(false, true);
        }
        else if (node instanceof IMXMLStateNode)
        {
            IMXMLStateNode stateNode = (IMXMLStateNode)node;
            String name = stateNode.getStateName();
            if (name != null)
            {
                MXMLDescriptorSpecifier stateName = new MXMLDescriptorSpecifier();
                stateName.isProperty = true;
                stateName.id = id;
                stateName.name = "name";
                stateName.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
                stateName.parent = currentInstance;
                currentInstance.propertySpecifiers.add(stateName);
            }
            MXMLDescriptorSpecifier overrides = new MXMLDescriptorSpecifier();
            overrides.isProperty = true;
            overrides.hasArray = true;
            overrides.id = id;
            overrides.name = "overrides";
            overrides.parent = currentInstance;
            currentInstance.propertySpecifiers.add(overrides);
            moveDown(false, null, overrides);

            IMXMLClassDefinitionNode classDefinitionNode = stateNode.getClassDefinitionNode();
            List<IMXMLNode> snodes = classDefinitionNode.getNodesDependentOnState(stateNode.getStateName());
            if (snodes != null)
            {
                for (int i=snodes.size()-1; i>=0; --i)
                {
                    IMXMLNode inode = snodes.get(i);
                    if (inode.getNodeID() == ASTNodeID.MXMLInstanceID)
                    {
                        emitInstanceOverride((IMXMLInstanceNode)inode);
                    }
                }
                // Next process the non-instance overrides dependent on this state.
                // Each one will generate code to push an IOverride instance.
                for (IMXMLNode anode : snodes)
                {
                    switch (anode.getNodeID())
                    {
                        case MXMLPropertySpecifierID:
                        {
                            emitPropertyOverride((IMXMLPropertySpecifierNode)anode);
                            break;
                        }
                        case MXMLStyleSpecifierID:
                        {
                            emitStyleOverride((IMXMLStyleSpecifierNode)node);
                            break;
                        }
                        case MXMLEventSpecifierID:
                        {
                            emitEventOverride((IMXMLEventSpecifierNode)node);
                            break;
                        }
                        default:
                        {
                            break;
                        }
                    }
                }
            }
            
            moveUp(false, false);
        }

        IMXMLEventSpecifierNode[] enodes = node.getEventSpecifierNodes();
        if (enodes != null)
        {
            moveDown(false, currentInstance, null);

            for (IMXMLEventSpecifierNode enode : enodes)
            {
                getMXMLWalker().walk(enode); // Event Specifier
            }

            moveUp(false, true);
        }
    }

    public void emitPropertyOverride(IMXMLPropertySpecifierNode propertyNode)
    {
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name propertyOverride = project.getPropertyOverrideClassName();
        emitPropertyOrStyleOverride(propertyOverride, propertyNode);
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetStyle
     * with its <code>target</code>, <code>name</code>,
     * and <code>value</code> properties set.
     */
    void emitStyleOverride(IMXMLStyleSpecifierNode styleNode)
    {
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name styleOverride = project.getStyleOverrideClassName();
        emitPropertyOrStyleOverride(styleOverride, styleNode);
    }
    
    void emitPropertyOrStyleOverride(Name overrideName, IMXMLPropertySpecifierNode propertyOrStyleNode)
    {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        IASNode parentNode = propertyOrStyleNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = propertyOrStyleNode.getName();        
        
        IMXMLInstanceNode propertyOrStyleValueNode = propertyOrStyleNode.getInstanceNode();
        
        MXMLDescriptorSpecifier setProp = new MXMLDescriptorSpecifier();
        setProp.isProperty = false;
        setProp.name = nameToString(overrideName);
        setProp.parent = currentInstance;
        currentInstance.propertySpecifiers.add(setProp);
            // Set its 'target' property to the id of the object
            // whose property or style this override will set.
        MXMLDescriptorSpecifier target = new MXMLDescriptorSpecifier();
        target.isProperty = true;
        target.name = "target";
        target.parent = setProp;
        target.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + id + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setProp.propertySpecifiers.add(target);

            // Set its 'name' property to the name of the property or style.
        MXMLDescriptorSpecifier pname = new MXMLDescriptorSpecifier();
        pname.isProperty = true;
        pname.name = "name";
        pname.parent = setProp;
        pname.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setProp.propertySpecifiers.add(pname);

            // Set its 'value' property to the value of the property or style.
        MXMLDescriptorSpecifier value = new MXMLDescriptorSpecifier();
        value.isProperty = true;
        value.name = "value";
        value.parent = setProp;
        setProp.propertySpecifiers.add(value);
        moveDown(false, null, value);
        getMXMLWalker().walk(propertyOrStyleValueNode); // instance node
        moveUp(false, false);
    }
        
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetEventHandler
     * with its <code>target</code>, <code>name</code>,
     * and <code>handlerFunction</code> properties set.
     */
    void emitEventOverride(IMXMLEventSpecifierNode eventNode)
    {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name eventOverride = project.getEventOverrideClassName();
        
        IASNode parentNode = eventNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = eventNode.getName();
        
        MXMLDocumentNode doc = (MXMLDocumentNode)eventNode.getAncestorOfType(MXMLDocumentNode.class);

        Name eventHandler = doc.cdp.getEventHandlerName(eventNode);

        MXMLDescriptorSpecifier setEvent = new MXMLDescriptorSpecifier();
        setEvent.isProperty = true;
        setEvent.name = nameToString(eventOverride);
        setEvent.parent = currentInstance;
        currentInstance.propertySpecifiers.add(setEvent);
        // Set its 'target' property to the id of the object
        // whose event this override will set.
        MXMLDescriptorSpecifier target = new MXMLDescriptorSpecifier();
        target.isProperty = true;
        target.name = "target";
        target.parent = setEvent;
        target.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + id + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setEvent.propertySpecifiers.add(target);

        // Set its 'name' property to the name of the property or style.
        MXMLDescriptorSpecifier pname = new MXMLDescriptorSpecifier();
        pname.isProperty = true;
        pname.name = "name";
        pname.parent = setEvent;
        pname.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setEvent.propertySpecifiers.add(pname);
        
        // Set its 'handlerFunction' property to the autogenerated event handler.
        MXMLDescriptorSpecifier handler = new MXMLDescriptorSpecifier();
        handler.isProperty = false;
        handler.name = "handlerFunction";
        handler.parent = setEvent;
        handler.value = eventHandler.toString();
        setEvent.propertySpecifiers.add(handler);
        
    }

    public void emitInstanceOverride(IMXMLInstanceNode instanceNode)
    {
        inStatesOverride = true;
        
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name instanceOverrideName = project.getInstanceOverrideClassName();

        MXMLDescriptorSpecifier addItems = new MXMLDescriptorSpecifier();
        addItems.isProperty = false;
        addItems.name = nameToString(instanceOverrideName);
        addItems.parent = currentInstance;
        currentInstance.propertySpecifiers.add(addItems);
        MXMLDescriptorSpecifier itemsDesc = new MXMLDescriptorSpecifier();
        itemsDesc.isProperty = true;
        itemsDesc.hasArray = true;
        itemsDesc.name = "itemsDescriptor";
        itemsDesc.parent = addItems;
        addItems.propertySpecifiers.add(itemsDesc);
        boolean oldInMXMLContent = inMXMLContent;
        moveDown(false, null, itemsDesc);
        inMXMLContent = true;
        getMXMLWalker().walk(instanceNode); // instance node
        inMXMLContent = oldInMXMLContent;
        moveUp(false, false);
        
        //-----------------------------------------------------------------------------
        // Second property set: maybe set destination and propertyName
        
        // get the property specifier node for the property the instanceNode represents
        IMXMLPropertySpecifierNode propertySpecifier = (IMXMLPropertySpecifierNode) 
            instanceNode.getAncestorOfType( IMXMLPropertySpecifierNode.class);
    
        if (propertySpecifier == null)
        {
           assert false;        // I think this indicates an invalid tree...
        }
        else
        {
            // Check the parent - if it's an instance then we want to use these
            // nodes to get our property values from. If not, then it's the root
            // and we don't need to specify destination
            
            IASNode parent = propertySpecifier.getParent();
            if (parent instanceof IMXMLInstanceNode)
            {
               IMXMLInstanceNode parentInstance = (IMXMLInstanceNode)parent;
               String parentId = parentInstance.getEffectiveID();
               assert parentId != null;
               String propName = propertySpecifier.getName();
               
               MXMLDescriptorSpecifier dest = new MXMLDescriptorSpecifier();
               dest.isProperty = true;
               dest.name = "destination";
               dest.parent = addItems;
               dest.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + parentId + ASEmitterTokens.SINGLE_QUOTE.getToken();
               addItems.propertySpecifiers.add(dest);

               MXMLDescriptorSpecifier prop = new MXMLDescriptorSpecifier();
               prop.isProperty = true;
               prop.name = "propertyName";
               prop.parent = addItems;
               prop.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + propName + ASEmitterTokens.SINGLE_QUOTE.getToken();
               addItems.propertySpecifiers.add(prop);
            }
        }  
        
        //---------------------------------------------------------------
        // Third property set: position and relativeTo
        String positionPropertyValue = null;
        String relativeToPropertyValue = null;
       
        // look to see if we have any sibling nodes that are not state dependent
        // that come BEFORE us
        IASNode instanceParent = instanceNode.getParent();
        IASNode prevStatelessSibling=null;
        for (int i=0; i< instanceParent.getChildCount(); ++i)
        {
            IASNode sib = instanceParent.getChild(i);
            assert sib instanceof IMXMLInstanceNode;    // surely our siblings are also instances?
           
            // stop looking for previous nodes when we find ourself
            if (sib == instanceNode)
                break;

            if (!isStateDependent(sib))
            {
                prevStatelessSibling = sib;
            }
        }
        
        if (prevStatelessSibling == null) {
            positionPropertyValue = "first";        // TODO: these should be named constants
        }
        else {
            positionPropertyValue = "after";
            relativeToPropertyValue = ((IMXMLInstanceNode)prevStatelessSibling).getEffectiveID();
        }
       
        MXMLDescriptorSpecifier pos = new MXMLDescriptorSpecifier();
        pos.isProperty = true;
        pos.name = "position";
        pos.parent = addItems;
        pos.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + positionPropertyValue + ASEmitterTokens.SINGLE_QUOTE.getToken();
        addItems.propertySpecifiers.add(pos);
        
        if (relativeToPropertyValue != null)
        {
            MXMLDescriptorSpecifier rel = new MXMLDescriptorSpecifier();
            rel.isProperty = true;
            rel.name = "relativeTo";
            rel.parent = addItems;
            rel.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + relativeToPropertyValue + ASEmitterTokens.SINGLE_QUOTE.getToken();
            addItems.propertySpecifiers.add(rel);
        }
        
        inStatesOverride = false;
    }

    private String nameToString(Name name)
    {
        String s = "";
        Namespace ns = name.getSingleQualifier();
        s = ns.getName() + ASEmitterTokens.MEMBER_ACCESS.getToken() + name.getBaseName();
        return s;
    }
    /**
     * Determines whether a node is state-dependent.
     * TODO: we should move to IMXMLNode
     */
    protected boolean isStateDependent(IASNode node)
    {
        if (node instanceof IMXMLSpecifierNode)
        {
            String suffix = ((IMXMLSpecifierNode)node).getSuffix();
            return suffix != null && suffix.length() > 0;
        }
        else if (isStateDependentInstance(node))
            return true;
        return false;
    }
    
    /**
     * Determines whether the geven node is an instance node, as is state dependent
     */
    protected boolean isStateDependentInstance(IASNode node)
    {
        if (node instanceof IMXMLInstanceNode)
        {
            String[] includeIn = ((IMXMLInstanceNode)node).getIncludeIn();
            String[] excludeFrom = ((IMXMLInstanceNode)node).getExcludeFrom();
            return includeIn != null || excludeFrom != null;
        }
        return false;
    }
    
    /**
     * Is a give node a "databinding node"?
     */
    public static boolean isDataBindingNode(IASNode node)
    {
        return node instanceof IMXMLDataBindingNode;
    }
    
    protected static boolean isDataboundProp(IMXMLPropertySpecifierNode propertyNode)
    {
        boolean ret = propertyNode.getChildCount() > 0 && isDataBindingNode(propertyNode.getInstanceNode());
        
        // Sanity check that we based our conclusion about databinding on the correct node.
        // (code assumes only one child if databinding)
        int n = propertyNode.getChildCount();
        for (int i = 0; i < n; i++)
        {
            boolean db = isDataBindingNode(propertyNode.getChild(i));
            assert db == ret;
        }
        
        return ret;
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        if (isDataboundProp(node))
            return;
        
        IDefinition cdef = node.getDefinition();

        IASNode cnode = node.getChild(0);

        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");

        MXMLDescriptorSpecifier currentPropertySpecifier = new MXMLDescriptorSpecifier();
        currentPropertySpecifier.isProperty = true;
        currentPropertySpecifier.name = cdef.getQualifiedName();
        currentPropertySpecifier.parent = currentInstance;

        boolean oldInMXMLContent = inMXMLContent;
        if (currentPropertySpecifier.name.equals("mxmlContent"))
            inMXMLContent = true;
        
        if (currentInstance != null)
            currentInstance.propertySpecifiers.add(currentPropertySpecifier);
        else if (inMXMLContent)
            descriptorTree.add(currentPropertySpecifier);
        else
        {
            currentPropertySpecifier.parent = propertiesTree;
            propertiesTree.propertySpecifiers.add(currentPropertySpecifier);
        }

        boolean bypass = cnode != null && cnode instanceof IMXMLArrayNode;

        currentPropertySpecifier.hasArray = bypass;

        moveDown(bypass, null, currentPropertySpecifier);

        getMXMLWalker().walk(cnode); // Array or Instance

        moveUp(bypass, false);
        
        inMXMLContent = oldInMXMLContent;
    }

    @Override
    public void emitScript(IMXMLScriptNode node)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        String nl = ASEmitterTokens.NEW_LINE.getToken();

        StringBuilder sb = null;
        MXMLScriptSpecifier scriptSpecifier = null;

        int len = node.getChildCount();
        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                IASNode cnode = node.getChild(i);

                if (!(cnode instanceof IImportNode))
                {
                    sb = new StringBuilder();
                    scriptSpecifier = new MXMLScriptSpecifier();

                    sb.append(asEmitter.stringifyNode(cnode));

                    sb.append(ASEmitterTokens.SEMICOLON.getToken());

                    if (i == len - 1)
                        indentPop();

                    sb.append(nl);
                    sb.append(nl);

                    scriptSpecifier.fragment = sb.toString();

                    scripts.add(scriptSpecifier);
                }
            }
        }
    }

    @Override
    public void emitStyleSpecifier(IMXMLStyleSpecifierNode node)
    {
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitArray(IMXMLArrayNode node)
    {
        moveDown(false, null, null);

        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i)); // Instance
        }

        moveUp(false, false);
    }

    @Override
    public void emitString(IMXMLStringNode node)
    {
        getCurrentDescriptor("ps").valueNeedsQuotes = true;

        emitAttributeValue(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitLiteral(IMXMLLiteralNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "";

        if (ps.valueNeedsQuotes)
            ps.value += ASEmitterTokens.SINGLE_QUOTE.getToken();

        String s = node.getValue().toString();
        if (ps.valueNeedsQuotes)
        {
            // escape all single quotes found within the string
            s = s.replace(ASEmitterTokens.SINGLE_QUOTE.getToken(), 
                    "\\" + ASEmitterTokens.SINGLE_QUOTE.getToken());
        }
        ps.value += s;
        
        if (ps.valueNeedsQuotes)
            ps.value += ASEmitterTokens.SINGLE_QUOTE.getToken();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitFactory(IMXMLFactoryNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new mx.core.ClassFactory(";

        IASNode cnode = node.getChild(0);
        if (cnode instanceof IMXMLClassNode)
        {
            ps.value += ((IMXMLClassNode)cnode).getValue(getMXMLWalker().getProject()).getQualifiedName();
        }
        ps.value += ")";
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitComponent(IMXMLComponentNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new mx.core.ClassFactory(";

        ps.value += node.getName();
        ps.value += ")";
        
        setBufferWrite(true);
        emitSubDocument(node);
        subDocuments.append(getBuilder().toString());
        getBuilder().setLength(0);
        setBufferWrite(false);
    }

    //--------------------------------------------------------------------------
    //    JS output
    //--------------------------------------------------------------------------

    private void emitEncodedCSS(String cname)
    {
        String s = ((MXMLFlexJSBlockWalker)getMXMLWalker()).encodedCSS;
        if (!s.isEmpty())
        {
            int reqidx = s.indexOf("goog.require");
            if (reqidx != -1)
                s = s.substring(0, reqidx - 1);

            writeNewline();
            writeNewline("/**");
            writeNewline(" * @expose");
            writeNewline(" */");
            StringBuilder sb = new StringBuilder();
            sb.append(cname);
            sb.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
            sb.append(JSEmitterTokens.PROTOTYPE.getToken());
            sb.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
            sb.append("cssData");
            sb.append(ASEmitterTokens.SPACE.getToken() +
                        ASEmitterTokens.EQUAL.getToken() +
                        ASEmitterTokens.SPACE.getToken() +
                        ASEmitterTokens.SQUARE_OPEN.getToken());
            sb.append(s);
            write(sb.toString());
            writeNewline();
        }
    }
    
    private void emitHeader(IMXMLDocumentNode node)
    {
        String cname = node.getFileNode().getName();
        String bcname = node.getBaseClassName();

        writeNewline("/**");
        writeNewline(" * " + cname);
        writeNewline(" *");
        writeNewline(" * @fileoverview");
        writeNewline(" *");
        writeNewline(" * @suppress {checkTypes}");
        writeNewline(" */");
        writeNewline();
        
        emitHeaderLine(cname, true); // provide
        for (String subDocumentName : subDocumentNames)
            emitHeaderLine(subDocumentName, true);
        writeNewline();
        emitHeaderLine(bcname);
        ArrayList<String> writtenInstances = new ArrayList<String>();
        writtenInstances.add(cname); // make sure we don't add ourselves
        writtenInstances.add(bcname); // make sure we don't add the baseclass twice
        for (MXMLDescriptorSpecifier instance : instances)
        {
            String name = instance.name;
            if (writtenInstances.indexOf(name) == -1)
            {
                emitHeaderLine(name);
                writtenInstances.add(name);
            }
        }
        FlexJSProject project = (FlexJSProject) getMXMLWalker().getProject();
        ASProjectScope projectScope = (ASProjectScope) project.getScope();
        IDefinition cdef = node.getDefinition();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(cdef);
        ArrayList<String> deps = project.getRequires(cu);

        if (deps != null)
        {
            for (String imp : deps)
            {
                if (imp.indexOf(JSGoogEmitterTokens.AS3.getToken()) != -1)
                    continue;
    
                if (imp.equals(cname))
                    continue;
    
                if (imp.equals("mx.binding.Binding"))
                    continue;
                if (imp.equals("mx.binding.BindingManager"))
                    continue;
                if (imp.equals("mx.binding.FunctionReturnWatcher"))
                    continue;
                if (imp.equals("mx.binding.PropertyWatcher"))
                    continue;
                if (imp.equals("mx.binding.StaticPropertyWatcher"))
                    continue;
                if (imp.equals("mx.binding.XMLWatcher"))
                    continue;
                if (imp.equals("mx.events.PropertyChangeEvent"))
                    continue;
                if (imp.equals("mx.events.PropertyChangeEventKind"))
                    continue;
                if (imp.equals("mx.core.DeferredInstanceFromFunction"))
                    continue;
    
                if (NativeUtils.isNative(imp))
                    continue;
    
                if (writtenInstances.indexOf(imp) == -1)
                {
                    emitHeaderLine(imp);
                    writtenInstances.add(imp);
                }
            }
        }

        String s = ((MXMLFlexJSBlockWalker)getMXMLWalker()).encodedCSS;
        if (!s.isEmpty())
        {
            int reqidx = s.indexOf("goog.require");
            if (reqidx != -1)
            {
                String reqs = s.substring(reqidx);
                writeNewline(reqs);
            }
        }

        // erikdebruin: Add missing language feature support, like the 'is' and 
        //              'as' operators. We don't need to worry about requiring
        //              this in every project: ADVANCED_OPTIMISATIONS will NOT
        //              include any of the code if it is not used in the project.
        if (project.mainCU != null &&
                cu.getName().equals(project.mainCU.getName()))
        {
            emitHeaderLine(JSFlexJSEmitterTokens.LANGUAGE_QNAME.getToken());
        }

        writeNewline();
        writeNewline();
    }

    private void emitHeaderLine(String qname)
    {
        emitHeaderLine(qname, false);
    }

    private void emitHeaderLine(String qname, boolean isProvide)
    {
        write((isProvide) ? JSGoogEmitterTokens.GOOG_PROVIDE
                : JSGoogEmitterTokens.GOOG_REQUIRE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(qname);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
    }

    //--------------------------------------------------------------------------
    //    Utils
    //--------------------------------------------------------------------------

    @Override
    protected void emitAttributeValue(IASNode node)
    {
        IMXMLLiteralNode cnode = (IMXMLLiteralNode) node.getChild(0);

        if (cnode.getValue() != null)
            getMXMLWalker().walk((IASNode) cnode); // Literal
    }

    private MXMLDescriptorSpecifier getCurrentDescriptor(String type)
    {
        MXMLDescriptorSpecifier currentDescriptor = null;

        int index;

        if (type.equals("i"))
        {
            index = currentInstances.size() - 1;
            if (index > -1)
                currentDescriptor = currentInstances.get(index);
        }
        else
        {
            index = currentPropertySpecifiers.size() - 1;
            if (index > -1)
                currentDescriptor = currentPropertySpecifiers.get(index);
        }

        return currentDescriptor;
    }

    protected void moveDown(boolean byPass,
            MXMLDescriptorSpecifier currentInstance,
            MXMLDescriptorSpecifier currentPropertySpecifier)
    {
        if (!byPass)
        {
            if (currentInstance != null)
                currentInstances.add(currentInstance);
        }

        if (currentPropertySpecifier != null)
            currentPropertySpecifiers.add(currentPropertySpecifier);
    }

    protected void moveUp(boolean byPass, boolean isInstance)
    {
        if (!byPass)
        {
            int index;

            if (isInstance)
            {
                index = currentInstances.size() - 1;
                if (index > -1)
                    currentInstances.remove(index);
            }
            else
            {
                index = currentPropertySpecifiers.size() - 1;
                if (index > -1)
                    currentPropertySpecifiers.remove(index);
            }
        }
    }

}
