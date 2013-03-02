package org.apache.flex.compiler.internal.test;

import java.io.File;

import org.apache.flex.compiler.common.driver.IBackend;
import org.apache.flex.compiler.internal.mxml.driver.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class FlexJSTestBase extends TestBase
{

    @Override
    public void setUp()
    {
        super.setUp();

        mxmlEmitter = backend.createMXMLEmitter(writer);
        mxmlBlockWalker = backend
                .createMXMLWalker(project, errors, mxmlEmitter);
    }

    @Override
    public void addLibraries()
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/11.1/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\rpc.swc")));
        libraries.add(new File(
                "./test-files/flexjs/projects/FlexJSTest/libs/FlexJSUI.swc"));

        super.addLibraries();
    }

    @Override
    public void addNamespaceMappings()
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping(
                        "library://ns.apache.org/flexjs/basic",
                        new File(
                                "./test-files/flexjs/projects/FlexJSTest/libs/basic-manifest.xml")
                                .getAbsolutePath()));

        super.addNamespaceMappings();
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLFlexJSBackend();
    }

}
