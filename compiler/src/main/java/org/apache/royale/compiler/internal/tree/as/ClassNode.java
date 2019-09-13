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

package org.apache.royale.compiler.internal.tree.as;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.common.RecursionGuard;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IClassDefinition.ClassClassification;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * ActionScript parse tree node representing a class definition
 */
public class ClassNode extends MemberedNode implements IClassNode
{
    /**
     * Constructor.
     * 
     * @param name The node holding this class name.
     */
    public ClassNode(ExpressionNodeBase name)
    {
        init(name);
    }

    /**
     * The class keyword
     */
    protected KeywordNode classKeywordNode;

    /**
     * The extends keyword (if one is present)
     */
    protected KeywordNode extendsKeywordNode;

    /**
     * The name of the base class
     */
    protected ExpressionNodeBase baseClassNode;

    /**
     * Cache of qualified name to use during type checking
     */
    private String qualifiedName;

    /**
     * The extends keyword (if one is present)
     */
    protected KeywordNode implementsKeywordNode;

    /**
     * Container full of interfaces implemented by this class
     */
    protected TransparentContainerNode interfacesNode;

    /**
     * Generated FunctionNode to represent explicit or default constructor
     */
    protected FunctionNode constructorNode;

    /**
     * Generated FunctionNode to represent default constructor (if there isn't
     * an explicit one)
     */
    protected FunctionNode defaultConstructorNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ClassID;
    }

    @Override
    public int getSpanningStart()
    {
        return getNodeStartForTooling();
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(classKeywordNode, fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(extendsKeywordNode, fillInOffsets);
        addChildInOrder(baseClassNode, fillInOffsets);
        addChildInOrder(implementsKeywordNode, fillInOffsets);
        if (implementsKeywordNode != null || interfacesNode.getChildCount() > 0)
            addChildInOrder(interfacesNode, fillInOffsets);
        addChildInOrder(contentsNode, fillInOffsets);
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            ClassDefinition definition = buildDefinition();
            setDefinition(definition);
            scope.addDefinition(definition);

            // The BlockNode inside a ClassNode creates a new ASScope,
            // with the current scope as its parent.
            // This new scope then gets passed down as the current scope.
            TypeScope typeScope = new TypeScope(scope, contentsNode, definition);
            definition.setContainedScope(typeScope);
            definition.setupThisAndSuper();
            definition.buildContingentDefinitions();
            scope = typeScope;
        }

        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            reconnectDef(scope);
            scope = this.getDefinition().getContainedScope();
            contentsNode.reconnectScope(scope);
        }

        // Recurse on the class block.
        contentsNode.analyze(set, scope, problems);

        // Recurse on the class metadata.
        MetaTagsNode metadata = getMetaTagsNode();
        if (metadata != null)
            metadata.analyze(set, scope, problems);

        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            // Look for a constructor, or add one if we can't find one
            setupConstructor(set, scope, problems);
        }

        // Don't call super.analyze, as we've already called analyze on some of our children
        // and calling it again would result in duplicate work
        //super.analyze(set, scope, errors);

        if (baseClassNode != null)
            baseClassNode.analyze(set, scope, problems);
        if (interfacesNode != null)
            interfacesNode.analyze(set, scope, problems);
    }
    
    /*
     * For debugging only.
     * Builds a string such as <code>"Button"</code> from
     * the name of the class.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }
    
    //
    // TreeNode overrides
    //
    
    @Override
    protected int getInitialChildCount()
    {
        return 6;
    }

    //
    // BaseDefinitionNode overrides
    //

    @Override
    protected void init(ExpressionNodeBase nameNode)
    {
        super.init(nameNode);
        
        extendsKeywordNode = null;
        baseClassNode = null;
        implementsKeywordNode = null;
        interfacesNode = new TransparentContainerNode();
        constructorNode = null;
        defaultConstructorNode = null;
    }
    
    @Override
    public ClassDefinition getDefinition()
    {
        return (ClassDefinition)super.getDefinition();
    }
    
    @Override
    public void setDefinition(IDefinition def)
    {
        assert def instanceof ClassDefinition;
        super.setDefinition(def);
    }
    
    //
    // IClassNode implementations
    //
    
    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public String getQualifiedName()
    {
        if (qualifiedName == null)
        {
            IImportTarget importTarget = ASImportTarget.buildImportFromPackageName(getWorkspace(), getPackageName());
            String qname = importTarget.getQualifiedName(getName());
            // #124877: core.as has a bunch of different packages in it, which is illegal.
            // just handle it quietly here.
            if (qname == null)
                qualifiedName = getShortName();
            qualifiedName = qname;
        }
        return qualifiedName;
    }

    @Override
    public String getShortName()
    {
        String name = getName();
        int lastDot = name.lastIndexOf(".");
        if (lastDot != -1)
            name = name.substring(lastDot + 1);
        return name;
    }
    
    @Override
    public ClassClassification getClassClassification()
    {
        if (getParent() instanceof FileNode)
            return ClassClassification.FILE_MEMBER;

        return ClassClassification.PACKAGE_MEMBER;
    }
    
    @Override
    public IMetaTag[] getMetaTagsByName(String name)
    {
        return getDefinition().getMetaTagsByName(name);
    }

    @Override
    public IMetaTagNode[] getMetaTagNodesByName(String name)
    {
        ArrayList<IMetaTagNode> allMatchingAttributes = new ArrayList<IMetaTagNode>();
        getMetaTagsByName(name, new RecursionGuard(), allMatchingAttributes);
        return allMatchingAttributes.toArray(new IMetaTagNode[0]);
    }

    @Override
    public IExpressionNode getBaseClassExpressionNode()
    {
        return baseClassNode;
    }

    @Override
    public String getBaseClassName()
    {
        return (baseClassNode instanceof IIdentifierNode) ? ((IIdentifierNode)baseClassNode).getName() : "";
    }
    
    @Override
    public IExpressionNode[] getImplementedInterfaceNodes()
    {
        ArrayList<IExpressionNode> names = new ArrayList<IExpressionNode>();
        int childCount = interfacesNode.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (interfacesNode.getChild(i) instanceof IIdentifierNode)
                names.add(((IExpressionNode)interfacesNode.getChild(i)));
        }
        return names.toArray(new IExpressionNode[0]);
    }
    
    @Override
    public String[] getImplementedInterfaces()
    {
        ArrayList<String> interfaceNodeList = new ArrayList<String>();
        if( interfacesNode != null )
        {
            int childCount = interfacesNode.getChildCount();
            if (childCount > 0)
            {
                for (int i = 0; i < childCount; i++)
                {
                    IASNode child = interfacesNode.getChild(i);
                    if (child instanceof IIdentifierNode)
                    {
                        interfaceNodeList.add(((IIdentifierNode)child).getName());
                    }
                }
            }
        }
        return interfaceNodeList.toArray(new String[0]);
    }

    @Override
    public IDefinitionNode[] getAllMemberNodes()
    {
        ArrayList<IDefinitionNode> names = new ArrayList<IDefinitionNode>();
        int childCount = contentsNode.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (contentsNode.getChild(i) instanceof IDefinitionNode)
                names.add(((IDefinitionNode)contentsNode.getChild(i)));
            else if (contentsNode.getChild(i).getNodeID() == ASTNodeID.ConfigBlockID)
            {
            	ConfigConditionBlockNode configNode = (ConfigConditionBlockNode)contentsNode.getChild(i);
            	int configChildCount = configNode.getChildCount();
            	for (int j = 0; j < configChildCount; j++)
            	{
                    if (configNode.getChild(j) instanceof IDefinitionNode)
                        names.add(((IDefinitionNode)configNode.getChild(j)));            		
            	}            	
            }
        }
        return names.toArray(new IDefinitionNode[0]);
    }
    
    //
    // Other methods
    //

    /**
     * Sets the class keyword if one is present. Used during parsing.
     * 
     * @param classKeyword token containing the keyword
     */
    public void setClassKeyword(IASToken classKeyword)
    {
        classKeywordNode = new KeywordNode(classKeyword);
    }

    /**
     * Sets the extends keyword if one is present. Used during parsing.
     * 
     * @param extendsKeyword token containing the keyword
     */
    public void setExtendsKeyword(IASToken extendsKeyword)
    {
        extendsKeywordNode = new KeywordNode(extendsKeyword);
    }

    /**
     * Set the base class of this class. Used during parsing.
     * 
     * @param baseClassNode node containing the base class name
     */
    public void setBaseClass(ExpressionNodeBase baseClassNode)
    {
        this.baseClassNode = baseClassNode;
    }

    /**
     * Sets the implements keyword if one is present. Used during parsing.
     * 
     * @param implementsKeyword token containing the keyword
     */
    public void setImplementsKeyword(IASToken implementsKeyword)
    {
        implementsKeywordNode = new KeywordNode(implementsKeyword);
    }

    /**
     * Add an implemented interface to this class. Used during parsing.
     * 
     * @param interfaceName node containing the interface name
     */
    public void addInterface(ExpressionNodeBase interfaceName)
    {
        interfacesNode.addChild(interfaceName);
    }

     /**
     * Method that will only build the explicit definitions for this AS3 class.
     * This is used by the MXML scope build code to build definitions for a
     * &lt;fx:Script&gt; tag.
     * 
     * @param classScope {@link TypeScope} into which this AS3 class' definition
     * should be added.
     */
    public void buildExplicitMemberDefs(TypeScope classScope)
    {
        // Recurse on the class block.
        contentsNode.analyze(EnumSet.of(PostProcessStep.POPULATE_SCOPE),
                             classScope, new ArrayList<ICompilerProblem>());
    }

    ClassDefinition buildDefinition()
    {
        // Ugh... CM allows
        // class definition that look like this:
        // public class org.apache.Foo {}
        // out fix for this is to run the string
        // through Multiname.getBaseNameForQName.
        String definitionName = Multiname.getBaseNameForQName(nameNode.computeSimpleReference());
        INamespaceReference namespaceReference = NamespaceDefinition.createNamespaceReference(getASScope(), getNamespaceNode());

        ClassDefinition definition = new ClassDefinition(definitionName, namespaceReference);
        definition.setNode(this);

        fillInModifiers(definition);
        fillInMetadata(definition);
        fillInStateNames(definition);

        // Set the base class.
        IReference baseRef = null;
        if (baseClassNode != null)
            baseRef = baseClassNode.computeTypeReference();
        definition.setBaseClassReference(baseRef);

        // Set the implemented interfaces.
        if (interfacesNode != null)
        {
            int n = interfacesNode.getChildCount();
            List<IReference> interfaces = new ArrayList<IReference>(n);
            for (int i = 0; i < n; i++)
            {
                IASNode child = interfacesNode.getChild(i);
                if (child instanceof ExpressionNodeBase)
                {
                    IReference typeReference = ((ExpressionNodeBase)child).computeTypeReference();
                    if (typeReference != null)
                        interfaces.add(typeReference);
                }
            }
            definition.setImplementedInterfaceReferences(interfaces.toArray(new IReference[interfaces.size()]));
        }

        return definition;
    }

    private void fillInStateNames(ClassDefinition definition)
    {
        IMetaTag[] statesMetaData = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_STATES);
        for (IMetaTag stateMetaData : statesMetaData)
        {
            for (IMetaTagAttribute attribute : stateMetaData.getAllAttributes())
            {
                // only look at the value of the attribute, ignoring any
                // keys which may have been (incorrectly) specified.  This matches
                // the behavior of the old compiler.
                definition.addStateName(attribute.getValue());
            }
        }
    }

    private void setupConstructor(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        FunctionDefinition ctorDef = null;
        // If there's not an explicit constructor, use an implicit one.
        if (constructorNode == null)
        {
            // We don't have an explicit constructor
            // so we'll create one and add it to the ClassNode
            IdentifierNode constructorNameNode = new IdentifierNode(getName());
            constructorNameNode.setReferenceValue(getDefinition());
            constructorNameNode.span(getNameAbsoluteStart(), getNameAbsoluteEnd(), -1, -1, -1, -1);
            defaultConstructorNode = new FunctionNode(null, constructorNameNode);
            NamespaceIdentifierNode pub = new NamespaceIdentifierNode(INamespaceConstants.public_);
            pub.span(-1, -1, -1, -1, -1, -1);
            defaultConstructorNode.setNamespace(pub);
            defaultConstructorNode.normalize(true);
            defaultConstructorNode.setParent(contentsNode);
            ctorDef = defaultConstructorNode.buildDefinition();
            ctorDef.setImplicit();

            scope.addDefinition(ctorDef);

            assert constructorNode == defaultConstructorNode : "FunctionNode.buildDefinition should set the constructor node field";

        }
        else
        {
            ctorDef = constructorNode.getDefinition();
        }

        // We need to tell the constructor definition
        // that it is the definition of a constructor.
        assert ctorDef != null;
        ctorDef.setAsConstructor((ClassDefinition)scope.getDefinition());
    }

    /**
     * Get the node representing the constructor
     * 
     * @return explicit or default constructor
     */
    public FunctionNode getConstructorNode()
    {
        return constructorNode;
    }

    /**
     * Get the node representing the default constructor (if there's no explicit
     * constructor)
     * 
     * @return default constructor
     */
    public FunctionNode getDefaultConstructorNode()
    {
        return defaultConstructorNode;
    }

    /**
     * Get the implemented interface names (as they appear in the class
     * definition)
     * 
     * @return array of interface names
     */
    public String[] getInterfaceNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        int childCount = interfacesNode.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (interfacesNode.getChild(i) instanceof IIdentifierNode)
                names.add(((IIdentifierNode)interfacesNode.getChild(i)).getName());
        }
        return names.toArray(new String[0]);
    }

    /**
     * Get the node containing the class keyword
     * 
     * @return the node containing class
     */
    public KeywordNode getClassKeywordNode()
    {
        return classKeywordNode;
    }

    /**
     * Get the node containing the extends keyword, if one exists
     * 
     * @return the node containing extends
     */
    public KeywordNode getExtendsKeywordNode()
    {
        return extendsKeywordNode;
    }

    /**
     * Get the node containing the base class, if one exists
     * 
     * @return the node containing the base class
     */
    public ExpressionNodeBase getBaseClassNode()
    {
        return baseClassNode;
    }

    /**
     * Get the node containing the implements keyword, if one exists
     * 
     * @return the node containing implements
     */
    public KeywordNode getImplementsKeywordNode()
    {
        return implementsKeywordNode;
    }

    /**
     * Get the container of interfaces for this class
     * 
     * @return the node containing the interfaces
     */
    public ContainerNode getInterfacesNode()
    {
        return interfacesNode;
    }

    /**
     * Retrieves all of the matching meta attributes associated with this class
     * or any of its base classes
     * 
     * @param name name of meta attributes to retrieve
     * @param recursionGuard guard to help avoid infinite loops in the base
     * class hierarchy
     * @param allMatchingAttributes list to be filled with all matching meta
     * attributes
     */
    protected void getMetaTagsByName(String name, RecursionGuard recursionGuard, ArrayList<IMetaTagNode> allMatchingAttributes)
    {
        IMetaTagsNode metaTags = getMetaTags();
        if (metaTags != null)
        {
            IMetaTagNode[] matchingAttributes = metaTags.getTagsByName(name);
            allMatchingAttributes.addAll(Arrays.asList(matchingAttributes));
        }
        //TODO where is metadata stored
        //		if(!IMetaAttributeConstants.NON_INHERITING_METATAGS.contains(name)) {
        //			IClassNode baseClass = getBaseClassDefinition(recursionGuard);
        //			if (baseClass instanceof ClassNode)
        //				((ClassNode) baseClass).getMetaTagsByName(name, recursionGuard, allMatchingAttributes);
        //		}
    }

    /**
     * If a classes containing file has includes, it's metadata values cannot be
     * cached
     * 
     * @return true if they can be cached
     */
    public boolean canCacheMetaTags()
    {
        //when we build, if we have includes, we can't cache these values.  Unless we're a swc
        //and in that case, we don't have to worry about the included definitions changing
        FileNode fileNode = ((FileNode)getAncestorOfType(FileNode.class));
        if (!fileNode.hasIncludes())
            return true;
        
        return false;
    }
}
