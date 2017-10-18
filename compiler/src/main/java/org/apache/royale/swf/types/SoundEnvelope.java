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
 * Sound Envelope record.
 * 
 * @see SoundInfo
 */
public class SoundEnvelope implements IDataType
{
    private long pos44;
    private int leftLevel;
    private int rightLevel;

    /**
     * @return the pos44
     */
    public long getPos44()
    {
        return pos44;
    }

    /**
     * @param pos44 the pos44 to set
     */
    public void setPos44(long pos44)
    {
        this.pos44 = pos44;
    }

    /**
     * @return the leftLevel
     */
    public int getLeftLevel()
    {
        return leftLevel;
    }

    /**
     * @param leftLevel the leftLevel to set
     */
    public void setLeftLevel(int leftLevel)
    {
        this.leftLevel = leftLevel;
    }

    /**
     * @return the rightLevel
     */
    public int getRightLevel()
    {
        return rightLevel;
    }

    /**
     * @param rightLevel the rightLevel to set
     */
    public void setRightLevel(int rightLevel)
    {
        this.rightLevel = rightLevel;
    }
}
