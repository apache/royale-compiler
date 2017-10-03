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

import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;

/**
 * An IABCVisitor that ignores its input as far as possible.
 */
public class NilABCVisitor implements IABCVisitor
{
    @Override
    public void visit(int majorVersion, int minorVersion)
    {
    }

    @Override
    public void visitEnd()
    {
    }

    @Override
    public IScriptVisitor visitScript()
    {
        return NilVisitors.NIL_SCRIPT_VISITOR;
    }

    @Override
    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        return NilVisitors.NIL_CLASS_VISITOR;
    }

    @Override
    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        return NilVisitors.NIL_METHOD_VISITOR;
    }

    @Override
    public void visitPooledInt(Integer i)
    {
    }

    @Override
    public void visitPooledUInt(Long l)
    {
    }

    @Override
    public void visitPooledDouble(Double d)
    {
    }

    @Override
    public void visitPooledString(String s)
    {
    }

    @Override
    public void visitPooledNamespace(Namespace ns)
    {
    }

    @Override
    public void visitPooledNsSet(Nsset nss)
    {
    }

    @Override
    public void visitPooledName(Name n)
    {
    }

    @Override
    public void visitPooledMetadata(Metadata md)
    {
    }
}
