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

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.parsing.IMXMLToken;

public class MXMLTokenQuery {
	public static final int TOKEN_TYPE_FORMATTING = 999999;

	public MXMLTokenQuery(IMXMLToken[] tokens) {
		allTokens = tokens;
	}

	private IMXMLToken[] allTokens;

	/**
	 * Returns all tokens in the file.
	 */
	public IMXMLToken[] getTokens() {
		return allTokens;
	}

	/**
	 * Returns the token immediately before a source location.
	 */
	public IMXMLToken getTokenBefore(ISourceLocation sourceLocation) {
		return getTokenBefore(sourceLocation, false, false);
	}

	/**
	 * Returns the token immediately before a source location, with the option
	 * to skip comment and formatting tokens.
	 */
	public IMXMLToken getTokenBefore(ISourceLocation sourceLocation, boolean skipComments, boolean skipFormatting) {
		IMXMLToken result = null;
		for (IMXMLToken token : allTokens) {
			if (skipComments && isComment(token)) {
				continue;
			}
			if (skipFormatting && isFormatting(token)) {
				continue;
			}
			if (token.getStart() >= sourceLocation.getAbsoluteStart()) {
				return result;
			}
			result = token;
		}
		return null;
	}

	/**
	 * Returns the token immediately after a source location.
	 */
	public IMXMLToken getTokenAfter(ISourceLocation sourceLocation) {
		return getTokenAfter(sourceLocation, false, false);
	}

	/**
	 * Returns the token immediately after a source location, with the option to
	 * skip comment tokens.
	 */
	public IMXMLToken getTokenAfter(ISourceLocation sourceLocation, boolean skipComments, boolean skipFormatting) {
		for (IMXMLToken token : allTokens) {
			if (token.getStart() >= sourceLocation.getAbsoluteEnd()) {
				if (skipComments && isComment(token)) {
					continue;
				}
				if (skipFormatting && isFormatting(token)) {
					continue;
				}
				return token;
			}
		}
		return null;
	}

	/**
	 * Checks if a token is comment.
	 */
	public boolean isComment(IMXMLToken token) {
		return token.getMXMLTokenKind() == IMXMLToken.MXMLTokenKind.COMMENT;
	}

	/**
	 * Returns the first comment that appears before the start of a particular
	 * source location.
	 */
	public IMXMLToken getCommentBefore(ISourceLocation before) {
		IMXMLToken result = null;
		for (IMXMLToken token : allTokens) {
			if (token.getStart() >= before.getAbsoluteStart()) {
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
	public IMXMLToken getCommentAfter(ISourceLocation after) {
		for (IMXMLToken token : allTokens) {
			if (token.getStart() >= after.getAbsoluteEnd() && isComment(token)) {
				return token;
			}
		}
		return null;
	}

	/**
	 * Checks if a token is formatting.
	 */
	public boolean isFormatting(IMXMLToken token) {
		return token.getType() == TOKEN_TYPE_FORMATTING;
	}

	/**
	 * Returns the first formatting token that appears before the start of a
	 * particular source location.
	 */
	public IMXMLToken getFormattingBefore(ISourceLocation before) {
		IMXMLToken result = null;
		for (IMXMLToken token : allTokens) {
			if (token.getStart() >= before.getAbsoluteStart()) {
				return result;
			}
			if (isFormatting(token)) {
				result = token;
			}
		}
		return null;
	}

	/**
	 * Returns the first formatting token that appears after the end of a
	 * particular source location.
	 */
	public IMXMLToken getFormattingAfter(ISourceLocation after) {
		for (IMXMLToken token : allTokens) {
			if (token.getStart() >= after.getAbsoluteEnd() && isFormatting(token)) {
				return token;
			}
		}
		return null;
	}

	/**
	 * Returns the first non-comment, non-formatting token that appears before
	 * the start of a particular source location.
	 */
	public IMXMLToken getSignificantTokenBefore(ISourceLocation before) {
		return getTokenBefore(before, true, true);
	}

	/**
	 * Returns the first non-comment, non-formatting token that appears after
	 * the end of a particular source location.
	 */
	public IMXMLToken getSignificantTokenAfter(ISourceLocation after) {
		return getTokenAfter(after, true, true);
	}
	
}
