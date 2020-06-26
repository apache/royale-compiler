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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLUnclosedTagProblem;
import org.apache.royale.utils.FastStack;

/**
 * Each MXMLTagDepth tracks open and close tags inside of it, and at each level
 * attempts to balance its results if unbalanced, by either adding an open tag, adding a close
 * tag or delegating the decision to the next depth.
 */
class MXMLTagDataDepth {
	
    private static final IMXMLTagAttributeData[] MXML_TAG_ATTRIBUTE_DATAS = new IMXMLTagAttributeData[0];
    
    final class MXMLTagDataComparator implements Comparator<IMXMLTagData> {

        @Override
        public final int compare(final IMXMLTagData o1, final IMXMLTagData o2)
        {
            if(o1.getIndex() == o2.getIndex()) return 0;
            if(o1.getIndex() < o2.getIndex()) return -1;
            return 1;
        }
        
    }
    /**
     * fake {@link MXMLTagData} that we add to our parent MXMLData
     */
    private class FakeMXMLTagData extends MXMLTagData {
        
        public FakeMXMLTagData(String tagName, boolean closeTag, boolean emptyTag) {
            this.tagName = tagName;
            nameType = closeTag ? MXMLTokenTypes.TOKEN_CLOSE_TAG_START : MXMLTokenTypes.TOKEN_OPEN_TAG_START;
			this.emptyTag = emptyTag;

			// a call to findMatchingEndTag() will fail without this
			this.setProblems(new ArrayList<ICompilerProblem>());
        }
        
        public FakeMXMLTagData(MXMLTagData data, boolean emptyTag) {
            super(data);
            this.emptyTag = emptyTag;

			// a call to findMatchingEndTag() will fail without this
			this.setProblems(new ArrayList<ICompilerProblem>());
        }

        @Override
        public String getRawAttributeValue(String attributeName)
        {
            //API allows for null, so return null since we don't have attrs
            return null;
        }

        @Override
        public IMXMLTagAttributeData[] getAttributeDatas()
        {
            //return empty array since our value for children is null
            return MXML_TAG_ATTRIBUTE_DATAS;
        }

        @Override
        public boolean isImplicit()
        {
            return true;
        }
    }
    
    
    private MXMLTagDataDepth parent;
	private ArrayList<IMXMLTagData> openTags;
	private FastStack<IMXMLTagData> closeTags;
	private int depth;
	
	public MXMLTagDataDepth(int depth) {
		this.depth = depth;
		openTags = new ArrayList<IMXMLTagData>();
		closeTags = new FastStack<IMXMLTagData>();
	}
	
	/**
	 * Adds an open tag to our list of tags we are tracking
	 * @param openTag the {@link IMXMLTagData} that is open
	 */
	public final void addOpenTag(final IMXMLTagData openTag) {
		openTags.add(openTag);
	} 
	
	/**
     * Adds a close tag to our list of tags we are tracking
     * @param closeTag the {@link IMXMLTagData} that is close
     */
	public final void addCloseTag(final IMXMLTagData closeTag) {
		closeTags.push(closeTag);
	}
	
	public final void setParent(final MXMLTagDataDepth parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the depth we represent
	 * @return a non-negative depth
	 */
	public final int getDepth() {
		return depth;
	}
	
	/**
	 * Sorts the tags we have encountered
	 */
	final void ensureOrder() {
		Collections.sort(openTags, new MXMLTagDataComparator());
		Collections.sort(closeTags, Collections.reverseOrder(new MXMLTagDataComparator()));
	}
	
	/**
	 * if any repairs were done, either there will be tags in the payload, or the return value will be true,
	 * or both.
	 * 
	 * to determine if any repairing was done:
	 *  ret = balance(payload...)
	 *  did_repair = ret | !payload.isEmpty()
	 *  
	 */
	public final boolean balance(List<MXMLTagDataPayload> payload, Map<IMXMLTagData, PrefixMap> prefixMap, IMXMLData mxmlData, IMXMLUnitData[] data, Collection<ICompilerProblem> problems, IFileSpecification fileSpec) {
		ensureOrder();
		final int size = openTags.size();
		boolean didNonPayloadRepair = false;
		
		for(int i = 0; i < size; i++) {
			IMXMLTagData openTag = openTags.get(i);
			if(!closeTags.isEmpty()) {
				final IMXMLTagData closeTag = closeTags.peek();
				if(closeTag.getName().compareTo(openTag.getName()) != 0) {
					//let's determine where to end, and then move all of our tags to our parent
					int insertOffset = -1;
					while(!closeTags.isEmpty()) {
						final IMXMLTagData pop = closeTags.pop();
						if(pop.getName().compareTo(openTag.getName()) != 0) {
							insertOffset = pop.getIndex();
							if(parent != null) {
								parent.addCloseTag(pop);
							} else {
								//since the parent cannot handle this, we should insert an open tag for this close tag
							    FakeMXMLTagData tagData = new FakeMXMLTagData(pop.getName(), false, false);
                                tagData.setOffsets(pop.getAbsoluteEnd(), pop.getAbsoluteEnd());
                                tagData.setLine(pop.getLine());
                                tagData.setColumn(pop.getColumn());
							    payload.add(new MXMLTagDataPayload(tagData, insertOffset - 1)); 
								problems.add(produceProblemFromToken(tagData, fileSpec));
								if(i + 1 < size) 
								    openTag = openTags.get(++i);
								
							}
						} else { //punt to the parent
							insertOffset = -1;
							break;
						}
					}
					if(insertOffset != -1) {
					    
					    if (!openTag.hasExplicitCloseTag())
					    {
    					    // we have an open with no matching close, so let's just make
                            // it an empty tag. CMP-916
                            ((MXMLTagData)openTag).setEmptyTag();
                            didNonPayloadRepair = true;     // note a repair, so we can alert caller
                            problems.add(produceProblemFromToken(openTag, fileSpec));
                            // TODO: below (line 230) the old code used to make up a new fake tag and
                            // transfer stuff over, and log a problem. We aren't doing that here.
                            // Why do they go to all the trouble to clone the tag??
					    }
					    else
					    {
    					    //we don't have a location to insert, meaning we need to drop in an open tag
    					    //due to the direction of the imbalance
    					    FakeMXMLTagData tagData = new FakeMXMLTagData(openTag.getName(), true, false);
    					    tagData.setOffsets(openTag.getAbsoluteEnd(), openTag.getAbsoluteEnd());
                            tagData.setLine(openTag.getLine());
                            tagData.setColumn(openTag.getColumn());
                            payload.add(new MXMLTagDataPayload(tagData, insertOffset));
    						problems.add(produceProblemFromToken(tagData, fileSpec));
					    }
					}
				} else {
					closeTags.pop();
				}
			} else {
				if(parent != null) {
					parent.addOpenTag(openTag);
				} else {
					int pos = openTag.getIndex();
					int tokenSize = data.length;
					while(pos < tokenSize) {
						IMXMLUnitData currToken = data[pos];
						if(currToken instanceof MXMLTagData && !((MXMLTagData)currToken).hasExplicitCloseTag()) {
						    problems.add(new MXMLUnclosedTagProblem(currToken, ((MXMLTagData)currToken).getName()));
						    FakeMXMLTagData fakeMXMLTagData = new FakeMXMLTagData((MXMLTagData)currToken, true);
                            data[pos] = fakeMXMLTagData;
                            prefixMap.remove((MXMLTagData)currToken);
                            didNonPayloadRepair = true;      // note a repair, so we can alert caller
                             
                            // If the original tag had a prefix map, transfer to to the new tag.
                            PrefixMap map = ((MXMLTagData)currToken).getPrefixMap();
                            if (map != null)
                                prefixMap.put(fakeMXMLTagData, map);
							break;
						}
						pos++;
					}
				}
			}
		}
		if(parent != null) {
			while(!closeTags.isEmpty()) {
				parent.addCloseTag(closeTags.pop());
			}
		}
		return didNonPayloadRepair;
 	}

    private ICompilerProblem produceProblemFromToken(IMXMLTagData tagData, IFileSpecification fileSpec)
    {
        if (tagData instanceof MXMLTagData)
        {
            MXMLTagData tag = (MXMLTagData)tagData;
            if (tag.getSourcePath() == null)
                tag.setSourcePath(fileSpec.getPath());
        }
        return new MXMLUnclosedTagProblem(tagData, tagData.getName());
    }
	
}
