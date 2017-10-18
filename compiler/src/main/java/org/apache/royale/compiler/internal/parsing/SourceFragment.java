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

package org.apache.royale.compiler.internal.parsing;

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * This class is an implementation of the {@code ISourceFragment} interface.
 * It also adds the ability to split a fragment at a list of character indices
 * within the logical text.
 */
public class SourceFragment implements ISourceFragment
{
    public SourceFragment(String physicalText, String logicalText,
                          int physicalStart, int physicalLine, int physicalColumn)
    {
        this.physicalText = physicalText;
        this.logicalText = logicalText;
        this.physicalStart = physicalStart;
        this.physicalLine = physicalLine;
        this.physicalColumn = physicalColumn;
    }
    
    public SourceFragment(String physicalText, String logicalText,
                          ISourceLocation location)
    {
        this(physicalText, logicalText,
             location.getStart(), location.getLine(), location.getColumn());
    }
    
    public SourceFragment(String text, ISourceLocation location)
    {
        this(text, text, location);
    }

    private String physicalText;
    private String logicalText;
    private int physicalStart;
    private int physicalLine;
    private int physicalColumn;

    @Override
    public String getPhysicalText()
    {
        return physicalText;
    }

    @Override
    public String getLogicalText()
    {
        return logicalText;
    }

    @Override
    public int getPhysicalStart()
    {
        return physicalStart;
    }

    /**
     * Sets the physical starting offset.
     * <p>
     * This is used for mapping it from a local to an absolute offset,
     * when we account for include statements and source attributes.
     */
    public void setPhysicalStart(int physicalStart)
    {
        this.physicalStart = physicalStart;
    }
    
    @Override
    public int getPhysicalLine()
    {
        return physicalLine;
    }

    @Override
    public int getPhysicalColumn()
    {
        return physicalColumn;
    }
    
    /**
     * Returns a subfragment from the fragment.
     * 
     * @param beginIndex The beginning character index within the logical text.
     * 
     * @param endIndex The ending character index within the logical txext.
     * 
     * @return The resulting subfragment.
     */
    public ISourceFragment subfragment(int beginIndex, int endIndex)
    {
        assert 0 <= beginIndex && beginIndex <= logicalText.length() : "Invalid beginIndex";
        assert 0 <= endIndex && endIndex <= logicalText.length() : "Invalid endIndex";
        
        if (beginIndex == endIndex)
            return null;
        
        return new SourceFragment(
            physicalText.substring(beginIndex, endIndex),
            logicalText.substring(beginIndex, endIndex),
            physicalStart + beginIndex,
            physicalLine,
            physicalColumn + beginIndex);
    }
    
    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return "\"" + physicalText + "\"";
    }
}
