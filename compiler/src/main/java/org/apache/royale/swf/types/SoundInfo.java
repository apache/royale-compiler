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
 * The SOUNDINFO record modifies how an event sound is played. An event sound is
 * defined with the DefineSound tag. Sound characteristics that can be modified
 * include
 * <ul>
 * <li>Whether the sound loops (repeats) and how many times it loops.</li>
 * <li>Where sound play back begins and ends.</li>
 * <li>A sound envelope for time-based volume control.</li>
 * </ul>
 */
public class SoundInfo implements IDataType
{
    private boolean syncStop;
    private boolean syncNoMultiple;
    private boolean hasEnvelope;
    private boolean hasLoops;
    private boolean hasOutPoint;
    private boolean hasInPoint;
    private long inPoint;
    private long outPoint;
    private int loopCount;
    private int envPoints;
    private SoundEnvelope envelopeRecords[];

    /**
     * @return the syncStop
     */
    public boolean isSyncStop()
    {
        return syncStop;
    }

    /**
     * @param syncStop the syncStop to set
     */
    public void setSyncStop(boolean syncStop)
    {
        this.syncStop = syncStop;
    }

    /**
     * @return the syncNoMultiple
     */
    public boolean isSyncNoMultiple()
    {
        return syncNoMultiple;
    }

    /**
     * @param syncNoMultiple the syncNoMultiple to set
     */
    public void setSyncNoMultiple(boolean syncNoMultiple)
    {
        this.syncNoMultiple = syncNoMultiple;
    }

    /**
     * @return the hasEnvelope
     */
    public boolean isHasEnvelope()
    {
        return hasEnvelope;
    }

    /**
     * @param hasEnvelope the hasEnvelope to set
     */
    public void setHasEnvelope(boolean hasEnvelope)
    {
        this.hasEnvelope = hasEnvelope;
    }

    /**
     * @return the hasLoops
     */
    public boolean isHasLoops()
    {
        return hasLoops;
    }

    /**
     * @param hasLoops the hasLoops to set
     */
    public void setHasLoops(boolean hasLoops)
    {
        this.hasLoops = hasLoops;
    }

    /**
     * @return the hasOutPoint
     */
    public boolean isHasOutPoint()
    {
        return hasOutPoint;
    }

    /**
     * @param hasOutPoint the hasOutPoint to set
     */
    public void setHasOutPoint(boolean hasOutPoint)
    {
        this.hasOutPoint = hasOutPoint;
    }

    /**
     * @return the hasInPoint
     */
    public boolean isHasInPoint()
    {
        return hasInPoint;
    }

    /**
     * @param hasInPoint the hasInPoint to set
     */
    public void setHasInPoint(boolean hasInPoint)
    {
        this.hasInPoint = hasInPoint;
    }

    /**
     * @return the inPoint
     */
    public long getInPoint()
    {
        return inPoint;
    }

    /**
     * @param inPoint the inPoint to set
     */
    public void setInPoint(long inPoint)
    {
        this.inPoint = inPoint;
    }

    /**
     * @return the outPoint
     */
    public long getOutPoint()
    {
        return outPoint;
    }

    /**
     * @param outPoint the outPoint to set
     */
    public void setOutPoint(long outPoint)
    {
        this.outPoint = outPoint;
    }

    /**
     * @return the loopCount
     */
    public int getLoopCount()
    {
        return loopCount;
    }

    /**
     * @param loopCount the loopCount to set
     */
    public void setLoopCount(int loopCount)
    {
        this.loopCount = loopCount;
    }

    /**
     * @return the envPoints
     */
    public int getEnvPoints()
    {
        return envPoints;
    }

    /**
     * @param envPoints the envPoints to set
     */
    public void setEnvPoints(int envPoints)
    {
        this.envPoints = envPoints;
    }

    /**
     * @return the envelopeRecords
     */
    public SoundEnvelope[] getEnvelopeRecords()
    {
        return envelopeRecords;
    }

    /**
     * @param envelopeRecords the envelopeRecords to set
     */
    public void setEnvelopeRecords(SoundEnvelope[] envelopeRecords)
    {
        this.envelopeRecords = envelopeRecords;
    }
}
