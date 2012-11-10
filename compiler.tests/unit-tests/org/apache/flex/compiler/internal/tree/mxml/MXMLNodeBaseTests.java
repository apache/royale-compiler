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

package org.apache.flex.compiler.internal.tree.mxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.FlexProjectConfigurator;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLNodeBase}.
 * 
 * @author Gordon Smith
 */
public class MXMLNodeBaseTests
{
	private static final String SDK = FilenameNormalization.normalize("../compiler/generated/dist/sdk");

	protected static Workspace workspace = new Workspace();
	
	protected FlexProject project;
	
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
	
	protected IMXMLFileNode getMXMLFileNode(String code)
	{
		project = new FlexProject(workspace);
		FlexProjectConfigurator.configure(project);
		
		String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists
				
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile("MXMLBooleanNodeTests", ".mxml", new File(tempDir));
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

		// Compile the code against playerglobal.swc.
		List<File> libraries = new ArrayList<File>();
		libraries.add(new File(SDK + "\\frameworks\\libs\\player\\11.1\\playerglobal.swc"));
		libraries.add(new File(SDK + "\\frameworks\\libs\\framework.swc"));
		libraries.add(new File(SDK + "\\frameworks\\libs\\rpc.swc"));
		libraries.add(new File(SDK + "\\frameworks\\libs\\spark.swc"));
		project.setLibraries(libraries);
		
		// Use the MXML 2009 manifest.
		List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
		IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
		    "http://ns.adobe.com/mxml/2009", SDK + "\\frameworks\\mxml-2009-manifest.xml");
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
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return fileNode;
	}
	
	@Test
	public void MXMLNodeBase_placeholder()
	{
	}
}
