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

package org.apache.royale.compiler.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants used in metadata (e.g. <code>[Event(name="click")]</code>
 * and compiler directives (e.g., <code>@Resource(bundle='foo', key='bar')<code>).
 */
public interface IMetaAttributeConstants
{
	// [AccessibilityClass]
    static final String ATTRIBUTE_ACCESSIBIlITY_CLASS = "AccessibilityClass";
    static final String NAME_ACCESSIBILITY_IMPLEMENTATION = "implementation";
    
    // [Alternative]
    static final String ATTRIBUTE_ALTERNATIVE = "Alternative";
    static final String NAME_ALTERNATIVE_REPLACEMENT = "replacement";
    static final String NAME_ALTERNATIVE_SINCE = "since"; 
    
    // [ArrayElementType]
    static final String ATTRIBUTE_ARRAYELEMENTTYPE = "ArrayElementType";
    
    // [Bindable]
    static final String ATTRIBUTE_BINDABLE = "Bindable";
    static final String NAME_BINDABLE_EVENT = "event";
    static final String NAME_BINDABLE_STYLE = "style";
    
    // [ChangeEvent]
    static final String ATTRIBUTE_CHANGE_EVENT = "ChangeEvent";
    
    // [CollapseWhiteSpace]
    static final String ATTRIBUTE_COLLAPSEWHITESPACE = "CollapseWhiteSpace";
    
    // [DefaultProperty]
    static final String ATTRIBUTE_DEFAULTPROPERTY = "DefaultProperty";
    
    // [DefaultTriggerEvent]
    static final String ATTRIBUTE_DEFAULT_TRIGGER_EVENT = "DefaultTriggerEvent";
    
    // [Deprecated]
    static final String ATTRIBUTE_DEPRECATED = "Deprecated";
    static final String NAME_DEPRECATED_MESSAGE="message";
    static final String NAME_DEPRECATED_SINCE="since";
    static final String NAME_DEPRECATED_METADATA_MESSAGE="deprecatedMessage";
    static final String NAME_DEPRECATED_METADATA_REPLACEMENT = "deprecatedReplacement";
    static final String NAME_DEPRECATED_METADATA_SINCE="deprecatedSince";
    static final String NAME_DEPRECATED_REPLACEMENT="replacement";
    
    // [DiscouragedForProfile]
    static final String ATTRIBUTE_DISCOURAGED_FOR_PROFILE = "DiscouragedForProfile";
    static final String NAME_DISCOURAGED_PROFILE = "profile";
    static final String VALUE_DISCOURAGED_PROFILE_DESKTOP = "desktop";
    static final String VALUE_DISCOURAGED_PROFILE_EXTENDED_DESKTOP = "extendedDesktop";
    static final String VALUE_DISCOURAGED_PROFILE_EXTENDED_MOBILE_DEVICE = "extendedMobileDevice";
    static final String VALUE_DISCOURAGED_PROFILE_MOBILE_DEVICE = "mobileDevice";
    
    // [Effect]
    static final String ATTRIBUTE_EFFECT = "Effect";
    static final String NAME_EFFECT_EVENT = "event";
    static final String NAME_EFFECT_NAME = "name";
    
    // [Embed] and @Embed
    static final String ATTRIBUTE_EMBED = "Embed";
    
    // [Event]
    static final String ATTRIBUTE_EVENT = "Event";
    static final String NAME_EVENT_NAME = "name";
    static final String NAME_EVENT_TYPE = "type";
    
    // [Exclude]
    static final String ATTRIBUTE_EXCLUDE = "Exclude";
    static final String NAME_EXCLUDE_KIND = "kind";
    static final String VALUE_EXCLUDE_KIND_EFFECT = "effect";
    static final String VALUE_EXCLUDE_KIND_EVENT = "event";
    static final String VALUE_EXCLUDE_KIND_METHOD = "method";
    static final String VALUE_EXCLUDE_KIND_PROPERTY = "property";
    static final String VALUE_EXCLUDE_KIND_STYLE = "style";
    static final String NAME_EXCLUDE_NAME = "name";
   
    // [ExcludeClass]
    static final String ATTRIBUTE_EXCLUDECLASS = "ExcludeClass";
    
    // [Frame]
    static final String ATTRIBUTE_FRAME = "Frame";
    static final String NAME_FRAME_EXTRA_CLASS = "extraClass";
    static final String NAME_FRAME_FACTORY_CLASS = "factoryClass";
    
    // [__go_to_definition_help]
    static final String ATTRIBUTE_GOTODEFINITIONHELP = "__go_to_definition_help"; 
    static final String NAME_GOTODEFINITIONHELP_FILE = "file"; 
    static final String NAME_GOTODEFINITIONHELP_POS = "pos"; 
    
    // [__go_to_ctor_definition_help]
    static final String ATTRIBUTE_GOTODEFINITION_CTOR_HELP = "__go_to_ctor_definition_help";
    
    // [HostComponent]
    static final String ATTRIBUTE_HOST_COMPONENT = "HostComponent";
    
    // [IconFile]
    static final String ATTRIBUTE_ICON_FILE = "IconFile";

    // [Inline]
    public static final String ATTRIBUTE_INLINE = "Inline";

    // [Inspectable]
	static final String ATTRIBUTE_INSPECTABLE = "Inspectable";
    static final String NAME_INSPECTABLE_ARRAYTYPE = "arrayType";
	static final String NAME_INSPECTABLE_CATEGORY = "category";
	static final String VALUE_INSPECTABLE_CATEGORY_GENERAL = "General";
    static final String NAME_INSPECTABLE_DEFAULT_VALUE = "defaultValue";
	static final String NAME_INSPECTABLE_ENUMERATION = "enumeration";
    static final String NAME_INSPECTABLE_ENVIRONMENT = "environment";
    static final String VALUE_INSPECTABLE_ENVIRONMENT_NONE = "none";
    static final String NAME_INSPECTABLE_FORMAT = "format";
    static final String VALUE_INSPECTABLE_FORMAT_COLOR = "Color";
	static final String NAME_INSPECTABLE_NAME = "name"; 
	static final String NAME_INSPECTABLE_TYPE = "type";
	static final String NAME_INSPECTABLE_VARIABLE = "variable";
	static final String NAME_INSPECTABLE_VERBOSE = "verbose";
	
	// [InstanceType]
    static final String ATTRIBUTE_INSTANCETYPE = "InstanceType";
    
    // [Mixin]
    static final String ATTRIBUTE_MIXIN = "Mixin";   
        
    // [NonCommittingChangeEvent]
	static final String ATTRIBUTE_NONCOMMITTING = "NonCommittingChangeEvent";
	
	// [PercentProxy]
	static final String ATTRIBUTE_PERCENT_PROXY = "PercentProxy";
	
	// [RemoteClass]
    static final String ATTRIBUTE_REMOTECLASS = "RemoteClass";
    static final String NAME_REMOTECLASS_ALIAS = "alias";
    
    // @Resource
    static final String ATTRIBUTE_RESOURCE = "Resource";
    static final String NAME_RESOURCE_BUNDLE = "bundle";
    static final String NAME_RESOURCE_KEY = "key";
    
    // [ResourceBundle]
    static final String ATTRIBUTE_RESOURCEBUNDLE = "ResourceBundle";
    
    // [RichTextcContent]
    static final String ATTRIBUTE_RICHTEXTCONTENT = "RichTextContent";
    
    // [SkinClass]
    static final String ATTRIBUTE_SKIN_CLASS = "SkinClass";
    
    // [SkinPart]
    static final String ATTRIBUTE_SKIN_PART = "SkinPart";
    static final String NAME_SKIN_PART_REQUIRED = "required";
    static final String VALUE_SKIN_PART_REQUIRED_FALSE = "false";
    static final String VALUE_SKIN_PART_REQUIRED_TRUE = "true";
    
    // [SkinState]
    static final String ATTRIBUTE_SKIN_STATE = "SkinState";
    
    // [States]
    static final String ATTRIBUTE_STATES = "States";	
    
    // [Style]
	static final String ATTRIBUTE_STYLE = "Style";
    static final String NAME_STYLE_ARRAYTYPE = "arrayType";
    static final String NAME_STYLE_ENUMERATION = "enumeration";
    static final String NAME_STYLE_FORMAT = "format";
    static final String VALUE_STYLE_FORMAT_COLOR = "Color";
    static final String NAME_STYLE_INHERIT = "inherit";
    static final String VALUE_STYLE_INHERIT_YES = "yes";
    static final String VALUE_STYLE_INHERIT_NO = "no";
	static final String NAME_STYLE_NAME = "name";
	static final String NAME_STYLE_TYPE = "type";
	static final String NAME_STYLE_STATES = "states";
	static final String NAME_STYLE_THEME = "theme";
	
	// [SWFOverride]
    static final String ATTRIBUTE_SWFOVERRIDE = "SWFOverride";
    static final String NAME_SWFOVERRIDE_RETURNS = "returns";
    static final String NAME_SWFOVERRIDE_PARAMS = "params";
    static final String NAME_SWFOVERRIDE_ALTPARAMS = "altparams";
    
	// [VisualContentHolder]
    static final String ATTRIBUTE_VISUALCONTENTHOLDER = "VisualContentHolder";

    // Attribute names shared by [Inspectable] and [Style].
    static final String NAME_MIN_VALUE = "minValue";
    static final String NAME_MIN_VALUE_EXCLUSIVE = "minValueExclusive";
    static final String NAME_MAX_VALUE = "maxValue";
    static final String NAME_MAX_VALUE_EXCLUSIVE = "maxValueExclusive";

    // [RoyaleAbstract]
    static final String ATTRIBUTE_ABSTRACT = "RoyaleAbstract";

    // [RoyalePrivateConstructor]
    static final String ATTRIBUTE_PRIVATE_CONSTRUCTOR = "RoyalePrivateConstructor";
    
    // [RoyaleArrayLike(...args)]
    static final String ATTRIBUTE_ARRAYLIKE = "RoyaleArrayLike";

    // [RoyaleBindings] (added by compiler)
    static final String ATTRIBUTE_BINDINGS= "RoyaleBindings";
	
	/**
	 * List of metadata tags that do not inherit
	 */
	static final Set<String> NON_INHERITING_METATAGS =
	    Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]
		{
		    ATTRIBUTE_ALTERNATIVE,
		    ATTRIBUTE_BINDABLE,
		    ATTRIBUTE_DEPRECATED,
		    ATTRIBUTE_DISCOURAGED_FOR_PROFILE,
            ATTRIBUTE_EXCLUDECLASS,
            ATTRIBUTE_ABSTRACT,
            ATTRIBUTE_PRIVATE_CONSTRUCTOR,
        })));
}

