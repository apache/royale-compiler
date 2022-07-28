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

package org.apache.royale.formatter.internal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.formatter.FORMATTER;

public class MXMLTokenFormatter extends BaseTokenFormatter {
	private static final int TOKEN_TYPE_EXTRA = 999999;
	private static final Pattern SCRIPT_START_PATTERN = Pattern.compile("<((?:mx|fx):(Script|Metadata))");
	private static final String FORMATTER_TAG_OFF = "@formatter:off";
	private static final String FORMATTER_TAG_ON = "@formatter:on";

	public MXMLTokenFormatter(FORMATTER formatter) {
		super(formatter);
	}

	private int indent;
	private int numRequiredNewLines;
	private boolean requiredSpace;
	private boolean inOpenTag;
	private boolean inCloseTag;
	private boolean skipFormatting;
	private String attributeIndent;
	private IMXMLToken prevToken;
	private IMXMLToken prevTokenOrExtra;
	private IMXMLToken token;
	private IMXMLToken nextToken;
	private List<ElementStackItem> elementStack;

	public String format(String filePath, String text, Collection<ICompilerProblem> problems) {
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

		if (!formatter.ignoreProblems && hasErrors(problems)) {
			return text;
		}

		List<IMXMLToken> tokens = insertExtraMXMLTokens(originalTokens, text);
		try {
			return parseTokens(filePath, text, tokens, problems);
		} catch (Exception e) {
			if (problems != null) {
				System.err.println(e);
				e.printStackTrace(System.err);
				problems.add(new UnexpectedExceptionProblem(e));
			}
			return text;
		}

	}

	private String parseTokens(String filePath, String text, List<IMXMLToken> tokens, Collection<ICompilerProblem> problems) throws Exception {
		indent = 0;
		numRequiredNewLines = 0;
		requiredSpace = false;
		inOpenTag = false;
		inCloseTag = false;
		skipFormatting = false;
		attributeIndent = "";
		prevToken = null;
		prevTokenOrExtra = null;
		token = null;
		nextToken = null;
		elementStack = new ArrayList<ElementStackItem>();

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
					&& SCRIPT_START_PATTERN.matcher(token.getText()).matches()) {

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
					if (inOpenTag && formatter.mxmlInsertNewLineBetweenAttributes && nextToken != null
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

	private String formatMXMLScriptElement(String filePath, int line, String text,
			Collection<ICompilerProblem> problems) {
		String indent = "\t";
		if (formatter.insertSpaces) {
			indent = "";
			for (int i = 0; i < formatter.tabSize; i++) {
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
		ASTokenFormatter asFormatter = new ASTokenFormatter(formatter);
		String formattedScriptText = asFormatter.format(filePath + "@Script[" + line + "]", scriptText, problems);
		if (!formatter.ignoreProblems && hasErrors(problems)) {
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

	private String getAttributeIndent(IMXMLToken openTagToken) {
		if (!formatter.mxmlAlignAttributes) {
			return getIndent();
		}
		int indentSize = openTagToken.getText().length() + 1;
		String result = "";
		while (indentSize >= formatter.tabSize) {
			result += getIndent();
			indentSize -= formatter.tabSize;
		}
		for (int i = 0; i < indentSize; i++) {
			result += " ";
		}
		return result;
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
