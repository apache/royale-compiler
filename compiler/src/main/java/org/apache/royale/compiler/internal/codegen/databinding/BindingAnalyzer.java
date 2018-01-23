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

package org.apache.royale.compiler.internal.codegen.databinding;

import java.util.Collection;

import org.apache.royale.compiler.internal.as.codegen.MXMLClassDirectiveProcessor;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;

/**
 * Does an analysis of a single binding expressions.
 * Will update the BindingDataBase with BindingInfo objects that
 * represent mx.internal.binding.Binding objects that will be generated.
 */
public class BindingAnalyzer
{
    /**
     * Analyze an IMXMLDataBindingNode (the ones we see when
     * there are curly braces)
     * BindingInfo ctor does all the work.
     * 
     * @param problems - if analysis finds problems they are reported here
     * @param bindingDataBase - results of analysis are stored here
     * @param node - the node that is being analyzed
     */
    public static BindingInfo analyze(
            IMXMLDataBindingNode node,
            BindingDatabase bindingDataBase,
            Collection<ICompilerProblem> problems,
            int bindingIndex,
            MXMLClassDirectiveProcessor host)
    {
        BindingInfo info = new BindingInfo(node, bindingIndex, host);

        bindingDataBase.getBindingInfo().add(info);
        saveScope(node, bindingDataBase);


        return info;
    }

    private static void saveScope(IMXMLNode node, BindingDatabase bindingDataBase)
    {
        // Later (for code gen), we will need a scope, so let's save it here.
        IScopedNode scopedNode = node.getContainingScope();
        IASScope iscope = scopedNode.getScope();
        ASScope scope = (ASScope)iscope;
        bindingDataBase.setScope(scope);
    }

    /**
     * BindingInfo ctor does all the work.
     *
     * Analyzes an IMXMLBindingNode (the ones we see from Binding tags, and 
     * inside of fx:XML tags, at the moment)
     *
     * @param problems - if analysis finds problems they are reported here
     * @param bindingDataBase - results of analysis are stored here
     * @param node - the node that is being analyzed
     * @param reverseSourceAndDest - if true, treat the binding source as the destination, and vice versa
     */
    public static BindingInfo analyze(
            IMXMLBindingNode node,
            BindingDatabase bindingDataBase,
            Collection<ICompilerProblem> problems,
            int bindingIndex,
            boolean reverseSourceAndDest,
            MXMLClassDirectiveProcessor host)
    {
       
        BindingInfo info = new BindingInfo(node, bindingIndex, host, reverseSourceAndDest);
        bindingDataBase.getBindingInfo().add(info);
        saveScope(node, bindingDataBase);
        return info;
    }
}
