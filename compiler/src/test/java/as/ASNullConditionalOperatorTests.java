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

package as;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ASNullConditionalOperatorTests extends ASFeatureTestsBase
{
	@Test
    public void testInvalidSyntaxBeforeDynamicAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
            // the ?. operator before [] square brackets is not valid syntax
			"var result:* = o?.a?.[0];",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndExpectErrors(source, false, false, false, new String[0], "'[' is not allowed here\n");
    }

	@Test
    public void testInvalidSyntaxBeforeFunctionCall()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
            // the ?. operator before () parentheses is not valid syntax
			"var result:* = o?.a?.toString?.();",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndExpectErrors(source, false, false, false, new String[0], "'(' is not allowed here\n");
    }

	// null is considered nullish
    @Test
    public void testNullToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// undefined is considered nullish
	@Test
    public void testUndefinedToString()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// false is considered falsy, but not nullish
	@Test
    public void testFalseToString()
    {
        String[] testCode = new String[]
        {
            "var b:Boolean = false;",
			"var result:* = b?.toString();",
			"assertEqual('null conditional', result, 'false');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// NaN is considered falsy, but not nullish
	@Test
    public void testNaNToString()
    {
        String[] testCode = new String[]
        {
            "var n:Number = NaN;",
			"var result:* = n?.toString();",
			"assertEqual('null conditional', result, 'NaN');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// 0 is considered falsy, but not nullish
	@Test
    public void testZeroToString()
    {
        String[] testCode = new String[]
        {
            "var n:Number = 0;",
			"var result:* = n?.toString();",
			"assertEqual('null conditional', result, '0');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// empty string is considered falsy, but not nullish
	@Test
    public void testEmptyStringToString()
    {
        String[] testCode = new String[]
        {
            "var s:String = '';",
			"var result:* = s?.toString();",
			"assertEqual('null conditional', result, '');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testObjectToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, '[object Object]');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNestedNullToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: null};",
			"var result:* = o?.a?.toString();",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNestedImplicitUndefinedToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
			"var result:* = o?.a?.toString();",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNestedExplicitUndefinedToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: undefined};",
			"var result:* = o?.a?.toString();",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedFalseToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: false};",
			"var result:* = o?.a?.toString();",
			"assertEqual('null conditional', result, 'false');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedZeroToString()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: 0};",
			"var result:* = o?.a?.toString();",
			"assertEqual('null conditional', result, '0');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNullFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: null};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testImplicitUndefinedFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testExplicitUndefinedFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: undefined};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testFalseFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: false};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testZeroFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: 0};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, 0);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedNullFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: null}};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedImplicitUndefinedFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {}};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedExplicitUndefinedFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: undefined}};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedFalseFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: false}};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNestedZeroFieldAsValue()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: 0}};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, 0);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNullMethodCallWithArgument()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
			"var result:* = o?.hasOwnProperty('a');",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testUndefinedMethodCallWithArgument()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
			"var result:* = o?.hasOwnProperty('a');",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testFalseMethodCallWithArgument()
    {
        String[] testCode = new String[]
        {
            "var b:Boolean = false;",
			"var result:* = b?.hasOwnProperty('xyz');",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testZeroMethodCallWithArgument()
    {
        String[] testCode = new String[]
        {
            "var n:Number = 0;",
			"var result:* = n?.hasOwnProperty('xyz');",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testObjectMethodCallWithArgument()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: 123};",
			"var result:* = o?.hasOwnProperty('a');",
			"assertEqual('null conditional', result, true);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNullWithDynamicAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
			"var result:* = o?.a['b'];",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testUndefinedWithDynamicAccess()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
			"var result:* = o?.a['b'];",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testObjectWithDynamicAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {}};",
			"var result:* = o?.a['b'];",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNestedObjectWithDynamicAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: 2}};",
			"var result:* = o?.a['b'];",
			"assertEqual('null conditional', result, 2);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNullWithMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
			"var result:* = o?.a.b;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testUndefinedWithMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
			"var result:* = o?.a.b;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testObjectWithMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {}};",
			"var result:* = o?.a.b;",
			"assertEqual('null conditional', result, undefined);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNestedObjectWithMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: {b: 2}};",
			"var result:* = o?.a.b;",
			"assertEqual('null conditional', result, 2);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testDeepNesting()
    {
        String[] testCode = new String[]
        {
            "var o:* = {a: {b: {c: {d1: undefined, d2: null, d3: 0, d4: false}}}};",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, undefined);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, 0);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, false);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), '0');",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), 'false');",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = {a: {b: {c: {}}}};",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, undefined);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, undefined);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, undefined);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, undefined);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = {a: {b: {}}};",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = {a: {}};",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = {};",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = null;",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
            "o = undefined;",
			"assertEqual('null conditional', o?.a?.b?.c?.d1, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d3?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d4?.toString(), null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d1?.e, null);",
			"assertEqual('null conditional', o?.a?.b?.c?.d2?.e, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testNullToStringInsideIf()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
            "var result:Boolean = false;",
            "if (o?.toString() === null) {",
            "  result = true;",
            "}",
			"assertEqual('null conditional', result, true);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testUndefinedToStringInsideIf()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
            "var result:Boolean = false;",
            "if (o?.toString() === null) {",
            "  result = true;",
            "}",
			"assertEqual('null conditional', result, true);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testFalseToStringInsideIf()
    {
        String[] testCode = new String[]
        {
            "var b:Boolean = false;",
            "var result:Boolean = false;",
            "if (b?.toString() === null) {",
            "  result = true;",
            "}",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testZeroToStringInsideIf()
    {
        String[] testCode = new String[]
        {
            "var n:Number = 0;",
            "var result:Boolean = false;",
            "if (n?.toString() === null) {",
            "  result = true;",
            "}",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

    @Test
    public void testObjectToStringInsideIf()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
            "var result:Boolean = false;",
            "if (o?.toString() === null) {",
            "  result = true;",
            "}",
			"assertEqual('null conditional', result, false);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

}