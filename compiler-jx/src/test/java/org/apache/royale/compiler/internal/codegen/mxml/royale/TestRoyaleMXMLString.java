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

public class TestRoyaleMXMLString extends RoyaleTestBase
{
    @Test
    public void testStringTag()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<fx:String id=\"string\"> abc 123 </fx:String>\n"
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
            "  this.string = ' abc 123 ';\n" +
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
            " * @type {string}\n" +
            " */\n" +
            "AppName.prototype.string;";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }

    @Test
    public void testStringTagWithCharsThatShouldNotBeEscaped()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<fx:String id=\"string\">\\t\\n\\r\\f\\b</fx:String>\n"
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
            "  this.string = '\\\\t\\\\n\\\\r\\\\f\\\\b';\n" +
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
            " * @type {string}\n" +
            " */\n" +
            "AppName.prototype.string;";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }

    @Test
    public void testStringTagWithUnicodeEscape()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<fx:String id=\"string\">\\u00B0</fx:String>\n"
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
            "  this.string = '\\u00B0';\n" +
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
            " * @type {string}\n" +
            " */\n" +
            "AppName.prototype.string;";
        	
        assertOut(outTemplate.replaceAll("AppName", appName), false);
    }

    @Test
    public void testStringPropertyAttribute()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label text=\" abc 123 \"/>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          ' abc 123 ',\n" +
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
    public void testStringPropertyAttributeWithCharsThatShouldNotBeEscaped()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label text=\"\\t\\n\\r\\f\\b\"/>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          '\\\\t\\\\n\\\\r\\\\f\\\\b',\n" +
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
    public void testStringPropertyAttributeWithUnicodeEscape()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label text=\"\\u00B0\"/>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          '\\u00B0',\n" +
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
    public void testStringPropertyChildTag()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label>\n"
                + "\t\t<basic:text> abc 123 </basic:text>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          ' abc 123 ',\n" +
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
    public void testStringPropertyChildTagWithCharsThatShouldNotBeEscaped()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label>\n"
                + "\t\t<basic:text>\\t\\n\\r\\f\\b</basic:text>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          '\\\\t\\\\n\\\\r\\\\f\\\\b',\n" +
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
    public void testStringPropertyChildTagWithUnicodeEscape()
    {
        String code = "<fx:Declarations>\n"
				+ "\t<basic:Label>\n"
                + "\t\t<basic:text>\\u00B0</basic:text>\n"
				+ "\t</basic:Label>\n"
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
            "          'text',\n" +
            "          true,\n" +
            "          '\\u00B0',\n" +
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
