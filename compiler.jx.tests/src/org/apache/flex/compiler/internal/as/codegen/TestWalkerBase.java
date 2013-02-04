package org.apache.flex.compiler.internal.as.codegen;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.driver.ASBackend;
import org.apache.flex.compiler.visitor.IASBlockVisitor;
import org.junit.After;
import org.junit.Ignore;

@Ignore
public class TestWalkerBase extends TestBase
{
    protected IASBlockVisitor visitor;

    protected IBackend backend;
    
    protected IASEmitter emitter;

    protected ASFilterWriter writer;

    protected String mCode;

    @Override
    public void setUp()
    {
        super.setUp();

        backend = createBackend();
        writer = backend.createWriterBuffer(project);
        emitter = backend.createEmitter(writer);
        visitor = backend.createWalker(project, errors, emitter);
    }

    @After
    public void tearDown()
    {
        backend = null;
        writer = null;
        emitter = null;
        visitor = null;
    }

    protected IBackend createBackend()
    {
        return new ASBackend();
    }

    protected void assertOut(String code)
    {
        mCode = writer.toString();
        //System.out.println(mCode);
        assertThat(mCode, is(code));
    }
    
    @Override
    public String toString()
    {
        return writer.toString();
    }
}
