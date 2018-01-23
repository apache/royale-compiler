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
 * Represents a <code>SetTabIndex</code> tag in a SWF file.
 * <p>
 * Flash Player maintains a concept of tab order of the interactive and textual
 * objects displayed. Tab order is used both for actual tabbing and, in SWF 6
 * and later, to determine the order in which objects are exposed to
 * accessibility aids (such as screen readers). The SWF 7 SetTabIndex tag sets
 * the index of an object within the tab order.
 * <p>
 * If no character is currently placed at the specified depth, this tag is
 * ignored.
 * <p>
 * You can also use using the ActionScript tabIndex property to establish tab
 * ordering, but this does not provide a way to set a tab index for a static
 * text object, because the player does not provide a scripting reflection of
 * static text objects. Fortunately, this is not a problem for the purpose of
 * tabbing, because static text objects are never actually tab stops. However,
 * this is a problem for the purpose of accessibility ordering, because static
 * text objects are exposed to accessibility aids. When generating SWF content
 * that is intended to be accessible and contains static text objects, the
 * SetTabIndex tag is more useful than the tabIndex property.
 */
public class SetTabIndexTag extends Tag
{
    /**
     * Constructor.
     */
    public SetTabIndexTag()
    {
        super(TagType.SetTabIndex);
    }

    private int depth;
    private int tabIndex;

    /**
     * @return the depth
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    /**
     * @return the tabIndex
     */
    public int getTabIndex()
    {
        return tabIndex;
    }

    /**
     * @param tabIndex the tabIndex to set
     */
    public void setTabIndex(int tabIndex)
    {
        this.tabIndex = tabIndex;
    }
}
