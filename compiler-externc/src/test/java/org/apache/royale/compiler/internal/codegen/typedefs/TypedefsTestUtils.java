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

package org.apache.royale.compiler.internal.codegen.typedefs;

import java.io.File;
import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.utils.FilenameNormalization;

public class TypedefsTestUtils
{
    public static File TEMP_DIR = new File(
            FilenameNormalization.normalize("target/junit-temp"));

    // This is a copy of the missing.js in royale-typedefs/js.  It doesn't have to
    // stay in sync.  We just want to prove we can override a few things
    public static File MISSING_JS_FILE = FilenameNormalization.normalize(new File(
            "../compiler-externc/src/test/resources/typedefs/unit_tests/missing.js"));

    public static File TYPEDEFS_JS_DIR = FilenameNormalization.normalize(new File(
           "../compiler-externc/target/downloads"));

    // XXX AS3.as is a namespace needed to override toString in some classes
    public static File AS3_NAMESPACE_FILE;
    
    public static File EXTERNAL_JQUERY_DIR;

    public static File EXTERNAL_JASMINE_DIR;

    public static File EXTERNAL_NODE_DIR;

    public static File AS_ROOT_DIR = new File(TEMP_DIR, "typedefs/as");

    // some additional places to look for royale-typedefs files for
    // integration tests that rely on royale-typedefs.
    public static void init()
    {
        File f = new File(
        "../../../royale-typedefs/js/src/main/royale/AS3.as");
        if (!f.exists())
        	f = new File(
            "../../royale-typedefs/js/src/main/royale/AS3.as");
        // XXX AS3.as is a namespace needed to override toString in some classes
        AS3_NAMESPACE_FILE = FilenameNormalization.normalize(f);

        f = new File(
        "../../../royale-typedefs/jquery/target/downloads");
        if (!f.exists())
        	f = new File(
            "../../royale-typedefs/jquery/target/downloads");
        
        EXTERNAL_JQUERY_DIR = FilenameNormalization.normalize(f);

        f = new File(
                "../../../royale-typedefs/jasmine/target/downloads");
        if (!f.exists())
        	f = new File(
                    "../../royale-typedefs/jasmine/target/downloads");
        EXTERNAL_JASMINE_DIR = FilenameNormalization.normalize(f);

        f = new File(
        "../../../royale-typedefs/node/target/downloads/closure-compiler-master/contrib/nodejs");
        if (!f.exists())
        	f = new File(
            "../../royale-typedefs/node/target/downloads/closure-compiler-master/contrib/nodejs");
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
        config.addExclude("Document", "loadXML");
        config.addExclude("Document", "open");
        config.addExclude("Document", "close");
        config.addExclude("Document", "createTreeWalker");
        config.addExclude("Event", "initUIEvent");
        config.addExclude("Event", "initMessageEvent");
        config.addExclude("Element", "load");

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

    public static void addTestTypedefsFull(ExternCConfiguration config)
            throws IOException
    {
        String coreRoot = TypedefsTestUtils.TYPEDEFS_JS_DIR.getAbsolutePath();

        config.addTypedef(TypedefsTestUtils.MISSING_JS_FILE);
        config.addTypedef(coreRoot + "/es3.js");
        config.addTypedef(coreRoot + "/es5.js");
        config.addTypedef(coreRoot + "/es6.js");
        config.addTypedef(coreRoot + "/es6_collections.js");

        config.addTypedef(coreRoot + "/browser/w3c_anim_timing.js");
        config.addTypedef(coreRoot + "/browser/w3c_audio.js");
        config.addTypedef(coreRoot + "/browser/w3c_batterystatus.js");
        config.addTypedef(coreRoot + "/browser/w3c_css.js");
        config.addTypedef(coreRoot + "/browser/w3c_css3d.js");
        config.addTypedef(coreRoot + "/browser/w3c_device_sensor_event.js");
        config.addTypedef(coreRoot + "/browser/w3c_dom1.js");
        config.addTypedef(coreRoot + "/browser/w3c_dom2.js");
        config.addTypedef(coreRoot + "/browser/w3c_dom3.js");
        //config.addTypedef(coreRoot + "/browser/w3c_dom4.js");
        config.addTypedef(coreRoot + "/browser/w3c_elementtraversal.js");
        config.addTypedef(coreRoot + "/browser/w3c_event.js");
        config.addTypedef(coreRoot + "/browser/w3c_event3.js");
        config.addTypedef(coreRoot + "/browser/w3c_gamepad.js");
        config.addTypedef(coreRoot + "/browser/w3c_geolocation.js");
        config.addTypedef(coreRoot + "/browser/w3c_indexeddb.js");
        config.addTypedef(coreRoot + "/browser/w3c_midi.js");
        config.addTypedef(coreRoot + "/browser/w3c_navigation_timing.js");
        config.addTypedef(coreRoot + "/browser/w3c_permissions.js");
        config.addTypedef(coreRoot + "/browser/w3c_pointer_events.js");
        config.addTypedef(coreRoot + "/browser/w3c_range.js");
        config.addTypedef(coreRoot + "/browser/w3c_requestidlecallback.js");
        config.addTypedef(coreRoot + "/browser/w3c_rtc.js");
        config.addTypedef(coreRoot + "/browser/w3c_screen_orientation.js");
        config.addTypedef(coreRoot + "/browser/w3c_selectors.js");
        //config.addTypedef(coreRoot + "/browser/w3c_serviceworker.js");
        config.addTypedef(coreRoot + "/browser/w3c_touch_event.js");
        //config.addTypedef(coreRoot + "/browser/w3c_webcrypto.js");
        config.addTypedef(coreRoot + "/browser/w3c_xml.js");

        config.addTypedef(coreRoot + "/browser/whatwg_encoding.js");

        //config.addTypedef(coreRoot + "/browser/fetchapi.js");
        config.addTypedef(coreRoot + "/browser/fileapi.js");
        config.addTypedef(coreRoot + "/browser/flash.js");
        config.addTypedef(coreRoot + "/browser/gecko_css.js");
        config.addTypedef(coreRoot + "/browser/gecko_dom.js");
        config.addTypedef(coreRoot + "/browser/gecko_event.js");
        config.addTypedef(coreRoot + "/browser/gecko_xml.js");
        config.addTypedef(coreRoot + "/browser/html5.js");
        config.addTypedef(coreRoot + "/browser/ie_css.js");
        config.addTypedef(coreRoot + "/browser/ie_dom.js");
        config.addTypedef(coreRoot + "/browser/ie_event.js");
        config.addTypedef(coreRoot + "/browser/ie_vml.js");
        config.addTypedef(coreRoot + "/browser/intl.js");
        config.addTypedef(coreRoot + "/browser/iphone.js");
        config.addTypedef(coreRoot + "/browser/mediasource.js");
        config.addTypedef(coreRoot + "/browser/page_visibility.js");
        config.addTypedef(coreRoot + "/browser/streamsapi.js");
        config.addTypedef(coreRoot + "/browser/url.js");
        config.addTypedef(coreRoot + "/browser/v8.js");
        config.addTypedef(coreRoot + "/browser/webgl.js");

        config.addTypedef(coreRoot + "/browser/webkit_css.js");
        config.addTypedef(coreRoot + "/browser/webkit_dom.js");
        config.addTypedef(coreRoot + "/browser/webkit_event.js");
        //config.addTypedef(coreRoot + "/browser/webkit_notifications.js");
        config.addTypedef(coreRoot + "/browser/webkit_usercontent.js");
        config.addTypedef(coreRoot + "/browser/webstorage.js");
        config.addTypedef(coreRoot + "/browser/window.js");

    }
}
