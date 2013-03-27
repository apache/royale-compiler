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

import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.codegen.mxml.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * @author Erik de Bruin
 */
public class MXMLFlexJSEmitter extends MXMLEmitter implements
        IMXMLFlexJSEmitter
{

    // list of all current instance and it's ancestors
    private ArrayList<MXMLDescriptorSpecifier> currentInstances;

    // list of all current property specifier and it's ancestors
    private ArrayList<MXMLDescriptorSpecifier> currentPropertySpecifiers;

    // tree of all contents
    private ArrayList<MXMLDescriptorSpecifier> descriptorTree;

    // linear list of all events
    private ArrayList<MXMLEventSpecifier> events;

    // linear list of all instances
    private ArrayList<MXMLDescriptorSpecifier> instances;

    // linear list of all scripts
    private ArrayList<MXMLScriptSpecifier> scripts;

    // linear list of all styles
    //private ArrayList<MXMLStyleSpecifier> styles;

    private int eventCounter;
    private int idCounter;

    private boolean isMainFile;

    public MXMLFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDocument(IMXMLDocumentNode node)
    {
        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();

        events = new ArrayList<MXMLEventSpecifier>();
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        scripts = new ArrayList<MXMLScriptSpecifier>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        isMainFile = !isMXMLContentNode((IMXMLPropertySpecifierNode) node
                .getPropertySpecifierNodes()[0]);

        eventCounter = 0;
        idCounter = 0;

        if (isMainFile)
        {
            // fake a 'root' node, like 'mxmlContent'
            MXMLDescriptorSpecifier fakeRoot = new MXMLDescriptorSpecifier();
            fakeRoot.name = "mxmlContent";
            descriptorTree.add(fakeRoot);
            currentInstances.add(fakeRoot);
        }

        // visit tags
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i));
        }

        String cname = node.getFileNode().getName();
        emitHeaderDoc(cname, "provide");

        writeNewline();
        emitHeaderDoc(node.getBaseClassName(), "require");
        ArrayList<String> writtenInstances = new ArrayList<String>();
        for (MXMLDescriptorSpecifier instance : instances)
        {
            String name = instance.name;
            if (writtenInstances.indexOf(name) == -1)
            {
                emitHeaderDoc(name, "require");
                writtenInstances.add(name);
            }
        }

        writeNewline();
        writeNewline("/**");
        writeNewline(" * @constructor");
        writeNewline(" * @extends {" + node.getBaseClassName() + "}");
        writeNewline(" */");
        writeToken(cname);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        indentPush();
        writeNewline(ASEmitterTokens.BLOCK_OPEN);
        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);

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
        write(node.getBaseClassName());
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();

        for (MXMLScriptSpecifier script : scripts)
        {
            writeNewline(script.output());
        }

        for (MXMLEventSpecifier event : events)
        {
            writeNewline("/**");
            writeNewline(" * @this {" + cname + "}");
            writeNewline(" * @expose");
            writeNewline(" * @param {" + event.type + "} event");
            writeNewline(" */");
            writeNewline(cname
                    + ".prototype." + event.eventHandler + " = function(event)");
            indentPush();
            writeNewline("{");
            indentPop();
            writeNewline(event.value + ASEmitterTokens.SEMICOLON.getToken());
            writeNewline("};");
            writeNewline();
        }

        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (!instance.id.startsWith(MXMLFlexJSEmitterTokens.ID_PREFIX
                    .getToken()))
            {
                writeNewline("/**");
                writeNewline(" * @this {" + cname + "}");
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
                writeNewline("/**");
                writeNewline(" * @this {" + cname + "}");
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
            }
        }

        // top level is 'mxmlContent', skip it...
        MXMLDescriptorSpecifier root = descriptorTree.get(0);
        root.isTopNodeInMainFile = isMainFile;

        writeNewline("/**");
        writeNewline(" * @override");
        writeNewline(" * @this {" + cname + "}");
        writeNewline(" * @return {Array} the Array of UI element descriptors.");
        writeNewline(" */");
        writeNewline(cname + ".prototype.get_MXMLDescriptor = function()");
        indentPush();
        writeNewline("{");
        writeNewline("if (this.mxmldd == undefined)");
        indentPush();
        writeNewline("{");
        writeNewline("/** @type {Array} */");
        writeNewline("var arr = goog.base(this, 'get_MXMLDescriptor');");
        writeNewline("/** @type {Array} */");
        indentPop();
        indentPop();
        writeNewline("var data = [");

        if (!isMainFile)
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
        writeNewline("/**");
        writeNewline(" * @override");
        writeNewline(" * @this {" + cname + "}");
        writeNewline(" * @return {Array} the Array of UI element descriptors.");
        writeNewline(" */");
        writeNewline(cname + ".prototype.get_MXMLProperties = function()");
        indentPush();
        writeNewline("{");
        writeNewline("if (this.mxmldp == undefined)");
        indentPush();
        writeNewline("{");
        writeNewline("/** @type {Array} */");
        writeNewline("var arr = goog.base(this, 'get_MXMLProperties');");
        writeNewline("/** @type {Array} */");
        indentPop();
        indentPop();
        writeNewline("var data = [");

        if (isMainFile)
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
        writeNewline("return this.mxmldp;");
        writeNewline("};");
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

        eventSpecifier.value = asEmitter.stringifyNode(node.getChild(0));

        if (currentDescriptor != null)
            currentDescriptor.eventSpecifiers.add(eventSpecifier);

        events.add(eventSpecifier);
    }

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");

        String id = node.getID();
        if (id == null)
            id = MXMLFlexJSEmitterTokens.ID_PREFIX.getToken() + idCounter++;

        MXMLDescriptorSpecifier currentInstance = new MXMLDescriptorSpecifier();
        currentInstance.isProperty = false;
        currentInstance.id = id;
        currentInstance.name = cdef.getQualifiedName();
        currentInstance.parent = currentPropertySpecifier;

        if (currentPropertySpecifier != null)
            currentPropertySpecifier.propertySpecifiers.add(currentInstance);
        else
            descriptorTree.add(currentInstance);

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

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        IDefinition cdef = node.getDefinition();

        IASNode cnode = node.getChild(0);

        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");

        MXMLDescriptorSpecifier currentPropertySpecifier = new MXMLDescriptorSpecifier();
        currentPropertySpecifier.isProperty = true;
        currentPropertySpecifier.name = cdef.getQualifiedName();
        currentPropertySpecifier.parent = currentInstance;

        if (currentInstance != null)
            currentInstance.propertySpecifiers.add(currentPropertySpecifier);
        else
            descriptorTree.add(currentPropertySpecifier);

        boolean bypass = cnode != null && cnode instanceof IMXMLArrayNode;

        currentPropertySpecifier.hasArray = bypass;

        moveDown(bypass, null, currentPropertySpecifier);

        getMXMLWalker().walk(cnode); // Array or Instance

        moveUp(bypass, false);
    }

    @Override
    public void emitScript(IMXMLScriptNode node)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        IJSGoogDocEmitter docEmitter = (IJSGoogDocEmitter) asEmitter
                .getDocEmitter();

        String nl = ASEmitterTokens.NEW_LINE.getToken();

        StringBuilder sb = null;
        MXMLScriptSpecifier scriptSpecifier = null;

        int len = node.getChildCount();
        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                sb = new StringBuilder();
                scriptSpecifier = new MXMLScriptSpecifier();

                IASNode cnode = node.getChild(i);

                docEmitter.setBufferWrite(true);
                docEmitter.emitMethodDoc((IFunctionNode) cnode, getMXMLWalker()
                        .getProject());
                sb.append(docEmitter.flushBuffer());
                
                sb.append(asEmitter.stringifyNode(cnode));

                if (i == len - 1)
                    indentPop();

                sb.append(nl);

                scriptSpecifier.fragment = sb.toString();

                scripts.add(scriptSpecifier);
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

        ps.value += node.getValue().toString();

        if (ps.valueNeedsQuotes)
            ps.value += ASEmitterTokens.SINGLE_QUOTE.getToken();
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

    private void emitHeaderDoc(String qname, String type)
    {
        write((type == "provide") ? JSGoogEmitterTokens.GOOG_PROVIDE
                : JSGoogEmitterTokens.GOOG_REQUIRE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(qname);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
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
