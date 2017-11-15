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

package org.apache.royale.compiler.internal.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Capture the information in one argument specifing
 * -runtime-shared-libraries-path information.
 */
public class RuntimeSharedLibraryPathInfo
{
    /**
     * The extension given to a signed RLS that is assumed to be signed.
     * Unsigned RSLs should use the standard "swf" extension.
     */
    public static final String SIGNED_RSL_URL_EXTENSION = "swz";
    public static final String SIGNED_RSL_URL_DOT_EXTENSION = "." + SIGNED_RSL_URL_EXTENSION;

    // path to swc to link against, this is logically added
    // -external-library-path option
    private String swcPath;
    private File swcVf; // the swc's virtual file		

    // rsls in the order to load. The first if the primary rsl, the
    // others are failover rsls.
    private List<String> rslURLs;

    // policy file urls, optional. The first in the list if applies to the
    // first rsl in _rslURLs. The second in the list applies to the second
    // in _rslURLs and so on. If there are more policy file urls than rsl
    // urls,
    // then display a warning.
    private List<String> policyFileURLs;

    //
    // List of type Boolean. Entry i in this list tells if entry i in the list
    // given by getRSLURLs() is targeting a signed or unsigned rsl.
    //
    private List<Boolean> isSignedList;

    /**
     * Create a new cross-domain RSL entry. The info specified the swc file
     * to exclude put a list of RSL URLs and policy file URLs. The first RSL
     * url/policy file URL pair are the primary URLs. The remaining URLs are
     * failovers and are only used if the primary RSL fails to load.
     */
    public RuntimeSharedLibraryPathInfo()
    {
        rslURLs = new ArrayList<String>();
    }

    /**
     * Test is the url is signed.
     * 
     * @param url url to test, the file specified by the url does not need
     * to exist.
     * @return true if the url specifies a signed rsl, false otherwise.
     */
    public boolean isRSLURLSigned(String url)
    {
        if (url == null)
        {
            return false;
        }

        return url.endsWith(SIGNED_RSL_URL_DOT_EXTENSION);
    }

    /**
     * Set the path to the SWC.
     * 
     * @param swcPath The path to the SWC.
     */
    public void setSWCPath(String swcPath)
    {
        this.swcPath = swcPath;
    }

    /**
     * @return the path to the SWC.
     */
    public String getSWCPath()
    {
        return swcPath;
    }

    /**
     * Set the virtual file associated with the SWC path.
     * 
     * @param vf The virtual file.
     */
    public void setSWCFile(File vf)
    {
        swcVf = vf;
    }

    /**
     */
    public File getSWCFile()
    {
        return swcVf;
    }

    /**
     * Add an RSL to the list of RSLs.
     * 
     * @param url url of the RSL, may not be null
     */
    public void addRSLURL(String url)
    {
        if (url == null)
        {
            throw new NullPointerException("url may not be null");
        }

        rslURLs.add(url);
        addSignedFlag(isRSLURLSigned(url));
    }

    /**
     * @return List of urls to RSLs. Each entry in the list is of type
     * <code>String</code>.
     */
    public List<String> getRSLURLs()
    {
        return rslURLs;
    }

    /**
     * Add a policy file to support the associated entry in the RSL URL
     * list. Policy file entries my be empty, but must be specified.
     * 
     * @param url url of the policy file.
     */
    public void addPolicyFileURL(String url)
    {
        if (policyFileURLs == null)
        {
            policyFileURLs = new ArrayList<String>();
        }

        policyFileURLs.add(url == null ? "" : url);
    }

    /**
     * Get the list of policy files.
     * 
     * @return Listof policy file URLs. Each entry in the list of type
     * <code>String</code>
     */
    public List<String> getPolicyFileURLs()
    {
        return policyFileURLs == null ? Collections.<String> emptyList() : policyFileURLs;
    }

    /**
     * Return a list of booleans that indicate if an RSL URL is signed or
     * unsigned. There is a matching entry is this list for every entry in
     * the RSL URL list.
     * 
     * @return List of boolean signed flags for the RSL URL list. Each entry
     * in the list is of type <code>Boolean</code>.
     */
    public List<Boolean> getSignedFlags()
    {
        return isSignedList;
    }

    /**
     * Add a signed flag to the list of flags. This flag is determines if
     * the RSL URL associated with this entry is considered signed or
     * unsigned.
     * 
     * @param isSigned true if the RSL URL is signed.
     */
    private void addSignedFlag(boolean isSigned)
    {
        if (isSignedList == null)
        {
            isSignedList = new ArrayList<Boolean>();
        }

        isSignedList.add(Boolean.valueOf(isSigned));
    }

}
