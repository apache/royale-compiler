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

import org.apache.royale.compiler.mxml.IMXMLTagData;

/**
 * This class stores the new MXMLTagData object, as well as the index
 * in which we should insert it into the parent MXMLData object once balancing
 * is complete
 */
class MXMLTagDataPayload implements Comparable<MXMLTagDataPayload>{
	
    private IMXMLTagData tagData;
	private int offset;
	
	public MXMLTagDataPayload(IMXMLTagData token, int position) {
        offset = position;
        tagData = token;
    }
	
	/**
	 * @return the {@link IMXMLTagData} that should be inserted into its parent
	 * MXMLData object
	 */
	public IMXMLTagData getTagData() {
		return tagData;
	}
	
	/**
	 * Returns the position that we should insert our payload into
	 * @return a non-negative position
	 */
	public int getPosition() {
		return offset;
	}
	
	@Override
    public int compareTo(MXMLTagDataPayload o) {
		if(offset == o.offset)
		    return 0;
		if(offset < o.offset)
		    return 1;
		return -1;
	}
}
