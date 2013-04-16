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

package org.apache.flex.compiler.internal.mxml;

import java.util.ListIterator;

import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.flex.compiler.mxml.IMXMLDatabindingData;
import org.apache.flex.compiler.mxml.IMXMLDatabindingValue;
import org.apache.flex.compiler.parsing.IASToken;

/**
 * Represents a databinding expression found within content of MXML
 */
public class MXMLDatabindingData extends MXMLUnitData implements
        IMXMLDatabindingData
{
    /**
     * Constructor.
     */
    MXMLDatabindingData(MXMLToken start, ListIterator<MXMLToken> iterator)
    {
        bindingValue = new MXMLDatabindingValue(start, iterator);

        setOffsets(bindingValue.getAbsoluteStart(), bindingValue.getAbsoluteEnd());
        setLine(start.getLine());
        setColumn(start.getColumn());
    }

    private IMXMLDatabindingValue bindingValue;

    //
    // IMXMLDatabindingData implementations
    //

    @Override
    public IASToken[] getDatabindingContent()
    {
        return bindingValue.getDatabindingContent();
    }
}
