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

package org.apache.royale.compiler.internal.abc;

import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTagAttribute;

/**
 * Collect all the metadata and attributes in this trait.
 */
class CollectMetadataTraitVisitor implements ITraitVisitor
{
    CollectMetadataTraitVisitor(final DefinitionBase definition)
    {
        assert definition != null;
        this.definition = definition;
    }

    private final DefinitionBase definition;
    private IMetaTag[] metaTags;

    /**
     * Visiting all the metadata associated with this trait. The {@code count}
     * is used to determine size of MetaTag array.
     */
    @Override
    public IMetadataVisitor visitMetadata(final int count)
    {
        assert metaTags == null : "Can not re-use the collector.";
        metaTags = new IMetaTag[count];
        return new IMetadataVisitor()
        {
            int currentIndex = 0;

            @Override
            public void visit(Metadata metadata)
            {
                metaTags[currentIndex++] = createMetaTag(definition, metadata);
            }
        };
    }

    @Override
    public void visitStart()
    {
    }

    private IMetaTag findMetaTag(String name)
    {
        for (IMetaTag tag : metaTags)
        {
            if (tag.getTagName().equals(name))
                return tag;
        }
        return null;
    }
    
    private static void setNameLocation(IDefinition def, IMetaTag gotoDefTag)
    {
        String posString = gotoDefTag.getAttributeValue(IMetaAttributeConstants.NAME_GOTODEFINITIONHELP_POS);
        int nameStart = Integer.parseInt(posString);

        DefinitionBase constructorDef = (DefinitionBase)def;
        int nameEnd = nameStart + def.getBaseName().length();
        constructorDef.setNameLocation(nameStart, nameEnd);
    }
    
    /**
     * Store the meta tags in the definition object.
     */
    @Override
    public void visitEnd()
    {
        if (metaTags == null)
            metaTags = new IMetaTag[0];
        if (definition instanceof ClassDefinition)
        {
            ClassDefinition classDef = (ClassDefinition)definition;
            IFunctionDefinition constructor = classDef.getConstructor();
            if (constructor != null)
            {
                IMetaTag ctorGotoDefHelpTag = findMetaTag(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITION_CTOR_HELP);
                if (ctorGotoDefHelpTag != null)
                    setNameLocation(constructor, ctorGotoDefHelpTag);
            }
        }
        IMetaTag gotoDefHelpTag = findMetaTag(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITIONHELP);
        if (gotoDefHelpTag != null)
            setNameLocation(definition, gotoDefHelpTag);
        definition.setMetaTags(metaTags);
    }

    @Override
    public void visitAttribute(String attr_name, Object attr_value)
    {
        if (definition instanceof FunctionDefinition)
        {
            if (attr_name.equals(Trait.TRAIT_FINAL) && (Boolean)attr_value)
                definition.setFinal();
            else if (attr_name.equals(Trait.TRAIT_OVERRIDE) && (Boolean)attr_value)
                definition.setOverride();
        }
    }

    /**
     * Create a {@link IMetaTag} object from {@link Metadata}.
     * 
     * @param metadata metadata object
     * @return IMetaTag object
     */
    private static IMetaTag createMetaTag(final IDefinition definition, final Metadata metadata)
    {
        final String[] keys = metadata.getKeys();
        final String[] values = metadata.getValues();
        
        assert keys.length == values.length;
        final int attributesCount = keys.length;
        final IMetaTagAttribute[] attributes = new IMetaTagAttribute[attributesCount];
        for (int i = 0; i < attributesCount; i++)
            attributes[i] = new MetaTagAttribute(keys[i], values[i]);
        
        return new MetaTag(definition, metadata.getName(), attributes);
    }
}
