////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.linter;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.ConfigurationPathResolver;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.linter.config.CommandLineConfigurator;
import org.apache.royale.linter.config.Configuration;
import org.apache.royale.linter.config.ConfigurationBuffer;
import org.apache.royale.linter.config.ConfigurationValue;
import org.apache.royale.linter.config.Configurator;
import org.apache.royale.linter.config.ILinterSettingsConstants;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Lints .as and .mxml source files.
 */
public class LINTER {
	private static final String DEFAULT_VAR = "files";
	private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";
	private static final String FILE_EXTENSION_ACTIONSCRIPT = ".as";
	private static final String FILE_EXTENSION_MXML = ".mxml";

	static enum ExitCode {
		SUCCESS(0), PRINT_HELP(1), FAILED_WITH_ERRORS(2), FAILED_WITH_EXCEPTIONS(3), FAILED_WITH_CONFIG_PROBLEMS(4);

		ExitCode(int code) {
			this.code = code;
		}

		final int code;

		int getCode() {
			return code;
		}
	}

	/**
	 * Java program entry point.
	 * 
	 * @param args command line arguments
	 */
	public static void main(final String[] args) {
		LINTER formatter = new LINTER();
		int exitCode = formatter.execute(args);
		System.exit(exitCode);
	}

	public LINTER() {

	}

	private ProblemQuery problemQuery;
	private List<File> inputFiles = new ArrayList<File>();
	private Configuration configuration;
	private ConfigurationBuffer configBuffer;

	private LinterSettings settings = new LinterSettings();

	public int execute(String[] args) {
		ExitCode exitCode = ExitCode.SUCCESS;
		problemQuery = new ProblemQuery();
		problemQuery.setShowWarnings(false);

		try {
			boolean continueLinting = configure(args, problemQuery);
			if (continueLinting) {
				for (File inputFile : inputFiles) {
					String filePath = FilenameNormalization.normalize(inputFile.getAbsolutePath());
					FileSpecification fileSpec = new FileSpecification(filePath);
					String fileText = IOUtils.toString(fileSpec.createReader());
					if (filePath.endsWith(FILE_EXTENSION_MXML)) {
						MXMLLinter linter = new MXMLLinter(settings);
						linter.lint(filePath, fileText, problemQuery.getProblems());
					} else {
						ASLinter linter = new ASLinter(settings);
						linter.lint(filePath, fileText, problemQuery.getProblems());
					}
				}
			} else if (problemQuery.hasFilteredProblems()) {
				exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
			} else {
				exitCode = ExitCode.PRINT_HELP;
			}
		} catch (Exception e) {
			problemQuery.add(new UnexpectedExceptionProblem(e));
			System.err.println(e.getMessage());
			exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
		} finally {
			if (problemQuery.hasFilteredProblems()) {
				final Workspace workspace = new Workspace();
				final CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer();
				final ProblemFormatter formatter = new WorkspaceProblemFormatter(workspace, categorizer);
				final ProblemPrinter printer = new ProblemPrinter(formatter);
				printer.printProblems(problemQuery.getFilteredProblems());
			}
		}
		return exitCode.getCode();
	}

	/**
	 * Get the start up message that contains the program name with the copyright
	 * notice.
	 * 
	 * @return The startup message.
	 */
	protected String getStartMessage() {
		// This message should not be localized.
		String message = "Apache Royale ActionScript Linter (aslint)\n" + VersionInfo.buildMessage()
				+ "\n";
		return message;
	}

	/**
	 * Get my program name.
	 * 
	 * @return always "aslint".
	 */
	protected String getProgramName() {
		return "aslint";
	}

	/**
	 * Print detailed help information if -help is provided.
	 */
	private void processHelp(final List<ConfigurationValue> helpVar) {
		final Set<String> keywords = new LinkedHashSet<String>();
		if (helpVar != null) {
			for (final ConfigurationValue val : helpVar) {
				for (final Object element : val.getArgs()) {
					String keyword = (String) element;
					while (keyword.startsWith("-"))
						keyword = keyword.substring(1);
					keywords.add(keyword);
				}
			}
		}

		if (keywords.size() == 0)
			keywords.add("help");

		final String usages = CommandLineConfigurator.usage(getProgramName(), DEFAULT_VAR, configBuffer, keywords,
				LocalizationManager.get(), L10N_CONFIG_PREFIX);
		System.out.println(getStartMessage());
		System.out.println(usages);
	}

	private boolean configure(String[] args, ProblemQuery problems) {
		try {
			Configurator configurator = new Configurator();
            ConfigurationPathResolver resolver = new ConfigurationPathResolver(System.getProperty("user.dir")); 
            configurator.setConfigurationPathResolver(resolver);
			configurator.setConfiguration(args, ILinterSettingsConstants.FILES);
			configuration = configurator.getConfiguration();
			configBuffer = configurator.getConfigurationBuffer();

			problems.addAll(configurator.getConfigurationProblems());

			if (configBuffer.getVar("version") != null) {
				System.out.println(VersionInfo.buildMessage());
				return false;
			}

			// // Print help if "-help" is present.
			final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
			if (helpVar != null || (args.length == 0 && configuration.getFiles().size() == 0)) {
				processHelp(helpVar);
				return false;
			}

			if (problems.hasErrors()) {
				return false;
			}

			settings = LinterUtils.configurationToLinterSettings(configuration);
			if (settings.rules.size() == 0) {
				ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, "No linter rules were specified");
				problems.add(problem);
				return false;
			}

			for (String filePath : configuration.getFiles()) {
				File inputFile = new File(filePath);
				if (!inputFile.exists()) {
					ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, "Input file does not exist: " + filePath);
					problems.add(problem);
					return false;
				}
				if (inputFile.isDirectory()) {
					addDirectory(inputFile);
				} else {
					inputFiles.add(inputFile);
				}
			}
			if (inputFiles.size() == 0) {
				ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, "Missing input file(s)");
				problems.add(problem);
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, e.getMessage());
			problems.add(problem);
			return false;
		}
	}

	private void addDirectory(File inputFile) {
		for (File file : inputFile.listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith(".")) {
				continue;
			}
			if (file.isDirectory()) {
				addDirectory(file);
			} else if (fileName.endsWith(FILE_EXTENSION_ACTIONSCRIPT) || fileName.endsWith(FILE_EXTENSION_MXML)) {
				inputFiles.add(file);
			}
		}
	}
}
