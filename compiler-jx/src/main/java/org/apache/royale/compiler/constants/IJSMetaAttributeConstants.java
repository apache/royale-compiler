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

public interface IJSMetaAttributeConstants
{
    // [JSDynamicOverride]
    static final String ATTRIBUTE_DYNAMIC_OVERRIDE = "JSDynamicOverride";
    static final String NAME_DYNAMIC_OVERRIDE_GET_METHOD = "getMethod";
    static final String NAME_DYNAMIC_OVERRIDE_SET_METHOD = "setMethod";
    static final String NAME_DYNAMIC_OVERRIDE_DELETE_METHOD = "deleteMethod";
    static final String NAME_DYNAMIC_OVERRIDE_IN_METHOD = "inMethod";

    // [JSForInOverride]
    static final String ATTRIBUTE_FOR_IN_OVERRIDE = "JSForInOverride";
    static final String NAME_FOR_IN_OVERRIDE_ITERATOR_METHOD = "iteratorMethod";
    static final String NAME_FOR_IN_OVERRIDE_ITERATOR_NEXT_METHOD = "iteratorNextMethod";
    static final String NAME_FOR_IN_OVERRIDE_ITERATOR_HAS_NEXT_METHOD = "iteratorHasNextMethod";
    static final String NAME_FOR_IN_OVERRIDE_ITERATOR_DONE_METHOD = "iteratorDoneMethod";

    // [JSForEachOverride]
    static final String ATTRIBUTE_FOR_EACH_OVERRIDE = "JSForEachOverride";
    static final String NAME_FOR_EACH_OVERRIDE_ITERATOR_METHOD = "iteratorMethod";
    static final String NAME_FOR_EACH_OVERRIDE_ITERATOR_NEXT_METHOD = "iteratorNextMethod";
    static final String NAME_FOR_EACH_OVERRIDE_ITERATOR_HAS_NEXT_METHOD = "iteratorHasNextMethod";

    // [JSIncludeScript]
    static final String ATTRIBUTE_INCLUDE_SCRIPT = "JSIncludeScript";
    static final String NAME_INCLUDE_SCRIPT_SOURCE = "source";

    // [JSIncludeCSS]
    static final String ATTRIBUTE_INCLUDE_CSS = "JSIncludeCSS";
    static final String NAME_INCLUDE_CSS_SOURCE = "source";

    // [JSIncludeAsset]
    static final String ATTRIBUTE_INCLUDE_ASSET = "JSIncludeAsset";
    static final String NAME_INCLUDE_ASSET_SOURCE = "source";

    // [JSModule]
    static final String ATTRIBUTE_MODULE = "JSModule";

    // [JSRoyaleSuppressExport]
    static final String ATTRIBUTE_SUPPRESS_EXPORT = "JSRoyaleSuppressExport";
}
