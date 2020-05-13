header
{
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

/*
 * This file is generated from ASTreeAssembler.g
 * DO NOT MAKE EDITS DIRECTLY TO THIS FILE.  THEY WILL BE LOST WHEN THE FILE IS GENERATED AGAIN!!!
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IASToken.ASTokenKind;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.internal.tree.as.metadata.*;
import org.apache.royale.compiler.asdoc.IASParserASDocDelegate;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.*;

}

/**
 * ActionScript3 parser grammar. It consumes ASTokens and produces IASNode AST.
 * The number of tokens in a single syntactic predicate can not be greater than
 * StreamingTokenBuffer.REWIND_BUFFER_SIZE.
 *
 * @see <a href="https://zerowing.corp.adobe.com/display/FlashPlayer/ActionScript+Language+Specification">ActionScript Language Syntax Specification</a>
 */
class ASParser extends Parser("org.apache.royale.compiler.internal.parsing.as.BaseASParser");
 
options
{ 
	exportVocab = AS;
	defaultErrorHandler = false;
}	

tokens
{	
	// These hidden tokens are matched by the raw tokenizer but are not sent to the parser.
	HIDDEN_TOKEN_COMMENT; 
	HIDDEN_TOKEN_SINGLE_LINE_COMMENT; 
	HIDDEN_TOKEN_STAR_ASSIGNMENT; 
	HIDDEN_TOKEN_BUILTIN_NS;
	HIDDEN_TOKEN_MULTI_LINE_COMMENT; 
	
	// These two tokens are used by code model's ASDoc tokenizer.
	TOKEN_ASDOC_TAG; 
	TOKEN_ASDOC_TEXT; 
	
	// These tokens are transformed from reserved words by StreamingASTokenizer.
	TOKEN_RESERVED_WORD_EACH; 
	TOKEN_RESERVED_WORD_CONFIG;
	TOKEN_KEYWORD_INCLUDE;
	TOKEN_RESERVED_WORD_GOTO; 
}

{

    /**
     * Construct an AS3 parser from a token buffer.
     */
    public ASParser(IWorkspace workspace, IRepairingTokenBuffer buffer) 
    {
    	super(workspace, buffer);
    	tokenNames = _tokenNames;
    }

	/**
     * Construct an AS3 parser for parsing command line config args
     */
	public ASParser(IWorkspace workspace, IRepairingTokenBuffer buffer, boolean parsingProjectConfigVariables) 
    {
    	super(workspace, buffer, parsingProjectConfigVariables);
    	tokenNames = _tokenNames;
    }
}


/**
 * Matches multiple directives. This layer is added to handle parsing error in directives.
 */
fileLevelDirectives[ContainerNode c]
    :   (directive[c, NO_END_TOKEN])*
    ;
    exception catch [RecognitionException parserError] { handleParsingError(parserError);  }

/**
 * Matches a "directive" level input.
 * The first couple of alternatives gated with semantic predicates are used to
 * either disambiguate inputs, or to trap erroneous syntax.
 */
directive[ContainerNode c, int endToken]
{
    final ASToken lt1 = LT(1);
    final ASToken lt2 = LT(2);
    final int la1 = LA(1);
    final int la2 = LA(2);
    final int la3 = LA(3);
    final int la4 = LA(4);
}
    :   { la1 == TOKEN_BLOCK_OPEN }? groupDirective[c, endToken]
    |   { la1 == TOKEN_RESERVED_WORD_NAMESPACE && la2 == TOKEN_PAREN_OPEN }? statement[c, endToken] 
    |   { la1 == TOKEN_IDENTIFIER && la2 == TOKEN_NAMESPACE_ANNOTATION && lt1.getLine() == lt2.getLine() }?
        // Skip over the user-defined namespace name and continue.
        nsT:TOKEN_IDENTIFIER attributedDefinition[c]
        { trapInvalidNamespaceAttribute((ASToken)nsT); }
    |   asDocComment
    |   importDirective[c]
    |   useNamespaceDirective[c] 
    |   { la1 == TOKEN_NAMESPACE_NAME && 
    	  la2 == TOKEN_OPERATOR_NS_QUALIFIER && 
    	  la3 == TOKEN_IDENTIFIER && 
    	  la4 == TOKEN_BLOCK_OPEN}?
        // ns::var { ... }
        groupDirectiveWithConfigVariable[c, endToken]
    |   { la1 == TOKEN_NAMESPACE_NAME &&
    	  la2 == TOKEN_OPERATOR_NS_QUALIFIER &&
    	  la3 == TOKEN_NAMESPACE_ANNOTATION }?
        // ns::var private var foo:int;
        attributedDefinition[c]  
    |   { la1 == TOKEN_NAMESPACE_NAME &&
    	  la2 == TOKEN_OPERATOR_NS_QUALIFIER }? 
        // "ns::var" or "ns::[x, y]"
        statement[c,endToken]
    |   { !isFunctionClosure() }? 
        attributedDefinition[c]  
    |   packageDirective[c]
    |   statement[c,endToken]
    |   configNamespace[c]  
    |   includeDirective
    // The following alternatives are error traps
    |   fT:TOKEN_KEYWORD_FINALLY   { reportUnexpectedTokenProblem((ASToken)fT); }
    |   cT:TOKEN_KEYWORD_CATCH     { reportUnexpectedTokenProblem((ASToken)cT); }
    ;
    exception catch [RecognitionException ex]
    { 
    	handleParsingError(ex, endToken);  
    	consumeUntilKeywordOrIdentifier(endToken); 
    }
	
/**
 * Include processing is usually done in the lexer. However, this rule is added
 * in order to support code model partitioner whose tokenizer is set to not
 * "follow includes". In a normal AS3 compilation, the parser would never see
 * the "include" token. 
 */
includeDirective
    :   TOKEN_KEYWORD_INCLUDE TOKEN_LITERAL_STRING 
    ;
    
/**
 * Matches an attributed definition. An "attribute" can be a namespace or a 
 * modifier.
 */
attributedDefinition[ContainerNode c]	
{ 
    List<INamespaceDecorationNode> namespaceAttributes = new ArrayList<INamespaceDecorationNode>();
    List<ModifierNode> modifiers = new ArrayList<ModifierNode>(); 
    INamespaceDecorationNode namespaceAttr = null;
	IASNode lastChild = null;
	if(c.getChildCount() > 0)
	{
		lastChild = c.getChild(c.getChildCount() - 1);
	}
	ConfigConditionBlockNode configBlock = null;
    
    boolean enabled = isDefinitionEnabled(c);
    boolean eval = true;
}
    :   (eval=configConditionOfDefinition)?
        {
        	// A configuration condition variable can either be matched by
        	// the above rule or be transformed into a LiteralNode of boolean
        	// type. If either is evaluated to false, the definition is disabled.
        	enabled &= eval;
            if (!enabled)
			{
				// previously, we removed the entire definition from the AST,
				// but some IDEs can "fade out" disabled definitions. by adding
				// the children to a ConfigConditionBlockNode, they can be seen
				// in the AST, but ignored when generating compiled output. -JT
			    configBlock = new ConfigConditionBlockNode(false); 
				if(lastChild != null && c.getRemovedConditionalCompileNode())
				{
					configBlock.startBefore(lastChild);
				}
				c.addItem(configBlock);
				c = configBlock;
			}
        }
        (attribute[modifiers, namespaceAttributes])* 
        {
            // Verify that at most one namespace attribute is matched.
            verifyNamespaceAttributes(namespaceAttributes);
            
            if (!namespaceAttributes.isEmpty())
               namespaceAttr = namespaceAttributes.get(0);
        }
        definition[c, namespaceAttr, modifiers]
		{
			if(configBlock != null)
			{
				// since this config block doesn't have braces {}, check if
				// there's a semicolon at the end of the definition
				Token prevToken = buffer.previous();
				if(prevToken.getType() == TOKEN_SEMICOLON)
				{
					configBlock.endAfter(prevToken);
				}
			}
		}
    	exception catch [RecognitionException ex]
    	{ 
    		handleParsingError(ex);  
    	}
    ;

/**
 * Matches an attribute such as:
 * - Modifiers: dynamic, final, native, override, static, virtual, abstract.
 * - Namespace names.
 * - Reserved namespace names: internal, private, public, protected.
 *
 * A definition can have at most one "namespace attribute".
 * The matched attribute is added to the lists passed in as arguments.
 */
attribute [List<ModifierNode> modifiers, List<INamespaceDecorationNode> namespaceAttributes] 
{
    ExpressionNodeBase namespaceNode = null; 
    ExpressionNodeBase configAsNamespaceNode = null; 
    ModifierNode modifierNode = null;
}
    :   modifierNode=modifierAttribute
        {   
            if (modifierNode != null)
                modifiers.add(modifierNode);
        }
    |   namespaceNode=namespaceModifier 
        {
            if (namespaceNode instanceof INamespaceDecorationNode)
                namespaceAttributes.add((INamespaceDecorationNode) namespaceNode); 
        }
    |   configAsNamespaceNode=configConditionAsNamespaceModifier
        {
            if (configAsNamespaceNode instanceof INamespaceDecorationNode)
                namespaceAttributes.add((INamespaceDecorationNode) configAsNamespaceNode); 
        }
    ;
	
configConditionAsNamespaceModifier returns [ExpressionNodeBase n]
{
    n = null; 
}
    :   ns:TOKEN_NAMESPACE_NAME op:TOKEN_OPERATOR_NS_QUALIFIER id:TOKEN_NAMESPACE_ANNOTATION
        { final NamespaceIdentifierNode nsNode = new NamespaceIdentifierNode((ASToken)ns); 
          nsNode.setIsConfigNamespace(isConfigNamespace(nsNode));
	  final IdentifierNode idNode = new IdentifierNode((ASToken)id);
	  final IdentifierNode idNode2 = (IdentifierNode)transformToNSAccessExpression(nsNode, (ASToken) op, idNode);
          n = new NamespaceIdentifierNode(idNode2.getName());
          n = n.copyForInitializer(null);
	  n.setSourcePath(nsNode.getSourcePath());
	  n.setLine(nsNode.getLine());
	  n.setColumn(nsNode.getColumn());
	  n.setEndLine(idNode.getEndLine());
	  n.setEndColumn(idNode.getEndColumn());
	  n.setStart(nsNode.getStart());
	  n.setEnd(idNode.getEnd());
        }
    ;
	
/**
 * Matches a definition of variable, function, namespace, class or interface.
 */
definition[ContainerNode c, INamespaceDecorationNode ns, List<ModifierNode> modList]
    :   variableDefinition[c, ns, modList]
    |   functionDefinition[c, ns, modList]
    |   namespaceDefinition[c, ns, modList]
    |   classDefinition[c, ns, modList]
    |   interfaceDefinition[c, ns, modList]
    ;
	
/**
 * Matches a "group" in a "group directive". 
 * Entering a "Block" leaves the global context, but entering a "Group" doesn't.
 */
groupDirective[ContainerNode c, int endToken]
{ 
    BlockNode b = new BlockNode(); 
    enterGroup();
}
    :   openT:TOKEN_BLOCK_OPEN      { b.startAfter(openT); }
        (directive[c, endToken])*
    	{ if(b.getChildCount() > 0) c.addItem(b); }
    	closeT:TOKEN_BLOCK_CLOSE    { b.endBefore(closeT); leaveGroup(); }
    ;

/**
 * Matches a config condition such as "CONFIG::debug". This rule only applies 
 * to blocks gated with configuration variable.
 *
 * @return Evaluated result of the configuration variable.
 */
configCondition returns [boolean result]
{
	result = false;
}
    :   ns:TOKEN_NAMESPACE_NAME op:TOKEN_OPERATOR_NS_QUALIFIER id:TOKEN_IDENTIFIER
        {
            result = evaluateConfigurationVariable(new NamespaceIdentifierNode((ASToken)ns), (ASToken) op, new IdentifierNode((ASToken)id));
        }
    ;
    
/**
 * Similar to "configCondition", only that the token type after "::" is 
 * "TOKEN_NAMESPACE_ANNOTATION". This rule only applies to "attributed 
 * definitions".
 */
configConditionOfDefinition returns [boolean result]
{
	result = false;
}
    :   ns:TOKEN_NAMESPACE_NAME op:TOKEN_OPERATOR_NS_QUALIFIER id:TOKEN_NAMESPACE_ANNOTATION
        {
            result = evaluateConfigurationVariable(new NamespaceIdentifierNode((ASToken)ns), (ASToken) op, new IdentifierNode((ASToken)id));
        }
    ;
    
/**
 * Matches a group of directives gated with configuration variable.
 *
 *     CONFIG::debug {
 *         trace("debugging code");
 *     }
 *
 * If the configuration variable evaluates to false, the following block will
 * not be added to the resulting AST.
 */
groupDirectiveWithConfigVariable [ContainerNode c, int endToken]
{
    boolean b;
    ConfigConditionBlockNode block;
    final Token lt = LT(1);
}
    :   b=configCondition   
        {
        	block = new ConfigConditionBlockNode(b);
        	block.startBefore(lt);
        	c.addItem(block);
        }
        groupDirective[block, endToken]
		{
			Token prevToken = buffer.previous();
			if(prevToken.getType() == endToken)
			{
				block.endAfter(prevToken);
			}
		}
    ;
	
/**
 * Matches a statement.
 *
 * Note that the "SuperStatement" in ASL syntax spec is not explicitly defined.
 * The "super" statements like <code>super(args);</code> are matched as regular
 * "call" expressions.
 */
statement[ContainerNode c, int exitCondition]
{
    final int la1 = LA(1);
    final int la2 = LA(2);
}
    :   breakOrContinueStatement[c]
    |   defaultXMLNamespaceStatement[c]
    |   gotoStatement[c]                 
    |   emptyStatement
    |   { la1 == TOKEN_IDENTIFIER && la2 == TOKEN_COLON }? labeledStatement[c, exitCondition]
    |   { la1 != TOKEN_SQUARE_OPEN && 
    	  la1 != TOKEN_OPERATOR_LESS_THAN && 
    	  la1 != TOKEN_BLOCK_OPEN }? expressionStatement[c]
    |   forStatement[c]
    |   ifStatement[c]
    |   meta[c]
    |   returnStatement[c]
    |   switchStatement[c]
    |   throwsStatement[c]
    |   tryStatement[c]
    |   whileStatement[c]
    |   doStatement[c]
    |   withStatement[c]
    ;
    exception catch [RecognitionException ex]
    { 
        handleParsingError(ex);  
        consumeUntilKeywordOrIdentifier(exitCondition); 
    }
    
/**
 * Matches an "expression statement". The ASL syntax specification says the 
 * lookahead can not be "[", "{" or "function". Legacy code requires that "<"
 * be excluded as well.
 */
expressionStatement[ContainerNode c]
{ 
    ExpressionNodeBase e = null; 
    
    if (LA(1) == TOKEN_KEYWORD_FUNCTION)
    {
        // Recover: continue parsing function as an anonymous function.
        logSyntaxError(LT(1));
    }
}
    :   e=expression
        {
            c.addItem(e);
            if (!matchOptionalSemicolon())
            {
                recoverFromExpressionStatementMissingSemicolon(e);
            }
        }
    ;

/**
 * <h1>From ASL syntax spec:</h1>
 * <quote>
 * InnerSubstatement is defined in the grammar for the sole purpose of 
 * specifying side conditions that disambiguate various syntactic ambiguities 
 * in a context-sensitive manner specified in Section 5.
 * </quote>
 *
 * It is only used in "do statement" and "if statement" to loosen the following
 * two cases allowed by AS3 but not by ECMA5.
 *
 * <code>
 * do x++ while (x < 10);     // ES5 would require a ; after x++
 * if (x > 10) x++ else y++;  // ES5 would require a ; after x++
 * <code>
 */
innerSubstatement[ContainerNode c]
    :   substatement[c] { afterInnerSubstatement(); }
    ;
    
/**
 * Matches a sub-statement. 
 */
substatement[ContainerNode c]
    :   (   { LA(1) != TOKEN_BLOCK_OPEN }? statement[c,NO_END_TOKEN]
        |   block[c] 
        |   variableDefinition[c, null, null]
        )
        {
        	if (c.getContainerType() == ContainerType.SYNTHESIZED)
            	c.setContainerType(ContainerType.IMPLICIT);
        }
    ;

/**
 * Matches a "labeled statement". For example:
 *
 *     innerLoop: x++;
 *
 */
labeledStatement[ContainerNode c, int exitCondition]
{
	LabeledStatementNode statementNode = null;
	ASToken offendingNSToken = null;
}
    :   labelT:TOKEN_IDENTIFIER TOKEN_COLON 
        { 
            final NonResolvingIdentifierNode labelNode = 
            	new NonResolvingIdentifierNode(
                    labelT != null ? labelT.getText() : "",
                    labelT);	
            statementNode = new LabeledStatementNode(labelNode);
            c.addItem(statementNode);
        }
        (   { LA(1) == TOKEN_RESERVED_WORD_NAMESPACE && LA(2) == TOKEN_IDENTIFIER }?
            { offendingNSToken = LT(1); }
            namespaceDefinition[c, null, null] 
            { trapInvalidSubstatement(offendingNSToken); }
        |   substatement[statementNode.getLabeledStatement()]
        )
    ;
		 	
/**
 * Matches a block.
 */
block[ContainerNode b]
    :   openT:TOKEN_BLOCK_OPEN      
        { 
        	b.startAfter(openT); 
            b.setContainerType(ContainerType.BRACES);
        }
        (directive[b, TOKEN_BLOCK_CLOSE])*
        closeT:TOKEN_BLOCK_CLOSE    { b.endBefore(closeT); }
    ;
    exception catch [RecognitionException ex]
    { 
        handleParsingError(ex);   
        consumeUntilKeywordOrIdentifier(TOKEN_BLOCK_CLOSE); 
        endContainerAtError(ex, b);
    }

/**
 * Matches an import directive.
 *
 *     import flash.display.Sprite;
 *     import flash.events.*;
 */
importDirective[ContainerNode c]
{  
	ExpressionNodeBase n = null; 
	ImportNode i = null; 
	IIdentifierNode alias = null;
}
    :   importT:TOKEN_KEYWORD_IMPORT 
    	{
    		i = new ImportNode((ExpressionNodeBase) null);
    		i.startBefore(importT);
    		i.endAfter(importT); 
    		c.addItem(i);
			if (LA(2) == TOKEN_OPERATOR_ASSIGNMENT)
			{
				alias = identifier();
				match(TOKEN_OPERATOR_ASSIGNMENT);
			}
    	}

        n=importName
    	{
     		if(n != null) {
				if(alias != null) {
					i.setImportAlias(alias.getName());
				}
     			i.setImportTarget(n);
     			i.setEnd(n.getEnd());
     			encounteredImport(i);
     		} 
     		else {
     			i.setImportTarget(new IdentifierNode(""));
     		}
     		matchOptionalSemicolon();
    	}
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches "use namespace ns" directive.
 */
useNamespaceDirective[ContainerNode c]
{  
	ExpressionNodeBase n = null;
	UseNamespaceNode u = null;
}
    :   useT:TOKEN_KEYWORD_USE { u = new UseNamespaceNode(n); u.startBefore(useT); c.addItem(u); }
        nsT:TOKEN_RESERVED_WORD_NAMESPACE { u.endAfter(nsT); } n=restrictedName
    	{
    		u.setTargetNamespace(n);
    		u.setEnd(n.getEnd());
    		matchOptionalSemicolon();
    	}
    ;
    exception catch [RecognitionException ex] 
	{ 
		if (u != null && u.getTargetNamespace() == null) 
			u.setTargetNamespace(handleMissingIdentifier(ex));
		else 
			handleParsingError(ex);  
	}

/**
 * Matches an ASDoc block.
 */		 
asDocComment 
    :   asdocT:TOKEN_ASDOC_COMMENT 
		{
			asDocDelegate.setCurrentASDocToken(asdocT);
		}
	;

/**
 * Matches a "modifier attribute" such as "final", "dynamic", "override", 
 * "static" or "native".
 */
modifierAttribute returns [ModifierNode modifierNode]
{ 
    modifierNode = null;
    final ASToken modifierT = LT(1); 
}
    :   (   TOKEN_MODIFIER_FINAL
        |   TOKEN_MODIFIER_DYNAMIC
        |   TOKEN_MODIFIER_OVERRIDE
        |   TOKEN_MODIFIER_STATIC
        |   TOKEN_MODIFIER_NATIVE
        |   TOKEN_MODIFIER_VIRTUAL
        |   TOKEN_MODIFIER_ABSTRACT
        )
        { modifierNode = new ModifierNode((ASToken) modifierT);	}
	;
	
 
/**
 * Matches a namespace modifier on an "attributed definition".
 */
namespaceModifier returns[ExpressionNodeBase n]
{ 
    n = null;
}
    :   nsPart1T:TOKEN_NAMESPACE_ANNOTATION
    	{ 
        	// If our text token is a member access, then build a normal 
        	// identifier. Otherwise, build a NS specific one.
        	
        	if (LA(1) == TOKEN_OPERATOR_MEMBER_ACCESS) 
        	{
                n = new IdentifierNode((ASToken)nsPart1T) ;
            }
            else
            {
                final NamespaceIdentifierNode nsNode = new NamespaceIdentifierNode((ASToken)nsPart1T); 
        		nsNode.setIsConfigNamespace(isConfigNamespace(nsNode));
        		n = nsNode;
        	}
    	}
    	(
            dotT:TOKEN_OPERATOR_MEMBER_ACCESS
            (
                nsNameT:TOKEN_NAMESPACE_ANNOTATION
        		{
        			IdentifierNode id = new IdentifierNode((ASToken)nsNameT);
        			n = new FullNameNode(n, (ASToken) dotT, id);
        		}
        	)
    	)*
    	{
            if (n instanceof FullNameNode) 
        	   n = new QualifiedNamespaceExpressionNode((FullNameNode)n);
    	}
    
    ;

/**
 * Matches a "metadata statement".
 *
 *     [ExcludeClass()]
 *     [Bindable]
 */
meta[ContainerNode c]
{
	ArrayLiteralNode al = new ArrayLiteralNode(); 
	final ASToken lt = LT(1);
}
    :   attributeT:TOKEN_ATTRIBUTE
        { 
        	// Note that a separate parser is invoked here for metadata.
            parseMetadata(attributeT, errors); 
            preCheckMetadata(attributeT, c);
    	} 
	|   { isIncompleteMetadataTagOnDefinition() }?
	    TOKEN_SQUARE_OPEN
	    // Error trap for "[" before a definition item.
	    { logSyntaxError(LT(1)); } 
    |   arrayInitializer[al]  
        // This is statement-level metadata.
        {
        	// Synthesize a MetaTagsNode to hold the metadata offsets.
            currentAttributes = new MetaTagsNode();
            currentAttributes.span(al, al);
            preCheckMetadata(lt, c);
        }
    ;
 	exception catch [RecognitionException ex] 
 	{ 
 		recoverFromMetadataTag(c, al); 
	}
 	
/**
 * Matches a "config namespace foo" directive.
 */
configNamespace[ContainerNode c]
	:   TOKEN_RESERVED_WORD_CONFIG TOKEN_RESERVED_WORD_NAMESPACE configN:TOKEN_IDENTIFIER 
		{
			NamespaceNode cNode = new ConfigNamespaceNode(new IdentifierNode((ASToken)configN));
			addConditionalCompilationNamespace(cNode);
            matchOptionalSemicolon();
		}
	    
	;
	exception catch [RecognitionException ex]
	{ handleParsingError(ex); }

/**
 * Matches a "package" block.
 *
 *     package mx.controls { ... }
 *
 */
packageDirective[ContainerNode c]
{ 
	PackageNode p = null; 
	ExpressionNodeBase name = null; 
	BlockNode b = null; 
}	
    :   packageT:TOKEN_KEYWORD_PACKAGE { enterPackage((ASToken)packageT); }
        (name=packageName)?
        {
        	p = new PackageNode(name != null ? name : IdentifierNode.createEmptyIdentifierNodeAfterToken(packageT), (ASToken)packageT);
        	p.startBefore(packageT);
        	c.addItem(p);
        	b = p.getScopedNode();
        }
        (   openT:TOKEN_BLOCK_OPEN { b.startAfter(openT); }
        	packageContents[b]
        )
        { leavePackage(); }
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); leavePackage();  }
	
/**
 * Matches a package name such as:
 *     org.apache.royale
 *
 * A Whitespace or LineTerminator is allowed around a . in a PackageName. 
 * For example, the following is a syntactically valid
 * <pre>
 * package a .
 *         b
 * { }
 * </pre>
 * The resulting PackageName value is equivalent to a PackageName without any intervening Whitespace and LineTerminators.
 */	
packageName returns [ExpressionNodeBase n]
{ 
	n = null; 
	ExpressionNodeBase e = null; 
}
 	:	n=identifier 
		(options{greedy=true;}: { LA(2) != TOKEN_OPERATOR_STAR }?
			dotT:TOKEN_OPERATOR_MEMBER_ACCESS 
			{ 
				n = new FullNameNode(n, (ASToken) dotT, null); 
			} 
			e=identifier
			{ 
				((FullNameNode)n).setRightOperandNode(e); 
			}
		)*
	;
	exception catch [RecognitionException ex] { return handleMissingIdentifier(ex, n); }	
	
/**
 * Matches contents in a package block.
 */
packageContents[ContainerNode b]
    :   (directive[b, TOKEN_BLOCK_CLOSE])* 
    	closeT:TOKEN_BLOCK_CLOSE { b.endBefore(closeT); }
    ;
    exception catch [RecognitionException ex]
	{
        if(handleParsingError(ex)) 
        {
        	//attempt to recover from the error so we can keep parsing within the block
            packageContents(b); 
        } 
        else 
        {
            endContainerAtError(ex, b); 
        } 
    }

/**
 * Matches a namespace definition.
 *
 *     namespace ns1;
 */
namespaceDefinition[ContainerNode c, INamespaceDecorationNode namespace, List<ModifierNode> modList]
{
	NamespaceNode n = null; 
	IdentifierNode id = null; 
	ExpressionNodeBase v = null; 
}
	:   nsT:TOKEN_RESERVED_WORD_NAMESPACE id=identifier
		{
			n = new NamespaceNode(id);
			n.startBefore(nsT);
			storeDecorations(n, c, namespace, modList);
            checkNamespaceDefinition(n);
		}
		(initializer[n])?
    	{ 
			c.addItem(n);
    		matchOptionalSemicolon(); 
    	}
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }	

/**
 * Matches an interface definition.
 *
 *     interface IFoo extends IBar {...}
 */
interfaceDefinition[ContainerNode c,  INamespaceDecorationNode namespace, List<ModifierNode> modList]
{ 	
	InterfaceNode interfaceNode = null; 
	IdentifierNode intName = null; 
	ExpressionNodeBase baseInterfaceName = null; 
	BlockNode b = null; 
    enterInterfaceDefinition(LT(1));
} 
    :   intT:TOKEN_KEYWORD_INTERFACE intName=identifier 
		{
			interfaceNode = new InterfaceNode(intName);
			interfaceNode.setInterfaceKeyword((IASToken)intT);
			storeDecorations(interfaceNode, c, namespace, modList);
			c.addItem(interfaceNode);
			
			// Recover from invalid interface name.
			final int la1 = LA(1);
			if (la1 != TOKEN_RESERVED_WORD_EXTENDS && la1 != TOKEN_BLOCK_OPEN)
			{
			    addProblem(new SyntaxProblem(LT(1)));
			    consumeUntilKeywordOr(TOKEN_BLOCK_OPEN);
			}
		}
		
		(	extendsT:TOKEN_RESERVED_WORD_EXTENDS
			{ interfaceNode.setExtendsKeyword((ASToken)extendsT); }
			(	baseInterfaceName=restrictedName
				{ 
					interfaceNode.addBaseInterface(baseInterfaceName);  
					interfaceNode.setEnd(baseInterfaceName.getEnd()); 
				}
 
				(	commaT:TOKEN_COMMA 
					{ interfaceNode.endAfter(commaT); }
					(	baseInterfaceName=restrictedName
						{ 
							interfaceNode.addBaseInterface(baseInterfaceName); 
							interfaceNode.setEnd(baseInterfaceName.getEnd()); 
						}
					)
				)*
			)
		)?
 
		{ 
 		  	b = interfaceNode.getScopedNode();
 		}
 	
 		openT:TOKEN_BLOCK_OPEN
			{ b.startAfter(openT); }
		classOrInterfaceBlock[b]
		{
			Token prevToken = buffer.previous();
			if(prevToken.getType() == TOKEN_BLOCK_CLOSE)
			{
				interfaceNode.endAfter(prevToken);
			}
		}
 	;
 	exception catch [RecognitionException ex] { handleParsingError(ex);  }
 
/**
 * Matches a class definition. For example:
 *
 *     class Player extends my_ns::GameObject implements IPlayer { ... }
 *
 */
classDefinition [ContainerNode c, INamespaceDecorationNode namespace, List<ModifierNode> modList]
{ 	
    IdentifierNode className = null; 
    ExpressionNodeBase superName = null; 
    ExpressionNodeBase interfaceName = null;
    ClassNode classNode = null;
    disableSemicolonInsertion();
    enterClassDefinition(LT(1));
}
    :   classT:TOKEN_KEYWORD_CLASS className=identifier
        {
            // When class name is empty, it is a synthesized IdentifierNode
            // created by the error recovery logic in "identifier" rule.
            // In such case, we fast-forward the token stream to the next
            // keyword to recover.
            if (className.getName().isEmpty())
            {
                // If the parser recover from "extends", "implements" or "{", 
                // we are could continue parsing the class definition, because
                // these tokens are the "follow set" of a class name token.
                // Otherwise, the next keyword is still a good starting point.
                consumeUntilKeywordOr(TOKEN_BLOCK_OPEN);
            }
            
            insideClass = true;
            classNode = new ClassNode(className);
            classNode.setSourcePath(((IASToken)classT).getSourcePath());
            classNode.setClassKeyword((IASToken)classT);
            storeDecorations(classNode, c, namespace, modList);
            c.addItem(classNode);
        }
         
        (   extendsT:TOKEN_RESERVED_WORD_EXTENDS 
            { classNode.setExtendsKeyword((ASToken)extendsT); }
            // The rule for super type should be "restrictedName". However, in
            // order to trap errors like "class Foo extends Vector.<T>", the
            // parser has to allow parameterized type as super name. It's up to
            // semantic analysis to report this problem.
            superName=type 
        	{ 
        		classNode.setBaseClass(superName); 
        		classNode.setEnd(superName.getEnd()); 
        	}
            exception catch [RecognitionException ex] { handleParsingError(ex); }
        )?
 
        (   impT: TOKEN_RESERVED_WORD_IMPLEMENTS 
            { classNode.setImplementsKeyword((ASToken)impT); }
            (   interfaceName=restrictedName
                { 
                    classNode.addInterface(interfaceName);
                    classNode.setEnd(interfaceName.getEnd());
                }
                (   commaT:TOKEN_COMMA 
                    { classNode.endAfter(commaT); }
                    interfaceName=restrictedName
                    { 
                        classNode.addInterface(interfaceName); 
                        classNode.setEnd(interfaceName.getEnd()); 
                    }
                )*
                exception catch [RecognitionException ex] { handleParsingError(ex); }
            )
            exception catch [RecognitionException ex] { handleParsingError(ex);  }
        )?
		
        openT:TOKEN_BLOCK_OPEN
        { classNode.getScopedNode().startAfter(openT); }
        classOrInterfaceBlock[classNode.getScopedNode()]
		{
			Token prevToken = buffer.previous();
			if(prevToken.getType() == TOKEN_BLOCK_CLOSE)
			{
				classNode.endAfter(prevToken);
			}
		}
	;
	exception catch [RecognitionException ex] { handleParsingError(ex);  }

/**
 * Matches the content block of a class definition or an interface definition.
 */
classOrInterfaceBlock[BlockNode b]
 	{ enableSemicolonInsertion(); }
	
	:   (directive[b, TOKEN_BLOCK_CLOSE])*
		closeT:TOKEN_BLOCK_CLOSE
		{ b.endBefore(closeT); }
	;
	exception catch [RecognitionException ex] 
	{ 
		if(handleParsingError(ex))  {
			classOrInterfaceBlock(b); //attempt to retry
		} else {
			endContainerAtError(ex, b); 
		}
	}
	
/**
 * Matches an anonymous function (function closure).
 */
functionExpression returns [FunctionObjectNode n]
{ 
	n = null; 
	BlockNode b = null; 
	FunctionNode f = null; 
	IdentifierNode name=null; 
	ContainerNode p = null;
}
 	:   functionT:TOKEN_KEYWORD_FUNCTION 
 	
 		// optional function name
 		(name=identifier)?
		{
			if(name == null) 
				name = IdentifierNode.createEmptyIdentifierNodeAfterToken(functionT);
			f = new FunctionNode((ASToken)functionT, name);
			n = new FunctionObjectNode(f);
			f.startBefore(functionT);
			n.startBefore(functionT);
			b = f.getScopedNode();
			disableSemicolonInsertion();
		}
 
        // function signature
    	lpT:TOKEN_PAREN_OPEN
		{
			p = f.getParametersContainerNode();
			p.startBefore(lpT);
		}
    	formalParameters[p]
    	rpT:TOKEN_PAREN_CLOSE
 		{ p.endAfter(rpT); }
    	(resultType[f])?
     	{ enableSemicolonInsertion(); }	

		// non-optional function body     		
     	lbT:TOKEN_BLOCK_OPEN
		{ b.startAfter(lbT);}
     	functionBlock[f, (ASToken)lbT]		
 	;
 	exception catch [RecognitionException ex] { handleParsingError(ex);  }

/**
 * Matches a function block, excluding the open "{" but including the closing "}".
 */
functionBlock[FunctionNode f, ASToken openT]
{ 
    final BlockNode b = f.getScopedNode();
    b.setContainerType(IContainerNode.ContainerType.BRACES); 
    skipFunctionBody(f, openT);
}
	:   (directive[b, TOKEN_BLOCK_CLOSE])*
        rbT:TOKEN_BLOCK_CLOSE
	  	{ b.endBefore(rbT); }
	;
  	exception catch [RecognitionException ex] 
	{ 
		IASToken prev = buffer.previous();
        if (prev.getType() != ASTokenTypes.EOF)
            b.endAfter(prev);
        else
            b.setEnd(b.getStart());
		if(handleParsingError(ex))  {
			functionBlock(f, openT); //attempt to retry
		} 
	}

/**
 * Matches an optional function body. It can either be a "block" or a 
 * "semicolon".
 */
optionalFunctionBody [FunctionNode f]
{
    BlockNode blockNode = f.getScopedNode();
    enableSemicolonInsertion();
}
    :   { LA(1) == TOKEN_BLOCK_OPEN }? lbT:TOKEN_BLOCK_OPEN 
        { blockNode.startAfter(lbT); } 
        functionBlock[f, (ASToken)lbT]   
    |   { buffer.matchOptionalSemicolon() }? // Matches a function without body.
    |   {
            final Token lt = LT(1);
            blockNode.startBefore(lt);
            blockNode.endBefore(lt);

            // Report missing left-curly problem if there's no other syntax
            // problems in the function definition.
            reportFunctionBodyMissingLeftBraceProblem();
        } 
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a function definition. For example:
 *
 *     private function myFunction(name:String) : void
 *     {
 *         return;
 *     }
 *
 */	
functionDefinition[ContainerNode c, INamespaceDecorationNode namespace, List<ModifierNode> modList]
{  
	IdentifierNode name=null; 
    disableSemicolonInsertion();
}
	:   (   functionT:TOKEN_KEYWORD_FUNCTION 
	
			// optional accessors:
			// Although "get" and "set" can be identifiers as well, here
			// they can only be the reserved words, unless it's a function
			// called "get()" or "set()". As a result, the parser
			// needs to match it in a "greedy" fashion.
	        (options{greedy=true;}:
	        	{ LA(2) != TOKEN_PAREN_OPEN}? getT:TOKEN_RESERVED_WORD_GET
        	|   { LA(2) != TOKEN_PAREN_OPEN}? setT:TOKEN_RESERVED_WORD_SET
        	)? 
        	
        	// non-optional function name:
	        name=identifier
			//we need to be able to keep going in case we are in the processing of typing a function name
			exception catch [RecognitionException ex] { name = handleMissingIdentifier(ex); }
		)
		{
			final FunctionNode n ;
			if (getT != null)
				n = new GetterNode((ASToken)functionT, (ASToken)getT, name);
			else if (setT != null)
				n = new SetterNode((ASToken)functionT, (ASToken)setT, name);
			else
				n = new FunctionNode((ASToken)functionT, name);
 
			storeDecorations(n, c, namespace, modList);
			c.addItem(n);
		}
		
		// function signature:
		lpT:TOKEN_PAREN_OPEN
		{
			final ContainerNode parameters = n.getParametersContainerNode();
			parameters.startBefore(lpT);
 		}
     	formalParameters[parameters]	
     	(	rpT:TOKEN_PAREN_CLOSE 
     		{ parameters.endAfter(rpT); }
     		// error recovery for typing in-progress function definitions
     		exception catch [RecognitionException ex] { handleParsingError(ex); }
     	)
     	(resultType[n])?
     	
     	optionalFunctionBody[n]
		{
			Token prevToken = buffer.previous();
			if(prevToken.getType() == TOKEN_BLOCK_CLOSE)
			{
				n.endAfter(prevToken);
			}
		}
 	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches the parameters of a function definition signature (excluding the parenthesis).
 * 
 *     arg1:int, arg2:String
 */
formalParameters[ContainerNode c]
	: (formal[c] (TOKEN_COMMA formal[c])*)?
	;
	exception catch [RecognitionException ex] {handleParsingError(ex); }

/**
 * Matches a single parameter in a function definition.
 */
formal[ContainerNode c]
	{ ParameterNode p = null; }
	
	:(p=restParameter | p=parameter)
		{ if (p != null) c.addItem(p); }
	;

/**
 * Matches the "rest parameters" in a function definition.
 *
 *     ...args
 */
restParameter returns [ParameterNode p]
	{ p = null; }
	
	: e:TOKEN_ELLIPSIS p=parameter
		{
			if (p != null){
				// ??? following is an override on default type-specification
				// ??? and should be pulled soon as that gets resolved.
				if (p.getTypeNode() == null){
					p.span(e);
				}
				p.setIsRestParameter(true);
			}
		}
	;
	
/**
 * Matches a parameter in a function definition.
 */
parameter returns [ParameterNode p]
{ 
	p = null; 
	ASToken t = null;
	IdentifierNode name = null; 
}
	:   (   t=varOrConst 
    		{
    			// const allowed here, var is not...log error, keep going
    			if (t.getType() == TOKEN_KEYWORD_VAR) 
    				handleParsingError(new RecognitionException()); 
    		}
        )? 
	
        name=identifier
		{ 
			p = new ParameterNode(name); 
			if (t != null && t.getType() == TOKEN_KEYWORD_CONST)
				p.setKeyword(t);
		}
		
        (resultType[p])?
        (initializer[p])?	 
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches keyword "var" or keyword "const".
 */
varOrConst returns[ASToken token]
{ 
	token = LT(1); 
}
	:   TOKEN_KEYWORD_VAR 
	|   TOKEN_KEYWORD_CONST
	;
	
/**
 * Matches a result type: either a "void" keyword or a restricted name.
 *
 *     :void
 *     :String
 *     :int
 *
 */
resultType [BaseTypedDefinitionNode result]
{ 
    ExpressionNodeBase t = null; 
}
    :   colon:TOKEN_COLON 
        (   ( t=voidLiteral | t=type )
        	exception catch [RecognitionException ex] 
            { t = handleMissingIdentifier(ex); }
        )    
        { 
            if(t.getStart() == -1) 
                t.startAfter(colon);
            	
            if (t.getEnd() == -1) 
                t.endAfter(colon);
            	
            result.endAfter(colon);
            result.setType((ASToken) colon, t); 
        }
    ;
 	
/**
 * Matches an initializer in a variable/constant definition.
 */
initializer [IInitializableDefinitionNode v]
{
	ExpressionNodeBase e = null; 
}
  	:   assignT:TOKEN_OPERATOR_ASSIGNMENT 
        e=assignmentRightValue
 		{ v.setAssignedValue((IASToken) assignT, e); }
 	;	
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }
 	
/**
 * Matches a variable/constant definition.
 */
variableDefinition[ContainerNode c, INamespaceDecorationNode namespace, List<ModifierNode> modList]
{
	VariableNode v = null;
	ChainedVariableNode v2 = null;
	ASToken tok = null;
	asDocDelegate.beforeVariable();
}
 	
	:   tok=varOrConst v=singleVariable[(ASToken)tok, namespace]
		{
			asDocDelegate.afterVariable();	
			storeVariableDecorations(v, c, namespace, modList);
			if(v instanceof ConfigConstNode) {
				addConfigConstNode((ConfigConstNode)v);
			} else {
				c.addItem(v);
			}
		}
    	// don't allow chain after a config
    	(   {!(v instanceof ConfigConstNode)}? 
    	    TOKEN_COMMA v2=chainedVariable[c]
    		{
    		 	if(v2 != null)
                {
                    v.addChainedVariableNode(v2);
                    storeEmbedDecoration(v2, v.getMetaTags());
                }
    		}
    		exception catch [RecognitionException ex] { handleParsingError(ex); }
    	)*
    	{ matchOptionalSemicolon(); setAllowErrorsInContext(true); }
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); setAllowErrorsInContext(true); }

/**
 * Matches a single variable/constant definition.
 */
singleVariable[IASToken keyword, INamespaceDecorationNode namespace] returns [VariableNode v]
{ 
	v = null; 
	IdentifierNode name = null; 
}
	:   name=identifier
		{ 
			if(namespaceIsConfigNamespace(namespace)) {
				v = new ConfigConstNode(name);
			} else {
				v = new VariableNode(name);
			}
			v.setKeyword(keyword);
			v.setIsConst(keyword.getType() == TOKEN_KEYWORD_CONST);
			if(name.getStart() == -1) {
				name.startAfter(keyword);
				name.endAfter(keyword);
			}
		}
		
		(resultType[v])? 
		(initializer[v])?
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }
	
/**
 * Matches a chained variable/constant definition.
 */     
chainedVariable[ContainerNode c] returns [ChainedVariableNode v]
{
	 v = null; 
	 IdentifierNode name = null; 
}
	:   name=identifier
		{ v = new ChainedVariableNode(name); }
		
		(resultType[v])?
		(initializer[v])?
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }
	
/**
 * Matches variable definitions in a for loop.
 */
variableDefExpression returns[NodeBase v]
{ 
	v = null; 
	ContainerNode c = null; 
	NodeBase v1 = null; 
	ASToken varT = null;
} 
	:   varT=varOrConst v=singleVariableDefExpression[varT, varT.getType() == TOKEN_KEYWORD_CONST]
		(   TOKEN_COMMA v1=singleVariableDefExpression[null, varT.getType() == TOKEN_KEYWORD_CONST]
			{
				if (c == null) {
					c = new ContainerNode();
					c.setStart(v.getStart());
					c.addItem(v);
					v = c;
				}
				c.addItem(v1);
				c.setEnd(v1.getEnd());
			}
		)*
	;
	exception catch [RecognitionException ex] { handleParsingError(ex);  }
		
/**
 * Matches a single variable definition in a for loop.
 */
singleVariableDefExpression[ASToken varToken, boolean isConst] returns [ExpressionNodeBase n]
{
	n = null;
	VariableNode variable = null;
	IdentifierNode varName = null; 
	ExpressionNodeBase value = null; 
}
	:   varName=identifier
		{ 
			variable = new VariableNode(varName);
			if(varToken != null)
				variable.setKeyword(varToken);
			variable.setIsConst(isConst);
			n = new VariableExpressionNode(variable);
		}
		
    	(resultType[variable])?
    		
    	(initializer[variable])?
	;

/**
 * Matches a default XML namespace statement. For example:
 * 
 *    default xml namespace = "domain";
 *
 */
defaultXMLNamespaceStatement[ContainerNode c]
	{ ExpressionNodeBase e = null; }
	
	: defT:TOKEN_DIRECTIVE_DEFAULT_XML TOKEN_OPERATOR_ASSIGNMENT e=assignmentExpression
		{ 
			DefaultXMLNamespaceNode n = new DefaultXMLNamespaceNode(new KeywordNode((IASToken)defT));
			c.addItem(n);
			n.setExpressionNode(e); 
			matchOptionalSemicolon();
		}
	;

/**
 * Matches an expression in a pair of parenthesis. It's usually used as a
 * condition expression in {@code if (...)}, {@code while (...)}, etc.
 *
 *     (....)
 */
statementParenExpression returns [ExpressionNodeBase e]
{
	e = null; 
}
	:   TOKEN_PAREN_OPEN     { disableSemicolonInsertion(); }
		e=expression 
	    TOKEN_PAREN_CLOSE    { enableSemicolonInsertion(); }
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); enableSemicolonInsertion(); }
	
		
/**
 * Matches an empty statement which is an explicit semicolon.
 */
emptyStatement
 	:   TOKEN_SEMICOLON
 	;

/**
 * Matches a "return" statement.
 */
returnStatement[ContainerNode c]
{ 
	ExpressionNodeBase n = null; 
	ExpressionNodeBase e = null;
}
	:   returnT:TOKEN_KEYWORD_RETURN 
		{
			n = new ReturnNode((ASToken)returnT);
			c.addItem(n);
			afterRestrictedToken((ASToken)returnT);
		}
		
		e=optExpression
		{ 
			((ReturnNode)n).setStatementExpression(e);
		}
	   { matchOptionalSemicolon(); }
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a "throw" statement.
 */
throwsStatement[ContainerNode c]
{ 
	ExpressionNodeBase n = null; 
	ExpressionNodeBase e = null;
}
	:   throwT:TOKEN_KEYWORD_THROW
		{
			n = new ThrowNode((ASToken)throwT);
			c.addItem(n);
			afterRestrictedToken((ASToken)throwT);
		}
	
		(   e=expression
			{ 
				((ThrowNode)n).setStatementExpression(e); 
			}
			exception catch [RecognitionException ex] { handleParsingError(ex); }
		)
	   { matchOptionalSemicolon(); }
	;
	
/**
 * Matches a "for loop" statement.
 */
forStatement[ContainerNode c]
{ 
    ForLoopNode node = null; 
    ContainerNode forContainer = null; 
    BlockNode b = null; 
    NodeBase fi = null; 
    ExpressionNodeBase e = null; 
    BinaryOperatorNodeBase inNode = null;
}
    :   forKeyword:TOKEN_KEYWORD_FOR lparenT:TOKEN_PAREN_OPEN
        { 
            node = new ForLoopNode((ASToken)forKeyword); 
            c.addItem(node);
            forContainer = node.getConditionalsContainerNode();
            b = node.getContentsNode();
            forContainer.startAfter(lparenT); 
        }
        
        { 
            expressionMode = ExpressionMode.noIn;
        }
        fi=forInitializer
        {
            expressionMode = ExpressionMode.normal;
        }
        
        (   TOKEN_SEMICOLON           { forContainer.addItem(fi); }
            forCondition[forContainer] 
            TOKEN_SEMICOLON 
            forStep[forContainer]
        |   in:TOKEN_KEYWORD_IN 
            {
                final ExpressionNodeBase leftOfIn;
                if (fi instanceof ExpressionNodeBase)
                {
                    leftOfIn = (ExpressionNodeBase) fi;
                }
                else
                {
                    // for...in doesn't allow multiple variable definition in the initializer clause
                    addProblem(new InvalidForInInitializerProblem(node));
                    if (fi instanceof ContainerNode &&
                        fi.getChildCount() > 0 &&
                        ((ContainerNode)fi).getChild(0) instanceof ExpressionNodeBase)
                    {
                        // Recover by taking the first variable initializer and
                        // drop the rest.
                        leftOfIn = (ExpressionNodeBase)((ContainerNode)fi).getChild(0);
                    }
                    else
                    {
                        // No valid variable initializer found: recover by adding
                        // an empty identifier node.
                        leftOfIn = IdentifierNode.createEmptyIdentifierNodeAfterToken((ASToken)lparenT);
                    }
                }
                inNode = BinaryOperatorNodeBase.create((ASToken)in, leftOfIn, null);
                forContainer.addItem(inNode);
            }
            e=optExpression
            { inNode.setRightOperandNode(e); }
        )?  // Make optional for error handling.
        
        {
            if (forContainer.getChildCount() == 0 && fi != null)
                forContainer.addItem(fi);
        }
        
        (   rparenT:TOKEN_PAREN_CLOSE
            { 
                forContainer.endBefore(rparenT); 
            }
            exception catch [RecognitionException ex] { handleParsingError(ex); }
        )
        substatement[b] 
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches the "initializer" part in a for loop.
 */    
forInitializer returns [NodeBase n]
{ 
	n = null; 
}
    :   n=variableDefExpression              								
    |   n=optExpression
    ;
    exception catch [RecognitionException ex] {handleParsingError(ex);  }

/**
 * Matches the "condition" part in a for loop.
 */
forCondition[ContainerNode c]
{ 
	ExpressionNodeBase e = null; 
}
	:   e=optExpression
		{if (e != null) c.addItem(e);}
	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }

/**
 * Matches the "step" part in a for loop.
 */
forStep[ContainerNode c]
{ 
	ExpressionNodeBase e = null; 
}
	:    e=optExpression	
		{if (e != null) c.addItem(e);}
	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }
 
/**
 * Matches a "do...while" statement.
 */
doStatement[ContainerNode c] 
{ 
	DoWhileLoopNode n = null; 
    ExpressionNodeBase e = null; 
    BlockNode b = null; 
}
 	:   doT:TOKEN_KEYWORD_DO
 		{
 			n = new DoWhileLoopNode((ASToken)doT);
 			c.addItem(n);
 			b = n.getContentsNode();
 		}
 		
        innerSubstatement[b]
        
        TOKEN_KEYWORD_WHILE e=statementParenExpression
 	 	{ 
 	 		n.setConditionalExpression(e); 	
	        matchOptionalSemicolon(); 
        }
 	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }
 	
/**
 * Matches a "while" loop statement.
 * 
 *     while (x > 1) { x--; }
 */
whileStatement[ContainerNode c] 
{ 
	WhileLoopNode n = null; 
	ExpressionNodeBase e = null; 
	BlockNode b = null; 
}
 	:   whileT:TOKEN_KEYWORD_WHILE e=statementParenExpression	
 		{
 			n = new WhileLoopNode((ASToken)whileT);
 			n.setConditionalExpression(e);
 			c.addItem(n); 
 			b = n.getContentsNode();
 		}
 	
 	    substatement[b]
 	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }
 	
 	
/**
 * Matches a "break statement" or a "continue statement". For example:
 *
 *    break;
 *    break innerLoop;
 *    continue;
 *    continue outerLoop;
 *
 */
breakOrContinueStatement[ContainerNode c]
{ 
    IdentifierNode id = null; 
    IterationFlowNode n = null; 
    final ASToken t = LT(1);
}
    :   ( TOKEN_KEYWORD_CONTINUE | TOKEN_KEYWORD_BREAK ) 
    	{ 
    		n = new IterationFlowNode(t); 
    		c.addItem(n); 
    		afterRestrictedToken(t);
    	}
    	
    	// "greedy" mode is required to associate the following ID with the flow control.
    	(options{greedy=true;}: 
    	   id=identifier 
    	   { n.setLabel(id); }	
	    )?
    	{ matchOptionalSemicolon(); }
    ;
    exception catch [RecognitionException ex] {handleParsingError(ex); }
 	
/**
 * Matches a "goto" statement.
 */
gotoStatement[ContainerNode c]
{ 
	IdentifierNode id = null; 
	IterationFlowNode n = null; 
	final ASToken t = LT(1);
}
    :   TOKEN_RESERVED_WORD_GOTO id=identifier
        {
    		n = new IterationFlowNode(t); 
    		c.addItem(n);
    		n.setLabel(id);
    		matchOptionalSemicolon();
        }
    ;
 	exception catch [RecognitionException ex] { handleParsingError(ex); }
 	
/**
 * Matches a "with" statement.
 */
withStatement[ContainerNode c]
{
	WithNode n = null; 
	ExpressionNodeBase e = null; 
	BlockNode b = null; 
}
	:   withT:TOKEN_KEYWORD_WITH e=statementParenExpression
		{ 
			n = new WithNode((ASToken)withT); 
			n.setConditionalExpression(e);
			c.addItem(n);
			b = n.getContentsNode(); 
		}
    	substatement[b]
	;

/**
 * Matches a "try...catch...finally" statement.
 */
tryStatement[ContainerNode c] 
{ 
	TryNode n = null; 
	BlockNode b = null; 
}
 	:   tryT:TOKEN_KEYWORD_TRY
 		{
 			n = new TryNode((ASToken)tryT);
 			b = n.getContentsNode();
 			c.addItem(n);
 		}
 		
        block[b]
        
        (   options { greedy=true;}: 
            catchBlock[n]
        )*

        (   options { greedy=true;}: 
            finallyT:TOKEN_KEYWORD_FINALLY
            {
            	TerminalNode t = new TerminalNode((ASToken)finallyT);
            	n.addFinallyBlock(t);
            	b = t.getContentsNode();
            }
            block[b]
        )?
 	;  

/**
 * Matches the "catch" block in a "try" statement.
 */
catchBlock[TryNode tryNode]
{ 
	CatchNode n = null; 
	ParameterNode arg = null; 
	BlockNode b = null; 
}
	:   catchT:TOKEN_KEYWORD_CATCH TOKEN_PAREN_OPEN 
		{ disableSemicolonInsertion(); } 
	  
	    arg=catchBlockArgument
		{ 
			n = new CatchNode(arg); 
			tryNode.addCatchClause(n);
			b = n.getContentsNode();
			n.startBefore(catchT); 
		}
		
		(   rpT:TOKEN_PAREN_CLOSE  
			{ 
				enableSemicolonInsertion();
				n.endAfter(rpT);
			}
			
 			exception catch [RecognitionException ex]
 			{handleParsingError(ex);  enableSemicolonInsertion(); }
 		)
 		
	    block[b]
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches the argument in the "try...catch(arg)" statement.
 */
catchBlockArgument returns [ParameterNode p]
{ 
	p = null; 
	IdentifierNode name = null; 
	ExpressionNodeBase t = null; 
}
	:   name=identifier
		{ p = new ParameterNode(name); }
		
		(   colonT:TOKEN_COLON
			{ p.setType((ASToken)colonT, null); }
			t=type
			{ p.setType((ASToken)colonT, t); }
		)?
	;	
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches an "if" statement.
 */
ifStatement[ContainerNode c]
{ 
	IfNode i = null; 
	ExpressionNodeBase cond = null; 
	ContainerNode b = null; 
	boolean hasElse = false; 
}
	:   ifT:TOKEN_KEYWORD_IF cond=statementParenExpression  
		{
			i = new IfNode((ASToken)ifT);
			ConditionalNode cNode = new ConditionalNode((ASToken)ifT);			
			cNode.setConditionalExpression(cond);
			b = cNode.getContentsNode();
			i.addBranch(cNode);
			c.addItem(i);
		}
		innerSubstatement[b]
	
		(options{greedy=true;}: 
            hasElse=elsePart[i]
			{ if (hasElse == true) return; }
		)*
	;
 	exception catch [RecognitionException ex] { handleParsingError(ex); }
 	
/**
 * Matches the optional "else" block of an "if" statement.
 *
 * @return true if there is an "else" block.
 */
elsePart[IfNode i] returns [boolean hasElse]
{ 
	hasElse = false; 
	ContainerNode b = null; 
	ExpressionNodeBase cond = null; 
	ConditionalNode elseIf = null; 
}
	
	:   elseT:TOKEN_KEYWORD_ELSE
		(options{greedy=true;}: 
            TOKEN_KEYWORD_IF cond=statementParenExpression
			{
		    	elseIf = new ConditionalNode((ASToken) elseT);
		    	elseIf.setConditionalExpression(cond);
		    	i.addBranch(elseIf);
				b = elseIf.getContentsNode();
			}
		)?
		
		{
			if (elseIf == null){
				hasElse = true;
				TerminalNode t = new TerminalNode((ASToken) elseT);
				i.addBranch(t);
				b = t.getContentsNode();
			}
		}
		substatement[b]
	;
	exception catch [RecognitionException ex] {handleParsingError(ex);  }
	
/**
 * Matches a "switch" statement.
 */
switchStatement[ContainerNode c]
{ 
	SwitchNode sw = null; 
	ExpressionNodeBase e = null; 
}
 	:   switchT:TOKEN_KEYWORD_SWITCH e=statementParenExpression
 		{
			sw = new SwitchNode((ASToken)switchT);			
			c.addItem(sw);
			if(e != null)
				sw.setConditionalExpression(e);
 		}
 
 	    cases[sw]
 	;

/**
 * Matches the "case" block in a "switch" statement.
 */
cases[SwitchNode sw]
{ 
	final ContainerNode b = sw.getContentsNode(); 
}
    :   openT:TOKEN_BLOCK_OPEN           { b.startBefore(openT); }
    	caseClauses[b]
    	closeT:TOKEN_BLOCK_CLOSE         { b.endAfter(closeT); }
	;

/**
 * Matches the "case" clauses in a "switch" statement.
 */
caseClauses[ContainerNode swb]
 	:    (caseClause[swb])*	
 	;

/**
 * Matches a single "case" clause in a "switch" statement.
 */ 	
caseClause[ContainerNode swb]
{ 
	ExpressionNodeBase e = null; 
	ContainerNode b = null; 
}
 	:   caseT:TOKEN_KEYWORD_CASE e=expression colon
 		{
 			ConditionalNode cond = new ConditionalNode((ASToken) caseT);
 			cond.setConditionalExpression(e);
 		 	swb.addItem(cond);
 		 	b = cond.getContentsNode(); 
 		}
 		caseStatementList[b]
 		
 	|   defaultT:TOKEN_KEYWORD_DEFAULT colon 	
 		{
 			TerminalNode t = new TerminalNode((ASToken)defaultT);
 			swb.addItem(t);
 			b = t.getContentsNode();
 		}
 		caseStatementList[b]
        |   asDocComment
 	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }

/**
 * Matches a colon token ":" or recover from a missing colon.
 */
colon
	:	TOKEN_COLON
	;
 	exception catch [RecognitionException ex] { addProblem(unexpectedTokenProblem(LT(1), ASTokenKind.COLON)); }

/**
 * Matches the statements in a "case" clause.
 */ 	 
caseStatementList[ContainerNode b]
 	:   (directive[b, TOKEN_BLOCK_CLOSE])*
 	;
 	exception catch [RecognitionException ex] {handleParsingError(ex);  }
 
/**
 * Matches an identifier token. An identifier can come from different token
 * types such as: 
 *
 *   - IDENTIFIER
 *   - namespace
 *   - get 
 *   - set
 *
 * This is because in AS3, these elements are not reserved keyword. However they
 * have special meaning in some syntactic contexts.
 * See "AS3 syntax spec - 3.5 Keywords and Punctuators" for details.
 */
identifier returns [IdentifierNode n]
{ 
    n = null;  
    final ASToken token = LT(1);
}
    :   (	TOKEN_IDENTIFIER 
    	|	TOKEN_RESERVED_WORD_NAMESPACE 
    	|	TOKEN_RESERVED_WORD_GET 
    	| 	TOKEN_RESERVED_WORD_SET 
    	)
		{ n = new IdentifierNode(token); }
    ;
    exception
    catch [NoViableAltException e1] { n = expectingIdentifier(e1); }
    catch [RecognitionException e2] { n = handleMissingIdentifier(e2); }
    
/**
 * Matches an "import-able" name.
 *
 *     flash.display.Sprite;
 *     flash.events.*;
 */    
importName returns [ExpressionNodeBase n]
{ 
	n=null; 
	ExpressionNodeBase e = null;
}
	
	: 	n=packageName 
		(	dot:TOKEN_OPERATOR_MEMBER_ACCESS 
			{ 
				n = new FullNameNode(n, (ASToken) dot, null); 
			} 
			e=starLiteral
			{
				((FullNameNode)n).setRightOperandNode(e);
			}
		)?
	;
	exception catch [RecognitionException ex] { return handleMissingIdentifier(ex, n); }

/**
 * Matches a restricted name. For example:
 *
 *     my.package.name.Clock;
 *     private::myPrivateVar;
 *     UnqualifiedTypeClock;
 *
 */
restrictedName returns [ExpressionNodeBase nameExpression]
{
    nameExpression = null;
 	IdentifierNode placeHolderRightNode = null;
    ASToken opToken = null;
    ExpressionNodeBase part = null;
}
    :   nameExpression=restrictedNamePart
    	
    	// LL(1) grammar can only branch on the next token. 
        // The LA(2) semantic predicate is used to disambiguate:
        // 1. "foo.bar" - a restricted name consisting two identifiers
        // 2. "foo.(bar)" - a member expression whose left-hand side is an identifier
        //                  and the right-hand side is a parenthesis expression
        (options { greedy=true; }: { LA(2) != TOKEN_PAREN_OPEN }?
            {
                opToken = LT(1); 
                
                // The place-holder node is a safe-net in case parsing the 
                // "right" node fails, so that we will still have a balanced
                // FullNameNode.
                placeHolderRightNode = IdentifierNode.createEmptyIdentifierNodeAfterToken(opToken);
                
                final ExpressionNodeBase nameLeft = nameExpression;
            }
            (   TOKEN_OPERATOR_MEMBER_ACCESS 
                { nameExpression = new FullNameNode(nameLeft, opToken, placeHolderRightNode); } 
            |   TOKEN_OPERATOR_NS_QUALIFIER 
                { nameExpression = new NamespaceAccessExpressionNode(nameLeft, opToken, placeHolderRightNode); }
            )
            
            (	{ opToken.getType() == TOKEN_OPERATOR_NS_QUALIFIER && LA(1) == TOKEN_SQUARE_OPEN }?
                // matches ns::["var_in_ns"]
            	nameExpression=bracketExpression[nameLeft]
	        |   part=restrictedNamePart
	            { 
	            	((BinaryOperatorNodeBase)nameExpression).setRightOperandNode(part);
	            	checkForChainedNamespaceQualifierProblem(opToken, part);
	        	}
        	)
        )*
    ;
	exception catch [RecognitionException ex] 
	{  
	   if (nameExpression == null)
	       nameExpression = handleMissingIdentifier(ex); 
       else
           consumeParsingError(ex);
    }
    
    
/**
 * Matches the identifier part of a restricted name. For example:
 * 
 *     private
 *     public
 *     foo
 *     MyType
 *
 */
restrictedNamePart returns [IdentifierNode id]
{
    id = null;
    final ASToken lt = LT(1);
}
    :   id=identifier
    |   TOKEN_NAMESPACE_NAME 
        { id = new IdentifierNode(lt); }
    |   TOKEN_KEYWORD_SUPER
        { id = LanguageIdentifierNode.buildSuper(lt); }
    ;
    // "identifier", "namespace name" and "super" are all "identifiers" to
    // the user. So we override the default error handling in order to emit
    // "expecting identifier but found ..." syntax problem.
    exception catch [NoViableAltException ex] { id = expectingIdentifier(ex); }

/**
 * Keep legacy rule for transpiling.
 */
typedNameOrStar returns [ExpressionNodeBase n]
    :    n=type
    ;
    
/**
 * Matches a type reference.
 *
 *     String
 *     int
 *     *
 *     Vector.<Clock>
 *     foo.bar.Vector.<T>
 *
 */
type returns [ExpressionNodeBase n]
{ 
    n = null; 
}
    :   n=starLiteral
    |   n=configConditionAsType
    |   n=restrictedName ( n=typeApplication[n] )?
    ;	
    exception catch [RecognitionException ex] { n = handleMissingIdentifier(ex); }
  
configConditionAsType returns [ExpressionNodeBase n]
{
    n = null; 
}
    :   ns:TOKEN_NAMESPACE_NAME op:TOKEN_OPERATOR_NS_QUALIFIER id:TOKEN_IDENTIFIER
        { final NamespaceIdentifierNode nsNode = new NamespaceIdentifierNode((ASToken)ns); 
          nsNode.setIsConfigNamespace(isConfigNamespace(nsNode));
	  final IdentifierNode idNode = new IdentifierNode((ASToken)id);
	  n = transformToNSAccessExpression(nsNode, (ASToken) op, idNode);
          n = n.copyForInitializer(null);
	  n.setSourcePath(nsNode.getSourcePath());
	  n.setLine(nsNode.getLine());
	  n.setColumn(nsNode.getColumn());
	  n.setEndLine(idNode.getEndLine());
	  n.setEndColumn(idNode.getEndColumn());
	  n.setStart(nsNode.getStart());
	  n.setEnd(idNode.getEnd());
        }
    ;

/**
 * Matches a "type application" part>
 *
 *     .<String>
 *     .<Clock>
 *     .<uint>
 *
 */
typeApplication [ExpressionNodeBase root] returns[TypedExpressionNode n]
{ 
    n = null; 
    ExpressionNodeBase t = null; 
    Token closeT = null;
    enterTypeApplication(root);
}
    :   openT:TOKEN_TYPED_COLLECTION_OPEN
        t=type
        {
            n = new TypedExpressionNode(root, t, (ASToken)openT);
            closeT = LT(1);
        }
        ( TOKEN_TYPED_COLLECTION_CLOSE | TOKEN_OPERATOR_GREATER_THAN )
        { n.endAfter(closeT); }
    ; 
    exception catch [RecognitionException ex] { consumeParsingError(ex); } 


/**
 * Matches an optional expression.
 * @return NilNode or ExpressionNodeBase.
 */	 		
optExpression returns[ExpressionNodeBase e]
{ 
	e = null; 
}
	:   (options{greedy=true;}: e=expression)?												
		{ if (e == null) e = new NilNode(); }
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }
		
/**
 * Matches an expression or a comma-separated expression list.
 */
expression returns [ExpressionNodeBase n]
{
    n = null; 
    ExpressionNodeBase e1 = null; 
}
    :   n=assignmentExpression 
    	(options{greedy=true;}: 
            op:TOKEN_COMMA 
            e1=assignmentExpression
            { n = BinaryOperatorNodeBase.create((ASToken)op,n,e1); }
    	)*
    ;	
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches an "assignment expression".
 *
 * According to ASL sytax spec, the productions for an "assignment expression"
 * is either a "conditional expression" or a "left-hand side expression" followed
 * by an "assignment operator" and an "assignment expression". However, since
 * "assignmentExpression" and "conditionaExpression" is ambiguous at indefinite 
 * look-ahead distance, this LL(1) grammar can't decide which alternative to
 * choose. As a result, the implementation is more lenient in that an AST node
 * for an assignment binary node will be built even the left-hand side expression
 * is not a valid "LeftHandSideExpression", such as a constant.
 * 
 * For example:
 * <code>100 = "hello";</code>
 * This statement will be parsed without syntax error, generating tree like:
 * <pre>
 *        =
 *       / \
 *    100  "hello"
 * </pre>
 *
 * A possible solution to this is to find out the difference between "conditional
 * expression" and "left-hand side expression", then insert a semantic predicate
 * before matching a "assignment operator".
 */
assignmentExpression returns [ExpressionNodeBase n]
{
	n = null; 
	ASToken op = null; 
	ExpressionNodeBase r = null;
}		
	:   n=condExpr 
		(options{greedy=true;}: 
		    op=assignOp 
		    r=assignmentRightValue
	        { n = BinaryOperatorNodeBase.create(op,n,r); }
		)?
	;
	
/**
 * Matches the right-hand side of an assignment expression.
 * "public" namespace is allowed as an R-value for backward compatibility.
 * @see "CMP-335 and ASLSPEC-19"
 */
assignmentRightValue returns [ExpressionNodeBase rightExpr]
{
	rightExpr = null;
}
    :   { isNextTokenPublicNamespace() }? p:TOKEN_NAMESPACE_ANNOTATION
        { rightExpr = new NamespaceIdentifierNode((ASToken)p); }
    |   rightExpr=assignmentExpression  
    ;
	
assignOp returns [ASToken op]
{ 
	op = LT(1); 
}
    :   TOKEN_OPERATOR_ASSIGNMENT 
    |   TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT
    |   TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT
    |   TOKEN_OPERATOR_PLUS_ASSIGNMENT
    |   TOKEN_OPERATOR_MINUS_ASSIGNMENT
    |   TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT
    |   TOKEN_OPERATOR_DIVISION_ASSIGNMENT
    |   TOKEN_OPERATOR_MODULO_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT
    |   TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT
    ;

/**
 * Matches a ternary expression such as: 
 *
 *     (x > 2) ? "greater" : "smaller"
 */		
condExpr returns [ExpressionNodeBase n]
{
    n = null; 
    ExpressionNodeBase trueExpr = null; 
    ExpressionNodeBase falseExpr = null;
    TernaryOperatorNode ternary = null;
}
	:   n=binaryExpr 
        (   op:TOKEN_OPERATOR_TERNARY       
            {
                ternary = new TernaryOperatorNode((ASToken)op,n,null,null);
                n = ternary; 
            }
            trueExpr=assignmentExpression   { ternary.setLeftOperandNode(trueExpr); }
            TOKEN_COLON 
            falseExpr=assignmentExpression  { ternary.setRightOperandNode(falseExpr); }
        )?
        exception catch [RecognitionException ex] { handleParsingError(ex); }
	;
	
/**
 * Binary expression uses operator precedence parser in BaseASParser.
 */
binaryExpr returns [ExpressionNodeBase n]
{
	n = precedenceParseExpression(4);
	if (true)	return n;
}
	: fakeExpr
	;
	
/**
 * fakeExpr simulates the set of allowable follow tokens in an expression context, which allows antlr to function.
 * It is unreachable.
 */
fakeExpr
{ 
	ExpressionNodeBase n = null; 
}
	:	n=unaryExpr (options{greedy=true;}: binaryOperators fakeExpr)?
	;
	
/**
 * Declares all the binary operators.
 */
binaryOperators
    :   TOKEN_OPERATOR_LOGICAL_OR 
    |   TOKEN_OPERATOR_LOGICAL_AND
    |   TOKEN_OPERATOR_BITWISE_OR 
    |   TOKEN_OPERATOR_BITWISE_XOR 
    |   TOKEN_OPERATOR_BITWISE_AND
    |   TOKEN_OPERATOR_EQUAL 
    |   TOKEN_OPERATOR_NOT_EQUAL 
    |   TOKEN_OPERATOR_STRICT_EQUAL 
    |   TOKEN_OPERATOR_STRICT_NOT_EQUAL
    |   TOKEN_OPERATOR_GREATER_THAN 
    |   TOKEN_OPERATOR_GREATER_THAN_EQUALS
    |   TOKEN_OPERATOR_LESS_THAN 
    |   TOKEN_OPERATOR_LESS_THAN_EQUALS
    |   TOKEN_KEYWORD_INSTANCEOF
    |   TOKEN_KEYWORD_IS 
    |   TOKEN_KEYWORD_AS 
    |   TOKEN_KEYWORD_IN
    |   TOKEN_OPERATOR_BITWISE_LEFT_SHIFT 
    |   TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT 
    |   TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT
    |   TOKEN_OPERATOR_MINUS 
    |   TOKEN_OPERATOR_PLUS
    |   TOKEN_OPERATOR_DIVISION 
    |   TOKEN_OPERATOR_MODULO 
    |   TOKEN_OPERATOR_STAR
	;	

/**
 * Matches a "prefix expression".
 *
 *     delete x[i]
 *     ++i
 *     --i
 *
 * The distinction between this rule and "unary expression" makes the parser
 * more strict about what expressions can follow what tokens.
 */
prefixExpression returns [ExpressionNodeBase n]
{
	n = null;
	final ASToken op = LT(1);
}
	:	n=postfixExpr
	|	(	TOKEN_KEYWORD_DELETE n=postfixExpr 
	    |   TOKEN_OPERATOR_INCREMENT n=lhsExpr
	    |   TOKEN_OPERATOR_DECREMENT n=lhsExpr
	    )
        { 
	    	if (n == null)
	            n = IdentifierNode.createEmptyIdentifierNodeAfterToken(op);
	    	n = UnaryOperatorNodeBase.createPrefix(op, n); 
    	} 
	;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a "unary expression".
 *
 * This rule is called out of the precedence parser in BaseASParser.
 * If you need to change the name of this rule, you'll also need to update 
 * the base class.
 */
unaryExpr returns [ExpressionNodeBase n]
{ 
    n = null; 
    ASToken op = null; 
}	
    :   (   n=prefixExpression 
        |   op=unaryOp n=unaryExpr 
            { 
            	if (n == null)
                    n = IdentifierNode.createEmptyIdentifierNodeAfterToken(op);
            	n = UnaryOperatorNodeBase.createPrefix(op, n); 
        	} 
        ) 
        (options { greedy = true; }: 
            n=propertyAccessExpression[n] 
        |   n=arguments[n] 
        )*
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a unary operator.
 */
unaryOp returns [ASToken op]
{
	op = LT(1); 
}
    :   TOKEN_KEYWORD_VOID
    |   TOKEN_KEYWORD_TYPEOF
    |   TOKEN_OPERATOR_PLUS 
    |   TOKEN_OPERATOR_MINUS 
    |   TOKEN_OPERATOR_BITWISE_NOT 
    |   TOKEN_OPERATOR_LOGICAL_NOT
    ;
	   
/**
 * Matches "Postfix Expression" such as: i++, i--
 *
 * Since ECMA semicolon insertion rule requires that if a "++" or "--" is not
 * on the same line as its left-hand side expression, a semicolon is inserted
 * before the "--" or "++" token. The side-effect of the inserted semicolon is
 * to terminate the expression parsing at this point. As a result, we have to
 * return "null" to stop parsing the expression. An upstream production will
 * pickup the "--" or "++" by starting a new expression.
 *
 * A good test case for such situation would be:
 *
 *    var i = 99
 *    ++i
 *
 * A semicolon should be inserted after "99", resulting in two separate ASTs
 * for "var i=99" and "++i". Otherwise, "var i=99++" is a bad recognition.
 */
postfixExpr returns [ExpressionNodeBase n]
{
    n = null; 
    boolean isSemicolonInserted = false;
}
	: n=lhsExpr 
	  {
	      final ASToken nextToken = LT(1); 
	      if (nextToken.getType() == ASTokenTypes.TOKEN_OPERATOR_INCREMENT ||
	          nextToken.getType() == ASTokenTypes.TOKEN_OPERATOR_DECREMENT)
	          isSemicolonInserted = beforeRestrictedToken(nextToken);
      }
      (   {!isSemicolonInserted}? (options{greedy=true;}: n=postfixOp[n])?
      |   // Do nothing if optional semicolon is inserted.
          // This empty alternative is required because otherwise a semantic 
          // predicate exception will be thrown, leading the code enter error 
          // handling, which will create incorrect tree shape.
      )
	;

/**
 * Matches a "postfix" operator such as: ++, --
 * The parameter "n" is the expression the postfix operator acts on.
 * The return value "top" is a UnaryOperatorNode.
 */ 
postfixOp[ExpressionNodeBase n] returns [UnaryOperatorNodeBase top]
{
    final ASToken op = LT(1); 
    top = null;
}	
	:  ( TOKEN_OPERATOR_INCREMENT 
	   | TOKEN_OPERATOR_DECREMENT )
       { top = UnaryOperatorNodeBase.createPostfix(op, n); }
	;

/**
 * Matches a primary expression.
 */ 
primaryExpression returns [ExpressionNodeBase n]
{ 
    n = null;  
    ASToken token = LT(1);
}
    :   TOKEN_KEYWORD_NULL
        { n = new LiteralNode(token, LiteralType.NULL); }
    |   TOKEN_KEYWORD_TRUE
        { n = new LiteralNode(token, LiteralType.BOOLEAN); }
    |   TOKEN_KEYWORD_FALSE
        { n = new LiteralNode(token, LiteralType.BOOLEAN); }
    |   TOKEN_KEYWORD_THIS
        { n = LanguageIdentifierNode.buildThis(token); }
    |   token=numericLiteral
        { n = new NumericLiteralNode(token); }
    |   TOKEN_LITERAL_STRING
        { n = new LiteralNode(token, LiteralType.STRING); }
    |   TOKEN_VOID_0
        { n = new LiteralNode(token, LiteralType.OBJECT); }
    |   TOKEN_LITERAL_REGEXP
        { n = new RegExpLiteralNode(token, this); }
    |   { n = new ArrayLiteralNode(); } arrayInitializer[(ArrayLiteralNode)n]
    |   n=objectLiteralExpression
    |   n=xmlInitializer      { leaveXMLLiteral(); }
    |   n=xmlListInitializer  { leaveXMLLiteral(); }
    |   n=functionExpression 
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }
	
/**
 * Matches a numeric literal token.
 */
numericLiteral returns [ASToken op]
{ 
	op = LT(1); 
}
    :   TOKEN_LITERAL_NUMBER 
    |   TOKEN_LITERAL_HEX_NUMBER
	;
	
/**
 * Matches an "object literal".
 */
objectLiteralExpression returns [ExpressionNodeBase n]
{ 
	ObjectLiteralNode o = new ObjectLiteralNode(); 
	n = o; 
	ContainerNode b = o.getContentsNode(); 
	ExpressionNodeBase vp = null;
}
    :   openT:TOKEN_BLOCK_OPEN           { n.startBefore(openT); }
        (   vp=objectLiteralValuePair    { b.addItem(vp); }
      	    (   TOKEN_COMMA vp=objectLiteralValuePair
    	        { if (vp != null) b.addItem(vp); }
     		    exception catch [RecognitionException ex] 
     		    { handleParsingError(ex); }
      	    )*
        )?		
        closeT:TOKEN_BLOCK_CLOSE         { n.endAfter(closeT); }  		
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a "field" in an "object literal".
 * The "field" can be gated with a "config condition". If the condition is
 * "false", return "null" value; Otherwise, return the expression node of the
 * key/value pair.
 */
objectLiteralValuePair returns [ExpressionNodeBase n]
{ 
	ExpressionNodeBase v = null;  
	n = null;            
	boolean condition = true;   
    ASToken numberT = null;     
}
    :   (   { isConfigCondition() && LA(4) != TOKEN_COLON && LA(4) != TOKEN_BLOCK_CLOSE }?
            condition=configCondition
        |   // Skip - no config varaible.
        )
        
        // Field name:
        (   nameT:TOKEN_IDENTIFIER
        	{ n = new NonResolvingIdentifierNode(nameT != null ? nameT.getText() : "",nameT); }
        |   numberT=numericLiteral
        	{ n = new NumericLiteralNode(numberT); }
        |   stringT:TOKEN_LITERAL_STRING
        	{ n = new LiteralNode(LiteralType.STRING, stringT); }
        )
        
        c:TOKEN_COLON 
        
        // Field value:
        v=assignmentExpression 
    	{ 
    		if (condition)
    		    n = new ObjectLiteralValuePairNode((ASToken)c,n,v); 
		    else
		        n = null;
	    }
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex);  }
 

/**
 * Matches array literal. For example:
 *
 *    []
 *    ["hello", 3.14, foo]
 *    [ , x, y]
 *    [ , ,]
 *
 * "Holes" (empty array elements) are allowed. See "arrayElements" rule for details.
 */ 
arrayInitializer [ArrayLiteralNode node] 
    {
        final ContainerNode contents = node.getContentsNode(); 
    }
    :   open:TOKEN_SQUARE_OPEN            { node.startBefore(open); contents.startAfter(open); }
        arrayElements[contents]
        close:TOKEN_SQUARE_CLOSE          { node.endAfter(close); contents.endBefore(close); }
    ;	
    exception catch [RecognitionException ex] 
    { 
    	// Do not convert keywords to identifiers.
    	// This is for recovering from:
    	// [
    	// var x:int;
    	handleParsingError(ex); 
    	// Notify the caller that the array literal failed.
    	throw ex; 
    }
	
/**
 * Matches all the elements in an "arrayInitializer". For example:
 *
 *    x,y,z
 *    x,,
 *    (empty)
 *    ,,,,,,
 *
 * "Holes" are compiled as "undefined".
 * Leading "holes" are kept as "undefined" values.
 * "Holes" in the middle are kept as "undefined" values.
 * Trailing "holes" are kept as "undefined" values except that the last "hole"
 * is dropped.
 *
 * For example: x=[,,1,,,2,,,] has 2 leading holes, 2 holes in the middle, and 3
 * holes at the end. All the holes except for the last trailing holes are kept
 * as undefined values:
 *
 * x[0]=undefined
 * x[1]=undefined
 * x[2]=1
 * x[3]=undefined
 * x[4]=undefined
 * x[5]=2
 * x[6]=undefined
 * x[7]=undefined
 * (end)
 *
 */
arrayElements[ContainerNode b]
{ 
    ExpressionNodeBase e = null;  
}
    :   ( TOKEN_COMMA { b.addItem(new NilNode()); } )*
        (   { LA(1) != TOKEN_SQUARE_CLOSE}?   
            	e=arrayElement { b.addItem(e); /*1*/ }
                (   TOKEN_COMMA 
                    (   { LA(1) != TOKEN_SQUARE_CLOSE && LA(1) != TOKEN_COMMA }? e=arrayElement { b.addItem(e); /*2*/}
                    |   { LA(1) != TOKEN_SQUARE_CLOSE && LA(1) == TOKEN_COMMA }? { b.addItem(new NilNode()); }
                    |   // Next token is "]" - pass.
            	 	)
             		exception catch [RecognitionException ex] { handleParsingError(ex); }
              	)*
      	|    // Next token is "]" - the initializer is a list of commas: [,,,,,]
      	)
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches an "array element" in an "array literal". An array element can be 
 * gated with a config variable. If the config variable is false, the element
 * will be matched as a "hole" in the array literal.
 */
arrayElement returns [ExpressionNodeBase e]
{ 
	e = null; 
	boolean c = true;  // config variable
}
    :   (   { isConfigCondition() && LA(4) != TOKEN_COMMA && LA(4) != TOKEN_SQUARE_CLOSE }?
            c=configCondition
        |   // Skip - no config varaible.
        )
        e=assignmentExpression
        {
        	if (!c) 
        	{
        		final NilNode nilNode = new NilNode(); 
        		nilNode.span(e, e);
        		e = nilNode;
    		}
        }
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex);  }
	
/**
 * Matches a "vector initializer".
 */
vectorLiteralExpression returns [VectorLiteralNode node] 
{ 
	node = new VectorLiteralNode(); 
	ContainerNode b = node.getContentsNode(); 
	ExpressionNodeBase type = null; 
}
	:   open:TOKEN_TYPED_LITERAL_OPEN      { node.endAfter(open); }
	 	type=type                          { node.setCollectionTypeNode(type); }
        close:TOKEN_TYPED_LITERAL_CLOSE    { node.endAfter(close); }
        openT:TOKEN_SQUARE_OPEN            { b.startAfter(openT); }
	 	(vectorLiteralContents[b])?
        closeT:TOKEN_SQUARE_CLOSE          { b.endBefore(closeT); }
	;
	exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a "vector element" in a vector initializer.
 */
vectorLiteralContents[ContainerNode b]
{ 
	ExpressionNodeBase e = null;  
}
    :   e=arrayElement       { b.addItem(e); }
        (   TOKEN_COMMA
            {
                //  A trailing comma is allowed, but 
                //  an intermediate comma is not.
                if ( LA(1) != TOKEN_SQUARE_CLOSE ){
                    e=arrayElement();
                    b.addItem(e); 
                }
    	 	}
            exception catch [RecognitionException ex] { handleParsingError(ex); }
      	)*
    ;

/**
 * Matches "XML literal expression".
 */
xmlInitializer returns [XMLLiteralNode n]
{ 
	n = new XMLLiteralNode(); 
	final ASToken lt = LT(1);
	enterXMLLiteral();
}
    :   { LA(1) == TOKEN_E4X_COMMENT || 
    	  LA(1) == TOKEN_E4X_CDATA || 
    	  LA(1) == TOKEN_E4X_PROCESSING_INSTRUCTION 
    	}? 
        xmlMarkup
        { n.appendLiteralToken(lt); }
        xmlWhitespace[n]
    |   (options { greedy = true; }: xmlElementContent[n] )+
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }	
    
/**
 * Match XML whitespace tokens. If {@code ContainerNode} is null, drop the
 * whitespace tokens.
 */
xmlWhitespace [BaseLiteralContainerNode n]
    :   (options { greedy = true; }: 
	        ws:TOKEN_E4X_WHITESPACE 
            { 
            	if (n != null) 
            	    n.appendLiteralToken((ASToken)ws); 
    	    }
        )*
	;
    
/**
 * Matches an XML comment, XML CDATA or XML PI token.
 */
xmlMarkup
    :   TOKEN_E4X_COMMENT
    |   TOKEN_E4X_CDATA
    |   TOKEN_E4X_PROCESSING_INSTRUCTION
    ;    
       
/**
 * Matches an E4X token that can be aggregated in "xmlTokenAggregated".
 * Instead of a full recursive descent parser for XML tags, the base class
 * uses a tag name stack to check matching tags. A complete parse tree with
 * XML structure is unnecessary and adds extra overhead to the parser.
 */
xmlToken [BaseLiteralContainerNode n]
{
	final ASToken t = LT(1);
}
    :   (   xmlMarkup
        |   TOKEN_E4X_WHITESPACE 
        |   TOKEN_E4X_ENTITY
        |   TOKEN_E4X_DECIMAL_ENTITY
        |   TOKEN_E4X_HEX_ENTITY 
        |   TOKEN_E4X_TEXT
        |   TOKEN_E4X_STRING
        )
        { n.appendLiteralToken(t); }       
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }	
    
/**
 * Matches an XML tag.
 *
 *     <foo>
 *     </foo>
 *     <foo />
 *     <{name}>
 *     <foo name={nameValue}>
 *     <foo {attrs}>
 */
xmlTag [BaseLiteralContainerNode n]
    :   (   openT:TOKEN_E4X_OPEN_TAG_START          // <foo
            { 
            	xmlTagOpen((ASToken)openT); 
                n.appendLiteralToken((ASToken)openT);
        	} 
        |   closeT:TOKEN_E4X_CLOSE_TAG_START        // </foo
            { 
            	xmlTagClose((ASToken)closeT); 
                n.appendLiteralToken((ASToken)closeT);
        	} 
        |   openNoNameT:HIDDEN_TOKEN_E4X            // <
            { 
            	xmlTagOpenBinding((ASToken)openNoNameT); 
                n.appendLiteralToken((ASToken)openNoNameT);
        	}
        	
        	// Note about compatibility:
        	// x = <  tagName foo="bar" />;
        	//      ^
        	// Whitespace isn't allowed here according to ASL spec.
            // Avik from AS3 spec team confirmed it was a bug that the old ASC allowed it.

        	(   xmlContentBlock[n]
        	|   nT:TOKEN_E4X_NAME
        	    { n.appendLiteralToken((ASToken)nT); }
        	)
        )
        xmlWhitespace[n]
        (   ( { isXMLAttribute() }? xmlAttribute[n] 
	    | xmlContentBlock[n] )
            xmlWhitespace[n]
        )*
        (   endT:TOKEN_E4X_TAG_END                  // >
            { n.appendLiteralToken((ASToken)endT); }
        |   emptyEndT:TOKEN_E4X_EMPTY_TAG_END       // />
            {
            	xmlEmptyTagEnd((ASToken)emptyEndT); 
                n.appendLiteralToken((ASToken)emptyEndT);
            }
        )
    ;
    
/**
 * Matches an XML attribute.
 * 
 *     name="value"
 *     name='value'
 *     name={value}
 *     {name}="value"
 *     {name}='value'
 *     {name}={value}
 */
xmlAttribute [BaseLiteralContainerNode n]
    :   (   nT:TOKEN_E4X_NAME  
            { n.appendLiteralToken((ASToken)nT); }
        |   nsT:TOKEN_E4X_XMLNS 
            { n.appendLiteralToken((ASToken)nsT); }
        |   xmlAttributeBlock[n]
        ) 
        (   dT:TOKEN_E4X_NAME_DOT 
            { n.appendLiteralToken((ASToken)dT); }
            dnT:TOKEN_E4X_DOTTED_NAME_PART 
            { n.appendLiteralToken((ASToken)dnT); }
        )* 
        xmlWhitespace[n]
        eqT:TOKEN_E4X_EQUALS
        { n.appendLiteralToken((ASToken)eqT); }
        xmlWhitespace[n]
        (options { greedy = true; }:
            strT:TOKEN_E4X_STRING
            { n.appendLiteralToken((ASToken)strT); }
        |   eT:TOKEN_E4X_ENTITY
            { n.appendLiteralToken((ASToken)eT); }
        |   hexT:TOKEN_E4X_HEX_ENTITY
            { n.appendLiteralToken((ASToken)hexT); }
        |   xmlContentBlock[n]
        )+
    ;

/**
 * Matches an expression block in XML literals.
 *
 *    <foo>{ this.fooValue }</foo>
 */
xmlElementContent [BaseLiteralContainerNode n]
    :   xmlToken[n] 
    |   xmlContentBlock[n]
    |   xmlTag[n]
    ;
    
/**
 * Matches an E4X XML list expression.
 */
xmlListInitializer returns [XMLListLiteralNode n]
{ 
	n = new XMLListLiteralNode(); 
	enterXMLLiteral();
}
    :   xmlListT:TOKEN_LITERAL_XMLLIST 
    	{ n.getContentsNode().addItem(new LiteralNode(LiteralType.XML, xmlListT)); }      
        ( xmlElementContent[n] )*
        closeT: TOKEN_E4X_XMLLIST_CLOSE 
    	{ n.getContentsNode().addItem(new LiteralNode(LiteralType.XML, closeT)); }      
    	xmlWhitespace[null]
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }	

/**
 * Matches a binding expression in an XML literal.
 */
xmlContentBlock[BaseLiteralContainerNode n]
{ 
	ExpressionNodeBase e = null; 
}
    :   TOKEN_E4X_BINDING_OPEN 
        e=expression 
    	{ 
            if(e != null) 
                n.getContentsNode().addItem(e); 
    	}
        TOKEN_E4X_BINDING_CLOSE
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }
    

/**
 * Matches a binding expression in an XML literal attribute name.
 */
xmlAttributeBlock[BaseLiteralContainerNode n]
{ 
	ExpressionNodeBase e = null; 
}
    :   TOKEN_E4X_BINDING_OPEN 
        e=lhsExpr 
    	{ 
            if(e != null) 
                n.getContentsNode().addItem(e); 
    	}
        TOKEN_E4X_BINDING_CLOSE
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }
    

/**
 * Matches a left-hand side (of asssignment) expression.
 */
lhsExpr returns [ExpressionNodeBase n]
{
	n = null;
}
    :   (   n=newExpression 
        |   n=parenExpression 
        |   n=nameExpression 
        |   n=primaryExpression 
        |   n=xmlAttributeName
        )
    	(options { greedy = true; }: 
            n=propertyAccessExpression[n] 
        |   n=arguments[n] 
    	)*	
    ;
	exception catch [RecognitionException ex] { handleParsingError(ex); }
	
/**
 * Matches a member expression. See ASL syntax spec for details.
 */
memberExpression returns [ExpressionNodeBase n]
    :   (   n=primaryExpression
        |   n=parenExpression
        |   n=propertyName
        |   n=newExpression
        )
        ( options { greedy = true; }: n=propertyAccessExpression[n] )*
    ;

/**
 * Matches a new expression. See ASL syntax spec for details.
 */			
newExpression returns[ExpressionNodeBase n]
{ 
	n = null; 
}
	:   newT:TOKEN_KEYWORD_NEW 
        (   { LA(1) != TOKEN_KEYWORD_FUNCTION }?
        	(   n=vectorLiteralExpression
            |   n=memberExpression 
            )   
            {   
                if (n == null) 
                	n= handleMissingIdentifier(null); 
            	else 
            		n = FullNameNode.toMemberAccessExpressionNode(n);
				n = new FunctionCallNode((ASToken)newT, n); 
			} 
            (options{greedy=true;}: n=arguments[n])?
        |   n=functionExpression  { n = new FunctionCallNode((ASToken)newT, n); }
        )
		exception catch [RecognitionException ex] { 
			//if we have the 'new' keyword, but no expression, drop in a dummy identifier
		    if(newT != null && n == null) {
			    IdentifierNode identifier = handleMissingIdentifier(ex);
			    if(identifier != null) {
			        //if we're here, that means identifier fixup is turned on
			        n = new FunctionCallNode((ASToken)newT, identifier);
			    }
			} else {
			    handleParsingError(ex); 
			}
		}
	;

/**
 * Matches an expression with parenthesis.
 *
 *     (id)
 *     (1 + 2)
 *     (name == "hello")
 *
 */
parenExpression returns[ExpressionNodeBase n]
{
    n = null; 
} 
    :   TOKEN_PAREN_OPEN n=expression TOKEN_PAREN_CLOSE
	    { if(n != null) n.setHasParenthesis(true); }
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a property name in a member expression.
 */
propertyName returns [ExpressionNodeBase n]
{
    n = null;
}
    :   n=starLiteral                 // *
    |   n=restrictedName              // qualified & unqualified name
    |   n=xmlAttributeName            // e4x attribute
    ;
    
/**
 * This is a non-greedy and non-aggregating version of "restricted name".
 * It is defined in addition to "propertyName" in order to get correct
 * precedence in the name expressions and expected tree shapes.
 */
nameExpression returns[ExpressionNodeBase n]
	{ n = null; }
	
	: n=identifier 
	| n=starLiteral
	| superT:TOKEN_KEYWORD_SUPER
		{ n = LanguageIdentifierNode.buildSuper((IASToken)superT); }
	| nsT:TOKEN_NAMESPACE_NAME
		{ 
			n = new NamespaceIdentifierNode((ASToken)nsT); 
			((NamespaceIdentifierNode)n).setIsConfigNamespace(isConfigNamespace((NamespaceIdentifierNode)n));	
		}
	;	
	
	
/**
 * Matches an E4X attribute name. For example:
 *  
 *     "@*", @data, @[foo="hello"]
 *
 */
xmlAttributeName returns [ExpressionNodeBase result]
{
    result = UnaryOperatorNodeBase.createPrefix(LT(1), null);
    ExpressionNodeBase e = null;
}
    :   TOKEN_OPERATOR_ATSIGN
        (	(   e=starLiteral                
	        |   e=identifier                 
	        |   nsT:TOKEN_NAMESPACE_NAME     { e = new NamespaceIdentifierNode((ASToken)nsT); }
	        )
	        { 
	        	((UnaryOperatorNodeBase)result).setExpression(e); 
        	}
        |   result=bracketExpression[result]
        )
    ;

/**
 * Matches a property access expression:
 * For example: (assuming 'foo' is already matched)
 *     foo.bar
 *     foo..bar
 *     foo::bar
 *     foo[bar]
 *     foo.<bar>
 */  	
propertyAccessExpression [ExpressionNodeBase l] returns [ExpressionNodeBase n]
{ 
    n = null;  
    ExpressionNodeBase r = null; 
    final ASToken op = LT(1); 
}
    :   TOKEN_OPERATOR_MEMBER_ACCESS r=accessPart 
        { n = new MemberAccessExpressionNode(l, op, r); }
    |   TOKEN_OPERATOR_DESCENDANT_ACCESS r=accessPart
        { n = new MemberAccessExpressionNode(l, op, r); }
    |   TOKEN_OPERATOR_NS_QUALIFIER r=nsAccessPart
        { if (l instanceof NamespaceIdentifierNode)
          {
            final NamespaceIdentifierNode nsNode = (NamespaceIdentifierNode)l; 
            nsNode.setIsConfigNamespace(isConfigNamespace(nsNode));
            final IdentifierNode idNode = (IdentifierNode)r;
	    n = transformToNSAccessExpression(nsNode, (ASToken) op, idNode);
            n = n.copyForInitializer(null);
	    n.setSourcePath(nsNode.getSourcePath());
	    n.setLine(nsNode.getLine());
	    n.setColumn(nsNode.getColumn());
	    n.setEndLine(idNode.getEndLine());
	    n.setEndColumn(idNode.getEndColumn());
	    n.setStart(nsNode.getStart());
	    n.setEnd(idNode.getEnd());
          }
          else
          {
	    n = transformToNSAccessExpression(l, (ASToken) op, r);
          }
        } 
    |   n=bracketExpression[l]
    |   n=typeApplication[l]
    ;
	
/**
 * Matches parts after the dot in a property access expression.
 */
accessPart returns [ExpressionNodeBase n]
{
    n = null; 
}
	:   n=nameExpression
	|   n=xmlAttributeName 
	|   n=parenExpression 
	;
	exception catch [RecognitionException ex] { n = handleMissingIdentifier(ex);  }

/**
 * Matches parts after "::" in a property access expression.
 */
nsAccessPart returns [ExpressionNodeBase n]
{
    n = null; 
}
	:   n=nameExpression
	|   n=xmlAttributeName 
	|   n=parenExpression 
	|   n=runtimeName
	;
	exception catch [RecognitionException ex] { n = handleMissingIdentifier(ex);  }
	
/**
 * Matches a runtime attribute name.
 * 
 *     foo["name"]
 */
runtimeName returns[ExpressionNodeBase n]
{  
	ExpressionNodeBase e = null; 
	n = null; 
}
    :   TOKEN_SQUARE_OPEN e=expression closeT:TOKEN_SQUARE_CLOSE
    	{ 
    		n = new RuntimeNameExpressionNode(e);
    		n.endAfter(closeT); 
    	}
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }

/**
 * Matches a "star" literal token, and create an IdentifierNode for "*".
 */
starLiteral returns [IdentifierNode id]
{
    id = LanguageIdentifierNode.buildAnyType(LT(1));
}
    :   TOKEN_OPERATOR_STAR
    ;

/**
 * Matches a "void" keyword token, and create an IdentifierNode for "void".
 */
voidLiteral returns [IdentifierNode id]
{
    id = LanguageIdentifierNode.buildVoid(LT(1));
}
    :   TOKEN_KEYWORD_VOID
    ;
	
/**
 * Matches the arguments of a function call: allcharacters between (...) 
 * including the parenthesis.
 *
 *    foo(true, 10, "hello");
 *    new MyData(100, "hundred");
 *
 * "newExpression" rule always creates a FunctionCallNode and passes it in as
 * the "root". Since the "newExpression" rule uses "arguments" rule to consume
 * its arguments, we don't need to create a new FunctionCallNode because the
 * "root" parameter is the FunctionCallNode to which the arguments belongs.
 *
 * On the other hand, if the "root" argument is not a FunctionCallNode from a
 * "new" expression, then the "root" can only be the "name expression" of a 
 * function call. As a result, we must create a FunctionCallNode for it. 
 * For example:
 *     myFunc() -- the name expression is identifier "myFunc"
 *     token.getCallBack()() -- the name expression is "token.getCallBack()"
 *                              expecting the return value of "getCallBack" to
 *                              be a function object.
 * 
 * [Shaoting] We could also make newExpression rule not create a 
 *            FunctionCallNode inside the "newExpression" rule, and pass in 
 *            the "new" token so that all FunctionCallNode are constructed in
 *            this rule. However, we want the parser to construct nodes as early
 *            as possible for IDE features. For example: "new T" without "(..)"
 *            would not result in a FunctionCallNode if we don't create it right
 *            after we see "T".
 */
arguments[ExpressionNodeBase root] returns[ExpressionNodeBase n]
{ 
	n = root; 
	ContainerNode args = null; 
}
    :   lpT:TOKEN_PAREN_OPEN
    	{
    	    final boolean isNewExpression = 
    	            (n instanceof FunctionCallNode) && 
    	            ((FunctionCallNode)n).isNewExpression();
            final boolean newFunctionCallAlreadyHasArgs = isNewExpression &&
	            ((FunctionCallNode)n).getArgumentsNode().getStart() != -1;
	            // the above line is a hack to try to catch "new" expressions
	            // where the class to instantiate is the result of a function
	            // call:  new someFunction(someArgs)(constructorParams)
	            // we check to see if the arg node as a start() value which
	            // means that the first argument list (someargs) was already
	            // processed.
    	    final FunctionCallNode oldNode = newFunctionCallAlreadyHasArgs ? (FunctionCallNode)n : null;
    		if (n == null || !isNewExpression || newFunctionCallAlreadyHasArgs ) 
    			n = new FunctionCallNode(n);
    		if (newFunctionCallAlreadyHasArgs) {
                    ((FunctionCallNode)n).setNewKeywordNode(oldNode.getNewKeywordNode());
                    oldNode.setNewKeywordNode(null);
                }
    		args = ((FunctionCallNode)n).getArgumentsNode();
    		args.startBefore(lpT);
    		args.endAfter(lpT);
    		disableSemicolonInsertion();
    	} 
    		
        ( argumentList[args] )
    
    ;	
    exception catch [RecognitionException ex]
    { 	
    	//only consume this error b/c we're looking for a missing ')'  Let the parser handle the next token, maybe it will correct us
    	consumeParsingError(ex); 
    	if ( args != null ){
    		if(ex instanceof NoViableAltException) {
    			args.endBefore(((NoViableAltException)ex).token);
    		} else if(ex instanceof MismatchedTokenException) {
    			args.endBefore(((MismatchedTokenException)ex).token);
    		} else {
    			endContainerAtError(ex, args);
    	}	
    	}	
    	enableSemicolonInsertion(); 
    }

/**
 * Matches an argument list in a function call arguments.
 *
 * For argument list, we want to support the case where the user might be in the 
 * middle of typing, which could mean a couple of things:
 * 1. The user adds an argument before another.
 * 2. After which could lead to comma where we don't want it.
 */	
argumentList[ContainerNode args]
{ 
	ExpressionNodeBase n = null; 
	boolean foundFirstArg = false; 
}
    :   (   n=assignmentExpression
    		{ foundFirstArg = true; if (args != null) args.addItem(n); }
        	exception catch [RecognitionException ex] 
    		{ 
    			n = handleMissingIdentifier(ex); 
    			if(n != null) {
    				foundFirstArg = true;  //we don't want to add a second error for this case
    				if (args != null) args.addItem(n);
    			}
    		}
    	)? //make this optional for malformed code handling, but we will log a parser error against it
    	
    	(  commaT:TOKEN_COMMA  
    		{ 
    			//if we didn't find the first arg, log an error
    			if(!foundFirstArg) {
    				logSyntaxError((ASToken)commaT);
    				foundFirstArg = true;
    				n = handleMissingIdentifier(null); 
    				if (n!= null && args != null) args.addItem(n);
    			}
    			if (args != null) args.endAfter(commaT);	
    		}
    	
    		n=assignmentExpression
			{ 
				if(n == null) 
				    n = handleMissingIdentifier(null); 
			    
			    if (args != null) 
                    args.addItem(n); 
            }
    	
    		exception catch [RecognitionException ex] 
			{ 
				n = handleMissingIdentifier(ex); 
				if(n != null && args != null) {
					args.addItem(n);
				}
			}

    	)*

		rpT:TOKEN_PAREN_CLOSE
    		{ args.endAfter(rpT); enableSemicolonInsertion(); }

    ;
		
/**
 * Matches a bracket expression. For example:
 *
 *     [10]
 *     [idx]
 *
 */
bracketExpression [ExpressionNodeBase root] returns [DynamicAccessNode result]
{ 
    result = new DynamicAccessNode(root); 
    ExpressionNodeBase e = null; 
}
    :   TOKEN_SQUARE_OPEN 
        e=expression 
        (   closeT:TOKEN_SQUARE_CLOSE
            exception catch [RecognitionException ex] { handleParsingError(ex); }
        )
        { 
            result.setRightOperandNode(e); 
            result.endAfter(closeT); 
        }
    ;
    exception catch [RecognitionException ex] { handleParsingError(ex); }
