package org.apache.flex.compiler.tools.patchfiles;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by christoferdutz on 16.03.16.
 */
public class CopyFileOperation extends Operation {

    private File targetDirectory;

    public CopyFileOperation() {
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public void perform(File file) throws IOException {
        FileUtils.copyFile(file, new File(targetDirectory, file.getName()));
    }

}
