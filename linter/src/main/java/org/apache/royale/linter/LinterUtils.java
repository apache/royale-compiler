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

package org.apache.royale.linter;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.linter.config.Configuration;
import org.apache.royale.linter.config.LineCommentPosition;
import org.apache.royale.linter.rules.ClassNameRule;
import org.apache.royale.linter.rules.ConstantNameRule;
import org.apache.royale.linter.rules.EmptyCommentRule;
import org.apache.royale.linter.rules.EmptyFunctionBodyRule;
import org.apache.royale.linter.rules.EmptyNestedBlockRule;
import org.apache.royale.linter.rules.EmptyStatementRule;
import org.apache.royale.linter.rules.FieldNameRule;
import org.apache.royale.linter.rules.FunctionNameRule;
import org.apache.royale.linter.rules.InterfaceNameRule;
import org.apache.royale.linter.rules.LineCommentPositionRule;
import org.apache.royale.linter.rules.LocalVarAndParameterNameRule;
import org.apache.royale.linter.rules.LocalVarShadowsFieldRule;
import org.apache.royale.linter.rules.MXMLEmptyAttributeRule;
import org.apache.royale.linter.rules.MXMLIDRule;
import org.apache.royale.linter.rules.MaxBlockDepthRule;
import org.apache.royale.linter.rules.MaxParametersRule;
import org.apache.royale.linter.rules.MissingASDocRule;
import org.apache.royale.linter.rules.MissingConstructorSuperRule;
import org.apache.royale.linter.rules.MissingNamespaceRule;
import org.apache.royale.linter.rules.MissingSemicolonRule;
import org.apache.royale.linter.rules.MissingTypeRule;
import org.apache.royale.linter.rules.NoAnyTypeRule;
import org.apache.royale.linter.rules.NoBooleanEqualityRule;
import org.apache.royale.linter.rules.NoConstructorDispatchEventRule;
import org.apache.royale.linter.rules.NoConstructorReturnTypeRule;
import org.apache.royale.linter.rules.NoDuplicateObjectKeysRule;
import org.apache.royale.linter.rules.NoDynamicClassRule;
import org.apache.royale.linter.rules.NoIfBooleanLiteralRule;
import org.apache.royale.linter.rules.NoLeadingZeroesRule;
import org.apache.royale.linter.rules.NoSparseArrayRule;
import org.apache.royale.linter.rules.NoStringEventNameRule;
import org.apache.royale.linter.rules.NoThisInClosureRule;
import org.apache.royale.linter.rules.NoTraceRule;
import org.apache.royale.linter.rules.NoVoidOperatorRule;
import org.apache.royale.linter.rules.NoWildcardImportRule;
import org.apache.royale.linter.rules.NoWithRule;
import org.apache.royale.linter.rules.OverrideContainsOnlySuperCallRule;
import org.apache.royale.linter.rules.PackageNameRule;
import org.apache.royale.linter.rules.StaticConstantsRule;
import org.apache.royale.linter.rules.StrictEqualityRule;
import org.apache.royale.linter.rules.SwitchWithoutDefaultRule;
import org.apache.royale.linter.rules.UnsafeNegationRule;
import org.apache.royale.linter.rules.ValidTypeofRule;
import org.apache.royale.linter.rules.VariablesOnTopRule;

public class LinterUtils {
	public static LinterSettings configurationToLinterSettings(Configuration configuration) {
		LinterSettings settings = new LinterSettings();
		settings.ignoreProblems = configuration.getIgnoreParsingProblems();

		List<LinterRule> rules = LinterUtils.configurationToRules(configuration);
		settings.rules = rules;

		return settings;
	}

	public static List<LinterRule> configurationToRules(Configuration configuration) {
		List<LinterRule> rules = new ArrayList<LinterRule>();
		if (configuration.getClassName()) {
			rules.add(new ClassNameRule());
		}
		if (configuration.getConstantName()) {
			rules.add(new ConstantNameRule());
		}
		if (configuration.getEmptyFunctionBody()) {
			rules.add(new EmptyFunctionBodyRule());
		}
		if (configuration.getEmptyNestedBlock()) {
			rules.add(new EmptyNestedBlockRule());
		}
		if (configuration.getFunctionName()) {
			rules.add(new FunctionNameRule());
		}
		if (configuration.getFieldName()) {
			rules.add(new FieldNameRule());
		}
		if (configuration.getOverrideSuper()) {
			rules.add(new OverrideContainsOnlySuperCallRule());
		}
		if (configuration.getEmptyComment()) {
			rules.add(new EmptyCommentRule());
		}
		if (configuration.getEmptyStatement()) {
			rules.add(new EmptyStatementRule());
		}
		if (configuration.getInterfaceName()) {
			rules.add(new InterfaceNameRule());
		}
		if (configuration.getLineCommentPosition() != null) {
			LineCommentPositionRule rule = new LineCommentPositionRule();
			rule.position = LineCommentPosition.valueOf(configuration.getLineCommentPosition().toUpperCase());
			rules.add(rule);
		}
		if (configuration.getLocalVarParamName()) {
			rules.add(new LocalVarAndParameterNameRule());
		}
		if (configuration.getLocalVarShadowsField()) {
			rules.add(new LocalVarShadowsFieldRule());
		}
		if (configuration.getMaxParams() > 0) {
			MaxParametersRule rule = new MaxParametersRule();
			rule.maximum = configuration.getMaxParams();
			rules.add(rule);
		}
		if (configuration.getMaxBlockDepth() > 0) {
			MaxBlockDepthRule rule = new MaxBlockDepthRule();
			rule.maximum = configuration.getMaxBlockDepth();
			rules.add(rule);
		}
		if (configuration.getMissingAsdoc()) {
			rules.add(new MissingASDocRule());
		}
		if (configuration.getMissingConstructorSuper()) {
			rules.add(new MissingConstructorSuperRule());
		}
		if (configuration.getMissingNamespace()) {
			rules.add(new MissingNamespaceRule());
		}
		if (configuration.getMissingSemicolon()) {
			rules.add(new MissingSemicolonRule());
		}
		if (configuration.getMissingType()) {
			rules.add(new MissingTypeRule());
		}
		if (configuration.getMxmlId()) {
			rules.add(new MXMLIDRule());
		}
		if (configuration.getMxmlEmptyAttr()) {
			rules.add(new MXMLEmptyAttributeRule());
		}
		if (configuration.getNoAnyType()) {
			rules.add(new NoAnyTypeRule());
		}
		if (configuration.getNoBooleanEquality()) {
			rules.add(new NoBooleanEqualityRule());
		}
		if (configuration.getNoConstructorDispatch()) {
			rules.add(new NoConstructorDispatchEventRule());
		}
		if (configuration.getNoConstructorReturnType()) {
			rules.add(new NoConstructorReturnTypeRule());
		}
		if (configuration.getNoDuplicateKeys()) {
			rules.add(new NoDuplicateObjectKeysRule());
		}
		if (configuration.getNoDynamicClass()) {
			rules.add(new NoDynamicClassRule());
		}
		if (configuration.getNoIfBoolean()) {
			rules.add(new NoIfBooleanLiteralRule());
		}
		if (configuration.getNoLeadingZero()) {
			rules.add(new NoLeadingZeroesRule());
		}
		if (configuration.getNoSparseArray()) {
			rules.add(new NoSparseArrayRule());
		}
		if (configuration.getNoStringEvent()) {
			rules.add(new NoStringEventNameRule());
		}
		if (configuration.getNoThisClosure()) {
			rules.add(new NoThisInClosureRule());
		}
		if (configuration.getNoTrace()) {
			rules.add(new NoTraceRule());
		}
		if (configuration.getNoVoidOperator()) {
			rules.add(new NoVoidOperatorRule());
		}
		if (configuration.getNoWildcardImport()) {
			rules.add(new NoWildcardImportRule());
		}
		if (configuration.getNoWith()) {
			rules.add(new NoWithRule());
		}
		if (configuration.getPackageName()) {
			rules.add(new PackageNameRule());
		}
		if (configuration.getStaticConstants()) {
			rules.add(new StaticConstantsRule());
		}
		if (configuration.getStrictEquality()) {
			rules.add(new StrictEqualityRule());
		}
		if (configuration.getSwitchDefault()) {
			rules.add(new SwitchWithoutDefaultRule());
		}
		if (configuration.getUnsafeNegation()) {
			rules.add(new UnsafeNegationRule());
		}
		if (configuration.getValidTypeof()) {
			rules.add(new ValidTypeofRule());
		}
		if (configuration.getVarsOnTop()) {
			rules.add(new VariablesOnTopRule());
		}
		return rules;
	}
}
