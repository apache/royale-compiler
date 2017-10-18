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
 * A button record defines a character to be displayed in one or more button
 * states. The ButtonState flags indicate which state (or states) the character
 * belongs to.
 * <p>
 * A one-to-one relationship does not exist between button records and button
 * states. A single button record can apply to more than one button state (by
 * setting multiple ButtonState flags), and multiple button records can be
 * present for any button state.
 * <p>
 * Each button record also includes a transformation matrix and depth
 * (stacking-order) information. These apply just as in a PlaceObject tag,
 * except that both pieces of information are relative to the button character
 * itself.
 */
public class ButtonRecord implements IDataType
{
    private boolean hasBlendMode;
    private boolean hasFilterList;
    private boolean stateHitTest;
    private boolean stateDown;
    private boolean stateOver;
    private boolean stateUp;
    private int characterID;
    private int placeDepth;
    private Matrix placeMatrix;
    private CXFormWithAlpha colorTransform;
    private Filter filterList[];
    private int blendMode;

    /**
     * @return the hasBlendMode
     */
    public boolean isHasBlendMode()
    {
        return hasBlendMode;
    }

    /**
     * @param hasBlendMode the hasBlendMode to set
     */
    public void setHasBlendMode(boolean hasBlendMode)
    {
        this.hasBlendMode = hasBlendMode;
    }

    /**
     * @return the hasFilterList
     */
    public boolean isHasFilterList()
    {
        return hasFilterList;
    }

    /**
     * @param hasFilterList the hasFilterList to set
     */
    public void setHasFilterList(boolean hasFilterList)
    {
        this.hasFilterList = hasFilterList;
    }

    /**
     * @return the stateHitTest
     */
    public boolean isStateHitTest()
    {
        return stateHitTest;
    }

    /**
     * @param stateHitTest the stateHitTest to set
     */
    public void setStateHitTest(boolean stateHitTest)
    {
        this.stateHitTest = stateHitTest;
    }

    /**
     * @return the stateDown
     */
    public boolean isStateDown()
    {
        return stateDown;
    }

    /**
     * @param stateDown the stateDown to set
     */
    public void setStateDown(boolean stateDown)
    {
        this.stateDown = stateDown;
    }

    /**
     * @return the stateOver
     */
    public boolean isStateOver()
    {
        return stateOver;
    }

    /**
     * @param stateOver the stateOver to set
     */
    public void setStateOver(boolean stateOver)
    {
        this.stateOver = stateOver;
    }

    /**
     * @return the stateUp
     */
    public boolean isStateUp()
    {
        return stateUp;
    }

    /**
     * @param stateUp the stateUp to set
     */
    public void setStateUp(boolean stateUp)
    {
        this.stateUp = stateUp;
    }

    /**
     * @return the characterID
     */
    public int getCharacterID()
    {
        return characterID;
    }

    /**
     * @param characterID the characterID to set
     */
    public void setCharacterID(int characterID)
    {
        this.characterID = characterID;
    }

    /**
     * @return the placeDepth
     */
    public int getPlaceDepth()
    {
        return placeDepth;
    }

    /**
     * @param placeDepth the placeDepth to set
     */
    public void setPlaceDepth(int placeDepth)
    {
        this.placeDepth = placeDepth;
    }

    /**
     * @return the placeMatrix
     */
    public Matrix getPlaceMatrix()
    {
        return placeMatrix;
    }

    /**
     * @param placeMatrix the placeMatrix to set
     */
    public void setPlaceMatrix(Matrix placeMatrix)
    {
        this.placeMatrix = placeMatrix;
    }

    /**
     * @return the colorTransform
     */
    public CXFormWithAlpha getColorTransform()
    {
        return colorTransform;
    }

    /**
     * @param colorTransform the colorTransform to set
     */
    public void setColorTransform(CXFormWithAlpha colorTransform)
    {
        this.colorTransform = colorTransform;
    }

    /**
     * @return the filterList
     */
    public Filter[] getFilterList()
    {
        return filterList;
    }

    /**
     * @param filterList the filterList to set
     */
    public void setFilterList(Filter[] filterList)
    {
        this.filterList = filterList;
    }

    /**
     * @return the blendMode
     */
    public int getBlendMode()
    {
        return blendMode;
    }

    /**
     * @param blendMode the blendMode to set
     */
    public void setBlendMode(int blendMode)
    {
        this.blendMode = blendMode;
    }
}
