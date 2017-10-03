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

package org.apache.royale.compiler.internal.parsing.mxml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * The BalancingMXMLProcessor performs a balancing operation over a collection of 
 * {@link IMXMLTagData} objects.  It looks for un-balanced MXML and attempts to 
 * add open and close tags in order to create a well-formed, or better-formed, DOM.
 * 
 * The method used is to track tags by their depth, and going from inside out, to check for matches
 * add open/close tags if each depth isn't balanced.  If a depth is unbalanced by close tags, the close tag is
 * promoted to its parent depth and dealt with at that level
 */
public class BalancingMXMLProcessor {
	
	private ArrayList<MXMLTagDataDepth> depths = new ArrayList<MXMLTagDataDepth>();
	
    private Collection<ICompilerProblem> problems;
    private IFileSpecification spec;
    
    private boolean wasRepaired = false;
	
    public BalancingMXMLProcessor(IFileSpecification specification, Collection<ICompilerProblem> problems) {
        setFileSpecification(specification);
        this.problems = problems;
    }
    
    /**
     * Initialize our balancing structures from a full MXMLUnitData array. 
     * @param data the array to use to populate our balancing structures
     */
    public void initialize(IMXMLUnitData[] data) {
        int index = 0;
        for(int i = 0; i < data.length; i++) {
            if(data[i] instanceof IMXMLTagData) {
                if(!((IMXMLTagData)data[i]).isEmptyTag()) {
                    if( ((IMXMLTagData)data[i]).isOpenTag()) {
                        addOpenTag((IMXMLTagData)data[i], index);
                        index++;
                    } else {
                        index--;
                        addCloseTag((IMXMLTagData)data[i], index);
                    }
                }
            }
        }
    }
    
    public final void setFileSpecification(IFileSpecification specification) {
        spec = specification;
    }
	
	public IMXMLUnitData[] balance(IMXMLUnitData[] data, IMXMLData mxmlData, Map<IMXMLTagData, PrefixMap> map) {
        ArrayList<MXMLTagDataPayload> payload = new ArrayList<MXMLTagDataPayload>();
        for(int i = depths.size() - 1; i >= 0 ; i--) {
            boolean b = depths.get(i).balance(payload, map, mxmlData, data, problems, spec);
            if (b) 
                wasRepaired = true; // if any iteration returns true, some repair occurred
        }
        //sort the collection so we can insert from back to front
        Collections.sort(payload, Collections.reverseOrder());
        if(payload.size() > 0) {
            wasRepaired = true;        // If any payload returned, then that also means repairing occurred
            List<IMXMLUnitData> newTags = new ArrayList<IMXMLUnitData>(Arrays.asList(data));
            for(int i = payload.size() - 1; i >=0; i--) {
                MXMLTagDataPayload tokenPayload = payload.get(i);
                newTags.add(tokenPayload.getPosition(), tokenPayload.getTagData());
                
            }
            return newTags.toArray(new IMXMLUnitData[0]);
        }
        return data;
    }
	
	/**
	 * Returns true if the balance operation resulted in a repaired document
	 * @return true if we were repaired
	 */
	public boolean wasRepaired() {
	    return wasRepaired;
	}
	
	private final MXMLTagDataDepth getDepth(int depth) {
		MXMLTagDataDepth dep = null;
		if(depth < 0)
			depth = depth * -1; //take the inverse for the depth if we're unbalanced on the tag side
		if(depths.size() > depth) {
			dep = depths.get(depth);
		} else {
			dep = new MXMLTagDataDepth(depth);
			if( depth - 1 >= 0 && depth -1 < depths.size()) {
				dep.setParent(depths.get(depth - 1));
			}
			depths.add(dep);
		}
		return dep;
	}
	
	public void addOpenTag(IMXMLTagData openTag, int depth) {
		MXMLTagDataDepth dep = getDepth(depth);
		dep.addOpenTag(openTag);
	}
	
	public void addCloseTag(IMXMLTagData closeTag, int depth) {
		MXMLTagDataDepth dep = getDepth(depth);
		dep.addCloseTag(closeTag);
	}
}
