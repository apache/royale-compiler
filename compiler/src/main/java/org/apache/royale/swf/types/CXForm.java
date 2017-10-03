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
 * The CXFORM record defines a simple transform that can be applied to the color
 * space of a graphic object.
 */
public class CXForm implements IDataType
{
    private boolean hasAdd;
    private boolean hasMult;
    private int redMultTerm;
    private int greenMultTerm;
    private int blueMultTerm;
    private int redAddTerm;
    private int greenAddTerm;
    private int blueAddTerm;

    public void setAddTerm(int red, int green, int blue)
    {
        hasAdd = true;
        redAddTerm = red;
        greenAddTerm = green;
        blueAddTerm = blue;
    }

    public void setMultTerm(int red, int green, int blue)
    {
        hasMult = true;
        redMultTerm = red;
        greenMultTerm = green;
        blueMultTerm = blue;
    }

    public boolean hasAdd()
    {
        return hasAdd;
    }

    public boolean hasMult()
    {
        return hasMult;
    }

    public int getRedMultTerm()
    {
        return redMultTerm;
    }

    public int getGreenMultTerm()
    {
        return greenMultTerm;
    }

    public int getBlueMultTerm()
    {
        return blueMultTerm;
    }

    public int getRedAddTerm()
    {
        return redAddTerm;
    }

    public int getGreenAddTerm()
    {
        return greenAddTerm;
    }

    public int getBlueAddTerm()
    {
        return blueAddTerm;
    }
    
    public String toString()
    {
        return redMultTerm + "r" + (redAddTerm>=0 ? "+" : "") + redAddTerm + " " +
                greenMultTerm + "g" + (greenAddTerm>=0 ? "+" : "") + greenAddTerm + " " +
                blueMultTerm + "b" + (blueAddTerm>=0 ? "+" : "") + blueAddTerm;
    }

}
