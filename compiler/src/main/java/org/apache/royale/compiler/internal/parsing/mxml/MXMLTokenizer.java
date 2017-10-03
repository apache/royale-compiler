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

package org.apache.royale.compiler.internal.parsing.mxml;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.MutablePrefixMap;
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.IMXMLTokenizer;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.problems.MXMLUnclosedTagProblem;
import org.apache.royale.utils.NonLockingStringReader;

/**
 * Tokenizes MXML files.  Uses RawTagTokenizer to get basic tokens.  Ignores comments (<!--...-->),
 * processing instructions (<?...?>), and whitespace.  Replaces CDATA tokens with text tokens (strips
 * out the cdata stuff.
 */
public class MXMLTokenizer implements IMXMLTokenizer, Closeable
{
	/**
	 * Start offset (for when you're parsing a section of the document that
	 * doesn't start at the beginning)
	 */
	protected int startOffset;
	
	private int tagDepth = -1;
	
	/**
	 * Specifies that we are within a tags content, ie inside &lt; and &gt;
	 */
	private boolean inTagContent = false;

	private RawMXMLTokenizer tokenizer;
	
	protected MXMLToken xmlNSToken = null;

	protected MutablePrefixMap rootPrefixMap;

	private MXMLToken postRepairToken = null; 
	
	private boolean isRepairing = true;
	
	private boolean wasRepaired = false;
	
	private static final int SIZE = 100;
	
	private List<ICompilerProblem> problems;

    private String path;
    
    private MXMLToken lastToken = null;
    
    private static final String SUB_SYSTEM = "MXMLTokenizer";

	/**
	 * Constructor
	 */
	public MXMLTokenizer(String path)
	{
	    tokenizer = new RawMXMLTokenizer();
	    tokenizer.setSourcePath(path);
        problems = new ArrayList<ICompilerProblem>();
        rootPrefixMap = new MutablePrefixMap();
        this.path = path;
	}
	
	public MXMLTokenizer() {
	    this("");
	}
	
	public MXMLTokenizer(IFileSpecification specification) {
	    this(specification.getPath());
	}
	
	/**
	 * Reparse constructor.  Allows you to start the tokenizer with a start
	 * offset (for when you're parsing a section of the document that doesn't
	 * start at the beginning).
	 * @param startOffset		Start offset
	 */
	public MXMLTokenizer(int startOffset)
	{
		this("");
	    this.startOffset = startOffset;
	}
	
	public void setPath(String path) {
	    this.path = path;
	    tokenizer.setSourcePath(path);
	}
	
	public void setReader(Reader reader) {
	    tokenizer.reset();
        tokenizer.yyreset(reader);
	}
	
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
	 * If it exists, return the PrefixMap from the last parse
	 * @return a {@link PrefixMap} or null
	 */
	public PrefixMap getPrefixMap() {
		return rootPrefixMap;
	}
	
	/**
	 * Sets a flag to indicate whether this tokenizer should try to repair its token stream
	 * @param isRepairing <code>true</code> to repair, <code>false</code> to not repair
	 */
	@Override
    public void setIsRepairing(boolean isRepairing) {
		this.isRepairing = isRepairing;
	}
	
	@Override
    public IMXMLToken[] getTokens(Reader reader) {
		List<MXMLToken> parseTokens = parseTokens(reader);
		return parseTokens.toArray( new IMXMLToken[0]);
	}

	@Override
    public IMXMLToken[] getTokens(String range) {
		List<MXMLToken> parseTokens = parseTokens(new NonLockingStringReader(range));
		return parseTokens.toArray( new IMXMLToken[0]);
	}
	
	/**
	 * Determines if the the tokenizer has encountered any problems as it lexed the given input
	 * @return true if we have encountered any problems
	 */
	public boolean hasTokenizationProblems() {
		return tokenizer.hasProblems() || problems.size() > 0;
	}
	
	/**
	 * Processes the given input and builds a {@link PrefixMap} for the root tag found within this document
	 */
	public PrefixMap getRootTagPrefixMap() {
		boolean cont = true;
		do {
			MXMLToken token = nextToken();
			if(token == null || token.isTagEnd()) {
				cont = false;
			}
		} while(cont);
		return rootPrefixMap;
	}

	/**
	 * Returns a collection of problems encountered while processing the given input
	 * @return a {@link Collection} of {@link ICompilerProblem} objects, or an empty {@link Collection}
	 */
	public List<ICompilerProblem> getTokenizationProblems() {
		ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(this.problems);
		problems.addAll(tokenizer.getProblems());
		return problems;
	}
	
	/**
     * Returns the next token that can be produced from the given input, without performing any repair code
     * @return an {@link MXMLToken} or null when no more tokens can be produced
     */
	private final MXMLToken nextTokenInternal() {
        try
        {
            MXMLToken token = tokenizer.hasBufferToken() ? (MXMLToken)tokenizer.getBufferToken() : (MXMLToken)tokenizer.nextToken();
            if(token == null)
                return null;
            MXMLToken mxmlToken = processToken(token);
            return mxmlToken;
        }
        catch (Exception e)
        {
            ICompilerProblem problem = new InternalCompilerProblem2(path, e, SUB_SYSTEM); 
            problems.add(problem);
        }
        return null;
	}

	
	/**
	 * Returns the next token that can be produced from the given input
	 * @return an {@link MXMLToken} or null when no more tokens can be produced
	 */
	public MXMLToken nextToken() {
	    if(isRepairing) {
	        if(postRepairToken != null) {
                MXMLToken retVal = postRepairToken;
                postRepairToken = null;
                return retVal;
            }
	        MXMLToken mxmlToken = nextTokenInternal();
            MXMLToken addedToken = analyzeForEndTagProblems(mxmlToken);
            if(addedToken != null) {
                postRepairToken = mxmlToken;
                wasRepaired = true;   
                return addedToken;
            }
            return mxmlToken;
	    }
	    return nextTokenInternal();
	}

	/**
	 * Parse the contents of input
	 * @param input		Reader containing file to be parsed
	 * @return			List of MXMLTokens
	 */
	public List<MXMLToken> parseTokens(Reader input) {
		// Add fake characters onto the end of the stream to make it easier to handle
		// unclosed constructs like <![CDATA[ and <!--.
		wasRepaired = false;
		setReader(input);
		// Set the start offset in the tokenizer
		// This is done after setReader() as setReader() resets the tokenizer, setting yychar to 0
		tokenizer.setOffset(startOffset);
		MXMLToken token = null;
		List<MXMLToken> list = new ArrayList<MXMLToken>(SIZE);
		try {
			do {
			    token = nextToken();
			    if(token != null)
			        buildTokenList((MXMLToken)token.clone(), list);
			    
			}while(token != null);
			lastToken = null;
			return list;
		} finally {
			try {
				tokenizer.yyclose();
			} catch (IOException e) {
			    ICompilerProblem problem = new InternalCompilerProblem2(path, e, SUB_SYSTEM);
			    problems.add(problem);
			}
		}
	}
	
	// TODO: remove this. It now does nothing. See note below
	private MXMLToken analyzeForEndTagProblems(MXMLToken currentToken) {
        if(currentToken == null)
            return null;
        try {
            
            if(currentToken.isTagStart() && lastToken != null) {
                switch(lastToken.getType()) {
                    case MXMLTokenTypes.TOKEN_WHITESPACE:
                    case MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION:
                    case MXMLTokenTypes.TOKEN_COMMENT:
                    case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
                    case MXMLTokenTypes.TOKEN_STRING:
                    case MXMLTokenTypes.TOKEN_TEXT:
                    case MXMLTokenTypes.TOKEN_CDATA:
                    case MXMLTokenTypes.TOKEN_TAG_END:
                    case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
                    case -1:
                        return null; //all legal to come before open tag start
                    default:
                        // turn off this logic that makes up a fake token. The MXMLData already
                        // known how do to this. And if we do it here, we lose the information that the repair
                        // was done. Since we actually care, this causes bugs.
                        return null;
                      
                }
            } 
            return null;
        }
        finally
        {
            lastToken = currentToken;
        }
    }
	
	/**
	 * Determines if any tokens were added as a side effect of repair.  This can only be called after a tokenize call
	 * @return true if the token stream was modified
	 */
	public boolean tokensWereRepaired() {
		return wasRepaired;
	}
	
	/**
	 * Processes tokens, performs various transforms on the tokens that we return, such as:
	 * <ul>
	 * <li>transform XMLNS style tokens to name tokens for easier consumption by clients</li>
	 * <li>filter out state combiner tokens</li>
	 * <li>track xmlns string values</li>
	 * </ul>
	 * Note that we don't modify/merge whitespace and text tokens here as there are a number
	 * of tests which are sensitive to whitespace, ie MetaMXMLSuite.
	 * @param token
	 * @return an {@link MXMLToken} or null if it was not accepted
	 */
	private MXMLToken processToken(final MXMLToken token) {
		
		if (lastToken != null && lastToken.getType() == MXMLTokenTypes.TOKEN_CLOSE_TAG_START &&
			token.getType() != MXMLTokenTypes.TOKEN_TAG_END)
		{
			// once we hit this condition, we currently stop parsing.  
			// There is a condition where the last closing tag of the file in unclosed
			// as in "<js:Application" (no closing ">") and the lexer
			// can't seem to detect that and stop lexing.  Yes, this means that
			// if a bad closing tag occurs higher up in the file we'll bail
			// and not report errors later in the file, but that's better than
			// hanging, IMO.
			problems.add(new MXMLUnclosedTagProblem(token, lastToken.getText()));
			return null;
		}
	    //TODO find xmlns uri values in the lexer instead of here
	    switch (token.getType())
        {
            // tags (and also DTD directives)
            case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
            	tagDepth++;
            	inTagContent = true;
            	return token;
            case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
            	tagDepth--;
            	inTagContent = true;
            	return token;
            case MXMLTokenTypes.TOKEN_TAG_END:
            case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
                inTagContent = false;
                return token;
            // stuff inside tags
            case MXMLTokenTypes.TOKEN_EQUALS:
            //outside tags
            case MXMLTokenTypes.TOKEN_CDATA:
                return token;
            case MXMLTokenTypes.TOKEN_NAME:
                xmlNSToken = null;
                return token;
            case MXMLTokenTypes.TOKEN_XMLNS:
                token.setType(MXMLTokenTypes.TOKEN_NAME);
                xmlNSToken = token;
                return token;
            case MXMLTokenTypes.TOKEN_STRING:
                //if the current namespace we are tracking is not null, then this string should yield the namespace URI
            	//only track the namespace of the root document
            	if(xmlNSToken != null && tagDepth == 0) {
                    String prefix = "";
                    String text = xmlNSToken.getText();
                    if(text.length() > 5) { //has prefix
                        prefix = text.substring(6);
                    }
                    String nsText = token.getText();
                    String ns = nsText.length() > 1 ? 
                            nsText.substring(1, nsText.length() -1) : "";
                    rootPrefixMap.add(prefix, ns);
                }
                return token;
            // stuff outside tags
            default:
            {
                if(tagDepth != 0 && !tokenizer.isInE4XDatabinding() && !inTagContent) {
                    //probably mixed content.  Allow it and let it fail downstream if we're wrong
                    if(token.isLiteral() || token.getType() == ASTokenTypes.TOKEN_IDENTIFIER) {
                        token.setType(MXMLTokenTypes.TOKEN_TEXT);
                    }
                }
                return token;
            }
        }
	}

	/**
	 * Handles the addition of tokens to the internal token list.  Subclasses should override this method to handle
	 * different tokenizing strategies
	 * @param token The current token.
	 * @param list The list of tokens being built.
	 */
	protected void buildTokenList(MXMLToken token, List<MXMLToken> list)
	{
		if(token != null) {
		    list.add(token);
		}
	}
	
	public static void main(String[] args)
	{
        final FileSpecification fileSpec = new FileSpecification(args[0]);
        
        final MXMLTokenizer tokenizer = new MXMLTokenizer(fileSpec.getPath());
        try
        {
            List<MXMLToken> tokens = tokenizer.parseTokens(fileSpec.createReader());
            for (MXMLToken token : tokens)
            {
                System.out.println(token.toDumpString());
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
	}
}
