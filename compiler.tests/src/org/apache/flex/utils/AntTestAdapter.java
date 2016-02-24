package org.apache.flex.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by christoferdutz on 23.02.16.
 */
public class AntTestAdapter implements ITestAdapter {

    private static EnvProperties env = EnvProperties.initiate();

    private static final File PLAYERGLOBAL_SWC = new File(FilenameNormalization.normalize(env.FPSDK + "\\" + env.FPVER + "\\playerglobal.swc"));
    // The Ant script for compiler.tests copies a standalone player to the temp directory.
    private static final File FLASHPLAYER = new File(FilenameNormalization.normalize(env.FDBG));

    private static final File LIBS_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs"));
    private static final File RESOURCE_BUNDLES_ROOT = new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US"));

    @Override
    public String getTempDir() {
        return FilenameNormalization.normalize("temp"); // ensure this exists
    }

    @Override
    public List<File> getLibraries(boolean withFlex) {
        // Do some checks if all needed environment variables are set.
        if (withFlex) {
            assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
        }
        assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set", env.FPSDK);

        // Create a list of libs needed to compile.
        List<File> libraries = new ArrayList<File>();
        libraries.add(getPlayerglobal());
        if (withFlex)
        {
            libraries.add(getArtifact("framework"));
            libraries.add(getArtifact("rpc"));
            libraries.add(getArtifact("spark"));
        }
        return libraries;
    }

    @Override
    public String getManifestPath() {
        return env.SDK + "\\frameworks\\mxml-2009-manifest.xml";
    }

    @Override
    public File getPlayerglobal() {
        return PLAYERGLOBAL_SWC;
    }

    @Override
    public File getFlashplayerDebugger() {
        return FLASHPLAYER;
    }

    @Override
    public File getArtifact(String artifactName) {
        return getLib(artifactName);
    }

    @Override
    public File getArtifactResourceBundle(String artifactName) {
        return getResourceBundle(artifactName);
    }

    private File getLib(String artifactId) {
        return new File(LIBS_ROOT, artifactId + ".swc");
    }

    private File getResourceBundle(String artifactId) {
        return new File(RESOURCE_BUNDLES_ROOT, artifactId + "_rb.swc");
    }

}
