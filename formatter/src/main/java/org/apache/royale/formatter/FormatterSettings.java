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

import org.apache.royale.formatter.config.Semicolons;

public class FormatterSettings {
	public int tabSize = 4;
	public boolean insertSpaces = false;
	public boolean insertFinalNewLine = false;
	public boolean insertNewLineElse = true;
	public boolean placeOpenBraceOnNewLine = true;
	public boolean insertSpaceAfterSemicolonInForStatements = true;
	public boolean insertSpaceAfterKeywordsInControlFlowStatements = true;
	public boolean insertSpaceAfterFunctionKeywordForAnonymousFunctions = false;
	public boolean insertSpaceBeforeAndAfterBinaryOperators = true;
	public boolean insertSpaceAfterCommaDelimiter = true;
	public boolean insertSpaceBetweenMetadataAttributes = true;
	public boolean insertSpaceAtStartOfLineComment = true;
	public int maxPreserveNewLines = 2;
	public Semicolons semicolons = Semicolons.INSERT;
	public boolean ignoreProblems = false;
	public boolean collapseEmptyBlocks = false;
	public boolean mxmlAlignAttributes = false;
	public boolean mxmlInsertNewLineBetweenAttributes = false;
}
