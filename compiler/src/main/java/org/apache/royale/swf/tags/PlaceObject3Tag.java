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
import org.apache.royale.swf.types.Filter;

/**
 * Represents a <code>PlaceObject3</code> tag in a SWF file.
 * <p>
 * The PlaceObject3 tag extends the functionality of the PlaceObject2 tag.
 * PlaceObject3 adds the following new features:
 * <ul>
 * <li>The PlaceFlagHasClassName field indicates that a class name will be
 * specified, indicating the type of object to place. Because we no longer use
 * ImportAssets in ActionScript 3.0, there needed to be some way to place a
 * Timeline object using a class imported from another SWF, which does not have
 * a 16-bit character ID in the instantiating SWF. Supported in Flash Player
 * 9.0.45.0 and later.</li>
 * <li>The PlaceFlagHasImage field indicates the creation of native Bitmap
 * objects on the display list. When PlaceFlagHasClassName and PlaceFlagHasImage
 * are both defined, this indicates a Bitmap class to be loaded from another
 * SWF. Immediately following the flags is the class name (as above) for the
 * BitmapData class in the loaded SWF. A Bitmap object will be placed with the
 * named BitmapData class as it's internal data. When PlaceFlagHasCharacter and
 * PlaceFlagHasImage are both defined, this indicates a Bitmap from the current
 * SWF. The BitmapData to be used as its internal data will be defined by the
 * following characterID. This only occurs when the BitmapData has a class
 * associated with it. If there is no class associated with the BitmapData,
 * DefineShape should be used with a Bitmap fill. Supported in Flash Player
 * 9.0.45.0 and later.</li>
 * <li>The PlaceFlagHasCacheAsBitmap field specifies whether Flash Player should
 * internally cache a display object as a bitmap. Caching can speed up rendering
 * when the object does not change frequently.</li>
 * <li>A number of different blend modes can be specified as an alternative to
 * normal alpha compositing.</li>
 * <li>A number of bitmap filters can be applied to the display object. Adding
 * filters implies that the display object will be cached as a bitmap.</li>
 * </ul>
 */
public class PlaceObject3Tag extends PlaceObject2Tag
{
    /**
     * Constructor.
     */
    public PlaceObject3Tag()
    {
        super(TagType.PlaceObject3);
    }

    private boolean hasImage;
    private boolean hasClassName;
    private boolean hasCacheAsBitmap;
    private boolean hasBlendMode;
    private boolean hasFilterList;
    private String className;
    private Filter[] surfaceFilterList;
    private int blendMode;
    private int bitmapCache;

    /**
     * @return the hasImage
     */
    public boolean isHasImage()
    {
        return hasImage;
    }

    /**
     * @param hasImage the hasImage to set
     */
    public void setHasImage(boolean hasImage)
    {
        this.hasImage = hasImage;
    }

    /**
     * @return the hasCacheAsBitmap
     */
    public boolean isHasCacheAsBitmap()
    {
        return hasCacheAsBitmap;
    }

    /**
     * @param hasCacheAsBitmap the hasCacheAsBitmap to set
     */
    public void setHasCacheAsBitmap(boolean hasCacheAsBitmap)
    {
        this.hasCacheAsBitmap = hasCacheAsBitmap;
    }

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
     * @return the surfaceFilterList
     */
    public Filter[] getSurfaceFilterList()
    {
        return surfaceFilterList;
    }

    /**
     * @param surfaceFilterList the surfaceFilterList to set
     */
    public void setSurfaceFilterList(Filter[] surfaceFilterList)
    {
        this.surfaceFilterList = surfaceFilterList;
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

    /**
     * @return the bitmapCache
     */
    public int getBitmapCache()
    {
        return bitmapCache;
    }

    /**
     * @param bitmapCache the bitmapCache to set
     */
    public void setBitmapCache(int bitmapCache)
    {
        this.bitmapCache = bitmapCache;
    }

    /**
     * @return the hasClassName
     */
    public boolean isHasClassName()
    {
        return hasClassName;
    }

    /**
     * @param hasClassName the hasClassName to set
     */
    public void setHasClassName(boolean hasClassName)
    {
        this.hasClassName = hasClassName;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }
}
