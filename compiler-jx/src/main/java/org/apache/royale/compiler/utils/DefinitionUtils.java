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

package org.apache.royale.compiler.utils;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.internal.codegen.js.jx.TryEmitter;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * @author Michael Schmalle
 */
public class DefinitionUtils
{

    public static final String JSROYALE_SUPPRESS_EXPORT = "JSRoyaleSuppressExport";

    /**
     * Utility method for checking if a definition has been marked to be suppressed for export by Closure.
     * The implementation is to check for [JSRoyaleSuppressExport] Metadata.
     * (This metadata is typically added based on the @royalesuppressexport doc-comment directive, by the ASDocDelegate
     * during parsing, instead of being present in the original code)
     * @param def the definition to inspect as suppressed for export
     */
    public static boolean hasExportSuppressed(IDefinition def) {
        return def.hasMetaTagByName(JSROYALE_SUPPRESS_EXPORT);
    }


    public static final boolean isMemberDefinition(IDefinition definition)
    {
        return definition != null
                && (definition.getParent() instanceof IClassDefinition || definition
                        .getParent() instanceof IInterfaceDefinition);
    }


    public static final int deltaFromObject(IClassDefinition definition, ICompilerProject project) {
        int ret = -1;
        if (definition != null) {
            return definition.resolveAncestry(project).length - 1;
        }
        return ret;
    }

    /**
     * Utility method for checking if an assigned definition represents the rewritten part of a
     * multi-catch sequence - currently needed to avoid implicit coercions.
     * This check ultimately relies on the TryEmitter.ROYALE_MULTI_CATCH_ERROR_NAME naming scheme.
     */
    public static final boolean isRewrittenMultiCatchParam(IDefinition definition) {
        return (definition instanceof ParameterDefinition &&
                definition.getContainingScope() instanceof CatchScope &&
                definition.getBaseName().equals(TryEmitter.ROYALE_MULTI_CATCH_ERROR_NAME)
                );
    }


}
