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

package org.apache.royale.compiler.internal.common;

public class JSModuleRequireDescription implements Comparable<JSModuleRequireDescription>
{
	public JSModuleRequireDescription(String moduleName, String qname)
	{
		this.moduleName = moduleName;
		this.qname = qname;
	}

	public String moduleName;
	public String qname;

	public int compareTo(JSModuleRequireDescription o)
	{
		return moduleName.compareTo(o.moduleName);
	}
}