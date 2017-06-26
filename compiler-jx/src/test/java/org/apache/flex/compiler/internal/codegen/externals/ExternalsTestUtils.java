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

package org.apache.flex.compiler.internal.codegen.externals;

import java.io.File;
import java.io.IOException;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.utils.FilenameNormalization;

public class ExternalsTestUtils
{
    public static File TEMP_DIR = new File(
            FilenameNormalization.normalize("../../target/junit-temp"));

    // XXX missing.js is a temp location until we can figure out where it should placed in the build
    public static File MISSING_JS_FILE = FilenameNormalization.normalize(new File(
            "../../../externs/js/src/main/javascript/missing.js"));

    // XXX AS3.as is a namespace needed to override toString in some classes
    public static File AS3_NAMESPACE_FILE;

    public static File EXTERNAL_JS_DIR;

    public static File EXTERNAL_JQUERY_DIR;

    public static File EXTERNAL_JASMINE_DIR;

    public static File EXTERNAL_NODE_DIR;

    public static File AS_ROOT_DIR = new File(TEMP_DIR, "externals/as");

    public static void init()
    {
    	File f = new File(
        "../../../flex-typedefs/js/src/main/javascript/missing.js");
    	if (!f.exists())
    		 f = new File(
    	        "../../../../flex-typedefs/js/src/main/javascript/missing.js");
        // XXX missing.js is a temp location until we can figure out where it should placed in the build
        MISSING_JS_FILE = FilenameNormalization.normalize(f);

        f = new File(
        "../../../flex-typedefs/js/src/main/flex/AS3.as");
        if (!f.exists())
        	f = new File(
            "../../../../flex-typedefs/js/src/main/flex/AS3.as");
        // XXX AS3.as is a namespace needed to override toString in some classes
        AS3_NAMESPACE_FILE = FilenameNormalization.normalize(f);

        f = new File(
        "../../../flex-typedefs/js/target/downloads");
        if (!f.exists())
        	f = new File(
            "../../../../flex-typedefs/js/target/downloads");
        EXTERNAL_JS_DIR = FilenameNormalization.normalize(f);

        f = new File(
        "../../../flex-typedefs/jquery/target/downloads");
        if (!f.exists())
        	f = new File(
            "../../../../flex-typedefs/jquery/target/downloads");
        
        EXTERNAL_JQUERY_DIR = FilenameNormalization.normalize(f);

        f = new File(
                "../../../flex-typedefs/jasmine/target/downloads");
        if (!f.exists())
        	f = new File(
                    "../../../../flex-typedefs/jasmine/target/downloads");
        EXTERNAL_JASMINE_DIR = FilenameNormalization.normalize(f);

        f = new File(
        "../../../flex-typedefs/node/target/downloads/closure-compiler-master/contrib/nodejs");
        if (!f.exists())
        	f = new File(
            "../../../../flex-typedefs/node/target/downloads/closure-compiler-master/contrib/nodejs");
        EXTERNAL_NODE_DIR = FilenameNormalization.normalize(f);
    	
    }
    
    public static void addTestExcludesFull(ExternCConfiguration config)
    {
        config.addFieldExclude("Window", "focus");
        config.addClassExclude("controlRange");
        config.addClassExclude("ITemplateArray");

        config.addExclude("Array", "toSource");
        config.addExclude("Date", "valueOf");
        config.addExclude("String", "valueOf");
        config.addExclude("String", "raw");

        // SVG
        config.addExclude("SVGStylable", "className");
        config.addExclude("SVGStylable", "style");
        config.addExclude("SVGLocatable", "farthestViewportElement");
        config.addExclude("SVGLocatable", "nearestViewportElement");

        // jQuery XXX (these will need to be defined in some config when we get external libs
        // working correctly with EXTERNC)
        config.addClassToFunction("$");

        config.addExclude("jQuery", "is");
        config.addExclude("jQuery", "promise");
        config.addExclude("jQuery", "getJSON");
        config.addExclude("jQuery", "ajax");
        config.addExclude("jQuery", "when");
        config.addExclude("jQuery", "post");
        config.addExclude("jQuery", "getScript");
        config.addExclude("jQuery", "Callbacks");

        config.addClassExclude("Deferred");
        config.addClassExclude("jQuery.deferred");
        config.addClassExclude("jQuery.Event");
        config.addClassExclude("jQuery.Deferred");
        config.addClassExclude("$.Event");
        config.addClassExclude("$.Deferred");
        config.addClassExclude("$.deferred");
    }

    public static void addTestExternalsFull(ExternCConfiguration config)
            throws IOException
    {
        String coreRoot = ExternalsTestUtils.EXTERNAL_JS_DIR.getAbsolutePath();

        config.addExternal(ExternalsTestUtils.MISSING_JS_FILE);
        config.addExternal(coreRoot + "/es3.js");
        config.addExternal(coreRoot + "/es5.js");
        config.addExternal(coreRoot + "/es6.js");
        config.addExternal(coreRoot + "/es6_collections.js");

        config.addExternal(coreRoot + "/browser/w3c_anim_timing.js");
        config.addExternal(coreRoot + "/browser/w3c_audio.js");
        config.addExternal(coreRoot + "/browser/w3c_batterystatus.js");
        config.addExternal(coreRoot + "/browser/w3c_css.js");
        config.addExternal(coreRoot + "/browser/w3c_css3d.js");
        config.addExternal(coreRoot + "/browser/w3c_device_sensor_event.js");
        config.addExternal(coreRoot + "/browser/w3c_dom1.js");
        config.addExternal(coreRoot + "/browser/w3c_dom2.js");
        config.addExternal(coreRoot + "/browser/w3c_dom3.js");
        //config.addExternal(coreRoot + "/browser/w3c_dom4.js");
        config.addExternal(coreRoot + "/browser/w3c_elementtraversal.js");
        config.addExternal(coreRoot + "/browser/w3c_event.js");
        config.addExternal(coreRoot + "/browser/w3c_event3.js");
        config.addExternal(coreRoot + "/browser/w3c_gamepad.js");
        config.addExternal(coreRoot + "/browser/w3c_geolocation.js");
        config.addExternal(coreRoot + "/browser/w3c_indexeddb.js");
        config.addExternal(coreRoot + "/browser/w3c_midi.js");
        config.addExternal(coreRoot + "/browser/w3c_navigation_timing.js");
        config.addExternal(coreRoot + "/browser/w3c_permissions.js");
        config.addExternal(coreRoot + "/browser/w3c_pointer_events.js");
        config.addExternal(coreRoot + "/browser/w3c_range.js");
        config.addExternal(coreRoot + "/browser/w3c_requestidlecallback.js");
        config.addExternal(coreRoot + "/browser/w3c_rtc.js");
        config.addExternal(coreRoot + "/browser/w3c_screen_orientation.js");
        config.addExternal(coreRoot + "/browser/w3c_selectors.js");
        //config.addExternal(coreRoot + "/browser/w3c_serviceworker.js");
        config.addExternal(coreRoot + "/browser/w3c_touch_event.js");
        //config.addExternal(coreRoot + "/browser/w3c_webcrypto.js");
        config.addExternal(coreRoot + "/browser/w3c_xml.js");

        config.addExternal(coreRoot + "/browser/whatwg_encoding.js");

        config.addExternal(coreRoot + "/browser/chrome.js");
        //config.addExternal(coreRoot + "/browser/fetchapi.js");
        config.addExternal(coreRoot + "/browser/fileapi.js");
        config.addExternal(coreRoot + "/browser/flash.js");
        config.addExternal(coreRoot + "/browser/gecko_css.js");
        config.addExternal(coreRoot + "/browser/gecko_dom.js");
        config.addExternal(coreRoot + "/browser/gecko_event.js");
        config.addExternal(coreRoot + "/browser/gecko_xml.js");
        config.addExternal(coreRoot + "/browser/html5.js");
        config.addExternal(coreRoot + "/browser/ie_css.js");
        config.addExternal(coreRoot + "/browser/ie_dom.js");
        config.addExternal(coreRoot + "/browser/ie_event.js");
        config.addExternal(coreRoot + "/browser/ie_vml.js");
        config.addExternal(coreRoot + "/browser/intl.js");
        config.addExternal(coreRoot + "/browser/iphone.js");
        config.addExternal(coreRoot + "/browser/mediasource.js");
        config.addExternal(coreRoot + "/browser/page_visibility.js");
        config.addExternal(coreRoot + "/browser/streamsapi.js");
        config.addExternal(coreRoot + "/browser/url.js");
        config.addExternal(coreRoot + "/browser/v8.js");
        config.addExternal(coreRoot + "/browser/webgl.js");

        config.addExternal(coreRoot + "/browser/webkit_css.js");
        config.addExternal(coreRoot + "/browser/webkit_dom.js");
        config.addExternal(coreRoot + "/browser/webkit_event.js");
        //config.addExternal(coreRoot + "/browser/webkit_notifications.js");
        config.addExternal(coreRoot + "/browser/webkit_usercontent.js");
        config.addExternal(coreRoot + "/browser/webstorage.js");
        config.addExternal(coreRoot + "/browser/window.js");

        config.addExternal(coreRoot + "/svg.js");
    }
}
