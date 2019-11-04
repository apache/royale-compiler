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

package org.apache.royale.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;

/**
 * A collection of file related utilities.
 */
public final class FileUtils
{
    public static String canonicalPath(File file)
    {
        return canonicalFile(file).getPath();
    }

    public static File canonicalFile(File file)
    {
        try
		{
			return file.getCanonicalFile();
		}
		catch (IOException e)
		{
			return file.getAbsoluteFile();
		}
    }

    private static HashMap<String, String> filemap = null;
    private static boolean checkCase = false;

    /**
     * Canonicalize on Win32 doesn't fix the case of the file to match what is on disk.
     * Its annoying that this exists.  It will be very slow until the server stabilizes.
     * If this is called with a pattern where many files from the same directory will be
     * needed, then the cache should be changed to hold the entire directory contents
     * and check the modtime of the dir.  It didn't seem like this was worth it for now.
     * @param f A file.
     */
    public static synchronized String getTheRealPathBecauseCanonicalizeDoesNotFixCase( File f )
    {
        if (filemap == null)
        {
            filemap = new HashMap<String, String>();
            checkCase = System.getProperty("os.name").toLowerCase().startsWith("windows");
        }

        String path = FileUtils.canonicalPath( f );

        if (!checkCase || !f.exists())    // if the file doesn't exist, then we can't do anything about it.
            return path;

        // We're going to ignore the issue where someone changes the capitalization of a file on the fly.
        // If this becomes an important issue we'll have to make this cache smarter.

        if (filemap.containsKey( path ))
            return filemap.get( path );

        String file = f.getName();

        File canonfile = new File(path);

        File dir = new File(canonfile.getParent());

		// removing dir.listFiles() because it is not supproted in .NET
		String[] ss = dir.list();
        if (ss != null)
        {
            int n = ss.length;
            File[] files = new File[n];
            for (int i = 0; i < n; i++)
            {
                files[i] = new File(dir.getPath(), ss[i]);
            }

            for (int i = 0; i < files.length; ++i)
            {
                if (files[i].getName().equalsIgnoreCase( file ))
                {
                    filemap.put( path, files[i].getAbsolutePath() );
                    return files[i].getAbsolutePath();
                }
            }
        }
        // If we got here, it must be because we can't read the directory?
        return path;
    }

    public static URI toURI(File f) throws URISyntaxException
    {
        String s = f.getAbsolutePath();
        if (File.separatorChar != '/')
        {
            s = s.replace(File.separatorChar, '/');
        }
        if (!s.startsWith("/"))
        {
            s = "/" + s;
        }
        if (!s.endsWith("/") && f.isDirectory())
        {
            s = s + "/";
        }
        return new URI("file", s, null);
    }

	public static String addPathComponents(String p1, String p2, char sepchar)
    {
        if (p1 == null)
            p1 = "";
        if (p2 == null)
            p2 = "";

        int r1 = p1.length() - 1;

        while ((r1 >= 0) && ((p1.charAt( r1 ) == sepchar)))
            --r1;

        int r2 = 0;
        while ((r2 < p2.length()) && (p2.charAt( r2 ) == sepchar ))
            ++r2;

        String left = p1.substring( 0, r1 + 1 );
        String right = p2.substring( r2 );

        String sep = "";
        if ((left.length() > 0) && (right.length() > 0))
            sep += sepchar;

        return left + sep + right;
    }

    public static byte[] toByteArray(InputStream in)
    {
        DAByteArrayOutputStream baos = new DAByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        int num = 0;
        InputStream inputStream = new BufferedInputStream(in);
        try
        {
            while ((num = inputStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, num);
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("FileUtils waiting for lock in toByteArray");
            //byte[] b = 
            baos.getDirectByteArray();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("FileUtils waiting for lock in toByteArray");
        }
        catch (IOException ex)
        {
            if (Trace.error)
                 ex.printStackTrace();
            
            // TODO Do we really want to swallow this IOException?
        }
        finally
        {
            IOUtils.closeQuietly(baos);
            
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException ex)
            {
            }
        }

        return new byte[0];
    }

    /**
     * returns whether the file is absolute
     * if a security exception is thrown, always returns false
     */
    public static boolean isAbsolute(File f)
    {
        boolean absolute = false;
        try
        {
            absolute = f.isAbsolute();
        }
        catch (SecurityException se)
        {
            if (Trace.pathResolver)
            {
                Trace.trace(se.getMessage());
            }
        }

        return absolute;
    }
}
