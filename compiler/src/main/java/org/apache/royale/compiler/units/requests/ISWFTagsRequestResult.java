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

package org.apache.royale.compiler.units.requests;

import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;

/**
 * Result object for the GET_SWF_TAGS operation on ICompilationUnit.
 * 
 * @see org.apache.royale.compiler.units.ICompilationUnit
 */
public interface ISWFTagsRequestResult extends IRequestResult
{
    /**
     * Adds generated SWF tags to specified SWFFrame
     * 
     * @param f frame to addd tags to.
     * @return true if tags were successfully generated and added to the frame.
     */
    boolean addToFrame(SWFFrame f);
    
    /**
     * Gets the abc tag that {@link #addToFrame(SWFFrame)} will add to a
     * frame.
     * 
     * @return The abc tag.
     */
    DoABCTag getDoABCTag();
    
    /**
     * Gets the name of the abc tag that {@link #addToFrame(SWFFrame)} will add to a
     * frame.
     * 
     * @return The name of the abc tag.
     */
    String getDoABCTagName();
}
