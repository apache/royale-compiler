package org.apache.flex.compiler.internal.test;

import java.io.File;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.mxml.MXMLBackend;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class MXMLTestBase extends TestBase
{

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

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    public static final int WRAP_LEVEL_DOCUMENT = 1;
    public static final int WRAP_LEVEL_NODE = 2;

    protected IMXMLNode getNode(String code, Class<? extends IMXMLNode> type,
            int wrapLevel)
    {
        if (wrapLevel >= WRAP_LEVEL_NODE)
            code = "<s:Button " + code + "></s:Button>";

        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                    + " xmlns:s=\"library://ns.adobe.com/flex/spark\""
                    + " xmlns:mx=\"library://ns.adobe.com/flex/mx\">" + code
                    + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        if (wrapLevel >= WRAP_LEVEL_NODE) // for now: attributes
        {
            IMXMLNode pnode = findFirstDescendantOfType(node,
                    IMXMLPropertySpecifierNode.class);

            IMXMLNode cnode = findFirstDescendantOfType(pnode,
                    IMXMLPropertySpecifierNode.class);

            return (IMXMLNode) cnode;
        }
        else
        {
            return (IMXMLNode) findFirstDescendantOfType(node, type);
        }
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

            IMXMLNode found = (IMXMLNode) findFirstDescendantOfType(child,
                    nodeType);
            if (found != null)
                return found;
        }

        return null;
    }
}
