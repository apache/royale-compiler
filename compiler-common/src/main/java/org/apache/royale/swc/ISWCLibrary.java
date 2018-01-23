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

import java.util.List;
import java.util.Set;

import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFReader;

/**
 * Model for library definitions in a SWC file.
 */
public interface ISWCLibrary
{
    /**
     * Get the path of the library SWF file.
     * 
     * @return relative path of the SWF file
     */
    String getPath();

    /**
     * Get all the {@code IScript} objects in this library.
     * 
     * @return list of scripts
     */
    List<ISWCScript> getScripts();

    /**
     * Get script by name.
     * 
     * @param name script name
     * @return script
     */
    ISWCScript getScript(String name);

    /**
     * Gets all the metadata names in the {@code <keep-as3-metadata>} tag.
     * 
     * @return metadata name list
     */
    Set<String> getKeepAS3MetadataSet();
    
    /**
     * Adds a metadata name to appear in the {@code <keep-as3-metadata>} tag.
     * 
     * @param name A metadata name, like <code>"Bindable"</code>.
     */
    public void addNameToKeepAS3MetadataSet(String name);

    /**
     * Add an {@link ISWCScript} to the library.
     * 
     * @param script {@code IScript} object
     */
    void addScript(ISWCScript script);

    /**
     * Add an {@link ISWCDigest} to the library.
     * 
     * @param digest {@code ISWCDigest} object
     */
    void addDigest(ISWCDigest digest);

    /**
     * Read the {@code InputStream} of the library SWF file.
     * 
     * @param swfReader containing {@code ISWFReader} object
     * @param swc containing {@code ISWC} object
     */
    void readSWFInputStream(ISWFReader swfReader, ISWC swc);

    /**
     * Get the digests of the library.
     * 
     * @return a list of digest information
     */
    List<ISWCDigest> getDigests();

    /**
     * Get the SWF model for the library.
     * 
     * @return null or library SWF model
     */
    ISWF getSWF();

}
