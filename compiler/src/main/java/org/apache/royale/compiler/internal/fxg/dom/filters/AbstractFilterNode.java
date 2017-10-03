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

package org.apache.royale.compiler.internal.fxg.dom.filters;

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BEVEL_FULL_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BEVEL_INNER_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BEVEL_OUTER_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_TYPE_ATTRIBUTE;

import java.util.Collection;

import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.IFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.types.BevelType;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

public abstract class AbstractFilterNode extends AbstractFXGNode implements IFilterNode
{
    protected static final int QUALITY_MIN_INCLUSIVE = 1;
    protected static final int QUALITY_MAX_INCLUSIVE = 3;

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
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Convert an FXG String value to a BevelType enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching BevelType type.
     */
    protected BevelType getBevelType(String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_BEVEL_INNER_VALUE.equals(value))
            return BevelType.INNER;
        else if (FXG_BEVEL_OUTER_VALUE.equals(value))
            return BevelType.OUTER;
        else if (FXG_BEVEL_FULL_VALUE.equals(value))
            return BevelType.FULL;
        else
        {
        	//Unknown bevel type: {0}.
            problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), getStartLine(), 
                    getStartColumn(), FXG_TYPE_ATTRIBUTE, value));
            return null;
        }
    }
}
