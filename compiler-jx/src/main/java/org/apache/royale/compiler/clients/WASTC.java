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

package org.apache.royale.compiler.clients;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.codegen.wast.IWASTPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.ICompilerSettingsConstants;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.driver.wast.IWASTBackend;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.driver.wast.WASTBackend;
import org.apache.royale.compiler.internal.projects.RoyaleWASTProject;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.targets.RoyaleWASTTarget;
import org.apache.royale.compiler.internal.units.ResourceModuleCompilationUnit;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.utils.ArgumentUtil;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class WASTC {
	
	public static void main(final String[] args) {
		long startTime = System.nanoTime();

		System.out.println("WASTC");
		
		for (String arg : args) {
			System.out.println(arg);
		}
		
		WASTC wastc = new WASTC(new WASTBackend());
		
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		
		int exitCode = wastc.mainNoExit(args, problems);

		long endTime = System.nanoTime();
		
		System.out.println((endTime - startTime) / 1e9 + " seconds");
		
		System.exit(exitCode);
	}

	
	
	static enum ExitCode {
		SUCCESS(0), PRINT_HELP(1), FAILED_WITH_PROBLEMS(2), FAILED_WITH_ERRORS(3), FAILED_WITH_EXCEPTIONS(
				4), FAILED_WITH_CONFIG_PROBLEMS(5);

		ExitCode(int code) {
			this.code = code;
		}

		final int code;
	}
	
	

	public WASTC(IWASTBackend backend) {
		workspace = new Workspace();

		project = new RoyaleWASTProject(workspace, backend);
		
		problems = new ProblemQuery(); // this gets replaced in configure(). Do we need it here?
	}

	
	
	private Configuration config;
	private ICompilationUnit mainCU;
	private ProblemQuery problems;
	private RoyaleWASTProject project;
	private Configurator projectConfigurator;
	private ITarget target;
	private Workspace workspace;

    
	
	public int mainNoExit(final String[] args, List<ICompilerProblem> theProblems) {
		int result = -1;
		
		try {
			ExitCode exitCode = ExitCode.SUCCESS;
			
			try {
				final boolean continueCompilation = configure(ArgumentUtil.fixArgs(args));

				if (continueCompilation) {
					project.setProblems(problems.getProblems());
					
					compile();
					
					if (problems.hasFilteredProblems()) {
						if (problems.hasErrors())
							exitCode = ExitCode.FAILED_WITH_ERRORS;
						else
							exitCode = ExitCode.FAILED_WITH_PROBLEMS;
					}
				} else if (problems.hasFilteredProblems()) {
					exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
				} else {
					exitCode = ExitCode.PRINT_HELP;
				}
			} catch (Exception e) {
				if (theProblems == null) {
					System.err.println(e.getMessage());
				} else {
					final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(e);
					problems.add(unexpectedExceptionProblem);
				}
				exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
			} finally {
				workspace.startIdleState();
				
				try {
					workspace.close();
				} finally {
					workspace.endIdleState(Collections.<ICompilerProject, Set<ICompilationUnit>>emptyMap());
				}

				if (theProblems != null && problems.hasFilteredProblems()) {
					for (ICompilerProblem problem : problems.getFilteredProblems()) {
						theProblems.add(problem);
					}
				}
			}
			
			result = exitCode.code;
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			if (theProblems != null && !theProblems.isEmpty()) {
				final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(workspace);
				final ProblemPrinter printer = new ProblemPrinter(formatter);
				printer.printProblems(theProblems);
			}
		}
		
		return result;
	}

	private boolean compile() {
		boolean compilationSuccess = false;

		try {
			if (!setupTargetFile()) {
				return false;
			}

			List<ICompilerProblem> problemsBuildingSWF = new ArrayList<ICompilerProblem>();

			project.mainCU = mainCU;
			
			IJSApplication jsTarget = null;
			
			Collection<ICompilerProblem> fatalProblems = project.getFatalProblems();
			
			if (!fatalProblems.isEmpty()) {
				problemsBuildingSWF.addAll(fatalProblems);
			} else {
				jsTarget = ((JSTarget) target).build(mainCU, problemsBuildingSWF);
				
				problems.addAll(problemsBuildingSWF);
				
				if (jsTarget == null) {
					problems.add(new UnableToBuildSWFProblem(getOutputFilePath()));
				}
			}

			if (jsTarget != null) {
				List<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
				List<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();

				if (!config.getCreateTargetWithErrors()) {
					problems.getErrorsAndWarnings(errors, warnings);
					if (errors.size() > 0)
						return false;
				}

				IWASTPublisher wastPublisher = project.getBackend().createPublisher(project, errors, config);

				File outputFolder = wastPublisher.getOutputFolder();

				ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
				roots.add(mainCU);
				Set<ICompilationUnit> incs = target.getIncludesCompilationUnits();
				roots.addAll(incs);
				project.mixinClassNames = new TreeSet<String>();
				project.remoteClassAliasMap = new HashMap<String, String>();
				List<ICompilationUnit> reachableCompilationUnits = project
						.getReachableCompilationUnitsInSWFOrder(roots);
				((RoyaleWASTTarget) target).collectMixinMetaData(project.mixinClassNames, reachableCompilationUnits);
				((RoyaleWASTTarget) target).collectRemoteClassMetaData(project.remoteClassAliasMap,
						reachableCompilationUnits);
				for (final ICompilationUnit cu : reachableCompilationUnits) {
					ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

					if (cuType == ICompilationUnit.UnitType.AS_UNIT || cuType == ICompilationUnit.UnitType.MXML_UNIT) {
						final File outputClassFile = getOutputClassFile(cu.getQualifiedNames().get(0), outputFolder);

						System.out.println("Compiling file: " + outputClassFile);

						ICompilationUnit unit = cu;

						IASWriter writer;
						if (cuType == ICompilationUnit.UnitType.AS_UNIT) {
							writer = (IASWriter) project.getBackend().createWriter(project, errors, unit, false);
						} else {
							writer = (IASWriter) project.getBackend().createMXMLWriter(project, errors, unit, false);
						}

						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputClassFile));

						writer.writeTo(out);
						out.flush();
						out.close();
						writer.close();
					}
				}

      			compilationSuccess = wastPublisher.publish(problems);
			}
		} catch (Exception e) {
			final ICompilerProblem problem = new InternalCompilerProblem(e);
			problems.add(problem);
		}

		return compilationSuccess;
	}

	private String getOutputFilePath() {
		if (config.getOutput() == null) {
			final String extension = "." + project.getBackend().getOutputExtension();
			return FilenameUtils.removeExtension(config.getTargetFile()).concat(extension);
		} else
			return config.getOutput();
	}

	private File getOutputClassFile(String qname, File outputFolder) {
		String[] cname = qname.split("\\.");
		String sdirPath = outputFolder + File.separator;
		if (cname.length > 0) {
			for (int i = 0, n = cname.length - 1; i < n; i++) {
				sdirPath += cname[i] + File.separator;
			}

			File sdir = new File(sdirPath);
			if (!sdir.exists())
				sdir.mkdirs();

			qname = cname[cname.length - 1];
		}

		return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension());
	}

	protected boolean setupTargetFile() throws InterruptedException {
		final String mainFileName = config.getTargetFile();

		final String normalizedMainFileName = FilenameNormalization.normalize(mainFileName);

		final SourceCompilationUnitFactory compilationUnitFactory = project.getSourceCompilationUnitFactory();

		File normalizedMainFile = new File(normalizedMainFileName);
		if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile)) {
			project.addIncludeSourceFile(normalizedMainFile);

			final List<String> sourcePath = config.getCompilerSourcePath();
			String mainQName = null;
			if (sourcePath != null && !sourcePath.isEmpty()) {
				for (String path : sourcePath) {
					final String otherPath = new File(path).getAbsolutePath();
					if (mainFileName.startsWith(otherPath)) {
						mainQName = mainFileName.substring(otherPath.length() + 1);
						mainQName = mainQName.replaceAll("\\\\", "/");
						mainQName = mainQName.replaceAll("\\/", ".");
						if (mainQName.endsWith(".as"))
							mainQName = mainQName.substring(0, mainQName.length() - 3);
						break;
					}
				}
			}

			if (mainQName == null)
				mainQName = FilenameUtils.getBaseName(mainFileName);

			Collection<ICompilationUnit> mainFileCompilationUnits = workspace
					.getCompilationUnits(normalizedMainFileName, project);

			mainCU = Iterables.getOnlyElement(mainFileCompilationUnits);

			config.setMainDefinition(mainQName);
		}

		Preconditions.checkNotNull(mainCU, "Main compilation unit can't be null");

		ITargetSettings targetSettings = projectConfigurator.getTargetSettings(null);
		if (targetSettings != null)
			project.setTargetSettings(targetSettings);

		target = project.getBackend().createTarget(project, targetSettings, null);

		return true;
	}

	protected boolean configure(final String[] args) {
		IWASTBackend backend = project.getBackend();
		
		project.getSourceCompilationUnitFactory().addHandler(backend.getSourceFileHandlerInstance());
		
		project.configurator = projectConfigurator = backend.createConfigurator();

		try {
			projectConfigurator.setConfiguration(args, ICompilerSettingsConstants.FILE_SPECS_VAR);

			projectConfigurator.applyToProject(project);
			project.config = (WASTConfiguration) projectConfigurator.getConfiguration();

			config = projectConfigurator.getConfiguration();
			ConfigurationBuffer configBuffer = projectConfigurator.getConfigurationBuffer();

			problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());
			problems.addAll(projectConfigurator.getConfigurationProblems());

			if (configBuffer.getVar("version") != null) //$NON-NLS-1$
				return false;

			if (problems.hasErrors())
				return false;

			validateTargetFile();
			return true;
		} catch (ConfigurationException e) {
			final ICompilerProblem problem = new ConfigurationProblem(e);
			problems.add(problem);
			return false;
		} catch (Exception e) {
			final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, e.getMessage());
			problems.add(problem);
			return false;
		} finally {
			if (config == null) {
				config = new Configuration();
			}
		}
	}

	protected void validateTargetFile() throws ConfigurationException {
		if (mainCU instanceof ResourceModuleCompilationUnit)
			return; // when compiling a Resource Module, no target file is defined.

		final String targetFile = config.getTargetFile();
		if (targetFile == null)
			throw new ConfigurationException.MustSpecifyTarget(null, null, -1);

		final File file = new File(targetFile);
		if (!file.exists())
			throw new ConfigurationException.IOError(targetFile);
	}

	public List<String> getSourceList() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
			roots.add(mainCU);
			Set<ICompilationUnit> incs = target.getIncludesCompilationUnits();
			roots.addAll(incs);
			project.mixinClassNames = new TreeSet<String>();
			List<ICompilationUnit> units = project.getReachableCompilationUnitsInSWFOrder(roots);
			for (ICompilationUnit unit : units) {
				UnitType ut = unit.getCompilationUnitType();
				if (ut == UnitType.AS_UNIT || ut == UnitType.MXML_UNIT) {
					list.add(unit.getAbsoluteFilename());
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	public String getMainSource() {
		return mainCU.getAbsoluteFilename();
	}

}
