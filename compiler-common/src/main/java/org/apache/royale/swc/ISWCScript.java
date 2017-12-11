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

package org.apache.royale.swc;

import java.util.Set;

import org.apache.royale.compiler.common.DependencyType;
import com.google.common.collect.SetMultimap;

/**
 * Model for {@code <script>} entry in a SWC catagory.xml file.
 */
public interface ISWCScript
{
    /**
     * Get the name of the script. The name maps to the DoABC tag name in the
     * library SWF file.
     * 
     * @return script name
     */
    String getName();

    /**
     * Get the last modified time-stamp of the script.
     * 
     * @return last modified
     */
    long getLastModified();

    /**
     * Get signature checksum.
     * 
     * @return signature checksum
     */
    String getSignatureChecksum();

    /**
     * Get a list of QNames declared in the {@code <def>} tags.
     * 
     * @return definition QName list
     */
    Set<String> getDefinitions();

    /**
     * Get a map of dependencies and their dependency types.
     * 
     * @return dependencies
     */
    SetMultimap<String, DependencyType> getDependencies();

    /**
     * Add a definition to the script.
     * 
     * @param def name of the definition
     */
    void addDefinition(String def);

    /**
     * Add a dependency to the script.
     * 
     * @param name QName of the dependency
     * @param type type of dependency
     */
    void addDependency(String name, DependencyType type);
    
    /**
     * Attach the ABC byte code to the script.
     * @param abcData ABC byte code
     */
    void setSource(byte[] abcData);
    
    /**
     * Get the ABC byte code of the script.
     * @return ABC data
     */
    byte[] getSource();
}
