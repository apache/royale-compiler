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

package org.apache.royale.compiler.codegen;

/**
 * AS3 language global functions, such as Array, encodeURI, isNaN etc.
 * 
 * @author Erik de Bruin
 */
public interface IASGlobalFunctionConstants
{
    static final String Array = "Array";
    static final String Boolean = "Boolean";
    static final String decodeURI = "decodeURI";
    static final String decodeURIComponent = "decodeURIComponent";
    static final String encodeURI = "encodeURI";
    static final String encodeURIComponent = "encodeURIComponent";
    static final String escape = "escape";
    static final String _int = "int";
    static final String isFinite = "isFinite";
    static final String isNaN = "isNaN";
    static final String isXMLName = "isXMLName";
    static final String Number = "Number";
    static final String Object = "Object";
    static final String parseFloat = "parseFloat";
    static final String parseInt = "parseInt";
    static final String String = "String";
    static final String trace = "trace";
    static final String uint = "uint";
    static final String unescape = "unescape";
    static final String Vector = "Vector";
    static final String XML = "XML";
    static final String XMLList = "XMLList";

    /**
     * An enumeration of core built-in functions.
     */
    public static enum BuiltinType
    {
        ARRAY(IASGlobalFunctionConstants.Array), BOOLEAN(
                IASGlobalFunctionConstants.Boolean), DECODEURI(
                IASGlobalFunctionConstants.decodeURI), DECODEURICOMPONENT(
                IASGlobalFunctionConstants.decodeURIComponent), ENCODEURI(
                IASGlobalFunctionConstants.encodeURI), ENCODEURICOMPONENT(
                IASGlobalFunctionConstants.encodeURIComponent), ESCAPE(
                IASGlobalFunctionConstants.escape), INT(
                IASGlobalFunctionConstants._int), ISFINITE(
                IASGlobalFunctionConstants.isFinite), ISNAN(
                IASGlobalFunctionConstants.isNaN), ISXMLNAME(
                IASGlobalFunctionConstants.isXMLName), NUMBER(
                IASGlobalFunctionConstants.Number), OBJECT(
                IASGlobalFunctionConstants.Object), PARSEFLOAT(
                IASGlobalFunctionConstants.parseFloat), PARSEINT(
                IASGlobalFunctionConstants.parseInt), STRING(
                IASGlobalFunctionConstants.String), TRACE(
                IASGlobalFunctionConstants.trace), UINT(
                IASGlobalFunctionConstants.uint), UNESCAPE(
                IASGlobalFunctionConstants.unescape), VECTOR(
                IASGlobalFunctionConstants.Vector), XML(
                IASGlobalFunctionConstants.XML), XMLLIST(
                IASGlobalFunctionConstants.XMLList);

        private BuiltinType(String name)
        {
            this.name = name;
        }

        private final String name;

        public String getName()
        {
            return name;
        }
    }
}
