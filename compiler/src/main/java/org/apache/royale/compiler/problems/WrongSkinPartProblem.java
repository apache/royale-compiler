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
import org.apache.royale.compiler.definitions.ITypeDefinition;

/**
 * Created when type type of the skin part is not compatible with the host
 * component skin type
 */
public final class WrongSkinPartProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "The skin part type '${skinPartTypeName}' must be assignable to '${hostSkinPart}'.";

    public static final int errorCode = 1301;
    public WrongSkinPartProblem(ISourceLocation site, ITypeDefinition skinPart, ITypeDefinition hostSkinPart)
    {
        super(site);
        this.skinPartTypeName = skinPart.getQualifiedName();
        this.hostSkinPart = hostSkinPart.getQualifiedName();
    }

    public final String skinPartTypeName;
    public final String hostSkinPart;
}
