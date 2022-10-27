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

public class TestValidTypeofRule {
	@Test
	public void testInvalidTypeof() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"other\"", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof ValidTypeofRule.ValidTypeofLinterProblem);
	}

	@Test
	public void testValidTypeofBoolean() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"boolean\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofFunction() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"function\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofNumber() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"number\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofObject() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"object\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofString() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"string\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofXml() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"xml\"", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testValidTypeofUndefined() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new ValidTypeofRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "typeof a == \"undefined\"", problems);
		assertEquals(0, problems.size());
	}
}