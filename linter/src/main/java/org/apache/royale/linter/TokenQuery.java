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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.as.IASNode;

public class TokenQuery {
	public static final int TOKEN_TYPE_WHITESPACE = 999999;

	public TokenQuery(IASToken[] tokens) {
		allTokens = tokens;
	}

	private IASToken[] allTokens;

	/**
	 * Returns all tokens in the file.
	 */
	public IASToken[] getTokens() {
		return allTokens;
	}

	/**
	 * Returns all tokens inside of a particular node.
	 */
	public IASToken[] getTokens(IASNode node) {
		return getTokens(node, false, false);
	}

	/**
	 * Returns all tokens inside of a particular node, with the option to skip
	 * comment and whitespace tokens.
	 */
	public IASToken[] getTokens(IASNode node, boolean skipComments, boolean skipWhitespace) {
		List<IASToken> result = new ArrayList<>();
		int start = findNearestToken(allTokens, node);
		int end = allTokens.length;
		for (int i = start; i < end; i++) {
			IASToken token = allTokens[i];
			if (token.getAbsoluteStart() >= node.getAbsoluteEnd()) {
				break;
			}
			if (token.getAbsoluteStart() < node.getAbsoluteStart()) {
				continue;
			}
			if (skipComments && isComment(token)) {
				continue;
			}
			if (skipWhitespace && isWhitespace(token)) {
				continue;
			}
			result.add(token);
		}
		return result.toArray(new IASToken[0]);
	}

	/**
	 * Returns the token immediately before a source location. Includes comment
	 * and whitespace tokens.
	 */
	public IASToken getTokenBefore(ISourceLocation sourceLocation) {
		return getTokenBefore(sourceLocation, false, false);
	}

	/**
	 * Returns the token immediately before a source location, with the option
	 * to skip comment and whitespace tokens.
	 */
	public IASToken getTokenBefore(ISourceLocation sourceLocation, boolean skipComments, boolean skipWhitespace) {
		int start = findNearestToken(allTokens, sourceLocation);
		for (int i = start - 1; i >= 0; i--) {
			IASToken token = allTokens[i];
			if (token.getAbsoluteStart() >= sourceLocation.getAbsoluteStart()) {
				continue;
			}
			if (skipComments && isComment(token)) {
				continue;
			}
			if (skipWhitespace && isWhitespace(token)) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the token immediately after a source location. Includes comment
	 * and whitespace tokens.
	 */
	public IASToken getTokenAfter(ISourceLocation sourceLocation) {
		return getTokenAfter(sourceLocation, false, false);
	}

	/**
	 * Returns the token immediately after a source location, with the option to
	 * skip comment and whitespace tokens.
	 */
	public IASToken getTokenAfter(ISourceLocation sourceLocation, boolean skipComments, boolean skipWhitespace) {
		int start = findNearestToken(allTokens, sourceLocation);
		int end = allTokens.length;
		for (int i = start; i < end; i++) {
			IASToken token = allTokens[i];
			if (token.getAbsoluteStart() < sourceLocation.getAbsoluteEnd()) {
				continue;
			}
			if (skipComments && isComment(token)) {
				continue;
			}
			if (skipWhitespace && isWhitespace(token)) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the first token inside a node. Includes comment
	 * and whitespace tokens.
	 */
	public IASToken getFirstToken(IASNode node) {
		return getFirstToken(node, false, false);
	}

	/**
	 * Returns the first token inside a node, with the option to
	 * skip comment and whitespace tokens.
	 */
	public IASToken getFirstToken(IASNode node, boolean skipComments, boolean skipWhitespace) {
		IASToken[] tokens = getTokens(node, skipComments, skipWhitespace);
		if (tokens.length == 0) {
			return null;
		}
		return tokens[0];
	}

	/**
	 * Returns the last token inside a node. Includes comment
	 * and whitespace tokens.
	 */
	public IASToken getLastToken(IASNode node) {
		return getLastToken(node, false, false);
	}

	/**
	 * Returns the last token inside a node, with the option to
	 * skip comment and whitespace tokens.
	 */
	public IASToken getLastToken(IASNode node, boolean skipComments, boolean skipWhitespace) {
		IASToken[] tokens = getTokens(node, skipComments, skipWhitespace);
		if (tokens.length == 0) {
			return null;
		}
		return tokens[tokens.length - 1];
	}

	/**
	 * Returns the first token of the specified type that appears before the
	 * start of a particular source location.
	 */
	public IASToken getPreviousTokenOfType(ISourceLocation before, int type) {
		int start = findNearestToken(allTokens, before);
		for (int i = start - 1; i >= 0; i--) {
			IASToken token = allTokens[i];
			if (token.getType() != type) {
				continue;
			}
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the first token of the specified type that appears after the end
	 * of a particular source location.
	 */
	public IASToken getNextTokenOfType(ISourceLocation after, int type) {
		int start = findNearestToken(allTokens, after);
		int end = allTokens.length;
		for (int i = start; i < end; i++) {
			IASToken token = allTokens[i];
			if (token.getType() != type) {
				continue;
			}
			if (token.getAbsoluteStart() < after.getAbsoluteEnd()) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns all comment tokens inside a node.
	 */
	public IASToken[] getCommentsInside(IASNode node) {
		List<IASToken> result = new ArrayList<>();
		IASToken[] tokensInside = getTokens(node);
		for (IASToken token : tokensInside) {
			if (isComment(token)) {
				result.add(token);
			}
		}
		return result.toArray(new IASToken[0]);
	}

	/**
	 * Checks if a token is a comment.
	 */
	public boolean isComment(IASToken token) {
		return token.getType() == ASTokenTypes.HIDDEN_TOKEN_COMMENT
				|| token.getType() == ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT
				|| token.getType() == ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT
				|| token.getType() == ASTokenTypes.TOKEN_ASDOC_COMMENT;
	}

	/**
	 * Returns the first comment that appears before the start of a particular
	 * source location.
	 */
	public IASToken getCommentBefore(ISourceLocation before) {
		int start = findNearestToken(allTokens, before);
		for (int i = start - 1; i >= 0; i--) {
			IASToken token = allTokens[i];
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				continue;
			}
			if (!isComment(token)) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the first comment that appears after the end of a particular
	 * source location.
	 */
	public IASToken getCommentAfter(ISourceLocation after) {
		int start = findNearestToken(allTokens, after);
		int end = allTokens.length;
		for (int i = start; i < end; i++) {
			IASToken token = allTokens[i];
			if (!isComment(token)) {
				continue;
			}
			if (token.getAbsoluteStart() < after.getAbsoluteEnd()) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Checks if a token is whitespace.
	 */
	public boolean isWhitespace(IASToken token) {
		return token.getType() == TOKEN_TYPE_WHITESPACE;
	}

	/**
	 * Returns the first whitespace that appears before the start of a
	 * particular source location.
	 */
	public IASToken getWhitespaceBefore(ISourceLocation before) {
		int start = findNearestToken(allTokens, before);
		for (int i = start - 1; i >= 0; i--) {
			IASToken token = allTokens[i];
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				continue;
			}
			if (!isWhitespace(token)) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the first whitespace that appears after the end of a
	 * particular source location.
	 */
	public IASToken getWhitespaceAfter(ISourceLocation after) {
		int start = findNearestToken(allTokens, after);
		int end = allTokens.length;
		for (int i = start; i < end; i++) {
			IASToken token = allTokens[i];
			if (!isWhitespace(token)) {
				continue;
			}
			if (token.getAbsoluteStart() < after.getAbsoluteEnd()) {
				continue;
			}
			return token;
		}
		return null;
	}

	/**
	 * Returns the first non-comment, non-whitespace token that appears before
	 * the start of a particular source location.
	 */
	public IASToken getSignificantTokenBefore(ISourceLocation before) {
		return getTokenBefore(before, true, true);
	}

	/**
	 * Returns the first non-comment, non-whitespace token that appears after
	 * the end of a particular source location.
	 */
	public IASToken getSignificantTokenAfter(ISourceLocation after) {
		return getTokenAfter(after, true, true);
	}

	private static int findNearestToken(IASToken[] array, ISourceLocation toFind) {
		int low = 0;
		int high = array.length - 1;
		int keyVal = toFind.getAbsoluteStart();
		while (low <= high) {
			int mid = (low + high) / 2;
			int midVal = array[mid].getAbsoluteStart();
			if (midVal < keyVal) {
				low = mid + 1;
			} else if (midVal > keyVal) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return low;
	}
}
