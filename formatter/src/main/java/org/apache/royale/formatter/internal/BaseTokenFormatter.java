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

import java.util.Collection;

import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.formatter.FormatterSettings;

public abstract class BaseTokenFormatter {
	protected FormatterSettings settings;

	protected BaseTokenFormatter(FormatterSettings settings) {
		this.settings = settings;
	}

	protected boolean hasErrors(Collection<ICompilerProblem> problems) {
		CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(null);
		for (ICompilerProblem problem : problems) {
			CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem);
			if (CompilerProblemSeverity.ERROR.equals(severity)) {
				return true;
			}
		}
		return false;
	}

	protected int increaseIndent(int indent) {
		return indent + 1;
	}

	protected int decreaseIndent(int indent) {
		return Math.max(0, indent - 1);
	}

	protected String getIndent() {
		if (settings.insertSpaces) {
			String result = "";
			for (int j = 0; j < settings.tabSize; j++) {
				result += " ";
			}
			return result;
		}
		return "\t";
	}

	protected void appendIndent(StringBuilder builder, int indent) {
		String indentString = getIndent();
		for (int i = 0; i < indent; i++) {
			builder.append(indentString);
		}
	}

	protected void appendNewLines(StringBuilder builder, int numRequiredNewLines) {
		if (settings.maxPreserveNewLines != 0) {
			numRequiredNewLines = Math.min(settings.maxPreserveNewLines, numRequiredNewLines);
		}
		for (int j = 0; j < numRequiredNewLines; j++) {
			builder.append('\n');
		}
	}
}
