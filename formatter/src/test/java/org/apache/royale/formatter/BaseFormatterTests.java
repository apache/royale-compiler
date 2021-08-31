package org.apache.royale.formatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

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
		int numProblems = problems.size();
		if (numProblems > 0) {
			for (ICompilerProblem problem : problems) {
				fail(problem.toString() + " (" + problem.getLine() +", " + problem.getColumn() + ")");
			}
		}
		problems = null;
	}
}
