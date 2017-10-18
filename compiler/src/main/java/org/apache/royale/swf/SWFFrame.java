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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.ExportAssetsTag;
import org.apache.royale.swf.tags.FrameLabelTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.IManagedTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.ShowFrameTag;
import org.apache.royale.swf.tags.SymbolClassTag;

/**
 * SWF frame model manages a list of SWF tags. For the "managed" tags,
 * {@code SWFFrame} provides API to access their data. Other tags are grouped by
 * types and are stored in lists.
 * <p>
 * {@code SWFFrame} is a {@link ITagContainer}. The enclosed tags are not stored
 * in a one-dimensional array, however, they can be iterated in a deterministic
 * order.
 */
public class SWFFrame implements ITagContainer
{
    /**
     * Constructor.
     */
    public SWFFrame()
    {
        unfiledTags = new ArrayList<ITag>();
        doABCTags = new ArrayList<DoABCTag>();
        characterTags = new LinkedHashSet<ICharacterTag>();
    }
    
    // TODO: remove this generic tag container
    private final List<ITag> unfiledTags;

    private final List<DoABCTag> doABCTags;
    private final LinkedHashSet<ICharacterTag> characterTags;
    protected SymbolClassTag symbolClass;
    protected ExportAssetsTag exportAssetsTag;
    protected FrameLabelTag frameLabel;

    /**
     * Add a SWF tag to the frame.
     * 
     * @param tag tag
     */
    public void addTag(ITag tag)
    {
        if (tag == null)
            throw new NullPointerException("can't not add null to SWF frame");

        if (tag.getTagType() == TagType.Undefined)
            return; // ignore undefined tags

        if (tag instanceof IManagedTag)
            throw new IllegalArgumentException("Can't add a managed tag to frame.");

        if (tag.getTagType() == TagType.DoABC)
        {
            doABCTags.add((DoABCTag)tag);
        }
        else if (tag instanceof ICharacterTag)
        {
            characterTags.add((ICharacterTag)tag);
        }
        else
        {
            unfiledTags.add(tag);
        }
    }

    /**
     * Export an {@code ICharacterTag} with {@code name}. The character tag has
     * to be added to the frame before it is defined as a symbol.
     * 
     * @param tag character tag
     * @param name export name
     */
    public void defineSymbol(ICharacterTag tag, String name)
    {
        defineSymbol(tag, name, null);
    }

    /**
     * Export an {@code ICharacterTag} with {@code name}. The character tag has
     * to be added to the frame before it is defined as a symbol.
     * 
     * @param tag character tag
     * @param name export name
     * @param dictionary Used when reading an existing SWF. If null, only the
     * character tags in the current frame will be checked.
     */
    public void defineSymbol(ICharacterTag tag, String name,
            Map<Integer, ICharacterTag> dictionary)
    {
        assert tag != null;
        assert name != null;

        if (!(dictionary != null && dictionary.get(tag.getCharacterID()) != null) &&
             !characterTags.contains(tag))
        {
            throw new IllegalStateException("Tag not added to the frame yet: " + tag);
        }

        getSymbolClass().addSymbol(tag, name);
    }

    /**
     * Get the SymbolClass tag of the current frame. This method helps
     * lazy-initialize the tag.
     * 
     * @return SymbolClass tag object.
     */
    private SymbolClassTag getSymbolClass()
    {
        if (symbolClass == null)
            symbolClass = new SymbolClassTag();

        return symbolClass;
    }

    /**
     * Get the symbol name of a class. The symbol class needs to be read
     * before this can be queried.
     * 
     * @param tag A character tag.
     * @return The name of the tag in the dictionary. Null if the symbol 
     * table has not been read or the tag is not defined in this frame.
     */
    public String getSymbolName(ICharacterTag tag)
    {
        if (symbolClass == null)
            return null;
        
        return symbolClass.getSymbolName(tag);
    }
    
    /**
     * Export an {@code ICharacterTag} with {@code name}. The character tag has
     * to be added to the frame before it is defined as an export asset.
     * 
     * @param tag character tag
     * @param name export name
     */
    public void defineExport(ICharacterTag tag, String name)
    {
        assert tag != null;
        assert name != null;

        if (!characterTags.contains(tag))
            throw new IllegalStateException("Tag not added to the frame yet: " + tag);

        if (exportAssetsTag == null)
            exportAssetsTag = new ExportAssetsTag();

        exportAssetsTag.addExport(tag, name);
    }

    /**
     * Get an iterator of all the tags in the frame. This method combines all
     * the managed and not managed tags into one ordered collection.
     */
    @Override
    public Iterator<ITag> iterator()
    {
        final List<ITag> orderedTags = new ArrayList<ITag>();

        // frame label
        if (frameLabel != null)
            orderedTags.add(frameLabel);

        // TODO: imports

        // ABC tags
        orderedTags.addAll(doABCTags);

        // character tags
        orderedTags.addAll(characterTags);

        // symbol class
        if (symbolClass != null)
            orderedTags.add(symbolClass);

        // export assets
        if (exportAssetsTag != null)
            orderedTags.add(exportAssetsTag);

        // TODO: control tags

        // TODO: frame actions

        orderedTags.addAll(unfiledTags);

        // show frame
        orderedTags.add(new ShowFrameTag());

        return orderedTags.iterator();
    }

    /**
     * Assign a name to this frame.
     * 
     * @param name frame name
     * @param isAnchor true if the frame name is a named anchor tag
     */
    public void setName(String name, boolean isAnchor)
    {
        assert name != null;
        assert !"".equals(name);

        if (frameLabel == null)
            frameLabel = new FrameLabelTag(name);
        
        frameLabel.setNamedAnchorTag(isAnchor);
    }

    /**
     * Get frame name.
     * 
     * @return frame name
     */
    public String getName()
    {
        return frameLabel == null ? null : frameLabel.getName();
    }

    /**
     * Check if this frame has a named anchor tag.
     * <p>
     * In SWF files of version 6 or later, an extension to the FrameLabel tag
     * called named anchors is available. A named anchor is a special kind of
     * frame label that, in addition to labeling a frame for seeking using
     * {@code ActionGoToLabel}, labels the frame for seeking using HTML anchor
     * syntax.
     * 
     * @return true if this frame uses a named anchor tag.
     */
    public boolean hasNamedAnchor()
    {
        return frameLabel == null ? false : frameLabel.isNamedAnchorTag();
    }

    /**
     * Force creating a SymbolClass tag on the frame.
     * 
     * @param frame SWF frame
     */
    public static final void forceSymbolClassTag(SWFFrame frame)
    {
        frame.getSymbolClass();
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return Collections.emptyList();
    }
}
