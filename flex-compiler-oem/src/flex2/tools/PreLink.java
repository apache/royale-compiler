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

package flex2.tools;

import flex2.compiler.util.CompilerMessage;

/**
 * A flex2.compiler.PreLink implementation, which creates the FlexInit
 * and SystemManager subclass.
 *
 * @author Clement Wong
 * @author Roger Gonzalez (mixin, flexinit, bootstrap)
 * @author Basil Hosmer (service config)
 * @author Brian Deitte (font)
 * @author Cathy Murphy (accessibility)
 * @author Gordon Smith (i18n)
 */
public class PreLink// implements flex2.compiler.PreLink
{
    //private final static String DEFAULTS_CSS = "defaults.css";
    //private final static String DEFAULTS_DASH = "defaults-";
    //private final static String DOT_CSS = ".css";


    public static class CouldNotParseNumber extends CompilerMessage.CompilerError
    {
        private static final long serialVersionUID = 2186380089141871093L;

        public CouldNotParseNumber(String num, String attribute)
        {
            this.num = num;
            this.attribute = attribute;
        }

        public String num;
        public String attribute;
    }

    public static class MissingSignedLibraryDigest extends CompilerMessage.CompilerError
    {
        private static final long serialVersionUID = -1865860949469218550L;

        public MissingSignedLibraryDigest(String libraryPath)
        {
            this.libraryPath = libraryPath;
        }

        public String libraryPath;
    }

    public static class MissingUnsignedLibraryDigest extends CompilerMessage.CompilerError
    {
        private static final long serialVersionUID = 8092666584208136222L;

        public MissingUnsignedLibraryDigest(String libraryPath)
        {
            this.libraryPath = libraryPath;
        }

        public String libraryPath;
    }

	/**
	 *  Warn users with [RemoteClass] metadata that ends up mapping more than one class to the same alias. 
	 */
    public static class ClassesMappedToSameRemoteAlias extends CompilerMessage.CompilerWarning
    {
        private static final long serialVersionUID = 4365280637418299961L;
        
        public ClassesMappedToSameRemoteAlias(String className, String existingClassName, String alias)
        {
            this.className = className;
            this.existingClassName = existingClassName;
            this.alias = alias;
        }

        public String className;
        public String existingClassName;
        public String alias;
    }

    /**
     *  Tell the user they are making a mistake by compiling a module or application as a component. 
     */
    public static class CompiledAsAComponent extends CompilerMessage.CompilerWarning
    {
        private static final long serialVersionUID = -2874508107726441350L;

        public CompiledAsAComponent(String className, String mainDefinition)
        {
            this.className = className;
            this.mainDefinition = mainDefinition;
        }
        
        public String className;
        public String mainDefinition;

    }    
  
    /**
     *  "Required RSLs:" message. 
     */
    public static class RequiredRsls extends CompilerMessage.CompilerInfo
    {
        private static final long serialVersionUID = 2303666861783668660L;

        public RequiredRsls()
        {
        }

    }
    
    /**
     *  Display RSL URL with no failovers. 
     */
    public static class RequiredRslUrl extends CompilerMessage.CompilerInfo
    {
        private static final long serialVersionUID = 2303666861783668660L;

        public RequiredRslUrl(String rslUrl)
        {
            this.rslUrl = rslUrl;
        }

        public String rslUrl;
    }

    /**
     *  Display RSL URL with one failover. 
     */
    public static class RequiredRslUrlWithFailover extends CompilerMessage.CompilerInfo
    {
        private static final long serialVersionUID = 2303666861783668660L;

        public RequiredRslUrlWithFailover(String rslUrl)
        {
            this.rslUrl = rslUrl;
        }

        public String rslUrl;
    }

    /**
     *  Display RSL URL with more than one failovers. 
     */
    public static class RequiredRslUrlWithMultipleFailovers extends CompilerMessage.CompilerInfo
    {
        private static final long serialVersionUID = 2303666861783668660L;

        public RequiredRslUrlWithMultipleFailovers(String rslUrl, int failoverCount)
        {
            this.rslUrl = rslUrl;
            this.failoverCount = failoverCount;
        }
        
        public String rslUrl;
        public int failoverCount;

    }
    
}
