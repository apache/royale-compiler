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

package org.apache.royale.compiler.constants;

import com.google.common.collect.ImmutableSet;

/**
 * Collection of keywords in the ActionScript 3 language
 */
public interface IASKeywordConstants
{
    static final String ABSTRACT = "abstract";
    static final String AS = "as";
    static final String BREAK = "break";
    static final String CASE = "case";
    static final String CATCH = "catch";
    static final String CLASS = "class";
    static final String CONFIG = "config";
    static final String CONST = "const";
    static final String CONTINUE = "continue";
    static final String DELETE = "delete";
    static final String DEFAULT = "default";
    static final String DEFAULT_XML_NAMESPACE = "default xml namespace";
    static final String DO = "do";
    static final String DYNAMIC = "dynamic";
    static final String ELSE = "else";
    static final String EXTENDS = "extends";
    static final String FALSE = "false";
    static final String FINAL = "final";
    static final String FINALLY = "finally";
    static final String FOR = "for";
    static final String EACH = "each";
    static final String FUNCTION = "function";
    static final String GOTO = "goto";
    static final String GET = "get";
    static final String IF = "if";
    static final String IMPLEMENTS = "implements";
    static final String IMPORT = "import";
    static final String IN = "in";
    static final String INTERNAL = "internal";
    static final String INCLUDE = "include";
    static final String INTERFACE = "interface";
    static final String INSTANCEOF = "instanceof";
    static final String IS = "is";
    static final String NA_N = "NaN";
    static final String NAMESPACE = "namespace";
    static final String NATIVE = "native";
    static final String NEW = "new";
    static final String NULL = "null";
    static final String OVERRIDE = "override";
    static final String PACKAGE = "package";
    static final String PRIVATE = "private";
    static final String PROTECTED = "protected";
    static final String PUBLIC = "public";
    static final String SET = "set";
    static final String STATIC = "static";
    static final String SUPER = "super";
    static final String RETURN = "return";
    static final String SWITCH = "switch";
    static final String THIS = "this";
    static final String TRUE = "true";
    static final String TRY = "try";
    static final String THROW = "throw";
    static final String TYPEOF = "typeof";
    static final String USE = "use";
    static final String VAR = "var";
    static final String VIRTUAL = "virtual";
    static final String VOID = "void";
    static final String WHILE = "while";
    static final String WITH = "with";

    /**
     * A list of all the keywords found in the AS3 language
     */
    static final String[] KEYWORDS = new String[]
    {
        AS,
        BREAK,
        CASE,
        CATCH,
        CLASS,
        CONFIG,
        CONST,
        CONTINUE,
        DELETE,
        DEFAULT,
        DO,
        DYNAMIC,
        ELSE,
        EXTENDS,
        FALSE,
        FINAL,
        FINALLY,
        FOR,
        EACH,
        FUNCTION,
        GET,
        IF,
        IMPLEMENTS,
        IMPORT,
        IN,
        INTERNAL,
        INCLUDE,
        INTERFACE,
        INSTANCEOF,
        IS,
        NA_N,
        NAMESPACE,
        NATIVE,
        NEW,
        NULL,
        OVERRIDE,
        PACKAGE,
        PUBLIC,
        PRIVATE,
        PROTECTED,
        SET,
        STATIC,
        SUPER,
        RETURN,
        SWITCH,
        THIS,
        TRUE,
        TRY,
        THROW,
        TYPEOF,
        USE,
        VAR,
        VIRTUAL,
        VOID,
        WHILE,
        WITH
    };

    /**
     * An immutable set of keywords found within the the AS3 language
     */
    static final ImmutableSet<String> KEYWORD_SET = ImmutableSet.copyOf(KEYWORDS);
}
