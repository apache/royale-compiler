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

package org.apache.flex.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by christoferdutz on 23.02.16.
 */
public class MavenTestAdapter implements ITestAdapter {

    @Override
    public String getTempDir() {
        File tempDir = new File("target/surefire-temp");
        if(!tempDir.exists()) {
            if(!tempDir.mkdirs()) {
                throw new RuntimeException("Could not create temp dir at: " + tempDir.getAbsolutePath());
            }
        }
        return tempDir.getPath();
    }

    @Override
    public List<File> getLibraries(boolean withFlex) {
        List<File> libs = new ArrayList<File>();
        libs.add(getPlayerglobal());
        if(withFlex) {
            String flexVersion = System.getProperty("flexVersion");
            libs.add(getDependency("org.apache.flex.framework", "framework", flexVersion, "swc", null));
            libs.add(getDependency("org.apache.flex.framework", "rpc", flexVersion, "swc", null));
            libs.add(getDependency("org.apache.flex.framework", "spark", flexVersion, "swc", null));
        }
        return libs;
    }

    @Override
    public File getPlayerglobal() {
        return getDependency("com.adobe.flash.framework", "playerglobal",
                System.getProperty("flashVersion"), "swc", null);
    }

    @Override
    public File getFlashplayerDebugger() {
        // TODO: If the archive isn't unpacked, unpack it.
        // TODO: Return a reference to the player debugger executable, depending on the current platform.
        String FLASHPLAYER_DEBUGGER = System.getProperty("FLASHPLAYER_DEBUGGER", null);
        return new File(FLASHPLAYER_DEBUGGER);
        /*return getDependency("com.adobe.flash.runtime", "player-debugger",
                System.getProperty("flashVersion"), "zip", null);*/
    }

    @Override
    public String getFlexManifestPath(String type) {
        File configsZip = getDependency("org.apache.flex.framework", "framework",
                System.getProperty("flexVersion"), "zip", "configs");
        File frameworkDir = configsZip.getParentFile();
        File unpackedConfigsDir = new File(frameworkDir, "configs_zip");
        // If the directory doesn't exist, we have to create it by unpacking the zip archive.
        // This is identical behaviour to Flexmojos, which does the same thing.
        if(!unpackedConfigsDir.exists()) {
            // TODO: Implement
        }
        return new File(unpackedConfigsDir, type + "-manifest.xml").getPath();
    }

    @Override
    public File getFlexArtifact(String artifactName) {
        String flexVersion = System.getProperty("flexVersion");
        return getDependency("org.apache.flex.framework", artifactName, flexVersion, "swc", null);
    }

    @Override
    public File getFlexArtifactResourceBundle(String artifactName) {
        String flexVersion = System.getProperty("flexVersion");
        return getDependency("org.apache.flex.framework", artifactName, flexVersion, "rb.swc", "en_US");
    }

    @Override
    public String getFlexJsManifestPath(String type) {
        File configsZip = getDependency("org.apache.flex.framework.flexjs", "framework",
                System.getProperty("flexJsVersion"), "zip", "configs");
        File frameworkDir = configsZip.getParentFile();
        File unpackedConfigsDir = new File(frameworkDir, "configs_zip");
        // If the directory doesn't exist, we have to create it by unpacking the zip archive.
        // This is identical behaviour to Flexmojos, which does the same thing.
        if(!unpackedConfigsDir.exists()) {
            // TODO: Implement
        }
        return new File(unpackedConfigsDir, type + "-manifest.xml").getPath();
    }

    @Override
    public File getFlexJSArtifact(String artifactName) {
        String flexJsVersion = System.getProperty("flexJsVersion");
        return getDependency("org.apache.flex.framework.flexjs", artifactName, flexJsVersion, "swc", null);
    }

    @Override
    public File getUnitTestBaseDir() {
        return new File(FilenameUtils.normalize("target/test-classes"));
    }

    private File getDependency(String groupId, String artifactId, String version, String type, String classifier) {
        String dependencyPath = System.getProperty("mavenLocalRepoDir") + File.separator +
                groupId.replaceAll("\\.", File.separator) + File.separator + artifactId + File.separator + version +
                File.separator + artifactId + "-" + version + ((classifier != null) ? "-" + classifier : "") + "." +
                type;
        File dependency = new File(dependencyPath);
        if(!dependency.exists()) {
            throw new RuntimeException("Could not read SWC dependency at " + dependency.getAbsolutePath());
        }
        return dependency;
    }

}
