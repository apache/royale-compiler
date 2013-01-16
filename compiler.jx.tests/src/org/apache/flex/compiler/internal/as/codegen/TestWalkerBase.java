package org.apache.flex.compiler.internal.as.codegen;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.driver.ASBackend;
import org.apache.flex.compiler.visitor.IASBlockVisitor;
import org.junit.After;

public class TestWalkerBase extends TestBase
{
    protected IASBlockVisitor visitor;

    private IBackend backend;

    private ASFilterWriter writer;

    protected String mCode;

    @Override
    public void setUp()
    {
        super.setUp();

        backend = createBackend();
        writer = backend.createFilterWriter(project);
        visitor = backend.createWalker(project, errors, writer);
    }

    @After
    public void tearDown()
    {
        backend = null;
        writer = null;
        visitor = null;
    }

    protected IBackend createBackend()
    {
        return new ASBackend();
    }

    protected void assertOut(String code)
    {
        mCode = writer.toString();
        assertThat(writer.toString(), is(code));
    }
}
