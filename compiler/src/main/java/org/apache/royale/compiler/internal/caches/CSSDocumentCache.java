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

package org.apache.royale.compiler.internal.caches;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.caches.ICSSDocumentCache;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSFontFace;
import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import org.apache.royale.compiler.css.ICSSNode;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.internal.css.CSSDocument;
import org.apache.royale.compiler.internal.css.CSSModelTreeType;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.io.SWCReader;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.ImmutableList;

/**
 * Cache for {@link ICSSDocument} at workspace level. The CSS model can be a
 * "defaults.css" file inside a SWC library, or a CSS file on the disk.
 * <p>
 * The cache key is normalized path to the SWC file (optional) and the CSS file
 * name inside the SWC. The cache value is an {@link ICSSDocument}.
 */
public class CSSDocumentCache extends ConcurrentCacheStoreBase<ICSSDocument> implements ICSSDocumentCache
{

    /**
     * Since {@link ConcurrentCacheStoreBase#get} doesn't return compiler
     * problems, when there's problem parsing CSS file in
     * {@link #createEntryValue}, we have to throw a runtime exception to pass
     * the compiler problems to the caller of the cache store.
     */
    public static class ProblemParsingCSSRuntimeException extends RuntimeException
    {
        private static final long serialVersionUID = 156921800741800866L;

        public ProblemParsingCSSRuntimeException(final Collection<ICompilerProblem> problems)
        {
            super();
            this.cssParserProblems = problems;
        }

        /**
         * A collection of compiler problems from parsing the CSS file.
         */
        public final Collection<ICompilerProblem> cssParserProblems;
    }

    /**
     * Since {@link ConcurrentCacheStoreBase} does not allow null values, when a
     * SWC library does not have a "defaults.css" file, this dummy value is
     * used.
     */
    public static final ICSSDocument EMPTY_CSS_DOCUMENT = new ICSSDocument()
    {
        @Override
        public ImmutableList<ICSSRule> getRules()
        {
            return ImmutableList.of();
        }

        @Override
        public ICSSNamespaceDefinition getNamespaceDefinition(String prefix)
        {
            return null;
        }

        @Override
        public ImmutableList<ICSSFontFace> getFontFaces()
        {
            return ImmutableList.of();
        }

        @Override
        public ICSSNamespaceDefinition getDefaultNamespaceDefinition()
        {
            return null;
        }

        @Override
        public ImmutableList<ICSSNamespaceDefinition> getAtNamespaces()
        {
            return ImmutableList.of();
        }

        @Override
        public String toStringTree()
        {
            return null;
        }

        @Override
        public int getArity()
        {
            return 0;
        }

        @Override
        public ICSSNode getNthChild(int index)
        {
            throw new IllegalStateException();
        }

        @Override
        public CSSModelTreeType getOperator()
        {
            throw new IllegalStateException();
        }

        @Override
        public String getSourcePath()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getStart()
        {
            return 0;
        }

        @Override
        public int getEnd()
        {
            return 0;
        }

        @Override
        public int getLine()
        {
            return 0;
        }

        @Override
        public int getColumn()
        {
            return 0;
        }

        @Override
        public int getEndLine()
        {
            return 0;
        }

        @Override
        public int getEndColumn()
        {
            return 0;
        }

        @Override
        public int getAbsoluteStart()
        {
            return 0;
        }

        @Override
        public int getAbsoluteEnd()
        {
            return 0;
        }

    };

    private abstract static class CSSDocumentCacheKeyBase extends CacheStoreKeyBase
    {
        abstract ICSSDocument parse() throws IOException;
    }
    
    /**
     * Key object for {@code CSSDocumentCache}. It the combination of a
     * normalized SWC file path and the CSS file inside the SWC. If the
     * {@code swcFile} is null, the {@code cssFileName} points to a CSS disk
     * file.
     */
    public static class CSSDocumentCacheKey extends CSSDocumentCacheKeyBase
    {
        public final ISWC swc;
        public final String cssFileName;

        public CSSDocumentCacheKey(final ISWC swc, final String cssFileName)
        {
            assert cssFileName != null : "CSS file name can't be null.";
            this.swc = swc;
            this.cssFileName = cssFileName;
        }

        @Override
        public String generateKey()
        {
            return String.format(
                        "%s:%s",
                        FilenameNormalization.normalize(swc.getSWCFile()).getAbsolutePath(),
                        cssFileName);
        }

        /**
         * Parse a CSS file in a SWC library into {@link ICSSDocument} model. If the
         * CSS file does not exist, returns {@link #EMPTY_CSS_DOCUMENT} dummy
         * object.
         * 
         * @throws IOException IO error.
         */
        @Override
        ICSSDocument parse() throws IOException
        {
            final ZipFile zipFile = new ZipFile(swc.getSWCFile(), ZipFile.OPEN_READ);
            ICSSDocument result = EMPTY_CSS_DOCUMENT;
            InputStream input = null;
            try
            {
                input = SWCReader.getInputStream(zipFile, cssFileName);
                if (input != null)
                {
                    final ANTLRInputStream in = new ANTLRInputStream(input);
                    in.name = String.format("%s:%s", swc.getSWCFile().getName(), cssFileName);
                    final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
                    result = CSSDocument.parse(in, problems);
                    if (!problems.isEmpty())
                        throw new ProblemParsingCSSRuntimeException(problems);
                }
            }
            finally
            {
                IOUtils.closeQuietly(input);
                zipFile.close();
            }
            return result;
        }
    }
    
    /**
     * Key object for {@code CSSDocumentCache}. It the combination of a
     * normalized SWC file path and the CSS file inside the SWC. If the
     * {@code swcFile} is null, the {@code cssFileName} points to a CSS disk
     * file.
     */
    protected static class CSSDocumentCacheKey2 extends CSSDocumentCacheKeyBase
    {
        protected final String cssFileName; // non-null

        public CSSDocumentCacheKey2(final String cssFileName)
        {
            assert cssFileName != null : "CSS file name can't be null.";
            this.cssFileName = cssFileName;
        }

        @Override
        public String generateKey()
        {
            return cssFileName;
        }

        /**
         * parse a bare CSS file on the file system.
         */
        @Override
        ICSSDocument parse() throws IOException
        {
            final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
            final CSSDocument css = CSSDocument.parse(new ANTLRFileStream(cssFileName), problems);
            if (!problems.isEmpty())
                throw new ProblemParsingCSSRuntimeException(problems);
            if (css != null)
                return css;
            return EMPTY_CSS_DOCUMENT;
        }
    }

    /**
     * Create a cache key for {@code CSSDocumentCache} that references
     * a CSS file in a SWC.
     * 
     * @param swc SWC file
     * @param cssFileName CSS file name
     * @return Key for {@code CSSDocumentCache}.
     */
    public static CacheStoreKeyBase createKey(final ISWC swc, final String cssFileName)
    {
        return new CSSDocumentCacheKey(swc, cssFileName);
    }
    
    /**
     * Create a cache key for {@code CSSDocumentCache} that references
     * a CSS file on disk.
     * 
     * @param cssFileName CSS file name
     * @return Key for {@code CSSDocumentCache}.
     */
    public static CacheStoreKeyBase createKey(final String cssFileName)
    {
        return new CSSDocumentCacheKey2(cssFileName);
    }

    @Override
    protected ICSSDocument createEntryValue(CacheStoreKeyBase key)
    {
        assert key instanceof CSSDocumentCacheKeyBase : "Expected 'CSSDocumentCacheKeyBase' but got " + key.getClass().getSimpleName();
        final CSSDocumentCacheKeyBase cacheKey = (CSSDocumentCacheKeyBase)key;

        ICSSDocument result = EMPTY_CSS_DOCUMENT;
        try
        {
            result = cacheKey.parse();
        }
        catch (IOException e)
        {
            // Ignore exception and return dummy value.
        }
        return result;
    }

    public static String[] ALL_DEFAULTS_CSS_FILENAMES = {"defaults.css", "defaults-3.0.0.css" };
    
    /**
     * Get the compatible-mode default CSS filename.
     * 
     * @param version Compatible version.
     * @return Defaults CSS filename.
     */
    private static String getCompatibleModeCSSFilename(final Integer version)
    {
        if (version == null)
            return "defaults.css";
        else if (version <= Configuration.MXML_VERSION_3_0)
            return "defaults-3.0.0.css";
        else
            return "defaults.css";
    }

    /**
     * Get the "default" CSS model in a SWC library. If
     * {@code compatibility-version=3} is set, this method will try to get
     * "defaults-3.0.0.css" first. If the compatibility version isn't present,
     * it will fall back to "defaults.css".
     * 
     * @param swc SWC file.
     * @param compatibilityVersion Compatibility version, or null if the
     * compiler is not under compatibility mode.
     * @return "defaults" CSS model or null if not found
     */
    public ICSSDocument getDefaultsCSS(final ISWC swc, final Integer compatibilityVersion)
    {
        final CacheStoreKeyBase key;
        final String cssFilename = getCompatibleModeCSSFilename(compatibilityVersion);
        key = createKey(swc, cssFilename);

        final ICSSDocument css = this.get(key);
        assert css != null : "ConcurrentCacheStoreBase never caches null value.";

        if (css == CSSDocumentCache.EMPTY_CSS_DOCUMENT)
        {
            if (compatibilityVersion != null)
            {
                // If compatible CSS is not present, fall back to "defaults.css".
                return getDefaultsCSS(swc, null);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return css;
        }
    }
}
