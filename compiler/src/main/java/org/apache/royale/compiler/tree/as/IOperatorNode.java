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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.constants.IASKeywordConstants;

/**
 * An AST node representing any kind of operator expression.
 * <p>
 * The relevant subinterfaces are {@link IUnaryOperatorNode},
 * {@link IBinaryOperatorNode}, and {@link ITernaryOperatorNode}.
 */
public interface IOperatorNode extends IExpressionNode
{
    /**
     * An enum that lists the types of expressions supported
     */
    static enum ExpressionType
    {
        /**
         * An expression that only has both a left and a right side
         */
        BINARY,
        
        /**
         * Either a ++ or a -- after the expression
         */
        POSTFIX,
        
        /**
         * Either a ++ or a -- before the expression
         */
        PREFIX
    }

    /**
     * An enum that lists all the operators in ActionScript
     */
    static enum OperatorType
    {
        /**
         * The '<code>as</code>' binary operator, as in <code>a as B</code>
         */
        AS(IASKeywordConstants.AS),
        
        /**
         * The '<code>=</code>' binary operator, as in <code>a = b</code>
         */
        ASSIGNMENT("="),
        
        /**
         * The '<code>@</code>' unary operator, as in <code>a.@b</code>
         */
        AT("@"),
        
        /**
         * The '<code>&</code>' binary operator, as in <code>a & b</code>
         */
        BITWISE_AND("&"),
        
        /**
         * The '<code>&=</code>' binary operator, as in <code>a &= b</code>
         */
        BITWISE_AND_ASSIGNMENT("&="),
        
        /**
         * The '<code>|</code>' binary operator, as in <code>a | b</code>
         */
        BITWISE_OR("|"),
        
        /**
         * The '<code>|=</code>' binary operator, as in <code>a |= b</code>
         */
        BITWISE_OR_ASSIGNMENT("|="),
        
        /**
         * The '<code>^</code>' binary operator, as in <code>a ^ b</code>
         */
        BITWISE_XOR("^"),
        
        /**
         * The '<code>^=</code>' binary operator, as in <code>a ^= b</code>
         */
        BITWISE_XOR_ASSIGNMENT("^="),
        
        /**
         * The '<code><<</code>' binary operator, as in <code>a << b</code>
         */
        BITWISE_LEFT_SHIFT("<<"),
        
        /**
         * The '<code><<=</code>' binary operator, as in <code>a <<= b</code>
         */
        BITWISE_LEFT_SHIFT_ASSIGNMENT("<<="),
        
        /**
         * The '<code>~</code>' unary operator, as in <code>~a</code>
         */
        BITWISE_NOT("~"),
        
        /**
         * The <code>>></code> binary operator, as in <code>a >> b</code>
         */
        BITWISE_RIGHT_SHIFT(">>"),
        
        /**
         * The '<code>>>=</code>' binary operator, as in <code>a >>= b</code>
         */
        BITWISE_RIGHT_SHIFT_ASSIGNMENT(">>="),
        
        /**
         * The '<code>>>></code>' binary operator, as in <code>a >>> b</code>
         */
        BITWISE_UNSIGNED_RIGHT_SHIFT(">>>"),
        
        /**
         * The <code>>>>=</code> binary operator, as in <code>a >>>= b</code>
         */
        BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT(">>>="),
        
        /**
         * The '<code>,</code>' binary operator), as in <code>a, b</code>
         */
        COMMA(","),
        
        /**
         * The '<code>?</code>' operator, as in <code>a ? b : c</code>
         */
        CONDITIONAL("?"),
        
        /**
         * The '<code>--</code>' unary operator, as in <code>--a</code> or <code>a--</code>
         */
        DECREMENT("--"),
        
        /**
         * The '<code>delete</code>' unary operator, as in <code>delete a</code>
         */
        DELETE(IASKeywordConstants.DELETE),
        
        /**
         * The '<code>..</code>' binary operator, as in <code>a..b</code>
         */
        DESCENDANT_ACCESS(".."),

        /**
         * The '<code>/</code>' binary operator, as in <code>a/b</code>
         */
        DIVISION("/"),
        
        /**
         * The '<code>/=</code>' binary operator, as in <code>a /= b</code>
         */
        DIVISION_ASSIGNMENT("/="),
        
        /**
         * The '<code>[]</code>' binary operator, as in <code>a[b]</code>
         */
        DYNAMIC_ACCESS("[]"),
        
        /**
         * The '<code>==</code>' binary operator, as in <code>a == b</code>
         */
        EQUAL("=="),
        
        /**
         * The '<code>></code>' binary operator, as in <code>a > b</code>
         */
        GREATER_THAN(">"),
        
        /**
         * The '<code>>=</code>' binary operator, as in <code>a >= b</code>
         */
        GREATER_THAN_EQUALS(">="),
        
        /**
         * The '<code>in</code>' binary operator, as in <code>a in b</code>
         */
        IN(IASKeywordConstants.IN),
        
        /**
         * The '<code>++</code>' unary operator, as in <code>a++</code> or <code>++a</code>
         */
        INCREMENT("++"),
        
        /**
         * The '<code>instanceof</code>' binary operator, as in <code>a instanceof b</code>
         */
        INSTANCEOF(IASKeywordConstants.INSTANCEOF),
        
        /**
         * The '<code>is</code>' binary operator, as in <code>a is B</code>
         */
        IS(IASKeywordConstants.IS),
        
        /**
         * The '<code><</code>' binary operator, as in <code>a < b</code>
         */
        LESS_THAN("<"),
        
        /**
         * The '<code><=</code>' binary operator, as in <code>a <= b</code>
         */
        LESS_THAN_EQUALS("<="),
        
        /**
         * The '<code>&&</code>' binary operator, as in <code>a && b</code>
         */
        LOGICAL_AND("&&"),

        /**
         * The '<code>&&=</code>' binary operator, as in <code>a &&= b</code>
         */
        LOGICAL_AND_ASSIGNMENT("&&="),
        
        /**
         * The '<code>||</code>' binary operator, as in <code>a || b</code>
         */
        LOGICAL_OR("||"),

        /**
         * The '<code>||=</code>' binary operator, as in <code>a ||= b</code>
         */
        LOGICAL_OR_ASSIGNMENT("||="),

        /**
         * The '<code>!</code>' unary operator, as in <code>!a</code>
         */
        LOGICAL_NOT("!"),
        
        /**
         * The '<code>.</code>' binary operator, as in <code>a.b</code>
         */
        MEMBER_ACCESS("."),
        
        /**
         * The '<code>-</code>' unary/binary operator, as in <code>-a</code> or <code>a - b</code>
         */
        MINUS("-"),
        
        /**
         * The '<code>-=</code>' binary operator, as in <code>a -= b</code>
         */
        MINUS_ASSIGNMENT("-="),
        
        /**
         * The '<code>%</code>' binary operator, as in <code>a % b</code>
         */
        MODULO("%"),
        
        /**
         * The '<code>%=</code>' binary operator, as in <code>a %= b</code>
         */
        MODULO_ASSIGNMENT("%="),
        
        /**
         * The '<code>*</code>' binary operator, as in <code>a * b</code>
         */
        MULTIPLICATION("*"),
        
        /**
         * The '<code>*=</code>' binary operator, as in <code>a *= b</code>
         */
        MULTIPLICATION_ASSIGNMENT("*="),
        
        /**
         * The '<code>::</code>' binary operator, as in <code>a::b</code>
         */
        NAMESPACE_ACCESS("::"),
        
        /**
         * The '<code>!=</code>' binary operator, as in <code>a != b</code>
         */
        NOT_EQUAL("!="),
        
        /**
         * The '<code>+</code>' unary/binary operator, as in <code>+a</code> or <code>a + b</code>
         */
        PLUS("+"),
        
        /**
         * The '<code>+=</code>' binary operator, as in <code>a += b</code>
         */
        PLUS_ASSIGNMENT("+="),
        
        /**
         * The '<code>===</code>' binary operator, as in <code>a === b</code>
         */
        STRICT_EQUAL("==="),
        
        /**
         * The '<code>!==</code>' binary operator, as in <code>a !== b</code>
         */
        STRICT_NOT_EQUAL("!=="),
        
        /**
         * The '<code>typeof</code>' unary operator, as in <code>typeof a</code>
         */
        TYPEOF(IASKeywordConstants.TYPEOF),
        
        /**
         * The '<code>void</code>' unary operator, as in <code>void 0</code>
         */
        VOID("void");

        private String operatorText;

        OperatorType(String operatorText)
        {
            this.operatorText = operatorText;
        }

        public String getOperatorText()
        {
            return operatorText;
        }

        public boolean isBooleanOperator()
        {
            return this == GREATER_THAN ||
                   this == LESS_THAN ||
                   this == GREATER_THAN_EQUALS ||
                   this == LESS_THAN_EQUALS ||
                   this == EQUAL ||
                   this == LOGICAL_NOT ||
                   this == NOT_EQUAL ||
                   this == IS ||
                   this == LOGICAL_OR ||
                   this == LOGICAL_AND ||
                   this == STRICT_EQUAL ||
                   this == OperatorType.STRICT_EQUAL ||
                   this == OperatorType.INSTANCEOF ||
                   this == DELETE;
        }
    }

    /**
     * Returns the type of expression that is represented by this node
     * 
     * @return the type of our expression
     */
    ExpressionType getExpressionType();

    /**
     * Returns the type of the operator that is referenced from this expression
     * 
     * @return the operator that we're using
     */
    OperatorType getOperator();

    /**
     * Get the local offset where the operator starts
     * 
     * @return the local operator start offset
     */
    int getOperatorStart();

    /**
     * Get the local offset where the operator ends
     * 
     * @return the local operator end offset
     */
    int getOperatorEnd();

    /**
     * Get the absolute offset where the operator starts
     * 
     * @return the absolute operator start offset
     */
    int getOperatorAbsoluteStart();

    /**
     * Get the absolute offset where the operator ends
     * 
     * @return the absolute operator end offset
     */
    int getOperatorAbsoluteEnd();
}
