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

import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Test;

public class TestFlexJSMXMLScript extends FlexJSTestBase
{

    @Test
    public void testSuperInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    override public function addedToParent():void {"
                + "    super.addedToParent();}"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, FlexJSTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
				" * @export\n" +
				" * @override\n" +
				" */\n" +
				"AppName.prototype.addedToParent = function() {\n" +
				"  AppName.superClass_.addedToParent.apply(this);\n" +
				"};\n" +
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
        		"    variables: function () {return {};},\n" +
        		"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
        		"        'addedToParent': { type: 'void', declaredBy: 'AppName'},\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n" +
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";
        	
        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testFunctionAssignmentInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    private var foo:Function = bar;"
                + "    public function bar():void {};"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, FlexJSTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"  this.foo = org.apache.flex.utils.Language.closure(this.bar, this, 'bar');\n" +
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
				" * @private\n" +
				" * @type {Function}\n" +
				" */\n" +
				"AppName.prototype.foo;\n" +
        		"\n" +
				"\n" +
				"/**\n" +
				" * @export\n" +
				" */\n" +
				"AppName.prototype.bar = function() {\n" +
				"};\n" +
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
        		"    variables: function () {return {};},\n" +
        		"    accessors: function () {return {};},\n" +
        		"    methods: function () {\n" +
        		"      return {\n" +
        		"        'bar': { type: 'void', declaredBy: 'AppName'},\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n" +
        		"      };\n" +
        		"    }\n" +
        		"  };\n" +
        		"};\n" +
        		"\n" +
        		"\n";
        	
        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }

    @Test
    public void testComplexInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    public var foo:Array = ['foo'];"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, FlexJSTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"  this.foo = ['foo'];\n" +
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
				" * @export\n" +
				" * @type {Array}\n" +
				" */\n" +
				"AppName.prototype.foo;\n" +
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
				"        'foo': { type: 'Array'}\n" +
				"      };\n" +
				"    },\n" +
				"    accessors: function () {return {};},\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'AppName': { type: '', declaredBy: 'AppName'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
        		"\n" +
        		"\n" ;
        	
        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }
}
