package org.apache.flex.utils;

import java.io.File;

/**
 * @author: Frederic Thomas
 * Date: 29/05/2015
 * Time: 17:04
 */
public class ConfigurationUtil {

    // workaround for Falcon bug.
    // Input files with relative paths confuse the algorithm that extracts the root class name.
    public static String[] fixArgs(final String[] args)
    {
        String[] newArgs = args;
        if (args.length > 1)
        {
            String targetPath = args[args.length - 1];
            if (targetPath.startsWith("."))
            {
                targetPath = FileUtils.getTheRealPathBecauseCanonicalizeDoesNotFixCase(new File(targetPath));
                newArgs = new String[args.length];
                for (int i = 0; i < args.length - 1; ++i)
                    newArgs[i] = args[i];
                newArgs[args.length - 1] = targetPath;
            }
        }
        return newArgs;
    }
}
