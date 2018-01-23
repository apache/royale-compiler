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

package org.apache.royale.compiler.internal.config;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;

/**
 * Setters for Project settings that can come from command line compiler 
 * options. This interface is implemented by Configurator and IRoyaleProject to
 * keep the common setters for these interfaces in sync.  
 */
public interface IWriteOnlyProjectSettings
{
    /**
     * Sets a list of path elements that form the roots of ActionScript class hierarchies.
     * Calling this method can invalidate a significant amount of incremental compilation results.
     * This is equivalent to using <code>mxmlc/compc --compiler.source-path</code>.
     * 
     * @param paths List of directories that should be searched for source files.
     */
    void setSourcePath(List<File> paths);
    
    /**
     * Sets the locales that the compiler would use to replace <code>{locale}</code> tokens that appear in some configuration values.
     * This is equivalent to using <code>mxmlc/compc --compiler.locale</code>.
     * For example,
     * 
     * The <code>locale/en_US</code> directory will be added to the source path.
     * 
     */
    void setLocales(Collection<String> locales);
    
    /**
     * Sets the ActionScript file encoding. The compiler will use this encoding to read
     * the ActionScript source files.
     * This is equivalent to using <code>mxmlc/compc --compiler.actionscript-file-encoding</code>.<p>
     * By default, the encoding is <code>UTF-8</code>.
     * 
     * @param encoding charactere encoding, e.g. <code>UTF-8</code>, <code>Big5</code>
     */
    void setActionScriptFileEncoding(String encoding);
    
    /**
     * Set global AS3 compiler configuration values and clear existing definitions.
     * This is equivalent to using the <code>mxmlc/compc --define</code> option 
     * once for each definition in the map.
     * 
     * @param defines A map of qualified names/configuration constants, e.g. "CONFIG::debugging" or "APP::version",
     * to value constants, e.g. "true" or "!CONFIG::release" or "3.0". Null clears 
     * the existing definitions.
     */
    void setDefineDirectives(Map<String,String> defines);
    
    /**
     * Sets the SDK compatibility version.
     * 
     * @param major The major version.
     * @param minor The minor version.
     * @param revision The revision component of the version.
     */
    void setCompatibilityVersion(int major, int minor, int revision);
    
    /**
     * Sets the mappings from MXML namespace URI to manifest files, as specified
     * by -namespace options.
     * 
     * @param namespaceMappings An array of {@code MXMLNamespaceMapping}
     * objects.
     */
    void setNamespaceMappings(List<? extends IMXMLNamespaceMapping> namespaceMappings);

    /**
     * Configures a list of many extensions mapped to a single Extension URI. <extension>
     * <extension>something-extension.jar</extension> <parameters>version=1.1,content=1.2</parameters> </extension>
     * The extensions run at the end of a compilation.
     * 
     * @param extensions a map of extension element files to extension paths.
     */
    void setExtensionLibraries(Map<File, List<String>> extensions);
    
    /**
     * Configure this project to use a services-config.xml file. 
     * The services-config.xml file is used to configure a Royale client to talk 
     * to a BlazeDS server.
     * 
     * This is equivalent to using <code>mxmlc/compc <code>--services</code> and 
     * <code>--context-root</code>.
     * 
     * @param path an absolute path to the services-config.xml file.
     * @param contextRoot sets the value of the {context.root} token, which is
     *  often used in channel definitions in the services-config.xml file.
     */
    // TODO: should path be a File instead of a String?
    void setServicesXMLPath(String path, String contextRoot);
    
    /**
     * A collection of source files or directories to add to the target library.
     *
     * This is equivalent to the <code>include-sources</code> option of the 
     * <code>compc</code> compiler.
     *
     * @param sources A collection of source files or directories.
     */
    void setIncludeSources(Collection<File> sources) throws InterruptedException;
 
    /**
     * Option to enable or prevent various Royale compiler behaviors. This is
     * currently used to enable/disable the generation of a root class for
     * library swfs and generation of Royale specific code for application swfs.
     * 
     * @param value true to turn on Royale behaviors, false otherwise.
     */
    void setRoyale(boolean value);

    /**
     * Option to remove the Native JS libraries from external-library-path
     * and library-path as they shouldn't be any when compiling SWFs / SWCs.
     *
     * @param value true to turn on the behaviors, false otherwise.
     */
    void setExcludeNativeJSLibraries(boolean value);
}
