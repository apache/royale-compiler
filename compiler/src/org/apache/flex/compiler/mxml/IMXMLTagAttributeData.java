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

package org.apache.flex.compiler.mxml;

import java.util.Collection;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.internal.parsing.ISourceFragment;
import org.apache.flex.compiler.problems.ICompilerProblem;

/**
 * Encapsulation of a tag attribute in MXML.
 */
public interface IMXMLTagAttributeData extends ISourceLocation
{
    MXMLDialect getMXMLDialect();
    
    MXMLTagData getParent();
    
    String getPrefix();

    String getURI();
    
    String getName();
    
    String getShortName();
    
    String getStateName();
    
    boolean isSpecialAttribute(String name);
    
    boolean hasValue();
    
    MXMLTagAttributeValue[] getValues();

    String getRawValue();
    
    ISourceFragment[] getValueFragments(Collection<ICompilerProblem> problems);
    
    int getValueStart();
    
    int getValueEnd();
    
    int getValueLine();
    
    int getValueColumn();
    
    ISourceLocation getValueLocation();
}
