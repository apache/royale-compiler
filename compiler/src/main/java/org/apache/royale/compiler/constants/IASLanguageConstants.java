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

import com.google.common.collect.ImmutableSet;

/**
 * AS3 types and constants found within the AS3 language, such as constants for
 * Object, etc.
 */
public interface IASLanguageConstants
{
	// ActionScript data types
    static final String ApplicationDomain = "flash.system.ApplicationDomain"; 
	static final String Array = "Array"; 
	static final String Boolean = "Boolean"; 
    static final String Capabilities = "flash.system.Capabilities"; 
	static final String Class = "Class"; 
	static final String Date = "Date"; 
	static final String Dictionary = "flash.utils.Dictionary"; 
	static final String Error = "Error"; 
	/** {@code [Exclude(kind="style" name="foo")]} */
	static final String EXCLUDE_META_TAG = "Exclude"; 
	static final String EXCLUDE_META_TAG_KIND = "kind"; 
	static final String EXCLUDE_META_TAG_NAME = "name"; 
	static final String EXCLUDE_META_TAG_STYLE = "style"; 
	static final String Event = "flash.events.Event"; 
	static final String EventDispatcher = "flash.events.EventDispatcher"; 
	static final String Function = "Function";
	static final String getClassByAlias = "flash.net.getClassByAlias"; 
	static final String getDefinitionByName = "flash.utils.getDefinitionByName"; 
	static final String IEventDispatcher = "flash.events.IEventDispatcher"; 
	static final String _int = "int"; 
    static final String Namespace = "Namespace"; 
    static final String Number = "Number"; 
    static final String Null = "Null"; 
	static final String Object = "Object"; 
	static final String QName = "QName"; 
    static final String registerClassAlias = "flash.net.registerClassAlias"; 
    static final String Sprite = "flash.display.Sprite"; 
	static final String String = "String"; 
	static final String uint = "uint"; 
    static final String Undefined = "Undefined";
	static final String Vector = "Vector"; 
	static final String Vector_double = "Vector$double"; 
	static final String Vector_int = "Vector$int"; 
	static final String Vector_object = "Vector$object"; 
	static final String Vector_uint = "Vector$uint"; 
	static final String XML = "XML"; 
	static final String XMLList = "XMLList"; 
	static final String RegExp = "RegExp"; 
	
	static final String void_ = "void"; 
	static final String ANY_TYPE = "*"; 
	static final String REST = "..."; 
	static final String REST_IDENTIFIER = "rest"; 
    static final String NULL = "null"; 
    static final String UNDEFINED = "undefined";
    static final String arguments = "arguments"; 
    
    static final String Vector_impl_package = "__AS3__.vec"; 
    static final String Vector_qname = (Vector_impl_package + "." + Vector).intern(); 
    
    static final String TRUE = "true"; 
    static final String FALSE = "false"; 

    /**
     * Default name always available for conditional compilation 
     */
    static final String DEFAULT_CONFIG_NAME = "CONFIG"; 
	
	/**
	 * An enumeration of core built-in types.
	 */
	static enum BuiltinType
	{
	    ANY_TYPE(IASLanguageConstants.ANY_TYPE),
	    ARRAY(IASLanguageConstants.Array),
	    BOOLEAN(IASLanguageConstants.Boolean),
	    CLASS(IASLanguageConstants.Class),
	    FUNCTION(IASLanguageConstants.Function),
	    INT(IASLanguageConstants._int),
        NAMESPACE(IASLanguageConstants.Namespace),
        NULL(IASLanguageConstants.Null),
        NUMBER(IASLanguageConstants.Number),
	    OBJECT(IASLanguageConstants.Object),
	    QNAME(IASLanguageConstants.QName),
        REGEXP(IASLanguageConstants.RegExp),
	    STRING(IASLanguageConstants.String),
	    
	    /** 
	     * This is a built-in "pseudo type" : the "undefined class"
	     * It is NOT the value whose qname is "undefined".
	     * 
	     * Having a fake build int type to represent a class that we can't resolve
	     * may not have been the best choice here (according to Chris B).
	     * 
	     * Here we are explicitly not using the normal convention of all upper case for ENUM names,
	     * in order to try and minimize confusion with our two different undefined definitions.
	     */
        Undefined(IASLanguageConstants.Undefined),  
        UINT(IASLanguageConstants.uint),
	    VECTOR(IASLanguageConstants.Vector), 
	    VOID(IASLanguageConstants.void_),
	    XML(IASLanguageConstants.XML),
	    XMLLIST(IASLanguageConstants.XMLList);
	    
        private BuiltinType(String name)
        {
           this.name = name;
        }
        
        private final String name;
        
        public String getName()
        {
            return name;
        }
	}
	
	/**
	 * Dynamic type names.
	 */
	static final ImmutableSet<String> DYNAMIC_TYPES_SET = 
	    ImmutableSet.of(Array, Class, Object, Function, ANY_TYPE);
	
	/**
	 * Name of the core AS3 language SWC for the web
	 */
	static final String PLAYERGLOBAL_SWC = "playerglobal.swc"; 
	
	/**
	 * Name of the core AS3 language SWC for the desktop
	 */
	static final String AIRGLOBAL_SWC = "airglobal.swc"; 
}
