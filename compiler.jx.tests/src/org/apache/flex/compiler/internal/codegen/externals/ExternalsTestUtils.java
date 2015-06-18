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
            FilenameNormalization.normalize("temp"));

    // XXX missing.js is a temp location until we can figure out where it should placed in the build
    public static File MISSING_JS_FILE = FilenameNormalization.normalize(new File(
            "test-files/externals/missing.js"));

    public static File EXTERNAL_JS_DIR = FilenameNormalization.normalize(new File(
            "../closure-compiler/externs"));

    public static File AS_ROOT_DIR = new File(TEMP_DIR, "externals/as");

    public static void addTestExcludesFull(ExternCConfiguration config)
    {
        config.addFieldExclude("Window", "focus");
        config.addClassExclude("controlRange");

        config.addExclude("Array", "toSource");
        config.addExclude("Date", "valueOf");
        config.addExclude("String", "valueOf");

        config.addExclude("FontFaceSet", "delete");

        config.addExclude("CSSStyleDeclaration", "cssText");
        config.addExclude("CSSStyleRule", "style");
        config.addExclude("CSSFontFaceRule", "style");
        config.addExclude("CSSPageRule", "style");

        config.addExclude("Generator", "throw");
        config.addExclude("Generator", "return");
        config.addExclude("HTMLMenuItemElement", "default");
        config.addExclude("MessageEvent", "data"); // TODO returns T
        config.addExclude("MessageEvent", "initMessageEventNS"); // TODO param T
        config.addExclude("MessageEvent", "initMessageEvent"); // TODO param T
        config.addExclude("MessageEvent", "default");
        config.addExclude("Object", "is");
        config.addExclude("Promise", "catch");

        config.addExclude("IDBCursor", "continue");
        config.addExclude("IDBCursor", "delete");
        config.addExclude("IDBObjectStore", "delete");

        // TODO method treated like field
        config.addFieldExclude("Iterator", "next");
        config.addExclude("Generator", "next");
        config.addExclude("LinkStyle", "sheet");

        // SVG
        config.addExclude("SVGStylable", "className");
        config.addExclude("SVGStylable", "style");
        config.addExclude("SVGLocatable", "farthestViewportElement");
        config.addExclude("SVGLocatable", "nearestViewportElement");
    }

    public static void addTestExternalsFull(ExternCConfiguration config)
            throws IOException
    {
        String coreRoot = ExternalsTestUtils.EXTERNAL_JS_DIR.getAbsolutePath();

        config.addExternal(ExternalsTestUtils.MISSING_JS_FILE);
        config.addExternal(coreRoot + "/es3.js");
        config.addExternal(coreRoot + "/es5.js");
        config.addExternal(coreRoot + "/es6.js");

        config.addExternal(coreRoot + "/w3c_anim_timing.js");
        config.addExternal(coreRoot + "/w3c_audio.js");
        config.addExternal(coreRoot + "/w3c_batterystatus.js");
        config.addExternal(coreRoot + "/w3c_css.js");
        config.addExternal(coreRoot + "/w3c_css3d.js");
        config.addExternal(coreRoot + "/w3c_device_sensor_event.js");
        config.addExternal(coreRoot + "/w3c_dom1.js");
        config.addExternal(coreRoot + "/w3c_dom2.js");
        config.addExternal(coreRoot + "/w3c_dom3.js");
        config.addExternal(coreRoot + "/w3c_elementtraversal.js");
        config.addExternal(coreRoot + "/w3c_encoding.js");
        config.addExternal(coreRoot + "/w3c_event.js");
        config.addExternal(coreRoot + "/w3c_event3.js");
        config.addExternal(coreRoot + "/w3c_geolocation.js");
        config.addExternal(coreRoot + "/w3c_indexeddb.js");
        config.addExternal(coreRoot + "/w3c_navigation_timing.js");
        config.addExternal(coreRoot + "/w3c_range.js");
        config.addExternal(coreRoot + "/w3c_rtc.js");
        config.addExternal(coreRoot + "/w3c_selectors.js");
        //model.addExternal(coreRoot + "/w3c_serviceworker.js");
        //model.addExternal(coreRoot + "/w3c_webcrypto.js");
        config.addExternal(coreRoot + "/w3c_xml.js");

        //model.addExternal(coreRoot + "/fetchapi");

        config.addExternal(coreRoot + "/window.js");

        config.addExternal(coreRoot + "/ie_dom.js");
        config.addExternal(coreRoot + "/gecko_dom.js");

        config.addExternal(coreRoot + "/webkit_css.js");
        config.addExternal(coreRoot + "/webkit_dom.js");
        config.addExternal(coreRoot + "/webkit_event.js");
        //model.addExternal(coreRoot + "/webkit_notifications.js");

        config.addExternal(coreRoot + "/iphone.js");
        config.addExternal(coreRoot + "/chrome.js");
        config.addExternal(coreRoot + "/flash.js");

        config.addExternal(coreRoot + "/page_visibility.js");
        config.addExternal(coreRoot + "/fileapi.js");
        config.addExternal(coreRoot + "/html5.js");

        config.addExternal(coreRoot + "/webgl.js");
        config.addExternal(coreRoot + "/webstorage.js");

        //config.addExternal(coreRoot + "/svg.js");
    }
}
