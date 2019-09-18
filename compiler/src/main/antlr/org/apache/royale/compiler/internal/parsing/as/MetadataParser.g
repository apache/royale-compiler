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
 * This file is generated from MetadataParser.g.
 * DO NOT MAKE EDITS DIRECTLY TO THIS FILE.  THEY WILL BE LOST WHEN THE FILE IS GENERATED AGAIN!!!
 */

import java.util.HashMap;

import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;

import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.metadata.*; 

import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;

}

// MetadataParser parses a bunch of metadata attrs for a single node
// (var, class, etc.) and makes up some tables of what it found.
class MetadataParser extends Parser("org.apache.royale.compiler.internal.parsing.as.BaseMetaTagParser");
options
{
	importVocab = ImportMetadata;
	exportVocab = Metadata;
}

meta[MetaTagsNode parent]
{
	MetaTagNode tag = null;
}
	: ( asDocComment | openBrace : TOKEN_OPEN_BRACE 
	( tag = event[parent]
	| tag = effect[parent]
	| tag = style[parent]
	| tag = eventTrigger[parent]
	| tag = typedTag[parent]
	| tag = inspectable[parent]
	| tag = defaultproperty[parent]
	| tag = accessibilityClass[parent]
	| tag = multiValue[parent]
	| tag = skinClass[parent]
	| tag = alternative[parent]
	| tag = resourcebundle[parent]	
	| tag = other[parent]
	)
	(closeBrace : TOKEN_CLOSE_BRACE
	| closeParen : TOKEN_CLOSE_PAREN )*
	)*
	{
		afterTag(tag, openBrace, closeBrace, closeParen);
	}	
	;

asDocComment : docToken: TOKEN_ASDOC_COMMENT 
	{
		handleComment(docToken);
	}
	;
	
event[MetaTagsNode parent] returns [EventTagNode node]
	{ 	
		node = new EventTagNode();
		}
	: TOKEN_EVENT_KEYWORD 
		(TOKEN_ATTR_NAME ( nameString: TOKEN_STRING { node.setName(build(nameString)); } )
		| TOKEN_ATTR_TYPE (typeString: TOKEN_STRING { node.setEvent(typeString); } ) 
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		applyComment(node);
		parent.addTag(node);
		return node;
	}
	;
	
effect[MetaTagsNode parent] returns [EffectTagNode node]
	{ 
		node = new EffectTagNode();
	}
	: TOKEN_EFFECT_KEYWORD 
		(TOKEN_ATTR_NAME ( nameString: TOKEN_STRING { node.setName(build(nameString)); } )
		| TOKEN_ATTR_EVENT (typeString: TOKEN_STRING { node.setEvent(build(typeString)); } ) 
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
			
		applyComment(node);
		parent.addTag(node);
	
	}
	;

style[MetaTagsNode parent] returns [StyleTagNode node]
	{ 	
		node = new StyleTagNode();
		}
	: TOKEN_STYLE_KEYWORD 
		(TOKEN_ATTR_NAME ( nameString: TOKEN_STRING { node.setName(build(nameString)); } )
		| TOKEN_ATTR_TYPE ( typeString: TOKEN_STRING { node.setType(typeString); } )
		| TOKEN_ATTR_ARRAY_TYPE ( arrayTypeToken: TOKEN_STRING { node.setArrayType(build(arrayTypeToken)); } )
		| TOKEN_ATTR_INHERITS ( boolType: TOKEN_STRING { node.setIsInheritable(getText(boolType)); } )
		| TOKEN_ATTR_FORMAT ( formatToken: TOKEN_STRING { node.setFormat(getText(formatToken).trim()); } )
		| TOKEN_ATTR_ENUM ( valuesToken : TOKEN_STRING { node.parseValues(getText(valuesToken).trim()); } )
		| TOKEN_ATTR_STATES ( statesToken : TOKEN_STRING { node.parseStates(getText(statesToken).trim()); } )
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		applyComment(node);
		parent.addTag(node);
	}
	;
	
inspectable[MetaTagsNode parent] returns [InspectableTagNode node]
	{ 	
		IdentifierNode type = null;
		IdentifierNode var = null;
		IdentifierNode arrayType = null;
		node = new InspectableTagNode();
		resetComments(node.getTagName());
		}
		
	: TOKEN_INSPECTABLE_KEYWORD 
		(TOKEN_ATTR_NAME ( nameToken: TOKEN_STRING { node.setName( getText(nameToken).trim() ); } )
		| TOKEN_ATTR_ARRAY_TYPE ( atypeToken : TOKEN_STRING { node.setArrayType(build(atypeToken)); } )
		| TOKEN_ATTR_TYPE ( typeToken : TOKEN_STRING { node.setType(typeToken); } )
		| TOKEN_ATTR_VARIABLE ( varToken : TOKEN_STRING { node.setVariable(build(varToken)); } )
		| TOKEN_ATTR_FORMAT ( formatToken : TOKEN_STRING { node.setFormat( getText(formatToken).trim() ); } )
		| TOKEN_ATTR_ENUM ( valuesToken : TOKEN_STRING { node.parseValues( getText(valuesToken).trim() ); } )
		| TOKEN_ATTR_CATEGORY ( catToken : TOKEN_STRING { node.setCategory( getText(catToken).trim()); } )
		| TOKEN_ATTR_DEFAULT_VALUE ( defaultToken : TOKEN_STRING { node.setDefaultValue( getText(defaultToken).trim()); } )
		| TOKEN_ATTR_VERBOSE ( verToken : TOKEN_STRING { node.setVerbose(getText(verToken).trim()); } )
		| TOKEN_ATTR_ENV ( envToken : TOKEN_STRING {node.setEnvironment(getText(envToken).trim()); } )
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		parent.addTag(node);
	}
	;
	
eventTrigger[MetaTagsNode parent] returns [EventTriggerTagNode node]
	{ 	
		node = null;
		IdentifierNode event = null;
		String name = null;
	}
	: ( name = eventTriggerSet) {
		resetComments(name);
		node = new EventTriggerTagNode(name);
	}
		(TOKEN_ATTR_EVENT (eventToken: TOKEN_STRING { event = build(eventToken); if(event != null) { node.setEventName(event); }} )
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		parent.addTag(node);
	}
	;
	
eventTriggerSet returns [String value]
	{
		value = null;
	}
	: bk : TOKEN_BINDABLE_KEYWORD
		{
			value = bk.getText();
		}
	| nck : TOKEN_NONCOMMITTINGCHANGE_KEYWORD
		{
			value = nck.getText();
		}
	;
	

typedTag[MetaTagsNode parent] returns [TypedTagNode node]
	{ 	
		node = null;
		IdentifierNode type = null;
		String name = null;
	}
	: (name = typedTagSet)
		{
			node = new TypedTagNode(name);
			resetComments(name);
		}
		( typeToken:TOKEN_STRING 
		  { node.setTypeName(null, typeToken); }
		| TOKEN_OPEN_PAREN {})*
	{	
		parent.addTag(node);
	}
	;
	
skinClass[MetaTagsNode parent] returns [SkinClassTagNode node]
	{ 	
		node = null;
		IdentifierNode type = null;
		String name = null;
		node = new SkinClassTagNode();
		resetComments(node.getTagName());
	}
	: TOKEN_SKINCLASS_KEYWORD
		( TOKEN_ATTR_TYPE typeToken:TOKEN_STRING 
		  { node.setTypeName(IMetaAttributeConstants.NAME_EVENT_TYPE, typeToken); }
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		parent.addTag(node);
	}
	;
	
alternative[MetaTagsNode parent] returns [AlternativeTagNode node]
	{ 	
		node = null;
		IdentifierNode type = null;
		String name = null;
		node = new AlternativeTagNode();
		resetComments(node.getTagName());
	}
	: TOKEN_ALTERNATIVE_KEYWORD
		( TOKEN_ATTR_TYPE typeToken:TOKEN_STRING 
		  { node.setTypeName(IMetaAttributeConstants.NAME_ALTERNATIVE_REPLACEMENT, typeToken); }
		| unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
	{	
		parent.addTag(node);
	}
	;
	
typedTagSet returns [String value]
	{
		value = null;
	}
	: ik : TOKEN_INSTANCETYPE_KEYWORD
		{
			value = ik.getText();
		}
	| hck : TOKEN_HOST_COMPONENT_KEYWORD
		{
			value = hck.getText();
		}
	| aek : TOKEN_ARRAYELEMENTTYPE_KEYWORD
		{
			value = aek.getText();
		}
	;
	
multiValue[MetaTagsNode parent] returns [MultiValueMetaTagNode node]
	{ 	
		String state = null;
		node = null;
		String name = null;
	}
	: (name = mvSet) 
		{
			node = new MultiValueMetaTagNode(name);
			resetComments(name);
		}
		(stateToken: TOKEN_STRING { state = getText(stateToken); if(state != null) { node.addValue(state); state = null;}}
		| TOKEN_OPEN_PAREN {})*
	{	
		parent.addTag(node);
	}
	;
	
mvSet returns [String value]
	{
		value = null;
	}
	: bk : TOKEN_STATES_KEYWORD
		{
			value = bk.getText();
		}
	| nck : TOKEN_SKIN_STATES_KEYWORD
		{
			value = nck.getText();
		}
	;
	
accessibilityClass[MetaTagsNode parent] returns [AccessibilityTagNode node]
	{ 	
		IdentifierNode type = null;
		IdentifierNode name = null;
		node = new AccessibilityTagNode();
		resetComments(node.getTagName());	
	}
	: TOKEN_ACCESSIBILITY_KEYWORD 
		(TOKEN_ATTR_IMPLEMENTATION nameString:TOKEN_STRING
		 { node.setTypeName(IMetaAttributeConstants.NAME_ACCESSIBILITY_IMPLEMENTATION, nameString);}
		| TOKEN_OPEN_PAREN {})*
	{	parent.addTag(node); }
	;
	
	
defaultproperty[MetaTagsNode parent] returns [DefaultPropertyTagNode node]
	{ 	
		IdentifierNode type = null;
		node = new DefaultPropertyTagNode();
		resetComments(node.getTagName());
	}
	: TOKEN_DEFAULTPROPERTY_KEYWORD 
		(typeToken: TOKEN_STRING { type = build(typeToken); if(type != null) { node.setPropertyNameNode(type); }}
		| TOKEN_OPEN_PAREN {})*
	{	
		parent.addTag(node);
	}
	;
	
resourcebundle[MetaTagsNode parent] returns [ResourceBundleTagNode node]
	{ 	
		IdentifierNode bundleNameNode = null;
		node = new ResourceBundleTagNode();
		resetComments(node.getTagName());
	}
	: TOKEN_RESOURCEBUNDLE_KEYWORD 
		( {LA(1) == TOKEN_STRING}? bundleToken:TOKEN_STRING { bundleNameNode = build(bundleToken); if(bundleNameNode != null) { node.setBundleNameNode(bundleNameNode); }}
		| unknownProperty[node]
		| unknownTypeAttribute[node]
		| TOKEN_OPEN_PAREN {})*
	{	
		parent.addTag(node);
	}
	;
	
unknownTypeAttribute[MetaTagNode node]
	: (TOKEN_ATTR_TYPE typeToken:TOKEN_STRING)
	{
		final String val = typeToken.getText();
		if(val != null)
		{
			node.addToMap(IMetaAttributeConstants.NAME_EVENT_TYPE, val);
		}
	}
	;
	
other[MetaTagsNode parent] returns [MetaTagNode node]
	{
		String name = "";
		node = null; 
	}
		: keyWord : TOKEN_UNKNOWN_KEYWORD 
			{ name = getText(keyWord); node = new BasicMetaTagNode(name); resetComments(name); } 
		( unknownProperty[node]
		| TOKEN_OPEN_PAREN {}
		)*
		{
			parent.addTag(node);
		}
	
	;

unknownProperty[MetaTagNode node]
    { 
    	String attr = null;
    }

    : { LA(1) == TOKEN_ATTR_UNKNOWN && LA(2) != TOKEN_STRING }? unknownPropertyNameOnly[node]	
	| (
		( attrName:TOKEN_ATTR_UNKNOWN 
    		{ attr = getText(attrName); }
    	)? 
    
    	stringVal:TOKEN_STRING 
			{ node.addToMap(attr, getText(stringVal)); } 
	)
    ;

unknownPropertyNameOnly[MetaTagNode node]

	: attrName:TOKEN_ATTR_UNKNOWN 
    	{ node.addToMap(getText(attrName), ""); }
	;