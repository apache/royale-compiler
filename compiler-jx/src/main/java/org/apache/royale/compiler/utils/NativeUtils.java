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

package org.apache.royale.compiler.utils;

/**
 * @author Michael Schmalle
 */
public class NativeUtils
{
    public enum NativeASType
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
        JSON("JSON"),
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
        uint("uint"),
        undefined("undefined"),
        unescape("unescape");

        private final String value;

        NativeASType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    public enum NativeJSType
    {
        // (erikdebruin) Ref.: https://cwiki.apache.org/confluence/display/FLEX/Full+Table
        
        NaN("NaN"),
        Event("Event"),
        Array("Array"),
        RegExp("RegExp"),
        Float32Array("Float32Array"),
        Float64Array("Float64Array"),
        Int8Array("Int8Array"),
        Int16Array("Int16Array"),
        Int32Array("Int32Array"),
        Uint8Array("Uint8Array"),
        Uint8ClampedArray("Uint8ClampedArray"),
        Uint16Array("Uint16Array"),
        Uint32Array("Uint32Array"),
        Date("Date"),
        Math("Math"),
        Error("Error"),
        RangeError("RangeError"),
        Boolean("Boolean"),
        decodeURI("decodeURI"),
        decodeURIComponent("decodeURIComponent"),
        encodeURI("encodeURI"),
        encodeURIComponent("encodeURIComponent"),
        escape("escape"),
        isFinite("isFinite"),
        isNaN("isNaN"),
        Map("Map"),
        Number("Number"),
        Object("Object"),
        parseFloat("parseFloat"),
        parseInt("parseInt"),
        String("String"),
        undefined("undefined"),
        unescape("unescape"),
        window("window"),

        // (erikdebruin) These aren't strictly 'native' to JS, but the
        //               Publisher provides global functions, so, for all
        //               intends and purposes they behave like they are.
        _int("int"),
        trace("trace"),
        uint("uint"),
        
        // (erikdebruin) These are left out, but should, at some point, be
        //               treated as if they actually are 'native'.
        /*
        isXMLName("isXMLName"),
        Vector("Vector"),
        XML("XML"),
        XMLList("XMLList"),
        */
        
        _byte("byte"),
        
        ;
        private final String value;

        NativeJSType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }
    
    public enum SyntheticJSType
    {
        _int("int"),
        uint("uint"),
        Vector("Vector"),
        Class("Class");
        
        private final String value;
    
        SyntheticJSType(String value)
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
        for (NativeASType test : NativeASType.values())
        {
            if (test.getValue().equals(type))
                return true;
        }
        if (type.startsWith("Vector.<"))
            return true;
        return false;
    }

    public static boolean isJSNative(String type)
    {
        for (NativeJSType test : NativeJSType.values())
        {
            if (test.getValue().equals(type))
                return true;
        }
        return false;
    }
    
    public static boolean isSyntheticJSType(String type)
    {
        for (SyntheticJSType test : SyntheticJSType.values())
        {
            if (test.getValue().equals(type)) {
                return true;
            }
        }
        if (type.startsWith("Vector.<")) {
            return true;
        }
        return false;
    }
    
    public static boolean isVector(String type)
    {
        return type != null && type.startsWith("Vector.<");
    }

}
