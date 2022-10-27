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

public class TestPackageNameRule {
	@Test
	public void testPackageNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package myPackage {}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testEmptyPackageNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMultiPackageNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package com.myPackage {}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testPackageNameStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package MyPackage {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}

	@Test
	public void testPackageNameMultiStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package com.MyPackage {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}

	@Test
	public void testPackageNameStartsWith$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package $myPackage {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}

	@Test
	public void testPackageNameStartsWith_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package _myPackage {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}

	@Test
	public void testPackageNameContains$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package my$Package {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}

	@Test
	public void testPackageNameContains_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new PackageNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package my_Package {}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof PackageNameRule.PackageNameLinterProblem);
	}
}