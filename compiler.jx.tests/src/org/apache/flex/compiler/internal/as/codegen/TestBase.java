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

import org.apache.flex.compiler.clients.MXMLJSC;
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
    protected void compileJS(String path)
    {
        // Construct a command line which simply loads the project's config file.
        String playerglobalHome = System.getenv("PLAYERGLOBAL_HOME");
        assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set",
                playerglobalHome);

        String flexHome = System.getenv("FLEX_HOME");
        assertNotNull("Environment variable FLEX_HOME is not set", flexHome);

        String configFile = flexHome + "/frameworks/flex-config.xml";
        String[] args = new String[] { "-load-config=" + configFile,
                "+env.PLAYERGLOBAL_HOME=" + playerglobalHome,
                "+playerglobal.version=11.1",
                "-define=CONFIG::performanceInstrumentation,false", "" + path };

        MXMLJSC.main(args);
    }

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
        String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists

        File tempASFile = null;
        try
        {
        	String tempFileName = (isFileName) ? input : getClass().getSimpleName();
        	
        	tempASFile = File.createTempFile(tempFileName, ".as", new File(tempDir));
            tempASFile.deleteOnExit();
	
            String code = "";
            if (!isFileName)
            {
            	code = input;
            }
            else
            {
            	code = getCodeFromFile(input, false);
            }
            
            BufferedWriter out = new BufferedWriter(new FileWriter(tempASFile));
            out.write(code);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        List<File> sourcePath = new ArrayList<File>();
        sourcePath.add(new File(tempDir));
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
        
        addLibrary(libraries);
        
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
    
    protected void addLibrary(List<File> libraries)
    {
    }

    protected String getCodeFromFile(String fileName, boolean isJS)
    {
        String testFileDir = FilenameNormalization.normalize("test-files");
        
        File testFile = new File(testFileDir + "/" + fileName + (isJS ? ".js" : ".as"));
        
        String code = "";
        try
        {
        	BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF8"));
            
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

    protected IASNode getNode(String code, Class<? extends IASNode> type)
    {
        String source = "package {public class A {function a():void {" + code
                + "}}";
        IFileNode node = getFileNode(source);
        if (type.isInstance(node))
            return node;
        IASNode child = findFirstDescendantOfType(node, type);
        return child;
    }

    protected IExpressionNode getExpressionNode(String code,
            Class<? extends IASNode> type)
    {
        String source = "package {public class A {function a():void {" + code
                + "}}";
        IFileNode node = getFileNode(source);
        IExpressionNode child = (IExpressionNode) findFirstDescendantOfType(
                node, type);
        return child;
    }

    protected IAccessorNode getAccessor(String code)
    {
        String source = "package {public class A {" + code + "}}";
        IFileNode node = getFileNode(source);
        IAccessorNode child = (IAccessorNode) findFirstDescendantOfType(node,
                IAccessorNode.class);
        return child;
    }

    protected IBinaryOperatorNode getBinaryNode(String code)
    {
        String source = "package {public class A {function a():void {" + code
                + "}}";
        IFileNode node = getFileNode(source);
        IBinaryOperatorNode child = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        return child;
    }

    protected IDynamicAccessNode getDynamicAccessNode(String code)
    {
        String source = "package {public class A {function a():void {" + code
                + "}}";
        IFileNode node = getFileNode(source);
        IDynamicAccessNode child = (IDynamicAccessNode) findFirstDescendantOfType(
                node, IDynamicAccessNode.class);
        return child;
    }

    protected IVariableNode getField(String code)
    {
        String source = "package {public class A {" + code + "}}";
        IFileNode node = getFileNode(source);
        IVariableNode child = (IVariableNode) findFirstDescendantOfType(node,
                IVariableNode.class);
        return child;
    }

    protected IInterfaceNode getInterfaceNode(String code)
    {
        String source = "package {" + code + "}";
        IFileNode node = getFileNode(source);
        IInterfaceNode child = (IInterfaceNode) findFirstDescendantOfType(node,
                IInterfaceNode.class);
        return child;
    }

    protected IFunctionNode getMethod(String code)
    {
        String source = "package {public class A {" + code + "}}";
        IFileNode node = getFileNode(source);
        IFunctionNode child = (IFunctionNode) findFirstDescendantOfType(node,
                IFunctionNode.class);
        return child;
    }
    
    protected IFunctionNode getMethodWithPackage(String code)
    {
        String source = "package foo.bar {public class A {" + code + "}}";
        IFileNode node = getFileNode(source);
        IFunctionNode child = (IFunctionNode) findFirstDescendantOfType(node,
                IFunctionNode.class);
        return child;
    }

    protected IUnaryOperatorNode getUnaryNode(String code)
    {
        String source = "package {public class A {function a():void {" + code
                + "}}";
        IFileNode node = getFileNode(source);
        IUnaryOperatorNode child = (IUnaryOperatorNode) findFirstDescendantOfType(
                node, IUnaryOperatorNode.class);
        return child;
    }

    protected IVariableNode getVariable(String code)
    {
    	IVariableNode node = (IVariableNode) getNode(code, IVariableNode.class);
        return node;
    }
}
