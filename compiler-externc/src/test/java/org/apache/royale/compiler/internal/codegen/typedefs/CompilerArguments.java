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

package org.apache.royale.compiler.internal.codegen.typedefs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Schmalle
 */
public class CompilerArguments
{
    private List<String> bundles = new ArrayList<String>();

    private List<String> libraries = new ArrayList<String>();

    private List<String> sources = new ArrayList<String>();

    private List<String> includes = new ArrayList<String>();

    private String sdkPath = "";

    private String appName = "";

    private String output;

    private String jsLibraryPath = "";

    private String jsBasePath = "";

    private Boolean jsOutputAsFiles = null;

    //    private List<MergedFileSettings> jsMergedFiles = new ArrayList<MergedFileSettings>();

    public void addBundlePath(String path)
    {
        if (bundles.contains(path))
            return;
        bundles.add(path);
    }

    public void addLibraryPath(String path)
    {
        if (libraries.contains(path))
            return;
        libraries.add(path);
    }

    public void addSourcepath(String path)
    {
        if (sources.contains(path))
            return;
        sources.add(path);
    }

    public void addIncludedSources(String path)
    {
        if (includes.contains(path))
            return;
        includes.add(path);
    }

    public List<String> getBundles()
    {
        return bundles;
    }

    public List<String> getLibraries()
    {
        return libraries;
    }

    public List<String> getSources()
    {
        return sources;
    }

    public List<String> getIncludes()
    {
        return includes;
    }

    public String getSDKPath()
    {
        return sdkPath;
    }

    public void setSDKPath(String value)
    {
        sdkPath = value;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String value)
    {
        appName = value;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String path)
    {
        output = path;
    }

    public String getJsLibraryPath()
    {
        return jsLibraryPath;
    }

    public void setJsLibraryPath(String jsLibraryPath)
    {
        this.jsLibraryPath = jsLibraryPath;
    }

    public String getJsBasePath()
    {
        return jsBasePath;
    }

    public void setJsBasePath(String jsBasePath)
    {
        this.jsBasePath = jsBasePath;
    }

    public Boolean isJsOutputAsFiles()
    {
        return jsOutputAsFiles;
    }

    public void setJsOutputAsFiles(Boolean jsOutputAsFiles)
    {
        this.jsOutputAsFiles = jsOutputAsFiles;
    }

    //    public void addJsMergedFiles(MergedFileSettings setting)
    //    {
    //        jsMergedFiles.add(setting);
    //    }

    //--------------------------------------------------------------------------
    // SWC
    //--------------------------------------------------------------------------

    private List<String> metadatas = new ArrayList<String>();

    // -keep-as3-metadata
    public List<String> getKeepMetadata()
    {
        return metadatas;
    }

    public void addKeepMetadata(String metadata)
    {
        if (metadatas.contains(metadata))
            return;
        metadatas.add(metadata);
    }

    //--------------------------------------------------------------------------
    // Doc
    //--------------------------------------------------------------------------

    private List<String> docMembers = new ArrayList<String>();

    private List<String> docNamespace = new ArrayList<String>();

    private String mainTitle;

    public String getMainTitle()
    {
        return mainTitle;
    }

    public void setMainTitle(String value)
    {
        mainTitle = value;
    }

    private String footer;

    public String getFooter()
    {
        return footer;
    }

    public void setFooter(String value)
    {
        footer = value;
    }

    public void addDocMember(String member)
    {
        if (docMembers.contains(member))
            return;
        docMembers.add(member);
    }

    public void addDocNamespace(String namespace)
    {
        if (docNamespace.contains(namespace))
            return;
        docNamespace.add(namespace);
    }

    public void clear()
    {
        jsBasePath = "";
        jsLibraryPath = "";
        jsOutputAsFiles = false;
        output = "";
        clearLibraries();
        clearSourcePaths();
        includes.clear();
        //        jsMergedFiles.clear();
    }

    public void clearSourcePaths()
    {
        sources = new ArrayList<String>();
    }

    public void clearBundles()
    {
        bundles = new ArrayList<String>();
    }

    public void clearLibraries()
    {
        libraries = new ArrayList<String>();
    }

    public List<String> toArguments()
    {
        List<String> result = new ArrayList<String>();

        for (String arg : bundles)
        {
            result.add("-bundle-path=" + arg);
        }

        for (String arg : libraries)
        {
            result.add("-library-path=" + arg);
        }

        for (String arg : sources)
        {
            result.add("-source-path=" + arg);
        }

        if (includes.size() > 0)
        {
            result.add("-include-sources=" + join(includes, ","));
        }
        if (metadatas.size() > 0)
        {
            result.add("-keep-as3-metadata=" + join(metadatas, ","));
        }

        //        if (jsMergedFiles.size() > 0)
        //        {
        //            final StringBuilder sb = new StringBuilder();
        //            for (MergedFileSettings setting : jsMergedFiles)
        //            {
        //                sb.append("-js-merged-file=");
        //                sb.append(setting.getFileName());
        //                sb.append(",");
        //                sb.append(StringUtils.join(
        //                        setting.getQualifiedNames().toArray(new String[] {}),
        //                        ","));
        //                result.add(sb.toString());
        //                sb.setLength(0);
        //            }
        //        }

        String sdk = getSDKPath();
        if (sdk != null && !sdk.equals(""))
            result.add("-sdk-path=" + sdk);

        String name = getAppName();
        if (name != null && !name.equals(""))
            result.add("-app-name=" + name);

        String base = getJsBasePath();
        if (!base.equals(""))
            result.add("-js-base-path=" + base);

        String library = getJsLibraryPath();
        if (!library.equals(""))
            result.add("-js-library-path=" + library);

        if (isJsOutputAsFiles() != null)
        {
            result.add("-js-classes-as-files="
                    + (isJsOutputAsFiles() ? "true" : "false"));
        }

        result.add("-output=" + getOutput());

        addArguments(result);

        return result;
    }

    protected void addArguments(List<String> arguments)
    {
        String mainTitle = getMainTitle();
        if (mainTitle != null && !mainTitle.equals(""))
            arguments.add("-main-title=" + mainTitle);

        if (footer != null && !footer.equals(""))
            arguments.add("-footer=" + footer);

        if (docMembers.size() > 0)
        {
            arguments.add("-doc-member=" + join(docMembers, ","));
        }

        if (docNamespace.size() > 0)
        {
            arguments.add("-doc-namespace=" + join(docNamespace, ","));
        }
    }

    private String join(List<String> items, String separator)
    {
        StringBuilder sb = new StringBuilder();
        int len = items.size();
        int index = 0;
        for (String item : items)
        {
            sb.append(item);
            if (index < len)
                sb.append(separator);
            index++;
        }
        return sb.toString();
    }

    public static class CompilerArgument
    {
        private String name;

        private String value;

        CompilerArgument(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public static CompilerArgument create(String name, String value)
        {
            return new CompilerArgument(name, value);
        }
    }

    @Override
    public String toString()
    {
        return join(toArguments(), " ");
    }
}
