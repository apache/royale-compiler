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

import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.parsing.IMXMLToken;

public class MXMLTokenQuery {
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
		return getTokenBefore(sourceLocation, false);
	}

	/**
	 * Returns the token immediately before a source location, with the option
	 * to skip comment tokens.
	 */
	public IMXMLToken getTokenBefore(ISourceLocation sourceLocation, boolean skipComments) {
		IMXMLToken result = null;
		for (IMXMLToken otherToken : allTokens) {
			if (skipComments && isComment(otherToken)) {
				continue;
			}
			if (otherToken.getStart() >= sourceLocation.getAbsoluteStart()) {
				return result;
			}
			result = otherToken;
		}
		return null;
	}

	/**
	 * Returns the token immediately after a source location.
	 */
	public IMXMLToken getTokenAfter(ISourceLocation sourceLocation) {
		return getTokenAfter(sourceLocation, false);
	}

	/**
	 * Returns the token immediately after a source location, with the option to
	 * skip comment tokens.
	 */
	public IMXMLToken getTokenAfter(ISourceLocation sourceLocation, boolean skipComments) {
		for (IMXMLToken token : allTokens) {
			if (skipComments && isComment(token)) {
				continue;
			}
			if (token.getStart() >= sourceLocation.getAbsoluteEnd()) {
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
	
}
