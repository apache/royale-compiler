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

package org.apache.royale.compiler.tree.as;

/**
 * An AST node representing a <code>package</code> directive.
 * <p>
 * The shape of this node is:
 * <pre>
 * IPackageNode
 *   IIdentifierNode <-- getNameExpressionNode()
 *   IScopedNode     <-- getScopedNode()
 * </pre>
 */
public interface IPackageNode extends IScopedDefinitionNode
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
}
