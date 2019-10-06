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

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.tree.as.parts.DecorationPart;
import org.apache.royale.compiler.internal.tree.as.parts.IDecorationPart;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

// TODO IDefinitionNode is unnecessary here?
public abstract class BaseDefinitionNode extends TreeNode implements IDocumentableDefinitionNode, IDefinitionNode
{
    // Returns start if the child start is < 0,
    // otherwise returns min(start, child start)/
    private static int accumulateChildStart(IASNode child, int start)
    {
        int childStart = child.getStart();
        
        if (childStart >= 0 && childStart < start)
            start = childStart;

        return start;
    }

    private static boolean shouldIgnoreChildForTooling(IASNode child)
    {
        return child instanceof IMetaTagsNode;
    }
    
    /**
     * Constructor.
     */
    public BaseDefinitionNode()
    {
        super();
    }
    
    /**
     * The name of the definition
     */
    protected ExpressionNodeBase nameNode;

    /**
     * {@link IDecorationPart} that will hold the decorations for this node
     */
    private IDecorationPart decorationPart;

    protected IDefinition definition;

    //
    // NodeBase overrides
    //

    @Override
    public int getSpanningStart()
    {
        int result = getAbsoluteStart();
        
        // Internal namespaces have a start of -1.  Not sure if modifiers can have a -1 value too
        // but it can't hurt to check.  -gse
        ModifiersSet modifiers = getModifiers();
        
        INamespaceDecorationNode namespace = decorationPart != null ? decorationPart.getNamespace() : null;
        
        if (modifiers != null && modifiers.getStart() < result && modifiers.getStart() >= 0)
            result = modifiers.getStart();
        
        if (namespace != null && namespace.getAbsoluteStart() < result && namespace.getAbsoluteStart() >= 0)
            result = namespace.getAbsoluteStart();
        
        return result;
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
            reconnectDef(scope);

        super.analyze(set, scope, problems);
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
        
        if (decorationPart != null)
            decorationPart.compact();
    }
    
    //
    // IDocumentableDefinitionNode implementations
    //
    
    @Override
    public boolean hasExplicitComment()
    {
        return decorationPart.getASDocComment() != null;
    }

    @Override
    public IMetaTagsNode getMetaTags()
    {
        return decorationPart.getMetadata();
    }
    
    @Override
    public IMetaInfo[] getMetaInfos()
    {
        IMetaTagsNode metaTagsNode = getMetaTags();
        if (metaTagsNode == null)
            return new IMetaInfo[0];
        
        return metaTagsNode.getAllTags();
    }

    @Override
    public boolean hasNamespace(String namespace)
    {
        String thisNamespace = getNamespace();
        if (thisNamespace != null)
        {
            if (thisNamespace.indexOf('.') != -1 || namespace.indexOf('.') != -1)
            {
                //TODO what do we do here?
                //              //take fully qualifed namespaces into consideration
                //              INamespaceNode resolveNamespace = resolveNamespace();
                //              if(resolveNamespace != null) {
                //                  return namespace.indexOf('.') == -1 ? resolveNamespace.getShortName().compareTo(namespace) == 0
                //                          : resolveNamespace.getQualifiedName().compareTo(namespace) == 0;
                //              }

            }
            return thisNamespace.compareTo(namespace) == 0;
        }
        return false;
    }

    @Override
    public String getNamespace()
    {
        INamespaceDecorationNode namespace = decorationPart.getNamespace();
        return namespace != null ? namespace.getName() : INamespaceConstants.internal_;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        ModifiersContainerNode modifiers = decorationPart.getModifiers();
        return modifiers != null ? modifiers.getModifierSet().hasModifier(modifier) : false;
    }

    @Override
    public IExpressionNode getNameExpressionNode()
    {
        return nameNode;
    }

    @Override
    public String getName()
    {
        return nameNode instanceof IdentifierNode ? ((IdentifierNode)nameNode).getName() : "";
    }
    
    @Override
    public int getNameStart()
    {
        return nameNode != null ? nameNode.getStart() : -1;
    }

    @Override
    public int getNameEnd()
    {
        return nameNode != null ? nameNode.getEnd() : -1;
    }

    @Override
    public int getNameAbsoluteStart()
    {
        return nameNode != null ? nameNode.getAbsoluteStart() : -1;
    }

    @Override
    public int getNameAbsoluteEnd()
    {
        return nameNode != null ? nameNode.getAbsoluteEnd() : -1;
    }

    @Override
    public DefinitionBase getDefinition()
    {
        return (DefinitionBase)definition;
    }

    //
    // Other methods
    //

    protected void init(ExpressionNodeBase nameNode)
    {
        this.nameNode = nameNode;
        decorationPart = createDecorationPart();
    }

    protected IDecorationPart createDecorationPart()
    {
        return new DecorationPart();
    }

    protected IDecorationPart getDecorationPart()
    {
        return decorationPart;
    }

    /**
     * Set the metadata tags of the item.
     * 
     * @param tags the current collection of {@link IMetaTagNode} tags
     */
    public void setMetaTags(MetaTagsNode tags)
    {
        if (decorationPart != null && tags != null)
        {
            tags.setDecorationTarget(this);
            decorationPart.setMetadata(tags);
        }
    }

    public void setNamespace(INamespaceDecorationNode namespace)
    {
        if (namespace != null)
        {
            NodeBase namespaceNode = (NodeBase)namespace;
            namespaceNode.setParent(this);
            decorationPart.setNamespace(namespace);
        }
    }

    public INamespaceDecorationNode getNamespaceNode()
    {
        return decorationPart.getNamespace();
    }

    protected MetaTagsNode getMetaTagsNode()
    {
        return decorationPart.getMetadata();
    }

    public void addModifier(ModifierNode node)
    {
        ModifiersContainerNode modifiers = decorationPart.getModifiers();
        if (modifiers == null)
        {
            modifiers = new ModifiersContainerNode();
            decorationPart.setModifiers(modifiers);
        }
        modifiers.addModifier(node);
    }

    public void setModifiersContainer(ModifiersContainerNode container)
    {
        decorationPart.setModifiers(container);
    }

    /**
     * Get the modifiers of this variable.
     * 
     * @return modifiers node
     */
    public ModifiersSet getModifiers()
    {
        ModifiersContainerNode modifiers = decorationPart != null ? decorationPart.getModifiers() : null;
        if (modifiers != null)
            return modifiers.getModifierSet();
        return null;
    }

    public ModifiersContainerNode getModifiersContainer()
    {
        return decorationPart.getModifiers();
    }

    /**
     * returns the start of the first child node that is not ignored by the
     * tooling. This is a replacement for getSpanningStart. Now getStart
     * includes metadata and keywords, so getStart always includes that stuff.
     * But in most cases the tooling does not want to count the metadata. TODO:
     * consider removing getSpanningStart and all of the crazy overrides, and
     * replace with this function
     */
    protected int getNodeStartForTooling()
    {
        int start = getEnd(); // start with a guess that is higher than possible
        for (int i = 0; i < getChildCount(); ++i)
        {
            // keep moving the start up to include children we care about
            IASNode child = getChild(i);
            if (!shouldIgnoreChildForTooling(child))
            {
                int childStart = accumulateChildStart(child, start);
                if (childStart < start)
                    return childStart;
            }
        }
        return start;
    }

    public void setASDocComment(IASDocComment ref)
    {
        if (decorationPart != null)
            decorationPart.setASDocComment(ref);
    }

    /**
     * Returns the raw {@link IASDocComment} without any processing
     * 
     * @return an {@link IASDocComment} or null
     */
    public IASDocComment getASDocComment()
    {
        return decorationPart.getASDocComment();
    }

    protected void addDecorationChildren(boolean fillInOffsets)
    {
        addChildInOrder(decorationPart.getMetadata(), fillInOffsets);
        addChildInOrder(decorationPart.getModifiers(), fillInOffsets);
        addChildInOrder((NodeBase)decorationPart.getNamespace(), fillInOffsets);
    }

    /**
     * Helper method to fill in namespace, and modifier info for a definition.
     * This does not do any validation of the modifiers - the code calling this
     * method still needs to make sure that the modifiers are appropriate for
     * the specific definition type.
     * 
     * @param db
     */
    protected void fillInNamespaceAndModifiers(DefinitionBase db)
    {
        INamespaceReference namespaceReference = NamespaceDefinition.createNamespaceReference(
                getASScope(), getNamespaceNode(), this.hasModifier(ASModifier.STATIC));

        db.setNamespaceReference(namespaceReference);

        fillInModifiers(db);
    }

    protected void fillInModifiers(DefinitionBase db)
    {
        this.setDefinition(db);

        if (hasModifier(ASModifier.DYNAMIC))
            db.setDynamic();
        if (hasModifier(ASModifier.FINAL))
            db.setFinal();
        if (hasModifier(ASModifier.NATIVE))
            db.setNative();
        if (hasModifier(ASModifier.OVERRIDE))
            db.setOverride();
        if (hasModifier(ASModifier.STATIC))
            db.setStatic();
        if (hasModifier(ASModifier.ABSTRACT))
            db.setAbstract();
    }

    protected void fillInMetadata(DefinitionBase definition)
    {
        IMetaTagsNode metaTagsNode = getMetaTagsNode();
        if (metaTagsNode == null)
            return;

        IMetaTag[] metaTags = ((MetaTagsNode)metaTagsNode).buildMetaTags(getFileSpecification(), definition);
        definition.setMetaTags(metaTags);
    }

    protected void setDefinition(IDefinition definition)
    {
        this.definition = definition;
    }

    /**
     * Helper method to reconnect this node to it's corresponding definition
     * This method will search the containingScope for a definition whose start
     * offset matches the start offset of this node. If the AST no longer
     * matches the Definitions (a different node is present at the offset for a
     * definition, or there is no matching definition for a Node, etc) then this
     * method will not reconnect the Node to the Definition. This is expected,
     * and it probably means that the file has changed, but the compiler client
     * has not told us that the file changed. It is expected that this method
     * will only be called for ASTs where the client knows the file has not
     * changed since the definitions were constructed.
     * 
     * @param containingScope the scope that contains the definition of this
     * node
     */
    void reconnectDef(ASScope containingScope)
    {
        String baseName = getName();
        IDefinitionSet s = containingScope.getLocalDefinitionSetByName(baseName);
        if (s != null)
        {
            for (int i = 0, l = s.getSize(); i < l; ++i)
            {
                IDefinition d = s.getDefinition(i);
                if (d.getAbsoluteStart() == this.getAbsoluteStart())
                {
                    this.setDefinition(d);
                    ((DefinitionBase)d).setNode(this);
                }
            }
        }
    }
}
