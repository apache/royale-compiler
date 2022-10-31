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

public class TestMissingASDocRule {
	@Test
	public void testClassWithASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** hello */ public class MyClass{}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testClassMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testClassEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** */ public class MyClass{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}

	@Test
	public void testInterfaceWithASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** hello */ public interface MyClass{}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testInterfaceMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public interface MyClass{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testInterfaceEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** */ public interface MyClass{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}

	@Test
	public void testFieldWithASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{/** hello */public var myVar:String;}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testFieldMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{public var myVar:String;}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testFieldEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{/** */public var myVar:String;}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}

	@Test
	public void testMethodWithASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{/** hello */public function myMethod():void{}}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMethodMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{public function myMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testMethodEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** class */ public class MyClass{/** */public function myMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}

	@Test
	public void testPackageVariableWithASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** hello */ public var myVar:String;}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testPackageVariableMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public var myVar:String;}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testPackageVariableEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** */ public var myVar:String;}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}

	@Test
	public void testPackageFunctionMissingASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public function myFunction():void{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.MissingASDocLinterProblem);
	}

	@Test
	public void testPackageFunctionEmptyASDoc() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingASDocRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{/** */ public function myFunction():void{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingASDocRule.EmptyASDocLinterProblem);
	}
}
