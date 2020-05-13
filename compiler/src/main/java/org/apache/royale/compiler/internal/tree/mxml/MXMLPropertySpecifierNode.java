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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLData;
import org.apache.royale.compiler.internal.mxml.MXMLDialect.TextParsingFlags;
import org.apache.royale.compiler.internal.mxml.MXMLTagData;
import org.apache.royale.compiler.internal.mxml.MXMLTextData;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.*;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;

/**
 * {@code MXMLEventSpecifierNode} represents an MXML event attribute or event
 * child tag.
 * <p>
 * Its child nodes are ActionScript nodes representing the statements to be
 * executed.
 * <p>
 * For example, the MXML code
 * 
 * <pre>
 * &lt;s:Button click="doThis(); doThat()"/&gt;
 * </pre>
 * 
 * or
 * 
 * <pre>
 * &lt;s:Button&gt;
 *     &lt;s:click&gt;
 *     &lt;![CDATA[
 *         doThis();
 *         doThat();
 *     ]]&gt;
 *     &lt;/s:click&gt;
 * &lt;/s:Button&gt;
 * </pre>
 * 
 * produces an AST of the form
 * 
 * <pre>
 * MXMLInstanceNode "spark.components.Button"
 *   MXMLEventSpecifierNode "click"
 *     FunctionCallNode
 *       IdentifierNode "doThis"
 *       ContainerNode
 *     FunctionCallNode
 *       IdentifierNode "doThat"
 *       ContainerNode
 * </pre>
 */
class MXMLPropertySpecifierNode extends MXMLSpecifierNodeBase implements IMXMLPropertySpecifierNode
{
    private static final EnumSet<TextParsingFlags> FLAGS = EnumSet.of(
            TextParsingFlags.ALLOW_ARRAY,
            TextParsingFlags.ALLOW_BINDING,
            TextParsingFlags.ALLOW_COLOR_NAME,
            TextParsingFlags.ALLOW_COMPILER_DIRECTIVE,
            TextParsingFlags.ALLOW_ESCAPED_COMPILER_DIRECTIVE,
            TextParsingFlags.ALLOW_PERCENT);

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLPropertySpecifierNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The name of the property, if it is a dynamic property. Otherwise, this is
     * <code>null</code>.
     */
    private String dynamicName;

    /**
     * The sole child node, which is the value of the property.
     */
    private MXMLInstanceNode instanceNode;
    /*
     * Flag to track whether an instance problem has already been logged (multiple children)
     */
    private Boolean firstInstanceProblemAdded = false;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLPropertySpecifierID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? instanceNode : null;
    }

    @Override
    public int getChildCount()
    {
        return instanceNode != null ? 1 : 0;
    }

    @Override
    public IMXMLInstanceNode getInstanceNode()
    {
        return instanceNode;
    }

    void setInstanceNode(MXMLInstanceNode instanceNode)
    {
        this.instanceNode = instanceNode;
    }

    private ITypeDefinition getPropertyType(MXMLTreeBuilder builder)
    {
        IDefinition definition = getDefinition();

        RoyaleProject project = builder.getProject();

        // If there is no property definition, this is a dynamic property with type "*".
        if (definition == null)
            return (ITypeDefinition)project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);

        return definition.resolveType(project);
    }

    protected String getPropertyTypeName(MXMLTreeBuilder builder)
    {
        ITypeDefinition propertyType = getPropertyType(builder);

        // If there is no property definition, this is a dynamic property with type "*".
        if (propertyType == null)
            return IASLanguageConstants.ANY_TYPE;

        return propertyType.getQualifiedName();
    }

    void setDynamicName(String dynamicName)
    {
        this.dynamicName = dynamicName;
    }

    @Override
    public String getName()
    {
        return dynamicName != null ? dynamicName : super.getName();
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    /**
     * This override handles a property attribute like label="OK".
     */
    @Override
    protected void initializeFromAttribute(MXMLTreeBuilder builder,
                                           IMXMLTagAttributeData attribute,
                                           MXMLNodeInfo info)
    {
        super.initializeFromAttribute(builder, attribute, info);

        // Break the attribute value into fragments.
        // Each entity is a separate fragment.
        Collection<ICompilerProblem> problems = builder.getProblems();
        ISourceFragment[] fragments = attribute.getValueFragments(problems);

        info.addSourceFragments(attribute.getSourcePath(), fragments);

        // parse out text and make correct kind of child node
        processFragments(builder, attribute, info);

        info.clearFragments();

        validateProperty(builder, attribute);
    }

    private void processFragments(MXMLTreeBuilder builder,
                                  ISourceLocation sourceLocation,
                                  MXMLNodeInfo info)
    {
        ITypeDefinition propertyType = getPropertyType(builder);

        ISourceFragment[] fragments = info.getSourceFragments();

        ISourceLocation location = info.getSourceLocation();
        if (location == null)
            location = sourceLocation;

        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();

        EnumSet<TextParsingFlags> flags = FLAGS.clone();

        IDefinition definition = getDefinition();
        RoyaleProject project = builder.getProject();
        if (definition instanceof IVariableDefinition)
        {
            if (((IVariableDefinition)definition).hasCollapseWhiteSpace(project))
                flags.add(TextParsingFlags.COLLAPSE_WHITE_SPACE);
            if (((IVariableDefinition)definition).hasRichTextContent(project))
                flags.add(TextParsingFlags.RICH_TEXT_CONTENT);
        }

        instanceNode = builder.createInstanceNode(
                this, propertyType, fragments, location, flags, classNode);

        // If we don't have a value, we can't do codegen.
        if (instanceNode == null)
            markInvalidForCodeGen();

        // createInstanceNode() may have parsed a percentage value
        // for a property with [PercentProxy(...)] metadata.
        // In that case, this property node is actually for
        // a different property than appears in MXML,
        // such as width="100%" really meaning percentWidth="100",
        // so make this node refer the definition of the proxy property.
        IDefinition percentProxyDefinition = builder.getPercentProxyDefinition();
        if (percentProxyDefinition != null)
            setDefinition(percentProxyDefinition);
    }

    /**
     * This override makes sure that array and vectors have a child tag
     * of fx:Array or fx:Vector, or fakes that condition.
     */
    @Override
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        String propertyTypeName = getPropertyTypeName(builder);

        if (propertyTypeName.contains(IASLanguageConstants.Vector + ".<") ||
                propertyTypeName.equals(IASLanguageConstants.Array))
        {
            // Process each content unit.
            for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
            {
                if (unit instanceof IMXMLTagData)
                {
                    IMXMLTagData unitTag = (IMXMLTagData)unit;
                    IDefinition definition = builder.getFileScope().resolveTagToDefinition(unitTag);
                    if (propertyTypeName.contains(IASLanguageConstants.Vector + ".<") &&
                            !definition.getQualifiedName().contains(IASLanguageConstants.Vector + ".<"))
                    {
                        initializeDefaultProperty(builder, (IVariableDefinition)getDefinition(), 
                                tag, getListOfUnits(tag));
                        return;
                    }
                    else if (propertyTypeName.equals(IASLanguageConstants.Array) && (definition != null) &&
                            !definition.getQualifiedName().equals(IASLanguageConstants.Array))
                    {
                        initializeDefaultProperty(builder, (IVariableDefinition)getDefinition(), 
                                tag, getListOfUnits(tag));
                        return;                        
                    }
                }
            }
        }
        else if (propertyTypeName.contains(IASLanguageConstants.Object))
        {
            // Process each content unit.
            for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
            {
                if (unit instanceof IMXMLTagData)
                {
                    initializeDefaultProperty(builder, (IVariableDefinition)getDefinition(), 
                                tag, getListOfUnits(tag));
                    return;                        
                }
            }
        }
        super.initializeFromTag(builder, tag);
    }
    
    List<IMXMLUnitData> getListOfUnits(IMXMLTagData tag)
    {
        ArrayList<IMXMLUnitData> list = new ArrayList<IMXMLUnitData>();
        
        // Process each content unit.
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            list.add(unit);
        }
        return list;
    }
    
    /**
     * This override handles text specifying a default property.
     */
    @Override
    protected void initializeFromText(MXMLTreeBuilder builder,
                                      IMXMLTextData text,
                                      MXMLNodeInfo info)
    {
        super.initializeFromText(builder, text, info);

        // use helpers to set offsets on fragments and create child node of correct type
        accumulateTextFragments(builder, text, info);

        processFragments(builder, text, info);
    }

    void initializeDefaultProperty(MXMLTreeBuilder builder, IVariableDefinition defaultPropertyDefinition,
                                   IMXMLTagData parentTag, List<IMXMLUnitData> contentUnits)
    {
        RoyaleProject project = builder.getProject();

        assert (contentUnits.isEmpty()) ||
                (!builder.getFileScope().isScriptTag(contentUnits.get(0))) : "Script tags should not start a default property!";

        assert (contentUnits.isEmpty()) ||
                (!builder.getFileScope().isScriptTag(contentUnits.get(contentUnits.size() - 1))) : "Trailing script tags should be removed from default property content units!";

        // Set the location of the default property node
        // to span the tags that specify the default property value.
        setLocation(builder, contentUnits);

        String propertyTypeName = getPropertyTypeName(builder);
        
        int numChildTags = 0;
        IMXMLTagData oneAndOnlyChildTag = null;
    	boolean childrenAreClasses = true;
    	for (IMXMLUnitData unit : contentUnits)
    	{
    		if (unit instanceof IMXMLTagData)
    		{
    			oneAndOnlyChildTag = (IMXMLTagData)unit;
    			numChildTags++;
                IDefinition definition = builder.getFileScope().resolveTagToDefinition(oneAndOnlyChildTag);
                if (!(definition instanceof ClassDefinition))
                {
                	childrenAreClasses = false;
                }
    		}
    	}
    	boolean oneAndOnlyChildIsClass = false;
    	if (numChildTags == 1)
    	{
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(oneAndOnlyChildTag);
            if (definition instanceof ClassDefinition)
                oneAndOnlyChildIsClass = true;    		
    	}

        // If the property is of type IDeferredInstance or ITransientDeferredInstance,
        // create an implicit MXMLDeferredInstanceNode.
        if (propertyTypeName.equals(project.getDeferredInstanceInterface()) ||
            propertyTypeName.equals(project.getTransientDeferredInstanceInterface()))
        {
            instanceNode = new MXMLDeferredInstanceNode(this);
            ((MXMLDeferredInstanceNode)instanceNode).initializeDefaultProperty(
                    builder, defaultPropertyDefinition, contentUnits);
        }
        else if ((propertyTypeName.equals(IASLanguageConstants.Array) && 
                oneChildIsNotArray(builder, contentUnits)) ||
                (propertyTypeName.equals(IASLanguageConstants.Object) && childrenAreClasses && !oneAndOnlyChildIsClass))
        {
            // Create an implicit array node.
            instanceNode = new MXMLArrayNode(this);
            ((MXMLArrayNode)instanceNode).initializeDefaultProperty(
                    builder, defaultPropertyDefinition, contentUnits);
        }
        else if (propertyTypeName.equals(IASLanguageConstants.Object) && !oneAndOnlyChildIsClass)
        {
            // Create an implicit Object node.
            instanceNode = new MXMLObjectNode(this);
            ((MXMLObjectNode)instanceNode).initialize(
                    builder, parentTag, contentUnits, createNodeInfo(builder));
        }
        else if (propertyTypeName.contains(IASLanguageConstants.Vector + ".<") && 
                oneChildIsNotVector(builder, contentUnits))
        {
            // Create an implicit array node.
            instanceNode = new MXMLVectorNode(this);
            ((MXMLVectorNode)instanceNode).initializeDefaultProperty(
                    builder, defaultPropertyDefinition, contentUnits);
        }
        else if (oneAndOnlyChildTag != null)
        {
            IMXMLTagData tag = oneAndOnlyChildTag;
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
            if (definition instanceof ClassDefinition)
            {
                instanceNode = MXMLInstanceNode.createInstanceNode(
                        builder, definition.getQualifiedName(), this);
                instanceNode.setClassReference(project, (ClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                instanceNode.initializeFromTag(builder, tag);
            }
            else if (definition == null && defaultPropertyDefinition.getBaseName().equals("html"))
            {
            	String text = ((MXMLTagData)tag).stringify();
            	MXMLToken textToken = new MXMLToken(MXMLTokenTypes.TOKEN_TEXT, 
            										tag.getStart(), tag.getEnd(),
            										tag.getLine(), tag.getColumn(), text);
            	MXMLTextData textData = new MXMLTextData(textToken);
            	textData.setLocation((MXMLData) tag.getParent(), tag.getIndex());
            	initializeFromText(builder, textData, createNodeInfo(builder));
            }
        }
    }
    
    private boolean oneChildIsNotArray(MXMLTreeBuilder builder, List<IMXMLUnitData> contentUnits) 
    {
        if (contentUnits.size() != 1)
            return true;
        
        if (contentUnits.get(0) instanceof IMXMLTagData)
        {
            IMXMLTagData tag = (IMXMLTagData)contentUnits.get(0);
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
            if (definition.getQualifiedName().equals(IASLanguageConstants.Array))
                return false;
        }
        return true;
    }

    private boolean oneChildIsNotVector(MXMLTreeBuilder builder, List<IMXMLUnitData> contentUnits) 
    {
        if (contentUnits.size() != 1)
            return true;
        
        if (contentUnits.get(0) instanceof IMXMLTagData)
        {
            IMXMLTagData tag = (IMXMLTagData)contentUnits.get(0);
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
            if (definition.getQualifiedName().contains(IASLanguageConstants.Vector + ".<"))
                return false;
        }
        return true;
    }

    /**
     * This override handles a child tag in a property tag, such as
     * <label><String>OK</String></label>.
     */
    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        MXMLFileScope fileScope = builder.getFileScope();
        //use a local variable until we are sure that this is a valid instance node
        MXMLInstanceNode instanceNode = null;

        // Check whether the tag is an <fx:Component> tag.
        if (fileScope.isComponentTag(childTag))
        {
            instanceNode = new MXMLComponentNode(this);
            instanceNode.initializeFromTag(builder, childTag);
        }
        else
        {
            String propertyTypeName = getPropertyTypeName(builder);
            RoyaleProject project = builder.getProject();

            // If the property is of type IDeferredInstance or ITransientDeferredInstance,
            // create an implicit MXMLDeferredInstanceNode.
            if (propertyTypeName.equals(project.getDeferredInstanceInterface()) ||
                propertyTypeName.equals(project.getTransientDeferredInstanceInterface()))
            {
                instanceNode = new MXMLDeferredInstanceNode(this);
                instanceNode.initializeFromTag(builder, tag);
            }
            else
            {
                // Check whether the child tag is an instance tag
                IDefinition definition = builder.getFileScope().resolveTagToDefinition(childTag);
                if (definition instanceof ClassDefinition)
                {
                    instanceNode = MXMLInstanceNode.createInstanceNode(
                            builder, definition.getQualifiedName(), this);
                    instanceNode.setClassReference(project, (ClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                    instanceNode.initializeFromTag(builder, childTag);
                }
                else
                {
                    String uri = childTag.getURI();
                    if (uri != null && uri.equals(IMXMLLanguageConstants.NAMESPACE_MXML_2009))
                    {
                        instanceNode = MXMLInstanceNode.createInstanceNode(
                                builder, childTag.getShortName(), this);
                        instanceNode.setClassReference(project, childTag.getShortName());
                        instanceNode.initializeFromTag(builder, childTag);
                    }
                    else
                    {
                        ICompilerProblem problem = new MXMLUnresolvedTagProblem(childTag);
                        builder.addProblem(problem);
                    }
                }
            }

            ITypeDefinition assignToType = getPropertyType(builder);
            // if the assignToType is Array it is special-cased, and handled in initializationComplete method
            if (assignToType != project.getBuiltinType(IASLanguageConstants.BuiltinType.ARRAY)) {
                //otherwise if the type of the instance node is incompatible with the type of the property node,
                //or if there are multiple child tags
                //that's a problem
                if (instanceNode!=null && this.instanceNode==null &&
                    !(instanceNode.getClassReference(project).isInstanceOf(assignToType,project) ||
                      assignToType == project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE))) {
                    //we have a single value of incompatible type
                    ICompilerProblem problem = new MXMLBadChildTagPropertyAssignmentProblem(childTag,instanceNode.getClassReference(project).getQualifiedName(),propertyTypeName);
                    builder.addProblem(problem);
                    instanceNode=null;

                } else {
                    if (this.instanceNode!=null && instanceNode!=null) {
                        //we have a multiple values when we should only have one
                        if (!firstInstanceProblemAdded) {
                            //if we have multiple children problem scenario, we only encounter that on the 2nd childTag
                            //so start with a MXMLMultipleInitializersProblem instance for the first tag
                            ICompilerProblem problem = new MXMLMultipleInitializersProblem( tag.getFirstChild(false),getPropertyTypeName(builder));
                            builder.addProblem(problem);
                            firstInstanceProblemAdded=true;
                        }

                        ICompilerProblem problem = new MXMLMultipleInitializersProblem(childTag,getPropertyTypeName(builder));
                        builder.addProblem(problem);
                        instanceNode=null;
                    }
                }
            }
        }
        if (instanceNode!=null)
            this.instanceNode = instanceNode;
    }



    /**
     * This override is called on each non-whitespace unit of text inside a
     * property tag, such as <label>O<!-- comment -->K</label> which must
     * compile as if it had been written <label>OK</label>. All the text units
     * will be processed later in initializationComplete().
     */
    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        accumulateTextFragments(builder, text, info);
    }

    /**
     * This override is called on a property tag such as <label>O<!-- comment
     * -->K</label>. It concatenates all the text units to get "OK" and uses
     * that to specify the property value.
     */
    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        RoyaleProject project = builder.getProject();

        // If this property is type Array, and it didn't get set to an Array tag,
        // then create an implicit Array tag and initialize it from the
        // child tags of the property tag.
        IDefinition definition = getDefinition();
        if (definition != null && definition.getTypeAsDisplayString().equals(IASLanguageConstants.Array))
        {
            if (instanceNode == null || ((!(instanceNode instanceof MXMLArrayNode)) &&
                !instanceNode.getClassReference(project).getQualifiedName().equals(IASLanguageConstants.Array)))
            {
                instanceNode = new MXMLArrayNode(this);
                instanceNode.setClassReference(project, IASLanguageConstants.Array); // TODO Move to MXMLArrayNode
                ((MXMLArrayNode)instanceNode).initializeFromTag(builder, tag);
            }
        }

        if (instanceNode == null)
        {
            // use helpers for parse for bindings, @functions, create correct child node
            processFragments(builder, tag, info);
        }

        validateProperty(builder, tag);
    }

    @Override
    public IVariableDefinition getPercentProxyDefinition(RoyaleProject project)
    {
        // Get the name of the proxy property
        // from the [PercentProxy(...)] metadata.
        IVariableDefinition propertyDefinition = (IVariableDefinition)getDefinition();
        String percentProxy = propertyDefinition.getPercentProxy(project);
        if (percentProxy == null)
            return null;

        // Find the definition for the proxy property.
        ASScope classScope = (ASScope)propertyDefinition.getContainingScope();
        IClassDefinition classDefinition = (IClassDefinition)classScope.getDefinition();
        IDefinition proxyDefinition = classScope.getPropertyFromDef(
                project, classDefinition, percentProxy, false);
        if (!(proxyDefinition instanceof IVariableDefinition))
            return null;

        return (IVariableDefinition)proxyDefinition;
    }

    private void validateProperty(MXMLTreeBuilder builder, ISourceLocation source)
    {
        final IDefinition definition = getDefinition();
        if (definition instanceof IVariableDefinition || definition instanceof IAccessorDefinition)
        {
            MXMLFileScope fileScope = builder.getFileScope();
            Set<INamespaceDefinition> namespaceSet = new HashSet<INamespaceDefinition>(fileScope.getNamespaceSet(builder.getProject()));

            ITypeDefinition typeDef = (ITypeDefinition) definition.getParent();
            TypeScope typeScope = (TypeScope) typeDef.getContainedScope();

            ClassDefinition fileClassDef = fileScope.getMainClassDefinition();
            if(Arrays.asList(fileClassDef.resolveAncestry(builder.getProject())).contains(typeDef))
            {
                IClassDefinition current = fileClassDef;
                do
                {
                    namespaceSet.add(current.getProtectedNamespaceReference());
                    current = current.resolveBaseClass(builder.getProject());
                }
                while (current instanceof IClassDefinition);
            }

            List<IDefinition> foundDefs = new ArrayList<IDefinition>();
            typeScope.getPropertyForMemberAccess(builder.getProject(), foundDefs, definition.getBaseName(), namespaceSet, false);
            if(!foundDefs.contains(definition))
            {
                builder.addProblem(new InaccessiblePropertyReferenceProblem(this, definition.getBaseName(), typeDef.getBaseName()));
            }
        }
    }
}
