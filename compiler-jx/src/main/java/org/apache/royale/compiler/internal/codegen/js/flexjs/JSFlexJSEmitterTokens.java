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

import org.apache.flex.compiler.codegen.IEmitterTokens;

/**
 * @author Erik de Bruin
 */
public enum JSRoyaleEmitterTokens implements IEmitterTokens
{
    FLEXJS_CLASS_INFO("FLEXJS_CLASS_INFO"),
    FLEXJS_REFLECTION_INFO("FLEXJS_REFLECTION_INFO"),
    FLEXJS_CLASS_INFO_KIND("kind"),
    FLEXJS_CLASS_INFO_CLASS_KIND("class"),
    FLEXJS_CLASS_INFO_INTERFACE_KIND("interface"),
    GOOG_EXPORT_PROPERTY("goog.exportProperty"),
    GOOG_EXPORT_SYMBOL("goog.exportSymbol"),
    INDENT("  "),
    INTERFACES("interfaces"),
    LANGUAGE_QNAME("org.apache.flex.utils.Language"),
    NAME("name"),
    NAMES("names"),
    QNAME("qName"),
    UNDERSCORE("_"),
    EMIT_COERCION("@flexjsemitcoercion"),
    EXTERNS("@externs"),
    IGNORE_COERCION("@flexjsignorecoercion"),
    IGNORE_IMPORT("@flexjsignoreimport"),
    IGNORE_STRING_COERCION("@flexjsnoimplicitstringconversion"),
    PREINCREMENT("preincrement"),
    PREDECREMENT("predecrement"),
    POSTINCREMENT("postincrement"),
    POSTDECREMENT("postdecrement"),
    SUPERGETTER("superGetter"),
    SUPERSETTER("superSetter"),
    GETTER_PREFIX("get__"),
    SETTER_PREFIX("set__"),
    BINDABLE_PREFIX("bindable__"),
    CLOSURE_FUNCTION_NAME("org.apache.flex.utils.Language.closure"),
    SKIP_AS_COERCIONS("skipAsCoercions"),
    SKIP_FUNCTION_COERCIONS("skipFunctionCoercions"),
    JSX("JSX"),
    VECTOR("org.apache.flex.utils.Language.Vector"),
    ;

    private String token;

    private JSRoyaleEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
