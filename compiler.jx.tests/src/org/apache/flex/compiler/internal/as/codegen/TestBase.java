package org.apache.flex.compiler.internal.as.codegen;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.FlexProjectConfigurator;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.EnvProperties;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class TestBase
{
    protected List<ICompilerProblem> errors;

    private static EnvProperties env = EnvProperties.initiate();

    protected static Workspace workspace = new Workspace();

    protected FlexProject project;

    @Before
    public void setUp()
    {
        assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
        assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set",
                env.FPSDK);

        errors = new ArrayList<ICompilerProblem>();

        project = new FlexProject(workspace);
        FlexProjectConfigurator.configure(project);
    }

    protected IASNode findFirstDescendantOfType(IASNode node,
            Class<? extends IASNode> nodeType)
    {
        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IASNode child = node.getChild(i);
            if (child instanceof FunctionNode)
            {
                ((FunctionNode) child).parseFunctionBody(errors);
            }
            if (nodeType.isInstance(child))
                return child;

            IASNode found = findFirstDescendantOfType(child, nodeType);
            if (found != null)
                return found;
        }

        return null;
    }

    protected IFileNode getFileNode(String input)
    {
        return getFileNode(input, false);
    }

    protected IFileNode getFileNode(String input, boolean isFileName)
    {
        return getFileNode(input, isFileName, "");
    }

    protected IFileNode getFileNode(String input, boolean isFileName,
            String inputDir)
    {
        String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists

        File tempASFile = null;
        try
        {
            String tempFileName = (isFileName) ? input : getClass()
                    .getSimpleName();

            tempASFile = File.createTempFile(tempFileName, ".as", new File(
                    tempDir));
            tempASFile.deleteOnExit();

            String code = "";
            if (!isFileName)
            {
                code = input;
            }
            else
            {
                code = getCodeFromFile(input, false, inputDir);
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(tempASFile));
            out.write(code);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        List<File> sourcePaths = new ArrayList<File>();
        sourcePaths.add(new File(tempDir));
        project.setSourcePath(sourcePaths);

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
        project.setLibraries(libraries);

        // Use the MXML 2009 manifest.
        List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
        IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
                "http://ns.adobe.com/mxml/2009", env.SDK
                        + "\\frameworks\\mxml-2009-manifest.xml");
        namespaceMappings.add(mxml2009);
        project.setNamespaceMappings(namespaceMappings);

        ICompilationUnit cu = null;
        String normalizedMainFileName = FilenameNormalization
                .normalize(tempASFile.getAbsolutePath());

        SourceCompilationUnitFactory compilationUnitFactory = project
                .getSourceCompilationUnitFactory();
        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                    .getCompilationUnits(normalizedMainFileName, project);
            for (ICompilationUnit cu2 : mainFileCompilationUnits)
            {
                if (cu2 != null)
                    cu = cu2;
            }
        }

        // Build the AST.
        IFileNode fileNode = null;
        try
        {
            fileNode = (IFileNode) cu.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    protected String getCodeFromFile(String fileName, boolean isJS,
            String sourceDir)
    {
        String testFileDir = FilenameNormalization.normalize("test-files");

        File testFile = new File(testFileDir
                + File.separator + sourceDir + File.separator + fileName
                + (isJS ? ".js" : ".as"));

        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(testFile), "UTF8"));

            String line = in.readLine();

            while (line != null)
            {
                code += line + "\n";
                line = in.readLine();
            }
            code = code.substring(0, code.length() - 1); // (erikdebruin) remove last line break

            in.close();
        }
        catch (Exception e)
        {
        }

        return code;
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    private static final int WRAP_LEVEL_MEMBER = 3;
    private static final int WRAP_LEVEL_CLASS = 2;
    private static final int WRAP_LEVEL_PACKAGE = 1;

    protected IASNode getNode(String code, Class<? extends IASNode> type)
    {
        return getNode(code, type, WRAP_LEVEL_MEMBER, false);
    }

    protected IASNode getNode(String code, Class<? extends IASNode> type,
            int wrapLevel)
    {
        return getNode(code, type, wrapLevel, false);
    }

    protected IASNode getNode(String code, Class<? extends IASNode> type,
            int wrapLevel, boolean includePackage)
    {
        if (wrapLevel == WRAP_LEVEL_MEMBER)
            code = "function a():void {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_CLASS)
            code = "public class A {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_PACKAGE)
            code = "package"
                    + ((includePackage) ? " foo.bar" : "") + " {" + code + "}";

        IFileNode node = getFileNode(code);

        if (type.isInstance(node))
            return node;

        return (IASNode) findFirstDescendantOfType(node, type);
    }

    protected IInterfaceNode getInterfaceNode(String code)
    {
        return (IInterfaceNode) getNode(code, IInterfaceNode.class,
                WRAP_LEVEL_PACKAGE);
    }

    protected IAccessorNode getAccessor(String code)
    {
        return (IAccessorNode) getNode(code, IAccessorNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IVariableNode getField(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IFunctionNode getMethod(String code)
    {
        return (IFunctionNode) getNode(code, IFunctionNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IFunctionNode getMethodWithPackage(String code)
    {
        return (IFunctionNode) getNode(code, IFunctionNode.class,
                WRAP_LEVEL_CLASS, true);
    }

    protected IExpressionNode getExpressionNode(String code,
            Class<? extends IASNode> type)
    {
        return (IExpressionNode) getNode(code, type);
    }

    protected IBinaryOperatorNode getBinaryNode(String code)
    {
        return (IBinaryOperatorNode) getNode(code, IBinaryOperatorNode.class);
    }

    protected IDynamicAccessNode getDynamicAccessNode(String code)
    {
        return (IDynamicAccessNode) getNode(code, IDynamicAccessNode.class);
    }

    protected IUnaryOperatorNode getUnaryNode(String code)
    {
        return (IUnaryOperatorNode) getNode(code, IUnaryOperatorNode.class);
    }

    protected IVariableNode getVariable(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class);
    }
}
