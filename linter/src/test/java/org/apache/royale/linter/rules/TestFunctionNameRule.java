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

public class TestFunctionNameRule {
	@Test
	public void testMethodNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{}}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMethodNameStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function MyMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testMethodNameStartsWith$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function $myMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testMethodNameStartsWith_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function _myMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testMethodNameStartsWith_AndUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function _MyMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testMethodNameContains$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function my$Method():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testMethodNameContains_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function my_Method():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function myFunction():void{}}}}",
				problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testLocalFunctionNameStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function MyFunction():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameStartsWith$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function $myFunction():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameStartsWith_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function _myFunction():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameStartsWith_AndUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function _MyFunction():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameContains$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function my$Function():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}

	@Test
	public void testLocalFunctionNameContains_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new FunctionNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myMethod():void{function my_Function():void{}}}}",
				problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof FunctionNameRule.FunctionNameLinterProblem);
	}
}