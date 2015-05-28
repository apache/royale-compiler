package org.apache.flex.compiler.clients;

import org.apache.flex.compiler.problems.ICompilerProblem;

import java.util.Set;

/**
 * @author: Frederic Thomas
 * Date: 26/05/2015
 * Time: 16:56
 */
public interface JSCompilerEntryPoint {
    public int mainNoExit(final String[] args, Set<ICompilerProblem> problems,
                          Boolean printProblems);
}
