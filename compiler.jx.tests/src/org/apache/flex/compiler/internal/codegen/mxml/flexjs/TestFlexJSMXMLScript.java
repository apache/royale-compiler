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
        ((JSFlexJSEmitter)(mxmlBlockWalker.getASEmitter())).thisClass = dnode.getDefinition();
        mxmlBlockWalker.visitDocument(dnode);
        String appName = dnode.getQualifiedName();
        String outTemplate = "/**\n * AppName\n *\n * @fileoverview\n *\n * @suppress {checkTypes}\n */\n\ngoog.provide('AppName');\n\ngoog.require('org_apache_flex_core_Application');\n\n\n\n\n/**\n * @constructor\n * @extends {org_apache_flex_core_Application}\n */\nAppName = function() {\n  AppName.base(this, 'constructor');\n  \n  /**\n   * @private\n   * @type {Array}\n   */\n  this.mxmldd;\n  \n  /**\n   * @private\n   * @type {Array}\n   */\n  this.mxmldp;\n};\ngoog.inherits(AppName, org_apache_flex_core_Application);\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nAppName.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'AppName', qName: 'AppName' }] };\n\n\n/**\n * @expose\n * @override\n */\nAppName.prototype.addedToParent = function() {\n  AppName.base(this, 'addedToParent');\n};\n\n\n";
        	
        assertOut(outTemplate.replaceAll("AppName", appName));
    }

}
