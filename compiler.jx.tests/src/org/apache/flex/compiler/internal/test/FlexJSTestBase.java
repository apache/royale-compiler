package org.apache.flex.compiler.internal.test;

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class FlexJSTestBase extends TestBase
{

    @Override
    public void setUp()
    {
    	project = new FlexJSProject(workspace);
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        mxmlEmitter = backend.createMXMLEmitter(writer);

        asBlockWalker = backend.createWalker(project, errors, asEmitter);
        mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                mxmlEmitter, asEmitter, asBlockWalker);
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/11.1/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\rpc.swc")));
        libraries.add(new File(env.ASJS + "/frameworks/as/libs/FlexJSUI.swc"));

        super.addLibraries(libraries);
    }

    @Override
    protected void addNamespaceMappings(List<IMXMLNamespaceMapping> namespaceMappings)
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping(
                        "library://ns.apache.org/flexjs/basic", new File(
                                env.ASJS + "/frameworks/as/basic-manifest.xml")
                                .getAbsolutePath()));

        super.addNamespaceMappings(namespaceMappings);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(env.ASJS + "/examples/FlexJSTest_basic/src"));
        sourcePaths.add(new File(FilenameNormalization.normalize("test-files/flexjs/files")));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLFlexJSBackend();
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    public static final int WRAP_LEVEL_DOCUMENT = 1;

    protected IMXMLNode getNode(String code, Class<? extends IMXMLNode> type,
            int wrapLevel)
    {
        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                    + code + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        return findFirstDescendantOfType(node, type);
    }

    protected IMXMLNode findFirstDescendantOfType(IMXMLNode node,
            Class<? extends IMXMLNode> nodeType)
    {

        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IMXMLNode child = (IMXMLNode) node.getChild(i);
            if (nodeType.isInstance(child))
                return child;

            IMXMLNode found = findFirstDescendantOfType(child,
                    nodeType);
            if (found != null)
                return found;
        }

        return null;
    }

}
