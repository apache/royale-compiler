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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.tree.as.IPackageNode;

/**
 * A definition representing a package declaration.
 * <p>
 * An <code>IPackageDefinition</code is created from an <code>IPackageNode</code>.
 * <p>
 * For example, the declaration
 * <pre>package flash.display
 * {
 * }</pre>
 * creates a package definition whose base name is <code>"display"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is <code>null</code>.
 */
public interface IPackageDefinition extends IMemberedDefinition
{
    /**
     * Specifics the kind of package we are dealing with. Concrete packages are
     * those found explicitly in source, while virtual packages are runtime
     * approximations of packages, spanning multiple files
     */
    enum PackageKind
    {
        /**
         * Virtual packages are runtime approximations of packages, spanning
         * multiple files
         */
        VIRTUAL,

        /**
         * Concrete packages are those found explicitly in source
         */
        CONCRETE
    }

    /**
     * Returns the kind of package that this node represents
     * 
     * @return a {@link PackageKind}
     */
    PackageKind getPackageKind();

    /**
     * Returns the {@link IPackageNode} from which this definition was created,
     * if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    IPackageNode getNode();
}
