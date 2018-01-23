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

package org.apache.royale.compiler.internal.as.codegen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.diagnostics.AbstractDiagnosticVisitor;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.ScriptInfo;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.semantics.MethodBodySemanticChecker;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.OperandStackUnderflowProblem;
import org.apache.royale.compiler.problems.ScopeStackUnderflowProblem;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * The global lexical scope for codegen.  This class can't have a containing
 * lexical scope.
 */
public class GlobalLexicalScope extends LexicalScope
{
    /**
     * The project we are compiling in. Used
     * to resolve references, and determine 
     * what names to emit for those references.
     */
    private final ICompilerProject project;

    /**
     * The code generator that this lexical scope is using.
     */
    private final ICodeGenerator generator;

    /**
     *  This compilation unit's IABCVisitor.
     */
    private final IABCVisitor emitter;

    /**
     *  Prefix string for all synthetic names.  Usually the base name of
     *  the root source file of a {@link org.apache.royale.compiler.units.ICompilationUnit}.
     */
    private final String syntheticNamePrefix;

    /**
     *  A serial ID count.
     */
    private int ticketCount = 0;

    /**
     * Indicates whether this scope is for an {@link IInvisibleCompilationUnit}.
     * <p>
     * If so, {@link IDefinition}s need to be normalized before
     * doing certain semantic checks.
     * <p>
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}s create the
     * possibility that we will be processing a file whose definitions are not
     * in the {@link ASProjectScope} in the {@link ICompilerProject}. This in
     * turn creates the possibility there are two or more {@link IDefinition}s
     * floating around for the same class, variable, or function. One the
     * {@link IDefinition}, the "normalized" one, should be registered with the
     * {@link ASProjectScope} in the {@link ICompilerProject}. The other
     * {@link IDefinition}s are from
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}s. When doing
     * semantic analysis if we don't "normalize" {@link IDefinition}s in some
     * cases, we'll get spurious errors because the semantic analysis code
     * compares {@link IDefinition}s by identity ( which is faster ) rather
     * than by name. This method should only be called when doing semantic
     * analysis of an {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}.
     * <p>
     * Also, if we are in an invisible compilation unit, we cannot determine
     * the validity of an import simply by looking at whether it matches
     * a definition in the project scope. Since invisible compilation units
     * do not contribute their definition to the project scope, we must
     * also check whether the import matches an externaly-visible definition
     * in the file.
     */
    private final boolean inInvisibleCompilationUnit;

    /**
     * A mapping between absolute paths to a source file, and the encoded
     * path in the path;package_name;file_name format which is required
     * by builder.
     */
    private final Map<String, String> encodedDebugFiles;

    /** 
     *  The value for the <code>inInvisibleCompilationUnit</code> constructor
     *  parameter that means we are not in an invisible compilation unit.
     */
    public static final boolean VISIBLE_COMPILATION_UNIT = false;

    /**
     *  Single-thread code generation.
     */
    public static final boolean NO_PARALLEL_CODEGEN = false;

    /**
     *  No synthetic name prefix.
     */
    public static final String EMPTY_NAME_PREFIX = "";

    /**
     *  No code generator.
     */
    public static final ICodeGenerator NO_GENERATOR = null;

    /**
     *  No IABCVisitor supplied, this scope should supply a default implementation.
     */
    public static final IABCVisitor USE_DEFAULT_EMITTER = null;

    /**
     *  Container-level initialization instructions.
     *  Contrast hoistedInitInstructions, which is a
     *  more granular level of initialization.
     */
    private final InstructionList initInstructions;

    /**
     * Collection of all embedded assets discovered
     */
    private final Set<EmbedData> embeds;

    /**
     *  Invoking phase's collection of compiler problems.
     *  Problems encountered during code-gen added to this collection.
     */
    private final Collection<ICompilerProblem> problems;

    /**
     *  Construct a "global" scope to attempt to generate a constant value.
     *  @param project - the active compiler project.
     *  @param generator - the active code generator.
     */
    public GlobalLexicalScope(ICompilerProject project, ICodeGenerator generator)
    {
        this(
            project,
            generator,
            EMPTY_NAME_PREFIX,
            USE_DEFAULT_EMITTER,
            VISIBLE_COMPILATION_UNIT,
            NO_PARALLEL_CODEGEN,
            Collections.<String, String>emptyMap()
        );
    }

    /**
     *  Construct a global scope with a specialized IABCVisitor.
     *  Callers are typically cross-compilers with a visitor that
     *  implements novel semantics.
     *  @param project - the active compiler project.
     *  @param generator - the caller's code generator.
     *  @param syntheticNamePrefix - text to prefix any synthetic names generated.
     *  @param emitter - the caller's IABCVisitor.
     */
    public GlobalLexicalScope(ICompilerProject project, ICodeGenerator generator, String syntheticNamePrefix, IABCVisitor emitter)
    {
        this(
            project,
            generator,
            syntheticNamePrefix,
            emitter,
            VISIBLE_COMPILATION_UNIT,
            NO_PARALLEL_CODEGEN,
            Collections.<String, String>emptyMap()
        );
    }
    
    /**
     *  Construct a global lexical scope to serve as the basis for generating
     *  code for a compilation unit.
     *  @param project - the active compiler project.
     *  @param generator - the active code generator.
     *  @param syntheticNamePrefix - text to prefix any synthetic names generated.
     *  @param inInvisibleCompilationUnit - true if we are compiling an invisible
     *  compilation unit, which does not contribute any definitions to the project scope
     *  @param useParallelCodegen - use multiple threads to generate code if true.
     *  @param encodedDebugFiles - a mapping between the absolute path of a file, and the
     *         encoded path that is used by OP_debugfile
     */
    public GlobalLexicalScope(ICompilerProject project,
            ICodeGenerator generator,
            String syntheticNamePrefix, 
            boolean inInvisibleCompilationUnit,
            boolean useParallelCodegen,
            Map<String, String> encodedDebugFiles)
    {
        this (
            project,
            generator,
            syntheticNamePrefix,
            USE_DEFAULT_EMITTER,
            inInvisibleCompilationUnit,
            useParallelCodegen,
            encodedDebugFiles
        );
    }

    /**
     *  Initialize all fields of the GlobalLexicalScope.
     *  @param project - the active compiler project.
     *  @param generator - the active code generator.
     *  @param syntheticNamePrefix - text to prefix any synthetic names generated.
     *  @param emitter - the compilation unit's IABCVisitor, or USE_DEFAULT_EMITTER.
     *  @param inInvisibleCompilationUnit - true if we are compiling an invisible
     *  compilation unit, which does not contribute any definitions to the project scope
     *  @param useParallelCodegen - use multiple threads to generate code if true.
     *  @param encodedDebugFiles - a mapping between the absolute path of a file, and the
     *         encoded path that is used by OP_debugfile
     */
    private GlobalLexicalScope(ICompilerProject project,
            ICodeGenerator generator,
            String syntheticNamePrefix, 
            IABCVisitor emitter,
            boolean inInvisibleCompilationUnit,
            boolean useParallelCodegen,
            Map<String, String> encodedDebugFiles)
    {
        super();

        this.project = project;
        this.generator = generator;
        this.syntheticNamePrefix = syntheticNamePrefix;
        this.inInvisibleCompilationUnit = inInvisibleCompilationUnit;
        this.encodedDebugFiles = encodedDebugFiles;
        this.initInstructions = new InstructionList();

        super.methodBodySemanticChecker = new MethodBodySemanticChecker(this);

        if ( emitter != USE_DEFAULT_EMITTER )
            this.emitter = emitter;
        else
            this.emitter = new ABCEmitter(new AETDiagnosticsVisitor());

        // If we are using parallel code generation make sure the
        // collections are a synchronized collection so that
        // multiple threads can safely add to the collection simultaneously.
        if (useParallelCodegen)
        {
            this.embeds = Collections.synchronizedSet(new HashSet<EmbedData>());
            this.problems = Collections.synchronizedList(new LinkedList<ICompilerProblem>());
        }
        else
        {
            this.embeds = new HashSet<EmbedData>();
            this.problems = new LinkedList<ICompilerProblem>();
        }
    }

    @Override
    public boolean isGlobalScope()
    {
        return true;
    }

    @Override
    public ICompilerProject getProject()
    {
        return project;
    }

    @Override
    public ICodeGenerator getGenerator()
    {
        return generator;
    }

    @Override
    IABCVisitor getEmitter()
    {
        return emitter;
    }

    @Override
    public boolean getInInvisibleCompilationUnit()
    {
        return inInvisibleCompilationUnit;
    }

    @Override
    public InstructionList getInitInstructions()
    {
        return initInstructions;
    }

    @Override
    public MethodBodySemanticChecker getMethodBodySemanticChecker()
    {
        return methodBodySemanticChecker;
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }
    
    @Override
    public void addProblem(ICompilerProblem problem)
    {
        problems.add(problem);
    }

    /**
     * @return All embeds found during the code generation phase.
     */
    public Set<EmbedData> getEmbeds()
    {
        return embeds;
    }

    /**
     *  @return a synthetic name string, unique within this compilation unit.
     *  @param user_supplied - some user-supplied text, which may make debugging easier.
     */
    protected String getSyntheticName(String user_supplied)
    {
        return syntheticNamePrefix +  "$" + Integer.toString(ticketCount++) + ":" + user_supplied;
    }

    /**
     * @param filename The filename to get the encoded filename for
     * @return the encoded filename
     */
    protected String getEncodedDebugFile(String filename)
    {
        if (filename == null)
            return null;

        String encodedFilename = encodedDebugFiles.get(filename);
        // if there is no encoded filename, that means filename is already
        // encoded, or doesn't come from a source path
        if (encodedFilename == null)
            return filename;

        return encodedFilename;
    }

    /**
     * Sub-class of {@link AbstractDiagnosticVisitor} that creates compiler
     * problems for scope and operand stack underflow. All other diagnostics
     * result in an assertion failure and a runtime exception.
     */
    private class AETDiagnosticsVisitor extends AbstractDiagnosticVisitor
    {

        @Override
        public void operandStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
        {
            problems.add(new OperandStackUnderflowProblem(cfg, block, instructionIndex));
        }

        @Override
        public void scopeStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
        {
            problems.add(new ScopeStackUnderflowProblem(cfg, block, instructionIndex));
        }

        @Override
        public void unreachableBlock(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block)
        {
            // These are reported during ABCLinker-phase optimization.
            assert false : "unreachableBlock";
            throw new Error("unreachableBlock");
        }
        
        @Override
        public void tooManyDefaultParameters(MethodInfo methodInfo)
        {
            assert false : "tooManyDefaultParameters";
            throw new Error("tooManyDefaultParameters");
        }

        @Override
        public void incorrectNumberOfParameterNames(MethodInfo methodInfo)
        {
            assert false : "incorrectNumberOfParameterNames";
            throw new Error("incorrectNumberOfParameterNames");
        }

        @Override
        public void nativeMethodWithMethodBody(MethodInfo methodInfo, MethodBodyInfo methodBodyInfo)
        {
            assert false : "nativeMethodWithMethodBody";
            throw new Error("nativeMethodWithMethodBody");
        }

        @Override
        public void scriptInitWithRequiredArguments(ScriptInfo scriptInfo, MethodInfo methodInfo)
        {
            assert false : "scriptInitWithRequiredArguments";
            throw new Error("scriptInitWithRequiredArguments");
        }
        
    }
}
