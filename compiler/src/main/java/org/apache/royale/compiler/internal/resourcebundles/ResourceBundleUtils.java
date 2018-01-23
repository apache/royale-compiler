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

package org.apache.royale.compiler.internal.resourcebundles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.config.QNameNormalization;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.ResourceBundleSourceFileHandler;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundForLocaleProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;

public class ResourceBundleUtils
{
    public static final String CLASS_SUFFIX = "_" + ResourceBundleSourceFileHandler.EXTENSION;

    
    /**
     * Returns the qualified name for the auto generated resource bundle class for a
     * specified locale and a bundleName. These class names, such as
     * "foo.en_US$core_properties", are constructed in such a way that the locale
     * and bundle name can be extracted from them by the following two methods.
     * Note: The framework class mx.managers.SystemManager has similar logic
     * which must be kept in sync with this.
     * 
     * @param locale locale of the resource bundle
     * @param bundleName qualified name of the bundle
     * @return the fully qualified class name for this bundle and locale
     */
    public static String getQualifiedName(String locale, String bundleName)
    {
        String normalizedBundleName = QNameNormalization.normalize(bundleName);
        
        String packageName = Multiname.getPackageNameForQName(normalizedBundleName);
        String className = Multiname.getBaseNameForQName(normalizedBundleName);
        
        StringBuilder sb = new StringBuilder();
        if(packageName != null && packageName.length() > 0)
        {
            sb.append(packageName);
            sb.append(".");
        }
        sb.append(locale);
        sb.append("$");
        sb.append(className);
        sb.append(CLASS_SUFFIX);
        
        return sb.toString();
    }
    
    /**
     * Extracts the locale from the specified qualified name for an auto generated 
     * resource bundle class. Class names for ResourceBundles, such as
     * "foo.en_US$core_properties", are constructed in such a way that the locale
     * and bundle name can be extracted.
     * Note: The framework class mx.managers.SystemManager has similar logic
     * which must be kept in sync with this.
     * 
     * @param qualifiedName the fully qualified class name for a resource bundle
     * @return extracted locale of this qualified name
     */
    public static String getLocale(String qualifiedName)
    {
        String className = Multiname.getBaseNameForQName(qualifiedName);
        
        int indexOf = className.indexOf('$');
        if(indexOf > 0)
        {
            return className.substring(0, indexOf);
        }
        
        return null;
    }   
    
    /**
     * This methods converts a resource bundle qualified name in "dotted syntax"
     * to use "colon syntax". The reason is that Flex SDK expects the resource
     * bundle names in "colon syntax". 
     * [See Flex SDK's ResourceManagerImpl.installCompiledResourceBundle]
     * 
     * Example: foo.bar.xyz will be converted to foo.bar:xyz
     * 
     * @param bundleName qualified name of resource bundle
     * @return qualified name of the resource bundle in "colon syntax".
     */
    public static String convertBundleNameToColonSyntax(String bundleName)
    {
        String normalizedName = QNameNormalization.normalize(bundleName);
        
        String packageName = Multiname.getPackageNameForQName(normalizedName);
        if(packageName == null || packageName.length() == 0)
        {
            return bundleName;
        }
        
        String className = Multiname.getBaseNameForQName(normalizedName);
        
        return packageName + ":" + className;
    }

    
    /**
     * Resolving the references to the specified resourceBundleName and adds the
     * necessary dependency from the specified compilation unit to resolved
     * resource bundle's compilation unit.
     * 
     * @param bundleName resource bundle name
     * @param refCompUnit compilation unit that has a reference to the specified
     * resource bundle name
     * @param location location of the resource bundle's occurrence in the file
     * associated with the specified compilation unit
     * @param errors error collection to collect problems
     */
    public static void resolveDependencies(String bundleName, 
            final ICompilationUnit refCompUnit, 
            final ICompilerProject project, 
            final ISourceLocation location,
            final Collection<ICompilerProblem> errors) throws InterruptedException {
        
        Map<String, ICompilationUnit> qualifiedNameMap = findCompilationUnits(
            bundleName, project, refCompUnit.getAbsoluteFilename(), location, errors);
        for (Map.Entry<String, ICompilationUnit> entry : qualifiedNameMap.entrySet())
        {
            ((RoyaleProject)project).addDependency(refCompUnit, entry.getValue(), 
                    DependencyType.EXPRESSION, entry.getKey());
        }
    }
    
    /**
     * Find all the compilation units associated with the specified resource bundle name.
     * 
     * @param bundleName name of the resource bundle
     * @param project associated project
     * @param errors error collection to collect problems
     * @return the list of compilation units that exist to handle the specified resource bundle.
     */
    public static Collection<ICompilationUnit> findCompilationUnits(String bundleName,
            final ICompilerProject project,
            final Collection<ICompilerProblem> errors) throws InterruptedException {
        
        return findCompilationUnits(bundleName, project, null, null, errors).values();
    }

    /**
     * Find all the compilation units associated with the specified resource
     * bundle name.
     * 
     * @param bundleName name of the resource bundle
     * @param cach definition cache
     * @param refFilePath path of the file that has a reference to this resource
     * bundle or could be <code>null</code>.
     * @param location location where the reference in the specified file
     * occurred or could be <code>null</code>.
     * @param errors error collection to collect problems
     * @return the map of qualified name and compilation unit pairs that exist to handle the specified
     * resource bundle.
     * @throws InterruptedException
     */
    private static Map<String, ICompilationUnit> findCompilationUnits(String bundleName, 
            final ICompilerProject project,
            final String refFilePath,
            final ISourceLocation location,
            final Collection<ICompilerProblem> errors) throws InterruptedException {
        
        Map<String, ICompilationUnit> compilationUnits = new HashMap<String, ICompilationUnit>();
        
        if (project instanceof RoyaleProject)
        {
            final String normalizedBundleName = QNameNormalization.normalize(bundleName);
            final RoyaleProject royaleProject = (RoyaleProject)project;
            final ASProjectScope scope = royaleProject.getScope();
            final Collection<String> locales = royaleProject.getLocales();
            
            if(locales.size() == 0)
            {
                //If the project's locale is empty, then don't try to resolve this 
                //node because the referenced bundle won't go into swf or swc anyways.
                return Collections.emptyMap();
            }
            
            Collection<String> unresolvedLocales = new ArrayList<String>(locales);
            for(String locale : locales) 
            {   
                //resolve the generated qualified name for the bundle name referenced in this metadata tag and this locale 
                String qualifiedName = ResourceBundleUtils.getQualifiedName(locale, normalizedBundleName);
                
                //find the compilation units for this qualified name
                IDefinition def = scope.findDefinitionByName(qualifiedName);
                if(def != null) 
                {
                    ICompilationUnit compUnit = scope.getCompilationUnitForDefinition(def);

                    if (isValidCompilationUnit(compUnit, project, bundleName))
                    {
                        compilationUnits.put(def.getQualifiedName(), compUnit);
                        unresolvedLocales.remove(locale);
                    }                 
                }
            }
           
            if (unresolvedLocales.size() > 0 || locales.size() == 0) 
            {
                //If we are at this point, it means that either project's locale is empty or 
                //we could not resolve the resource bundle for some or all locales. 
                //The last thing we can check is to see whether we can resolve it using the 
                //pure bundle name (not the generated name that contains locale$..._properties)
                //This is also acceptable because "mx.managers.SystemManager" uses the same logic 
                //to find a resource bundle. First, it searches for the generated class name and then the pure bundle name.
                IDefinition def = scope.findDefinitionByName(normalizedBundleName);
                if(def != null)
                {
                    ICompilationUnit compUnit = scope.getCompilationUnitForDefinition(def);

                    if (isValidCompilationUnit(compUnit, project, bundleName))
                    { 
                        //This means we resolved the reference by using pure bundle name which will apply 
                        //to all locales during runtime, therefore return from this method.
                        compilationUnits.put(def.getQualifiedName(), compUnit);
                        return compilationUnits;
                    } 
                }
                
                // At this point, it means that we were not able to resolve this resource bundle reference.
                // Therefore, report an error.
                if (unresolvedLocales.size() == locales.size())
                {
                    //we couldn't resolve it for any locales or there was no locale, 
                    //so show the generic error which doesn't mention any locales.
                    ResourceBundleNotFoundProblem problem = (location != null) ? 
                            new ResourceBundleNotFoundProblem(location, bundleName) :
                            new ResourceBundleNotFoundProblem(bundleName);
                    errors.add(problem);
                } 
                else 
                {
                    //some of the locales couldn't be resolved so show errors expressing which locales couldn't be resolved.
                    for(String locale : unresolvedLocales) 
                    {
                        ResourceBundleNotFoundForLocaleProblem problem = (location != null) ? 
                                new ResourceBundleNotFoundForLocaleProblem(location, bundleName, locale) :
                                new ResourceBundleNotFoundForLocaleProblem(bundleName, locale);
                        errors.add(problem);
                    }
                }
            }
            
        }
        
        return compilationUnits;
    }

    /**
     * Checks whether the definition associated with the specified compilation
     * unit can be used as a ResourceBundle. A compilation unit is valid if it
     * is associated with a properties file or a class that extends
     * mx.resources.ResourceBundle.
     * 
     * @param compUnit compilation unit to validate
     * @param project owner project
     * @param cache project's cache
     * @return <code>true</code> if the definition associated with the
     * compilation unit can be used as a ResourceBundle, <code>false</code>
     * otherwise.
     * @throws InterruptedException
     */
    private static boolean isValidCompilationUnit(final ICompilationUnit compUnit, 
                                                  final ICompilerProject project,
                                                  final String bundleName) throws InterruptedException
    {
        if (compUnit != null) 
        {
            //If we have a resource bundle compilation unit, then use it.
            if (compUnit instanceof ResourceBundleCompilationUnit)
            {
                //This guarantees that the bundle name used while referencing is in the same syntax 
                //as we are expecting. For example, if the properties file is in a package, as of now, 
                //only "colon" syntax is accepted such as foo.bar:Name. Tehrefore, this check will fail
                //if the user references the bundle with "dotted syntax" such as foo.bar.Name 
                //since Flex SDK does not know how to handle that.
                if (((ResourceBundleCompilationUnit)compUnit).getBundleNameInColonSyntax().equals(bundleName))
                    return true;
            } 
            else 
            {
                IFileScopeRequestResult result = compUnit.getFileScopeRequest().get();

                for (IDefinition def : result.getExternallyVisibleDefinitions())
                {
                    if (def instanceof ClassDefinition)
                    {
                        //check whether class extends ResourceBundle
                        if (project instanceof RoyaleProject &&
                           ((ClassDefinition)def).isInstanceOf(((RoyaleProject)project).getResourceBundleClass(), project))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
