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

package org.apache.flex.compiler.utils;

/**
 * @author Michael Schmalle
 */
public class NativeUtils
{
    public enum NativeType
    {
        Any("*"), // not JS but use full in the context of native
        
        Argument("Argument"),
        Array("Array"),
        Boolean("Boolean"),
        Class("Class"),
        Date("Date"),
        Error("Error"),
        EvalError("EvalError"),
        Function("Function"),
        Infinity("Infinity"),
        Math("Math"),
        NaN("NaN"),
        Namespace("Namespace"),
        Number("Number"),
        Object("Object"),
        QName("QName"),
        RangeError("RangeError"),
        ReferenceError("ReferenceError"),
        RegExp("RegExp"),
        String("String"),
        SyntaxError("SyntaxError"),
        TypeError("TypeError"),
        URIError("URIError"),
        Vector("Vector"),
        XML("XML"),
        XMLList("XMLList"),
        _assert("assert"),
        decodeURI("decodeURI"),
        decodeURIComponent("decodeURIComponent"),
        encodeURI("encodeURI"),
        encodeURIComponent("encodeURIComponent"),
        escape("escape"),
        _int("int"),
        isFinite("isFinite"),
        isNaN("isNaN"),
        isXMLName("isXMLName"),
        parseFloat("parseFloat"),
        parseInt("parseInt"),
        trace("trace"),
        unit("unit"),
        undefined("undefined"),
        enescape("enescape");

        private final String value;

        NativeType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    public static boolean isNative(String type)
    {
        for (NativeType test : NativeType.values())
        {
            if (test.getValue().equals(type))
                return true;
        }
        return false;
    }
}
