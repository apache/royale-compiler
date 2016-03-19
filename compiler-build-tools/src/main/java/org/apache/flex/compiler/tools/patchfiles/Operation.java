package org.apache.flex.compiler.tools.patchfiles;

import java.io.File;
import java.io.IOException;

/**
 * Created by christoferdutz on 16.03.16.
 */
public abstract class Operation {

    public abstract void perform(File file) throws IOException;

}
