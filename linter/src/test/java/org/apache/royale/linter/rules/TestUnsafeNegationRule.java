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

public class TestUnsafeNegationRule {
	@Test
	public void testUnsafeNegationIn() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new UnsafeNegationRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!a in b", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof UnsafeNegationRule.UnsafeNegationLinterProblem);
	}

	@Test
	public void testUnsafeNegationIs() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new UnsafeNegationRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!a is b", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof UnsafeNegationRule.UnsafeNegationLinterProblem);
	}

	@Test
	public void testUnsafeNegationInstanceOf() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new UnsafeNegationRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!a instanceof b", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof UnsafeNegationRule.UnsafeNegationLinterProblem);
	}

	@Test
	public void testSafeNegationIn() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new StrictEqualityRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!(a in b)", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testSafeNegationIs() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new StrictEqualityRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!(a is b)", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testSafeNegationInstanceOf() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new StrictEqualityRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "!(a instanceof b)", problems);
		assertEquals(0, problems.size());
	}
}