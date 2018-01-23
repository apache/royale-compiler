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

package org.apache.royale.compiler.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.filespecs.IFileSpecification;

/**
 * The runtime RSL settings for a given library. The specified library will 
 * have all of its symbols removed from the application. The runtime RSL
 * settings are written in the root class of the application and read in 
 * frame1 of application.
 */
public class RSLSettings
{
    /**
     * A RSL URL and a policy file URL.
     */
    public static class RSLAndPolicyFileURLPair
    {
        /**
         * Create a new RSL URL and Policy File URL pair.
         * 
         * @param rslURL THe URL of the runtime shared library.
         * @param policyFileURL The URL of the policy file.
         */
        public RSLAndPolicyFileURLPair(String rslURL, String policyFileURL)
        {
            this.rslURL = rslURL;
            this.policyFileURL = policyFileURL;
        }

        private String rslURL;
        private String policyFileURL;
        
        /**
         * @return the url of the RSL to load.
         */
        public String getRSLURL()
        {
            return rslURL;
        }

        /**
         * @return the url of the policy file.
         */
        public String getPolicyFileURL()
        {
            return policyFileURL;
        }
    }

    /**
     * The extension given to a signed RLS that is assumed to be signed.
     * Unsigned RSLs should use the standard "swf" extension.
     */
    private static final String SIGNED_RSL_URL_EXTENSION = "swz";
    private static final String SIGNED_RSL_URL_DOT_EXTENSION = "." + SIGNED_RSL_URL_EXTENSION;
    
    /**
     * Test if the url is a signed RSL. Signed RSL have a .swz extension.
     * 
     * @param url url to test, the file specified by the url does not 
     *            need to exist.
     * @return true if the url specifies a signed rsl, false otherwise.
     */
    public static boolean isSignedRSL(String url)
    {
        if (url == null)
            return false;
        
        return url.endsWith(SIGNED_RSL_URL_DOT_EXTENSION);
    }

    /**
     * Create RSLSettings with:
     *  - a default {@link ApplicationDomainTarget}
     *  - verify digest set to true 
     *  
     *  @param libraryFile the library whose classes will be removed
     *  from the application. May not be null.
     *  @throws NullPointerException if libraryFile is null.
     */
    RSLSettings(IFileSpecification libraryFile)
    {
        if (libraryFile == null)
            throw new NullPointerException("libraryFile may not be null");
        
        this.libraryFile = new File(libraryFile.getPath());
        rslURLs = new ArrayList<RSLAndPolicyFileURLPair>();
        setApplicationDomain(ApplicationDomainTarget.DEFAULT);
        setVerifyDigest(true);
    }

    /**
     * Create RSLSettings with:
     *  - a default {@link ApplicationDomainTarget}
     *  - verify digest set to true 
     *  
     *  @param libraryFile the library whose classes will be removed
     *  from the application. May not be null.
     *  @throws NullPointerException if libraryFile is null.
     */
    public RSLSettings(File libraryFile)
    {
        if (libraryFile == null)
            throw new NullPointerException("libraryFile may not be null");
        
        this.libraryFile = libraryFile;
        rslURLs = new ArrayList<RSLAndPolicyFileURLPair>();
        setApplicationDomain(ApplicationDomainTarget.DEFAULT);
        setVerifyDigest(true);
    }

    private File libraryFile;   // the library whose definitions are externed
    private List<RSLAndPolicyFileURLPair> rslURLs; // list of rsls and failovers
    private ApplicationDomainTarget applicationDomain;  
    private boolean verifyDigest;   // if true the digest will be verified at runtime
    private boolean forceLoad;      // true if the RSL should be forced to load regardless of its use
    
    /**
     * @return true if the RSL should be force loaded, false otherwise.
     */
    public boolean isForceLoad()
    {
        return forceLoad;
    }

    /**
     * Sets a flag on the RSL so the compiler is not allowed to remove it when
     * the "remove unused RSLs" feature is on. 
     * 
     * @param forceLoad true to force the RSL to be loaded at runtime, false otherwise.
     */
    public void setForceLoad(boolean forceLoad)
    {
        this.forceLoad = forceLoad;
    }

    /**
     * @return a List of {@link RSLAndPolicyFileURLPair}
     */
    public List<RSLAndPolicyFileURLPair> getRSLURLs()
    {
        return rslURLs;
    }

    /**
     * Add a new RSL URL and Policy file URL. This first pair is the primary
     * RSL and the following RSLs are failover RSLs.
     * 
     * @param rslURL A String representing the URL to load the RSL from. May
     * not be null. 
     * @param policyFileURL A String representing the URL to load a policy file
     * from. This is optional and may be null to indicate there is no policy 
     * file.
     * @throws NullPointerException if rslURL is null.
     */
    public void addRSLURLAndPolicyFileURL(String rslURL, String policyFileURL)
    {
       if (rslURL == null)
           throw new NullPointerException("rslURL may not be null");
       
       rslURLs.add(new RSLAndPolicyFileURLPair(rslURL, policyFileURL)); 
    }
    
    /**
     * @return the libraryFile
     */
    public File getLibraryFile()
    {
        return libraryFile;
    }

    /**
     * @param applicationDomain the new value of the applicationDomain.
     */
    public void setApplicationDomain(ApplicationDomainTarget applicationDomain)
    {
        this.applicationDomain = applicationDomain;
    }

    /**
     * One of {@link ApplicationDomainTarget} that control which domain an RSL
     * is loaded into.
     * 
     * @return the applicationDomain
     */
    public ApplicationDomainTarget getApplicationDomain()
    {
        return applicationDomain;
    }

    /**
     * Change the value of the verify digests flag.
     * 
     * @param verifyDigest The new value of the verify digests flag.
     */
    public void setVerifyDigest(boolean verifyDigest)
    {
        this.verifyDigest = verifyDigest;
    }

    /**
     * @return if true, the RSL's digest must be verified at runtime.
     */
    public boolean getVerifyDigest()
    {
        return verifyDigest;
    }
    
    
}
