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

package org.apache.flex.compiler.internal.units.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.flex.compiler.common.IDefinitionPriority;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.scopes.ASFileScope;
import org.apache.flex.compiler.internal.units.ASCompilationUnit;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MultipleExternallyVisibleDefinitionsProblem;
import org.apache.flex.compiler.problems.NoMainDefinitionProblem;
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
            problems.add(new NoMainDefinitionProblem(fileName, expectedQName));
        
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
