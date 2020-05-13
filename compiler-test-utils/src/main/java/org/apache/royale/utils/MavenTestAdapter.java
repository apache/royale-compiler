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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 */
public class MavenTestAdapter implements ITestAdapter {

    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = KILOBYTE * 1024;
    private static final int BUFFER_MAX = MEGABYTE;

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
    public List<File> getLibraries(boolean withRoyale) {
        List<File> libs = new ArrayList<File>();
        File pg = getPlayerglobal();
        if (pg != null)
            libs.add(getPlayerglobal());
        if(withRoyale) {
            String royaleVersion = System.getProperty("royaleVersion");
            libs.add(getDependency("org.apache.royale.framework", "framework", royaleVersion, "swc", null));
            libs.add(getDependency("org.apache.royale.framework", "rpc", royaleVersion, "swc", null));
            libs.add(getDependency("org.apache.royale.framework", "spark", royaleVersion, "swc", null));
        }
        return libs;
    }

    public boolean optionWithSwfEnabled() {
        return Boolean.valueOf(System.getProperty("optionWithSwfEnabled", "false"));
    }

    @Override
    public File getPlayerglobal() {
        if(!optionWithSwfEnabled()) {
            return null;
        }
        try {
            return getDependency("com.adobe.flash.framework", "playerglobal",
                    System.getProperty("flashVersion"), "swc", null);
        }
        catch (RuntimeException e) {};
        return null;
    }

    @Override
    public File getFlashplayerDebugger() {
        if(!optionWithSwfEnabled()) {
            return null;
        }
        String FLASHPLAYER_DEBUGGER = System.getProperty("FLASHPLAYER_DEBUGGER", null);
        if(FLASHPLAYER_DEBUGGER == null || FLASHPLAYER_DEBUGGER.length() == 0) {
            FLASHPLAYER_DEBUGGER = System.getenv("FLASHPLAYER_DEBUGGER");
            if(FLASHPLAYER_DEBUGGER == null || FLASHPLAYER_DEBUGGER.length() == 0) {
                System.out.println("FLASHPLAYER_DEBUGGER not specified");
                return null;
            }
        }
        System.out.println("Using Flash");
        return new File(FLASHPLAYER_DEBUGGER);
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
            unpackFrameworkConfigs(configsZip, unpackedConfigsDir);
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
    public String getRoyaleManifestPath(String type) {
        File configsZip = getDependency("org.apache.royale.framework", "framework",
                System.getProperty("royaleVersion"), "zip", "configs");
        File frameworkDir = configsZip.getParentFile();
        File unpackedConfigsDir = new File(frameworkDir, "configs_zip");
        // If the directory doesn't exist, we have to create it by unpacking the zip archive.
        // This is identical behaviour to Flexmojos, which does the same thing.
        if(!unpackedConfigsDir.exists()) {
            unpackFrameworkConfigs(configsZip, unpackedConfigsDir);
        }
        return new File(unpackedConfigsDir, type + "-manifest.xml").getPath();
    }

    @Override
    public File getRoyaleArtifact(String artifactName) {
        String royaleVersion = System.getProperty("royaleVersion");
        return getDependency("org.apache.royale.framework", artifactName, royaleVersion, "swc", null);
    }

    @Override
    public File getUnitTestBaseDir() {
        return new File(FilenameUtils.normalize("target/test-classes"));
    }

    private File getDependency(String groupId, String artifactId, String version, String type, String classifier) {
        String dependencyPath = System.getProperty("mavenLocalRepoDir") + File.separator +
                groupId.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + artifactId + File.separator + version +
                File.separator + artifactId + "-" + version + ((classifier != null) ? "-" + classifier : "") + "." +
                type;
        File dependency = new File(dependencyPath);
        if(!dependency.exists()) {
            throw new RuntimeException("Could not read SWC dependency at " + dependency.getAbsolutePath());
        }
        return dependency;
    }

    private void unpackFrameworkConfigs(File configZip, File outputDirectory) {
        final byte[] data = new byte[BUFFER_MAX];
        ArchiveInputStream archiveInputStream = null;
        ArchiveEntry entry;
        try {
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(
                    new BufferedInputStream(new FileInputStream(configZip)));
            if(!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                throw new RuntimeException("Could not create output directory for config zip at " +
                        outputDirectory.getPath());
            }
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                final File outputFile = new File(outputDirectory, entry.getName());

                // Entry is a directory.
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        if(!outputFile.mkdirs()) {
                            throw new RuntimeException(
                                    "Could not create output directory " + outputFile.getAbsolutePath());
                        }
                    }
                }

                // Entry is a file.
                else {
                    final FileOutputStream fos = new FileOutputStream(outputFile);
                    BufferedOutputStream dest = null;
                    try {
                        dest = new BufferedOutputStream(fos, BUFFER_MAX);

                        int count;
                        while ((count = archiveInputStream.read(data, 0, BUFFER_MAX)) != -1) {
                            dest.write(data, 0, count);
                        }
                    } finally {
                        if(dest != null) {
                            dest.flush();
                            dest.close();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error unpacking resources", e);
        } catch (IOException e) {
            throw new RuntimeException("Error unpacking resources", e);
        } catch (ArchiveException e) {
            throw new RuntimeException("Error unpacking resources", e);
        } finally {
            if(archiveInputStream != null) {
                try {
                    archiveInputStream.close();
                } catch(Exception e) {
                    // Ignore...
                }
            }
        }
    }

}
