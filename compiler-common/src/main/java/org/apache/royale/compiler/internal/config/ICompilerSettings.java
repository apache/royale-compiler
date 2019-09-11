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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.constants.IASWarningConstants;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;

/**
 * An interface that allows any of the compiler settings to be modified.
 */
public interface ICompilerSettings extends IWriteOnlyProjectSettings
{
    /**
     * Defines a token that can be used for token substitutions. On the command line, you use token substitution in 
     * the following way:
     * 
     * <pre>
     * mxmlc +royalelib=path1 +foo=bar -var=${foo}
     * </pre>
     * 
     * Where <code>var=bar</code> occurs after the substitution of <code>${foo}</code>.
     * 
     * @param name The name of the token.
     * @param value The value of the token.
     */
    void setToken(String name, String value);
    
    /**
     * Sets the context root path so that the compiler can replace <code>{context.root}</code> tokens for
     * service channel endpoints. This is equivalent to using the <code>compiler.context-root</code> option
     * for the mxmlc or compc compilers.
     * 
     * <p>
     * By default, this value is undefined.
     * 
     * @param path An instance of String.
     */
    void setContextRoot(String path);

    /**
     * Sets the list of SWC files or directories to compile against, but to omit from linking.
     * This is equivalent to using the <code>compiler.external-library-path</code> option of the 
     * mxmlc or compc compilers.
     * 
     * @param paths A list of paths. The <code>File.isDirectory()</code> method should return 
     * <code>true</code>; <code>File</code> instances should represent SWC files.
     */
    void setExternalLibraryPath(Collection<File> paths);

    /**
     * Adds to the existing list of SWC files.
     * 
     * @see #setExternalLibraryPath
     * 
     * @param paths A list of paths. The <code>File.isDirectory()</code> method should return 
     * <code>true</code>; <code>File</code> instances should represent SWC files.
     */
    void addExternalLibraryPath(Collection<File> paths);

    /**
     * Adds to the existing list of namespace mappings.
     * 
     * @param namespaceMappings
     */
    void addNamespaceMappings(Collection<IMXMLNamespaceMapping> namespaceMappings);

    /**
     * Sets a list of SWC files or directories that contain SWC files.
     * This is equivalent to using the <code>compiler.library-path</code> option of the mxmlc or compc compilers.
     * 
     * @param paths An array of <code>File</code> objects. The <code>File.isDirectory()</code> method should return 
     * <code>true</code>; instances must represent SWC files.
     */
    void setLibraryPath(Collection<File> paths);

    /**
     * Adds a list of path elements to the existing source path list.
     * 
     * @param paths A collection of <code>java.io.File</code> objects. The <code>File.isDirectory()</code> method must return <code>true</code>.
     * @see #setSourcePath
     */
    void addSourcePath(Collection<File> paths);
    
    /**
     * Enables accessibility in the application.
     * This is equivalent to using the <code>accessible</code> option of the mxmlc or compc compilers.
     * 
     * <p>
     * The default value is <code>false</code>.
     * 
     * @param b Boolean value that enables or disables accessibility.
     */
    void enableAccessibility(boolean b);

    /**
     * Runs the ActionScript compiler in strict error checking mode.
     * This is equivalent to using the <code>compiler.strict</code> option of the mxmlc or compc compilers.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void enableStrictChecking(boolean b);
    
    /**
     * Runs the ActionScript compiler in a mode that detects legal but potentially incorrect code.
     * This is equivalent to using the <code>compiler.show-actionscript-warnings</code> option of the 
     * mxmlc or compc compilers.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @see #checkActionScriptWarning(int, boolean)
     * 
     * @param b Boolean value.
     */
    void showActionScriptWarnings(boolean b);

    /**
     * Toggles whether warnings generated from data binding code are displayed.
     * This is equivalent to using the <code>compiler.show-binding-warnings</code> option of the mxmlc or compc compilers.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void showBindingWarnings(boolean b);

    /**
     * Toggles whether the use of deprecated APIs generates a warning.
     * This is equivalent to using the <code>compiler.show-deprecation-warnings</code> option of the mxmlc or compc compilers.
     * 
     * <p>   
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void showDeprecationWarnings(boolean b);

    /**
     * Toggles whether warnings generated from unused type selectors are displayed.
     * This is equivalent to using the <code>compiler.show-unused-type-selector-warnings</code> option of the mxmlc or compc
     * compilers.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void showUnusedTypeSelectorWarnings(boolean b);
    
    
    /**
     * Toggles whether warnings generated from two code files with the same name are displayed.
     * This is equivalent to using the <code>compiler.show-multiple-definition-warnings</code> option of the mxmlc or compc
     * compilers.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void showMultipleDefinitionWarnings(boolean b);
    
    /**
     * Sets a list of CSS or SWC files to apply as a theme.
     * This is equivalent to using the <code>compiler.theme</code> option of the mxmlc or compc compilers.
     * 
     * @param files An array of <code>java.io.File</code> objects.
     */
    void setTheme(List<File> files);

    /**
     * Adds a list of CSS or SWC files to the existing list of theme files.
     * 
     * @param files An array of <code>java.io.File</code> objects.
     * @see #setTheme
     */
    void addTheme(List<File> files);
    
    /**
     * Sets a list of run-time shared library URLs to be loaded before the application starts.
     * This is equivalent to the <code>runtime-shared-libraries</code> option of the mxmlc or compc compilers.
     * 
     * @param libraries An array of <code>java.lang.String</code> objects.
     */
    void setRuntimeSharedLibraries(List<String> libraries);

    /**
     * A list of RSLs to load, complete with all the settings on how to load
     * the RSLs. The RSLs will be loaded in the order that they appear in the
     * list. This is the complete list of RSLs that could possibly be loaded.
     * This list could be reduced if unused RSLs are being removed. 
     * 
     * These RSL settings override any settings in configuration files. Set an
     * empty list to disable the use of RSLs.
     * 
     * This is equivalent to using the following command line options in the
     * mxmlc compiler:
     * 
     * <ul>
     * <li> <code>runtime-shared-library-path</code>
     * <li> <code>application-domain</code>
     * <li> <code>force-rsls</code>
     * </ul>

     * If <code>rslSettings</code> is null, then all existing RSL settings are 
     * removed. It will have the effect of removing the RSL related options 
     * from the command line which will allow settings from configuration files
     * to be applied to the configuration. 
     *
     * @param rslSettings A list of {@link RSLSettings}.
     */    
    void setRuntimeSharedLibraryPath(List<RSLSettings> rslSettings);
    
    
    /**
     * A list of RSLs to load, complete with all the settings on how to load
     * the RSLs. The RSLs will be loaded in the order that they appear in the
     * list. This is the complete list of RSLs that could possibly be loaded.
     * This list could be reduced if unused RSLs are being removed. 
     * 
     * The RSL provided to this method append to list of RSLs specified in any
     * configuration files.
     * 
     * This is equivalent to using the following command line options in the
     * mxmlc compiler:
     * 
     * <ul>
     * <li> <code>runtime-shared-library-path+=</code>
     * <li> <code>application-domain+=</code>
     * <li> <code>force-rsls+=</code>
     * </ul>
     *
     * @param rslSettings A list of {@link RSLSettings}. May not be null.
     * @throws NullPointerException if <code>rslSettings</code> is null.
     */    
    void addRuntimeSharedLibraryPath(List<RSLSettings> rslSettings);
    
    /**
     * Verifies the RSL loaded 
     * has the same digest as the RSL specified when the application was compiled.
     * This is equivalent to using the <code>verify-digests</code>
     * option in the mxmlc compiler.
     * 
     *  The default value is <code>true</code>
     * 
     * @param verify set to true to verify
     *               the digest of the loaded RSL matches the digest of the
     *               expected RSL. Set to false to disable the checks during
     *               the development process but it is highly recommend that 
     *               production applications be compiled with <code>verify</code>
     *               set to true.  
     * 
     * @since 3.0
     */
    void enableDigestVerification(boolean verify);

   /**
     * Enables the removal of RSLs associated with libraries
     * that are not used by an application.
     * This is equivalent to using the
     * <code>remove-unused-rsls</code> option of the mxmlc compiler.
     * 
     * <p>
     * The default value is <code>false</code>.
     * 
     * @param b Boolean value that enables or disables the removal.
     *    
     * @since 4.5
     */
    void removeUnusedRuntimeSharedLibraryPaths(boolean b);

    /**
     * Sets a list of definitions to omit from linking when building an application.
     * This is equivalent to using the <code>externs</code> option of the mxmlc and compc compilers.
     * 
     * @param definitions An array of definitions (for example, classes, functions, variables, or namespaces).
     */
    void setExterns(Collection<String> definitions);

    /**
     * Enables post-link optimization. This is equivalent to using the <code>compiler.optimize</code> option of the
     * mxmlc or compc compilers. Application sizes are usually smaller with this option enabled.
     * 
     * <p>
     * The default value is <code>true</code>.
     * 
     * @param b Boolean value.
     */
    void optimize(boolean b);


    /**
     * Enables debugging in the application.
     * This is equivalent to using the <code>compiler.debug</code> and <code>-debug-password=true|false</code> options
     * for the mxmlc or compc compilers.
     * 
     * <p>
     * The default value <code>debug</code> is <code>false</code>. The default debug password is "".
     * 
     * @param b Boolean value that enables or disables debugging.
     * @param debugPassword A password that is embedded in the application.
     */
    void enableDebugging(boolean b, String debugPassword);

    /**
     * Enables ZLIB compression on SWF file. This is equivalent to using the <code>compiler.compress</code> option of the
     * mxmlc or compc compilers. Application sizes are usually smaller with this option enabled.
     * 
     * @param b Boolean value.
     */    
    void compress(boolean b);

    /**
     * Allows some source path directories to be subdirectories of the other.
     * This is equivalent to using <code>mxmlc/compc --compiler.allow-source-path-overlap</code>.<p>
     * By default, this is disabled.<p>
     * 
     * In some J2EE settings, directory overlapping should be allowed. For example,
     * 
     * <pre>
     * wwwroot/MyAppRoot
     * wwwroot/WEB-INF/flex/source_path1
     * </pre>
     * 
     * @param b boolean value
     */
    void allowSourcePathOverlap(boolean b);

    /**
     * Determines whether resources bundles are included in the application.
     * This is equivalent to using <code>mxmlc/compc --compiler.use-resource-bundle-metadata</code>.
     * By default, it is set to <code>true</code>.
     * 
     * @param b boolean value
     */
    void useResourceBundleMetaData(boolean b);
    
    //---------------------------------------------------------------------------
    // Not used by Flash Builder
    /**
     * <code>Array.toString()</code> format has changed.
     */
    int WARN_ARRAY_TO_STRING_CHANGES = IASWarningConstants.ARRAY_TO_STRING_CHANGES;

    /**
     * Assignment within conditional.
     */
    int WARN_ASSIGNMENT_WITHIN_CONDITIONAL = IASWarningConstants.ASSIGNMENT_WITHIN_CONDITIONAL;

    /**
     * Possibly invalid Array cast operation.
     */
    int WARN_BAD_ARRAY_CAST = IASWarningConstants.BAD_ARRAY_CAST;

    /**
     * Non-Boolean value used where a <code>Boolean</code> value was expected.
     */
    int WARN_BAD_BOOLEAN_ASSIGNMENT = IASWarningConstants.BAD_BOOLEAN_ASSIGNMENT;

    /**
     * Invalid <code>Date</code> cast operation.
     */
    int WARN_BAD_DATE_CAST = IASWarningConstants.BAD_DATE_CAST;

    /**
     * Unknown method.
     */
    int WARN_BAD_ES3_TYPE_METHOD = IASWarningConstants.BAD_ES3_TYPE_METHOD;

    /**
     * Unknown property.
     */
    int WARN_BAD_ES3_TYPE_PROP = IASWarningConstants.BAD_ES3_TYPE_PROP;

    /**
     * Illogical comparison with <code>NaN</code>. Any comparison operation involving <code>NaN</code> will evaluate to <code>false</code> because <code>NaN != NaN</code>.
     */
    int WARN_BAD_NAN_COMPARISON = IASWarningConstants.BAD_NAN_COMPARISON;

    /**
     * Impossible assignment to <code>null</code>.
     */
    int WARN_BAD_NULL_ASSIGNMENT = IASWarningConstants.BAD_NULL_ASSIGNMENT;

    /**
     * Illogical comparison with <code>null</code>.
     */
    int WARN_BAD_NULL_COMPARISON = IASWarningConstants.BAD_NULL_COMPARISON;

    /**
     * Illogical comparison with <code>undefined</code>.  Only untyped variables (or variables of type <code>*</code>) can be <code>undefined</code>.
     */
    int WARN_BAD_UNDEFINED_COMPARISON = IASWarningConstants.BAD_UNDEFINED_COMPARISON;

    /**
     * <code>Boolean()</code> with no arguments returns <code>false</code> in ActionScript 3.0.
     * <code>Boolean()</code> returned <code>undefined</code> in ActionScript 2.0.
     */
    int WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS = IASWarningConstants.BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS;

    /**
     * <code>__resolve</code> is deprecated.
     */
    int WARN_CHANGES_IN_RESOLVE = IASWarningConstants.CHANGES_IN_RESOLVE;

    /**
     * <code>Class</code> is sealed. It cannot have members added to it dynamically.
     */
    int WARN_CLASS_IS_SEALED = IASWarningConstants.CLASS_IS_SEALED;

    /**
     * Constant not initialized.
     */
    int WARN_CONST_NOT_INITIALIZED = IASWarningConstants.CONST_NOT_INITIALIZED;

    /**
     * Function used in new expression returns a value.  Result will be what the function returns, rather than a new instance of that function.
     */
    int WARN_CONSTRUCTOR_RETURNS_VALUE = IASWarningConstants.CONSTRUCTOR_RETURNS_VALUE;

    /**
     * EventHandler was not added as a listener.
     */
    int WARN_DEPRECATED_EVENT_HANDLER_ERROR = IASWarningConstants.DEPRECATED_EVENT_HANDLER_ERROR;

    /**
     * Unsupported ActionScript 2.0 function.
     */
    int WARN_DEPRECATED_FUNCTION_ERROR = IASWarningConstants.DEPRECATED_FUNCTION_ERROR;

    /**
     * Unsupported ActionScript 2.0 property.
     */
    int WARN_DEPRECATED_PROPERTY_ERROR = IASWarningConstants.DEPRECATED_PROPERTY_ERROR;

    /**
     * More than one argument by the same name.
     */
    int WARN_DUPLICATE_ARGUMENT_NAMES = IASWarningConstants.DUPLICATE_ARGUMENT_NAMES;

    /**
     * Duplicate variable definition
     */
    int WARN_DUPLICATE_VARIABLE_DEF = IASWarningConstants.DUPLICATE_VARIABLE_DEF;

    /**
     * ActionScript 3.0 iterates over an object's properties within a "<code>for x in target</code>" statement in random order.
     */
    int WARN_FOR_VAR_IN_CHANGES = IASWarningConstants.FOR_VAR_IN_CHANGES;

    /**
     * Importing a package by the same name as the current class will hide that class identifier in this scope.
     */
    int WARN_IMPORT_HIDES_CLASS = IASWarningConstants.IMPORT_HIDES_CLASS;

    /**
     * Use of the <code>instanceof</code> operator.
     */
    int WARN_INSTANCEOF_CHANGES = IASWarningConstants.INSTANCEOF_CHANGES;

    /**
     * Internal error in compiler.
     */
    int WARN_INTERNAL_ERROR = IASWarningConstants.INTERNAL_ERROR;

    /**
     * <code>_level</code> is no longer supported. For more information, see the <code>flash.display</code> package.
     */
    int WARN_LEVEL_NOT_SUPPORTED = IASWarningConstants.LEVEL_NOT_SUPPORTED;

    /**
     * Missing namespace declaration (e.g. variable is not defined to be <code>public</code>, <code>private</code>, etc.).
     */
    int WARN_MISSING_NAMESPACE_DECL = IASWarningConstants.MISSING_NAMESPACE_DECL;

    /**
     * Negative value will become a large positive value when assigned to a <code>uint</code> data type.
     */
    int WARN_NEGATIVE_UINT_LITERAL = IASWarningConstants.NEGATIVE_UINT_LITERAL;

    /**
     * Missing constructor.
     */
    int WARN_NO_CONSTRUCTOR = IASWarningConstants.NO_CONSTRUCTOR;

    /**
     * The <code>super()</code> statement was not called within the constructor.
     */
    int WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR = IASWarningConstants.NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR;

    /**
     * Missing type declaration.
     */
    int WARN_NO_TYPE_DECL = IASWarningConstants.NO_TYPE_DECL;

    /**
     * In ActionScript 3.0, white space is ignored and <code>''</code> returns <code>0</code>.
     * <code>Number()</code> returns <code>NaN</code> in ActionScript 2.0 when the parameter is <code>''</code> or contains white space.
     */
    int WARN_NUMBER_FROM_STRING_CHANGES = IASWarningConstants.NUMBER_FROM_STRING_CHANGES;

    /**
     * Change in scoping for the <code>this</code> keyword.
     * Class methods extracted from an instance of a class will always resolve <code>this</code> back to that instance.
     * In ActionScript 2.0, <code>this</code> is looked up dynamically based on where the method is invoked from.
     */
    int WARN_SCOPING_CHANGE_IN_THIS = IASWarningConstants.SCOPING_CHANGE_IN_THIS;

    /**
     * Inefficient use of <code>+=</code> on a <code>TextField</code>.
     */
    int WARN_SLOW_TEXTFIELD_ADDITION = IASWarningConstants.SLOW_TEXTFIELD_ADDITION;

    /**
     * Possible missing parentheses.
     */
    int WARN_UNLIKELY_FUNCTION_VALUE = IASWarningConstants.UNLIKELY_FUNCTION_VALUE;

    /**
     * Possible usage of the ActionScript 2.0 <code>XML</code> class.
     */
    int WARN_XML_CLASS_HAS_CHANGED = IASWarningConstants.XML_CLASS_HAS_CHANGED;

    /**
     * Keyword this within closure.
     */
    int WARN_THIS_WITHIN_CLOSURE = IASWarningConstants.THIS_WITHIN_CLOSURE;
     
    /**
     * Enables checking of the following ActionScript warnings:
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
     * @param warningCode Warning code.
     * @param b Boolean value.
     * 
     * @see #WARN_ARRAY_TO_STRING_CHANGES
     * @see #WARN_ASSIGNMENT_WITHIN_CONDITIONAL
     * @see #WARN_BAD_ARRAY_CAST
     * @see #WARN_BAD_BOOLEAN_ASSIGNMENT
     * @see #WARN_BAD_DATE_CAST
     * @see #WARN_BAD_ES3_TYPE_METHOD
     * @see #WARN_BAD_ES3_TYPE_PROP
     * @see #WARN_BAD_NAN_COMPARISON
     * @see #WARN_BAD_NULL_ASSIGNMENT
     * @see #WARN_BAD_NULL_COMPARISON
     * @see #WARN_BAD_UNDEFINED_COMPARISON
     * @see #WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS
     * @see #WARN_CHANGES_IN_RESOLVE
     * @see #WARN_CLASS_IS_SEALED
     * @see #WARN_CONST_NOT_INITIALIZED
     * @see #WARN_CONSTRUCTOR_RETURNS_VALUE
     * @see #WARN_DEPRECATED_EVENT_HANDLER_ERROR
     * @see #WARN_DEPRECATED_FUNCTION_ERROR
     * @see #WARN_DEPRECATED_PROPERTY_ERROR
     * @see #WARN_DUPLICATE_ARGUMENT_NAMES
     * @see #WARN_DUPLICATE_VARIABLE_DEF
     * @see #WARN_FOR_VAR_IN_CHANGES
     * @see #WARN_IMPORT_HIDES_CLASS
     * @see #WARN_INSTANCEOF_CHANGES
     * @see #WARN_INTERNAL_ERROR
     * @see #WARN_LEVEL_NOT_SUPPORTED
     * @see #WARN_MISSING_NAMESPACE_DECL
     * @see #WARN_NEGATIVE_UINT_LITERAL
     * @see #WARN_NO_CONSTRUCTOR
     * @see #WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR
     * @see #WARN_NO_TYPE_DECL
     * @see #WARN_NUMBER_FROM_STRING_CHANGES
     * @see #WARN_SCOPING_CHANGE_IN_THIS
     * @see #WARN_SLOW_TEXTFIELD_ADDITION
     * @see #WARN_UNLIKELY_FUNCTION_VALUE
     * @see #WARN_XML_CLASS_HAS_CHANGED
     * @see #WARN_THIS_WITHIN_CLOSURE
     */
    void checkActionScriptWarning(int warningCode, boolean b);

    /**
     * A contributor's name to store in the SWF metadata.
     */
    int CONTRIBUTOR = 1;

    /**
     * A creator's name to store in the SWF metadata.
     */
    int CREATOR     = 2;

    /**
     * The creation date to store in the SWF metadata.
     */
    int DATE        = 4;

    /**
     * The default and localized RDF/XMP description to store in the SWF metadata.
     */
    int DESCRIPTION = 8;

    /**
     * The default and localized RDF/XMP title to store in the SWF metadata.
     */
    int TITLE       = 16;

    /**
     * The language to store in the SWF metadata (i.e. EN, FR).
     */
    int LANGUAGE    = 32;

    /**
     * A publisher's name to store in the SWF metadata.
     */
    int PUBLISHER   = 64;
    
    /**
     * Sets the metadata section of the application SWF. This option is equivalent to using the following options of
     * the mxmlc and compc compilers:
     * 
     * <pre>
     * metadata.contributor
     * metadata.creator
     * metadata.date
     * metadata.description
     * metadata.language
     * metadata.localized-description
     * metadata.localized-title
     * metadata.publisher
     * metadata.title
     * </pre>
     * 
     * <p>
     * The valid fields and types of value are specified below:
     * 
     * <pre>
     * CONTRIBUTOR      java.lang.String
     * CREATOR          java.lang.String
     * DATE             java.util.Date
     * DESCRIPTION      java.util.Map<String, String>
     * TITLE            java.util.Map<String, String>
     * LANGUAGE         java.lang.String
     * PUBLISHER        java.lang.String
     * </pre>
     * 
     * For example:
     * 
     * <pre>
     * Map titles = new HashMap();
     * titles.put("EN", "Apache Royale 1.0.0 Application");
     * 
     * Map descriptions = new HashMap();
     * descriptions.put("EN", "http://royale.apache.org");
     * 
     * setSWFMetadata(Configuration.LANGUAGE, "EN");
     * setSWFMetadata(Configuration.TITLE, titles);
     * setSWFMetadata(Configuration.DESCRIPTION, descriptions);
     * </pre>
     * 
     * @param field One of: <code>CONTRIBUTOR</code>, <code>CREATOR</code>, 
     * <code>DATE</code>, <code>DESCRIPTION</code>, <code>TITLE</code>, 
     * <code>LANGUAGE</code>, or <code>PUBLISHER</code>.
     * @param value A <code>String</code>, <code>Date</code>, or 
     * <code>Map</code> object.
     * 
     * @see #CONTRIBUTOR
     * @see #CREATOR
     * @see #DATE
     * @see #DESCRIPTION
     * @see #TITLE
     * @see #LANGUAGE
     * @see #PUBLISHER
     */
    void setSWFMetadata(int field, Object value);

    /**
     * Sets the metadata section of the application SWF.
     * This is equivalent to using <code>mxmlc/compc --raw-metadata</code>.
     * This option overrides everything set by the <code>setSWFMetadata</code> method.
     * 
     * @see #setSWFMetadata
     * @param xml a well-formed XML fragment
     */
    void setSWFMetadata(String xml);
    
    /**
     * Add to the existing list of locales without overriding and locales 
     * settings in configuration files.
     *  
     * The <code>locale/en_US</code> directory will be added to the source path.
     * 
     */
    void addLocales(Collection<String> locales);

    /**
     * Set the output of the target.
     * 
     * @param output File that specifies the where the target should be 
     * created.
     */
    void setOutput(File output);

    /**
     * Includes a list of libraries (SWCs) to completely include in the application
     * This is equivalent to using <code>mxmlc/compc --compiler.include-libraries</code>.
     * 
     * @param libraries a collection of <code>java.io.File</code> (<code>File.isDirectory()</code> should return <code>true</code> or instances must represent SWC files).
     */
    void setIncludeLibraries(Collection<File> libraries);

    /**
     * Provides a list of resource bundle names that should be compiled into
     * a resource module or library.
     * 
     * @param bundles A collection of resource bundle names.
     */
    void setIncludeResourceBundles(Collection<String> bundles);
    
    /**
     * Sets the version of the Flash Player that is being targeted by the application.  
     * Features requiring a later version of the Player will not be compiled into the application.
     *
     * @param major The major version. Must be greater than or equal to nine.
     * @param minor The minor version. Must be greater than or equal to zero.
     * @param revision The revsion must be greater than or equal to zero.
     * 
     */
    void setTargetPlayer(int major, int minor, int revision);

    //
    // Library Settings
    //
    
    /**
     * Adds a class, function, variable, or namespace to the target library.
     *
     * This is the equilvalent of the <code>include-classes</code> option of 
     * the compc compiler.
     *
     * @param includeClasses a collection of fully-qualified class names.
     */
    void setIncludeClasses(Collection<String> includeClasses);
    
    /**
     * Adds a collection of files to the target library. This is equivalent to the 
     * <code>include-file</code> option of the compc compiler.
     * 
     * Each entry in the map represents a file to include in the target 
     * library. The key is the name of the file in the target library and the
     * value is the file that will be added to the library.
     *
     * @param files The collection of files to be added.
     */
    void setIncludeFiles(Map<String, File> files);
    
    /**
     * Adds a collection of namespaces to include in this target library.
     *
     * This is equivalent to the <code>include-namespaces</code> option of the
     * compc compiler.
     *
     * @param namespaces A collection of Strings where each String is a 
     * namespace URI.
     */
    void setIncludeNamespaces(Collection<String> namespaces);
    
    /**
     * Specifies a map of style sheets to add to the target library. The map's
     * key/value pairs are as follows:
     *     key:   the name of the file in the target library
     *     value: the File to include.
     *
     * @param styleSheets a map of names to Files to include in the target 
     * library.
     */
    void setIncludeStyleSheet(Map<String, File> styleSheets);
    
    /**
     * Controls whether manifest entries with lookupOnly=true are included in
     * the SWC catalog.
     * 
     *  @param include true only manifest entries with lookupOnly=true are included in 
     *  the SWC catalog, false otherwise.
     */
    void enableIncludeLookupOnly(boolean include);

}
