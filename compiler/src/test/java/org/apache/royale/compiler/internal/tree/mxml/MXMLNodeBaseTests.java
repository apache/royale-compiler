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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.utils.*;
import org.junit.Ignore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JUnit tests for {@link MXMLNodeBase}.
 * 
 * @author Gordon Smith
 */
@Ignore
public class MXMLNodeBaseTests 
{

	protected static Workspace workspace = new Workspace();
	
	protected RoyaleProject project;
	
 	protected String[] getTemplate()
	{
 		// Tests of nodes for class-definition-level tags like <Declarations>,
 		// <Library>,  <Metadata>, <Script>, and <Style> use this document template.
 		// Tests for nodes produced by tags that appear at other locations
 		// override getTemplate() and getMXML().
		return new String[] 
		{
		    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009'",
		    "          xmlns:d='flash.display.*'>",
			"    %1",
		    "</d:Sprite>"
		};
    };
	
 	protected String[] getTemplateWithFlex()
	{
 		// Tests of nodes for class-definition-level tags like <Declarations>,
 		// <Library>,  <Metadata>, <Script>, and <Style> use this document template.
 		// Tests for nodes produced by tags that appear at other locations
 		// override getTemplate() and getMXML().
		return new String[] 
		{
		    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009'",
		    "          xmlns:d='flash.display.*'",
		    "          xmlns:s='library://ns.adobe.com/flex/spark'",
		    "          xmlns:mx='library://ns.adobe.com/flex/mx'>",
			"    %1",
		    "</d:Sprite>"
		};
    };
	
	protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, "\n    "));
        return mxml;
    }
	    
	protected String getMXMLWithFlex(String[] code)
    {
        String mxml = StringUtils.join(getTemplateWithFlex(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, "\n    "));
        return mxml;
    }
	    
    protected IMXMLFileNode getMXMLFileNode(String[] code)
    {
    	String mxml = getMXML(code);
    	return getMXMLFileNode(mxml, false);
    }

    protected IMXMLFileNode getMXMLFileNodeWithFlex(String[] code)
    {
    	String mxml = getMXMLWithFlex(code);
    	return getMXMLFileNode(mxml, true);
    }

    protected IMXMLFileNode getMXMLFileNode(String mxml)
	{
    	return getMXMLFileNode(mxml, false);
	}
    
    protected IMXMLFileNode getMXMLFileNode(String mxml, boolean withFlex)
	{
		project = new RoyaleProject(workspace);
		RoyaleProjectConfigurator.configure(project);

		ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
		String tempDir = testAdapter.getTempDir();
				
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".mxml", new File(tempDir));
			tempMXMLFile.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(tempMXMLFile));
		    out.write(mxml);
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
		project.setLibraries(libraries);
		
		// Use the MXML 2009 manifest.
		List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
		IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
		    "http://ns.adobe.com/mxml/2009", testAdapter.getFlexManifestPath("mxml-2009"));
		namespaceMappings.add(mxml2009);
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
		IMXMLFileNode fileNode = null;
		try
		{
			fileNode = (IMXMLFileNode)cu.getSyntaxTreeRequest().get().getAST();
			ISyntaxTreeRequestResult result = cu.getSyntaxTreeRequest().get();
			ICompilerProblem[] problems = result.getProblems();
			if (problems != null && problems.length > 0)
			{
				for (ICompilerProblem problem : problems)
					System.out.printf("%s(%d): %s\n", problem.getSourcePath(), problem.getLine(), problem.toString());
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return fileNode;
	}
    
	protected IASNode findFirstDescendantOfType(IASNode node, Class<? extends IASNode> nodeType)
	{
		int n = node.getChildCount();
		for (int i = 0; i < n; i++)
		{
			IASNode child = node.getChild(i);
			if (nodeType.isInstance(child))
				return child;
			
			IASNode found = findFirstDescendantOfType(child, nodeType);
			if (found != null)
				return found;
		}
		
		return null;
	}
}
