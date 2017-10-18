/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.as.codegen;

import java.util.ArrayList;
import java.util.Collection;


import org.apache.royale.compiler.problems.BURMPatternMatchFailureProblem;
import org.apache.royale.compiler.problems.CodegenInternalProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;

import static org.apache.royale.compiler.tree.ASTNodeID.*;


/**
 *  The UnknownTreeHandler-matches an annotated
 *  AST against a set of prototype problems; the most
 *  successful matches are recorded as problems.
 */
class UnknownTreeHandler
{
    Collection<ICompilerProblem> problems;

    /**
     *  Construct an Analyzer.
     *  @param problems - the caller's collection of problems.
     */
    public UnknownTreeHandler(Collection<ICompilerProblem> problems )
    {
        this.problems = problems;
    }

    /**
     *  Analyze an annotated AST against the preset collection
     *  of prototype problems.
     */
    public void analyze(CmcEmitter.JBurgAnnotation root)
    {
        Collection <UnknownTreeFinding> findings = exploreSubtrees(root);

        //  If the error presents with a completely unknown pattern,
        //  issue a last-gasp diagnosis.
        if ( findings.isEmpty() )
        {
            this.problems.add(new BURMPatternMatchFailureProblem(root.getNode()));
        }
        else
        {
            for ( UnknownTreeFinding finding: findings )
            {
                this.problems.add(finding.problem);
            }
        }
    }

    /**
     *  Traverse the failed AST and attempt to find an
     *  error pattern that yields a diagnostic.
     *  @param subtree_root - the root of this subtree.
     *    Note that this routine recursively descends through the subtree.
     */
    Collection <UnknownTreeFinding> exploreSubtrees(CmcEmitter.JBurgAnnotation subtree_root)
    {
        Collection<UnknownTreeFinding> result = new ArrayList<UnknownTreeFinding>();
        Collection <UnknownTreeFinding> provisional_findings = new ArrayList<UnknownTreeFinding>();

        //  Identify candidate matches.
        for ( UnknownTreeFinding match: findMatches(subtree_root) )
        {
            if ( match.provisional )
                provisional_findings.add(match);
            else
                result.add(match);
        }


        //  Explore this node's subtrees; if there is no definite
        //  finding at this level, then take all the subtree matches;
        //  if there was, only take the definite matches from the subtree.

        boolean definite_finding_at_level = !result.isEmpty();

        for ( int i = 0; i < subtree_root.getArity(); i++ )
        {
            if ( !definite_finding_at_level )
            {
                result.addAll(exploreSubtrees(subtree_root.getNthChild(i)));
            }
            else
            {
                for ( UnknownTreeFinding sub_finding: exploreSubtrees(subtree_root.getNthChild(i)) )
                {
                    if ( !sub_finding.provisional ) 
                        result.add(sub_finding);
                }
            }
        }

        //  Use this level's provisional findings if nothing better has been found.
        if ( result.isEmpty() )
            result = provisional_findings;

        return result;
    }

    /**
     *  Find all the findings that apply to an annotated AST.
     *  @param annotation - the annotated AST.
     */
    ArrayList<UnknownTreeFinding> findMatches(CmcEmitter.JBurgAnnotation annotation)
    {
        IASNode node = annotation.getNode();

        ArrayList<UnknownTreeFinding.Template> candidates = new ArrayList<UnknownTreeFinding.Template>();
        ArrayList<UnknownTreeFinding> result = new ArrayList<UnknownTreeFinding>();

        //  Get the initial set of findings: all findings filed
        //  under this node's ASTNodeID, and all findings filed
        //  under "Unknown" which means "any" in this context.
        if ( UnknownTreeHandlerPatterns.allTemplates.containsKey(node.getNodeID()) )
            candidates.addAll(UnknownTreeHandlerPatterns.allTemplates.get(node.getNodeID()));

        if ( UnknownTreeHandlerPatterns.allTemplates.containsKey(UnknownID) )
            candidates.addAll(UnknownTreeHandlerPatterns.allTemplates.get(UnknownID) );

        for ( UnknownTreeFinding.Template candidate: candidates )
        {
            if ( candidate.matches(annotation) )
            {
                try
                {
                    result.add(candidate.createFinding(candidate.getInnermostMatchedNode(annotation)));
                }
                catch ( Exception ex )
                {
                    this.problems.add(new CodegenInternalProblem(node, ex));
                }
            }
        }

        return result;
    }
}
