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

package org.apache.royale.compiler.internal.units;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.apache.commons.io.FilenameUtils;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.css.CSSDocument;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.css.codegen.CSSModuleGenerator;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.FileScopeRequestResultBase;
import org.apache.royale.compiler.internal.units.requests.SWFTagsRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.problems.CSSCodeGenProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swf.tags.DoABCTag;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The main compilation unit for a CSS module project. It can compile a CSS file
 * into a class to load styles at runtime. This compilation unit type is created
 * in order to setup the correct dependencies in the target.
 */
public class StyleModuleCompilationUnit extends CompilationUnitBase
{

    /**
     * This implementation of {@link ISyntaxTreeRequestResult} carries a CSS
     * model object ({@link CSSDocument}).
     */
    private static class StyleModuleSyntaxTreeRequestResult extends SyntaxTreeRequestResult
    {
        private StyleModuleSyntaxTreeRequestResult(final ICSSDocument cssDocument,
                                                   final Collection<ICompilerProblem> syntaxProblems,
                                                   final long lastModified)
        {
            super(lastModified, syntaxProblems);
            this.cssDocument = cssDocument;
        }

        private final ICSSDocument cssDocument;
    }

    /**
     * This implementation of {@link org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult} carries a CSS
     * model and a {@link CSSCompilationSession} objects that has all the
     * resolved symbols in the CSS.
     */
    private static class StyleModuleSemanticRequestResult implements IOutgoingDependenciesRequestResult
    {
        private StyleModuleSemanticRequestResult(final ICSSDocument cssDocument,
                                                 final CSSCompilationSession cssCompilationSession,
                                                 final Collection<ICompilerProblem> semanticProblems)
        {
            this.cssDocument = cssDocument;
            this.cssCompilationSession = cssCompilationSession;
            this.semanticProblems = semanticProblems;
        }

        private final ICSSDocument cssDocument;
        private final CSSCompilationSession cssCompilationSession;
        private final Collection<ICompilerProblem> semanticProblems;

        @Override
        public ICompilerProblem[] getProblems()
        {
            return semanticProblems.toArray(new ICompilerProblem[0]);
        }
    }

    /**
     * Create a {@code StyleModuleCompilationUnit}.
     * 
     * @param project Owner project.
     * @param cssFile Path to the source file.
     * @param basePriority Base priority.
     */
    public StyleModuleCompilationUnit(CompilerProject project, IFileSpecification cssFile, BasePriority basePriority)
    {
        super(project, cssFile.getPath(), basePriority, false);
        this.swfTagName = FilenameUtils.getBaseName(cssFile.getPath());
        this.cssFile = cssFile;
    }

    /**
     * Name of the generated {@code DoABC} SWF tag.
     */
    private final String swfTagName;

    /**
     * Main CSS file.
     */
    private final IFileSpecification cssFile;

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.CSS_UNIT;
    }

    /**
     * {@code StyleModuleCompilationUnit} does not provide an AST, unless we
     * decided to start CMP-879. However, the return value will contain the CSS
     * syntax errors in {@link ISyntaxTreeRequestResult#getProblems()}.
     */
    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        final List<ICompilerProblem> syntaxErrors = new ArrayList<ICompilerProblem>();
        CSSDocument css = null;
        try
        {
            final ANTLRFileStream fileStream = new ANTLRFileStream(cssFile.getPath());
            css = CSSDocument.parse(fileStream, syntaxErrors);
        }
        catch (IOException e)
        {
            ICompilerProblem problem = new FileNotFoundProblem(cssFile.getPath());
            syntaxErrors.add(problem);
        }
        return new StyleModuleSyntaxTreeRequestResult(css, syntaxErrors, cssFile.getLastModified());
    }

    /**
     * Synthesize a file scope with a public definition of module main class.
     */
    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        final ASFileScope fileScope = new ASFileScope(getProject().getWorkspace(), cssFile.getPath());
        // TODO: Generate class names from CSS file name after the CSS module runtime code is finalized.
        final ClassDefinition classDefinition = new ClassDefinition(
                "CSSModule2Main",
                NamespaceDefinition.createPackagePublicNamespaceDefinition(""));
        fileScope.addDefinition(classDefinition);
        return new FileScopeRequestResultBase(
                Collections.<ICompilerProblem> emptySet(),
                ImmutableSet.<IASScope> of(fileScope));
    }

    /**
     * Get the ABC byte code generated from the CSS file.
     * <p>
     * This request depends on {@link #handleOutgoingDependenciesRequest()}.
     */
    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        final StyleModuleSemanticRequestResult semanticResult = (StyleModuleSemanticRequestResult) getOutgoingDependenciesRequest().get();
        final ABCEmitter emitter = new ABCEmitter();
        emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);
        final RoyaleProject project = (RoyaleProject)getProject();
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        byte[] bytes = null;
        try
        {
            // TODO: Generate class names from CSS file name after the CSS module runtime code is finalized.
            final String moduleName = "CSSModule2";

            final Name mainClassName = new Name(moduleName + "Main");
            CSSModuleGenerator.generateMainClass(emitter, project, mainClassName);

            final Name styleDataClassName = new Name(moduleName + "_StyleData");
            CSSModuleGenerator.generateStyleDataClass(
                    emitter,
                    project,
                    semanticResult.cssDocument,
                    semanticResult.cssCompilationSession,
                    styleDataClassName);

            bytes = emitter.emit();
        }
        catch (Exception e)
        {
            final CSSCodeGenProblem problem = new CSSCodeGenProblem(e);
            problems.add(problem);
        }
        return new ABCBytesRequestResult(bytes, problems.toArray(new ICompilerProblem[0]), Collections.<IEmbedData>emptySet());
    }

    /**
     * Get a {@link DoABCTag} that contains the generated ABC byte code.
     */
    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        final IABCBytesRequestResult abcResult = getABCBytesRequest().get();

        return new SWFTagsRequestResult(abcResult.getABCBytes(), swfTagName);
    }

    /**
     * Collect CSS semantic problems. Add dependencies to the project.
     * <p>
     * This request depends on {@link #handleSyntaxTreeRequest()}.
     * 
     * @return The result value has two pay-load values: a CSS model and a CSS
     * compilation session.
     */
    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        final StyleModuleSyntaxTreeRequestResult syntaxResult = (StyleModuleSyntaxTreeRequestResult)getSyntaxTreeRequest().get();
        final Collection<ICompilerProblem> cssSemanticProblems = new ArrayList<ICompilerProblem>();
        final CSSCompilationSession session = new CSSCompilationSession();
        if (syntaxResult.cssDocument != null)
        {
            final RoyaleProject royaleProject = (RoyaleProject)getProject();
            updateStyleCompilationUnitDependencies(
                    session,
                    royaleProject,
                    ImmutableList.<ICSSDocument> of(syntaxResult.cssDocument),
                    cssSemanticProblems);
            IResolvedQualifiersReference styleModuleBaseClassRef = ReferenceFactory.packageQualifiedReference(
                    getProject().getWorkspace(), "StyleModuleBase");
            styleModuleBaseClassRef.resolve(royaleProject, this, DependencyType.INHERITANCE);
        }

        return new StyleModuleSemanticRequestResult(syntaxResult.cssDocument, session, cssSemanticProblems);
    }
}
