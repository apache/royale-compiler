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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;

/**
 * {@code MXMLSpecifierNodeBase} is the abstract base class for MXML node
 * classes that represent MXML property, event, and style specifiers.
 * <p>
 * Specifier nodes are children of tags that create instances of, or define
 * subclasses of, ActionScript classes; i.e., {@code MXMLInstanceNode} or
 * {@code MXMLClassDefinitionNode}, both of which which extend
 * {@code MXMLClassReferenceNode}.
 * <p>
 * For example, in the MXML code
 * 
 * <pre>
 * &lt;s:Button&gt; label="OK" color="#FF0000"
 *     &lt;s:click&gt;
 *     &lt;![CDATA[
 *         doThis();
 *         doThat();
 *     ]]&gt;
 *     &lt;/s:click&gt;
 * &lt;/s:Button&gt;
 * </pre>
 * 
 * the <code>label</code> attribute is a property specifier, the
 * <code>color</code> attribute is a style specifier, and the <code>click</code>
 * child tag is an event specifier.
 * <p>
 * This MXML would produce the following AST:
 * 
 * <pre>
 * MXMLInstanceNode
 *   MXMLPropertySpecifierNode
 *     ...
 *   MXMLStyleSpecifierNode
 *     ...
 *   MXMLEventSpecifierNode
 *     ...
 * </pre>
 */
abstract class MXMLSpecifierNodeBase extends MXMLNodeBase implements IMXMLSpecifierNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLSpecifierNodeBase(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The definition of the property, event, or style being specified.
     * <p>
     * This will be an {@code IVariableDefinition} or an
     * {@code ISetterDefinition} for a property, an {@code IEventDefinition} for
     * an event, and an {@code IStyleDefinition} for a style.
     * <p>
     * It will be <code>null</code> for a dynamic property.
     */
    private IDefinition definition;

    /**
     * The suffix on the attribute or child tag, specifying a state or state
     * group. If there is no suffix, this is null.
     */
    private String suffix;

    @Override
    public String getName()
    {
        // The definition can be null when getName() is called from toString()
        // in the debugger if the node is not yet fully initialized.
        return definition != null ? definition.getBaseName() : "";
    }

    @Override
    public IDefinition getDefinition()
    {
        return definition;
    }

    void setDefinition(IDefinition definition)
    {
        this.definition = definition;
    }

    @Override
    public String getSuffix()
    {
        return suffix;
    }

    void setSuffix(MXMLTreeBuilder builder, String suffix)
    {
        this.suffix = suffix;

        // Keep track of all nodes with suffixes on the class definition node,
        // for later MXML state processing.
        if (suffix != null && suffix.length() > 0)
            ((MXMLClassDefinitionNode)getClassDefinitionNode()).addStateDependentNode(builder, this);
    }

    /**
     * For debugging only. Builds a string such as <code>"label"</code>,
     * <code>"color"</code>, or <code>"click</code> from the name of the
     * property, style, or event being specified.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }

    /**
     * This method gives specifier nodes a chance to initialize themselves from
     * a property/style/event attribute.
     * <p>
     * The base class version calls <code>adjustOffset</code> to translate the
     * node start and end offset from local to absolute offsets.
     */
    protected void initializeFromAttribute(MXMLTreeBuilder builder,
                                           IMXMLTagAttributeData attribute,
                                           MXMLNodeInfo info)
    {
        setLocation(attribute);

        adjustOffsets(builder);
    }

    /**
     * This method gives specifier nodes a chance to initialize themselves from
     * a property/style/event attribute.
     * <p>
     * The base class version calls <code>adjustOffset</code> to translate the
     * node start and end offset from local to absolute offsets.
     */
    protected void initializeFromText(MXMLTreeBuilder builder,
                                      IMXMLTextData text,
                                      MXMLNodeInfo info)
    {
        setLocation(text);

        adjustOffsets(builder);
    }
}
