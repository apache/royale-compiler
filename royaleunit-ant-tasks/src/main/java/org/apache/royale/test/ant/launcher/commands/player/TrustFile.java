/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.royale.test.ant.launcher.commands.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.royale.test.ant.LoggingUtil;

public class TrustFile
{
    public static final String TRUST_FILENAME = "royaleUnit.cfg";

    private File trustDirectory;
    private File trustFile;
    private List<String> paths;

    public TrustFile(Project project, File userTrustDirectory, File globaTrustDirectory)
    {
        // determine which trust directory to use
        this.trustDirectory = userTrustDirectory != null ? userTrustDirectory : globaTrustDirectory;

        // create it if it doesn't exist
        if (!this.trustDirectory.exists())
        {
            try
            {
                trustDirectory.mkdirs();
            }
            catch (Exception e)
            {
                throw new BuildException("Could not create Flash Player trust directory at [" + trustDirectory.getAbsolutePath() + "]; permission denied.");
            }
        }

        // locate trust file
        this.trustFile = project.resolveFile(trustDirectory.getAbsolutePath() + "/" + TRUST_FILENAME);

        // parse trust file contents
        this.paths = read();
    }

    private List<String> read()
    {
        List<String> paths = new ArrayList<String>();

        if (trustFile.exists())
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(trustFile));
                String path = null;

                while ((path = reader.readLine()) != null)
                {
                    paths.add(path);
                }

                reader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return paths;
    }

    public void add(String url) 
    {
        String path = new File(url).getParentFile().getAbsolutePath();
        addPath(path);
    }
    
    public void add(File swf)
    {
        String path = swf.getParentFile().getAbsolutePath();
        addPath(path);
    }
    
    private void addPath(String path)
    {
        // create the appropriate FP trust directory is it doesn't exist
        if (!trustDirectory.exists())
        {
            trustDirectory.mkdir();
        }

        // Add path if it doesn't exist
        if (!paths.contains(path))
        {
            paths.add(path);

            // Write file
            write();

            LoggingUtil.log("Updated local trust file at [" + trustFile.getAbsolutePath() + "], added [" + path + "].");
        }
        else
        {
            LoggingUtil.log("Entry [" + path + "] already available in local trust file at [" + trustFile.getAbsolutePath() + "].");
        }	   
    }

    private void write()
    {
        try
        {
            FileWriter writer = new FileWriter(trustFile, false);

            for (String path : paths)
            {
                writer.write(path + System.getProperty("line.separator"));
            }

            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void remove(String url) {
        String path = new File(url).getParentFile().getAbsolutePath();
        removePath(path); 
    }
    
    public void remove(File swf)
    {
        // remove path if exists
        String path = swf.getParentFile().getAbsolutePath();
        removePath(path);
    }
    
    public void removePath(String path) 
    {
        if (paths.contains(path))
        {
            paths.remove(path);

            // write out new copy of file
            write();

            LoggingUtil.log("Updated local trust file at [" + trustFile.getAbsolutePath() + "], removed [" + path + "].");
        }	   
    }
    
}