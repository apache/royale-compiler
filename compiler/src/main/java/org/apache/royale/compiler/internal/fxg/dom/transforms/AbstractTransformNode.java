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

package org.apache.royale.compiler.internal.fxg.dom.transforms;

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_ID_ATTRIBUTE;

import java.util.Collection;

import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.ITransformNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A base class for all FXG nodes that represent a transform.
 */
public abstract class AbstractTransformNode extends AbstractFXGNode implements ITransformNode 
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    //------------
    // id
    //------------

    protected String id;

    /**
     * An id attribute provides a well defined name to a content node.
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Sets the node id.
     * @param value - the node id as a String.
     */
    @Override
    public void setId(String value)
    {
        id = value;
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Sets an FXG attribute on this transform node.
     * 
     * @param name - the unqualified attribute name.
     * @param value - the attribute value.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_ID_ATTRIBUTE.equals(name))
            id = value;
        else
            super.setAttribute(name, value, problems);
    }
}
