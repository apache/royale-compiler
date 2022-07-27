
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.parsing.as.RepairingTokenBuffer;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.linter.internal.BaseLinter;

public class ASLinter extends BaseLinter {
	private static final String LINTER_TAG_OFF = "@linter:off";
	private static final String LINTER_TAG_ON = "@linter:on";

	public ASLinter(LinterSettings settings) {
		super(settings);
	}

	public void lint(String filePath, String text, Collection<ICompilerProblem> allProblems) {
		if (allProblems == null) {
			allProblems = new ArrayList<ICompilerProblem>();
		}

		List<ICompilerProblem> fileProblems = new ArrayList<ICompilerProblem>();
		try {
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
				fileProblems.addAll(tokenizer.getTokenizationProblems());
			}

			if (!settings.ignoreProblems && hasErrors(fileProblems)) {
				return;
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
				parser.parseFile(node, EnumSet.of(PostProcessStep.CALCULATE_OFFSETS));
			} catch (Exception e) {
				parser = null;
				fileProblems.add(new UnexpectedExceptionProblem(e));
				return;
			}

			if (tokenizer.hasTokenizationProblems()) {
				fileProblems.addAll(tokenizer.getTokenizationProblems());
			}

			if (parser.getSyntaxProblems().size() > 0) {
				fileProblems.addAll(parser.getSyntaxProblems());
			}

			if (!settings.ignoreProblems && hasErrors(fileProblems)) {
				return;
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

			IASToken[] allTokens = repairedTokensList.toArray(new IASToken[0]);
			TokenQuery tokenQuery = new TokenQuery(allTokens);
			visitNode(node, tokenQuery, fileProblems);
			boolean skipLinting = false;
			for (LinterRule rule : settings.rules) {
				Map<Integer, TokenVisitor> tokenHandlers = rule.getTokenVisitors();
				if (tokenHandlers != null) {
					for (IASToken token : allTokens) {
						int tokenType = token.getType();
						if (tokenType == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
								|| tokenType == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT) {
							boolean isMultiline = tokenType == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT;
							String tokenText = token.getText();
							String trimmed = tokenText.substring(2, tokenText.length() - (isMultiline ? 2 : 0)).trim();
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
						if (tokenHandlers.containsKey(tokenType)) {
							tokenHandlers.get(tokenType).visit(token, tokenQuery, fileProblems);
						}
					}
				}
			}
		} finally {
			allProblems.addAll(fileProblems);
		}
	}

	private void visitNode(IASNode node, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		ASTNodeID nodeID = node.getNodeID();
		IASToken prevComment = tokenQuery.getPreviousComment(node);
		while (prevComment != null) {
			String commentText = null;
			if (prevComment.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT) {
				commentText = prevComment.getText().substring(2).trim();
			} else if (prevComment.getType() == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT) {
				commentText = prevComment.getText();
				commentText = commentText.substring(2, commentText.length() - 2).trim();
			} else {
				// not the type of comment that we care about
				prevComment = tokenQuery.getPreviousComment(prevComment);
				continue;
			}
			if (LINTER_TAG_ON.equals(commentText)) {
				// linter is on
				break;
			}
			if (LINTER_TAG_OFF.equals(commentText)) {
				// linter is off
				return;
			}
			prevComment = tokenQuery.getPreviousComment(prevComment);
		}
		for (LinterRule rule : settings.rules) {
			Map<ASTNodeID, NodeVisitor> nodeHandlers = rule.getNodeVisitors();
			if (nodeHandlers != null && nodeHandlers.containsKey(nodeID)) {
				nodeHandlers.get(nodeID).visit(node, tokenQuery, problems);
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			IASNode child = node.getChild(i);
			visitNode(child, tokenQuery, problems);
		}
	}
}
