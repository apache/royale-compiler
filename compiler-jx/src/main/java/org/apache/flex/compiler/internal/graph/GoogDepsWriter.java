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
package org.apache.flex.compiler.internal.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.common.DependencyTypeSet;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.driver.js.JSCompilationUnit;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.DefinitionPriority;
import org.apache.flex.compiler.internal.projects.DependencyGraph;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.problems.FileNotFoundProblem;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swc.ISWCFileEntry;

import com.google.common.io.Files;

public class GoogDepsWriter {

    public GoogDepsWriter(File outputFolder, String mainClassName, JSGoogConfiguration config, List<ISWC> swcs)
	{
		this.outputFolderPath = outputFolder.getAbsolutePath();
		this.mainName = mainClassName;
		removeCirculars = config.getRemoveCirculars();
		otherPaths = config.getSDKJSLib();
		otherPaths.add(new File(outputFolder.getParent(), "flexjs/FlexJS/src").getPath());
		this.swcs = swcs;
		for (ISWC swc : swcs)
		{
			System.out.println("using SWC: " + swc.getSWCFile().getAbsolutePath());
		}
	}
	
	private ProblemQuery problems;
	private String outputFolderPath;
	private String mainName;
	private List<String> otherPaths;
	private List<ISWC> swcs;
	private boolean removeCirculars = false;
	private ArrayList<GoogDep> dps;
	private DependencyGraph graph;
	private CompilerProject project;
	
	private HashMap<String, GoogDep> depMap = new HashMap<String,GoogDep>();
	private HashMap<String, ICompilationUnit> requireMap = new HashMap<String, ICompilationUnit>();
	private HashMap<ICompilationUnit, String> requireMap2 = new HashMap<ICompilationUnit, String>();
	
	public boolean needCSS = false;
	
	public ArrayList<String> getListOfFiles(CompilerProject project, ProblemQuery problems)
	{
		this.project = project;
		this.problems = problems;

		if (dps == null)
		{
			buildDB();
			dps = sort();
		}
		visited.clear();
		ArrayList<String> files = new ArrayList<String>();
		for (GoogDep gd : dps)
		{
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
					files.add(gd.filePath);			
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
	    	buildDB();
	    	dps = sort();
	    }
	    StringBuilder sb = new StringBuilder();
		int n = dps.size();
		for (int i = n - 1; i >= 0; i--)
		{
			GoogDep gd = dps.get(i);
			if (!isGoogClass(gd.className)) 
			{
				if (removeCirculars)
					sb.append("goog.addDependency('").append(relativePath(gd.filePath)).append("', ['")
						.append(gd.className).append("'], [")
						.append((gd.fileInfo.impls != null) ? getDependencies(gd.fileInfo.impls) : "")
						.append("]);\n");
				else
					sb.append("goog.addDependency('").append(relativePath(gd.filePath)).append("', ['")
					.append(gd.className).append("'], [")
					.append(getDependencies(gd.deps))
					.append("]);\n");					
			}
		}
		if (removeCirculars)
		{
			StringBuilder mainDeps = new StringBuilder();
			GoogDep mainDep = depMap.get(mainName);
			mainDeps.append("goog.addDependency('").append(relativePath(mainDep.filePath)).append("', ['")
								.append(mainDep.className).append("'], [");
			ArrayList<String> restOfDeps = new ArrayList<String>();
	        DependencyTypeSet dependencyTypes = DependencyTypeSet.allOf();
			// get the list of all units not referenced by other units
			for (GoogDep gd : depMap.values())
			{
				if (gd.className.equals(mainName)) 
				{
					for (String d : gd.fileInfo.impls)
					{
						if (!restOfDeps.contains(d))
							restOfDeps.add(d);
					}
					continue;
				}
				ICompilationUnit unit = requireMap.get(gd.className);
				if (unit == null)
				{
					if (!restOfDeps.contains(gd.className))
						restOfDeps.add(gd.className);
					continue;
				}
				Set<ICompilationUnit> deps = graph.getDirectReverseDependencies(unit, dependencyTypes);
				if (deps.size() == 0)
				{
					if (!restOfDeps.contains(gd.className))
						restOfDeps.add(gd.className);
				}
			}
			mainDeps.append(getDependencies(restOfDeps)).append("]);\n");
			sb.insert(0, mainDeps);
			sb.insert(0, "// generated by FalconJX\n");
			for (String dep : restOfDeps)
			{
				GoogDep gd = depMap.get(dep);
				sb.append("goog.addDependency('").append(relativePath(gd.filePath)).append("', ['")
				.append(gd.className).append("'], [")
				.append((gd.fileInfo.impls != null) ? getDependencies(gd.fileInfo.impls) : "")
				.append("]);\n");
			}
			addRestOfDeps(mainDep, restOfDeps);
		}
		return sb.toString();
	}
	
	private boolean isGoogClass(String className)
	{
	    return className.startsWith("goog.");
	}
	
	private void buildDB()
	{
		graph = new DependencyGraph();
		addDeps(mainName);
	}
	
    public ArrayList<String> additionalHTML = new ArrayList<String>();
    
    private HashMap<String, GoogDep> visited = new HashMap<String, GoogDep>();
    
	private ArrayList<GoogDep> sort()
	{
		ArrayList<GoogDep> arr = new ArrayList<GoogDep>();
		GoogDep current = depMap.get(mainName);
		sortFunction(current, arr);
		if (removeCirculars)
		{
			ICompilationUnit mainUnit = requireMap.get(mainName);
			ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
			roots.add(mainUnit);
			requireMap.remove(mainName);
			
			List<ICompilationUnit> order = graph.topologicalSort(requireMap.values());
			ArrayList<GoogDep> depsInOrder = new ArrayList<GoogDep>();
			for (ICompilationUnit unit : order)
			{
				String name = requireMap2.get(unit);
				if (isGoogClass(name)) continue;
				GoogDep dep = depMap.get(name);
				if (dep == null)
					System.out.println("No GoogDep for " + name);
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
			removeRequires(current);
        System.out.println("Dependencies calculated for '" + current.className + "'");

		ICompilationUnit unit = null;
		
		if (removeCirculars)
			if (current.fileInfo.deps == null)
				return;
		
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
					System.out.println(current.className + " static initialization depends on " + staticDep);
					graph.addDependency(unit, base, DependencyType.INHERITANCE);
					
				}
			}
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
							System.out.println(current.className + " depends on " + className);
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
		ArrayList<String> deps = current.deps;
		for (String className : deps)
		{
			if (!isGoogClass(className))
			{
				GoogDep gd = depMap.get(className);
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
			fileLines = Files.readLines(new File(main.filePath), Charset.defaultCharset());
			int n = restOfDeps.size();
			for (int i = n - 1; i >= 0; i--)
			{
				String dep = restOfDeps.get(i);
				//if (!main.deps.contains(dep))
					fileLines.add(main.fileInfo.googProvideLine + 1, JSGoogEmitterTokens.GOOG_REQUIRE.getToken() + "('" + dep + "');");
			}
            File file = new File(main.filePath);  
            PrintWriter out = new PrintWriter(new FileWriter(file));  
            for (String s : fileLines)
            {
                out.println(s);
            }
            out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addDeps(String className)
	{
		if (depMap.containsKey(className) || isGoogClass(className) || isExternal(className))
			return;
		
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
			fileLines = Files.readLines(new File(gd.filePath), Charset.defaultCharset());
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
		}
	}
	
	void removeRequires(GoogDep gd)
	{
		String className = gd.className;
		
	    // remove requires that are not base classes and interfaces
	    try
        {
	    	gd = depMap.get(className);
            List<String> fileLines = Files.readLines(new File(gd.filePath), Charset.defaultCharset());
            ArrayList<String> finalLines = new ArrayList<String>();
            
            boolean firstDependency = true;
        	StringBuilder sb = new StringBuilder();
        	sb.append(JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken());
        	
            FileInfo fi = gd.fileInfo;
            int suppressCount = 0;
            int i = 0;
            for (String line : fileLines)
            {
            	if (i < fi.constructorLine)
            	{
                    int c = line.indexOf(JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken());
                    if (c > -1)
                    	return; // already been processed
                    c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                    if (c > -1)
                    {
                        int c2 = line.indexOf(")");
                        String s = line.substring(c + 14, c2 - 1);
                        if ((gd.fileInfo.impls == null || !gd.fileInfo.impls.contains(s)) &&
                        		(gd.fileInfo.staticDeps == null || !gd.fileInfo.staticDeps.contains(s)))
                        {
                        	// don't remove the require if some class needs it at static initialization
                        	// time
                        	suppressCount++;
                        	System.out.println(gd.filePath + " removing require: " + s);
                    		if (!firstDependency)
                    			sb.append(",");
                    		sb.append(s);
                    		firstDependency = false;
                        	continue;
	                    }
                    }
            	}
                finalLines.add(line);
                i++;
            }
            if (suppressCount > 0)
            {
            	if (fi.suppressLine > 0)
            	{
            		if (fi.suppressLine < fi.constructorLine) 
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
                		}
                		else if (fi.googProvideLine > -1)
                		{
                			finalLines.add(fi.googProvideLine, " */");
                			finalLines.add(fi.googProvideLine, " *  @suppress {missingRequire}");
                			finalLines.add(fi.googProvideLine, " *  @fileoverview");
                			finalLines.add(fi.googProvideLine, "/**");
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
            		}
            		else if (fi.googProvideLine > -1)
            		{
            			finalLines.add(fi.googProvideLine, " */");
            			finalLines.add(fi.googProvideLine, " *  @suppress {missingRequire}");
            			finalLines.add(fi.googProvideLine, " *  @fileoverview");
            			finalLines.add(fi.googProvideLine, "/**");
            		}
            		else
            		{
            			System.out.println("Confused by @suppress in " + className);
            		}                		
            	}
            }

            sb.append("*/");
            finalLines.add(gd.fileInfo.googProvideLine + 1, sb.toString());
            File file = new File(gd.filePath);  
            PrintWriter out = new PrintWriter(new FileWriter(file));  
            for (String s : finalLines)
            {
                out.println(s);
            }
            out.close();
                
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }		
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
		boolean inInjectHTML = false;
	    for (int i = 0; i < n; i++)
	    {
	        String line = lines.get(i);
	        int c2;
	        int c = line.indexOf("*/");
	        if (c > -1 && constructorCount > 0 && constructorCount == numProvides)
	        {
                return fi;
	        }
	        else
	        {
		        if (inInjectHTML)
	            {
	                if (line.indexOf("</inject_html>") > -1)
	                {
	                    inInjectHTML = false;
	                    continue;
	                }
	            	line = line.trim();
	            	if (line.startsWith("*"))
	            		line = line.substring(1);
				    additionalHTML.add(line);
				    continue;
	            }
                c = line.indexOf("<inject_html>");
                if (c > -1)
                {
                    inInjectHTML = true;
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
					        			fi.impls.add(impl);
					        			if (impl.equals("org.apache.flex.core.ICSSImpl"))
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
						        			fi.impls.add(impl);
						        		}
						        		else
						        		{
						        			String token = JSGoogEmitterTokens.FLEXJS_STATIC_DEPENDENCY_LIST.getToken();
						    				c = line.indexOf(token);
						    				if (c > -1)
						    				{
						    					c2 = line.indexOf("*/");
						    					line = line.substring(c + token.length(), c2);
							        			fi.staticDeps = new ArrayList<String>();
						    					fi.staticDeps.addAll(Arrays.asList(line.split(",")));
						    				}
						    				else
						    				{
							        			token = JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken();
							    				c = line.indexOf(token);
							    				if (c > -1)
							    				{
							    					c2 = line.indexOf("*/");
							    					line = line.substring(c + token.length(), c2);
								        			fi.deps = new ArrayList<String>();
								        			if (line.length() > 2) // don't add blank or space if no deps
								        				fi.deps.addAll(Arrays.asList(line.split(",")));
							    					fi.depsLine = i;
							    				}
							    				else if (fi.depsLine == 0)
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
	    return fi;
	}
	
	String getFilePath(String className)
	{
	    String fn;
	    File destFile;
	    File f;
	    
		String classPath = className.replace(".", File.separator);
		// special case app names with underscores, but hope that
		// no other class names have underscores in them
        if (className.equals(mainName))
        	classPath = className;
        
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
    				if (className.contains("org.apache.flex"))
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

							        System.out.println("Copied assets of the '" + nameOfClass + "' class");
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

		String fwdClassPath = className.replace(".", "/");
		String bckClassPath = className.replace(".", "\\");
        for (ISWC swc : swcs)
        {
        	ISWCFileEntry fileEntry =  swc.getFile("js/src/" + fwdClassPath + ".js");
        	if (fileEntry == null)
        		fileEntry = swc.getFile("js/out/" + fwdClassPath + ".js");
        	if (fileEntry == null)
        		fileEntry = swc.getFile("js/src/" + bckClassPath + ".js");
        	if (fileEntry == null)
        		fileEntry = swc.getFile("js/out/" + bckClassPath + ".js");
            if (fileEntry == null)
                fileEntry = swc.getFile("js\\src\\" + bckClassPath + ".js");
            if (fileEntry == null)
                fileEntry = swc.getFile("js\\out\\" + bckClassPath + ".js");
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

    				// (erikdebruin) copy class assets files
    				if (className.contains("org.apache.flex"))
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
						        System.out.println("Copied asset " + assetName);
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
	
	protected void otherScanning(String s)
	{		
	}
	
	private String getDependencies(ArrayList<String> deps)
	{
		String s = "";
		for (String dep : deps)
		{
			if (s.length() > 0)
			{
				s += ", ";
			}
			s += "'" + dep + "'";			
		}
		return s;
	}

	String relativePath(String path)
	{
        if (path.indexOf(outputFolderPath) == 0)
        {
            path = path.replace(outputFolderPath, "../../..");
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
	
	boolean isExternal(String className)
	{
		ICompilationUnit cu = project.resolveQNameToCompilationUnit(className);
		if (cu == null) return false; // unit testing
		
		return ((FlexJSProject)project).isExternalLinkage(cu);
	}
	
	private class GoogDep
	{
		public String filePath;
		public String className;
		public ArrayList<String> deps;
		public FileInfo fileInfo;
		
	}
	
	@SuppressWarnings( "unused" )
	private class FileInfo
	{
		public ArrayList<String> impls;
		public ArrayList<String> deps;
		public ArrayList<String> staticDeps;
		public ArrayList<String> provides;
		public int constructorLine;
		public int depsLine;
		public int suppressLine;
		public int fileoverviewLine;
		public int googProvideLine;
	}
}
