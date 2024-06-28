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

package options;

import org.junit.Test;

import as.ASFeatureTestsBase;

public class CompcOnlyOptionsTests extends ASFeatureTestsBase
{
    @Test
    public void testDirectoryOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-directory=true"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'directory'.\n\n");
    }

    @Test
    public void testIncludeClassesOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-classes+=MyClass"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-classes'.\n\n");
    }

    @Test
    public void testIncludeFileOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-file+=file.xml,src/test/resources/unit_tests/included.xml"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-file'.\n\n");
    }

    @Test
    public void testIncludeLookupOnlyOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-lookup-only=true"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-lookup-only'.\n\n");
    }

    @Test
    public void testIncludeNamespacesOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-namespaces+=uri"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-namespaces'.\n\n");
    }

    @Test
    public void testIncludeSourcesOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-sources+=src"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-sources'.\n\n");
    }

    @Test
    public void testIncludeStyleSheetOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-stylesheet+=src/test/resources/unit_tests/stylesheet.css"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-stylesheet'.\n\n");
    }

    @Test
    public void testIncludeInheritanceDependenciesOnlyOption()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-include-inheritance-dependencies-only=true"
        };
        compileAndExpectErrors(source, false,false,false, options,"unknown configuration variable 'include-inheritance-dependencies-only'.\n\n");
    }
}