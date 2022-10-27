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

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.internal.filespecs.StringFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLData;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.IMXMLToken.MXMLTokenKind;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.linter.internal.BaseLinter;

public class MXMLLinter extends BaseLinter {
	private static final String LINTER_TAG_OFF = "@linter:off";
	private static final String LINTER_TAG_ON = "@linter:on";

	public MXMLLinter(LinterSettings settings) {
		super(settings);
	}

	public void lint(String filePath, String text, Collection<ICompilerProblem> allProblems) {
		if (allProblems == null) {
			allProblems = new ArrayList<ICompilerProblem>();
		}
		List<ICompilerProblem> fileProblems = new ArrayList<ICompilerProblem>();
		try {
			StringReader textReader = new StringReader(text);
			MXMLTokenizer mxmlTokenizer = new MXMLTokenizer();
			List<MXMLToken> originalTokens = null;
			try {
				originalTokens = mxmlTokenizer.parseTokens(textReader);
			} finally {
				IOUtils.closeQuietly(textReader);
				IOUtils.closeQuietly(mxmlTokenizer);
			}

			if (mxmlTokenizer.hasTokenizationProblems()) {
				fileProblems.addAll(mxmlTokenizer.getTokenizationProblems());
			}

			if (!settings.ignoreProblems && hasErrors(fileProblems)) {
				return;
			}

			boolean skipLinting = false;
			IMXMLToken[] allTokens = insertFormattingTokens(originalTokens, text);
			MXMLTokenQuery tokenQuery = new MXMLTokenQuery(allTokens);
			for (LinterRule rule : settings.rules) {
				Map<MXMLTokenKind, MXMLTokenVisitor> tokenHandlers = rule.getMXMLTokenVisitors();
				if (tokenHandlers != null) {
					for (IMXMLToken token : originalTokens) {
						MXMLTokenKind tokenKind = token.getMXMLTokenKind();
						if (tokenKind == MXMLTokenKind.COMMENT) {
							String tokenText = token.getText();
							boolean isASDoc = tokenText.startsWith("<!---") && tokenText.length() > 7;
							String trimmed = tokenText.substring(isASDoc ? 5 : 4, tokenText.length() - 3).trim();
							if (!skipLinting && LINTER_TAG_OFF.equals(trimmed)) {
								skipLinting = true;
							} else if (skipLinting && LINTER_TAG_ON.equals(trimmed)) {
								skipLinting = false;
								continue;
							}
						}
						if (skipLinting) {
							continue;
						}
						if (tokenHandlers.containsKey(tokenKind)) {
							tokenHandlers.get(tokenKind).visit(token, tokenQuery, fileProblems);
						}
					}
				}
			}

			PrefixMap prefixMap = mxmlTokenizer.getPrefixMap();
			MXMLData mxmlData = new MXMLData(originalTokens, prefixMap, new StringFileSpecification(filePath, text));
			IMXMLTagData rootTag = mxmlData.getRootTag();
			visitTag(rootTag, tokenQuery, fileProblems);
			IMXMLUnitData current = rootTag;
			int offset = 1;
			String className = Paths.get(filePath).getFileName().toString();
			int extensionIndex = className.indexOf('.');
			if (extensionIndex != -1) {
				className = className.substring(0, extensionIndex);
			}
			String componentName = mxmlData.getRootTag().getShortName();
			StringBuilder builder = new StringBuilder();
			builder.append("/* ");
			builder.append(LINTER_TAG_OFF);
			builder.append(" */");
			builder.append("package{public class ");
			builder.append(className);
			builder.append(" extends ");
			builder.append(componentName);
			builder.append("{");
			while (current != null) {
				if (current instanceof IMXMLTagData) {
					IMXMLTagData tag = (IMXMLTagData) current;
					if ("Script".equals(tag.getShortName())) {
						String prefix = tag.getPrefix();
						String ns = prefixMap.getNamespaceForPrefix(prefix);
						if (ns != null
								&& (IMXMLLanguageConstants.NAMESPACE_MXML_2006.equals(ns)
										|| IMXMLLanguageConstants.NAMESPACE_MXML_2009.equals(ns)
										|| IMXMLLanguageConstants.NAMESPACE_MXML_2012.equals(ns))) {
							if (tag.isOpenTag()) {
								int line = tag.getLine();
								String scriptText = tag.getCompilableText();
								// wrap the script inside a class so that rules don't
								// get confused. adds new lines to ensure the line
								// numbers match the original file
								for (int i = offset; i < line; i++) {
									builder.append("\n");
								}
								builder.append("/* ");
								builder.append(LINTER_TAG_ON);
								builder.append(" */\n");
								builder.append(scriptText);
								builder.append("/* ");
								builder.append(LINTER_TAG_OFF);
								builder.append(" */");
							} else {
								offset = tag.getLine() + 1;
							}
						}
					}
				}
				current = current.getNext();
			}
			builder.append("}}\n");
			ASLinter asLinter = new ASLinter(settings);
			asLinter.lint(filePath, builder.toString(), fileProblems);
		} finally {
			allProblems.addAll(fileProblems);
		}
	}

	private void visitTag(IMXMLTagData tag, MXMLTokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		for (LinterRule rule : settings.rules) {
			List<MXMLTagVisitor> tagVisitors = rule.getMXMLTagVisitors();
			if (tagVisitors != null) {
				for (int i = 0; i < tagVisitors.size(); i++) {
					tagVisitors.get(i).visit(tag, tokenQuery, problems);
				}
			}
		}

		IMXMLTagData current = tag.getFirstChild(true);
		while (current != null) {
			visitTag(current, tokenQuery, problems);
			current = current.getNextSibling(true);
		}
	}

	private IMXMLToken[] insertFormattingTokens(List<MXMLToken> originalTokens, String text) {
		ArrayList<IMXMLToken> tokens = new ArrayList<IMXMLToken>();
		IMXMLToken prevToken = null;
		for (IMXMLToken token : originalTokens) {
			if (prevToken != null) {
				int start = prevToken.getEnd();
				int end = token.getStart();
				if (end > start) {
					String tokenText = text.substring(start, end);
					MXMLToken formattingToken = new MXMLToken(MXMLTokenQuery.TOKEN_TYPE_FORMATTING, start, end,
							prevToken.getLine(), prevToken.getColumn() + end - start, tokenText);
							formattingToken.setEndLine(token.getLine());
							formattingToken.setEndLine(token.getColumn());
					tokens.add(formattingToken);
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
				MXMLToken formattingToken = new MXMLToken(MXMLTokenQuery.TOKEN_TYPE_FORMATTING, start, end,
						prevToken.getLine(), prevToken.getColumn() + end - start, tokenText);
					formattingToken.setEndLine(prevToken.getLine());
					formattingToken.setEndLine(prevToken.getColumn());
				tokens.add(formattingToken);
			}
		}
		return tokens.toArray(new IMXMLToken[0]);
	}
}
