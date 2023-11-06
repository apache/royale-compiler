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

import org.junit.Test;

public class ASFunctionTests extends ASFeatureTestsBase {
	
    @Test
    public void testFunctionWithoutBody()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo():void"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Function does not have a body.\n");
    }
	
    @Test
    public void testParameterHasNoTypeDeclaration()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "parameter 'bar' for function 'foo' has no type declaration.\n");
    }
	
    @Test
    public void testRequiredParameterAfterOptional()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar:Number = 123.4, baz:String):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Required parameters are not permitted after optional parameters.\n");
    }
	
    @Test
    public void testRestParameterMustBeLast()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(...rest, bar:String):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Rest parameters must be last.\n");
    }
	
    @Test
    public void testDuplicateParameterNames()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar:String, bar:Number):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "More than one argument named 'bar' specified for function 'foo'. References to that argument will always resolve to the last one.\n");
    }

    @Test
    public void testDuplicateParameterNames2()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var foo:Function = function(bar:String, bar:Number):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "More than one argument named 'bar' specified for function ''. References to that argument will always resolve to the last one.\n");
    }
}
