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

package org.apache.royale.compiler.internal.codegen.typedefs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.FunctionReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestCollectImports extends TypedefsTestBase
{
    private static final String IMPORTS_TEST_DIR = "imports/";

    private Boolean excludeClass;

    @Parameterized.Parameters
    public static Collection<Object[]> excludeClassYesNo()
    {
        return Arrays.asList(new Object[][] { { true }, { false } });
    }

    public TestCollectImports(final Boolean excludeClass)
    {
        this.excludeClass = excludeClass;
    }

    @Test
    public void import_constructor_signatures() throws Exception
    {
        if (excludeClass)
        {
            config.addClassExclude("foo.Baz");
        }

        assertCompileTestFileSuccess(IMPORTS_TEST_DIR);

        //client.emit();

        ClassReference importConstructorSignature = model.getClassReference("ImportConstructorSignature");
        assertNotNull(importConstructorSignature);

        assertFalse(importConstructorSignature.hasImport("Number"));
        assertFalse(importConstructorSignature.hasImport("foo.Qux"));

        assertTrue(importConstructorSignature.hasImport("foo.Bar"));

        if (excludeClass)
        {
            assertFalse(importConstructorSignature.hasImport("foo.Baz"));
        }
        else
        {
            assertTrue(importConstructorSignature.hasImport("foo.Baz"));
        }
    }

    @Test
    public void import_method_signatures() throws Exception
    {
        if (excludeClass)
        {
            config.addClassExclude("foo.Qux");
        }

        assertCompileTestFileSuccess(IMPORTS_TEST_DIR);

        //client.emit();

        ClassReference importMethodSignature = model.getClassReference("ImportMethodSignature");
        assertNotNull(importMethodSignature);

        assertFalse(importMethodSignature.hasImport("Number"));
        assertFalse(importMethodSignature.hasImport("foo.Quux"));
        assertFalse(importMethodSignature.hasImport("foo.Quuux"));

        assertTrue(importMethodSignature.hasImport("foo.Bar"));
        assertTrue(importMethodSignature.hasImport("foo.Baz"));

        if (excludeClass)
        {
            assertFalse(importMethodSignature.hasImport("foo.Qux"));
        }
        else
        {
            assertTrue(importMethodSignature.hasImport("foo.Qux"));
        }
    }

    @Test
    public void import_interfaces() throws Exception
    {
        if (excludeClass)
        {
            config.addClassExclude("API.foo.Baz");
        }

        assertCompileTestFileSuccess(IMPORTS_TEST_DIR);

        //client.emit();

        ClassReference importInterfaces = model.getClassReference("ImportInterfaces");
        assertNotNull(importInterfaces);

        assertFalse(importInterfaces.hasImport("qux"));
        assertTrue(importInterfaces.hasImport("API.Foo"));

        ClassReference apiFoo = model.getClassReference("API.Foo");
        assertNotNull(apiFoo);

        assertFalse(apiFoo.hasImport("qux"));
        assertFalse(apiFoo.hasImport("API.Bar"));

        if (excludeClass)
        {
            assertFalse(apiFoo.hasImport("API.foo.Baz"));
        }
        else
        {
            assertTrue(apiFoo.hasImport("API.foo.Baz"));
        }
    }

    @Test
    public void import_superclasses() throws Exception
    {
        if (excludeClass)
        {
            config.addClassExclude("BASE.Foo");
        }

        assertCompileTestFileSuccess(IMPORTS_TEST_DIR);

        //client.emit();

        ClassReference importSuperClass1 = model.getClassReference("ImportSuperClass1");
        assertNotNull(importSuperClass1);

        assertFalse(importSuperClass1.hasImport("qux"));

        ClassReference importSuperClass2 = model.getClassReference("ImportSuperClass2");
        assertNotNull(importSuperClass2);

        if (excludeClass)
        {
            assertFalse(importSuperClass2.hasImport("BASE.Foo"));
        }
        else
        {
            assertTrue(importSuperClass2.hasImport("BASE.Foo"));
        }

        ClassReference foo = model.getClassReference("BASE.Foo");
        assertNotNull(foo);

        assertFalse(foo.hasImport("BASE.Bar"));
    }

    @Test
    public void import_functions() throws Exception
    {
        if (excludeClass)
        {
            config.addClassExclude("foo.Qux");
        }

        assertCompileTestFileSuccess(IMPORTS_TEST_DIR);

        //client.emit();

        FunctionReference importFunction = (FunctionReference) model.getFunctions().toArray()[0];
        assertNotNull(importFunction);
        assertTrue(importFunction.getQualifiedName().equals("ImportFunction"));

        assertFalse(importFunction.hasImport("Quux"));

        assertTrue(importFunction.hasImport("foo.Bar"));
        assertTrue(importFunction.hasImport("foo.Baz"));

        if (excludeClass)
        {
            assertFalse(importFunction.hasImport("foo.Qux"));
        }
        else
        {
            assertTrue(importFunction.hasImport("foo.Qux"));
        }
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
        //config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);
    }

}
