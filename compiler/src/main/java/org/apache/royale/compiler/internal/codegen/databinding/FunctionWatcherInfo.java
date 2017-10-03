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
import java.util.List;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;


/**
 * Specific watcher info for FunctionReturnWatcher
 */
public class FunctionWatcherInfo extends WatcherInfoBase
{
    public FunctionWatcherInfo(Collection<ICompilerProblem> problems, IASNode sourceNode, List<String> eventNames)
    {
       super(problems, sourceNode, eventNames);
       type = WatcherType.FUNCTION;
    }
    
    //------------------ data fields -----------------------------
    
    public IExpressionNode[] params;
    
    /**
     * the short name of the function we will watch
     */
    private String functionName;
    
    /**
     * After creating a FunctionWatcherInfo, you should immediately call 
     * setFunctionName.
     * 
     * This would be a constructor argument, but unfortunately the factory that creates us would
     * find this awkward.
     * 
     */
    public void setFunctionName(String functionName)
    {
        assert functionName != null;
        this.functionName = functionName;
    }
    
    public String getFunctionName()
    {
        return functionName;
    }

    @Override
    public String dump(String leadingSpace)
    {
        String ret = leadingSpace + "FunctionReturnWatcher " + 
            "FuncName=" + functionName +
            super.dump(leadingSpace);
        return ret;
    }
    
    

}
