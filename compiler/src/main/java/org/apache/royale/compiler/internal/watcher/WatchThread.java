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

package org.apache.royale.compiler.internal.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.utils.FilenameNormalization;

public class WatchThread extends Thread
{
    /*
     * Exit code enumerations.
     */
    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_ERRORS(2),
        FAILED_WITH_EXCEPTIONS(3),
        FAILED_WITH_CONFIG_PROBLEMS(4);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
        
        int getCode()
        {
        	return code;
        }
    }

	/**
	 * Specifies how a target rebuilds and writes compilation units that have
	 * been detected as changed by the watch thread.
	 */
	public static interface IWatchWriter
	{
		/**
		 * Rebuilds the specified compilation units and populates any problems
		 * that were found while rebuilding.
		 */
		void rebuild(Collection<ICompilationUnit> units, Collection<ICompilerProblem> problems) throws InterruptedException, IOException;

		/**
		 * Writes the specified compilation units to the output directory.
		 */
		void write(Collection<ICompilationUnit> units) throws InterruptedException, IOException;
	}

	public WatchThread(String name, IWatchWriter writer, Configuration config, RoyaleProject project, Workspace workspace, ProblemQuery problems)
	{
		super();

		this.name = name;
		this.writer = writer;
		this.config = config;
		this.project = project;
		this.workspace = workspace;
		this.problems = problems;
	}

	private String name;
	private IWatchWriter writer;
	private Configuration config;
	private RoyaleProject project;
	private Workspace workspace;
    private ProblemQuery problems;

	private Map<WatchKey, Path> watchKeys;
	private WatchService watchService;

	public void run()
	{
		try
		{
			watchKeys = new HashMap<>();
			watchService = FileSystems.getDefault().newWatchService();
			Set<Path> watchedPaths = new HashSet<>();
			for (String sourcePath : config.getCompilerSourcePath())
			{
				sourcePath = FilenameNormalization.normalize(sourcePath);
				watchPath(Paths.get(sourcePath), watchedPaths);
			}
			for (String fileSpec : config.getFileSpecs())
			{
				fileSpec = FilenameNormalization.normalize(fileSpec);
				Path fileSpecPath = Paths.get(fileSpec);
				watchPath(fileSpecPath.getParent(), watchedPaths);
			}

			System.out.println("Watching for file changes in target " + name + "...");
			while (true)
			{
				if (isInterrupted())
				{
					return;
				}
				checkForChanges();
			}
		}
		catch(Exception e)
		{
			final ICompilerProblem problem = new InternalCompilerProblem(e);
			System.err.println(problem);
			System.exit(ExitCode.FAILED_WITH_EXCEPTIONS.code);
		}
	}

	private void checkForChanges() throws Exception
	{
		// pause the thread while there are no changes pending,
		// for better performance
		WatchKey watchKey = watchService.take();
		
		Set<ICompilationUnit> changedCUs = new HashSet<>();
		while (watchKey != null)
		{
			processWatchKey(watchKey, changedCUs);

			// keep handling new changes until we run out
			watchKey = watchService.poll();
		}

		recompile(changedCUs);
	}

	private void processWatchKey(WatchKey watchKey, Set<ICompilationUnit> changedCUs) throws InterruptedException
	{
		Path path = watchKeys.get(watchKey);
		for (WatchEvent<?> event : watchKey.pollEvents())
		{
			WatchEvent.Kind<?> kind = event.kind();
			Path childPath = (Path) event.context();
			childPath = path.resolve(childPath);
			String fileName = childPath.getFileName().toString();
			if (fileName.endsWith(".mxml") || fileName.endsWith(".as"))
			{
				String normalizedChildPath = FilenameNormalization.normalize(childPath.toString());
				IFileSpecification fileSpec = workspace.getFileSpecification(normalizedChildPath);

				if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
				{
					workspace.fileAdded(fileSpec);
				}
				else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE))
				{
					workspace.fileRemoved(fileSpec);
				}
				else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
				{
					workspace.fileChanged(fileSpec);
				}
				for (ICompilationUnit cu : workspace.getCompilationUnits(normalizedChildPath, project))
				{
					changedCUs.add(cu);
				}
			}
		}
		if (!watchKey.reset())
		{
			watchKeys.remove(watchKey);
		}
	}

	private void recompile(Set<ICompilationUnit> changedCUs) throws InterruptedException, IOException
	{
		if (changedCUs.size() == 0)
		{
		    return;
		}

		System.out.println("File change detected. Recompiling " + name + "...");

		problems.clear();

		writer.rebuild(changedCUs, problems.getProblems());

		for (ICompilationUnit cu : changedCUs)
		{
			DependencyTypeSet dependencyTypes = DependencyTypeSet.allOf();
			Set<ICompilationUnit> reverseDeps = project.getDirectReverseDependencies(cu, dependencyTypes);
			changedCUs.addAll(reverseDeps);
		}

		List<ICompilerProblem> errs = new ArrayList<ICompilerProblem>();
		List<ICompilerProblem> warns = new ArrayList<ICompilerProblem>();
		problems.getErrorsAndWarnings(errs, warns);
		
		if (!problems.hasFilteredProblems() || !problems.hasErrors())
		{
			writer.write(changedCUs);
		}

		if (problems.hasFilteredProblems())
		{
			Iterable<ICompilerProblem> filteredProblems = problems.getFilteredProblems();
			final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(
					workspace);
			final ProblemPrinter printer = new ProblemPrinter(formatter);
			printer.printProblems(filteredProblems);
		}

		System.out.println("Watching for file changes in target " + name + "...");
	}

    private void watchPath(Path path, Set<Path> watchedPaths) throws IOException
    {
        path = path.toAbsolutePath();
        if (watchedPaths.contains(path))
        {
            return;
        }
        watchedPaths.add(path);
		java.nio.file.Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path subPath, BasicFileAttributes attrs)
					throws IOException {
				WatchKey watchKey = subPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				watchKeys.put(watchKey, subPath);
				return FileVisitResult.CONTINUE;
			}
		});
    }
}