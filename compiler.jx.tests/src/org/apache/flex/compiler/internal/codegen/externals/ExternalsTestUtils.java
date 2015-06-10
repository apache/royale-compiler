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

import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;
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

    public static void addTestExcludesFull(ReferenceModel model)
    {
        model.addFieldExclude("Window", "focus");
        model.addClassExclude("controlRange");

        model.addExclude("Array", "toSource");
        model.addExclude("Date", "valueOf");
        model.addExclude("String", "valueOf");

        model.addExclude("FontFaceSet", "delete");

        model.addExclude("CSSStyleDeclaration", "cssText");
        model.addExclude("CSSStyleRule", "style");
        model.addExclude("CSSFontFaceRule", "style");
        model.addExclude("CSSPageRule", "style");

        model.addExclude("Generator", "throw");
        model.addExclude("Generator", "return");
        model.addExclude("HTMLMenuItemElement", "default");
        model.addExclude("MessageEvent", "data"); // TODO returns T
        model.addExclude("MessageEvent", "initMessageEventNS"); // TODO param T
        model.addExclude("MessageEvent", "initMessageEvent"); // TODO param T
        model.addExclude("MessageEvent", "default");
        model.addExclude("Object", "is");
        model.addExclude("Promise", "catch");

        model.addExclude("IDBCursor", "continue");
        model.addExclude("IDBCursor", "delete");
        model.addExclude("IDBObjectStore", "delete");
    }

    public static void addTestExternalsFull(ReferenceModel model)
            throws IOException
    {
        String coreRoot = ExternalsTestUtils.EXTERNAL_JS_DIR.getAbsolutePath();

        model.addExternal(ExternalsTestUtils.MISSING_JS_FILE);
        model.addExternal(coreRoot + "/es3.js");
        model.addExternal(coreRoot + "/es5.js");
        model.addExternal(coreRoot + "/es6.js");

        model.addExternal(coreRoot + "/w3c_anim_timing.js");
        model.addExternal(coreRoot + "/w3c_audio.js");
        model.addExternal(coreRoot + "/w3c_batterystatus.js");
        model.addExternal(coreRoot + "/w3c_css.js");
        model.addExternal(coreRoot + "/w3c_css3d.js");
        model.addExternal(coreRoot + "/w3c_device_sensor_event.js");
        model.addExternal(coreRoot + "/w3c_dom1.js");
        model.addExternal(coreRoot + "/w3c_dom2.js");
        model.addExternal(coreRoot + "/w3c_dom3.js");
        model.addExternal(coreRoot + "/w3c_elementtraversal.js");
        model.addExternal(coreRoot + "/w3c_encoding.js");
        model.addExternal(coreRoot + "/w3c_event.js");
        model.addExternal(coreRoot + "/w3c_event3.js");
        model.addExternal(coreRoot + "/w3c_geolocation.js");
        model.addExternal(coreRoot + "/w3c_indexeddb.js");
        model.addExternal(coreRoot + "/w3c_navigation_timing.js");
        model.addExternal(coreRoot + "/w3c_range.js");
        model.addExternal(coreRoot + "/w3c_rtc.js");
        model.addExternal(coreRoot + "/w3c_selectors.js");
        //model.addExternal(coreRoot + "/w3c_serviceworker.js");
        //model.addExternal(coreRoot + "/w3c_webcrypto.js");
        model.addExternal(coreRoot + "/w3c_xml.js");

        //model.addExternal(coreRoot + "/fetchapi");

        model.addExternal(coreRoot + "/window.js");

        model.addExternal(coreRoot + "/ie_dom.js");
        model.addExternal(coreRoot + "/gecko_dom.js");

        model.addExternal(coreRoot + "/webkit_css.js");
        model.addExternal(coreRoot + "/webkit_dom.js");
        model.addExternal(coreRoot + "/webkit_event.js");
        //model.addExternal(coreRoot + "/webkit_notifications.js");

        model.addExternal(coreRoot + "/iphone.js");
        model.addExternal(coreRoot + "/chrome.js");
        model.addExternal(coreRoot + "/flash.js");

        model.addExternal(coreRoot + "/page_visibility.js");
        model.addExternal(coreRoot + "/fileapi.js");
        model.addExternal(coreRoot + "/html5.js");

        model.addExternal(coreRoot + "/webgl.js");
        model.addExternal(coreRoot + "/webstorage.js");
    }
}
