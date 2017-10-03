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

package org.apache.royale.compiler.internal.fxg.dom;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * The mask property node is a special delegate that associates a mask with a
 * parent graphic content node. 
 */
public class MaskPropertyNode extends DelegateNode
{
    public IMaskingNode mask;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a mask node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_MASK_ELEMENT;
    }

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (mask == null && child instanceof IMaskingNode)
        {
            mask = (IMaskingNode)child;
            delegate.addChild(this, problems);
        }
        else
        {
            super.addChild(child, problems);
        }
    }
}
