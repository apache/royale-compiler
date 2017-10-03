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

package org.apache.royale.compiler.internal.units;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.InvalidABCByteCodeProblem;
import org.apache.royale.compiler.problems.NoScopesInABCCompilationUnitProblem;
import org.apache.royale.compiler.filespecs.IBinaryFileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.abc.ABCScopeBuilder;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.scopes.ASFileScopeProvider;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.ABCFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;

/**
 * This class represents a compilation unit for an ABC file.
 */
public class ABCCompilationUnit extends CompilationUnitBase
{
    /**
     * Create a compilation unit from an ABC file.
     * 
     * @param project compiler project
     * @param path ABC file path
     */
    public ABCCompilationUnit(CompilerProject project, String path)
    {
        super(project, path, DefinitionPriority.BasePriority.LIBRARY_PATH, false);
    }
    
    private static final String SUB_SYSTEM = "ABCCompilationUnit"; // Used for error reporting

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.ABC_UNIT;
    }

    public Operation[] notifyDependencyOperationResultsInvalid(ICompilationUnit dependency,
                                                               Operation[] invalidatedOperations)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SYNTAX_TREE);
        try
        {
            List<ICompilerProblem> noProblems = Collections.emptyList();
            return new SyntaxTreeRequestResult(getRootFileSpecification().getLastModified(), noProblems);
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
    }

    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);
        
        getProject().clearScopeCacheForCompilationUnit(this);

        // we should always have a binary file spec when dealing with ABC compilation units
        final Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        byte[] abcData = getABCBytes(problems);

        List<IASScope> scopeList = null;
        try
        {
            final String path = getAbsoluteFilename();
            final ABCScopeBuilder abcScopeBuilder = new ABCScopeBuilder(
                    this.getProject().getWorkspace(), 
                    abcData, 
                    path,
                    ASFileScopeProvider.getInstance());
            scopeList = abcScopeBuilder.build();
            if (scopeList.isEmpty())
            {
                final NoScopesInABCCompilationUnitProblem problem = new NoScopesInABCCompilationUnitProblem(path);
                problems.add(problem);
            }
        }
        catch (Exception e)
        {
            final InvalidABCByteCodeProblem problem = new InvalidABCByteCodeProblem(getRootFileSpecification().getPath());
            problems.add(problem);
        }

        final ABCFileScopeRequestResult result = new ABCFileScopeRequestResult(problems, scopeList);
        stopProfile(Operation.GET_FILESCOPE);

        return result;
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        startProfile(Operation.GET_ABC_BYTES);
        IABCBytesRequestResult result = new ABCBytesRequestResult();
        stopProfile(Operation.GET_ABC_BYTES);
        return result;
    }

    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SWF_TAGS);
        ISWFTagsRequestResult result = new ISWFTagsRequestResult()
        {
            @Override
            public boolean addToFrame(SWFFrame frame)
            {
                // TODO add ABC compilation unit to SWF
                return true;
            }

            @Override
            public ICompilerProblem[] getProblems()
            {
                return IABCBytesRequestResult.ZEROPROBLEMS;
            }

            @Override
            public String getDoABCTagName()
            {
                return "";
            }
            
            @Override
            public DoABCTag getDoABCTag()
            {
                return null;
            }
        };
        stopProfile(Operation.GET_SWF_TAGS);

        return result;
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        startProfile(Operation.GET_SEMANTIC_PROBLEMS);
        IOutgoingDependenciesRequestResult result = new IOutgoingDependenciesRequestResult()
        {
            @Override
            public ICompilerProblem[] getProblems()
            {
                return IABCBytesRequestResult.ZEROPROBLEMS;
            }
        };
        stopProfile(Operation.GET_SEMANTIC_PROBLEMS);

        return result;
    }

    protected byte[] getABCBytes(Collection<ICompilerProblem> problems)
    {
        IFileSpecification rootSource = getRootFileSpecification();
        byte[] abcData = null;
        if (rootSource instanceof IBinaryFileSpecification)
        {
            IBinaryFileSpecification abcFileSpec = (IBinaryFileSpecification)rootSource;
            InputStream abcStream = null;
            try
            {
                abcStream = abcFileSpec.createInputStream();
                abcData = IOUtils.toByteArray(abcStream);
                assert abcData != null : "No ABC byte code.";
            }
            catch (Exception e)
            {
                final ICompilerProblem problem = new InternalCompilerProblem2(rootSource.getPath(), e, SUB_SYSTEM);
                problems.add(problem);
            }
            finally
            {
                if (abcStream != null)
                {
                    try
                    {
                        abcStream.close();
                    }
                    catch (IOException e)
                    {
                        final ICompilerProblem problem = new InternalCompilerProblem2(rootSource.getPath(), e, SUB_SYSTEM);
                        problems.add(problem);
                    }
                }
            }
        }
        else
        {
            final InvalidABCByteCodeProblem problem = new InvalidABCByteCodeProblem(rootSource.getPath());
            problems.add(problem);
        }

        return abcData;
    }
    
    
}
