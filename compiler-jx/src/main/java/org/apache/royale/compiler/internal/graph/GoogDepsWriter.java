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
package org.apache.royale.compiler.internal.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.driver.js.JSCompilationUnit;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.MainDefinitionQNameProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;

import com.google.common.io.Files;
import com.google.debugging.sourcemap.FilePosition;
import com.google.debugging.sourcemap.SourceMapConsumerV3;
import com.google.debugging.sourcemap.SourceMapGeneratorV3;
import com.google.debugging.sourcemap.SourceMapParseException;

public class GoogDepsWriter {

    public GoogDepsWriter(File outputFolder, String mainClassName, JSGoogConfiguration config, List<ISWC> swcs)
	{
		this.outputFolderPath = outputFolder.getAbsolutePath();
		this.moduleOutput = config.getModuleOutput();
		this.mainName = mainClassName;
		removeCirculars = config.getRemoveCirculars();
		sourceMaps = config.getSourceMap();
		otherPaths = config.getSDKJSLib();
		verbose = config.isVerbose();
		otherPaths.add(new File(outputFolder.getParent(), "royale/Royale/src").getPath());
		this.swcs = swcs;
		if (verbose)
		{
			for (ISWC swc : swcs)
			{
				System.out.println("using SWC: " + swc.getSWCFile().getAbsolutePath());
			}
		}
	}
	
	private ProblemQuery problems;
	private String moduleOutput;
	private String outputFolderPath;
	private String mainName;
	private List<String> otherPaths;
	private List<ISWC> swcs;
	private boolean removeCirculars = false;
	private boolean sourceMaps = false;
	private boolean verbose = false;
	private ArrayList<GoogDep> dps;
	private DependencyGraph graph;
	private CompilerProject project;
	private ArrayList<String> staticInitializers;
	private ArrayList<String> staticInitializerOwners;
	
	private HashMap<String, GoogDep> depMap = new HashMap<String,GoogDep>();
	private HashMap<String, ICompilationUnit> requireMap = new HashMap<String, ICompilationUnit>();
	private HashMap<ICompilationUnit, String> requireMap2 = new HashMap<ICompilationUnit, String>();
	
	public boolean needCSS = false;
	
	public ArrayList<String> getListOfFiles(CompilerProject project, List<String> sourceExternFiles, ProblemQuery problems)
	{
		this.project = project;
		this.problems = problems;

		if (dps == null)
		{
			if (!buildDB())
				return null;
			dps = sort();
		}
		visited.clear();
		ArrayList<String> files = new ArrayList<String>();
		for (GoogDep gd : dps)
		{
			if (gd.fileInfo.isExtern)
				sourceExternFiles.add(gd.filePath);
			else
				files.add(gd.filePath);
			visited.put(gd.className, gd);
		}
		if (removeCirculars)
		{
			GoogDep mainDep = depMap.get(mainName);
			visited.put(mainName, mainDep);
			for (GoogDep gd : depMap.values())
			{
				if (!visited.containsKey(gd.className))
				{
					if (gd.fileInfo.isExtern)
						sourceExternFiles.add(gd.filePath);
					else
						files.add(gd.filePath);
				}
			}
			files.add(mainDep.filePath);
		}
		return files;
	}
	
	public String generateDeps(CompilerProject project, ProblemQuery problems) throws FileNotFoundException
	{
		this.project = project;
	    this.problems = problems;
	    if (dps == null)
	    {
	    	if (!buildDB())
	    		return "";
	    	dps = sort();
	    }
	    ArrayList<String> usedDeps = new ArrayList<String>();
	    ArrayList<String> addedDeps = new ArrayList<String>();
	    StringBuilder sb = new StringBuilder();
		int n = dps.size();
		for (int i = n - 1; i >= 0; i--)
		{
			GoogDep gd = dps.get(i);
			if (!isGoogClass(gd.className)) 
			{
				addedDeps.add(gd.filePath);
				if (removeCirculars)
				{
					ArrayList <String> deps = new ArrayList<String>();
					computeDeps(deps, gd, usedDeps);
					sb.append("goog.addDependency('")
						.append(relativePath(gd.filePath))
						.append("', ['")
						.append(gd.className)
						.append("'], [");
					appendDependencies(deps, sb);
					sb.append("]);\n");
				}
				else
				{
					sb.append("goog.addDependency('")
						.append(relativePath(gd.filePath))
						.append("', ['")
						.append(gd.className)
						.append("'], [");
					appendDependencies(gd.deps, sb);
					sb.append("]);\n");
				}
			}
		}
		if (removeCirculars)
		{
			StringBuilder mainDeps = new StringBuilder();
			GoogDep mainDep = depMap.get(mainName);
			mainDeps.append("goog.addDependency('").append(relativePath(mainDep.filePath)).append("', ['")
								.append(mainDep.className).append("'], [");
			ArrayList<String> restOfDeps = new ArrayList<String>();
			for (String dep: mainDep.deps)
			{
				if (isGoogProvided(dep))
				{
					restOfDeps.add(dep);
				}
			}
			if (mainDep.fileInfo.impls != null)
			{
				for (String dep: mainDep.fileInfo.impls)
				{
					if (isGoogProvided(dep))
					{
						restOfDeps.add(dep);
					}
				}				
			}
	        DependencyTypeSet dependencyTypes = DependencyTypeSet.allOf();
			// get the list of all units not referenced by other units
			for (GoogDep gd : depMap.values())
			{
				if (usedDeps.contains(gd.className))
					continue;
				
				if (gd.className.equals(mainName)) 
				{
					if (gd.fileInfo.impls != null)
					{
						for (String d : gd.fileInfo.impls)
						{
							if (!restOfDeps.contains(d) && !gd.fileInfo.isExtern && isGoogProvided(d) && !usedDeps.contains(d))
							{
								restOfDeps.add(d);
							}
						}
					}
					continue;
				}
				ICompilationUnit unit = requireMap.get(gd.className);
				if (unit == null)
				{
					if (!restOfDeps.contains(gd.className) && !gd.fileInfo.isExtern && isGoogProvided(gd.className) && !usedDeps.contains(gd.className))
					{
						restOfDeps.add(gd.className);
					}
					continue;
				}
				Set<ICompilationUnit> deps = graph.getDirectReverseDependencies(unit, dependencyTypes);
				if (deps.size() == 0)
				{
					if (!restOfDeps.contains(gd.className) && !gd.fileInfo.isExtern && isGoogProvided(gd.className) && !usedDeps.contains(gd.className))
					{
						restOfDeps.add(gd.className);
					}
				}
			}
			appendDependencies(restOfDeps, mainDeps);
			mainDeps.append("]);\n");
			sb.insert(0, mainDeps);
			sb.insert(0, "// generated by Royale\n");
			for (String dep : restOfDeps)
			{
				GoogDep gd = depMap.get(dep);
				if (gd == null)
				{
					//added this to prevent a NullPointerException when the
					//GoogDep is null. -JT
					problems.add(new FileNotFoundProblem(dep));
					continue;
				}
				if (addedDeps.contains(gd.filePath))
					continue;
				ArrayList<String> deps = new ArrayList<String>();
				computeDeps(deps, gd, usedDeps);
				sb.append("goog.addDependency('")
					.append(relativePath(gd.filePath))
					.append("', ['")
					.append(gd.className)
					.append("'], [");
				appendDependencies(deps, sb);
				sb.append("]);\n");
			}
			addRestOfDeps(mainDep, restOfDeps);
		}
		return sb.toString();
	}
	
	private void computeDeps(ArrayList<String> deps, GoogDep gd, ArrayList<String> usedDeps) {
		if (gd.fileInfo.impls != null)
		{
			//deps.addAll(gd.fileInfo.impls); // filter below to avoid impls-and-provides combination in same CU, e.g. via file-private classes extending other file-private classes
			for (String dep : gd.fileInfo.impls) {
				if (gd.fileInfo.provides != null &&
						gd.fileInfo.provides.contains(dep)) continue;
				deps.add(dep);
				if (!usedDeps.contains(dep))
					usedDeps.add(dep);
			}
		}
		if (gd.fileInfo.staticDeps != null)
		{
			for (String dep : gd.fileInfo.staticDeps)
			{
				if (!deps.contains(dep))
					deps.add(dep);
				if (!usedDeps.contains(dep))
					usedDeps.add(dep);
			}
		}
	}

	private boolean isGoogClass(String className)
	{
	    return className.startsWith("goog.");
	}
	
	private boolean buildDB()
	{
		staticInitializers = new ArrayList<String>();
		staticInitializerOwners = new ArrayList<String>();
		
		graph = new DependencyGraph();
		if (isGoogClass(mainName))
		{
			problems.add(new MainDefinitionQNameProblem("Google Closure Library", mainName));
			return false;
		}
		if (!isGoogProvided(mainName))
		{
			problems.add(new MainDefinitionQNameProblem("External Libraries", mainName));
			return false;
		}
		addDeps(mainName);
		return true;
	}
    
	private HashMap<String, GoogDep> visited = new HashMap<String, GoogDep>();
	
	public ArrayList<String> additionalHTML = new ArrayList<String>();
    
	private ArrayList<GoogDep> sort()
	{
		// first, promote all dependencies of classes used in static initializers to
		// the level of static dependencies since their constructors will be
		// run early.  This may need to be a full transitive walk someday.
		// This is because we move many goog.requires from the various classes
		// to the main app when removing circulars.  We only keep goog.requires
		// that Closure Compiler cares about, which are requires for @extends
		// and @implements.  However, as classes required by the application get
		// loaded, their static initializers will fail if we haven't already goog.required
		// the classes used by these static initializers, so we have to keep goog.requires
		// used by these static initializers in the class files.
		int n = staticInitializers.size();
		for (int i = 0; i < n; i++)
		{
			String staticClass = staticInitializers.get(i);
			String staticOwner = staticInitializerOwners.get(i);
			GoogDep info = depMap.get(staticClass);
			GoogDep ownerInfo = depMap.get(staticOwner);
			if (info != null && info.fileInfo != null && info.fileInfo.deps != null && 
					ownerInfo != null && ownerInfo.fileInfo != null)
			{
				if (ownerInfo.fileInfo.staticDeps == null)
					ownerInfo.fileInfo.staticDeps = new ArrayList<String>();
				for (String dep : info.fileInfo.deps)
				{
					if (!ownerInfo.fileInfo.staticDeps.contains(dep) 
							&& !isGoogClass(dep)
							&& !dep.equals(staticOwner))
					{
						ownerInfo.fileInfo.staticDeps.add(dep);
						if (verbose)
						{
							System.out.println(staticClass + " used in static initializer of " + staticOwner + " so make " + dep + " a static dependency");
						}
						// all things added here should get added to graph in sortFunction
					}
				}
			}
		}
		
		ArrayList<GoogDep> arr = new ArrayList<GoogDep>();
		GoogDep current = depMap.get(mainName);
		sortFunction(current, arr);
		if (removeCirculars)
		{
			ICompilationUnit mainUnit = requireMap.get(mainName);
			ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
			roots.add(mainUnit);
			requireMap.remove(mainName);
			
			if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.GOOG_DEPS) == CompilerDiagnosticsConstants.GOOG_DEPS)
			{
				Collection<ICompilationUnit> units = graph.getCompilationUnits();
				System.out.println("Contents of graph:");
				for (ICompilationUnit unit : units)
				{
					try {
						System.out.println(unit.getQualifiedNames().toString());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			}
			List<ICompilationUnit> order = graph.topologicalSort(requireMap.values());
			if (graph.lastCircularDependencyException != null)
			{
				problems.add(new UnexpectedExceptionProblem(graph.lastCircularDependencyException));
			}
			if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.GOOG_DEPS) == CompilerDiagnosticsConstants.GOOG_DEPS)
			{
				System.out.println("Contents of graph in order:");
				for (ICompilationUnit unit : order)
				{
					try {
						System.out.println(unit.getQualifiedNames().toString());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			}
			ArrayList<GoogDep> depsInOrder = new ArrayList<GoogDep>();
			for (ICompilationUnit unit : order)
			{
				String name = requireMap2.get(unit);
				if (isGoogClass(name)) continue;
				GoogDep dep = depMap.get(name);
				if (dep == null)
				{
					if (isGoogProvided(name))
					{
						System.out.println("No GoogDep for " + name);
						//added this to prevent a NullPointerException when the
						//GoogDep is null. -JT
						problems.add(new FileNotFoundProblem(name));
						continue;
					}
				}
				else
					depsInOrder.add(dep);
			}
			return depsInOrder;
		}
		return arr;
	}
	
	private void sortFunction(GoogDep current, List<GoogDep> arr)
	{
		visited.put(current.className, current);
		
		if (removeCirculars)
		{
			removeRequires(current);
		}
		if (verbose)
		{
			System.out.println("Dependencies calculated for '" + current.className + "'");
		}

		ICompilationUnit unit = null;
		
		if (removeCirculars)
		{
			unit = requireMap.get(current.className);
			if (unit == null)
			{
				unit = new JSCompilationUnit(project, current.filePath, DefinitionPriority.BasePriority.SOURCE_LIST, current.className);
				graph.addCompilationUnit(unit);
				requireMap.put(current.className, unit);
				requireMap2.put(unit, current.className);
			}
			if (current.fileInfo.deps == null)
				return;
		}
		
		ArrayList<String> impls = current.fileInfo.impls != null ? current.fileInfo.impls : null;
		if (impls != null)
		{
			for (String className : impls)
			{
				if (!isGoogClass(className))
				{
					GoogDep gd = depMap.get(className);
					if (gd != null)
					{
						if (removeCirculars)
						{
							ICompilationUnit base = requireMap.get(className);
							if (base == null)
							{
								base = new JSCompilationUnit(project, className, DefinitionPriority.BasePriority.SOURCE_LIST, className);
								graph.addCompilationUnit(base);
								requireMap.put(className, base);
								requireMap2.put(base, className);
							}
							if (verbose)
							{
								System.out.println(current.className + " depends on " + className);
							}
							graph.addDependency(unit, base, DependencyType.INHERITANCE);
						}
						if (!visited.containsKey(className))
						{
							sortFunction(gd, arr);
						}
					}
				}
			}
		}
		if (removeCirculars)
		{
			if (current.fileInfo.staticDeps != null && removeCirculars)
			{
				for (String staticDep : current.fileInfo.staticDeps)
				{
					ICompilationUnit base = requireMap.get(staticDep);
					if (base == null)
					{
						base = new JSCompilationUnit(project, staticDep, DefinitionPriority.BasePriority.SOURCE_LIST, staticDep);
						graph.addCompilationUnit(base);
						requireMap.put(staticDep, base);
						requireMap2.put(base, staticDep);
					}
					if (verbose)
					{
						System.out.println(current.className + " static initialization depends on " + staticDep);
					}
					graph.addDependency(unit, base, DependencyType.INHERITANCE);
				}
			}
		}
		ArrayList<String> deps = current.deps;
		for (String className : deps)
		{
			if (!isGoogClass(className) && isGoogProvided(className))
			{
				GoogDep gd = depMap.get(className);
				if (gd == null)
				{
					System.out.println("No GoogDep for " + className);
					throw new RuntimeException("Unable to find dependency information for class: " + className);
				}
				if (!visited.containsKey(className))
				{
					sortFunction(gd, arr);
				}
			}
		}
		arr.add(current);
	}
	
	private void addRestOfDeps(GoogDep main, List<String> restOfDeps)
	{
        List<String> fileLines;
		try {
			File mainFile = new File(main.filePath);
			fileLines = Files.readLines(mainFile, Charset.forName("utf8"));

			SourceMapConsumerV3 sourceMapConsumer = null;
			File sourceMapFile = null;
			if (sourceMaps)
			{
				sourceMapFile = new File(main.filePath + ".map");
				if (sourceMapFile.exists())
				{
					String sourceMapContents = FileUtils.readFileToString(sourceMapFile, Charset.forName("utf8"));
					sourceMapConsumer = new SourceMapConsumerV3();
					try
					{
						sourceMapConsumer.parse(sourceMapContents);
					}
					catch(SourceMapParseException e)
					{
						sourceMapConsumer = null;
					}
				}
			}

			// first scan requires in case this is a module and some have been externed
			int j = main.fileInfo.googProvideLine + 1;
			while (j < fileLines.size() && !fileLines.get(j).contains(JSGoogEmitterTokens.GOOG_REQUIRE.getToken()))
			{
				j++;
			}
			while (j < fileLines.size() && fileLines.get(j).contains(JSGoogEmitterTokens.GOOG_REQUIRE.getToken()))
			{
				String line = fileLines.get(j);
				int c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
				int c2 = line.indexOf(")");
                String s = line.substring(c + 14, c2 - 1);
                if (!isGoogProvided(s))
                {
                	fileLines.remove(j);
					sourceMapConsumer = removeLineFromSourceMap(sourceMapConsumer, mainFile.getName(), j);
                }
				else
				{
					j++;
				}
			}
			
			int n = restOfDeps.size();
			for (int i = n - 1; i >= 0; i--)
			{
				String dep = restOfDeps.get(i);
				StringBuilder lineBuilder = new StringBuilder();
				lineBuilder.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken())
					.append("('")
					.append(dep)
					.append("');");
				fileLines.add(main.fileInfo.googProvideLine + 1, lineBuilder.toString());
				sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, mainFile.getName(), main.fileInfo.googProvideLine + 1);
			}

			FileUtils.writeLines(mainFile, "utf8", fileLines);

			if (sourceMapConsumer != null)
			{
				String newSourceMap = sourceMapConsumerToString(sourceMapConsumer, mainFile.getName());
				FileUtils.write(sourceMapFile, newSourceMap, "utf8");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addDeps(String className)
	{
		if (depMap.containsKey(className) || isGoogClass(className) || !isGoogProvided(className))
		{
			return;
		}
		
		// build goog dependency list
		GoogDep gd = new GoogDep();
		gd.className = className;
		gd.filePath = getFilePath(className);
		if(gd.filePath.isEmpty()) {
			// TODO Send a ICompilerProblem instead.
			throw new RuntimeException("Unable to find JavaScript filePath for class: " + className);
		}
		depMap.put(gd.className, gd);
        List<String> fileLines;
		try {
			fileLines = Files.readLines(new File(gd.filePath), Charset.forName("utf8"));
            FileInfo fi = getFileInfo(fileLines, className);
			gd.fileInfo = fi;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (gd.fileInfo.impls != null)
		{
	        for (String dep : gd.fileInfo.impls)
	        {
	        	if (gd.fileInfo.provides != null &&
	        			gd.fileInfo.provides.contains(dep)) continue;
	            addDeps(dep);
	        }
		}
		gd.deps = new ArrayList<String>();
		if (gd.fileInfo.deps != null)
		{
	        for (String dep : gd.fileInfo.deps)
	        {
	        	gd.deps.add(dep);
	        	if (gd.fileInfo.provides != null &&
	        			gd.fileInfo.provides.contains(dep)) continue;
	            addDeps(dep);
	        }
	        if (gd.fileInfo.staticDeps != null)
	        {
		        for (String dep : gd.fileInfo.staticDeps)
		        {
		        	if (!gd.deps.contains(dep))
		        	{
		        		gd.deps.add(dep);
		        		if (gd.fileInfo.provides != null &&
		        			gd.fileInfo.provides.contains(dep)) continue;
		        		addDeps(dep);
		        	}
		        }
	        }
		}
	}
	
	void removeRequires(GoogDep gd)
	{
		String className = gd.className;
		
	    // remove requires that are not base classes and interfaces
	    try
        {
			gd = depMap.get(className);
			File depFile = new File(gd.filePath);
            List<String> fileLines = Files.readLines(depFile, Charset.forName("utf8"));
			ArrayList<String> finalLines = new ArrayList<String>();
			
			SourceMapConsumerV3 sourceMapConsumer = null;
			File sourceMapFile = null;
			if (sourceMaps)
			{
				sourceMapFile = new File(gd.filePath + ".map");
				if (sourceMapFile.exists())
				{
					String sourceMapContents = FileUtils.readFileToString(sourceMapFile, Charset.forName("utf8"));
					sourceMapConsumer = new SourceMapConsumerV3();
					try
					{
						sourceMapConsumer.parse(sourceMapContents);
					}
					catch(SourceMapParseException e)
					{
						sourceMapConsumer = null;
					}
				}
			}
            
            boolean firstDependency = true;
        	StringBuilder sb = new StringBuilder();
        	sb.append(JSGoogEmitterTokens.ROYALE_DEPENDENCY_LIST.getToken());
        	
        	ArrayList<String> writtenRequires = new ArrayList<String>();
//        	int staticDepsLine = -1;
        	int lastRequireLine = -1;
            FileInfo fi = gd.fileInfo;
//            int suppressCount = 0;
            int i = 0;
            int stopLine = fi.constructorLine;
            if (fi.constructorLine == -1) // standalone functions
            	stopLine = fi.googProvideLine + 4; // search a few more lines after goog.provide
            for (String line : fileLines)
            {
            	if (i < stopLine)
            	{
                    int c = line.indexOf(JSGoogEmitterTokens.ROYALE_DEPENDENCY_LIST.getToken());
                    if (c > -1)
                    	return; // already been processed
//                    c = line.indexOf(JSGoogEmitterTokens.ROYALE_STATIC_DEPENDENCY_LIST.getToken());
//                    if (c > -1)
//                    	staticDepsLine = i;
                    c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                    if (c > -1)
                    {
                    	lastRequireLine = i;
                        int c2 = line.indexOf(")");
                        String s = line.substring(c + 14, c2 - 1);
                        if (((gd.fileInfo.impls == null || !gd.fileInfo.impls.contains(s)) &&
                        		(gd.fileInfo.staticDeps == null || !gd.fileInfo.staticDeps.contains(s))) ||
                        		!isGoogProvided(s))
                        {
                        	// don't remove the require if some class needs it at static initialization
                        	// time
//                        	suppressCount++;
							if (verbose)
							{
								System.out.println(gd.filePath + " removing require: " + s);
							}
                    		if (!firstDependency)
                    			sb.append(",");
                    		sb.append(s);
							firstDependency = false;
							sourceMapConsumer = removeLineFromSourceMap(sourceMapConsumer, depFile.getName(), finalLines.size());
                        	continue;
	                    }
                        else
                        {
                        	writtenRequires.add(s);
                        }
                    }
            	}
				finalLines.add(line);
				//no need to call addLineToSourceMap here because we're
				//populating finalLines for the first time
                i++;
            }
            // add any static deps not already listed that were added by static initializers;
            if (gd.fileInfo.staticDeps != null)
            {
    			if (lastRequireLine == -1)
					lastRequireLine = gd.fileInfo.googProvideLine + 1;
            	for (String dep : gd.fileInfo.staticDeps)
            	{
            		if (!writtenRequires.contains(dep) && isGoogProvided(dep))
            		{
						StringBuilder lineBuilder = new StringBuilder();
						lineBuilder.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken())
							.append("('")
							.append(dep)
							.append("');");
            			finalLines.add(lastRequireLine++, lineBuilder.toString());
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, new File(gd.filePath).getName(), lastRequireLine);
            			if (verbose)
						{
							System.out.println("adding require for static dependency " + dep + " to " + className);
						}
            		}
				}
            }
            //if (suppressCount > 0)
            //{
            	if (fi.suppressLine > 0)
            	{
            		if (fi.suppressLine < fi.constructorLine || fi.constructorLine == -1) 
            		{
                		String line = finalLines.get(fi.suppressLine);
                		int c = line.indexOf("@suppress {");
                		if (c > -1)
                		{
                			if (!line.contains("missingRequire"))
                			{
                				line = line.substring(0, c) + "@suppress {missingRequire|" + line.substring(c + 11);
                				finalLines.remove(fi.suppressLine);
								finalLines.add(fi.suppressLine, line);
								//no need to call addLineToSourceMap or removeLineFromSourceMap here
                			}
                		}
                		else
                			System.out.println("Confused by @suppress in " + className);
                	}
                	else                		
                	{
                		// the @suppress was for the constructor or some other thing so add a top-level
                		// @suppress
                		if (fi.fileoverviewLine > -1)
                		{
                			// there is already a fileOverview but no @suppress
                			finalLines.add(fi.fileoverviewLine + 1, " *  @suppress {missingRequire}");
							sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.fileoverviewLine + 1);
                		}
                		else if (fi.googProvideLine > -1)
                		{
                			finalLines.add(fi.googProvideLine, " */");
                			finalLines.add(fi.googProvideLine, " *  @suppress {missingRequire}");
                			finalLines.add(fi.googProvideLine, " *  @fileoverview");
                			finalLines.add(fi.googProvideLine, "/**");
							sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
							sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
							sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
							sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
                		}
                		else
                		{
                			System.out.println("Confused by @suppress in " + className);
                		}
                	}
            	}
            	else
            	{
            		if (fi.fileoverviewLine > -1)
            		{
            			// there is already a fileoverview but no @suppress
            			finalLines.add(fi.fileoverviewLine + 1, " *  @suppress {missingRequire}");
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.fileoverviewLine + 1);
            		}
            		else if (fi.googProvideLine > -1)
            		{
            			finalLines.add(fi.googProvideLine, " */");
            			finalLines.add(fi.googProvideLine, " *  @suppress {missingRequire}");
            			finalLines.add(fi.googProvideLine, " *  @fileoverview");
            			finalLines.add(fi.googProvideLine, "/**");
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
						sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), fi.googProvideLine);
            		}
            		else
            		{
            			System.out.println("Confused by @suppress in " + className);
            		}                		
            	}
            //}

            sb.append("*/");
            finalLines.add(gd.fileInfo.googProvideLine + 1, sb.toString());
			sourceMapConsumer = addLineToSourceMap(sourceMapConsumer, depFile.getName(), gd.fileInfo.googProvideLine + 1);

			FileUtils.writeLines(depFile, "utf8", finalLines);

			if (sourceMapConsumer != null)
			{
				String newSourceMap = sourceMapConsumerToString(sourceMapConsumer, depFile.getName());
				FileUtils.write(sourceMapFile, newSourceMap, "utf8");
			}
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }		
	}

	String sourceMapConsumerToString(SourceMapConsumerV3 consumer, String file)
	{
		SourceMapGeneratorV3 generator = sourceMapConsumerToGenerator(consumer);
		StringBuilder builder = new StringBuilder();
		try
		{
			generator.appendTo(builder, file);
		}
		catch(IOException e)
		{
			return "";
		}
		return builder.toString();
	}

	private void appendExtraMappingToGenerator(SourceMapGeneratorV3 generator,
		String sourceName,
		String symbolName,
		FilePosition sourceStartPosition,
		FilePosition startPosition,
		FilePosition endPosition)
	{
		//add an extra mapping because there seems to be a bug in
		//SourceMapGeneratorV3's appendTo() that omits the last
		//entry, for some reason
		FilePosition newEndPosition = new FilePosition(endPosition.getLine(), endPosition.getColumn() + 1);
		generator.addMapping(sourceName, null, sourceStartPosition, endPosition, newEndPosition);
	}

	private SourceMapGeneratorV3 sourceMapConsumerToGenerator(SourceMapConsumerV3 consumer)
	{
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return generator;
	}

	class SourceMapEntryCounter implements SourceMapConsumerV3.EntryVisitor
	{
		private int count = 0;

		@Override
		public void visit(String sourceName,
			String symbolName,
			FilePosition sourceStartPosition,
			FilePosition startPosition,
			FilePosition endPosition) {
			count++;
		}
	}

	SourceMapConsumerV3 sourceMapGeneratorToConsumer(SourceMapGeneratorV3 generator, String fileName)
	{
		StringBuilder builder = new StringBuilder();
		try
		{
			generator.appendTo(builder, fileName);
		}
		catch(IOException e)
		{
			return null;
		}
		SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
		try
		{
			consumer.parse(builder.toString());
		}
		catch(SourceMapParseException e)
		{
			return null;
		}
		return consumer;
	}

	SourceMapConsumerV3 addLineToSourceMap(SourceMapConsumerV3 consumer, String sourceFileName, final int lineToAdd)
	{
		if (consumer == null)
		{
			return null;
		}
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				if(startPosition.getLine() >= lineToAdd)
				{
					startPosition = new FilePosition(startPosition.getLine() + 1, startPosition.getColumn());
					endPosition = new FilePosition(endPosition.getLine() + 1, endPosition.getColumn());
				}
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return sourceMapGeneratorToConsumer(generator, sourceFileName);
	}

	SourceMapConsumerV3 removeLineFromSourceMap(SourceMapConsumerV3 consumer, String sourceFileName, final int lineToRemove)
	{
		if (consumer == null)
		{
			return null;
		}
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				if(startPosition.getLine() == lineToRemove)
				{
					return;
				}
				if(startPosition.getLine() > lineToRemove)
				{
					startPosition = new FilePosition(startPosition.getLine() - 1, startPosition.getColumn());
				}
				if(endPosition.getLine() > lineToRemove)
				{
					endPosition = new FilePosition(endPosition.getLine() - 1, endPosition.getColumn());
				}
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return sourceMapGeneratorToConsumer(generator, sourceFileName);
	}
		
	FileInfo getFileInfo(List<String> lines, String className)
	{
		FileInfo fi = new FileInfo();
		
		int numProvides = 0;
		int constructorCount = 0;
	    int n = lines.size();
	    fi.constructorLine = -1;
	    fi.suppressLine = -1;
	    fi.fileoverviewLine = -1;
		fi.googProvideLine = -1;
		boolean inInjectScript = false;
	    for (int i = 0; i < n; i++)
	    {
	        String line = lines.get(i);
	        int c2;
	        int c = line.indexOf("*/");
	        if (c > -1 && constructorCount > 0 && constructorCount == numProvides)
	        {
                break;
	        }
	        else
	        {
		        if (inInjectScript)
	            {
	                if (line.indexOf("</inject_script>") > -1)
	                {
	                    inInjectScript = false;
	                    continue;
	                }
	            	line = line.trim();
	            	if (line.startsWith("*"))
	            		line = line.substring(1);
				    additionalHTML.add(line);
				    continue;
	            }
                c = line.indexOf("<inject_script>");
                if (c > -1)
                {
                    inInjectScript = true;
                }
		        c = line.indexOf("@constructor");
		        if (c > -1)
		        {
		        	if (fi.constructorLine == -1)
		        		fi.constructorLine = i;
		        	constructorCount++;
		        		
		        }
		        else
		        {
			        c = line.indexOf("@interface");
			        if (c > -1)
			        	fi.constructorLine = i;
			        else
			        {
			        	c = line.indexOf("@suppress");
			        	if (c > -1)
			        		fi.suppressLine = i;
			        	else
			        	{
				        	c = line.indexOf("@fileoverview");
				        	if (c > -1)
				        		fi.fileoverviewLine = i;
				        	else
				        	{
					        	c = line.indexOf("goog.provide");
					        	if (c > -1)
					        	{
					        		if (fi.googProvideLine == -1)
					        			fi.googProvideLine = i;
					        		if (numProvides > 0)
					        		{
					        			if (fi.provides == null)
					        				fi.provides = new ArrayList<String>();
					        			c2 = line.indexOf(")", c);
					        			String provide = line.substring(c + 14, c2 - 1);
					        			fi.provides.add(provide);
					        		}
					        		numProvides++;
					        	}
					        	else
					        	{
					        		c = line.indexOf("@implements");
					        		if (c > -1)
					        		{
					        			if (fi.impls == null)
					        				fi.impls = new ArrayList<String>();
					        			c2 = line.indexOf("}", c);
					        			String impl = line.substring(c + 13, c2);
					        			if (!fi.impls.contains(impl) && !impl.contentEquals(className))
					        				fi.impls.add(impl);
					        			if (impl.equals("org.apache.royale.core.ICSSImpl"))
					        				needCSS = true;
					        		}
					        		else
					        		{
						        		c = line.indexOf("@extends");
						        		if (c > -1)
						        		{
						        			if (fi.impls == null)
						        				fi.impls = new ArrayList<String>();
						        			c2 = line.indexOf("}", c);
						        			String impl = line.substring(c + 10, c2);
						        			if (!fi.impls.contains(impl) && !impl.contentEquals(className))
						        				fi.impls.add(impl);
						        		}
						        		else
						        		{
						        			String token = JSGoogEmitterTokens.ROYALE_STATIC_DEPENDENCY_LIST.getToken();
						    				c = line.indexOf(token);
						    				if (c > -1)
						    				{
						    					c2 = line.indexOf("*/");
						    					line = line.substring(c + token.length(), c2);
						    					List<String> staticDeps = Arrays.asList(line.split(","));
							        			fi.staticDeps = new ArrayList<String>();
						    					fi.staticDeps.addAll(staticDeps);
						    					for (String staticDep : staticDeps)
						    					{
						    						if (staticDep.equals(className))
						    							continue;
						    						staticInitializers.add(staticDep);
						    						staticInitializerOwners.add(className);
						    					}
						    				}
						    				else
						    				{
						    					c = line.indexOf("@externs");
						    					if (c > -1)
						    					{
						    						fi.isExtern = true;
						    					}
						    					else
						    					{
								        			token = JSGoogEmitterTokens.ROYALE_DEPENDENCY_LIST.getToken();
								    				c = line.indexOf(token);
								    				if (c > -1)
								    				{
								    					c2 = line.indexOf("*/");
								    					line = line.substring(c + token.length(), c2);
									        			fi.deps = new ArrayList<String>();
									        			if (line.length() > 2) // don't add blank or space if no deps
									        				fi.deps.addAll(Arrays.asList(line.split(",")));
//								    					fi.depsLine = i;
								    				}
								    				else /* if (fi.depsLine == 0) */
								    				{
								    					token = JSGoogEmitterTokens.GOOG_REQUIRE.getToken();
								    					c = line.indexOf(token);
								    					if (c > -1)
								    					{
								                            c2 = line.indexOf(")");
								                            String s = line.substring(c + 14, c2 - 1);
								                            if (fi.deps == null)
								                            	fi.deps = new ArrayList<String>();
								                            fi.deps.add(s);
								    					}
								    				}
						    					}
							        		}
						        		}
					        		}
					        	}
				        	}
			        	}
			        }
		        }
	        }
	    }
	    if (fi.deps != null)
	    {
	    	Collections.sort(fi.deps);
	    }
	    if (fi.staticDeps != null)
	    {
	    	Collections.sort(fi.staticDeps);
	    }
	    return fi;
	}
	
	String getFilePath(String className)
	{
	    String fn;
	    File destFile;
	    File f;
	    
		String classPath = className.replace(".", File.separator);
        
        fn = outputFolderPath + File.separator + classPath + ".js";
        f = new File(fn);
        if (f.exists())
        {
            return fn;
        }
        
        for (String otherPath : otherPaths)
        {
    		fn = otherPath + File.separator + classPath + ".js";
    		f = new File(fn);
    		if (f.exists())
    		{
    			fn = outputFolderPath + File.separator + classPath + ".js";
    			destFile = new File(fn);
    			// copy source to output
    			try {
    				FileUtils.copyFile(f, destFile);
    				
    				// (erikdebruin) copy class assets files
    				if (className.contains("org.apache.royale"))
    				{
    				    File assetsDir = new File(f.getParentFile(), "assets");
    				    if (assetsDir.exists())
    				    {
    				        String nameOfClass = className.substring(className.lastIndexOf('_') + 1);
    				        
    				        File[] assetsList = assetsDir.listFiles();
					        assert assetsList != null;
					        for (File assetFile : assetsList) {
						        String assetFileName = assetFile.getName();

						        if (assetFile.isFile() && assetFileName.indexOf(nameOfClass) == 0) {
							        String pathOfClass;
							        pathOfClass = className.substring(0, className.lastIndexOf('_'));
							        pathOfClass = pathOfClass.replace(".", File.separator);

							        destFile = new File(outputFolderPath +
									        File.separator + pathOfClass +
									        File.separator + "assets" +
									        File.separator + assetFileName);
							        FileUtils.copyFile(assetFile, destFile);

							        destFile = new File(outputFolderPath.replace("js-debug", "js-release") +
									        File.separator + pathOfClass +
									        File.separator + "assets" +
									        File.separator + assetFileName);
							        FileUtils.copyFile(assetFile, destFile);

							        if (verbose)
									{
										System.out.println("Copied assets of the '" + nameOfClass + "' class");
									}
						        }
					        }
    				    }
    				}
    			} catch (IOException e) {
    				System.out.println("Error copying file for class: " + className);
    			}
    			return fn;
    		}
        }

        for (ISWC swc : swcs)
        {
			ISWCFileEntry fileEntry = getFileEntry(swc, className);
    		if (fileEntry != null)
    		{
    			fn = outputFolderPath + File.separator + classPath + ".js";
    			destFile = new File(fn);
    			// copy source to output
    			try {
    				InputStream inStream = fileEntry.createInputStream();
    				OutputStream outStream = FileUtils.openOutputStream(destFile);
    				byte[] b = new byte[1024 * 1024];
    				int bytes_read;
    				while ((bytes_read = inStream.read(b)) != -1)
    				{
        				outStream.write(b, 0, bytes_read);
    				}
    				outStream.flush();
    				outStream.close();    					
					inStream.close();
					
					//if source maps requested, copy from the swc, if available
					if (sourceMaps)
					{
						ISWCFileEntry sourceMapFileEntry = getFileEntry(swc, className, ".js.map");
						if (sourceMapFileEntry != null)
						{
							String sourceMapFn = outputFolderPath + File.separator + classPath + ".js.map";
							File sourceMapDestFile = new File(sourceMapFn);
							inStream = sourceMapFileEntry.createInputStream();
							outStream = FileUtils.openOutputStream(sourceMapDestFile);
							b = new byte[1024 * 1024];
							while ((bytes_read = inStream.read(b)) != -1)
							{
								outStream.write(b, 0, bytes_read);
							}
							outStream.flush();
							outStream.close();    					
							inStream.close();
						}
					}

    				// (erikdebruin) copy class assets files
    				if (className.contains("org.apache.royale"))
    				{
    					Map<String, ISWCFileEntry> includedfiles = swc.getFiles();
    					Set<String> includedList = includedfiles.keySet();
    					for (String included : includedList)
    					{
    						if (included.contains(".png") ||
    							included.contains(".gif") ||
    							included.contains(".jpg") ||
    							included.contains(".jpeg") ||
    							included.contains(".svg") ||
    							included.contains(".json"))
    						{
    							fileEntry = includedfiles.get(included);
    			    			String assetName = outputFolderPath + File.separator + included;
    			    			File assetFile = new File(assetName);
    		    				inStream = fileEntry.createInputStream();
    		    				outStream = FileUtils.openOutputStream(assetFile);
    		    				b = new byte[inStream.available()];
    		    				inStream.read(b);
    		    				outStream.write(b);
    		    				inStream.close();
    		    				outStream.flush();
    		    				outStream.close();
						        if (verbose)
								{
									System.out.println("Copied asset " + assetName);
								}
    						}
    					}
    				}
    			} catch (IOException e) {
    				System.out.println("Error copying file for class: " + className);
    			}
    			return fn;
    		}
        }
        
		System.out.println("Could not find file for class: " + className);
		problems.add(new FileNotFoundProblem(className));
		return "";
	}

	private ISWCFileEntry getFileEntry(ISWC swc, String className)
	{
		return getFileEntry(swc, className, ".js");
	}

	private ISWCFileEntry getFileEntry(ISWC swc, String className, String extension)
	{
		String fwdClassPath = className.replace(".", "/");
		String bckClassPath = className.replace(".", "\\");
		ISWCFileEntry fileEntry = swc.getFile("js/src/" + fwdClassPath + extension);
		if (fileEntry == null)
			fileEntry = swc.getFile("js/out/" + fwdClassPath + extension);
		if (fileEntry == null)
			fileEntry = swc.getFile("js/src/" + bckClassPath + extension);
		if (fileEntry == null)
			fileEntry = swc.getFile("js/out/" + bckClassPath + extension);
		if (fileEntry == null)
			fileEntry = swc.getFile("js\\src\\" + bckClassPath + extension);
		if (fileEntry == null)
			fileEntry = swc.getFile("js\\out\\" + bckClassPath + extension);
		return fileEntry;
	}
	
	/*
	private ArrayList<String> getDirectDependencies(String fn)
	{
		ArrayList<String> deps = new ArrayList<String>();
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(fn);
			Scanner scanner = new Scanner(fis, "UTF-8");
			boolean inInjectHTML = false;
			while (scanner.hasNextLine())
			{
				String s = scanner.nextLine();
				if (s.contains("goog.inherits"))
					break;
                if (inInjectHTML)
                {
                    int c = s.indexOf("</inject_html>");
                    if (c > -1)
                    {
                        inInjectHTML = false;
                        continue;
                    }
                }    
                if (inInjectHTML)
                {
                	s = s.trim();
                	if (s.startsWith("*"))
                		s = s.substring(1);
				    additionalHTML.add(s);
				    continue;
                }
                else
                	otherScanning(s);
				int c = s.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
				if (c > -1)
				{
					int c2 = s.indexOf(")");
					s = s.substring(c + 14, c2 - 1);
					deps.add(s);
				}
                c = s.indexOf("<inject_html>");
                if (c > -1)
                {
                    inInjectHTML = true;
                }
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return deps;
	}
	*/
	
	protected void otherScanning(String s)
	{		
	}
	
	private void appendDependencies(ArrayList<String> deps, StringBuilder builder)
	{
		boolean hasDeps = false;
		for (String dep : deps)
		{
			if (hasDeps)
			{
				builder.append(", ");
			}
			builder.append("'");
			builder.append(dep);
			builder.append("'");
			hasDeps = true;
		}
	}

	String relativePath(String path)
	{
		String replacement = "../../..";
        if (path.indexOf(outputFolderPath) == 0)
        {
            if (moduleOutput != null)
            {
            	replacement += moduleOutput;
            	if (replacement.endsWith("/"))
            		replacement = replacement.substring(0, replacement.length() - 1);
            }
        	path = path.replace(outputFolderPath, replacement);
        }
        else
        {
    	    for (String otherPath : otherPaths)
    	    {
        		if (path.indexOf(otherPath) == 0)
        		{
        			path = path.replace(otherPath, "../../..");
        			
        		}
    	    }
        }
		// paths are actually URIs and always have forward slashes
		path = path.replace('\\', '/');
		return path;
	}
	
	boolean isGoogProvided(String className)
	{
		return ((RoyaleJSProject)project).isGoogProvided(className);
	}
	
	boolean isExternal(String className)
	{
		ICompilationUnit cu = project.resolveQNameToCompilationUnit(className);
		if (cu == null) return false; // unit testing
		
		return ((RoyaleJSProject)project).isExternalLinkage(cu);
	}
	
	private class GoogDep
	{
		public String filePath;
		public String className;
		public ArrayList<String> deps;
		public FileInfo fileInfo;
		
	}
	
	private class FileInfo
	{
		public ArrayList<String> impls;
		public ArrayList<String> deps;
		public ArrayList<String> staticDeps;
		public ArrayList<String> provides;
		public int constructorLine;
//		public int depsLine;
		public int suppressLine;
		public int fileoverviewLine;
		public int googProvideLine;
		public boolean isExtern;
	}
}
