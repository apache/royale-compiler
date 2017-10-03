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

import java.lang.reflect.Constructor;


import static org.apache.royale.compiler.tree.ASTNodeID.UnknownID;


import org.apache.royale.compiler.internal.as.codegen.CmcEmitter;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;


/**
 *  UnknownTreeFinding tracks a problem and state information
 *  as the problem makes its way through the UnknownTreeHandler's
 *  problem analysis system.
 */
class UnknownTreeFinding
{
    /**
     *  The problem payload.
     */
    final ICompilerProblem problem;

    /**
     *  If true, this problem may be superseded by
     *  a problem from a subtree or a non-provisonal problem.
     */
    final boolean provisional;

    /**
     *  Construct a new finding.
     *  @param problem - the payload problem.
     *  @param provisional - can this finding be superseded?
     */
    UnknownTreeFinding(ICompilerProblem problem, boolean provisional)
    {
        this.problem = problem;
        this.provisional = provisional;
    }

    /**
     *  Diagnostic toString() method.
     */
    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();

        buff.append(problem.getClass().getName());
        buff.append("@");
        buff.append(Integer.toHexString(System.identityHashCode(this)));
        buff.append(", p:");
        buff.append(provisional);

        return buff.toString();
    }


    /**
     *  An UnknownTreeFinding.Template holds the filter criteria and
     *  prototype instance data of a potential UnknownTreeFinding.
     *  @see matches to check a candidate annotated
     *    IASNode against this template's criteria.
     *  @see createFinding to create a UnknownTreeFinding.
     */
    static class Template
    {
        /*
         *  ****************
         *  **  Criteria  **
         *  ****************
         */

        /**
         *  The node id to match.
         *  UnknownID means "match any node ID"
         */
        ASTNodeID id = UnknownID;

        /**
         *  The class of node to match;
         *  used to create templates that
         *  apply to more general groups
         *  of ASTs (e.g., BinaryOperatorNode).
         */
        Class<? extends Object> nodeClass = null;

        /**
         *  If not -1, then the node's annotation
         *  must have a feasible cost for this state.
         */
        int mustHaveState = -1;

        /**
         *  If not -1, then the node's annotation
         *  cannot have a feasible cost for this state.
         */
        int cantHaveState = -1;
        
        /**
         *  If not null, then the node must have
         *  a child that matches this pattern.
         */
        Template requiredSubtree = null;


        /*
         *  ******************************
         *  **  Finding Prototype Data  **
         *  ******************************
         */

        /**
         *  The class of the finding's diagnostic problem.
         *  The problem is assumed to take an IASNode parameter.
         */
        Class<? extends Object> problemClass;

        /**
         *  Is this finding provisional?
         */
        boolean provisional = false;

        /**
         *  @return true if the annotated AST matches this template.
         */
        boolean matches( CmcEmitter.JBurgAnnotation annotation )
        {
            //  Check criteria.
            if ( id != annotation.getNode().getNodeID() && id != UnknownID )
                return false;
            if ( mustHaveState != -1 && annotation.getCost(mustHaveState) >= Integer.MAX_VALUE )
                return false;
            if ( cantHaveState != -1 && annotation.getCost(cantHaveState) <  Integer.MAX_VALUE )
                return false;
            if ( nodeClass != null && ! ( nodeClass.isInstance(annotation.getNode()) )  )
                return false;
            
            //  Check subtree matches.
            if ( requiredSubtree != null )
            {
                boolean found_match = false;

                for ( int i = 0; !found_match && i < annotation.getArity(); i++ )
                {
                    found_match |= requiredSubtree.matches(annotation.getNthChild(i));
                }

                return found_match;
            }
            else
            {
                return true;
            }
        }

        /**
         *  Get the innermost matched node rooted at the given node.
         *  @param annotation - the annotated subtree root.
         *  @return the innermost matching node.
         */
        CmcEmitter.JBurgAnnotation getInnermostMatchedNode( CmcEmitter.JBurgAnnotation annotation )
        {
            if ( requiredSubtree != null )
            {
                for ( int i = 0; i < annotation.getArity(); i++ )
                {
                    if ( requiredSubtree.matches(annotation.getNthChild(i)) )
                        return requiredSubtree.getInnermostMatchedNode(annotation.getNthChild(i));
                }

                //  Should have been verified by matches(), above.
                assert false: "Required subtree not present";
            }

            return annotation;
        }

        /**
         *  Create a UnknownTreeFinding from this template.
         *  @param problem_node - the node that's been identified as
         *    the site of the error.  Passed to the problem constructor
         *    as the constructor's only parameter.
         */
        UnknownTreeFinding createFinding(CmcEmitter.JBurgAnnotation problem_node)
        throws Exception
        {
            Constructor<? extends Object> ctor = problemClass.getDeclaredConstructor(IASNode.class);
            ICompilerProblem problem = (ICompilerProblem) ctor.newInstance(problem_node.getNode());

            return new UnknownTreeFinding(problem, this.provisional);
        }

        /**
         *  @return a diagnostic representation of this template.
         */
        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();
            buf.append(id);
            if ( cantHaveState != -1 )
            {
                buf.append(",ch:");
                buf.append(cantHaveState);
            }
            if ( mustHaveState != -1 )
            {
                buf.append(",mh:");
                buf.append(mustHaveState);
            }
            if ( nodeClass != null )
            {
                buf.append(",nc: ");
                buf.append(nodeClass.getName());
            }
            buf.append("@");
            buf.append(Integer.toHexString(System.identityHashCode(this)));

            if ( requiredSubtree != null )
            {
                buf.append("(" + requiredSubtree + ")");
            }

            return buf.toString();
        }
    }
}
