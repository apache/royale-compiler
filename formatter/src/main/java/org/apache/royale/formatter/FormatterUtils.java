////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.formatter;

import org.apache.royale.formatter.config.Configuration;
import org.apache.royale.formatter.config.Semicolons;

public class FormatterUtils {
	public static FormatterSettings configurationToFormatterSettings(Configuration configuration) {
		FormatterSettings settings = new FormatterSettings();
		settings.tabSize = configuration.getTabSize();
		settings.insertSpaces = configuration.getInsertSpaces();
		settings.insertFinalNewLine = configuration.getInsertFinalNewLine();
		settings.placeOpenBraceOnNewLine = configuration.getPlaceOpenBraceOnNewLine();
		settings.insertNewLineElse = configuration.getPlaceElseOnNewLine();
		settings.insertSpaceAfterSemicolonInForStatements = configuration.getInsertSpaceAfterSemicolonInForStatements();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = configuration.getInsertSpaceAfterKeywordsInControlFlowStatements();
		settings.insertSpaceAfterFunctionKeywordForAnonymousFunctions = configuration.getInsertSpaceAfterFunctionKeywordForAnonymousFunctions();
		settings.insertSpaceBeforeAndAfterBinaryOperators = configuration.getInsertSpaceBeforeAndAfterBinaryOperators();
		settings.insertSpaceAfterCommaDelimiter = configuration.getInsertSpaceAfterCommaDelimiter();
		settings.insertSpaceBetweenMetadataAttributes = configuration.getInsertSpaceBetweenMetadataAttributes();
		settings.insertSpaceAtStartOfLineComment = configuration.getInsertSpaceAtStartOfLineComment();
		settings.maxPreserveNewLines = configuration.getMaxPreserveNewLines();
		settings.semicolons = Semicolons.valueOf(configuration.getSemicolons().toUpperCase());
		settings.ignoreProblems = configuration.getIgnoreParsingProblems();
		settings.collapseEmptyBlocks = configuration.getCollapseEmptyBlocks();
		settings.mxmlAlignAttributes = configuration.getMxmlAlignAttributes();
		settings.mxmlInsertNewLineBetweenAttributes = configuration.getMxmlInsertNewLineBetweenAttributes();
		return settings;
	}
}
