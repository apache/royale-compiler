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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>ExportAssets</code> tag in a SWF file.
 * <p>
 * The ExportAssets tag makes portions of a SWF file available for import by
 * other SWF files. Each exported character is identified by a string. Any type
 * of character can be exported.
 * <p>
 * If the value of the character in ExportAssets was previously exported with a
 * different identifier, Flash Player associates the tag with the latter
 * identifier. That is, if Flash Player has already read a given value for Tag1
 * and the same Tag1 value is read later in the SWF file, the second Name1 value
 * is used.
 * <p>
 * The map between character tag and name is implemented in a BiMap fashion. A
 * character can only be exported with one name; and a name can only be used to
 * export one character tag.
 * <p>
 * <b>Note:</b> ExportAssets tag is only used in ActionScript 1 and 2 only. In
 * ActionScript3, ExportAssets is ignored. SymbolClass is used instead.
 */
public class ExportAssetsTag extends Tag implements IManagedTag, ICharacterReferrer
{
    /**
     * Constructor.
     */
    public ExportAssetsTag()
    {
        super(TagType.ExportAssets);
        exports = new LinkedHashMap<String, ICharacterTag>();
    }

    // Associate name with symbols.
    private final Map<String, ICharacterTag> exports;

    /**
     * Get all the symbol names.
     * 
     * @return symbol names
     */
    public Set<String> getCharacterNames()
    {
        return exports.keySet();
    }

    /**
     * Get a character tag by its assigned name.
     * 
     * @param name symbol name
     * @return character tag
     */
    public ICharacterTag getCharacterTagByName(String name)
    {
        return exports.get(name);
    }

    /**
     * Export character tag with the given name.
     * <ul>
     * <li>If a character is exported multiple time with different name, the
     * last name wins.</li>
     * <li>If a name is used to export multiple character tags, the last
     * character tag gets the name, and the previous tags will not be exported.</li>
     * </ul>
     * 
     * @param characterTag character tag
     * @param name symbol name
     */
    public void addExport(ICharacterTag characterTag, String name)
    {
        assert characterTag != null;
        removeExport(characterTag);
        exports.put(name, characterTag);
    }

    /**
     * Find name by character tag.
     * 
     * @param characterTag character tag
     * @return the name used to export the character tag.
     */
    private final String findNameByCharacterTag(ICharacterTag characterTag)
    {
        String name = null;
        for (final String key : exports.keySet())
        {
            if (exports.get(key) == characterTag)
            {
                name = key;
                break;
            }
        }
        return name;
    }

    /**
     * Remove a character tag from the exported tags by reference.
     * 
     * @param characterTag tags to remove
     */
    public void removeExport(ICharacterTag characterTag)
    {
        final String name = findNameByCharacterTag(characterTag);
        if (name != null)
        {
            exports.remove(name);
        }
    }

    /**
     * Remove a character tag from the exported tags by name.
     * 
     * @param name name of the tag to remove
     */
    public void removeExport(String name)
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

    @Override
    public String description()
    {
        final StringBuilder buffer = new StringBuilder();
        for (final String name : exports.keySet())
        {
            final ICharacterTag tag = exports.get(name);
            buffer.append(String.format("(%s:%s#%d) ", name, tag.getTagType(), tag.getCharacterID()));
        }
        return buffer.toString();
    }

    /**
     * Get all the referred character tags.
     * 
     * @return an iterator of referred character tags
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return exports.values();
    }
}
