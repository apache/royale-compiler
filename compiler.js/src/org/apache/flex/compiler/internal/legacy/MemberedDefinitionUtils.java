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

package org.apache.flex.compiler.internal.legacy;

import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IMemberedDefinition;
import org.apache.flex.compiler.internal.scopes.TypeScope;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;

/**
 * This class contains static methods that used to be instance methods of
 * {@link IMemberedDefinition}. They now take a new first parameter which is the
 * <code>IMemberDefinitionm</code>. The methods are no longer part of
 * <code>IMemberedDefinition</code> because {@link ASDefinitionFilter} has been
 * removed from the compiler.
 */
public class MemberedDefinitionUtils
{
    /**
     * Look up a member by name within an {@link IMemberedDefinition}.
     * 
     * @param memberedDefinition The {@link IMemberedDefinition} whose member is
     * being looked up.
     * @param project Project whose symbol table is used to resolve base types.
     * @param name the name of the member
     * @param filter the {@link ASDefinitionFilter} used to match the member we
     * are looking for
     * @return the specified member or null if the member could not be found
     */
    public static IDefinition getMemberByName(IMemberedDefinition memberedDefinition,
                                              ICompilerProject project, String name,
                                              ASDefinitionFilter filter)
    {
        IASScope scope = memberedDefinition.getContainedScope();
        return ASScopeUtils.findDefinitionByName(scope, project, name, filter);
    }

    /**
     * Look up a member by name within an {@link IMemberedDefinition}
     * 
     * @param memberedDefinition The {@link IMemberedDefinition} whose members
     * are being looked up.
     * @param project Project whose symbol table is used to resolve base types.
     * @param name the name of the member
     * @param filter the {@link ASDefinitionFilter} used to match the member we
     * are looking for
     * @return the specified member or null if the member could not be found
     */
    public static IDefinition[] getAllMembersByName(IMemberedDefinition memberedDefinition,
                                                    ICompilerProject project, String name,
                                                    ASDefinitionFilter filter)
    {
        assert !filter.searchContainingScope();

        List<IDefinition> definitions = new ArrayList<IDefinition>();

        IASScope scope = memberedDefinition.getContainedScope();
        if (memberedDefinition instanceof IClassDefinition)
        {
            if (filter.requiresModifier(ASModifier.STATIC))
                scope = ((TypeScope)scope).getStaticScope();
            else if (filter.excludesModifier(ASModifier.STATIC))
                scope = ((TypeScope)scope).getInstanceScope();
        }

        ASScopeUtils.findAllDefinitionsByName(scope, project, name, filter, definitions);
        return definitions.toArray(new IDefinition[0]);
    }

    /**
     * Get an array containing all of the members contained within an
     * {@link IMemberedDefinition}. If the hierarchy of this type allows for
     * overriding member definitions, then this list could contain shadowed
     * member signatures, since this returns all members from the hierarchy of
     * this type
     * 
     * @param memberedDefinition The {@link IMemberedDefinition} whose members
     * are being looked up.
     * @param project Project whose symbol table is used to resolve base types.
     * @param filter ASDefinitionFilter describing the members that should be
     * included
     * @return an array containing all of the members matching the
     * {@link ASDefinitionFilter}
     */
    public static IDefinition[] getAllMembers(IMemberedDefinition memberedDefinition,
                                              ICompilerProject project,
                                              ASDefinitionFilter filter)
    {
        assert !filter.searchContainingScope();

        List<IDefinition> definitions = new ArrayList<IDefinition>();

        IASScope scope = memberedDefinition.getContainedScope();
        if (memberedDefinition instanceof IClassDefinition)
        {
            if (filter.requiresModifier(ASModifier.STATIC))
                scope = ((TypeScope)scope).getStaticScope();
            else if (filter.excludesModifier(ASModifier.STATIC))
                scope = ((TypeScope)scope).getInstanceScope();
        }

        ASScopeUtils.findAllDefinitions(scope, project, filter, definitions);
        return definitions.toArray(new IDefinition[0]);
    }
}
