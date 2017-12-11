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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

public class TestAnnotationEnum extends TypedefsTestBase
{
    @Test
    public void test_class_creation() throws IOException
    {
        compile("annotation_enum.js");

        ClassReference FontFaceLoadStatus = model.getClassReference("FontFaceLoadStatus");
        ClassReference FontFaceSetLoadStatus = model.getClassReference("FontFaceSetLoadStatus");
        assertNotNull(FontFaceLoadStatus);
        assertNotNull(FontFaceSetLoadStatus);

        assertTrue(FontFaceLoadStatus.hasStaticField("ERROR"));
        assertTrue(FontFaceLoadStatus.hasStaticField("LOADED"));
        assertTrue(FontFaceLoadStatus.hasStaticField("LOADING"));
        assertTrue(FontFaceLoadStatus.hasStaticField("UNLOADED"));

        assertTrue(FontFaceSetLoadStatus.hasStaticField("FOO_LOADED"));
        assertTrue(FontFaceSetLoadStatus.hasStaticField("FOO_LOADING"));

        assertTrue(FontFaceLoadStatus.getStaticField("ERROR").isConst());

        // TODO check values and value type IE String, Number

        //String emit1 = client.getEmitter().emit(FontFaceLoadStatus);
        //String emit2 = client.getEmitter().emit(FontFaceSetLoadStatus);
    }

    @Test
    public void test_qualified_enum() throws IOException
    {
        compile("annotation_enum.js");

        ClassReference QualifiedEnum = model.getClassReference("foo.bar.baz.QualifiedEnum");
        assertNotNull(QualifiedEnum);
        assertEquals("foo.bar.baz.QualifiedEnum",
                QualifiedEnum.getQualifiedName());
        assertEquals("foo.bar.baz", QualifiedEnum.getPackageName());
        assertEquals("QualifiedEnum", QualifiedEnum.getBaseName());

        assertTrue(QualifiedEnum.hasStaticField("One"));
        assertTrue(QualifiedEnum.hasStaticField("Two"));

        //String emit1 = client.getEmitter().emit(QualifiedEnum);
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    }
}
