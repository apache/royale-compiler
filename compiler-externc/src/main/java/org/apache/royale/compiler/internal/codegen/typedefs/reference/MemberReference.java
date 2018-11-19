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

package org.apache.royale.compiler.internal.codegen.typedefs.reference;

import org.apache.royale.compiler.clients.ExternCConfiguration.ExcludedMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.ReadOnlyMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.TrueConstant;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

public abstract class MemberReference extends BaseReference
{

    private ClassReference classReference;

    public ClassReference getClassReference()
    {
        return classReference;
    }

    public MemberReference(ReferenceModel model, ClassReference classReference,
            Node node, String name, JSDocInfo comment)
    {
        super(model, node, name, comment);
        this.classReference = classReference;
    }

    @Override
    public ExcludedMember isExcluded()
    {
        return getClassReference().getModel().isExcludedMember(
                getClassReference(), this);
    }
    
    @Override
    public ReadOnlyMember isReadOnly()
    {
    	return getClassReference().getModel().isReadOnlyMember(
    			getClassReference(), this);
    }

    @Override
    public TrueConstant isTrueConstant()
    {
    	return getClassReference().getModel().isTrueConstant(
    			getClassReference(), this);
    }

}
