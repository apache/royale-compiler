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
 * ZoneRecord used in {@link org.apache.royale.swf.tags.DefineFontAlignZonesTag}.
 */
public class ZoneRecord implements IDataType
{
    private final int numZoneData = 2; // always 2
    private ZoneData zoneData0;
    private ZoneData zoneData1;
    private boolean zoneMaskY;
    private boolean zoneMaskX;

    /**
     * @return the zoneData0
     */
    public ZoneData getZoneData0()
    {
        return zoneData0;
    }

    /**
     * @param zoneData0 the zoneData0 to set
     */
    public void setZoneData0(ZoneData zoneData0)
    {
        this.zoneData0 = zoneData0;
    }

    /**
     * @return the zoneData1
     */
    public ZoneData getZoneData1()
    {
        return zoneData1;
    }

    /**
     * @param zoneData1 the zoneData1 to set
     */
    public void setZoneData1(ZoneData zoneData1)
    {
        this.zoneData1 = zoneData1;
    }

    /**
     * @return the zoneMaskY
     */
    public boolean isZoneMaskY()
    {
        return zoneMaskY;
    }

    /**
     * @param zoneMaskY the zoneMaskY to set
     */
    public void setZoneMaskY(boolean zoneMaskY)
    {
        this.zoneMaskY = zoneMaskY;
    }

    /**
     * @return the zoneMaskX
     */
    public boolean isZoneMaskX()
    {
        return zoneMaskX;
    }

    /**
     * @param zoneMaskX the zoneMaskX to set
     */
    public void setZoneMaskX(boolean zoneMaskX)
    {
        this.zoneMaskX = zoneMaskX;
    }

    /**
     * @return the numZuneData
     */
    public int getNumZoneData()
    {
        return numZoneData;
    }
}
