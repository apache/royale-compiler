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

public class TestLocalVarAndParameterNameRule {
	@Test
	public void testLocalVarNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var myVar:String;}}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testLocalVarNameStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var MyVar:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testLocalVarNameStartsWith$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var $myVar:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testLocalVarNameStartsWith_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var _myVar:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testLocalVarNameStartsWith_AndUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var _MyVar:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testLocalVarNameContains$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var my$Var:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testLocalVarNameContains_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction():void{var my_Var:String;}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.LocalVarNameLinterProblem);
	}

	@Test
	public void testParameterNameValid() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction(myParam:String):void{}}}", problems);
		assertEquals(0, problems.size());
	}

	@Test
	public void testParameterNameStartsWithUpperCase() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction(MyParam:String):void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.ParameterNameLinterProblem);
	}

	@Test
	public void testParameterNameStartsWith$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction($myParam:String):void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.ParameterNameLinterProblem);
	}

	@Test
	public void testParameterNameStartsWith_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction(_myParam:String):void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.ParameterNameLinterProblem);
	}

	@Test
	public void testParameterNameContains$() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction(my$Param:String):void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.ParameterNameLinterProblem);
	}

	@Test
	public void testParameterNameContains_() {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		rules.add(new LocalVarAndParameterNameRule());
		LinterSettings settings = new LinterSettings();
		settings.rules = rules;
		ASLinter linter = new ASLinter(settings);
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		linter.lint("file.as", "package {class MyClass{function myFunction(my_Param:String):void{}}}", problems);
		assertEquals(1, problems.size());
		assertTrue(problems.get(0) instanceof LocalVarAndParameterNameRule.ParameterNameLinterProblem);
	}
}