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

import org.apache.royale.swf.tags.EnableDebugger2Tag;
import org.apache.royale.swf.tags.EnableTelemetryTag;
import org.apache.royale.swf.tags.ProductInfoTag;
import org.apache.royale.swf.tags.ScriptLimitsTag;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.Rect;

/**
 * SWF model interface. The properties include fields in SWF header,
 * {@code FileAttributes} tag and optionally {@code Metadata} tag.
 */
public interface ISWF
{
    /**
     * Get SWF file version.
     * 
     * @return version number.
     */
    int getVersion();

    /**
     * Set SWF version.
     * 
     * @param version SWF version
     */
    void setVersion(int version);

    /**
     * Get frame delay in 8.8 fixed number of frames per second.
     * 
     * @return frame delay.
     */
    float getFrameRate();

    /**
     * Set frame rate.
     * 
     * @param frameRate frame per second
     */
    void setFrameRate(float frameRate);

    /**
     * Get SWF frame size in Twips.
     * 
     * @return frame size
     */
    Rect getFrameSize();

    /**
     * Set frame size.
     * 
     * @param rect frame size
     */
    void setFrameSize(Rect rect);

    /**
     * Get number of frames.
     * 
     * @return frame count.
     */
    int getFrameCount();

    // Frame operations

    /**
     * Add a frame.
     * 
     * @param frame SWF frame.
     */
    void addFrame(SWFFrame frame);

    /**
     * Get frame by index.
     * 
     * @param index frame index.
     * @return frame
     */
    SWFFrame getFrameAt(int index);

    // Properties in managed tags

    /**
     * Get SWF movie background color.
     * 
     * @return color
     */
    RGB getBackgroundColor();

    /**
     * Set SWF movie background color.
     * 
     * @param color color in RGB
     */
    void setBackgroundColor(RGB color);

    /**
     * Get the QName of the top level class.
     * 
     * @return name of top level class.
     */
    String getTopLevelClass();

    /**
     * Set root class name.
     * 
     * @param value root class name
     */
    void setTopLevelClass(String value);

    /**
     * Get the Metadata text. This is a Metadata property.
     * <p>
     * The Metadata tag is an optional tag to describe the SWF file to an
     * external process. The tag embeds XML metadata in the SWF file so that,
     * for example, a search engine can locate this tag, access a title for the
     * SWF file, and display that title in search results.
     * 
     * @return metadata XML text; null if Metadata tag doesn't exit.
     */
    String getMetadata();

    /**
     * Set the Metadata text. This is a Metadata property.
     * <p>
     * The Metadata tag is an optional tag to describe the SWF file to an
     * external process. The tag embeds XML metadata in the SWF file so that,
     * for example, a search engine can locate this tag, access a title for the
     * SWF file, and display that title in search results.
     * <p>
     * Setting a non-null value will also set FileAttributes.hasMetadata to
     * true.
     * 
     * @param value Metadata XML string; null value will remove the Metadata
     * tag on the SWF.
     */
    void setMetadata(String value);

    /**
     * Check if the SWF has Metadata tag. This is a FileAttributes property.
     * 
     * @return true if the SWF has Metadata tag
     */
    boolean hasMetadata();

    /**
     * Check if the SWF uses ActionScript3. This is a FileAttributes property.
     * 
     * @return true if the SWF uses ActionScript3
     */
    boolean getUseAS3();

    /**
     * Set whether the SWF uses ActionScript3. This is a FileAttributes
     * property.
     * 
     * @param value true if the SWF uses ActionScript3
     */
    void setUseAS3(boolean value);

    /**
     * Check if the SWF uses direct Blit. This is a FileAttributes property.
     * 
     * @return true if the SWF uses direct Blit.
     */
    boolean getUseDirectBlit();

    /**
     * Set whether the SWF uses direct Blit. This is a FileAttributes property.
     * 
     * @param value true if the SWF uses direct Blit.
     */
    void setUseDirectBlit(boolean value);

    /**
     * Check if the SWF uses GPU. This is a FileAttributes property.
     * 
     * @return true if the SWF uses GPU
     */
    boolean getUseGPU();

    /**
     * Set whether the SWF uses GPU. This is a FileAttributes property.
     * 
     * @param value true if the SWF uses GPU
     */
    void setUseGPU(boolean value);

    /**
     * Set whether the SWF uses network. This is a FileAttributes property.
     * 
     * @return true if the SWF uses network.
     */
    boolean getUseNetwork();

    /**
     * Check if the SWF uses network. This is a FileAttributes property.
     * 
     * @param value true if the SWF uses network.
     */
    void setUseNetwork(boolean value);

    /**
     * Delete a SWF frame.
     * 
     * @param index frame index
     */
    void deleteFrame(int index);

    /**
     * @return {@code EnableDebugger2} tag or null.
     */
    EnableDebugger2Tag getEnableDebugger2();

    /**
     * Set a script limit values.
     * 
     * @param maxRecursionDepth max recursion depth
     * @param scriptTimeoutSeconds script timeout seconds
     */
    void setScriptLimits(int maxRecursionDepth, int scriptTimeoutSeconds);

    /**
     * Get the managed {@code ScriptLimits} tag.
     * 
     * @return {@code ScriptLimits} tag in this SWF, or {@code null} if
     * {@link #setScriptLimits(int, int)} hasn't be called.
     */
    ScriptLimitsTag getScriptLimits();

    /**
     * Get the {@code ProductInfoTag} tag.
     * 
     * @return {@code ProductInfoTag} or null if there is no product info
     * associated with the SWF.
     */
    ProductInfoTag getProductInfo();

    /**
     * Set the product info tag associated with the SWF.
     * 
     * @param tag the ProductInfoTag, may be null.
     */
    void setProductInfo(ProductInfoTag tag);

    /**
     * Get the {@code EnableTelemetryTag} tag.
     *
     * @return {@code EnableTelemetryTag} or null if there is no enable telemetry
     * information associated with the SWF.
     */
    EnableTelemetryTag getEnableTelemetry();

    /**
     * Set the enable telemetry tag associated with the SWF.
     *
     * @param tag the EnableTelemetryTag, may be null.
     */
    void setEnableTelemetry(EnableTelemetryTag tag);

}
