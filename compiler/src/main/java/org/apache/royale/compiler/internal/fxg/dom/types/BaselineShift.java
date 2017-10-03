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

package org.apache.royale.compiler.internal.fxg.dom.types;

/**
 * The BaselineShift class. Underline value can be either a double or 
 * a BaselineShiftAsEnum enum.
 * 
 * <pre>
 *   0 = superscript
 *   1 = subscript
 * </pre>
 */
public class BaselineShift
{
    private double baselineShiftAsDbl = 0.0;
    private BaselineShiftAsEnum baselineShiftAsEnum = null;
    
    /**
     *  The BaselineShiftAsEnum class.
     * 
     * <pre>
     *   0 = superscript
     *   1 = subscript
     * </pre>
     */
    public enum BaselineShiftAsEnum
    {
        /**
         * The enum representing an 'superscript' BaselineShift.
         */
        SUPERSCRIPT,

        /**
         * The enum representing an 'subscript' BaselineShift.
         */
        SUBSCRIPT;
    }
    
    private BaselineShift()
    {    
    }
    
    /**
     * Create a new instance of BaselineShift with value set as an enum.
     * @param baselineShiftAsEnum - BaselineShift value set as enum.
     * @return a new instance of BaselineShift.
     */
    public static BaselineShift newInstance(BaselineShiftAsEnum baselineShiftAsEnum)
    {
        BaselineShift baselineShift = new BaselineShift();
        baselineShift.baselineShiftAsEnum = baselineShiftAsEnum;
        return baselineShift;
    }
    
    /**
     * Create a new instance of BaselineShift with value set as a double.
     * @param baselineShiftAsDbl - BaselineShift value set as double.
     * @return a new instance of BaselineShift.
     */
    public static BaselineShift newInstance(double baselineShiftAsDbl)
    {
        BaselineShift baselineShift = new BaselineShift();
        baselineShift.baselineShiftAsDbl = baselineShiftAsDbl;
        return baselineShift;
    }  
    
    public boolean isBaselineShiftAsEnum()
    {
        if (this.baselineShiftAsEnum != null)
            return true;
        else
            return false;
    }
    
    public BaselineShiftAsEnum getBaselineShiftAsEnum()
    {
        return this.baselineShiftAsEnum;
    }
    
    public double getBaselineShiftAsDbl()
    {
        return this.baselineShiftAsDbl;
    }
}
