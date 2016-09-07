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

package flex2.compiler;

import java.io.File;
import java.io.IOException;

import flex2.compiler.common.Configuration;
import flex2.compiler.common.LocalFilePathResolver;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.util.ConsoleLogger;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.compiler.common.PathResolver;
import flex2.compiler.util.URLPathResolver;

/**
 * This class orchestrates delegation to the subcompilers using
 * batch1() when -conservative is true or batch2(), the default.  It
 * also handles common tasks like validating CompilationUnit's before
 * an incremental compilation, resolving dependences, loading a cache
 * from a previous compilation, and storing a compilation cache.
 *
 * @author Clement Wong
 */
public final class CompilerAPI
{
    //private final static int INHERITANCE = 1;
    //private final static int NAMESPACES = 2;
    //private final static int TYPES = 3;
    //private final static int EXPRESSIONS = 4;


    public static void useAS3()
    {
        // do this so there is no need to start java with -DAS3 and -DAVMPLUS...
        // this will likely not work in server environment.
        System.setProperty("AS3", "");
        System.setProperty("AVMPLUS", "");
    }

	public static void useConsoleLogger()
	{
		useConsoleLogger(true, true, true, true);
	}

	public static void useConsoleLogger(boolean isInfoEnabled, boolean isDebugEnabled, boolean isWarningEnabled, boolean isErrorEnabled)
	{
		ThreadLocalToolkit.setLogger(new ConsoleLogger(isInfoEnabled, isDebugEnabled, isWarningEnabled, isErrorEnabled));
	}

	public static void usePathResolver()
	{
		usePathResolver(null);
	}

	public static void usePathResolver(SinglePathResolver resolver)
	{
		PathResolver pathResolver = new PathResolver();
		if (resolver != null)
		{
			pathResolver.addSinglePathResolver(resolver);
		}
		pathResolver.addSinglePathResolver( LocalFilePathResolver.getSingleton() );
		pathResolver.addSinglePathResolver( URLPathResolver.getSingleton() );
		ThreadLocalToolkit.setPathResolver(pathResolver);
	}

	public static void removePathResolver()
	{
		ThreadLocalToolkit.setPathResolver(null);
		ThreadLocalToolkit.resetResolvedPaths();
	}

	static String constructClassName(String namespaceURI, String localPart)
	{
		return (namespaceURI.length() == 0) ? localPart : new StringBuilder(namespaceURI.length() + localPart.length() + 1).append(namespaceURI).append(":").append(localPart).toString();
	}
    public static VirtualFile getVirtualFile(String path) throws ConfigurationException
    {
        return getVirtualFile(path, true);
    }

    /**
     * Create virtual file for given file and throw configuration exception if not possible
     */
    public static VirtualFile getVirtualFile(String path, boolean reportError) throws ConfigurationException
    {
        VirtualFile result = null;
        File file = new File(path);

        if (file.exists())
        {
            try
            {
                result = new LocalFile(file.getCanonicalFile());
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            PathResolver resolver = ThreadLocalToolkit.getPathResolver();
            result = resolver.resolve(path);

            if (result == null && reportError)
            {
                throw new ConfigurationException.IOError(path);
            }
        }

        return result;
    }

}
