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

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.junit.After;
import org.junit.Before;

public class BaseFormatterTests {
	protected ArrayList<ICompilerProblem> problems;

	@Before
	public void setup() {
		problems = new ArrayList<ICompilerProblem>();
	}

	@After
	public void teardown() {
		CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(null);
		for (ICompilerProblem problem : problems) {
			CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem);
			if(CompilerProblemSeverity.ERROR.equals(severity)) {
				fail(problem.toString() + " (" + problem.getLine() + ", " + problem.getColumn() + ")");
			}
		}
		problems = null;
	}
}
