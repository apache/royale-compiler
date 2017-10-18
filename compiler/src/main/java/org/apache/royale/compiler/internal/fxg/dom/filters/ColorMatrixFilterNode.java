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

import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.problems.FXGInvalidColorMatrixValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

public class ColorMatrixFilterNode extends AbstractFilterNode
{
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------
    
    /**
     * A 4 x 5 matrix transformation for RGBA. Matrix is in row major order
     * with each row comprising of srcR, srcG, srcB, srcA, 1. The first five
     * values apply to Red, the next five to Green, and so forth.
     */
    public float[] matrix = new float[] {1,0,0,0,0,
                                         0,1,0,0,0,
                                         0,0,1,0,0,
                                         0,0,0,1,0};

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a ColorMatrixFilter node, without tag
     * markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_COLORMATRIXFILTER_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_MATRIX_ATTRIBUTE.equals(name))
            matrix = get4x5FloatMatrix(value, name, matrix, problems);
        else
            super.setAttribute(name, value, problems);
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Convert a comma delimited String of 20 numbers to an array of 20 float
     * values representing a 4 x 5 color transform matrix.
     */
    protected float[] get4x5FloatMatrix(String value, String name, float[] defaultMatrix, Collection<ICompilerProblem> problems)
    {
        byte index = 0;
        float[] result = new float[20];
        StringTokenizer tokenizer = new StringTokenizer(value, ",", false);
        try{
            while (tokenizer.hasMoreTokens() && index < 20)
            {
                String token = tokenizer.nextToken();
                float f = DOMParserHelper.parseFloat(this, token);
                result[index++] = f;
            }
        }
        catch(Exception e)
        {
            problems.add(new FXGInvalidColorMatrixValueProblem(getDocumentPath(), getStartLine(), getStartColumn(), value));
            return defaultMatrix;
        }                
        return result;
    }
}
