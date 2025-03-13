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

public class TestRoyaleMXMLNumber extends RoyaleTestBase
{
    @Test
    public void testNumberTag()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<fx:Number id=\"number\">123.4</fx:Number>\n"
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
            "  this.number = 123.4;\n" +
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
            " * @type {number}\n" +
            " */\n" +
            "AppName.prototype.number;";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }

    @Test
    public void testNumberPropertyAttribute()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label alpha=\"0.5\"/>\n"
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
            "goog.require('org.apache.royale.html.Label');\n" +
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
            "};\n" +
            "goog.inherits(AppName, org.apache.royale.core.Application);\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Object.defineProperties(AppName.prototype, /** @lends {AppName.prototype} */ {\n" +
            "  'MXMLDescriptor': {\n" +
            "    /** @this {AppName} */\n" +
            "    get: function() {\n" +
            "      if (this.mxmldd == undefined)\n" +
            "      {\n" +
            "        /** @type {Array} */\n" +
            "        var arr = AppName.superClass_.get__MXMLDescriptor.apply(this);\n" +
            "        /** @type {Array} */\n" +
            "        var mxmldd = [\n" +
            "          org.apache.royale.html.Label,\n" +
            "          2,\n" +
            "          '_id',\n" +
            "          true,\n" +
            "          '$ID_8_0',\n" +
            "          'alpha',\n" +
            "          true,\n" +
            "          0.5,\n" +
            "          0,\n" +
            "          0,\n" +
            "          null\n" +
            "        ];\n" +
            "        if (arr)\n" +
            "          this.mxmldd = arr.concat(mxmldd);\n" +
            "        else\n" +
            "          this.mxmldd = mxmldd;\n" +
            "      }\n" +
            "      return this.mxmldd;\n" +
            "    }\n" +
            "  }\n" +
            "});";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }

    @Test
    public void testNumberPropertyChildTag()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label>\n"
                + "\t\t<basic:alpha>0.5</basic:alpha>\n"
                + "\t</basic:Label>"
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
            "goog.require('org.apache.royale.html.Label');\n" +
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
            "};\n" +
            "goog.inherits(AppName, org.apache.royale.core.Application);\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Object.defineProperties(AppName.prototype, /** @lends {AppName.prototype} */ {\n" +
            "  'MXMLDescriptor': {\n" +
            "    /** @this {AppName} */\n" +
            "    get: function() {\n" +
            "      if (this.mxmldd == undefined)\n" +
            "      {\n" +
            "        /** @type {Array} */\n" +
            "        var arr = AppName.superClass_.get__MXMLDescriptor.apply(this);\n" +
            "        /** @type {Array} */\n" +
            "        var mxmldd = [\n" +
            "          org.apache.royale.html.Label,\n" +
            "          2,\n" +
            "          '_id',\n" +
            "          true,\n" +
            "          '$ID_8_0',\n" +
            "          'alpha',\n" +
            "          true,\n" +
            "          0.5,\n" +
            "          0,\n" +
            "          0,\n" +
            "          null\n" +
            "        ];\n" +
            "        if (arr)\n" +
            "          this.mxmldd = arr.concat(mxmldd);\n" +
            "        else\n" +
            "          this.mxmldd = mxmldd;\n" +
            "      }\n" +
            "      return this.mxmldd;\n" +
            "    }\n" +
            "  }\n" +
            "});";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }
}
