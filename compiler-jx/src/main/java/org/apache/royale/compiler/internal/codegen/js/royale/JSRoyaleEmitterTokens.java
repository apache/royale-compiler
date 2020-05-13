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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.codegen.IEmitterTokens;

/**
 * @author Erik de Bruin
 */
public enum JSRoyaleEmitterTokens implements IEmitterTokens
{
    ROYALE_CLASS_INFO("ROYALE_CLASS_INFO"),
    ROYALE_REFLECTION_INFO("ROYALE_REFLECTION_INFO"),
    ROYALE_REFLECTION_INFO_GET_SET("get_set"),
    ROYALE_REFLECTION_INFO_INITIAL_STATICS("ROYALE_INITIAL_STATICS"),
    ROYALE_REFLECTION_INFO_COMPILE_TIME_FLAGS("ROYALE_COMPILE_FLAGS"),
    ROYALE_CLASS_INFO_KIND("kind"),
    ROYALE_CLASS_INFO_CLASS_KIND("class"),
    ROYALE_CLASS_INFO_INTERFACE_KIND("interface"),
    ROYALE_CLASS_INFO_IS_DYNAMIC("isDynamic"),
    ROYALE_SYNTH_TAG_FIELD_NAME("SYNTH_TAG_FIELD"),
    GOOG_EXPORT_PROPERTY("goog.exportProperty"),
    GOOG_EXPORT_SYMBOL("goog.exportSymbol"),
    INDENT("  "),
    INTERFACES("interfaces"),
    LANGUAGE_QNAME("org.apache.royale.utils.Language"),
    NAME("name"),
    NAMES("names"),
    QNAME("qName"),
    UNDERSCORE("_"),
    EMIT_COERCION("@royaleemitcoercion"),
    EXTERNS("@externs"),
    IGNORE_COERCION("@royaleignorecoercion"),
    IGNORE_IMPORT("@royaleignoreimport"),
    IGNORE_STRING_COERCION("@royalenoimplicitstringconversion"),
    SUPPRESS_EXPORT("@royalesuppressexport"),
    SUPPRESS_CLOSURE("@royalesuppressclosure"),
    SUPPRESS_PUBLIC_VAR_WARNING("@royalesuppresspublicvarwarning"),
    SUPPRESS_COMPLEX_IMPLICIT_COERCION("@royalesuppresscompleximplicitcoercion"),
    SUPPRESS_RESOLVE_UNCERTAIN("@royalesuppressresolveuncertain"),
    SUPPRESS_VECTOR_INDEX_CHECK("@royalesuppressvectorindexcheck"),
    DEBUG_COMMENT("@royaledebug"),
    DEBUG_RETURN("if(!goog.DEBUG)return;"),
    PREINCREMENT("preincrement"),
    PREDECREMENT("predecrement"),
    POSTINCREMENT("postincrement"),
    POSTDECREMENT("postdecrement"),
    SUPERGETTER("superGetter"),
    SUPERSETTER("superSetter"),
    GETTER_PREFIX("get__"),
    SETTER_PREFIX("set__"),
    BINDABLE_PREFIX("bindable__"),
    CLOSURE_FUNCTION_NAME("org.apache.royale.utils.Language.closure"),
    SKIP_AS_COERCIONS("skipAsCoercions"),
    SKIP_FUNCTION_COERCIONS("skipFunctionCoercions"),
    JSX("JSX"),
    VECTOR(LANGUAGE_QNAME.getToken() + ".Vector"),
    SYNTH_TYPE(LANGUAGE_QNAME.getToken() + ".synthType"),
    SYNTH_VECTOR(LANGUAGE_QNAME.getToken() + ".synthVector"),
    VECTOR_INDEX_CHECK_METHOD_NAME(LANGUAGE_QNAME.getToken() + ".CHECK_INDEX"),
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
