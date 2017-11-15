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

package org.apache.royale.compiler.mxml;

import org.apache.royale.compiler.constants.IASLanguageConstants;

/**
 * Core MXML constants.
 */
public interface IMXMLLanguageConstants
{
    //
    // Language namespaces
    //

    /**
     * The langauge namespace for MXML 2006.
     */
    static final String NAMESPACE_MXML_2006 = "http://www.adobe.com/2006/mxml";

    /**
     * The language namespace for MXML 2009.
     */
    static final String NAMESPACE_MXML_2009 = "http://ns.adobe.com/mxml/2009";

    /**
     * The language namespace for MXML 2012 (experimental).
     */
    static final String NAMESPACE_MXML_2012 = "http://ns.adobe.com/mxml/2012";

    //
    // Short names of tags for builtin ActionScript types
    //

    /**
     * The short name of the tag representing the ActionScript
     * <code>Array</code> type.
     */
    static final String ARRAY = IASLanguageConstants.Array;

    /**
     * The short name of the tag representing the ActionScript
     * <code>Boolean</code> type.
     */
    static final String BOOLEAN = IASLanguageConstants.Boolean;

    /**
     * The short name of the tag representing the ActionScript
     * <code>Class</code> type.
     */
    static final String CLASS = IASLanguageConstants.Class;

    /**
     * The short name of the tag representing the ActionScript <code>Date</code>
     * type.
     */
    static final String DATE = IASLanguageConstants.Date;

    /**
     * The short name of the tag representing the ActionScript
     * <code>Function</code> type.
     */
    static final String FUNCTION = IASLanguageConstants.Function;

    /**
     * The short name of the tag representing the ActionScript <code>int</code>
     * type.
     */
    static final String INT = IASLanguageConstants._int;

    /**
     * The short name of the tag representing the ActionScript
     * <code>Number</code> type.
     */
    static final String NUMBER = IASLanguageConstants.Number;

    /**
     * The short name of the tag representing the ActionScript
     * <code>Object</code> type.
     */
    static final String OBJECT = IASLanguageConstants.Object;

    /**
     * The short name of the tag representing the ActionScript <code>uint</code>
     * type.
     */
    static final String STRING = IASLanguageConstants.String;

    /**
     * The short name of the tag representing the ActionScript <code>uint</code>
     * type.
     */
    static final String UINT = IASLanguageConstants.uint;

    /**
     * The short name of the tag representing the ActionScript <code>XML</code>
     * type.
     */
    static final String XML = IASLanguageConstants.XML;

    /**
     * The short name of the tag representing the ActionScript
     * <code>XMLList</code> type.
     */
    static final String XML_LIST = IASLanguageConstants.XMLList;

    //
    // Short names of special language tags
    //

    /**
     * The short name of the special {@code <Binding>} tag.
     */
    static final String BINDING = "Binding";

    /**
     * The short name of the special {@code <Component>} tag.
     */
    static final String COMPONENT = "Component";

    /**
     * The short name of the special {@code <Declarations>} tag.
     */
    static final String DECLARATIONS = "Declarations";

    /**
     * The short name of the special {@code <Definition>} tag.
     */
    static final String DEFINITION = "Definition";

    /**
     * The short name of the special {@code <DesignLayer>} tag.
     */
    static final String DESIGN_LAYER = "DesignLayer";

    /**
     * The short name of the special {@code <Library>} tag.
     */
    static final String LIBRARY = "Library";

    /**
     * The short name of the special {@code <Metadata>} tag.
     */
    static final String METADATA = "Metadata";

    /**
     * The short name of the special {@code <Model>} tag.
     */
    static final String MODEL = "Model";

    /**
     * The short name of the special {@code <Private>} tag.
     */
    static final String PRIVATE = "Private";

    /**
     * The short name of the special {@code <Reparent>} tag.
     */
    static final String REPARENT = "Reparent";

    /**
     * The short name of the special {@code <Repeater>} tag.
     */
    static final String REPEATER = "Repeater";

    /**
     * The short name of the special {@code <Script>} tag.
     */
    static final String SCRIPT = "Script";

    /**
     * The short name of the special {@code <State>} tag.
     */
    static final String STATE = "State";

    /**
     * The short name of the special {@code <Style>} tag.
     */
    static final String STYLE = "Style";

    //
    // Special attributes
    //

    /**
     * The short name of the special <code>backgroundColor</code> attribute.
     */
    static final String ATTRIBUTE_BACKGROUND_COLOR = "clbackgroundColorassName";

    /**
     * The short name of the special <code>className</code> attribute.
     */
    static final String ATTRIBUTE_CLASS_NAME = "className";

    /**
     * The short name of the special <code>destination</code> attribute.
     */
    static final String ATTRIBUTE_DESTINATION = "destination";

    /**
     * The short name of the special <code>excludeFrom</code> attribute.
     */
    static final String ATTRIBUTE_EXCLUDE_FROM = "excludeFrom";

    /**
     * The short name of the special <code>fixed</code> attribute.
     */
    static final String ATTRIBUTE_FIXED = "fixed";

    /**
     * The short name of the special <code>format</code> attribute.
     */
    static final String ATTRIBUTE_FORMAT = "format";

    /**
     * The short name of the special <code>frameRate</code> attribute.
     */
    static final String ATTRIBUTE_FRAME_RATE = "frameRate";

    /**
     * The short name of the special <code>height</code> attribute.
     */
    static final String ATTRIBUTE_HEIGHT = "height";

    /**
     * The short name of the special <code>id</code> attribute.
     */
    static final String ATTRIBUTE_ID = "id";

    /**
     * The short name of the special <code>localId</code> attribute.
     */
    static final String ATTRIBUTE_LOCAL_ID = "localId";

    /**
     * The short name of the special <code>implements</code> attribute.
     */
    static final String ATTRIBUTE_IMPLEMENTS = "implements";

    /**
     * The short name of the special <code>includeIn</code> attribute.
     */
    static final String ATTRIBUTE_INCLUDE_IN = "includeIn";

    /**
     * The short name of the special <code>itemCreationPolicy</code> attribute.
     */
    static final String ATTRIBUTE_ITEM_CREATION_POLICY = "itemCreationPolicy";

    /**
     * The short name of the special <code>itemDestructionPolicy</code>
     * attribute.
     */
    static final String ATTRIBUTE_ITEM_DESTRUCTION_POLICY = "itemDestructionPolicy";

    /**
     * The short name of the special <code>name</code> attribute.
     */
    static final String ATTRIBUTE_NAME = "name";

    /**
     * The short name of the special <code>pageTitle</code> attribute.
     */
    static final String ATTRIBUTE_PAGE_TITLE = "pageTitle";

    /**
     * The short name of the special <code>prelaoder</code> attribute.
     */
    static final String ATTRIBUTE_PRELOADER = "preloader";

    /**
     * The short name of the special <code>rsl</code> attribute.
     */
    static final String ATTRIBUTE_RSL = "rsl";

    /**
     * The short name of the special <code>runtimeDPIProvider</code> attribute.
     */
    static final String ATTRIBUTE_RUNTIME_DPI_PROVIDER = "runtimeDPIProvider";

    /**
     * The short name of the special <code>scriptRecursionLimit</code>
     * attribute.
     */
    static final String ATTRIBUTE_SCRIPT_RECURSION_LIMIT = "scriptRecursionLimit";

    /**
     * The short name of the special <code>scriptTimeLimit</code> attribute.
     */
    static final String ATTRIBUTE_SCRIPT_TIME_LIMIT = "scriptTimeLimit";

    /**
     * The short name of the special <code>source</code> attribute.
     */
    static final String ATTRIBUTE_SOURCE = "source";

    /**
     * The short name of the special <code>splashScreenImage</code> attribute.
     */
    static final String ATTRIBUTE_SPLASH_SCREEN_IMAGE = "splashScreenImage";

    /**
     * The short name of the special <code>stateGroups</code> attribute.
     */
    static final String ATTRIBUTE_STATE_GROUPS = "stateGroups";

    /**
     * The short name of the special <code>target</code> attribute.
     */
    static final String ATTRIBUTE_TARGET = "target";

    /**
     * The short name of the special <code>theme</code> attribute.
     */
    static final String ATTRIBUTE_THEME = "theme";

    /**
     * The short name of the special <code>twoWay</code> attribute.
     */
    static final String ATTRIBUTE_TWO_WAY = "twoWay";

    /**
     * The short name of the special <code>type</code> attribute.
     */
    static final String ATTRIBUTE_TYPE = "type";

    /**
     * The short name of the special <code>useDirectBlit</code> attribute.
     */
    static final String ATTRIBUTE_USE_DIRECT_BLIT = "useDirectBlit";

    /**
     * The short name of the special <code>useGPU</code> attribute.
     */
    static final String ATTRIBUTE_USE_GPU = "useGPU";

    /**
     * The short name of the special <code>usePreloader</code> attribute.
     */
    static final String ATTRIBUTE_USE_PRELOADER = "usePreloader";

    /**
     * The short name of the special <code>width</code> attribute.
     */
    static final String ATTRIBUTE_WIDTH = "width";

    //
    // Allowed values for itemCreationPolicy attribute
    //

    /**
     * One of the two allowed values for the <code>itemCreationPolicy</code>
     * attribute.
     */
    static final String ITEM_CREATION_POLICY_IMMEDIATE = "immediate";

    /**
     * One of the two allowed values for the <code>itemCreationPolicy</code>
     * attribute.
     */
    static final String ITEM_CREATION_POLICY_DEFERRED = "deferred";

    //
    // Allowed values for itemDestructionPolicy attribute
    //

    /**
     * One of the two allowed values for the <code>itemDestructionPolicy</code>
     * attribute.
     */
    static final String ITEM_DESTRUCTION_POLICY_AUTO = "auto";

    /**
     * One of the two allowed values for the <code>itemDestructionPolicy</code>
     * attribute.
     */
    static final String ITEM_DESTRUCTION_POLICY_NEVER = "never";

    //
    // Allowed values for format attribute
    //

    /**
     * One of the two allowed values for the <code>format</code> attribute.
     */
    static final String FORMAT_E4X = "e4x";

    /**
     * One of the two allowed values for the <code>format</code> attribute.
     */
    static final String FORMAT_XML = "xml";

    //
    // Autogenerated properties
    //

    /**
     * The name of the autogenerated <code>outerDocument</code> property of a
     * {@code <Component>} class.
     */
    static final String PROPERTY_OUTER_DOCUMENT = "outerDocument";
}
