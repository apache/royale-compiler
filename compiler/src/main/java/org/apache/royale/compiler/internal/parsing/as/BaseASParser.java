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

package org.apache.royale.compiler.internal.parsing.as;

import static org.apache.royale.compiler.common.ISourceLocation.UNKNOWN;
import static org.apache.royale.compiler.internal.parsing.as.ASTokenTypes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.royale.abc.semantics.ECMASupport;
import org.apache.royale.compiler.parsing.GenericTokenStream;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IASToken.ASTokenKind;
import org.apache.royale.compiler.problems.InvalidConfigLocationProblem;
import org.apache.royale.compiler.problems.NonConstConfigVarProblem;
import org.apache.royale.compiler.problems.ShadowedConfigNamespaceProblem;

import org.apache.commons.io.IOUtils;

import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.MismatchedTokenException;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.asdoc.IASParserASDocDelegate;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.filespecs.StringFileSpecification;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.ArrayLiteralNode;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorNodeBase;
import org.apache.royale.compiler.internal.tree.as.BlockNode;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.ConfigConstNode;
import org.apache.royale.compiler.internal.tree.as.ConfigExpressionNode;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.EmbedNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.FullNameNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.ModifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.QualifiedNamespaceExpressionNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.tree.as.UnaryOperatorNodeBase;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.problems.AttributesNotAllowedOnPackageDefinitionProblem;
import org.apache.royale.compiler.problems.CanNotInsertSemicolonProblem;
import org.apache.royale.compiler.problems.EmbedInitialValueProblem;
import org.apache.royale.compiler.problems.EmbedMultipleMetaTagsProblem;
import org.apache.royale.compiler.problems.EmbedOnlyOnClassesAndVarsProblem;
import org.apache.royale.compiler.problems.EmbedUnsupportedTypeProblem;
import org.apache.royale.compiler.problems.ExpectDefinitionKeywordAfterAttributeProblem;
import org.apache.royale.compiler.problems.ExtraCharactersAfterEndOfProgramProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.InvalidAttributeProblem;
import org.apache.royale.compiler.problems.InvalidLabelProblem;
import org.apache.royale.compiler.problems.InvalidTypeProblem;
import org.apache.royale.compiler.problems.MXMLInvalidDatabindingExpressionProblem;
import org.apache.royale.compiler.problems.MissingLeftBraceBeforeFunctionBodyProblem;
import org.apache.royale.compiler.problems.MultipleConfigNamespaceDecorationsProblem;
import org.apache.royale.compiler.problems.MultipleNamespaceAttributesProblem;
import org.apache.royale.compiler.problems.MultipleReservedNamespaceAttributesProblem;
import org.apache.royale.compiler.problems.NamespaceAttributeNotAllowedProblem;
import org.apache.royale.compiler.problems.NestedClassProblem;
import org.apache.royale.compiler.problems.NestedInterfaceProblem;
import org.apache.royale.compiler.problems.NestedPackageProblem;
import org.apache.royale.compiler.problems.ParserProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.compiler.problems.UnboundMetadataProblem;
import org.apache.royale.compiler.problems.UnexpectedEOFProblem;
import org.apache.royale.compiler.problems.UnexpectedTokenProblem;
import org.apache.royale.compiler.problems.XMLOpenCloseTagNotMatchProblem;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode.NamespaceDecorationKind;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.IInvisibleCompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.NonLockingStringReader;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Base class for the ANTLR-generated ActionScript parser {@link ASParser}.
 * Complex Java action code should be put in this base class instead of in the
 * ANTLR grammar.
 */
abstract class BaseASParser extends LLkParser implements IProblemReporter
{
    private static final String CONFIG_AS = "config.as";

    /**
     * Variable to use as an empty project configuration
     */
    public static final IProjectConfigVariables EMPTY_CONFIGURATION = null;

    /**
     * Used to specify "directive" rule's end token as "no end token" - the
     * parser will consume till the end of the input stream when recover from
     * errors.
     */
    protected static final int NO_END_TOKEN = -1;

    private static final String SUB_SYSTEM = "ASParser";

    /**
     * Produces an AST from the given file input. A FileNode will always be
     * returned, however it is not guaranteed to contain any content. This is
     * provided as a convenience, as it is equivalent to calling:
     * <code>parseFile(spec, workspace, EnumSet.of(PostProcessStep.CALCULATE_OFFSETS), true, true)</code>
     * meaning we follow includes, fix the tree and calculate offsets.
     * 
     * @param spec the {@link IFileSpecification} that points to the file that
     * will be parsed
     * @param fileSpecGetter the {@link IFileSpecificationGetter} that should be
     * used to open files.
     * @return a {@link FileNode} built from the given input
     */
    public static FileNode parseFile(IFileSpecification spec, IFileSpecificationGetter fileSpecGetter)
    {
        return parseFile(spec, fileSpecGetter, EnumSet.of(PostProcessStep.CALCULATE_OFFSETS), EMPTY_CONFIGURATION, true, false);
    }

    /**
     * Produces an AST from the given file input. A FileNode will always be
     * returned, however it is not guaranteed to contain any content
     * 
     * @param spec the {@link IFileSpecification} that points to the file that
     * will be parsed
     * @param fileSpecGetter the {@link IFileSpecificationGetter} that should be
     * used to open files.
     * @param postProcess the set of operations we want to perform on this tree
     * before it is returned. See {@link PostProcessStep}
     * @param variables the {@link IProjectConfigVariables} containing
     * project-level conditional compilation variables
     * @param followIncludes flag to determine if include statements should be
     * followed
     * @return a {@link FileNode} built from the given input
     */
    public static FileNode parseFile(IFileSpecification spec,
            IFileSpecificationGetter fileSpecGetter,
            EnumSet<PostProcessStep> postProcess,
            IProjectConfigVariables variables,
            boolean followIncludes,
            boolean strictIdentifierNames)
    {
        return parseFile(spec, fileSpecGetter, postProcess, variables, followIncludes, true, strictIdentifierNames, Collections.<String> emptyList(), DeferFunctionBody.DISABLED, null, null);
    }

    /**
     * Produces an AST from the given file input. A FileNode will always be
     * returned, however it is not guaranteed to contain any content
     * 
     * @param spec the {@link IFileSpecification} that points to the file that
     * will be parsed
     * @param postProcess the set of operations we want to perform on this tree
     * before it is returned. See {@link PostProcessStep}
     * @param variables the {@link IProjectConfigVariables} containing
     * project-level conditional compilation variables
     * @param followIncludes flag to determine if include statements should be
     * followed
     * @param allowEmbeds flag to indicate if we should ignore embed meta data
     * or create EmbedNodes
     * @param includedFiles Files included by {@code asc -in} option.
     * @param flashProject Used to resolve included files in all the source
     * folders. Use {@code null} if the project is not a {@link IASProject}.
     * @param compilationUnit used to manage missing include files. both project
     * and compilation unit must be passed if you wish to have compilation units
     * re-built when their missing referenced files are added to the project.
     * @return a {@link FileNode} built from the given input
     */
    public static FileNode parseFile(IFileSpecification spec,
            IFileSpecificationGetter fileSpecGetter,
            EnumSet<PostProcessStep> postProcess,
            IProjectConfigVariables variables,
            boolean followIncludes,
            boolean allowEmbeds,
            boolean strictIdentifierNames,
            List<String> includedFiles,
            DeferFunctionBody deferFunctionBody,
            IASProject flashProject,
            ICompilationUnit compilationUnit)
    {
        assert spec != null : "File spec can't be null.";
        assert fileSpecGetter != null : "File specification getter can't be null";
        assert fileSpecGetter.getWorkspace() instanceof Workspace : "Expected Workspace type.";

        final FileNode node = new FileNode(fileSpecGetter, spec.getPath());
        final IncludeHandler includeHandler = node.getIncludeHandler();
        includeHandler.setProjectAndCompilationUnit(flashProject, compilationUnit);

        StreamingASTokenizer tokenizer = null;
        try
        {
            tokenizer = StreamingASTokenizer.createForASParser(
                    spec,
                    includeHandler,
                    followIncludes,
                    includedFiles,
                    strictIdentifierNames);

            final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);

            final ASParser parser = new ASParser(fileSpecGetter.getWorkspace(), buffer);
            parser.deferFunctionBody = deferFunctionBody;
            parser.setProjectConfigVariables(variables);
            parser.setFilename(spec.getPath());
            parser.setAllowEmbeds(allowEmbeds);
            parser.parseFile(node, postProcess);
            if (node.getAbsoluteEnd() < tokenizer.getEndOffset())
                node.setEnd(tokenizer.getEndOffset());

            node.setProblems(tokenizer.getTokenizationProblems());
            node.setProblems(parser.getSyntaxProblems());
            final OffsetLookup offsetLookup =
                    new OffsetLookup(includeHandler.getOffsetCueList());
            node.setOffsetLookup(offsetLookup);

            int[] absoluteOffset = offsetLookup.getAbsoluteOffset(spec.getPath(), tokenizer.getEndOffset());
            //Absolute offset for the last token in a file shouldn't be more than one.
            assert absoluteOffset.length == 1 : "There seems to be a cycle in the include tree which has not been handled.";
            if (node.getAbsoluteEnd() < absoluteOffset[0])
                node.setEnd(absoluteOffset[0]);

        }
        catch (FileNotFoundException e)
        {
            // Use the message from the FileNotFoundException exception as that
            // will have the actual file that is missing in cases where .as files
            // are combined.
            node.addProblem(new FileNotFoundProblem(e.getMessage()));
        }
        catch (Exception e)
        {
            ICompilerProblem problem = new InternalCompilerProblem2(spec.getPath(), e, SUB_SYSTEM);
            node.addProblem(problem);
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
        return node;
    }

    /**
     * Parse a fragment of ActionScript. The resulting AST node is the return
     * value. The resulting definitions and scopes will be attached to the given
     * {@code containingScope}. This is used by MXML script tags.
     * 
     * @param fragment ActionScript block.
     * @param path Containing source path of the ActionScript fragment.
     * @param offset Start offset of the script.
     * @param line Line offset of the script.
     * @param column Column offset of the script.
     * @param problems Problems parsing the script.
     * @param workspace Owner workspace.
     * @param fileNodeAccumulator Collect data that needs to be stored on
     * {@code FileNode}.
     * @param containingScope The resulting definitions and scopes from the
     * script will be attached to this scope.
     * @param variables Project config variables.
     * @param postProcess Post process steps.
     * @param followIncludes True if includes are followed.
     * @param includeHandler The include handler to use.
     * @return The resulting AST subtree generated from the ActionScript
     * fragment.
     */
    public static ScopedBlockNode parseFragment2(
            final String fragment,
            final String path,
            final int offset,
            final int line,
            final int column,
            final Collection<ICompilerProblem> problems,
            final IWorkspace workspace,
            final IFileNodeAccumulator fileNodeAccumulator,
            final ASScope containingScope,
            final IProjectConfigVariables variables,
            final EnumSet<PostProcessStep> postProcess,
            final boolean followIncludes,
            final IncludeHandler includeHandler)
    {
        assert fragment != null;
        assert path != null;
        assert workspace != null;
        assert includeHandler != null;
        assert problems != null;

        StreamingASTokenizer tokenizer = null;
        ASParser parser = null;

        final IFileSpecification textFileSpec = new StringFileSpecification(path, fragment);

        final ScopedBlockNode container = new ScopedBlockNode();
        container.setScope(containingScope);
        try
        {
            tokenizer = StreamingASTokenizer.create(textFileSpec, includeHandler);
            tokenizer.setSourcePositionAdjustment(offset, line, column);

            final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);
            parser = new ASParser(workspace, buffer);
            parser.setFileNodeAccumulator(fileNodeAccumulator);
            parser.setFilename(path);
            parser.setProjectConfigVariables(variables);

            // Initialize depth of {...} to be positive number so that nested
            // package/class definitions can be detected.
            ((BaseASParser)parser).blockDepth = 1;
            while (buffer.LA(1) != ASTokenTypes.EOF)
                parser.directive(container, NO_END_TOKEN);
            problems.addAll(tokenizer.getTokenizationProblems());
            problems.addAll(parser.getSyntaxProblems());
            problems.addAll(container.runPostProcess(postProcess, containingScope));
        }
        catch (RecognitionException e)
        {
            parser.consumeParsingError(e);
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (TokenStreamException e)
        {
            ICompilerProblem problem = genericParserProblem(path, offset, offset, line, column);
            problems.add(problem);
        }
        catch (FileNotFoundException e)
        {
            ICompilerProblem problem = genericParserProblem(path, offset, offset, line, column);
            problems.add(problem);
        }
        finally
        {
            if (parser != null)
                parser.disconnect();
            IOUtils.closeQuietly(tokenizer);
            if (includeHandler != null && tokenizer != null)
                includeHandler.leaveFile(tokenizer.getEndOffset());
        }
        return container;
    }

    /**
     * Parser entry-point for rebuild function body nodes.
     * 
     * @param container Function body container node.
     * @param reader Function body reader.
     * @param path Source path.
     * @param blockOpenToken "{" of the function body.
     * @param problems Compiler problems.
     * @param workspace Current workspace.
     * @param fileNode AS file node.
     * @param configProcessor Configuration variables.
     */
    public static void parseFunctionBody(
            final ScopedBlockNode container,
            final Reader reader,
            final String path,
            final ASToken blockOpenToken,
            final Collection<ICompilerProblem> problems,
            final IWorkspace workspace,
            final FileNode fileNode,
            final ConfigProcessor configProcessor)
    {
        assert container != null;
        assert reader != null;
        assert path != null;
        assert blockOpenToken != null;
        assert problems != null;
        assert workspace != null;
        assert fileNode != null;
        assert configProcessor != null;

        StreamingASTokenizer tokenizer = null;
        ASParser parser = null;

        try
        {
            tokenizer = new StreamingASTokenizer();
            tokenizer.setReader(reader);
            tokenizer.setPath(path);
            tokenizer.setSourcePositionAdjustment(
                    blockOpenToken.getEnd(),
                    blockOpenToken.getLine(),
                    blockOpenToken.getColumn());

            final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);
            parser = new ASParser(workspace, buffer);
            parser.setFileNodeAccumulator(fileNode);
            parser.setFilename(path);
            parser.setConfigProcessor(configProcessor);

            // Initialize "{..}" depth to positive number so that nested "package"
            // problems can be detected.
            ((BaseASParser)parser).blockDepth = 1;
            while (buffer.LA(1) != ASTokenTypes.EOF && buffer.LA(1) != ASTokenTypes.TOKEN_BLOCK_CLOSE)
                parser.directive(container, ASTokenTypes.TOKEN_BLOCK_CLOSE);
            problems.addAll(tokenizer.getTokenizationProblems());
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (RecognitionException e)
        {
            parser.consumeParsingError(e);
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (TokenStreamException e)
        {
            problems.add(new ParserProblem(container));
        }
        finally
        {
            if (parser != null)
                parser.disconnect();
            IOUtils.closeQuietly(tokenizer);
        }
    }

    private static ICompilerProblem genericParserProblem(String path, int start, int end, int line, int column)
    {
        ISourceLocation location = new SourceLocation(path, start, end, line, column);
        return new ParserProblem(location);
    }

    /**
     * Parse a script tag with inline ActionScript. This function does not
     * trigger "enter" and "leave" event for the inlined script, because the
     * script's token offset is still in the same offset space with its parent
     * document.
     * <p>
     * Both {@code MXMLScopeBuilder} and {@code MXMLScriptNode} use
     * this parser entry point. Different post-process tasks are requested.
     */
    public static ScopedBlockNode parseInlineScript(
            final IFileNodeAccumulator fileNodeAccumulator,
            final IMXMLTextData mxmlTextData,
            final Collection<ICompilerProblem> problems,
            final ASScope containingScope,
            final IProjectConfigVariables variables,
            final IncludeHandler includeHandler,
            final EnumSet<PostProcessStep> postProcess)
    {
        assert mxmlTextData != null : "MXMLTextData can't be null.";
        assert includeHandler != null : "IncludeHandler can't be null.";
        assert problems != null : "Problem container can't be null";

        // create a file specification for the script from MXML tag data
        final String scriptSourcePath = mxmlTextData.getParent().getPath();
        final String scriptContent = mxmlTextData.getCompilableText();
        final Reader scriptReader = new StringReader(scriptContent);

        // source adjustment
        final int compilableTextStart = mxmlTextData.getCompilableTextStart();
        final int compilableTextLine = mxmlTextData.getCompilableTextLine();
        final int compilableTextColumn = mxmlTextData.getCompilableTextColumn();

        final ScopedBlockNode container = new ScopedBlockNode();

        // create lexer
        final StreamingASTokenizer tokenizer =
                StreamingASTokenizer.createForInlineScriptScopeBuilding(
                        scriptReader,
                        scriptSourcePath,
                        includeHandler,
                        compilableTextStart,
                        compilableTextLine,
                        compilableTextColumn);
        final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);

        // create parser
        final ASParser parser = new ASParser(containingScope.getWorkspace(), buffer);

        try
        {
            // parse script
            parser.setFilename(scriptSourcePath);
            parser.setProjectConfigVariables(variables);
            parser.setFileNodeAccumulator(fileNodeAccumulator);
            while (buffer.LA(1) != ASTokenTypes.EOF)
                parser.directive(container, NO_END_TOKEN);
            problems.addAll(tokenizer.getTokenizationProblems());
            problems.addAll(parser.getSyntaxProblems());

            // attach to given outer scope
            container.setScope(containingScope);

            // run post-processes
            final Collection<ICompilerProblem> postProcessProblems =
                    container.runPostProcess(postProcess, containingScope);
            problems.addAll(postProcessProblems);
        }
        catch (RecognitionException e)
        {
            parser.consumeParsingError(e);
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (TokenStreamException e)
        {
            ICompilerProblem problem = genericParserProblem(
                    scriptSourcePath,
                    mxmlTextData.getAbsoluteStart(),
                    mxmlTextData.getAbsoluteEnd(),
                    mxmlTextData.getLine(),
                    mxmlTextData.getColumn());
            problems.add(problem);
        }
        finally
        {
            parser.disconnect();
            IOUtils.closeQuietly(tokenizer);
        }
        return container;
    }

    /**
     * Parse an expression from the passed in Reader. This method can handle
     * expressions that are inline (such as the body of a Function tag) or
     * expressions that just come from a java String. For the inline case, the
     * Reader should be a SourceFragmentsReader - it is up to the caller to set
     * up the SourceFragmentsReader correctly - see
     * MXMLExpressionNodeBase.parseExpressionFromTag. For the String case, the
     * Reader can just be a StringReader. This will parse an Expression from the
     * Reader and return the ExpressionNodeBase. It will return null, if there
     * is no expression, or it can't be parsed.
     */
    public static ExpressionNodeBase parseExpression(
            final IWorkspace workspace,
            final Reader scriptReader,
            final Collection<ICompilerProblem> problems,
            final IProjectConfigVariables variables,
            final ISourceLocation location)
    {
        assert scriptReader != null : "reader can't be null.";
        assert problems != null : "Problem container can't be null";

        String sourcePath = location.getSourcePath();

        // create tokenizer
        final StreamingASTokenizer tokenizer =
                StreamingASTokenizer.createForInlineExpressionParsing(
                        scriptReader,
                        sourcePath);

        final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);

        // create parser
        final ASParser parser = new ASParser(workspace, buffer);

        ExpressionNodeBase expressionNode = null;
        try
        {
            // parse script
            parser.setFilename(sourcePath);
            parser.setProjectConfigVariables(variables);
            if (buffer.LA(1) != ASTokenTypes.EOF)
                expressionNode = parser.expression();
            problems.addAll(tokenizer.getTokenizationProblems());
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (RecognitionException e)
        {
            parser.consumeParsingError(e);
            problems.addAll(parser.getSyntaxProblems());
        }
        catch (TokenStreamException e)
        {
            final ParserProblem genericParserProblem = new ParserProblem(location);
            problems.add(genericParserProblem);
        }
        finally
        {
            parser.disconnect();
            IOUtils.closeQuietly(tokenizer);
        }
        return expressionNode;
    }

    /**
     * Parses a string into a name valid in ActionScript. If the code is not
     * valid or does not yield an identifier, null will be returned
     * 
     * @param fragment the string that contains a name
     * @return an {@link IIdentifierNode}, or null
     */
    public static final IASNode[] parseProjectConfigVariables(IWorkspace workspace, String fragment, Collection<ICompilerProblem> problems)
    {
        ScopedBlockNode container = new ScopedBlockNode();
        ASParser parser = null;

        try
        {
            IFileSpecification fileSpec = new StringFileSpecification(CONFIG_AS, fragment, 0);
            IncludeHandler includeHandler = new IncludeHandler(workspace);

            StreamingASTokenizer tokenizer = StreamingASTokenizer.create(fileSpec, includeHandler);
            tokenizer.setReader(new NonLockingStringReader(fragment));
            tokenizer.setPath(CONFIG_AS);
            tokenizer.setFollowIncludes(false);

            final IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);

            parser = new ASParser(workspace, buffer, true);
            parser.setFilename(CONFIG_AS);

            while (buffer.LA(1) != ASTokenTypes.EOF)
                parser.directive(container, NO_END_TOKEN);
            problems.addAll(tokenizer.getTokenizationProblems());
            problems.addAll(parser.getSyntaxProblems());
            return parser.getConfigProcessorResults();
        }
        catch (FileNotFoundException e)
        {
            assert false : "StringFileSpecification never raises this exception";
        }
        catch (ANTLRException e)
        {
            // Ignore any parsing errors.
        }
        catch (RuntimeException e)
        {
            String path = parser.getSourceFilePath();
            ICompilerProblem problem = (path == null) ?
                    new InternalCompilerProblem(e) :
                    new InternalCompilerProblem2(path, e, SUB_SYSTEM);
            parser.errors.add(problem);
        }
        finally
        {
            if (parser != null)
                parser.disconnect();
        }

        int n = container.getChildCount();
        IASNode[] children = new IASNode[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = container.getChild(i);
        }
        return children;
    }

    /**
     * Parses a databinding expression.
     */
    public static final IExpressionNode parseDataBinding(IWorkspace workspace, Reader reader,
            Collection<ICompilerProblem> problems)
    {
        StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(reader);
        IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);
        ASParser parser = new ASParser(workspace, buffer);
        FileNode fileNode = new FileNode(workspace);

        // Parse the databinding and build children inside the FileNode.
        parser.parseFile(fileNode, EnumSet.noneOf(PostProcessStep.class));

        // Run post-processing to calculate all offsets.
        // Without this, identifiers have the right offsets but operators don't.
        EnumSet<PostProcessStep> postProcessSteps = EnumSet.of(
                PostProcessStep.CALCULATE_OFFSETS);
        Collection<ICompilerProblem> postProcessProblems =
                fileNode.runPostProcess(postProcessSteps, null);

        problems.addAll(tokenizer.getTokenizationProblems());
        problems.addAll(parser.getSyntaxProblems());
        problems.addAll(postProcessProblems);

        int n = fileNode.getChildCount();

        // If we didn't get any children of the file node,
        // we must have parsed whitespace (or nothing).
        // An databinding like {} or { } represents the empty string.
        if (n == 0)
        {
            return new LiteralNode(LiteralType.STRING, "");
        }

        // If we got more than one child, report that the databinding expression
        // is invalid. It must be a single expression.
        else if (n > 1)
        {
            final ICompilerProblem problem = new MXMLInvalidDatabindingExpressionProblem(fileNode);
            problems.add(problem);
            return null;
        }

        IASNode firstChild = fileNode.getChild(0);

        // If we got a single child but it isn't an expression,
        // report a problem.
        if (!(firstChild instanceof IExpressionNode))
        {
            final ICompilerProblem problem = new MXMLInvalidDatabindingExpressionProblem(fileNode);
            problems.add(problem);
            return null;
        }

        // We got a single expression, so return it.
        return (IExpressionNode)firstChild;
    }

    /**
     * Our version of a token buffer that allows us to handle optional
     * semicolons, etc
     */
    protected IRepairingTokenBuffer buffer;

    /**
     * Current metadata attributes. Attributes go into this container, then are
     * pulled out when the corresponding class, function, or variable is parsed.
     */
    protected MetaTagsNode currentAttributes;

    /**
     * {@link IASParserASDocDelegate} used to track ASDoc information.
     */
    protected final IASParserASDocDelegate asDocDelegate;

    /**
     * Last token that produced an error
     */
    protected Token errorToken = null;

    /**
     * Flag to determine if we should create EmbedNodes or ignore embed metadata
     */
    protected boolean allowEmbeds = true;

    /**
     * flag that tracks whether the parser is currently inside of a class
     * definition this is "borrowed" from the old parser to deal with some ASDoc
     * functionality
     */
    protected boolean insideClass = false;

    /**
     * Flag that indicates we are parsing a pseudo file that represents the
     * config vars on the command line (or from some other project configuration
     * option)
     */
    private final boolean parsingProjectConfigVariables;

    /**
     * Cut down on object construction when we throw on the matches call
     */
    private MismatchedTokenException exceptionPool = new MismatchedTokenException(tokenNames, null, null, false);

    /**
     * Errors we've collected as we're moving forward
     */
    protected final Set<ICompilerProblem> errors = new LinkedHashSet<ICompilerProblem>();

    /**
     * This object allows the parser to store data on a given {@code FileNode}.
     */
    private IFileNodeAccumulator fileNodeAccumulator;

    /**
     * Config processor used to handle config namespace expressions
     */
    protected ConfigProcessor configProcessor;

    /**
     * Determines if we should allowe errors. Used in conditional compilation to
     * suppress blocks that are excluded
     */
    private boolean allowErrorsInContext = true;

    /**
     * True if the current input source is an ActionScript file, as opposed to
     * AS3 scripts in MXML or other synthesized source fragments.
     */
    protected DeferFunctionBody deferFunctionBody = DeferFunctionBody.DISABLED;

    /**
     * A secondary reader to capture function body text on-the-fly. This is an
     * optimization for large files in order to avoid {@link Reader#skip(long)}
     * when the function body is rebuilt from source file and start offset.
     * <p>
     * This optimization is turned off if there's an {@code include} directive
     * in the function body.
     */
    private final Reader secondaryReader;

    /**
     * Character offset into the {@link #secondaryReader}.
     */
    private int secondaryReaderPosition = 0;

    /**
     * Number of nested "packages".
     */
    private int packageDepth = 0;

    /**
     * Number of nested "blocks".
     */
    private int blockDepth = 0;

    /**
     * Number of nested "groups" in "group directives".
     */
    private int groupDepth = 0;

    /**
     * A recursive descent parsing stack of XML tags.
     */
    private final Stack<XMLTagInfo> xmlTags;

    /**
     * Create an ActionScript parser from a workspace and a token buffer.
     * 
     * @param workspace Current workspace.
     * @param buffer Token buffer.
     */
    protected BaseASParser(IWorkspace workspace, IRepairingTokenBuffer buffer)
    {
        this(workspace, buffer, false);
    }

    /**
     * This constrcutor should ONLY be used in the very special case of parsing
     * project config vars. Typically this is done as part of parsing the
     * command line. For normal case, use the two argument constructor
     * 
     * @param workspace Current workspace.
     * @param buffer Token buffer.
     * @param parsingProjectConfigVariables true for special case when we are
     * parsing not text, but a fake "file" of configuration variables.
     */
    protected BaseASParser(IWorkspace workspace, IRepairingTokenBuffer buffer, boolean parsingProjectConfigVariables)
    {
        super(new ParserSharedInputState(), 1);
        xmlTags = new Stack<XMLTagInfo>();
        this.buffer = buffer;
        configProcessor = new ConfigProcessor(workspace, this);

        // If there are no include directives in the function body, we
        // can cache the function body text to save the seeking time.
        secondaryReader = tryGetSecondaryReader(workspace, buffer);

        this.asDocDelegate = workspace.getASDocDelegate().getASParserASDocDelegate();
        this.parsingProjectConfigVariables = parsingProjectConfigVariables;
    }

    /**
     * Try to initialize {@link #secondaryReader}. If fails, this optimization
     * is not available for the current file.
     * 
     * @param workspace Current workspace.
     * @param buffer Underlying token buffer.
     * @return Initialized secondary reader or null.
     */
    private static Reader tryGetSecondaryReader(IWorkspace workspace, IRepairingTokenBuffer buffer)
    {
        if (buffer instanceof StreamingTokenBuffer)
        {
            final StreamingTokenBuffer streamingTokenBuffer = (StreamingTokenBuffer)buffer;

            // token without source path (probably from string literals
            if (streamingTokenBuffer.getSourcePath() == null)
                return null;

            // token source path doesn't exist: imaginary sources
            final String sourcePath = FilenameNormalization.normalize(streamingTokenBuffer.getSourcePath());
            if (!new File(sourcePath).isFile())
                return null;

        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
        		System.out.println("BaseASParser waiting for lock in tryGetSecondaryReader");
            // try to create a reader from file specification
            final IFileSpecification fileSpec = workspace.getFileSpecification(sourcePath);
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
        		System.out.println("BaseASParser done with lock in tryGetSecondaryReader");
            if (fileSpec != null)
            {
                try
                {
                    // success - optimization is available
                    return fileSpec.createReader();
                }
                catch (FileNotFoundException e)
                {
                    return null;
                }
            }
        }
        return null;
    }

    protected BaseASParser(TokenBuffer tokenBuf, int k)
    {
        super(tokenBuf, k);
        throw new UnsupportedOperationException();
    }

    protected BaseASParser(TokenStream lexer, int k)
    {
        super(lexer, k);
        throw new UnsupportedOperationException();
    }

    protected BaseASParser(ParserSharedInputState state, int i)
    {
        super(state, i);
        throw new UnsupportedOperationException();
    }

    protected final void addConditionalCompilationNamespace(final NamespaceNode node)
    {

        // Need to process the node even if there is an error so the node will 
        // have the location information needed to display an error.
        configProcessor.addConditionalCompilationNamespace(node);

        if (!isGlobalContext())
        {
            ICompilerProblem problem = new InvalidConfigLocationProblem(node);
            addProblem(problem);
        }
    }

    /**
     * Sets the {@link IProjectConfigVariables} to be used to support
     * conditional compilation
     * 
     * @param variables {@link IProjectConfigVariables} for the given context
     */
    public void setProjectConfigVariables(IProjectConfigVariables variables)
    {
        configProcessor.connect(variables);
    }

    /**
     * Bind the current parser to the given {@code ConfigProcessor}.
     * 
     * @param configProcessor Resolve configuration variables at parse-time.
     */
    protected final void setConfigProcessor(ConfigProcessor configProcessor)
    {
        assert configProcessor != null;
        configProcessor.setParser(this);
        this.configProcessor = configProcessor;
    }

    void addConfigConstNode(ConfigConstNode node)
    {
        configProcessor.addConfigConstNode(node);

        if (node.getKeywordNode().getKeywordId() != ASTokenTypes.TOKEN_KEYWORD_CONST)
        {
            ICompilerProblem problem = new NonConstConfigVarProblem(node.getNameExpressionNode());
            addProblem(problem);
        }
        if (!isGlobalContext())
        {
            ICompilerProblem problem = new InvalidConfigLocationProblem(node.getNameExpressionNode());
            addProblem(problem);
        }
    }

    IASNode[] getConfigProcessorResults()
    {
        if (configProcessor != null)
        {
            return configProcessor.getConfigChildren();
        }

        return new IASNode[0];
    }

    protected final IASNode evaluateConstNodeExpression(final ConfigExpressionNode node)
    {
        return configProcessor.evaluateConstNodeExpression(node);
    }

    protected final boolean isConfigNamespace(final NamespaceIdentifierNode id)
    {
        return configProcessor.isConfigNamespace(id.getName());
    }

    public Collection<ICompilerProblem> getSyntaxProblems()
    {
        return errors;
    }

    /**
     * Close the parser and release resources.
     */
    protected final void disconnect()
    {
        if (configProcessor != null)
            configProcessor.detachParser(this);
        IOUtils.closeQuietly(secondaryReader);
    }

    void setFileNodeAccumulator(IFileNodeAccumulator fileNodeAccumulator)
    {
        this.fileNodeAccumulator = fileNodeAccumulator;
    }

    @Override
    public String getSourceFilePath()
    {
        return getFilename();
    }

    protected void setAllowErrorsInContext(boolean allow)
    {
        allowErrorsInContext = allow;
    }

    @Override
    public void addProblem(ICompilerProblem problem)
    {
        if (allowErrorsInContext)
            errors.add(problem);
    }

    public void setAllowEmbeds(boolean allowEmbeds)
    {
        this.allowEmbeds = allowEmbeds;
    }

    /**
     * Assemble the tokens into a parse tree with a root FileNode.
     * 
     * @param fileNode The parser builds AST into this root {@code FileNode}.
     * @param features Post-process steps to be performed.
     */
    public void parseFile(FileNode fileNode, EnumSet<PostProcessStep> features)
    {
        currentAttributes = null;
        fileNode.setStart(0);
        fileNode.setLine(0);
        fileNode.setColumn(0);
        String sourcePath = fileNode.getSourcePath();
        setFilename(sourcePath);
        exceptionPool.fileName = sourcePath;
        exceptionPool.mismatchType = MismatchedTokenException.RANGE;
        fileNodeAccumulator = fileNode;
        try
        {
            file(fileNode);
        }
        catch (RecognitionException e)
        {
            final ASToken current = buffer.LT(1);
            if (e instanceof NoViableAltException)
            {
                addProblem(new SyntaxProblem(current));
            }
            else if (e instanceof MismatchedTokenException)
            {
                ASToken expected = new ASToken(((MismatchedTokenException)e).expecting, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "");
                addProblem(unexpectedTokenProblem(current, expected.getTokenKind()));
            }

        }
        catch (TokenStreamException e)
        {
            addProblem(new SyntaxProblem(fileNode, null));
        }
        finally
        {
            disconnect();
        }
        fileNode.processAST(features);
    }

    protected abstract void fileLevelDirectives(ContainerNode containerNode) throws RecognitionException, TokenStreamException;

    protected void encounteredImport(ImportNode importNode)
    {
        if (fileNodeAccumulator != null)
        {
            fileNodeAccumulator.addImportNode(importNode);
        }
    }

    /**
     * Parse a String of meta data from an @function, by changing a string such
     * as: <code>@Embed(source='a.png')</code> to:
     * <code>[Embed(source='a.png')]</code> and then running it through the meta
     * data parser
     * 
     * @param metadataTagText The @function text.
     * @param problems The collection of compiler problems to which this method will add problems.
     * @return MetaTagsNode or null if error parsing the meta data
     */
    public static MetaTagsNode parseAtFunction(IWorkspace workspace, String metadataTagText, String sourcePath, int start, int line, int column, Collection<ICompilerProblem> problems)
    {
        StringBuilder stringBuffer = new StringBuilder();

        stringBuffer.append("[");
        stringBuffer.append(metadataTagText.substring(1));
        stringBuffer.append("]");

        return ASParser.parseMetadata(workspace, stringBuffer.toString(), sourcePath, start, line, column, problems);
    }

    /**
     * Parse metadata tags from a string.
     * 
     * @param workspace Current workspace.
     * @param metadataContent Source text.
     * @param sourcePath Source path.
     * @param start Start offset.
     * @param line Line offset.
     * @param column Column offset.
     * @param problems Problem collection.
     * @return A {@code MetaTagsNode} containing all the parsed metadata tags.
     */
    public static MetaTagsNode parseMetadata(
            final IWorkspace workspace,
            final String metadataContent,
            final String sourcePath,
            final int start,
            final int line,
            final int column,
            final Collection<ICompilerProblem> problems)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("BaseASParser waiting for lock in parseMetadata");
        final long lastModified = workspace.getFileSpecification(sourcePath).getLastModified();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("BaseASParser done with lock in parseMetadata");
        final IFileSpecification fileSpec = new StringFileSpecification(sourcePath, metadataContent, lastModified);
        final IncludeHandler includeHandler = new IncludeHandler(workspace);
        final ASParser parser = new ASParser(workspace, (IRepairingTokenBuffer)null);

        try
        {
            final StreamingASTokenizer tokenizer = StreamingASTokenizer.create(fileSpec, includeHandler);
            tokenizer.setSourcePositionAdjustment(start, line, column);

            for (ASToken asToken = tokenizer.next(); asToken != null; asToken = tokenizer.next())
            {
                // Only metadata tokens and ASDoc tokens are relevant.
                switch (asToken.getType())
                {
                    case TOKEN_ATTRIBUTE:
                        parser.parseMetadata(asToken, problems);
                        break;
                    case TOKEN_ASDOC_COMMENT:
                        parser.asDocDelegate.setCurrentASDocToken(asToken);
                        break;
                    default:
                        // Ignore other tokens.
                        break;
                }
            }

        }
        catch (FileNotFoundException e)
        {
            // Do nothing. Source is from a string literal, so there should 
            // never be FileNotFoundException.
        }
        return parser.currentAttributes;
    }

    /**
     * Parse metadata tags with a forked sub-parser.
     * 
     * @param attributeToken Metadata token.
     * @param problems Problem collection.
     */
    protected final void parseMetadata(Token attributeToken, Collection<ICompilerProblem> problems)
    {
        assert problems != null : "Expected problem collection.";
        assert attributeToken != null : "Expected attribute token.";

        // Extract metadata tokens.
        final List<MetadataToken> metadataTokens;
        if (attributeToken instanceof MetaDataPayloadToken)
        {
            metadataTokens = ((MetaDataPayloadToken)attributeToken).getPayload();
        }
        else
        {
            final NonLockingStringReader metadataReader = new NonLockingStringReader(attributeToken.getText());
            final MetadataTokenizer tokenizer = new MetadataTokenizer(metadataReader);
            tokenizer.setAdjust(((TokenBase)attributeToken).getStart());
            metadataTokens = tokenizer.parseTokens();
        }

        // manage namespace variables 
        int i = 2;// skip the metadata type 
        while (i<metadataTokens.size()-1) {
            MetadataToken t = metadataTokens.get(i);
            if (t.getType()==MetadataTokenTypes.TOKEN_ATTR_OPERATOR_NS_QUALIFIER) {
                // get the previous and next tokens to check there types and transform them as the left and right parts of the operator
                MetadataToken prev = metadataTokens.get(i - 1);
                MetadataToken next = metadataTokens.get(i + 1);
                if (prev.getType()==MetadataTokenTypes.TOKEN_ATTR_UNKNOWN && next.getType()==MetadataTokenTypes.TOKEN_ATTR_UNKNOWN) {
                    // trasform to literal node if possible
                    NamespaceIdentifierNode nsNode = new NamespaceIdentifierNode(prev.getText());
                    nsNode.setIsConfigNamespace(true);
                    ASToken op = new ASToken(TOKEN_OPERATOR_NS_QUALIFIER, t.getStart(), t.getEnd(), t.getLine(), t.getColumn(), t.getText());
                    IdentifierNode idNode = new IdentifierNode(next.getText());
                    IASNode n = transformToNSAccessExpression(nsNode, (ASToken) op, idNode);
                    if (n instanceof LiteralNode) {
                        // replace the left, operator and right tokens by the result string token
                        t = new MetadataToken(MetadataTokenTypes.TOKEN_STRING, prev.getSourcePath(), prev.getStart(), next.getEnd(), prev.getLine(), prev.getColumn(), ((LiteralNode)n).getValue());
                        metadataTokens.remove(i+1);
                        metadataTokens.remove(i--);
                        metadataTokens.set(i, t);
                    }
                }
            }
            ++i;
        }

        // Initialize metadata parser.
        final GenericTokenStream metadataTokenStream = new GenericTokenStream(metadataTokens);
        final MetadataParser metadataParser = new MetadataParser(metadataTokenStream);
        metadataParser.setASDocDelegate(asDocDelegate.getMetadataParserASDocDelegate());

        // The parsed metadata tags will be added to this container node.
        if (currentAttributes == null)
            currentAttributes = new MetaTagsNode();

        // Parse metadata.
        try
        {
            metadataParser.meta(currentAttributes);
        }
        catch (RecognitionException e)
        {
            final ParserProblem problem = new ParserProblem((TokenBase)attributeToken);
            problems.add(problem);
        }
        catch (TokenStreamException e)
        {
            //do nothing
        }
    }

    /**
     * Determines if the current metadata is found within a pure statement
     * context, or if it is going to be bound to a definition. If the item is
     * free-floating (not about to be bound to a definition) then we set its
     * parent as the passed in container, and log an error.
     * 
     * @param attributeToken the token that contains the metadata
     * @param container the {@link ContainerNode} we will potentially add our
     * metadata to as a child
     */
    protected final void preCheckMetadata(Token attributeToken, ContainerNode container)
    {
        if (currentAttributes == null)
            return;

        final int la = LA(1);
        if (ASToken.isDefinitionKeyword(la) ||
            ASToken.isModifier(la) ||
            isConfigCondition() ||
            la == TOKEN_NAMESPACE_ANNOTATION ||
            la == TOKEN_ASDOC_COMMENT ||
            la == TOKEN_ATTRIBUTE)
            return;

        container.addItem(currentAttributes);
        final ICompilerProblem problem = new UnboundMetadataProblem((TokenBase)attributeToken);
        addProblem(problem);
        currentAttributes = null;
    }

    /**
     * Check if the look-ahead can be matched as a "ConfigCondition".
     * 
     * @return True if the following input is "ConfigCondition".
     */
    protected final boolean isConfigCondition()
    {
        return LA(1) == TOKEN_NAMESPACE_NAME &&
               LA(2) == TOKEN_OPERATOR_NS_QUALIFIER &&
               LA(3) == TOKEN_IDENTIFIER;
    }

    /**
     * Check if the look-ahead can be matched as a "XMLAttribute".
     * 
     * @return True if the following input is "XMLAttribute".
     */
    protected final boolean isXMLAttribute()
    {
        return LA(1) == TOKEN_E4X_NAME ||
               LA(1) == TOKEN_E4X_XMLNS ||
               (LA(1) == TOKEN_E4X_BINDING_OPEN && hasEqualsAfterClose());
    }

    /** 
     * See if there is an assignment right after the close of the binding expr.
     * If there is, then it is an attribute name otherwise no
     */
    private final boolean hasEqualsAfterClose()
    {
        int i = 2;
        while (true)
        {
            if (LA(i) == TOKEN_E4X_BINDING_CLOSE)
                return LA(i+1) == TOKEN_E4X_EQUALS;
            i++;
        }
    }
    
    /**
     * Stores decorations on the given variable definition. This will set any
     * collected modifiers, namespace, metadata or comment we've encountered
     * 
     * @param decoratedNode
     * @param node
     */
    protected void storeVariableDecorations(VariableNode decoratedNode, ContainerNode node, INamespaceDecorationNode namespace, List<ModifierNode> modList)
    {
        storeDecorations(decoratedNode, node, namespace, modList);
        storeEmbedDecoration(decoratedNode, decoratedNode.getMetaTags());
    }

    protected void storeEmbedDecoration(VariableNode variable, IMetaTagsNode metaTags)
    {
        if (!allowEmbeds)
            return;

        // no embed meta tag, so nothing more to do
        if (metaTags == null || !metaTags.hasTagByName(IMetaAttributeConstants.ATTRIBUTE_EMBED))
            return;

        // can't have an initial value on an embed variable
        if (variable.getAssignedValueNode() != null)
        {
            addProblem(new EmbedInitialValueProblem(variable));
            return;
        }

        // This type checking was ported from the old compiler in EmbedEvaluator.evaluate(Context, MetaDataNode)
        // Under normal circumstances type checking should be done on ITypeDefinition NOT string compares.
        // To make that happen, it should be done during codegen reduce_embed().
        String typeName = variable.getTypeName();
        if (!(IASLanguageConstants.Class.equals(typeName) || IASLanguageConstants.String.equals(typeName)))
        {
            addProblem(new EmbedUnsupportedTypeProblem(variable));
            return;
        }

        IMetaTagNode[] embedMetaTags = metaTags.getTagsByName(IMetaAttributeConstants.ATTRIBUTE_EMBED);
        if (embedMetaTags.length > 1)
        {
            addProblem(new EmbedMultipleMetaTagsProblem(variable));
            return;
        }

        EmbedNode embedNode = new EmbedNode(getFilename(), embedMetaTags[0], fileNodeAccumulator);
        variable.setAssignedValue(null, embedNode);
    }

    /**
     * Stores decorations on the given definition. This will set any collected
     * modifiers, namespace, metadata or comment we've encountered
     * 
     * @param decoratedNode
     * @param node
     */
    protected void storeDecorations(BaseDefinitionNode decoratedNode, ContainerNode node, INamespaceDecorationNode namespace, List<ModifierNode> modList)
    {
        //add the metadata
        if (currentAttributes != null)
        {
            IMetaTagNode[] embedTags = currentAttributes.getTagsByName(IMetaAttributeConstants.ATTRIBUTE_EMBED);
            assert embedTags != null;
            if (embedTags.length > 0)
            {
                // only member variables and classes can be annotated with embed data
                if (!((decoratedNode instanceof VariableNode) || (decoratedNode instanceof ClassNode)))
                {
                    for (IMetaTagNode embedTag : embedTags)
                        addProblem(new EmbedOnlyOnClassesAndVarsProblem(embedTag));
                }
            }

            decoratedNode.setMetaTags(currentAttributes);
        }
        //add modifiers
        if (modList != null)
        {
            int size = modList.size();
            for (int i = 0; i < size; i++)
            {
                ModifierNode modifierNode = modList.get(i);
                decoratedNode.addModifier(modifierNode);
            }
        }
        //set the namespace
        if (namespace != null)
            decoratedNode.setNamespace(namespace);

        final IASDocComment asDocComment = asDocDelegate.afterDefinition(decoratedNode);
        if (asDocComment != null)
            decoratedNode.setASDocComment(asDocComment);

        currentAttributes = null;
    }

    protected final boolean namespaceIsConfigNamespace(INamespaceDecorationNode node)
    {
        return node != null && node.getNamespaceDecorationKind() == NamespaceDecorationKind.CONFIG;
    }

    protected void logMultipleConfigNamespaceDecorationsError(NodeBase source)
    {
        ICompilerProblem problem = new MultipleConfigNamespaceDecorationsProblem(source);
        addProblem(problem);
    }

    /**
     * Check if the namespace conflicts with a config namespace
     * 
     * @param ns the NamespaceNode to check
     */
    protected void checkNamespaceDefinition(NamespaceNode ns)
    {
        if (configProcessor.isConfigNamespace(ns.getName()))
            addProblem(new ShadowedConfigNamespaceProblem(ns, ns.getName()));
    }

    /**
     * Create AST for various types of namespace access expressions.
     * 
     * @param left left-hand side of {@code ::} is the namespace expression
     * @param op {@code ::} token
     * @param right right-hand side of {@code ::} is the variable
     * @return AST for the namespace access expressions.
     */
    protected final ExpressionNodeBase transformToNSAccessExpression(ExpressionNodeBase left, ASToken op, ExpressionNodeBase right)
    {
        checkForChainedNamespaceQualifierProblem(op, right);

        final ExpressionNodeBase result;

        if (left instanceof FullNameNode)
        {
            // Left-hand side is a "full name", for example: "ns1::ns2::member".
            // Then convert the "full name" node into a namespace qualifier, 
            // and associate it with the variable.
            final QualifiedNamespaceExpressionNode qualifier = new QualifiedNamespaceExpressionNode((FullNameNode)left);
            result = new NamespaceAccessExpressionNode(qualifier, op, right);
        }
        else if (left instanceof MemberAccessExpressionNode)
        {
            // In this case, we need to turn the right side into the full qualified bit.
            IExpressionNode maRight = ((MemberAccessExpressionNode)left).getRightOperandNode();
            if (maRight instanceof NamespaceIdentifierNode)
            {
                ((MemberAccessExpressionNode)left).setRightOperandNode(new NamespaceAccessExpressionNode((NamespaceIdentifierNode)maRight, op, right));
            }
            else if (maRight instanceof IdentifierNode)
            {
                ((MemberAccessExpressionNode)left).setRightOperandNode(new NamespaceAccessExpressionNode(new NamespaceIdentifierNode((IdentifierNode)maRight), op, right));
                //this is the @ case, so @x::y
            }
            else if (maRight instanceof UnaryOperatorNodeBase && ((UnaryOperatorNodeBase)maRight).getOperator() == OperatorType.AT)
            {
                ((UnaryOperatorNodeBase)maRight).setExpression(new NamespaceAccessExpressionNode((IdentifierNode)((UnaryOperatorNodeBase)maRight).getOperandNode(), op, right));
            }
            if (maRight.hasParenthesis() && maRight instanceof MemberAccessExpressionNode)
            {
                ((MemberAccessExpressionNode)left).setRightOperandNode(new NamespaceAccessExpressionNode(new QualifiedNamespaceExpressionNode((MemberAccessExpressionNode)maRight), op, right));
            }
            result = left;
        }
        else if (left instanceof NamespaceIdentifierNode &&
                 right instanceof IdentifierNode &&
                 ((NamespaceIdentifierNode)left).getNamespaceDecorationKind() == NamespaceDecorationKind.CONFIG)
        {
            // Check to see if this is a "configCondition".  
            final ConfigExpressionNode cn = new ConfigExpressionNode(
                    (NamespaceIdentifierNode)left,
                    (ASToken)op,
                    (IdentifierNode)right);
            IASNode possibleResult = evaluateConstNodeExpression(cn);
            //it's possible for evaluateConstNodeExpression() to return null
            //if that happens, fall back to the same behavior as the final
            //else to avoid a null reference exception -JT
            if (possibleResult != null)
            {
                result = (ExpressionNodeBase) possibleResult;
            }
            else
            {
                result = new NamespaceAccessExpressionNode(left, op, right);
            }
        }
        else
        {
            result = new NamespaceAccessExpressionNode(left, op, right);
        }

        return result;
    }

    /**
     * Check for syntax error of chained namespace qualifiers:
     * 
     * <pre>
     * ns1::ns2::ns3::foo = 10;
     * </pre>
     */
    protected final void checkForChainedNamespaceQualifierProblem(ASToken nsAccessOp, ExpressionNodeBase right)
    {
        if (nsAccessOp != null &&
            right != null &&
            right.getNodeID() == ASTNodeID.NamespaceIdentifierID)
        {
            final ICompilerProblem problem = new UnexpectedTokenProblem(nsAccessOp, ASTokenKind.IDENTIFIER);
            addProblem(problem);
        }
    }

    /**
     * Set the offsets of an empty BlockNode generated from a {} token.
     * 
     * @param blockToken {} token
     * @param block BlockNode corresponding to that token
     */
    protected void setOffsetsOfEmptyBlock(IASToken blockToken, BlockNode block)
    {
        if (blockToken.isImplicit())
        {
            block.span((Token)blockToken);
        }
        else
        {
            block.span(blockToken.getStart() + 1, blockToken.getStart() + 1, blockToken.getLine(), blockToken.getColumn(), blockToken.getLine(), blockToken.getColumn());
        }
    }

    protected final void disableSemicolonInsertion()
    {
        buffer.setEnableSemicolonInsertion(false);
    }

    protected final void enableSemicolonInsertion()
    {
        buffer.setEnableSemicolonInsertion(true);
    }

    /**
     * Create a syntax error or warning based on the
     * {@code RecognitionException}.
     * 
     * @param ex ANTLR-generated parsing exception.
     * @param endToken The expected end token for the currently parsing
     * fragment. It is used to determine the compiler problem type to create.
     */
    protected final void consumeParsingError(RecognitionException ex, int endToken)
    {
        // Skip over inserted semicolon, because the fix-up token shouldn't 
        // appear in the error message.
        final ASToken current = buffer.lookAheadSkipInsertedSemicolon();
        final ICompilerProblem syntaxProblem;
        if (ex instanceof MismatchedTokenException)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
        		System.out.println("BaseASParser waiting for lock for typeToKind");
            final ASTokenKind expectedKind = ASToken.typeToKind(((MismatchedTokenException)ex).expecting);
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
        		System.out.println("BaseASParser done with lock for typeToKind");
            syntaxProblem = unexpectedTokenProblem(current, expectedKind);
        }
        else if (endToken != NO_END_TOKEN)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
        		System.out.println("BaseASParser waiting for lock for typeToKind");
            final ASTokenKind expectedKind = ASToken.typeToKind(endToken);
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASTOKEN) == CompilerDiagnosticsConstants.ASTOKEN)
        		System.out.println("BaseASParser done with lock for typeToKind");
            syntaxProblem = unexpectedTokenProblem(current, expectedKind);
        }
        else
        {
            final ASToken errorToken = current.getType() == EOF ? buffer.previous() : current;
            syntaxProblem = new SyntaxProblem(errorToken);
        }

        addProblem(syntaxProblem);
    }

    /**
     * @see #consumeParsingError(RecognitionException, int)
     */
    protected final void consumeParsingError(RecognitionException ex)
    {
        consumeParsingError(ex, NO_END_TOKEN);
    }

    /**
     * Force the parser to report a {@code MismatchedTokenException} when failed
     * to parse an identifier. The reason is that an identifier can either be an
     * {@code IDENTIFIER} token or one of the "contextual reserved words" like
     * {@code namespace}. Since we always want the error message to be
     * "Expected ... but got ....", we convert the exception here.
     * 
     * @param ex {@code NoViableAltException} thrown in
     * {@link ASParser#identifier()}.
     * @return missing identifier
     */
    protected IdentifierNode expectingIdentifier(NoViableAltException ex)
    {
        final MismatchedTokenException mismatchedTokenException = new MismatchedTokenException(
                new String[] {"IDENTIFIER"},
                ex.token,
                ASTokenTypes.TOKEN_IDENTIFIER,
                false,
                ex.fileName);
        return handleMissingIdentifier(mismatchedTokenException);
    }

    /**
     * handles a case where we expected an identifier but our production
     * produced an error, while we were trying to produce a short name. If tree
     * fixing is enabled, this will return a new identifier to make the tree
     * "correct"
     * 
     * @param ex the exception that was thrown
     * @return an {@link IdentifierNode} or null if tree fixing is not turned on
     */
    protected IdentifierNode handleMissingIdentifier(RecognitionException ex)
    {
        consumeParsingError(ex); //we don't want to drop a semicolon here
        //now let's guard against the case where the very next token is an identifier.
        //if we produce an identifier here, everything downstream might fail
        final IdentifierNode node;
        ASToken current = buffer.previous();
        ASToken la2 = LT(1 + 1);
        ASToken la1 = LT(1);

        if (current.getType() == ASTokenTypes.TOKEN_RESERVED_WORD_EXTENDS)
        {
            // Fix for CMP-1087: the following two branches are too greedy.
            // i.e. 
            //   public class T extends   implements MyInterface
            //                          ^
            // The "extends" keyword expects an identifier. However, instead of 
            // reporting the missing identifier and continue with "implements", 
            // the following recovery logic throws away "implements" keyword and
            // sends back "MyInterface".
            node = IdentifierNode.createEmptyIdentifierNodeAfterToken(current);
        }
        else if (la2.getType() == ASTokenTypes.TOKEN_IDENTIFIER && la2.getLine() == current.getLine())
        {
            //let's make sure this is on the same line, avoiding possibly going past the end of the statement
            //since this is all repair code anyway, this produces a much "saner" repaired tree
            ASToken token = LT(1 + 1);
            node = new IdentifierNode(token.getText(), (IASToken)token);
            consume(); //consume error token
            consume(); //act as match() for identifier, which consumes it
        }
        else if (la1.isKeywordOrContextualReservedWord() && la1.getLine() == current.getLine())
        {
            // If it's a keyword, repair by making an identifier node with the text of the keyword
            // This makes a more sensible tree - the user may be in the middle of typing an identifier that
            // starts with a keyword.  They probably meant "f.is" rather than "(f.)is" for example 
            node = new IdentifierNode(la1.getText(), (IASToken)la1);
            consume();
        }
        else
        {
            node = IdentifierNode.createEmptyIdentifierNodeAfterToken(current);
        }

        return node;
    }

    /**
     * handles a case where we expected an identifier but our production
     * produced an error, while we were trying to produce a dotted name. If tree
     * fixing is enabled, this will return a new identifier to make the tree
     * "correct"
     * 
     * @param ex the exception that was thrown
     * @return an {@link IdentifierNode} or null if tree fixing is not turned on
     */
    protected ExpressionNodeBase handleMissingIdentifier(RecognitionException ex, ExpressionNodeBase n)
    {
        if (n instanceof FullNameNode)
        {
            ExpressionNodeBase temp = handleMissingIdentifier(ex);
            if (temp != null)
            {
                ((FullNameNode)n).setRightOperandNode(temp);
            }
            return n;
        }
        handleParsingError(ex);
        return n;
    }

    /**
     * Logs a syntax error against the given token
     * 
     * @param badToken the token that caused the syntax error
     */
    protected final void logSyntaxError(ASToken badToken)
    {
        addProblem(new SyntaxProblem(badToken));
    }

    /**
     * Called by the parser to handle specific error cases we come across. If
     * the error handler can fix the error to potentially yield a successful
     * production then true is returned by this method. This method may insert
     * tokens into the token stream, or change the types of tokens that produced
     * the error.
     * 
     * @param ex the exception thrown by the parser
     * @param endToken expected end token
     * @return true if this error case can potentially be fixed
     */
    protected boolean handleParsingError(final RecognitionException ex, final int endToken)
    {
        final ASToken current = buffer.LT(1);

        // This variable will be assigned to "fErrorToken" after the error 
        // recovery. We don't want inserted "virtual semicolons" to be come 
        // an "error token", because it generates confusing syntax errors.
        final ASToken errorToken;
        if (current.getType() == TOKEN_SEMICOLON)
            errorToken = buffer.lookAheadSkipInsertedSemicolon();
        else
            errorToken = current;

        final boolean result = handleParsingError(ex, current, errorToken, endToken);
        this.errorToken = errorToken;
        return result;
    }

    /**
     * @see #handleParsingError(RecognitionException, int)
     */
    protected boolean handleParsingError(RecognitionException ex)
    {
        return handleParsingError(ex, NO_END_TOKEN);
    }

    private boolean handleParsingError(final RecognitionException ex, final ASToken current, final ASToken errorToken, final int endToken)
    {
        // Ignore ASDoc problems.
        if (current.getType() == ASTokenTypes.TOKEN_ASDOC_COMMENT)
        {
            consume();
            return true;
        }

        // Don't log an error if we've seen this token already.
        if (errorToken != this.errorToken)
            consumeParsingError(ex, endToken);

        return recoverFromRecognitionException(ex, current);
    }

    /**
     * Generic routine to recover from an {@code RecognitionException}.
     * 
     * @param ex ANTLR-generated parsing exception.
     * @param nextToken Next token.
     * @return True if error recovery succeeded.
     */
    private boolean recoverFromRecognitionException(final RecognitionException ex, final ASToken nextToken)
    {
        // Don't recover from EOF, because an "unexpected EOF" problem should
        // have been logged already.
        if (nextToken.getType() == EOF)
            return false;

        // Same token is producing an error, consume it.
        if (nextToken == errorToken)
        {
            consume();
            return true;
        }

        /*
         * if the token we had is a keyword, there is a chance that the user
         * could be in the middle of typing the name of an identifier that
         * starts with a keyword change it to an identifier, don't consume and
         * see if the parser can recover and turn it into a name the error,
         * however, will still be logged.
         */
        if (nextToken.isKeywordOrContextualReservedWord())
        {
            /*
             * There are a few cases where we don't want this to occur however.
             * Most notably, if the user is missing a close bracket or paren,
             * assume they are closing a structure and just return without
             * changing the type. Both of these can occur when adding metadata
             */
            if (ex instanceof MismatchedTokenException)
            {
                switch (((MismatchedTokenException)ex).expecting)
                {
                    case ASTokenTypes.TOKEN_SQUARE_CLOSE:
                    case ASTokenTypes.TOKEN_PAREN_CLOSE:
                        return true;
                }
            }
            nextToken.setType(ASTokenTypes.TOKEN_IDENTIFIER);
            return true;
        }

        if (nextToken.isOpenToken())
        {
            // Don't have the inserted semicolon be the next token we are 
            // looking for, since we're opening a statement.
            buffer.insertSemicolon(false);
        }
        else if (ASToken.isCloseToken(nextToken.getType()))
        {
            // Closed a block, semicolon will be matched later.
            buffer.insertSemicolon(true);
        }
        else if (nextToken.getType() == ASTokenTypes.TOKEN_SEMICOLON)
        {
            // Our semicolon isn't valid here, so don't bother inserting 
            // another one.
            consume();
        }
        else
        {
            buffer.insertSemicolon(true);
        }

        return true;
    }

    protected void endContainerAtError(RecognitionException ex, NodeBase node)
    {
        Token endToken = null;
        if (ex instanceof NoViableAltException)
        {
            endToken = ((NoViableAltException)ex).token;
        }
        else if (ex instanceof MismatchedTokenException)
        {
            endToken = ((MismatchedTokenException)ex).token;
        }
        if (endToken == null || endToken.getType() == ASTokenTypes.EOF)
        {
            endToken = buffer.previous();
        }
        node.endBefore(endToken);
    }

    @Override
    public final void consume()
    {
        buffer.consume();
    }

    /**
     * Match optional semicolon.
     * <p>
     * This method will report a {@link CanNotInsertSemicolonProblem} if a
     * "virtual semicolon" is expected but failed to be inserted, except when
     * there's already a syntax error on the same line, because the preceding
     * syntax error might early-terminate a statement, making the parser to
     * expect a optional semicolon.
     * <p>
     * It's not "wrong" to always report the semicolon problem. However, it
     * would make too much "noise" on the console, and it would break almost all
     * negative ASC tests.
     * 
     * @return True if optional semicolon is matched.
     * @see IRepairingTokenBuffer#matchOptionalSemicolon()
     */
    protected boolean matchOptionalSemicolon()
    {
        final boolean success = buffer.matchOptionalSemicolon();

        if (!success)
        {
            final ICompilerProblem lastError = Iterables.getLast(errors, null);
            if (lastError == null || lastError.getLine() < buffer.previous().getLine())
            {
                addProblem(new CanNotInsertSemicolonProblem(buffer.LT(1)));
            }
        }

        return success;
    }

    /**
     * An optional function body can either be a function block or a semicolon
     * (virtual semicolon). If neither is matched, a
     * {@link MissingLeftBraceBeforeFunctionBodyProblem} occurs. However, we
     * only report the problem if the function definition parsing doesn't have
     * other syntax issues so far.
     */
    protected void reportFunctionBodyMissingLeftBraceProblem()
    {
        final ICompilerProblem lastError = Iterables.getLast(errors, null);
        if (lastError == null || lastError.getLine() < buffer.previous().getLine())
        {
            final ICompilerProblem problem = new MissingLeftBraceBeforeFunctionBodyProblem(buffer.LT(1));
            addProblem(problem);
        }
    }

    /**
     * Consume until one of the following tokens:
     * <ul>
     * <li>keyword</li>
     * <li>identifier</li>
     * <li>EOF</li>
     * <li>metadata tag</li>
     * <li>ASDoc comment</li>
     * <li>token type of the given exit condition</li>
     * </ul>
     * 
     * @param exitCondition Stop if the next token type matches the exit
     * condition.
     */
    protected void consumeUntilKeywordOrIdentifier(int exitCondition)
    {
        consumeUntilKeywordOr(
                ASTokenTypes.TOKEN_IDENTIFIER,
                ASTokenTypes.TOKEN_ATTRIBUTE,
                ASTokenTypes.TOKEN_ASDOC_COMMENT,
                exitCondition);
    }

    /**
     * Consume until a keyword, EOF or specified token types.
     * 
     * @param types Stop if the next token's type is one of these.
     */
    protected void consumeUntilKeywordOr(final Integer... types)
    {
        final ImmutableSet<Integer> stopSet = new ImmutableSet.Builder<Integer>()
                .add(types)
                .add(ASTokenTypes.EOF)
                .build();

        while (!stopSet.contains(buffer.LA(1)) && !LT(1).isKeywordOrContextualReservedWord())
        {
            consume();
        }
    }

    @Override
    public final void match(final int t) throws MismatchedTokenException, TokenStreamException
    {
        final int la = LA(1);
        if (la != t)
        {
            exceptionPool.expecting = t;
            exceptionPool.token = LT(1);
            throw exceptionPool;
        }

        // Only update the "block depth" tracker when the parser is not in 
        // syntactic predicates.
        if (inputState.guessing == 0)
        {
            switch (la)
            {
                case TOKEN_BLOCK_OPEN:
                    blockDepth++;
                    break;
                case TOKEN_BLOCK_CLOSE:
                    blockDepth = blockDepth > 0 ? blockDepth - 1 : 0;
                    break;
                default:
                    // Ignore other tokens.
                    break;
            }
        }

        consume();
    }

    @Override
    public final int LA(final int i)
    {
        return buffer.LA(i);
    }

    @Override
    public final ASToken LT(final int i)
    {
        return buffer.LT(i);
    }

    @Override
    public final int mark()
    {
        return buffer.mark();
    }

    @Override
    public final void rewind(final int pos)
    {
        buffer.rewind(pos);
    }

    protected static enum ExpressionMode
    {
        normal, noIn, e4x
    }

    /**
     * Binary precedence table used for expression parsing optimization.
     */
    private static final ImmutableMap<Integer, Integer> BINARY_PRECEDENCE =
            new ImmutableMap.Builder<Integer, Integer>()
                    .put(TOKEN_COMMA, 1)
                    .put(TOKEN_OPERATOR_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_PLUS_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_MINUS_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_DIVISION_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_MODULO_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, 2)
                    .put(TOKEN_OPERATOR_TERNARY, 3)
                    .put(TOKEN_OPERATOR_LOGICAL_OR, 4)
                    .put(TOKEN_OPERATOR_LOGICAL_AND, 5)
                    .put(TOKEN_OPERATOR_BITWISE_OR, 6)
                    .put(TOKEN_OPERATOR_BITWISE_XOR, 7)
                    .put(TOKEN_OPERATOR_BITWISE_AND, 8)
                    .put(TOKEN_OPERATOR_EQUAL, 9)
                    .put(TOKEN_OPERATOR_NOT_EQUAL, 9)
                    .put(TOKEN_OPERATOR_STRICT_EQUAL, 9)
                    .put(TOKEN_OPERATOR_STRICT_NOT_EQUAL, 9)
                    .put(TOKEN_OPERATOR_GREATER_THAN, 10)
                    .put(TOKEN_OPERATOR_GREATER_THAN_EQUALS, 10)
                    .put(TOKEN_OPERATOR_LESS_THAN, 10)
                    .put(TOKEN_OPERATOR_LESS_THAN_EQUALS, 10)
                    .put(TOKEN_KEYWORD_INSTANCEOF, 10)
                    .put(TOKEN_KEYWORD_IS, 10)
                    .put(TOKEN_KEYWORD_AS, 10)
                    .put(TOKEN_KEYWORD_IN, 10)
                    .put(TOKEN_OPERATOR_BITWISE_LEFT_SHIFT, 11)
                    .put(TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT, 11)
                    .put(TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT, 11)
                    .put(TOKEN_OPERATOR_MINUS, 12)
                    .put(TOKEN_OPERATOR_PLUS, 12)
                    .put(TOKEN_OPERATOR_DIVISION, 13)
                    .put(TOKEN_OPERATOR_MODULO, 13)
                    .put(TOKEN_OPERATOR_STAR, 13)
                    .build();

    /**
     * Get the precedence of the given operator token.
     * 
     * @param op Operator token.
     * @return Precedence of the operator token.
     */
    private final int precedence(final ASToken op)
    {
        // When processing IN in a for-loop, drop precedence to 0, so that operator precedence parsing halts.
        if (expressionMode == ExpressionMode.noIn && op.getType() == TOKEN_KEYWORD_IN)
            return 0;

        final Integer precedence = BINARY_PRECEDENCE.get(op.getType());
        if (precedence != null)
            return precedence;
        else
            return 0;
    }

    /**
     * Operator precedence parser. Refer to Vaughan Pratt,
     * "top down operator precedence parsing" or Dijkstra's shunting yard
     * algorithm, or precedence climbing.
     */
    protected final ExpressionNodeBase precedenceParseExpression(int prec) throws RecognitionException, TokenStreamException
    {
        ExpressionNodeBase result = unaryExpr();

        ASToken op = LT(1);
        int p1 = precedence(op);
        int p2 = p1;

        for (; p1 >= prec; p1--)
        {
            while (p2 == p1)
            {
                consume();

                ExpressionNodeBase t = precedenceParseExpression(p1 + 1); // parse any sub expressions that are of higher precedence
                result = (ExpressionNodeBase)BinaryOperatorNodeBase.create(op, result, t);
                op = LT(1);
                p2 = precedence(op);
            }
        }
        return result;
    }

    /**
     * Expression mode of the current parsing state. {@code ASParser} updates
     * this field according to its context.
     */
    protected ExpressionMode expressionMode = ExpressionMode.normal;

    /**
     * Provides method signature to {@link ASParser#unaryExpr()} so that
     * {@link #precedenceParseExpression()} can call it.
     */
    abstract ExpressionNodeBase unaryExpr() throws RecognitionException, TokenStreamException;

    /**
     * True if a {@link ExtraCharactersAfterEndOfProgramProblem} was logged
     * already.
     */
    private boolean extraCharacterAfterEndOfProgramProblemLogged = false;

    /**
     * Call this method when extra characters were found after the end of the
     * program. The {@link ExtraCharactersAfterEndOfProgramProblem} is only
     * logged when it's the first error in the current file. Even though this
     * method might be called multiple times because of the parser recovery
     * logic, such problem can only be reported once.
     * 
     * @param token Token recognized as "extra characters".
     */
    protected void foundExtraCharacterAfterEndOfProgram(ASToken token)
    {
        if (!extraCharacterAfterEndOfProgramProblemLogged && errors.isEmpty())
        {
            extraCharacterAfterEndOfProgramProblemLogged = true;
            addProblem(new ExtraCharactersAfterEndOfProgramProblem(token));
        }
    }

    /**
     * Call this method in the grammar once a "restricted token" is matched.
     * According to ECMA-262 Section 7.9.1:
     * <p>
     * <blockquote> When, as the program is parsed from left to right, a token
     * is encountered that is allowed by some production of the grammar, but the
     * production is a restricted production and the token would be the first
     * token for a terminal or nonterminal immediately following the annotation
     * [no LineTerminator here] within the restricted production (and therefore
     * such a token is called a restricted token), and the restricted token is
     * separated from the previous token by at least one LineTerminator, then a
     * semicolon is automatically inserted before the restricted token.
     * </blockquote>
     * <p>
     * These are the only restricted productions in ECMA grammar:
     * 
     * <pre>
     * Expression [no LineTerminator] ++
     * Expression [no LineTerminator] --
     * continue [no LineTerminator] Identifier
     * break [no LineTerminator] Identifier
     * return [no LineTerminator] Expression
     * throw [no LineTerminator] Expression
     * </pre>
     * 
     * As a result, this method should be called after these tokens are matched:
     * {@code continue, break, return, throw}.
     * 
     * @param current The current token should be the restricted token.
     * @return True if the semicolon is inserted.
     * @note See ECMA 262 section 7.9.1 for details about semicolon insertion rules.
     */
    protected boolean afterRestrictedToken(final ASToken current)
    {
        assert RESTRICTED_TOKEN_TYPES.contains(current.getType()) : "Unexpected restricted token type: " + current.getTypeString();
        final ASToken nextToken = LT(1);

        // If the next token is a semicolon, even when there's a new-line, we 
        // needn't insert a virtual semicolon.
        if (nextToken != null &&
            nextToken.getLine() > current.getLine() &&
            nextToken.getType() != TOKEN_SEMICOLON)
            return buffer.insertSemicolon(true);
        else
            return false;
    }

    /**
     * If any of these tokens is followed by a new line, we must insert a
     * semicolon after it. The set is only used by the assertion in
     * {@link #afterRestrictedToken(ASToken)}.
     */
    private static final ImmutableSet<Integer> RESTRICTED_TOKEN_TYPES = ImmutableSet.of(
            ASTokenTypes.TOKEN_KEYWORD_RETURN,
            ASTokenTypes.TOKEN_KEYWORD_CONTINUE,
            ASTokenTypes.TOKEN_KEYWORD_BREAK,
            ASTokenTypes.TOKEN_KEYWORD_THROW);

    /**
     * See Javadoc for {@link #afterRestrictedToken(ASToken)} for details about
     * "restricted token" and semicolon insertion.
     * 
     * @param nextToken The next token should be the restricted token.
     * @return True if the semicolon is inserted.
     * @see #afterRestrictedToken(ASToken)
     */
    protected boolean beforeRestrictedToken(final ASToken nextToken)
    {
        // The next token should be the restricted token ++ or --.
        final ASToken current = buffer.previous();
        assert nextToken != null : "token can't be null";
        assert nextToken.getType() == ASTokenTypes.TOKEN_OPERATOR_INCREMENT ||
               nextToken.getType() == ASTokenTypes.TOKEN_OPERATOR_DECREMENT : "Unexpected restricted token type: " + nextToken.getTypeString();

        // If the next token is a semicolon, even when there's a new-line, we 
        // needn't insert a virtual semicolon.
        if (nextToken != null &&
            nextToken.getLine() > current.getLine() &&
            nextToken.getType() != TOKEN_SEMICOLON)
            return buffer.insertSemicolon(false);
        else
            return false;
    }

    /**
     * Log an "unexpected token" problem.
     * 
     * @param site offending token
     * @param expectedKind expected token kind
     * @return {@link UnexpectedTokenProblem} instance.
     */
    protected final ICompilerProblem unexpectedTokenProblem(ASToken site, ASTokenKind expectedKind)
    {
        if (site.getType() == EOF)
        {
            // EOF token is synthesized. It doesn't have reasonable source location.
            // Use the last token instead.
            return new UnexpectedEOFProblem(buffer.previous(), expectedKind);
        }
        else
        {
            return new UnexpectedTokenProblem(site, expectedKind);
        }
    }

    /**
     * @see ASL syntax spec chapter 4.1
     */
    private static final ImmutableSet<String> RESERVED_NAMESPACE_ATTRIBUTES =
            ImmutableSet.of("private", "public", "protected", "internal");

    /**
     * Make sure there's at most one namespace attribute found. Report syntax
     * problem otherwise.
     * 
     * @param namespaceAttributes A list of namespace attributes.
     * @note See AS3 syntax specification chapter 8 - Enforcement of Syntactic
     * Restrictions.
     */
    protected void verifyNamespaceAttributes(final List<INamespaceDecorationNode> namespaceAttributes)
    {
        if (namespaceAttributes.size() < 2)
            return;

        final List<INamespaceDecorationNode> reservedNamespaceAttributes = new ArrayList<INamespaceDecorationNode>();
        final List<INamespaceDecorationNode> userDefinedNamespaceAttributes = new ArrayList<INamespaceDecorationNode>();

        for (final INamespaceDecorationNode namespaceAttribute : namespaceAttributes)
        {
            final String namespaceName = namespaceAttribute.getName();
            if (RESERVED_NAMESPACE_ATTRIBUTES.contains(namespaceName))
                reservedNamespaceAttributes.add(namespaceAttribute);
            else
                userDefinedNamespaceAttributes.add(namespaceAttribute);
        }

        if (reservedNamespaceAttributes.size() > 1)
        {
            final MultipleReservedNamespaceAttributesProblem problem =
                    new MultipleReservedNamespaceAttributesProblem(reservedNamespaceAttributes.get(0));
            addProblem(problem);
        }

        if (userDefinedNamespaceAttributes.size() > 1)
        {
            final MultipleNamespaceAttributesProblem problem =
                    new MultipleNamespaceAttributesProblem(userDefinedNamespaceAttributes.get(0));
            addProblem(problem);
        }

        if (!userDefinedNamespaceAttributes.isEmpty() && !reservedNamespaceAttributes.isEmpty())
        {
            final NamespaceAttributeNotAllowedProblem problem =
                    new NamespaceAttributeNotAllowedProblem(userDefinedNamespaceAttributes.get(0));
            addProblem(problem);
        }
    }

    private static final ImmutableSet<String> CONTEXTUAL_RESERVED_KEYWORD_MODIFIERS =
            ImmutableSet.of("dynamic", "final", "native", "static", "override", "abstract");

    /**
     * Recover from {@link CanNotInsertSemicolonProblem} after an expression
     * statement. Such error pattern is usually a sign of invalid code being
     * parsed as expressions.
     * <p>
     * <h2>Recover from contextual keywords</h2> When there's syntax errors, the
     * token transformation logic in {@link StreamingASTokenizer} might fail.
     * For example, a contextual reserved keyword like "static" is labeled as
     * "identifier" instead of "modifier" when the token following "static"
     * doesn't start a definition. As a result "static" is parsed as a single
     * variable "expressionStatement", and the following content will be
     * reported with a {@link CanNotInsertSemicolonProblem}.
     * </p>
     * <p>
     * Since we want the error reporting to be user-friendly, we need to detect
     * such cases and override the compiler problems. The pattern is simple: an
     * {@link ExpressionNodeBase} of type {@link IdentifierNode} and it's
     * identifier name is one of the contextual reserved keywords:
     * {@code dynamic}, {@code final}, {@code native}, {@code static} and
     * {@code override}.
     * </p>
     * <p>
     * <h2>Recover from invalid labels</h2> When the next offending token is
     * colon, it means the parsed expression isn't a valid label identifier.
     * </p>
     * 
     * @param e The AST created for the parsed expression.
     */
    protected final void recoverFromExpressionStatementMissingSemicolon(final ExpressionNodeBase e)
    {
        final ASToken lt = buffer.LT(1);
        ICompilerProblem problem = null;
        if (e instanceof IdentifierNode)
        {
            final String name = ((IdentifierNode)e).getName();
            if (CONTEXTUAL_RESERVED_KEYWORD_MODIFIERS.contains(name))
            {
                if (lt.getType() == ASTokenTypes.TOKEN_KEYWORD_PACKAGE)
                {
                    problem = new AttributesNotAllowedOnPackageDefinitionProblem(lt);
                }
                else
                {
                    problem = new ExpectDefinitionKeywordAfterAttributeProblem(name, lt);
                }
            }
        }
        else if (lt.getType() == TOKEN_COLON)
        {
            problem = new InvalidLabelProblem(lt);
            consume(); // drop the colon
        }

        if (problem != null)
        {
            // replace the syntax problem
            final ICompilerProblem lastError = Iterables.getLast(errors);
            errors.remove(lastError);
            errors.add(problem);
        }
    }

    /**
     * Traps erroneous syntax like:
     * 
     * <pre>
     *     innerLabel: namespace ns1;
     * </pre>
     * 
     * Although the {@link InvalidAttributeProblem} doesn't make much sense in
     * this context, we are just being compatible with the old ASC's behavior.
     * 
     * @param offendingNSToken The offending "namespace" token after the label
     * and colon.
     */
    protected void trapInvalidSubstatement(ASToken offendingNSToken)
    {
        final InvalidAttributeProblem problem = new InvalidAttributeProblem(offendingNSToken);
        addProblem(problem);
    }

    /**
     * Traps erroneous syntax like:
     * 
     * <pre>
     * package foo 
     * {
     *     ns1 private class T {}
     * }
     * </pre>
     * 
     * @param offendingNSToken The unexpected namespace name token.
     */
    protected void trapInvalidNamespaceAttribute(ASToken offendingNSToken)
    {
        final NamespaceAttributeNotAllowedProblem problem =
                new NamespaceAttributeNotAllowedProblem(offendingNSToken);
        addProblem(problem);
    }

    /**
     * Evaluate configuration variable result.
     * 
     * @param configNamespace The configuration namespace.
     * @param opToken The operator token. Always "::".
     * @param configVar The unqualified configuration variable name.
     * @return True if the qualified configuration variable evaluates to true.
     */
    protected boolean evaluateConfigurationVariable(
            final IdentifierNode configNamespace,
            final ASToken opToken,
            final IdentifierNode configVar)
    {
        final ConfigExpressionNode configExpression = new ConfigExpressionNode(
                configNamespace,
                opToken,
                configVar);

        final Object value = configProcessor.evaluateConstNodeExpressionToJavaObject(configExpression);
        return value == null ? false : ECMASupport.toBoolean(value);
    }

    /**
     * Report unexpected token problem.
     * 
     * @param token Offending token.
     */
    protected final void reportUnexpectedTokenProblem(final ASToken token)
    {
        addProblem(new SyntaxProblem(token));
    }

    /**
     * Consume all the tokens in a function body, excluding the open curly and
     * close curly braces.
     * <p>
     * In order to allow lazy-parsing of the skipped function bodies, the
     * contents need to be cached. We can't cache all the tokens because they
     * take up huge amount of memory.
     * <p>
     * On the other hand, we can't cache the text of the function body by
     * accessing the underlying {@code Reader} directly, because it isn't
     * guaranteed to point to the start of the function block when then the
     * parser sees the function body due to the possible "look-ahead" operations
     * triggered by the tokenizer.
     * <p>
     * As a result, it leaves us the only option which is to capture the start
     * and end character offsets of the function body.
     * <p>
     * Since {@link Reader#skip(long)} is slow, the parser keeps a secondary
     * {@code Reader} ({@link #secondaryReader}) in order to cache the text of
     * the function body on-the-fly. The text is stored on the corresponding
     * {@link FunctionNode}.
     * <p>
     * This feature is turn on only <b>all</b> of the following conditions are
     * true:
     * <ul>
     * <li>The source is from a readable ActionScript source file.</li>
     * <li>The function is not a function closure.</li>
     * <li>The source file is not an in-memory compilation (see
     * {@link IInvisibleCompilationUnit}) unit in the editor.</li>
     * </ul>
     * 
     * @param functionNode Function node.
     * @param openT The open curly token of the function body.
     * @see <a
     * href="https://zerowing.corp.adobe.com/display/compiler/Deferring+function+body+nodes">Deferring
     * function body nodes</a>
     */
    protected final void skipFunctionBody(final FunctionNode functionNode, final ASToken openT)
    {
        assert functionNode != null : "Function node can't be null.";
        assert openT != null && openT.getType() == TOKEN_BLOCK_OPEN : "Expected '{' token";

        // this feature is not applicable to this function node
        if (deferFunctionBody != DeferFunctionBody.ENABLED ||
            functionNode.getParent() instanceof FunctionObjectNode)
            return;

        // If the first token in the function body is "}" or "EOF", the function
        // body is empty. There's nothing to skip.
        if (LA(1) == TOKEN_BLOCK_CLOSE || LA(1) == EOF)
            return;

        // Start skipping function body by matching curly braces.
        boolean functionBodyHasInclude = false;
        ASToken prevToken = null;

        tokenLoop:
        for (int depth = 0; depth > 0 || LA(1) != TOKEN_BLOCK_CLOSE; consume())
        {
            prevToken = LT(1);

            // If a function body token's source path is different from the
            // function node's source path, there's include processing. Then,
            // the function body text caching optimization can't be used. 
            if (!this.getSourceFilePath().equals(LT(1).getSourcePath()))
                functionBodyHasInclude = true;

            switch (prevToken.getType())
            {
                case TOKEN_BLOCK_OPEN:
                    depth++;
                    break;
                case TOKEN_BLOCK_CLOSE:
                    depth--;
                    break;
                case EOF:
                    break tokenLoop;
            }
        }

        assert LA(1) == TOKEN_BLOCK_CLOSE || LA(1) == EOF : "Loop should stop before the '}' of the function body or 'eof'.";
        assert prevToken != null : "Function body must have at least one token if we reached here.";

        final StringBuilder functionBodyText = tryGetFunctionBodyText(openT, functionBodyHasInclude, prevToken);
        functionNode.setFunctionBodyInfo(openT, prevToken, configProcessor, functionBodyText);
        fileNodeAccumulator.addDeferredFunctionNode(functionNode);
    }

    /**
     * Optimization: cache function body text if there's no include processing.
     * 
     * @param openT Open curly token of the function body.
     * @param functionBodyHasInclude True if there are "include" directives in
     * the function body.
     * @param lastToken Last token of the function body (excluding close curly
     * "}").
     * @return Function body text or null.
     */
    private StringBuilder tryGetFunctionBodyText(
            final ASToken openT,
            final boolean functionBodyHasInclude,
            final ASToken lastToken)
    {
        final StringBuilder functionBodyText;
        if (secondaryReader != null && !functionBodyHasInclude)
        {
            // Get function body text
            functionBodyText = new StringBuilder();
            try
            {
                // Move secondary reader to the beginning of a function body.
                final int readerSkip = openT.getLocalEnd() - secondaryReaderPosition;
                assert readerSkip >= 0 : "Invalid position";
                final long skip = secondaryReader.skip(readerSkip);
                assert skip == readerSkip : "The buffer didn't skip full length.";

                // Read text till the end of the function body (including the 
                // closing curly if there is one).
                final ASToken endToken;
                if (LA(1) == TOKEN_BLOCK_CLOSE)
                    endToken = LT(1);
                else
                    endToken = lastToken;

                for (int position = openT.getLocalEnd(); position < endToken.getLocalEnd(); position++)
                {
                    functionBodyText.append((char)secondaryReader.read());
                }

                // Update secondary reader position to the end of the function body.
                secondaryReaderPosition = endToken.getLocalEnd();
            }
            catch (IOException e)
            {
                String path = openT.getSourcePath();
                ICompilerProblem problem = (path == null) ?
                        new InternalCompilerProblem(e) :
                        new InternalCompilerProblem2(path, e, SUB_SYSTEM);
                errors.add(problem);
            }
        }
        else
        {
            functionBodyText = null;
        }
        return functionBodyText;
    }

    /**
     * Increment package depth counter and check for
     * {@link NestedPackageProblem}.
     * 
     * @param keywordPackage "package" keyword token.
     */
    protected final void enterPackage(ASToken keywordPackage)
    {
        if (packageDepth > 0)
        {
            final ICompilerProblem problem = new NestedPackageProblem(keywordPackage);
            addProblem(problem);
        }
        else if (!isGlobalContext())
        {
            final ICompilerProblem problem = new SyntaxProblem(keywordPackage);
            addProblem(problem);
        }
        packageDepth++;
    }

    /**
     * Called when the parser enters a type application expression. It validates
     * the "collection expression" of the type application expression. It must
     * be either a {@code MemberAccessExpressionNode} or an
     * {@code IdentifierNode}.
     * 
     * @param collectionExpression The expression node on the left-hand side of
     * {@code .< >}.
     */
    protected final void enterTypeApplication(final ExpressionNodeBase collectionExpression)
    {
        if (collectionExpression == null ||
            collectionExpression.getNodeID() == ASTNodeID.IdentifierID ||
            collectionExpression.getNodeID() == ASTNodeID.FunctionCallID ||
            collectionExpression.getNodeID() == ASTNodeID.TypedExpressionID ||
            collectionExpression instanceof MemberAccessExpressionNode)
        {
            return;
        }
        else
        {
            final ICompilerProblem problem = new InvalidTypeProblem(
                    LT(1),
                    collectionExpression.getNodeID().getParaphrase());
            addProblem(problem);
        }
    }

    /**
     * Enter a "group" and increment the depth counter.
     */
    protected final void enterGroup()
    {
        groupDepth++;
    }

    /**
     * Leave a "group" and decrement the depth counter.
     */
    protected final void leaveGroup()
    {
        groupDepth--;
    }

    /**
     * Check if class definition is allowed.
     * 
     * @param keywordClass {@code class} keyword token.
     */
    protected final void enterClassDefinition(final ASToken keywordClass)
    {
        if (!isGlobalContext())
            addProblem(new NestedClassProblem(keywordClass));
    }

    /**
     * Check if interface definition is allowed.
     * 
     * @param keywordInterface {@code interface} keyword token.
     */
    protected final void enterInterfaceDefinition(final ASToken keywordInterface)
    {
        if (!isGlobalContext())
            addProblem(new NestedInterfaceProblem(keywordInterface));
    }

    /**
     * Decrement package depth counter.
     */
    protected final void leavePackage()
    {
        assert blockDepth >= 0 : "package depth should never be negative";
        packageDepth = packageDepth > 0 ? packageDepth - 1 : 0;
    }

    /**
     * A syntax tree is in a global context if it is nested by a <i>program</i>
     * without crossing a <i>block</i>, or nested by the <i>block</i> of a
     * <i>package directive</i> without crossing another <i>block</i>.
     * <p>
     * Note that directive-level blocks are called "groups". Entering a group
     * doesn't cause the parser to leave global context.
     * 
     * @return True if the current context is global context.
     */
    protected final boolean isGlobalContext()
    {
        assert blockDepth >= 0 : "block depth should never be negative";
        assert packageDepth <= blockDepth : "package depth can't be greater than block depth";
        assert groupDepth <= blockDepth : "group depth can't be greater than block depth";
        assert packageDepth + groupDepth <= blockDepth : "invalid state of block depth";

        return blockDepth - packageDepth - groupDepth == 0;
    }

    /**
     * Disambiguate between a function definition and a function closure.
     * 
     * @return True if the next "function" keyword defines a closure.
     */
    protected final boolean isFunctionClosure()
    {
        return LA(1) == TOKEN_KEYWORD_FUNCTION && (LA(2) == TOKEN_PAREN_OPEN || buffer.previous().canPreceedAnonymousFunction());
    }

    /**
     * Ignore missing semicolons in the following two cases:
     * 
     * <pre>
     * do x++ while (x<10);     // after x++
     * if (x<10) x++ else x--;  // after x++
     * </pre>
     */
    protected final void afterInnerSubstatement()
    {
        final ICompilerProblem lastError = Iterables.getLast(errors, null);
        if (lastError != null && lastError instanceof CanNotInsertSemicolonProblem)
        {
            // Ideally, we should compare absolute offset. However, CMP-1828 requires
            // all syntax problems to have local offset.
            final int problemEnd = ((CanNotInsertSemicolonProblem)lastError).getEnd();
            final int lastTokenEnd = LT(1).getLocalEnd();
            if (problemEnd == lastTokenEnd)
                errors.remove(lastError);
        }
    }

    /**
     * Recover from metadata parsing failure. Code model require error recovery
     * logic for the following case:
     * 
     * <pre>
     * [
     * function hello():void {}
     * </pre>
     * 
     * The incomplete square bracket is expected to be matched as part of a
     * metadata tag, and the following function definition is properly built.
     * However, function objects in array literals are valid:
     * 
     * <pre>
     * var fs = [ function f1():void{}, function f2():void{} ];
     * </pre>
     * 
     * In order to appease code hinting, the recovery logic will hoist the
     * elements in the failed array literal into the parent node of the array
     * literal node.
     * 
     * @param container Parent node of array literal node.
     * @param arrayLiteralNode Array literal node, whose contents will be
     * hoisted.
     */
    protected final void recoverFromMetadataTag(final ContainerNode container, final ArrayLiteralNode arrayLiteralNode)
    {
        if (container == null || arrayLiteralNode == null)
            return;

        // Hoist content associated with the failed array literal.
        final ContainerNode contents = arrayLiteralNode.getContentsNode();
        for (int i = 0; i < contents.getChildCount(); i++)
        {
            final IASNode child = contents.getChild(i);
            if (child.getNodeID() == ASTNodeID.FunctionObjectID)
                container.addChild(((FunctionObjectNode)child).getFunctionNode());
            else
                container.addChild((NodeBase)child);
        }

        // Handle special case:
        // [
        // var x:int;
        // The keyword 'var' is turned into a identifier by 'handleParsingError()'.
        final ASToken lookback = buffer.previous();
        if (lookback != null &&
            lookback.getType() == TOKEN_IDENTIFIER &&
            lookback.getText().equals(IASKeywordConstants.VAR))
        {
            lookback.setType(TOKEN_KEYWORD_VAR);
            buffer.rewind(buffer.mark() - 1);
        }
    }

    /**
     * Determine if the parser should deploy an error trap for a
     * typing-in-progress metadata tag on a definition item. For example:
     * 
     * <pre>
     * class T
     * {
     *     [
     *     public var name:String;
     *     
     *     [
     *     public override function hello():void {}
     *     
     *     [
     *     function process();
     * }
     * </pre>
     * 
     * The error trap is enabled when the "[" token is the only token on the
     * current line, and the next token can start a definition item.
     */
    protected final boolean isIncompleteMetadataTagOnDefinition()
    {
        if (LA(1) == TOKEN_SQUARE_OPEN &&
            LT(1).getLine() > buffer.previous().getLine() &&
            LT(2).getLine() > LT(1).getLine())
        {
            // Note that function object is allowed inside array literal.
            switch (LA(2))
            {
                case TOKEN_KEYWORD_VAR:
                case TOKEN_KEYWORD_CLASS:
                case TOKEN_KEYWORD_INTERFACE:
                case TOKEN_NAMESPACE_ANNOTATION:
                case TOKEN_MODIFIER_DYNAMIC:
                case TOKEN_MODIFIER_FINAL:
                case TOKEN_MODIFIER_NATIVE:
                case TOKEN_MODIFIER_OVERRIDE:
                case TOKEN_MODIFIER_STATIC:
                case TOKEN_MODIFIER_VIRTUAL:
                case TOKEN_MODIFIER_ABSTRACT:
                    return true;
            }
        }
        return false;
    }

    /**
     * All tags with binding names such as {@code < foo}>} use this name in the
     * XML tag stack.
     */
    private static final String TAG_NAME_BINDING = "{...}";

    /**
     * Called when a {@code <foo} token is encountered.
     * 
     * @param token {@code TOKEN_E4X_OPEN_TAG_START} token.
     */
    protected final void xmlTagOpen(final ASToken token)
    {
        assert token.getType() == TOKEN_E4X_OPEN_TAG_START : "unexpected token type: " + token;
        assert token.getText().startsWith("<") : "unexpected token text: " + token;
        final String tagName = token.getText().substring(1);
        xmlTags.push(new XMLTagInfo(tagName, token));
    }

    /**
     * Called when a {@code <} token is encountered.
     * 
     * @param token {@code HIDDEN_TOKEN_E4X} token.
     */
    protected final void xmlTagOpenBinding(final ASToken token)
    {
        assert token.getType() == HIDDEN_TOKEN_E4X : "unexpected token " + token;
        assert token.getText().equals("<") : "unexpected token text: " + token;
        xmlTags.push(new XMLTagInfo(TAG_NAME_BINDING, token));
    }

    /**
     * Called when a {@code </foo} token is encountered.
     * 
     * @param token {@code TOKEN_E4X_CLOSE_TAG_START} token.
     */
    protected final void xmlTagClose(final ASToken token)
    {
        assert token.getType() == TOKEN_E4X_CLOSE_TAG_START : "unexpected token " + token;
        assert token.getText().startsWith("</") : "unexpected token text: " + token;
        final String tagName = token.getText().substring(2);
        if (!xmlTags.isEmpty())
        {
            final XMLTagInfo openTag = xmlTags.pop();
            if (TAG_NAME_BINDING.equals(openTag.name) || tagName.isEmpty())
            {
                // At least one of the open or close tag's name is a binding.
                // No need to check if tags match.
                return;
            }
            else if (openTag.name.equals(tagName))
            {
                // Open and close tags match.
                return;
            }
            else
            {
                addProblem(new XMLOpenCloseTagNotMatchProblem(token));
                return;
            }
        }
        else
        {
            addProblem(new SyntaxProblem(token));
        }
    }

    /**
     * Called when a {@code />} token is encountered.
     * 
     * @param token {@code TOKEN_E4X_EMPTY_TAG_END} token.
     */
    protected final void xmlEmptyTagEnd(final ASToken token)
    {
        assert token.getType() == TOKEN_E4X_EMPTY_TAG_END : "unexpected token " + token;
        assert token.getText().endsWith("/>") : "unexpected token text: " + token;
        if (!xmlTags.isEmpty())
        {
            xmlTags.pop();
        }
    }

    /**
     * Called when start parsing an XML literal.
     */
    protected final void enterXMLLiteral()
    {
        assert xmlTags.isEmpty() : "Invalid state: must clear tag stack after each XML literal.";
        xmlTags.clear();
    }

    /**
     * Called when finish parsing an XML literal. This method validates the XML.
     */
    protected final void leaveXMLLiteral()
    {
        while (!xmlTags.isEmpty())
        {
            final XMLTagInfo openTag = xmlTags.pop();
            final ICompilerProblem problem = new XMLOpenCloseTagNotMatchProblem(openTag.location);
            addProblem(problem);
        }
    }

    /**
     * A tuple of XML tag name and its source location.
     */
    private static final class XMLTagInfo
    {
        /**
         * XML tag name.
         */
        final String name;
        /**
         * XML tag location.
         */
        final ASToken location;

        XMLTagInfo(final String name, final ASToken location)
        {
            this.name = name;
            this.location = location;
        }
    }

    /**
     * Syntactic predicate for CMP-335.
     * 
     * @return True if the next token is "public" namespace token.
     */
    protected final boolean isNextTokenPublicNamespace()
    {
        final ASToken token = LT(1);
        return TOKEN_NAMESPACE_ANNOTATION == token.getType() &&
               IASKeywordConstants.PUBLIC.equals(token.getText());
    }

    /**
     * Check if the next attribute definition is gated with a configuration
     * condition variable. If it is, then return the evaluated result of the
     * condition.
     * 
     * @param containerNode parent node
     * @return True if the attributed definition is enabled.
     */
    protected final boolean isDefinitionEnabled(final ContainerNode containerNode)
    {
        assert containerNode != null : "Container node can't be null.";
        final int childCount = containerNode.getChildCount();
        if (childCount > 0)
        {
            final IASNode lastChild = containerNode.getChild(childCount - 1);
            if (lastChild.getNodeID() == ASTNodeID.LiteralBooleanID)
            {
                // evaluate
                final boolean eval = Boolean.parseBoolean(((LiteralNode)lastChild).getValue());
                // remove the configuration condition node
                containerNode.removeItem((NodeBase)lastChild);
                containerNode.setRemovedConditionalCompileNode(true);
                return eval;
            }
        }
        return true;
    }

    /**
     * Parses an ActionScript3 file and populate the FileNode. This method
     * forces the parser to retry after
     * {@code fileLevelDirectives(ContainerNode)} fails. Before retrying, the
     * parser makes sure one and only one
     * {@link ExtraCharactersAfterEndOfProgramProblem} is logged. Then, it
     * discards the offending token and continue matching at "directive" level.
     * <p>
     * I didn't write this as a regular ANTLR rule because the ANTLR-generated
     * code wouldn't enter {@code fileLevelDirectives(ContainerNode)} if the
     * lookahead is not one of the tokens in the "start set".
     */
    public final void file(ContainerNode c) throws RecognitionException, TokenStreamException
    {
        while (LA(1) != EOF)
        {
            fileLevelDirectives(c);
            if (LA(1) != EOF)
            {
                foundExtraCharacterAfterEndOfProgram(LT(1));
                consume();
            }
        }
    }

    /**
     * @return true when we are parsing project vars (like the command line)
     */
    public final boolean isParsingProjectConfigVariables()
    {
        return parsingProjectConfigVariables;
    }
}
