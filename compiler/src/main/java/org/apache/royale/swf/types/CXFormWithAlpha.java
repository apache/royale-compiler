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

package org.apache.royale.swf.types;

/**
 * The {@code CXFORMWITHALPHA} record extends the functionality of
 * {@code CXFORM} by allowing color transforms to be applied to the alpha
 * channel, as well as the red, green, and blue channels.
 */
public class CXFormWithAlpha extends CXForm
{
    private int alphaMultTerm;
    private int alphaAddTerm;

    public void setAddTerm(int red, int green, int blue, int alpha)
    {
        super.setAddTerm(red, green, blue);
        alphaAddTerm = alpha;
    }

    public void setMultTerm(int red, int green, int blue, int alpha)
    {
        super.setMultTerm(red, green, blue);
        alphaMultTerm = alpha;
    }

    public int getAlphaMultTerm()
    {
        return alphaMultTerm;
    }

    public int getAlphaAddTerm()
    {
        return alphaAddTerm;
    }

    public String toString()
    {
        return super.toString() +
            alphaMultTerm + "a" + (alphaAddTerm>=0 ? "+" : "") + alphaAddTerm + " " ;
        
    }
}
