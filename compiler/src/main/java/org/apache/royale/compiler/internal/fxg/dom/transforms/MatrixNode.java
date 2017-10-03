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

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class MatrixNode extends AbstractTransformNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double a = 1.0;
    public double b = 0.0;
    public double c = 0.0;
    public double d = 1.0;
    public double tx = 0.0;
    public double ty = 0.0;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a Matrix node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_MATRIX_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_A_ATTRIBUTE.equals(name))
            a = DOMParserHelper.parseDouble(this, value, name, a, problems);
        else if (FXG_B_ATTRIBUTE.equals(name))
            b = DOMParserHelper.parseDouble(this, value, name, b, problems);
        else if (FXG_C_ATTRIBUTE.equals(name))
            c = DOMParserHelper.parseDouble(this, value, name, c, problems);
        else if (FXG_D_ATTRIBUTE.equals(name))
            d = DOMParserHelper.parseDouble(this, value, name, d, problems);
        else if (FXG_TX_ATTRIBUTE.equals(name))
            tx = DOMParserHelper.parseDouble(this, value, name, tx, problems);
        else if (FXG_TY_ATTRIBUTE.equals(name))
            ty = DOMParserHelper.parseDouble(this, value, name, ty, problems);
    }

}
