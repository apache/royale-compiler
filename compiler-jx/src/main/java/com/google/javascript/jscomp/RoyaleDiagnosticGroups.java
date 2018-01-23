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

package com.google.javascript.jscomp;

/**
 * Custom DiagnosticGroups allow @suppress directives to disable
 * certain warnings while letting other warnings in the group
 * show.
 */
public class RoyaleDiagnosticGroups {

	/**
	 * Royale ItemRenderer Factories store the constructor in a variable
	 * resulting in this warning.
	 */
	public static final DiagnosticGroup ROYALE_NOT_A_CONSTRUCTOR =
		DiagnosticGroups.registerGroup("royaleNotAConstructor",
                TypeCheck.NOT_A_CONSTRUCTOR);

	/**
	 * Royale code calls super.methodName from functions other than
	 * overrides of the methodName.
	 */
	public static final DiagnosticGroup ROYALE_SUPER_CALL_TO_DIFFERENT_NAME =
		DiagnosticGroups.registerGroup("royaleSuperCallToDifferentName",
                ProcessClosurePrimitives.BASE_CLASS_ERROR);
	/*
	public static final DiagnosticGroup ROYALE_REFERENCE_BEFORE_DECLARE =
		DiagnosticGroups.registerGroup("royaleReferenceBeforeDeclare",
                VariableReferenceCheck.UNDECLARED_REFERENCE);
    */
	
	/**
	 * Royale code won't always generate a goog.requires for types only used
	 * in JSDoc annotations, but the compiler complains.
	 */
	public static final DiagnosticGroup ROYALE_UNKNOWN_JSDOC_TYPE_NAME =
		DiagnosticGroups.registerGroup("royaleUnknownJSDocTypeName",
                RhinoErrorReporter.TYPE_PARSE_ERROR);

}
