package org.apache.flex.compiler.tools.patchfiles;

import java.io.File;
import java.io.IOException;

/**
 * Created by christoferdutz on 16.03.16.
 */
public class DeleteFileOperation extends Operation {

    public DeleteFileOperation() {
    }

    @Override
    public void perform(File file) throws IOException {
        if(file.exists()) {
            if(!file.delete()) {
                throw new IOException("Could not delete file " + file.getPath());
            }
        }
    }

}
