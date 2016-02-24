package org.apache.flex.utils;

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
        libs.add(getDependency("com.adobe.flash.framework", "playerglobal",
                System.getProperty("flashVersion"), "swc", null));
        if(withFlex) {
            String flexVersion = System.getProperty("flexVersion");
            libs.add(getDependency("org.apache.flex.framework", "framework", flexVersion, "swc", null));
            libs.add(getDependency("org.apache.flex.framework", "rpc", flexVersion, "swc", null));
            libs.add(getDependency("org.apache.flex.framework", "spark", flexVersion, "swc", null));
        }
        return libs;
    }

    @Override
    public String getManifestPath() {
        File configsZip = getDependency("org.apache.flex.framework", "framework",
                System.getProperty("flexVersion"), "zip", "configs");
        File frameworkDir = configsZip.getParentFile();
        File unpackedConfigsDir = new File(frameworkDir, "configs_zip");
        // If the directory doesn't exist, we have to create it by unpacking the zip archive.
        // This is identical behaviour to Flexmojos, which does the same thing.
        if(!unpackedConfigsDir.exists()) {
            // TODO: Implement
        }
        return new File(unpackedConfigsDir, "mxml-2009-manifest.xml").getPath();
    }

    private File getDependency(String groupId, String artifactId, String version, String type, String classifier) {
        String dependencyPath = System.getProperty("mavenLocalRepoDir") + "/" + groupId.replaceAll("\\.", "/") + "/" +
                artifactId + "/" + version + "/" + artifactId + "-" + version +
                ((classifier != null) ? "-" + classifier : "") + "." + type;
        dependencyPath = FilenameNormalization.normalize(dependencyPath);
        File dependency = new File(dependencyPath);
        if(!dependency.exists()) {
            throw new RuntimeException("Could not read SWC dependency at " + dependency.getAbsolutePath());
        }
        return dependency;
    }

}
