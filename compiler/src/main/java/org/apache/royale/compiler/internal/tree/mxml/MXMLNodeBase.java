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

package org.apache.royale.compiler.internal.tree.mxml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLScopeBuilder;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLNamespaceAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLAttributeVersionProblem;
import org.apache.royale.compiler.problems.MXMLEmptyAttributeProblem;
import org.apache.royale.compiler.problems.MXMLOtherLanguageNamespaceProblem;
import org.apache.royale.compiler.problems.MXMLPrivateAttributeProblem;
import org.apache.royale.compiler.problems.MXMLUnexpectedAttributeProblem;
import org.apache.royale.compiler.problems.MXMLUnexpectedTagProblem;
import org.apache.royale.compiler.problems.MXMLUnexpectedTextProblem;
import org.apache.royale.compiler.problems.MXMLUnknownNamespaceProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;
import org.apache.royale.utils.FilenameNormalization;

/**
 * {@code MXMLNodeBase} is the abstract base class for all MXML nodes in an AST.
 * <p>
 * Although the MXML node interfaces (i.e., {@code IMXMLNode} and its
 * subinterfaces) support only read-only operations on the MXML AST, the MXML
 * node class contain the logic for building the tree node-by-node from the MXML
 * DOM.
 * <p>
 * For example, the processing of the &lt;Script&gt; tag is handled by the
 * {@code MXMLScriptNode} class, which encapsulates the logic that a
 * &lt;Script&gt; tag can only have namespace attributes and a
 * <code>source</code> attribute, and that it cannot have any child tags.
 * <p>
 * This base class handles some analysis that must be done on every kind of MXML
 * tag:
 * <ul>
 * <li>Walking the tag's attributes and its child units.</li>
 * <li>Noticing whether a namespace attribute defines an illegal language
 * namespace.</li>
 * <li>Noticing whether any tag or attribute uses an illegal language namespace.
 * </li>
 * <li>Noticing any private attributes. We will report a problem for these,
 * since developers who don't use them may prefer to catch them as warnings.</li>
 * </ul>
 * <p>
 * Problems at the level of incorrect XML are noticed before tree construction,
 * during MXML parsing. Such problems include:
 * <ul>
 * <li>Using an undefined prefix on a tag or attribute.
 * <li>Using an unrecognized entity.
 * </ul>
 * <p>
 * The default behavior implemented in this base class reports a problem for
 * every attribute (except for namespace and private ones) and every
 * non-whitespace text unit. In other words, by default it implements
 * "you can't put that here". It is the responsibility then of subclasses to
 * allow particular attributes and content units rather than disallow them.
 */
public abstract class MXMLNodeBase extends NodeBase implements IMXMLNode
{
    // TODO Make this class package internal rather than public.

    /**
     * This tokenizer is used to implement
     * {@link #isValidActionScriptIdentifier()}. {@link StreamingASTokenizer} is
     * <b>not</b> thread-safe, so we need to make a thread-local copy for each
     * thread.
     */
    private static final ThreadLocal<StreamingASTokenizer> asTokenizer =
            new ThreadLocal<StreamingASTokenizer>()
            {
                protected StreamingASTokenizer initialValue()
                {
                    return new StreamingASTokenizer();
                };
            };

    /**
     * This tokenizer is used to implement {@link #isValidXMLTagName()}.
     * {@link MXMLTokenizer} is <b>not</b> thread-safe, so we need to make a
     * thread-local copy for each thread.
     */
    private static final ThreadLocal<MXMLTokenizer> mxmlTokenizer =
            new ThreadLocal<MXMLTokenizer>()
            {
                protected MXMLTokenizer initialValue()
                {
                    return new MXMLTokenizer();
                };
            };

    /**
     * Resolves the path specified in an MXML tag's <code>source</code>
     * attribute.
     * 
     * @param builder The {@code MXMLTreeBuilder} object which is building this
     * MXML tree.
     * @param attribute The <code>source</code> attribute.
     * @return A resolved and normalized path to the external file.
     */
    public static String resolveSourceAttributePath(MXMLTreeBuilder builder,
                                                    IMXMLTagAttributeData attribute,
                                                    MXMLNodeInfo info)
    {
        if (info != null)
            info.hasSourceAttribute = true;

        String sourcePath = attribute.getMXMLDialect().trim(attribute.getRawValue());

        if (sourcePath.isEmpty() && builder != null)
        {
            ICompilerProblem problem = new MXMLEmptyAttributeProblem(attribute);
            builder.addProblem(problem);
            return null;
        }

        String resolvedPath;

        if ((new File(sourcePath)).isAbsolute())
        {
            // If the source attribute specifies an absolute path,
            // it doesn't need to be resolved.
            resolvedPath = sourcePath;
        }
        else
        {
            // Otherwise, resolve it relative to the MXML file.
            String mxmlPath = attribute.getParent().getParent().getPath();
            resolvedPath = FilenameUtils.getPrefix(mxmlPath) + FilenameUtils.getPath(mxmlPath);
            resolvedPath = FilenameUtils.concat(resolvedPath, sourcePath);
        }

        String normalizedPath = FilenameNormalization.normalize(resolvedPath);

        // Make the compilation unit dependent on the external file.
        if (builder != null)
            builder.getFileScope().addSourceDependency(normalizedPath);

        return normalizedPath;
    }

    /**
     * Determines whether the specified String is a single valid ActionScript
     * identifier, by tokenizing it.
     * 
     * @param identifier A String to be tokenized.
     * @return <code>true</code> if the String tokenizes to a single identifier
     * token.
     */
    protected static boolean isValidASIdentifier(String identifier)
    {
        IASToken[] tokens = asTokenizer.get().getTokens(identifier);
        return tokens != null && tokens.length == 1 && tokens[0].getType() == ASToken.TOKEN_IDENTIFIER;
    }

/**
     * Determines whether the specified String is a valid XML tag name
     * by appending it to "<" and tokenizing it.
     * 
     * @param tagName A String proposed as a tag name.
     * @return <code>true</code> if the String is a valid tag name.
     */
    protected static boolean isValidXMLTagName(String tagName)
    {
        String s = "<" + tagName;
        IMXMLToken[] tokens = mxmlTokenizer.get().getTokens(s + "/>");
        return tokens != null && tokens.length == 2 &&
               tokens[0].getType() == MXMLTokenTypes.TOKEN_OPEN_TAG_START &&
               tokens[0].getText().equals(s) &&
               tokens[1].getType() == MXMLTokenTypes.TOKEN_EMPTY_TAG_END;
    }

    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLNodeBase(NodeBase parent)
    {
        this.parent = parent;
    }

    private boolean validForCodeGen = true;

    @Override
    public boolean isValidForCodeGen()
    {
        return validForCodeGen;
    }

    void markInvalidForCodeGen()
    {
        validForCodeGen = false;
    }

    /**
     * Initializes this MXML node from an MXML tag by processing the attribute
     * and content units of the tag. This processing sets the properties of this
     * node and creates the children of this node. The child nodes will
     * themselves be initialized from attributes or child tags; in this way, the
     * MXML AST will get constructed from the MXML DOM.
     * 
     * @param builder The {@code MXMLTreeBuilder} object which is building this
     * MXML tree.
     * @param tag The MXML tag from which this MXML node is being created.
     */
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        setLocation(tag);

        MXMLNodeInfo info = createNodeInfo(builder);

        // Process each attribute.
        for (IMXMLTagAttributeData attribute : tag.getAttributeDatas())
        {
            processAttribute(builder, tag, attribute, info);
        }

        // Process each content unit.
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            processContentUnit(builder, tag, unit, info);
        }

        // Do any final processing.
        initializationComplete(builder, tag, info);
    }

    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return null;
    }

    /**
     * This method gives subclasses a chance to do final processing after
     * considering each attribute and content unit.
     * <p>
     * The base class version calls <code>adjustOffset</code> to translate the
     * node start and end offset from local to absolute offsets.
     */
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        adjustOffsets(builder);
    }

    /**
     * Translates the node's start and end offset from local to absolute
     * offsets.
     */
    public void adjustOffsets(MXMLTreeBuilder builder)
    {
        final MXMLFileScope fileScope = builder.getFileScope();
        assert fileScope != null : "Expected MXMLFileScope.";

        final OffsetLookup offsetLookup = fileScope.getOffsetLookup();
        assert offsetLookup != null : "Expected OffsetLookup on file scope.";

        final String path = getFileSpecification().getPath(); // TODO Get from builder?

        final int absoluteStart = offsetLookup.getAbsoluteOffset(path, getAbsoluteStart())[0];
        final int absoluteEnd = offsetLookup.getAbsoluteOffset(path, getAbsoluteEnd())[0];
        setStart(absoluteStart);
        setEnd(absoluteEnd);
    }

    @Override
    public IASNode getChild(int i)
    {
        // By default, MXML nodes have no children.
        // This base class does not provide child storage.
        return null;
    }

    @Override
    public int getChildCount()
    {
        // By default, MXML nodes have no children.
        // This base class does not provide child storage.
        return 0;
    }

    @Override
    public IMXMLClassDefinitionNode getClassDefinitionNode()
    {
        return (IMXMLClassDefinitionNode)getAncestorOfType(IMXMLClassDefinitionNode.class);
    }

    @Override
    public IMXMLDocumentNode getDocumentNode()
    {
        return (IMXMLDocumentNode)getAncestorOfType(IMXMLDocumentNode.class);
    }

    @Override
    public IMXMLFileNode getFileNode()
    {
        return (IMXMLFileNode)getAncestorOfType(IMXMLFileNode.class);
    }

    /**
     * Processes a single attribute during the initialization of this node.
     * <p>
     * The default behavior implemented in this base class is to simply call
     * <code>processNamespaceAttribute()</code> on namespace attributes,
     * <code>processPrivateAttribute()</code> on private attributes, and
     * <code>processTagSpecificAttribute()</code> on other attributes.
     * <p>
     * Subclass should not need to override this method, so it is private.
     * 
     * @param builder The {@code MXMLTreeBuilder} object which is building this
     * MXML tree.
     * @param tag An {@code IMXMLTagData} object representing the tag.
     * @param attribute An {@code MXMLTagAttributeData} object representing the
     * attribute.
     */
    private void processAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                  IMXMLTagAttributeData attribute,
                                  MXMLNodeInfo info)
    {
        if (attribute instanceof IMXMLNamespaceAttributeData)
            processNamespaceAttribute(builder, tag, (IMXMLNamespaceAttributeData)attribute);

        else if (isPrivateAttribute(attribute))
            processPrivateAttribute(builder, tag, attribute);

        else
            processTagSpecificAttribute(builder, tag, attribute, info);
    }

    /**
     * Processes a single namespace attribute such as xmlns:foo="..." or
     * xmlns="...".
     * <p>
     * The default behavior implemented in this base class is to check whether
     * the namespace URI is a language URI that is different from that of the
     * overall MXML document. (E.g., the document is in MXML 2009 but then you
     * use MXML 2006 somewhere inside.)
     * <p>
     * Subclasses do not need to override this method, so it is private.
     * 
     * @param attribute An {@code MXMLNamespaceAttributeData} object
     * representing the attribute.
     */
    private void processNamespaceAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                           IMXMLNamespaceAttributeData attribute)
    {
        String attributeURI = attribute.getNamespace();

        IMXMLData mxmlData = attribute.getParent().getParent();
        String languageURI = mxmlData.getMXMLDialect().getLanguageNamespace();

        if (MXMLDialect.isLanguageNamespace(attributeURI) &&
            !attributeURI.equals(languageURI))
        {
            ICompilerProblem problem = new MXMLOtherLanguageNamespaceProblem(attribute);
            builder.addProblem(problem);
        }
    }

    /**
     * Determines whether the specified attribute is a private attribute.
     * <p>
     * MXML 2006 does not have the concept of private attributes. In MXML 2009,
     * a private attribute is one whose URI is neither the document's language
     * URI nor the URI of the attribute's tag.
     * <p>
     * 
     * @param attribute An {@code MXMLNamespaceAttributeData} object
     * representing the attribute.
     * @return <code>true</code> if the attribute is a private attribute and
     * <code>false</code> otherwise.
     */
    private static boolean isPrivateAttribute(IMXMLTagAttributeData attribute)
    {
        String attributeURI = attribute.getURI();
        if (attributeURI == null)
            return false;

        String tagURI = attribute.getParent().getURI();

        IMXMLData mxmlData = attribute.getParent().getParent();
        MXMLDialect mxmlDialect = mxmlData.getMXMLDialect();
        String languageURI = mxmlDialect.getLanguageNamespace();

        boolean isPrivate = false;
        if (mxmlDialect.isEqualToOrAfter(MXMLDialect.MXML_2009))
        {
            isPrivate = !attributeURI.equals(languageURI) &&
                        !attributeURI.equals(tagURI);
        }
        return isPrivate;
    }

    /**
     * Processes a single private attribute such as p="...".
     * <p>
     * The default behavior implemented in this base class is to report each
     * private attribute as a "problem". They're allowed by some versions of
     * MXML, but even in those versions developers may not want to use them and
     * would prefer to catch typographical mistakes that produce private
     * attributes.
     * <p>
     * Subclasses do not need to override this method so it is private.
     * 
     * @param attribute An {@code MXMLAttributeData} object representing the
     * attribute.
     */
    private void processPrivateAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                         IMXMLTagAttributeData attribute)
    {
        ICompilerProblem problem = new MXMLPrivateAttributeProblem(attribute);
        builder.addProblem(problem);
    }

    /**
     * Processes a single tag-specific attribute (i.e., one that isn't allowed
     * on every tag, unlike a namespace attribute or a private attribute).
     * <p>
     * The attribute might be specifying a property, event, or style; or it
     * might be a special compile-time attribute such as 'id', 'includeIn',
     * 'excludeFrom', 'source', etc. A property/event/style attribute will cause
     * a child node to be added to this node; a compile-time attribute typically
     * will simply set a property of the node.
     * <p>
     * The default behavior implemented in this base class is to report each
     * tag-specific attribute as a problem.
     * <p>
     * Subclasses must override this method in order to allow tag-specific
     * attributes.
     * 
     * @param attribute An {@code MXMLTagAttributeData} object representing the
     * attribute.
     */
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        ICompilerProblem problem = new MXMLUnexpectedAttributeProblem(attribute);
        builder.addProblem(problem);
    }

    /**
     * Processes a single content unit. A content unit can be a child tag, text
     * (including CDATA or an entity), a comment, or a processing instruction.
     * <p>
     * The default behavior implemented in this base class is simply to call
     * <code>processChildTag()</code> on each child tag,
     * <code>processChildTextUnit()</code> on each child text unit (including
     * whitespace, entities, comments, and processing instructions), and
     * <code>processChildDatabindingUnit()</code> on each child databinding
     * unit.
     * <p>
     * Subclasses do not need to override this method so it is private.
     * 
     * @param unit An {@code MXMLUnitData} object representing the content unit.
     */
    private void processContentUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                    IMXMLUnitData unit,
                                    MXMLNodeInfo info)
    {
        if (unit instanceof IMXMLTagData)
            processChildTag(builder, tag, (IMXMLTagData)unit, info);

        else if (unit instanceof IMXMLTextData)
            processChildTextUnit(builder, tag, (IMXMLTextData)unit, info);
    }

    /**
     * Processes a single child tag.
     * 
     * @param tag An {@code MXMLTagData} object representing the child tag.
     * <p>
     * The default behavior implemented in this base class is to report each
     * child tag as a problem.
     * <p>
     * Subclasses must override this method in order to allow the child tags
     * that they recognize.
     */
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        if (childTag.getURI() == null)
            builder.addProblem(new MXMLUnknownNamespaceProblem(childTag, childTag.getPrefix()));
        else
            builder.addProblem(new MXMLUnexpectedTagProblem(childTag));
    }

    /**
     * Processes a single child text unit.
     * 
     * @param text An {@code MXMLTextData} object representing the child text
     * unit.
     * <p>
     * The default behavior implemented in this base class is to ignore comments
     * and to call <code>processChildWhitespaceUnit()</code> if the text (which
     * might be an entity or a CDATA block) is all whitespace and
     * <code>processChildNonWhitespaceUnit()</code> otherwise.
     * <p>
     * Subclasses do not need to override this method so it is private.
     */
    private void processChildTextUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                      IMXMLTextData text,
                                      MXMLNodeInfo info)
    {
        switch (text.getTextType())
        {
            case TEXT:
            case WHITESPACE:
            case CDATA:
            {
                if (tag.getMXMLDialect().isWhitespace(text.getCompilableText()))
                    processChildWhitespaceUnit(builder, tag, text, info);
                else
                    processChildNonWhitespaceUnit(builder, tag, text, info);
                break;
            }

            case COMMENT:
            case ASDOC:
            {
                // ignore these
                break;
            }
        }
    }

    /**
     * Processes a single child text unit which is all whitespace.
     * <p>
     * The default behavior implemented in this base class is to ignore
     * whitespace.
     * <p>
     * Subclasses must override this method in order to disallow whitespace text
     * units.
     * 
     * @param text An {@code MXMLTextData} object representing the child text
     * unit.
     */
    protected void processChildWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                              IMXMLTextData text,
                                              MXMLNodeInfo info)
    {
    }

    /**
     * Processes a single child text unit which is not all whitespace.
     * <p>
     * The default behavior implemented in this base class is to report each
     * unit of non-whitespace as a problem.
     * <p>
     * Subclasses must override this method in order to allow the non-whitespace
     * text unit.
     * 
     * @param text An {@code MXMLTextData} object representing the child text
     * unit.
     */
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        ICompilerProblem problem = new MXMLUnexpectedTextProblem(text);
        builder.addProblem(problem);
    }

    /**
     * Processes all the children of the given {@link IMXMLTagData} unit that are
     * {@link IMXMLTextData} nodes. Each node will be processes separately, in
     * the order in which the appear in the document.
     * <p>
     * This method is only used by MXML AST building. To parse an ActionScript
     * block for scope building, see {@link MXMLScopeBuilder#processScriptTag}.
     * 
     * @param tag the {@link IMXMLTagData} to process
     * @return a {@link List} of {@link ScopedBlockNode}s for each
     * {@link IMXMLTextData} we encountered.
     */
    public static List<ScopedBlockNode> processUnitAsAS(
            MXMLTreeBuilder builder,
            IMXMLTagData tag,
            String sourcePath,
            ASScope containingScope,
            PostProcessStep buildOrReconnect,
            IMXMLFileNode ancestorFileNode)
    {
        assert buildOrReconnect == PostProcessStep.POPULATE_SCOPE || buildOrReconnect == PostProcessStep.RECONNECT_DEFINITIONS;
        List<ScopedBlockNode> nodes = new ArrayList<ScopedBlockNode>(2);
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLTextData)
            {
                final IMXMLTextData mxmlTextData = (IMXMLTextData)unit;
                if (mxmlTextData.getTextType() != TextType.WHITESPACE)
                {
                    final Workspace workspace = builder.getWorkspace();
                    final RoyaleProject project = builder.getProject();
                    final Collection<ICompilerProblem> problems = builder.getProblems();

                    final IncludeHandler includeHandler = new IncludeHandler(builder.getFileSpecificationGetter());
                    includeHandler.setProjectAndCompilationUnit(project, builder.getCompilationUnit());

                    final ScopedBlockNode node = ASParser.parseFragment2(
                            mxmlTextData.getCompilableText(),
                            sourcePath,
                            mxmlTextData.getCompilableTextStart(),
                            mxmlTextData.getCompilableTextLine() - 1,
                            mxmlTextData.getCompilableTextColumn() - 1,
                            problems,
                            workspace,
                            builder.getFileNode(),
                            containingScope,
                            project.getProjectConfigVariables(),
                            EnumSet.of(PostProcessStep.CALCULATE_OFFSETS, buildOrReconnect),
                            true, //follow includes 
                            includeHandler
                            );
                    ((MXMLFileNode)ancestorFileNode).updateIncludeTreeLastModified(includeHandler.getLastModified());
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    /**
     * Sets the start/end/line/column location information for this node.
     * 
     * @param start The starting offset of this node.
     * @param end The ending offset of this node.
     * @param line The number of the line on which this node starts.
     * @param column This number of the column at which this node starts.
     * @deprecated Use setLocation(String,int,int,int,int,int,int) instead
     */
    public void setLocation(String sourcePath, int start, int end, int line, int column)
    {
        setSourcePath(sourcePath);
        setStart(start);
        setEnd(end);
        setLine(line);
        setColumn(column);
    }

    /**
     * Sets the start/end/line/column/endLine/endColumn location information for this node.
     * 
     * @param start The starting offset of this node.
     * @param end The ending offset of this node.
     * @param line The number of the line on which this node starts.
     * @param column This number of the column at which this node starts.
     * @param endLine The number of the line on which this node ends.
     * @param endColumn This number of the column at which this node ends.
     */
    public void setLocation(String sourcePath, int start, int end, int line, int column, int endLine, int endColumn)
    {
        setSourcePath(sourcePath);
        setStart(start);
        setEnd(end);
        setLine(line);
        setColumn(column);
        setEndLine(endLine);
        setEndColumn(endColumn);
    }

    /**
     * Sets the start/end/line/column location information for this node.
     * 
     * @param location A {@link SourceLocation} object.
     */
    public void setLocation(ISourceLocation location)
    {
        String sourcePath = location.getSourcePath();
        int start = location.getStart();
        int end = location.getEnd();
        int line = location.getLine();
        int column = location.getColumn();
        int endLine = location.getEndLine();
        int endColumn = location.getEndColumn();

        setLocation(sourcePath, start, end, line, column, endLine, endColumn);
    }

    /**
     * Sets the start/end/line/column location information for this node.
     * 
     * @param unit The MXML unit from which this node was created.
     */
    protected void setLocation(IMXMLUnitData unit)
    {
        String sourcePath = unit.getSourcePath();
        int start = unit.getAbsoluteStart();
        int end;
        if (unit instanceof IMXMLTagData)
        {
            IMXMLTagData startTag = (IMXMLTagData)unit;
            IMXMLTagData endTag = startTag.findMatchingEndTag();
            end = endTag != null ? endTag.getAbsoluteEnd() : startTag.getAbsoluteEnd();
        }
        else
        {
            end = unit.getAbsoluteEnd();
        }
        int line = unit.getLine();
        int column = unit.getColumn();
        int endLine = unit.getEndLine();
        int endColumn = unit.getEndColumn();

        setLocation(sourcePath, start, end, line, column, endLine, endColumn);
    }

    /**
     * Sets the start/end/line/column location information for this node.
     * 
     * @param attribute The MXML attribute from which this node was created.
     */
    protected void setLocation(IMXMLTagAttributeData attribute)
    {
        String sourcePath = attribute.getSourcePath();
        int start = attribute.getAbsoluteStart();
        int end = attribute.getAbsoluteEnd();
        int line = attribute.getLine();
        int column = attribute.getColumn();
        int endLine = attribute.getEndLine();
        int endColumn = attribute.getEndColumn();

        setLocation(sourcePath, start, end, line, column, endLine, endColumn);
    }

    /**
     * Sets the start/end/line/column location information for this node so that
     * it spans a list of MXML content units.
     * 
     * @param units A list of MXML content units.
     */
    protected void setLocation(MXMLTreeBuilder builder, List<IMXMLUnitData> units)
    {
        int n = units.size();

        IMXMLUnitData firstUnit = units.get(0);
        IMXMLUnitData lastUnit = units.get(n - 1);

        // we only store the open tags in the units
        // and the end offset should be the end of the last close tag
        // check this here and fetch the end tag if the last tag is an open and non-empty tag
        if (lastUnit instanceof IMXMLTagData && lastUnit.isOpenAndNotEmptyTag())
        {
            IMXMLUnitData endTag = (IMXMLUnitData)((IMXMLTagData)lastUnit).findMatchingEndTag();
            if (endTag != null)
                lastUnit = endTag;
        }

        String sourcePath = firstUnit.getSourcePath();
        int start = firstUnit.getStart();
        int end = lastUnit.getEnd();
        int line = firstUnit.getLine();
        int column = firstUnit.getColumn();
        int endLine = lastUnit.getEndLine();
        int endColumn = lastUnit.getEndColumn();
        
        setLocation(sourcePath, start, end, line, column, endLine, endColumn);
        
        adjustOffsets(builder);
    }

    /**
     * Accumulates source fragments that are produced by a text unit for later
     * processing.
     */
    protected void accumulateTextFragments(MXMLTreeBuilder builder,
                                           IMXMLTextData text,
                                           MXMLNodeInfo info)
    {
        Collection<ICompilerProblem> problems = builder.getProblems();
        ISourceFragment[] fragments = text.getFragments(problems);
        info.addSourceFragments(text.getSourcePath(), fragments);
    }

    /**
     * Processes an <code>includeIn="..."</code> or
     * <code>excludeFrom"..."</code> attribute on an instance tag or a
     * <code>&lt;Reparent&gt;</code> tag.
     */
    protected String[] processIncludeInOrExcludeFromAttribute(
            MXMLTreeBuilder builder, IMXMLTagAttributeData attribute)
    {
        MXMLDialect mxmlDialect = builder.getMXMLDialect();
        if (mxmlDialect == MXMLDialect.MXML_2006)
        {
            ICompilerProblem problem = new MXMLAttributeVersionProblem(attribute, attribute.getName(), "2009");
            builder.addProblem(problem);
            return null;
        }

        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();
        classNode.addStateDependentNode(builder, this);

        return mxmlDialect.splitAndTrim(attribute.getRawValue());
    }

    /**
     * MXML node classes can choose to create this object in
     * <code>createNodeInfo()</code>. It then gets passed to the various methods
     * involved in initializing one node, so that state can be conveyed between
     * these methods, rather than having this state stored in fields of the node
     * which would take up space after the node is completely initialized.
     * <p>
     * It is currently used accumulate child nodes and source fragments.
     */
    protected static class MXMLNodeInfo
    {
        public MXMLNodeInfo(MXMLTreeBuilder builder)
        {
            this.builder = builder;
        }

        private MXMLTreeBuilder builder;

        private List<IMXMLNode> childNodeList = new ArrayList<IMXMLNode>();

        private String sourcePath;

        private List<ISourceFragment> sourceFragmentList = new ArrayList<ISourceFragment>();

        /**
         * A flag that keeps track of whether the tag had a <code>source</code>
         * attribute.
         */
        public boolean hasSourceAttribute = false;

        /**
         * A flag that keeps track of whether the tag had content that would
         * conflict with the <code>source</code> attribute. For a Script, Style,
         * or String tag, this is non-whitespace text content. For an XML or
         * Model tag, this is a child tag.
         */
        public boolean hasDualContent = false;
        
        /**
         * Set of specifiers
         */
        protected Set<String> specifierSet = new HashSet<String>(0);

        public void addChildNode(IMXMLNode childNode)
        {
            childNodeList.add(childNode);
            
            if (childNode instanceof IMXMLSpecifierNode)
            {
                IMXMLSpecifierNode specifierNode = (IMXMLSpecifierNode)childNode;
                String suffix = specifierNode.getSuffix() != null ? specifierNode.getSuffix() : "";
                specifierSet.add(specifierNode.getName() + '.' + suffix);
            }
        }

        public List<IMXMLNode> getChildNodeList()
        {
            return childNodeList;
        }

        public void addSourceFragments(String sourcePath, ISourceFragment[] sourceFragments)
        {
            this.sourcePath = sourcePath;

            for (ISourceFragment sourceFragment : sourceFragments)
            {
                sourceFragmentList.add(sourceFragment);
            }
        }

        public void clearFragments()
        {
            sourceFragmentList.clear();
        }

        public String getSourcePath()
        {
            return sourcePath;
        }

        public ISourceFragment[] getSourceFragments()
        {
            final MXMLFileScope fileScope = builder.getFileScope();
            final OffsetLookup offsetLookup = fileScope.getOffsetLookup();
            assert offsetLookup != null : "Expected OffsetLookup on FileScope.";
            for (ISourceFragment fragment : sourceFragmentList)
            {
                int physicalStart = fragment.getPhysicalStart();
                final int[] absoluteOffsets = offsetLookup.getAbsoluteOffset(sourcePath, physicalStart);
                ((SourceFragment)fragment).setPhysicalStart(absoluteOffsets[0]);
            }

            return sourceFragmentList.toArray(new ISourceFragment[0]);
        }

        public SourceLocation getSourceLocation()
        {
            int n = sourceFragmentList.size();

            if (n == 0)
                return null;

            ISourceFragment firstFragment = sourceFragmentList.get(0);
            ISourceFragment lastFragment = sourceFragmentList.get(n - 1);

            int start = firstFragment.getPhysicalStart();
            int end = lastFragment.getPhysicalStart() + lastFragment.getPhysicalText().length();
            int line = firstFragment.getPhysicalLine();
            int column = firstFragment.getPhysicalColumn();
            int endLine = lastFragment.getPhysicalLine();
            int endColumn = lastFragment.getPhysicalColumn() + lastFragment.getPhysicalText().length();

            return new SourceLocation(sourcePath, start, end, line, column, endLine, endColumn);
        }
        
        /**
         * Check whether a specifier (attribute or child tag) with the same name and state already exist
         * @param name name of the specifier (attribute or child tag)
         * @param stateName name of the state
         * @return true, if the child already exist, false otherwise
         */
        public boolean hasSpecifierWithName(String name, String stateName)
        {
            return specifierSet.contains(name + '.' + stateName);
        }
    }
}
