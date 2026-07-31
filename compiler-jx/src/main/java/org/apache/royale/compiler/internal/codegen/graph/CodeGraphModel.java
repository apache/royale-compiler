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

package org.apache.royale.compiler.internal.codegen.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodeGraphModel
{
    public static final String SCHEMA_VERSION = "1.0";

    private final String target;
    private final String module;
    private final List<CodeGraphSymbol> symbols = new ArrayList<CodeGraphSymbol>();
    private final List<CodeGraphSymbol> externalSymbols = new ArrayList<CodeGraphSymbol>();

    public CodeGraphModel(String target, String module)
    {
        this.target = target;
        this.module = module;
    }

    public String getTarget()
    {
        return target;
    }

    public String getModule()
    {
        return module;
    }

    public void addSymbol(CodeGraphSymbol symbol)
    {
        symbols.add(symbol);
    }

    public List<CodeGraphSymbol> getSymbols()
    {
        return Collections.unmodifiableList(symbols);
    }

    public void addExternalSymbol(CodeGraphSymbol symbol)
    {
        externalSymbols.add(symbol);
    }

    public List<CodeGraphSymbol> getExternalSymbols()
    {
        return Collections.unmodifiableList(externalSymbols);
    }
}