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

package org.apache.royale.compiler.internal.css.semantics;

import static org.apache.royale.compiler.internal.css.CSSStringPropertyValue.stripQuotes;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.css.ICSSSelectorCondition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.royale.compiler.internal.css.CSSManager;
import org.apache.royale.compiler.internal.css.CSSSelector;
import org.apache.royale.compiler.internal.css.CSSStringPropertyValue;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.targets.Target;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnitFactory;
import org.apache.royale.compiler.mxml.IXMLNameResolver;
import org.apache.royale.compiler.problems.CSSEmbedAssetProblem;
import org.apache.royale.compiler.problems.CSSExcludedStylePropertyProblem;
import org.apache.royale.compiler.problems.CSSUndefinedNamespacePrefixProblem;
import org.apache.royale.compiler.problems.CSSUndefinedTypeProblem;
import org.apache.royale.compiler.problems.CSSUnknownDefaultNamespaceProblem;
import org.apache.royale.compiler.problems.CSSUnresolvedClassReferenceProblem;
import org.apache.royale.compiler.problems.CSSUnusedTypeSelectorProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Semantic analyzer for CSS. This is a static class. It's used by
 * {@link CSSManager}.
 */
public class CSSSemanticAnalyzer
{

    /** Global selector. */
    private static final String GLOBAL_SELECTOR = "global";
    /** Universal selector. */
    private static final String UNIVERSAL_SELECTOR = "*";

    /**
     * Try to resolve all the dependencies introduced by
     * {@code ClassReference()} and {@code Embed()} property values in a CSS
     * rule.
     * 
     * @param resolvedEmbedProperties A map from {@code Embed()} property values
     * to their resolved {@link EmbedCompilationUnit}'s.
     * @param cssRule CSS rule.
     * @param project Current project.
     * @param classReferences Definitions of the {@code ClassReference("...")}
     * properties will be stored in this collection after the function returns.
     * @param embedCompilationUnits {@link EmbedCompilationUnit}'s used in the
     * rules will be stored in this collection after the function returns.
     * @param problems Compiler problems. This method reports
     * {@link CSSUnresolvedClassReferenceProblem} issues.
     */
    public static void resolveDependencies(
            final Map<CSSFunctionCallPropertyValue, EmbedCompilationUnit> resolvedEmbedProperties,
            final ICSSRule cssRule,
            final ICompilerProject project,
            final Set<IClassDefinition> classReferences,
            final Set<EmbedCompilationUnit> embedCompilationUnits,
            final Collection<ICompilerProblem> problems)
    {
        assert cssRule != null : "CSS rule can't be null";
        assert project != null : "Project can't be null";
        assert problems != null : "Problems can't be null";
        assert classReferences != null : "Expected an output collection for ClassReferences.";
        assert embedCompilationUnits != null : "Expected an output collection for Embed.";

        for (final ICSSProperty property : cssRule.getProperties())
        {
            final ICSSPropertyValue propertyValue = property.getValue();
            if (propertyValue instanceof CSSFunctionCallPropertyValue)
            {
                final CSSFunctionCallPropertyValue functionCall = (CSSFunctionCallPropertyValue)propertyValue;
                if (CSSFunctionCallPropertyValue.CLASS_REFERENCE.equals(functionCall.name))
                {
                    // Found a ClassReference() property.
                    if ("null".equals(functionCall.rawArguments))
                    {
                        // Do nothing. ClassReference(null) resets the skin class.
                    }
                    else
                    {
                        final String qName;
                        if (CSSStringPropertyValue.isQuoted(functionCall.rawArguments))
                            qName = stripQuotes(functionCall.rawArguments);
                        else
                            qName = functionCall.rawArguments;

                        final IDefinition definition = project.resolveQNameToDefinition(qName);
                        // The definition is expected to be a class definition.
                        if (definition != null && definition instanceof IClassDefinition)
                        {
                            classReferences.add((IClassDefinition)definition);
                        }
                        else
                        {
                            final CSSUnresolvedClassReferenceProblem problem = new CSSUnresolvedClassReferenceProblem(functionCall);
                            problems.add(problem);
                        }
                    }
                }
                else if (CSSFunctionCallPropertyValue.EMBED.equals(functionCall.name))
                {
                    final String embedMetadata = String.format("[%s(%s)]", functionCall.name, functionCall.rawArguments);
                    // TODO Calling normalize here prevents an assert later
                    // in the getFileSpecification() of Workspace. The problem is that an embed
                    // in default.css inside a SWC has a source path which doesn't look normalized.
                    final String sourcePath = FilenameNormalization.normalize(functionCall.getSourcePath());
                    final MetaTagsNode metadata = ASParser.parseMetadata(project.getWorkspace(), embedMetadata,
                                                                         sourcePath, functionCall.getStart(),
                                                                         functionCall.getLine(), functionCall.getColumn(),
                                                                         problems);
                    final IMetaTagNode embedTag = metadata.getTagByName("Embed");
                    if (embedTag == null)
                    {
                        problems.add(new CSSEmbedAssetProblem(functionCall));
                    }
                    else
                    {
                        try
                        {
                            final EmbedCompilationUnit embedCompilationUnit =
                                    EmbedCompilationUnitFactory.getCompilationUnit(
                                            (ASProject)project,
                                            embedTag.getSourcePath(),
                                            functionCall,
                                            embedTag.getAllAttributes(),
                                            problems);
                            if (embedCompilationUnit == null)
                            {
                                problems.add(new CSSEmbedAssetProblem(functionCall));
                            }
                            else
                            {
                                resolvedEmbedProperties.put(functionCall, embedCompilationUnit);
                                embedCompilationUnits.add(embedCompilationUnit);
                            }
                        }
                        catch (InterruptedException e)
                        {
                            // Incremental build interrupts the current build. We can
                            // throw away the results.
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolve type selectors to class definitions within a file scope context
     * <p>
     * If a namespace short name is mapped to a undefined namespace URI, just
     * ignore.
     * 
     * @param xmlNameResolver XML name resolver
     * @param css CSS DOM.
     * @param problems Collect problems.
     * @param isCompatibilityVersion3 If true, do not create
     * {@link CSSUnknownDefaultNamespaceProblem}'s.
     * @return A map of CSS selectors to QNames of their resolved types.
     */
    public static ImmutableMap<ICSSSelector, String> resolveSelectors(
            final IXMLNameResolver xmlNameResolver,
            final ICSSDocument css,
            final Collection<ICompilerProblem> problems,
            final IRoyaleProject project,
            final boolean isCompatibilityVersion3)
    {
        assert xmlNameResolver != null : "Expected xmlNameResolver";
        assert css != null : "Expected CSS";

        final ImmutableSet<ICSSSelector> allSelectors = getAllSelectors(css, project);

        if (isCompatibilityVersion3)
            return resolveSelectorsAsFlex3Style(allSelectors);

        final ICSSNamespaceDefinition defaultNamespace = css.getDefaultNamespaceDefinition();
        final Builder<ICSSSelector, String> builder = new Builder<ICSSSelector, String>();
        for (final ICSSSelector selector : allSelectors)
        {
            // Expand selector to QName and conditions.
            if (isWildcardSelector(selector))
                continue;

            final String prefix = selector.getNamespacePrefix();
            final ICSSNamespaceDefinition namespace;

            if (prefix == null)
            {
                // Check if the selector is a type selector without explicit namespace.
                if (defaultNamespace == null)
                {
                    problems.add(new CSSUnknownDefaultNamespaceProblem((CSSSelector)selector));
                    continue;
                }
                else
                {
                    namespace = defaultNamespace;
                }
            }
            else
            {
                // Resolve namespace.
                namespace = css.getNamespaceDefinition(prefix);
            }

            if (namespace == null)
            {
                problems.add(new CSSUndefinedNamespacePrefixProblem((CSSSelector)selector));
                continue;
            }

            assert (selector.getElementName() != null) : "Null element name should be skipped as a wildcard selector.";
            final XMLName xmlName = new XMLName(namespace.getURI(), selector.getElementName());

            // Resolve type name.
            final String qname = xmlNameResolver.resolveXMLNameToQualifiedName(xmlName, MXMLDialect.MXML_2009);
            if (qname == null)
            {
            	if (defaultNamespace != null && defaultNamespace.getURI().equals("http://www.w3.org/1999/xhtml"))
            		builder.put(selector, selector.getElementName());
            	else
            		problems.add(new CSSUndefinedTypeProblem((CSSSelector)selector));
            }
            else
            {
                builder.put(selector, qname);
            }
        }
        return builder.build();
    }

    /**
     * Resolve selectors as Flex 3 CSS. In Flex 3 CSS, selectors don't have
     * namespaces. As a result, they don't have to be resolved to a type
     * definition. For example, selector "Button" will be emitted as a selector
     * literal "Button". Whereas, in Flex 4 mode, "Button" would be resolved to
     * a default namespace and then emitted as a Spark Button QName or MX Button
     * QName.
     * 
     * @param selectors All the selectors to resolve.
     * @return A map from selectors to its resolved runtime style selector name.
     */
    private static ImmutableMap<ICSSSelector, String> resolveSelectorsAsFlex3Style(
            final Iterable<ICSSSelector> selectors)
    {
        final ImmutableMap.Builder<ICSSSelector, String> builder = new ImmutableMap.Builder<ICSSSelector, String>();
        for (final ICSSSelector selector : selectors)
        {
            builder.put(selector, selector.getCSSSyntax());
        }
        return builder.build();
    }

    /**
     * Collect all the selectors in the CSS document including the subjects and
     * the combination selectors.
     * 
     * @param document CSS document
     * @return All the selectors in the CSS.
     */
    public static ImmutableSet<ICSSSelector> getAllSelectors(final ICSSDocument document, final IRoyaleProject project)
    {
        assert document != null : "Expected CSS document";

        final ImmutableSet.Builder<ICSSSelector> builder = new ImmutableSet.Builder<ICSSSelector>();
        for (final ICSSRule rule : document.getRules())
        {
        	if (!project.isPlatformRule(rule))
        		continue;
            for (final ICSSSelector subject : rule.getSelectorGroup())
            {
                ICSSSelector selector = subject;
                while (selector != null)
                {
                    builder.add(selector);
                    if (selector.getCombinator() != null)
                        selector = selector.getCombinator().getSelector();
                    else
                        selector = null;
                }
            }
        }
        return builder.build();
    }

    /**
     * A {@link Predicate} that filters {@link ICSSSelector}'s matched by a
     * given set of class definitions. This is created for
     * {@link Collections2#filter(Collection, Predicate)}.
     */
    private static class MatchedCSSRulePredicate implements Predicate<ICSSRule>
    {
        /**
         * The project
         */
        private final IRoyaleProject project;

        /**
         * QNames of the definitions to be matched by the CSS rules.
         */
        private final ImmutableSet<String> qnames;

        /**
         * A map of selectors resolved to class definitions.
         */
        private final ImmutableMap<ICSSSelector, String> resolvedSelectors;

        /**
         * Create a predicate for filtering matched CSS rules.
         * 
         * @param qnames A set of class definitions to be matched by the
         * CSS rules.
         * @param resolvedSelectors A map of selectors resolved to class
         * definitions.
         */
        public MatchedCSSRulePredicate(final ImmutableSet<String> qnames, IRoyaleProject project,
                                       final ImmutableMap<ICSSSelector, String> resolvedSelectors)
        {
            assert qnames != null : "Expected a set of definition for the CSS rules to match.";
            assert resolvedSelectors != null : "Expected a map of selectors resolved to class definitions.";
            this.qnames = qnames;
            this.resolvedSelectors = resolvedSelectors;
            this.project = project;
        }

        /**
         * Return true if any of the <b>subject</b> selectors in the
         * {@code rule}'s selector group match any definitions in
         * {@link #qnames}. Combinator selectors are ignored.
         */
        @Override
        public boolean apply(final ICSSRule rule)
        {
        	if (!project.isPlatformRule(rule))
        		return false;
        	
            for (final ICSSSelector selector : rule.getSelectorGroup())
            {
                if (isWildcardSelector(selector))
                {
                	String selName = getOptionalSelectorName(selector);
                	if (selName == null) return true;
                    return qnames.contains(selName);
                }
                final String qname = resolvedSelectors.get(selector);
                if (qnames.contains(qname))
                    return true;
            }
            return false;
        }
        
        @Override
        public boolean test(ICSSRule input)
        {
            return apply(input);
        }

    }

    /**
     * This predicate is created for {@code -compatibility-version=3} mode. In
     * Flex 3, the selectors don't have namespace specifiers. Under the
     * "compatible" mode, {@code Button} means {@code *|Button} in CSS3 syntax.
     * <p>
     * All selectors with Flex 4 advanced syntax will be dropped.
     * <p>
     * This class only compares the selector element names and the definition
     * short names.
     */
    private static class Flex3CSSRulePredicate implements Predicate<ICSSRule>
    {
        private final ImmutableSet<String> definitionSimpleNames;

        private Flex3CSSRulePredicate(final ImmutableSet<String> definitionSimpleNames)
        {
            this.definitionSimpleNames = definitionSimpleNames;
        }

        @Override
        public boolean apply(ICSSRule rule)
        {
            for (final ICSSSelector selector : rule.getSelectorGroup())
            {
                // drop advanced selectors for flex 3
                if (selector.isAdvanced())
                    return false;

                // drop unused css rules
                final String elementName = selector.getElementName();
                if (GLOBAL_SELECTOR.equals(elementName))
                    continue;
                if (elementName == null)
                    continue;
                if (!definitionSimpleNames.contains(elementName))
                    return false;
            }
            return true;
        }
        
        @Override
        public boolean test(ICSSRule input)
        {
            return apply(input);
        }
    }

    /**
     * Convert a dot-separated QName string to the simple name. For example:
     * <ul>
     * <li>{@code f("a.b.foo") = "foo";}</li>
     * <li>{@code f("bar") = "bar";}</li>
     * </ul>
     */
    private static final Function<String, String> QNAME_TO_SIMPLE_NAME = new Function<String, String>()
    {
        @Override
        public String apply(String qname)
        {
            return Iterables.getLast(Splitter.on(".").omitEmptyStrings().split(qname));
        }
    };

    /**
     * Get a set of {@link ICSSRule}'s that match any of the class definitions
     * passed in.
     * 
     * @param session CSS compilation session data.
     * @param royaleProject Flex project.
     * @param cssDocument CSS document.
     * @param qnames A set of QNames of the definitions to be matched the CSS
     * rules.
     * @param problems Problems collection.
     * @return A set of CSS rules matched by one of the given class definitions.
     */
    public static ImmutableSet<ICSSRule> getMatchedRules(
            final CSSCompilationSession session,
            final IRoyaleProject royaleProject,
            final ICSSDocument cssDocument,
            final ImmutableSet<String> qnames,
            final Collection<ICompilerProblem> problems)
    {
        final boolean isFlex3CSS = royaleProject.getCSSManager().isFlex3CSS();
        final ImmutableMap<ICSSSelector, String> resolvedSelectors =
                resolveSelectors(royaleProject, cssDocument, problems, royaleProject, isFlex3CSS);
        final Predicate<ICSSRule> predicate;
        if (isFlex3CSS)
        {
            final ImmutableSet<String> simpleNames =
                    ImmutableSet.copyOf(transform(qnames, QNAME_TO_SIMPLE_NAME));
            predicate = new Flex3CSSRulePredicate(simpleNames);
        }
        else
        {
            predicate = new MatchedCSSRulePredicate(qnames, royaleProject, resolvedSelectors);
        }

        // Cache the result of selector resolution on the session. 
        // The CSS code generation will use this map later.
        session.resolvedSelectors.putAll(resolvedSelectors);

        // Find rules with selectors that match types in a given definition set.
        return ImmutableSet.copyOf(filter(cssDocument.getRules(), predicate));
    }

    /**
     * Check if the selector is a wildcard selector. For example:
     * <ul>
     * <li>global</li>
     * <li>*</li>
     * <li>.highlight</li>
     * <li>:up</li>
     * </ul>
     * 
     * @param selector CSS selector
     * @return True if the selector is a "wildcard" selector.
     */
    public static boolean isWildcardSelector(ICSSSelector selector)
    {
        final String elementName = selector.getElementName();
        return elementName == null ||
               UNIVERSAL_SELECTOR.equals(elementName) ||
               GLOBAL_SELECTOR.equals(elementName);
    }

    /**
     * Check if the selector is a optional class selector.
     * An optional class selector is a class selector
     * with the name opt_qname_otherstuff.
     * 
     * The output will not contain the selector if the
     * class identified by qname is not in the output.
     * 
     * @param selector CSS selector
     * @return True if the selector is a "optional" selector.
     */
    public static String getOptionalSelectorName(ICSSSelector selector)
    {
    	ImmutableList<ICSSSelectorCondition> conditions = selector.getConditions();
    	if (conditions.size() == 0)
    		return null;
        final String elementName = conditions.get(0).getValue();
        if (elementName == null) return null;
        if (elementName.startsWith("opt_"))
        {
        	int c = elementName.indexOf("_", 4);
        	if (c >= 0)
        	{
        		return elementName.substring(4, c).replace("-", ".");
        		
        	}
        }
        return null;
    }
    /**
     * Build a map from QNames to class definitions.
     * 
     * @param classDefinitions Class definitions.
     * @return Lookup map.
     */
    public static final ImmutableMap<String, IClassDefinition> buildQNameToDefinitionMap(final Collection<IClassDefinition> classDefinitions)
    {
        final Map<String, IClassDefinition> builder = new HashMap<String, IClassDefinition>();
        for (final IClassDefinition classDefinition : classDefinitions)
        {
            builder.put(classDefinition.getQualifiedName(), classDefinition);
        }
        return ImmutableMap.copyOf(builder);
    }

    /**
     * Find all the class definitions in the given collection.
     * 
     * @param definitions A collection of definitions.
     * @return A set of class definitions.
     */
    public static ImmutableSet<IClassDefinition> getClassDefinitionSet(final Collection<IDefinition> definitions)
    {
        final ImmutableSet.Builder<IClassDefinition> builder = new ImmutableSet.Builder<IClassDefinition>();
        for (final IDefinition def : definitions)
        {
            if (def instanceof IClassDefinition)
                builder.add((IClassDefinition)def);
        }
        final ImmutableSet<IClassDefinition> classDefinitions = builder.build();
        return classDefinitions;
    }

    /**
     * <p>
     * Validate a CSS model. The validation only works for Flex 4+.
     * </p>
     * <h1>Find CSS rules with unused type selectors.</h1>
     * <p>
     * The result is added to the problem collection.
     * <p>
     * For example, if an MXML document only uses {@code <s:Button>} tags, and
     * its {@code <fx:Style>} block contains:
     * 
     * <pre>
     * ...
     * s|Button { fontSize : 12; }
     * local|MyComponent {  color : red; }
     * ...
     * </pre>
     * 
     * Since {@code <local:MyComponent>} isn't used in the current MXML
     * document, the {@code local|MyComponent} is a rule with an <i>unused type
     * selector</i>.
     * <p>
     * The validation process only finds all the unused type selectors, but it
     * doesn't take them out of the code generation.
     * <p>
     * <h1>Find usages of excludes styles.</h1> If a component declares one of
     * its styles to be "excluded", usages of such styles will be reported as
     * {@link CSSExcludedStylePropertyProblem}.
     * 
     * <pre>
     * [Exclude(kind="style", name="foo")]
     * public class MyButton
     * {
     * }
     * </pre>
     * 
     * The following CSS will cause the problem:
     * 
     * <pre>
     * local|MyButton { foo : something; }
     * </pre>
     * 
     * @param linkingCompilationUnits All type selectors that doesn't map to any
     * definition in this collection are "unused".
     * @param session {@link CSSCompilationSession#cssDocuments} has all the CSS
     * models an MXML document has.
     * @throws InterruptedException Abort compilation.
     */
    public static void validate(
            final Set<ICompilationUnit> linkingCompilationUnits,
            final CSSCompilationSession session,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
        final CSSValidator validator = new CSSValidator(session, linkingCompilationUnits, problems);
        for (final ICSSDocument cssDocument : session.cssDocuments)
        {
            visit(cssDocument, validator);
        }
    }

    /**
     * CSS model visitor.
     */
    private static interface ICSSVisitor
    {

        /**
         * Visit a CSS document.
         */
        void beginDocument(final ICSSDocument document);

        /**
         * Visit a CSS rule.
         */
        void beginRule(final ICSSRule rule);

        /**
         * Visit a CSS subject selector.
         */
        void beginSubject(final ICSSSelector selector, final ICSSRule rule);

        /**
         * Visit a CSS property.
         */
        void beginProperty(final ICSSProperty property, final ICSSRule rule);
    }

    /**
     * Validate the following CSS semantic constraints:
     * <ol>
     * <li>unused type selectors</li>
     * <li>usage of excluded styles</li>
     * </ol>
     */
    private static class CSSValidator implements ICSSVisitor
    {
        private final CSSCompilationSession session;
        private final ImmutableMap<String, IClassDefinition> qnameToDefinition;
        private final Collection<ICompilerProblem> problems;
        private final Multimap<ICSSSelector, String> excludedStyles;

        /**
         * Create a CSS validating visitor.
         * 
         * @param session CSS compilation session.
         * @param linkingCompilationUnits All the compilation units to be
         * linked. This is used to find <i>unused type selectors</i>.
         * @param problems Problem collection.
         * @throws InterruptedException Compilation aborted.
         */
        private CSSValidator(final CSSCompilationSession session,
                             final Set<ICompilationUnit> linkingCompilationUnits,
                             final Collection<ICompilerProblem> problems) throws InterruptedException
        {
            this.session = session;
            final ImmutableList<IDefinition> linkingDefinitions =
                    Target.getAllExternallyVisibleDefinitions(linkingCompilationUnits);
            final ImmutableSet<IClassDefinition> classDefinitions =
                    getClassDefinitionSet(linkingDefinitions);
            this.qnameToDefinition = buildQNameToDefinitionMap(classDefinitions);
            this.problems = problems;
            this.excludedStyles = HashMultimap.create();
        }

        /**
         * <ol>
         * <li>Find all excluded styles for the current subject.</li>
         * <li>Find unused type selectors.</li>
         * </ol>
         */
        @Override
        public void beginSubject(final ICSSSelector selector, final ICSSRule rule)
        {
            if (!isWildcardSelector(selector))
            {
                final String qname = session.resolvedSelectors.get(selector);
                if (qnameToDefinition.containsKey(qname))
                {
                    // The subject's resolved QName is in the linking set.
                    // Collect all "excluded" styles for this subject selector.
                    // Only check "Exclude" styles on used styles.
                    final IClassDefinition classDefinition = qnameToDefinition.get(qname);
                    final IMetaTag[] excludeMetaTags = classDefinition.getMetaTagsByName(IASLanguageConstants.EXCLUDE_META_TAG);
                    for (final IMetaTag exclude : excludeMetaTags)
                    {
                        final String kind = exclude.getAttributeValue(IASLanguageConstants.EXCLUDE_META_TAG_KIND);
                        if (IASLanguageConstants.EXCLUDE_META_TAG_STYLE.equals(kind))
                        {
                            final String excludedStyleName = exclude.getAttributeValue(IASLanguageConstants.EXCLUDE_META_TAG_NAME);
                            if (excludedStyleName != null && !excludedStyleName.isEmpty())
                            {
                                this.excludedStyles.put(selector, excludedStyleName);
                            }
                        }
                    }
                }
                else
                {
                    // Selector's resolved QName is not in the linking set.
                    problems.add(new CSSUnusedTypeSelectorProblem(selector));
                }
            }
        }

        @Override
        public void beginRule(final ICSSRule rule)
        {
        }

        @Override
        public void beginDocument(final ICSSDocument document)
        {
        }

        /**
         * Check usages of excluded styles.
         */
        @Override
        public void beginProperty(final ICSSProperty property, final ICSSRule rule)
        {
            for (final ICSSSelector subject : rule.getSelectorGroup())
            {
                final Collection<String> excludedStylesForSubject = excludedStyles.get(subject);
                if (excludedStylesForSubject != null && excludedStylesForSubject.contains(property.getName()))
                {
                    final String qname = session.resolvedSelectors.get(subject);
                    problems.add(new CSSExcludedStylePropertyProblem(property, qname));
                }

            }
        }

    }

    /**
     * Visit a CSS document model.
     * 
     * @param document CSS model.
     * @param visitor Handler for various visit methods.
     */
    private static void visit(final ICSSDocument document, final ICSSVisitor visitor)
    {
        visitor.beginDocument(document);
        for (final ICSSRule rule : document.getRules())
        {
            visitor.beginRule(rule);
            for (final ICSSSelector selector : rule.getSelectorGroup())
                visitor.beginSubject(selector, rule);

            for (final ICSSProperty property : rule.getProperties())
                visitor.beginProperty(property, rule);
        }
    }

}
