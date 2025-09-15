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

import org.apache.royale.compiler.internal.test.RoyaleTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.junit.Test;

public class TestRoyaleMXMLRegExp extends RoyaleTestBase
{
    @Test
    public void testRegExpTag()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<fx:RegExp id=\"regexp\">/[a-z]{3,} \\d+(oz|g)/ig</fx:RegExp>\n"
				+ "</fx:Declarations>";

        IMXMLDeclarationsNode declNode = (IMXMLDeclarationsNode) getNode(code,
            IMXMLDeclarationsNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);
        IMXMLDocumentNode docNode = (IMXMLDocumentNode) declNode.getAncestorOfType(IMXMLDocumentNode.class);
		mxmlBlockWalker.visitDocument(docNode);
        
        String appName = docNode.getQualifiedName();

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
            "  this.regexp = /[a-z]{3,}\\u0020\\d+(oz|g)/ig;\n" +
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
            " * @export\n" +
            " * @type {RegExp}\n" +
            " */\n" +
            "AppName.prototype.regexp;";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }
}
