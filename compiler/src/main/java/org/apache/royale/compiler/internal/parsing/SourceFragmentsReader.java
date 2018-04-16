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

import java.io.StringReader;

import org.apache.royale.compiler.constants.IMXMLCoreConstants;

public class SourceFragmentsReader extends StringReader
{
    /**
     * Concatenates the logical text of multiple source fragments.
     */
    public static String concatLogicalText(ISourceFragment[] sourceFragments)
    {
        StringBuilder sb = new StringBuilder();
        for (ISourceFragment sourceFragment : sourceFragments)
        {
            sb.append(sourceFragment.getLogicalText());
        }
        return sb.toString();
    }

    /**
     * Concatenates the physical text of multiple source fragments.
     */
    public static String concatPhysicalText(ISourceFragment[] sourceFragments)
    {
        StringBuilder sb = new StringBuilder();
        for (ISourceFragment sourceFragment : sourceFragments)
        {
        	String physicalText = sourceFragment.getPhysicalText();
        	if (physicalText.startsWith(IMXMLCoreConstants.cDataStart))
        	{
        		physicalText = physicalText.substring(IMXMLCoreConstants.cDataStart.length());
        		if (physicalText.endsWith(IMXMLCoreConstants.cDataEnd))
        		{
        			physicalText = physicalText.substring(0, physicalText.length() - IMXMLCoreConstants.cDataEnd.length());
        		}
        	}
            sb.append(physicalText);
        }
        return sb.toString();
    }

    /**
     * Constructor.
     */
    SourceFragmentsReader(ISourceFragment[] sourceFragments)
    {
        // This class extends StringReader.
        // We want the StringReader to read the concatenated logical text
        // of all the fragments.
        super(concatLogicalText(sourceFragments));
        
        this.sourceFragments = sourceFragments;
        
        // Build an array which keeps tracks of the fragment boundaries
        // as logical offsets.
        int n = sourceFragments.length;
        logicalFragmentBoundaries = new int[n + 1];
        logicalFragmentBoundaries[0] = 0;
        for (int i = 0; i < n; i++)
        {
            logicalFragmentBoundaries[i + 1] =
                logicalFragmentBoundaries[i] +
                sourceFragments[i].getLogicalText().length();
        }
    }
    
    public SourceFragmentsReader(String sourcePath, ISourceFragment[] sourceFragments)
    {
        this(sourceFragments);
        this.sourcePath = sourcePath;
    }
    
    private String sourcePath;
    
    /**
     * The source fragments that we're reading.
     */
    private ISourceFragment[] sourceFragments;
    
    /**
     * The boundaries between source fragments, as logical offsets.
     * For example, if there are two fragments, one with logical length 3
     * and one with logical length 5, then this array will be [0, 3, 8].
     * This information allows us to take a starting or ending
     * logical offset and determine which fragment it came from.
     */
    private int[] logicalFragmentBoundaries;
    
    public void adjustLocation(TokenBase token)
    {      
        token.setSourcePath(sourcePath);
        
        // Note that fragments are not necessarily physically contiguous,
        // and a particular logical offset could be the end of fragment n
        // and the beginning of fragment n + 1. Therefore, we need different
        // logic for mapping logical-start-to-physical-start and
        // logical-end-to-physical-end. For example, suppose there are
        // two fragments:
        //
        //       physical-start physical-end logical-start logical-end
        // #0    100            105          0             1
        // #1    110            115          1             6
        //
        // Then a logical start of 1 maps to a physical start of 110,
        // while a logical end of 1 maps to a physical end of 105.

        // Convert token's 'start' from logical to physical.
        int logicalStart = token.getStart();
        int i = getFragmentIndexForStart(logicalStart);
        int startDelta = logicalStart - logicalFragmentBoundaries[i];
        int physicalStart = sourceFragments[i].getPhysicalStart();
        physicalStart += startDelta;
        token.setStart(physicalStart);
        
        // Convert token's 'end' from logical to physical.
        int logicalEnd = token.getEnd();
        int j = getFragmentIndexForEnd(logicalEnd);
        int endDelta = logicalFragmentBoundaries[j + 1] - logicalEnd;
        int physicalEnd = sourceFragments[j].getPhysicalStart() +
                          sourceFragments[j].getPhysicalText().length();
        physicalEnd -= endDelta;
        token.setEnd(physicalEnd);
        
        // Convert token's 'line' from logical to physical.
        int logicalLine = token.getLine();
        int physicalLine = logicalLine + sourceFragments[i].getPhysicalLine();;
        token.setLine(physicalLine);
        
        // Convert token's 'column' from logical to physical.
        int logicalColumn = token.getColumn();
        int physicalColumn = logicalColumn + sourceFragments[i].getPhysicalColumn();
        token.setColumn(physicalColumn);
        
        // Convert token's 'end line' from logical to physical.
        int logicalEndLine = token.getEndLine();
        int physicalEndLine = logicalEndLine + sourceFragments[i].getPhysicalLine();
        token.setEndLine(physicalEndLine);
        
        // Convert token's 'end column' from logical to physical.
        int logicalEndColumn = token.getEndColumn();
        int physicalEndColumn = logicalEndColumn + sourceFragments[i].getPhysicalColumn();
        token.setEndColumn(physicalEndColumn);
    }
    
    private int getFragmentIndexForStart(int start)
    {
        int n = logicalFragmentBoundaries.length;
        for (int i = n - 1; i >= 0; i--)
        {
            if (logicalFragmentBoundaries[i] <= start)
                return i;
        }
        return -1;
    }
    
    private int getFragmentIndexForEnd(int end)
    {
        int n = logicalFragmentBoundaries.length;
        for (int i = 0; i < n; i++)
        {
            if (logicalFragmentBoundaries[i] >= end)
                return i - 1;
        }
        return -1;
    }
    
    public int getLogicalEnd()
    {
        int n = logicalFragmentBoundaries.length;
        return logicalFragmentBoundaries[n - 1];
    }
    
    @Override
    public String toString()
    {
        return concatLogicalText(sourceFragments);
    }
}
