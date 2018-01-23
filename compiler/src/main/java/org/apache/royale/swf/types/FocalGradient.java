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
 * A {@code FocalGradient} must be declared in {@link org.apache.royale.swf.tags.DefineShape4Tag}.
 * <p>
 * The value range is from -1.0 to 1.0, where -1.0 means the focal point is
 * close to the left border of the radial gradient circle, 0.0 means that the
 * focal point is in the center of the radial gradient circle, and 1.0 means
 * that the focal point is close to the right border of the radial gradient
 * circle.
 */
public class FocalGradient extends Gradient
{
    private float focalPoint;

    /**
     * Get focal point location.
     * 
     * @return focal point location
     */
    public float getFocalPoint()
    {
        return focalPoint;
    }

    /**
     * Set focal point location.
     * 
     * @param focalPoint focal point location
     */
    public void setFocalPoint(float focalPoint)
    {
        this.focalPoint = focalPoint;
    }
}
