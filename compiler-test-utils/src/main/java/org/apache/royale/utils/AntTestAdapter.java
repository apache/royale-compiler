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

package org.apache.royale.utils;

import org.junit.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class AntTestAdapter implements ITestAdapter {

    private static EnvProperties env = EnvProperties.initiate();

    private static File PLAYERGLOBAL_SWC;
    // The Ant script for compiler.tests copies a standalone player to the temp directory.
    private static File FLASHPLAYER;

    private static File LIBS_ROOT;
    private static File RESOURCE_BUNDLES_ROOT;

    @Override
    public String getTempDir() {
        return FilenameNormalization.normalize("target/junit-temp"); // ensure this exists
    }

    @Override
    public List<File> getLibraries(boolean withFlex) {
        // Do some checks if all needed environment variables are set.
        if (withFlex) {
            Assert.assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
	        Assert.assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set", env.FPSDK);
            LIBS_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs"));        	
            RESOURCE_BUNDLES_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US"));
        }
        
        // Create a list of libs needed to compile.
        List<File> libraries = new ArrayList<File>();
        File pg = getPlayerglobal();
        if (pg != null)
        	libraries.add(getPlayerglobal());
        if (withFlex)
        {
            libraries.add(getFlexArtifact("framework"));
            libraries.add(getFlexArtifact("rpc"));
            libraries.add(getFlexArtifact("spark"));
        }
        return libraries;
    }

    @Override
    public File getPlayerglobal() {
        if (PLAYERGLOBAL_SWC == null && env.FPSDK != null)
	        PLAYERGLOBAL_SWC = new File(FilenameNormalization.normalize(env.FPSDK + "\\" + env.FPVER + "\\playerglobal.swc"));
        return PLAYERGLOBAL_SWC;
    }

    @Override
    public File getFlashplayerDebugger() {
    	if (FLASHPLAYER == null && env.FDBG != null)
        	FLASHPLAYER = new File(FilenameNormalization.normalize(env.FDBG));
        return FLASHPLAYER;
    }

    @Override
    public String getFlexManifestPath(String type) {
        return FilenameNormalization.normalize(env.SDK + "\\frameworks\\" + type + "-manifest.xml");
    }

    @Override
    public File getFlexArtifact(String artifactName) {
        return getLib(artifactName);
    }

    @Override
    public File getFlexArtifactResourceBundle(String artifactName) {
        return getResourceBundle(artifactName);
    }

    @Override
    public String getRoyaleManifestPath(String type) {
        return null;
    }

    @Override
    public File getRoyaleArtifact(String artifactName) {
        return null;
    }

    @Override
    public File getUnitTestBaseDir() {
        return new File("target/test-classes");
    }

    private File getLib(String artifactId) {
    	if (LIBS_ROOT == null)
    		LIBS_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs"));        	
        return new File(LIBS_ROOT, artifactId + ".swc");
    }

    private File getResourceBundle(String artifactId) {
    	if (RESOURCE_BUNDLES_ROOT == null)
    		RESOURCE_BUNDLES_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US"));
        return new File(RESOURCE_BUNDLES_ROOT, artifactId + "_rb.swc");
    }

}
