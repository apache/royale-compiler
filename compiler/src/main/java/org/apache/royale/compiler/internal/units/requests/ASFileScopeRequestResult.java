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

package org.apache.royale.compiler.internal.units.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.royale.compiler.common.IDefinitionPriority;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MultipleExternallyVisibleDefinitionsProblem;
import org.apache.royale.compiler.problems.NoMainDefinitionProblem;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IScopedNode;

import com.google.common.collect.Iterables;

/**
 * {@link FileScopeRequestResultBase} for {@link ASCompilationUnit}.
 */
public class ASFileScopeRequestResult extends FileScopeRequestResultBase
{
    /**
     * {@link ASCompilationUnit} only allow one public visible definition.
     */
    public ASFileScopeRequestResult(Collection<IDefinition> definitionPromises,
                                    IDefinitionPriority definitionPriority,
                                    Collection<ICompilerProblem> problems,
                                    ASFileScope fileScope,
                                    IFileSpecification fileSpec)
    {
        super(problems, Collections.singleton(fileScope));
        fileName = fileScope.getContainingPath();
        addProblems(checkExternallyVisibleDefinitions(fileName, definitionPromises));
    }
    
    private final String fileName;
    
    @Override
    public Collection<ICompilerProblem> checkExternallyVisibleDefinitions(String dottedQName)
    {
        return checkExternallyVisibleDefinitions(this.fileName, dottedQName);
    }
    
    private Collection<ICompilerProblem> checkExternallyVisibleDefinitions(String fileName, String expectedQName)
    {
        boolean foundMainDefinition = false;
        ArrayList<ICompilerProblem> problems =
            new ArrayList<ICompilerProblem>(definitions.size());
        
        for (final IDefinition def : definitions)
        {
            final String defQName = def.getQualifiedName();
            if ((!foundMainDefinition) && (expectedQName.equals(defQName)))       
                foundMainDefinition = true;
            else
                problems.add(new MultipleExternallyVisibleDefinitionsProblem(def, def.getQualifiedName()));
        }
        
        if (!foundMainDefinition)
        {
            boolean ignoreMissingMainDefinition = false;
            for (final ASFileScope scope : this.getFileScopes())
            {
                for (IDefinitionSet defSet : scope.getAllLocalDefinitionSets())
                {
                    int n = defSet.getSize();
                    for (int i = 0; i < n; i++)
                    {
                        IDefinition def = defSet.getDefinition(i);
                        if (def instanceof IPackageDefinition)
                        {
                            IPackageDefinition packageDef = (IPackageDefinition)def;
                            ASScope packageScope = (ASScope)packageDef.getContainedScope();
                            IScopedNode scopeNode = packageScope.getScopeNode();
                            if (scopeNode == null)
                            {
                                //FLEX-35226: it's possible for the scope node
                                //to be null in an empty MXML class -JT
                                continue;
                            }
                            ContainerNode packageNode = (ContainerNode)scopeNode;
                            if (packageNode.getRemovedConditionalCompileNode())
                                ignoreMissingMainDefinition = true;
                        }
                    }
                }
            }
            if (!ignoreMissingMainDefinition)
                problems.add(new NoMainDefinitionProblem(fileName, expectedQName));
        }
        
        return problems;
    }
    
    private Collection<ICompilerProblem> checkExternallyVisibleDefinitions(String fileName, Collection<IDefinition> definitionPromises)
    {
        if (definitionPromises.isEmpty())
            return Collections.emptyList();
        assert definitionPromises.size() == 1;
        final IDefinition mainDefinitionPromise = Iterables.getOnlyElement(definitionPromises);
        final String mainDefinitionPromiseQName = mainDefinitionPromise.getQualifiedName();
        return checkExternallyVisibleDefinitions(fileName, mainDefinitionPromiseQName);
    }

    /**
     * The only public definition in the file scope should match the given
     * QName.
     */
    @Override
    public IDefinition getMainDefinition(String qname)
    {
        assert qname != null : "Excpect QName.";
        if (definitions.isEmpty())
            return null;
        
        final IDefinition definition = definitions.iterator().next();
        if (qname.equals(definition.getQualifiedName()))
            return definition;
        else
            return null;
    }

}
