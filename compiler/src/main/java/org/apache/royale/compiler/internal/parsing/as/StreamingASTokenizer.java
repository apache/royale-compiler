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

package org.apache.royale.compiler.internal.parsing.as;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.clients.ASC;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.ITokenStreamFilter;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IASTokenizer;
import org.apache.royale.compiler.parsing.IASToken.ASTokenKind;
import org.apache.royale.compiler.problems.CyclicalIncludesProblem;
import org.apache.royale.compiler.problems.ExpectXmlBeforeNamespaceProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.UnexpectedTokenProblem;
import org.apache.royale.utils.ILengthAwareReader;
import org.apache.royale.utils.NonLockingStringReader;
import org.apache.royale.utils.ILengthAwareReader.InputType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This Tokenizer provides tokens to be used by various clients, most notably
 * the ASParser. Given the nature of ambiguities in the ActionScript 3 language,
 * this tokenizer also serves to disambiguate tokens based on a combination of
 * look behind and lookahead. For all cases of ambiguity, only one token is
 * needed for look behind, and in our worst case, n tokens forwards where n is
 * the number of tokens that can be produced. Some other state is kept in order
 * to know which type of container we may exist in (function, class, interface,
 * etc). We buffer LA token results to avoid unneeded lookahead
 */
public class StreamingASTokenizer implements ASTokenTypes, IASTokenizer, Closeable
{
    private static final String FOR_EACH = "for each";
    private static final String XML = "xml";
    private static final String DEFAULT_XML_NAMESPACE = "default xml namespace";
    private static final String ZERO = "0";

    /**
     * Map from keyword text to token type.
     * <p>
     * We use a HashMap here to avoid slowing down the performance of the
     * underlying lexer. We are avoid the "longest match" problem, requiring a
     * lot of rescanning on the lexer level to determine keywords from
     * identifiers. And since hash map lookup is constant, this is (in theory)
     * faster than doing this in the scanner since we're not bound by i/o or
     * state machine back-tracing.
     */
    private static final Map<String, Integer> keywordToTokenMap = new ImmutableMap.Builder<String, Integer>()
            .put(IASKeywordConstants.AS, TOKEN_KEYWORD_AS)
            .put(IASKeywordConstants.IS, TOKEN_KEYWORD_IS)
            .put(IASKeywordConstants.INSTANCEOF, TOKEN_KEYWORD_INSTANCEOF)
            .put(IASKeywordConstants.IN, TOKEN_KEYWORD_IN)
            .put(IASKeywordConstants.DELETE, TOKEN_KEYWORD_DELETE)
            .put(IASKeywordConstants.TYPEOF, TOKEN_KEYWORD_TYPEOF)
            .put(IASKeywordConstants.CONST, TOKEN_KEYWORD_CONST)
            .put(IASKeywordConstants.GET, TOKEN_RESERVED_WORD_GET)
            .put(IASKeywordConstants.IMPLEMENTS, TOKEN_RESERVED_WORD_IMPLEMENTS)
            .put(IASKeywordConstants.IMPORT, TOKEN_KEYWORD_IMPORT)
            .put(IASKeywordConstants.USE, TOKEN_KEYWORD_USE)
            .put(IASKeywordConstants.EXTENDS, TOKEN_RESERVED_WORD_EXTENDS)
            .put(IASKeywordConstants.NEW, TOKEN_KEYWORD_NEW)
            .put(IASKeywordConstants.DYNAMIC, TOKEN_MODIFIER_DYNAMIC)
            .put(IASKeywordConstants.FINAL, TOKEN_MODIFIER_FINAL)
            .put(IASKeywordConstants.NATIVE, TOKEN_MODIFIER_NATIVE)
            .put(IASKeywordConstants.OVERRIDE, TOKEN_MODIFIER_OVERRIDE)
            .put(IASKeywordConstants.STATIC, TOKEN_MODIFIER_STATIC)
            .put(IASKeywordConstants.VIRTUAL, TOKEN_MODIFIER_VIRTUAL)
            .put(IASKeywordConstants.ABSTRACT, TOKEN_MODIFIER_ABSTRACT)
            .put(IASKeywordConstants.SET, TOKEN_RESERVED_WORD_SET)
            // Keywords with special token types that affect subsequent blocks
            .put(IASKeywordConstants.CATCH, TOKEN_KEYWORD_CATCH)
            .put(IASKeywordConstants.CLASS, TOKEN_KEYWORD_CLASS)
            .put(IASKeywordConstants.FUNCTION, TOKEN_KEYWORD_FUNCTION)
            .put(IASKeywordConstants.INTERFACE, TOKEN_KEYWORD_INTERFACE)
            .put(IASKeywordConstants.PACKAGE, TOKEN_KEYWORD_PACKAGE)
            // #120009: allow "var" inside parameter list, even though it's not 
            // valid AS (don't turn the subsequent function block open into a block open
            .put(IASKeywordConstants.VAR, TOKEN_KEYWORD_VAR)
            .put(IASKeywordConstants.FALSE, TOKEN_KEYWORD_FALSE)
            .put(IASKeywordConstants.NULL, TOKEN_KEYWORD_NULL)
            .put(IASKeywordConstants.TRUE, TOKEN_KEYWORD_TRUE)
            .put(IASKeywordConstants.PUBLIC, HIDDEN_TOKEN_BUILTIN_NS)
            .put(IASKeywordConstants.PRIVATE, HIDDEN_TOKEN_BUILTIN_NS)
            .put(IASKeywordConstants.PROTECTED, HIDDEN_TOKEN_BUILTIN_NS)
            .put(IASKeywordConstants.INTERNAL, HIDDEN_TOKEN_BUILTIN_NS)
            .put(IASKeywordConstants.INCLUDE, TOKEN_KEYWORD_INCLUDE)
            // Keywords for statements that affect subsequent blocks
            .put(IASKeywordConstants.DO, TOKEN_KEYWORD_DO)
            .put(IASKeywordConstants.WHILE, TOKEN_KEYWORD_WHILE)
            .put(IASKeywordConstants.BREAK, TOKEN_KEYWORD_BREAK)
            .put(IASKeywordConstants.CONTINUE, TOKEN_KEYWORD_CONTINUE)
            .put(IASKeywordConstants.GOTO, TOKEN_RESERVED_WORD_GOTO)
            .put(IASKeywordConstants.FOR, TOKEN_KEYWORD_FOR)
            .put(StreamingASTokenizer.FOR_EACH, TOKEN_KEYWORD_FOR)
            .put(IASKeywordConstants.EACH, TOKEN_RESERVED_WORD_EACH)
            .put(IASKeywordConstants.WITH, TOKEN_KEYWORD_WITH)
            .put(IASKeywordConstants.ELSE, TOKEN_KEYWORD_ELSE)
            .put(IASKeywordConstants.IF, TOKEN_KEYWORD_IF)
            .put(IASKeywordConstants.SWITCH, TOKEN_KEYWORD_SWITCH)
            .put(IASKeywordConstants.CASE, TOKEN_KEYWORD_CASE)
            .put(IASKeywordConstants.DEFAULT, TOKEN_KEYWORD_DEFAULT)
            .put(IASKeywordConstants.TRY, TOKEN_KEYWORD_TRY)
            .put(IASKeywordConstants.FINALLY, TOKEN_KEYWORD_FINALLY)
            // Keywords with a generic keyword token type that have no effect 
            // on subsequent blocks.
            .put(IASKeywordConstants.NAMESPACE, TOKEN_RESERVED_WORD_NAMESPACE)
            .put(IASKeywordConstants.CONFIG, TOKEN_RESERVED_WORD_CONFIG)
            .put(IASKeywordConstants.THROW, TOKEN_KEYWORD_THROW)
            .put(IASKeywordConstants.SUPER, TOKEN_KEYWORD_SUPER)
            .put(IASKeywordConstants.THIS, TOKEN_KEYWORD_THIS)
            .put(IASKeywordConstants.VOID, TOKEN_KEYWORD_VOID)
            .put(IASKeywordConstants.RETURN, TOKEN_KEYWORD_RETURN)
            .build();

    /**
     * Configuration for out tokenizer
     */
    private static final class TokenizerConfig
    {
        /**
         * Flag that lets us ignore keywords for more general string parsing
         */
        public boolean ignoreKeywords = false;

        /**
         * Flag that lets us be aware of metadata
         */
        public boolean findMetadata = true;

        /**
         * Flag indicating that we are tokenizing full content/files, and not
         * segments
         */
        public boolean completeContent = true;

        /**
         * IFilter for old APIs
         */
        public ITokenStreamFilter filter;

        /**
         * Flag indicating we should collect comments
         */
        public boolean collectComments = false;

        /**
         * Flag indicating we follow include statements, including their tokens
         */
        public boolean followIncludes = true;

        /**
         * Flag indicating if identifier naming is strictly enforced to consider
         * keywords or reserved words invalid, or if it's more lenient, like
         * newer ECMAScript versions introduced after ActionScript 3.
         */
        public boolean strictIdentifierNames = false;
    }

    private Reader reader;

    //underlying lexer
    private RawASTokenizer tokenizer;

    //last exception to prevent us from looping forever
    private Exception lastException = null;

    //LA buffer
    private final List<ASToken> lookAheadBuffer;
    private int bufferSize = 0; //maintain size ourselves since it's faster

    //last token we encountered, used for lookback
    private ASToken lastToken;

    private int offsetAdjustment; //for offset adjustment
    private int lineAdjustment = 0;
    private int columnAdjustment = 0;

    private IncludeHandler includeHandler;

    /**
     * The forked tokenizer for included files. If not null, {@link #next()}
     * will return a token from this tokenizer.
     * <p>
     * After all the tokens are returned from the included source file,
     * {@link #closeIncludeTokenizer()} closes the tokenizer and set this field
     * to null.
     */
    private StreamingASTokenizer forkIncludeTokenizer;

    /**
     * Flag to indicate if we have followed include statements
     */
    private boolean hasEncounteredIncludeStatements = false;

    private TokenizerConfig config;

    /**
     * Source file handler. This is used by resolving included file path.
     * {@link #StreamingASTokenizer(IFileSpecification)} and
     * {@link #StreamingASTokenizer(IFileSpecification, Stack)} sets the value.
     */
    private String sourcePath;

    /**
     * Lexer problems.
     * */
    private final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

    /**
     * Imaginary tokens generated for {@code asc -in} option.
     */
    private Iterator<ASToken> ascIncludeImaginaryTokens;

    /**
     * You should probably not use this constructor. There is some legacy code
     * that uses this constructor, but that code should be updated to use one of
     * the static create methods below.
     * <p>
     * TODO: make this private.
     */
    public StreamingASTokenizer(final Reader reader)
    {
        this();
        setReader(reader);
    }

    /**
     * A pool to reduce duplicated string literals created
     */
    private final HashMap<String, String> stringPool;

    /**
     * You should probably not use this constructor. There is a lot of code that
     * uses this constructor, but that code should be updated to use one of the
     * static create methods below.
     * <p>
     * TODO: make this private.
     */
    public StreamingASTokenizer()
    {
        tokenizer = new RawASTokenizer();
        config = new TokenizerConfig();
        lookAheadBuffer = new ArrayList<ASToken>(5);
        includeHandler = IncludeHandler.creatDefaultIncludeHandler();
        stringPool = new HashMap<String, String>();

        // Initialize string pool with keyword strings. The keyword strings 
        // are declared as constants which are automatically "interned".
        for (final String keyword : keywordToTokenMap.keySet())
        {
            stringPool.put(keyword, keyword);
        }
    }

    /**
     * Creates a tokenizer suitable for the mxml indexing code. fragments the
     * new tokenizer will tokenize.
     * 
     * @return A new tokenizer suitable for tokenizing script fragments in an
     * mxml document that is being tokenized for the full text search index.
     */
    public static StreamingASTokenizer createForMXMLIndexing(String fileName)
    {
        StreamingASTokenizer result = new StreamingASTokenizer();
        result.setPath(fileName);
        result.includeHandler.enterFile(result.sourcePath);
        return result;
    }

    /**
     * Fork a new tokenizer when an "include" directive is found. This method
     * will pass the {@code StructureTracker} of the current tokenizer down to
     * the forked tokenizer.
     * 
     * @param currentTokenizer Current tokenizer.
     * @param fileSpec File specification of the included file.
     * @param includeHandler Include handler.
     * @return A tokenizer for the included file.
     * @throws FileNotFoundException Error.
     */
    private static StreamingASTokenizer createForIncludeFile(
            final StreamingASTokenizer currentTokenizer,
            final IFileSpecification fileSpec,
            final IncludeHandler includeHandler)
            throws FileNotFoundException
    {
        final StreamingASTokenizer tokenizer = create(fileSpec, includeHandler);
        return tokenizer;
    }

    /**
     * Create a tokenizer from a source file. This is the lexer entry-point used
     * by {@link ASCompilationUnit}.
     * 
     * @param fileSpec File specification provides the reader and the file path.
     * @param includeHandler Include handler.
     * @throws FileNotFoundException error
     */
    protected static StreamingASTokenizer create(
            final IFileSpecification fileSpec,
            final IncludeHandler includeHandler)
            throws FileNotFoundException
    {
        assert fileSpec != null : "File specification can't be null.";
        assert includeHandler != null : "Include handler can't be null.";

        final StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(fileSpec.createReader());
        tokenizer.setPath(fileSpec.getPath());
        tokenizer.includeHandler = includeHandler;
        tokenizer.includeHandler.enterFile(tokenizer.sourcePath);
        return tokenizer;
    }

    /**
     * Create a tokenizer for {@code ASParser#parseFile()}.
     * 
     * @param fileSpec File specification provides the reader and the file path.
     * @param includeHandler Include handler.
     * @param followIncludes True if included files are also parsed.
     * @param includedFiles A list of included file paths.
     * @return Lexer.
     * @throws FileNotFoundException error
     */
    protected static StreamingASTokenizer createForASParser(
            final IFileSpecification fileSpec,
            final IncludeHandler includeHandler,
            final boolean followIncludes,
            final List<String> includedFiles,
            final boolean strictIdentifierNames)
            throws FileNotFoundException
    {
        final StreamingASTokenizer tokenizer = create(fileSpec, includeHandler);
        tokenizer.setFollowIncludes(followIncludes);
        tokenizer.setStrictIdentifierNames(strictIdentifierNames);

        final ImmutableList.Builder<ASToken> imaginaryTokensBuilder =
                new ImmutableList.Builder<ASToken>();
        for (final String filename : includedFiles)
        {
            imaginaryTokensBuilder.add(new ASToken(
                    ASTokenTypes.TOKEN_KEYWORD_INCLUDE,
                    0,
                    0,
                    0,
                    0,
                    "include"));
            imaginaryTokensBuilder.add(new ASToken(
                    ASTokenTypes.TOKEN_LITERAL_STRING,
                    0,
                    0,
                    0,
                    0,
                    '"' + filename + '"'));
        }
        tokenizer.ascIncludeImaginaryTokens = imaginaryTokensBuilder.build().iterator();
        return tokenizer;
    }

    /**
     * This creator doesn't "enter file" on creation.
     */
    protected static StreamingASTokenizer createForInlineScriptScopeBuilding(
            final Reader reader,
            final String path,
            final IncludeHandler includeHandler,
            final int offsetAdjustment,
            final int lineAdjustment,
            final int columnAdjustment)
    {
        assert reader != null : "Reader can't be null";
        assert path != null : "Path can't be null";
        assert includeHandler != null : "IncludeHandler can't be null";

        final StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(reader);
        tokenizer.setPath(path);
        tokenizer.includeHandler = includeHandler;
        tokenizer.setSourcePositionAdjustment(
                offsetAdjustment, lineAdjustment, columnAdjustment);
        return tokenizer;
    }

    /**
     * Create a tokenizer to parse an Expression.
     */
    protected static StreamingASTokenizer createForInlineExpressionParsing(
            final Reader reader,
            final String path
            )
    {
        assert reader != null : "Reader can't be null";
        assert path != null : "Path can't be null";

        final StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(reader);
        tokenizer.setPath(path);
        tokenizer.includeHandler.enterFile(path);

        // Have to do this to get the tokenizer to work right - some things, like function expressions,
        // won't tokenize correctly unless the last token is '=' or some other special tokens.
        tokenizer.lastToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT, -1, -1, -1, -1, "=");

        return tokenizer;
    }

    /**
     * This method can create a {@code StreamingASTokenizer} with optional
     * "follow includes". If {@code IncludeHandler} is not null, it will follow
     * {@code include} directives.
     * 
     * @param reader Input to the tokenizer.
     * @param path File path of the input.
     * @param includeHandler If not null, the created tokenizer will follow
     * {@code include} directives.
     * @return A {@code StreamingASTokenizer}.
     */
    public static StreamingASTokenizer createForRepairingASTokenizer(
            final Reader reader,
            final String path,
            final IncludeHandler includeHandler)
    {
        assert path != null || includeHandler == null : "We need a source path to follow includes";
        final StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(reader);
        tokenizer.setPath(path);
        if (includeHandler != null)
        {
            tokenizer.includeHandler = includeHandler;
            includeHandler.enterFile(path);
        }
        return tokenizer;
    }

    /**
     * Sets the {@link Reader} that supplies the content to this tokenizer. It
     * is up to the client to close any previous readers that have been in use.
     * It is also up to the client to close the reader once it has been used
     * 
     * @param reader a {@link Reader}
     */
    public void setReader(final Reader reader)
    {
        setReader(reader, 0, 0, 0);
    }

    /**
     * Sets the {@link Reader} that supplies the content to this tokenizer. It
     * is up to the client to close any previous readers that have been in use.
     * It is also up to the client to close the reader once it has been used
     * 
     * @param reader a {@link Reader}
     * @param offset Offset adjustment. If the specified reader is reading from
     * a string extracted from a source file, this should be the offset of the
     * first character read from the reader in the source file.
     * @param line Line adjustment.
     * @param column Column adjustment
     */
    public void setReader(final Reader reader, int offset, int line, int column)
    {
        this.reader = reader;
        tokenizer = new RawASTokenizer();
        tokenizer.yyreset(reader);
        tokenizer.setCollectComments(config.collectComments);
        setSourcePositionAdjustment(offset, line, column);
    }

    /**
     * Sets the path to the file this tokenizer is scanning
     * 
     * @param path a file path
     */
    @Override
    public void setPath(String path)
    {
        assert path != null : "path of tokenizer shouldn't be null";
        sourcePath = path;
        tokenizer.setSourcePath(path);
    }

    /**
     * Allows for the adjustment of offset, line and column information when
     * parsing subsequences of text. This should be called before tokenization
     * has started
     * 
     * @param offset The offset where the fragment starts.
     * @param line The line where the fragment starts. This should be a
     * ZERO-based line number
     * @param column The column where the fragment starts. This should be a
     * ZERO-based column number
     */
    public void setSourcePositionAdjustment(int offset, int line, int column)
    {
        offsetAdjustment = offset;
        lineAdjustment = line;
        columnAdjustment = column;
    }

    /**
     * Sets whether we comments are collected: single line and multi-line.
     * Default is <code>false</code>
     * 
     * @param collect true if we should collect comments
     */
    @Override
    public void setCollectComments(final boolean collect)
    {
        config.collectComments = collect;
        
        if (tokenizer != null)
            tokenizer.setCollectComments(collect);
    }

    /**
     * Sets whether we follow include statements, including their tokens.
     * Default is <code>true</code>
     * 
     * @param followIncludes true if we should follow includes
     */
    @Override
    public void setFollowIncludes(final boolean followIncludes)
    {
        config.followIncludes = followIncludes;
    }

    public void setStrictIdentifierNames(boolean value)
    {
        config.strictIdentifierNames = value;
    }

    /**
     * Closes the underlying reader
     */
    @Override
    public void close() throws IOException
    {
        if (tokenizer != null)
        {
            tokenizer.reset();
            tokenizer.yyclose(); //close the reader
        }
    }

    /**
     * Sets whether we ignore keywords while scanning. Default is
     * <code>false</code>
     * 
     * @param ignore true if we should ignore keywords
     */
    public void setIgnoreKeywords(final boolean ignore)
    {
        config.ignoreKeywords = ignore;
    }

    /**
     * Sets whether we are scanning a full file, or a fragment. Default is
     * <code>true</code>
     * 
     * @param full true if we are scanning a full file.
     */
    public void setScanningFullContent(final boolean full)
    {
        config.completeContent = full;
    }

    /**
     * Sets whether we will find metadata constructs Default is
     * <code>true</code>
     * 
     * @param aware true if we will find metadata
     */
    public void setIsMetadataAware(final boolean aware)
    {
        config.findMetadata = aware;
    }

    /**
     * Sets the {@link ITokenStreamFilter} used to filter out unwanted tokens
     * 
     * @param filter the token filter to alter the stream returned from the
     * tokenizer
     */
    public void setTokenFilter(ITokenStreamFilter filter)
    {
        config.filter = filter;
    }

    /**
     * Sets the include handler used by this tokenizer to get
     * {@link IFileSpecification} for included files.
     * 
     * @param handler {@link IncludeHandler} this tokenizer should use.
     */
    public void setIncludeHandler(IncludeHandler handler)
    {
        includeHandler = handler;
    }

    /**
     * Indicated that we have tokenization problems. Can be called once scanning
     * has begun
     * 
     * @return true if problems have been encountered
     */
    public boolean hasTokenizationProblems()
    {
        return tokenizer.hasProblems() || problems.size() > 0;
    }

    /**
     * Indicated whether this tokenizer has encountered include statements,
     * regardless of whether it is set to follow them or not
     * 
     * @return true if we have encountered includes
     */
    public boolean hasEncounteredIncludeStatements()
    {
        return hasEncounteredIncludeStatements;
    }

    /**
     * Returns a collection of problems that have been encountered while
     * scanning.
     * 
     * @return a list of problems, never null
     */
    public List<ICompilerProblem> getTokenizationProblems()
    {
        ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(this.problems);
        problems.addAll(tokenizer.getProblems());
        return problems;
    }

    public ASToken[] getTokens(final Reader reader, ITokenStreamFilter filter)
    {
        setReader(reader);
        List<ASToken> tokenList = initializeTokenList(reader);
        ASToken token = null;
        do
        {
            token = next();
            if (token != null && filter.accept(token))
                tokenList.add(token.clone()); //make a copy because of object pool
        }
        while (token != null);
        return tokenList.toArray(new ASToken[0]);
    }

    @Override
    public ASToken[] getTokens(final Reader reader)
    {
        if (config.filter != null)
            return getTokens(reader, config.filter);
        setReader(reader);
        List<ASToken> tokenList = initializeTokenList(reader);
        ASToken token = null;
        do
        {
            token = next();
            if (token != null)
                tokenList.add(token.clone()); //copy ctor because of object pool
        }
        while (token != null);
        return tokenList.toArray(new ASToken[0]);
    }

    /**
     * @param reader
     * @return
     */
    private List<ASToken> initializeTokenList(final Reader reader)
    {
        List<ASToken> tokenList;
        int listSize = 8012;
        if (reader instanceof NonLockingStringReader)
        {
            //we know the length of this string.  For string of length x, their are roughly x/5 tokens that
            //can be constructed from that string.  size the array appropriately.
            listSize = 5;
            if (((NonLockingStringReader)reader).getLength() > 0)
            {
                listSize = Math.max((int)((NonLockingStringReader)reader).getLength() / 5, 5);
            }

        }
        else if (reader instanceof ILengthAwareReader && ((ILengthAwareReader)reader).getInputType() == InputType.FILE)
        {
            listSize = 9;
            if (((ILengthAwareReader)reader).getLength() > 0)
            {
                listSize = Math.max((int)((ILengthAwareReader)reader).getLength() / 9, 9);

            }
        }
        tokenList = new ArrayList<ASToken>(listSize);
        return tokenList;
    }

    @Override
    public IASToken[] getTokens(final String range)
    {
        return getTokens(new NonLockingStringReader(range));
    }

    /**
     * Returns the next token that can be produced from the underlying reader
     * 
     * @param filter an {@link ITokenStreamFilter} to restrict the tokens that
     * are returned
     * @return an ASToken, or null if no more tokens can be produced
     */
    public final ASToken next(final ITokenStreamFilter filter)
    {
        ASToken retVal = null;
        while (true)
        {
            retVal = next();
            if (retVal == null || filter.accept(retVal))
            {
                break;
            }
        }
        return retVal;
    }

    /**
     * Returns the next token that can be produced from the underlying reader.
     * <p>
     * If the forked "include file tokenizer" is open (not null), return the
     * next token from it. If the forked tokenizer reaches the end of the
     * included file, close (set to null) the forked tokenizer and return token
     * from the main source file.
     * 
     * @return an ASToken, or null if no more tokens can be produced
     */
    public final ASToken next()
    {
        ASToken retVal = null;
        // If the lexer for the included file is open, read from the included tokenizer.
        boolean consumeSemi = false;
        try
        {
            // Return token from the main file.
            if (forkIncludeTokenizer != null)
            {
                retVal = forkIncludeTokenizer.next();

                // Check if the forked tokenizer reached EOF. 
                if (retVal == null)
                {
                    closeIncludeTokenizer();
                    // We should consume the next semicolon we find.
                    // Most include statements are terminated with a semicolon,
                    // and because we read the contents of the included file,
                    // this could cause problems with a semicolon in a place
                    // we don't want it.
                    consumeSemi = true; 
                }
                else
                    return retVal;
            }
            if (bufferSize > 0)
            {
                retVal = lookAheadBuffer.remove(0);
                bufferSize--;
            }
            else
            {
                retVal = nextTokenFromReader();

            }
            if (retVal == null)
                return null;
            final int tokenType = retVal.getType();

            switch (tokenType)
            {
                // if we're seeing each in this part of the loop, it's not a
                // syntactic keyword
                // since we do lookahead when we see "for", checking for "each"
                case TOKEN_RESERVED_WORD_EACH:
                    treatKeywordAsIdentifier(retVal);
                    processUserDefinedNamespace(retVal, 0);
                    return retVal;
                case TOKEN_KEYWORD_INCLUDE:
                {
                    if (!config.strictIdentifierNames && lastToken != null)
                    {
                        int lastTokenType = lastToken.getType();
                        switch (lastTokenType)
                        {
                            case TOKEN_KEYWORD_VAR:
                            case TOKEN_KEYWORD_FUNCTION:
                            case TOKEN_RESERVED_WORD_GET:
                            case TOKEN_RESERVED_WORD_SET:
                            case TOKEN_OPERATOR_MEMBER_ACCESS:
                            {
                                retVal.setType(TOKEN_IDENTIFIER);
                                return retVal;
                            }
                        }
                    }
                    // "followIncludes=false" is usually used for code model
                    // partitioner. They want the "include" token.
                    if (!config.followIncludes)
                        return retVal;

                    final ASToken token = LT(1);

                    // "include" at EOF is always a keyword
                    if (token == null)
                        return retVal;

                    if (!matches(token, TOKEN_LITERAL_STRING))
                    {
                        treatKeywordAsIdentifier(retVal); // it's an identifier
                        processUserDefinedNamespace(retVal, 0);
                    }
                    else
                    {
                        hasEncounteredIncludeStatements = true;
                        // Consume the file path after the include token.
                        consume(1);
                        final String filenameTokenText = token.getText();
                        final String includeString = filenameTokenText.substring(1, filenameTokenText.length() - 1);

                        if (sourcePath == null)
                            throw new NullPointerException("Source file is needed for resolving included file path.");
                        IFileSpecification includedFileSpec = null;
                        //respond to problems from our file handler
                        includedFileSpec = includeHandler.getFileSpecificationForInclude(sourcePath, includeString);
                        //
                        if (includedFileSpec == null)
                        {
                            ICompilerProblem problem = new FileNotFoundProblem(token, filenameTokenText); //the text will be the path not found
                            problems.add(problem);
                            retVal = next();
                            return retVal;
                        }
                        if (includeHandler.isCyclicInclude(includedFileSpec.getPath()))
                        {
                            ICompilerProblem problem = new CyclicalIncludesProblem(token);
                            problems.add(problem);
                            retVal = next();
                            return retVal;
                        }
                        else
                        {
                            // Fork a tokenizer for the included file
                            try
                            {
                                forkIncludeTokenizer = createForIncludeFile(this, includedFileSpec, includeHandler);
                                retVal = forkIncludeTokenizer.next();
                            }
                            catch (FileNotFoundException fnfe)
                            {
                                includeHandler.handleFileNotFound(includedFileSpec);
                                ICompilerProblem problem = new FileNotFoundProblem(token, includedFileSpec.getPath());
                                problems.add(problem);
                                retVal = next();
                                return retVal;
                            }
                        }
                    }

                    // Recover from compiler problems and continue.
                    if (retVal == null)
                    {
                        // Included file is empty. 
                        closeIncludeTokenizer();
                        // Fall back to main source.
                        retVal = this.next();
                    }
                    return retVal;
                }
                case TOKEN_RESERVED_WORD_CONFIG:
                    if (matches(LT(1), TOKEN_RESERVED_WORD_NAMESPACE))
                    { //we config namespace
                        retVal.setType(TOKEN_RESERVED_WORD_CONFIG);
                        return retVal;
                    }
                    treatKeywordAsIdentifier(retVal); //identifier
                    processUserDefinedNamespace(retVal, 0);
                    return retVal;
                case HIDDEN_TOKEN_BUILTIN_NS:
                    if (matches(LT(1), TOKEN_OPERATOR_NS_QUALIFIER))
                    { //we have public:: and this structure is not an annotation but a name ref
                        retVal.setType(TOKEN_NAMESPACE_NAME);
                        return retVal;
                    }
                    retVal.setType(TOKEN_NAMESPACE_ANNOTATION);
                    return retVal;
                case TOKEN_MODIFIER_DYNAMIC:
                case TOKEN_MODIFIER_FINAL:
                case TOKEN_MODIFIER_NATIVE:
                case TOKEN_MODIFIER_OVERRIDE:
                case TOKEN_MODIFIER_STATIC:
                case TOKEN_MODIFIER_VIRTUAL:
                case TOKEN_MODIFIER_ABSTRACT:
                {
                    // previous token is either a modifier or a namespace, or if
                    // null, assume keyword
                    // next token is from a definition or a modifier or a namespace
                    final ASToken nextToken = LT(1);
                    if (nextToken != null)
                    {
                        switch (nextToken.getType())
                        {
                            case TOKEN_KEYWORD_CLASS:
                            case TOKEN_KEYWORD_FUNCTION:
                            case TOKEN_KEYWORD_INTERFACE:
                            case TOKEN_RESERVED_WORD_NAMESPACE:
                            case TOKEN_KEYWORD_VAR:
                            case TOKEN_KEYWORD_CONST:
                            case TOKEN_MODIFIER_DYNAMIC:
                            case TOKEN_MODIFIER_FINAL:
                            case TOKEN_MODIFIER_NATIVE:
                            case TOKEN_MODIFIER_OVERRIDE:
                            case TOKEN_MODIFIER_STATIC:
                            case TOKEN_MODIFIER_VIRTUAL:
                            case TOKEN_MODIFIER_ABSTRACT:
                            case TOKEN_NAMESPACE_ANNOTATION:
                            case TOKEN_NAMESPACE_NAME:
                            case HIDDEN_TOKEN_BUILTIN_NS:
                                return retVal;
                            case TOKEN_IDENTIFIER:
                                if (isUserDefinedNamespace(nextToken, 1)) // we're already looking ahead one so make sure we look ahead one further
                                    return retVal;
                            default:
                                // Not applicable to other token types.
                                break;
                        }
                    }
                    treatKeywordAsIdentifier(retVal);
                    processUserDefinedNamespace(retVal, 0);
                    return retVal;
                }
                    //we combine +/- for numeric literals here
                case TOKEN_OPERATOR_MINUS:
                case TOKEN_OPERATOR_PLUS:
                {
                    if (lastToken == null || !lastToken.canPreceedSignedOperator())
                    {
                        final ASToken nextToken = LT(1);
                        if (nextToken != null)
                        {
                            switch (nextToken.getType())
                            {
                                case TOKEN_LITERAL_NUMBER:
                                case TOKEN_LITERAL_HEX_NUMBER:
                                    retVal.setEnd(nextToken.getEnd());
                                    final StringBuilder builder = new StringBuilder(retVal.getText());
                                    builder.append(nextToken.getText());
                                    retVal.setText(poolString(builder.toString()));
                                    consume(1);
                                    retVal.setType(nextToken.getType());
                                    break;
                                default:
                                    // ignore other tokens
                                    break;
                            }
                        }
                    }

                    return retVal;
                }
                    //RECOGNIZE: for each
                case TOKEN_KEYWORD_FOR:
                {
                    final ASToken token = LT(1);
                    if (matches(token, TOKEN_RESERVED_WORD_EACH))
                    {
                        retVal.setEnd(token.getEnd());
                        retVal.setText(FOR_EACH);
                        consume(1);
                        return retVal;
                    }
                    if (!config.strictIdentifierNames && lastToken != null)
                    {
                        int lastTokenType = lastToken.getType();
                        switch (lastTokenType)
                        {
                            case TOKEN_KEYWORD_VAR:
                            case TOKEN_KEYWORD_FUNCTION:
                            case TOKEN_RESERVED_WORD_GET:
                            case TOKEN_RESERVED_WORD_SET:
                            case TOKEN_OPERATOR_MEMBER_ACCESS:
                                retVal.setType(TOKEN_IDENTIFIER);
                        }
                    }
                    return retVal;
                }
                    //RECOGNIZE: default xml namespace
                    //default xml namespace must exist on the same line
                case TOKEN_KEYWORD_DEFAULT:
                {
                    final ASToken maybeNS = LT(2);
                    final boolean foundTokenNamespace = maybeNS != null &&
                                                        maybeNS.getType() == TOKEN_RESERVED_WORD_NAMESPACE;
                    final ASToken maybeXML = LT(1);
                    if (foundTokenNamespace)
                    {
                        final boolean foundTokenXML = maybeXML != null &&
                                                      maybeXML.getType() == TOKEN_IDENTIFIER &&
                                                      XML.equals(maybeXML.getText());
                        if (!foundTokenXML)
                        {
                            final ICompilerProblem problem =
                                    new ExpectXmlBeforeNamespaceProblem(maybeNS);
                            problems.add(problem);
                        }

                        //combine all of these tokens together
                        retVal.setEnd(maybeNS.getEnd());
                        retVal.setText(DEFAULT_XML_NAMESPACE);
                        retVal.setType(TOKEN_DIRECTIVE_DEFAULT_XML);
                        consume(2);
                    }
                    // if this isn't "default xml namespace" then
                    // see if it is the default case in a switch
                    // otherwise, assume it is an identiferName
                    else if (!config.strictIdentifierNames &&
                            maybeXML != null && 
                            maybeXML.getType() != TOKEN_COLON)
                        retVal.setType(TOKEN_IDENTIFIER);
                    else if (!config.strictIdentifierNames && lastToken != null)
                    {
                        int lastTokenType = lastToken.getType();
                        switch (lastTokenType)
                        {
                            case TOKEN_KEYWORD_VAR:
                            case TOKEN_KEYWORD_FUNCTION:
                            case TOKEN_RESERVED_WORD_GET:
                            case TOKEN_RESERVED_WORD_SET:
                            case TOKEN_OPERATOR_MEMBER_ACCESS:
                                retVal.setType(TOKEN_IDENTIFIER);
                        }
                    }
                    return retVal;
                }
                case TOKEN_KEYWORD_VOID:
                {
                    //check for void 0
                    final ASToken token = LT(1);
                    if (matches(token, TOKEN_LITERAL_NUMBER) && ZERO.equals(token.getText()))
                    {
                        retVal.setType(TOKEN_VOID_0);
                        combineText(retVal, token);
                        consume(1);
                    }
                    //check for void(0)
                    else if (matches(token, TOKEN_PAREN_OPEN))
                    {
                        final ASToken zeroT = LT(2);
                        if (matches(zeroT, TOKEN_LITERAL_NUMBER) && ZERO.equals(zeroT.getText()))
                        {
                            final ASToken closeParenT = LT(3);
                            if (matches(closeParenT, TOKEN_PAREN_CLOSE))
                            {
                                combineText(retVal, token);
                                combineText(retVal, zeroT);
                                combineText(retVal, closeParenT);
                                retVal.setType(TOKEN_VOID_0);
                                consume(3);
                            }
                        }
                    }
                    return retVal;
                }
                case TOKEN_IDENTIFIER:
                {
                    //check for user-defined namespace before we return anything
                    processUserDefinedNamespace(retVal, 0);
                    return retVal;
                }
                    //this is for metadata processing
                case TOKEN_SQUARE_OPEN:
                {
                    retVal = tryParseMetadata(retVal);
                    return retVal;
                }
                case HIDDEN_TOKEN_STAR_ASSIGNMENT:
                {
                    //this is to solve an ambiguous case, where we can't tell the difference between 
                    //var foo:*=null and foo *= null;
                    retVal.setType(TOKEN_OPERATOR_STAR);
                    retVal.setEnd(retVal.getEnd() - 1);
                    retVal.setText("*");
                    //add the equals
                    final ASToken nextToken = tokenizer.buildToken(TOKEN_OPERATOR_ASSIGNMENT,
                                retVal.getEnd() + 1, retVal.getEnd() + 2,
                                retVal.getLine(), retVal.getColumn(), "=");
                    nextToken.setSourcePath(sourcePath);
                    addTokenToBuffer(nextToken);
                    return retVal;
                }
                case TOKEN_SEMICOLON:
                    if (consumeSemi)
                    {
                        return next();
                    }
                    return retVal;
                case TOKEN_VOID_0:
                case TOKEN_LITERAL_REGEXP:
                case TOKEN_COMMA:
                case TOKEN_COLON:
                case TOKEN_PAREN_OPEN:
                case TOKEN_PAREN_CLOSE:
                case TOKEN_SQUARE_CLOSE:
                case TOKEN_ELLIPSIS:
                case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
                case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
                case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
                case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
                case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
                case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                case TOKEN_OPERATOR_STAR:
                case TOKEN_OPERATOR_NS_QUALIFIER:
                case TOKEN_ASDOC_COMMENT:
                case TOKEN_OPERATOR_DIVISION:
                case TOKEN_OPERATOR_MODULO:
                case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
                case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
                case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
                case TOKEN_OPERATOR_LESS_THAN:
                case TOKEN_OPERATOR_GREATER_THAN:
                case TOKEN_OPERATOR_LESS_THAN_EQUALS:
                case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
                case TOKEN_OPERATOR_EQUAL:
                case TOKEN_OPERATOR_NOT_EQUAL:
                case TOKEN_OPERATOR_STRICT_EQUAL:
                case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
                case TOKEN_OPERATOR_BITWISE_AND:
                case TOKEN_OPERATOR_BITWISE_XOR:
                case TOKEN_OPERATOR_BITWISE_OR:
                case TOKEN_OPERATOR_LOGICAL_AND:
                case TOKEN_OPERATOR_LOGICAL_OR:
                case TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT:
                case TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT:
                case TOKEN_TYPED_COLLECTION_OPEN:
                case TOKEN_TYPED_COLLECTION_CLOSE:
                case TOKEN_OPERATOR_MEMBER_ACCESS:
                case TOKEN_RESERVED_WORD_NAMESPACE:
                case TOKEN_RESERVED_WORD_GET:
                case TOKEN_RESERVED_WORD_SET:
                case TOKEN_OPERATOR_ASSIGNMENT:
                case TOKEN_TYPED_LITERAL_CLOSE:
                case TOKEN_TYPED_LITERAL_OPEN:
                case TOKEN_OPERATOR_TERNARY:
                case TOKEN_OPERATOR_DECREMENT:
                case TOKEN_OPERATOR_INCREMENT:
                case TOKEN_OPERATOR_ATSIGN:
                case TOKEN_OPERATOR_BITWISE_NOT:
                case TOKEN_OPERATOR_LOGICAL_NOT:
                case TOKEN_E4X_BINDING_CLOSE:
                case TOKEN_E4X_BINDING_OPEN:
                case TOKEN_OPERATOR_DESCENDANT_ACCESS:
                case TOKEN_NAMESPACE_ANNOTATION:
                case TOKEN_NAMESPACE_NAME:
                case TOKEN_BLOCK_OPEN:
                case TOKEN_BLOCK_CLOSE:
                case TOKEN_KEYWORD_FUNCTION:
                    return retVal;
                case HIDDEN_TOKEN_MULTI_LINE_COMMENT:
                case HIDDEN_TOKEN_SINGLE_LINE_COMMENT:
                    if (tokenizer.isCollectingComments())
                    {
                        return retVal;
                    }
                    assert (false);
                    return null;
                case TOKEN_KEYWORD_INSTANCEOF:
                case TOKEN_KEYWORD_AS:
                case TOKEN_KEYWORD_IN:
                case TOKEN_KEYWORD_IS:
                    if(!config.strictIdentifierNames)
                    {
                        if (lastToken != null)
                        {
                            int lastTokenType = lastToken.getType();
                            switch (lastTokenType)
                            {
                                case TOKEN_SEMICOLON:
                                case TOKEN_BLOCK_OPEN:
                                case TOKEN_COMMA:
                                    retVal.setType(TOKEN_IDENTIFIER);
                                    return retVal;
                            }
                        }
                        else 
                        {
                            // we are first token so assume identifier
                            retVal.setType(TOKEN_IDENTIFIER);
                            return retVal;
                        }
                    }
                    // and fall through
                case TOKEN_KEYWORD_DELETE:
                    if(!config.strictIdentifierNames)
                    {
                        ASToken nextToken = LT(1);
                        if (nextToken != null)
                        {
                            int nextTokenType = nextToken.getType();
                            switch (nextTokenType)
                            {
                                // if followed by a token assume it is the
                                // keyword and not the identiferName;
                                case TOKEN_IDENTIFIER:
                                    return retVal;
                                // followed by a comma or semicolon
                                // probably being used in an expression
                                case TOKEN_COMMA:
                                case TOKEN_SEMICOLON:
                                    retVal.setType(TOKEN_IDENTIFIER);
                                    return retVal;
                            }
                        }
                    }
                    // and fall through
                case TOKEN_KEYWORD_BREAK:
                case TOKEN_KEYWORD_CASE:
                case TOKEN_KEYWORD_CATCH:
                case TOKEN_KEYWORD_CLASS:
                case TOKEN_KEYWORD_CONST:
                case TOKEN_KEYWORD_CONTINUE:
                case TOKEN_KEYWORD_DO:
                case TOKEN_KEYWORD_ELSE:
                case TOKEN_KEYWORD_FALSE:
                case TOKEN_KEYWORD_FINALLY:
                case TOKEN_KEYWORD_IF:
                case TOKEN_KEYWORD_IMPORT:
                case TOKEN_KEYWORD_INTERFACE:
                case TOKEN_KEYWORD_NULL:
                case TOKEN_KEYWORD_PACKAGE:
                case TOKEN_KEYWORD_SUPER:
                case TOKEN_KEYWORD_SWITCH:
                case TOKEN_KEYWORD_THIS:
                case TOKEN_KEYWORD_TRUE:
                case TOKEN_KEYWORD_TRY:
                case TOKEN_KEYWORD_TYPEOF:
                case TOKEN_KEYWORD_USE:
                case TOKEN_KEYWORD_VAR:
                case TOKEN_KEYWORD_WHILE:
                case TOKEN_KEYWORD_WITH:
                case TOKEN_KEYWORD_RETURN:
                case TOKEN_KEYWORD_THROW:
                case TOKEN_KEYWORD_NEW:
                    if (!config.strictIdentifierNames && lastToken != null)
                    {
                        int lastTokenType = lastToken.getType();
                        switch (lastTokenType)
                        {
                            case TOKEN_KEYWORD_VAR:
                            case TOKEN_KEYWORD_FUNCTION:
                            case TOKEN_RESERVED_WORD_GET:
                            case TOKEN_RESERVED_WORD_SET:
                            case TOKEN_OPERATOR_MEMBER_ACCESS:
                                retVal.setType(TOKEN_IDENTIFIER);
                        }
                    }
                    return retVal;
                default:
                    if (ASToken.isE4X(tokenType))
                        return retVal;

                    if (retVal.isKeywordOrContextualReservedWord() || retVal.isLiteral())
                        return retVal;

                    // If we reach here, the token fails to match any processing logic.
                    final UnexpectedTokenProblem problem = new UnexpectedTokenProblem(
                            retVal,
                            ASTokenKind.UNKNOWN);
                    problems.add(problem);
            }
        }
        catch (final Exception e)
        {
            if (lastException != null)
            {
                if (lastException.getClass().isInstance(e))
                {
                    ICompilerProblem problem = new InternalCompilerProblem2(sourcePath, e, "StreamingASTokenizer");
                    problems.add(problem);
                    return null;
                }
            }
            else
            {
                lastException = e;
                retVal = null;
                return next();
            }
        }
        finally
        {
            consumeSemi = false;
            lastToken = retVal;
        }
        return null;
    }

    /**
     * Error recovery: convert the given keyword token into an identifier token,
     * and log a syntax error.
     * 
     * @param token Keyword token.
     */
    private void treatKeywordAsIdentifier(final ASToken token)
    {
        assert token != null : "token can't be null";
        assert token.isKeywordOrContextualReservedWord() : "only transfer reserved words";

        if (token.isKeyword())
        {
            final UnexpectedTokenProblem problem = new UnexpectedTokenProblem(token, ASTokenKind.IDENTIFIER);
            problems.add(problem);
        }
        token.setType(TOKEN_IDENTIFIER);
    }

    /**
     * Decide within the current context whether the following content can be
     * parsed as a metadata tag token.
     * 
     * @param nextToken The next token coming from
     * {@link #nextTokenFromReader()}.
     * @return If the following content can be a metadata tag, the result is a
     * token of type {@link ASTokenTypes#TOKEN_ATTRIBUTE}. Otherwise, the
     * argument {@code nextToken} is returned.
     * @throws Exception Parsing error.
     */
    private ASToken tryParseMetadata(ASToken nextToken) throws Exception
    {
        // Do not initialize this variable so that Java flow-analysis can check if
        // the following rules cover all the possibilities.
        final boolean isNextMetadata;

        if (!config.findMetadata)
        {
            // The lexer is configured to not recognize metadata.
            isNextMetadata = false;
        }
        else if (lastToken == null)
        {
            // An "[" at the beginning of a script is always a part of a metadata.
            isNextMetadata = true;
        }
        else
        {
            switch (lastToken.getType())
            {
                case TOKEN_ASDOC_COMMENT:
                case TOKEN_SEMICOLON:
                case TOKEN_ATTRIBUTE:
                case TOKEN_BLOCK_OPEN:
                    // "[" after these tokens are always part of a metadata token.
                    isNextMetadata = true;
                    break;

                case TOKEN_SQUARE_CLOSE:
                case TOKEN_IDENTIFIER:
                    // "[" following a "]" is an array access.
                    // "[" following an identifier is an array access.
                    isNextMetadata = false;
                    break;
                    
                case TOKEN_KEYWORD_INCLUDE:
                case TOKEN_BLOCK_CLOSE:
                case TOKEN_OPERATOR_STAR:
                    // "[" after these tokens are part of a metadata token, if
                    // the "[" is on a new line.
                    isNextMetadata = !lastToken.matchesLine(nextToken);
                    break;

                default:
                    // If we are lexing an entire file
                    // then at this point we "know" that the next token
                    // is not meta-data.
                    if (config.completeContent)
                    {
                        isNextMetadata = false;
                    }
                    else
                    {
                        // In "fragment" mode which is used by the syntax coloring code
                        // in builder, we assume the following list of tokens can not
                        // precede meta-data because they all start or occur in expressions.
                        switch (lastToken.getType())
                        {
                            case TOKEN_OPERATOR_EQUAL:
                            case TOKEN_OPERATOR_TERNARY:
                            case TOKEN_COLON:
                            case TOKEN_OPERATOR_PLUS:
                            case TOKEN_OPERATOR_MINUS:
                            case TOKEN_OPERATOR_STAR:
                            case TOKEN_OPERATOR_DIVISION:
                            case TOKEN_OPERATOR_MODULO:
                            case TOKEN_OPERATOR_BITWISE_AND:
                            case TOKEN_OPERATOR_BITWISE_OR:
                            case TOKEN_KEYWORD_AS:
                            case TOKEN_OPERATOR_BITWISE_XOR:
                            case TOKEN_OPERATOR_LOGICAL_AND:
                            case TOKEN_OPERATOR_LOGICAL_OR:
                            case TOKEN_PAREN_OPEN:
                            case TOKEN_COMMA:
                            case TOKEN_OPERATOR_BITWISE_NOT:
                            case TOKEN_OPERATOR_LOGICAL_NOT:
                            case TOKEN_OPERATOR_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
                            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
                            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
                            case TOKEN_OPERATOR_LESS_THAN:
                            case TOKEN_OPERATOR_GREATER_THAN:
                            case TOKEN_OPERATOR_LESS_THAN_EQUALS:
                            case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
                            case TOKEN_OPERATOR_NOT_EQUAL:
                            case TOKEN_OPERATOR_STRICT_EQUAL:
                            case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
                            case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
                            case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
                            case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
                            case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
                            case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
                            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                                isNextMetadata = false;
                                break;
                            default:
                                isNextMetadata = true;
                                break;
                        }
                    }
                    break;
            }
        }

        final ASToken result;
        if (isNextMetadata)
            result = consumeMetadata(nextToken);
        else
            result = nextToken;

        return result;
    }

    /**
     * Close the forked include file tokenizer, and set it to null.
     */
    private void closeIncludeTokenizer()
    {
        if (forkIncludeTokenizer == null)
            return;

        try
        {
            problems.addAll(forkIncludeTokenizer.problems);
            forkIncludeTokenizer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        includeHandler.leaveFile(forkIncludeTokenizer.getEndOffset());
        forkIncludeTokenizer = null;
    }

    /**
     * @throws Exception
     */
    private final ASToken consumeMetadata(final ASToken startToken) throws Exception
    {
        final ASToken originalToken = new ASToken(startToken);
        MetaDataPayloadToken payload = new MetaDataPayloadToken(originalToken);
        final ArrayList<ASToken> safetyNet = new ArrayList<ASToken>(5);
        boolean isMetadata = true;
        while (true)
        {
            tokenizer.setReuseLastToken();
            final ASToken next = LT(1);
            if (next == null)
            {
                break;
            }
            safetyNet.add(new ASToken(next)); //sadly, we have to deal with the extra object creation if we're wrong
            payload.addToken(next); //here too

            if (!next.canExistInMetadata())
            {
                isMetadata = false;
                //consume the last token we saw so that we don't get ourselves into an infinite loop
                //it was the last token of the metadata, and this makes "next" the current token.
                consume(1);
                break;
            }
            consume(1);
            if (next.getType() == TOKEN_SQUARE_CLOSE)
            {
                break;
            }
        }
        if (!isMetadata)
        { //we're wrong, so let's add back the tokens to our lookahead buffer
            lookAheadBuffer.addAll(safetyNet);
            bufferSize = lookAheadBuffer.size();
            return originalToken;
        }
        return payload;

    }

    private final void fill(final int distance) throws Exception
    {
        int pos = 0;
        while (pos < distance)
        {
            addTokenToBuffer(nextTokenFromReader());
            pos++;
        }
    }

    /**
     * @param nextToken
     */
    private final void addTokenToBuffer(final ASToken nextToken)
    {
        bufferSize++;
        lookAheadBuffer.add(nextToken);
        // at EOF, nextToken can be null.
        if (nextToken != null)
            nextToken.lock();
    }

    /**
     * Get the pooled version of a given string.
     * 
     * @param text String literal.
     * @return Pooled string.
     */
    private final String poolString(final String text)
    {
        String pooledString = stringPool.get(text);
        if (pooledString == null)
        {
            stringPool.put(text, text);
            pooledString = text;
        }
        return pooledString;
    }

    /**
     * Get the next token from the source input. If this tokenizer is created
     * for a source file by {@link ASC}, and there are files included by
     * {@code -in} option, the tokenizer will return the
     * "injected include tokens" before real tokens coming from the JFlex
     * generated tokenizer.
     * 
     * @return next token from the source input
     * @throws IOException error
     * @see ASCompilationUnit#createMainCompilationUnitForASC()
     */
    private final ASToken nextTokenFromReader() throws IOException
    {
        final ASToken nextToken;
        if (ascIncludeImaginaryTokens != null && ascIncludeImaginaryTokens.hasNext())
            nextToken = ascIncludeImaginaryTokens.next();
        else if (tokenizer.hasBufferToken())
            nextToken = tokenizer.getBufferToken();
        else
            nextToken = tokenizer.nextToken();

        if (nextToken != null)
        {
            // Converting unicode on-the-fly in the lexer is much slower than
            // converting it here after the token is made, especially for 
            // identifiers.
            switch (nextToken.getType())
            {
                case TOKEN_LITERAL_NUMBER:
                    nextToken.setText(poolString(nextToken.getText()));
                    break;
                case TOKEN_LITERAL_REGEXP:
                    // Any "backslash-u" entities left after "convertUnicode"
                    // are invalid unicode escape sequences. According to AS3
                    // behavior, the backslash character is dropped.
                    nextToken.setText(poolString(convertUnicode(nextToken.getText()).replaceAll("\\\\u", "u")));
                    break;
                case TOKEN_IDENTIFIER:
                    // Intern 'identifiers' and 'keywords'. 
                    // 'keywords' were 'identifiers' before they are analyzed.
                    final String originalIdentifierName = nextToken.getText();
                    final String normalizedIdentifierName = poolString(convertUnicode(originalIdentifierName));
                    nextToken.setText(normalizedIdentifierName);
                    if (!config.ignoreKeywords)
                    {
                        /**
                         * If the identifier has escaped unicode sequence, it
                         * can't be a keyword.
                         * <p>
                         * According to ASL syntax spec chapter 3.4:
                         * <blockquote> Unicode escape sequences may be used to
                         * spell the names of identifiers that would otherwise
                         * be keywords. This is in contrast to ECMAScript.
                         * </blockquote>
                         */
                        if (originalIdentifierName.equals(normalizedIdentifierName))
                        {
                            // do keyword analysis here
                            final Integer info = keywordToTokenMap.get(nextToken.getText());
                            if (info != null)
                                nextToken.setType(info);
                        }
                    }
                    break;
                default:
                    // Ignore other tokens.
                    break;
            }

            //so we want to adjust all of our offsets here, BUT
            //the column is really only valid for the first line, which is line 0.
            //if we're not the first line, don't bother
            nextToken.adjustLocation(
                    offsetAdjustment,
                    lineAdjustment,
                    nextToken.getLine() == 0 ? columnAdjustment : 0);
            nextToken.storeLocalOffset();

            if (includeHandler != null)
            {
                nextToken.setSourcePath(includeHandler.getIncludeStackTop());
                includeHandler.onNextToken(nextToken);
            }

            if (nextToken.getSourcePath() == null)
                nextToken.setSourcePath(sourcePath);

            if (reader instanceof SourceFragmentsReader)
                ((SourceFragmentsReader)reader).adjustLocation(nextToken);
        }
        return nextToken;
    }

    /**
     * Consume tokens in the buffer
     * 
     * @param distance the number of tokens to consume
     */
    private final void consume(int distance)
    {
        if (bufferSize >= distance)
        {
            for (; distance > 0; distance--)
            {
                lookAheadBuffer.remove(bufferSize - 1);
                bufferSize--;
            }
        }
    }

    /**
     * Returns the next token that will be produced by the underlying lexer
     * 
     * @param distance distance to look ahead
     * @return an {@link ASToken}
     * @throws Exception
     */
    private final ASToken LT(final int distance) throws Exception
    {
        if (bufferSize < distance)
        {
            fill(distance - bufferSize);
        }
        return lookAheadBuffer.get(distance - 1);
    }

    private static final boolean matches(final ASToken token, final int type)
    {
        return token != null && token.getType() == type;
    }

    /**
     * Retrieve the end offset of the file.
     * <p>
     * The result is the end offset of the file, not the offset of the last
     * token, this allows any trailing space to be included so that the parser
     * can span the result {@code FileNode} to the entire file.
     * 
     * @return the end offset of the input file
     */
    public final int getEndOffset()
    {
        return tokenizer.getOffset() + offsetAdjustment;
    }

    /**
     * Computers whether the following token is a user-defined namespace. This
     * method calls processUserDefinedNamespace which will change token types
     * 
     * @param token token to start our analysis
     * @param lookaheadOffset offset of the tokens to look at
     * @return true if we're a user-defined namespace
     * @throws Exception
     */
    private final boolean isUserDefinedNamespace(final ASToken token, final int lookaheadOffset) throws Exception
    {
        processUserDefinedNamespace(token, lookaheadOffset);
        return token.getType() == TOKEN_NAMESPACE_ANNOTATION || token.getType() == TOKEN_NAMESPACE_NAME;
    }

    /**
     * Because AS3 supports qualified/unqualified namespaces as decorators on
     * definitions, we need to detect them before we even make it to the parser.
     * These look exactly like names/qnames, and so if they're on the same line
     * as a definition they might be a namespace name instead of a standard
     * identifier. This method will detect these cases, and change token types
     * accordingly
     * 
     * @param token token token to start our analysis
     * @param lookaheadOffset offset of the tokens to look at
     * @throws Exception
     */
    private final void processUserDefinedNamespace(final ASToken token, final int lookaheadOffset) throws Exception
    {
        token.lock();

        //determine if we have a user-defined namespace
        //our first token will be an identifier, and the cases we're looking for are:
        //1.) user_namespace (function|var|dynamic|static|final|native|override)
        //2.) my.pack.user_namespace (function|var|dynamic|static|final|native|override)
        //option number 1 is probably the 99% case so optimize for it
        ASToken nextToken = LT(1 + lookaheadOffset);
        if (token.matchesLine(nextToken))
        {
            // If the next token is an identifier check to see if it should
            // be modified to a TOKEN_NAMESPACE_ANNOTATION
            // This is so that code like:
            //    ns1 ns2 var x;
            // gets parsed correctly (2 namespace annotations, which is an error)
            if (nextToken.getType() == TOKEN_IDENTIFIER)
                processUserDefinedNamespace(nextToken, 1 + lookaheadOffset);

            switch (nextToken.getType())
            {
                case TOKEN_KEYWORD_FUNCTION:
                case TOKEN_KEYWORD_VAR:
                case TOKEN_KEYWORD_CONST:
                case TOKEN_RESERVED_WORD_NAMESPACE:
                case TOKEN_MODIFIER_DYNAMIC:
                case TOKEN_MODIFIER_FINAL:
                case TOKEN_MODIFIER_NATIVE:
                case TOKEN_MODIFIER_OVERRIDE:
                case TOKEN_MODIFIER_STATIC:
                case TOKEN_MODIFIER_VIRTUAL:
                case TOKEN_MODIFIER_ABSTRACT:
                case TOKEN_KEYWORD_CLASS:
                case TOKEN_KEYWORD_INTERFACE:
                case TOKEN_NAMESPACE_ANNOTATION:
                case HIDDEN_TOKEN_BUILTIN_NS:
                    token.setType(TOKEN_NAMESPACE_ANNOTATION);
                    return;
                case TOKEN_OPERATOR_NS_QUALIFIER: //simple name with a :: binding after it.  has to be a NS
                    token.setType(TOKEN_NAMESPACE_NAME);
                    return;
            }
            if (nextToken.getType() == TOKEN_OPERATOR_MEMBER_ACCESS)
            {
                int nextValidPart = TOKEN_IDENTIFIER;
                final ArrayList<ASToken> toTransform = new ArrayList<ASToken>(3);
                toTransform.add(token);
                toTransform.add(nextToken);
                int laDistance = lookaheadOffset + 1;
                while (true)
                {
                    nextToken = LT(++laDistance);
                    if (token.matchesLine(nextToken))
                    {
                        if (nextToken.getType() == nextValidPart)
                        {
                            nextValidPart = (nextToken.getType() == TOKEN_IDENTIFIER) ? TOKEN_OPERATOR_MEMBER_ACCESS : TOKEN_IDENTIFIER;
                            toTransform.add(nextToken);
                        }
                        else if (nextValidPart != TOKEN_IDENTIFIER && nextToken.canFollowUserNamespace())
                        {
                            // Next token is in the follow set of a namespace,
                            // so all the buffered tokens need to be converted
                            // into namespace tokens.
                            for (final ASToken ttToken : toTransform)
                            {
                                if (ttToken.getType() == TOKEN_IDENTIFIER)
                                    ttToken.setType(TOKEN_NAMESPACE_ANNOTATION);
                                else
                                    ttToken.setType(TOKEN_OPERATOR_MEMBER_ACCESS);
                            }
                            break;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Combines the text of two tokens, adding whitespace between them and
     * adjusting offsets appropriately
     * 
     * @param target the base token that we will add the next to
     * @param source the source of the text to add
     */
    private final void combineText(TokenBase target, TokenBase source)
    {
        StringBuilder text = new StringBuilder();
        text.append(target.getText());
        //add whitespace for gaps between tokens
        for (int i = 0; i < (source.getStart() - target.getEnd()); i++)
        {
            text.append(" ");
        }
        text.append(source.getText());
        target.setText(poolString(text.toString()));
        target.setEnd(target.getStart() + text.length());
    }

    /**
     * Unicode pattern for {@code \u0000}.
     */
    private static final Pattern UNICODE_PATTERN = Pattern.compile(BaseRawASTokenizer.PATTERN_U4);

    /**
     * Leading characters of a unicode pattern.
     */
    private static final String UNICODE_LEADING_CHARS = "\\u";

    /**
     * Convert escaped unicode sequence in a string. For example:
     * {@code foo\u0051bar} is converted into {@code fooQbar}.
     * 
     * @param text input string
     * @return converted text
     */
    static String convertUnicode(final String text)
    {
        // Calling Pattern.matcher() is much slower than String.contains(), so
        // we need this predicate to skip unnecessary RegEx computation.
        if (text.contains(UNICODE_LEADING_CHARS))
        {
            final StringBuilder result = new StringBuilder();
            final Matcher matcher = UNICODE_PATTERN.matcher(text);
            int start = 0;
            while (matcher.find())
            {
                result.append(text, start, matcher.start());
                result.append(Character.toChars(BaseRawASTokenizer.decodeEscapedUnicode(matcher.group())));
                start = matcher.end();
            }
            result.append(text, start, text.length());
            return result.toString();
        }
        else
        {
            return text;
        }
    }

    /**
     * Gets the source path to the file being tokenized.
     */
    public String getSourcePath()
    {
        return sourcePath;
    }
}
