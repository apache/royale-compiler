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

import java.util.Collection;

import org.apache.royale.compiler.fxg.FXGConstants;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGInvalidChildNodeProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * This is a special delegate which special cases content node children for
 * the ITextNode and RichTextNode classes.
 * 
 */
public class ContentPropertyNode extends DelegateNode implements IPreserveWhiteSpaceNode
{
    @Override
    public void setDelegate(IFXGNode delegate, Collection<ICompilerProblem> problems)
    {
        if (!(delegate instanceof ITextNode))
        {
            problems.add(new FXGInvalidChildNodeProblem(getDocumentPath(), getStartLine(), 
                    getStartColumn(), getNodeName(), delegate.getNodeName()));
            return;  
        }
        
        if (delegate instanceof RichTextNode && ((RichTextNode)delegate).content != null)
        {
            problems.add(new FXGMultipleElementProblem(getDocumentPath(), getStartLine(), 
                    getStartColumn(), FXGConstants.FXG_CONTENT_PROPERTY_ELEMENT));
            return; 
        }

        super.setDelegate(delegate, problems);
    }
    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (delegate instanceof TextGraphicNode)
        {
            ((TextGraphicNode)delegate).addContentChild(child, problems);
        }
        else if (delegate instanceof RichTextNode)
        {
            ((RichTextNode)delegate).addContentChild(child, problems);
        }
        else
        {
            problems.add(new FXGInvalidChildNodeProblem(getDocumentPath(), getStartLine(), 
                    getStartColumn(), child.getNodeName(), getNodeName())); 
        }
    }
}
