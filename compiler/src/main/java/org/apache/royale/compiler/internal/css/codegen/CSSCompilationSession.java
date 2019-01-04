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

package org.apache.royale.compiler.internal.css.codegen;

import static com.google.common.collect.Collections2.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSFontFace;
import org.apache.royale.compiler.css.ICSSMediaQueryCondition;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.css.CSSDocument;
import org.apache.royale.compiler.internal.css.CSSFontFace;
import org.apache.royale.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.royale.compiler.internal.css.CSSMediaQueryCondition;
import org.apache.royale.compiler.internal.css.CSSNamespaceDefinition;
import org.apache.royale.compiler.internal.css.CSSProperty;
import org.apache.royale.compiler.internal.css.CSSRule;
import org.apache.royale.compiler.internal.css.CSSSelector;
import org.apache.royale.compiler.internal.css.semantics.CSSSemanticAnalyzer;
import org.apache.royale.compiler.internal.css.codegen.CSSEmitter;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * A CSS compilation session stores data used by one compilation cycle. <h3>This
 * class has two goals</h3>
 * <ol>
 * <li>Cache CSS semantic resolution results for CSS code generation.</li>
 * <li>Exclude CSS rules not used by any compilation units so that the target
 * size SWF is smaller.</li>
 * </ol>
 * <p>
 * In order to keep CSS model immutable during semantic analysis, the analysis
 * result such as which rules to keep and which properties to clobber is stored
 * in this auxiliary data structure.
 */
public class CSSCompilationSession
{
    /**
     * Synthesized CSS model doesn't have an AST.
     */
    private static final CommonTree NO_TREE = null;

    /**
     * Synthesized CSS model doesn't have a token stream.
     */
    private static final TokenStream NO_TOKEN_STREAM = null;

    /**
     * Common variable for empty media query list.
     */
    private static final ImmutableList<CSSMediaQueryCondition> NO_MEDIA_QUERIES = ImmutableList.of();

    /**
     * Initialize a CSS compilation session object.
     */
    public CSSCompilationSession()
    {
        resolvedSelectors = new LinkedHashMap<ICSSSelector, String>();
        inheritingStyles = new LinkedHashSet<String>();
        resolvedEmbedProperties = new HashMap<CSSFunctionCallPropertyValue, EmbedCompilationUnit>();
        activatedRules = new HashSet<ICSSRule>();
        cssDocuments = new ArrayList<ICSSDocument>();
        fontFaces = new ArrayList<CSSFontFace>();
        singleSelectorRules = new LinkedHashMap<String, SingleSelectorRule>();
        rulesWithMediaQueries = new LinkedHashSet<ICSSRule>();
        cssDisabled = false;
        keepAllTypeSelectors = false;
    }

    /**
     * A map from {@code Embed()} property values to their resolved
     * {@link EmbedCompilationUnit}'s.
     */
    public final Map<CSSFunctionCallPropertyValue, EmbedCompilationUnit> resolvedEmbedProperties;

    /**
     * A map of type selectors to their resolved class qnames.
     */
    public final Map<ICSSSelector, String> resolvedSelectors;

    /**
     * A set of inheriting style names.
     */
    public final Set<String> inheritingStyles;

    /**
     * A set of rules that will be included in the code generation.
     */
    public final Set<ICSSRule> activatedRules;

    /**
     * A set of font faces that will be included in the code generation.
     */
    public ArrayList<CSSFontFace> fontFaces;

    /**
     * A list of CSS models to be included in the code generation. The CSS
     * properties are prioritized by their order in the list. The first CSS in
     * the model has the least priority.
     */
    public final List<ICSSDocument> cssDocuments;

    /**
     * A collection of rules used by the Flex application. Each rule is
     * normalized to have only one selector in its selector group. The keys are
     * selector texts, such as "
     * {@code mx.controls.Button.myStyles#highlight:up}". These rules will be
     * added to a {@link SynthesizedCSSDocument} when
     * {@link #synthesisNormalizedCSS()} is called.
     */
    private final Map<String, SingleSelectorRule> singleSelectorRules;

    /**
     * Rules with media queries are not normalized (clobbering properties).
     * These rules will be added to a {@link SynthesizedCSSDocument} when
     * {@link #synthesisNormalizedCSS()} is called.
     */
    private final Set<ICSSRule> rulesWithMediaQueries;

    /**
     * If true, then all CSS rules should be ignored.
     */
    private boolean cssDisabled;

    /**
     * If true, all CSS rules in an activated style sheet will be included in
     * code generation.
     */
    private boolean keepAllTypeSelectors;

    /**
     * Determine if a rule should be in the output
     * 
     * @return true if rule should be in the output
     */
    protected boolean keepRule(ICSSRule newRule)
    {
        return (keepAllTypeSelectors || activatedRules.contains(newRule));
    }

    /**
     * Synthesize a normalized CSS model from the {@link ICSSRule}'s activated
     * from {@link #singleSelectorRules}. The normalized CSS
     * does not have "@namespace" rules; Its rules come from different CSS
     * documents.
     * 
     * @return A synthesized CSS model from normalized CSS model.
     */
    protected ICSSDocument synthesisNormalizedCSS(boolean isSWF)
    {
        fontFaces = new ArrayList<CSSFontFace>();
        
        for (final ICSSDocument cssDocument : cssDocuments)
        {        			
            for (final ICSSRule newRule : cssDocument.getRules())
            {
                if (keepRule(newRule))
                {
                    addRuleToCodeGeneration(newRule, isSWF);
                }
            }
            for (final ICSSFontFace fontFace : cssDocument.getFontFaces())
            {
                fontFaces.add((CSSFontFace)fontFace);
            }
        }

        // Merge all rules.
        final List<CSSRule> rules = new ArrayList<CSSRule>();
        for (final SingleSelectorRule ssRule : singleSelectorRules.values())
        {
            rules.add(ssRule.createCSSRule());
        }

        for (final ICSSRule rule : rulesWithMediaQueries)
        {
            rules.add((CSSRule)rule);
        }

        return new SynthesizedCSSDocument(rules, fontFaces);
    }

    /**
     * Include a rule in code generation.
     * 
     * @param newRule A CSS rule to be added to the synthesized CSS document.
     */
    private void addRuleToCodeGeneration(final ICSSRule newRule, boolean isSWF)
    {
    	ImmutableList<ICSSMediaQueryCondition> mq = newRule.getMediaQueryConditions();
        if (mq.isEmpty() || (isSWF && mq.size() == 1 && mq.get(0).getValue().toString().equals("-royale-swf")))
        {
            // Normalize the rule and clobber properties if the rule has no media query.
            final ImmutableList<CSSProperty> properties = ImmutableList.copyOf(
                    transform(newRule.getProperties(), INTERF_TO_IMPL));

            // Normalize selector group into rules with single selectors.
            // A, B { key:value;} => A { key:value; } and B { key:value; }
            for (final ICSSSelector newSelector : newRule.getSelectorGroup())
            {
                final String selectorName = getResolvedSelectorName(newSelector);
                assert selectorName != null : "All selectors in the rule must be resolved before the rule can be activated.";

                final SingleSelectorRule activatedRuleWithSameSelector = singleSelectorRules.get(selectorName);
                if (activatedRuleWithSameSelector == null)
                {
                    // Synthesis a rule with single selector.
                    final SingleSelectorRule newSingleSelectorRule = new SingleSelectorRule((CSSSelector)newSelector, properties);
                    singleSelectorRules.put(selectorName, newSingleSelectorRule);
                }
                else
                {
                    // Found rule with same selector. Clobber the property values.
                    for (final CSSProperty newProperty : properties)
                    {
                        final String propertyName = newProperty.getName();
                        activatedRuleWithSameSelector.propertyMap.put(propertyName, newProperty);
                    }
                }
            }
        }
        else
        {
            // Do not normalize rules with media queries.
            rulesWithMediaQueries.add(newRule);
        }
    }

    /**
     * Generate code for CSS data.
     * 
     * @param project {@link ICompilerProject} for which code is being
     * generated.
     * @param abcVisitor {@link IABCVisitor} to which needed abc constructs are
     * added.
     * @throws Exception error
     */
    public ICSSCodeGenResult emitStyleDataClass(final IRoyaleProject project, final IABCVisitor abcVisitor) throws Exception
    {
        final ICSSDocument css = synthesisNormalizedCSS(true);
        //LoggingProfiler.onSynthesisCSS(css);
        final CSSReducer reducer = new CSSReducer(project, css, abcVisitor, this, true, 0);
        final CSSEmitter emitter = new CSSEmitter(reducer);
        emitter.burm(css);
        return reducer;
    }

    /**
     * Resolve the {@code selector} against the {@link #resolvedSelectors} map
     * and return the name in the following pattern: <br>
     * <code>mx.controls.Label.typeName#id:state</code>
     */
    private String getResolvedSelectorName(final ICSSSelector selector)
    {
        final String selectorQname;
        if (CSSSemanticAnalyzer.isWildcardSelector(selector))
        {
            selectorQname = "";
        }
        else
        {
            final String qname = resolvedSelectors.get(selector);
            // commented out this assert.  Seems like it too strict for when someone has multiple type selectors on a single ruleset
            //assert qname != null : "Expected resolved class definition for an activated selector. Possible bug in CSS dependency loop. Selector=" + selector;
            if (qname == null)
                selectorQname = selector.getElementName();
            else
                selectorQname = qname;
        }
        final String resolvedSelectorName = selectorQname.concat(
                selector.getCSSSyntaxNoNamespaces());
        return resolvedSelectorName;
    }

    /**
     * Convert a {@link ICSSProperty} object to a {@link CSSProperty} object.
     */
    private static Function<ICSSProperty, CSSProperty> INTERF_TO_IMPL = new Function<ICSSProperty, CSSProperty>()
    {
        @Override
        public CSSProperty apply(ICSSProperty property)
        {
            return (CSSProperty)property;
        }
    };

    /**
     * A {@link CSSRule} node with only one selector. This implementation has a
     * mutable property collection, because the property values can be clobbered
     * by another CSS rule with the same selector.
     */
    private static class SingleSelectorRule
    {
        private SingleSelectorRule(CSSSelector selector, List<CSSProperty> properties)
        {
            propertyMap = new HashMap<String, CSSProperty>();
            for (final CSSProperty property : properties)
            {
                // This will automatically clobber duplicated properties inside a rule.
                propertyMap.put(property.getName(), property);
            }
            this.selector = selector;
        }

        private final CSSSelector selector;
        private final Map<String, CSSProperty> propertyMap;

        /**
         * @return Synthesize a {@link ICSSRule} object from this
         * single-selector rule.
         */
        private CSSRule createCSSRule()
        {
            final CSSRule cssRule = new CSSRule(
                    NO_MEDIA_QUERIES,
                    ImmutableList.of(selector),
                    ImmutableList.copyOf(propertyMap.values()),
                    NO_TREE,
                    NO_TOKEN_STREAM);
            return cssRule;
        }
    }

    /**
     * Synthesized CSS DOM from normalized CSS model. This tree will be used for
     * code generation.
     */
    private static class SynthesizedCSSDocument extends CSSDocument
    {
        /**
         * Synthesized CSS DOM doesn't need namespace declarations, because the
         * type selectors have been resolved to {@link IClassDefinition} definitions
         * already.
         */
        private static final List<CSSNamespaceDefinition> NO_NAMESPACES = ImmutableList.of();

        private SynthesizedCSSDocument(final List<CSSRule> rules, List<CSSFontFace> fontFaces)
        {
            super(rules, NO_NAMESPACES, fontFaces, NO_TREE, NO_TOKEN_STREAM);
        }

    }

    /**
     * Disables CSS code generation and dependency analysis for this
     * {@link CSSCompilationSession}. This method is called from
     * {@link org.apache.royale.compiler.internal.targets.SWFTarget} when compiling
     * a SWF that will not have a system manager.
     */
    public void disable()
    {
        cssDisabled = true;
    }

    /**
     * @return true if CSS code generation and dependency analysis is disabled
     * for this {@link CSSCompilationSession}, false otherwise.
     */
    public boolean isDisabled()
    {
        return cssDisabled;
    }

    /**
     * @return True if all type selectors are kept for linking.
     */
    public boolean isKeepAllTypeSelectors()
    {
        return keepAllTypeSelectors;
    }

    /**
     * Set whether to keep all type selectors for linking.
     * 
     * @param keepAllTypeSelectors value
     */
    public void setKeepAllTypeSelectors(boolean keepAllTypeSelectors)
    {
        this.keepAllTypeSelectors = keepAllTypeSelectors;
    }

}
