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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMXMLCoreConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IEffectDefinition;
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.mxml.MXMLTagData;
import org.apache.royale.compiler.internal.mxml.MXMLTextData;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDuplicateChildTagProblem;
import org.apache.royale.compiler.problems.MXMLUnresolvedTagProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;

/**
 * {@code MXMLClassReferenceNodeBase} is the abstract base class for AST nodes
 * that represent MXML tags which map to ActionScript classes
 * (either as instances of those classes
 * or as definitions of subclasses of those classes).
 */
abstract class MXMLClassReferenceNodeBase extends MXMLNodeBase implements IMXMLClassReferenceNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLClassReferenceNodeBase(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The class definition to which this node refers. For example,
     * <code>&lt;s:Button&gt;</code> typically refers to the class definition
     * for <code>spark.components.Button</code>. An {@code MXMLInstanceNode}
     * creates an instance of this class, while an
     * {@code MXMLClassDefinitionNode} declares a subclass of this class.
     */
    private IClassDefinition classReference;

    /**
     * A flag that keeps track of whether the node represents a class that
     * implements mx.core.IMXML.
     */
    private boolean isMXMLObject = false;

    /**
     * A flag that keeps track of whether the node represents an MX container
     * (i.e., an mx.core.IContainer).
     */
    private boolean isContainer = false;

    /**
     * A flag that keeps track of whether the node represents a visual element
     * container (i.e., an mx.core.IVisualElementContainer).
     */
    private boolean isVisualElementContainer = false;

    /**
     * A flag that keeps track of whether the node represents a UIComponent
     * supporting deferred instantiation.
     */
    private boolean isDeferredInstantiationUIComponent = false;

    /**
     * The child nodes of this node. For {@code MXMLInstanceNode} the children
     * will all be property/event/style specifiers. For
     * {@code MXMLClassDefinitionNode} the children may include other nodes such
     * as {@MXMLMetadataNode}, {@MXMLScriptNode
     * }, etc. If there are no children, this will be null.
     */
    private IMXMLNode[] children;

    /**
     * A map of the child nodes of this node which specify properties. The keys
     * are the property names. If there are no properties specified, this will
     * be null.
     */
    private Map<String, IMXMLPropertySpecifierNode> propertyNodeMap;
    
    /**
     * All child property nodes.  The propertyNodeMap only has the last value
     * specified for a property name.  There can be more than one value
     * specified if there is different values for different states.
     */
    private List<IMXMLPropertySpecifierNode> allPropertyNodes;

    /**
     * A map of child nodes of this node which specify events. The keys are the
     * event names. If there are no events specified, this will be null.
     */
    private Map<String, IMXMLEventSpecifierNode> eventNodeMap;

    /**
     * All child event nodes.  The eventNodeMap only has the last value
     * specified for a event name.  There can be more than one value
     * specified if there is different values for different states.
     */
    private List<IMXMLEventSpecifierNode> allEventNodes;
    
    /**
     * A map of suffix (specifying a state or state group) to the child nodes
     * with this suffix.
     */
    private Map<String, Collection<IMXMLSpecifierNode>> suffixSpecifierMap;

    /**
     * The definition of the default property. This gets lazily initialized by
     * {@code getDefaultPropertyDefinition()} if we need to know it.
     */
    private IVariableDefinition defaultPropertyDefinition;

    /**
     * The definition of an alternate default property. This gets lazily initialized by
     * {@code getDefaultPropertyDefinition()} if we need to know it.
     */
    private IVariableDefinition altDefaultPropertyDefinition;
    
    /**
     * A flag that keeps track of whether the {@code defaultPropertyDefinition}
     * field has been initialized. Simply checking whether it is
     * <code>null</code> doesn't work, because <code>null</code> means
     * "no default property" rather than "default property not determined yet".
     */
    private boolean defaultPropertyDefinitionInitialized = false;

    /**
     * A flag that keeps track of whether we are processing a content unit for
     * the default property. For example, you can have MXML like
     * 
     * <pre>
     * &lt;Application&gt;
     *     &lt;width&gt;100&lt;/width&gt;
     *     &lt;Button/&gt;
     *     &lt;Button/&gt;
     *     &lt;height&gt;100&lt;/height&gt;
     * &lt;/Application&gt;
     * </pre>
     * 
     * where the two <code>Button</code> tags specify an implicit array for the
     * <code>mxmlContentFactory</code> property. This flag is set true on the
     * first <code>Button</code> tag and then set back to false on the
     * <code>height</code> tag.
     */
    private boolean processingDefaultProperty = false;

    /**
     * A flag that keeps track of whether we have complete the processing of the
     * content units for the default property, so that we don't process
     * non-contiguous units.
     */
    private boolean processedDefaultProperty = false;

    /**
     * The implicit node created to represent the default property.
     */
    private MXMLPropertySpecifierNode defaultPropertyNode;

    /**
     * A list that accumulates content units for the default property.
     */
    private List<IMXMLUnitData> defaultPropertyContentUnits;

    @Override
    public IASNode getChild(int i)
    {
        return children != null ? children[i] : null;
    }

    @Override
    public int getChildCount()
    {
        return children != null ? children.length : 0;
    }

    @Override
    public String getName()
    {
        // The classReference can be null when getName() is called from toString()
        // in the debugger if the node is not yet fully initialized.
        return classReference != null ? classReference.getQualifiedName() : "";
    }

    @Override
    public IClassDefinition getClassReference(ICompilerProject project)
    {
        return classReference;
    }

    @Override
    public boolean isMXMLObject()
    {
        return isMXMLObject;
    }

    @Override
    public boolean isContainer()
    {
        return isContainer;
    }

    @Override
    public boolean isVisualElementContainer()
    {
        return isVisualElementContainer;
    }

    @Override
    public boolean isDeferredInstantiationUIComponent()
    {
        return isDeferredInstantiationUIComponent;
    }

    /**
     * Sets the definition of the ActionScript class to which this node refers.
     */
    void setClassReference(RoyaleProject project, IClassDefinition classReference)
    {
        this.classReference = classReference;

        // TODO Optimize this by enumerating all interfaces one time.

        // Keep track of whether the class implements mx.core.IMXML,
        // because that affects code generation.
        String mxmlObjectInterface = project.getMXMLObjectInterface();
        isMXMLObject = classReference.isInstanceOf(mxmlObjectInterface, project);

        // Keep track of whether the class implements mx.core.IVisualElementContainer,
        // because that affects code generation.
        String visualElementContainerInterface = project.getVisualElementContainerInterface();
        isVisualElementContainer = classReference.isInstanceOf(visualElementContainerInterface, project);

        // Keep track of whether the class implements mx.core.IContainer,
        // because that affects code generation.
        String containerInterface = project.getContainerInterface();
        isContainer = classReference.isInstanceOf(containerInterface, project);

        // Keep track of whether the class implements mx.core.IDeferredInstantiationUIComponent
        // because that affects code generation.
        String deferredInstantiationUIComponentInterface = project.getDeferredInstantiationUIComponentInterface();
        isDeferredInstantiationUIComponent = classReference.isInstanceOf(deferredInstantiationUIComponentInterface, project);
    }

    /**
     * Sets the definition of the ActionScript class to which this node refers,
     * from its fully qualified name.
     * 
     * @param project An {@code ICompilerProject}, used for finding the class by
     * name.
     * @param qname A fully qualified class name.
     */
    void setClassReference(RoyaleProject project, String qname)
    {
        ASProjectScope projectScope = (ASProjectScope)project.getScope();
        IDefinition definition = projectScope.findDefinitionByName(qname);
        // TODO This method is getting called by MXML tree-building
        // with an interface qname if there is a property whose type is an interface.
        // Until databinding is implemented, we need to protect against this.
        if (definition instanceof IClassDefinition)
            setClassReference(project, (IClassDefinition)definition);
    }

    /**
     * Sets the child nodes of this node.
     * 
     * @param children An array of {@code IMXMLNode} objects.
     */
    void setChildren(IMXMLNode[] children)
    {
        this.children = children;

        if (children != null)
        {
            for (IMXMLNode child : children)
            {
                if (child instanceof IMXMLPropertySpecifierNode)
                {
                    if (propertyNodeMap == null)
                    {
                        propertyNodeMap = new HashMap<String, IMXMLPropertySpecifierNode>();
                        allPropertyNodes = new ArrayList<IMXMLPropertySpecifierNode>();
                    }

                    propertyNodeMap.put(child.getName(), (IMXMLPropertySpecifierNode)child);
                    allPropertyNodes.add((IMXMLPropertySpecifierNode)child);
                }
                else if (child instanceof IMXMLEventSpecifierNode)
                {
                    if (eventNodeMap == null)
                    {
                        eventNodeMap = new HashMap<String, IMXMLEventSpecifierNode>();
                        allEventNodes = new ArrayList<IMXMLEventSpecifierNode>();
                    }

                    eventNodeMap.put(child.getName(), (IMXMLEventSpecifierNode)child);
                    allEventNodes.add((IMXMLEventSpecifierNode)child);
                }

                if (child instanceof IMXMLSpecifierNode)
                {
                    if (suffixSpecifierMap == null)
                        suffixSpecifierMap = new HashMap<String, Collection<IMXMLSpecifierNode>>();

                    //                    suffixSpecifierMap.put(((IMXMLSpecifierNode)child).getSuffix(),
                    //                                           (IMXMLSpecifierNode)child);
                }
            }
        }
    }

    @Override
    public IMXMLPropertySpecifierNode getPropertySpecifierNode(String name)
    {
        return propertyNodeMap != null ? propertyNodeMap.get(name) : null;
    }

    @Override
    public IMXMLPropertySpecifierNode[] getPropertySpecifierNodes()
    {
        return allPropertyNodes != null ?
        		allPropertyNodes.toArray(new IMXMLPropertySpecifierNode[0]) :
                null;
    }

    @Override
    public IMXMLEventSpecifierNode getEventSpecifierNode(String name)
    {
        return eventNodeMap != null ? eventNodeMap.get(name) : null;
    }

    @Override
    public IMXMLEventSpecifierNode[] getEventSpecifierNodes()
    {
        return allEventNodes != null ?
                allEventNodes.toArray(new IMXMLEventSpecifierNode[0]) :
                null;
    }

    @Override
    public IMXMLSpecifierNode[] getSpecifierNodesWithSuffix(String suffix)
    {
        return suffixSpecifierMap != null ?
                suffixSpecifierMap.get(suffix).toArray(new IMXMLSpecifierNode[0]) :
                null;
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {

        MXMLSpecifierNodeBase childNode = createSpecifierNode(builder, attribute.getName());
        if (childNode != null)
        {
            childNode.setLocation(attribute);
            childNode.setSuffix(builder, attribute.getStateName());
            childNode.initializeFromAttribute(builder, attribute, info);
            info.addChildNode(childNode);
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        if (info.hasSpecifierWithName(childTag.getShortName(), childTag.getStateName()))
        {
            ICompilerProblem problem = new MXMLDuplicateChildTagProblem(childTag);
            builder.addProblem(problem);
            return ;
        }
        
        RoyaleProject project = builder.getProject();

        // Handle child tags that are property/style/event specifiers.
        MXMLSpecifierNodeBase childNode = null;
        // ...but only if the child has the same prefix as the parent -JT
        // apache/royale-compiler#101
        if(tag.getPrefix().equals(childTag.getPrefix()))
        {
            childNode = createSpecifierNode(builder, childTag.getShortName());
        }
        if (childNode != null)
        {
            // This tag is not part of the default property value.
            processNonDefaultPropertyContentUnit(builder, info, tag);

            childNode.setSuffix(builder, childTag.getStateName());
            childNode.initializeFromTag(builder, childTag);
            info.addChildNode(childNode);
        }
        else if (builder.getFileScope().isScriptTag(childTag) &&
                 (builder.getMXMLDialect().isEqualToOrBefore(MXMLDialect.MXML_2009)))
        {
            // In MXML 2006 and 2009, allow a <Script> tag
            // inside any class reference tag

            if (!processingDefaultProperty)
            {
                // Not processing the default property, just make a script
                // node and put it in the tree.
                MXMLScriptNode scriptNode = new MXMLScriptNode(this);
                scriptNode.initializeFromTag(builder, childTag);
                info.addChildNode(scriptNode);
            }
            else
            {
                // We are processing a default property.  Script nodes need
                // to be a child of that default specifier nodes so that
                // finding a node by offset works properly.
                // See: http://bugs.adobe.com/jira/browse/CMP-955
                processDefaultPropertyContentUnit(builder, childTag, info);
            }

        }
        else if (builder.getFileScope().isReparentTag(childTag))
        {
            MXMLReparentNode reparentNode = new MXMLReparentNode(this);
            reparentNode.initializeFromTag(builder, childTag);
            info.addChildNode(reparentNode);
        }
        else
        {
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(childTag);
            if (definition instanceof ClassDefinition)
            {
                // Handle child tags that are instance tags.

                IVariableDefinition defaultPropertyDefinition = getDefaultPropertyDefinition(builder);
                if (defaultPropertyDefinition != null)
                {
                	if (processedDefaultProperty)
                	{
                		MXMLDuplicateChildTagProblem problem = new MXMLDuplicateChildTagProblem(childTag);
                        problem.childTag = defaultPropertyDefinition.getBaseName();
                        problem.element = tag.getShortName();
                        builder.addProblem(problem);
                        return ;
                	}
                	else
                	{
	                    // Since there is a default property and we haven't already processed it,
	                    // assume this child instance tag is part of its value.
	                    processDefaultPropertyContentUnit(builder, childTag, info);
                	}
                }
                else
                {
                    // This tag is not part of the default property value.
                    processNonDefaultPropertyContentUnit(builder, info, tag);

                    MXMLInstanceNode instanceNode = MXMLInstanceNode.createInstanceNode(
                            builder, definition.getQualifiedName(), this);
                    instanceNode.setClassReference(project, (IClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                    instanceNode.initializeFromTag(builder, childTag);
                    info.addChildNode(instanceNode);
                }
            }
            else
            {
                IVariableDefinition defaultPropertyDefinition = getDefaultPropertyDefinition(builder);
                if (defaultPropertyDefinition != null && !processedDefaultProperty && defaultPropertyDefinition.getBaseName().equals("text"))
                {
                	String uri = childTag.getURI();
                	if (uri != null && uri.equals("http://www.w3.org/1999/xhtml"))
                	{
                        IVariableDefinition htmlDef = (IVariableDefinition)project.resolveSpecifier(classReference, "html");
                        if (htmlDef != null)
                        {
                        	defaultPropertyDefinition = this.defaultPropertyDefinition = htmlDef;
	                        processDefaultPropertyContentUnit(builder, childTag, info);
	                        // seems strange we have to finish default property processing
	                        // by calling nonDefaultProperty code
	                        processNonDefaultPropertyContentUnit(builder, info, tag);
	                        return;
                        }
                	}
                }
                else if (altDefaultPropertyDefinition != null && !processedDefaultProperty && altDefaultPropertyDefinition.getBaseName().equals("innerHTML"))
                {
                	String uri = childTag.getURI();
                	if (uri != null && uri.equals("library://ns.apache.org/royale/html"))
                	{
                        IVariableDefinition textDef = (IVariableDefinition)project.resolveSpecifier(classReference, "innerHTML");
                        if (textDef != null)
                        {
                        	List<IMXMLNode> nodes = info.getChildNodeList();
                        	if (nodes.size() > 0)
                        	{
                        		IMXMLNode lastNode = nodes.get(nodes.size() - 1);
                        		if (lastNode.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
                        		{
                        			MXMLPropertySpecifierNode propNode = (MXMLPropertySpecifierNode)lastNode;
                        			String name = propNode.getName();
                        			if (name.equals("innerHTML"))
                        			{
                        				/*
                        				MXMLStringNode stringNode = (MXMLStringNode)propNode.getChild(0);
                        				MXMLLiteralNode valueNode = (MXMLLiteralNode)stringNode.getChild(0);
                        				String tagAsString = ((MXMLTagData)childTag).stringify();
                        				String currentString = (String)valueNode.getValue();
                        				MXMLLiteralNode newValueNode = new MXMLLiteralNode(stringNode, 
                        						currentString + tagAsString);
                        				IMXMLNode[] newChildren = new IMXMLNode[1];
                        				newChildren[0] = newValueNode;
                        				stringNode.setChildren(newChildren);
                        				stringNode.setExpressionNode(newValueNode);
                        				*/
                        				SourceFragment[] sourceFragments = new SourceFragment[1];
                        				String tagAsString = ((MXMLTagData)childTag).stringify();
                        				SourceFragment sourceFragment = new SourceFragment(tagAsString, tagAsString, childTag.getLocationOfChildUnits());
                        				sourceFragments[0] = sourceFragment;
                        				info.addSourceFragments(childTag.getSourcePath(), sourceFragments);
                        			}
                        		}
                        	}
                        	else
                        	{
                                childNode = createSpecifierNode(builder, "innerHTML");
                                if (childNode != null)
                                {
                                    childNode.setSuffix(builder, childTag.getStateName());
                    				String tagAsString = ((MXMLTagData)childTag).stringify();
                    				String tagAsCData = IMXMLCoreConstants.cDataStart + tagAsString + IMXMLCoreConstants.cDataEnd;
                    				MXMLToken token = new MXMLToken(MXMLTokenTypes.TOKEN_CDATA,
                    						childTag.getStart(), childTag.getEnd(),
                    						childTag.getLine(), childTag.getColumn(),
                    						tagAsCData);
                    				MXMLTextData text = new MXMLTextData(token);
                    				text.setSourceLocation(childTag.getLocationOfChildUnits());
                    				childNode.initializeFromText(builder, text, info);
                                    info.addChildNode(childNode);
                                }
                        	}
	                        return;
                        }
                	}
                }
                if (processingDefaultProperty && definition == null)
                {
                    builder.getProblems().add(new MXMLUnresolvedTagProblem(childTag));
                	return;
                }
                // Handle child tags that are something other than property/style/event tags
                // or instance tags.

                // This tag is not part of the default property value.
                processNonDefaultPropertyContentUnit(builder, info, tag);

                super.processChildTag(builder, tag, childTag, info);
            }
        }
    }

    /**
     * Determines, and caches, the default property for the class to which this
     * node refers.
     */
    private IVariableDefinition getDefaultPropertyDefinition(MXMLTreeBuilder builder)
    {
        if (!defaultPropertyDefinitionInitialized)
        {
            RoyaleProject project = builder.getProject();
            String defaultPropertyName = classReference.getDefaultPropertyName(project);
            if (defaultPropertyName != null)
            {
            	if (defaultPropertyName.contains("|"))
            	{
            		int c = defaultPropertyName.indexOf("|");
            		String alt = defaultPropertyName.substring(c + 1);
            		defaultPropertyName = defaultPropertyName.substring(0, c);
            		altDefaultPropertyDefinition = (IVariableDefinition)project.resolveSpecifier(classReference, alt);
            	}
                defaultPropertyDefinition =
                        (IVariableDefinition)project.resolveSpecifier(classReference, defaultPropertyName);
            }

            defaultPropertyDefinitionInitialized = true;
        }

        return defaultPropertyDefinition;
    }

    /**
     * Called on each content unit that is part of the default value.
     */
    private void processDefaultPropertyContentUnit(MXMLTreeBuilder builder,
                                                   IMXMLTagData childTag,
                                                   MXMLNodeInfo info)
    {
        // If this gets called and we're not already processing the default property,
        // then childTag is the first child tag of the default property value.
        if (!processingDefaultProperty)
        {
            processingDefaultProperty = true;

            String defaultPropertyName = getDefaultPropertyDefinition(builder).getBaseName();

            // Create an implicit MXMLPropertySpecifierNode for the default property,
            // at the correct location in the child list.
            defaultPropertyNode =
                    (MXMLPropertySpecifierNode)createSpecifierNode(builder, defaultPropertyName);
            info.addChildNode(defaultPropertyNode);

            // Create a list in which we'll accumulate the tags for the default property.
            defaultPropertyContentUnits = new ArrayList<IMXMLUnitData>(1);
        }

        defaultPropertyContentUnits.add((IMXMLUnitData)childTag);
    }

    /**
     * Called on each content unit that is not part of the default value.
     */
    private void processNonDefaultPropertyContentUnit(MXMLTreeBuilder builder, MXMLNodeInfo info, IMXMLTagData parentTag)
    {
        // If this gets called and we're processing the default property,
        // then childTag is the first child tag after the default property value tags.
        if (processingDefaultProperty)
        {
            processingDefaultProperty = false;
            processedDefaultProperty = true;

            assert defaultPropertyContentUnits.size() > 0;
            assert !builder.getFileScope().isScriptTag(defaultPropertyContentUnits.get(0)) : "First default property content unit must not be a script tag!";
            // We've accumulated all the default property child tags
            // in defaultPropertyChildTags. Use them to initialize
            // the defaultPropertyNode.

            // But first find all the trailing script tags
            // and remove those from the list of default
            // property content units.
            // Script tags are put in the defaultPropertyContentUnits collection
            // to fix http://bugs.adobe.com/jira/browse/CMP-955.
            int lastNonScriptTagIndex;
            for (lastNonScriptTagIndex = (defaultPropertyContentUnits.size() - 1); lastNonScriptTagIndex > 0; --lastNonScriptTagIndex)
            {
                IMXMLUnitData unitData = defaultPropertyContentUnits.get(lastNonScriptTagIndex);
                if (!builder.getFileScope().isScriptTag(unitData))
                    break;
            }
            assert lastNonScriptTagIndex >= 0;
            assert lastNonScriptTagIndex < defaultPropertyContentUnits.size();

            List<IMXMLUnitData> trailingScriptTags = defaultPropertyContentUnits.subList(lastNonScriptTagIndex + 1, defaultPropertyContentUnits.size());
            List<IMXMLUnitData> defaultPropertyContentUnitsWithoutTrailingScriptTags =
                    defaultPropertyContentUnits.subList(0, lastNonScriptTagIndex + 1);

            // process the default property content units with the trailing
            // script tags removed.
            IVariableDefinition defaultPropertyDefinition =
                    getDefaultPropertyDefinition(builder);
            defaultPropertyNode.initializeDefaultProperty(
                    builder, defaultPropertyDefinition, parentTag, defaultPropertyContentUnitsWithoutTrailingScriptTags);

            // Now create MXMLScriptNode's for all the trailing script tags.
            for (IMXMLUnitData scriptTagData : trailingScriptTags)
            {
                assert builder.getFileScope().isScriptTag(scriptTagData);
                MXMLScriptNode scriptNode = new MXMLScriptNode(this);
                scriptNode.initializeFromTag(builder, (IMXMLTagData)scriptTagData);
                info.addChildNode(scriptNode);
            }
        }
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        // Non-whitespace may be the value of a default property.
        IVariableDefinition defaultPropertyDefinition = getDefaultPropertyDefinition(builder);
        IVariableDefinition getterDefinition = (defaultPropertyDefinition instanceof ISetterDefinition) ? 
        		((ISetterDefinition)defaultPropertyDefinition).resolveCorrespondingAccessor(builder.getProject()) :null;
        if (defaultPropertyDefinition != null && 
        		(defaultPropertyDefinition.getTypeAsDisplayString().equals(IASLanguageConstants.String) ||
        		 (defaultPropertyDefinition.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_RICHTEXTCONTENT) != null) ||
        		 (getterDefinition != null && 
        		     (getterDefinition.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_RICHTEXTCONTENT) != null))))
        {
            MXMLSpecifierNodeBase childNode =
                    createSpecifierNode(builder, defaultPropertyDefinition.getBaseName());
            if (childNode != null)
            {
                childNode.initializeFromText(builder, text, info);
                info.addChildNode(childNode);
            }
        }
        else if (altDefaultPropertyDefinition != null && altDefaultPropertyDefinition.getTypeAsDisplayString().equals(IASLanguageConstants.String))
        {
            MXMLSpecifierNodeBase childNode =
                    createSpecifierNode(builder, altDefaultPropertyDefinition.getBaseName());
            if (childNode != null)
            {
                childNode.initializeFromText(builder, text, info);
                info.addChildNode(childNode);
            }
        }
        else
        {
            super.processChildNonWhitespaceUnit(builder, tag, text, info);
        }
    }

    /**
     * Resolve the specifier name in the class definition to a member
     * definition, and create a specifier node based on the member type.
     * 
     * @param builder MXML tree builder.
     * @param specifierName Specifier name.
     * @return A MXML specifier node.
     */
    protected MXMLSpecifierNodeBase createSpecifierNode(MXMLTreeBuilder builder, String specifierName)
    {
        MXMLSpecifierNodeBase specifierNode = null;

        // Check if the attribute is a declared property, style, or event.
        RoyaleProject project = builder.getProject();
        IDefinition specifierDefinition = project.resolveSpecifier(classReference, specifierName);

        if (specifierDefinition instanceof ISetterDefinition ||
            specifierDefinition instanceof IVariableDefinition)
        {
            specifierNode = new MXMLPropertySpecifierNode(this);
        }
        else if (specifierDefinition instanceof IEventDefinition)
        {
            specifierNode = new MXMLEventSpecifierNode(this);
        }
        else if (specifierDefinition instanceof IStyleDefinition)
        {
            specifierNode = new MXMLStyleSpecifierNode(this);
        }
        else if (specifierDefinition instanceof IEffectDefinition)
        {
            specifierNode = new MXMLEffectSpecifierNode(this);
        }

        if (specifierNode != null)
        {
            specifierNode.setDefinition(specifierDefinition); // TODO Move this logic
        }

        // If not, dynamic classes allow new properties to be set via attributes.
        else if (classReference.isDynamic())
        {
            specifierNode = new MXMLPropertySpecifierNode(this);
            ((MXMLPropertySpecifierNode)specifierNode).setDynamicName(specifierName); // TODO Move this logic
        }

        return specifierNode;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        // If the last child unit was part of the default property,
        // we don't know to process the default property units
        // until we get here.
        processNonDefaultPropertyContentUnit(builder, info, tag);

        setChildren(info.getChildNodeList().toArray(new IMXMLNode[0]));

        // If the class references by this node implements mx.core.IContainer,
        // add an expression dependency on mx.core.UIComponentDescriptor
        // because we'll have to codegen descriptors.
        if (isContainer)
        {
            RoyaleProject project = builder.getProject();
            builder.addExpressionDependency(project.getUIComponentDescriptorClass());
        }
    }

    /**
     * For debugging only. Builds a string such as
     * <code>"spark.components.Application"</code> from the qualified name of
     * the class reference by the node.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }
}
