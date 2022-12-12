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
    public void testNull()
    {
        String[] testCode = new String[]
        {
            "var o:Object = null;",
			"var result:* = o?.field;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// 0 is considered falsy, but not nullish
	@Test
    public void testUndefined()
    {
        String[] testCode = new String[]
        {
            "var o:* = undefined;",
			"var result:* = o?.field;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// false is considered falsy, but not nullish
	@Test
    public void testFalse()
    {
        String[] testCode = new String[]
        {
            "var o:Boolean = false;",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, 'false');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// NaN is considered falsy, but not nullish
	@Test
    public void testNaN()
    {
        String[] testCode = new String[]
        {
            "var o:Number = NaN;",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, 'NaN');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// 0 is considered falsy, but not nullish
	@Test
    public void testZero()
    {
        String[] testCode = new String[]
        {
            "var o:Number = 0;",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, '0');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	// empty string is considered falsy, but not nullish
	@Test
    public void testEmptyString()
    {
        String[] testCode = new String[]
        {
            "var o:String = '';",
			"var result:* = o?.toString();",
			"assertEqual('null conditional', result, '');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNotNull()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: 123.4};",
			"var result:* = o?.a;",
			"assertEqual('null conditional', result, 123.4);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNullField()
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
    public void testUndefinedField()
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
    public void testNullNestedField()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {a: null};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testUndefinedNestedField()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
			"var result:* = o?.a?.b;",
			"assertEqual('null conditional', result, null);",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNullConditionalArrayAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {};",
			"var result:* = o?.a?.[0];",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndExpectErrors(source, false, false, false, new String[0], "'[' is not allowed here\n");
    }


}