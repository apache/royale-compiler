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

package org.apache.royale.compiler.constants;

/**
 * Constants for ActionScript warnings. The constants are using to 
 * enable and disable the various warnings in the ActionScript
 * compiler.
 */
public interface IASWarningConstants
{
    /**
     * <code>Array.toString()</code> format has changed.
     */
    static final int ARRAY_TO_STRING_CHANGES = 1044;

    /**
     * Assignment within conditional.
     */
    static final int ASSIGNMENT_WITHIN_CONDITIONAL = 1100;

    /**
     * Possibly invalid Array cast operation.
     */
    static final int BAD_ARRAY_CAST = 1112;

    /**
     * Non-Boolean value used where a <code>Boolean</code> value was expected.
     */
    static final int BAD_BOOLEAN_ASSIGNMENT = 3590;

    /**
     * Invalid <code>Date</code> cast operation.
     */
    static final int BAD_DATE_CAST = 3575;

    /**
     * Unknown method.
     */
    static final int BAD_ES3_TYPE_METHOD = 3594;

    /**
     * Unknown property.
     */
    static final int BAD_ES3_TYPE_PROP = 3592;

    /**
     * Illogical comparison with <code>NaN</code>. Any comparison operation involving <code>NaN</code> will evaluate to <code>false</code> because <code>NaN != NaN</code>.
     */
    static final int BAD_NAN_COMPARISON = 1098;

    /**
     * Impossible assignment to <code>null</code>.
     */
    static final int BAD_NULL_ASSIGNMENT = 1102;

    /**
     * Illogical comparison with <code>null</code>.
     */
    static final int BAD_NULL_COMPARISON = 1096;

    /**
     * Illogical comparison with <code>undefined</code>.  Only untyped variables (or variables of type <code>*</code>) can be <code>undefined</code>.
     */
    static final int BAD_UNDEFINED_COMPARISON = 1012;

    /**
     * <code>Boolean()</code> with no arguments returns <code>false</code> in ActionScript 3.0.
     * <code>Boolean()</code> returned <code>undefined</code> in ActionScript 2.0.
     */
    static final int BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS = 1034;

    /**
     * <code>__resolve</code> is deprecated.
     */
    static final int CHANGES_IN_RESOLVE = 1066;

    /**
     * <code>Class</code> is sealed. It cannot have members added to it dynamically.
     */
    static final int CLASS_IS_SEALED = 1072;

    /**
     * Constant not initialized.
     */
    static final int CONST_NOT_INITIALIZED = 1110;

    /**
     * Function used in new expression returns a value.  Result will be what the function returns, rather than a new instance of that function.
     */
    static final int CONSTRUCTOR_RETURNS_VALUE = 1030;

    /**
     * EventHandler was not added as a listener.
     */
    static final int DEPRECATED_EVENT_HANDLER_ERROR = 1090;

    /**
     * Unsupported ActionScript 2.0 function.
     */
    static final int DEPRECATED_FUNCTION_ERROR = 1060;

    /**
     * Unsupported ActionScript 2.0 property.
     */
    static final int DEPRECATED_PROPERTY_ERROR = 1058;

    /**
     * More than one argument by the same name.
     */
    static final int DUPLICATE_ARGUMENT_NAMES = 3583;

    /**
     * Duplicate variable definition
     */
    static final int DUPLICATE_VARIABLE_DEF = 3596;

    /**
     * ActionScript 3.0 iterates over an object's properties within a "<code>for x in target</code>" statement in random order.
     */
    static final int FOR_VAR_IN_CHANGES = 1086;

    /**
     * Importing a package by the same name as the current class will hide that class identifier in this scope.
     */
    static final int IMPORT_HIDES_CLASS = 3581;

    /**
     * Use of the <code>instanceof</code> operator.
     */
    static final int INSTANCEOF_CHANGES = 3555;

    /**
     * Internal error in compiler.
     */
    static final int INTERNAL_ERROR = 1088;

    /**
     * <code>_level</code> is no longer supported. For more information, see the <code>flash.display</code> package.
     */
    static final int LEVEL_NOT_SUPPORTED = 1070;

    /**
     * Missing namespace declaration (e.g. variable is not defined to be <code>public</code>, <code>private</code>, etc.).
     */
    static final int MISSING_NAMESPACE_DECL = 1084;

    /**
     * Negative value will become a large positive value when assigned to a <code>uint</code> data type.
     */
    static final int NEGATIVE_UINT_LITERAL = 1092;

    /**
     * Missing constructor.
     */
    static final int NO_CONSTRUCTOR = 1104;

    /**
     * The <code>super()</code> statement was not called within the constructor.
     */
    static final int NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR = 1114;

    /**
     * Missing type declaration.
     */
    static final int NO_TYPE_DECL = 1008;

    /**
     * In ActionScript 3.0, white space is ignored and <code>''</code> returns <code>0</code>.
     * <code>Number()</code> returns <code>NaN</code> in ActionScript 2.0 when the parameter is <code>''</code> or contains white space.
     */
    static final int NUMBER_FROM_STRING_CHANGES = 1038;

    /**
     * Change in scoping for the <code>this</code> keyword.
     * Class methods extracted from an instance of a class will always resolve <code>this</code> back to that instance.
     * In ActionScript 2.0, <code>this</code> is looked up dynamically based on where the method is invoked from.
     */
    static final int SCOPING_CHANGE_IN_THIS = 1082;

    /**
     * Inefficient use of <code>+=</code> on a <code>TextField</code>.
     */
    static final int SLOW_TEXTFIELD_ADDITION = 3551;

    /**
     * Possible missing parentheses.
     */
    static final int UNLIKELY_FUNCTION_VALUE = 3553;

    /**
     * Possible usage of the ActionScript 2.0 <code>XML</code> class.
     */
    static final int XML_CLASS_HAS_CHANGED = 3573;

    /**
     * Keyword this within closure.
     */
    static final int THIS_WITHIN_CLOSURE = 20000;
}
