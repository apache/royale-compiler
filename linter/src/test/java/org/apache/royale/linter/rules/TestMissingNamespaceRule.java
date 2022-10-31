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

public class TestMissingNamespaceRule {
	@Test
	public void testWithClassNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingClassNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{class MyClass{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnClassLinterProblem);
	}

	@Test
	public void testWithInterfaceNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public interface MyInterface{}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingInterfaceNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{interface MyInterface{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnInterfaceLinterProblem);
	}

	@Test
	public void testWithFieldNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{private var myField:String;}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingFieldNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{var myField:String;}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnFieldLinterProblem);
	}

	@Test
	public void testWithMethodNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{private function myMethod():void{}}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingMethodNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public class MyClass{function myMethod():void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnMethodLinterProblem);
	}

	@Test
	public void testWithPackageVarNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public var myVar:String;}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingPackageVarNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{var myVar:String;}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnPackageVariableLinterProblem);
	}

	@Test
	public void testWithPackageFunctionNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{public function myFunction():void{}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testMissingPackageFunctionNamespace() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new MissingNamespaceRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package{function myFunction():void{}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof MissingNamespaceRule.MissingNamespaceOnPackageFunctionLinterProblem);
	}
}
