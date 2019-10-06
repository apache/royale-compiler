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

package org.apache.royale.compiler.internal.definitions;

import java.util.List;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;

import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;

/**
 * IDefinition marker to represent ambiguous results. TODO: Can modify to keep
 * track of what the ambiguous results were. This would be an improvement over
 * TODO: ASC, which only reported that a reference was ambiguous, but didn't
 * tell you which symbols it matched.
 */
public final class AmbiguousDefinition extends DefinitionBase implements IDefinition
{

    private static AmbiguousDefinition def = new AmbiguousDefinition();

    /**
     * Is the definition passed in an AmbiguousDefinition.
     * 
     * @param d the definition to check
     * @return true if d is an AmbiguousDefinition
     */
    public static boolean isAmbiguous(IDefinition d)
    {
        return d == def;
    }

    /**
     * Get the AmbiguousDefinition instance - currently there is only 1
     * AmbiguousDefinition instance, but we may modify that if each
     * AmbiguousDefinition needs to keep track of it's ambiguous results
     */
    public static AmbiguousDefinition get()
    {
        return def;
    }

    /**
     * Constructor.
     */
    private AmbiguousDefinition()
    {
        super("");
    }

    /**
     * Helper method to resolve apparently ambiguous results - there are 2 cases
     * where we what looks like an ambiguity is not - getter/setter pairs, and
     * legally re-declared local variables If we have a getter and a setter, we
     * will end up with 2 resulting definitions, but we don't want to report
     * them as ambiguous if they are a getter & setter for the same property. in
     * this case, this method arbitrarily returns the first definition passed in
     * - this should be ok because getter and setter definitions have methods to
     * access the other one (resolveCorrespondingAccessor), so callers can use
     * that to get at the one they want. If we have the same variable declared
     * multiple times, then we don't want to report access to that variable as
     * ambiguous. A variable can be legally re-declared when: 1. It is declared
     * at global scope (outside a package), or it is a local var in a function.
     * 2. All of the declarations of that variable are declared with the same
     * type, or '*'. One of the VariableDefinitions with the specific type will
     * be returned so that the correct type is used by the compiler e.g. if a
     * Variable is declared twice, once as * and once as String, the result of
     * this method will be the VariableDefinition typed as String. If all the
     * VariableDefinitions are declared with the same type, then one will be
     * arbitrarily returned.
     * 
     * @param project The Project to use to resolve things
     * @param defs an Array of definitions to compare
     * @param favorTypes 
     * @return the definition to use as the result of the lookup, if the
     * ambiguity was successfully resolved, otherwise null
     */
    public static IDefinition resolveAmbiguities(ICompilerProject project, List<IDefinition> defs, boolean favorTypes)
    {
        IDefinition resolvedDef = null;

        assert defs.size() > 1 : "This method should only be called when there is an ambiguity to resolve!";

        // If we have exactly two definition to choose from, the
        // two definitions might be a getter/setter pair.
        if (SemanticUtils.isGetterSetterPair(defs, project))
        {
            resolvedDef = defs.get(0);
        }

        // this is used to favor type definitions over function definitions
        // when resolving the type of a variable (which can't be a function)
        if (resolvedDef == null && defs.size() == 2)
        {
        	IDefinition def0 = defs.get(0);
        	IDefinition def1 = defs.get(1);
        	if (def0 instanceof FunctionDefinition &&
        			(def1 instanceof ClassDefinition ||
        			(def1 instanceof InterfaceDefinition)))
        	{
        		resolvedDef = favorTypes ? def1 : def0;
        	}
        	else if (def1 instanceof FunctionDefinition &&
        			(def0 instanceof ClassDefinition ||
        			(def0 instanceof InterfaceDefinition)))
        	{
        		resolvedDef = favorTypes ? def0 : def1;
        	}
        }
        
        if (resolvedDef == null)
        {
            // check for redeclared variables and functions

            resolvedDef = defs.get(0);
            assert resolvedDef.isInProject(project);
            
            List<IDefinition> defsAfterFirst = defs.subList(1, defs.size());
            // The result of this loop will either be the VariableDefinition to return if all the ambiguities were
            // resolved, or null if the ambiguities could not be resolved.
            for (IDefinition d : defsAfterFirst)
            {
                assert d.isInProject(project);
                if ( resolvedDef instanceof VariableDefinition )
                {
                    if( d instanceof VariableDefinition )
                        resolvedDef = resolveAmbiguousVariableDefinitions(project, resolvedDef, d);
                    else if( d instanceof FunctionDefinition )
                        resolvedDef = resolveAmbiguousFunctionVariableDefinitions(project, resolvedDef, d);
                    else
                        resolvedDef = null;
                }
                else if( resolvedDef instanceof FunctionDefinition )
                {
                    if( d instanceof FunctionDefinition )
                        resolvedDef = resolveAmbiguousFunctionDefinitions(project, resolvedDef, d);
                    else if( d instanceof VariableDefinition )
                        resolvedDef = resolveAmbiguousFunctionVariableDefinitions(project, resolvedDef, d);
                    else
                        resolvedDef = null;
                }
                else
                {
                    resolvedDef = null; // At least one definition was not a variable definition, so just bail
                }

                if (resolvedDef == null)
                    break;
            }
        }
        return resolvedDef;
    }

    /**
     * Determine if two definitions have the same name, namespace qualifier, and
     * containing scope.
     * 
     * @param project {@link ICompilerProject} used to resolve namespace
     * references.
     * @param d1 first {@link IDefinition} to compare.
     * @param d2 second {@link IDefinition} to compare.
     * @return true if both of the specified {@link IDefinition}'s have the same
     * name, namespace qualifier, and containing scope, false otherwise.
     */
    private static boolean namesAndContainingScopeMatch(ICompilerProject project, IDefinition d1, IDefinition d2)
    {
        if (!d1.getBaseName().equals(d2.getBaseName()))
            return false;

        if (!d1.resolveNamespace(project).equals(d2.resolveNamespace(project)))
            return false;

        if (d1.getContainingScope() != d2.getContainingScope())
            return false;
        return true;
    }

    /**
     * Helper method to try and resolve 2 potentially ambiguous
     * VariableDefinitions. If the definitions are not actually ambiguous, then
     * the Definition with the more specific type will be returned. For more
     * detail see the comments for resolveAmbiguities above
     */
    private static IDefinition resolveAmbiguousVariableDefinitions(ICompilerProject project, IDefinition v1, IDefinition v2)
    {
        assert (v1 instanceof VariableDefinition) && (v2 instanceof VariableDefinition);

        // Make sure they have the same name, and namespace, otherwise they can't possible be a re-decl
        if (!namesAndContainingScopeMatch(project, v1, v2))
            return null;

        //  const definitions always conflict.
        if (v1 instanceof ConstantDefinition || v2 instanceof ConstantDefinition)
            return null;

        IASScope containingScope = v1.getContainingScope();
        IScopedDefinition containingDef = v1.getContainingScope().getDefinition();
        if (containingDef instanceof FunctionDefinition || containingScope instanceof ASFileScope)
        {
            // Only global (outside a package) or function locals can be redeclared.

            VariableDefinition var1 = (VariableDefinition)v1;
            VariableDefinition var2 = (VariableDefinition)v2;

            ITypeDefinition thisType = var1.resolveType(project);
            ITypeDefinition thatType = var2.resolveType(project);

            // If the types match, doesn't matter which one we return
            if (thisType == thatType)
            {
                return v1;
            }
            else
            {
                // If the types don't match, the re-decl is only allowed if one of them is '*' and the other
                // one is a specific type.  In this case, return the def with the more specific type, as that
                // type will be the type of the var throughout the function
                IDefinition anyType = project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);
                if (thisType == anyType)
                    return v2;
                if (thatType == anyType)
                    return v1;
            }
        }

        return null;
    }

    /**
     * Helper method to try and resolve 2 potentially ambiguous
     * FunctionDefinitions. If the definitions are not actually ambiguous, then
     * the Definition with the more specific type will be returned. For more
     * detail see the comments for resolveAmbiguities above
     */
    private static IDefinition resolveAmbiguousFunctionDefinitions(ICompilerProject project, IDefinition f1, IDefinition f2)
    {
        assert (f1 instanceof FunctionDefinition) && (f2 instanceof FunctionDefinition);

        // Make sure they have the same name, and namespace, otherwise they can't possible be a re-decl
        if (!namesAndContainingScopeMatch(project, f1, f2))
            return null;

        ASScope containingScope = (ASScope)f1.getContainingScope();
        if ((containingScope instanceof ASFileScope) || (containingScope.getContainingDefinition() instanceof FunctionDefinition))
        {
            // All function declarations match, because functions
            // declared outside of a package are really variables of
            // type * that are initialized to function closures.

            int thisStartAbsoluteOffset = f1.getAbsoluteStart();
            int thatStartAbsoluteOffset = f2.getAbsoluteStart();
            // We'll pretend last definition wins, because that is more like JavaScript.
            if (thisStartAbsoluteOffset >= thatStartAbsoluteOffset)
                return f1;
            else
                return f2;
        }

        return null;
    }

    /**
     * Helper method to try and resolve 2 potentially ambiguous Function and Variable definitions.  If the definitions
     * are not actually ambiguous, the Function definition will be returned, as it will always have the most specific type ('Function').
     *
     * This method must be called with 1 variable definition, and 1 function definition - the order is not important.
     *
     * @param project   The project to resolve things in
     * @param f1        The first definition
     * @param f2        The second definition
     * @return          The Function definition if the ambiguity can be resolve, otherwise null
     */
    private static IDefinition resolveAmbiguousFunctionVariableDefinitions(ICompilerProject project, IDefinition f1, IDefinition f2)
    {
        VariableDefinition varDef = null;
        FunctionDefinition funcDef = null;
        if(f1 instanceof VariableDefinition )
        {
            assert f2 instanceof FunctionDefinition;
            varDef = (VariableDefinition)f1;
            funcDef = (FunctionDefinition)f2;
        }
        else
        {
            assert f1 instanceof FunctionDefinition && f2 instanceof VariableDefinition;
            varDef = (VariableDefinition)f2;
            funcDef = (FunctionDefinition)f1;
        }

        // Make sure they have the same name, and namespace, otherwise they can't possible be a re-decl
        if (!namesAndContainingScopeMatch(project, varDef, funcDef))
            return null;

        //  const definitions always conflict.
        //  getters/setters always conlflict with vars
        if (varDef instanceof ConstantDefinition || funcDef instanceof AccessorDefinition)
            return null;

        IASScope containingScope = varDef.getContainingScope();
        IScopedDefinition containingDef = varDef.getContainingScope().getDefinition();
        if (containingDef instanceof FunctionDefinition || containingScope instanceof ASFileScope)
        {
            // Only global (outside a package) or function locals can be redeclared.

            ITypeDefinition varType = varDef.resolveType(project);
            IDefinition funcType = project.getBuiltinType(IASLanguageConstants.BuiltinType.FUNCTION);

            // If the types match, doesn't matter which one we return
            if (varType == funcType)
            {
                return funcDef;
            }
            else
            {
                // If the types don't match, the re-decl is only allowed if the var type is '*' (the Function type is 'Function').
                // In this case, return the def with the more specific type, which will be the function
                IDefinition anyType = project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);
                if (varType == anyType)
                    return funcDef;
            }
        }

        return null;
    }

    @Override
    public boolean isInProject(ICompilerProject project)
    {
        // The ambiguous definition singleton is logically in all projects.
        return true;
    }
}
