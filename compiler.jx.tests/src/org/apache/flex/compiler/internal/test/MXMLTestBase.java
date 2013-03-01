package org.apache.flex.compiler.internal.test;

import java.io.File;

import org.apache.flex.compiler.common.driver.IBackend;
import org.apache.flex.compiler.internal.mxml.driver.MXMLBackend;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class MXMLTestBase extends TestBase
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
                + "/frameworks/libs/mx.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/spark.swc")));

        super.addLibraries();
    }

    @Override
    public void addNamespaceMappings()
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping("http://ns.adobe.com/mxml/2009",
                        new File(env.SDK, "frameworks/mxml-2009-manifest.xml")
                                .getAbsolutePath()));
        namespaceMappings.add(new MXMLNamespaceMapping(
                "library://ns.adobe.com/flex/mx", new File(env.SDK,
                        "frameworks/mx-manifest.xml").getAbsolutePath()));
        namespaceMappings.add(new MXMLNamespaceMapping(
                "library://ns.adobe.com/flex/spark", new File(env.SDK,
                        "frameworks/spark-manifest.xml").getAbsolutePath()));

        super.addNamespaceMappings();
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLBackend();
    }

}
