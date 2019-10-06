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

/**
 * Constants for the compiler's command line options.
 */
public interface ICompilerSettingsConstants
{
    static final String USE_NETWORK                                          = "--use-network";
    static final String RUNTIME_SHARED_LIBRARIES                             = "--runtime-shared-libraries";
    static final String RAW_METADATA                                         = "--raw-metadata";
    static final String PROJECTOR                                            = "--projector";
    static final String METADATA_PUBLISHER                                   = "--metadata.publisher";
    static final String METADATA_LANGUAGE                                    = "--metadata.language";
    static final String METADATA_LOCALIZED_TITLE                             = "--metadata.localized-title";
    static final String METADATA_LOCALIZED_DESCRIPTION                       = "--metadata.localized-description";
    static final String METADATA_DATE                                        = "--metadata.date";
    static final String METADATA_CREATOR                                     = "--metadata.creator";
    static final String METADATA_CONTRIBUTOR                                 = "--metadata.contributor";
    static final String LINK_REPORT                                          = "--link-report";
    static final String SIZE_REPORT                                          = "--size-report";
    static final String LICENSES_LICENSE                                     = "--licenses.license";
    static final String INCLUDES                                             = "--includes";
    static final String INCLUDE_RESOURCE_BUNDLES                             = "--include-resource-bundles";
    static final String ROYALE                                               = "--royale";
    static final String FRAMES_FRAME                                         = "--frames.frame";
    static final String LOAD_EXTERNS                                         = "--load-externs";
    static final String LOAD_CONFIG                                          = "--load-config";
    static final String EXTERNS                                              = "--externs";
    static final String DEFAULT_SIZE                                         = "--default-size";
    static final String DEFAULT_SCRIPT_LIMITS                                = "--default-script-limits";
    static final String DEFAULT_FRAME_RATE                                   = "--default-frame-rate";
    static final String DEFAULT_BACKGROUND_COLOR                             = "--default-background-color";
    static final String DEBUG_PASSWORD                                       = "--debug-password";
    static final String SWF_VERSION                                          = "--swf-version";
    static final String COMPILER_WARN_THIS_WITHIN_CLOSURE                    = "--compiler.warn-this-within-closure";
    static final String COMPILER_WARN_XML_CLASS_HAS_CHANGED                  = "--compiler.warn-xml-class-has-changed";
    static final String COMPILER_WARN_UNLIKELY_FUNCTION_VALUE                = "--compiler.warn-unlikely-function-value";
    static final String COMPILER_WARN_SLOW_TEXT_FIELD_ADDITION               = "--compiler.warn-slow-text-field-addition";
    static final String COMPILER_WARN_SCOPING_CHANGE_IN_THIS                 = "--compiler.warn-scoping-change-in-this";
    static final String COMPILER_WARN_NUMBER_FROM_STRING_CHANGES             = "--compiler.warn-number-from-string-changes";
    static final String COMPILER_WARN_NO_TYPE_DECL                           = "--compiler.warn-no-type-decl";
    static final String COMPILER_WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR  = "--compiler.warn-no-explicit-super-call-in-constructor";
    static final String COMPILER_WARN_NO_CONSTRUCTOR                         = "--compiler.warn-no-constructor";
    static final String COMPILER_WARN_NEGATIVE_UINT_LITERAL                  = "--compiler.warn-negative-uint-literal";
    static final String COMPILER_WARN_MISSING_NAMESPACE_DECL                 = "--compiler.warn-missing-namespace-decl";
    static final String COMPILER_WARN_LEVEL_NOT_SUPPORTED                    = "--compiler.warn-level-not-supported";
    static final String COMPILER_WARN_INTERNAL_ERROR                         = "--compiler.warn-internal-error";
    static final String COMPILER_WARN_INSTANCE_OF_CHANGES                    = "--compiler.warn-instance-of-changes";
    static final String COMPILER_WARN_IMPORT_HIDES_CLASS                     = "--compiler.warn-import-hides-class";
    static final String COMPILER_WARN_FOR_VAR_IN_CHANGES                     = "--compiler.warn-for-var-in-changes";
    static final String COMPILER_WARN_DUPLICATE_VARIABLE_DEF                 = "--compiler.warn-duplicate-variable-def";
    static final String COMPILER_WARN_DUPLICATE_ARGUMENT_NAMES               = "--compiler.warn-duplicate-argument-names";
    static final String COMPILER_WARN_DEPRECATED_PROPERTY_ERROR              = "--compiler.warn-deprecated-property-error";
    static final String COMPILER_WARN_DEPRECATED_FUNCTION_ERROR              = "--compiler.warn-deprecated-function-error";
    static final String COMPILER_WARN_DEPRECATED_EVENT_HANDLER_ERROR         = "--compiler.warn-deprecated-event-handler-error";
    static final String COMPILER_WARN_CONSTRUCTOR_RETURNS_VALUE              = "--compiler.warn-constructor-returns-value";
    static final String COMPILER_WARN_CONST_NOT_INITIALIZED                  = "--compiler.warn-const-not-initialized";
    static final String COMPILER_WARN_CLASS_IS_SEALED                        = "--compiler.warn-class-is-sealed";
    static final String COMPILER_WARN_CHANGES_IN_RESOLVE                     = "--compiler.warn-changes-in-resolve";
    static final String COMPILER_WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS       = "--compiler.warn-boolean-constructor-with-no-args";
    static final String COMPILER_WARN_BAD_UNDEFINED_COMPARISON               = "--compiler.warn-bad-undefined-comparison";
    static final String COMPILER_WARN_BAD_NULL_COMPARISON                    = "--compiler.warn-bad-null-comparison";
    static final String COMPILER_WARN_BAD_NULL_ASSIGNMENT                    = "--compiler.warn-bad-null-assignment";
    static final String COMPILER_WARN_BAD_NAN_COMPARISON                     = "--compiler.warn-bad-nan-comparison";
    static final String COMPILER_WARN_BAD_ES3_TYPE_PROP                      = "--compiler.warn-bad-es3-type-prop";
    static final String COMPILER_WARN_BAD_ES3_TYPE_METHOD                    = "--compiler.warn-bad-es3-type-method";
    static final String COMPILER_WARN_BAD_DATE_CAST                          = "--compiler.warn-bad-date-cast";
    static final String COMPILER_WARN_BAD_BOOL_ASSIGNMENT                    = "--compiler.warn-bad-bool-assignment";
    static final String COMPILER_WARN_BAD_ARRAY_CAST                         = "--compiler.warn-bad-array-cast";
    static final String COMPILER_WARN_ASSIGNMENT_WITHIN_CONDITIONAL          = "--compiler.warn-assignment-within-conditional";
    static final String COMPILER_WARN_ARRAY_TOSTRING_CHANGES                 = "--compiler.warn-array-tostring-changes";
    static final String COMPILER_VERBOSE_STACKTRACES                         = "--compiler.verbose-stacktraces";
    static final String COMPILER_USE_RESOURCE_BUNDLE_METADATA                = "--compiler.use-resource-bundle-metadata";
    static final String COMPILER_THEME                                       = "--compiler.theme";
    static final String COMPILER_STRICT                                      = "--compiler.strict";
    static final String COMPILER_SOURCE_PATH                                 = "--compiler.source-path";
    static final String COMPILER_SHOW_UNUSED_TYPE_SELECTOR_WARNINGS          = "--compiler.show-unused-type-selector-warnings";
    static final String COMPILER_SHOW_DEPRECATION_WARNINGS                   = "--compiler.show-deprecation-warnings";
    static final String COMPILER_SHOW_BINDING_WARNINGS                       = "--compiler.show-binding-warnings";
    static final String COMPILER_SHOW_ACTIONSCRIPT_WARNINGS                  = "--compiler.show-actionscript-warnings";
    static final String COMPILER_SERVICES                                    = "--compiler.services";
    static final String COMPILER_OPTIMIZE                                    = "--compiler.optimize";
    static final String COMPILER_NAMESPACES_NAMESPACE                        = "--compiler.namespaces.namespace";
    static final String COMPILER_MOBILE                                      = "--compiler.mobile";
    static final String COMPILER_LOCALE                                      = "--compiler.locale";
    static final String COMPILER_LIBRARY_PATH                                = "--compiler.library-path";
    static final String COMPILER_INCLUDE_LIBRARIES                           = "--compiler.include-libraries";
    static final String COMPILER_KEEP_GENERATED_ACTIONSCRIPT                 = "--compiler.keep-generated-actionscript";
    static final String COMPILER_KEEP_AS3_METADATA                           = "--compiler.keep-as3-metadata";
    static final String COMPILER_KEEP_ALL_TYPE_SELECTORS                     = "--compiler.keep-all-type-selectors";
    static final String COMPILER_HEADLESS_SERVER                             = "--compiler.headless-server";
    static final String COMPILER_EXTERNAL_LIBRARY_PATH                       = "--compiler.external-library-path";
    static final String COMPILER_ES                                          = "--compiler.es";
    static final String COMPILER_DEFAULTS_CSS_URL                            = "--compiler.defaults-css-url";
    static final String COMPILER_DEBUG                                       = "--compiler.debug";
    static final String COMPILER_COMPRESS                                    = "--compiler.compress";
    static final String COMPILER_CONTEXT_ROOT                                = "--compiler.context-root";
    static final String COMPILER_AS3                                         = "--compiler.as3";
    static final String COMPILER_ALLOW_SOURCE_PATH_OVERLAP                   = "--compiler.allow-source-path-overlap";
    static final String COMPILER_ACTIONSCRIPT_FILE_ENCODING                  = "--compiler.actionscript-file-encoding";
    static final String COMPILER_ACCESSIBLE                                  = "--compiler.accessible";
    static final String TARGET_PLAYER                                        = "--target-player";
    static final String RUNTIME_SHARED_LIBRARY_PATH                          = "--runtime-shared-library-path";
    static final String VERIFY_DIGESTS                                       = "--verify-digests";
    static final String COMPILER_COMPUTE_DIGEST                              = "--compute-digest";
    static final String COMPILER_DEFINE                                      = "--compiler.define";
    static final String COMPILER_MXML_COMPATIBILITY                          = "--compiler.mxml.compatibility-version";
    static final String COMPILER_EXTENSIONS                                  = "--compiler.extensions.extension";
    static final String REMOVE_UNUSED_RSLS                                   = "--remove-unused-rsls";
    static final String RUNTIME_SHARED_LIBRARY_SETTINGS_FORCE_RSLS           = "--runtime-shared-library-settings.force-rsls";
    static final String RUNTIME_SHARED_LIBRARY_SETTINGS_APPLICATION_DOMAIN   = "--runtime-shared-library-settings.application-domain";
    static final String OUTPUT                                               = "--output";
    static final String STATIC_LINK_RUNTIME_SHARED_LIBRARIES                 = "--static-link-runtime-shared-libraries";
    static final String COMPILER_SHOW_MULTIPLE_DEFINITION_WARNINGS           = "--show-multiple-definition-warnings";

    // 
    // Library Settings
    //
    static final String INCLUDE_CLASSES                                      = "--include-classes";
    static final String INCLUDE_FILE                                         = "--include-file";
    static final String INCLUDE_INHERITANCE_DEPENDENCIES_ONLY                = "--include-inheritance-dependencies-only";
    static final String INCLUDE_LOOKUP_ONLY                                  = "--include-lookup-only";
    static final String INCLUDE_NAMESPACES                                   = "--include-namespaces";
    static final String INCLUDE_SOURCES                                      = "--include-sources";
    static final String INCLUDE_STYLESHEET                                   = "--include-stylesheet";
    static final String EXCLUDE_NATIVE_JS_LIBRARIES                          = "--exclude-native-js-libraries";

    // Setting options without the "--" separator.
    // These are used to set and get vars from the ConfigurationBuffer.
    static final String DUMP_CONFIG_VAR                                      = "dump-config";    
    static final String FILE_SPECS_VAR                                       = "file-specs";
    static final String INCLUDE_CLASSES_VAR                                 = "include-classes";
    static final String OUTPUT_VAR                                           = "output";
}
