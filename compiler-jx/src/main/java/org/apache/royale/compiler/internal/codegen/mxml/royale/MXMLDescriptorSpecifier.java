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

package org.apache.royale.compiler.internal.codegen.mxml.royale;

import java.util.ArrayList;

/**
 * @author Erik de Bruin
 */
public class MXMLDescriptorSpecifier extends MXMLNodeSpecifier
{

    //--------------------------------------------------------------------------
    //
    //    Constructor
    //
    //--------------------------------------------------------------------------

    public MXMLDescriptorSpecifier()
    {
        super();

        eventSpecifiers = new ArrayList<MXMLEventSpecifier>();
        propertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        valueNeedsQuotes = false;
    }

    //--------------------------------------------------------------------------
    //
    //    Properties
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    children
    //---------------------------------

    public MXMLDescriptorSpecifier childrenSpecifier;

    //---------------------------------
    //    properties
    //---------------------------------

    public ArrayList<MXMLDescriptorSpecifier> propertySpecifiers;

    //---------------------------------
    //    events
    //---------------------------------

    public ArrayList<MXMLEventSpecifier> eventSpecifiers;

    //---------------------------------
    //    hasArray
    //---------------------------------

    public boolean hasArray;

    //---------------------------------
    //    hasObject
    //---------------------------------

    public boolean hasObject;

    //---------------------------------
    //    id
    //---------------------------------

    public String id;

    //---------------------------------
    //    hasLocalId
    //---------------------------------

    public boolean hasLocalId;

    //---------------------------------
    //    effectiveId
    //---------------------------------

    public String effectiveId;

    //---------------------------------
    //    isTopNode
    //---------------------------------

    public boolean isTopNode;

    //---------------------------------
    //    isProperty
    //---------------------------------

    public boolean isProperty;

    //---------------------------------
    //    parent
    //---------------------------------

    public MXMLDescriptorSpecifier parent;
}
