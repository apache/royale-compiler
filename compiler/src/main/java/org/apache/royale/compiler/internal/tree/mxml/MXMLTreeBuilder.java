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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.mxml.MXMLDialect.TextParsingFlags;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.IProjectConfigVariables;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLDataManager;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLInvalidPercentageProblem;
import org.apache.royale.compiler.problems.MXMLInvalidSourceAttributeProblem;
import org.apache.royale.compiler.problems.MXMLInvalidTextForTypeProblem;
import org.apache.royale.compiler.problems.MXMLPercentageNotAllowedProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFunctionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRegExpNode;
import org.apache.royale.compiler.tree.mxml.IMXMLResourceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * {@code MXMLTreeBuilder} is used by {@code MXMLCompilationUnit} to build
 * abstract syntax trees (ASTs) for MXML files.
 * <p>
 * The input for MXML tree-building is an {@code MXMLData} object, a DOM-like
 * representing of an MXML file, and the {@code MXMLFileScope} already
 * constructed from that {@code MXMLData}. The output is an
 * {@code IOldMXMLFileNode} object, the root of a MXML AST.
 * <p>
 * {@code MXMLTreeBuilder} is the only public class in this package.
 * <p>
 * An instance of this class is passed to each tree-building method in MXML node
 * classes. It provides quick access to various useful objects while building an
 * MXML AST.
 */
public class MXMLTreeBuilder
{
    /**
     * Valid percentage expressions are: [whitespace]
     * positive-whole-or-decimal-number [whitespace] % [whitespace]
     */
    private static final Pattern percentagePattern =
            Pattern.compile("\\s*((\\d+)(.(\\d)+)?)\\s*%\\s*");

    /**
     * Constructor.
     * 
     * @param fileSpecGetter The {@link IFileSpecificationGetter} that will be
     * used to open included files.
     * @param compilationUnit The compilation unit which is compiling the MXML
     * file.
     * @param qname The fully-qualified classname corresponding to the MXML
     * file.
     * @param mxmlData The DOM representation of the MXML file.
     * @param fileScope The file scope already built for the MXML file.
     * @param problems An existing collection of {@code ICompilerProblem}
     * objects to which the tree-building code adds the problems it finds.
     */
    public MXMLTreeBuilder(MXMLCompilationUnit compilationUnit,
                           IFileSpecificationGetter fileSpecGetter,
                           String qname,
                           IMXMLData mxmlData,
                           MXMLFileScope fileScope,
                           Collection<ICompilerProblem> problems)
    {
        this.compilationUnit = compilationUnit;
        this.fileSpecGetter = fileSpecGetter;
        project = compilationUnit.getProject();
        factoryDef = (ITypeDefinition) project.resolveQNameToDefinition(project.getFactoryInterface());
        projectScope = (ASProjectScope)project.getScope();
        workspace = (Workspace)project.getWorkspace();
        this.qname = qname;
        fileSpecification = fileSpecGetter.getFileSpecification(compilationUnit.getAbsoluteFilename());
        path = fileSpecification.getPath();

        this.mxmlData = mxmlData;
        mxmlDialect = mxmlData.getMXMLDialect();
        this.fileScope = fileScope;
        this.problems = problems;
    }

    private final MXMLCompilationUnit compilationUnit;

    private final IFileSpecificationGetter fileSpecGetter;

    private final RoyaleProject project;
    
    private final ITypeDefinition factoryDef;

    private final ASProjectScope projectScope;

    private final Workspace workspace;

    private final IFileSpecification fileSpecification;

    private final String path;

    private final IMXMLData mxmlData;

    private final MXMLDialect mxmlDialect;

    private final String qname;

    private final MXMLFileScope fileScope;

    private final Collection<ICompilerProblem> problems;

    private MXMLFileNode fileNode;

    private IVariableDefinition percentProxyDefinition;

    /**
     * Gets the compilation unit which is building the MXML tree.
     * 
     * @return An {@code MXMLCompilationUnit} object.
     */
    public MXMLCompilationUnit getCompilationUnit()
    {
        return compilationUnit;
    }

    /**
     * Gets the project which is building the MXML tree.
     * 
     * @return A {@code RoyaleProject} object.
     */
    public RoyaleProject getProject()
    {
        return project;
    }

    /**
     * Gets the project scope for building the MXML tree.
     * 
     * @return A {@code ASProjectScope} object.
     */
    public ASProjectScope getProjectScope()
    {
        return projectScope;
    }

    /**
     * Gets the workspace which is building the MXML tree.
     * 
     * @return A {@code Workspace} object.
     */
    public Workspace getWorkspace()
    {
        return workspace;
    }

    /**
     * Gets the file specification of the MXML file.
     * 
     * @return The file specification as an {@code IFileSpecification} object.
     */
    public IFileSpecification getFileSpecification()
    {
        return fileSpecification;
    }

    /**
     * Gets the path of the MXML file.
     * 
     * @return The path as a String.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Gets the DOM representation of the MXML file.
     * 
     * @return An {@code MXMLData} object.
     */
    public IMXMLData getMXMLData()
    {
        return mxmlData;
    }

    /**
     * Gets the version of MXML used by the MXML file.
     * 
     * @return An {@code MXMLDialect} object.
     */
    public MXMLDialect getMXMLDialect()
    {
        return mxmlDialect;
    }

    /**
     * Gets the fully-qualified classname for the MXML file.
     * 
     * @return The classname as a String.
     */
    public String getQName()
    {
        return qname;
    }

    /**
     * Gets the file scope previously built for thsi compilation unit.
     * 
     * @return An {@code MXMLFileScope} object.
     */
    public MXMLFileScope getFileScope()
    {
        return fileScope;
    }

    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    public void addProblem(ICompilerProblem problem)
    {
        problems.add(problem);
    }

    public MXMLFileNode getFileNode()
    {
        return fileNode;
    }

    public IVariableDefinition getPercentProxyDefinition()
    {
        return percentProxyDefinition;
    }

    public ITypeDefinition getBuiltinType(String name)
    {
        return (ITypeDefinition)projectScope.findDefinitionByName(name);
    }

    /**
     * Builds an MXML tree.
     * 
     * @return An {@code IOldMXMLFileNode} that is the root of the MXML tree.
     */
    public IMXMLFileNode build()
    {
        fileNode = new MXMLFileNode();

        try
        {
            fileNode.initialize(this);
        }
        catch (Exception e)
        {
            // Something went wrong, so log it.  
            ICompilerProblem problem = new UnexpectedExceptionProblem(e);
            addProblem(problem);
        }

        return fileNode;
    }

    /**
     * Parses the specified text as a value of the specified type and returns a
     * Java object representing its value.
     * 
     * @param type The type of the value, as a String. This should be one of the
     * builtin types <code>"Boolean"</code>, <code>"int"</code>,
     * <code>"uint"</code>, <code>"Number"</code>, <code>"String"</code>,
     * <code>"Class"</code>, <code>"Function"</code>, <code>"RegExp"</code>,
     * <code>"Array"</code>, <code>"Object"</code>, or <code>"*"</code>.
     * @param text The text to be parsed.
     * @param flags Flags determining the details of the parsing algorithms.
     * @return A Java <code>Boolean</code>, <code>Integer</code>,
     * <code>Long</code>, <code>Number</code>, String</code>, or
     * <code>List&lt;Object&gt;</code>, if the text could be parsed as the
     * expected type. (If <code>List&lt;Object&gt;</code>, the elements of the
     * list are <code>Boolean</code>, <code>Integer</code>, <code>Long</code>,
     * <code>Number</code>, <code>String</code>, or, recursively, another list.)
     * If the text could not be parsed as the expected type, returns
     * <code>null</code>.
     */
    private Object parseValue(IMXMLNode propertyNode, ITypeDefinition type,
                              String text, EnumSet<TextParsingFlags> flags)
    {
        Object result = null;

        String typeName = type.getQualifiedName();

        if (typeName.equals(IASLanguageConstants.Boolean))
        {
            result = mxmlDialect.parseBoolean(project, text, flags);
        }
        else if (typeName.equals(IASLanguageConstants._int))
        {
            result = mxmlDialect.parseInt(project, text, flags);
            if (result == null)
                result = parsePercent(project, propertyNode, text, flags);
        }
        else if (typeName.equals(IASLanguageConstants.uint))
        {
            result = mxmlDialect.parseUint(project, text, flags);
            if (result == null)
                result = parsePercent(project, propertyNode, text, flags);
        }
        else if (typeName.equals(IASLanguageConstants.Number))

        {
            result = mxmlDialect.parseNumber(project, text, flags);
            if (result == null)
                result = parsePercent(project, propertyNode, text, flags);
        }
        else if (typeName.equals(IASLanguageConstants.String))
        {
            result = mxmlDialect.parseString(project, text, flags);
        }
        else if (typeName.equals(IASLanguageConstants.Array))
        {
            result = mxmlDialect.parseArray(project, text, flags);
            if (result == null && flags.contains(TextParsingFlags.RICH_TEXT_CONTENT))
            {
                result = mxmlDialect.parseString(project, text, flags);
                if (result != null)
                {
                	ArrayList<Object> arr = new ArrayList<Object>();
                	arr.add(result);
                	result = arr;
                }
            }
        }
        else if (typeName.equals(IASLanguageConstants.Object) ||
                 typeName.equals(IASLanguageConstants.ANY_TYPE))
        {
            result = mxmlDialect.parseObject(project, text, flags);
        }

        return result;
    }

    private Number parsePercent(RoyaleProject project, IMXMLNode propertyNode,
            String s, EnumSet<TextParsingFlags> flags)
    {
        if (flags != null && flags.contains(TextParsingFlags.ALLOW_PERCENT))
        {
            int percentIndex = s.indexOf('%');
            if (percentIndex == -1)
                return null;

            IVariableDefinition percentProxyDefinition =
                    propertyNode instanceof IMXMLPropertySpecifierNode ?
                            ((IMXMLPropertySpecifierNode)propertyNode).getPercentProxyDefinition(project) :
                            null;

            if (percentProxyDefinition != null)
            {
                Matcher m = percentagePattern.matcher(s);
                String match = m.matches() ? m.group(1) + '%' : null;

                if (match != null)
                {
                    this.percentProxyDefinition = percentProxyDefinition;

                    return Double.valueOf(s.substring(0, percentIndex));
                }
                else
                {
                    ICompilerProblem problem = new MXMLInvalidPercentageProblem(
                            (SourceLocation)propertyNode, propertyNode.getName(), s);
                    problems.add(problem);
                }
            }
            else
            {
                ICompilerProblem problem = new MXMLPercentageNotAllowedProblem(
                        (SourceLocation)propertyNode, propertyNode.getName(), s);
                problems.add(problem);
            }
        }

        return null;
    }

    /**
     * Parses the specified text as a value of the specified type and returns an
     * MXML instance node representing the value.
     * 
     * @param parent The parent node for the instance node.
     * @param type The type of the value, as a String. This should be one of the
     * builtin types <code>"Boolean"</code>, <code>"int"</code>,
     * <code>"uint"</code>, <code>"Number"</code>, <code>"String"</code>,
     * <code>"Class"</code>, <code>"Function"</code>, <code>"RegExp"</code>,
     * <code>"Array"</code>, <code>"Object"</code>, or <code>"*"</code>.
     * @param text The text to be parsed.
     * @param flags Flags determining the details of the parsing algorithms.
     * @return An {@link IMXMLInstanceNode} representing the value. It will be an
     * {@link IMXMLBooleanNode}, {@link IMXMLIntNode}, {@link IMXMLUintNode},
     * {@link IMXMLNumberNode}, {@link IMXMLStringNode}, or
     * {@link IMXMLArrayNode}.
     */
    private MXMLLiteralNode createLiteralNode(IMXMLNode propertyNode, ITypeDefinition type,
                                              ISourceFragment[] fragments,
                                              ISourceLocation location,
                                              EnumSet<TextParsingFlags> flags,
                                              Object defaultValue)
    {
        String text = SourceFragmentsReader.concatLogicalText(fragments);
        if (propertyNode != null && propertyNode.getName().equals("innerHTML"))
        	text = SourceFragmentsReader.concatPhysicalText(fragments);

        Object value = mxmlDialect.isWhitespace(text) ?
                       defaultValue :
                       parseValue(propertyNode, type, text, flags);

        if (value == null)
        {
            String typeName = type.getQualifiedName();
            if (typeName.equals(IASLanguageConstants.String) ||
                typeName.equals(IASLanguageConstants.Object) ||
                typeName.equals(IASLanguageConstants.ANY_TYPE))
            {
                value = "";
            }
            if (typeName.equals(IASLanguageConstants.Number) ||
                typeName.equals(IASLanguageConstants._int) ||
                typeName.equals(IASLanguageConstants.uint) ||
                typeName.equals(IASLanguageConstants.Boolean))
            {
                return null;
            }
        }
        if (value == null)
        {
            // we can't parse null from text alone
            // to pass null, you need to use binding like {null}
            // with that in mind, a null value here means that the text is
            // invalid for the specified type
            return null;
        }
        
        MXMLLiteralNode literalNode = new MXMLLiteralNode(null, value);
        literalNode.setSourceLocation(location);
        return literalNode;
    }

    public NodeBase parseExpressionNode(ITypeDefinition type,
                                        ISourceFragment[] fragments,
                                        ISourceLocation location,
                                        EnumSet<TextParsingFlags> flags,
                                        Object defaultValue,
                                        MXMLClassDefinitionNode classNode,
                                        boolean postProcess)
    {
        SourceFragmentsReader reader = new SourceFragmentsReader(location.getSourcePath(), fragments);

        IProjectConfigVariables projectConfigVariables =
                getProject().getProjectConfigVariables();

        ExpressionNodeBase expressionNode = ASParser.parseExpression(getWorkspace(),
                reader, problems, projectConfigVariables, location);

        if (expressionNode == null)
            return null;

        // We found an expression.
        // Temporarily make it a child of the class node so that
        // we can run post-process.
        expressionNode.setParent(classNode);
        //((MXMLExpressionNodeBase)parent).setExpressionNode(expressionNode);

        // Post-process the expression nodes.
        if (postProcess)
            postProcess(expressionNode, classNode);

        return expressionNode;
    }

    private void postProcess(NodeBase node, IMXMLClassDefinitionNode classNode)
    {
        final EnumSet<PostProcessStep> postProcessSteps = EnumSet.of(
                PostProcessStep.CALCULATE_OFFSETS,
                PostProcessStep.RECONNECT_DEFINITIONS);

        final ASScope classScope =
                (ASScope)classNode.getClassDefinition().getContainedScope();

        node.runPostProcess(postProcessSteps, classScope);
    }

    /**
     * Creates a databinding node, a class directive node, or a literal node.
     */
    public NodeBase createExpressionNode(IMXMLNode propertyNode, ITypeDefinition type,
                                         ISourceFragment[] fragments,
                                         ISourceLocation location,
                                         EnumSet<TextParsingFlags> flags,
                                         Object defaultValue,
                                         MXMLClassDefinitionNode classNode)
    {
        NodeBase expressionNode = null;

        // Look for databindings.
        if (flags.contains(TextParsingFlags.ALLOW_BINDING))
        {
            Object result = MXMLDataBindingParser.parse(
                    null, location, fragments,
                    problems, workspace, mxmlDialect, project);

            if (result instanceof IMXMLDataBindingNode)
            {
                expressionNode = (NodeBase)result;

                // Record on the class definition node
                // that we found a databinding.
                classNode.setHasDataBindings();
            }
        }

        // Look for compiler directives such as @Embed, @Resource, and @Clear.
        if (expressionNode == null)
        {
            if (flags.contains(TextParsingFlags.ALLOW_COMPILER_DIRECTIVE))
            {
                String text = SourceFragmentsReader.concatLogicalText(fragments);

                expressionNode = MXMLCompilerDirectiveParser.parse(
                        this, propertyNode, location, text, type);
            }
        }

        String typeName = (type != null) ? type.getQualifiedName() : "";

        if (expressionNode == null)
        {
            // Class, Function, and RegExp values get parsed by the ActionScript compiler,
            // to support complex syntax such as Vector.<Vector.<flash.display.Sprite>>
            // as a Class or function(n:int}:int { return n * n } as a Function.
            if (typeName.equals(IASLanguageConstants.Class) ||
                typeName.equals(IASLanguageConstants.Function) ||
                typeName.equals(IASLanguageConstants.RegExp))
            {
                expressionNode = parseExpressionNode(
                        type, fragments, location, flags, defaultValue, classNode, false);
            }
        }

        // Look for other primitive values.
        if (expressionNode == null)
            expressionNode = createLiteralNode(propertyNode, type, fragments, location, flags, defaultValue);

        if (expressionNode == null)
        {
            String text = SourceFragmentsReader.concatLogicalText(fragments);
            ICompilerProblem problem = new MXMLInvalidTextForTypeProblem(location, text, typeName);
            addProblem(problem);
        }

        return expressionNode;
    }

    /**
     * Parses the specified source fragments as a databinding, or a compiler
     * directive, or a textual value of the specified type and returns an MXML
     * instance node representing the result.
     * 
     * @param parent The parent node for the instance node.
     * @param type The type of the value, as a String. This should be one of the
     * builtin types <code>"Boolean"</code>, <code>"int"</code>,
     * <code>"uint"</code>, <code>"Number"</code>, <code>"String"</code>,
     * <code>"Class"</code>, <code>"Function"</code>, <code>"RegExp"</code>,
     * <code>"Array"</code>, <code>"Object"</code>, or <code>"*"</code>.
     * @param flags Flags determining the details of the parsing algorithms.
     * @return An {@link IMXMLInstanceNode} representing the value. It will be
     * one of the following:
     * <ul>
     * <li>{@link IMXMLBooleanNode}</li>
     * <li>{@link IMXMLIntNode}</li>
     * <li>{@link IMXMLUintNode}</li>
     * <li>{@link IMXMLNumberNode}</li>
     * <li>{@link IMXMLStringNode}</li>
     * <li>{@link IMXMLArrayNode}</li>
     * <li>{@link IMXMLClassNode}</li>
     * <li>{@link IMXMLFunctionNode}</li>
     * <li>{@link IMXMLRegExpNode}</li>
     * <li>{@link IMXMLFactoryNode}</li>
     * <li>{@link IMXMLDeferredInstanceNode}</li>
     * <li>{@link IMXMLSingleDataBindingNode}</li>
     * <li>{@link IMXMLConcatenatedDataBindingNode}</li>
     * <li>{@link IMXMLEmbedNode}</li>
     * <li>{@link IMXMLResourceNode}</li>
     * </ul>
     */
    public MXMLInstanceNode createInstanceNode(NodeBase parent, ITypeDefinition type,
                                               ISourceFragment[] fragments,
                                               ISourceLocation location,
                                               EnumSet<TextParsingFlags> flags,
                                               MXMLClassDefinitionNode classNode)
    {
        MXMLInstanceNode instanceNode = null;

        // If parseValue() parses a percentage value for a property 
        // of type int/uint/Number with [PercentProxy(...)] metadata,
        // it will set this field to the definition of the property
        // specified by the metadata, so that MXMLPropertySpecifierNode
        // can change which property definition it refers to.
        percentProxyDefinition = null;

        String typeName = (type != null) ? type.getQualifiedName() : "";

        // For a property of type IFactory, create an MXMLFactoryNode.
        if (type != null && type.isInstanceOf(factoryDef, project))
        {
            if (flags.contains(TextParsingFlags.ALLOW_BINDING))
            {
                Object result = MXMLDataBindingParser.parse(
                        null, location, fragments,
                        problems, workspace, mxmlDialect, project);

                if (result instanceof IMXMLDataBindingNode)
                {
                    // Record on the class definition node
                    // that we found a databinding.
                    classNode.setHasDataBindings();
                    
                    instanceNode = (MXMLInstanceNode)result;
                }
            }
            if (instanceNode == null)
            {
                instanceNode = new MXMLFactoryNode(parent);
                ((MXMLFactoryNode)instanceNode).initializeFromFragments(
                    this, location, fragments);
            }
        }

        // For a property of type IDeferredInstance or ITransientDeferredInstance,
        // create an MXMLDeferredInstanceNode.
        else if (typeName.equals(project.getDeferredInstanceInterface()) ||
                 typeName.equals(project.getTransientDeferredInstanceInterface()))
        {
            instanceNode = new MXMLDeferredInstanceNode(parent);
            // setClassReference() will get called later
            // because the value depends on the child node
            ((MXMLDeferredInstanceNode)instanceNode).initializeFromFragments(
                    this, location, fragments);
        }

        else
        {
            NodeBase expressionNode = createExpressionNode(
                    (IMXMLNode)parent, type, fragments, location, flags, null, classNode);

            // If we produced a databinding node or a class directive node,
            // those are already instance nodes.
            if (expressionNode instanceof MXMLInstanceNode)
            {
                instanceNode = (MXMLInstanceNode)expressionNode;
            }

            // If we produced a literal node,
            // it needs to be wrapped with the appropriate instance node.
            else if (expressionNode instanceof MXMLLiteralNode)
            {
                Object value = ((MXMLLiteralNode)expressionNode).getValue();

                if (value instanceof Boolean)
                {
                    instanceNode = new MXMLBooleanNode(parent);
                    ((MXMLBooleanNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.Boolean, expressionNode);
                }
                else if (value instanceof Integer)
                {
                    instanceNode = new MXMLIntNode(parent);
                    ((MXMLIntNode)instanceNode).initialize(
                            this, location, IASLanguageConstants._int, expressionNode);
                }
                else if (value instanceof Long)
                {
                    instanceNode = new MXMLUintNode(parent);
                    ((MXMLUintNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.uint, expressionNode);
                }
                else if (value instanceof Number)
                {
                    instanceNode = new MXMLNumberNode(parent);
                    ((MXMLNumberNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.Number, expressionNode);
                }
                else if (value instanceof String || value == null)
                {
                    instanceNode = new MXMLStringNode(parent);
                    ((MXMLStringNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.String, expressionNode);
                }
                else if (value instanceof List<?>)
                {
                    instanceNode = new MXMLArrayNode(parent);
                    ((MXMLArrayNode)instanceNode).initialize(this, location, (List<?>)value);
                }
                else
                {
                    assert false;
                }
            }

            else
            {
                if (typeName.equals(IASLanguageConstants.Class))
                {
                    instanceNode = new MXMLClassNode(parent);
                    ((MXMLClassNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.Class, expressionNode);
                    postProcess(instanceNode, classNode);
                }
                else if (typeName.equals(IASLanguageConstants.Function))
                {
                    instanceNode = new MXMLFunctionNode(parent);
                    ((MXMLFunctionNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.Function, expressionNode);
                    postProcess(instanceNode, classNode);
                }
                else if (typeName.equals(IASLanguageConstants.RegExp))
                {
                    instanceNode = new MXMLRegExpNode(parent);
                    ((MXMLRegExpNode)instanceNode).initialize(
                            this, location, IASLanguageConstants.RegExp, expressionNode);
                    postProcess(instanceNode, classNode);
                }
            }
        }

        if (instanceNode != null)
            instanceNode.setParent(parent);

        return instanceNode;
    }
    
    /**
     * Reads an external file specified by a <code>source</code> attribute.
     * 
     * @param sourceAttribute The {@link IMXMLTagAttributeData} representing the
     * <code>source</code> attribute.
     * @param resolvedSourcePath The path to the file specified by the attribute,
     * resolved and normalized.
     * @return A String containing the contents of the file, or <code>null</code>
     * if the file does not exist or cannot be read.
     */
    public String readExternalFile(IMXMLTagAttributeData sourceAttribute,
                                   String resolvedSourcePath)
    {
        final IFileSpecificationGetter fileSpecGetter = getFileSpecificationGetter();
        IFileSpecification sourceFileSpec =
                fileSpecGetter.getFileSpecification(resolvedSourcePath);
        Reader sourceFileReader;
        try
        {
            sourceFileReader = sourceFileSpec.createReader();
        }
        catch (FileNotFoundException e)
        {
            ICompilerProblem problem =
                    new MXMLInvalidSourceAttributeProblem(sourceAttribute, resolvedSourcePath);
            addProblem(problem);
            return null;
        }
        String text;
        try
        {
            text = IOUtils.toString(sourceFileReader);
            IOUtils.closeQuietly(sourceFileReader);
        }
        catch (IOException e)
        {
            // Report file can't be read.
            return null;
        }
        return text;
    }

    /**
     * Gets the {@link IMXMLData} representation of an external file specified by
     * a <code>source</code> attribute.
     * 
     * @param sourceAttribute The {@link IMXMLTagAttributeData} representing the
     * <code>source</code> attribute.
     * @param resolvedSourcePath The path to the file specified by the attribute,
     * resolved and normalized.
     * @return An {@link IMXMLData} object representing the contents of the file,
     * or <code>null</code> if the file does not exist or cannot be read.
     */
    public IMXMLData getExternalMXMLData(IMXMLTagAttributeData sourceAttribute,
                                         String resolvedSourcePath)
    {
        File file = new File(resolvedSourcePath);

        if (!file.exists())
        {
            ICompilerProblem problem =
                    new MXMLInvalidSourceAttributeProblem(sourceAttribute, resolvedSourcePath);
            addProblem(problem);
            return null;
        }

        Workspace workspace = getWorkspace();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLTreeBuilder waiting for lock in getExternalMXMLData");
        IFileSpecification sourceFileSpec =
                workspace.getFileSpecification(resolvedSourcePath);
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLTreeBuilder done with lock in getExternalMXMLData");
        IMXMLDataManager mxmlDataManager = workspace.getMXMLDataManager();

        return mxmlDataManager.get(sourceFileSpec);
    }

    /**
     * Adds a dependency from the current compilation unit to the compilation
     * unit for the specified qualified name.
     * 
     * @param qname A fully-qualified name.
     * @param type The type of dependency to add.
     */
    public void addDependency(String qname, DependencyType type)
    {
        if (qname == null)
            return;

        ASProjectScope projectScope = getProjectScope();
        IDefinition definition = projectScope.findDefinitionByName(qname);

        if (definition == null)
            return;

        ICompilationUnit thisCU = getCompilationUnit();
        ICompilationUnit otherCU = projectScope.getCompilationUnitForDefinition(definition);

        // otherCU will be null if qname is "*" because the "*" type does not come from any compilation unit
        if (otherCU != null)
        {
            RoyaleProject project = getProject();
            project.addDependency(thisCU, otherCU, type, qname);
        }
    }

    /**
     * Adds an expression dependency from the current compilation unit to the
     * compilation unit for the specified qualified name.
     * 
     * @param qname A fully-qualified name.
     */
    public void addExpressionDependency(String qname)
    {
        addDependency(qname, DependencyType.EXPRESSION);
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return fileNode != null ? fileNode.toString() : "";
    }

    public final IFileSpecificationGetter getFileSpecificationGetter()
    {
        return fileSpecGetter;
    }
}
