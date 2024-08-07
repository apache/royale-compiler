/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.tree.as;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Ignore;

@Ignore
public class ASTestBase
{
    protected static Workspace workspace = new Workspace();
    protected RoyaleProject project;
	
	protected String[] errorFilters;

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    protected static final int WRAP_LEVEL_MEMBER = 3;
    protected static final int WRAP_LEVEL_CLASS = 2;
    protected static final int WRAP_LEVEL_PACKAGE = 1;
    protected static final int WRAP_LEVEL_NONE = 0;

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
            code = "function royaleTest_a():void {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_CLASS)
            code = "public class RoyaleTest_A {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_PACKAGE)
            code = "package" + ((includePackage) ? " foo.bar" : "") + " {"
                    + code + "}";

        IFileNode node = getFileNode(code);

        if (type.isInstance(node))
            return node;

        return findFirstDescendantOfType(node, type);
    }

    protected IASNode findFirstDescendantOfType(IASNode node,
            Class<? extends IASNode> nodeType)
    {
        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IASNode child = node.getChild(i);
            if (child instanceof ImportNode)
                continue;   // not interested in these and they have BinaryOps inside
            if (child instanceof FunctionNode)
            {
                ((FunctionNode) child).parseFunctionBody(new ArrayList<ICompilerProblem>());
            }
            if (nodeType.isInstance(child))
                return child;

            IASNode found = findFirstDescendantOfType(child, nodeType);
            if (found != null)
                return found;
        }

        return null;
    }

    protected IClassNode getClassNode(String code)
    {
        return (IClassNode) getNode(code, IClassNode.class,
                WRAP_LEVEL_PACKAGE);
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

    protected IForLoopNode getForLoopNode(String code)
    {
        return (IForLoopNode) getNode(code, IForLoopNode.class);
    }

    protected INamespaceAccessExpressionNode getNamespaceAccessExpressionNode(
            String code)
    {
        return (INamespaceAccessExpressionNode) getNode(code,
                INamespaceAccessExpressionNode.class);
    }

    protected IDynamicAccessNode getDynamicAccessNode(String code)
    {
        return (IDynamicAccessNode) getNode(code, IDynamicAccessNode.class);
    }

    protected IUnaryOperatorNode getUnaryNode(String code)
    {
        return (IUnaryOperatorNode) getNode(code, IUnaryOperatorNode.class);
    }

    protected IUnaryOperatorNode getUnaryNode(String code, int wrapLevel)
    {
        return (IUnaryOperatorNode) getNode(code, IUnaryOperatorNode.class, wrapLevel);
    }

    protected IVariableNode getVariable(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class);
    }

    protected IASNode getLocalFunction(String code)
    {
        IFunctionNode method = (IFunctionNode) getNode(code, IFunctionNode.class);
        return (IFunctionNode) findFirstDescendantOfType(method, IFunctionNode.class);
    }

    protected IFileNode getFileNode(String code)
	{
    	return getFileNode(code, false);
	}
    
    protected IFileNode getFileNode(String code, boolean withFlex)
	{
		project = new RoyaleProject(workspace);
		RoyaleProjectConfigurator.configure(project);

		ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
		String tempDir = testAdapter.getTempDir();
				
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".as", new File(tempDir));
			tempMXMLFile.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(tempMXMLFile));
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

		List<File> libraries = testAdapter.getLibraries(withFlex);
		if (!withFlex)
		{
        	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
			libraries.add(new File(jsSwcPath));
        	String customSwcPath = FilenameNormalization.normalize("target/custom.swc");
			libraries.add(new File(customSwcPath));
		}
		project.setLibraries(libraries);
		
		// Use the MXML 2009 manifest.
		List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
		File mxml2009File = new File(testAdapter.getUnitTestBaseDir(), "mxml-2009-manifest.xml");
		if (!mxml2009File.exists())
			mxml2009File = new File(FilenameNormalization.normalize("src/test/resources/mxml-2009-manifest.xml"));
		if (withFlex)
			mxml2009File = new File(testAdapter.getFlexManifestPath("mxml-2009"));
		if (!mxml2009File.exists())
			System.out.println("could not find mxml-2009-manifest.xml");
		IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
		    "http://ns.adobe.com/mxml/2009", mxml2009File.getAbsolutePath());
		namespaceMappings.add(mxml2009);
		IMXMLNamespaceMapping custom = new MXMLNamespaceMapping(
			    "library://ns.apache.org/royale/test", new File(testAdapter.getUnitTestBaseDir(), "custom-manifest.xml").getAbsolutePath());
			namespaceMappings.add(custom);
		project.setNamespaceMappings(namespaceMappings);
				
		ICompilationUnit cu = null;
        String normalizedMainFileName = FilenameNormalization.normalize(tempMXMLFile.getAbsolutePath());
		//String normalizedMainFileName = FilenameNormalization.normalize("code.mxml");
        SourceCompilationUnitFactory compilationUnitFactory = project.getSourceCompilationUnitFactory();
        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            Collection<ICompilationUnit> mainFileCompilationUnits = workspace.getCompilationUnits(normalizedMainFileName, project);
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
			fileNode = (IFileNode)cu.getSyntaxTreeRequest().get().getAST();
			ISyntaxTreeRequestResult result = cu.getSyntaxTreeRequest().get();
			ICompilerProblem[] problems = result.getProblems();
			if (problems != null && problems.length > 0)
			{
				for (ICompilerProblem problem : problems)
				{
					String errorString = problem.toString();
					boolean unexpected = true;
					if (errorFilters != null)
					{
						for (String filter : errorFilters)
						{
							if (errorString.contains(filter))
								unexpected = false;			
						}
					}
					if (unexpected)
						System.out.printf("%s(%d): %s\n", problem.getSourcePath(), problem.getLine(), errorString);
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		errorFilters = null;
		return fileNode;
	}

}
