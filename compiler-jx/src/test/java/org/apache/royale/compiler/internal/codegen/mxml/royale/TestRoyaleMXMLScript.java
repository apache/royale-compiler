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

import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.test.RoyaleTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Test;

public class TestRoyaleMXMLScript extends RoyaleTestBase
{

    @Test
    public void testSuperInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    override public function addedToParent():void {"
                + "    super.addedToParent();}"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"  this.foo = org.apache.royale.utils.Language.closure(this.bar, this, 'bar');\n" +
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
				" * @private\n" +
				" * @type {Function}\n" +
				" */\n" +
				"AppName.prototype.foo;\n" +
        		"\n" +
				"\n" +
				"/**\n" +
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
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"goog.inherits(AppName, org.apache.royale.core.Application);\n" +
          		"\n" +
        		"\n" +
        		"\n" +
				"\n" +
				"/**\n" +
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
				"        'AppName': { type: '', declaredBy: 'AppName'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
        		"\n" +
        		"\n" ;
        	
        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }
    
    @Test
    public void testComplexExpressionStaticInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    import org.apache.royale.events.CloseEvent;"
                + "    public static var foo:String = CloseEvent.CLOSE;"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"goog.require('org.apache.royale.events.CloseEvent');\n" +
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
				"AppName.get__foo = function() {\n" +
				"  var value = org.apache.royale.events.CloseEvent.CLOSE;\n" +
				"  Object.defineProperty(AppName, 'foo', { value: value, writable: true });\n" +
				"  return value;\n" +
				"};\n" +
				"AppName.set__foo = function(value) {\n" +
				"  Object.defineProperty(AppName, 'foo', { value: value, writable: true });\n" +
				"};\n" +
				"/**\n" +
				" * @type {string}\n" +
				" */\n" +
				"AppName.foo;\n" +
				"\n" +
				"Object.defineProperties(AppName, /** @lends {AppName} */ {\n" +
				"/**\n" +
				" * @type {string}\n" +
				" */\n" +
				"foo: {\n" +
				"  get: AppName.get__foo,\n" +
				"  set: AppName.set__foo,\n" +
				"  configurable: true}});\n" +
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
				"        '|foo': { type: 'String', get_set: function (/** * */ v) {return v !== undefined ? AppName.foo = v : AppName.foo;}}\n" +
				"      };\n" +
				"    },\n" +
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
    
    @Test
    public void testSimpleStaticInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    public static var foo:String = 'foo';"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
				" * @type {string}\n" +
				" */\n" +
				"AppName.foo = 'foo';\n" +
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
				"        '|foo': { type: 'String', get_set: function (/** * */ v) {return v !== undefined ? AppName.foo = v : AppName.foo;}}\n" +
				"      };\n" +
				"    },\n" +
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
    
    @Test
    public void testComplexStaticInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    public static var foo:Array = ['foo'];"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
				" * @type {Array}\n" +
				" */\n" +
				"AppName.foo = ['foo'];\n" +
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
				"        '|foo': { type: 'Array', get_set: function (/** * */ v) {return v !== undefined ? AppName.foo = v : AppName.foo;}}\n" +
				"      };\n" +
				"    },\n" +
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
        
    @Test
    public void testConstComplexExpressionStaticInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
        		+ "    import org.apache.royale.events.CloseEvent;"
                + "    public static const foo:String = CloseEvent.CLOSE;"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"goog.require('org.apache.royale.events.CloseEvent');\n" +
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
				"AppName.get__foo = function() {\n" +
				"  var value = org.apache.royale.events.CloseEvent.CLOSE;\n" +
				"  Object.defineProperty(AppName, 'foo', { value: value, writable: false });\n" +
				"  return value;\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {string}\n" +
				" */\n" +
				"AppName.foo;\n" +
				"\n" +
				"Object.defineProperties(AppName, /** @lends {AppName} */ {\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {string}\n" +
				" */\n" +
				"foo: {\n" +
				"  get: AppName.get__foo,\n" +
				"  configurable: true}});\n" +
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
				"        'AppName': { type: '', declaredBy: 'AppName'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"\n" +
				"\n" ;
        	
        assertOutWithMetadata(outTemplate.replaceAll("AppName", appName));
    }
    
    @Test
    public void testComplexCustomNamespaceInitializersInScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    import custom.custom_namespace;"    
                + "    use namespace custom_namespace;"    
                + "    custom_namespace var foo:Array = ['foo'];"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);

        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
        	.getAncestorOfType(IMXMLDocumentNode.class);
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
        		"  this.http_$$ns_apache_org$2017$custom$namespace__foo = ['foo'];\n" +
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
				"//use namespace custom.custom_namespace;\n" +
        		"\n" +
				"\n" +
				"/**\n" +
				" * @type {Array}\n" +
				" */\n" +
				"AppName.prototype.http_$$ns_apache_org$2017$custom$namespace__foo;\n" +
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
