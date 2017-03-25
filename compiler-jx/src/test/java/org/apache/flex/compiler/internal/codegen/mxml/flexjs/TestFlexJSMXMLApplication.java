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
package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.driver.js.flexjs.JSCSSCompilationSession;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.utils.ITestAdapter;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    public void setUp()
    {
        super.setUp();
    	((FlexJSProject)project).config = new JSGoogConfiguration();
    }

    @Test
    public void testCSSComplexSelectors()
    {
        String fileName = "CSSTest";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "flexjs/files").getPath(), false);

        mxmlBlockWalker.visitFile(node);
        
        JSCSSCompilationSession jscss = (JSCSSCompilationSession)project.getCSSCompilationSession();
        jscss.setExcludeDefaultsCSSFiles(new ArrayList<String>());

        String result = getCodeFromFile("CSSTestSource_result.css", "flexjs/files");
        String output = jscss.emitCSS();       
        assertThat(output, is(result));

    }

    @Test
    public void testFile()
    {
        String fileName = "wildcard_import";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "flexjs/files").getPath(), false);

        mxmlBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    @Test
    public void testFlexJSMainFile()
    {
        String fileName = "FlexJSTest_again";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "flexjs/files").getPath(), false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    @Test
    public void testFlexJSInitialViewFile()
    {
        String fileName = "MyInitialView";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "flexjs/files").getPath(), false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    @Test
    public void testInterfaceAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\" implements=\"org.apache.flex.core.IChrome\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.flex.core.IChrome;"
                + "]]></fx:Script></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.IChrome');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
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
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }], interfaces: [org.apache.flex.core.IChrome] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {return {};},\n" +
				"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testTwoInterfaceAttribute()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\" implements=\"org.apache.flex.core.IChrome, org.apache.flex.core.IPopUp\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.flex.core.IPopUp;"
                + "    import org.apache.flex.core.IChrome;"
                + "]]></fx:Script></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.IChrome');\n" +
        		"goog.require('org.apache.flex.core.IPopUp');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
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
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }], interfaces: [org.apache.flex.core.IChrome, org.apache.flex.core.IPopUp] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    variables: function () {return {};},\n" +
				"    accessors: function () {return {};},\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
        		"\n" +
        		"\n" ;

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testConstantBinding()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.flex.net.HTTPConstants;"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{HTTPConstants.GET}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.View');\n" +
        		"goog.require('org.apache.flex.html.Label');\n" +
        		"goog.require('org.apache.flex.net.HTTPConstants');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.core.View}\n" +
        		"   */\n" +
        		"  this.$ID1_;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID0_;\n" +
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
        		"  this.generateMXMLAttributes\n" +
        		"  ([1,\n" +
        		"'initialView',\n" +
        		"false,\n" +
        		"[org.apache.flex.core.View, 1, '_id', true, '$ID1', 0, 0, [org.apache.flex.html.Label, 1, '_id', true, '$ID0', 0, 0, null]],\n" +
        		"0,\n" +
        		"0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"org.apache.flex.net.HTTPConstants\", \"GET\"],\n" +
        		"null,\n" +
        		"[\"$ID0\", \"text\"],\n" +
        		"];\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {return {};},\n" +
				"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testConstantBindingQname()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import org.apache.flex.net.HTTPConstants;"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{org.apache.flex.net.HTTPConstants.GET}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.View');\n" +
        		"goog.require('org.apache.flex.html.Label');\n" +
        		"goog.require('org.apache.flex.net.HTTPConstants');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.core.View}\n" +
        		"   */\n" +
        		"  this.$ID1_;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID0_;\n" +
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
        		"  this.generateMXMLAttributes\n" +
        		"  ([1,\n" +
        		"'initialView',\n" +
        		"false,\n" +
        		"[org.apache.flex.core.View, 1, '_id', true, '$ID1', 0, 0, [org.apache.flex.html.Label, 1, '_id', true, '$ID0', 0, 0, null]],\n" +
        		"0,\n" +
        		"0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"org.apache.flex.net.HTTPConstants\", \"GET\"],\n" +
        		"null,\n" +
        		"[\"$ID0\", \"text\"],\n" +
        		"];\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {return {};},\n" +
				"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }
    
    @Test
    public void testChainBinding()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
        		+ "<fx:Script><![CDATA["
                + "    import binding.ComplexValueObject;\n"
        		+ "    public var firstOne:ComplexValueObject;\n"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"{firstOne.subObject.labelText}\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.View');\n" +
        		"goog.require('org.apache.flex.html.Label');\n" +
        		"goog.require('binding.ComplexValueObject');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.core.View}\n" +
        		"   */\n" +
        		"  this.$ID1_;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID0_;\n" +
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
        		"  this.generateMXMLAttributes\n" +
        		"  ([1,\n" +
        		"'initialView',\n" +
        		"false,\n" +
        		"[org.apache.flex.core.View, 1, '_id', true, '$ID1', 0, 0, [org.apache.flex.html.Label, 1, '_id', true, '$ID0', 0, 0, null]],\n" +
        		"0,\n" +
        		"0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {binding.ComplexValueObject}\n" +
        		" */\n" +
        		"AppName.prototype.firstOne;\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" */\n" +
        		"AppName.prototype._bindings = [\n" +
        		"1,\n" +
        		"[\"firstOne\", \"subObject\", \"labelText\"],\n" +
        		"null,\n" +
        		"[\"$ID0\", \"text\"],\n" +
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
        		"\"propertyChange\",\n" +
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
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'firstOne': { type: 'binding.ComplexValueObject'}\n" +
        		"      };\n" +
        		"    },\n" +
				"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";

        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testXMLUsage()
    {
        String code = "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
        		+ "<fx:Script><![CDATA["
        		+ "    public var xml:XML = new XML();\n"
                + "]]></fx:Script><basic:initialView><basic:View><basic:Label text=\"Hello World\"/></basic:View></basic:initialView></basic:Application>";

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) getNode(code,
        		IMXMLDocumentNode.class, FlexJSTestBase.WRAP_LEVEL_NONE);

        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).getModel().setCurrentClass(dnode.getDefinition());
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
        		"goog.require('org.apache.flex.core.Application');\n" +
        		"goog.require('org.apache.flex.core.View');\n" +
        		"goog.require('org.apache.flex.html.Label');\n" +
        		"goog.require('XML');\n" +
        		"goog.require('XML');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" * @extends {org.apache.flex.core.Application}\n" +
        		" */\n" +
        		"AppName = function() {\n" +
        		"  AppName.base(this, 'constructor');\n" +
        		"  \n" +
        		"  this.xml = new XML();\n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.core.View}\n" +
        		"   */\n" +
        		"  this.$ID1_;\n" +
        		"  \n" +
        		"  /**\n" +
        		"   * @private\n" +
        		"   * @type {org.apache.flex.html.Label}\n" +
        		"   */\n" +
        		"  this.$ID0_;\n" +
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
        		"  this.generateMXMLAttributes\n" +
        		"  ([1,\n" +
        		"'initialView',\n" +
        		"false,\n" +
        		"[org.apache.flex.core.View, 1, '_id', true, '$ID1', 0, 0, [org.apache.flex.html.Label, 2, '_id', true, '$ID0', 'text', true, 'Hello World', 0, 0, null]],\n" +
        		"0,\n" +
        		"0\n" +
        		"  ]);\n" +
        		"  \n" +
        		"};\n" +
        		"goog.inherits(AppName, org.apache.flex.core.Application);\n" +
        		"\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * @export\n" +
        		" * @type {XML}\n" +
        		" */\n" +
        		"AppName.prototype.xml;\n" +
        		"\n" +
				"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName', kind: 'class'  }] };\n" +
          		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Prevent renaming of class. Needed for reflection.\n" +
        		" */\n" +
        		"goog.exportSymbol('AppName', AppName);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"AppName.prototype.FLEXJS_REFLECTION_INFO = function () {\n" +
        		"  return {\n" +
        		"    variables: function () {\n" +
        		"      return {\n" +
        		"        'xml': { type: 'XML'}\n" +
        		"      };\n" +
        		"    },\n" +
				"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n"+
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};";

        assertOutMXMLPostProcess(outTemplate.replaceAll("AppName", appName), true);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "flexjs/files"));
        super.addSourcePaths(sourcePaths);
    }

}
