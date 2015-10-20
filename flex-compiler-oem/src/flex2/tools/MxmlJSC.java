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

import org.apache.flex.compiler.clients.JSCompilerEntryPoint;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.clients.problems.ProblemQueryProvider;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.flex.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.internal.driver.mxml.vf2js.MXMLVF2JSBackend;
import org.apache.flex.compiler.problems.ICompilerProblem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MxmlJSC implements ProblemQueryProvider {

    protected static Class<? extends MXMLJSC> COMPILER;

    protected JSCompilerEntryPoint compiler;

    protected JSCompilerEntryPoint getCompilerInstance(IBackend backend) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        COMPILER = MXMLJSC.class;
        if (compiler == null) {
            compiler = COMPILER.getDeclaredConstructor(IBackend.class).newInstance(backend);
        }
        return compiler;
    }

    public int execute(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IBackend backend = new ASBackend();
        String jsOutputTypeString = "";
        for (String s : args) {
            String[] kvp = s.split("=");

            if (s.contains("-js-output-type")) {
                jsOutputTypeString = kvp[1];
            }
        }

        if (jsOutputTypeString.equals("")) {
            jsOutputTypeString = MXMLJSC.JSOutputType.FLEXJS.getText();
        }

        MXMLJSC.JSOutputType jsOutputType = MXMLJSC.JSOutputType.fromString(jsOutputTypeString);
        switch (jsOutputType) {
            case AMD:
                backend = new AMDBackend();
                break;
            case FLEXJS:
            case FLEXJS_DUAL:
                backend = new MXMLFlexJSBackend();
                break;
            case GOOG:
                backend = new GoogBackend();
                break;
            case VF2JS:
                backend = new MXMLVF2JSBackend();
                break;
        }

        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        return getCompilerInstance(backend).mainNoExit(args, problems, false);
    }

    @Override
    public ProblemQuery getProblemQuery() {
        return ((ProblemQueryProvider) compiler).getProblemQuery();
    }
}
