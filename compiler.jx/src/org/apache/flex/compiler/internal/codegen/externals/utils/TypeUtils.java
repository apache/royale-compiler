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

package org.apache.flex.compiler.internal.codegen.externals.utils;

import java.util.HashMap;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.ErrorReporter;

public final class TypeUtils
{

    @SuppressWarnings("unused")
    private static boolean isIdeMode = true;
    @SuppressWarnings("unused")
    private static LanguageMode mode = LanguageMode.ECMASCRIPT5;

    //    private static Node parseWarning(String string, String... warnings)
    //    {
    //        TestErrorReporter testErrorReporter = new TestErrorReporter(null,
    //                warnings);
    //        StaticSourceFile file = new SimpleSourceFile("input", false);
    //        Node script = ParserRunner.parse(file, string,
    //                ParserRunner.createConfig(isIdeMode, mode, false, null),
    //                testErrorReporter).ast;
    //
    //        // verifying that all warnings were seen
    //        testErrorReporter.assertHasEncounteredAllErrors();
    //        testErrorReporter.assertHasEncounteredAllWarnings();
    //        //
    //        //        JSDocInfo j = script.getChildAtIndex(0).getJSDocInfo();
    //        //        JSTypeExpression baseType = j.getType();
    //        //        boolean functionDeclaration = j.containsFunctionDeclaration();
    //        //
    //        //        String stringTree = script.toStringTree();
    //
    //        return script;
    //    }

    //    public static String getType(String value)
    //    {
    //
    //        String type = getTypeRaw(value);
    //
    //        String comment = "/** @type " + type + " */ var foo;";
    //
    //        Node node = parseWarning(comment);
    //        JSDocInfo j = node.getChildAtIndex(0).getJSDocInfo();
    //        JSTypeExpression baseType = j.getType();
    //        boolean functionDeclaration = j.containsFunctionDeclaration();
    //        boolean varArgs = baseType.isVarArgs();
    //        boolean optionalArg = baseType.isOptionalArg();
    //
    //        // XXX Warnings
    //        if (type == null)
    //            return "Object";
    //        if (type.indexOf("{?function (") != -1
    //                || type.equals("{?function (Event)}")
    //                || type.equals("{?function (Event=)}"))
    //            return "Function";
    //
    //        int start = -1;
    //        int end = -1;
    //
    //        // XXX {?function(this:S, T, number, !Array.<T>): ?} callback
    //        if (type.indexOf("<") != -1 && type.indexOf(">") != -1)
    //            return "Object";
    //
    //        // {*} [The ALL type]
    //        start = type.indexOf("{*}");
    //        if (start != -1)
    //        {
    //            return "*";
    //        }
    //
    //        // {?} [The UNKNOWN type]
    //        start = type.indexOf("{?}");
    //        if (start != -1)
    //        {
    //            return "Object /* ? */";
    //        }
    //
    //        // {Array<string>}
    //        start = type.indexOf("{Array<");
    //        if (start != -1)
    //        {
    //            end = type.indexOf(">}", start);
    //            String innerType = type.substring(start + 7, end);
    //            innerType = transformParamType(innerType);
    //            return "Vector.<" + innerType + ">";
    //        }
    //
    //        // {Object<string, number>}
    //        start = type.indexOf("{Object<");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(1, end);
    //            return "Object" + " /* " + orignal + " */";
    //        }
    //
    //        // @param {...number} var_args [Variable parameters (in @param annotations) ]
    //        start = type.indexOf("{...");
    //        if (start != -1)
    //        {
    //            // Function /* (string, boolean) */
    //            end = type.indexOf("}", start);
    //            //String orignal = type.substring(start + 12, end).trim();
    //            return "...rest";// + " /* " + orignal + " */";
    //        }
    //
    //        // XXX ADDD
    //        // ?=}
    //        end = type.indexOf("?=}");
    //        if (end != -1)
    //        {
    //            start = type.indexOf("{");
    //            String innerType = type.substring(start + 1, end);
    //            String asType = transformParamType(innerType);
    //            return asType + " = " + transformOptionNull(asType);
    //        }
    //
    //        // @param {number=} opt_argument [Optional parameter in a @param annotation ]
    //        end = type.indexOf("=}");
    //        if (end != -1)
    //        {
    //            start = type.indexOf("{");
    //            //end = type.indexOf("}", start);
    //            String innerType = type.substring(start + 1, end);
    //            String asType = transformParamType(innerType);
    //            return asType + " = " + transformOptionNull(asType);
    //        }
    //
    //        // {{myNum: number, myObject}} 
    //        start = type.indexOf("{{");
    //        if (start != -1)
    //        {
    //            return "Object";
    //        }
    //
    //        // XXX ADDED {?string=}
    //        start = type.indexOf("{?");
    //        if (start != -1 && type.indexOf("=}") != -1)
    //        {
    //            end = type.indexOf("?}", start);
    //            String innerType = type.substring(start + 2, end);
    //            return transformParamType(innerType);
    //        }
    //
    //        // {?number} or {?null}
    //        start = type.indexOf("{?");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String innerType = type.substring(start + 2, end);
    //            return transformParamType(innerType);
    //        }
    //
    //        // {!Object}
    //        start = type.indexOf("{!");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String innerType = type.substring(start + 2, end);
    //            return transformParamType(innerType);
    //        }
    //
    //        // {function(): number} [return type]
    //        start = type.indexOf("{function():");
    //        if (start != -1)
    //        {
    //            // Function /* (string, boolean) */
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 12, end).trim();
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // {function(this:goog.ui.Menu, string)} [Function this Type]
    //        start = type.indexOf("{function(this:");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 9, end).trim();
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // {function(new:goog.ui.Menu, string)} [Function new Type]
    //        start = type.indexOf("{function(new:");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 9, end).trim();
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // {function(string, ...number): number} [Variable parameters]
    //        start = type.indexOf("{function(");
    //        if (start != -1 && type.indexOf("...") != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 9, end).trim();
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // {function(?string=, number=)} [Optional argument in a function type]
    //        start = type.indexOf("{function(?");
    //        if (start != -1)
    //        {
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 9, end).trim();
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // {function(string, boolean)} [function type]
    //        start = type.indexOf("{function(");
    //        if (start != -1)
    //        {
    //            // Function /* (string, boolean) */
    //            end = type.indexOf("}", start);
    //            String orignal = type.substring(start + 9, end);
    //            return "Function" + " /* " + orignal + " */";
    //        }
    //
    //        // XXX multiple functions
    //        start = type.indexOf("{(function");
    //        if (start != -1)
    //        {
    //            return "Object";
    //        }
    //
    //        // {(, {(foo|bar)}
    //        start = type.indexOf("{(");
    //        if (start != -1)
    //        {
    //            end = type.indexOf(")}", start);
    //            String orignal = type.substring(1, end + 1);
    //            return "Object" + " /* " + orignal + " */";
    //        }
    //
    //        // {boolean}
    //        return transformParamType(type.substring(1, type.length() - 1));
    //    }

    @SuppressWarnings("unused")
    private static String transformOptionNull(String asType)
    {
        if (asType.indexOf("|") != -1)
            return "*";

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("*", "null");
        map.put("String", "null");
        map.put("Number", "null");
        map.put("Boolean", "null");
        map.put("Object", "null");
        map.put("Function", "null");

        if (map.containsKey(asType))
            return map.get(asType);

        return asType;
    }

    public static String transformReturnType(String type)
    {
        if (type.indexOf("|") != -1)
            return "Object";

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "void /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

    // XXX These are NOT for returned types
    public static String transformParamType(String type)
    {
        if (type.indexOf("|") != -1)
            return "Object";

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("?", "Object /* ? */");
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "Object /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

    public static String transformType(String type)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("?", "Object /* ? */");
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "Object /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

    public final static class TestErrorReporter implements ErrorReporter
    {
        private String[] errors;
        private String[] warnings;
        private int errorsIndex = 0;
        private int warningsIndex = 0;

        public TestErrorReporter(String[] errors, String[] warnings)
        {
            this.errors = errors;
            this.warnings = warnings;
        }

        public static TestErrorReporter forNoExpectedReports()
        {
            return new TestErrorReporter(null, null);
        }

        public void setErrors(String[] errors)
        {
            this.errors = errors;
            errorsIndex = 0;
        }

        public void setWarnings(String[] warnings)
        {
            this.warnings = warnings;
            warningsIndex = 0;
        }

        @Override
        public void error(String message, String sourceName, int line,
                int lineOffset)
        {
            if (errors != null && errorsIndex < errors.length)
            {
                //assertThat(message).isEqualTo(errors[errorsIndex++]);
            }
            else
            {
                //Assert.fail("extra error: " + message);
            }
        }

        @Override
        public void warning(String message, String sourceName, int line,
                int lineOffset)
        {
            if (warnings != null && warningsIndex < warnings.length)
            {
                //assertThat(message).isEqualTo(warnings[warningsIndex++]);
            }
            else
            {
                //Assert.fail("extra warning: " + message);
            }
        }

        public void assertHasEncounteredAllWarnings()
        {
            if (warnings == null)
            {
                //assertThat(warningsIndex).isEqualTo(0);
            }
            else
            {
                //assertThat(warnings).hasLength(warningsIndex);
            }
        }

        public void assertHasEncounteredAllErrors()
        {
            if (errors == null)
            {
                //assertThat(errorsIndex).isEqualTo(0);
            }
            else
            {
                //assertThat(errors).hasLength(errorsIndex);
            }
        }

    }
}
