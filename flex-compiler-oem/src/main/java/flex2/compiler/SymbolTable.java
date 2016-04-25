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

package flex2.compiler;

/**
 * This class supports looking up information for a class or a style
 * and looking up a <code>Source</code> by QName or by resource bundle
 * name.
 *
 * @author Clement Wong
 */
public final class SymbolTable
{
	// These may look funny, but they line up with the values that ASC uses.
	public static final String internalNamespace = "internal";
	public static final String privateNamespace = "private";
	public static final String protectedNamespace = "protected";
	public static final String publicNamespace = "";
	public static final String unnamedPackage = "";
	public static final String[] VISIBILITY_NAMESPACES = new String[] {SymbolTable.publicNamespace,
																	   SymbolTable.protectedNamespace,
																	   SymbolTable.internalNamespace,
																       SymbolTable.privateNamespace};

	public static final String NOTYPE = "*";
	public static final String STRING = "String";
	public static final String BOOLEAN = "Boolean";
	public static final String NUMBER = "Number";
	public static final String INT = "int";
	public static final String UINT = "uint";
	public static final String NAMESPACE = "Namespace";
	public static final String FUNCTION = "Function";
	public static final String CLASS = "Class";
	public static final String ARRAY = "Array";
	public static final String OBJECT = "Object";
	public static final String XML = "XML";
	public static final String XML_LIST = "XMLList";
	public static final String REPARENT = "Reparent";
	public static final String REGEXP = "RegExp";
	public static final String EVENT = "flash.events:Event";
    public static final String VECTOR = "__AS3__.vec:Vector";

}
