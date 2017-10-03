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

package org.apache.royale.compiler.internal.fxg.swf;

import org.apache.royale.swf.tags.IDefineBinaryImageTag;

/**
 * A container class for any kind of binary image tag with a height and width.
 */
public class DefineImage
{
    private IDefineBinaryImageTag tag;
    private int width;
    private int height;
    public DefineImage(IDefineBinaryImageTag tag, int width, int height)
    {
        this.tag = tag;
        this.setWidth(width);
        this.setHeight(height);
    }
    
    public IDefineBinaryImageTag getTag()
    {
        return tag;
    }
    public void setWidth(int width)
    {
        this.width = width;
    }
    public int getWidth()
    {
        return width;
    }
    public void setHeight(int height)
    {
        this.height = height;
    }
    public int getHeight()
    {
        return height;
    }
}
