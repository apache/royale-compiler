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

package org.apache.royale.compiler.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * List of built-in namespace constants found in the AS3 language
 */
public interface INamespaceConstants
{
	static final String internal_ = IASKeywordConstants.INTERNAL;
	static final String protected_ = IASKeywordConstants.PROTECTED;
	static final String private_ = IASKeywordConstants.PRIVATE;
	static final String public_ = IASKeywordConstants.PUBLIC;
	static final String AS3 = "AS3"; 
	static final String AS3URI = "http://adobe.com/AS3/2006/builtin";
	static final String FLASH10 = "flash10";
    static final String ANY = "*";

	/**
	 * List of builtin namespaces found in AS3
	 */
	static final String[] BUILTINS = new String[]
	{
	    internal_,
	    protected_,
	    private_,
	    AS3,
	    public_
	};
	
	/**
	 * Set of builtin namespaces found in AS3
	 */
	static final Set<String> BUILTINS_SET =
	    Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(BUILTINS)));
}
