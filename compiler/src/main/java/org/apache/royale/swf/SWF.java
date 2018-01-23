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

package org.apache.royale.swf;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.swf.io.SWFWriter;
import org.apache.royale.swf.tags.*;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.Rect;

/**
 * The implementation of a {@code ISWF} object. Most of the ISWF properties are
 * encapsulated in the a {@link Header} member, but we don't expose the
 * {@code Header} at the interface level.
 */
public class SWF implements ISWF
{
    /**
     * Get a {@code FileAttributes} tag from an {@code ISWF} object.
     * 
     * @param swf {@code ISWF} object
     * @return {@code FileAttributesTag}
     */
    public static FileAttributesTag getFileAttributes(ISWF swf)
    {
        if (swf instanceof SWF)
        {
            final SWF swfObject = (SWF)swf;
            return swfObject.fileAttributes;
        }
        else
        {
            final FileAttributesTag tag = new FileAttributesTag();
            tag.setAS3(swf.getUseAS3());
            tag.setHasMetadata(swf.hasMetadata());
            tag.setUseDirectBlit(swf.getUseDirectBlit());
            tag.setUseGPU(swf.getUseGPU());
            tag.setUseNetwork(swf.getUseNetwork());
            return tag;
        }
    }

    /**
     * Constructor.
     */
    public SWF()
    {
        frames = new ArrayList<SWFFrame>();
        header = new Header();
        fileAttributes = new FileAttributesTag();
    }

    private final List<SWFFrame> frames;
    private String topLevelClass;
    private FileAttributesTag fileAttributes;
    private MetadataTag metadata;
    private RGB backgroundColor;
    private Header header;
    private EnableDebugger2Tag enableDebugger2;
    private ScriptLimitsTag scriptLimits;
    private ProductInfoTag productInfoTag;
    private EnableTelemetryTag enableTelemetry;

    @Override
    public void addFrame(SWFFrame frame)
    {
        if (frame == null)
            throw new NullPointerException();

        frames.add(frame);

        // Keep frame count in header sync'ed.
        header.setFrameCount(frames.size());
    }

    // The following are ISWF implementation methods.

    @Override
    public RGB getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * Expose the FileAttributes tag for {@link SWFWriter}.
     * 
     * @return FileAttributes tag
     */
    public FileAttributesTag getFileAttributes()
    {
        return fileAttributes;
    }

    @Override
    public SWFFrame getFrameAt(int index)
    {
        // If the frameCount is greater than the number of frames
        // then assume the header was set correctly but the frames
        // in the SWF were not found. Correct the model at this point
        // by creating the correct number of frames.
        for (int i = frames.size(); i <= index; i++) 
            addFrame(new SWFFrame());

        return frames.get(index);
    }

    @Override
    public int getFrameCount()
    {
        // disable for now, as frames.size is zero if SWFReader.buildFramesFromTags()
        // hasn't been called
        // assert header.getFrameCount() == frames.size();
        return header.getFrameCount();
    }

    @Override
    public float getFrameRate()
    {
        return header.getFrameRate();
    }

    /**
     * Return all the frames in the SWF.
     * 
     * @return frames
     */
    public List<SWFFrame> getFrames()
    {
        return frames;
    }

    @Override
    public Rect getFrameSize()
    {
        return header.getFrameSize();
    }

    public Header getHeader()
    {
        return header;
    }

    @Override
    public String getMetadata()
    {
        return metadata == null ? null : metadata.getMetadata();
    }

    @Override
    public String getTopLevelClass()
    {
        return topLevelClass;
    }

    @Override
    public int getVersion()
    {
        return header.getVersion();
    }

    @Override
    public boolean hasMetadata()
    {
        assert fileAttributes.isHasMetadata() == (metadata != null);
        return fileAttributes.isHasMetadata();
    }

    @Override
    public boolean getUseAS3()
    {
        return fileAttributes.isAS3();
    }

    @Override
    public void setUseAS3(boolean value)
    {
        fileAttributes.setAS3(value);
    }

    @Override
    public void setBackgroundColor(RGB color)
    {
        assert color != null;
        backgroundColor = color;
    }

    @Override
    public void setFrameRate(float frameRate)
    {
        header.setFrameRate(frameRate);
    }

    @Override
    public void setFrameSize(Rect rect)
    {
        header.setFrameSize(rect);
    }

    @Override
    public void setMetadata(String value)
    {
        if (value == null)
        {
            metadata = null;
            fileAttributes.setHasMetadata(false);
        }
        else
        {
            metadata = new MetadataTag(value);
            fileAttributes.setHasMetadata(true);
        }
    }

    @Override
    public void setTopLevelClass(String value)
    {
        topLevelClass = value;
    }

    @Override
    public void setUseDirectBlit(boolean value)
    {
        fileAttributes.setUseDirectBlit(value);
    }

    @Override
    public void setUseGPU(boolean value)
    {
        fileAttributes.setUseGPU(value);
    }

    @Override
    public void setUseNetwork(boolean value)
    {
        fileAttributes.setUseNetwork(value);
    }

    @Override
    public void setVersion(int version)
    {
        header.setVersion(version);
    }

    @Override
    public boolean getUseDirectBlit()
    {
        return fileAttributes.isUseDirectBlit();
    }

    @Override
    public boolean getUseGPU()
    {
        return fileAttributes.isUseGPU();
    }

    @Override
    public boolean getUseNetwork()
    {
        return fileAttributes.isUseNetwork();
    }

    @Override
    public void deleteFrame(int index)
    {
        frames.remove(index);
    }

    public void setEnableDebugger2(EnableDebugger2Tag tag)
    {
        this.enableDebugger2 = tag;
    }

    @Override
    public EnableDebugger2Tag getEnableDebugger2()
    {
        return enableDebugger2;
    }

    @Override
    public void setScriptLimits(int maxRecursionDepth, int scriptTimeoutSeconds)
    {
        // TODO: check for value range and report problem or clamp value
        // They should both be UI16.
        scriptLimits = new ScriptLimitsTag(maxRecursionDepth, scriptTimeoutSeconds);
    }

    @Override
    public ScriptLimitsTag getScriptLimits()
    {
        return scriptLimits;
    }

    @Override
    public ProductInfoTag getProductInfo()
    {
        return productInfoTag;
    }

    @Override
    public void setProductInfo(ProductInfoTag tag)
    {
        this.productInfoTag = tag;
    }

    @Override
    public EnableTelemetryTag getEnableTelemetry() {
        return enableTelemetry;
    }

    @Override
    public void setEnableTelemetry(EnableTelemetryTag tag) {
        this.enableTelemetry = tag;
    }

}
