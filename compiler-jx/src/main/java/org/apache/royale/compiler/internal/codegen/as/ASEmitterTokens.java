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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.codegen.IEmitterTokens;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public enum ASEmitterTokens implements IEmitterTokens
{
    DOUBLE_QUOTE("\""), INDENT("\t"), NEW_LINE("\n"), SINGLE_QUOTE("'"), SPACE(
            " "),

    INTERNAL("internal"), PRIVATE("private"), PROTECTED("protected"),

    ANY_TYPE("*"), UNDEFINED("undefined"),

    //    int EOF = 1;
    //    int NULL_TREE_LOOKAHEAD = 3;
    //    int HIDDEN_TOKEN_COMMENT = 4;
    //    int HIDDEN_TOKEN_SINGLE_LINE_COMMENT = 5;
    //    int HIDDEN_TOKEN_STAR_ASSIGNMENT = 6;
    //    int HIDDEN_TOKEN_BUILTIN_NS = 7;
    //    int HIDDEN_TOKEN_MULTI_LINE_COMMENT = 8;
    //    int TOKEN_ASDOC_TAG = 9;
    //    int TOKEN_ASDOC_TEXT = 10;
    EACH("each"),
    //    int TOKEN_RESERVED_WORD_CONFIG = 12;
    //    int TOKEN_KEYWORD_INCLUDE = 13;
    //    int TOKEN_RESERVED_WORD_GOTO = 14;
    //    int TOKEN_IDENTIFIER = 15;
    FINALLY("finally"),
    CATCH("catch"),
    //    int TOKEN_LITERAL_STRING = 18;
    BLOCK_OPEN("{"),
    BLOCK_CLOSE("}"),
    //    int TOKEN_NAMESPACE_NAME = 21;
    //    int TOKEN_OPERATOR_NS_QUALIFIER = 22;
    //    int TOKEN_NAMESPACE_ANNOTATION = 23;
    COLON(":"),
    IMPORT("import"),
    //    int TOKEN_KEYWORD_USE = 26;
    NAMESPACE("namespace"),
    //    int TOKEN_ASDOC_COMMENT = 28;
    FINAL("final"),
    DYNAMIC("dynamic"),
    OVERRIDE("override"),
    //    int TOKEN_MODIFIER_STATIC = 32;
    //    int TOKEN_MODIFIER_NATIVE = 33;
    //    int TOKEN_MODIFIER_VIRTUAL = 34;
    MEMBER_ACCESS("."),
    //    int TOKEN_ATTRIBUTE = 36;
    SQUARE_OPEN("["),
    PACKAGE("package"),
    INTERFACE("interface"),
    EXTENDS("extends"),
    COMMA(","),
    CLASS("class"),
    IMPLEMENTS("implements"),
    FUNCTION("function"),
    PAREN_CLOSE(")"),
    PAREN_OPEN("("),
    GET("get"),
    SET("set"),
    ELLIPSIS("..."),
    VAR("var"),
    CONST("const"),
    //    int TOKEN_OPERATOR_ASSIGNMENT = 52;
    //    int TOKEN_DIRECTIVE_DEFAULT_XML = 53;
    SEMICOLON(";"),
    RETURN("return"),
    THROW("throw"),
    FOR("for"),
    IN("in"),
    DO("do"),
    WHILE("while"),
    //    int TOKEN_KEYWORD_CONTINUE = 61;
    //    int TOKEN_KEYWORD_BREAK = 62;
    WITH("with"),
    TRY("try"),
    IF("if"),
    ELSE("else"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    SUPER("super"),
    //    int TOKEN_TYPED_COLLECTION_OPEN = 71;
    //    int TOKEN_TYPED_COLLECTION_CLOSE = 72;
    GREATER_THAN(">"),
    //    int TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT = 74;
    //    int TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT = 75;
    //    int TOKEN_OPERATOR_PLUS_ASSIGNMENT = 76;
    //    int TOKEN_OPERATOR_MINUS_ASSIGNMENT = 77;
    //    int TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT = 78;
    //    int TOKEN_OPERATOR_DIVISION_ASSIGNMENT = 79;
    //    int TOKEN_OPERATOR_MODULO_ASSIGNMENT = 80;
    //    int TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT = 81;
    //    int TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT = 82;
    //    int TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT = 83;
    //    int TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT = 84;
    //    int TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT = 85;
    //    int TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT = 86;
    TERNARY("?"),
    LOGICAL_OR("||"),
    LOGICAL_AND("&&"),
    //    int TOKEN_OPERATOR_BITWISE_OR = 90;
    //    int TOKEN_OPERATOR_BITWISE_XOR = 91;
    //    int TOKEN_OPERATOR_BITWISE_AND = 92;
    EQUAL("="),
    //    int TOKEN_OPERATOR_NOT_EQUAL = 94;
    STRICT_EQUAL("==="),
    STRICT_NOT_EQUAL("!=="),
    //    int TOKEN_OPERATOR_GREATER_THAN_EQUALS = 97;
    LESS_THAN("<"),
    //    int TOKEN_OPERATOR_LESS_THAN_EQUALS = 99;
    INSTANCEOF("instanceof"),
    IS("is"),
    AS("as"),
    //    int TOKEN_OPERATOR_BITWISE_LEFT_SHIFT = 103;
    //    int TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT = 104;
    //    int TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT = 105;
    MINUS("-"),
    PLUS("+"),
    //    int TOKEN_OPERATOR_DIVISION = 108;
    //    int TOKEN_OPERATOR_MODULO = 109;
    //    int TOKEN_OPERATOR_STAR = 110;
    //    int TOKEN_KEYWORD_DELETE = 111;
    //    int TOKEN_OPERATOR_INCREMENT = 112;
    //    int TOKEN_OPERATOR_DECREMENT = 113;
    VOID("void"),
    TYPEOF("typeof"),
    //    int TOKEN_OPERATOR_BITWISE_NOT = 116;
    //    int TOKEN_OPERATOR_LOGICAL_NOT = 117;
    NULL("null"),
    TRUE("true"),
    FALSE("false"),
    THIS("this"),
    //    int TOKEN_VOID_0 = 122;
    //    int TOKEN_LITERAL_REGEXP = 123;
    //    int TOKEN_LITERAL_NUMBER = 124;
    //    int TOKEN_LITERAL_HEX_NUMBER = 125;
    SQUARE_CLOSE("]"),
    //    int TOKEN_TYPED_LITERAL_OPEN = 127;
    //    int TOKEN_TYPED_LITERAL_CLOSE = 128;
    //    int TOKEN_E4X_WHITESPACE = 129;
    //    int TOKEN_E4X_COMMENT = 130;
    //    int TOKEN_E4X_CDATA = 131;
    //    int TOKEN_E4X_PROCESSING_INSTRUCTION = 132;
    //    int TOKEN_E4X_ENTITY = 133;
    //    int TOKEN_E4X_DECIMAL_ENTITY = 134;
    //    int TOKEN_E4X_HEX_ENTITY = 135;
    //    int TOKEN_E4X_TEXT = 136;
    //    int TOKEN_E4X_STRING = 137;
    //    int TOKEN_E4X_OPEN_TAG_START = 138;
    //    int TOKEN_E4X_CLOSE_TAG_START = 139;
    //    int HIDDEN_TOKEN_E4X = 140;
    //    int TOKEN_E4X_NAME = 141;
    //    int TOKEN_E4X_TAG_END = 142;
    //    int TOKEN_E4X_EMPTY_TAG_END = 143;
    //    int TOKEN_E4X_XMLNS = 144;
    //    int TOKEN_E4X_NAME_DOT = 145;
    //    int TOKEN_E4X_DOTTED_NAME_PART = 146;
    //    int TOKEN_E4X_EQUALS = 147;
    //    int TOKEN_LITERAL_XMLLIST = 148;
    //    int TOKEN_E4X_XMLLIST_CLOSE = 149;
    //    int TOKEN_E4X_BINDING_OPEN = 150;
    //    int TOKEN_E4X_BINDING_CLOSE = 151;
    NEW("new"),
    ATSIGN("@"),
    //    int TOKEN_OPERATOR_DESCENDANT_ACCESS = 154;
    ;

    private String token;

    private ASEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
