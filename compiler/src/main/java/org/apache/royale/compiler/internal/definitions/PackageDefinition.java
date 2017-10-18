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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.tree.as.IPackageNode;

/**
 * This is the abstract base class for definitions in the symbol table that
 * represent packages.
 * <p>
 * An instance of this class can represent a top-level packages such as
 * "flashx", an intermediate-level package such as "flashx.textLayout", or a
 * leaf-level package such as "flashx.textLayout.elements".
 * <p>
 * After a package definition is in the symbol table, it should always be
 * accessed through the read-only <code>IPackageDefinition</code> interface.
 */
public class PackageDefinition extends MemberedDefinition implements IPackageDefinition
{
    private static final String STAR = "*";

    private static final String DOT_STAR = ".*";

    /**
     * Given an MXML namespace URI, this method returns the corresponding
     * package name, or <code>null</code> if the URI doesn't have the form of a
     * package URI.
     * <p>
     * For example, the namespace <code>attribute xmlns:my="*"</code>
     * corrresponds to the package name <code>""</code>, the attribute
     * <code>xmlns:fd="flash.display.*"</code> corresponds to the package name
     * <code>"flash.display"</code>, and the attribute
     * <code>xmlns:s="library://ns.adobe.com/flex/spark"</code> does not
     * correspond to any package.
     * 
     * @param uri An MXML namespace URI.
     * @return A qualified package name, or <code>null</code>.
     */
    public static String getQualifiedPackageNameFromNamespaceURI(String uri)
    {
        String packageName = null;

        if (uri.equals(STAR))
        {
            packageName = "";
        }
        else if (uri.endsWith(DOT_STAR))
        {
            // Strip off the ".*".
            packageName = uri.substring(0, uri.length() - DOT_STAR.length());
        }

        return packageName;
    }

    /**
     * Constructor.
     * 
     * @param packageName The fully-qualified name of the package,
     * such as <code>"flash.display"</code>.
     */
    public PackageDefinition(String packageName)
    {
        // Unlike most definitions, which store an undotted name
        // in the 'storageName' field, a PackageDefinition stores
        // its dotted qualified name (e.g. "flash.display").
        // For a package, the package name is considered to be
        // both the base name and qualified name.
        super(packageName);

        setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());
    }

    private PackageKind packageKind = PackageKind.CONCRETE;

    @Override
    public PackageKind getPackageKind()
    {
        return packageKind;
    }

    public void setVirtual()
    {
        packageKind = PackageKind.VIRTUAL;
    }

    @Override
    protected String toStorageName(String name)
    {
        return name;
    }

    @Override
    public String getBaseName()
    {
        // A package name like "flash.display", which is stored in the
        // 'storageName' field, is considered to be both the base name
        // and the qualified name of a PackageDefinition.
        return getStorageName();
    }

    @Override
    public String getQualifiedName()
    {
        // A package name like "flash.display", which is stored in the
        // 'storageName' field, is considered to be both the base name
        // and the qualified name of a PackageDefinition.
        return getStorageName();
    }

    @Override
    public IPackageNode getNode()
    {
        return (IPackageNode)super.getNode();
    }

    /**
     * Used only for debugging, as part of {@link #toString()}.
     */
    @Override
    protected void buildInnerString(StringBuilder sb)
    {
        String name = getQualifiedName();

        sb.append(IASKeywordConstants.PACKAGE);
        sb.append(' ');
        sb.append(name.length() > 0 ? name : "\"\"");
    }
}
