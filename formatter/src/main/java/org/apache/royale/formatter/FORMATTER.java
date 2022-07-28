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

package org.apache.royale.formatter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.parsing.as.MetaDataPayloadToken;
import org.apache.royale.compiler.internal.parsing.as.MetadataToken;
import org.apache.royale.compiler.internal.parsing.as.MetadataTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.RepairingTokenBuffer;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.formatter.config.CommandLineConfigurator;
import org.apache.royale.formatter.config.Configuration;
import org.apache.royale.formatter.config.ConfigurationBuffer;
import org.apache.royale.formatter.config.ConfigurationValue;
import org.apache.royale.formatter.config.Configurator;
import org.apache.royale.formatter.config.Semicolons;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Formats .as source files.
 */
public class FORMATTER {
	private static final int TOKEN_TYPE_EXTRA = 999999;

	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String DEFAULT_VAR = "files";
	private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";
	private static final Pattern ASDOC_START_LINE_PATTERN = Pattern.compile("^\\*(\\s*)");
	private static final String FORMATTER_TAG_OFF = "@formatter:off";
	private static final String FORMATTER_TAG_ON = "@formatter:on";

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
		FORMATTER formatter = new FORMATTER();
		int exitCode = formatter.execute(args);
		System.exit(exitCode);
	}

	public FORMATTER() {

	}

	public int tabSize = 4;
	public boolean insertSpaces = false;
	public boolean insertFinalNewLine = false;
	public boolean placeOpenBraceOnNewLine = true;
	public boolean insertSpaceAfterSemicolonInForStatements = true;
	public boolean insertSpaceAfterKeywordsInControlFlowStatements = true;
	public boolean insertSpaceAfterFunctionKeywordForAnonymousFunctions = false;
	public boolean insertSpaceBeforeAndAfterBinaryOperators = true;
	public boolean insertSpaceAfterCommaDelimiter = true;
	public boolean insertSpaceBetweenMetadataAttributes = true;
	public boolean insertSpaceAtStartOfLineComment = true;
	public int maxPreserveNewLines = 2;
	public Semicolons semicolons = Semicolons.INSERT;
	public boolean ignoreProblems = false;
	public boolean collapseEmptyBlocks = false;
	public boolean mxmlAlignAttributes = false;
	public boolean mxmlInsertNewLineBetweenAttributes = false;

	private ProblemQuery problemQuery;
	private List<File> inputFiles = new ArrayList<File>();
	private boolean writeBackToInputFiles = false;
	private boolean listChangedFiles = false;
	private Configuration configuration;
	private ConfigurationBuffer configBuffer;

	public int execute(String[] args) {
		ExitCode exitCode = ExitCode.SUCCESS;
		problemQuery = new ProblemQuery();
		problemQuery.setShowWarnings(false);

		try {
			boolean continueFormatting = configure(args, problemQuery);
			if (continueFormatting) {
				if (inputFiles.size() == 0) {
					StringBuilder builder = new StringBuilder();
					Scanner sysInScanner = new Scanner(System.in);
					try {
						while (sysInScanner.hasNext()) {
							builder.append(sysInScanner.next());
						}
					} finally {
						IOUtils.closeQuietly(sysInScanner);
					}
					String filePath = FilenameNormalization.normalize("stdin.as");
					String fileText = builder.toString();
					String formattedText = formatFileText(filePath, fileText, problemQuery.getProblems());
					if (!fileText.equals(formattedText)) {
						if (listChangedFiles) {
							System.out.println(filePath);
						}
					}
					if (!listChangedFiles) {
						System.out.println(formattedText);
					}
				} else {
					for (File inputFile : inputFiles) {
						String filePath = FilenameNormalization.normalize(inputFile.getAbsolutePath());
						FileSpecification fileSpec = new FileSpecification(filePath);
						String fileText = IOUtils.toString(fileSpec.createReader());
						String formattedText = formatFileText(filePath, fileText, problemQuery.getProblems());
						if (!fileText.equals(formattedText)) {
							if (listChangedFiles) {
								System.out.println(filePath);
							}
							if (writeBackToInputFiles) {
								FileUtils.write(inputFile, formattedText, "utf8");
							}
						}
						if (!listChangedFiles && !writeBackToInputFiles) {
							System.out.println(formattedText);
						}
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

	public String formatFile(File file, Collection<ICompilerProblem> problems) throws IOException {
		String filePath = FilenameNormalization.normalize(file.getAbsolutePath());
		FileSpecification fileSpec = new FileSpecification(filePath);
		String fileText = IOUtils.toString(fileSpec.createReader());
		return formatFileText(filePath, fileText, problems);
	}

	public String formatFile(File file) throws IOException {
		return formatFile(file, null);
	}

	public String formatFileText(String filePath, String text, Collection<ICompilerProblem> problems) {
		filePath = FilenameNormalization.normalize(filePath);
		String result = null;
		if (filePath.endsWith(".mxml")) {
			result = formatMXMLTextInternal(filePath, text, problems);
		} else {
			result = formatAS3TextInternal(filePath, text, problems);
		}
		if (insertFinalNewLine && result.charAt(result.length() - 1) != '\n') {
			return result + '\n';
		}
		return result;
	}

	public String formatFileText(String filePath, String text) {
		return formatFileText(filePath, text, null);
	}

	public String formatActionScriptText(String text, Collection<ICompilerProblem> problems) {
		String filePath = FilenameNormalization.normalize("stdin.as");
		return formatAS3TextInternal(filePath, text, problems);
	}

	public String formatActionScriptText(String text) {
		return formatActionScriptText(text, null);
	}

	public String formatMXMLText(String text, Collection<ICompilerProblem> problems) {
		String filePath = FilenameNormalization.normalize("stdin.mxml");
		return formatMXMLTextInternal(filePath, text, problems);
	}

	public String formatMXMLText(String text) {
		return formatMXMLText(text, null);
	}

	/**
	 * Get the start up message that contains the program name with the copyright
	 * notice.
	 * 
	 * @return The startup message.
	 */
	protected String getStartMessage() {
		// This message should not be localized.
		String message = "Apache Royale ActionScript Formatter (asformat)" + NEWLINE + VersionInfo.buildMessage()
				+ NEWLINE;
		return message;
	}

	/**
	 * Get my program name.
	 * 
	 * @return always "asformat".
	 */
	protected String getProgramName() {
		return "asformat";
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
			configurator.setConfiguration(args, "files");
			configuration = configurator.getConfiguration();
			configBuffer = configurator.getConfigurationBuffer();

			problems.addAll(configurator.getConfigurationProblems());

			if (configBuffer.getVar("version") != null) {
				System.out.println(VersionInfo.buildMessage());
				return false;
			}

			// Print help if "-help" is present.
			final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
			if (helpVar != null || args.length == 0) {
				processHelp(helpVar);
				return false;
			}

			if (problems.hasErrors()) {
				return false;
			}

			collapseEmptyBlocks = configuration.getCollapseEmptyBlocks();
			ignoreProblems = configuration.getIgnoreParsingProblems();
			insertFinalNewLine = configuration.getInsertFinalNewLine();
			insertSpaceAfterCommaDelimiter = configuration.getInsertSpaceAfterCommaDelimiter();
			insertSpaceBetweenMetadataAttributes = configuration.getInsertSpaceBetweenMetadataAttributes();
			insertSpaceAfterFunctionKeywordForAnonymousFunctions = configuration
					.getInsertSpaceAfterFunctionKeywordForAnonymousFunctions();
			insertSpaceAfterKeywordsInControlFlowStatements = configuration
					.getInsertSpaceAfterKeywordsInControlFlowStatements();
			insertSpaceAfterSemicolonInForStatements = configuration.getInsertSpaceAfterSemicolonInForStatements();
			insertSpaceBeforeAndAfterBinaryOperators = configuration.getInsertSpaceBeforeAndAfterBinaryOperators();
			insertSpaceAtStartOfLineComment = configuration.getInsertSpaceAtStartOfLineComment();
			insertSpaces = configuration.getInsertSpaces();
			mxmlInsertNewLineBetweenAttributes = configuration.getMxmlInsertNewLineBetweenAttributes();
			mxmlAlignAttributes = configuration.getMxmlAlignAttributes();
			listChangedFiles = configuration.getListFiles();
			maxPreserveNewLines = configuration.getMaxPreserveNewLines();
			placeOpenBraceOnNewLine = configuration.getPlaceOpenBraceOnNewLine();
			semicolons = Semicolons.valueOf(configuration.getSemicolons().toUpperCase());
			tabSize = configuration.getTabSize();
			writeBackToInputFiles = configuration.getWriteFiles();
			for (String filePath : configuration.getFiles()) {
				File inputFile = new File(filePath);
				if (!inputFile.exists()) {
					throw new ConfigurationException("Input file does not exist: " + filePath, null, -1);
				}
				if (inputFile.isDirectory()) {
					addDirectory(inputFile);
				} else {
					inputFiles.add(inputFile);
				}
			}
			if (inputFiles.size() == 0 && listChangedFiles) {
				throw new ConfigurationException("Cannot use -list-files with standard input", null, -1);
			}
			if (writeBackToInputFiles) {
				if (inputFiles.size() == 0) {
					throw new ConfigurationException("Cannot use -write-files with standard input", null, -1);
				}
				for (File inputFile : inputFiles) {
					if (!inputFile.canWrite()) {
						throw new ConfigurationException("File is read-only: " + inputFile.getPath(), null, -1);
					}
				}
			}
			return true;
		} catch (ConfigurationException e) {
			final ICompilerProblem problem = new ConfigurationProblem(e);
			problems.add(problem);
			return false;
		} catch (Exception e) {
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

	private String formatMXMLScriptElement(String filePath, int line, String text,
			Collection<ICompilerProblem> problems) {
		String indent = "\t";
		if (insertSpaces) {
			indent = "";
			for (int i = 0; i < tabSize; i++) {
				indent += " ";
			}
		}
		StringBuilder builder = new StringBuilder();
		Pattern scriptPattern = Pattern.compile(
				"^<((?:mx|fx):(\\w+))>\\s*(<!\\[CDATA\\[)?((?:.|(?:\\r?\\n))*?)(?:\\]\\]>)?\\s*<\\/(?:mx|fx):(?:\\w+)>$");
		Matcher scriptMatcher = scriptPattern.matcher(text);
		if (!scriptMatcher.matches()) {
			return text;
		}
		if (problems == null) {
			// we need to know if there were problems because it means that we
			// need to return the original, unformatted text
			problems = new ArrayList<ICompilerProblem>();
		}
		String scriptTagText = scriptMatcher.group(1);
		String scriptTagName = scriptMatcher.group(2);
		String cdataText = scriptMatcher.group(3);
		String scriptText = scriptMatcher.group(4);
		boolean requireCdata = cdataText != null || "Script".equals(scriptTagName);
		String formattedScriptText = formatAS3TextInternal(filePath + "@Script[" + line + "]", scriptText, problems);
		if (!ignoreProblems && hasErrors(problems)) {
			return text;
		}
		if (formattedScriptText.length() > 0) {
			String[] formattedLines = formattedScriptText.split("\n");
			String lineIndent = requireCdata ? (indent + indent + indent) : (indent + indent);
			for (int i = 0; i < formattedLines.length; i++) {
				formattedLines[i] = lineIndent + formattedLines[i];
			}
			formattedScriptText = String.join("\n", formattedLines);
		}
		builder.append(indent);
		builder.append("<");
		builder.append(scriptTagText);
		builder.append(">\n");
		if (requireCdata) {
			builder.append(indent);
			builder.append(indent);
			builder.append("<![CDATA[\n");
		}
		if (formattedScriptText.length() > 0) {
			builder.append(formattedScriptText);
			builder.append("\n");
		}
		if (requireCdata) {
			builder.append(indent);
			builder.append(indent);
			builder.append("]]>\n");
		}
		builder.append(indent);
		builder.append("</");
		builder.append(scriptTagText);
		builder.append(">");

		return builder.toString();
	}

	private String formatAS3TextInternal(String filePath, String text, Collection<ICompilerProblem> problems) {
		if (problems == null) {
			problems = new ArrayList<ICompilerProblem>();
		}

		StringReader textReader = new StringReader(text);
		StreamingASTokenizer tokenizer = null;
		ASToken[] streamingTokens = null;
		try {
			tokenizer = StreamingASTokenizer.createForRepairingASTokenizer(textReader, filePath,
					IncludeHandler.creatDefaultIncludeHandler());
			tokenizer.setCollectComments(true);
			tokenizer.setFollowIncludes(false);
			streamingTokens = tokenizer.getTokens(textReader);
		} finally {
			IOUtils.closeQuietly(textReader);
			IOUtils.closeQuietly(tokenizer);
		}

		if (tokenizer.hasTokenizationProblems()) {
			problems.addAll(tokenizer.getTokenizationProblems());
		}

		if (!ignoreProblems && hasErrors(problems)) {
			return text;
		}

		// temporarily remove the comments from the token list because ASParser
		// doesn't know how to deal with them properly.
		// we'll add them back at the same locations after the parser is done.
		List<ASToken> comments = new ArrayList<ASToken>();
		List<ASToken> streamingTokensList = new ArrayList<ASToken>();
		for (ASToken token : streamingTokens) {
			if (token.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					|| token.getType() == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT) {
				comments.add(token);
			} else {
				streamingTokensList.add(token);
			}
		}

		Workspace workspace = new Workspace();
		RepairingTokenBuffer buffer = new RepairingTokenBuffer(streamingTokensList.toArray(new ASToken[0]));
		ASParser parser = new ASParser(workspace, buffer);
		FileNode node = new FileNode(workspace);
		try {
			parser.file(node);
		} catch (Exception e) {
			parser = null;
			problems.add(new UnexpectedExceptionProblem(e));
			return text;
		}

		if (tokenizer.hasTokenizationProblems()) {
			problems.addAll(tokenizer.getTokenizationProblems());
		}

		if (parser.getSyntaxProblems().size() > 0) {
			problems.addAll(parser.getSyntaxProblems());
		}

		if (!ignoreProblems && hasErrors(problems)) {
			return text;
		}

		List<IASToken> repairedTokensList = new ArrayList<IASToken>(Arrays.asList(buffer.getTokens(true)));
		// restore the comments that were removed before parsing
		IASToken nextComment = null;
		for (int i = 0; i < repairedTokensList.size(); i++) {
			if (nextComment == null) {
				if (comments.size() == 0) {
					// no more comments to add
					break;
				}
				nextComment = comments.get(0);
			}
			IASToken currentToken = repairedTokensList.get(i);
			if (nextComment.getAbsoluteStart() <= currentToken.getAbsoluteStart()) {
				repairedTokensList.add(i, nextComment);
				nextComment = null;
				comments.remove(0);
			}
		}
		// there may be some comments left that didn't appear before any
		// of the repaired tokens, so add them all at the end
		repairedTokensList.addAll(comments);

		List<IASToken> tokens = insertExtraAS3Tokens(repairedTokensList, text);
		try {
			return parseTokens(filePath, tokens, node);
		} catch (Exception e) {
			if (problems != null) {
				System.err.println(e);
				e.printStackTrace(System.err);
				problems.add(new UnexpectedExceptionProblem(e));
			}
			return text;
		}

	}

	private List<IASToken> insertExtraAS3Tokens(List<IASToken> originalTokens, String text) {
		ArrayList<IASToken> tokens = new ArrayList<IASToken>();
		IASToken prevToken = null;
		for (IASToken token : originalTokens) {
			if (prevToken != null) {

				boolean skipSemicolon = token.getType() == ASTokenTypes.TOKEN_SEMICOLON && token.isImplicit()
						&& prevToken != null && (prevToken.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
								|| prevToken.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN);
				if (skipSemicolon) {
					continue;
				}

				int start = prevToken.getAbsoluteEnd();
				int end = token.getAbsoluteStart();
				if (end > start) {
					String tokenText = text.substring(start, end);
					ASToken extraToken = new ASToken(TOKEN_TYPE_EXTRA, start, end, prevToken.getEndLine(),
							prevToken.getEndColumn(), tokenText);
					extraToken.setEndLine(token.getLine());
					extraToken.setEndLine(token.getColumn());
					tokens.add(extraToken);
				}
			}
			tokens.add(token);
			prevToken = token;
		}
		if (prevToken != null) {
			int start = prevToken.getAbsoluteEnd();
			int end = text.length();
			if (end > start) {
				String tokenText = text.substring(start, end);
				ASToken extraToken = new ASToken(TOKEN_TYPE_EXTRA, start, end, prevToken.getEndLine(),
						prevToken.getEndColumn(), tokenText);
				extraToken.setEndLine(prevToken.getLine());
				extraToken.setEndLine(prevToken.getColumn());
				tokens.add(extraToken);
			}
		}
		return tokens;
	}

	private List<IMXMLToken> insertExtraMXMLTokens(IMXMLToken[] originalTokens, String text) {
		ArrayList<IMXMLToken> tokens = new ArrayList<IMXMLToken>();
		IMXMLToken prevToken = null;
		for (IMXMLToken token : originalTokens) {
			if (prevToken != null) {
				int start = prevToken.getEnd();
				int end = token.getStart();
				if (end > start) {
					String tokenText = text.substring(start, end);
					MXMLToken extraToken = new MXMLToken(TOKEN_TYPE_EXTRA, start, end, prevToken.getLine(),
							prevToken.getColumn() + end - start, tokenText);
					extraToken.setEndLine(token.getLine());
					extraToken.setEndLine(token.getColumn());
					tokens.add(extraToken);
				}
			}
			tokens.add(token);
			prevToken = token;
		}
		if (prevToken != null) {
			int start = prevToken.getEnd();
			int end = text.length();
			if (end > start) {
				String tokenText = text.substring(start, end);
				MXMLToken extraToken = new MXMLToken(TOKEN_TYPE_EXTRA, start, end, prevToken.getLine(),
						prevToken.getColumn() + end - start, tokenText);
				extraToken.setEndLine(prevToken.getLine());
				extraToken.setEndLine(prevToken.getColumn());
				tokens.add(extraToken);
			}
		}
		return tokens;
	}

	private IASToken getNextTokenSkipExtra(List<IASToken> tokens, int startIndex) {
		for (int i = startIndex; i < tokens.size(); i++) {
			IASToken token = tokens.get(i);
			if (token.getType() != TOKEN_TYPE_EXTRA) {
				return token;
			}
		}
		return null;
	}

	private IASToken getNextTokenSkipExtraAndComments(List<IASToken> tokens, int startIndex) {
		for (int i = startIndex; i < tokens.size(); i++) {
			IASToken token = tokens.get(i);
			if (token.getType() != TOKEN_TYPE_EXTRA && token.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					&& token.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_ASDOC_COMMENT) {
				return token;
			}
		}
		return null;
	}

	private String parseTokens(String filePath, List<IASToken> tokens, FileNode node) throws Exception {
		int indent = 0;
		boolean inCaseOrDefaultClause = false;
		boolean inControlFlowStatement = false;
		boolean inVarOrConstDeclaration = false;
		boolean inFunctionDeclaration = false;
		boolean inPackageDeclaration = false;
		boolean inClassDeclaration = false;
		boolean inInterfaceDeclaration = false;
		boolean blockOpenPending = false;
		boolean indentedStatement = false;
		boolean caseOrDefaultBlockOpenPending = false;
		boolean skipFormatting = false;
		int varOrConstChainLevel = -1;
		List<BlockStackItem> blockStack = new ArrayList<BlockStackItem>();
		int controlFlowParenStack = 0;
		int ternaryStack = 0;
		int numRequiredNewLines = 0;
		boolean requiredSpace = false;
		IASToken prevTokenNotComment = null;
		IASToken prevToken = null;
		IASToken prevTokenOrExtra = null;
		IASToken token = null;
		IASToken nextToken = null;
		IASToken nextTokenOrExtra = null;
		IASToken nextTokenNotComment = null;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			token = tokens.get(i);
			if (token.getType() == TOKEN_TYPE_EXTRA) {
				if (skipFormatting) {
					builder.append(token.getText());
				} else {
					if (i == (tokens.size() - 1)) {
						// if the last token is whitespace, include at most one
						// new line, but strip the rest
						numRequiredNewLines = Math.min(1, Math.max(0, countNewLinesInExtra(token)));
						appendNewLines(builder, numRequiredNewLines);
						break;
					}
					if (!blockOpenPending) {
						int newLinesInExtra = countNewLinesInExtra(token);
						if (prevToken != null && prevToken.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT) {
							newLinesInExtra++;
						}
						boolean oneLineBlock = prevToken != null && prevToken.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN
								&& nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE;
						if (oneLineBlock && collapseEmptyBlocks) {
							newLinesInExtra = 0;
						}
						numRequiredNewLines = Math.max(numRequiredNewLines, newLinesInExtra);
						if (!indentedStatement && numRequiredNewLines > 0 && prevTokenNotComment != null
								&& prevTokenNotComment.getType() != ASTokenTypes.TOKEN_SEMICOLON
								&& prevTokenNotComment.getType() != ASTokenTypes.TOKEN_BLOCK_CLOSE
								&& !(caseOrDefaultBlockOpenPending
										&& prevTokenNotComment.getType() == ASTokenTypes.TOKEN_COLON)
								&& !(prevTokenNotComment instanceof MetaDataPayloadToken)) {
							boolean needsIndent = prevTokenNotComment.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN
									|| (!blockStack.isEmpty() && blockStack
											.get(blockStack.size() - 1) instanceof ObjectLiteralBlockStackItem);
							if (needsIndent) {
								indentedStatement = true;
								indent = increaseIndent(indent);
							}
						}
					}
				}
				prevTokenOrExtra = token;
				continue;
			}
			nextTokenOrExtra = ((i + 1) < tokens.size()) ? tokens.get(i + 1) : null;
			nextToken = getNextTokenSkipExtra(tokens, i + 1);
			nextTokenNotComment = getNextTokenSkipExtraAndComments(tokens, i + 1);

			boolean skipWhitespaceBeforeSemicolon = nextToken == null
					|| nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON;

			// characters that must appear before the token
			if (token instanceof MetaDataPayloadToken) {
				numRequiredNewLines = Math.max(numRequiredNewLines, 1);
			} else {
				switch (token.getType()) {
					case ASTokenTypes.TOKEN_BLOCK_OPEN: {
						if (!blockOpenPending) {
							// detect some cases of blocks that may have been
							// missed earlier (these will be standalone blocks,
							// without a control flow statement or definition).
							// this should not detect object literals, though!
							blockOpenPending = prevTokenNotComment == null
									|| prevTokenNotComment.getType() == ASTokenTypes.TOKEN_SEMICOLON
									|| prevTokenNotComment.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN
									|| prevTokenNotComment.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE;
							if (!blockOpenPending && prevTokenNotComment.getType() == ASTokenTypes.TOKEN_COLON
									&& !blockStack.isEmpty()) {
								IASToken blockToken = blockStack.get(blockStack.size() - 1).token;
								blockOpenPending = blockToken.getType() == ASTokenTypes.TOKEN_KEYWORD_DEFAULT
										|| blockToken.getType() == ASTokenTypes.TOKEN_KEYWORD_CASE;
							}
							if (blockOpenPending) {
								blockStack.add(new BlockStackItem(token));
							}
						}
						if (blockOpenPending) {
							if (indentedStatement) {
								indentedStatement = false;
								indent = decreaseIndent(indent);
							}
							boolean oneLineBlock = nextToken != null
									&& nextToken.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE;
							boolean needsNewLine = placeOpenBraceOnNewLine && (!collapseEmptyBlocks || !oneLineBlock);
							if (needsNewLine) {
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							} else {
								if (oneLineBlock && collapseEmptyBlocks) {
									numRequiredNewLines = 0;
								}
								requiredSpace = true;
							}
						} else {
							// probably an object literal
							blockStack.add(new ObjectLiteralBlockStackItem(token));
							indent = increaseIndent(indent);
						}
						requiredSpace = true;
						break;
					}
					case ASTokenTypes.TOKEN_BLOCK_CLOSE: {
						boolean skipSwitchDecrease = false;
						if (!blockStack.isEmpty()) {
							BlockStackItem stackItem = blockStack.get(blockStack.size() - 1);
							if (stackItem.blockDepth <= 1) {
								boolean oneLineBlock = prevToken != null
										&& prevToken.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN;
								if (!collapseEmptyBlocks || !oneLineBlock) {
									indent = decreaseIndent(indent);
								}
								if (stackItem.token.getType() == ASTokenTypes.TOKEN_KEYWORD_CASE
										|| stackItem.token.getType() == ASTokenTypes.TOKEN_KEYWORD_DEFAULT) {
									blockStack.remove(blockStack.size() - 1);
									indent = decreaseIndent(indent);
									skipSwitchDecrease = true;
								}
							}
						}
						if (!skipSwitchDecrease && !blockStack.isEmpty()) {
							BlockStackItem stackItem = blockStack.get(blockStack.size() - 1);
							if (stackItem.token.getType() == ASTokenTypes.TOKEN_KEYWORD_SWITCH) {
								SwitchBlockStackItem switchStackItem = (SwitchBlockStackItem) stackItem;
								if (switchStackItem.clauseCount > 0
										&& (prevToken == null
												|| prevToken.getType() != ASTokenTypes.TOKEN_BLOCK_CLOSE)) {
									indent = decreaseIndent(indent);
								}
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_SQUARE_CLOSE: {
						if (!blockStack.isEmpty()) {
							BlockStackItem item = blockStack.get(blockStack.size() - 1);
							if (item.token.getType() == ASTokenTypes.TOKEN_SQUARE_OPEN) {
								indent = decreaseIndent(indent);
								blockStack.remove(item);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_PAREN_CLOSE: {
						if (!blockStack.isEmpty()) {
							BlockStackItem item = blockStack.get(blockStack.size() - 1);
							if (item.token.getType() == ASTokenTypes.TOKEN_PAREN_OPEN) {
								indent = decreaseIndent(indent);
								blockStack.remove(item);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_AS:
					case ASTokenTypes.TOKEN_KEYWORD_IS:
					case ASTokenTypes.TOKEN_KEYWORD_IN:
					case ASTokenTypes.TOKEN_RESERVED_WORD_EACH:
					case ASTokenTypes.TOKEN_RESERVED_WORD_EXTENDS:
					case ASTokenTypes.TOKEN_RESERVED_WORD_IMPLEMENTS:
					case ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT:
					case ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT: {
						// needs an extra space before the token
						requiredSpace = true;
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_NOT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_STRICT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_STRICT_NOT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_LESS_THAN:
					case ASTokenTypes.TOKEN_OPERATOR_GREATER_THAN:
					case ASTokenTypes.TOKEN_OPERATOR_LESS_THAN_EQUALS:
					case ASTokenTypes.TOKEN_OPERATOR_GREATER_THAN_EQUALS:
					case ASTokenTypes.TOKEN_OPERATOR_DIVISION:
					case ASTokenTypes.TOKEN_OPERATOR_MODULO:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_AND:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_OR:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_XOR:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_AND:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_OR:
					case ASTokenTypes.TOKEN_OPERATOR_PLUS_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MODULO_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT: {
						if (insertSpaceBeforeAndAfterBinaryOperators) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_STAR: {
						boolean isAnyType = checkTokenBeforeAnyType(prevTokenNotComment);
						boolean isAnyVectorType = checkTokensForAnyVectorType(prevTokenNotComment, nextTokenNotComment);
						if (!isAnyType && !isAnyVectorType && insertSpaceBeforeAndAfterBinaryOperators
								&& !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_PLUS:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS: {
						boolean isUnary = checkTokenBeforeUnaryOperator(prevTokenNotComment);
						if (!isUnary && insertSpaceBeforeAndAfterBinaryOperators) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT: {
						inVarOrConstDeclaration = false;
						if (insertSpaceBeforeAndAfterBinaryOperators) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_TERNARY: {
						ternaryStack++;
						if (insertSpaceBeforeAndAfterBinaryOperators) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_ELLIPSIS: {
						boolean isFirstArg = prevToken == null || prevToken.getType() == ASTokenTypes.TOKEN_PAREN_OPEN;
						if (!isFirstArg) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_DEFAULT:
					case ASTokenTypes.TOKEN_KEYWORD_CASE: {
						if (!blockStack.isEmpty()) {
							BlockStackItem stackItem = blockStack.get(blockStack.size() - 1);
							switch (stackItem.token.getType()) {
								case ASTokenTypes.TOKEN_KEYWORD_DEFAULT:
								case ASTokenTypes.TOKEN_KEYWORD_CASE: {
									blockStack.remove(blockStack.size() - 1);
									indent = decreaseIndent(indent);
									break;
								}
							}
						}
						if (!blockStack.isEmpty()) {
							BlockStackItem stackItem = blockStack.get(blockStack.size() - 1);
							if (stackItem.token.getType() == ASTokenTypes.TOKEN_KEYWORD_SWITCH) {
								SwitchBlockStackItem switchStackItem = (SwitchBlockStackItem) stackItem;
								switchStackItem.clauseCount++;
								inCaseOrDefaultClause = true;
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
								blockStack.add(new BlockStackItem(token));
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_COLON: {
						if (ternaryStack > 0) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_SEMICOLON: {
						if (indentedStatement) {
							indentedStatement = false;
							indent = decreaseIndent(indent);
						}
						inVarOrConstDeclaration = false;
						varOrConstChainLevel = -1;
						break;
					}
					case ASTokenTypes.TOKEN_ASDOC_COMMENT: {
						if (prevToken != null && prevToken.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN) {
							numRequiredNewLines = Math.max(numRequiredNewLines, 1);
						} else {
							// add an extra line before an asdoc comment
							numRequiredNewLines = Math.max(numRequiredNewLines, 2);
						}
						break;
					}
				}
			}
			if (!skipFormatting && prevToken != null) {
				if (numRequiredNewLines > 0) {
					appendNewLines(builder, numRequiredNewLines);
					appendIndent(builder, indent);
				} else if (requiredSpace) {
					builder.append(' ');
				}
			}
			switch (token.getType()) {
				case ASTokenTypes.TOKEN_BLOCK_OPEN: {
					if (blockOpenPending) {
						boolean oneLineBlock = nextToken != null
								&& nextToken.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE;
						if (placeOpenBraceOnNewLine && (!collapseEmptyBlocks || !oneLineBlock)) {
							indent = increaseIndent(indent);
						}
					}
					break;
				}
			}

			// include the token's own text
			builder.append(getTokenText(token, indent, skipFormatting));

			// characters that must appear after the token
			if (token.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					&& token.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_ASDOC_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN) {
				blockOpenPending = false;
			}
			if (token.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					&& token.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_ASDOC_COMMENT) {
				caseOrDefaultBlockOpenPending = false;
			}
			requiredSpace = false;
			numRequiredNewLines = 0;
			if (token instanceof MetaDataPayloadToken) {
				numRequiredNewLines = Math.max(numRequiredNewLines, 1);
			} else {
				switch (token.getType()) {
					case ASTokenTypes.TOKEN_SEMICOLON: {
						if (inControlFlowStatement && isInForStatement(blockStack)) {
							if (insertSpaceAfterSemicolonInForStatements) {
								requiredSpace = true;
							}
							// else no space
						} else {
							boolean checkNext = true;
							while (!blockStack.isEmpty() && checkNext) {
								checkNext = false;
								BlockStackItem prevStackItem = blockStack.get(blockStack.size() - 1);
								if (prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_CASE
										&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_DEFAULT
										&& prevStackItem.blockDepth <= 0) {
									blockStack.remove(blockStack.size() - 1);
									if (prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_CLASS
											&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_INTERFACE
											&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_FUNCTION) {
										checkNext = !prevStackItem.braces;
										indent = decreaseIndent(indent);
									}
								}
							}
						}
						if (!inControlFlowStatement) {
							if (nextToken != null
									&& (nextToken.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
											|| nextToken.getType() == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT)) {
								requiredSpace = true;
							} else {
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_BLOCK_OPEN: {
						if (blockOpenPending) {
							blockOpenPending = false;
							if (!blockStack.isEmpty()) {
								BlockStackItem item = blockStack.get(blockStack.size() - 1);
								item.blockDepth++;
							}
							boolean oneLineBlock = nextToken != null
									&& nextToken.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE;
							if (!collapseEmptyBlocks || !oneLineBlock) {
								if (!placeOpenBraceOnNewLine) {
									indent = increaseIndent(indent);
								}
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_BLOCK_CLOSE: {
						if (!blockStack.isEmpty()) {
							BlockStackItem item = blockStack.get(blockStack.size() - 1);
							item.blockDepth--;
							if (item.blockDepth <= 0) {
								blockStack.remove(blockStack.size() - 1);
							}
							if (!(item instanceof ObjectLiteralBlockStackItem)
									&& (nextToken == null || (nextToken.getType() != ASTokenTypes.TOKEN_SEMICOLON
											&& nextToken.getType() != ASTokenTypes.TOKEN_PAREN_CLOSE
											&& nextToken.getType() != ASTokenTypes.TOKEN_COMMA
											&& nextToken.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
											&& nextToken.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT))) {
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_SQUARE_OPEN:
						indent = increaseIndent(indent);
						blockStack.add(new BlockStackItem(token));
						break;
					case ASTokenTypes.TOKEN_PAREN_OPEN: {
						indent = increaseIndent(indent);
						blockStack.add(new BlockStackItem(token));
						if (inControlFlowStatement) {
							controlFlowParenStack++;
						}
						break;
					}
					case ASTokenTypes.TOKEN_PAREN_CLOSE: {
						if (inControlFlowStatement) {
							controlFlowParenStack--;
							if (controlFlowParenStack <= 0) {
								inControlFlowStatement = false;
								controlFlowParenStack = 0;
								blockOpenPending = true;
								if (nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON) {
									blockStack.remove(blockStack.size() - 1);
									if (!skipWhitespaceBeforeSemicolon) {
										numRequiredNewLines = Math.max(numRequiredNewLines, 1);
									}
								} else if (nextToken != null && nextToken.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN
										&& nextToken.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
										&& !skipWhitespaceBeforeSemicolon) {
									indent = increaseIndent(indent);
									BlockStackItem item = blockStack.get(blockStack.size() - 1);
									item.braces = false;
									numRequiredNewLines = Math.max(numRequiredNewLines, 1);
								}
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_INCREMENT:
					case ASTokenTypes.TOKEN_OPERATOR_DECREMENT: {
						if (!inControlFlowStatement && prevToken != null
								&& prevToken.getType() != ASTokenTypes.TOKEN_SEMICOLON && nextToken != null
								&& nextToken.getType() != ASTokenTypes.TOKEN_SEMICOLON) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_CONTINUE:
					case ASTokenTypes.TOKEN_KEYWORD_BREAK:
					case ASTokenTypes.TOKEN_KEYWORD_RETURN: {
						if (!skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_PACKAGE: {
						blockStack.add(new BlockStackItem(token));
						requiredSpace = true;
						inPackageDeclaration = true;
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_CLASS: {
						blockStack.add(new BlockStackItem(token));
						requiredSpace = true;
						inClassDeclaration = true;
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_INTERFACE: {
						blockStack.add(new BlockStackItem(token));
						requiredSpace = true;
						inInterfaceDeclaration = true;
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_FUNCTION: {
						blockStack.add(new BlockStackItem(token));
						inFunctionDeclaration = true;
						boolean skipSpace = !insertSpaceAfterFunctionKeywordForAnonymousFunctions
								&& (nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_PAREN_OPEN);
						if (!skipSpace) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_VAR:
					case ASTokenTypes.TOKEN_KEYWORD_CONST: {
						inVarOrConstDeclaration = true;
						requiredSpace = true;
						varOrConstChainLevel = blockStack.size();
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_CATCH:
					case ASTokenTypes.TOKEN_KEYWORD_FOR:
					case ASTokenTypes.TOKEN_KEYWORD_IF:
					case ASTokenTypes.TOKEN_KEYWORD_WHILE:
					case ASTokenTypes.TOKEN_KEYWORD_WITH: {
						inControlFlowStatement = true;
						blockStack.add(new BlockStackItem(token));
						if (insertSpaceAfterKeywordsInControlFlowStatements && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_SWITCH: {
						inControlFlowStatement = true;
						blockStack.add(new SwitchBlockStackItem(token));
						if (insertSpaceAfterKeywordsInControlFlowStatements && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_TRY: {
						blockStack.add(new BlockStackItem(token));
						if (!skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						blockOpenPending = true;
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_ELSE: {
						if (nextTokenNotComment != null
								&& nextTokenNotComment.getType() == ASTokenTypes.TOKEN_KEYWORD_IF) {
							requiredSpace = true;
						} else {
							blockStack.add(new BlockStackItem(token));
							blockOpenPending = true;
							if (nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON) {
								blockStack.remove(blockStack.size() - 1);
								if (!skipWhitespaceBeforeSemicolon) {
									numRequiredNewLines = Math.max(numRequiredNewLines, 1);
								}
							} else if (nextToken != null && nextToken.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN
									&& nextToken.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
									&& !skipWhitespaceBeforeSemicolon) {
								indent = increaseIndent(indent);
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_DO: {
						blockStack.add(new BlockStackItem(token));
						blockOpenPending = true;
						if (nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON) {
							blockStack.remove(blockStack.size() - 1);
							if (!skipWhitespaceBeforeSemicolon) {
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							}
						} else if (nextToken != null && nextToken.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN
								&& nextToken.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
								&& !skipWhitespaceBeforeSemicolon) {
							indent = increaseIndent(indent);
							numRequiredNewLines = Math.max(numRequiredNewLines, 1);
						}
						break;
					}
					case ASTokenTypes.TOKEN_COLON: {
						if (!inControlFlowStatement && !inVarOrConstDeclaration && !inFunctionDeclaration) {
							if (inCaseOrDefaultClause) {
								inCaseOrDefaultClause = false;
								caseOrDefaultBlockOpenPending = true;
								boolean nextIsBlock = nextTokenNotComment != null
										&& nextTokenNotComment.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN;
								if (nextIsBlock) {
									IASToken afterBlockClose = findTokenAfterBlock(nextTokenNotComment, tokens);
									if (afterBlockClose != null) {
										if (afterBlockClose.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE
												|| afterBlockClose.getType() == ASTokenTypes.TOKEN_KEYWORD_CASE
												|| afterBlockClose.getType() == ASTokenTypes.TOKEN_KEYWORD_DEFAULT) {
											blockOpenPending = true;
											requiredSpace = true;
											blockStack.remove(blockStack.size() - 1);
										}
									}
								}
								if (!nextIsBlock || !blockOpenPending) {
									indent = increaseIndent(indent);
									if (nextToken != null && (nextToken
											.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
											|| nextToken.getType() == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT)) {
										requiredSpace = true;
									} else {
										numRequiredNewLines = Math.max(numRequiredNewLines, 1);
									}
								}
							} else if (ternaryStack > 0) {
								ternaryStack--;
								requiredSpace = true;
							} else {
								requiredSpace = true;
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_CASE: {
						if (!skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_DEFAULT: {
						if (!inCaseOrDefaultClause && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_KEYWORD_AS:
					case ASTokenTypes.TOKEN_KEYWORD_DELETE:
					case ASTokenTypes.TOKEN_KEYWORD_IMPORT:
					case ASTokenTypes.TOKEN_KEYWORD_IN:
					case ASTokenTypes.TOKEN_KEYWORD_INCLUDE:
					case ASTokenTypes.TOKEN_KEYWORD_INSTANCEOF:
					case ASTokenTypes.TOKEN_KEYWORD_IS:
					case ASTokenTypes.TOKEN_KEYWORD_NEW:
					case ASTokenTypes.TOKEN_KEYWORD_THROW:
					case ASTokenTypes.TOKEN_KEYWORD_TYPEOF:
					case ASTokenTypes.TOKEN_KEYWORD_USE:
					case ASTokenTypes.TOKEN_RESERVED_WORD_CONFIG:
					case ASTokenTypes.TOKEN_RESERVED_WORD_EXTENDS:
					case ASTokenTypes.TOKEN_RESERVED_WORD_GET:
					case ASTokenTypes.TOKEN_RESERVED_WORD_GOTO:
					case ASTokenTypes.TOKEN_RESERVED_WORD_IMPLEMENTS:
					case ASTokenTypes.TOKEN_RESERVED_WORD_NAMESPACE:
					case ASTokenTypes.TOKEN_RESERVED_WORD_SET:
					case ASTokenTypes.TOKEN_MODIFIER_ABSTRACT:
					case ASTokenTypes.TOKEN_MODIFIER_DYNAMIC:
					case ASTokenTypes.TOKEN_MODIFIER_FINAL:
					case ASTokenTypes.TOKEN_MODIFIER_NATIVE:
					case ASTokenTypes.TOKEN_MODIFIER_OVERRIDE:
					case ASTokenTypes.TOKEN_MODIFIER_STATIC:
					case ASTokenTypes.TOKEN_MODIFIER_VIRTUAL:
					case ASTokenTypes.TOKEN_NAMESPACE_ANNOTATION: {
						if (!skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_NOT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_STRICT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_STRICT_NOT_EQUAL:
					case ASTokenTypes.TOKEN_OPERATOR_LESS_THAN:
					case ASTokenTypes.TOKEN_OPERATOR_GREATER_THAN:
					case ASTokenTypes.TOKEN_OPERATOR_LESS_THAN_EQUALS:
					case ASTokenTypes.TOKEN_OPERATOR_GREATER_THAN_EQUALS:
					case ASTokenTypes.TOKEN_OPERATOR_DIVISION:
					case ASTokenTypes.TOKEN_OPERATOR_MODULO:
					case ASTokenTypes.TOKEN_OPERATOR_TERNARY:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_AND:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_OR:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_XOR:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_AND:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_OR:
					case ASTokenTypes.TOKEN_OPERATOR_PLUS_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_MODULO_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT:
					case ASTokenTypes.TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT: {
						if (insertSpaceBeforeAndAfterBinaryOperators && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_STAR: {
						boolean isAnyType = checkTokenBeforeAnyType(prevTokenNotComment);
						boolean isAnyVectorType = checkTokensForAnyVectorType(prevTokenNotComment, nextTokenNotComment);
						if (!isAnyType && !isAnyVectorType && insertSpaceBeforeAndAfterBinaryOperators
								&& !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_OPERATOR_PLUS:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS: {
						boolean isUnary = checkTokenBeforeUnaryOperator(prevTokenNotComment);
						if (!isUnary && insertSpaceBeforeAndAfterBinaryOperators && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.TOKEN_COMMA: {
						if (varOrConstChainLevel == blockStack.size()) {
							inVarOrConstDeclaration = true;
						}
						if (insertSpaceAfterCommaDelimiter && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT: {
						numRequiredNewLines = Math.max(numRequiredNewLines, 1);

						String trimmed = token.getText().substring(2).trim();
						if (!skipFormatting && FORMATTER_TAG_OFF.equals(trimmed)) {
							skipFormatting = true;
							appendNewLines(builder, 1);
						} else if (skipFormatting && FORMATTER_TAG_ON.equals(trimmed)) {
							skipFormatting = false;
							numRequiredNewLines = 0;
						}
						break;
					}
					case ASTokenTypes.TOKEN_ASDOC_COMMENT:
					case ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT: {
						if (!skipWhitespaceBeforeSemicolon) {
							if (nextTokenOrExtra != null && nextTokenOrExtra.getType() == TOKEN_TYPE_EXTRA) {
								requiredSpace = true;
							}
						}
						break;
					}
				}
			}
			if ((inPackageDeclaration || inClassDeclaration || inInterfaceDeclaration || inFunctionDeclaration)
					&& nextToken != null && nextToken.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN) {
				blockOpenPending = true;
				inPackageDeclaration = false;
				inClassDeclaration = false;
				inInterfaceDeclaration = false;
				inFunctionDeclaration = false;
			}
			prevToken = token;
			prevTokenOrExtra = prevToken;
			if (prevToken.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					&& prevToken.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
					&& prevToken.getType() != ASTokenTypes.TOKEN_ASDOC_COMMENT) {
				prevTokenNotComment = prevToken;
			}
		}
		if (blockStack.size() > 0) {
			throw new Exception("Block stack size too large");
		}
		return builder.toString();
	}

	private IASToken findTokenAfterBlock(IASToken tokenBlockOpen, List<IASToken> tokens) {
		List<IASToken> stack = new ArrayList<IASToken>();
		int startIndex = tokens.indexOf(tokenBlockOpen) + 1;
		for (int i = startIndex; i < tokens.size(); i++) {
			IASToken current = tokens.get(i);
			if (current.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN) {
				stack.add(current);
			} else if (current.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE) {
				if (stack.size() == 0) {
					return getNextTokenSkipExtraAndComments(tokens, i + 1);
				}
				stack.remove(stack.size() - 1);
			}
		}
		return null;
	}

	private int countNewLinesInExtra(IASToken tokenOrExtra) {
		if (tokenOrExtra == null || tokenOrExtra.getType() != TOKEN_TYPE_EXTRA) {
			return 0;
		}
		int numNewLinesInWhitespace = 0;
		String whitespace = tokenOrExtra.getText();
		int index = -1;
		while ((index = whitespace.indexOf('\n', index + 1)) != -1) {
			numNewLinesInWhitespace++;
		}
		return numNewLinesInWhitespace;
	}

	private String formatSingleLineComment(String comment) {
		comment = comment.substring(2).trim();
		StringBuilder builder = new StringBuilder();
		builder.append("//");
		if (insertSpaceAtStartOfLineComment) {
			builder.append(" ");
		}
		builder.append(comment);
		return builder.toString();
	}

	private String formatMultiLineComment(String comment) {
		return comment;
	}

	private String formatLiteralString(String string) {
		String charsToEscape = "\b\t\n\f\r\\";
		String escapeChars = "btnfr\\";
		int escapeIndex = -1;
		char currChar;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < string.length(); ++i) {
			currChar = string.charAt(i);
			if (i == 0) {
				charsToEscape += currChar;
				escapeChars += currChar;
				builder.append(currChar);
				continue;
			}
			if (i == string.length() - 1) {
				builder.append(currChar);
				continue;
			}
			escapeIndex = charsToEscape.indexOf(currChar);
			if (escapeIndex != -1) {
				builder.append("\\");
				builder.append(escapeChars.charAt(escapeIndex));
			} else {
				builder.append(currChar);
			}
		}
		return builder.toString();
	}

	private boolean isInForStatement(List<BlockStackItem> blockStack) {
		for (int i = blockStack.size() - 1; i >= 0; i--) {
			BlockStackItem item = blockStack.get(i);
			switch (item.token.getType()) {
				case ASTokenTypes.TOKEN_BLOCK_OPEN:
				case ASTokenTypes.TOKEN_SQUARE_OPEN:
				case ASTokenTypes.TOKEN_PAREN_OPEN: {
					// these tokens are fine, keep searching
					break;
				}
				case ASTokenTypes.TOKEN_KEYWORD_FOR: {
					return true;
				}
				default: {
					return false;
				}
			}
		}
		return false;
	}

	private boolean isInListing(String lineText, boolean alreadyInListing) {
		int searchIndex = 0;
		boolean inListing = alreadyInListing;
		while (searchIndex < lineText.length()) {
			if (!inListing) {
				searchIndex = lineText.indexOf("<listing", searchIndex);
				if (searchIndex == -1) {
					return false;
				}
				searchIndex += 8;
				inListing = true;
			}
			searchIndex = lineText.indexOf("</listing>", searchIndex);
			if (searchIndex == -1) {
				return true;
			}
			searchIndex += 10;
			inListing = false;
		}
		return inListing;
	}

	private String formatASDocComment(String comment, int indent) {
		String[] lines = comment.split("\n");
		StringBuilder builder = new StringBuilder();
		String lineText = lines[0].trim();
		builder.append(lineText);
		boolean inListing = isInListing(lineText, false);
		if (lines.length > 1) {
			builder.append('\n');
		}
		String listingIndent = null;
		for (int i = 1; i < lines.length - 1; i++) {
			lineText = lines[i].trim();
			if (inListing) {
				Matcher startMatcher = ASDOC_START_LINE_PATTERN.matcher(lineText);
				if (startMatcher.find()) {
					if (listingIndent == null) {
						listingIndent = startMatcher.group(1);
					} else if (startMatcher.group().length() >= lineText.length()) {
						lineText = "*" + listingIndent;
					}
				}
			}
			appendIndent(builder, indent);
			builder.append(' ');
			builder.append(lineText);
			builder.append('\n');
			inListing = isInListing(lineText, inListing);
			if (!inListing) {
				listingIndent = null;
			}
		}
		if (lines.length > 1) {
			appendIndent(builder, indent);
			builder.append(' ');
			builder.append(lines[lines.length - 1].trim());
		}
		return builder.toString();
	}

	private String getTokenText(IASToken token, int indent, boolean skipFormatting) {

		if (token instanceof MetaDataPayloadToken) {
			MetaDataPayloadToken metaPlayloadToken = (MetaDataPayloadToken) token;
			return formatMetadataToken(metaPlayloadToken);
		} else {
			switch (token.getType()) {
				case ASTokenTypes.TOKEN_ASDOC_COMMENT: {
					if (skipFormatting) {
						return token.getText();
					}
					return formatASDocComment(token.getText(), indent);
				}
				case ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT: {
					if (skipFormatting) {
						return token.getText();
					}
					return formatSingleLineComment(token.getText());
				}
				case ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT: {
					if (skipFormatting) {
						return token.getText();
					}
					return formatMultiLineComment(token.getText());
				}
				case ASTokenTypes.TOKEN_LITERAL_STRING: {
					return formatLiteralString(token.getText());
				}
				case ASTokenTypes.TOKEN_SEMICOLON: {
					if (skipFormatting) {
						return token.isImplicit() ? "" : token.getText();
					}
					boolean skipSemicolon = Semicolons.REMOVE.equals(semicolons)
							|| (Semicolons.IGNORE.equals(semicolons) && token.isImplicit());
					if (!skipSemicolon) {
						return token.getText();
					}
					return null;
				}
				default: {
					return token.getText();
				}
			}
		}
	}

	private String formatMetadataToken(MetaDataPayloadToken token) {
		// metadata doesn't include all of its text as tokens, so we need to
		// insert some extra characters here and there
		StringBuilder builder = new StringBuilder();
		boolean needsComma = false;
		List<MetadataToken> payload = token.getPayload();
		for (int i = 0; i < payload.size(); i++) {
			MetadataToken metaToken = payload.get(i);
			switch (metaToken.getType()) {
				case MetadataTokenTypes.TOKEN_ATTR_NAME:
				case MetadataTokenTypes.TOKEN_ATTR_EVENT:
				case MetadataTokenTypes.TOKEN_ATTR_FORMAT:
				case MetadataTokenTypes.TOKEN_ATTR_TYPE:
				case MetadataTokenTypes.TOKEN_ATTR_ARRAY_TYPE:
				case MetadataTokenTypes.TOKEN_ATTR_INHERITS:
				case MetadataTokenTypes.TOKEN_ATTR_ENUM:
				case MetadataTokenTypes.TOKEN_ATTR_UNKNOWN:
				case MetadataTokenTypes.TOKEN_ATTR_ENV:
				case MetadataTokenTypes.TOKEN_ATTR_VERBOSE:
				case MetadataTokenTypes.TOKEN_ATTR_CATEGORY:
				case MetadataTokenTypes.TOKEN_ATTR_VARIABLE:
				case MetadataTokenTypes.TOKEN_ATTR_DEFAULT_VALUE:
				case MetadataTokenTypes.TOKEN_ATTR_STATES:
				case MetadataTokenTypes.TOKEN_ATTR_IMPLEMENTATION:
				case MetadataTokenTypes.TOKEN_ATTR_OPERATOR_NS_QUALIFIER: {
					if (needsComma) {
						builder.append(",");
						if (insertSpaceBetweenMetadataAttributes) {
							builder.append(" ");
						}
					}
					needsComma = true;
					builder.append(metaToken.getText());
					MetadataToken nextToken = payload.get(i + 1);
					if (nextToken.getType() == 8) {
						builder.append("=");
						builder.append("\"");
						builder.append(nextToken.getText());
						builder.append("\"");
						i++;
					}
					break;
				}
				case MetadataTokenTypes.TOKEN_STRING: {
					if (needsComma) {
						builder.append(",");
						if (insertSpaceBetweenMetadataAttributes) {
							builder.append(" ");
						}
					}
					builder.append("\"");
					builder.append(metaToken.getText());
					builder.append("\"");
					needsComma = true;
					break;
				}
				default: {
					builder.append(metaToken.getText());
				}
			}
		}
		return builder.toString();
	}

	private boolean checkTokenBeforeAnyType(IASToken token) {
		return token.getType() == ASTokenTypes.TOKEN_COLON;
	}

	private boolean checkTokensForAnyVectorType(IASToken prevToken, IASToken nextToken) {
		return prevToken != null && nextToken != null
				&& ((prevToken.getType() == ASTokenTypes.TOKEN_TYPED_COLLECTION_OPEN
						&& nextToken.getType() == ASTokenTypes.TOKEN_TYPED_COLLECTION_CLOSE)
						|| (prevToken.getType() == ASTokenTypes.TOKEN_TYPED_LITERAL_OPEN
								&& nextToken.getType() == ASTokenTypes.TOKEN_TYPED_LITERAL_CLOSE));
	}

	private boolean checkTokenBeforeUnaryOperator(IASToken token) {
		return (token instanceof ASToken) ? ((ASToken) token).isOperator()
				|| token.getType() == ASTokenTypes.TOKEN_SQUARE_OPEN || token.getType() == ASTokenTypes.TOKEN_PAREN_OPEN
				|| token.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN || token.getType() == ASTokenTypes.TOKEN_SEMICOLON
				|| token.getType() == ASTokenTypes.TOKEN_KEYWORD_RETURN || token.getType() == ASTokenTypes.TOKEN_COMMA
				|| token.getType() == ASTokenTypes.TOKEN_COLON : (token == null);
	}

	private int increaseIndent(int indent) {
		return indent + 1;
	}

	private int decreaseIndent(int indent) {
		return Math.max(0, indent - 1);
	}

	private String getIndent() {
		if (insertSpaces) {
			String result = "";
			for (int j = 0; j < tabSize; j++) {
				result += " ";
			}
			return result;
		}
		return "\t";
	}

	private String getAttributeIndent(IMXMLToken openTagToken) {
		if (!mxmlAlignAttributes) {
			return getIndent();
		}
		int indentSize = openTagToken.getText().length() + 1;
		String result = "";
		while (indentSize >= tabSize) {
			result += getIndent();
			indentSize -= tabSize;
		}
		for (int i = 0; i < indentSize; i++) {
			result += " ";
		}
		return result;
	}

	private void appendIndent(StringBuilder builder, int indent) {
		String indentString = getIndent();
		for (int i = 0; i < indent; i++) {
			builder.append(indentString);
		}
	}

	private void appendNewLines(StringBuilder builder, int numRequiredNewLines) {
		if (maxPreserveNewLines != 0) {
			numRequiredNewLines = Math.min(maxPreserveNewLines, numRequiredNewLines);
		}
		for (int j = 0; j < numRequiredNewLines; j++) {
			builder.append('\n');
		}
	}

	private boolean hasErrors(Collection<ICompilerProblem> problems) {
		CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(null);
		for (ICompilerProblem problem : problems) {
			CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem);
			if (CompilerProblemSeverity.ERROR.equals(severity)) {
				return true;
			}
		}
		return false;
	}

	private static class BlockStackItem {
		public BlockStackItem(IASToken token) {
			this.token = token;
		}

		public IASToken token;
		public int blockDepth = 0;
		public boolean braces = true;
	}

	private static class ObjectLiteralBlockStackItem extends BlockStackItem {
		public ObjectLiteralBlockStackItem(IASToken token) {
			super(token);
		}
	}

	private static class SwitchBlockStackItem extends BlockStackItem {
		public SwitchBlockStackItem(IASToken token) {
			super(token);
		}

		public int clauseCount = 0;
	}

	private String formatMXMLTextInternal(String filePath, String text, Collection<ICompilerProblem> problems) {
		if (problems == null) {
			problems = new ArrayList<ICompilerProblem>();
		}

		StringReader textReader = new StringReader(text);
		MXMLTokenizer mxmlTokenizer = new MXMLTokenizer();
		IMXMLToken[] originalTokens = null;
		try {
			originalTokens = mxmlTokenizer.getTokens(textReader);
		} finally {
			IOUtils.closeQuietly(textReader);
			IOUtils.closeQuietly(mxmlTokenizer);
		}

		if (mxmlTokenizer.hasTokenizationProblems()) {
			problems.addAll(mxmlTokenizer.getTokenizationProblems());
		}

		if (!ignoreProblems && hasErrors(problems)) {
			return text;
		}

		Pattern scriptStartPattern = Pattern.compile("<((?:mx|fx):(Script|Metadata))");

		List<IMXMLToken> tokens = insertExtraMXMLTokens(originalTokens, text);

		int indent = 0;
		int numRequiredNewLines = 0;
		boolean requiredSpace = false;
		boolean inOpenTag = false;
		boolean inCloseTag = false;
		boolean skipFormatting = false;
		String attributeIndent = "";
		IMXMLToken prevToken = null;
		IMXMLToken prevTokenOrExtra = null;
		IMXMLToken token = null;
		IMXMLToken nextToken = null;
		List<ElementStackItem> elementStack = new ArrayList<ElementStackItem>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			token = tokens.get(i);
			nextToken = null;
			if (i < (tokens.size() - 1)) {
				nextToken = tokens.get(i + 1);
			}
			if (token.getType() == TOKEN_TYPE_EXTRA) {
				if (skipFormatting) {
					builder.append(token.getText());
				} else {
					if (i == (tokens.size() - 1)) {
						// if the last token is whitespace, include at most one
						// new line, but strip the rest
						numRequiredNewLines = Math.min(1, Math.max(0, countNewLinesInExtra(token)));
						appendNewLines(builder, numRequiredNewLines);
						break;
					}
					numRequiredNewLines = Math.max(numRequiredNewLines, countNewLinesInExtra(token));
				}
				prevTokenOrExtra = token;
				continue;
			} else if (token.getType() == MXMLTokenTypes.TOKEN_WHITESPACE) {
				if (skipFormatting) {
					builder.append(token.getText());
				} else {
					if (elementStack.isEmpty() || !elementStack.get(elementStack.size() - 1).containsText) {
						numRequiredNewLines = Math.max(numRequiredNewLines, countNewLinesInExtra(token));
					} else {
						// if the parent element contains text, treat whitespace
						// the same as text, and don't reformat it
						// text is never reformatted because some components use it
						// without collapsing whitespace, and developers would be
						// confused if whitespace that they deliberately added were
						// to be removed
						builder.append(token.getText());
					}
					if (i == (tokens.size() - 1)) {
						// if the last token is whitespace, include at most one
						// new line, but strip the rest
						numRequiredNewLines = Math.min(1, numRequiredNewLines);
						appendNewLines(builder, numRequiredNewLines);
					}
				}
				continue;
			} else if (token.getType() == MXMLTokenTypes.TOKEN_OPEN_TAG_START
					&& scriptStartPattern.matcher(token.getText()).matches()) {

				if (prevToken != null && numRequiredNewLines > 0) {
					appendNewLines(builder, numRequiredNewLines);
				}
				StringBuilder scriptBuilder = new StringBuilder();
				scriptBuilder.append(token.getText());
				boolean inScriptCloseTag = false;
				while (i < (tokens.size() - 1)) {
					i++;
					token = tokens.get(i);
					scriptBuilder.append(token.getText());
					if (token.getType() == MXMLTokenTypes.TOKEN_CLOSE_TAG_START) {
						inScriptCloseTag = true;
					} else if (inScriptCloseTag && token.getType() == MXMLTokenTypes.TOKEN_TAG_END) {
						break;
					}
				}
				if (problems == null) {
					// we need to know if there were problems because it means that we
					// need to return the original, unformatted text
					problems = new ArrayList<ICompilerProblem>();
				}
				builder.append(formatMXMLScriptElement(filePath, token.getLine(), scriptBuilder.toString(), problems));
				if (hasErrors(problems)) {
					return text;
				}
				prevToken = token;
				prevTokenOrExtra = token;
				requiredSpace = false;
				numRequiredNewLines = 1;
				continue;
			}

			// characters that must appear before the token
			switch (token.getType()) {
				case MXMLTokenTypes.TOKEN_OPEN_TAG_START: {
					inOpenTag = true;
					// if the parent contains text, children should be the same
					boolean containsText = !elementStack.isEmpty()
							&& elementStack.get(elementStack.size() - 1).containsText;
					elementStack.add(new ElementStackItem(token, token.getText().substring(1), containsText));
					break;
				}
				case MXMLTokenTypes.TOKEN_CLOSE_TAG_START: {
					if (elementStack.isEmpty() || !elementStack.get(elementStack.size() - 1).containsText) {
						indent = decreaseIndent(indent);
					}
					inCloseTag = true;
					break;
				}
				case MXMLTokenTypes.TOKEN_NAME: {
					requiredSpace = true;
					break;
				}
			}

			if (!skipFormatting && prevToken != null) {
				if (numRequiredNewLines > 0) {
					appendNewLines(builder, numRequiredNewLines);
					appendIndent(builder, indent);
					if (attributeIndent.length() > 0) {
						builder.append(attributeIndent);
					}
				} else if (requiredSpace) {
					builder.append(' ');
				}
			}

			// include the token's own text
			// no token gets reformatted before being appended
			// whitespace is the only special case, but that's not handled here
			builder.append(token.getText());

			// characters that must appear after the token
			requiredSpace = false;
			numRequiredNewLines = 0;

			switch (token.getType()) {
				case MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION: {
					numRequiredNewLines = Math.max(numRequiredNewLines, 1);
					break;
				}
				case MXMLTokenTypes.TOKEN_CLOSE_TAG_START: {
					if (nextToken != null && nextToken.getType() != MXMLTokenTypes.TOKEN_TAG_END
							&& nextToken.getType() != MXMLTokenTypes.TOKEN_EMPTY_TAG_END
							&& nextToken.getType() != TOKEN_TYPE_EXTRA) {
						requiredSpace = true;
					}
					if (elementStack.isEmpty()) {
						// something is very wrong!
						return text;
					}
					String elementName = token.getText().substring(2);
					ElementStackItem elementItem = elementStack.remove(elementStack.size() - 1);
					if (!elementName.equals(elementItem.elementName)) {
						// there's a unclosed tag with a different name somewhere
						return text;
					}
					break;
				}
				case MXMLTokenTypes.TOKEN_OPEN_TAG_START: {
					if (nextToken != null && nextToken.getType() != MXMLTokenTypes.TOKEN_TAG_END
							&& nextToken.getType() != MXMLTokenTypes.TOKEN_EMPTY_TAG_END) {
						attributeIndent = getAttributeIndent(token);
						if (nextToken.getType() != TOKEN_TYPE_EXTRA) {
							requiredSpace = true;
						}
					}
					break;
				}
				case MXMLTokenTypes.TOKEN_TAG_END: {
					if (inOpenTag) {
						ElementStackItem element = elementStack.get(elementStack.size() - 1);
						if (!element.containsText) {
							element.containsText = elementContainsText(tokens, i + 1, element.token);
						}
						if (elementStack.isEmpty() || !elementStack.get(elementStack.size() - 1).containsText) {
							indent = increaseIndent(indent);
						}
					} else {
						if (elementStack.isEmpty() || !elementStack.get(elementStack.size() - 1).containsText) {
							numRequiredNewLines = Math.max(numRequiredNewLines, 1);
						}
					}
					inOpenTag = false;
					attributeIndent = "";
					inCloseTag = false;
					break;
				}
				case MXMLTokenTypes.TOKEN_EMPTY_TAG_END: {
					if (inOpenTag) {
						elementStack.remove(elementStack.size() - 1);
					} else {
						if (elementStack.isEmpty() || !elementStack.get(elementStack.size() - 1).containsText) {
							numRequiredNewLines = Math.max(numRequiredNewLines, 1);
						}
					}
					inOpenTag = false;
					// no need to change nested indent after this tag
					// however, we may need to remove attribute indent
					attributeIndent = "";
					// we shouldn't find an empty close tag, but clear flag anyway
					inCloseTag = false;
					break;
				}
				case MXMLTokenTypes.TOKEN_STRING: {
					if (inOpenTag && mxmlInsertNewLineBetweenAttributes && nextToken != null
							&& nextToken.getType() != MXMLTokenTypes.TOKEN_TAG_END
							&& nextToken.getType() != MXMLTokenTypes.TOKEN_EMPTY_TAG_END) {
						numRequiredNewLines = Math.max(numRequiredNewLines, 1);
					}
					break;
				}
				case MXMLTokenTypes.TOKEN_COMMENT: {
					String tokenText = token.getText();
					String trimmed = tokenText.substring(4, tokenText.length() - 3).trim();
					if (!skipFormatting && FORMATTER_TAG_OFF.equals(trimmed)) {
						skipFormatting = true;
					} else if (skipFormatting && FORMATTER_TAG_ON.equals(trimmed)) {
						skipFormatting = false;
					}
					break;
				}
			}

			prevToken = token;
			prevTokenOrExtra = token;
		}

		return builder.toString();
	}

	private boolean elementContainsText(List<IMXMLToken> tokens, int startIndex, IMXMLToken openTagToken) {
		ArrayList<IMXMLToken> elementStack = new ArrayList<IMXMLToken>();
		elementStack.add(openTagToken);
		for (int i = startIndex; i < tokens.size(); i++) {
			IMXMLToken token = tokens.get(i);
			switch (token.getType()) {
				case MXMLTokenTypes.TOKEN_TEXT: {
					if (elementStack.size() == 1) {
						return true;
					}
					break;
				}
				case MXMLTokenTypes.TOKEN_OPEN_TAG_START: {
					elementStack.add(token);
					break;
				}
				case MXMLTokenTypes.TOKEN_EMPTY_TAG_END: {
					elementStack.remove(elementStack.size() - 1);
					if (elementStack.size() == 0) {
						return false;
					}
					break;
				}
				case MXMLTokenTypes.TOKEN_CLOSE_TAG_START: {
					elementStack.remove(elementStack.size() - 1);
					if (elementStack.size() == 0) {
						return false;
					}
					break;
				}
			}
		}
		return false;
	}

	private int countNewLinesInExtra(IMXMLToken token) {
		if (token == null
				|| (token.getType() != MXMLTokenTypes.TOKEN_WHITESPACE && token.getType() != TOKEN_TYPE_EXTRA)) {
			return 0;
		}
		int numNewLinesInWhitespace = 0;
		String whitespace = token.getText();
		int index = -1;
		while ((index = whitespace.indexOf('\n', index + 1)) != -1) {
			numNewLinesInWhitespace++;
		}
		return numNewLinesInWhitespace;
	}

	private static class ElementStackItem {
		public ElementStackItem(IMXMLToken token, String elementName, boolean containsText) {
			this.token = token;
			this.elementName = elementName;
			this.containsText = containsText;
		}

		public IMXMLToken token;
		public String elementName;
		public boolean containsText = false;
	}
}