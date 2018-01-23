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

package flex2.tools;

import org.apache.royale.compiler.clients.JSCompilerEntryPoint;
import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.ProblemQueryProvider;
import org.apache.royale.compiler.problems.ICompilerProblem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MxmlJSC implements ProblemQueryProvider {
    protected JSCompilerEntryPoint compiler;

    protected JSCompilerEntryPoint getCompilerInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (compiler == null) {
            compiler = new MXMLJSC();
        }
        return compiler;
    }

    public int execute(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        return getCompilerInstance().mainNoExit(args, problems, false);
    }

    public ProblemQuery getProblemQuery() {
        return ((ProblemQueryProvider) compiler).getProblemQuery();
    }
}
