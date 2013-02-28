package org.apache.flex.compiler.test;

import java.io.File;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.visitor.IASBlockWalker;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;
import org.junit.Ignore;

@Ignore
public class MXMLTestBase extends TestBase
{

    protected IMXMLBlockWalker walker;

    @Override
    public void setUp()
    {
        super.setUp();

        IASBlockWalker asWalker = (IASBlockWalker) visitor;
        walker = backend.createMXMLWalker(asWalker.getEmitter(), project,
                errors);
    }

    @Override
    public void addLibraries()
    {
        libraries.add(new File(tempDir, "libs/FlexJSUI.swc"));
        
        super.addLibraries();
    }

    @Override
    public void addNamespaceMappings()
    {
        namespaceMappings.add(new MXMLNamespaceMapping(
                "library://ns.apache.org/flexjs/basic", new File(tempDir,
                        "libs/manifest.xml").getAbsolutePath()));
        
        super.addNamespaceMappings();
    }
    
    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
