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


/**
 * A set of singleton visitors that ignore their input
 * as far as possible.
 */
public class NilVisitors
{
    /**
     * Nil {@code IScriptVisitor}.
     */
    public static final IScriptVisitor NIL_SCRIPT_VISITOR = new NilScriptVisitor();

    /**
     * Nil {@code IClassVisitor}.
     */
    public static final IClassVisitor NIL_CLASS_VISITOR = new NilClassVisitor();

    /**
     * Nil {@code IMethodBodyVisitor}.
     */
    public static final IMethodBodyVisitor NIL_METHOD_BODY_VISITOR = new NilMethodBodyVisitor();

    /**
     * Nil {@code IMethodVisitor}.
     */
    public static final IMethodVisitor NIL_METHOD_VISITOR = new NilMethodVisitor();

    /**
     * Nil {@code IABCVisitor}.
     */
    public static final IABCVisitor NIL_ABC_VISITOR = new NilABCVisitor();

    /**
     * Nil {@code ITraitsVisitor};
     */
    public static final ITraitsVisitor NIL_TRAITS_VISITOR = new NilTraitsVisitor();

    /**
     * Nil {@code ITraitVisitor};
     */
    public static final ITraitVisitor NIL_TRAIT_VISITOR = new NilTraitVisitor();

    /**
     * Nil {@code IMetadataVisitor};
     */
    public static final IMetadataVisitor NIL_METADATA_VISITOR = new NilMetadataVisitor();

    /**
     * Nil {@code IDiagnosticsVisitor}
     */
    public static final IDiagnosticsVisitor NIL_DIAGNOSTICS_VISITOR = new NilDiagnosticsVisitor();

}
