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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.royale.linter.config.LineCommentPosition;
import org.apache.royale.linter.rules.NoAnyTypeRule;
import org.apache.royale.linter.rules.NoBooleanEqualityRule;
import org.apache.royale.linter.rules.ClassNameRule;
import org.apache.royale.linter.rules.ConstantNameRule;
import org.apache.royale.linter.rules.NoConstructorDispatchEventRule;
import org.apache.royale.linter.rules.NoConstructorReturnTypeRule;
import org.apache.royale.linter.rules.NoDuplicateObjectKeysRule;
import org.apache.royale.linter.rules.NoDynamicClassRule;
import org.apache.royale.linter.rules.EmptyCommentRule;
import org.apache.royale.linter.rules.EmptyFunctionBodyRule;
import org.apache.royale.linter.rules.EmptyNestedBlockRule;
import org.apache.royale.linter.rules.EmptyStatementRule;
import org.apache.royale.linter.rules.FieldNameRule;
import org.apache.royale.linter.rules.FunctionNameRule;
import org.apache.royale.linter.rules.NoIfBooleanLiteralRule;
import org.apache.royale.linter.rules.InterfaceNameRule;
import org.apache.royale.linter.rules.LineCommentPositionRule;
import org.apache.royale.linter.rules.LocalVarAndParameterNameRule;
import org.apache.royale.linter.rules.LocalVarShadowsFieldRule;
import org.apache.royale.linter.rules.MXMLEmptyAttributeRule;
import org.apache.royale.linter.rules.MXMLIDRule;
import org.apache.royale.linter.rules.MaxBlockDepthRule;
import org.apache.royale.linter.rules.MaxParametersRule;
import org.apache.royale.linter.rules.MissingASDocRule;
import org.apache.royale.linter.rules.MissingConstructorSuperRule;
import org.apache.royale.linter.rules.MissingNamespaceRule;
import org.apache.royale.linter.rules.MissingSemicolonRule;
import org.apache.royale.linter.rules.MissingTypeRule;
import org.apache.royale.linter.rules.NoLeadingZeroesRule;
import org.apache.royale.linter.rules.OverrideContainsOnlySuperCallRule;
import org.apache.royale.linter.rules.PackageNameRule;
import org.apache.royale.linter.rules.NoSparseArrayRule;
import org.apache.royale.linter.rules.StaticConstantsRule;
import org.apache.royale.linter.rules.StrictEqualityRule;
import org.apache.royale.linter.rules.NoStringEventNameRule;
import org.apache.royale.linter.rules.SwitchWithoutDefaultRule;
import org.apache.royale.linter.rules.NoThisInClosureRule;
import org.apache.royale.linter.rules.NoTraceRule;
import org.apache.royale.linter.rules.UnsafeNegationRule;
import org.apache.royale.linter.rules.ValidTypeofRule;
import org.apache.royale.linter.rules.VariablesOnTopRule;
import org.apache.royale.linter.rules.NoVoidOperatorRule;
import org.apache.royale.linter.rules.NoWildcardImportRule;
import org.apache.royale.linter.rules.NoWithRule;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Lints .as and .mxml source files.
 */
public class LINTER {
	private static final String DEFAULT_VAR = "files";
	private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";

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
					lintFileText(filePath, fileText, problemQuery.getProblems());
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

			settings = new LinterSettings();
			settings.ignoreProblems = configuration.getIgnoreParsingProblems();

			List<LinterRule> rules = new ArrayList<LinterRule>();
			if (configuration.getClassName()) {
				rules.add(new ClassNameRule());
			}
			if (configuration.getConstantName()) {
				rules.add(new ConstantNameRule());
			}
			if (configuration.getEmptyFunctionBody()) {
				rules.add(new EmptyFunctionBodyRule());
			}
			if (configuration.getEmptyNestedBlock()) {
				rules.add(new EmptyNestedBlockRule());
			}
			if (configuration.getFunctionName()) {
				rules.add(new FunctionNameRule());
			}
			if (configuration.getFieldName()) {
				rules.add(new FieldNameRule());
			}
			if (configuration.getOverrideSuper()) {
				rules.add(new OverrideContainsOnlySuperCallRule());
			}
			if (configuration.getEmptyComment()) {
				rules.add(new EmptyCommentRule());
			}
			if (configuration.getEmptyStatement()) {
				rules.add(new EmptyStatementRule());
			}
			if (configuration.getInterfaceName()) {
				rules.add(new InterfaceNameRule());
			}
			if (configuration.getLineCommentPosition() != null) {
				LineCommentPositionRule rule = new LineCommentPositionRule();
				rule.position = LineCommentPosition.valueOf(configuration.getLineCommentPosition().toUpperCase());
				rules.add(rule);
			}
			if (configuration.getLocalVarParamName()) {
				rules.add(new LocalVarAndParameterNameRule());
			}
			if (configuration.getLocalVarShadowsField()) {
				rules.add(new LocalVarShadowsFieldRule());
			}
			if (configuration.getMaxParams() > 0) {
				MaxParametersRule rule = new MaxParametersRule();
				rule.maximum = configuration.getMaxParams();
				rules.add(rule);
			}
			if (configuration.getMaxBlockDepth() > 0) {
				MaxBlockDepthRule rule = new MaxBlockDepthRule();
				rule.maximum = configuration.getMaxBlockDepth();
				rules.add(rule);
			}
			if (configuration.getMissingAsdoc()) {
				rules.add(new MissingASDocRule());
			}
			if (configuration.getMissingConstructorSuper()) {
				rules.add(new MissingConstructorSuperRule());
			}
			if (configuration.getMissingNamespace()) {
				rules.add(new MissingNamespaceRule());
			}
			if (configuration.getMissingSemicolon()) {
				rules.add(new MissingSemicolonRule());
			}
			if (configuration.getMissingType()) {
				rules.add(new MissingTypeRule());
			}
			if (configuration.getMxmlId()) {
				rules.add(new MXMLIDRule());
			}
			if (configuration.getMxmlEmptyAttr()) {
				rules.add(new MXMLEmptyAttributeRule());
			}
			if (configuration.getNoAnyType()) {
				rules.add(new NoAnyTypeRule());
			}
			if (configuration.getNoBooleanEquality()) {
				rules.add(new NoBooleanEqualityRule());
			}
			if (configuration.getNoConstructorDispatch()) {
				rules.add(new NoConstructorDispatchEventRule());
			}
			if (configuration.getNoConstructorReturnType()) {
				rules.add(new NoConstructorReturnTypeRule());
			}
			if (configuration.getNoDuplicateKeys()) {
				rules.add(new NoDuplicateObjectKeysRule());
			}
			if (configuration.getNoDynamicClass()) {
				rules.add(new NoDynamicClassRule());
			}
			if (configuration.getNoIfBoolean()) {
				rules.add(new NoIfBooleanLiteralRule());
			}
			if (configuration.getNoLeadingZero()) {
				rules.add(new NoLeadingZeroesRule());
			}
			if (configuration.getNoSparseArray()) {
				rules.add(new NoSparseArrayRule());
			}
			if (configuration.getNoStringEvent()) {
				rules.add(new NoStringEventNameRule());
			}
			if (configuration.getNoThisClosure()) {
				rules.add(new NoThisInClosureRule());
			}
			if (configuration.getNoTrace()) {
				rules.add(new NoTraceRule());
			}
			if (configuration.getNoVoidOperator()) {
				rules.add(new NoVoidOperatorRule());
			}
			if (configuration.getNoWildcardImport()) {
				rules.add(new NoWildcardImportRule());
			}
			if (configuration.getNoWith()) {
				rules.add(new NoWithRule());
			}
			if (configuration.getPackageName()) {
				rules.add(new PackageNameRule());
			}
			if (configuration.getStaticConstants()) {
				rules.add(new StaticConstantsRule());
			}
			if (configuration.getStrictEquality()) {
				rules.add(new StrictEqualityRule());
			}
			if (configuration.getSwitchDefault()) {
				rules.add(new SwitchWithoutDefaultRule());
			}
			if (configuration.getUnsafeNegation()) {
				rules.add(new UnsafeNegationRule());
			}
			if (configuration.getValidTypeof()) {
				rules.add(new ValidTypeofRule());
			}
			if (configuration.getVarsOnTop()) {
				rules.add(new VariablesOnTopRule());
			}
			settings.rules = rules;
			if (rules.size() == 0) {
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
			} else if (fileName.endsWith(".as") || fileName.endsWith(".mxml")) {
				inputFiles.add(file);
			}
		}
	}

	public void lintFile(File file, Collection<ICompilerProblem> problems) throws IOException {
		String filePath = FilenameNormalization.normalize(file.getAbsolutePath());
		FileSpecification fileSpec = new FileSpecification(filePath);
		String fileText = IOUtils.toString(fileSpec.createReader());
		lintFileText(filePath, fileText, problems);
	}

	public void lintFileText(String filePath, String text, Collection<ICompilerProblem> problems) {
		filePath = FilenameNormalization.normalize(filePath);
		if (filePath.endsWith(".mxml")) {
			lintMXMLTextInternal(filePath, text, problems);
		} else {
			lintAS3TextInternal(filePath, text, problems);
		}
	}

	public void lintActionScriptText(String text, Collection<ICompilerProblem> problems) {
		String filePath = FilenameNormalization.normalize("stdin.as");
		lintAS3TextInternal(filePath, text, problems);
	}

	public void lintMXMLText(String text, Collection<ICompilerProblem> problems) {
		String filePath = FilenameNormalization.normalize("stdin.mxml");
		lintMXMLTextInternal(filePath, text, problems);
	}

	private void lintAS3TextInternal(String filePath, String text, Collection<ICompilerProblem> problems) {
		ASLinter linter = new ASLinter(settings);
		linter.lint(filePath, text, problems);
	}

	private void lintMXMLTextInternal(String filePath, String text, Collection<ICompilerProblem> problems) {
		MXMLLinter linter = new MXMLLinter(settings);
		linter.lint(filePath, text, problems);
	}
}
