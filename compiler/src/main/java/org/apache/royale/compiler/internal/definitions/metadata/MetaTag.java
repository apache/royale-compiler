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

package org.apache.royale.compiler.internal.definitions.metadata;

import java.util.Arrays;

import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.NodeReference;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

public class MetaTag implements IMetaTag
{
    /**
     * Append a {@link IMetaTag} to an array of {@link IMetaTag}.
     * 
     * @param metaTags An existing array of meta tags. May be empty but
     * may not be null.
     * @param metaTag The new meta tag to append to the metaTags array.
     * If null, the metaTags parameter is returned unmodified.
     * @return The new array of meta tags.
     */
    public static IMetaInfo[] addMetaTag(IMetaInfo[] metaTags, IMetaInfo metaTag)
    {
        assert metaTags != null;
        
        if (metaTag != null)
        {
            IMetaInfo[] newMetaTags = Arrays.copyOf(metaTags, metaTags.length + 1, IMetaInfo[].class);
            newMetaTags[metaTags.length] = metaTag;
            metaTags = newMetaTags;
        }

        return metaTags;
    }
    
    /**
     * Create a new meta tag for either "__go_to_ctor_definition_help" or
     * "__go_to_definition_help".
     * 
     * @param definition The definition to add the meta data for.
     * @param file The absolute path of the source file the definition is found
     *  in. May be null.
     * @param pos The position of the definition in the source file. If "-1"
     * no MetaTag is created.
     * @param ctor True if the definition is for a constructor, false otherwise.
     * @return A new MetaTag. If the pos paramater is "-1", null is returned.
     */
    public static MetaTag createGotoDefinitionHelp(IDefinition definition, 
            String file, String pos, boolean ctor)
    {
        assert pos != null;
        
        if (pos.equals("-1"))
            return null;
        
        IMetaTagAttribute[] attributes = new MetaTagAttribute[file != null ? 2 : 1];
        if (file != null)
        {
            attributes[0] = new MetaTagAttribute(IMetaAttributeConstants.NAME_GOTODEFINITIONHELP_FILE,
                    file); 
        }

        attributes[file != null ? 1 : 0] = new MetaTagAttribute(IMetaAttributeConstants.NAME_GOTODEFINITIONHELP_POS,
                pos); 

        return new MetaTag(definition, 
                ctor ? IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITION_CTOR_HELP :
                       IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITIONHELP, 
                        attributes);
    }
    
    public MetaTag(IDefinition decoratedDefinition, String tagName, IMetaTagAttribute[] attributes)
    {
        this.decoratedDefinition = decoratedDefinition;
        this.tagName = tagName;
        if (attributes == null)
        {
            // this is a low cost way to make sure that clients never get null attributes
            attributes = emptyAttributes;
        }
        this.attributes = attributes;
    }

    private IDefinition decoratedDefinition;

    private String tagName;

    private IMetaTagAttribute[] attributes;

    // Singleton empty array to be shared by all instances that don't have attributes
    private static final IMetaTagAttribute[] emptyAttributes = new IMetaTagAttribute[0];

    private String sourcePath;

    private int absoluteStart = UNKNOWN;

    private int absoluteEnd = UNKNOWN;

    private int line = UNKNOWN;

    private int column = UNKNOWN;

    // Hold a reference to the node this definition came from
    // (NodeReference only holds onto the node weakly, so we don't have to worry about leaks).
    private NodeReference nodeRef = NodeReference.noReference;

    @Override
    public String getTagName()
    {
        return tagName;
    }

    @Override
    public IMetaTagAttribute[] getAllAttributes()
    {
        return attributes;
    }

    @Override
    public IMetaTagAttribute getAttribute(String key)
    {
        for (IMetaTagAttribute attribute : attributes)
        {
            String attrKey = attribute.getKey();
            if (attrKey != null && attrKey.equals(key))
                return attribute;
        }
        return null;
    }

    @Override
    public String getAttributeValue(String key)
    {
        for (IMetaTagAttribute attribute : attributes)
        {
            // For metadata such as [Foo("abc", "def")],
            // the attribute keys are null, so a null
            // check on the key is necessary.
            // BTW, keyless values like "abc" and "def"
            // cannot be retrieved by this API;
            // you have to use getAttributes()[i].getValue().
            String attrKey = attribute.getKey();
            if (attrKey != null && attrKey.equals(key))
                return attribute.getValue();
        }
        return null;
    }

    @Override
    public String getSourcePath()
    {
        return sourcePath;
    }

    private OffsetLookup getOffsetLookup()
    {
        DefinitionBase definition = (DefinitionBase)getDecoratedDefinition();
        if (definition == null)
            return null;

        final ASFileScope fileScope = definition.getFileScope();
        if (fileScope == null)
            return null;

        return fileScope.getOffsetLookup();
    }

    @Override
    public int getStart()
    {
        OffsetLookup offsetLookup = getOffsetLookup();

        if (offsetLookup == null)
            return absoluteStart;

        return offsetLookup.getLocalOffset(absoluteStart);
    }

    @Override
    public int getEnd()
    {
        OffsetLookup offsetLookup = getOffsetLookup();

        if (offsetLookup == null)
            return absoluteEnd;

        return offsetLookup.getLocalOffset(absoluteEnd);
    }

    @Override
    public int getLine()
    {
        return line;
    }

    @Override
    public int getColumn()
    {
        return column;
    }

    @Override
    public int getEndLine()
    {
        return line;
    }

    @Override
    public int getEndColumn()
    {
        return column;
    }

    @Override
    public int getAbsoluteStart()
    {
        return absoluteStart;
    }

    @Override
    public int getAbsoluteEnd()
    {
        return absoluteEnd;
    }

    @Override
    public IDefinition getDecoratedDefinition()
    {
        return decoratedDefinition;
    }

    @Override
    public String getValue()
    {
        return attributes.length == 1 && !attributes[0].hasKey() ?
                attributes[0].getValue() :
                null;
    }

    @Override
    public IMetaTagNode getTagNode()
    {
        // If this definition didn't come from source, return null.
        if (nodeRef == NodeReference.noReference)
            return null;

        // Get the scope for the definition this metadata is attached to.
        ASScope containingScope = (ASScope)getDecoratedDefinition().getContainingScope();
        if (containingScope == null)
            return null;

        // Get the file scope for that scope.
        ASFileScope fileScope = containingScope.getFileScope();
        if (fileScope == null)
            return null;

        // Get the workspace.
        IWorkspace workspace = fileScope.getWorkspace();
        assert workspace != null;

        // Use the stored NodeReference to get the original node.
        IASNode node = nodeRef.getNode(workspace, containingScope);
        if (!(node instanceof IMetaTagNode))
        {
            // CMP-2168: The NodeReference resolver assumes that all definitions
            // have a unique start offset.  This true in every case except when
            // there are metadata definitions decorating a class.  In this case, the
            // start of the metadata and the start of the class are the same, and the
            // resolver will return the ClassNode, not the MetaTagNode.  Catch this
            // case by when we get a ClassNode, walking any metatags on the class
            // looking for an offset that matches this MetaTag offset.
            if (node instanceof ClassNode)
            {
                IMetaTagsNode metaTags = ((ClassNode)node).getMetaTags();
                if (metaTags != null)
                {
                    for (IMetaTagNode metaTagNode : metaTags.getAllTags())
                    {
                        if (metaTagNode.getAbsoluteStart() == getAbsoluteStart())
                            return metaTagNode;
                    }
                }
            }

            return null;
        }

        return (IMetaTagNode)node;
    }

    /**
     * Updates the location information of this tag.
     */
    public void setLocation(IFileSpecification containingFileSpec, int absoluteStart, int absoluteEnd, int line, int column)
    {
        this.nodeRef = new NodeReference(containingFileSpec, absoluteStart);
        this.sourcePath = containingFileSpec.getPath();
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
        this.line = line;
        this.column = column;
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        sb.append(tagName);

        IMetaTagAttribute[] attrs = getAllAttributes();
        if (attrs != null && attrs.length > 0)
        {
            sb.append('(');

            int i = 0;
            for (IMetaTagAttribute attr : getAllAttributes())
            {
                if (i != 0)
                {
                    sb.append(',');
                    sb.append(' ');
                }

                String key = attr.getKey();
                String value = attr.getValue();

                sb.append(key);
                sb.append('=');
                sb.append('"');
                sb.append(value);
                sb.append('"');

                i++;
            }
            sb.append(')');
        }

        sb.append(']');

        return sb.toString();
    }
}
