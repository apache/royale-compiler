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

package org.apache.flex.compiler.internal.css;

import static org.apache.flex.compiler.internal.css.semantics.CSSSemanticAnalyzer.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSManager;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.caches.CSSDocumentCache;
import org.apache.flex.compiler.internal.caches.CacheStoreKeyBase;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.units.EmbedCompilationUnit;
import org.apache.flex.compiler.problems.CSSUnresolvedClassReferenceProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swc.ISWCManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Project-level CSS manager.
 */
public class CSSManager implements ICSSManager
{

    /**
     * Find all the dependency class definitions and compilation units
     * introduced by the given CSS rules. This method is basically an
     * aggregation of {@link CSSSemanticAnalyzer#resolveDependencies()}.
     * 
     * @param session CSS compilation session that stores resolved embed
     * compilation units.
     * @param flexProject Flex project.
     * @param cssRules Resolve class dependencies in these CSS rules.
     * @param problems Problem collection.
     */
    static void getClassDefinitionDependencies(
            final CSSCompilationSession session,
            final FlexProject flexProject,
            final ImmutableSet<ICSSRule> cssRules,
            final Set<IClassDefinition> classReferences,
            final Set<EmbedCompilationUnit> embedCompilationUnits,
            final Collection<ICompilerProblem> problems)
    {
        for (final ICSSRule matchedRule : cssRules)
        {
            resolveDependencies(
                    session.resolvedEmbedProperties,
                    matchedRule,
                    flexProject,
                    classReferences,
                    embedCompilationUnits,
                    problems);
        }
    }

    /**
     * Find compilation units for the given definition set. A definition might
     * not have a compilation unit, but a compiler problem must already be
     * logged for it in {@code problems} before this method is called.
     * 
     * @param flexProject Flex project.
     * @param classDefinitions Class definitions.
     * @param problems Problems collection.
     * @return A set of compilation unit for the given class definitions.
     */
    static ImmutableSet<ICompilationUnit> getCompilationUnitsForDefinitions(
            final FlexProject flexProject,
            final Set<IClassDefinition> classDefinitions,
            final Collection<ICompilerProblem> problems)
    {
        final ImmutableSet.Builder<ICompilationUnit> builder = new ImmutableSet.Builder<ICompilationUnit>();
        for (final IClassDefinition classDefinition : classDefinitions)
        {
            final ASProjectScope scope = flexProject.getScope();
            final ICompilationUnit compilationUnit = scope.getCompilationUnitForDefinition(classDefinition);
            if (compilationUnit != null)
                builder.add(compilationUnit);
            else
                assert problemCreatedForUnresolvedClassReference(problems, classDefinition) : "Can't find compilation unit for class '" + classDefinition.getQualifiedName() + "'. Expected a 'CSSUnresolvedClassReference'.";
        }
        return builder.build();
    }

    /**
     * Check that a {@link CSSUnresolvedClassReferenceProblem} problem is created for
     * the given class definition.
     * 
     * @param problems Problems collection.
     * @param classDefinition {@link IClassDefinition} definition of the unresolved class.
     * @return True if there's a problem for the unresolved class definition.
     */
    static boolean problemCreatedForUnresolvedClassReference(
            final Collection<ICompilerProblem> problems,
            final IClassDefinition classDefinition)
    {
        for (final ICompilerProblem problem : problems)
        {
            if (problem instanceof CSSUnresolvedClassReferenceProblem)
            {
                final CSSUnresolvedClassReferenceProblem unresolved = (CSSUnresolvedClassReferenceProblem)problem;
                if (unresolved.qname.equals(classDefinition.getQualifiedName()))
                    return true;
            }
        }
        return false;
    }

    /** Owner project. */
    private final FlexProject flexProject;

    /**
     * Initialize a CSS manager.
     * 
     * @param flexProject Owner project.
     */
    public CSSManager(final FlexProject flexProject)
    {
        assert flexProject != null;
        this.flexProject = flexProject;
    }

    @Override
    public Collection<ICSSDocument> getCSSFromStyleModules()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ICSSDocument> getCSSFromSWCDefaultStyle()
    {
        final Collection<ICSSDocument> result = new ArrayList<ICSSDocument>();
        for (final ISWC swc : flexProject.getLibraries())
        {
            final ICSSDocument css = getDefaultCSS(swc.getSWCFile());
            if (css != null)
                result.add(css);
        }
        return result;
    }

    @Override
    public ICSSDocument getDefaultCSS(final File swcFile)
    {
        final ISWCManager swcManager = flexProject.getWorkspace().getSWCManager();
        final CSSDocumentCache cache = swcManager.getCSSDocumentCache();
        final ISWC swc = swcManager.get(swcFile);
        return cache.getDefaultsCSS(swc, flexProject.getCompatibilityVersion());
    }
    
    @Override
    public Collection<ICompilationUnit> getDependentCompilationUnitsFromCSS(
            final CSSCompilationSession session,
            final ICSSDocument cssDocument,
            final Collection<IDefinition> definitions,
            final Collection<ICompilerProblem> problems)
    {
        assert cssDocument != null : "Expected CSS document to get CSS dependencies.";
        assert definitions != null : "Expected a set of definitions to activate CSS rules.";
        assert problems != null : "Expected problems collection. Do not ignore problems.";

        final ImmutableSet<String> qnames = buildQNameToDefinitionMap(getClassDefinitionSet(definitions)).keySet();

        // IFilter (ICSSRule[], IClassDefinition[]) -> (ICSSRule[] applies to IClassDefinition[])
        final ImmutableSet<ICSSRule> matchedRules = getMatchedRules(session, flexProject, cssDocument, qnames, problems);

        // Find IClassDefinition(ClassReference) and CompilationUnit(Embed) from the matched ICSSRule[] 
        final Set<IClassDefinition> classReferences = new HashSet<IClassDefinition>();
        final Set<EmbedCompilationUnit> embedCompilationUnits = new HashSet<EmbedCompilationUnit>();
        getClassDefinitionDependencies(
                session,
                flexProject,
                matchedRules,
                classReferences, // output
                embedCompilationUnits, // output
                problems);

        // Find ICompilationUnit[] for IClassDefinition(ClassReference)[]
        final ImmutableSet<ICompilationUnit> classReferenceCompilationUnits =
                getCompilationUnitsForDefinitions(flexProject, classReferences, problems);

        // Only "activated" rules are included in code generation.
        for (final ICSSRule rule : matchedRules)
        {
            session.activatedRules.add(rule);
        }

        // Compilation units from ClassReference() and Embed().
        return new ImmutableSet.Builder<ICompilationUnit>()
                .addAll(classReferenceCompilationUnits)
                .addAll(embedCompilationUnits)
                .build();
    }

    @Override
    public Collection<ICSSDocument> getCSSFromThemes(final Collection<ICompilerProblem> problems)
    {
        final ImmutableList.Builder<ICSSDocument> builder = new ImmutableList.Builder<ICSSDocument>();
        final ISWCManager swcManager = flexProject.getWorkspace().getSWCManager();
        final CSSDocumentCache cssCache = swcManager.getCSSDocumentCache();
        for (final IFileSpecification themeFile : flexProject.getThemeFiles())
        {
            try
            {
                final ICSSDocument css;
                final String extension = FilenameUtils.getExtension(themeFile.getPath());
                if ("swc".equalsIgnoreCase(extension))
                {
                    final ISWC swc = swcManager.get(new File(themeFile.getPath()));
                    css = cssCache.getDefaultsCSS(
                                swc,
                                flexProject.getCompatibilityVersion());
                }
                else if ("css".equalsIgnoreCase(extension))
                {
                    final CacheStoreKeyBase key = CSSDocumentCache.createKey(themeFile.getPath());
                    css = cssCache.get(key);
                }
                else
                {
                    continue;
                }

                // Ignore theme file without a defaults CSS.
                if (css != null && css != CSSDocumentCache.EMPTY_CSS_DOCUMENT)
                    builder.add(css);
            }
            catch (CSSDocumentCache.ProblemParsingCSSRuntimeException cssError)
            {
                problems.addAll(cssError.cssParserProblems);
            }

        }
        return builder.build();
    }

    @Override
    public boolean isFlex3CSS()
    {
        final Integer compatibilityVersion = flexProject.getCompatibilityVersion();
        if (compatibilityVersion == null)
            return false;
        else if (compatibilityVersion > Configuration.MXML_VERSION_3_0)
            return false;
        else
            return true;
    }

    @Override
    public ICSSDocument getCSS(String cssFilename)
    {
        final CSSDocumentCache cache = flexProject.getWorkspace().getSWCManager().getCSSDocumentCache();
        final CacheStoreKeyBase key = CSSDocumentCache.createKey(cssFilename);
        final ICSSDocument css = cache.get(key);
        
        if(CSSDocumentCache.EMPTY_CSS_DOCUMENT == css)
            return null;
        else
            return css;
    }
}
