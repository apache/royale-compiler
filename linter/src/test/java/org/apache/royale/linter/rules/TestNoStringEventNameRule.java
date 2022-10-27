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

package org.apache.royale.linter.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.linter.ASLinter;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.LinterSettings;
import org.junit.Test;

public class TestNoStringEventNameRule {
	@Test
	public void testAddEventListenerStringEventName() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new NoStringEventNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "addEventListener(\"change\", listener)", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof NoStringEventNameRule.NoStringEventNameLinterProblem);
	}

	@Test
	public void testRemoveEventListenerStringEventName() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new NoStringEventNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "removeEventListener(\"change\", listener)", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof NoStringEventNameRule.NoStringEventNameLinterProblem);
	}

	@Test
	public void testHasEventListenerStringEventName() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new NoStringEventNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "hasEventListener(\"change\", listener)", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof NoStringEventNameRule.NoStringEventNameLinterProblem);
	}
}