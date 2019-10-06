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

package org.apache.royale.compiler.internal.parsing.as;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 * Tokens generated from {@link StreamingASTokenizer} has been altered so that
 * their start and end offsets are absolute offsets.
 * <P>
 * This class can translate an "absolute" offset to a tuple of filename and
 * "local offset" in the file. Each ASFileScope has an
 * {@code OffsetLookup} object.
 */
public class OffsetLookup
{
    /**
     * Comparator to sort {@code OffetCue} by absolute offset in ascending
     * order.
     */
    private static final Comparator<OffsetCue> OFFSET_CUE_COMPARATOR =
            new Comparator<OffsetCue>()
    {
        @Override
        public int compare(OffsetCue o1, OffsetCue o2)
        {
            return o1.absolute - o2.absolute;
        }
    };

    /**
     * This is used to sort the immutable list and do binary-search.
     */
    private static final Ordering<OffsetCue> ORDER_BY_ABSOLUTE =
            Ordering.from(OFFSET_CUE_COMPARATOR);

    /**
     * Create a wrapper {@code OffsetCue} object in for binary-searching the
     * {@link #offsetCueList}
     * 
     * @param absolute absolute offset
     * @return The {@code OffsetCue} that applies to the given absolute offset.
     */
    private static OffsetCue createSearchKey(int absolute)
    {
        return new OffsetCue("", absolute, 0);
    }

    public OffsetLookup(final ImmutableList<OffsetCue> offsetCueList)
    {
        assert offsetCueList != null : "Offset cue list can't be null.";
        assert ORDER_BY_ABSOLUTE.isOrdered(offsetCueList) : "Offset cue list should be sorted by absolute offset.";
        // For now the creation of offsetCueList guarantees the ordering of the
        // list, so we needn't do a sorted immutable copy.
        this.offsetCueList = offsetCueList;
    }

    private final ImmutableList<OffsetCue> offsetCueList;

    /**
     * Find the nearest {@link OffsetCue} before the given absolute offset
     * 
     * @param absoluteOffset absolute offset
     * @return The {@code OffsetCue} that applies to the given absolute offset.
     */
    @SuppressWarnings("deprecation")
	private OffsetCue findOffsetCue(int absoluteOffset)
    {
        if (offsetCueList.isEmpty() || absoluteOffset < 0)
            return null;

        final OffsetCue key = createSearchKey(absoluteOffset);
        final int index = ORDER_BY_ABSOLUTE.binarySearch(offsetCueList, key);

        // The return value of "binarySearch" is either the index of the found
        // item, or (-(insertion point) - 1).
        if (index >= 0)
        {
            return offsetCueList.get(index);
        }
        else
        {
            final int insertionPoint = -(index + 1);
            return offsetCueList.get(insertionPoint - 1);
        }

    }

    /**
     * Get the name of the file that contains the given absolute offset.
     * 
     * @param absoluteOffset absolute offset
     * @return Name of the file that contains the given absolute offset.
     */
    public String getFilename(final int absoluteOffset)
    {
        final OffsetCue result = findOffsetCue(absoluteOffset);
        if (result == null)
            return null;
        else
            return result.filename;
    }

    /**
     * Translate absolute offset to local offset.
     * 
     * @param absoluteOffset absolute offset
     * @return Local offset.
     */
    public int getLocalOffset(final int absoluteOffset)
    {
        final OffsetCue result = findOffsetCue(absoluteOffset);
        if (result == null)
            return absoluteOffset;
        else
            return absoluteOffset - result.adjustment;
    }

    /**
     * Convert a local offset to an absolute offset. If a file is included more
     * than once, there would be more than one absolute offset corresponding to
     * a given local offset within that included file.
     * 
     * @param filename Normalized path of the file the local offset is in.
     * @param localOffset Local offset.
     * @return Absolute offsets corresponding to the given local offset.
     */
    public int[] getAbsoluteOffset(final String filename, final int localOffset)
    {
        if (offsetCueList.isEmpty())
        {
            return new int[] {localOffset};
        }

        assert filename != null : "Filename can't be null.";
        if (localOffset < 0)
            return new int[] {localOffset};

        // OffsetCue objects in the given file.
        final Iterable<OffsetCue> fileOffsetCueList =
                Iterables.filter(offsetCueList, new Predicate<OffsetCue>()
                {
                    @Override
                    public boolean apply(OffsetCue cue)
                    {
                        return cue.filename.equals(filename);
                    }
                    
                    @Override
                    public boolean test(OffsetCue input)
                    {
                        return apply(input);
                    }
                });

        // Find a list of OffsetCues before the local offset.
        final List<OffsetCue> candidateCues = new ArrayList<OffsetCue>(1);
        OffsetCue candidate = null;
        for (final OffsetCue cue : fileOffsetCueList)
        {
            if (cue.local <= localOffset)
            {
                candidate = cue;
            }
            else if (candidate != null)
            {
                candidateCues.add(candidate);
                candidate = null;
            }
        }

        // There is no OffsetCue to end and included file. 
        // The last one needs to be manually added.
        if (candidate != null)
            candidateCues.add(candidate);

        if (candidateCues.isEmpty())
        {
            throw new IllegalArgumentException(
                    "Local offset '" + localOffset + "' is not in file " + filename);
        }

        // Collect the absolute offsets.
        final int matchSize = candidateCues.size();
        final int[] absoluteOffsets = new int[matchSize];
        for (int i = 0; i < matchSize; i++)
        {
            final OffsetCue cue = candidateCues.get(i);
            absoluteOffsets[i] = cue.absolute + (localOffset - cue.local);
        }
        return absoluteOffsets;
    }

    @Override
    public String toString()
    {
        return Joiner.on("\n").join(offsetCueList);
    }

    /**
     * Determines if there are any includes.
     * 
     * @return true if there are any included files, false otherwise.
     */
    public boolean hasIncludes()
    {
        // If there are no Cue's then there are no includes.
        // If there is only one Cue, then there are no includes.
        return offsetCueList.size() > 1;
    }
}
