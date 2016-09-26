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

package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

public class BinaryOperatorEmitter extends JSSubEmitter implements
        ISubEmitter<IBinaryOperatorNode>
{

    public BinaryOperatorEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IBinaryOperatorNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        String op = node.getOperator().getOperatorText();
        boolean isAssignment = op.contains("=")
                && !op.contains("==")
                && !(op.startsWith("<") || op.startsWith(">") || op
                        .startsWith("!"));
        ASTNodeID id = node.getNodeID();
        /*
        if (id == ASTNodeID.Op_InID
                || id == ASTNodeID.Op_LogicalAndAssignID
                || id == ASTNodeID.Op_LogicalOrAssignID)
        {
            super.emitBinaryOperator(node);
        }
        else */if (id == ASTNodeID.Op_IsID || id == ASTNodeID.Op_AsID)
        {
            fjs.emitIsAs(node, node.getLeftOperandNode(), node.getRightOperandNode(),
                    id, false);
        }
        else if (id == ASTNodeID.Op_InstanceOfID)
        {
            getWalker().walk(node.getLeftOperandNode());

            startMapping(node, node.getLeftOperandNode());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.INSTANCEOF);
            endMapping(node);

            IDefinition dnode = (node.getRightOperandNode())
                    .resolve(getProject());
            if (dnode != null)
                write(getEmitter()
                        .formatQualifiedName(dnode.getQualifiedName()));
            else
                getWalker().walk(node.getRightOperandNode());
        }
        else
        {
            IExpressionNode leftSide = node.getLeftOperandNode();
            IDefinition leftDef = leftSide.resolveType(getWalker().getProject());
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode lnode = leftSide.getChild(0);
                IASNode rnode = leftSide.getChild(1);
                IDefinition rnodeDef = (rnode instanceof IIdentifierNode) ? 
                		((IIdentifierNode) rnode).resolve(getWalker().getProject()) :
                		null;
                if (lnode.getNodeID() == ASTNodeID.SuperID
                        && rnodeDef instanceof AccessorDefinition)
                {
                    if (isAssignment)
                    {
                        IClassNode cnode = (IClassNode) node
                                .getAncestorOfType(IClassNode.class);
                        if (cnode != null)
                        	write(getEmitter().formatQualifiedName(
                                cnode.getQualifiedName()));
                        else
                        	write(getEmitter().formatQualifiedName(
                        		getModel().getCurrentClass().getQualifiedName()));
                        			
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSGoogEmitterTokens.GOOG_BASE);
                        write(ASEmitterTokens.PAREN_OPEN);
                        write(ASEmitterTokens.THIS);
                        writeToken(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(JSFlexJSEmitterTokens.SETTER_PREFIX);
                        write(rnodeDef.getBaseName());
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        writeToken(ASEmitterTokens.COMMA);

                        if (op.length() > 1) // += and things like that
                        {
                            write(getEmitter().formatQualifiedName(
                                    cnode.getQualifiedName()));
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSGoogEmitterTokens.GOOG_BASE);
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(ASEmitterTokens.THIS);
                            writeToken(ASEmitterTokens.COMMA);
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(JSFlexJSEmitterTokens.SETTER_PREFIX);
                            write(rnodeDef.getBaseName());
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(ASEmitterTokens.PAREN_CLOSE);
                            write(op.substring(0, 1));
                        }

                        getWalker().walk(node.getRightOperandNode());
                        write(ASEmitterTokens.PAREN_CLOSE);
                        return;
                    }
                }
                else if (((JSFlexJSEmitter)getEmitter()).isXMLList((MemberAccessExpressionNode)leftSide))
                {
                	MemberAccessExpressionNode xmlNode = (MemberAccessExpressionNode)leftSide;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
	                    getWalker().walk(xmlNode.getLeftOperandNode());
	                    IExpressionNode rightSide = xmlNode.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute('");
		                    getWalker().walk(((UnaryOperatorAtNode)rightSide).getChild(0));
	                    }
	                    else
	                    {
		                    write(".setChild('");
		                    getWalker().walk(rightSide);
	                    }
	                    write("', ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    getWalker().walk(xmlNode.getLeftOperandNode());
	                    IExpressionNode rightSide = xmlNode.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute('");
		                    getWalker().walk(((UnaryOperatorAtNode)rightSide).getChild(0));
	                    }
	                    else
	                    {
		                    write(".setChild('");
		                    getWalker().walk(rightSide);
	                    }
	                    write("', ");
                        getWalker().walk(node.getLeftOperandNode());
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddID)
                	{
	                    getWalker().walk(xmlNode);
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
                else if (((JSFlexJSEmitter)getEmitter()).isProxy((MemberAccessExpressionNode)leftSide))
                {
                	MemberAccessExpressionNode proxyNode = (MemberAccessExpressionNode)leftSide;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    IExpressionNode rightSide = proxyNode.getRightOperandNode();
	                    write(".setProperty('");
	                    getWalker().walk(rightSide);
	                    write("', ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    IExpressionNode rightSide = proxyNode.getRightOperandNode();
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    write(".setProperty('");
	                    getWalker().walk(rightSide);
	                    write("', ");
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    write(".getProperty(");
	                    write(ASEmitterTokens.SINGLE_QUOTE);
	                    getWalker().walk(rightSide);
	                    write(ASEmitterTokens.SINGLE_QUOTE);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(" + ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
                else if (((JSFlexJSEmitter)getEmitter()).isDateProperty((MemberAccessExpressionNode)leftSide))
                {
                	specialCaseDate(node, (MemberAccessExpressionNode)leftSide);
                    return;
                }
            }
            else if (leftSide.getNodeID() == ASTNodeID.IdentifierID)
            {
    			if ((leftDef != null)
    				&& IdentifierNode.isXMLish(leftDef, getWalker().getProject()))
    			{
                	if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    getWalker().walk(leftSide);
	                    write(" = ");
	                    getWalker().walk(leftSide);
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
            }

            boolean leftIsNumber = (leftDef != null && (leftDef.getQualifiedName().equals(IASLanguageConstants.Number) ||
					  leftDef.getQualifiedName().equals(IASLanguageConstants._int) ||
					  leftDef.getQualifiedName().equals(IASLanguageConstants.uint)));
        	IExpressionNode rNode = node.getRightOperandNode();
        	IDefinition rightDef = rNode.resolveType(getWalker().getProject());
        	boolean rightIsNumber = (rightDef != null && (rightDef.getQualifiedName().equals(IASLanguageConstants.Number) ||
					  rightDef.getQualifiedName().equals(IASLanguageConstants._int) ||
					  rightDef.getQualifiedName().equals(IASLanguageConstants.uint)));
            if (leftIsNumber && !rightIsNumber && (rightDef == null || rightDef.getQualifiedName().equals(IASLanguageConstants.ANY_TYPE))
            		&& rNode.getNodeID() == ASTNodeID.FunctionCallID)
            {
            	IExpressionNode fnNameNode = ((FunctionCallNode)rNode).getNameNode();
            	if (fnNameNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            	{
            		MemberAccessExpressionNode mae = (MemberAccessExpressionNode)fnNameNode;
            		IExpressionNode rightNode = mae.getRightOperandNode();
            		rightIsNumber = rightNode.getNodeID() == ASTNodeID.IdentifierID && 
            				((IdentifierNode)rightNode).getName().equals("length") &&
            				fjs.isXMLList(mae);
            	}
            }
            String coercion = (leftIsNumber && !rightIsNumber && isAssignment) ? "Number(" : "";
            if (isAssignment && leftDef != null && leftDef.getQualifiedName().equals(IASLanguageConstants.String))
            {
            	if (rNode.getNodeID() != ASTNodeID.LiteralStringID &&
            			rNode.getNodeID() != ASTNodeID.LiteralNullID)
            	{
		        	if (rightDef == null ||
		        			(!(rightDef.getQualifiedName().equals(IASLanguageConstants.String) ||
		        			  (rightDef.getQualifiedName().equals(IASLanguageConstants.ANY_TYPE)
		                    		&& rNode.getNodeID() == ASTNodeID.FunctionCallID &&
		                    		isToString(rNode)) ||
		        			  // if not an assignment we don't need to coerce numbers
		        			  (!isAssignment && rightIsNumber) ||
		        			   rightDef.getQualifiedName().equals(IASLanguageConstants.Null))))
		        	{
		        		JSFlexJSDocEmitter docEmitter = (JSFlexJSDocEmitter)(getEmitter().getDocEmitter());
		        		if (docEmitter.emitStringConversions)
		        		{
		        			coercion = "org.apache.flex.utils.Language.string(";
		        		}
		        	}
            	}
            }
            super_emitBinaryOperator(node, coercion);
            if (coercion.length() > 0)
            	write(")");
            	
            /*
            IExpressionNode leftSide = node.getLeftOperandNode();

            IExpressionNode property = null;
            int leftSideChildCount = leftSide.getChildCount();
            if (leftSideChildCount > 0)
            {
                IASNode childNode = leftSide.getChild(leftSideChildCount - 1);
                if (childNode instanceof IExpressionNode)
                    property = (IExpressionNode) childNode;
                else
                    property = leftSide;
            }
            else
                property = leftSide;

            IDefinition def = null;
            if (property instanceof IIdentifierNode)
                def = ((IIdentifierNode) property).resolve(getWalker()
                        .getProject());

            boolean isSuper = false;
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode cnode = leftSide.getChild(0);
                ASTNodeID cId = cnode.getNodeID();

                isSuper = cId == ASTNodeID.SuperID;
            }

            String op = node.getOperator().getOperatorText();
            boolean isAssignment = op.contains("=") && !op.contains("==") && 
                                                    !(op.startsWith("<") || 
                                                            op.startsWith(">") || 
                                                            op.startsWith("!"));

            if (def instanceof AccessorDefinition && isAssignment)
            {
                // this will make the set_foo call
                getWalker().walk(leftSide);
            }
            else if (isSuper) 
            {
                emitSuperCall(node, "");
            }
            else
            {
                if (ASNodeUtils.hasParenOpen(node))
                    write(ASEmitterTokens.PAREN_OPEN);

                getWalker().walk(leftSide);

                if (node.getNodeID() != ASTNodeID.Op_CommaID)
                    write(ASEmitterTokens.SPACE);

                writeToken(node.getOperator().getOperatorText());

                getWalker().walk(node.getRightOperandNode());

                if (ASNodeUtils.hasParenClose(node))
                    write(ASEmitterTokens.PAREN_CLOSE);
            }
            */
        }
    }
    
    private boolean isToString(IASNode rNode)
    {
    	IExpressionNode fnNameNode = ((FunctionCallNode)rNode).getNameNode();
    	if (fnNameNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    	{
    		MemberAccessExpressionNode mae = (MemberAccessExpressionNode)fnNameNode;
    		IExpressionNode rightNode = mae.getRightOperandNode();
    		return rightNode.getNodeID() == ASTNodeID.IdentifierID && 
    				((IdentifierNode)rightNode).getName().equals("toString");
    	}
    	return false;
    }

    private void super_emitBinaryOperator(IBinaryOperatorNode node, String coercion)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);

        ASTNodeID id = node.getNodeID();

        if (id == ASTNodeID.Op_IsID)
        {
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else if (id == ASTNodeID.Op_AsID)
        {
            // (is(a, b) ? a : null)
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            writeToken(ASEmitterTokens.PAREN_CLOSE);
            writeToken(ASEmitterTokens.TERNARY);
            getWalker().walk(node.getLeftOperandNode());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.NULL);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else
        {
            getWalker().walk(node.getLeftOperandNode());

            startMapping(node, node.getLeftOperandNode());
            
            if (id != ASTNodeID.Op_CommaID)
                write(ASEmitterTokens.SPACE);

            // (erikdebruin) rewrite 'a &&= b' to 'a = a && b'
            if (id == ASTNodeID.Op_LogicalAndAssignID
                    || id == ASTNodeID.Op_LogicalOrAssignID)
            {
                IIdentifierNode lnode = (IIdentifierNode) node
                        .getLeftOperandNode();

                writeToken(ASEmitterTokens.EQUAL);
                endMapping(node);

                startMapping(node);
                write(lnode.getName());
                endMapping(node);

                startMapping(node, node.getLeftOperandNode());
                write(ASEmitterTokens.SPACE);
                write((id == ASTNodeID.Op_LogicalAndAssignID) ? ASEmitterTokens.LOGICAL_AND
                        : ASEmitterTokens.LOGICAL_OR);
            }
            else
            {
                write(node.getOperator().getOperatorText());
            }

            write(ASEmitterTokens.SPACE);
            endMapping(node);

            write(coercion);
            /*
            IDefinition definition = node.getRightOperandNode().resolve(getProject());
        	if (definition instanceof FunctionDefinition &&
        			(!(definition instanceof AccessorDefinition)))
        	{
        	}
        	else */
        		getWalker().walk(node.getRightOperandNode());
                if (node.getNodeID() == ASTNodeID.Op_InID &&
                        ((JSFlexJSEmitter)getEmitter()).isXML(node.getRightOperandNode()))
                {
                	write(".elementNames()");
                }   
                else if (node.getNodeID() == ASTNodeID.Op_InID &&
                        ((JSFlexJSEmitter)getEmitter()).isProxy(node.getRightOperandNode()))
                {
                	write(".propertyNames()");
                }
        }

        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    private enum DateProperties
    {
    	FULLYEAR("fullYear", "setFullYear"),
    	MONTH("month", "setMonth"),
    	DATE("date", "setDate"),
    	FULLYEARUTC("fullYearUTC", "setUTCFullYear"),
    	MONTHUTC("monthUTC", "setUTCMonth"),
    	DATEUTC("dateUTC", "setUTCDate"),
    	HOURS("hours", "setHours"),
    	MINUTES("minutes", "setMinutes"),
    	SECONDS("seconds", "setSeconds"),
    	MILLISECONDS("milliseconds", "setMilliseconds"),
    	HOURSUTC("hoursUTC", "setUTCHours"),
    	MINUTESUTC("minutesUTC", "setUTCMinutes"),
    	SECONDSUTC("secondsUTC", "setUTCSeconds"),
    	MILLISECONDSUTC("millisecondsUTC", "setUTCMilliseconds");
    	
    	DateProperties(String value, String functionName)
    	{
    		this.value = value;
    		this.functionName = functionName;
    	}
    	
    	private String value;
    	private String functionName;
    	
    	public String getFunctionName()
    	{
    		return functionName;
    	}
    }
    
    void specialCaseDate(IBinaryOperatorNode node, MemberAccessExpressionNode leftSide)
    {
    	MemberAccessExpressionNode dateNode = (MemberAccessExpressionNode)leftSide;
        IIdentifierNode rightSide = (IIdentifierNode)dateNode.getRightOperandNode();
        getWalker().walk(dateNode.getLeftOperandNode());
        String rightName = rightSide.getName();
        DateProperties prop = DateProperties.valueOf(rightName.toUpperCase());
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(prop.getFunctionName());
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getRightOperandNode());
        switch (prop)
        {
        case FULLYEAR:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getMonth()");
            // fall through
        case MONTH:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getDate()");
            break;
        case FULLYEARUTC:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getMonthUTC()");
            // fall through
        case MONTHUTC:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getDateUTC()");
            break;
        case HOURS:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getMinutes()");
            // fall through
        case MINUTES:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getSeconds()");
            // fall through
        case SECONDS:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getMilliseconds()");
            break;
        case HOURSUTC:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getUTCMinutes()");
            // fall through
        case MINUTESUTC:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getUTCSeconds()");
            // fall through
        case SECONDSUTC:
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
            getWalker().walk(dateNode.getLeftOperandNode());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("getUTCMilliseconds()");
            break;
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }
}
