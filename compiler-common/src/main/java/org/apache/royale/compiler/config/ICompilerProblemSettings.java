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

package org.apache.royale.compiler.config;

import java.util.Collection;

import org.apache.royale.compiler.problems.ICompilerProblem;

public interface ICompilerProblemSettings
{
    /**
     * Returns whether some source path directories are allowed to be 
     * subdirectories of another.
     * 
     * @return true if source path overlap is allowed, false otherwise.
     */
     boolean isSourcePathOverlapAllowed();

     /**
      * Returns whether ActionScript warnings are enabled.
      * 
      * If enabled, the ActionScript compiler runs in a mode that detects legal
      * but potentially incorrect code.
      * 
      * @return true if ActionScript warnings are enabled, false otherwise.
      */
     boolean showActionScriptWarnings();

     /**
      * Returns whether binding warnings are enabled.
      * 
      * If binding warning are enabled, then warnings generated from data binding
      * code are displayed.
      * 
      * @return true if binding warnings are enabled, false otherwise.
      */
     boolean showBindingWarnings();

     /**
      * Returns whether deprecation warnings are enabled.
      * 
      * If deprecation warning are enabled, then warnings generated from the use of 
      * deprecated APIs are displayed.
      * 
      * @return true if deprecation warnings are enabled, false otherwise.
      */
     boolean showDeprecationWarnings();

     /**
      * Returns whether unused type selector warnings are enabled.
      * 
      * If unused type selector warnings are enabled, then warnings generated from 
      * unused type selectors are displayed.
      * 
      * @return true if unused type selector warnings are enabled, false otherwise.
      */
     boolean showUnusedTypeSelectorWarnings();
     
     /**
      * Returns whether multiple definition warnings are enabled.
      *
      * @return true if multiple definition warnings are enabled, false otherwise.
      */
     boolean showMultipleDefinitionWarnings();
     
     /**
      * Returns true if the compiler should run in strict error checking mode.
      * 
      * In strict mode the compiler reports undefined property and function calls. 
      * The compiler also performs type checking on assignments and options 
      * supplied to method calls.
      * 
      * @return true if strict error check mode is enabled, false otherwise.
      */
     boolean isStrict();
     
     /**
      * A collection of problem classes that should be report as errors. 
      * This list overrides the default settings.
      * 
      * @return collection of classes that should be reported as errors.
      */
     Collection<Class<ICompilerProblem>> getErrorProblems();
     
     
     /**
      * A collection of problem classes that should be report as warnings. 
      * This list overrides the default settings.
      * 
      * @return collection of classes that should be reported as warnings.
      */
     Collection<Class<ICompilerProblem>> getWarningProblems();
     
     /**
      * A collection of problem classes that should not be reported. 
      * This list overrides the default settings.
      * 
      * @return collection of classes that should be ignored.
      */
     Collection<Class<ICompilerProblem>> getIgnoreProblems();
     
     /**
      * Returns whether checking of a type ActionScript warning is enabled.
      * The kinds of warnings are:
      * 
      * <pre>
      * --compiler.warn-array-tostring-changes
      * --compiler.warn-assignment-within-conditional
      * --compiler.warn-bad-array-cast
      * --compiler.warn-bad-bool-assignment
      * --compiler.warn-bad-date-cast
      * --compiler.warn-bad-es3-type-method
      * --compiler.warn-bad-es3-type-prop
      * --compiler.warn-bad-nan-comparison
      * --compiler.warn-bad-null-assignment
      * --compiler.warn-bad-null-comparison
      * --compiler.warn-bad-undefined-comparison
      * --compiler.warn-boolean-constructor-with-no-args
      * --compiler.warn-changes-in-resolve
      * --compiler.warn-class-is-sealed
      * --compiler.warn-const-not-initialized
      * --compiler.warn-constructor-returns-value
      * --compiler.warn-deprecated-event-handler-error
      * --compiler.warn-deprecated-function-error
      * --compiler.warn-deprecated-property-error
      * --compiler.warn-duplicate-argument-names
      * --compiler.warn-duplicate-variable-def
      * --compiler.warn-for-var-in-changes
      * --compiler.warn-import-hides-class
      * --compiler.warn-instance-of-changes
      * --compiler.warn-internal-error
      * --compiler.warn-level-not-supported
      * --compiler.warn-missing-namespace-decl
      * --compiler.warn-negative-uint-literal
      * --compiler.warn-no-constructor
      * --compiler.warn-no-explicit-super-call-in-constructor
      * --compiler.warn-no-type-decl
      * --compiler.warn-number-from-string-changes
      * --compiler.warn-scoping-change-in-this
      * --compiler.warn-slow-text-field-addition
      * --compiler.warn-unlikely-function-value
      * --compiler.warn-xml-class-has-changed
      * --compiler.warn-this-within-closure
      * </pre>
      * 
      * @param warningCode warning code, one of:
      * <p>
      * <ul>
      * <li> WARN_ARRAY_TOSTRING_CHANGES
      * <li> WARN_ASSIGNMENT_WITHIN_CONDITIONAL
      * <li> WARN_BAD_ARRAY_CAST
      * <li> WARN_BAD_BOOLEAN_ASSIGNMENT
      * <li> WARN_BAD_DATE_CAST
      * <li> WARN_BAD_ES3_TYPE_METHOD
      * <li> WARN_BAD_ES3_TYPE_PROP
      * <li> WARN_BAD_NAN_COMPARISON
      * <li> WARN_BAD_NULL_ASSIGNMENT
      * <li> WARN_BAD_NULL_COMPARISON
      * <li> WARN_BAD_UNDEFINED_COMPARISON
      * <li> WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS
      * <li> WARN_CHANGES_IN_RESOLVE
      * <li> WARN_CLASS_IS_SEALED
      * <li> WARN_CONST_NOT_INITIALIZED
      * <li> WARN_CONSTRUCTOR_RETURNS_VALUE
      * <li> WARN_DEPRECATED_EVENT_HANDLER_ERROR
      * <li> WARN_DEPRECATED_FUNCTION_ERROR
      * <li> WARN_DEPRECATED_PROPERTY_ERROR
      * <li> WARN_DUPLICATE_ARGUMENT_NAMES
      * <li> WARN_DUPLICATE_VARIABLE_DEF
      * <li> WARN_FOR_VAR_IN_CHANGES
      * <li> WARN_IMPORT_HIDES_CLASS
      * <li> WARN_INSTANCEOF_CHANGES
      * <li> WARN_INTERNAL_ERROR
      * <li> WARN_LEVEL_NOT_SUPPORTED
      * <li> WARN_MISSING_NAMESPACE_DECL
      * <li> WARN_NEGATIVE_UINT_LITERAL
      * <li> WARN_NO_CONSTRUCTOR
      * <li> WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR
      * <li> WARN_NO_TYPE_DECL
      * <li> WARN_NUMBER_FROM_STRING_CHANGES
      * <li> WARN_SCOPING_CHANGE_IN_THIS
      * <li> WARN_SLOW_TEXTFIELD_ADDITION
      * <li> WARN_UNLIKELY_FUNCTION_VALUE
      * <li> WARN_XML_CLASS_HAS_CHANGED
      * <li> WARN_THIS_WITHIN_CLOSURE
      * </ul>
      * </p>
      */
     boolean checkActionScriptWarning(int warningCode);

     /**
      * Determines if any warnings are shown. This is the master warning flag
      * that overrides all other warning options.
      * 
      * @return true if warnings should be shown, false otherwise.
      */
     boolean showWarnings();
}
