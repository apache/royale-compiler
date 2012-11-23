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

package org.apache.flex.compiler.internal.as.codegen;

import java.util.HashMap;
import java.util.Map;

import org.apache.flex.compiler.constants.IMetaAttributeConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter;
import org.apache.flex.compiler.internal.legacy.MemberedDefinitionUtils;
import org.apache.flex.compiler.projects.ICompilerProject;

/**
 * Utilities for Flex-oriented special cases in FalconJS compilation. Example:
 * handling the [SkinPart] metatag
 */
public class JSFlexUtils
{
    /**
     * Generates a JavaScript object literal (JSON string) representing the
     * value that should be returned by the generated get_skinParts() method.
     * The JSON object is a map from skin part id to a boolean indicating
     * whether the part is required.
     */
    public static String generateGetSkinPartsJSON(IClassDefinition classDef, ICompilerProject project)
    {
        // Gather set of skin parts
        Map<String, JSFlexUtils.SkinPartInfo> skinPartsInfo = JSFlexUtils.getSkinParts(classDef, project);

        // Build the JSON string
        StringBuilder partsObj = new StringBuilder();
        partsObj.append("{ ");

        // for each skin part...
        boolean comma = false;
        for (JSFlexUtils.SkinPartInfo skinPart : skinPartsInfo.values())
        {
            if (comma)
                partsObj.append(", ");
            else
                comma = true;

            partsObj.append(skinPart.name);
            partsObj.append(": ");
            partsObj.append(skinPart.required);
        }

        partsObj.append(" }");

        return partsObj.toString();
    }

    /**
     * Returns a map specifying the skin parts of the given class. The keys are
     * the skin part ids, and the values are SkinPartInfo objects containing
     * additional information about each part.
     */
    public static Map<String, SkinPartInfo> getSkinParts(IClassDefinition componentClass, ICompilerProject project)
    {
        // NOTE: derived from ModelUtils.getSkinPartsForType() in the Flash Builder codebase (in com.adobe.flexide.mxmlmodel plugin)

        ASDefinitionFilter filter = new ASDefinitionFilter(ASDefinitionFilter.ClassificationValue.VARIABLES,
                                                           ASDefinitionFilter.SearchScopeValue.INHERITED_MEMBERS,
                                                           ASDefinitionFilter.AccessValue.ALL,
                                                           componentClass);
        IDefinition[] members = MemberedDefinitionUtils.getAllMembers(componentClass, project, filter);

        HashMap<String, SkinPartInfo> map = new HashMap<String, SkinPartInfo>();

        // For each variable declared in class...
        for (IDefinition member : members)
        {
            IVariableDefinition variable = (IVariableDefinition)member;

            // Is it tagged [SkinPart] ?
            IMetaTag skinPartTag = variable.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_SKIN_PART);
            if (skinPartTag != null)
            {
                // Gather information about the skin part
                String name = variable.getBaseName();
                ITypeDefinition partType = variable.resolveType(project);
                String asClass = partType != null ? partType.getQualifiedName() : null;
                boolean required = parseRequired(skinPartTag);

                SkinPartInfo info = new SkinPartInfo(name, asClass, required);

                // Add to result
                // Note: We get duplicates for [Bindable] variables because they generate getter/setter pairs
                map.put(name, info);
            }
        }

        return map;
    }

    private static boolean parseRequired(IMetaTag metaTag)
    {
        String requiredStr = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_SKIN_PART_REQUIRED);
        return requiredStr != null && requiredStr.length() > 0 &&
               requiredStr.equals(IMetaAttributeConstants.VALUE_SKIN_PART_REQUIRED_TRUE);
    }

    /**
     * Value object representing information about a skin part. Immutable. NOTE:
     * derived from ModelUtils.SkinPartInfo in the Flash Builder codebase
     */
    public static class SkinPartInfo
    {
        /** Part name - this should be set as the ID in the skin. Not null. */
        public final String name;

        /**
         * Type of the part, or null if the type could not be resolved on the
         * classpath.
         */
        public final String asClass;

        /** Does the metadata indicate that the part is required? */
        public final boolean required;

        public SkinPartInfo(String name, String asClass, boolean required)
        {
            assert name != null;
            assert asClass != null;
            this.name = name;
            this.asClass = asClass;
            this.required = required;
        }
    }
}
