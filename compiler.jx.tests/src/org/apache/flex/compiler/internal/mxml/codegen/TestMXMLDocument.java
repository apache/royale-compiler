package org.apache.flex.compiler.internal.mxml.codegen;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestWalkerBase;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.FlexProjectConfigurator;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.EnvProperties;
import org.apache.flex.compiler.visitor.IASBlockWalker;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Test;

public class TestMXMLDocument extends TestWalkerBase
{

    private static EnvProperties env = EnvProperties.initiate();

    private IMXMLBlockWalker walker;

    @Override
    public void setUp()
    {
        super.setUp();

        IASBlockWalker asWalker = (IASBlockWalker) visitor;
        walker = backend.createMXMLWalker(asWalker.getEmitter(), project,
                errors);
    }

    @Test
    public void MXMLClassNode_flashDisplaySprite()
    {
        IMXMLFileNode node = _getMXMLFileNode("");

        walker.visitFile(node);
    }

    // <component className="org.apache.flex.core:Application" name="Application" uri="library://ns.apache.org/flexjs/basic"  />

    protected IMXMLFileNode _getMXMLFileNode(String code)
    {
        assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
        assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set",
                env.FPSDK);

        project = new FlexProject(workspace);
        FlexProjectConfigurator.configure(project);

        String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists

        //        File tempMXMLFile = null;
        //        try
        //        {
        //            tempMXMLFile = File.createTempFile(getClass().getSimpleName(),
        //                    ".mxml", new File(tempDir));
        //            tempMXMLFile.deleteOnExit();
        //
        //            BufferedWriter out = new BufferedWriter(
        //                    new FileWriter(tempMXMLFile));
        //            out.write(code);
        //            out.close();
        //        }
        //        catch (IOException e1)
        //        {
        //            e1.printStackTrace();
        //        }

        List<File> sourcePath = new ArrayList<File>();
        sourcePath.add(new File(tempDir, "src"));
        project.setSourcePath(sourcePath);

        // Compile the code against playerglobal.swc.
        List<File> libraries = new ArrayList<File>();
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "\\11.1\\playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\rpc.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\spark.swc")));

        // asjs library

        libraries.add(new File(tempDir, "libs/FlexJSUI.swc"));

        project.setLibraries(libraries);

        // Use the MXML 2009 manifest.
        List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
        IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
                "http://ns.adobe.com/mxml/2009", env.SDK
                        + "\\frameworks\\mxml-2009-manifest.xml");

        IMXMLNamespaceMapping flexJS = new MXMLNamespaceMapping(
                "library://ns.apache.org/flexjs/basic", new File(tempDir,
                        "libs/manifest.xml").getAbsolutePath());

        namespaceMappings.add(mxml2009);
        namespaceMappings.add(flexJS);
        project.setNamespaceMappings(namespaceMappings);

        ICompilationUnit cu = null;
        File mainMXMLFile = new File(tempDir, "src/FlexJSTest.mxml");
        String normalizedMainFileName = FilenameNormalization
                .normalize(mainMXMLFile.getAbsolutePath());

        Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                .getCompilationUnits(normalizedMainFileName, project);

        List<ICompilationUnit> root = new ArrayList<ICompilationUnit>();
        root.add(mainFileCompilationUnits.iterator().next());

        for (ICompilationUnit cu2 : mainFileCompilationUnits)
        {
            if (cu2 != null)
                cu = cu2;
        }

        //List<ICompilationUnit> list = project
        //        .getReachableCompilationUnitsInSWFOrder(mainFileCompilationUnits);

        // Build the AST.
        IMXMLFileNode fileNode = null;
        try
        {
            fileNode = (IMXMLFileNode) cu.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
