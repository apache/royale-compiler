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

public class ASAbstractClassTests extends ASFeatureTestsBase
{
    @Test
    public void testAbstractNotAllowedOnClassError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
            //error because abstract classes have not been enabled
            "abstract class A {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
    }
    
    @Test
    public void testAbstractNotAllowedOnInterfaceError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
            //error because abstract classes have not been enabled
            "abstract interface A {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
    }

    @Test
    public void testAbstractNotAllowedOnClassMethodError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceMethodError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticMethodError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public static abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractNotAllowedOnGetterError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public abstract function get a():Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractNotAllowedOnSetterError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public abstract function set a(value:Object):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractNotAllowedOnVariableError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticVariableError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because abstract classes have not been enabled
					"public static abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceGetterError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function get a():Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceSetterError_withAllowAbstractClassesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function set a(value:Object):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

	@Test
    public void testAbstractClassNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false,false,false, options, true);
        Assert.assertEquals("", result);
    }

	@Test
    public void testAbstractMethodOnClassNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractMethodWithParametersAndReturnOnClassNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a(arg0:String, arg1:Number):Boolean;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractMethodOnInterfaceError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					"abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractGetterOnInterfaceError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					"abstract function get a():Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractSetterOnInterfaceError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"interface A {",
					"abstract function set a(value:Object):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractProtectedMethodNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"protected abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractInternalMethodNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"internal abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractPrivateMethodError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"private abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Methods that are abstract cannot be declared private.\n");
	}

	@Test
    public void testAbstractFinalClassError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract final class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractFinalMethodError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public final abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractMethodNotInAbstractClassError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"class A {",
					//error because an abstract method may only be defined on an
					//abstract class
					"public abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractStaticMethodError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because a static function cannot be abstract
					"public static abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractGetterError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because a getter cannot be abstract
					"public abstract function get a():Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractSetterError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because a setter cannot be abstract
					"public abstract function set a(value:Object):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractVariableError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because a variable cannot be abstract
					"public abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractStaticVariableError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because a static variable cannot be abstract
					"public static abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractMethodBodyError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					//error because an abstract method has a body
					"public abstract function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method marked abstract must not have a body.\n");
	}

	@Test
    public void testAbstractClassNewOperatorError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
					//error because the class is abstract and cannot be
					//instantiated with new
					"var obj:A = new A();",
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Abstract classes cannot be instantiated with the new operator.\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractNewOperatorNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
					"var obj:A = new B();",
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"}",
					"class B extends A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractClassSuperError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					"override public function a():void {",
					//error because the the super method is abstract
					"super.a();",
					"};",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Call to a possibly undefined method a.\n");
	}

	@Test
    public void testAbstractClassNotImplementedError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					//error because we did not implement the abstract method in
					//a concrete subclass
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractClassNotImplementedErrorOneTimeForTwoConcreteSubclasses_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
                    "class B extends A {",
					//error because we did not implement the abstract method in
					//a concrete subclass
                    "}",
                    "class C extends B {",
                    //no duplicate error here!
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractClassMissingOverrideError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					//error because we did not use the override keyword
					"public function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Overriding a function that is not marked for override\n");
	}

	@Test
    public void testAbstractClassExtendsAbstractWithoutImplementingNoError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractNotImplementedError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}",
					"class C extends B {",
					//but we do want an error here for unimplemented methods
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractMultipleNotImplementedErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					"public abstract function b():void;",
					"}",
					"class C extends B {",
					//mulitple errors because more than one method is not implemented
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method b in abstract class B not implemented by class C\nMethod a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testAbstractClassExtendsAbstractAndImplementsNoError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//it's okay for an abstract subclass to implement any
					//abstract methods from the superclass
					"override public function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testConcreteClassExtendsAbstractNoError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					//no errors because we've implemented all of the methods
					//that are abstract
					"override public function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}
}