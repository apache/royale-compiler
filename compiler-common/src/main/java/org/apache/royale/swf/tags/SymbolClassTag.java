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

import java.util.Set;

import org.apache.royale.swf.TagType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Represents a <code>SymbolClass</code> tag in a SWF file.
 * <p>
 * The SymbolClass tag creates associations between symbols in the SWF file and
 * ActionScript 3.0 classes. It is the ActionScript 3.0 equivalent of the
 * ExportAssets tag. If the character ID is zero, the class is associated with
 * the main time-line of the SWF. This is how the root class of a SWF is
 * designated. Classes listed in the SymbolClass tag are available for creation
 * by other SWF files (see StartSound2, DefineEditText (HasFontClass), and
 * PlaceObject3 (PlaceFlagHasClassName and PlaceFlagHasImage). For example, ten
 * SWF files that are all part of the same web site can share an embedded custom
 * font if one file embeds and exports the font class.
 */
public final class SymbolClassTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     */
    public SymbolClassTag()
    {
        super(TagType.SymbolClass);
        exports = HashBiMap.create();
    }

    // Associate name with symbols.
    private final BiMap<String, ICharacterTag> exports;

    @Override
    public String description()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("total=%d; ", size()));
        for (final String name : exports.keySet())
        {
            final ICharacterTag tag = exports.get(name);
            buffer.append(String.format("(%s:%s#%d) ", name, tag.getTagType(), tag.getCharacterID()));
        }
        return buffer.toString();
    }

    /**
     * Get all the symbol names.
     * 
     * @return symbol names
     */
    public Set<String> getSymbolNames()
    {
        return exports.keySet();
    }

    /**
     * Find a symbol by name.
     * 
     * @param name symbol name
     * @return character tag
     */
    public ICharacterTag getSymbol(String name)
    {
        return exports.get(name);
    }

    /**
     * Register a symbol with the given name.
     * <ul>
     * <li>If a symbol is registered multiple time with different name, the last
     * name wins.</li>
     * <li>If a name is used to register multiple symbols, the last symbol gets
     * the name, and the previous symbols will not be exported.</li>
     * </ul>
     * 
     * @param characterTag character tag
     * @param name symbol name
     */
    public void addSymbol(ICharacterTag characterTag, String name)
    {
        assert characterTag != null;
        assert name != null;
        assert !"".equals(name);

        removeSymbol(characterTag);
        exports.forcePut(name, characterTag);
    }

    /**
     * Get symbol name.
     * 
     * @param characterTag character tag
     * @return the name used to export the character tag.
     */
    public final String getSymbolName(ICharacterTag characterTag)
    {
        return exports.inverse().get(characterTag);
    }

    /**
     * Remove a symbol.
     * 
     * @param characterTag tags to remove
     */
    public void removeSymbol(ICharacterTag characterTag)
    {
        exports.inverse().remove(characterTag);
    }

    /**
     * Remove a symbol.
     * 
     * @param name name of the tag to remove
     */
    public void removeSymbol(String name)
    {
        exports.remove(name);
    }

    /**
     * Get the total number of exported tags.
     * 
     * @return count of exported tags
     */
    public int size()
    {
        return exports.size();
    }
}
