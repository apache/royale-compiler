/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.royale.maven.trust;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Little helper that adds a directory to the FlashPlayer trust settings.
 * This prevents the FlashPlayer from complaining about running untrusted
 * code, which will prevent the tests using the FlashPlayer from succeeding.
 */
@Named
@Singleton
public class DefaultTrustHandler implements TrustHandler {

    @Override
    public void trustDirectory(File directory) {
        File securityTrustFile = new File(getSecuritySettingsDirectory(), "apache-royale-maven-plugin.cfg");

        if(!securityTrustFile.exists()) {
            System.out.println(" - Creating new FlashPlayer security trust file at: " + securityTrustFile.getPath());
            try {
                if(!securityTrustFile.createNewFile()) {
                    throw new RuntimeException("Could not create FlashPlayer security trust file at: " +
                            securityTrustFile.getPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create FlashPlayer security trust file at: " +
                        securityTrustFile.getPath(), e);
            }
        } else {
            System.out.println(" - Updating FlashPlayer security trust file at: " + securityTrustFile.getPath());
        }

        // Check if the current directory is already listed in the file, if not, append it to the file.
        try {
            List<String> trustedDirectories = FileUtils.readLines(securityTrustFile, "UTF-8");
            if(!trustedDirectories.contains(directory.getAbsolutePath())) {
                FileUtils.writeStringToFile(securityTrustFile, directory.getAbsolutePath() + "\n", "UTF-8", true);
                System.out.println(" - Added directory '" + directory.getAbsolutePath() +
                        "' to FlashPlayer security trust file at: " + securityTrustFile.getPath());
            } else {
                System.out.println(" - Directory '" + directory.getAbsolutePath() +
                        "' already listed in FlashPlayer security trust file at: " + securityTrustFile.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not add directory '" + directory.getPath() +
                    "' to FlashPlayer security trust file", e);
        }
    }

    private File getSecuritySettingsDirectory() {
        File userHome = new File(System.getProperty("user.home"));
        File securitySettingsDirectory;

        if(SystemUtils.IS_OS_WINDOWS) {
            // Try to get the location of the APPDATA directory from an environment-variable.
            File appDataDirectory;
            if(System.getenv("APPDATA") != null) {
                appDataDirectory = new File(System.getenv("APPDATA"));
            }
            // If the environment-variable was not set, try defaults, depending on the
            // detail version of Windows.
            else {
                // Vista did things differently.
                if(SystemUtils.IS_OS_WINDOWS_VISTA) {
                    appDataDirectory = new File(userHome, "AppData/Roaming");
                } else {
                    appDataDirectory = new File(userHome, "Application Data");
                }
            }
            securitySettingsDirectory =
                    new File(appDataDirectory, "Macromedia/Flash Player/#Security/FlashPlayerTrust");
        }

        else if(SystemUtils.IS_OS_MAC) {
            securitySettingsDirectory = new File(userHome,
                    "Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust");
        }

        else if(SystemUtils.IS_OS_LINUX) {
            securitySettingsDirectory = new File(userHome, ".macromedia/Flash_Player/#Security/FlashPlayerTrust");
        }

        // As the FlashPlayer is only available on Windows, Mac and Linux, this is all we can do.
        else {
            throw new UnsupportedOperationException(
                    "FlashplayerSecurityHandler not prepared for handling OS type of: " + SystemUtils.OS_NAME);
        }

        // If the directory didn't exist yet, create it now.
        if(!securitySettingsDirectory.exists()) {
            if(!securitySettingsDirectory.mkdirs()) {
                throw new RuntimeException("Could not create FlashPlayer security settings directory at: " +
                        securitySettingsDirectory.getPath());
            }
        }

        return securitySettingsDirectory;
    }

}
