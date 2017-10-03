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

package org.apache.royale.swf.tags;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>VideoFrame</code> tag in a SWF file.
 * <p>
 * VideoFrame provides a single frame of video data for a video character that
 * is already defined with DefineVideoStream.
 * <p>
 * In playback, the time sequencing of video frames depends on the SWF frame
 * rate only. When SWF playback reaches a particular SWF frame, the video images
 * from any VideoFrame tags in that SWF frame are rendered. Any timing
 * mechanisms built into the video payload are ignored.
 * <p>
 * A VideoFrame tag is not needed for every video character in every frame
 * number specified. A VideoFrame tag merely sets video data associated with a
 * particular frame number; it does not automatically display a video frame. To
 * display a video frame, specify the frame number as the Ratio field in
 * PlaceObject2 or PlaceObject3.
 * <p>
 */
public class VideoFrameTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public VideoFrameTag()
    {
        super(TagType.VideoFrame);
    }

    private DefineVideoStreamTag streamTag;
    private int frameNum;
    private byte[] videoData;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert streamTag != null;
        return CharacterIterableFactory.from(streamTag);
    }

    /**
     * @return the streamTag
     */
    public DefineVideoStreamTag getStreamTag()
    {
        return streamTag;
    }

    /**
     * @param streamTag the streamTag to set
     */
    public void setStreamTag(DefineVideoStreamTag streamTag)
    {
        this.streamTag = streamTag;
    }

    /**
     * @return the frameNum
     */
    public int getFrameNum()
    {
        return frameNum;
    }

    /**
     * @param frameNum the frameNum to set
     */
    public void setFrameNum(int frameNum)
    {
        this.frameNum = frameNum;
    }

    /**
     * @return the videoData
     */
    public byte[] getVideoData()
    {
        return videoData;
    }

    /**
     * @param videoData the videoData to set
     */
    public void setVideoData(byte[] videoData)
    {
        this.videoData = videoData;
    }
}
