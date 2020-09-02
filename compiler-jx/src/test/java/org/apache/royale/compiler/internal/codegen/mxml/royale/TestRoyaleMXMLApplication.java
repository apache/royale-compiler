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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.driver.js.royale.JSCSSCompilationSession;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.RoyaleTestBase;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestRoyaleMXMLApplication extends RoyaleTestBase
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    public void setUp()
    {
        super.setUp();
    	((RoyaleJSProject)project).config = new JSGoogConfiguration();
    }

    @Test
    public void testCSSComplexSelectors()
    {
        String fileName = "CSSTest";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "royale/files").getPath(), false);
		assertNotNull(node);

        mxmlBlockWalker.visitFile(node);
        
        JSCSSCompilationSession jscss = (JSCSSCompilationSession)project.getCSSCompilationSession();

        String result = getCodeFromFile("CSSTestSource_result.css", "royale/files");
        String output = jscss.emitCSS();       
        assertThat(output, is(result));
        String encoding = jscss.getEncodedCSS();
        result = getCodeFromFile("CSSTestSource_encoded_result.txt", "royale/files");
        assertThat(encoding, is(result));
        

    }

    @Test
    public void testFile()
    {
        String fileName = "wildcard_import";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "royale/files").getPath(), false);
		assertNotNull(node);

        mxmlBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "royale/files"));
    }

    @Test
    public void testRoyaleMainFile()
    {
        String fileName = "RoyaleTest_again";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "royale/files").getPath(), false);
		assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "royale/files"));
    }

    @Test
    public void testRoyaleInitialViewFile()
    {
        String fileName = "MyInitialView";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "royale/files").getPath(), false);
		assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "royale/files"));
    }

    @Test
    public void testBackslashStringAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
                + "<basic:beads><basic:TextPromptBead prompt=\"0-9\\\"/></basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.html.accessories.TextPromptBead');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.accessories.TextPromptBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
        		"    null,\n" +
        		"    [\n" +
        		"      org.apache.royale.html.accessories.TextPromptBead,\n" +
        		"      2,\n" +
        		"      '_id',\n" +
        		"      true,\n" +
        		"      '$ID_8_0',\n" +
        		"      'prompt',\n" +
        		"      true,\n" +
        		"      '0-9\\\\',\n" +
        		"      0,\n" +
        		"      0,\n" +
        		"      null\n" +
        		"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testAlreadyEscapedBackslashStringAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
                + "<basic:beads><basic:TextPromptBead prompt=\"0-9\\\\\"/></basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.html.accessories.TextPromptBead');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.accessories.TextPromptBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
        		"    null,\n" +
        		"    [\n" +
        		"      org.apache.royale.html.accessories.TextPromptBead,\n" +
        		"      2,\n" +
        		"      '_id',\n" +
        		"      true,\n" +
        		"      '$ID_8_0',\n" +
        		"      'prompt',\n" +
        		"      true,\n" +
        		"      '0-9\\\\',\n" +
        		"      0,\n" +
        		"      0,\n" +
        		"      null\n" +
        		"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testInterfaceAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\" implements=\"org.apache.royale.core.IChrome\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.royale.core.IChrome;"
                + "]]></fx:Script></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.IChrome');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" * @implements {org.apache.royale.core.IChrome}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }], interfaces: [org.apache.royale.core.IChrome] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testTwoInterfaceAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\" implements=\"org.apache.royale.core.IChrome, org.apache.royale.core.IPopUp\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.royale.core.IPopUp;"
                + "    import org.apache.royale.core.IChrome;"
                + "]]></fx:Script></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.IChrome');\n" +
        		"goog.require('org.apache.royale.core.IPopUp');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" * @implements {org.apache.royale.core.IChrome}\n" +
        		" * @implements {org.apache.royale.core.IPopUp}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }], interfaces: [org.apache.royale.core.IChrome, org.apache.royale.core.IPopUp] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testConstantBinding()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.royale.net.HTTPConstants;"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{HTTPConstants.GET}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.Label');\n" +
        		"goog.require('org.apache.royale.net.HTTPConstants');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
				"  this.generateMXMLAttributes([\n" +
				"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n"+
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.Label,\n" +
				"        1,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" + 
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"org.apache.royale.net.HTTPConstants\", \"GET\"],\n" +
        		"null,\n" +
        		"[\"$ID_8_0\", \"text\"]\n" +
        		"];\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testConstantBindingQname()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.royale.net.HTTPConstants;"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{org.apache.royale.net.HTTPConstants.GET}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.Label');\n" +
        		"goog.require('org.apache.royale.net.HTTPConstants');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" + 
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.Label,\n" +
				"        1,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"org.apache.royale.net.HTTPConstants\", \"GET\"],\n" +
        		"null,\n" +
        		"[\"$ID_8_0\", \"text\"]\n" +
        		"];\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }
    
    @Test
    public void testChainBinding()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import binding.ComplexValueObject;\n"
        		+ "    public var firstOne:ComplexValueObject;\n"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{firstOne.subObject.labelText}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.Label');\n" +
        		"goog.require('binding.ComplexValueObject');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
				"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" + 
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.Label,\n" +
				"        1,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @type {binding.ComplexValueObject}\n" +
        		" */\n" +
        		"AppName.prototype.firstOne = null;\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"firstOne\", \"subObject\", \"labelText\"],\n" +
        		"null,\n" +
        		"[\"$ID_8_0\", \"text\"],\n" +
        		"0,\n" +
        		"2,\n" +
        		"\"firstOne\",\n" +
        		"null,\n" +
        		"0,\n" +
        		"null,\n" +
        		"[\n" +
        		"1,\n" +
        		"2,\n" +
        		"\"subObject\",\n" +
        		"\"subObjectChanged\",\n" +
        		"0,\n" +
        		"null,\n" +
        		"[\n" +
        		"2,\n" +
        		"2,\n" +
        		"\"labelText\",\n" +
        		"\"valueChange\",\n" +
        		"0,\n" +
        		"null,\n" +
        		"null,\n" +
        		"null],\n" +
        		"null]];\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'firstOne': { type: 'binding.ComplexValueObject', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.firstOne = v : inst.firstOne;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testXMLUsage()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA["
        		+ "    public var xml:XML = new XML();\n"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"Hello World\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.Label');\n" +
//        		"goog.require('XML');\n" +
//        		"goog.require('XML');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.xml = new XML();\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.Label,\n" +
				"        2,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        'text',\n" +
				"        true,\n" +
				"        'Hello World',\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @type {XML}\n" +
        		" */\n" +
        		"AppName.prototype.xml = null;\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'xml': { type: 'XML', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.xml = v : inst.xml;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";


        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }

    @Test
    public void testFXStringDeclaration()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Declarations><fx:String id=\"foo\">Ro'ale</fx:String>"
                + "</fx:Declarations><basic:initialView><basic:View><basic:DropDownList dataProvider=\"['Hello', 'World']\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.DropDownList');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.foo = 'Ro\\'ale';\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.DropDownList}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.DropDownList,\n" +
				"        2,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        'dataProvider',\n" +
				"        true,\n" +
				"        ['Hello','World'],\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {string}\n" +
        		" */\n" +
        		"AppName.prototype.foo;\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'foo': { type: 'String', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.foo = v : inst.foo;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" + 
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXArrayDeclaration()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Declarations><fx:Array id=\"foo\"><fx:String>Royale</fx:String><fx:String>Rules</fx:String></fx:Array>"
                + "</fx:Declarations><basic:initialView><basic:View><basic:DropDownList dataProvider=\"['Hello', 'World']\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.DropDownList');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.foo = ['Royale', 'Rules'];\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_3;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.DropDownList}\n" +
        		"   */\n" +
        		"  this.$ID_8_2;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_3',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.DropDownList,\n" +
				"        2,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_2',\n" +
				"        'dataProvider',\n" +
				"        true,\n" +
				"        ['Hello','World'],\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {Array}\n" +
        		" */\n" +
        		"AppName.prototype.foo;\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'foo': { type: 'Array', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.foo = v : inst.foo;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" + 
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXXMLDeclaration()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Declarations><fx:XML id=\"foo\"><root><menuitem label=\"foo\"/><menuitem label=\"bar\"/></root></fx:XML>"
                + "</fx:Declarations><basic:initialView><basic:View><basic:DropDownList dataProvider=\"['Hello', 'World']\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.DropDownList');\n" +
//        		"goog.require('XML');\n" +
//        		"goog.require('XML');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.foo = new XML('<root><menuitem label=\\\"foo\\\"\\/><menuitem label=\\\"bar\\\"\\/><\\/root>');\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.DropDownList}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.DropDownList,\n" +
				"        2,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        'dataProvider',\n" +
				"        true,\n" +
				"        ['Hello','World'],\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {XML}\n" +
        		" */\n" +
        		"AppName.prototype.foo;\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'foo': { type: 'XML', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.foo = v : inst.foo;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" + 
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }

    @Test
    public void testFXXMLListDeclaration()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Declarations><fx:XMLList id=\"foo\"><item><name>foo</name></item><item><name>bar</name></item></fx:XMLList>"
                + "</fx:Declarations><basic:initialView><basic:View><basic:DropDownList dataProvider=\"['Hello', 'World']\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.core.View');\n" +
        		"goog.require('org.apache.royale.html.DropDownList');\n" +
//        		"goog.require('XMLList');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.foo = new XMLList('<item><name>foo<\\/name><\\/item><item><name>bar<\\/name><\\/item>');\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.core.View}\n" +
        		"   */\n" +
        		"  this.$ID_8_1;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.html.DropDownList}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'initialView',\n" +
        		"    false,\n" +
				"    [\n" +
				"      org.apache.royale.core.View,\n" +
				"      1,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_1',\n" +
				"      0,\n" +
				"      0,\n" +
				"      [\n" +
				"        org.apache.royale.html.DropDownList,\n" +
				"        2,\n" +
				"        '_id',\n" +
				"        true,\n" +
				"        '$ID_8_0',\n" +
				"        'dataProvider',\n" +
				"        true,\n" +
				"        ['Hello','World'],\n" +
				"        0,\n" +
				"        0,\n" +
				"        null\n" +
				"      ]\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {XMLList}\n" +
        		" */\n" +
        		"AppName.prototype.foo;\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'foo': { type: 'XMLList', get_set: function (/** AppName */ inst, /** * */ v) {return v !== undefined ? inst.foo = v : inst.foo;}}\n" +
        		"      };\n" +
        		"    },\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" + 
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXComponentEventHandler()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Declarations><fx:Component><basic:DropDownList change=\"trace('bar')\" />"
                + "</fx:Component></fx:Declarations></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"goog.provide('AppName.AppName_component0');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.events.Event');\n" +
        		"goog.require('org.apache.royale.html.DropDownList');\n" +
        		"goog.require('org.apache.royale.core.ClassFactory');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.html.DropDownList}\n" +
        		" */\n" +
        		"AppName.AppName_component0 = function() {\n" +
        		"  AppName.AppName_component0.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    0,\n" +
        		"    0,\n" +
        		"    1,\n" +
        		"    'change',\n" +
        		"this.$EH_9_0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName.AppName_component0, org.apache.royale.html.DropDownList);\n" +
          		"\n" +
				"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.AppName_component0.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName_component0', qName: 'AppName.AppName_component0', kind: 'class'  }] };\n" +
				"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.AppName_component0.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName_component0': { type: '', declaredBy: 'AppName_component0'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.AppName_component0.prototype.ROYALE_COMPILE_FLAGS = 9;" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @param {org.apache.royale.events.Event} event\n" +
        		" */\n" +
        		"AppName.AppName_component0.prototype.$EH_9_0 = function(event)\n" +
				"{\n" +
				"  org.apache.royale.utils.Language.trace('bar');\n" +
				"};\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXComponentFunctionAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA[public function fn_test(foo:String):void {}]]></fx:Script><basic:beads><basic:DataTipBead labelFunction=\"fn_test\" />"
                + "</basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.charts.beads.DataTipBead');\n" +
//        		"goog.require('org.apache.royale.utils.Language');\n" + // in real compiles this will be output, but not in tests
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.charts.beads.DataTipBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
				"    null,\n" +
				"    [\n" +
				"      org.apache.royale.charts.beads.DataTipBead,\n" +
				"      2,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_0',\n" +
				"      'labelFunction',\n" +
				"      true,\n" +
				"      org.apache.royale.utils.Language.closure(this.fn_test, this, 'fn_test'),\n" +
				"      0,\n" +
				"      0,\n" +
				"      null\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @param {string} foo\n" +
        		" */\n" +
        		"AppName.prototype.fn_test = function(foo) {\n" +
          		"};" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'fn_test': { type: 'void', declaredBy: 'AppName', parameters: function () { return [ 'String', false ]; }},\n"+
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXComponentFunctionAttributeFromAnotherObject()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<basic:beads><basic:DataTipBead labelFunction=\"initialView.addedToParent\" />"
                + "</basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.charts.beads.DataTipBead');\n" +
//        		"goog.require('org.apache.royale.utils.Language');\n" + // in real compiles this will be output, but not in tests
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.charts.beads.DataTipBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
				"    null,\n" +
				"    [\n" +
				"      org.apache.royale.charts.beads.DataTipBead,\n" +
				"      2,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_0',\n" +
				"      'labelFunction',\n" +
				"      true,\n" +
				"      org.apache.royale.utils.Language.closure(this.initialView.addedToParent, this.initialView, 'addedToParent'),\n" +
				"      0,\n" +
				"      0,\n" +
				"      null\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testFXComponentFunctionAttributeStatic()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA[public static function fn_test(foo:String):void {}]]></fx:Script><basic:beads><basic:DataTipBead labelFunction=\"fn_test\" />"
                + "</basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.charts.beads.DataTipBead');\n" +
//        		"goog.require('org.apache.royale.utils.Language');\n" + // in real compiles this will be output, but not in tests
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.charts.beads.DataTipBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
				"    null,\n" +
				"    [\n" +
				"      org.apache.royale.charts.beads.DataTipBead,\n" +
				"      2,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_0',\n" +
				"      'labelFunction',\n" +
				"      true,\n" +
				"      AppName.fn_test,\n" +
				"      0,\n" +
				"      0,\n" +
				"      null\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
				"/**\n" +
				" * @nocollapse\n" + 
        		" * @param {string} foo\n" +
        		" */\n" +
        		"AppName.fn_test = function(foo) {\n" +
          		"};" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        '|fn_test': { type: 'void', declaredBy: 'AppName', parameters: function () { return [ 'String', false ]; }},\n"+
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
        		"/**\n" +
        		" * Provide reflection support for distinguishing dynamic fields on class object (static)\n" +
        		" * @const\n" +
        		" * @type {Array<string>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_INITIAL_STATICS = Object.keys(AppName);";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }

    @Test
    public void testFXComponentPrivateFunctionAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
        		+ "<fx:Script><![CDATA[private function fn_test(foo:String):void {}]]></fx:Script><basic:beads><basic:DataTipBead labelFunction=\"fn_test\" />"
                + "</basic:beads></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, RoyaleTestBase.WRAP_LEVEL_NONE);

        RoyaleJSProject project = (RoyaleJSProject)mxmlBlockWalker.getProject();
        project.setAllowPrivateNameConflicts(true);
        ((JSRoyaleEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n" +
        		" * AppName\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('AppName');\n" +
        		"\n" +
        		"goog.require('org.apache.royale.core.Application');\n" +
        		"goog.require('org.apache.royale.charts.beads.DataTipBead');\n" +
//        		"goog.require('org.apache.royale.utils.Language');\n" + // in real compiles this will be output, but not in tests
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.royale.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.royale.charts.beads.DataTipBead}\n" +
        		"   */\n" +
        		"  this.$ID_8_0;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldd;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {Array}\n" +
        		"   */\n" +
        		"  this.mxmldp;\n" +
        		"\n" +
        		"  this.generateMXMLAttributes([\n" +
        		"    1,\n" +
        		"    'beads',\n" +
				"    null,\n" +
				"    [\n" +
				"      org.apache.royale.charts.beads.DataTipBead,\n" +
				"      2,\n" +
				"      '_id',\n" +
				"      true,\n" +
				"      '$ID_8_0',\n" +
				"      'labelFunction',\n" +
				"      true,\n" +
				"      org.apache.royale.utils.Language.closure(this.AppName_fn_test, this, 'AppName_fn_test'),\n" +
				"      0,\n" +
				"      0,\n" +
				"      null\n" +
				"    ],\n" +
        		"    0,\n" +
        		"    0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @private\n" +
        		" * @param {string} foo\n" +
        		" */\n" +
        		"AppName.prototype.AppName_fn_test = function(foo) {\n" +
          		"};" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"/**\n" +
        		" * @const\n" +
        		" * @type {number}\n" +
        		" */\n" +
        		"AppName.prototype.ROYALE_COMPILE_FLAGS = 9;";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }
    
    @Test
    public void testRoyaleMainFileDual()
    {
        MXMLJSC mxmlc = new MXMLJSC();
        String[] args = new String[11];
        args[0] = "-compiler.targets=SWF,JSRoyale";
        args[1] = "-compiler.allow-subclass-overrides";
        args[2] = "-remove-circulars";
        args[3] = "-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs")).getPath();
        args[4] = "-js-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs")).getPath();
        args[5] = "-external-library-path+=" + testAdapter.getPlayerglobal().getPath();
        args[6] = "-js-external-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/js/libs/js.swc")).getPath();
        args[7] = "-js-external-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/js/libs/GCL.swc")).getPath();
        args[8] = "-output=" + new File(testAdapter.getTempDir(), "bin-debug/RoyaleTest_again.swf").getPath();
        if (env.GOOG != null)
        	args[9] = "-closure-lib=" + new File(FilenameNormalization.normalize(env.GOOG)).getPath();
        else
        	args[9] = "-define=COMPILE::temp,false";
        args[10] = new File(testAdapter.getUnitTestBaseDir(), "royale/files/RoyaleTest_again.mxml").getPath();

        ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        int result = mxmlc.mainNoExit(args, problems, true);
        assertThat(result, is(0));
    }

    @Test
    public void testRoyaleMainFileDualFlash()
    {
    	/* this should error because a Flash APi is used */
        MXMLJSC mxmlc = new MXMLJSC();
        String[] args = new String[19];
        args[0] = "-compiler.targets=SWF,JSRoyale";
        args[1] = "-remove-circulars";
        args[2] = "-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs/Core.swc")).getPath();
        args[3] = "-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs/Binding.swc")).getPath();
        args[4] = "-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs/Network.swc")).getPath();
        args[5] = "-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs/Collections.swc")).getPath();
        args[6] = "-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/libs/Basic.swc")).getPath();
        args[7] = "-external-library-path+=" + testAdapter.getPlayerglobal().getPath();
        args[8] = "-output=" + new File(testAdapter.getTempDir(), "bin-debug/RoyaleTest_again_Flash.swf").getPath();
        if (env.GOOG != null)
        	args[9] = "-closure-lib=" + new File(FilenameNormalization.normalize(env.GOOG)).getPath();
        else
        	args[9] = "-define=COMPILE::temp,false";
        args[10] = "-compiler.allow-subclass-overrides";
        args[11] = "-compiler.js-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs/CoreJS.swc")).getPath();
        args[12] = "-compiler.js-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs/BindingJS.swc")).getPath();
        args[13] = "-compiler.js-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs/NetworkJS.swc")).getPath();
        args[14] = "-compiler.js-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs/CollectionsJS.swc")).getPath();
        args[15] = "-compiler.js-library-path+=" + new File(FilenameNormalization.normalize(env.ASJS + "/frameworks/js/libs/BasicJS.swc")).getPath();
        args[16] = "-compiler.js-external-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/js/libs/js.swc")).getPath();
        args[17] = "-compiler.js-external-library-path=" + new File(FilenameNormalization.normalize(env.ASJS + "/js/libs/GCL.swc")).getPath();
        args[18] = new File(testAdapter.getUnitTestBaseDir(), "royale/files/RoyaleTest_again_Flash.mxml").getPath();

        int result = mxmlc.mainNoExit(args, errors, true);
        assertThat(result, is(3));
        assertErrors("Access of possibly undefined property scrollRect.");
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "royale/files"));
        super.addSourcePaths(sourcePaths);
    }

}
