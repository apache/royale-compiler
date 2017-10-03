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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.semantics.*;

/**
 * A DelegatingABCVisitor wraps another IABCVisitor and delegates actions to it.
 * The DelegatingABCVisitor can be used as a base class for an application that
 * needs to supplement the basic IABCVisitor's behavior, often by returning a
 * visitor that, in turn, implements application-specific behavior.
 */
public class DelegatingABCVisitor implements IABCVisitor
{
    /**
     * Construct a DelegatingABCVisitor.
     * 
     * @param delegate - the IABCVisitor this visitor delegates to.
     */
    public DelegatingABCVisitor(IABCVisitor delegate)
    {
        this.delegate = delegate;
    }

    /**
     * The backing (delegate) IABCVisitor.
     */
    private IABCVisitor delegate;

    @Override
    public void visit(int majorVersion, int minorVersion)
    {
        delegate.visit(majorVersion, minorVersion);
    }

    @Override
    public void visitEnd()
    {
        delegate.visitEnd();
    }

    @Override
    public IScriptVisitor visitScript()
    {
        return delegate.visitScript();
    }

    @Override
    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        return delegate.visitClass(iinfo, cinfo);
    }

    @Override
    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        return delegate.visitMethod(minfo);
    }

    @Override
    public void visitPooledInt(Integer i)
    {
        delegate.visitPooledInt(i);
    }

    @Override
    public void visitPooledUInt(Long l)
    {
        delegate.visitPooledUInt(l);
    }

    @Override
    public void visitPooledDouble(Double d)
    {
        delegate.visitPooledDouble(d);
    }

    @Override
    public void visitPooledString(String s)
    {
        delegate.visitPooledString(s);
    }

    @Override
    public void visitPooledNamespace(Namespace ns)
    {
        delegate.visitPooledNamespace(ns);
    }

    @Override
    public void visitPooledNsSet(Nsset nss)
    {
        delegate.visitPooledNsSet(nss);
    }

    @Override
    public void visitPooledName(Name n)
    {
        delegate.visitPooledName(n);
    }

    @Override
    public void visitPooledMetadata(Metadata md)
    {
        delegate.visitPooledMetadata(md);
    }

}
