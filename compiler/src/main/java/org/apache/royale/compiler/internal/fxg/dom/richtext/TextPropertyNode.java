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

package org.apache.royale.compiler.internal.fxg.dom.richtext;

import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DelegateNode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.problems.FXGInvalidChildNodeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class TextPropertyNode extends DelegateNode
{
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (!(delegate instanceof ITextNode))
        {
            problems.add(new FXGInvalidChildNodeProblem(getDocumentPath(), child.getStartLine(), 
                    child.getStartColumn(), getNodeName(), delegate.getNodeName()));    
        }
        else if (delegate instanceof ITextNode && child instanceof ITextNode)
        {
            ((ITextNode)delegate).addTextProperty(getNodeName(), (ITextNode)child, problems);
        }
        else    
        {
            problems.add(new FXGInvalidChildNodeProblem(getDocumentPath(), getStartLine(), 
                    getStartColumn(), child.getNodeName(), getNodeName()));  
        }
    }
}
