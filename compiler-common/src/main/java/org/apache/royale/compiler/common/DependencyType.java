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

package org.apache.royale.compiler.common;


/**
 * An enumeration representing the four types of dependencies that an
 * edge in a DependencyGraph can have.
 */
public enum DependencyType
{
    /**
     * An <code>INHERITANCE</code> dependency is created by a reference
     * to a class or interface in the <code>extends</code> clause or the
     * <code>implements</code> clause of a class or interface declaration.
     * <p>
     * Examples:
     * <ul>
     *   <li><code>class C2 extends <u>C1</u> implements <u>I1</u>, <u>I2</u></code>
     *   <li><code>interface I1 extends <u>I2</u>, <u>I3</u></code>
     * </ul>
     */
    INHERITANCE('i'),
    
    /**
     * A <code>SIGNATURE</code> dependency is created by a type annotation
     * that is not inside of a function body.
     * <p>
     * Examples:
     * <ul>
     *   <li><code>public var i:<u>int</u></code>
     *   <li><code>private function f(i:<u>int</u>):<u>String</u></code>
     * </ul>
     */
    SIGNATURE('s'),
    
    /**
     * A <code>NAMESPACE</code> dependency is created by a reference to a
     * user-defined namespace.
     * <p>
     * Examples:
     * <ul>
     *   <li><code><u>ns</u> var foo:int</code>
     *   <li><code><u>ns</u>::foo</code>
     *   <li><code>use namespace <u>ns</u></code>
     * </ul>
     */
    NAMESPACE('n'),

    /**
     * An <code>EXPRESSION</code> dependency is any dependency that isn't
     * one of the other three types.
     */
    EXPRESSION('e');
     
    /**
     * Private constructor.
     * 
     * @param symbol The character representing the dependency type in XML
     * format (such as the <code>&lt;dep&gt;</code> tag in a SWC catalog).
     */
    private DependencyType(char symbol)
    {
        this.symbol = symbol;
    }
    
    /**
     * The character representing the dependency type in XML
     * format (such as the <code>&lt;dep&gt;</code> tag in a SWC catalog).
     */
    private final char symbol;
    
    /**
     * Gets the character representing the dependency type in XML
     * format (such as the <code>&lt;dep&gt;</code> tag in a SWC catalog).
     * 
     * @return symbol The character.
     */
    public char getSymbol()
    {
        return symbol;
    }
    
    /**
     * A helper function to check if this {@link DependencyType}
     * is contained in an {@link DependencyTypeSet}.
     * 
     * @param dependencies A {@link DependencyTypeSet} to look in.
     * @return <code>true</code> if this {@link DependencyType}
     * is contained in the specified {@link DependencyTypeSet}.
     */
    public boolean existsIn(DependencyTypeSet dependencies)
    {
        return dependencies.contains(this);
    }
    
    /**
     * Gets the enum object from its symbol.
     * 
     * @param symbol The character representing the dependency type in XML
     * format (such as the <code>&lt;dep&gt;</code> tag in a SWC catalog),
     * such as <code>'i'</code>.
     * @return The enum object with the specified symbol,
     * such as <code>INHERITANCE</code>.
     */
    public static DependencyType get(char symbol)
    {
        switch (symbol)
        {
            case 'i':
                return INHERITANCE;
            case 'n':
                return NAMESPACE;
            case 's':
                return SIGNATURE;
            case 'e':
                return EXPRESSION;
            default:
                throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
    }
    
    /**
     * Gets a short string of symbols for the dependency types
     * in a dependency type set.
     * 
     * @param types A {@link DependencyTypeSet}.
     * @return A short string of symbols of the types in the {@link DependencyTypeSet}
     * in INHERITANCE, NAMESPACE, SIGNATURE, EXPRESSION order.
     */
    public static String getTypeString(DependencyTypeSet types)
    {
        StringBuilder sb = new StringBuilder();
        if (INHERITANCE.existsIn(types))
            sb.append('i');
        if (NAMESPACE.existsIn(types))
            sb.append('n');
        if (SIGNATURE.existsIn(types))
            sb.append('s');
        if (EXPRESSION.existsIn(types))
            sb.append('e');
        return sb.toString();
    }
}
