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

public class ASInferTypesTests extends ASFeatureTestsBase
{
	// ----- local variables

    @Test
    public void testLocalVariableHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "var s = \"hello\";"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipLocalVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "var s = \"hello\";"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testLocalVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "var s = null;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testLocalVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "var s = undefined;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipLocalVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "var s:* = undefined;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- parameters

	@Test
    public void testParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "function f(s = \"hello\"):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "parameter 's' for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for parameter because String is inferred
            "function f(s = \"hello\"):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "function f(s = null):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "parameter 's' for function 'f' has no type declaration.\n");
    }

	@Test
    public void testParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "function f(s = undefined):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "parameter 's' for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "function f(s:* = undefined):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- returns

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "function f() { return \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for return value because String is inferred
            "function f() { return \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "function f() { return null; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "function f() { return undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// can't infer a type from undefined
            "function f():* { return undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndNoReturnStatements()
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
            "function f() {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndNoReturnStatements()
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
			// no return statements means that void is inferred
            "function f() {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndNoReturnValues()
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
            "function f() { return; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndNoReturnValues()
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
			// no return statements with values means that void is inferred
            "function f() { return; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideInferredType()
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
            "class C {",
			"public function f() { return \"hello\"; }",
			"}",
            "class D extends C {",
			"override public function f() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 'f' has no type declaration.\nreturn value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideInferredType()
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
            "class C {",
			"public function f() { return \"hello\"; }",
			"}",
            "class D extends C {",
			"override public function f() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideDeclaredType()
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
            "class C {",
			"public function f():String { return null; }",
			"}",
            "class D extends C {",
			"override public function f() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        
        compileAndExpectErrors(source, false,false,false, options, "Incompatible override.\nreturn value for function 'f' has no type declaration.\n");
    }

	@Test
    public void testSkipReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideDeclaredType()
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
            "class C {",
			"public function f():String { return null; }",
			"}",
            "class D extends C {",
			"override public function f() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- member variables

	@Test
    public void testMemberVariableHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "class C { public var s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipMemberVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "class C { public var s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testMemberVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "class C { public var s = null; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testMemberVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "class C { public var s = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipMemberVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "class C { public var s:* = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- static variables

	@Test
    public void testStaticVariableHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "class C { public static var s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipStaticVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "class C { public static var s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testStaticVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "class C { public static var s = null; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testStaticVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "class C { public static var s = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipStaticVariableHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "class C { public static var s:* = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- static constants

	@Test
    public void testStaticConstantHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "class C { public static const s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipStaticConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "class C { public static const s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testStaticConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "class C { public static const s = null; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testStaticConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "class C { public static const s = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipStaticConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "class C { public static const s:* = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- member constants

	@Test
    public void testMemberConstantHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "class C { public const s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipMemberConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "class C { public const s = \"hello\"; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testMemberConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "class C { public const s = null; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testMemberConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "class C { public const s = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipMemberConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "class C { public const s:* = undefined; }"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- local constants

	@Test
    public void testLocalConstantHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "const s = \"hello\";"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipLocalConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
			// no warning for variable because String is inferred
            "const s = \"hello\";"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testLocalConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
			// can't infer a type from null
            "const s = null;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testLocalConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
			// can't infer a type from undefined
            "const s = undefined;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "variable 's' has no type declaration.\n");
    }

	@Test
    public void testSkipLocalConstantHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
			// no warning for undefined when the * type is declared
            "const s:* = undefined;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	// ----- getters and setters

	@Test
    public void testSetterParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndGetterDeclaredString()
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
            "class C {",
			"public function get s():String { return null; }",
			"public function set s(v):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "parameter 'v' for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipSetterParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndGetterDeclaredString()
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
            "class C {",
			"public function get s():String { return null; }",
			"public function set s(v):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndSetterDeclaredString()
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
            "class C {",
			"public function get s() { return null; }",
			"public function set s(v:String):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndSetterDeclaredString()
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
            "class C {",
			"public function get s() { return null; }",
			"public function set s(v:String):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarningAndSetterParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndGetterDefaultValueString()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"public function set s(v):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\nparameter 'v' for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarningAndSetterParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndGetterDefaultValueString()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"public function set s(v):void {}",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndDefaultValueString()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueString()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueNull()
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
            "class C {",
			"public function get s() { return null; }",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\n");
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefined()
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
            "class C {",
			"public function get s() { return undefined; }",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndDefaultValueUndefinedAndDeclaredAnyType()
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
            "class C {",
			"public function get s():* { return undefined; }",
			"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideInferredType()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"}",
            "class D extends C {",
			"override public function get s() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\nreturn value for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideInferredType()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"}",
            "class D extends C {",
			"override public function get s() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideDeclaredType()
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
            "class C {",
			"public function get s():String { return null; }",
			"}",
            "class D extends C {",
			"override public function get s() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        
        compileAndExpectErrors(source, false,false,false, options, "Incompatible override.\nreturn value for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipGetterReturnValueHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideDeclaredType()
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
            "class C {",
			"public function get s():String { return null; }",
			"}",
            "class D extends C {",
			"override public function get s() { return null; }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testSetterParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideDeclaredType()
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
            "class C {",
			"public function set s(p:String):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        
        compileAndExpectErrors(source, false,false,false, options, "Incompatible override.\nparameter 'p' for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipSetterParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideDeclaredType()
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
            "class C {",
			"public function set s(p:String):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testSetterParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideInferredType()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"public function set s(p):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        
        compileAndExpectErrors(source, false,false,false, options, "return value for function 's' has no type declaration.\nparameter 'p' for function 's' has no type declaration.\nparameter 'p' for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipSetterParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideInferredType()
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
            "class C {",
			"public function get s() { return \"hello\"; }",
			"public function set s(p):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testSetterParameterHasNoTypeDeclarationWarning_withInferTypesDisabledAndOverrideInferredType2()
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
            "class C {",
			"public function get s():String { return null; }",
			"public function set s(p):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=false"
        };
        
        compileAndExpectErrors(source, false,false,false, options, "parameter 'p' for function 's' has no type declaration.\nparameter 'p' for function 's' has no type declaration.\n");
    }

	@Test
    public void testSkipSetterParameterHasNoTypeDeclarationWarning_withInferTypesEnabledAndOverrideInferredType2()
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
            "class C {",
			"public function get s():String { return null; }",
			"public function set s(p):void {}",
			"}",
            "class D extends C {",
			"override public function set s(p):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-infer-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }
}
