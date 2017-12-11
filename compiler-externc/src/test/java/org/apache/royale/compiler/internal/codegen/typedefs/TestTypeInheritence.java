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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

public class TestTypeInheritence extends TypedefsTestBase
{

    @Test
    public void test_superclasses() throws Exception
    {
        compile("type_inheritence.js");

        ClassReference EventTarget = model.getInterfaceReference("EventTarget");

        ClassReference Object = model.getClassReference("Object");
        ClassReference Foo = model.getClassReference("Foo");
        ClassReference Bar = model.getClassReference("Bar");
        ClassReference Baz = model.getClassReference("Baz");

        assertNotNull(Object);
        assertNotNull(EventTarget);
        assertNotNull(Foo);
        assertNotNull(Bar);
        assertNotNull(Baz);

        assertSame(EventTarget, Foo.getImplementedInterfaces().get(0));
        assertSame(Object, Foo.getSuperClass());
        assertSame(Foo, Bar.getSuperClass());
        assertSame(Bar, Baz.getSuperClass());

        List<ClassReference> superClasses = Baz.getSuperClasses();
        assertEquals(3, superClasses.size());
        assertSame(Bar, superClasses.get(0));
        assertSame(Foo, superClasses.get(1));
        assertSame(Object, superClasses.get(2));

        assertTrue(Foo.hasInstanceMethod("addEventListener"));

        // TODO (mschmalle) need to revisit interface method overload
        // XXX Since Foo implements EventTarget BUT changes it's signature, we have to
        // use EventTargt.addEventListener()'s signature
        String result = client.getEmitter().emit(
                Foo.getInstanceMethod("addEventListener"));
        assertEquals(
                "    /**\n     "
                        + "* @param opt_useCapture [(boolean|undefined)] \n     "
                        + "* @see [type_inheritence]\n     */\n"
                        + "    public function addEventListener(type:String, listener:Object, useCapture:Boolean):Object /* undefined */ "
                        + "{  return null; }\n", result);
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    	TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);
    }

}
