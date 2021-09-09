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
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.parsing.as.MetaDataPayloadToken;
import org.apache.royale.compiler.internal.parsing.as.MetadataToken;
import org.apache.royale.compiler.internal.parsing.as.MetadataTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.RepairingTokenBuffer;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Formats .as source files.
 */
class FORMATTER {
	private static final int TOKEN_TYPE_EXTRA = 999999;

	static enum ExitCode {
		SUCCESS(0), PRINT_HELP(1), FAILED_WITH_PROBLEMS(2), FAILED_WITH_EXCEPTIONS(3), FAILED_WITH_CONFIG_PROBLEMS(4);

		ExitCode(int code) {
			this.code = code;
		}

		final int code;
	}

	public static enum Semicolons {
		IGNORE("ignore"), INSERT("insert"), REMOVE("remove");

		Semicolons(String value) {
			this.value = value;
		}

		final String value;
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
	public int maxPreserveNewLines = 2;
	public Semicolons semicolons = Semicolons.INSERT;
	public boolean ignoreProblems = false;
	public boolean collapseEmptyBlocks = false;

	private List<File> inputFiles = new ArrayList<File>();
	private boolean writeBackToInputFiles = false;
	private boolean listChangedFiles = false;

	public int execute(String[] args) {
		ExitCode exitCode = ExitCode.SUCCESS;

		try {
			boolean continueFormatting = configure(args);
			if (continueFormatting) {
				if (inputFiles.size() == 0) {
					StringBuilder builder = new StringBuilder();
					Scanner sysInScanner = new Scanner(System.in);
					try {
						while (sysInScanner.hasNext()) {
							builder.append(sysInScanner.next());
						}
					} finally {
						sysInScanner.close();
					}
					String filePath = FilenameNormalization.normalize("stdin.as");
					String fileText = builder.toString();
					String formattedText = formatFileText(filePath, fileText);
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
						String fileText = FileUtils.readFileToString(inputFile, "utf8");
						String formattedText = formatFileText(filePath, fileText);
						if (!fileText.equals(formattedText)) {
							if (listChangedFiles) {
								System.out.println(filePath);
							}
							if (writeBackToInputFiles) {
								FileUtils.write(inputFile, formattedText, "utf8");
							}
						}
						if (!listChangedFiles || !writeBackToInputFiles) {
							System.out.println(formattedText);
						}
					}
				}
			}
		} catch (ConfigurationException e) {
			System.err.println(e.getMessage());
			exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
		}
		return exitCode.code;
	}

	public String formatFile(File file, Collection<ICompilerProblem> problems) throws IOException {
		String fileText = FileUtils.readFileToString(file, "utf8");
		String filePath = FilenameNormalization.normalize(file.getAbsolutePath());
		return formatFileText(filePath, fileText, problems);
	}

	public String formatFile(File file) throws IOException {
		return formatFile(file, null);
	}

	public String formatFileText(String filePath, String text, Collection<ICompilerProblem> problems) {
		String result = formatTextInternal(filePath, text, problems);
		if (insertFinalNewLine && result.charAt(result.length() - 1) != '\n') {
			return result + '\n';
		}
		return result;
	}

	public String formatFileText(String filePath, String text) {
		return formatFileText(filePath, text, null);
	}

	public String formatText(String text, Collection<ICompilerProblem> problems) {
		String filePath = FilenameNormalization.normalize("source.as");
		return formatTextInternal(filePath, text, problems);
	}

	public String formatText(String text) {
		return formatText(text, null);
	}

	private boolean configure(String[] args) throws ConfigurationException {
		if (args.length == 0) {
			printHelp();
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.charAt(0) == '-') {
				if (arg.equals("-write") || arg.equals("-w")) {
					writeBackToInputFiles = true;
				} else if (arg.equals("-list") || arg.equals("-l")) {
					listChangedFiles = true;
				} else if (arg.equals("-help") || arg.equals("-h")) {
					printHelp();
					return false;
				} else {
					throw new ConfigurationException("Unknown command-line argument: " + arg, null, -1);
				}
			} else {
				File inputFile = new File(arg);
				if (!inputFile.exists()) {
					throw new ConfigurationException("Input file does not exist: " + arg, null, -1);
				}
				if (inputFile.isDirectory()) {
					addDirectory(inputFile);
				} else {
					inputFiles.add(inputFile);
				}
			}
		}
		if (inputFiles.size() == 0 && writeBackToInputFiles) {
			throw new ConfigurationException("Cannot use -w with standard input", null, -1);
		}
		if (writeBackToInputFiles) {
			if (inputFiles.size() == 0) {
				throw new ConfigurationException("Cannot use -w with standard input", null, -1);
			}
			for (File inputFile : inputFiles) {
				if (!inputFile.canWrite()) {
					throw new ConfigurationException("File is read-only: " + inputFile.getPath(), null, -1);
				}
			}
		}
		return true;
	}

	private void addDirectory(File inputFile) {
		for (File file : inputFile.listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith(".")) {
				continue;
			}
			if (file.isDirectory()) {
				addDirectory(file);
			} else if (fileName.endsWith(".as")) {
				inputFiles.add(file);
			}
		}
	}

	private void printHelp() {
		System.err.println("Apache Royale ActionScript Formatter (asformat)");
		System.err.println(VersionInfo.buildMessage());
		System.err.println("");
		System.err.println("-h, -help    Prints program usage information.");
		System.err.println(
				"-l, -list    Lists file names of files that are formatted, and does not print reformatted sources to standard output.");
		System.err.println(
				"-w, -write   Overwrites the input files that are foramtted, and does not print the reformatted sources to standard output.");
	}

	private String formatTextInternal(String filePath, String text, Collection<ICompilerProblem> problems) {
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
			try {
				textReader.close();
				tokenizer.close();
			} catch (IOException e) {
			}
		}

		if (!ignoreProblems && tokenizer.hasTokenizationProblems()) {
			if (problems != null) {
				problems.addAll(tokenizer.getTokenizationProblems());
			}
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
			if (problems != null) {
				problems.add(new UnexpectedExceptionProblem(e));
			}
			return text;
		}

		if (!ignoreProblems && tokenizer.hasTokenizationProblems()) {
			if (problems != null) {
				problems.addAll(tokenizer.getTokenizationProblems());
			}
			return text;
		}

		if (!ignoreProblems && parser.getSyntaxProblems().size() > 0) {
			if (problems != null) {
				problems.addAll(parser.getSyntaxProblems());
			}
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
		IASToken[] repairedTokens = repairedTokensList.toArray(new IASToken[0]);

		IASToken prevToken = null;
		ArrayList<IASToken> tokens = new ArrayList<IASToken>();
		for (IASToken token : repairedTokens) {
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
				if (!blockOpenPending) {
					numRequiredNewLines = Math.max(numRequiredNewLines, countNewLinesInExtra(token));
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
									|| prevTokenNotComment.getType() == ASTokenTypes.TOKEN_BLOCK_CLOSE
									|| prevTokenNotComment.getType() == ASTokenTypes.TOKEN_COLON;
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
								if (switchStackItem.clauseCount > 0 && (prevToken == null
										|| prevToken.getType() != ASTokenTypes.TOKEN_BLOCK_CLOSE)) {
									indent = decreaseIndent(indent);
								}
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
					case ASTokenTypes.TOKEN_OPERATOR_PLUS:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS:
					case ASTokenTypes.TOKEN_OPERATOR_STAR:
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
						requiredSpace = true;
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
			if (prevToken != null) {
				if (numRequiredNewLines > 0) {
					if (maxPreserveNewLines != 0) {
						numRequiredNewLines = Math.min(maxPreserveNewLines, numRequiredNewLines);
					}
					for (int j = 0; j < numRequiredNewLines; j++) {
						builder.append('\n');
					}
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
			builder.append(getTokenText(token, indent));

			// characters that must appear after the token
			if (token.getType() != ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
					&& token.getType() != ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_ASDOC_COMMENT
					&& token.getType() != ASTokenTypes.TOKEN_BLOCK_OPEN) {
				blockOpenPending = false;
			}
			caseOrDefaultBlockOpenPending = false;
			requiredSpace = false;
			numRequiredNewLines = 0;
			if (token instanceof MetaDataPayloadToken) {
				numRequiredNewLines = Math.max(numRequiredNewLines, 1);
			} else {
				switch (token.getType()) {
					case ASTokenTypes.TOKEN_SEMICOLON: {
						if (inControlFlowStatement && !blockStack.isEmpty()
								&& blockStack.get(blockStack.size() - 1).token
										.getType() == ASTokenTypes.TOKEN_KEYWORD_FOR) {
							if (insertSpaceAfterSemicolonInForStatements) {
								requiredSpace = true;
							}
							// else no space
						} else {
							if (!blockStack.isEmpty()) {
								BlockStackItem prevStackItem = blockStack.get(blockStack.size() - 1);
								if (prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_CASE
										&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_DEFAULT
										&& prevStackItem.blockDepth <= 0) {
									blockStack.remove(blockStack.size() - 1);
									if (prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_CLASS
										&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_INTERFACE
										&& prevStackItem.token.getType() != ASTokenTypes.TOKEN_KEYWORD_FUNCTION)
									{
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
						}
						if (nextToken == null || nextToken.getType() != ASTokenTypes.TOKEN_SEMICOLON) {
							numRequiredNewLines = Math.max(numRequiredNewLines, 1);
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
								indent = increaseIndent(indent);
								numRequiredNewLines = Math.max(numRequiredNewLines, 1);
							} else if (ternaryStack > 0) {
								ternaryStack--;
								requiredSpace = true;
							} else {
								requiredSpace = true;
							}
						}
						break;
					}
					case ASTokenTypes.TOKEN_PAREN_OPEN: {
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
									numRequiredNewLines = Math.max(numRequiredNewLines, 1);
								}
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
					case ASTokenTypes.TOKEN_OPERATOR_PLUS:
					case ASTokenTypes.TOKEN_OPERATOR_MINUS:
					case ASTokenTypes.TOKEN_OPERATOR_STAR:
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
					case ASTokenTypes.TOKEN_COMMA: {
						if (insertSpaceAfterCommaDelimiter && !skipWhitespaceBeforeSemicolon) {
							requiredSpace = true;
						}
						break;
					}
					case ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT: {
						numRequiredNewLines = Math.max(numRequiredNewLines, 1);
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
		return "// " + comment;
	}

	private String formatMultiLineComment(String comment) {
		return comment;
	}

	private String formatLiteralString(String string) {
		String charsToEscape = "\b\t\n\f\r\"\'\\";
		String escapeChars = "btnfr\"\'\\";
		int escapeIndex = -1;
		char currChar;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < string.length(); ++i) {
			currChar = string.charAt(i);
			if (i == 0 || i == string.length() - 1) {
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

	private String formatASDocComment(String comment, int indent) {
		String[] lines = comment.split("\n");
		StringBuilder builder = new StringBuilder();
		builder.append(lines[0].trim());
		if (lines.length > 1) {
			builder.append('\n');
		}
		for (int i = 1; i < lines.length - 1; i++) {
			appendIndent(builder, indent);
			builder.append(' ');
			builder.append(lines[i].trim());
			builder.append('\n');
		}
		if (lines.length > 1) {
			appendIndent(builder, indent);
			builder.append(' ');
			builder.append(lines[lines.length - 1].trim());
		}
		return builder.toString();
	}

	private String getTokenText(IASToken token, int indent) {

		if (token instanceof MetaDataPayloadToken) {
			MetaDataPayloadToken metaPlayloadToken = (MetaDataPayloadToken) token;
			return formatMetadataToken(metaPlayloadToken);
		} else {
			switch (token.getType()) {
				case ASTokenTypes.TOKEN_ASDOC_COMMENT: {
					return formatASDocComment(token.getText(), indent);
				}
				case ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT: {
					return formatSingleLineComment(token.getText());
				}
				case ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT: {
					return formatMultiLineComment(token.getText());
				}
				case ASTokenTypes.TOKEN_LITERAL_STRING: {
					return formatLiteralString(token.getText());
				}
				case ASTokenTypes.TOKEN_SEMICOLON: {
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
				case MetadataTokenTypes.TOKEN_ATTR_OPERATOR_NS_QUALIFIER:
					if (needsComma) {
						builder.append(", ");
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
				case MetadataTokenTypes.TOKEN_STRING:
					if (needsComma) {
						builder.append(", ");
					}
					builder.append("\"");
					builder.append(metaToken.getText());
					builder.append("\"");
					needsComma = true;
					break;
				default:
					builder.append(metaToken.getText());
			}
		}
		return builder.toString();
	}

	private int increaseIndent(int indent) {
		return indent + 1;
	}

	private int decreaseIndent(int indent) {
		return Math.max(0, indent - 1);
	}

	private void appendIndent(StringBuilder builder, int indent) {
		for (int i = 0; i < indent; i++) {
			if (insertSpaces) {
				for (int j = 0; j < tabSize; j++) {
					builder.append(" ");
				}
			} else {
				builder.append("\t");
			}
		}
	}

	private static class BlockStackItem {
		public BlockStackItem(IASToken token) {
			this.token = token;
		}

		public IASToken token;
		public int blockDepth = 0;
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
}