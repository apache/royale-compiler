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
package org.apache.royale.utils;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.ArrayLikeConfigurationErrorProblem;
import org.apache.royale.compiler.problems.ArrayLikeUsageErrorProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.tree.as.*;

import java.util.*;

/**
 * Support for processing a variety of characteristics via metadata on classes or interfaces
 * that translate Array like access and iteration from as3 language level to implementation support for
 * the annotated definition(s)
 * example:
 * [RoyaleArrayLike(getValue="getItemAt",setValue="setItemAt",length="length",lengthAccess="getter")]
 *
 * This class contains utilities to support both mutating AST of 'for each' loops with target (rightOperand) types that
 * are classes or interfaces which have [RoyaleArrayLike] metadata. It also supports mutation of array-like
 * dynamic access or assignment when the dynamic access keys are numeric types (int, uint, Number).
 * Subclasses of RoyaleArrayLike classes or implementers of interfaces which are RoyaleArrayLike
 * 'inherit' the RoyaleArrayLike special treatment by the compiler.
 */
public class ArrayLikeUtil
{
    
    //the metadata arg that specifies the member name for accessing length value of and instance of the decorated type
    final private static String LENGTH_ARG = "length";
    //the metadata arg that specifies the type of access that the length arg specifies ('getter' or 'method', default value is 'getter', see defaults below)
    final private static String LENGTH_ACCESS_ARG = "lengthAccess";
    //the metadata arg that specifies how numeric index level 'get' is performed
    final private static String GETVALUE_ARG = "getValue";
    //the metadata arg that specifies how numeric index level 'set' is performed
    final private static String SETVALUE_ARG = "setValue";
    //the metadata arg that specifies the order of arguments used in the setter ('value,index' or 'index,value', default value is 'value,index')
    final private static String SETTER_ARG_SEQUENCE_ARG = "setterArgSequence";
    
    
    //the valid SETTER_ARG_SEQUENCE_ARG value options.
    //Whether the 'setter' has the index arg first or the value to be set at the specified index (or vice versa)
    //checking the method signature in the class to determine this is not appropriate, because in the general case
    //there may be classes whose values are also typed as numeric, so it should be specified. (default is value,index, see defaults below)
    final private static String SETTER_ARG_SEQUENCE_VALUE_INDEX = "value,index";
    final private static String SETTER_ARG_SEQUENCE_INDEX_VALUE = "index,value";
    
    //the valid options for length access. Used when generated iteration support in for-each loops
    final private static String LENGTH_ACCESS_METHOD = "method";
    final private static String LENGTH_ACCESS_GETTER = "getter";
    
    //a value for either getValue or setValue that indicates regular array access should be used,
    //This results in no alteration compared to original source code for either the 'getter' or 'setter'
    // (or both, if both 'getValue' and 'setValue' have this value)
    final private static String INDEX_ACCESS_UNCHANGED = "[]";
    
    //default values for certain args that can be omitted.
    final private static String LENGTH_ACCESS_DEFAULT = LENGTH_ACCESS_GETTER;
    final private static String SETTER_ARG_SEQUENCE_DEFAULT = SETTER_ARG_SEQUENCE_VALUE_INDEX;
    
    //this is currently 'assumed safe', based on the chance of naming collision being extremely unlikely
    //with an actual user-coded variable name. It also assumes that the target platform will optimize its own local variable names,
    //therefore there is no attempt to make a 'short' name.
    final private static String ARRAY_LIKE_FOREACH_ITERATOR_VARNAME_BASE = "royale$for$Each$Iterator";
    
    /**
     * todo Consider the possibility of specific iterator factories instead of one generic function for all... the qName for the specific function could be specified in the Metadata def.
     * These could simply take the instance as an argument and already know the way to construct the 'iterator' because they are specific
     * (e.g. collections could have a specific iterator function, XMLish its own, BinaryData its own etc).
     */
    private final static String ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC_QNAME = "org.apache.royale.language.iterator.arrayLike";
    private final static String ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC = "arrayLike";
    
    final static String ARRAYLIKE_HAS_NEXT = "hasNext";
    final static String ARRAYLIKE_GET_NEXT = "next";
    
    private static String wrapQuotes(String original){
        return "'" + original + "'";
    }
    
    /**
     * pre-processes dynamic access nodes, replacing them where applicable with get or set value calls
     * on the arrayLike instance
     */
    public static void preProcessGetterSetters(ICompilerProject project, ContainerNode node, NodeBase lower ) {
        if (!(project instanceof IRoyaleProject)) {
            return;
        }
        NodeBase parent = lower != null ? lower : node;
        int childCount = parent.getChildCount();
        for (int i=0; i < childCount; i++) {
            IASNode child = parent.getChild(i);
            //future: consider ways to consolidate/abstract these various checks
            if (child instanceof BinaryOperatorAssignmentNode) {
                if (((BinaryOperatorAssignmentNode) child).getLeftOperandNode() instanceof DynamicAccessNode) {
                    DynamicAccessNode dynNode = (DynamicAccessNode) ((BinaryOperatorAssignmentNode) child).getLeftOperandNode();
                    if (isArrayLikeCandidate(dynNode, project)) {
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String setterArg;
                        setterArg = getSetterArg(arrayLikeTag);
                        if (!setterArg.equals(INDEX_ACCESS_UNCHANGED)) { //otherwise we leave it as is
                            //get the FunctionCallNode replacement's arguments from the original assignment
                            ExpressionNodeBase indexArg = (ExpressionNodeBase) dynNode.getRightOperandNode();
                            ExpressionNodeBase valueArg = (ExpressionNodeBase) ((BinaryOperatorAssignmentNode) child).getRightOperandNode();
                            
                            FunctionCallNode replacement = createDynamicAccessMutation(dynNode, setterArg);
                            
                            String argSequence = getSetterArgSequenceArg(arrayLikeTag);
                            if (argSequence.equals(SETTER_ARG_SEQUENCE_VALUE_INDEX)) {
                                replacement.getArgumentsNode().addChild(valueArg);
                                replacement.getArgumentsNode().addChild(indexArg);
                            } else { //index,value instead
                                replacement.getArgumentsNode().addChild(indexArg);
                                replacement.getArgumentsNode().addChild(valueArg);
                            }
                            replacement.getArgumentsNode().setParent(replacement);
                            if (parent instanceof ContainerNode) {
                                ((ContainerNode) parent).removeItem((NodeBase) child);
                                ((ContainerNode) parent).addChild(replacement, i);
                                ((BinaryOperatorAssignmentNode) child).setParent(null);
                                child = replacement; //we need to allow for recursive checking of the assigned value as well as some other 'getter'
                            } /*else {
                                System.out.println("Problem: " + (node.getNodeID() + ":" + node.getLine()));
                            }*/
                        }
                    }
                }
            } else if (child instanceof DynamicAccessNode) {
                DynamicAccessNode dynNode = (DynamicAccessNode)child;
                if (isArrayLikeCandidate(dynNode, project)) {
                    if (parent instanceof BaseStatementExpressionNode) {
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String getterArg = getGetterArg(arrayLikeTag);
                        if (!getterArg.equals(INDEX_ACCESS_UNCHANGED)) {
                            ExpressionNodeBase indexArg = (ExpressionNodeBase) dynNode.getRightOperandNode();
                            FunctionCallNode replacement = createDynamicAccessMutation(dynNode, getterArg);
                            replacement.getArgumentsNode().addChild(indexArg);
                            replacement.getArgumentsNode().setParent(replacement);
                            ((BaseStatementExpressionNode) parent).setStatementExpression(replacement);
                            replacement.setParent( parent);
                            dynNode.setParent(null);
                            child = replacement;
                        }
                    }
                    else if (parent instanceof ContainerNode) {
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String getterArg = getGetterArg(arrayLikeTag);
                        if (!getterArg.equals(INDEX_ACCESS_UNCHANGED)) { //otherwise we leave it as is
                            ExpressionNodeBase indexArg = (ExpressionNodeBase) dynNode.getRightOperandNode();
                            FunctionCallNode replacement = createDynamicAccessMutation(dynNode, getterArg);
                            replacement.getArgumentsNode().addChild(indexArg);
                            replacement.getArgumentsNode().setParent(replacement);
                            ((ContainerNode) parent).removeItem((NodeBase) child);
                            ((ContainerNode) parent).addChild(replacement, i);
                            dynNode.setParent(null);
                            child = replacement;
                        }
                    } else if (parent instanceof BinaryOperatorNodeBase) {
                        BinaryOperatorNodeBase binaryOp = (BinaryOperatorNodeBase) parent;
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String getterArg = getGetterArg(arrayLikeTag);
                        if (!getterArg.equals(INDEX_ACCESS_UNCHANGED)) { //otherwise we leave it as is
                            ExpressionNodeBase indexArg = (ExpressionNodeBase) dynNode.getRightOperandNode();
                            FunctionCallNode replacement = createDynamicAccessMutation(dynNode, getterArg);
                            replacement.getArgumentsNode().addChild(indexArg);
                            replacement.getArgumentsNode().setParent(replacement);
                            if (binaryOp.getLeftOperandNode() == child) {
                                binaryOp.setLeftOperandNode(replacement);
                            } else {
                                binaryOp.setRightOperandNode(replacement);
                            }
                            replacement.setParent(binaryOp);
                            dynNode.setParent(null);
                            child = replacement;
                        }
                    } else if (parent instanceof VariableNode) {
                        VariableNode variableNode = (VariableNode) parent;
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String getterArg = getGetterArg(arrayLikeTag);
                        if (!getterArg.equals(INDEX_ACCESS_UNCHANGED)) { //otherwise we leave it as is
                            ExpressionNodeBase indexArg = (ExpressionNodeBase) dynNode.getRightOperandNode();
                            FunctionCallNode replacement = createDynamicAccessMutation(dynNode, getterArg);
                            replacement.getArgumentsNode().addChild(indexArg);
                            replacement.getArgumentsNode().setParent(replacement);
                            variableNode.setAssignedValue(null, replacement);
                            replacement.setParent(variableNode);
                            dynNode.setParent(null);
                            child = replacement;
                        }
                    } else if (parent instanceof UnaryOperatorNodeBase) {
                        IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
                        IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(target, project);
                        IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
                        String getterArg = getGetterArg(arrayLikeTag);
                        String setterArg = getSetterArg(arrayLikeTag);
                        if (!(INDEX_ACCESS_UNCHANGED.equals(getterArg) && INDEX_ACCESS_UNCHANGED.equals(setterArg))) {
                            //report an error - unless we have native get/set bracket [] access (where we can assume it should work), we can't easily migrate unary operators
                            UnaryOperatorNodeBase unaryOperatorNodeBase = (UnaryOperatorNodeBase) parent;
                            ArrayLikeUsageErrorProblem usageError;
                            SourceLocation loc = new SourceLocation();
                            loc.setSourcePath(unaryOperatorNodeBase.getSourcePath());
                            if (unaryOperatorNodeBase.getOperandNode().getAbsoluteStart() > unaryOperatorNodeBase.getOperatorAbsoluteStart()) {
                                //prepended unary operator
                                loc.setColumn(unaryOperatorNodeBase.getColumn());
                                loc.setLine(unaryOperatorNodeBase.getLine());
                                loc.setEndColumn(unaryOperatorNodeBase.getOperatorAbsoluteStart() - unaryOperatorNodeBase.getOperandNode().getAbsoluteStart());
                                loc.setEndLine(unaryOperatorNodeBase.getLine());
                                loc.setStart(unaryOperatorNodeBase.getStart());
                                loc.setEnd(unaryOperatorNodeBase.getOperatorAbsoluteStart() - 1);
                            } else {
                                //post-pended unary operator
                                loc.setColumn(unaryOperatorNodeBase.getOperandNode().getEndColumn());
                                loc.setLine(unaryOperatorNodeBase.getOperandNode().getEndLine());
                                loc.setEndColumn(unaryOperatorNodeBase.getEndColumn());
                                loc.setEndLine(unaryOperatorNodeBase.getEndLine());
                                loc.setStart(unaryOperatorNodeBase.getOperandNode().getAbsoluteStart());
                                loc.setStart(unaryOperatorNodeBase.getAbsoluteEnd());
                            }
                            usageError = new ArrayLikeUsageErrorProblem(loc,
                                    "Unary Operation not supported");

                            project.getProblems().add(usageError);
                        }
                    } /*else {
                        System.out.println("Skipped: " + (child.getNodeID()));
                    }*/
                
                }
            }/* else {
                System.out.println("Skipped: " + (child.getNodeID()));
            }*/
    
            if (child instanceof FixedChildrenNode || child instanceof TreeNode) {
                preProcessGetterSetters(/*searchScope,*/ project, node, (NodeBase) child);
            }
        }
    }
    
    private static boolean isArrayLikeCandidate(DynamicAccessNode dynNode, ICompilerProject project) {
        if (!(project instanceof IRoyaleProject)) {
            return false;
        }
        boolean isCandidate = false;
        //first check to see if the access is numeric... if it is not then we consider that it is not a candidate
        IDefinition dynType = dynNode.getRightOperandNode().resolveType(project);
        if (project.getBuiltinType(IASLanguageConstants.BuiltinType.NUMBER).equals(dynType)
                || project.getBuiltinType(IASLanguageConstants.BuiltinType.UINT).equals(dynType)
                || project.getBuiltinType(IASLanguageConstants.BuiltinType.INT).equals(dynType)
        ) {
            IDefinition target = dynNode.getLeftOperandNode().resolveType(project);
            isCandidate = ArrayLikeUtil.isArrayLike(target, project);
        }
        return isCandidate;
    }
    
    private static FunctionCallNode createDynamicAccessMutation(DynamicAccessNode original, String methodName){
        ExpressionNodeBase base = (ExpressionNodeBase) original.getLeftOperandNode();
        IdentifierNode methodCallName = new IdentifierNode(methodName);
        MemberAccessExpressionNode nameNode;
        nameNode = new MemberAccessExpressionNode(base, null, methodCallName);
        base.setParent(nameNode);
        methodCallName.setParent(nameNode);
        FunctionCallNode replacement = new FunctionCallNode(nameNode);
        nameNode.setParent(replacement);
        //the scaffolding for the replacement is set up... adding the arguments will be managed by the call site
        return replacement;
    }
    
    /**
     * main support for loop mutations to support RoyaleArrayLikes as targets of 'for each' loops
     * @param searchScope
     * @param project
     */
    public static void preProcessLoopChecks(ASScope searchScope, IRoyaleProject project) {
        ScopedBlockNode funcScopeNode = (ScopedBlockNode) searchScope.getScopeNode();
        
        IForLoopNode[] forLoops = searchScope.getLoopChecks(true);
        List<IForLoopNode> forLoopList = Arrays.asList(forLoops);
        boolean importAdded = false;
        ArrayList<String> usedIterators = new ArrayList<String>();
        for (IForLoopNode loopNode : forLoopList) {
            int depth = 0;
            IASNode nodeCheck = loopNode;
            while (nodeCheck.getParent() != null && nodeCheck.getParent() != funcScopeNode) {
                if (nodeCheck.getParent() instanceof IForLoopNode && forLoopList.indexOf(nodeCheck.getParent()) != -1) {
                    depth++;
                }
                nodeCheck = nodeCheck.getParent();
            }
            //are we dealing with a regular for..in or a for each..in loop:
            boolean isForeach = loopNode.getKind() == IForLoopNode.ForLoopKind.FOR_EACH;
            //create a valid name for the iterator at the current depth of for-each loops, re-using previous declared variables, where possible
            String arrIter = ARRAY_LIKE_FOREACH_ITERATOR_VARNAME_BASE + depth;
            IDefinition targetType;
    
            final IDefinition xmlListDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.XMLLIST);
            try {
                //check the rightOperandType
                BinaryOperatorInNode conditionalExpressions = (BinaryOperatorInNode) loopNode.getConditionalExpressionNodes()[0];
                if (conditionalExpressions.getRightOperandNode() instanceof IFunctionCallNode) {
                    IASNode nameNode = ((FunctionCallNode) conditionalExpressions.getRightOperandNode()).getNameNode();
                    if (nameNode instanceof IMemberAccessExpressionNode) {
                        IFunctionDefinition funcDef = (IFunctionDefinition) ((IMemberAccessExpressionNode) nameNode).getRightOperandNode().resolve(project);
                        targetType = funcDef.resolveReturnType(project);
                    } else {
                        //back to error until we cover all cases
                        targetType = conditionalExpressions.getRightOperandNode().resolveType(project);
                    }
                } else {
                    if (conditionalExpressions.getRightOperandNode() instanceof IMemberAccessExpressionNode) {
                        //this does not resolve... check for XMLish on the left
                        targetType = conditionalExpressions.getRightOperandNode().resolveType(project);
                        IExpressionNode left = ((IMemberAccessExpressionNode) conditionalExpressions.getRightOperandNode()).getLeftOperandNode();
                        while (targetType == null && left != null) {
                            targetType = left.resolveType(project);
                            if (targetType == null) {
                                if (left instanceof IMemberAccessExpressionNode || left instanceof IDynamicAccessNode) {
                                    left = ((IBinaryOperatorNode) left).getLeftOperandNode();
                                } else {
                                    left = null;
                                }
                            } else {
                                if (SemanticUtils.isXMLish(targetType, project)) {
                                    //assume we have an XMLList
                                    targetType = xmlListDef;
                                }
                                if (targetType != xmlListDef) {
                                    //this does not make sense, so ignore it or add a problem?
                                    targetType = null;
                                    left = null;
    
                                } /*else {
                                    System.out.println("Searching, found left target type to "+targetType.getQualifiedName());
                                }*/
    
                            }
                        }
    
                    } else {
                        targetType = conditionalExpressions.getRightOperandNode().resolveType(project);
                    }
    
                }
    
            } catch (Exception e) {
               /* System.out.println("Failed to resolve the target type ");
                System.out.println(e.getStackTrace());*/
                continue;
            }
    
            if (ArrayLikeUtil.isArrayLike(targetType, project)) {
                //    System.out.println("processing ArrayLike "+targetType.getQualifiedName());
        
                if (!importAdded) {
                    if (searchScope.getImports() != null) {
                        List<String> imports = Arrays.asList(searchScope.getImports());
                        if (imports.contains(ArrayLikeUtil.ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC_QNAME)) {
                            importAdded = true;
                        }
                    }
                    if (!importAdded) {
                        searchScope.addImport(ArrayLikeUtil.ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC_QNAME);
                        importAdded = true;
                    }
                }
        
        
                ASProjectScope projectScope = (ASProjectScope) project.getScope();
                boolean alreadyUsed = usedIterators.contains(arrIter); //otherwise we need a typed var declaration
                if (!alreadyUsed)
                    usedIterators.add(arrIter); // we can use it again without var declaration in the current scope
        
                IDefinition metaSource = ArrayLikeUtil.resolveArrayLikeDefinitionSource(targetType, project);
                IMetaTag arrayLikeTag = ArrayLikeUtil.getArrayLikeMetaData(metaSource);
        
                //change the loop node to: for(arrIter:Object = arrayLike(instance,"{lengthCheck}","{getterCheck}" or (null if getterCheck=="[]"), Boolean(lengthAccess=="method"), Boolean(!isForEach)); arrIter.hasNext();)
                // {
                // {!alreadyUsed: var} originalName{!alreadyUsed: :OriginalType} =  arrIter.getNext();
                // {originalBody Loop body}
                // }

                boolean useDynamicAccess = !project.isStaticTypedTarget();
                ForLoopNode.ILoopMutation mutation = ArrayLikeUtil.createArrayLikeLoopMutation((ForLoopNode) loopNode, arrIter, useDynamicAccess);
        
                String generatorFuncName = ArrayLikeUtil.ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC;
                IdentifierNode funcName = new IdentifierNode(generatorFuncName);
                FunctionCallNode specificIteratorFunc = new FunctionCallNode(funcName);
                specificIteratorFunc.getNameNode().setParent(specificIteratorFunc);
        
                //now add the arguments
                //use the original iteration target node {from: for each(iteratee in target) }, reparent it to the generator function args as the target 'instance'
                specificIteratorFunc.getArgumentsNode().addItem((NodeBase) mutation.getIterationTarget());
                specificIteratorFunc.getArgumentsNode().setParent(specificIteratorFunc);
                //now add the metadata driven args
                //length check:
                LiteralNode metaArg = new LiteralNode(ILiteralNode.LiteralType.STRING, wrapQuotes(ArrayLikeUtil.getLengthArg(arrayLikeTag)));
                metaArg.setSynthetic(true); //may not be needed
                specificIteratorFunc.getArgumentsNode().addItem(metaArg);
                //index getter:
                String getter = ArrayLikeUtil.getGetterArg(arrayLikeTag);
                if (getter.equals(INDEX_ACCESS_UNCHANGED)) {
                    // pass null for the getter
                    metaArg = new LiteralNode(ILiteralNode.LiteralType.NULL, "null");
                    metaArg.setSynthetic(true); //may not be needed
                } else {
                    metaArg = new LiteralNode(ILiteralNode.LiteralType.STRING, wrapQuotes(ArrayLikeUtil.getGetterArg(arrayLikeTag)));
                    metaArg.setSynthetic(true); //may not be needed
                }
                specificIteratorFunc.getArgumentsNode().addItem(metaArg);
                //boolean indicating true if the length check is a method call, false if reguler 'getter' style:
                metaArg = new LiteralNode(ILiteralNode.LiteralType.BOOLEAN, ArrayLikeUtil.getLengthAccessArg(arrayLikeTag).equals("method") ? "true" : "false");
                metaArg.setSynthetic(true); //may not be needed
                specificIteratorFunc.getArgumentsNode().addItem(metaArg);
                
                //distinguish whether this iterator is values (for each(x in y)) or keys (regular for (x in y))
                if (!isForeach) {
                    //we add an extra boolean true indicating that we only want to iterate over keys (and not values)
                    metaArg = new LiteralNode(ILiteralNode.LiteralType.BOOLEAN, "true");
                    metaArg.setSynthetic(true); //may not be needed
                    specificIteratorFunc.getArgumentsNode().addItem(metaArg);
                }
        
                mutation.prepareConditionals(!alreadyUsed, specificIteratorFunc);
                mutation.prepareContent();
        
                Collection<ICompilerProblem> problems = ((ForLoopNode) loopNode).processMutation(mutation, searchScope);
                if (problems.size() > 0) {
                    project.getProblems().addAll(problems);
                }
            }
    
            //@todo if the targetType is null we could output an opt-in or opt-out (via config) warning suggesting that Strongly typed for each loop targets are more reliable
            //That would help people a lot when porting things to dynamic targets like js. for each(var something:Object in event.target.someField).... is sometimes a problem

            
            /*if (targetType == null){
                System.out.println("FOUND NULL FOREACH TARGET TYPE AT "+loopNode.getLine()+","+loopNode.getSourcePath());
            } else {
                System.out.println("Target type was "+targetType.getQualifiedName() + " at " + loopNode.getLine() +"," +loopNode.getSourcePath());
            }*/
        }
    
    }

    private static int currentProject = -1;
    private static HashMap<String, String> arrayLikeLookups = null;

    private static void resetLookupsIfNeeded(ICompilerProject project) {
        if (currentProject == project.hashCode()) return;
        arrayLikeLookups = new HashMap<String, String>(20);
        currentProject = project.hashCode();
    }

    /**
     *
     * @param definition the definition to check
     * @return true if this specific definition has the ArrayLike annotation
     */
    public static boolean definitionIsArrayLike(IDefinition definition) {
        return ((definition instanceof IClassDefinition || definition instanceof IInterfaceDefinition) &&
                definition.hasMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE));
    }
    
    /**
     * Method to check an annotated definition in the current project, and verify that it is valid
     * This method poplulates the external problems collection argument with the first observed problem
     * @param definition the definition to verify
     * @param project the current project
     * @param problems a problem list to populate with any detected issues
     * @return boolean true if there were no issues, otherwise false
     */
    public static boolean validateArrayLikeDefinition(IDefinition definition, ICompilerProject project, List<ICompilerProblem> problems) {
        IMetaTag arrayLikeTag  = definition.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE);
        if (!(project instanceof IRoyaleProject)) {
            if (problems != null) {
                problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "This is not a Royale Project. Only Royale projects support "+IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE));
            }
            return false;
        }

        //mandatory
        String lengthCheck = arrayLikeTag.getAttributeValue(LENGTH_ARG);
        boolean pass = true;
        if (lengthCheck == null)  {
            pass = false;
            if (problems != null) {
                problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "Missing '"+LENGTH_ARG+"' metadata argument"));
            }
        } //verify further after we check the lengthAccess arg
        if (pass) {
            String getterCheck = arrayLikeTag.getAttributeValue(GETVALUE_ARG);
            if (getterCheck == null)  {
                pass = false;
                if (problems != null) {
                    problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "Missing '"+GETVALUE_ARG+"' metadata argument"));
                }
            } else {
                //check the definition
                if (getterCheck != INDEX_ACCESS_UNCHANGED) { //'[]' is a special case where regular Array access is not changed
                    //@todo extra safety: check the definition has the method that is specified in the metadata, add a problem if not.
                }
            }
        }
        String setterCheck = null;
        if (pass) {
            setterCheck = arrayLikeTag.getAttributeValue(SETVALUE_ARG);
            if (setterCheck == null)  {
                pass = false;
                if (problems != null) {
                    problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "Missing '"+SETVALUE_ARG+"' metadata argument"));
                }
            } else {
                //check the definition
                if (setterCheck != INDEX_ACCESS_UNCHANGED) {//'[]' is a special case where regular Array access is not changed
                    //@todo extra safety: check the definition has the method that is specified in the metadata, add a problem if not.
                }
            }
        }
        if (pass) {
            if (!setterCheck.equals(INDEX_ACCESS_UNCHANGED)) {//otherwise we can ignore the setter arg sequence, even if it is (incorrectly) specified.
                String setterSequenceCheck = arrayLikeTag.getAttributeValue(SETTER_ARG_SEQUENCE_ARG);
                if (setterSequenceCheck == null) {
                    setterSequenceCheck = SETTER_ARG_SEQUENCE_DEFAULT;
                }
                if (!setterSequenceCheck.equals(SETTER_ARG_SEQUENCE_VALUE_INDEX) && !setterSequenceCheck.equals(SETTER_ARG_SEQUENCE_INDEX_VALUE))  {
                    pass = false;
                    if (problems != null) {
                        problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "Missing '"+SETVALUE_ARG+"' metadata argument"));
                    }
                } else {
                    //check the definition
                    //@todo extra safety: check the definition has the method signature that is specified in the metadata, add a problem if not.
                    //the index argument should be a numeric type, and the specified method should have the two arguments
                }
            }
        }
        if (pass) {
            //can be method or getter, defaults to getter
            String lengthAccess = arrayLikeTag.getAttributeValue(LENGTH_ACCESS_ARG);
            if (lengthAccess == null) lengthAccess = LENGTH_ACCESS_DEFAULT;
            //only "getter" or "method" are allowed options
            if (!lengthAccess.equals(LENGTH_ACCESS_GETTER) && !lengthAccess.equals(LENGTH_ACCESS_METHOD)) {
                pass = false;
                if (problems != null) {
                    //problems.add(new ArrayLikeConfigurationErrorProblem(definition.getNode(), "Metadata argument for 'lengthAccess' missing or invalid"));
                    problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "Metadata argument for '"+LENGTH_ACCESS_ARG+"' missing or invalid"));
                }
            } else {
                //@todo extra safety: check the definition has the length getter or method that is specified that is specified in the metadata, add a problem if not.
            }
        }
        if (pass) {
            ASProjectScope projectScope = ((ASProjectScope)project.getScope());
            IDefinition def = projectScope.findDefinitionByName(ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC_QNAME);
            if (!(def instanceof FunctionDefinition)) {
                //@todo extra safety: this may not be sufficient to verify a concrete reference
                problems.add(new ArrayLikeConfigurationErrorProblem(arrayLikeTag, "The RoyaleArrayLike Tag is valid, but there is a missing concrete reference in the project to: "+ARRAYLIKE_GENERIC_SUPPORT_ITERATOR_FACTORY_FUNC_QNAME+ " (which is required)"));
                pass = false;
            }
        }
    
        return pass;
    }
    
    /**
     * Checks whether an instance is ArrayLike at a usage site.
     * This is typically either array access or assignment, or for the target of a 'for each' loop
     * @param definition the definition check to verify if an instance is ArrayLike at a usage site
     * @return true if this definition either has its own ArrayLike annotation, or inherits via its ancestors or interfaces
     */
    synchronized public static boolean isArrayLike(IDefinition definition, ICompilerProject project) {
        if (definition != null && project instanceof IRoyaleProject) {
            resetLookupsIfNeeded(project);
            if (definition instanceof IClassDefinition) {
                String qName = definition.getQualifiedName();
                if (arrayLikeLookups.containsKey(qName)) return arrayLikeLookups.get(qName) != null;
                return checkClass((IClassDefinition) definition, (IRoyaleProject) project);
            } else if (definition instanceof IInterfaceDefinition) {
                String qName = definition.getQualifiedName();
                if (arrayLikeLookups.containsKey(qName)) return arrayLikeLookups.get(qName) != null;
                return checkInterface((IInterfaceDefinition) definition, (IRoyaleProject) project);
            }
        }
        return false;
    }
    
    private static boolean checkClass(IClassDefinition definition, IRoyaleProject project){
        String qName = definition.getQualifiedName();
        resetLookupsIfNeeded(project);
        if (arrayLikeLookups.containsKey(qName)) return arrayLikeLookups.get(qName) != null;
        boolean isArrayLike = false;
        //check the class
        isArrayLike = definition.hasMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE);
        String originalQName = qName;
        if (!isArrayLike) {
            //check this definition's interfaces and their ancestry
            Set<IInterfaceDefinition> interfaces = definition.resolveAllInterfaces(project);
            for (IInterfaceDefinition interfaceDef:interfaces) {
                isArrayLike = checkInterface(interfaceDef, project);
                if (isArrayLike) {
                    qName = arrayLikeLookups.get(interfaceDef.getQualifiedName());
                    break;
                }
            }
            
            //check this definition's parent class (recursive check)
            if (!isArrayLike) {
                IClassDefinition baseClassDef = definition.resolveBaseClass(project);
                if (baseClassDef != null) {
                    isArrayLike = checkClass(baseClassDef, project);
                    if (isArrayLike) {
                        //we need to share the same lookup that was used when checking the parent chain
                        qName = arrayLikeLookups.get(baseClassDef.getQualifiedName());
                    }
                }
            }
        }
        arrayLikeLookups.put(originalQName, isArrayLike ? qName : null);
        return isArrayLike;
    }
    
    private static boolean checkInterface(IInterfaceDefinition interfaceDefinition, IRoyaleProject project){
        String qName = interfaceDefinition.getQualifiedName();
        resetLookupsIfNeeded(project);
        if (arrayLikeLookups.containsKey(qName)) return arrayLikeLookups.get(qName) != null;
        
        boolean isArrayLike = interfaceDefinition.hasMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE);
        String originalQName = qName;
        if (!isArrayLike) qName = null;
        ArrayList<String> ancestry = null;
        if (!isArrayLike) {
            // if we find an ancestor in the interface extension chain, we can cache the lookups back to it as the source of ArrayLike data
            ancestry = new ArrayList<String>();
            Iterator<IInterfaceDefinition> interfaceIterator = interfaceDefinition.interfaceIterator(project, false);
            while (interfaceIterator.hasNext()) {
                interfaceDefinition = interfaceIterator.next();
                qName = interfaceDefinition.getQualifiedName();
                if (arrayLikeLookups.containsKey(qName) ){
                    //we don't have to keep going, we now already know the answer
                    qName = arrayLikeLookups.get(qName); //this will either be null or the correct mapping to the ArrayLike source definition qName
                   
                    break;
                }
                ancestry.add(qName);
                isArrayLike = interfaceDefinition.hasMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE);
                if (isArrayLike) break;
                qName = null;
            }
        }
        if (ancestry != null) {
            //process ancestry lookups
            for (String ancestorQName: ancestry) arrayLikeLookups.put(ancestorQName, qName);
        }
        
        arrayLikeLookups.put(originalQName, qName);
        return isArrayLike;
    }
    
    public static IDefinition resolveArrayLikeDefinitionSource(IDefinition sourceDefinition, ICompilerProject project){
        IDefinition metaSource = null;
        if (sourceDefinition != null){
            resetLookupsIfNeeded(project);
            String sourceDefinitionQName = sourceDefinition.getQualifiedName();
            if (arrayLikeLookups.containsKey(sourceDefinitionQName)) {
                String resolvedQName = arrayLikeLookups.get(sourceDefinitionQName);
                if (!sourceDefinitionQName.equals(resolvedQName)) {
                    metaSource = project.resolveQNameToDefinition(resolvedQName);
                } else {
                    metaSource = sourceDefinition;
                }
            }
        }
        return metaSource;
    }
    
    
    
    /**
     * Before calling the following methods, isArrayLike should be verified, and the source of the metadata should be used
     * via resolveArrayLikeDefinitionSource
     * @param definition
     * @return
     */
    public static IMetaTag getArrayLikeMetaData(IDefinition definition){
        return definition.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ARRAYLIKE);
    }
    
    public static String getLengthArg(IMetaTag arrayLikeTag) {
        return arrayLikeTag.getAttributeValue(LENGTH_ARG);
    }
    
    public static String getGetterArg(IMetaTag arrayLikeTag) {
        return arrayLikeTag.getAttributeValue(GETVALUE_ARG);
    }
    
    public static String getSetterArg(IMetaTag arrayLikeTag) {
        return arrayLikeTag.getAttributeValue(SETVALUE_ARG);
    }
    
    public static String getSetterArgSequenceArg(IMetaTag arrayLikeTag) {
        String val = arrayLikeTag.getAttributeValue(SETTER_ARG_SEQUENCE_ARG);
        if (val == null) val = SETTER_ARG_SEQUENCE_DEFAULT;
        return val;
    }
    
    public static String getLengthAccessArg(IMetaTag arrayLikeTag) {
        String val = arrayLikeTag.getAttributeValue(LENGTH_ACCESS_ARG);
        if (val == null) val = LENGTH_ACCESS_DEFAULT;
        return val;
    }
    
    
    public static ArrayLikeLoopMutation createArrayLikeLoopMutation(ForLoopNode loopNode, String arrIterName, boolean useDynamicAccess) {
        return new ArrayLikeLoopMutation(loopNode, arrIterName, useDynamicAccess);
    }

}


/**
 * Support for mutating a 'for each' loop with the following signature
 * for each(var iteratee:IterateeType in someInstance){ //swf has implicit cast here.
 *     {loop body}
 * }
 *
 * into the following style of loop:
 * for(var iteratorVarName:Object=arraylike(someInstance, { plus metadata driven data here}); iteratorVarName.hasNext(); ) {
 *     var iteratee:IterateeType = iteratorVarName.getNext(); //with implicit cast here
 *      {loop body}
 * }
 * someInstance is a class annotated with [RoyaleArrayLike] metadata that determines its eligibility for applying this change
 * and also the specific data to pass to the arrayLike function which serves as an 'iterator factory' for the above 'for each' support
 * An example would be [RoyaleArrayLike(getValue="getItemAt",setValue="setItemAt",setterArgSequence="item,index",length="length",lengthAccess="getter")]
 * (The setValue and setterArgSequence metatag params are not needed in terms of for-each support... they, along with the getValue arg are to support
 * transforming Array Access via [numericKey] Dynamic access to method call access for setting and getting values)
 * This only works when the compiler knows the type of 'someInstance', otherwise it cannot check to see if it has an ArrayLike metatag
 */
class ArrayLikeLoopMutation implements ForLoopNode.ILoopMutation {
    
    
    /**
     * Constructor.
     */
    public ArrayLikeLoopMutation(ForLoopNode target, String iteratorVarName, boolean useDynamicAccess)
    {
        super();
        this.target = target;
        this.iteratorVarName = iteratorVarName;
        this.useDynamicAccess = useDynamicAccess;
    }
    
    final private IForLoopNode.ForLoopKind kind = IForLoopNode.ForLoopKind.FOR;
    private boolean useDynamicAccess;
    private String iteratorVarName;
    private NodeBase iteratee;
    private FunctionCallNode iterateeValueNode;
    private ForLoopNode target;
    private ArrayList<NodeBase> analyzeRequests;
    private ContainerNode mutatedConditionals = new ContainerNode();
    
    public boolean isValid(){
        return mutatedConditionals !=null
                && mutatedConditionals.getChildCount() == 3
                && iteratee != null;
    }
    
    @Override
    public List<NodeBase> getAnalyzeRequests() {
        return analyzeRequests;
    }
    
    private FunctionCallNode createIteratorCall(String methodName){
        IdentifierNode leftOp = new IdentifierNode(this.iteratorVarName);
        ExpressionNodeBase nameNode;
        if (useDynamicAccess) {
            //this is the easiest way to keep things lightweight for JS without needing an actual iterator class or interface
            //use iterator['hasNext']()/iterator['getNext']() etc
            LiteralNode rightOp = new LiteralNode(ILiteralNode.LiteralType.STRING, "'"+methodName+"'");
            DynamicAccessNode dynNode = new DynamicAccessNode(leftOp);
            dynNode.setRightOperandNode(rightOp);
            rightOp.setParent(dynNode);
            leftOp.setParent(dynNode);
            nameNode = dynNode;
        } else {
            IdentifierNode rightOp = new IdentifierNode(methodName);
            nameNode = new MemberAccessExpressionNode(leftOp,null, rightOp);
            leftOp.setParent(nameNode);
            rightOp.setParent(nameNode);
        }
        FunctionCallNode functionCallNode = new FunctionCallNode(nameNode);
        nameNode.setParent(functionCallNode);
        functionCallNode.getArgumentsNode().setParent(functionCallNode);
        return functionCallNode;
    }
    
    private NodeBase setupIterator(boolean firstUse, FunctionCallNode assignedValue){
        
        IdentifierNode varName = new IdentifierNode(this.iteratorVarName);
        
        NodeBase setupNode;
        if (firstUse) {
            //create something like: var iteratorName:Object = {functionCallNode}
            IdentifierNode typeNode = new IdentifierNode(IASLanguageConstants.Object);
            VariableNode varNode =  new VariableNode(varName, typeNode);
            varNode.setKeyword(new ASToken(ASToken.TOKEN_KEYWORD_VAR, -1,-1,-1,-1,"var"));
            varNode.getKeywordNode().setParent(varNode);
            varNode.setAssignedValue(null, assignedValue);
            //explicitly set parent <--> child relationships
            varNode.addChild(varNode.getKeywordNode());
            varNode.addChild(varName);
            varNode.addChild(typeNode);
            varNode.addChild(assignedValue);
            setupNode = new VariableExpressionNode(varNode);
        } else {
            //recycledIteratorName = {functionCallNode}
            ASToken assignOp = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT,-1,-1,-1,-1,"=");
            setupNode = new BinaryOperatorAssignmentNode(assignOp, varName, assignedValue);
            //explicitly set parent <--> child relationships
            varName.setParent(setupNode);
            assignedValue.setParent(setupNode);
        }
        return setupNode;
    }
    
    private NodeBase setupIteratee(){
        //original part of conditionals group should be either a VariableExpressionNode or an IdentifierNode
        IExpressionNode original = ((BinaryOperatorInNode)(target.getConditionalExpressionNodes()[0])).getLeftOperandNode();
        NodeBase setupNode;
        FunctionCallNode assignedValue = createIteratorCall(ArrayLikeUtil.ARRAYLIKE_GET_NEXT);
        
        if (original instanceof VariableExpressionNode) {
            VariableNode varNode = (VariableNode)((VariableExpressionNode) original).getTargetVariable();
            //add the assigned value
            varNode.setAssignedValue(null, assignedValue);
            assignedValue.setParent(varNode);
            setupNode = varNode;
        } else if (original instanceof IdentifierNode) {
            ASToken assignOp = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT,-1,-1,-1,-1,"=");
            BinaryOperatorAssignmentNode assignmentNode = new BinaryOperatorAssignmentNode(assignOp,(IdentifierNode)original, assignedValue);
            ((IdentifierNode) original).setParent(assignmentNode);
            assignedValue.setParent(assignmentNode);
            setupNode = assignmentNode;
        } else {
            setupNode = null;
            //System.out.println("setupIteratee::UNEXPECTED");
        }
        if (setupNode != null) iterateeValueNode = assignedValue; //so we can run analyze on only the new content 'assignedValue'
        return setupNode;
    }
    
    public ForLoopNode getLoopTarget(){
        return target;
    }
    
    public IExpressionNode getIterationTarget(){
        //don't use copy/clone
        return ((BinaryOperatorInNode)(target.getConditionalExpressionNodes()[0])).getRightOperandNode();
    }
    
    public void prepareConditionals(boolean firstUse, FunctionCallNode factoryFuncCall){
        assert !conditionalsChanged : "populateConditionals was already called";
        //1st part of the substituted for(;;) loop
        mutatedConditionals.addItem(setupIterator(firstUse, factoryFuncCall));
        //2nd part of the  substituted for(;;) loop
        mutatedConditionals.addItem(createIteratorCall(ArrayLikeUtil.ARRAYLIKE_HAS_NEXT));
        //3rd part of the substituted for(;;) loop is a NilNode in this case
        mutatedConditionals.addItem(new NilNode());
        conditionalsChanged = true;
    }
    
    public void prepareContent(){
        assert !contentsChanged : "populateContent was already called";
        iteratee = setupIteratee();
        contentsChanged = iteratee != null;
    }
    
    public void processConditionals(ASScope scope, Collection<ICompilerProblem> problems){
        ContainerNode conditionals = target.getConditionalsContainerNode();
        conditionals.removeAllChildren();
        conditionals.addChild((NodeBase) mutatedConditionals.getChild(0),0);
        conditionals.addChild((NodeBase) mutatedConditionals.getChild(1),1);
        conditionals.addChild((NodeBase) mutatedConditionals.getChild(2),2);
        
        if (analyzeRequests == null) {
            analyzeRequests = new ArrayList<NodeBase>();
        }
        for (int i=0;i<3;i++) {
            analyzeRequests.add((NodeBase) conditionals.getChild(i));
        }
    }
    
    public void processContents(ASScope scope, Collection<ICompilerProblem> problems){
        BlockNode block = target.getContentsNode();
        //'insert' at position 0 to prepend this to the original loop's block content
        block.addChild(iteratee, 0);
        EnumSet<PostProcessStep> set = EnumSet.of(
                PostProcessStep.POPULATE_SCOPE);
        //only add the iteratee to the analysis. re-evaluating the original variable node (if it is not a pre-existing identifier)
        //would add it to the scope's definition store a second time, and we don't want that (probably no harm, but it would add a warning for duplicate variable declaration).
        if (analyzeRequests == null) {
            analyzeRequests = new ArrayList<NodeBase>();
        }
        analyzeRequests.add(iterateeValueNode);
    }
    
    @Override
    public IForLoopNode.ForLoopKind getKind() {
        return kind;
    }
    
    private boolean conditionalsChanged;
    @Override
    public boolean mutatesConditionals() {
        return conditionalsChanged;
    }
    
    private boolean contentsChanged;
    @Override
    public boolean mutatesContents() {
        return contentsChanged;
    }
    
}
