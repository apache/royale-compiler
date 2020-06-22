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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition.ClassClassification;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.mxml.StateDefinition;
import org.apache.royale.compiler.internal.mxml.StateGroupDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IStateDefinition;
import org.apache.royale.compiler.mxml.IStateGroupDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLReparentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStateNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import com.google.common.collect.ArrayListMultimap;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * {@code MXMLClassDefinitionNode} represents an MXML tag which defines a new
 * class. It might be the document tag, or the tag inside a &lt;Component&gt;
 * tag, or the tag inside a &lt;Definition&gt; tag.
 */
public class MXMLClassDefinitionNode extends MXMLClassReferenceNodeBase
    implements IMXMLClassDefinitionNode, IScopedNode
{

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLClassDefinitionNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The class that this node is defining.
     */
    private IClassDefinition classDefinition;

    /**
     * The interfaces that the class being defined claims it implements.
     */
    @SuppressWarnings("unused")
    private String[] implementedInterfaces;

    /**
     * The scope inside this class.
     */
    private IASScope scope;

    /**
     * The project for this class.
     */
    private RoyaleProject project;
    
    /**
     * A map mapping an id to the instance node with that id.
     */
    private Map<String, IMXMLInstanceNode> idMap;

    /**
     * A map mapping the name of a state defined within this class to the state
     * node with that name.
     */
    private Map<String, StateDefinition> stateMap;

    /**
     * A map mapping the name of a state group defined within this class to a
     * set of state names for the states contained by the group. Example: "odd"
     * -> { "s1", "s3", "s5" }
     */
    private Map<String, StateGroupDefinition> groupMap;

    /**
     * A list of all state-dependent nodes in this class, in tree order. This
     * includes instance nodes with includeIn/excludeFrom, and
     * property/style/event nodes with suffixes. This is a temporary list that
     * is used to build stateDependentNodeMap and then freed.
     */
    private List<IMXMLNode> stateDependentNodeList;

    /**
     * A map mapping the the name of a state defined within this class to a list
     * of nodes (in tree order) that depend on that class. These node lists for
     * each state are used by the code generator to generate the IOverride
     * objects for each state.
     */
    private ArrayListMultimap<String, IMXMLNode> stateDependentNodeMap;

    /**
     * The state name to which <code>currentState</code> should be initialized.
     */
    private String initialState;

    /**
     * An incrementing counter for compiler-generated ids within this class.
     */
    private int generatedIDCounter = 0;

    /**
     * A map mapping an instance node without an id to its autogenerated id.
     */
    Map<IMXMLInstanceNode, String> generatedIDMap =
            new HashMap<IMXMLInstanceNode, String>();

    /**
     * The child &lt;Metadata&gt; nodes.
     */
    private IMXMLMetadataNode[] metadataNodes;

    /**
     * The child &lt;Script&gt; nodes.
     */
    private IMXMLScriptNode[] scriptNodes;

    /**
     * The child &lt;Declarations&gt; nodes.
     */
    private IMXMLDeclarationsNode[] declarationsNodes;

    /**
     * This definition link is used only by CodeModel. It is created and updated
     * by {@code getAdapter()}.
     */

    /**
     * This counter keeps track of how many {@code MXMLComponentNode}s are
     * inside this {@code MXMLClassDefinitionNode}. The counter is incorporated
     * into the autogenerated name of the <code>&lt;fx:Component&gt;</code>
     * class.
     */
    private int componentCount = 0;

    /**
     * This flag keep track if there any data binding nodes in this class.
     */
    private boolean hasDataBindings;
    
    private IASDocComment asDocComment;

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        // Revisit all State nodes in this class after all the states are known,
        // to determine the state groups and which states are in which groups.
        reprocessStateNodes();

        // Revisit all state-dependent nodes in this class after the states
        // and state groups are known, to determine which of them depend on which state
        // (since the code generator needs this information to create IOverride
        // objects for each state).
        reprocessStateDependentNodes(builder);

        // Add the dependency between the class this node defines and its superclass,
        // as expressed by this tag that created this node.
        project = builder.getProject();
        IClassDefinition classReference = getClassReference(project);
        String qname = classReference.getQualifiedName();
        builder.addDependency(qname, DependencyType.INHERITANCE);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_IMPLEMENTS))
        {
            // Keep track of the specified interfaces as an array of Strings,
            // for use by the compiler.
            String rawValue = attribute.getRawValue();
            if (rawValue != null)
                implementedInterfaces = attribute.getMXMLDialect().splitAndTrim(rawValue);

            // For CodeModel's use, also keep track of them as children
            // of an {@code MXMLImplementsNode}.
            MXMLImplementsNode interfaceNode = new MXMLImplementsNode(this);
            interfaceNode.initializeFromAttribute(builder, attribute);
            info.addChildNode(interfaceNode);

            // TODO Report problems if the interfaces don't exist
            // Later we also have report a problems for each interface method
            // that isn't implemented.
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
        MXMLFileScope fileScope = builder.getFileScope();
        project = builder.getProject();

        MXMLNodeBase childNode = null;

        if (fileScope.isDeclarationsTag(childTag))
        {
            childNode = new MXMLDeclarationsNode(this);
        }
        else if ((fileScope.isScriptTag(childTag)) &&
                 // Our base class will handle script tags for MXML 2009 and below.
                 // Beginning with MXML 2012, only class definition tags may have script
                 // tags as children.
                 (builder.getMXMLDialect().isEqualToOrAfter(MXMLDialect.MXML_2012)))
        {
            childNode = new MXMLScriptNode(this);
        }
        else if (fileScope.isStyleTag(childTag))
        {
            childNode = new MXMLStyleNode(this);
        }
        else if (fileScope.isMetadataTag(childTag))
        {
            childNode = new MXMLMetadataNode(this);
        }
        else if (fileScope.isBindingTag(childTag))
        {
            childNode = new MXMLBindingNode(this);
            this.setHasDataBindings(); // we must have some, if we made this node
        }
        else
        {
            super.processChildTag(builder, tag, childTag, info);
        }

        if (childNode != null)
        {
            childNode.initializeFromTag(builder, childTag);
            info.addChildNode(childNode);
        }
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLClassDefinitionID;
    }

    @Override
    public String getPackageName()
    {
        // getPackageName() in NodeBase expects to find an IPackageNode.
        // MXML trees don't have package nodes, so instead we just ask
        // the corresponding IClassDefinition for its package name.
        return classDefinition.getPackageName();
    }

    @Override
    public IClassDefinition getClassDefinition()
    {
        return classDefinition;
    }

    /**
     * Sets the definition of the class defined by this node.
     * 
     * @param classDefinition An {@code IClassDefinition} object for the class.
     */
    void setClassDefinition(IClassDefinition classDefinition)
    {
        this.classDefinition = classDefinition;
        setScope((TypeScope)classDefinition.getContainedScope());
    }

    /**
     * Adds an entry to this class's id map, which maps an id to the node with
     * that id.
     * <p>
     * If a previously processed tag had the same id, the specified node is not
     * added to the map, and the old node is returned; otherwise
     * <code>null</code> is returned.
     * 
     * @param node An {@code IMXMLInstanceNode} with an id.
     * @return The {@code IMXMLInstanceNode}, if any, that already has the same
     * id.
     */
    IMXMLInstanceNode addNodeWithID(String id, IMXMLInstanceNode node)
    {
        if (idMap == null)
            idMap = new HashMap<String, IMXMLInstanceNode>();

        return idMap.put(id, node);
    }

    @Override
    public IMXMLInstanceNode getNodeWithID(String id)
    {
        return idMap != null ? idMap.get(id) : null;
    }

    /*
     * Notes about MXML States Each component within a document has its own set
     * of states and state groups, so these are managed by class definition
     * nodes. Only classes that implement mx.core.IStateClient2 can have states.
     * State tags can occur wherever a value of type mx.states.State would be
     * allowed. Usually <State> tags are written as the value of the <states>
     * property of the UIComponent-derived component that they're defining. But
     * they could also go into <Declarations>, or be used as other property
     * values. In MXML 2006, <mx:State> is a normal instance tag whose runtime
     * behavior is to apply a particular state. You specify an array of
     * IOverride objects to be executed when the state is entered by setting its
     * 'overrides' property (which is the default property). Various
     * implementations of IOverride handle setting properties and styles, and
     * adding event handlers, on specified objects, or creating instances of
     * objects. For example, you might write <mx:State name="s1">
     * <mx:SetProperty target="b1" name="label" value="OK"/> <mx:SetStyle
     * target="b1" name="color" value="0xFFFFFF"/> </mx:State> to change the
     * label and color of Button b1. MXML 2009 introduced state-suffix notation
     * (such as label.s1="OK or <mx:label.s1>OK<mx:label.s1> for specifying
     * state-dependent properties/styles/events, and includeIn/excludeFrom
     * attributes for specifying instances that exist only in particular states.
     * These two MXML features end up autogenerating the override objects for
     * the states, and you can no longer specify the overrides explicitly in
     * MXML. Consider the following example: <s:Application ...> <s:Button
     * id="b1" label="OK"/> <s:Button id="b2" label.even="Foo" label.odd="Bar"/>
     * <s:Button id="b3" includeIn="odd,s4"/> <s:Button id="b4"
     * excludeFrom="even,s3"/> <s:states> <s:State name="s1" groups="all,odd"/>
     * <s:State name="s2" groups="all,even"/> <s:State name="s3"
     * groups="all,odd"/> <s:State name="s4" groups="all,even"/> <s:State
     * name="s5" groups="all,odd"/> </s:state> </s:Application> The class
     * defined by the <s:Application> tag has five states (named "s1", "s2",
     * "s3", "s4", and "s5") and three state groups (named "all", "odd", and
     * "even".) Note that state groups are defined implicitly by being mentioned
     * on <State> tags. Also, the names of states and state groups must be
     * distinct. The members of the three groups are: all: s1, s2, s3, s4, s5
     * odd: s1, s3, s5 even: s4, s4 Button b1 and its label property are not
     * state-dependent. Button b2 is not a state-dependent instance, but it has
     * a state-dependent label property. Buttons b3 and b4 are state-dependent
     * instances; b3 exists in states "s1", "s3", "s4", "s5" but not in "s2"; b4
     * exists in states "s1" and "s5" but not in "s2", "s3", or "s4". As tree
     * nodes are created, their MXMLClassDefinitionNode keeps track of <State>
     * nodes and any state-dependent node (i.e., one with includeIn/excludeFrom
     * or a suffix). After all <State> nodes have been discovered, we reprocess
     * them to determine all the state groups. After we know the names of all
     * the states and state groups, we reprocess the state-dependent nodes to
     * check that they specify valid states or state groups and determine what
     * states they belong to. We build lists of state-dependent nodes for each
     * state, like this: s1 MXMLPropertyNode for attribute label.odd="Bar" on
     * Button b2 MXMLInstanceNode for Button b3 MXMLInstanceNode for Button b4
     * s2 MXMLPropertyNode for attribute label.even="Foo" on Button b2 s3
     * MXMLPropertyNode for attribute label.odd="Bar" on Button b2
     * MXMLInstanceNode for Button b3 s4 MXMLPropertyNode for attribute
     * label.even="Foo" on Button b2 MXMLInstanceNode for Button b3 s5
     * MXMLPropertyNode for attribute label.odd="Bar" on Button b2
     * MXMLInstanceNode for Button b3 MXMLInstanceNode for Button b4 The code
     * generator then uses these lists to generate the override objects that
     * each state object applies.
     */

    @Override
    public Set<String> getStateNames()
    {
        if (stateMap == null)
            return new HashSet<String>(0);

        return stateMap.keySet();
    }

    @Override
    public Set<IStateDefinition> getStates()
    {
        if (stateMap == null)
            return new HashSet<IStateDefinition>(0);

        Set<IStateDefinition> states = new HashSet<IStateDefinition>(stateMap.size());
        states.addAll(stateMap.values());
        return states;
    }

    /**
     * Gets the state node that defines a specified state.
     * 
     * @param stateName A state name.
     * @return The {code IMXMLStateNode} with that name.
     */
    public IMXMLStateNode getStateNode(String stateName)
    {
        if (stateMap == null)
            return null;

        return stateMap.get(stateName).getNode();
    }

    /**
     * Gets the state definition that defines a specified state.
     * 
     * @param stateName A state name
     * @return The {code IStateDefinition} with that name
     */
    public IStateDefinition getStateByName(String stateName)
    {
        if (stateMap == null)
            return null;

        return stateMap.get(stateName);
    }

    /**
     * Gets the state group that defines a specified state group.
     * 
     * @param groupName A state group name
     * @return The {code IStateGroup} with that name
     */
    public IStateGroupDefinition getStateGroupByName(String groupName)
    {
        if (groupMap == null)
            return null;

        return groupMap.get(groupName);
    }

    /**
     * Determines whether a string is the name of a state of this class.
     * 
     * @param stateName A String that might be a state name.
     * @return <code>true</code> if it is a state name.
     */
    public boolean isState(String stateName)
    {
        if (stateMap == null)
            return false;

        return stateMap.containsKey(stateName);
    }

    @Override
    public Set<String> getStateGroupNames()
    {
        if (groupMap == null)
            return new HashSet<String>(0);

        return groupMap.keySet();
    }

    @Override
    public Set<IStateGroupDefinition> getStateGroups()
    {
        if (groupMap == null)
            return new HashSet<IStateGroupDefinition>(0);

        Set<IStateGroupDefinition> groups = new HashSet<IStateGroupDefinition>(groupMap.size());
        groups.addAll(groupMap.values());
        return groups;
    }

    private String[] getStatesInGroup(String groupName)
    {
        return groupMap.get(groupName).getIncludedStates();
    }

    /**
     * Determines whether a string is the name of a state group of this class.
     * 
     * @param s A String that might be a state group name.
     * @return <code>true</code> if it is a state group name.
     */
    private boolean isStateGroup(String groupName)
    {
        if (groupMap == null)
            return false;

        return groupMap.containsKey(groupName);
    }

    @Override
    public List<IMXMLNode> getNodesDependentOnState(String stateName)
    {
        return stateDependentNodeMap != null ? stateDependentNodeMap.get(stateName) : null;
    }

    @Override
    public List<IMXMLNode> getAllStateDependentNodes()
    {
        return stateDependentNodeList;
    }

    @Override
    public String getInitialState()
    {
        return initialState;
    }

    /**
     * Iterates over the State nodes to determine the state groups defined
     * within this class. This initializes the stateGroupMap that's used by
     * getStateGroups(), getStatesInGroup(), and isStateGroup().
     */
    private void reprocessStateNodes()
    {
        Set<String> states = getStateNames();
        if (states == null)
            return;

        for (String state : states)
        {
            IMXMLStateNode stateNode = getStateNode(state);
            String[] stateGroups = stateNode.getStateGroups();
            if (stateGroups != null)
            {
                for (String stateGroup : stateGroups)
                {
                    addStateToStateGroup(stateGroup, state);
                }
            }
        }
    }

    private void addStateToStateGroup(String groupName, String stateName)
    {
        if (groupMap == null)
            groupMap = new HashMap<String, StateGroupDefinition>();

        StateDefinition state = stateMap.get(stateName);
        StateGroupDefinition group = groupMap.get(groupName);

        if (group == null)
        {
            group = new StateGroupDefinition(groupName, getDefinition());
            groupMap.put(groupName, group);
        }

        state.addGroup(group);
        group.addState(state);
    }

    /**
     * Determines which nodes depend on which states. Instance nodes depend on
     * the states implied by their includeIn and excludeFrom attributes.
     * Property, style, and event nodes depend on the states implied by their
     * suffix (as in label.up="OK").
     */
    private void reprocessStateDependentNodes(MXMLTreeBuilder builder)
    {
        if (stateDependentNodeList == null)
            return;

        // Flags that keep track of whether we have various kinds of stateful nodes.
        // Each kind introduce particular dependencies on various runtime classes.
        boolean haveInstanceOverride = false;
        boolean havePropertyOverride = false;
        boolean haveStyleOverride = false;
        boolean haveEventOverride = false;

        // Iterate over all instance nodes that have includeIn or excludeFrom,
        // and all property/style/event nodes that have a suffix.
        for (IMXMLNode node : stateDependentNodeList)
        {
            if (node instanceof IMXMLInstanceNode || node instanceof IMXMLReparentNode)
            {
                haveInstanceOverride = true;

                // TODO Consider introducing an interface
                // containing getIncludeIn() and getExcludeFrom()
                // to make IMXMLInstanceNode and IMXMLReparentNode
                // look alike here.

                String[] includeIn = null;
                if (node instanceof IMXMLInstanceNode)
                    includeIn = ((IMXMLInstanceNode)node).getIncludeIn();
                else if (node instanceof IMXMLReparentNode)
                    includeIn = ((IMXMLReparentNode)node).getIncludeIn();

                String[] excludeFrom = null;
                if (node instanceof IMXMLInstanceNode)
                    excludeFrom = ((IMXMLInstanceNode)node).getExcludeFrom();
                else if (node instanceof IMXMLReparentNode)
                    excludeFrom = ((IMXMLReparentNode)node).getExcludeFrom();

                // Determine which states contain this instance
                // based on includeIn or excludeFrom.
                Set<String> statesContainingInstance = null;
                if (includeIn != null)
                    statesContainingInstance = processIncludeIn(includeIn);
                else if (excludeFrom != null)
                    statesContainingInstance = processExcludeFrom(excludeFrom);

                // Add the node to each of those state's list of state-dependent nodes.
                if (statesContainingInstance != null)
                {
                    for (String state : statesContainingInstance)
                    {
                        addStateDependentNode(state, node);
                    }
                }
            }
            else if (node instanceof IMXMLSpecifierNode)
            {
                // havePropertyOverride must be last because
                // IMXMLStyleSpecifierNode extends IMXMLPropertySpecifierNode
                if (node instanceof IMXMLStyleSpecifierNode)
                    haveStyleOverride = true;
                else if (node instanceof IMXMLEventSpecifierNode)
                    haveEventOverride = true;
                else if (node instanceof IMXMLPropertySpecifierNode)
                    havePropertyOverride = true;

                String suffix = ((IMXMLSpecifierNode)node).getSuffix();

                if (isState(suffix))
                {
                    // For a specifier node like label.s1="OK",
                    // add the node to the s1's list of state-dependent nodes.
                    addStateDependentNode(suffix, node);
                }
                else if (isStateGroup(suffix))
                {
                    // For a specifier node like label.g1="OK",
                    // add the node to the state-dependent node list
                    // of each state in group g1.
                    for (String state : getStatesInGroup(suffix))
                    {
                        addStateDependentNode(state, node);
                    }
                }
            }
        }

        RoyaleProject project = builder.getProject();

        if (haveInstanceOverride)
            builder.addExpressionDependency(project.getInstanceOverrideClass());
        if (havePropertyOverride)
            builder.addExpressionDependency(project.getPropertyOverrideClass());
        if (haveStyleOverride)
            builder.addExpressionDependency(project.getStyleOverrideClass());
        if (haveEventOverride)
            builder.addExpressionDependency(project.getEventOverrideClass());
    }

    private Set<String> processIncludeIn(String[] includeIn)
    {
        // Start with an empty set.
        Set<String> applicableStates = new HashSet<String>();

        // Add the included states or groups of states.
        for (String item : includeIn)
        {
            if (isState(item))
            {
                applicableStates.add(item);
            }
            else if (isStateGroup(item))
            {
                for (String state : getStatesInGroup(item))
                {
                    applicableStates.add(state);
                }
            }
        }

        return applicableStates;
    }

    private Set<String> processExcludeFrom(String[] excludeFrom)
    {
        // Start with the set of all states.
        Set<String> applicableStates = new HashSet<String>();
        for (String state : getStateNames())
        {
            applicableStates.add(state);
        }

        // Remove the excluded states or groups of states.
        for (String item : excludeFrom)
        {
            if (isState(item))
            {
                applicableStates.remove(item);
            }
            else if (isStateGroup(item))
            {
                for (String state : getStatesInGroup(item))
                {
                    applicableStates.remove(state);
                }
            }
        }

        return applicableStates;
    }

    public void generateID(IMXMLInstanceNode instanceNode)
    {
        if (instanceNode != null && instanceNode.getID() == null)
        {
            if (generatedIDMap.containsKey(instanceNode))
                return;
            String id = project.getGeneratedIDBase(this) + generatedIDCounter++;
            generatedIDMap.put(instanceNode, id);
        }
    }

    void addStateDependentNode(MXMLTreeBuilder builder, IMXMLNode node)
    {
        if (stateDependentNodeList == null)
            stateDependentNodeList = new ArrayList<IMXMLNode>();

        stateDependentNodeList.add(node);

        // The codegen for a state-dependent instance node will require
        // an autogenerated id if that node doesn't have a specified id.
        // The codegen for a state-dependent property/style/event node
        // will require an autogenerated id for the target instance node
        // if the instance node doesn't have a specified id.
        IMXMLInstanceNode instanceNode = null;
        if (node instanceof IMXMLInstanceNode)
            instanceNode = (IMXMLInstanceNode)node;
        // in case of root node, parent won't be IMXMLInstanceNode
        else if (node instanceof IMXMLSpecifierNode && node.getParent() instanceof IMXMLInstanceNode)
            instanceNode = (IMXMLInstanceNode)node.getParent();
        generateID(instanceNode);
    }

    @Override
    public String getGeneratedID(IMXMLInstanceNode instanceNode)
    {
        return generatedIDMap.get(instanceNode);
    }

    private void addStateDependentNode(String state, IMXMLNode node)
    {
        if (stateDependentNodeMap == null)
            stateDependentNodeMap = ArrayListMultimap.create();
        
        stateDependentNodeMap.put(state, node);
    }

    /**
     * Adds an entry to this class's state map, which maps state names to state
     * nodes.
     * <p>
     * If a previously processed state node had the same name, the specified
     * node is not added to the map, and the old node is returned; otherwise
     * <code>null</code> is returned.
     * 
     * @param node An {@code IMXMLStateNode} with an name.
     * @return The {@code IMXMLStateNode}, if any, that already has the same
     * name.
     */
    IMXMLStateNode addStateNode(IMXMLStateNode node)
    {
        if (stateMap == null)
        {
            stateMap = new HashMap<String, StateDefinition>();

            // The first state we find is the initial state.
            // TODO Should it be the first one in the 'states' property?
            initialState = node.getStateName();
        }
        StateDefinition oldState = stateMap.put(node.getStateName(),
                (StateDefinition)node.getDefinition());

        return oldState != null ? oldState.getNode() : null;
    }

    @Override
    public IMXMLMetadataNode[] getMetadataNodes()
    {
        return metadataNodes;
    }

    @Override
    public IMXMLScriptNode[] getScriptNodes()
    {
        return scriptNodes;
    }

    @Override
    public IMXMLDeclarationsNode[] getDeclarationsNodes()
    {
        return declarationsNodes;
    }

    @Override
    void setChildren(IMXMLNode[] children)
    {
        super.setChildren(children);

        if (children != null)
        {
            List<IMXMLMetadataNode> metadataNodes = new ArrayList<IMXMLMetadataNode>();
            List<IMXMLScriptNode> scriptNodes = new ArrayList<IMXMLScriptNode>();
            List<IMXMLDeclarationsNode> declarationsNodes = new ArrayList<IMXMLDeclarationsNode>();

            for (IMXMLNode child : children)
            {
                if (child instanceof IMXMLMetadataNode)
                    metadataNodes.add((IMXMLMetadataNode)child);
                else if (child instanceof IMXMLScriptNode)
                    scriptNodes.add((IMXMLScriptNode)child);
                else if (child instanceof IMXMLDeclarationsNode)
                    declarationsNodes.add((IMXMLDeclarationsNode)child);
            }

            this.metadataNodes = metadataNodes.toArray(new IMXMLMetadataNode[0]);
            this.scriptNodes = scriptNodes.toArray(new IMXMLScriptNode[0]);
            this.declarationsNodes = declarationsNodes.toArray(new IMXMLDeclarationsNode[0]);
        }
    }

    @Override
    public IScopedNode getScopedNode()
    {
        return this;
    }

    @Override
    public IASScope getScope()
    {
        return scope;
    }

    /**
     * Sets the class scope.
     * 
     * @param scope A {@code TypeScope} object for the scope contained by the
     * class defined by this tag.
     */
    public void setScope(TypeScope scope)
    {
        this.scope = scope;
    }

    @Override
    public void getAllImports(Collection<String> imports)
    {
        ArrayList<IImportNode> importNodes = new ArrayList<IImportNode>();
        getAllImportNodes(importNodes);
        for (IImportNode importNode : importNodes)
            imports.add(importNode.getImportName());
    }

    @Override
    public void getAllImportNodes(Collection<IImportNode> imports)
    {
        // Add implicit import nodes created by MXML tags.
        // The implicit imports for each MXML class are stored on its ClassDefinition.
        IMXMLFileNode fileNode = (IMXMLFileNode)getAncestorOfType(IMXMLFileNode.class);
        ICompilerProject project = fileNode.getCompilerProject();
        for (String qname : ((ClassDefinition)classDefinition).getImplicitImports())
        {
            imports.add(new MXMLImplicitImportNode(project, qname));
        }

        // Add the implicit import nodes associated with the file node.
        fileNode.getAllImportNodes(imports);

        // Add the explicit import nodes inside of <Script> tags.
        for (IMXMLScriptNode scriptNode : getScriptNodes())
        {
            for (IASNode node : scriptNode.getASNodes())
            {
                if (node instanceof ImportNode)
                    imports.add((IImportNode)node);
                else
                    ((NodeBase)node).collectImportNodes(imports);
            }
        }
    }

    @Override
    public IExpressionNode getNameExpressionNode()
    {
        // The name is determined by the file, not by anything in the file.
        return null;
    }

    @Override
    public int getNameStart()
    {
        // The name is determined by the file, not by anything in the file.
        return -1;
    }

    @Override
    public int getNameEnd()
    {
        // The name is determined by the file, not by anything in the file.
        return -1;
    }

    @Override
    public int getNameAbsoluteStart()
    {
        return getNameStart();
    }

    @Override
    public int getNameAbsoluteEnd()
    {
        return getNameEnd();
    }

    @Override
    public String getQualifiedName()
    {
        return classDefinition.getQualifiedName();
    }

    @Override
    public String getShortName()
    {
        return classDefinition.getBaseName();
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return classDefinition.hasModifier(modifier);
    }

    @Override
    public boolean hasNamespace(String namespace)
    {
        return getNamespace().equals(namespace);
    }

    @Override
    public String getNamespace()
    {
        return INamespaceConstants.public_;
    }

    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public IMetaTagsNode getMetaTags()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public IMetaInfo[] getMetaInfos()
    {
        return classDefinition.getAllMetaTags();
    }

    @Override
    public IClassDefinition getDefinition()
    {
        return classDefinition;
    }

    /**
     * Used only for debugging.
     */
    @SuppressWarnings("unused")
    private void dumpStateDependentNodes()
    {
        Set<String> states = getStateNames();
        if (states == null)
            return;

        for (String state : states)
        {
            System.out.println("State " + state);
            List<IMXMLNode> nodes = getNodesDependentOnState(state);
            if (nodes != null)
            {
                for (IMXMLNode node : nodes)
                {
                    System.out.println("  " + node);
                }
            }
        }
    }

    @Override
    public String getBaseClassName()
    {
        return getDefinition().getBaseClassAsDisplayString();
    }

    @Override
    public String[] getImplementedInterfaces()
    {
        return getDefinition().getImplementedInterfacesAsDisplayStrings();
    }

    @Override
    public IMetaTag[] getMetaTagsByName(String name)
    {
        return getDefinition().getMetaTagsByName(name);
    }

    @Override
    public ClassClassification getClassClassification()
    {
        return ClassClassification.PACKAGE_MEMBER;
    }
    
    @Override
    public IASDocComment getASDocComment()
    {
        return asDocComment;
    }

    @Override
    public boolean hasExplicitComment()
    {
        return asDocComment != null;
    }

    /**
     * Notifies this class definition node that an "inner" <fx:Component> has
     * been found. In response, this methods returns an autogenerated name for
     * the component class.
     */
    String addComponent()
    {
        // Return a string such as "MyAppInnerClass0" or "MyCompInnerClass2InnerClass1".
        StringBuilder sb = new StringBuilder();
        sb.append(getShortName());
        sb.append("InnerClass");
        sb.append(componentCount);

        componentCount++;

        return sb.toString();
    }

    @Override
    public boolean needsDescriptor()
    {
        return isContainer();
    }

    @Override
    public boolean needsDocumentDescriptor()
    {
        return isContainer();
    }

    public void setHasDataBindings()
    {
        hasDataBindings = true;
    }

    @Override
    public boolean getHasDataBindings()
    {
        return hasDataBindings;
    }
    
    public void setASDocComment(IASDocComment ref)
    {
        asDocComment = ref;
    }
}
