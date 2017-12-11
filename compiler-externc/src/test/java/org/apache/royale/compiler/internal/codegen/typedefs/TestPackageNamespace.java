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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

public class TestPackageNamespace extends TypedefsTestBase
{
    @Test
    public void test_pacakge1() throws IOException
    {
        compile("package_namespace.js");

        ClassReference reference1 = model.getClassReference("Foo");
        ClassReference reference2 = model.getClassReference("foo.bar.Baz");
        ClassReference reference3 = model.getClassReference("Goo");

        assertFalse(reference1.isQualifiedName());
        assertEquals("Foo", reference1.getBaseName());
        assertEquals("", reference1.getPackageName());
        assertEquals("Foo", reference1.getQualifiedName());

        assertTrue(reference2.isQualifiedName());
        assertEquals("Baz", reference2.getBaseName());
        assertEquals("foo.bar", reference2.getPackageName());
        assertEquals("foo.bar.Baz", reference2.getQualifiedName());

        assertFalse(reference3.isQualifiedName());
        assertEquals("Goo", reference3.getBaseName());
        assertEquals("", reference3.getPackageName());
        assertEquals("Goo", reference3.getQualifiedName());
    }

    @Override
    protected void configure(ExternCConfiguration config)
    {
    }

}
