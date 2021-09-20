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

package org.apache.royale.formatter.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.problems.DeprecatedConfigurationOptionProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.RemovedConfigurationOptionProblem;

public class Configuration {

    private static Map<String, String> aliases = null;

    public static Map<String, String> getAliases()
    {
        if (aliases == null)
        {
            aliases = new HashMap<String, String>();

            aliases.put("w", "write-files");
            aliases.put("l", "list-files");
        }
        return aliases;
    }
	
    /**
     * Collection of fatal and non-fatal configuration problems.
     */
    private Collection<ICompilerProblem> configurationProblems = new ArrayList<ICompilerProblem>();

    /**
     * Get the configuration problems. This should be called after the configuration has been processed.
     * 
     * @return a collection of fatal and non-fatal configuration problems.
     */
    public Collection<ICompilerProblem> getConfigurationProblems()
    {
        return configurationProblems;
    }

    /**
     * Validate configuration options values.
     * 
     * @param configurationBuffer Configuration buffer.
     * @throws ConfigurationException Error.
     */
    public void validate(ConfigurationBuffer configurationBuffer) throws ConfigurationException
    {
        // process the merged configuration buffer. right, can't just process the args.
        processDeprecatedAndRemovedOptions(configurationBuffer);
    }

    private void processDeprecatedAndRemovedOptions(ConfigurationBuffer configurationBuffer)
    {
        for (final String var : configurationBuffer.getVars())
        {
            ConfigurationInfo info = configurationBuffer.getInfo(var);
            List<ConfigurationValue> values = configurationBuffer.getVar(var);
            if (values != null)
            {
                for (final ConfigurationValue cv : values)
                {
                    if (info.isRemoved())
                    {
                        addRemovedConfigurationOptionProblem(cv);
                    }
                    else if (info.isDeprecated() && configurationBuffer.getVar(var) != null)
                    {
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
     * Add a RemovedConfigurationOptionProblem to the list of configuration problems.
     * 
     * @param cv
     */
    private void addRemovedConfigurationOptionProblem(ConfigurationValue cv)
    {
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
    public void setHelp(ConfigurationValue cv, String[] keywords)
    {

    }

    //
    // 'version' option from CommandLineConfiguration
    //

    /**
     * Dummy option. Just a trigger for version info.
     */
    @Config
    public void setVersion(ConfigurationValue cv, boolean value)
    {
    }

    //
    // 'files' option
    //

    private List<String> files = new ArrayList<String>();

    /**
     * @return A list of filespecs. It's the default variable for command line.
     */
    public List<String> getFiles()
    {
        return files;
    }

    @Config(allowMultiple = true, hidden = true)
    @Mapping("files")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setFiles(ConfigurationValue cv, List<String> args) throws ConfigurationException
    {
        this.files.addAll(args);
    }

    //
    // 'write-files' option
    //

    private boolean writeFiles = false;

    public boolean getWriteFiles()
    {
        return writeFiles;
    }

    @Config
    @Mapping("write-files")
    public void setWriteFiles(ConfigurationValue cv, boolean b)
    {
        this.writeFiles = b;
    }

    //
    // 'list-files' option
    //

    private boolean listFiles = false;

    public boolean getListFiles()
    {
        return listFiles;
    }

    @Config
    @Mapping("list-files")
    public void setListFiles(ConfigurationValue cv, boolean b)
    {
        this.listFiles = b;
    }

    //
    // 'insert-spaces' option
    //

    private boolean insertSpaces = false;

    public boolean getInsertSpaces()
    {
        return insertSpaces;
    }

    @Config
    @Mapping("insert-spaces")
    public void setInsertSpaces(ConfigurationValue cv, boolean b)
    {
        this.insertSpaces = b;
    }

    //
    // 'insert-final-new-line' option
    //

    private boolean insertFinalNewLine = false;

    public boolean getInsertFinalNewLine()
    {
        return insertFinalNewLine;
    }

    @Config
    @Mapping("insert-final-new-line")
    public void setInsertFinalNewLine(ConfigurationValue cv, boolean b)
    {
        this.insertFinalNewLine = b;
    }

    //
    // 'open-brace-new-line' option
    //

    private boolean placeOpenBraceOnNewLine = true;

    public boolean getPlaceOpenBraceOnNewLine()
    {
        return placeOpenBraceOnNewLine;
    }

    @Config
    @Mapping("open-brace-new-line")
    public void setPlaceOpenBraceOnNewLine(ConfigurationValue cv, boolean b)
    {
        this.placeOpenBraceOnNewLine = b;
    }

    //
    // 'insert-space-for-loop-semicolon' option
    //

    private boolean insertSpaceAfterSemicolonInForStatements = true;

    public boolean getInsertSpaceAfterSemicolonInForStatements()
    {
        return insertSpaceAfterSemicolonInForStatements;
    }

    @Config
    @Mapping("insert-space-for-loop-semicolon")
    public void setInsertSpaceAfterSemicolonInForStatements(ConfigurationValue cv, boolean b)
    {
        this.insertSpaceAfterSemicolonInForStatements = b;
    }

    //
    // 'insert-space-control-flow-keywords' option
    //

    private boolean insertSpaceAfterKeywordsInControlFlowStatements = true;

    public boolean getInsertSpaceAfterKeywordsInControlFlowStatements()
    {
        return insertSpaceAfterKeywordsInControlFlowStatements;
    }

    @Config
    @Mapping("insert-space-control-flow-keywords")
    public void setInsertSpaceAfterKeywordsInControlFlowStatements(ConfigurationValue cv, boolean b)
    {
        this.insertSpaceAfterKeywordsInControlFlowStatements = b;
    }

    //
    // 'insert-space-anonymous-function-keyword' option
    //

    private boolean insertSpaceAfterFunctionKeywordForAnonymousFunctions = false;

    public boolean getInsertSpaceAfterFunctionKeywordForAnonymousFunctions()
    {
        return insertSpaceAfterFunctionKeywordForAnonymousFunctions;
    }

    @Config
    @Mapping("insert-space-anonymous-function-keyword")
    public void setInsertSpaceAfterFunctionKeywordForAnonymousFunctions(ConfigurationValue cv, boolean b)
    {
        this.insertSpaceAfterFunctionKeywordForAnonymousFunctions = b;
    }

    //
    // 'insert-space-binary-operators' option
    //

    private boolean insertSpaceBeforeAndAfterBinaryOperators = true;

    public boolean getInsertSpaceBeforeAndAfterBinaryOperators()
    {
        return insertSpaceBeforeAndAfterBinaryOperators;
    }

    @Config
    @Mapping("insert-space-binary-operators")
    public void setInsertSpaceBeforeAndAfterBinaryOperators(ConfigurationValue cv, boolean b)
    {
        this.insertSpaceBeforeAndAfterBinaryOperators = b;
    }

    //
    // 'insert-space-comma-delimiter' option
    //

    private boolean insertSpaceAfterCommaDelimiter = true;

    public boolean getInsertSpaceAfterCommaDelimiter()
    {
        return insertSpaceAfterCommaDelimiter;
    }

    @Config
    @Mapping("insert-space-comma-delimiter")
    public void setInsertSpaceAfterCommaDelimiter(ConfigurationValue cv, boolean b)
    {
        this.insertSpaceAfterCommaDelimiter = b;
    }

    //
    // 'collapse-empty-blocks' option
    //

    private boolean collapseEmptyBlocks = false;

    public boolean getCollapseEmptyBlocks()
    {
        return collapseEmptyBlocks;
    }

    @Config
    @Mapping("collapse-empty-blocks")
    public void setCollapseEmptyBlocks(ConfigurationValue cv, boolean b)
    {
        this.collapseEmptyBlocks = b;
    }

    //
    // 'tab-size' option
    //

    private int tabSize = 4;

    public int getTabSize()
    {
        return tabSize;
    }

    @Config
    @Mapping("tab-size")
    public void setTabSize(ConfigurationValue cv, int b)
    {
        this.tabSize = b;
    }

    //
    // 'max-preserve-new-lines' option
    //

    private int maxPreserveNewLines = 2;

    public int getMaxPreserveNewLines()
    {
        return maxPreserveNewLines;
    }

    @Config
    @Mapping("max-preserve-new-lines")
    public void setMaxPreserveNewLines(ConfigurationValue cv, int b)
    {
        this.maxPreserveNewLines = b;
    }

    //
    // 'semicolons' option
    //

    private Semicolons semicolons = Semicolons.INSERT;

    public String getSemicolons()
    {
        return semicolons.value;
    }

    @Config
    @Mapping("semicolons")
    public void setSemicolons(ConfigurationValue cv, String b)
    {
        this.semicolons = Semicolons.valueOf(b.toUpperCase());
    }

    //
    // 'ignore-parsing-problems' option
    //

    private boolean ignoreParsingProblems = false;

    public boolean getIgnoreParsingProblems()
    {
        return ignoreParsingProblems;
    }

    @Config(advanced = true)
    @Mapping("ignore-parsing-problems")
    public void setIgnoreParsingProblems(ConfigurationValue cv, boolean b)
    {
        this.ignoreParsingProblems = b;
    }
}
