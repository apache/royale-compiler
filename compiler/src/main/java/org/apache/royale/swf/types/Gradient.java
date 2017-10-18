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

import java.util.ArrayList;
import java.util.List;

/**
 * The gradient structures are part of the FILLSTYLE structure. SWF 8 and later
 * supports up to 15 gradient control points, spread modes and a new
 * interpolation type.
 */
public class Gradient implements IDataType
{
    /* Spread Mode */
    public static final int SM_PAD_MODE = 0;
    public static final int SM_REFLECT_MODE = 1;
    public static final int SM_REPEAT_MODE = 2;

    /* Interpolation Mode */
    public static final int IM_NORMAL_RGB_MODE = 0;
    public static final int IM_LINEAR_RGB_MODE = 1;

    private int spreadMode;
    private int interpolationMode;
    private List<GradRecord> gradientRecords;

    public Gradient()
    {
        gradientRecords = new ArrayList<GradRecord>();
    }

    public int getInterpolationMode()
    {
        return interpolationMode;
    }

    public void setInterpolationMode(int interpolationMode)
    {
        this.interpolationMode = interpolationMode;
    }

    public List<GradRecord> getGradientRecords()
    {
        return gradientRecords;
    }

    public void setGradientRecords(List<GradRecord> gradientRecords)
    {
        this.gradientRecords = gradientRecords;
    }

    public int getSpreadMode()
    {
        return spreadMode;
    }

    public void setSpreadMode(int spreadMode)
    {
        this.spreadMode = spreadMode;
    }

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName())
                .append(", spreadmode=")
                .append(this.spreadMode)
                .append(", int-mode=")
                .append(this.interpolationMode);
        for (final GradRecord gradRecord : gradientRecords)
        {
            result.append(", ").append(gradRecord);
        }
        return result.toString();
    }

}
