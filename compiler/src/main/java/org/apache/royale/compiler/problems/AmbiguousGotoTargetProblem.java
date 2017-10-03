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

package org.apache.royale.compiler.problems;

import java.util.Collection;

import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.IASNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Diagnostic emitted when a goto statement's target is ambiguous, ie there are
 * two labels with the same name that are visible to the goto statement.
 */
public final class AmbiguousGotoTargetProblem extends CodegenProblem
{
    public static final String DESCRIPTION =
        "Target of goto statement was ambiguous, possible targets (line:column): ${targets}";

    public static final int errorCode = 1306;

    public AmbiguousGotoTargetProblem(IASNode site, Collection<LabeledStatementNode> gotoLabels)
    {
        super(site);
        Iterable<String> locationStrings =
            Iterables.transform(gotoLabels, new Function<LabeledStatementNode, String>(){

                @Override
                public String apply(LabeledStatementNode targetLocation)
                {
                    return Integer.toString(targetLocation.getLine() + 1) + ":" + Integer.toString(targetLocation.getColumn() + 1);
                }});
        targets = Joiner.on(", ").join(locationStrings);
    }
    
    public final String targets;
}

