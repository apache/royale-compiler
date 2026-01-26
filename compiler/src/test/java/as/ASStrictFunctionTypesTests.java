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

public class ASStrictFunctionTypesTests extends ASFeatureTestsBase
{
    @Test
    public void testFunctionTypeExpression_withAllowStrictFunctionTypesDisabled()
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
            "var a:(s:String)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testMetadata_withAllowStrictFunctionTypesDisabled()
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
            "[RoyaleFunctionType(signature=\"(s:String)=>void\")]",
            "var a:Function;",
            "[RoyaleFunctionType(signature=\"(n:Number)=>void\")]",
            "var b:Function;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        // no errors. the metadata should just be ignored.
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testMetadataAssignment_withAllowStrictFunctionTypesEnabled()
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
            "[RoyaleFunctionType(signature=\"(s:String)=>void\")]",
            "var a:Function;",
            "[RoyaleFunctionType(signature=\"(n:Number)=>void\")]",
            "var b:Function;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (n:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testMetadataCall_withAllowStrictFunctionTypesEnabled()
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
            "[RoyaleFunctionType(signature=\"(s:String)=>void\")]",
            "var a:Function;",
            "a(123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Number to an unrelated type String.\n");
    }

    @Test
    public void testAConflictExistsWithDefinitionInNamespaceSignatureDoesNotMatch()
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
            "var a:(s:String)=>void;",
            "var a:(n:Number)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "A conflict exists with definition a in namespace private.\nA conflict exists with definition a in namespace private.\n");
    }

    @Test
    public void testDuplicateVariableDefinitionWithMatchingSignatureMatches()
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
            // if it has the same name and type, it's fine
            "var a:(s:String)=>void;",
            "var a:(s:String)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Duplicate variable definition: a.\n");
    }

    @Test
    public void testRestParmeterMustBeLast()
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
            "var a:(...rest:Array, s:String)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Rest parameters must be last.\n");
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
            "var a:(s?:String,n:Number)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Required parameters are not permitted after optional parameters.\n");
    }

    @Test
    public void testDuplicateParameterName()
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
            "var a:(s:String,s:String)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "More than one argument named 's' specified for function ''. References to that argument will always resolve to the last one.\n");
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
            "var a:(s)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "parameter 's' for function '' has no type declaration.\n");
    }

    @Test
    public void testNestedRequiredParameterAfterOptional()
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
            "var a:(f:(s?:String,n:Number)=>void)=>void;",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Required parameters are not permitted after optional parameters.\n");
    }

    @Test
    public void testInterfaceExtends()
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
            "interface MyInterface extends (s:String)=>void {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testTryCatch()
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
            "try {}",
            "catch (e:(s:String)=>void) {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testTryCatchFinally()
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
            "try {}",
            "catch (e:(s:String)=>void) {}",
            "finally {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testIs_withArrowFunctions()
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
            "var x:Function;",
            "if (x is (s:String)=>void) {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectFilteredErrors(source, false, false, false, options,
            "'=>' is not allowed here\n')' is not allowed here\n");
    }

    @Test
    public void testIs_withoutArrowFunctions()
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
            "var x:Function;",
            "if (x is (s:String)=>void) {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=false",
            "-allow-strict-function-types=true"
        };
        compileAndExpectFilteredErrors(source, false, false, false, options,
            "'=>' is not allowed here\n')' is not allowed here\n'=>' is not allowed here\n");
    }

    @Test
    public void testAs_withArrowFunctions()
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
            "var x:Function;",
            "var y:(s:String)=>void = x as (s:String)=>void;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectFilteredErrors(source, false, false, false, options,
            "'=>' is not allowed here\n';' is not allowed here\n");
    }

    @Test
    public void testAs_withoutArrowFunctions()
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
            "var x:Function;",
            "var y:(s:String)=>void = x as (s:String)=>void;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=false",
            "-allow-strict-function-types=true"
        };
        compileAndExpectFilteredErrors(source, false, false, false, options,
            "'=>' is not allowed here\n';' is not allowed here\n'=>' is not allowed here\n");
    }

    @Test
    public void testClassExtends()
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
            "class MyClass extends (s:String)=>void {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testClassImplements()
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
            "class MyClass implements (s:String)=>void {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "'(s:String)=>void' is not allowed here\n");
    }

    @Test
    public void testSuperclassParameterType_assignVariableToVariable()
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
            "class A {}",
            "class B extends A {}",
            "var a:(s:B)=>void;",
            "var b:(o:A)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testSubclassParameterType_assignVariableToVariable()
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
            "class A {}",
            "class B extends A {}",
            "var a:(o:A)=>void;",
            "var b:(o:B)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (o:B)=>void to an unrelated type (o:A)=>void.\n"
        );
    }

    @Test
    public void testSubclassReturnType_assignVariableToVariable()
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
            "class A {}",
            "class B extends A {}",
            "var a:()=>A;",
            "var b:()=>B;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testSuperclassReturnType_assignVariableToVariable()
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
            "class A {}",
            "class B extends A {}",
            "var a:()=>B;",
            "var b:()=>A;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>A to an unrelated type ()=>B.\n"
        );
    }

    @Test
    public void testWrongParameterType1_assignVariableToVariable()
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
            "var a:(s:String)=>void;",
            "var b:(n:Number)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (n:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_assignVariableToVariable()
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
            "var a:(s:String,n:Number)=>void;",
            "var b:(s:String,b:Boolean)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String,b:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s:String,n:Number,b:Boolean)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s?:String,n?:Number,b?:Boolean)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s?:String,n?:Number,b?:Boolean,i?:int,u?:uint)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_assignVariableToVariable()
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
            "var a:(s?:String)=>void;",
            "var b:(s:String)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s:String,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s:String,n:Number,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(s:String,n:Number,b:Boolean,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(x?:String,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(x?:String,y?:Number,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_assignVariableToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "var b:(x?:String,y?:Number,z?:Boolean,...rest:Array)=>void;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_assignVariableToVariable()
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
            "var a:()=>String;",
            "var b:()=>Number;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_assignVariableToVariable()
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
            "var a:()=>String;",
            "var b:()=>String;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_assignVariableToVariable()
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
            "var a:()=>void;",
            "var b:()=>String;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignToReturnAny_assignVariableToVariable()
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
            "var a:()=>*;",
            "var b:()=>String;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignStrictTypeToRegular_assignVariableToVariable()
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
            "var a:Function;",
            "var b:()=>String;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignRegularTypeToStrict_assignVariableToVariable()
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
            "var a:(s:String)=>void;",
            "var b:Function;",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Function to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType1_assignFunctionToVariable()
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
            "var a:(s:String)=>void;",
            "function b(x:Number):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_assignFunctionToVariable()
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
            "var a:(s:String,n:Number)=>void;",
            "function b(x:String, y:Boolean):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String, y:Number, z:Boolean):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_assignFunctionToVariable()
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
            "var a:(s?:String)=>void;",
            "function b(s:String):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(...rest):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String, ...rest):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String, y:Number, ...rest):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String, y:Number, z:Boolean, ...rest):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String = 'a', ...rest:Array):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, ...rest:Array):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_assignFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void {}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_assignFunctionToVariable()
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
            "var a:()=>String;",
            "function b():Number {return 0;}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_assignFunctionToVariable()
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
            "var a:()=>String;",
            "function b():String {return null;}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_assignFunctionToVariable()
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
            "var a:()=>void;",
            "function b():String {return null;}",
            "a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_assignArrowFunctionToVariable()
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
            "var a:(s:String)=>void;",
            "a = (x:Number):void => {};"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number)=>void;",
            "a = (x:String, y:Boolean):void => {};"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String, y:Number, z:Boolean):void => {};"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String = 'a', y:Number = 123.4, z:Boolean = false):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_assignArrowFunctionToVariable()
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
            "var a:(s?:String)=>void;",
            "a = (s:String):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (...rest):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String, ...rest):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String, y:Number, ...rest):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String, y:Number, z:Boolean, ...rest):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String = 'a', ...rest:Array):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a = (x:String = 'a', y:Number = 123.4, ...rest:Array):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_assignArrowFunctionToVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a =  (x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void => {}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_assignArrowFunctionToVariable()
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
            "var a:()=>String;",
            "a = ():Number => {return 0;}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_assignArrowFunctionToVariable()
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
            "var a:()=>String;",
            "a = ():String => {return null;}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_assignArrowFunctionToVariable()
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
            "var a:()=>void;",
            "a = ():String => {return null;}",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_passVariableToParameter()
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
            "function a(x:(s:String)=>void):void {}",
            "var b:(n:Number)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (n:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_passVariableToParameter()
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
            "function a(x:String, y:(s:String,n:Number)=>void):void {}",
            "var b:(s:String,b:Boolean)=>void;",
            "a(\"hi\", b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String,b:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s:String,n:Number,b:Boolean)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s?:String,n?:Number,b?:Boolean)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s?:String,n?:Number,b?:Boolean,i?:int,u?:uint)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_passVariableToParameter()
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
            "function a(x:(s?:String)=>void):void {}",
            "var b:(s:String)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s:String,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s:String,n:Number,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(s:String,n:Number,b:Boolean,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(x?:String,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(x?:String,y?:Number,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_passVariableToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "var b:(x?:String,y?:Number,z?:Boolean,...rest:Array)=>void;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_passVariableToParameter()
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
            "function a(x:()=>String):void {}",
            "var b:()=>Number;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_passVariableToParameter()
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
            "function a(x:()=>String):void {}",
            "var b:()=>String;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_passVariableToParameter()
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
            "function a(x:()=>void):void {}",
            "var b:()=>String;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignStrictTypeToRegular_passVariableToParameter()
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
            "function a(x:Function):void {}",
            "var b:()=>String;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignRegularTypeToStrict_passVariableToParameter()
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
            "function a(x:(s:String)=>void):void {}",
            "var b:Function;",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Function to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType1_passFunctionToParameter()
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
            "function a(x:(s:String)=>void):void {}",
            "function b(x:Number):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number)=>void):void {}",
            "function b(x:String, y:Boolean):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String, y:Number, z:Boolean):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_passFunctionToParameter()
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
            "function a(x:(s?:String)=>void):void {}",
            "function b(s:String):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String, y:Number, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String, y:Number, z:Boolean, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String = 'a', ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String = 'a', y:Number = 123.4, ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_passFunctionToParameter()
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
            "function a(x:()=>String):void {}",
            "function b():Number { return 0; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_passFunctionToParameter()
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
            "function a(x:()=>String):void {}",
            "function b():String { return null; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_passFunctionToParameter()
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
            "function a(x:()=>void):void {}",
            "function b():String { return null; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_passArrowFunctionToParameter()
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
            "function a(x:(s:String)=>void):void {}",
            "a((x:Number):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number)=>void):void {}",
            "a((x:String, y:Boolean):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String, y:Number, z:Boolean):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_passArrowFunctionToParameter()
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
            "function a(x:(s?:String)=>void):void {}",
            "a((s:String):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String, y:Number, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String, y:Number, z:Boolean, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String = 'a', ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String = 'a', y:Number = 123.4, ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_passArrowFunctionToParameter()
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
            "function a(x:(s:String,n:Number,b:Boolean)=>void):void {}",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_passArrowFunctionToParameter()
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
            "function a(x:()=>String):void {}",
            "a(():Number => { return 0; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_passArrowFunctionToParameter()
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
            "function a(x:()=>String):void {}",
            "a(():String => { return null; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_passArrowFunctionToParameter()
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
            "function a(x:()=>void):void {}",
            "a(():String => { return null; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_returnVariable()
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
            "function a():(s:String)=>void {",
            "var b:(n:Number)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (n:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_returnVariable()
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
            "function a():(s:String,n:Number)=>void {",
            "var b:(s:String,b:Boolean)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String,b:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s:String,n:Number,b:Boolean)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s?:String,n?:Number,b?:Boolean)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s?:String,n?:Number,b?:Boolean,i?:int,u?:uint)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_returnVariable()
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
            "function a():(s?:String)=>void {",
            "var b:(s:String)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(...rest:Array)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s:String,...rest:Array)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s:String,n:Number,...rest:Array)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_returnVariable()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "var b:(s:String,n:Number,b:Boolean,...rest:Array)=>void;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_returnVariable()
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
            "function a():()=>String {",
            "var b:()=>Number;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_returnVariable()
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
            "function a():()=>String {",
            "var b:()=>String;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_returnVariable()
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
            "function a():()=>void {",
            "var b:()=>String;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignStrictTypeToRegular_returnVariable()
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
            "function a():Function {",
            "var b:()=>String;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testAssignRegularTypeToStrict_returnVariable()
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
            "function a():(s:String)=>void {",
            "var b:Function;",
            "return b;",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Function to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType1_returnFunction()
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
            "function a():(s:String)=>void {",
            "return function b(x:Number):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_returnFunction()
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
            "function a():(s:String,n:Number)=>void {",
            "return function b(x:String, y:Boolean):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String, y:Number, z:Boolean):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String = 'a', y:Number = 123.4, z:Boolean = false):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, i:int = -5, u:uint = 6):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_returnFunction()
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
            "function a():(s?:String)=>void {",
            "return function b(s:String):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(...rest):void {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String, ...rest):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String, y:Number, ...rest):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_returnFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return function b(x:String, y:Number, z:Boolean, ...rest):void {}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_returnFunction()
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
            "function a():()=>String {",
            "return function b():Number {return 0;}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_returnFunction()
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
            "function a():()=>String {",
            "return function b():String {return null;}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_returnFunction()
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
            "function a():()=>void {",
            "return function b():String {return null;}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_returnArrowFunction()
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
            "function a():(s:String)=>void {",
            "return (x:Number):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_returnArrowFunction()
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
            "function a():(s:String,n:Number)=>void {",
            "return (x:String, y:Boolean):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String, y:Number, z:Boolean):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String = 'a', y:Number = 123.4, z:Boolean = false):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String = 'a', y:Number = 123.4, z:Boolean = false, i:int = -5, u:uint = 6):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_returnArrowFunction()
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
            "function a():(s?:String)=>void {",
            "return (s:String):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (...rest):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String, ...rest):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String, y:Number, ...rest):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_returnArrowFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void {",
            "return (x:String, y:Number, z:Boolean, ...rest):void => {};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_returnArrowFunction()
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
            "function a():()=>String {",
            "return ():Number => {return 0;};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_returnArrowFunction()
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
            "function a():()=>String {",
            "return ():String => {return null;};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_returnArrowFunction()
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
            "function a():()=>void {",
            "return ():String => {return null;};",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String)=>void)=>void;",
            "function b(x:Number):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number)=>void)=>void;",
            "function b(x:String, y:Boolean):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String, y:Number, z:Boolean):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s?:String)=>void)=>void;",
            "function b(s:String):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String, y:Number, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String, y:Number, z:Boolean, ...rest):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String = 'a', ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "function b(x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void {}",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>String)=>void;",
            "function b():Number { return 0; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>String)=>void;",
            "function b():String { return null; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_passFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>void)=>void;",
            "function b():String { return null; }",
            "a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String)=>void)=>void;",
            "a((x:Number):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:Number)=>void to an unrelated type (s:String)=>void.\n");
    }

    @Test
    public void testWrongParameterType2_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number)=>void)=>void;",
            "a((x:String, y:Boolean):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String,y:Boolean)=>void to an unrelated type (s:String,n:Number)=>void.\n");
    }

    @Test
    public void testCorrectParameterTypes_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String, y:Number, z:Boolean):void => {});"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional1_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testCorrectParameterTypesWithOptional2_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false, w:int = -5, u:uint = 6):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testParameterNotOptional_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s?:String)=>void)=>void;",
            "a((s:String):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (s:String)=>void to an unrelated type (s?:String)=>void.\n");
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String, y:Number, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String, y:Number, z:Boolean, ...rest):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes1_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String = 'a', ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes2_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String = 'a', y:Number = 123.4, ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalAndRestParameterInsteadOfCorrectParameterTypes3_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:(s:String,n:Number,b:Boolean)=>void)=>void;",
            "a((x:String = 'a', y:Number = 123.4, z:Boolean = false, ...rest:Array):void => {});",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>String)=>void;",
            "a(():Number => { return 0; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>String)=>void;",
            "a(():String => { return null; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testIgnoreReturnTypeForVoid_passArrowFunctionToParameterOfVariableWithFunctionType()
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
            "var a:(x:()=>void)=>void;",
            "a(():String => { return null; });",
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true",
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongParameterType1_callVariable()
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
            "var a:(s:String)=>void;",
            "a(123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Number to an unrelated type String.\n");
    }

    @Test
    public void testWrongParameterType2_callVariable()
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
            "var a:(s:String,n:Number)=>void;",
            "a(\"hello\", true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Boolean to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectParameterTypes_callVariable()
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
            "var a:(s:String,n:Number,b:Boolean)=>void;",
            "a(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testRestParameter1_callVariable()
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
            "var a:(...rest)=>void;",
            "a(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter2_callVariable()
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
            "var a:(...rest)=>void;",
            "a(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter3_callVariable()
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
            "var a:(s:String, ...rest)=>void;",
            "a(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter4_callVariable()
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
            "var a:(s:String, n:Number, b:Boolean, ...rest)=>void;",
            "a(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter5_callVariable()
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
            "var a:(s:String, ...rest)=>void;",
            "a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
                "Incorrect number of arguments.  Expected 1\n"
        );
    }

    @Test
    public void testOptionalParameter1_callVariable()
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
            "var a:(s?:String)=>void;",
            "a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter2_callVariable()
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
            "var a:(s?:String)=>void;",
            "a(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter3_callVariable()
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
            "var a:(s?:String, n?:Number, b?:Boolean)=>void;",
            "a(\"hello\", 123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_assignCalledVariableToVariable()
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
            "var a:()=>String;",
            "var b:Number = a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectReturnType_assignCalledVariableToVariable()
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
            "var a:()=>String;",
            "var b:String = a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongReturnType_returnCalledVariable()
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
            "function a():Number {",
            "    var b:()=>String;",
            "    return b();",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongReturnType_passCalledVariableToParameter()
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
            "var a:()=>String;",
            "function b(n:Number):void {}",
            "b(a());"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongReturnType_assignFunctionTypeReturnedByVariableToVariableWithFunctionTypeThatReturnsFunctionType1()
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
            "var a:()=>(x:String)=>Boolean;",
            "var b:(x:String)=>()=>void = a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String)=>Boolean to an unrelated type (x:String)=>()=>void.\n");
    }

    @Test
    public void testWrongReturnType_assignFunctionTypeReturnedByVariableToVariableWithFunctionTypeThatReturnsFunctionType2()
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
            "var a:()=>(x:String)=>Boolean;",
            "var b:(x:()=>void)=>Boolean = a();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type (x:String)=>Boolean to an unrelated type (x:()=>void)=>Boolean.\n");
    }

    @Test
    public void testWrongParameterType1_callFunctionTypeReturnedByVariable()
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
            "var a:(n:Number)=>(s:String)=>void;",
            "a(true)(123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Boolean to an unrelated type Number.\n"
            + "Implicit coercion of a value of type Number to an unrelated type String.\n");
    }

    @Test
    public void testWrongParameterType2_callFunctionTypeReturnedByVariable()
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
            "var a:(n:Number,a:Array)=>(s:String,n:Number)=>void;",
            "a(123.4, \"hi\")(\"hello\", true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Array.\n"
            + "Implicit coercion of a value of type Boolean to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectParameterTypes_callFunctionTypeReturnedByVariable()
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
            "var a:(b:Boolean,n:Number,s:String)=>(s:String,n:Number,b:Boolean)=>void;",
            "a(false, 98.76, \"hi\")(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testRestParameter1_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(...rest)=>void;",
            "a()(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter2_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(...rest)=>void;",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter3_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s:String, ...rest)=>void;",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter4_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s:String, n:Number, b:Boolean, ...rest)=>void;",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter5_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s:String, ...rest)=>void;",
            "a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
                "Incorrect number of arguments.  Expected 1\n"
        );
    }

    @Test
    public void testOptionalParameter1_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s?:String)=>void;",
            "a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter2_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s?:String)=>void;",
            "a()(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter3_callFunctionTypeReturnedByVariable()
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
            "var a:()=>(s?:String, n?:Number, b?:Boolean)=>void;",
            "a()(\"hello\", 123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByVariableAndAssignToVariable()
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
            "var a:()=>()=>String;",
            "var b:Number = a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectReturnType_callFunctionTypeReturnedByVariableAndAssignToVariable()
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
            "var a:()=>()=>String;",
            "var b:String = a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByVariableAndReturn()
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
            "function a():Number {",
            "    var b:()=>()=>String;",
            "    return b()();",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByVariableAndPassToParameter()
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
            "var a:()=>()=>String;",
            "function b(n:Number):void {}",
            "b(a()());"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongParameterType1_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String)=>void { return null; }",
            "a()(123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Number to an unrelated type String.\n");
    }

    @Test
    public void testWrongParameterType2_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String,n:Number)=>void { return null; }",
            "a()(\"hello\", true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type Boolean to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectParameterTypes_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String,n:Number,b:Boolean)=>void { return null; }",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testRestParameter1_callFunctionTypeReturnedByFunction()
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
            "function a():(...rest)=>void { return null; }",
            "a()(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter2_callFunctionTypeReturnedByFunction()
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
            "function a():(...rest)=>void { return null; }",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter3_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String, ...rest)=>void { return null; }",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter4_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String, n:Number, b:Boolean, ...rest)=>void { return null; }",
            "a()(\"hello\", 123.4, true);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testRestParameter5_callFunctionTypeReturnedByFunction()
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
            "function a():(s:String, ...rest)=>void { return null; }",
            "a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
                "Incorrect number of arguments.  Expected 1\n"
        );
    }

    @Test
    public void testOptionalParameter1_callFunctionTypeReturnedByFunction()
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
            "function a():(s?:String)=>void { return null; }",
            "a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter2_callFunctionTypeReturnedByFunction()
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
            "function a():(s?:String)=>void { return null; }",
            "a()(\"hello\");"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testOptionalParameter3_callFunctionTypeReturnedByFunction()
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
            "function a():(s?:String, n?:Number, b?:Boolean)=>void { return null; }",
            "a()(\"hello\", 123.4);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByFunctionAndAssignToVariable()
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
            "function a():()=>String { return null; };",
            "var b:Number = a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testCorrectReturnType_callFunctionTypeReturnedByFunctionAndAssignToVariable()
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
            "function a():()=>String { return null; };",
            "var b:String = a()();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByFunctionAndReturn()
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
            "function a():Number {",
            "    function b():()=>String { return null; };",
            "    return b()();",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongReturnType_callFunctionTypeReturnedByFunctionAndPassToParameter()
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
            "function a():()=>String { return null; };",
            "function b(n:Number):void {}",
            "b(a()());"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type String to an unrelated type Number.\n");
    }

    @Test
    public void testWrongReturnType_assignFunctionTypeReturnedByVariableToVariable()
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
            "var a:()=>String;",
            "var b:()=>()=>Number;",
            "a = b();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options,
            "Implicit coercion of a value of type ()=>Number to an unrelated type ()=>String.\n");
    }

    @Test
    public void testCorrectReturnType_assignFunctionTypeReturnedByVariableToVariable()
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
            "var a:()=>String;",
            "var b:()=>()=>String;",
            "a = b();"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }
}