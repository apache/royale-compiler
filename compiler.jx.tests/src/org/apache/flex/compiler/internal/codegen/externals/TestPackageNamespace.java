package org.apache.flex.compiler.internal.codegen.externals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.internal.codegen.externals.reference.ClassReference;
import org.junit.Test;

public class TestPackageNamespace extends ExternalsTestBase
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
