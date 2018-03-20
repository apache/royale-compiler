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

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_EXCLUDE_FROM;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_ID;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_LOCAL_ID;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_INCLUDE_IN;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_ITEM_CREATION_POLICY;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_ITEM_DESTRUCTION_POLICY;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ITEM_CREATION_POLICY_DEFERRED;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ITEM_CREATION_POLICY_IMMEDIATE;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ITEM_DESTRUCTION_POLICY_AUTO;
import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ITEM_DESTRUCTION_POLICY_NEVER;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLAttributeVersionProblem;
import org.apache.royale.compiler.problems.MXMLDuplicateIDProblem;
import org.apache.royale.compiler.problems.MXMLIncludeInAndExcludeFromProblem;
import org.apache.royale.compiler.problems.MXMLInvalidIDProblem;
import org.apache.royale.compiler.problems.MXMLInvalidItemCreationPolicyProblem;
import org.apache.royale.compiler.problems.MXMLInvalidItemDestructionPolicyProblem;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;

class MXMLInstanceNode extends MXMLClassReferenceNodeBase implements IMXMLInstanceNode
{
	private static boolean isStateClass(String instanceType, RoyaleProject project)
	{
		// we are going to require that all subclasses are also named State
		// but just in different packages.  That way we don't have to keep resolving
		// instanceType
		if (!instanceType.endsWith(".State"))
			return false;
		
		if (instanceType.equals(project.getStateClass()))
			return true;
		
        IResolvedQualifiersReference instanceRef = ReferenceFactory.packageQualifiedReference(
                project.getWorkspace(), instanceType);
        ITypeDefinition instanceTypeDef = (ITypeDefinition)instanceRef.resolve(project);
        
        if (project.stateClassType == null)
        {
            IResolvedQualifiersReference stateRef = ReferenceFactory.packageQualifiedReference(
                    project.getWorkspace(), project.getStateClass());
            project.stateClassType = (ITypeDefinition)stateRef.resolve(project);
        }

        return (instanceTypeDef.isInstanceOf(project.stateClassType, project));
	}
	
    protected static MXMLInstanceNode createInstanceNode(MXMLTreeBuilder builder,
                                                         String instanceType,
                                                         NodeBase parent)
    {
        MXMLDialect mxmlDialect = builder.getMXMLDialect();

        if (instanceType.equals(IASLanguageConstants.Boolean))
            return new MXMLBooleanNode(parent);

        else if (instanceType.equals(IASLanguageConstants._int))
            return new MXMLIntNode(parent);

        else if (instanceType.equals(IASLanguageConstants.uint))
            return new MXMLUintNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Number))
            return new MXMLNumberNode(parent);

        else if (instanceType.equals(IASLanguageConstants.String))
            return new MXMLStringNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Class))
            return new MXMLClassNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Object))
            return new MXMLObjectNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Array))
            return new MXMLArrayNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Vector_qname))
            return new MXMLVectorNode(parent);

        else if (isStateClass(instanceType, builder.getProject()) && mxmlDialect != MXMLDialect.MXML_2006)
            return new MXMLStateNode(parent);

        else if (instanceType.equals(builder.getProject().getWebServiceQName()))
            return new MXMLWebServiceNode(parent);

        else if (instanceType.equals(builder.getProject().getRemoteObjectQName()))
            return new MXMLRemoteObjectNode(parent);

        else if (instanceType.equals(builder.getProject().getHTTPServiceQName()))
            return new MXMLHTTPServiceNode(parent);

        else if (instanceType.equals(builder.getProject().getDesignLayerQName()))
            return new MXMLDesignLayerNode(parent);

        else if (instanceType.equals(builder.getProject().getRepeaterQName()))
            return new MXMLRepeaterNode(parent);

        else if (instanceType.equals(IASLanguageConstants.XML))
            return new MXMLXMLNode(parent);

        else if (instanceType.equals(IASLanguageConstants.XMLList))
            return new MXMLXMLListNode(parent);

        else if (instanceType.equals(IASLanguageConstants.Function))
            return new MXMLFunctionNode(parent);

        else if (instanceType.equals(IASLanguageConstants.RegExp))
            return new MXMLRegExpNode(parent);

        else
            return new MXMLInstanceNode(parent);
    }

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLInstanceNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The compile-time identifier for this instance. This is <code>null</code>
     * if there is no compile-time <code>id</code> attribute on the tag that
     * produced this instance.
     */
    private String id;

    /**
     * The compile-time identifier for this instance. This is <code>null</code>
     * if there is no compile-time <code>id</code> attribute on the tag that
     * produced this instance.  A localId does not set the id property used
     * by CSS.
     */
    private String localId;

    /**
     * The states (or state groups) that include this instance. This is
     * <code>null</code> if there was no <code>includeIn</code> attribute on the
     * tag that produced this instance node.
     */
    private String[] includeIn;

    /**
     * The states (or state groups) that exclude this instance. This is
     * <code>null</code> if there was no <code>excludeFrom</code> attribute on
     * the tag that produced this instance node.
     */
    private String[] excludeFrom;

    /**
     * The item creation policy for this instance, as specified by the
     * <code>itemCreationPolicy</code> attribute. This is <code>null</code> if
     * there was no such attribute on the tag that produced this instance node,
     * or if the attribute didn't specify either <code>"immediate"</code> or
     * <code>"deferred"</code>.
     */
    private String itemCreationPolicy;

    /**
     * The item destruction policy for this instance, as specified by the
     * <code>itemCreationPolicy</code> attribute. This is <code>null</code> if
     * there was no such attribute on the tag that produced this instance node,
     * or if the attribute didn't specify either <code>"auto"</code> or
     * <code>"never"</code>.
     */
    private String itemDestructionPolicy;

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_ID))
            id = processIDAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_LOCAL_ID))
            localId = processIDAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_INCLUDE_IN))
            includeIn = processIncludeInOrExcludeFromAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_EXCLUDE_FROM))
            excludeFrom = processIncludeInOrExcludeFromAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_ITEM_CREATION_POLICY))
            itemCreationPolicy = processItemCreationPolicyAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_ITEM_DESTRUCTION_POLICY))
            itemDestructionPolicy = processItemDestructionPolicyAttribute(builder, attribute);

        else
            super.processTagSpecificAttribute(builder, tag, attribute, info);
    }

    private String processIDAttribute(MXMLTreeBuilder builder,
                                      IMXMLTagAttributeData attribute)
    {
        String value = attribute.getRawValue();
        if (value == null)
            value = "";

        // Royale trims this attribute even though the old compiler didn't.
        MXMLDialect mxmlDialect = builder.getMXMLDialect();
        value = mxmlDialect.trim(value);

        // The id must be a valid ActionScript identifier.
        // Otherwise it will be null.
        if (!isValidASIdentifier(value))
        {
            ICompilerProblem problem = new MXMLInvalidIDProblem(attribute);
            builder.addProblem(problem);
            return null;
        }

        // The id must be unique within the class.
        // Otherwise it will be null.
        IMXMLInstanceNode previousNodeWithSameID =
                ((MXMLClassDefinitionNode)getClassDefinitionNode()).addNodeWithID(value, this);

        if (previousNodeWithSameID != null && !isEqual(previousNodeWithSameID))
        {
            ICompilerProblem problem = new MXMLDuplicateIDProblem(attribute);
            builder.addProblem(problem);
            return null;
        }

        return value;
    }

    protected String processItemCreationPolicyAttribute(MXMLTreeBuilder builder,
                                                        IMXMLTagAttributeData attribute)
    {
        MXMLDialect mxmlDialect = builder.getMXMLDialect();
        if (mxmlDialect == MXMLDialect.MXML_2006)
        {
            ICompilerProblem problem = new MXMLAttributeVersionProblem(attribute, attribute.getName(), "2009");
            builder.addProblem(problem);
            return null;
        }

        String value = attribute.getRawValue();
        value = mxmlDialect.trim(value);

        // The itemCreationPolicy must be "immediate" or "deferred";
        // otherwise, it will be null.
        if (!value.equals(ITEM_CREATION_POLICY_IMMEDIATE) &&
            !value.equals(ITEM_CREATION_POLICY_DEFERRED))
        {
            ICompilerProblem problem = new MXMLInvalidItemCreationPolicyProblem(attribute);
            builder.addProblem(problem);
            return null;
        }

        return value;
    }

    protected String processItemDestructionPolicyAttribute(MXMLTreeBuilder builder,
                                                           IMXMLTagAttributeData attribute)
    {
        MXMLDialect mxmlDialect = builder.getMXMLDialect();
        if (mxmlDialect == MXMLDialect.MXML_2006)
        {
            ICompilerProblem problem = new MXMLAttributeVersionProblem(attribute, attribute.getName(), "2009");
            builder.addProblem(problem);
            return null;
        }

        String value = attribute.getRawValue();
        value = mxmlDialect.trim(value);

        // The itemDestructionPolicy must be "auto" or "nevet";
        // otherwise, it will be null.
        if (!value.equals(ITEM_DESTRUCTION_POLICY_AUTO) &&
            !value.equals(ITEM_DESTRUCTION_POLICY_NEVER))
        {
            ICompilerProblem problem = new MXMLInvalidItemDestructionPolicyProblem(attribute);
            builder.addProblem(problem);
            return null;
        }

        return value;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        // If no id was specified for the instance, generate one unconditionally.
        // TODO Only generate an id when necessary.
        if (id == null)
            ((MXMLClassDefinitionNode)getClassDefinitionNode()).generateID(this);

        // If both includeIn and excludeFrom are specified, set both to null.
        if (includeIn != null && excludeFrom != null)
        {
            ICompilerProblem problem = new MXMLIncludeInAndExcludeFromProblem(tag);
            builder.addProblem(problem);
            includeIn = null;
            excludeFrom = null;
        }

        RoyaleProject project = builder.getProject();
        IClassDefinition classReference = getClassReference(project);
        if (classReference != null)
        {
            String qname = classReference.getQualifiedName();
            builder.addDependency(qname, DependencyType.EXPRESSION);
            if (id != null)
                builder.addDependency(qname, DependencyType.SIGNATURE);
        }
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLInstanceID;
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public String getLocalID()
    {
        return localId;
    }

    @Override
    public String getEffectiveID()
    {
        return id != null ? id : 
        	localId != null ? localId : getClassDefinitionNode().getGeneratedID(this);
    }

    @Override
    public IVariableDefinition resolveID()
    {
        // Get the variable definition for the id from the class scope.
        MXMLClassDefinitionNode classDefinitionNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();
        IClassDefinition classDefinition = classDefinitionNode.getClassDefinition();
        ASScope classScope = (ASScope)classDefinition.getContainedScope();
        IDefinitionSet definitionSet = classScope.getLocalDefinitionSetByName(id);
        int n = definitionSet.getSize();
        for (int i = 0; i < n; i++)
        {
            IDefinition definition = definitionSet.getDefinition(i);
            if (definition instanceof IVariableDefinition)
                return (IVariableDefinition)definition;
        }
        return null;
    }

    @Override
    public String[] getIncludeIn()
    {
        return includeIn;
    }

    @Override
    public String[] getExcludeFrom()
    {
        return excludeFrom;
    }

    @Override
    public String getItemCreationPolicy()
    {
        return itemCreationPolicy;
    }

    @Override
    public String getItemDestructionPolicy()
    {
        return itemDestructionPolicy;
    }

    @Override
    public boolean needsDescriptor()
    {
        // MX containers need descriptors.
        if (isContainer())
            return true;

        // So do any visual children (MX or Spark) of an MX container.
        if (isDeferredInstantiationUIComponent())
        {
            IASNode parent = getParent();
            if (parent instanceof IMXMLClassReferenceNode &&
                ((IMXMLClassReferenceNode)parent).isContainer())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean needsDocumentDescriptor()
    {
        // MX containers need document descriptors.
        // unless they are visual children of another MX container.
        return isContainer() &&
               getParent() instanceof IMXMLInstanceNode &&
               !((IMXMLInstanceNode)getParent()).isContainer();
    }

    /**
     * For debugging only. Builds a string such as
     * <code>"spark.components.Button" id="b1"</code> from the qualified name of
     * the class reference for the node and its id, if present.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        super.buildInnerString(sb);

        String id = getID();
        if (id != null)
        {
            sb.append(' ');
            sb.append("id=");
            sb.append('"');
            sb.append(id);
            sb.append('"');
        }

        return true;
    }
    
    public boolean isEqual(IMXMLInstanceNode node)
    {
        return (node.getLine() == getLine() &&
                node.getColumn() == getColumn());
    }
}
