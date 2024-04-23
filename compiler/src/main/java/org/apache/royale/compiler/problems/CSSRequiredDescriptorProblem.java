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

 package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * Problem generated when a CSS at-rule lacks a required descriptor.
 */
public final class CSSRequiredDescriptorProblem extends CSSProblem
{
	public static final String DESCRIPTION =
		"The '${atRule}' rule requires a '${descriptorName}' descriptor.";
	
	public CSSRequiredDescriptorProblem(ISourceLocation location, String atRule, String descriptorName)
	{
		super(location);
		this.atRule = atRule;
		this.descriptorName = descriptorName;
	}
	
	public final String atRule;
	public final String descriptorName;
}
 