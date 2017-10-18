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

import com.google.common.base.MoreObjects;

/**
 * <h3>Background</h3> Imagine an ActionScript source as a character stream
 * (include directives are expanded and merged into the main file), each
 * character's offset number is an "absolute" offset. It differs from the
 * "local" offset of a character in an included file.
 * <p>
 * For example, when "version.as" is included in "Button.as" at offset 10 in
 * "Button.as", the first character in "version.as" has local offset "0", but
 * its "absolute" offset in "Button.as" is "10". Suppose keyword {@code var} is
 * after the {@code include} statement, and its "local" offset is "20", since
 * "version.as" has 100 characters in it, the "absolute" offset of {@code var}
 * becomes "120".
 * <p>
 * <h3>Computing absolute offsets</h3> The {@code OffsetCue} objects are markers
 * on the "absolute" offset axis. It records the adjustment between an "local"
 * offset and its "absolute" offset.
 * <p>
 * The formula is:<br>
 * <code>local = absolute - adjustment</code>
 * <p>
 * The JFlex-generated lexer sets the local offset when it tokenizes a file.
 * Meanwhile, {@code IncludeHandler} records the "adjustment" to compute the
 * "absolute" offset.
 * <p>
 * Besides offset adjustment, an {@code OffsetCue} also records the name of the
 * file the character is from.
 * <p>
 * <h3>Example</h3> Suppose "main.as" includes "A.as"; "A.as" includes "B.as".
 * <p>
 * Imagine the following character stream and the tuples inside parentheses are
 * {@code OffsetCue}.
 * 
 * <pre>
 * --------->        ----------->  ----------->  ------>     ----------->
 * (0:main.as)_______(2:A.as)______(4:B.as)______(6:A.as)____(7:main.as)_______
 * [local]     0   1          0  1         0 1 2 3         2            2  3
 * [absolute]  0   1          2  3         4 5 6 7         8            9  10
 * [file] main.as    in "A"         in "B"        back to "A"   back to "main"
 * </pre>
 * 
 * <ol>
 * <li>{@code 0:main.as} - the root file.</li>
 * <li>{@code 2:A.as} - main.as includes A.as. Local offset "1" in A.as is
 * absolute offset "3" in parsing main.as, because the adjustment is "2";</li>
 * <li>{@code 4:B.as} - A.as includes B.as. Local offset "0" in B.as is absolute
 * offset "4" in parsing main.as, because the adjustment is "4"</li>
 * <li>{@code 6:A.as} - return to A.as from B.as. The adjustments change because
 * characters in B.as advance absolute offsets.</li>
 * <li>{@code 7:main.as} - return to main.as from A.as.</li>
 * </ol>
 * An {@code OffsetCue} applies to all the characters/tokens after itself
 * (inclusive) until the next {@code OffsetCue} (exclusive).
 * <p>
 * {@code OffsetCue} is an immutable class. It is created in
 * {@link IncludeHandler}.
 */
public class OffsetCue
{
    protected OffsetCue(String filename, int absolute, int adjustment)
    {
        assert filename != null : "filename can't be null";
        assert absolute >= 0 : "absolute offset can't be negative";
        assert adjustment >= 0 : "adjustment can't be negative";

        this.filename = filename;
        this.absolute = absolute;
        this.adjustment = adjustment;
        this.local = absolute - adjustment;
    }

    /**
     * File that contains the characters after this cue point until the next cue
     * point.
     */
    public final String filename;

    /**
     * Adjustment between absolute offset and local offset.<br>
     * <code>local = absolute - adjustment</code>
     */
    public final int adjustment;

    /**
     * Absolute offset.
     */
    public final int absolute;

    /**
     * Local offset.
     */
    public final int local;

    @Override
    public String toString()
    {
        return MoreObjects
                .toStringHelper(this)
                .add("absolute", absolute)
                .add("adjustment", adjustment)
                .add("local", local)
                .add("filename", filename)
                .toString();
    }
}
