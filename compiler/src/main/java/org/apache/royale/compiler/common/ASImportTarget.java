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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.workspaces.IWorkspace;

public class ASImportTarget implements IImportTarget
{
    private final NamespaceDefinition.ILanguageNamespaceDefinition importedPublicPackageNamespace;
    private final String definitionName;

    public static ASImportTarget get(IWorkspace w, String target)
    {
        String packageName = null;
        String definitionName = null;
        int lastDotIndex = target.lastIndexOf('.');
        if (lastDotIndex == -1)
        {
            packageName = "";
            if (!target.equals("*"))
            {
                definitionName = target;
            }
        }
        else
        {
            packageName = target.substring(0, lastDotIndex);
            String afterLastDot = target.substring(lastDotIndex + 1);
            if (!afterLastDot.equals("*"))
                definitionName = afterLastDot;
        }
        Workspace workspace = (Workspace)w;
        NamespaceDefinition.ILanguageNamespaceDefinition packagePublicNamespace = 
            workspace.getPackageNamespaceDefinitionCache().get(packageName, false);
        return new ASImportTarget(packagePublicNamespace, definitionName);
    }
    
    /**
     * Create an ASImport from a package-style XML namespace
     * @param xmlNamespace  package-style XML namespace (e.g. mycontrols.* from xmlns:my="mycontrols.*")
     * @return              ASImport representing the imported classes (e.g. mycontrols.*)
     */
    public static IImportTarget buildImportFromXMLNamespace(IWorkspace w, String xmlNamespace)
    {
        String directory = xmlNamespace;
        if (xmlNamespace != null && xmlNamespace.endsWith("*"))
        {
            directory = directory.substring(0, directory.length()-1);

            // make sure the namespace id is actually a package name and not a URI
            // like http://foo.adobe.com/2006/mxml
            if (directory.indexOf("/") == -1 &&
                    directory.indexOf(":") == -1)
            {
                return get(w, xmlNamespace); //new ASImportTarget(xmlNamespace);
            }
        }
        return null;
    }
    
    /**
     * Create an ASImport from a package name (as it appears in a package definition)
     * @param packageName       package name (e.g. mx.controls)
     * @return                  ASImport representing the imported classes (e.g. mx.controls.*)
     */
    public static IImportTarget buildImportFromPackageName(IWorkspace w, String packageName)
    {
        if (packageName == null || (packageName.length() == 1 && packageName.charAt(0) == '*') || packageName.length() == 0) 
            packageName = "";
        Workspace workspace = (Workspace)w;
        NamespaceDefinition.ILanguageNamespaceDefinition packagePublicNamespace = 
            workspace.getPackageNamespaceDefinitionCache().get(packageName, false);
        return new ASImportTarget(packagePublicNamespace, null);
    }
    
	/**
	 * Constructor
	 * @param target	import target (e.g. mx.controls.* or mx.controls.Button)
	 */
	private ASImportTarget(NamespaceDefinition.ILanguageNamespaceDefinition namespace, String definitionName)
	{
		this.importedPublicPackageNamespace = namespace;
		this.definitionName = definitionName;
	}
	
	@Override
    public String getTargetPackage()
	{
		return importedPublicPackageNamespace.getURI();
	}
	
	@Override
    public boolean isWildcard()
	{
		return definitionName == null;
	}
	
	@Override
    public String getTargetName() {
		return definitionName == null ? "" : definitionName;
	}
	
	@Override
    public String getQualifiedName(String reference)
	{
	    int lastIndexOfDotInReferece = reference.lastIndexOf('.');
		// If we're referring to a short name (e.g. Button)...
		if (lastIndexOfDotInReferece == -1)
		{
			if ((definitionName == null) || (reference.equals(definitionName)))	// importing a whole package (e.g. mx.controls.*)
			{
				StringBuilder builder = new StringBuilder();
				String uri = importedPublicPackageNamespace.getURI();
				if (uri != null && uri.length() > 0) {
				    builder.append(uri);
				    builder.append('.');
				}
				builder.append(reference);
				return builder.toString();
			}
			return null; // no match! (e.g. mx.controls.Button and RadioButton)
			
		}
		String packageNameInReference = reference.substring(0, lastIndexOfDotInReferece);
		if (!packageNameInReference.equals(importedPublicPackageNamespace.getURI()))
		    return null; // package names don't match ( e.g. mx.core and mx.controls.Button
		
		if (definitionName == null)	// importing a whole package (e.g. mx.controls.*)
		    return reference;
		
		String definitionNameInReference = reference.substring(lastIndexOfDotInReferece + 1);
		if (definitionNameInReference.equals(definitionName))		// exact match! (e.g. mx.controls.Button and mx.controls.Button)
			return reference;
		// no match! (e.g. mx.controls.Button and mx.controls.RadioButton)
		return null;
		
	}
	
	/**
	 * Get the import target as a string (as it would appear after the keyword "import").
	 * @return		the string representation of the import target
	 */
	@Override
	public String toString()
	{
	    String packageName = importedPublicPackageNamespace.getURI();
	    int definitionNameLen = definitionName == null ? 2 : definitionName.length();
	    StringBuilder builder = new StringBuilder(packageName.length() + definitionNameLen);
		builder.append(packageName);
		builder.append('.');
		builder.append(definitionName == null ? "*" : definitionName);
	    return builder.toString();
	}
	
	@Override
    public INamespaceDefinition getNamespace()
	{
	    return this.importedPublicPackageNamespace;
	}
}
