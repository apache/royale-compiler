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

package org.apache.royale.compiler.internal.units;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

//import flex.messaging.config.ServicesDependencies;

/**
 * A compilation unit used to compile Flex dependencies from BlazeDS. AS code
 * from BlazeDS is retrieved via an API call and used as source for this unit.
 * The path of the unit is represented by a services-config.xml file but the 
 * source of the file is obtained by calling 
 * ServicesDependencies.getServicesInitSource().
 *
 * The package of this compilation unit is fixed but the class name changes
 * based on the path of the services-config.xml file. Each services-config.xml 
 * needs a unique mixin class so its code will be executed at runtime. 
 */
public class ServicesXMLCompilationUnit extends ASCompilationUnit
{

    private static final String PACKAGE_NAME = "org.apache.royale.compiler.generated";
    private static final String CLASS_NAME_PREFIX = "_ServicesInit";

    /**
     * Get the Qname of a ServciesXMLCompilation unit based on path of the 
     * services-config.xml file.
     * 
     * @param path the absolute path of the services-config.xml file.
     * @return fully qualified name of a compilation unit.
     */
    public static String getQname(String path)
    {
        StringBuilder name = new StringBuilder();
        name.append(PACKAGE_NAME).append(".").append(CLASS_NAME_PREFIX).append("_").append(path.hashCode());
        return name.toString();
    }
    
    /**
     * Constructor.
     * 
     * @param project the project.
     * @param path the absolute path of the services configuration file.
     * @param contextRoot the string to substitute for "{content.root}" in 
     * the services configuration file.
     */
    public ServicesXMLCompilationUnit(CompilerProject project, String path, String contextRoot)
    {
        super(project, path, DefinitionPriority.BasePriority.SOURCE_LIST, 0, getQname(path));

        fileSpecification = new AtomicReference<IFileSpecification>();
    }
    
    private AtomicReference<IFileSpecification> fileSpecification;
    
    @Override
    protected final IFileSpecification getRootFileSpecification()
    {
        // This override creates gets the source of this unit and puts it
        // into a StringFileSpecification.
        if (fileSpecification.get() == null)
        {
            // Support for -services is being disabled because it is 
            // dependent on changes to flex-messaging-common.jar that
            // were never in an official release. The code is commented
            // out instead of removed so it can be revived in open
            // source.
            
//            ServicesDependencies services = new ServicesDependencies(getAbsoluteFilename(), null,
//                    contextRoot);
//            String qName = getQname(getAbsoluteFilename());
//            int classNameStart = qName.lastIndexOf('.');
//            String content = services.getServicesInitSource(PACKAGE_NAME, qName.substring(classNameStart + 1));
//            File file = new File(getAbsoluteFilename());
//            fileSpecification.compareAndSet(null, new StringFileSpecification(qName, content, file.lastModified()));
        }
        
        return fileSpecification.get();
    }

    @Override
    public boolean clean(Map<ICompilerProject, Set<File>> invalidatedSWCFiles, Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate, boolean clearFileScope)
    {
        // Reset the fileSpecification when the services-config.xml is modified by the user.
        // This is cause us re-fetch the source in getRootFileSpecification.
        fileSpecification.set(null);
        
        return super.clean(invalidatedSWCFiles, cusToUpdate, clearFileScope);
    }
}
