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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.IDefinitionPriority;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.IInvisibleCompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;

/**
 * Implementation of {@link IInvisibleCompilationUnit}.
 */
public class InvisibleCompilationUnit implements IInvisibleCompilationUnit
{
    /**
     * Constructor.
     * 
     * @param delegate {@link CompilationUnitBase} that this class will delegate
     * to.
     * @param fileSpecGetter {@link IFileSpecificationGetter} that should be
     * used to open files.
     */
    public InvisibleCompilationUnit(CompilationUnitBase delegate, IFileSpecificationGetter fileSpecGetter)
    {
        assert delegate != null;
        this.delegate = delegate;
        delegate.setFileSpecificationGetter(fileSpecGetter);
        delegate.makeInvisible(this);
    }
    
    private final CompilationUnitBase delegate;

    @Override
    public ICompilerProject getProject()
    {
        assert delegate.isInvisible();
        return delegate.getProject();
    }

    @Override
    public IRequest<ISyntaxTreeRequestResult, ICompilationUnit> getSyntaxTreeRequest()
    {
        assert delegate.isInvisible();
        return delegate.getSyntaxTreeRequest();
    }

    @Override
    public IRequest<IFileScopeRequestResult, ICompilationUnit> getFileScopeRequest()
    {
        assert delegate.isInvisible();
        return delegate.getFileScopeRequest();
    }

    @Override
    public IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit> getOutgoingDependenciesRequest()
    {
        assert delegate.isInvisible();
        return delegate.getOutgoingDependenciesRequest();
    }

    @Override
    public IRequest<IABCBytesRequestResult, ICompilationUnit> getABCBytesRequest()
    {
        assert delegate.isInvisible();
        return delegate.getABCBytesRequest();
    }

    @Override
    public IRequest<ISWFTagsRequestResult, ICompilationUnit> getSWFTagsRequest()
    {
        assert delegate.isInvisible();
        return delegate.getSWFTagsRequest();
    }

    @Override
    public List<String> getShortNames() throws InterruptedException
    {
        assert delegate.isInvisible();
        return delegate.getShortNames();
    }

    @Override
    public List<String> getQualifiedNames() throws InterruptedException
    {
        assert delegate.isInvisible();
        return delegate.getQualifiedNames();
    }

    @Override
    public String getName()
    {
        assert delegate.isInvisible();
        return delegate.getName();
    }

    @Override
    public String getAbsoluteFilename()
    {
        assert delegate.isInvisible();
        return delegate.getAbsoluteFilename();
    }

    @Override
    public List<IDefinition> getDefinitionPromises()
    {
        assert delegate.isInvisible();
        return delegate.getDefinitionPromises();
    }

    @Override
    public UnitType getCompilationUnitType()
    {
        assert delegate.isInvisible();
        return delegate.getCompilationUnitType();
    }

    @Override
    public boolean clean(Map<ICompilerProject, Set<File>> invalidatedSWCFiles, Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate, boolean clearFileScope)
    {
        assert delegate.isInvisible();
        clean();
        return true;
    }

    @Override
    public IDefinitionPriority getDefinitionPriority()
    {
        assert delegate.isInvisible();
        return delegate.getDefinitionPriority();
    }

    @Override
    public void clearProject()
    {
        assert delegate.isInvisible();
        delegate.clearProject();
    }

    @Override
    public synchronized void waitForBuildFinish(Collection<ICompilerProblem> problems, TargetType targetType) throws InterruptedException
    {
        assert delegate.isInvisible();
        delegate.waitForBuildFinish(problems, targetType);
    }

    @Override
    public void startBuildAsync(TargetType targetType)
    {
        assert delegate.isInvisible();
        delegate.startBuildAsync(targetType);
    }

    public boolean isInvisible()
    {
        assert delegate.isInvisible();
        return true;
    }

    @Override
    public synchronized void getCompilerProblems(Collection<ICompilerProblem> problems) throws InterruptedException
    {
        assert problems != null : "A valid collection of ICompilerProblems should be passed" ;
        
        startBuildAsync(TargetType.SWF);
        waitForBuildFinish(problems, TargetType.SWF);
    }

    @Override
    public synchronized void clean()
    {
        delegate.clean(null, null, true);
    }

    @Override
    public void remove()
    {
        ((ASProject)getProject()).removeCompilationUnit(delegate);
    }

    @Override
    public Collection<String> getEmbeddedFilenames()
    {
        assert delegate.isInvisible();
        return delegate.getEmbeddedFilenames();
    }

    /**
     * @return The delegate compilation unit for this {@link InvisibleCompilationUnit}
     */
    public CompilationUnitBase getDelegate()
    {
        return delegate;
    }
}
