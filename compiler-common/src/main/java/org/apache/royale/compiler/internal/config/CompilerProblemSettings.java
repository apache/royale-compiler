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

package org.apache.royale.compiler.internal.config;

import java.util.Collection;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A value object created by the Configurator. This object is used to control
 * how compiler problems are reported.
 */
public class CompilerProblemSettings implements ICompilerProblemSettings
{
    public CompilerProblemSettings(Configuration configuration)
    {
        this.configuration = configuration;
    }

    Configuration configuration;
    
    @Override
    public boolean isSourcePathOverlapAllowed()
    {
        return configuration.getAllowSourcePathOverlap();
    }

    @Override
    public boolean showActionScriptWarnings()
    {
        return configuration.warnings();
    }

    @Override
    public boolean showDeprecationWarnings()
    {
        return configuration.showDeprecationWarnings();
    }

    @Override
    public boolean isStrict()
    {
        return configuration.strict();
    }

    @Override
    public Collection<Class<ICompilerProblem>> getErrorProblems()
    {
        return configuration.getErrorProblems();
    }

    @Override
    public Collection<Class<ICompilerProblem>> getWarningProblems()
    {
        return configuration.getWarningProblems();
    }

    @Override
    public Collection<Class<ICompilerProblem>> getIgnoreProblems()
    {
        return configuration.getIgnoreProblems();
    }

    @Override
    public boolean showBindingWarnings()
    {
        return configuration.showBindingWarnings();
    }

    @Override
    public boolean showUnusedTypeSelectorWarnings()
    {
        return configuration.showUnusedTypeSelectorWarnings();
    }
    
    @Override
    public boolean showMultipleDefinitionWarnings()
    {
        return configuration.showMultipleDefinitionWarnings();
    }

    @Override
    public boolean checkActionScriptWarning(int warningCode)
    {
        boolean warning = false;
        
        switch (warningCode)
        {
        case ICompilerSettings.WARN_ARRAY_TO_STRING_CHANGES:
            warning = configuration.warn_array_tostring_changes();
            break;
        case ICompilerSettings.WARN_ASSIGNMENT_WITHIN_CONDITIONAL:
            warning = configuration.warn_assignment_within_conditional();
            break;
        case ICompilerSettings.WARN_BAD_ARRAY_CAST:
            warning = configuration.warn_bad_array_cast();
            break;
        case ICompilerSettings.WARN_BAD_BOOLEAN_ASSIGNMENT:
            warning = configuration.warn_bad_bool_assignment();
            break;
        case ICompilerSettings.WARN_BAD_DATE_CAST:
            warning = configuration.warn_bad_date_cast();
            break;
        case ICompilerSettings.WARN_BAD_ES3_TYPE_METHOD:
            warning = configuration.warn_bad_es3_type_method();
            break;
        case ICompilerSettings.WARN_BAD_ES3_TYPE_PROP:
            warning = configuration.warn_bad_es3_type_prop();
            break;
        case ICompilerSettings.WARN_BAD_NAN_COMPARISON:
            warning = configuration.warn_bad_nan_comparison();
            break;
        case ICompilerSettings.WARN_BAD_NULL_ASSIGNMENT:
            warning = configuration.warn_bad_null_assignment();
            break;
        case ICompilerSettings.WARN_BAD_NULL_COMPARISON:
            warning = configuration.warn_bad_null_comparison();
            break;
        case ICompilerSettings.WARN_BAD_UNDEFINED_COMPARISON:
            warning = configuration.warn_bad_undefined_comparison();
            break;
        case ICompilerSettings.WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS:
            warning = configuration.warn_boolean_constructor_with_no_args();
            break;
        case ICompilerSettings.WARN_CHANGES_IN_RESOLVE:
            warning = configuration.warn_changes_in_resolve();
            break;
        case ICompilerSettings.WARN_CLASS_IS_SEALED:
            warning = configuration.warn_class_is_sealed();
            break;
        case ICompilerSettings.WARN_CONST_NOT_INITIALIZED:
            warning = configuration.warn_const_not_initialized();
            break;
        case ICompilerSettings.WARN_CONSTRUCTOR_RETURNS_VALUE:
            warning = configuration.warn_constructor_returns_value();
            break;
        case ICompilerSettings.WARN_DEPRECATED_EVENT_HANDLER_ERROR:
            warning = configuration.warn_deprecated_event_handler_error();
            break;
        case ICompilerSettings.WARN_DEPRECATED_FUNCTION_ERROR:
            warning = configuration.warn_deprecated_function_error();
            break;
        case ICompilerSettings.WARN_DEPRECATED_PROPERTY_ERROR:
            warning = configuration.warn_deprecated_property_error();
            break;
        case ICompilerSettings.WARN_DUPLICATE_ARGUMENT_NAMES:
            warning = configuration.warn_duplicate_argument_names();
            break;
        case ICompilerSettings.WARN_DUPLICATE_VARIABLE_DEF:
            warning = configuration.warn_duplicate_variable_def();
            break;
        case ICompilerSettings.WARN_FOR_VAR_IN_CHANGES:
            warning = configuration.warn_for_var_in_changes();
            break;
        case ICompilerSettings.WARN_IMPORT_HIDES_CLASS:
            warning = configuration.warn_import_hides_class();
            break;
        case ICompilerSettings.WARN_INSTANCEOF_CHANGES:
            warning = configuration.warn_instance_of_changes();
            break;
        case ICompilerSettings.WARN_INTERNAL_ERROR:
            warning = configuration.warn_internal_error();
            break;
        case ICompilerSettings.WARN_LEVEL_NOT_SUPPORTED:
            warning = configuration.warn_level_not_supported();
            break;
        case ICompilerSettings.WARN_MISSING_NAMESPACE_DECL:
            warning = configuration.warn_missing_namespace_decl();
            break;
        case ICompilerSettings.WARN_NEGATIVE_UINT_LITERAL:
            warning = configuration.warn_negative_uint_literal();
            break;
        case ICompilerSettings.WARN_NO_CONSTRUCTOR:
            warning = configuration.warn_no_constructor();
            break;
        case ICompilerSettings.WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR:
            warning = configuration.warn_no_explicit_super_call_in_constructor();
            break;
        case ICompilerSettings.WARN_NO_TYPE_DECL:
            warning = configuration.warn_no_type_decl();
            break;
        case ICompilerSettings.WARN_NUMBER_FROM_STRING_CHANGES:
            warning = configuration.warn_number_from_string_changes();
            break;
        case ICompilerSettings.WARN_SCOPING_CHANGE_IN_THIS:
            warning = configuration.warn_scoping_change_in_this();
            break;
        case ICompilerSettings.WARN_SLOW_TEXTFIELD_ADDITION:
            warning = configuration.warn_slow_text_field_addition();
            break;
        case ICompilerSettings.WARN_UNLIKELY_FUNCTION_VALUE:
            warning = configuration.warn_unlikely_function_value();
            break;
        case ICompilerSettings.WARN_XML_CLASS_HAS_CHANGED:
            warning = configuration.warn_xml_class_has_changed();
            break;
        case ICompilerSettings.WARN_THIS_WITHIN_CLOSURE:
            warning = configuration.warn_this_within_closure();
            break;
        }
        
        return warning;
    }

    @Override
    public boolean showWarnings()
    {
        return configuration.getWarnings();
    }

}
