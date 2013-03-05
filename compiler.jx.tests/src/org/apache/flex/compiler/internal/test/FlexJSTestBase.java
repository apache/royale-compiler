package org.apache.flex.compiler.internal.test;

import java.io.File;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class FlexJSTestBase extends TestBase
{

    // TODO (erikdebruin) handle this path more like env.SDK or something
    //                    similarly non-hard coded
    protected final String asjsRoot = "../../asjs/";

    @Override
    public void setUp()
    {
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        mxmlEmitter = backend.createMXMLEmitter(writer);

        asBlockWalker = backend.createWalker(project, errors, asEmitter);
        mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                mxmlEmitter, asEmitter, asBlockWalker);
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
        libraries.add(new File(asjsRoot + "frameworks/as/libs/FlexJSUI.swc"));

        super.addLibraries();
    }

    @Override
    public void addNamespaceMappings()
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping(
                        "library://ns.apache.org/flexjs/basic", new File(
                                asjsRoot + "frameworks/as/basic-manifest.xml")
                                .getAbsolutePath()));

        super.addNamespaceMappings();
    }

    @Override
    public void addSourcePaths()
    {
        sourcePaths.add(new File(asjsRoot + "examples/FlexJSTest_again"));

        super.addSourcePaths();
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLFlexJSBackend();
    }

}
