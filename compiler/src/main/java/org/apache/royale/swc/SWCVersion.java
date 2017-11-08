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

package org.apache.royale.swc;

/**
 * Version information of a SWC in catalog.xml file.
 */
public class SWCVersion implements ISWCVersion
{
    private String swcVersion;
    private String royaleVersion;
    private String royaleBuild;
    private String royaleMinSupportedVersion;
    private int royaleMinSupportedVersionInt;
    private String compilerName;
    private String compilerVersion;
    private String compilerBuild;

    /**
     * @return the swcVersion
     */
    @Override
    public String getSWCVersion()
    {
        return swcVersion;
    }

    /**
     * @param swcVersion the swcVersion to set
     */
    @Override
    public void setSWCVersion(String swcVersion)
    {
        this.swcVersion = swcVersion;
    }

    /**
     * @return the royaleVersion
     */
    @Override
    public String getRoyaleVersion()
    {
        return royaleVersion;
    }

    /**
     * @param royaleVersion the royaleVersion to set
     */
    @Override
    public void setRoyaleVersion(String royaleVersion)
    {
        this.royaleVersion = royaleVersion;
    }

    /**
     * @return the royaleBuild
     */
    @Override
    public String getRoyaleBuild()
    {
        return royaleBuild;
    }

    /**
     * @param royaleBuild the royaleBuild to set
     */
    @Override
    public void setRoyaleBuild(String royaleBuild)
    {
        this.royaleBuild = royaleBuild;
    }

    /**
     * @return the royaleMinSupportedVersion
     */
    @Override
    public String getRoyaleMinSupportedVersion()
    {
        return royaleMinSupportedVersion;
    }

    /**
     * @param royaleMinSupportedVersion the royaleMinSupportedVersion to set
     */
    @Override
    public void setRoyaleMinSupportedVersion(String royaleMinSupportedVersion)
    {
        this.royaleMinSupportedVersion = royaleMinSupportedVersion; 
        this.royaleMinSupportedVersionInt = royaleVersionStringToInt(royaleMinSupportedVersion);
    }

    @Override
    public int getRoyaleMinSupportedVersionInt()
    {
        return this.royaleMinSupportedVersionInt;
    }
    
    /**
     * Convert a royale version String to an integer.
     * 
     * @param versionString
     * @return The string as a version. -1 if versionString is null.
     * 
     */
    private static int royaleVersionStringToInt(final String versionString)
    {
        if (versionString == null)
            return -1;
        String results[] = versionString.split("\\.");
        int major = 0;
        int minor = 0;
        int revision = 0;
        
        assert results.length < 4;
        
        int n = results.length;
        for (int i = 0; i < n; i++)
        {
            if (i == 0)
                major = Integer.parseInt(results[0]);
            else if (i == 1)
                minor = Integer.parseInt(results[1]);
            else if (i == 2)
                revision = Integer.parseInt(results[2]);
        }
        
        int version = (major << 24) + (minor << 16) + revision;
        return version;
    }

    @Override
    public String getCompilerName()
    {
        return compilerName;
    }

    @Override
    public void setCompilerName(String value)
    {
        compilerName = value;
    }

    @Override
    public String getCompilerVersion()
    {
        return compilerVersion;
    }

    @Override
    public void setCompilerVersion(String value)
    {
        compilerVersion = value;
    }

    @Override
    public String getCompilerBuild()
    {
        return compilerBuild;
    }

    @Override
    public void setCompilerBuild(String value)
    {
        compilerBuild = value;
    }
}
