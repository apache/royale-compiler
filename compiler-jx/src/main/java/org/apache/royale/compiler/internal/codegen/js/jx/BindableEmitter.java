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
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Set;

public class BindableEmitter extends JSSubEmitter implements
        ISubEmitter<IClassDefinition>
{

    public static String BINDABLE_DISPATCHER_NAME = "_bindingEventDispatcher";
    public static String STATIC_DISPATCHER_GETTER = "staticEventDispatcher";
    public static String EVENTS_PACKAGE ="org.apache.royale.events";
    public static String EVENTDISPATCHER = "EventDispatcher";
    public static String EVENTDISPATCHER_INTERFACE = "IEventDispatcher";
    public static String EVENT = "Event";
    public static String VALUECHANGE_EVENT = "ValueChangeEvent";
    public static String EVENT_QNAME = EVENTS_PACKAGE +"." +EVENT;
    public static String VALUECHANGE_EVENT_QNAME = EVENTS_PACKAGE +"." +VALUECHANGE_EVENT;
    public static String DISPATCHER_CLASS_QNAME = EVENTS_PACKAGE +"." + EVENTDISPATCHER;
    public static String DISPATCHER_INTERFACE_QNAME = EVENTS_PACKAGE +"." +EVENTDISPATCHER_INTERFACE;

    public BindableEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IClassDefinition definition)
    {
        if (getModel().hasBindableVars())
        {
            int statics = 0;
            if (getModel().hasStaticBindableVars()) {
                statics = emitStaticBindableVars(definition);
            }
            if (statics < getModel().getBindableVars().size())
                emitInstanceBindableVars(definition);
        }
    }

    public void emitBindableImplementsConstructorCode() {
        emitBindableImplementsConstructorCode(false);
    }

    public void emitBindableImplementsConstructorCode(boolean popIndent) {
        writeNewline("// Compiler generated Binding support implementation:");
        String dispatcherClass = getEmitter().formatQualifiedName(DISPATCHER_CLASS_QNAME);
        write(ASEmitterTokens.THIS);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(BINDABLE_DISPATCHER_NAME);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.NEW);
        write(ASEmitterTokens.SPACE);
        write(dispatcherClass);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        if (popIndent) writeNewline("",false);
        else writeNewline();

    }

    public void emitBindableExtendsConstructorCode(String qname,boolean popIndent) {
        writeNewline("// Compiler generated Binding support implementation:");
        write(getEmitter().formatQualifiedName(qname));
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
        if (popIndent) writeNewline("",false);
        else writeNewline();
    }


    public void emitBindableInterfaceMethods(IClassDefinition definition)  {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * Compiler generated");
        writeNewline(" * Binding support implementation of "+DISPATCHER_INTERFACE_QNAME);
        writeNewline("*/");

        String qname = getEmitter().formatQualifiedName(definition.getQualifiedName());
        String dispatcherClass = getEmitter().formatQualifiedName(DISPATCHER_CLASS_QNAME);
        writeNewline("/**");
        writeNewline("* @private");
        writeNewline("* @type {"+dispatcherClass+"}");
        writeNewline("*/");

        write(qname);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        write(ASEmitterTokens.MEMBER_ACCESS);
        writeNewline(BINDABLE_DISPATCHER_NAME);
        writeNewline();

        emitBindableInterfaceMethod(qname,
                new String[]{ "/**",
                        " * @export",
                        " * @param {string} type",
                        " * @param {function(?):?}",
                        " * @param {boolean=} opt_capture",
                        " * @param {Object=} opt_handlerScope",
                        " */"},
                "addEventListener",
                "type , handler , opt_capture , opt_handlerScope",
                "this."+BINDABLE_DISPATCHER_NAME+".addEventListener(type , handler , opt_capture , opt_handlerScope);");
        emitBindableInterfaceMethod(qname,
                new String[]{ "/**",
                        " * @export",
                        " * @param {string} type",
                        " * @param {function(?):?}",
                        " * @param {boolean=} opt_capture",
                        " * @param {Object=} opt_handlerScope",
                        " */"},
                "removeEventListener",
                "type , handler , opt_capture , opt_handlerScope",
                "this."+BINDABLE_DISPATCHER_NAME+".removeEventListener(type , handler , opt_capture , opt_handlerScope);");
        emitBindableInterfaceMethod(qname,
                new String[]{ "/**",
                        " * @export",
                        " * @param {"+EVENT_QNAME+"} e",
                        " * @return {boolean}",
                        " */"},
                "dispatchEvent",
                "e",
                "return this."+BINDABLE_DISPATCHER_NAME+".dispatchEvent(e);");
        emitBindableInterfaceMethod(qname,
                new String[]{ "/**",
                        " * @export",
                        " * @param {string} type",
                        " * @return {boolean}",
                        " */"},
                "hasEventListener",
                "type",
                "return this."+BINDABLE_DISPATCHER_NAME+".hasEventListener(type);");

        writeNewline("/**");
        writeNewline(" * End of Binding support implementation of "+DISPATCHER_INTERFACE_QNAME);
        writeNewline("*/");
        writeNewline();
    }

    private void emitBindableInterfaceMethod(String qualifiedClassName, String[] docLines, String methodName, String methodArgs, String methodBody) {

        for (String line : docLines)
        {
            writeNewline(line);
        }

        write(qualifiedClassName);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(methodName);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        write(methodArgs);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN,true);
        writeNewline(methodBody,false);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
    }



    private void emitInstanceBindableVars(IClassDefinition definition) {

        String qname = definition.getQualifiedName();
        write(JSGoogEmitterTokens.OBJECT);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTIES);
        write(ASEmitterTokens.PAREN_OPEN);
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        write(ASEmitterTokens.COMMA);
        write(ASEmitterTokens.SPACE);
        write("/** @lends {" + getEmitter().formatQualifiedName(qname)
                + ".prototype} */ ");
        writeNewline(ASEmitterTokens.BLOCK_OPEN);

        boolean firstTime = true;
        Set<Entry<String,BindableVarInfo>> entries = getModel().getBindableVars().entrySet();
        ArrayList<Entry<String,BindableVarInfo>> listOfEntries = new ArrayList<Entry<String,BindableVarInfo>>();
        listOfEntries.addAll(entries);
        class CustomComparator implements Comparator<Entry<String,BindableVarInfo>> {
            @Override
            public int compare(Entry<String,BindableVarInfo> o1, Entry<String,BindableVarInfo> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        }        
        Collections.sort(listOfEntries, new CustomComparator());
        for (Entry<String,BindableVarInfo> var : listOfEntries)
        {
            if (!var.getValue().isStatic) {
                if (firstTime)
                    firstTime = false;
                else
                    write(ASEmitterTokens.COMMA);

                emitBindableInstanceVarDefineProperty(var.getKey(), var.getValue(), definition);
            }

        }
        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }


    private int emitStaticBindableVars(IClassDefinition definition) {
        String qname = definition.getQualifiedName();
        int outputCount = 0;
        writeNewline();
        writeNewline("/**");
        writeNewline(" * Compiler generated");
        writeNewline(" * Static Binding support");
        writeNewline(" * @private");
        writeNewline(" * @type {"+DISPATCHER_CLASS_QNAME+"}");
        writeNewline("*/");
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(BINDABLE_DISPATCHER_NAME);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
        write(JSGoogEmitterTokens.OBJECT);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTIES);
        write(ASEmitterTokens.PAREN_OPEN);

        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.COMMA);
        write(ASEmitterTokens.SPACE);
        write("/** @lends {" + getEmitter().formatQualifiedName(qname)
                + "} */ ");
        writeNewline(ASEmitterTokens.BLOCK_OPEN);

        //writeNewline("/** @export */");
        // export above did not work in the release build for the static getter/setter bindables,
        // solution below:
        //Commented by JT, in AccessorEmitter:
        writeNewline("/** @export");
        writeNewline("  * @type {"+DISPATCHER_CLASS_QNAME+"} */");
        write(STATIC_DISPATCHER_GETTER);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SPACE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);

        write(ASEmitterTokens.GET);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN);

        indentPush();
        write(ASEmitterTokens.INDENT);
        write(ASEmitterTokens.INDENT);
        write(ASEmitterTokens.RETURN);
        write(ASEmitterTokens.SPACE);
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        writeNewline(BINDABLE_DISPATCHER_NAME);
        write(ASEmitterTokens.INDENT);
        write(ASEmitterTokens.LOGICAL_OR);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(getEmitter().formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(BINDABLE_DISPATCHER_NAME);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.NEW);
        write(ASEmitterTokens.SPACE);
        write(getEmitter().formatQualifiedName(DISPATCHER_CLASS_QNAME));
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        indentPop();
        writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        indentPop();
        write(ASEmitterTokens.BLOCK_CLOSE);

        boolean firstTime = true;
        for (Entry<String,BindableVarInfo> var : getModel().getBindableVars().entrySet())
        {
            if (var.getValue().isStatic)  {
                if (firstTime) {
                    writeNewline(ASEmitterTokens.COMMA);
                    firstTime = false;
                } else
                    writeNewline(ASEmitterTokens.COMMA, false);

                emitBindableStaticVarDefineProperty(var.getKey(), var.getValue() , definition);
                outputCount++;
            }
        }

        writeNewline("", false);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken()
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SEMICOLON.getToken());
        writeNewline();
        return outputCount;
    }


    private void emitBindableStaticVarDefineProperty(String name, BindableVarInfo info,
                                                     IClassDefinition cdef)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
        String qname = fjs.formatQualifiedName(cdef.getQualifiedName());
        // 'PropName': {

        if (info.namespace != "public") {
            writeNewline("/** @export");
            writeNewline("  * @private");
        } else {
            writeNewline("/** @export");
        }

        writeNewline("  * @type {"+convertASTypeToJS(info.type)+"} */");
        write(name);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SPACE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        write(ASEmitterTokens.GET);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        writeToken(ASEmitterTokens.RETURN);
        write(qname);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(name+"_");
        writeNewline(ASEmitterTokens.SEMICOLON, false);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken()
                + ASEmitterTokens.COMMA.getToken() );
        writeNewline();
        writeNewline(ASEmitterTokens.SET.getToken()
                + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.FUNCTION.getToken()
                + ASEmitterTokens.PAREN_OPEN.getToken() + "value"
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken(), true);
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + "var oldValue = "
                + qname
                + ASEmitterTokens.MEMBER_ACCESS.getToken()
                + name
                +"_"
                + ASEmitterTokens.SEMICOLON.getToken());
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + "if (value != oldValue) "
                + ASEmitterTokens.BLOCK_OPEN.getToken(), true);
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + ASEmitterTokens.INDENT.getToken()
                + qname
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + name
                + "_ = value;");
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + ASEmitterTokens.INDENT.getToken()
                + "var dispatcher = "
                + qname
                + ASEmitterTokens.MEMBER_ACCESS.getToken()
                + BINDABLE_DISPATCHER_NAME
                + ASEmitterTokens.SEMICOLON.getToken());
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + ASEmitterTokens.INDENT.getToken()
                +"if (dispatcher) dispatcher.dispatchEvent("+fjs.formatQualifiedName(VALUECHANGE_EVENT_QNAME)+".createUpdateEvent(");
        writeNewline(ASEmitterTokens.INDENT.getToken()
                + ASEmitterTokens.INDENT.getToken()
                + ASEmitterTokens.INDENT.getToken()
                +qname+", \"" + name + "\", oldValue, value));");
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken(),false);
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken(), false);
        write(ASEmitterTokens.BLOCK_CLOSE.getToken());
    }


    private void emitBindableInstanceVarDefineProperty(String name, BindableVarInfo info,
                                                       IClassDefinition cdef)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();
    		String qname = (info.namespace.equals("private") && getProject().getAllowPrivateNameConflicts()) ? fjs.formatPrivateName(cdef.getQualifiedName(), name) : name;
        if (info.namespace != "public") {
            writeNewline("/** @export");
            writeNewline("  * @private");
        } else {
            writeNewline("/** @export");
        }
        writeNewline("  * @type {"+convertASTypeToJS(info.type)+"} */");
        // 'PropName': {
        writeNewline(qname + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        indentPush();
        writeNewline("/** @this {"
                + fjs.formatQualifiedName(cdef.getQualifiedName()) + "} */");
        writeNewline(ASEmitterTokens.GET.getToken()
                + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.FUNCTION.getToken()
                + ASEmitterTokens.PAREN_OPEN.getToken()
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        writeNewline(ASEmitterTokens.RETURN.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + qname + "_"
                + ASEmitterTokens.SEMICOLON.getToken());
        indentPop();
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken()
                + ASEmitterTokens.COMMA.getToken());
        writeNewline();
        writeNewline("/** @this {"
                + fjs.formatQualifiedName(cdef.getQualifiedName()) + "} */");
        writeNewline(ASEmitterTokens.SET.getToken()
                + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.FUNCTION.getToken()
                + ASEmitterTokens.PAREN_OPEN.getToken() + "value"
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        writeNewline("if (value != " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + qname + "_) {");
        writeNewline("    var oldValue = " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + qname + "_"
                + ASEmitterTokens.SEMICOLON.getToken());
        writeNewline("    " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + qname
                + "_ = value;");
        writeNewline("    this.dispatchEvent("+fjs.formatQualifiedName(VALUECHANGE_EVENT_QNAME)+".createUpdateEvent(");
        writeNewline("         this, \"" + name + "\", oldValue, value));");
        writeNewline("}");
        write(ASEmitterTokens.BLOCK_CLOSE.getToken());
        write(ASEmitterTokens.BLOCK_CLOSE.getToken());
    }


    /** temp
     *  todo : figure out easy access to JSGoogDocEmitter for emitType
     *  for now, the following is copied (simplified) from JSGoogDocEmitter
     */

    protected String convertASTypeToJS(String qname)
    {
        String[] parts = qname.split(".");
        String name;
        String pname = "";
        if (parts.length >1) {
            name = parts[parts.length - 1];
            for (int i = 0; i< parts.length - 1; i++) {
                if (i > 0) pname += ".";
                pname += parts[i];
            }
        } else {
            name = qname;
        }

        String result = "";

        if (name.equals(""))
            result = ASEmitterTokens.ANY_TYPE.getToken();
        else if (name.equals(IASLanguageConstants.Class))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants.Boolean)
                || name.equals(IASLanguageConstants.String)
                || name.equals(IASLanguageConstants.Number))
            result = name.toLowerCase();
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number.toLowerCase();


        if (result == "")
            result = (pname != "" && name.indexOf(".") < 0) ? pname
                    + ASEmitterTokens.MEMBER_ACCESS.getToken() + name
                    : name;

        return result;
    }
}
