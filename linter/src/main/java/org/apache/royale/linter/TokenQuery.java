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
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() < node.getAbsoluteStart()) {
				continue;
			}
			if (token.getAbsoluteStart() >= node.getAbsoluteEnd()) {
				break;
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
		IASToken result = null;
		for (IASToken token : allTokens) {
			if (skipComments && isComment(token)) {
				continue;
			}
			if (skipWhitespace && isWhitespace(token)) {
				continue;
			}
			if (token.getAbsoluteStart() >= sourceLocation.getAbsoluteStart()) {
				return result;
			}
			result = token;
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
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= sourceLocation.getAbsoluteEnd()) {
				if (skipComments && isComment(token)) {
					continue;
				}
				if (skipWhitespace && isWhitespace(token)) {
					continue;
				}
				return token;
			}
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
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= node.getAbsoluteStart()) {
				if (skipComments && isComment(token)) {
					continue;
				}
				if (skipWhitespace && isWhitespace(token)) {
					continue;
				}
				return token;
			}
		}
		return null;
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
		IASToken result = null;
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= node.getAbsoluteStart()) {
				if (skipComments && isComment(token)) {
					continue;
				}
				if (skipWhitespace && isWhitespace(token)) {
					continue;
				}
				result = token;
			} else if (result != null) {
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the first token of the specified type that appears before the
	 * start of a particular source location.
	 */
	public IASToken getPreviousTokenOfType(ISourceLocation before, int type) {
		IASToken result = null;
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				return result;
			}
			if (token.getType() == type) {
				result = token;
			}
		}
		return null;
	}

	/**
	 * Returns the first token of the specified type that appears after the end
	 * of a particular source location.
	 */
	public IASToken getNextTokenOfType(ISourceLocation after, int type) {
		for (IASToken token : allTokens) {
			if (token.getType() == type && token.getAbsoluteStart() >= after.getAbsoluteEnd()) {
				return token;
			}
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
		IASToken result = null;
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				return result;
			}
			if (isComment(token)) {
				result = token;
			}
		}
		return null;
	}

	/**
	 * Returns the first comment that appears after the end of a particular
	 * source location.
	 */
	public IASToken getCommentAfter(ISourceLocation after) {
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= after.getAbsoluteEnd() && isComment(token)) {
				return token;
			}
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
		IASToken result = null;
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= before.getAbsoluteStart()) {
				return result;
			}
			if (isWhitespace(token)) {
				result = token;
			}
		}
		return null;
	}

	/**
	 * Returns the first whitespace that appears after the end of a
	 * particular source location.
	 */
	public IASToken getWhitespaceAfter(ISourceLocation after) {
		for (IASToken token : allTokens) {
			if (token.getAbsoluteStart() >= after.getAbsoluteEnd() && isWhitespace(token)) {
				return token;
			}
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
}
