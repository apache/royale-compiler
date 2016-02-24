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
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK + "\\" + env.FPVER + "\\playerglobal.swc")));
        if (withFlex)
        {
            libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\framework.swc")));
            libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\rpc.swc")));
            libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\spark.swc")));
        }
        return libraries;
    }

    @Override
    public String getManifestPath() {
        return env.SDK + "\\frameworks\\mxml-2009-manifest.xml";
    }

}
