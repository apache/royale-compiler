package org.apache.flex.utils;

import java.io.File;
import java.util.List;

/**
 * Created by christoferdutz on 23.02.16.
 */
public interface ITestAdapter {

    String getTempDir();

    List<File> getLibraries(boolean withFlex);

    String getManifestPath();

}
