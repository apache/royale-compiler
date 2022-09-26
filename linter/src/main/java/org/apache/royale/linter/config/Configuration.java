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

package org.apache.royale.linter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.problems.DeprecatedConfigurationOptionProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.RemovedConfigurationOptionProblem;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.collect.ImmutableList;

public class Configuration {

    private static Map<String, String> aliases = null;

    public static Map<String, String> getAliases() {
        if (aliases == null) {
            aliases = new HashMap<String, String>();

            // aliases.put("s", "short-option");
        }
        return aliases;
    }

    //
    // PathResolver
    //
    private IPathResolver pathResolver;

    /**
     * Set a path resolver to resolver files relative to a configuration. Files inside of configuration files are
     * resolved relative to those configuration files and files on the command line are resolved relative to the root
     * directory of the compile.
     * 
     * @param pathResolver a path resolver for this configuration. May not be null.
     */
    public void setPathResolver(IPathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }

    /**
     * Collection of fatal and non-fatal configuration problems.
     */
    private Collection<ICompilerProblem> configurationProblems = new ArrayList<ICompilerProblem>();

    /**
     * Get the configuration problems. This should be called after the configuration
     * has been processed.
     * 
     * @return a collection of fatal and non-fatal configuration problems.
     */
    public Collection<ICompilerProblem> getConfigurationProblems() {
        return configurationProblems;
    }

    /**
     * Validate configuration options values.
     * 
     * @param configurationBuffer Configuration buffer.
     * @throws ConfigurationException Error.
     */
    public void validate(ConfigurationBuffer configurationBuffer) throws ConfigurationException {
        // process the merged configuration buffer. right, can't just process the args.
        processDeprecatedAndRemovedOptions(configurationBuffer);
    }

    private void processDeprecatedAndRemovedOptions(ConfigurationBuffer configurationBuffer) {
        for (final String var : configurationBuffer.getVars()) {
            ConfigurationInfo info = configurationBuffer.getInfo(var);
            List<ConfigurationValue> values = configurationBuffer.getVar(var);
            if (values != null) {
                for (final ConfigurationValue cv : values) {
                    if (info.isRemoved()) {
                        addRemovedConfigurationOptionProblem(cv);
                    } else if (info.isDeprecated() && configurationBuffer.getVar(var) != null) {
                        String replacement = info.getDeprecatedReplacement();
                        String since = info.getDeprecatedSince();
                        DeprecatedConfigurationOptionProblem problem = new DeprecatedConfigurationOptionProblem(var,
                                replacement, since, cv.getSource(), cv.getLine());
                        configurationProblems.add(problem);
                    }
                }
            }
        }
    }

    /**
     * Add a RemovedConfigurationOptionProblem to the list of configuration
     * problems.
     * 
     * @param cv
     */
    private void addRemovedConfigurationOptionProblem(ConfigurationValue cv) {
        RemovedConfigurationOptionProblem problem = new RemovedConfigurationOptionProblem(cv.getVar(), cv.getSource(),
                cv.getLine());
        configurationProblems.add(problem);
    }

    //
    // 'help' option from CommandLineConfiguration
    //

    /**
     * dummy, just a trigger for help text
     */
    @Config(displayed = false, greedy = true)
    @Arguments("keyword")
    @InfiniteArguments
    public void setHelp(ConfigurationValue cv, String[] keywords) {

    }

    //
    // 'version' option from CommandLineConfiguration
    //

    /**
     * Dummy option. Just a trigger for version info.
     */
    @Config
    public void setVersion(ConfigurationValue cv, boolean value) {
    }

    //
    // 'load-config' option from CommandLineConfiguration
    //

    private String configFile = null;

    /**
     * @return Normalized path to a Flex configuration file.
     */
    public String getLoadConfig()
    {
        return configFile;
    }

    /**
     * Since {@link ConfigurationBuffer} loads the "load-config" files, the value of this configuration option isn't
     * intersting to the rest part of the compiler.
     */
    @Config(allowMultiple = true)
    @Arguments("filename")
    public void setLoadConfig(ConfigurationValue cv, String filename) throws ConfigurationException
    {
        configFile = resolvePathStrict(filename, cv);   
    }

    //
    // 'files' option
    //

    private List<String> files = new ArrayList<String>();

    /**
     * @return A list of filespecs. It's the default variable for command line.
     */
    public List<String> getFiles() {
        return files;
    }

    @Config(allowMultiple = true, hidden = true)
    @Mapping("files")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setFiles(ConfigurationValue cv, List<String> args) throws ConfigurationException {
        this.files.addAll(args);
    }

    //
    // 'ignore-parsing-problems' option
    //

    private boolean ignoreParsingProblems = false;

    public boolean getIgnoreParsingProblems() {
        return ignoreParsingProblems;
    }

    @Config(advanced = true)
    @Mapping("ignore-parsing-problems")
    public void setIgnoreParsingProblems(ConfigurationValue cv, boolean b) {
        this.ignoreParsingProblems = b;
    }

    //
    // 'any-type' option
    //

    private boolean anyType = false;

    public boolean getAnyType() {
        return anyType;
    }

    @Config
    @Mapping("any-type")
    public void setAnyType(ConfigurationValue cv, boolean b) {
        this.anyType = b;
    }

    //
    // 'boolean-equality' option
    //

    private boolean booleanEquality = false;

    public boolean getBooleanEquality() {
        return booleanEquality;
    }

    @Config
    @Mapping("boolean-equality")
    public void setBooleanEquality(ConfigurationValue cv, boolean b) {
        this.booleanEquality = b;
    }

    //
    // 'class-name' option
    //

    private boolean className = false;

    public boolean getClassName() {
        return className;
    }

    @Config
    @Mapping("class-name")
    public void setClassName(ConfigurationValue cv, boolean b) {
        this.className = b;
    }

    //
    // 'constant-name' option
    //

    private boolean constantName = false;

    public boolean getConstantName() {
        return constantName;
    }

    @Config
    @Mapping("constant-name")
    public void setConstantName(ConfigurationValue cv, boolean b) {
        this.constantName = b;
    }

    //
    // 'constructor-dispatch-event' option
    //

    private boolean constructorDispatchEvent = false;

    public boolean getConstructorDispatchEvent() {
        return constructorDispatchEvent;
    }

    @Config
    @Mapping("constructor-dispatch-event")
    public void setConstructorDispatchEvent(ConfigurationValue cv, boolean b) {
        this.constructorDispatchEvent = b;
    }

    //
    // 'constructor-return-type' option
    //

    private boolean constructorReturnType = false;

    public boolean getConstructorReturnType() {
        return constructorReturnType;
    }

    @Config
    @Mapping("constructor-return-type")
    public void setConstructorReturnType(ConfigurationValue cv, boolean b) {
        this.constructorReturnType = b;
    }

    //
    // 'dynamic-class' option
    //

    private boolean dynamicClass = false;

    public boolean getDynamicClass() {
        return dynamicClass;
    }

    @Config
    @Mapping("dynamic-class")
    public void setDynamicClass(ConfigurationValue cv, boolean b) {
        this.dynamicClass = b;
    }

    //
    // 'empty-comment' option
    //

    private boolean emptyComment = false;

    public boolean getEmptyComment() {
        return emptyComment;
    }

    @Config
    @Mapping("empty-comment")
    public void setEmptyComment(ConfigurationValue cv, boolean b) {
        this.emptyComment = b;
    }

    //
    // 'empty-function-body' option
    //

    private boolean emptyFunctionBody = false;

    public boolean getEmptyFunctionBody() {
        return emptyFunctionBody;
    }

    @Config
    @Mapping("empty-function-body")
    public void setEmptyFunctionBody(ConfigurationValue cv, boolean b) {
        this.emptyFunctionBody = b;
    }

    //
    // 'empty-nested-block' option
    //

    private boolean emptyNestedBlock = false;

    public boolean getEmptyNestedBlock() {
        return emptyNestedBlock;
    }

    @Config
    @Mapping("empty-nested-block")
    public void setEmptyNestedBlock(ConfigurationValue cv, boolean b) {
        this.emptyNestedBlock = b;
    }

    //
    // 'empty-statement' option
    //

    private boolean emptyStatement = false;

    public boolean getEmptyStatement() {
        return emptyStatement;
    }

    @Config
    @Mapping("empty-statement")
    public void setEmptyStatement(ConfigurationValue cv, boolean b) {
        this.emptyStatement = b;
    }

    //
    // 'field-name' option
    //

    private boolean fieldName = false;

    public boolean getFieldName() {
        return fieldName;
    }

    @Config
    @Mapping("field-name")
    public void setFieldName(ConfigurationValue cv, boolean b) {
        this.fieldName = b;
    }

    //
    // 'function-name' option
    //

    private boolean functionName = false;

    public boolean getFunctionName() {
        return functionName;
    }

    @Config
    @Mapping("function-name")
    public void setFunctionName(ConfigurationValue cv, boolean b) {
        this.functionName = b;
    }

    //
    // 'if-boolean' option
    //

    private boolean ifBoolean = false;

    public boolean getIfBoolean() {
        return ifBoolean;
    }

    @Config
    @Mapping("if-boolean")
    public void setIfBoolean(ConfigurationValue cv, boolean b) {
        this.ifBoolean = b;
    }

    //
    // 'interface-name' option
    //

    private boolean interfaceName = false;

    public boolean getInterfaceName() {
        return interfaceName;
    }

    @Config
    @Mapping("interface-name")
    public void setInterfaceName(ConfigurationValue cv, boolean b) {
        this.interfaceName = b;
    }

    //
    // 'line-comment-position' option
    //

    private LineCommentPosition lineCommentPosition = null;

    public String getLineCommentPosition() {
        if (lineCommentPosition == null) {
            return null;
        }
        return lineCommentPosition.getPosition();
    }

    @Config
    @Mapping("line-comment-position")
    public void setLineCommentPosition(ConfigurationValue cv, String s) {
        this.lineCommentPosition = LineCommentPosition.valueOf(s.toUpperCase());
    }

    //
    // 'local-var-param-name' option
    //

    private boolean localVarParamName = false;

    public boolean getLocalVarParamName() {
        return localVarParamName;
    }

    @Config
    @Mapping("local-var-param-name")
    public void setLocalVarParamName(ConfigurationValue cv, boolean b) {
        this.localVarParamName = b;
    }

    //
    // 'local-var-shadows-field' option
    //

    private boolean localVarShadowsField = false;

    public boolean getLocalVarShadowsField() {
        return localVarShadowsField;
    }

    @Config
    @Mapping("local-var-shadows-field")
    public void setLocalVarShadowsField(ConfigurationValue cv, boolean b) {
        this.localVarShadowsField = b;
    }

    //
    // 'max-block-depth' option
    //

    private int maxBlockDepth = 0;

    public int getMaxBlockDepth() {
        return maxBlockDepth;
    }

    @Config
    @Mapping("max-block-depth")
    public void setMaxBlockDepth(ConfigurationValue cv, int i) {
        this.maxBlockDepth = i;
    }

    //
    // 'max-params' option
    //

    private int maxParams = 0;

    public int getMaxParams() {
        return maxParams;
    }

    @Config
    @Mapping("max-params")
    public void setMaxParams(ConfigurationValue cv, int i) {
        this.maxParams = i;
    }

    //
    // 'missing-asdoc' option
    //

    private boolean missingAsdoc = false;

    public boolean getMissingAsdoc() {
        return missingAsdoc;
    }

    @Config
    @Mapping("missing-asdoc")
    public void setMissingAsdoc(ConfigurationValue cv, boolean b) {
        this.missingAsdoc = b;
    }

    //
    // 'missing-namespace' option
    //

    private boolean missingNamespace = false;

    public boolean getMissingNamespace() {
        return missingNamespace;
    }

    @Config
    @Mapping("missing-namespace")
    public void setMissingNamespace(ConfigurationValue cv, boolean b) {
        this.missingNamespace = b;
    }

    //
    // 'missing-semicolon' option
    //

    private boolean missingSemicolon = false;

    public boolean getMissingSemicolon() {
        return missingSemicolon;
    }

    @Config
    @Mapping("missing-semicolon")
    public void setMissingSemicolon(ConfigurationValue cv, boolean b) {
        this.missingSemicolon = b;
    }

    //
    // 'missing-type' option
    //

    private boolean missingType = false;

    public boolean getMissingType() {
        return missingType;
    }

    @Config
    @Mapping("missing-type")
    public void setMissingType(ConfigurationValue cv, boolean b) {
        this.missingType = b;
    }

    //
    // 'mxml-empty-attr' option
    //

    private boolean mxmlEmptyAttr = false;

    public boolean getMxmlEmptyAttr() {
        return mxmlEmptyAttr;
    }

    @Config
    @Mapping("mxml-empty-attr")
    public void setMxmlEmptyAttr(ConfigurationValue cv, boolean b) {
        this.mxmlEmptyAttr = b;
    }

    //
    // 'mxml-id' option
    //

    private boolean mxmlId = false;

    public boolean getMxmlId() {
        return mxmlId;
    }

    @Config
    @Mapping("mxml-id")
    public void setMxmlId(ConfigurationValue cv, boolean b) {
        this.mxmlId = b;
    }

    //
    // 'leading-zero' option
    //

    private boolean leadingZero = false;

    public boolean getLeadingZero() {
        return leadingZero;
    }

    @Config
    @Mapping("leading-zero")
    public void setLeadingZero(ConfigurationValue cv, boolean b) {
        this.leadingZero = b;
    }

    //
    // 'override-super' option
    //

    private boolean overrideSuper = false;

    public boolean getOverrideSuper() {
        return overrideSuper;
    }

    @Config
    @Mapping("override-super")
    public void setOverrideSuper(ConfigurationValue cv, boolean b) {
        this.overrideSuper = b;
    }

    //
    // 'package-name' option
    //

    private boolean packageName = false;

    public boolean getPackageName() {
        return packageName;
    }

    @Config
    @Mapping("package-name")
    public void setPackageName(ConfigurationValue cv, boolean b) {
        this.packageName = b;
    }

    //
    // 'static-constants' option
    //

    private boolean staticConstants = false;

    public boolean getStaticConstants() {
        return staticConstants;
    }

    @Config
    @Mapping("static-constants")
    public void setStaticConstants(ConfigurationValue cv, boolean b) {
        this.staticConstants = b;
    }

    //
    // 'strict-equality' option
    //

    private boolean strictEquality = false;

    public boolean getStrictEquality() {
        return strictEquality;
    }

    @Config
    @Mapping("strict-equality")
    public void setStrictEquality(ConfigurationValue cv, boolean b) {
        this.strictEquality = b;
    }

    //
    // 'string-event' option
    //

    private boolean stringEvent = false;

    public boolean getStringEvent() {
        return stringEvent;
    }

    @Config
    @Mapping("string-event")
    public void setStringEvent(ConfigurationValue cv, boolean b) {
        this.stringEvent = b;
    }

    //
    // 'switch-default' option
    //

    private boolean switchDefault = false;

    public boolean getSwitchDefault() {
        return switchDefault;
    }

    @Config
    @Mapping("switch-default")
    public void setSwitchDefault(ConfigurationValue cv, boolean b) {
        this.switchDefault = b;
    }

    //
    // 'this-closure' option
    //

    private boolean thisClosure = false;

    public boolean getThisClosure() {
        return thisClosure;
    }

    @Config
    @Mapping("this-closure")
    public void setThisClosure(ConfigurationValue cv, boolean b) {
        this.thisClosure = b;
    }

    //
    // 'trace' option
    //

    private boolean trace = false;

    public boolean getTrace() {
        return trace;
    }

    @Config
    @Mapping("trace")
    public void setTrace(ConfigurationValue cv, boolean b) {
        this.trace = b;
    }

    //
    // 'wildcard-import' option
    //

    private boolean wildcardImport = false;

    public boolean getWildcardImport() {
        return wildcardImport;
    }

    @Config
    @Mapping("wildcard-import")
    public void setWildcardImport(ConfigurationValue cv, boolean b) {
        this.wildcardImport = b;
    }

    //
    // 'with' option
    //

    private boolean with = false;

    public boolean getWith() {
        return with;
    }

    @Config
    @Mapping("with")
    public void setWith(ConfigurationValue cv, boolean b) {
        this.with = b;
    }

    /**
     * 
     * @param path A path to resolve.
     * @param cv Configuration context.
     * @return A single normalized resolved file. If the path could be expanded into more than one path, then use
     *         {@link resolvePathsStrict}
     * @throws CannotOpen
     */
    protected String resolvePathStrict(final String path, final ConfigurationValue cv) throws CannotOpen
    {
        return resolvePathStrict(path, cv, false);
    }

    /**
     * Resolve a single path. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param path A path to resolve.
     * @param cv Configuration context.
     * @param returnMissingFiles Determines if the CannotOpen exception is thrown if a file does not exist. Pass true to
     *        disable exceptions and return files that do not exist. Pass false to throw exceptions.
     * @return A single normalized resolved file. If the path could be expanded into more than one path, then use
     *         {@link resolvePathsStrict}.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private String resolvePathStrict(final String path, final ConfigurationValue cv, final boolean returnMissingFiles)
            throws CannotOpen
    {
        ImmutableList<String> singletonPath = ImmutableList.of(path);
        ImmutableList<String> results = resolvePathsStrict(singletonPath, cv, returnMissingFiles);
        return results.get(0);
    }

    /**
     * Resolve a list of paths. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param paths A list of paths to resolve.
     * @param cv Configuration context.
     * @return A list of normalized resolved file paths.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private ImmutableList<String> resolvePathsStrict(final ImmutableList<String> paths, final ConfigurationValue cv)
            throws CannotOpen
    {
        return resolvePathsStrict(paths, cv, false);
    }

    /**
     * Resolve a list of paths. This is a more strict version of {@link #resolvePaths()} in that it throws
     * {@link CannotOpen} exception when a file path element can't be resolved.
     * 
     * @param paths A list of paths to resolve.
     * @param cv Configuration context.
     * @param returnMissingFiles Determines if the CannotOpen exception is thrown if a file does not exist. Pass true to
     *        disable exceptions and return files that do not exist. Pass false to throw exceptions.
     * @return A list of normalized resolved file paths.
     * @throws CannotOpen error
     * @see #resolvePaths(ImmutableList, ConfigurationValue)
     */
    private ImmutableList<String> resolvePathsStrict(final ImmutableList<String> paths, final ConfigurationValue cv,
            final boolean returnMissingFiles) throws CannotOpen
    {
        assert paths != null : "Expected paths";
        assert cv != null : "Require ConfigurationValue as context.";

        final ImmutableList.Builder<String> resolvedPathsBuilder = new ImmutableList.Builder<String>();
        for (String processedPath : paths)
        {
            if (cv.getContext() != null)
            {
                boolean isAbsolute = new File(processedPath).isAbsolute();
                if (!isAbsolute)
                    processedPath = new File(cv.getContext(), processedPath).getAbsolutePath();
            }
            if (processedPath.contains("*"))
            {
                // if contains wild card, just prove the part before the wild card is valid
                int c = processedPath.lastIndexOf(File.separator, processedPath.indexOf("*"));
                if (c != -1)
                    processedPath = processedPath.substring(0, c);
            }
            if (!processedPath.contains(".swc:"))
            {
	            final File fileSpec = pathResolver.resolve(processedPath);
	            if (!returnMissingFiles && !fileSpec.exists())
	            {
	                throw new CannotOpen(FilenameNormalization.normalize(processedPath), cv.getVar(), cv.getSource(),
	                        cv.getLine());
	            }
	            resolvedPathsBuilder.add(fileSpec.getAbsolutePath());
            }
            else
                resolvedPathsBuilder.add(processedPath);
        }
        return resolvedPathsBuilder.build();
    }
}
