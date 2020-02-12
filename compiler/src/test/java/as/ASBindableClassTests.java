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


public class ASBindableClassTests extends ASFeatureTestsBase
{
    /**
     * These tests are simply to ensure that Bindable source code variations
     * that should compile without error do so, and those that should generate
     * and error also do what they should.
     * At compiler testing time, it is not possible to test the runtime behaviour of the
     * generated code, because it depends on framework support and that should be tested
     * in the framework build. So these tests are just to ensure that various combinations
     * of Bindable definitions compile successfully or they do not, if they should show an Error level problem.
     */

    @Test
    public void testEmptyBindableClass()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
            "[Bindable]",
            " class EmptyBindable {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {

        };

        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void testVariableBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "[Bindable]",
                        " class VariableBindable {",
                        "  public var something:String;",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this will compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void testGetterSetterBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "[Bindable]",
                        " class GetterSetterBindable {",
                        "  public function get something():String{" +
                                "return '';" +
                            "}",
                        "  public function set something(value:String):void{" +
                                "" +
                                "}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void testEmptyBindableExtendsNonBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "class NonBindable{}\n\n",

                        "[Bindable]",
                        " class EmptyBindableExtendsNonBindable extends NonBindable {",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }


    @Test
    public void testVarBindableExtendsNonBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "class NonBindable{}\n\n",

                        "[Bindable]",
                        " class VarBindableExtendsNonBindable extends NonBindable {",
                        "  public var something:String;",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void testGetterSetterBindableExtendsNonBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "class NonBindable{}\n\n",

                        "[Bindable]",
                        " class GetterSetterBindableExtendsNonBindable extends NonBindable{",
                        "  public function get something():String{" +
                                "return '';" +
                                "}",
                        "  public function set something(value:String):void{" +
                                "" +
                                "}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }


    @Test
    public void testEmptyBindableExtendsEmptyBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "[Bindable] class EmptyBindable{}\n\n",

                        "[Bindable]",
                        " class EmptyBindableExtendsEmptyBindable extends EmptyBindable {",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void testVarBindableExtendsEmptyBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "[Bindable] class EmptyBindable{}\n\n",

                        "[Bindable]",
                        " class EmptyBindableExtendsEmptyBindable extends EmptyBindable {",
                        "  public var something:String;",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }

    @Test
    public void  testGetterSetterBindableExtendsEmptyBindable()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "[Bindable] class EmptyBindable{}\n\n",

                        "[Bindable]",
                        " class GetterSetterBindableExtendsEmptyBindable extends EmptyBindable {",
                        "  public function get something():String{" +
                                "return '';" +
                                "}",
                        "  public function set something(value:String):void{" +
                                "" +
                                "}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }



    @Test
    public void  testGetterSetterBindableExtendsGetterSetterBaseVariation()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "class GetterSetterBaseVariation{" +
                                "    protected var _something:String = 'UnbindableBase_something';\n" +
                                "    public function get something():String{\n" +
                                "        return _something;\n" +
                                "    }\n" +
                                "\n" +
                                "\n" +
                                "    protected var _somethingElse:String = 'UnbindableBase_somethingElse';\n" +
                                "    public function set somethingElse(value:String):void{\n" +
                                "        _somethingElse = value;\n" +
                                "    }" +
                                "}\n\n",

                        "[Bindable]",
                        " class GetterSetterBindableExtendsEmptyBindable extends GetterSetterBaseVariation {",
                        "  public function set something(value:String):void{\n" +
                                "\t\t\t_something = value;\n" +
                                "\t\t}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };
        //this is only a compilation test: it should compile ok, but is missing the framework dispatcher imports (but the code that would cause a runtime error is never executed)
        compileAndRun(source,false, false, false, options);
    }


    @Test
    public void  testGetterSetterBindableExtendsGetterSetterBaseVariation2()
    {
        String[] imports = new String[]
                {
                };
        String[] declarations = new String[]
                {
                };
        String[] testCode = new String[]
                {
                };
        String[] extra = new String[]
                {
                        "class GetterSetterBaseVariation{" +
                                "    protected var _something:String = 'UnbindableBase_something';\n" +
                                "    public function get something():String{\n" +
                                "        return _something;\n" +
                                "    }\n" +
                                "\n" +
                                "\n" +
                                "    protected var _somethingElse:String = 'UnbindableBase_somethingElse';\n" +
                                "    public function set somethingElse(value:String):void{\n" +
                                "        _somethingElse = value;\n" +
                                "    }" +
                                "}\n\n",

                        "[Bindable]",
                        " class GetterSetterBindableExtendsEmptyBindable extends GetterSetterBaseVariation {",
                        "\t\tpublic function set something(value:String):void{\n" +
                                "\t\t\t_something = value;\n" +
                                "\t\t}",
                        "\t\tpublic function get somethingElse():String{\n" +
                                "\t\t\treturn _somethingElse;\n" +
                                "\t\t}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
                {

                };

        //This is 'requires locally defined setter' error in Flex and should also be in royale
        compileAndExpectErrors(source, false,false,false, options,"[Bindable] on 'somethingElse' getter requires a locally defined setter.\n");

    }

}