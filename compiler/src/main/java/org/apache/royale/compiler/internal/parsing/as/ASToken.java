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

package org.apache.royale.compiler.internal.parsing.as;

import java.lang.reflect.Field;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.ICMToken;

import com.google.common.collect.ImmutableMap;

/**
 * ActionScript token (output unit of ASTokenizer/ASTokenFixer, input unit of
 * ASTreeAssembler).
 */
public class ASToken extends TokenBase implements IASToken, ASTokenTypes
{
    /**
     * Builds a new {@link ASToken}
     * 
     * @param tokenType type of token
     * @param start start of token
     * @param end end of token
     * @param line line of token
     * @param tokenText token type from ASTokenTypes
     */
    public ASToken(final int tokenType, int start, int end, int line, int column, CharSequence tokenText)
    {
        super(tokenType, start, end, line, column, tokenText);
    }

    /**
     * Copy constructor
     * 
     * @param other the ASToken to copy
     */
    public ASToken(ASToken other)
    {
        super(other);
    }

    @Override
    public ASToken clone()
    {
        return new ASToken(this);
    }

    @Override
    public ICMToken changeType(int type)
    {
        return new ASToken(type, getStart(), getEnd(), getLine(), getColumn(), getText());
    }

    /**
     * Is this an open token (e.g. "(", "{", "[")
     * 
     * @param tokenType type of the token
     * @return true iff this is an open token
     */
    public static final boolean isOpenToken(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_PAREN_OPEN:
            case TOKEN_BLOCK_OPEN:
            case TOKEN_SQUARE_OPEN:
            case TOKEN_E4X_BINDING_OPEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Unknown open block token type.
     * 
     * @see #getOpenBlockTokenType()
     */
    public static final int UNKNOWN_OPEN_BLOCK_TOKEN_TYPE = -1;

    public static final int getOpenBlockTokenType(int tokenType)
    {

        switch (tokenType)
        {
            case TOKEN_KEYWORD_CATCH:
            case TOKEN_KEYWORD_DO:
            case TOKEN_KEYWORD_WHILE:
            case TOKEN_KEYWORD_FOR:
            case TOKEN_RESERVED_WORD_EACH:
            case TOKEN_KEYWORD_WITH:
            case TOKEN_KEYWORD_ELSE:
            case TOKEN_KEYWORD_IF:
            case TOKEN_KEYWORD_SWITCH:
            case TOKEN_KEYWORD_CASE:
            case TOKEN_KEYWORD_DEFAULT:
            case TOKEN_KEYWORD_TRY:
            case TOKEN_KEYWORD_FINALLY:
                return TOKEN_BLOCK_OPEN;
            case TOKEN_KEYWORD_CLASS:
                return TOKEN_BLOCK_OPEN;
            case TOKEN_KEYWORD_FUNCTION:
                return TOKEN_BLOCK_OPEN;
            case TOKEN_KEYWORD_INTERFACE:
                return TOKEN_BLOCK_OPEN;
            case TOKEN_KEYWORD_PACKAGE:
                return TOKEN_BLOCK_OPEN;
        }

        return UNKNOWN_OPEN_BLOCK_TOKEN_TYPE;
    }

    public final int getOpenBlockTokenType()
    {
        return getOpenBlockTokenType(type);
    }

    /**
     * Is this an open block token
     * 
     * @return true if this is an open block token
     */
    public final boolean isOpenBlockToken()
    {
        return ASToken.isOpenBlockToken(getType());
    }

    public static final boolean isOpenBlockToken(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_BLOCK_OPEN:
            case TOKEN_E4X_BINDING_OPEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Is this an open token (e.g. "(", "{", "[")
     * 
     * @return true iff this is an open token
     */
    public final boolean isOpenToken()
    {
        return ASToken.isOpenToken(getType());
    }

    /**
     * Is this a close token (e.g. ")", "}", "]")
     * 
     * @param tokenType type of the token
     * @return true if this is an close token
     */
    public static final boolean isCloseToken(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_PAREN_CLOSE:
            case TOKEN_BLOCK_CLOSE:
            case TOKEN_SQUARE_CLOSE:
            case TOKEN_E4X_BINDING_CLOSE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Is this a close block token
     * 
     * @return true if this is a close block token
     */
    public final boolean isCloseBlockToken()
    {
        return ASToken.isCloseBlockToken(getType());
    }

    /**
     * Is this a close token
     * 
     * @param tokenType type of the token
     * @return true if this is an close token
     */
    public static final boolean isCloseBlockToken(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_BLOCK_CLOSE:
            case TOKEN_E4X_BINDING_CLOSE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Is this a close token (e.g. ")", "}", "]")
     * 
     * @return true iff this is an close token
     */
    public final boolean isCloseToken()
    {
        return ASToken.isCloseToken(getType());
    }

    /**
     * If this is an open token, get the matching close token. If this is a
     * close token, get the matching open token
     * 
     * @param tokenType type of the token
     * @return balancing token (or 0 if this isn't an open/close token)
     */
    public static final int getBalancingToken(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_PAREN_OPEN:
                return TOKEN_PAREN_CLOSE;
            case TOKEN_PAREN_CLOSE:
                return TOKEN_PAREN_OPEN;
            case TOKEN_BLOCK_OPEN:
                return TOKEN_BLOCK_CLOSE;
            case TOKEN_BLOCK_CLOSE:
                return TOKEN_BLOCK_OPEN;
            case TOKEN_TYPED_COLLECTION_OPEN:
                return TOKEN_TYPED_COLLECTION_CLOSE;
            case TOKEN_TYPED_LITERAL_OPEN:
                return TOKEN_TYPED_LITERAL_CLOSE;
            case TOKEN_TYPED_LITERAL_CLOSE:
                return TOKEN_TYPED_LITERAL_OPEN;
            case TOKEN_SQUARE_OPEN:
                return TOKEN_SQUARE_CLOSE;
            case TOKEN_SQUARE_CLOSE:
                return TOKEN_SQUARE_OPEN;
            case TOKEN_TYPED_COLLECTION_CLOSE:
                return TOKEN_TYPED_COLLECTION_OPEN;
            case TOKEN_E4X_BINDING_OPEN:
                return TOKEN_E4X_BINDING_CLOSE;
            case TOKEN_E4X_BINDING_CLOSE:
                return TOKEN_E4X_BINDING_OPEN;
            default:
                return 0;
        }
    }

    /**
     * If this is an open token, get the matching close token. If this is a
     * close token, get the matching open token
     * 
     * @return balancing token (or 0 if this isn't an open/close token)
     */
    public final int getBalancingToken()
    {
        return ASToken.getBalancingToken(getType());
    }

    /**
     * Determine whether or not this token is capable of causing a containment
     * problem. If a token can cause canContain to return false when it's passed
     * in as containedType, then it must return true. This is used to improve
     * the performance of ASTokenFixer.locateBottommostContainerProblem.
     * 
     * @param tokenType type of the token
     * @return true if the token can cause canContain to return false.
     */
    private static final boolean canCauseContainmentProblems(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_KEYWORD_PACKAGE:
            case TOKEN_KEYWORD_CLASS:
            case TOKEN_KEYWORD_INTERFACE:
            case TOKEN_KEYWORD_FUNCTION:
            case TOKEN_KEYWORD_VAR:
            case TOKEN_BLOCK_OPEN:
            case TOKEN_BLOCK_CLOSE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determine whether or not this token is capable of causing a containment
     * problem. If a token can cause canContain to return false when it's passed
     * in as containedType, then it must return true. This is used to improve
     * the performance of ASTokenFixer.locateBottommostContainerProblem.
     * 
     * @return true iff the token can cause canContain to return false.
     */
    public final boolean canCauseContainmentProblems()
    {
        return ASToken.canCauseContainmentProblems(getType());
    }

    public final boolean isOperator()
    {
        return ASToken.isOperator(this);
    }

    private static final boolean isOperator(ASToken token)
    {
        switch (token.getType())
        {
            case TOKEN_OPERATOR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_NOT:
            case TOKEN_OPERATOR_LOGICAL_NOT:
            case TOKEN_OPERATOR_MINUS:
            case TOKEN_OPERATOR_PLUS:
            case TOKEN_OPERATOR_MEMBER_ACCESS:
            case TOKEN_OPERATOR_NS_QUALIFIER:
            case TOKEN_OPERATOR_STAR:
            case TOKEN_OPERATOR_DIVISION:
            case TOKEN_OPERATOR_MODULO:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
            case TOKEN_OPERATOR_LESS_THAN:
            case TOKEN_OPERATOR_GREATER_THAN:
            case TOKEN_OPERATOR_LESS_THAN_EQUALS:
            case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
            case TOKEN_OPERATOR_EQUAL:
            case TOKEN_OPERATOR_NOT_EQUAL:
            case TOKEN_OPERATOR_STRICT_EQUAL:
            case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
            case TOKEN_OPERATOR_BITWISE_AND:
            case TOKEN_OPERATOR_BITWISE_XOR:
            case TOKEN_OPERATOR_BITWISE_OR:
            case TOKEN_OPERATOR_LOGICAL_AND:
            case TOKEN_OPERATOR_LOGICAL_OR:
            case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
            case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
            case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT:
            case TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT:
            case TOKEN_KEYWORD_IN:
            case TOKEN_OPERATOR_TERNARY:
            case TOKEN_OPERATOR_DECREMENT:
            case TOKEN_OPERATOR_INCREMENT:
            case TOKEN_KEYWORD_AS:
            case TOKEN_KEYWORD_IS:
            case TOKEN_KEYWORD_DELETE:
            case TOKEN_KEYWORD_TYPEOF:
            case TOKEN_KEYWORD_INSTANCEOF:
            case TOKEN_OPERATOR_ATSIGN:
            case TOKEN_OPERATOR_DESCENDANT_ACCESS:
                return true;
            default:
                return false;
        }
    }

    public final boolean isE4X()
    {
        return ASToken.isE4X(type);
    }

    public static final boolean isE4X(final int type)
    {
        switch (type)
        {
            case TOKEN_E4X_CDATA:
            case TOKEN_E4X_COMMENT:
            case TOKEN_E4X_OPEN_TAG_START:
            case TOKEN_E4X_CLOSE_TAG_START:
            case TOKEN_E4X_TAG_END:
            case TOKEN_E4X_EMPTY_TAG_END:
            case TOKEN_E4X_NAME:
            case TOKEN_E4X_WHITESPACE:
            case TOKEN_E4X_DOTTED_NAME_PART:
            case TOKEN_E4X_NAME_DOT:
            case TOKEN_E4X_EQUALS:
            case HIDDEN_TOKEN_E4X:
            case TOKEN_E4X_PROCESSING_INSTRUCTION:
            case TOKEN_E4X_STRING:
            case TOKEN_E4X_XMLNS:
            case TOKEN_E4X_ENTITY:
            case TOKEN_E4X_DECIMAL_ENTITY:
            case TOKEN_E4X_HEX_ENTITY:
            case TOKEN_E4X_TEXT:
            case TOKEN_LITERAL_XMLLIST:
            case TOKEN_E4X_XMLLIST_CLOSE:
                return true;
        }
        return false;
    }

    public final boolean isLiteral()
    {
        return ASToken.isLiteral(type);
    }

    private static final boolean isLiteral(final int type)
    {
        switch (type)
        {
            case TOKEN_KEYWORD_TRUE:
            case TOKEN_KEYWORD_FALSE:
            case TOKEN_LITERAL_NUMBER:
            case TOKEN_VOID_0:
            case TOKEN_KEYWORD_NULL:
            case TOKEN_LITERAL_HEX_NUMBER:
            case TOKEN_LITERAL_REGEXP:
            case TOKEN_LITERAL_STRING:
            case TOKEN_LITERAL_XMLLIST:
            case TOKEN_E4X_XMLLIST_CLOSE:
                return true;
            default:
                return false;
        }
    }

    public static final boolean isDefinitionKeyword(final int type)
    {
        switch (type)
        {
            case TOKEN_KEYWORD_CLASS:
            case TOKEN_KEYWORD_FUNCTION:
            case TOKEN_KEYWORD_INTERFACE:
            case TOKEN_RESERVED_WORD_NAMESPACE:
            case TOKEN_KEYWORD_VAR:
            case TOKEN_KEYWORD_CONST:
                return true;
        }
        return false;
    }

    public static boolean isModifier(final int type)
    {
        switch (type)
        {
            case TOKEN_MODIFIER_DYNAMIC:
            case TOKEN_MODIFIER_FINAL:
            case TOKEN_MODIFIER_NATIVE:
            case TOKEN_MODIFIER_OVERRIDE:
            case TOKEN_MODIFIER_STATIC:
            case TOKEN_MODIFIER_ABSTRACT:
                return true;
        }
        return false;
    }

    public final boolean isModifier()
    {
        return isModifier(type);
    }

    public static final boolean isStatementKeyword(final int type)
    {
        switch (type)
        {
            case TOKEN_KEYWORD_FOR:
            case TOKEN_KEYWORD_WHILE:
            case TOKEN_KEYWORD_WITH:
            case TOKEN_KEYWORD_FINALLY:
            case TOKEN_KEYWORD_CASE:
            case TOKEN_KEYWORD_SWITCH:
            case TOKEN_KEYWORD_DO:
            case TOKEN_KEYWORD_DEFAULT:
            case TOKEN_KEYWORD_BREAK:
            case TOKEN_KEYWORD_CONTINUE:
            case TOKEN_KEYWORD_IF:
            case TOKEN_KEYWORD_ELSE:
                return true;
        }
        return false;
    }

    public final boolean isStatementKeyword()
    {
        return ASToken.isStatementKeyword(type);
    }

    /**
     * Check if the token is a contextual reserved word.
     * 
     * @return True if the token is a contextual reserved word.
     */
    public final boolean isContextualReservedWord()
    {
        switch (type)
        {
            case TOKEN_MODIFIER_DYNAMIC:
            case TOKEN_MODIFIER_FINAL:
            case TOKEN_MODIFIER_NATIVE:
            case TOKEN_MODIFIER_OVERRIDE:
            case TOKEN_MODIFIER_STATIC:
            case TOKEN_MODIFIER_VIRTUAL:
            case TOKEN_MODIFIER_ABSTRACT:
            case TOKEN_RESERVED_WORD_GET:
            case TOKEN_RESERVED_WORD_SET:
            case TOKEN_RESERVED_WORD_NAMESPACE:
            case TOKEN_RESERVED_WORD_CONFIG:
            case TOKEN_RESERVED_WORD_EXTENDS:
            case TOKEN_RESERVED_WORD_EACH:
            case TOKEN_RESERVED_WORD_IMPLEMENTS:
            case TOKEN_RESERVED_WORD_GOTO:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns whether this token represents an ActionScript keyword.
     * <p>
     * For missing "private", "protected", "public", "internal", see
     * https://bugs.adobe.com/jira/browse/ASLSPEC-8
     * 
     * @return True if the token is a reserved keyword.
     */
    public final boolean isKeyword()
    {
        switch (type)
        {
            case TOKEN_KEYWORD_AS:
            case TOKEN_KEYWORD_BREAK:
            case TOKEN_KEYWORD_CASE:
            case TOKEN_KEYWORD_CATCH:
            case TOKEN_KEYWORD_CLASS:
            case TOKEN_KEYWORD_CONST:
            case TOKEN_KEYWORD_CONTINUE:
            case TOKEN_KEYWORD_DEFAULT:
            case TOKEN_KEYWORD_DELETE:
            case TOKEN_KEYWORD_DO:
            case TOKEN_KEYWORD_ELSE:
            case TOKEN_KEYWORD_FALSE:
            case TOKEN_KEYWORD_FINALLY:
            case TOKEN_KEYWORD_FOR:
            case TOKEN_KEYWORD_FUNCTION:
            case TOKEN_KEYWORD_IF:
            case TOKEN_KEYWORD_IMPORT:
            case TOKEN_KEYWORD_IN:
            case TOKEN_KEYWORD_INCLUDE:
            case TOKEN_KEYWORD_INSTANCEOF:
            case TOKEN_KEYWORD_INTERFACE:
            case TOKEN_KEYWORD_IS:
            case TOKEN_KEYWORD_NEW:
            case TOKEN_KEYWORD_NULL:
            case TOKEN_KEYWORD_PACKAGE:
            case TOKEN_KEYWORD_RETURN:
            case TOKEN_KEYWORD_SUPER:
            case TOKEN_KEYWORD_SWITCH:
            case TOKEN_KEYWORD_THIS:
            case TOKEN_KEYWORD_THROW:
            case TOKEN_KEYWORD_TRUE:
            case TOKEN_KEYWORD_TRY:
            case TOKEN_KEYWORD_TYPEOF:
            case TOKEN_KEYWORD_USE:
            case TOKEN_KEYWORD_VAR:
            case TOKEN_KEYWORD_VOID:
            case TOKEN_KEYWORD_WHILE:
            case TOKEN_KEYWORD_WITH:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the token is either a "keyword" or a "contextual reserved word".
     * 
     * @return True if the token is either a "keyword" or a
     * "contextual reserved word".
     */
    public final boolean isKeywordOrContextualReservedWord()
    {
        return isKeyword() || isContextualReservedWord();
    }

    public static final boolean canExistInMetadata(final int tokenType)
    {
        switch (tokenType)
        {
            //this is the valid set of tokens contained within metadata
            case TOKEN_PAREN_OPEN:
            case TOKEN_PAREN_CLOSE:
            case TOKEN_IDENTIFIER:
            case TOKEN_COMMA:
            case TOKEN_SQUARE_CLOSE:
            case TOKEN_OPERATOR_ASSIGNMENT:
            case TOKEN_KEYWORD_INCLUDE:
            case TOKEN_KEYWORD_DEFAULT:
            case TOKEN_KEYWORD_TRUE:
            case TOKEN_KEYWORD_FALSE:
            case TOKEN_LITERAL_HEX_NUMBER:
            case TOKEN_KEYWORD_NULL:
            case TOKEN_LITERAL_NUMBER:
            case TOKEN_LITERAL_STRING:
            case TOKEN_OPERATOR_NS_QUALIFIER:
                return true;
        }
        return false;
    }

    public final boolean canExistInMetadata()
    {
        return canExistInMetadata(type);
    }

    public static final boolean canFollowMetadata(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_IDENTIFIER:
            case TOKEN_ASDOC_COMMENT:
            case HIDDEN_TOKEN_MULTI_LINE_COMMENT:
            case HIDDEN_TOKEN_SINGLE_LINE_COMMENT:
            case TOKEN_SQUARE_OPEN:
                return true; //we're a valid following token for metadata
        }
        return false;
    }

    public final boolean canFollowMetadata()
    {
        return canFollowMetadata(type);
    }

    public static final boolean canFollowUserNamespaceAnnotation(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_KEYWORD_FUNCTION:
            case TOKEN_KEYWORD_VAR:
            case TOKEN_KEYWORD_CONST:
            case TOKEN_MODIFIER_DYNAMIC:
            case TOKEN_MODIFIER_FINAL:
            case TOKEN_MODIFIER_NATIVE:
            case TOKEN_MODIFIER_OVERRIDE:
            case TOKEN_MODIFIER_STATIC:
            case TOKEN_MODIFIER_ABSTRACT:
                return true;
        }
        return false;
    }

    public final boolean canFollowUserNamespace()
    {
        return canFollowUserNamespaceAnnotation(type);
    }

    /**
     * @return True if a "function" keyword after this token should be
     * considered an anonymous function (closure).
     */
    public final boolean canPreceedAnonymousFunction()
    {
        switch (type)
        {
            case TOKEN_OPERATOR_ASSIGNMENT: // x = function () {...};
            case TOKEN_COMMA: // foo(x, function() {...});
            case TOKEN_PAREN_OPEN: // foo(function() {...}, ...);
            case TOKEN_SQUARE_OPEN: // x = [ function() {...}, ...];
            case TOKEN_KEYWORD_NEW: // x = new function() {...};
            case TOKEN_KEYWORD_RETURN: // return new function() {...};
            case TOKEN_COLON: // obj = { fn: function() {...} };
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true if the token type can come before the operators
     * <code>+</code> or <code>-</code>
     * 
     * @param tokenType the token type to check
     * @return true if able to come before
     */
    public static final boolean canPreceedSignedOperator(final int tokenType)
    {
        if (isLiteral(tokenType))
            return true;

        switch (tokenType)
        {
            case TOKEN_PAREN_CLOSE:
            case TOKEN_SQUARE_CLOSE:
            case TOKEN_IDENTIFIER:
            case TOKEN_OPERATOR_DECREMENT:
            case TOKEN_OPERATOR_INCREMENT:
            case TOKEN_KEYWORD_THIS:
            case TOKEN_KEYWORD_SUPER:
                return true;
        }
        return false;
    }

    /**
     * Returns true if the token can come before the operators <code>+</code> or
     * <code>-</code>
     * 
     * @return true if able to come before
     */
    public final boolean canPreceedSignedOperator()
    {
        return canPreceedSignedOperator(type);
    }

    public final boolean canPreceedE4X()
    {
        return canPreceedE4X(type);
    }

    public static final boolean canPreceedE4X(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_SEMICOLON:
            case TOKEN_PAREN_OPEN:
            case TOKEN_COMMA:
            case TOKEN_OPERATOR_ASSIGNMENT:
            case TOKEN_KEYWORD_RETURN:
            case TOKEN_KEYWORD_THROW:
            case TOKEN_SQUARE_OPEN: //array/vector initializer open 

                // case TOKEN_OPERATOR_BINARY:
            case TOKEN_OPERATOR_DIVISION:
            case TOKEN_OPERATOR_MODULO:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
            case TOKEN_OPERATOR_LESS_THAN:
            case TOKEN_OPERATOR_GREATER_THAN:
            case TOKEN_OPERATOR_LESS_THAN_EQUALS:
            case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
            case TOKEN_OPERATOR_EQUAL:
            case TOKEN_OPERATOR_NOT_EQUAL:
            case TOKEN_OPERATOR_STRICT_EQUAL:
            case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
            case TOKEN_OPERATOR_BITWISE_AND:
            case TOKEN_OPERATOR_BITWISE_XOR:
            case TOKEN_OPERATOR_BITWISE_OR:
            case TOKEN_OPERATOR_LOGICAL_AND:
            case TOKEN_OPERATOR_LOGICAL_OR:

            case TOKEN_OPERATOR_PLUS:
            case TOKEN_OPERATOR_MINUS:

            case TOKEN_OPERATOR_BITWISE_NOT:
            case TOKEN_OPERATOR_LOGICAL_NOT:
            case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
            case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
            case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:

                // Ternary("?") and Colon(":") are for XML literals inside ternary expressions:
                // For example:  boolValue ? <True></True> : <False></False>
            case TOKEN_OPERATOR_TERNARY:
            case TOKEN_COLON:

            case -1: //nothing
                return true;
        }
        return false;
    }

    /**
     * Check if regular-expression literal can follow a token of given type.
     * 
     * @param tokenType Token type
     * @return True if {@link ASTokenTypes#TOKEN_LITERAL_REGEXP} can follow the
     * given token type.
     */
    public static final boolean canPreceedRegex(final int tokenType)
    {
        switch (tokenType)
        {
            case TOKEN_PAREN_OPEN:
            case TOKEN_SQUARE_OPEN: // i.e. var a:Array = [ /foo/, /bar/ ] ;
            case TOKEN_COMMA:
            case TOKEN_COLON:
            case TOKEN_OPERATOR_ASSIGNMENT:
            case TOKEN_KEYWORD_RETURN:
            case TOKEN_KEYWORD_THROW:
            case TOKEN_SEMICOLON:
            case TOKEN_BLOCK_OPEN:
            case TOKEN_BLOCK_CLOSE:
            case TOKEN_OPERATOR_DIVISION:
            case TOKEN_OPERATOR_MODULO:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
            case TOKEN_OPERATOR_LESS_THAN:
            case TOKEN_OPERATOR_GREATER_THAN:
            case TOKEN_OPERATOR_LESS_THAN_EQUALS:
            case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
            case TOKEN_OPERATOR_EQUAL:
            case TOKEN_OPERATOR_NOT_EQUAL:
            case TOKEN_OPERATOR_STRICT_EQUAL:
            case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
            case TOKEN_OPERATOR_BITWISE_AND:
            case TOKEN_OPERATOR_BITWISE_XOR:
            case TOKEN_OPERATOR_BITWISE_OR:
            case TOKEN_OPERATOR_LOGICAL_AND:
            case TOKEN_OPERATOR_LOGICAL_OR:
            case TOKEN_OPERATOR_PLUS:
            case TOKEN_OPERATOR_MINUS:
            case TOKEN_OPERATOR_BITWISE_NOT:
            case TOKEN_OPERATOR_LOGICAL_NOT:
            case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
            case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
            case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
            case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                // The following ones are RegEx after a unary operator.
            case TOKEN_KEYWORD_VOID:
            case TOKEN_KEYWORD_TYPEOF:
            case TOKEN_KEYWORD_DELETE:
            case TOKEN_OPERATOR_INCREMENT:
            case TOKEN_OPERATOR_DECREMENT:
            case -1: //no previous token
                return true;
        }
        return false;
    }

    private static ImmutableMap<Integer, String> tokenNames;

    /**
     * Create a lookup map for token names using reflections.
     */
    private static synchronized void initializeTokenNames()
    {
        if (tokenNames != null)
            return;

        final ImmutableMap.Builder<Integer, String> builder = new ImmutableMap.Builder<Integer, String>();
        final ASToken token = new ASToken(UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "");
        for (final Field field : ASTokenTypes.class.getFields())
        {
            if (field.getType().equals(Integer.TYPE))
            {
                int tokenType;
                try
                {
                    tokenType = field.getInt(token);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                builder.put(tokenType, field.getName());
            }
        }
        tokenNames = builder.build();
    }

    /**
     * Get the display string for the token type
     * 
     * @return display string for the token type
     */
    @Override
    protected String getTypeString()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
    		System.out.println("ASToken waiting for lock for " + getSourcePath() + ":" + getLine() + ":" + getColumn());
        initializeTokenNames();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
    		System.out.println("ASToken done with lock for " + getSourcePath() + ":" + getLine() + ":" + getColumn());
        return tokenNames.get(getType());
    }

    @Override
    public ASTokenKind getTokenKind()
    {
        switch (getType())
        {
            case EOF:
                return ASTokenKind.EOF;
            case HIDDEN_TOKEN_COMMENT:
            case HIDDEN_TOKEN_MULTI_LINE_COMMENT:
            case HIDDEN_TOKEN_SINGLE_LINE_COMMENT:
            case TOKEN_ASDOC_COMMENT:
                return ASTokenKind.COMMENT;
            case TOKEN_MODIFIER_DYNAMIC:
            case TOKEN_MODIFIER_FINAL:
            case TOKEN_MODIFIER_NATIVE:
            case TOKEN_MODIFIER_OVERRIDE:
            case TOKEN_MODIFIER_STATIC:
            case TOKEN_MODIFIER_ABSTRACT:
                return ASTokenKind.MODIFIER;
            case HIDDEN_TOKEN_BUILTIN_NS:
            case TOKEN_NAMESPACE_ANNOTATION:
                return ASTokenKind.NAMESPACE;
            case TOKEN_DIRECTIVE_DEFAULT_XML:
                return ASTokenKind.DEFAULT_XML_STATEMENT;
            case TOKEN_BLOCK_CLOSE:
                return ASTokenKind.SCOPE_CLOSE;
            case TOKEN_BLOCK_OPEN:
                return ASTokenKind.SCOPE_OPEN;
            case TOKEN_LITERAL_STRING:
                return ASTokenKind.STRING_LITERAL;
            case TOKEN_LITERAL_NUMBER:
            case TOKEN_LITERAL_HEX_NUMBER:
                return ASTokenKind.NUMBER_LITERAL;
            case TOKEN_KEYWORD_TRUE:
            case TOKEN_KEYWORD_FALSE:
                return ASTokenKind.BOOLEAN_LITERAL;
            case TOKEN_VOID_0:
            case TOKEN_KEYWORD_NULL:
                return ASTokenKind.OBJECT_LITERAL;
            case TOKEN_LITERAL_REGEXP:
                return ASTokenKind.REGEX_LITERAL;
            case TOKEN_LITERAL_XMLLIST:
            case TOKEN_E4X_XMLLIST_CLOSE:
                return ASTokenKind.XMLLIST_LITERAL;
            case TOKEN_PAREN_OPEN:
                return ASTokenKind.PAREN_OPEN;
            case TOKEN_PAREN_CLOSE:
                return ASTokenKind.PAREN_CLOSE;
            case TOKEN_SQUARE_OPEN:
                return ASTokenKind.BRACKET_OPEN;
            case TOKEN_SQUARE_CLOSE:
                return ASTokenKind.BRACKET_CLOSE;
            case TOKEN_ATTRIBUTE:
                return ASTokenKind.METADATA;
            case TOKEN_SEMICOLON:
                return ASTokenKind.SEMICOLON;
            case TOKEN_COLON:
                return ASTokenKind.COLON;
            case TOKEN_COMMA:
                return ASTokenKind.OPERATOR;
            case TOKEN_E4X_BINDING_CLOSE:
                return ASTokenKind.E4X_BINDING_CLOSE;
            case TOKEN_E4X_BINDING_OPEN:
                return ASTokenKind.E4X_BINDING_OPEN;
            case TOKEN_TYPED_COLLECTION_CLOSE:
            case TOKEN_TYPED_LITERAL_CLOSE:
                return ASTokenKind.TYPED_COLLECTION_CLOSE;
            case TOKEN_TYPED_COLLECTION_OPEN:
            case TOKEN_TYPED_LITERAL_OPEN:
                return ASTokenKind.TYPED_COLLECTION_OPEN;
            case TOKEN_ELLIPSIS:
                return ASTokenKind.OPERATOR;
            case TOKEN_KEYWORD_INCLUDE:
                return ASTokenKind.INCLUDE;
            default:
                if (isKeywordOrContextualReservedWord())
                    return ASTokenKind.KEYWORD;
                if (isOperator())
                    return ASTokenKind.OPERATOR;
                if (getType() == TOKEN_IDENTIFIER) //handle this after keywords
                    return ASTokenKind.IDENTIFIER;
                if (isE4X())
                    return ASTokenKind.XML_LITERAL;
        }
        return ASTokenKind.UNKNOWN;
    }

    /**
     * Get {@code IASToken.ASTokenKind} from a given token type.
     * 
     * @param type AS token type.
     * @return Token kind.
     */
    public static synchronized ASTokenKind typeToKind(final int type)
    {
        TOKEN_TYPE_TO_KIND_CONVERTER.setType(type);
        return TOKEN_TYPE_TO_KIND_CONVERTER.getTokenKind();
    }

    /**
     * Surrogate token used by {@link #typeToKind(int)} so that we don't have to
     * create a new {@code ASToken} object every time we convert.
     */
    private static final ASToken TOKEN_TYPE_TO_KIND_CONVERTER =
            new ASToken(ASTokenTypes.EOF, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "");

    @Override
    public final boolean isMultiLineComment()
    {
        return getType() == HIDDEN_TOKEN_MULTI_LINE_COMMENT || getType() == TOKEN_ASDOC_COMMENT;
    }

}
