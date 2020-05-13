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

package org.apache.royale.compiler.internal.codegen.databinding;

import java.util.LinkedList;
import java.util.List;


import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.as.codegen.InstructionListNode;
import org.apache.royale.compiler.internal.as.codegen.MXMLClassDirectiveProcessor;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingAttributeNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;

/**
 * data that describes a single databinding expression.
 * This data is put together during an analysis pass, then used for codegen.
 */
public class BindingInfo implements Comparable<BindingInfo>
{
    /**
     * This form of the constructor is used for the "normal" case,
     * like <s:Button label="{foo}"/>
     * 
     * In this case the node MUST be either a MXMLDataBindingNode or 
     * MXMLConcatenatedDataBindingNode
     */
    public BindingInfo(IMXMLDataBindingNode dbnode, int index, MXMLClassDirectiveProcessor host)
    {
        this.index = index;
        node = dbnode;
        
        // look at the node we are passed, and expand it out to all
        // of its expression children
        expressionNodesForGetter = new LinkedList<IExpressionNode>();
        if (dbnode instanceof IMXMLSingleDataBindingNode)
        {
            expressionNodesForGetter.add( ((IMXMLSingleDataBindingNode) dbnode).getExpressionNode());
        }
        else if (dbnode instanceof IMXMLConcatenatedDataBindingNode)
        {
            for (int childIndex=0; childIndex < dbnode.getChildCount(); ++childIndex)
            {
                IASNode child = dbnode.getChild(childIndex);
                if (child instanceof IMXMLSingleDataBindingNode)
                {
                    expressionNodesForGetter.add( ((IMXMLSingleDataBindingNode) child).getExpressionNode());
                }
                else if (child instanceof IExpressionNode)
                {
                    expressionNodesForGetter.add( (IExpressionNode)child);
                }
                else
                {
                    assert false;
                }
            }
        }
        else
        {
            assert false;
        }
        
        // now attempt to make a destination function and a destination string
        // for the binding.
        expressionNodeForSetter = BindingDestinationMaker.makeDestinationFunctionInstructionList(dbnode, host);
        destinationString = findDestinationString(dbnode, host);
        
        finishInit(host);
    }


 
    /**
     * Constructor for use with a BindingNode.  The binding node specifies
     * both the source and destination expressions explicitly.
     * Usually these come from either the <fx:Binding> tag, or as an implementation detail
     * of the <fx:XML> tag
     * 
     * @param reverseSourceAndDest - if true, analyze the binding as if the source was the destination
     *          and the destination was the source
     */
    public BindingInfo(
            IMXMLBindingNode bindingNode,
            int index,
            MXMLClassDirectiveProcessor host,
            boolean reverseSourceAndDest)
    {
        this.index = index;
        node = bindingNode;
        
        IExpressionNode destinationNode = null;
        expressionNodesForGetter = new LinkedList<IExpressionNode>();
        // look at the node we are passed, and expand it out to all
        // of its expression children
        
        if (!reverseSourceAndDest)
        {
            expressionNodesForGetter.add( bindingNode.getSourceAttributeNode().getExpressionNode());
            destinationNode = bindingNode.getDestinationAttributeNode().getExpressionNode();
        }
        else
        {
            expressionNodesForGetter.add( bindingNode.getDestinationAttributeNode().getExpressionNode());
            destinationNode = bindingNode.getSourceAttributeNode().getExpressionNode();
        }
        expressionNodeForSetter = destinationNode;
        
        // We still need a dest string even if we have a function *sigh*
        // The binding manager requires this, as it uses it to identify bindings
       
        destinationString = findDestinationString(destinationNode, host);
       
       assert  expressionNodeForSetter != null;     // we should always be able to make a destination function
       
        finishInit(host);
    }
    
    /** common code used by all the constructors
     */
    private void finishInit(MXMLClassDirectiveProcessor host)
    {
        ClassDefinition classDef = host.getClassDefinition();
        ASScope classScope = classDef.getContainedScope();
        analyzeExpression(host.getProject(), classScope);
        
        // TODO: we should be able to assert that we make a dest string, becuase
        // in general the binding manager needs one, even if we have a dest func.
        // HOWEVER - we don't always generate on now, and it seems OK.
    }

   
    // ------------ private vars ------------
    private final List<IExpressionNode> expressionNodesForGetter;  
    private String destinationString;
    final int index;                  // the _bindings array index for this binding
    private  boolean isSimplePublicProperty;
    private  String sourceString;
    private int twoWayCounterpart = -1;     // index of two way counterpart, or -1
    public IMXMLNode node;
    public ClassDefinition classDef; // non-null if binding to static const or var
    
    // The expression node that represents the destination
    // this is used for more complex destinations, like inside an XML object
    // where the destination could be something like:
    //    myXML.a[0].text()[0]
    // that can't be easily codegen'ed from a String
    private IExpressionNode expressionNodeForSetter;
    

    // ------------ public methods ------------
    public int getIndex()
    {
        assert index >= 0;
        return index;
    }
    
    public int getTwoWayCounterpart()
    {
        return twoWayCounterpart;
    }
    
    public void setTwoWayCounterpart(int twoWayCounterparterpart)
    {
        this.twoWayCounterpart = twoWayCounterparterpart;
    }
    /**
     * 
     * @return the nodes for the expressions in between the { } curlies, or null if no getter is needed
     *  
     */
    public List<IExpressionNode> getExpressionNodesForGetter()
    {
       return expressionNodesForGetter;
    }

    /**
     * Get the IExpressionNode that represents the destination
     */
    public IExpressionNode getExpressionNodeForDestination()
    {
        return expressionNodeForSetter;
    }
    
    /**
     * @return the name of the binding destination property
     */
    public String getDestinationString()
    {
       return destinationString;
    }
    
    /**
     * param the name of the binding destination property
     */
    public void setDestinationString(String newDestString)
    {
       destinationString = newDestString;
    }
    
    /**
     * just for debugging
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "BindingInfo #" + index + 
                " destStr:" + destinationString +
                " srcStr:" + sourceString + "\n"
                );
        if (expressionNodeForSetter != null)
        {
            sb.append("    destFunc ");
            sb.append("node:\n");
            NodeBase n = (NodeBase) expressionNodeForSetter;
            n.buildStringRecursive(sb, 3, false);
        }
        if (!expressionNodesForGetter.isEmpty())
        {
            sb.append("    Getter Expressions:\n");
            for (IExpressionNode e: expressionNodesForGetter)
            {
                NodeBase n = (NodeBase)e;
                n.buildStringRecursive(sb, 3, false);      // put in the expression node with nice indenting
            }
        }
        return sb.toString();
    }
    
    // ------------ private methods ------------
    
    /** 
     * Synthesizes the "distinationString" argument for the PropertyWatcher constructor.
     * for example, in <s:Button id="foo" bar="{goo}"/>
     * the destination is "foo.bar"
     */
    private static String findDestinationString(IASNode node, MXMLClassDirectiveProcessor host)
    {
        // get the parent of the node to figure out what kind of destination we are
        String destString=null;
        final IASNode parent = node.getParent();
        if (parent instanceof IMXMLPropertySpecifierNode)
        {
            destString = findDestinationStringFromPropertySpecifier((IMXMLPropertySpecifierNode)parent)  ; 
        }
        else if (parent instanceof IMXMLBindingAttributeNode)
        {
            destString = findDestinationStringFromBindingAttribute(node)  ;
        }
        else if (parent instanceof IMXMLExpressionNode)
        {
            // We are an MXML primitive (like sf:String), so the dest string is just our ID
            String id = ((IMXMLExpressionNode)parent).getEffectiveID();
            assert id != null;
            destString = id;
        }
        else if (parent instanceof IMXMLModelPropertyNode)
        {
            // For now, we are always making a destination function, so we
            // don't need to make a string.
            // We might want to do either/or, as a possible optimization
        }
        else
        {
            // there will be (presumably) some cases where we can't make a destination string.
            // For now, however, any case where we fail to do so is probably a bug
            System.err.println("findDestinationString can't parse parent: " + parent);
        }
        return destString;
    }
    
    /**
     * A typical tree shape for this case is:
     * 
     * MXMLBindingNode twoWay="false" 31:2 loc: 900-948 abs: 900-948 null
     *   MXMLBindingAttributeNode "source" 31:14 loc: 912-924 abs: 912-924 null
     *    IdentifierNode "src" 31:22 loc: 920-923 abs: 920-923 null
     *  MXMLBindingAttributeNode "destination" 31:27 loc: 925-946 abs: 925-946 null
     *    MemberAccessExpressionNode "." 31:40 loc: 938-945 abs: 938-945 null
     *      IdentifierNode "b" 31:40 loc: 938-939 abs: 938-939 null
     *      IdentifierNode "label" 31:42 loc: 940-945 abs: 940-945 null
     *      
     * Where the "node" parameter is MemberAccessExpressionNode "." 31:40
     * and the "parent" that got us here is MXMLBindingAttributeNode "destination" 31:27
     * @param node
     * @return
     */
    private static String findDestinationStringFromBindingAttribute(IASNode node)
    {
        String ret = null;
        
        // TODO: why not just remember the string in the node?
        // TODO: This needs to be recursive in order to make destination strings from
        // a.b.c.d 
        // For now we can only do x or x.y
        
        // More importantly, we need to make destination functions for the really gnarly cases.
        // Once we do that, we may decide to remove this...
        
        if (node instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode maNode = (IMemberAccessExpressionNode)node;
            IExpressionNode left = maNode.getLeftOperandNode();
            IExpressionNode right = maNode.getRightOperandNode();
           if (left instanceof IIdentifierNode && right instanceof IIdentifierNode)
           {
               ret = ((IIdentifierNode) left).getName() + "." + ((IIdentifierNode) right).getName();
           }
           else
           {
               //
              // System.err.println("findDestinationStringFromBindingAttribute (1) can't parse " + node);
           }
        }
        else if (node instanceof IIdentifierNode)
        {
            ret = ((IIdentifierNode) node).getName();
        }
        else
        {
            if (!(node instanceof InstructionListNode))
            {
                //
                //System.err.println("findDestinationStringFromBindingAttribute (2) can't parse " + node);
            }
        }
        return ret;
    }


    /**
     *   A typical tree shape for this case is:
     *   
     * MXMLInstanceNode "spark.components.Button" id="b2" 38:3 loc: 863-901 abs: 863-901 null
     *   MXMLPropertySpecifierNode "label" 38:21 loc: 881-899 abs: 881-899 null
     *     MXMLDataBindingNode "DataBinding" 38:28 loc: 888-898 abs: 888-898 D:\builder_trunk\compiler\org.apache.royale.compiler\generated\tests\scratch\mxmlfunctional\Binding3\bindtest3.mxml
     *       MemberAccessExpressionNode "." 38:29 loc: 889-897 abs: 889-897 null
     *         IdentifierNode "b1" 38:29 loc: 889-891 abs: 889-891 null
     *         IdentifierNode "label" 38:32 loc: 892-897 abs: 892-897 null
     *         
     * where the "propertySpecifier" parameter to this function is MXMLPropertySpecifierNode "label" 38:21
     * 
     * and we discovered the property specifier because he is the parent of the node we
     * are making the binding for, which is MXMLDataBindingNode "DataBinding" 38:28 
     */
    private static String findDestinationStringFromPropertySpecifier(IMXMLPropertySpecifierNode propertySpecifier)
    {
        String ret = null;
        IMXMLClassReferenceNode propertyParent = (IMXMLClassReferenceNode)
        propertySpecifier.getAncestorOfType( IMXMLClassReferenceNode.class);  
        assert propertyParent != null;
   
        // If the property is a property of an instance, get id.name
        if (propertyParent instanceof IMXMLInstanceNode)
        {
            IMXMLInstanceNode instanceNode = (IMXMLInstanceNode)propertyParent;
            // get the effective id. If the component doesn't have an ID, then we will have already made up
            // one.
            String id = instanceNode.getEffectiveID();
            assert id != null;
            assert instanceNode.getID()==null || id.equals(instanceNode.getID());
            ret = id + "." + propertySpecifier.getName();
        }       
        // If it's a property of the root, get this.name
        else if (propertyParent instanceof IMXMLClassDefinitionNode)
        {
            ret = "this." +  propertySpecifier.getName();
        }
        else assert false;
        return ret;
    }

    @Override
    public int compareTo(BindingInfo o)
    {
        return this.index - o.index;
    }
   
    /**
     * Is the binding source a simple public member variable?
     */
    public boolean isSourceSimplePublicProperty()
    {
       return this.isSimplePublicProperty;
    }
    
    /**
     * The string that represents the binding source, but only for simple publics
     * may return null if binding source is not simple public property
     */
    public String getSourceString()
    {
        return sourceString;
    }
    
    /**
     * extract some information from the binding expression node, and store for later
     * The information we are looking for is "is this a simple property, and if so what is it's name"
     */
    private void analyzeExpression(ICompilerProject project, ASScope scope)
    {
        assert scope instanceof TypeScope;      // we expect to get in the MXML document we are compiling
        
        // we there are multiple children, then we can't be a simple prop
        if (expressionNodesForGetter.size() != 1)
            return;
        
        IExpressionNode expressionNodeForGetter = expressionNodesForGetter.get(0);
        //if it is a MemberAccessExpressionNode, and the first node is Identifier 'this' , then we will only examine right operand node
        boolean hadThis = false;
        if (expressionNodeForGetter instanceof MemberAccessExpressionNode) {
            if (((MemberAccessExpressionNode) expressionNodeForGetter).getLeftOperandNode() instanceof IIdentifierNode
                && ((IIdentifierNode) ((MemberAccessExpressionNode) expressionNodeForGetter).getLeftOperandNode()).getName().equals(IASKeywordConstants.THIS)
            ) {
                expressionNodeForGetter = ((MemberAccessExpressionNode) expressionNodeForGetter).getRightOperandNode();
                hadThis = true;
            }
        }


        if (expressionNodeForGetter instanceof IIdentifierNode)
        {
            String name = ((IIdentifierNode)expressionNodeForGetter).getName();
            IReference ref = ReferenceFactory.lexicalReference(project.getWorkspace(), name);
            ASScope resolutionScope = hadThis ? ((TypeScope) scope).getInstanceScope() : scope;
            IDefinition def = ref.resolve(project, resolutionScope, DependencyType.EXPRESSION, false);
            if (def instanceof IVariableDefinition)
            {
                // here we have decided that the binding expression is a variable
                IVariableDefinition var = (IVariableDefinition)def;
                    INamespaceReference ns = var.getNamespaceReference();
                    if (ns == NamespaceDefinition.getPublicNamespaceDefinition())
                    {
                        // ok, our variable is public - let's take it
                        sourceString = def.getBaseName();
                        isSimplePublicProperty = true;
                    }
            }

            if (def instanceof IConstantDefinition) {
                IConstantDefinition cnst = (IConstantDefinition) def;
                    INamespaceReference ns = cnst.getNamespaceReference();
                    if (ns == NamespaceDefinition.getPublicNamespaceDefinition())
                    {
                        // ok, our constant is public - let's take it
                        sourceString = def.getBaseName();
                        isSimplePublicProperty = true;
                    }
            }
        }
        else if (expressionNodeForGetter instanceof MemberAccessExpressionNode)
        {
        	MemberAccessExpressionNode mae = (MemberAccessExpressionNode)expressionNodeForGetter;
            IDefinition def;
        	if (hadThis) {
        	    def = expressionNodesForGetter.get(0).resolve(project);
            }
            else def = mae.resolve(project);
            if (def != null && def.isPublic() && 
            		(def instanceof IAccessorDefinition ||
            		 def instanceof IConstantDefinition ||
            		 def instanceof IVariableDefinition))
            {
            	IExpressionNode leftSide = mae.getLeftOperandNode();
            	if (leftSide instanceof IIdentifierNode)
            	{
                    IDefinition leftDef = leftSide.resolve(project);
                    if (leftDef.isPublic())
                    {
                        if (leftDef instanceof ClassDefinition)
                            classDef = (ClassDefinition)leftDef;
                        sourceString = leftDef.getBaseName() + "." + def.getBaseName();
                        isSimplePublicProperty = true;
                    }
            	}
            	else if (leftSide instanceof MemberAccessExpressionNode)
            	{
            		IDefinition leftDef = leftSide.resolve(project);
		        	if (leftDef.isPublic())
		        	{
		        		if (leftDef instanceof ClassDefinition)
		        		{
		        			classDef = (ClassDefinition)leftDef;
			        		sourceString = leftDef.getBaseName() + "." + def.getBaseName();
			                isSimplePublicProperty = true;            		
		        		}
		        		else
		        		{
		            		sourceString = buildChain((MemberAccessExpressionNode) leftSide);
		            		if (sourceString != null)
		            		{
		            			sourceString += "." + def.getBaseName();
				                isSimplePublicProperty = true;            		
		            		}
		        		}
		        	}
            	}
            	else if (leftSide instanceof IFunctionCallNode)
            	{
            		IFunctionCallNode fun = (IFunctionCallNode)leftSide;
        			IExpressionNode[] args = fun.getArgumentNodes();
        			if (args.length == 1)
        			{
        				IExpressionNode arg = args[0];
        				if (arg instanceof IIdentifierNode)
        				{
        					IDefinition argDef = arg.resolve(project);
        					if (argDef.isPublic())
        					{
        		        		sourceString = argDef.getBaseName() + "." + def.getBaseName();
        		                isSimplePublicProperty = true;            		
        					}
        				}
        			}
            	}
            }
        }
    }
    
    private String buildChain(MemberAccessExpressionNode mae)
    {
    	IExpressionNode left = mae.getLeftOperandNode();
    	if (left.getNodeID() == ASTNodeID.IdentifierID)
    	{
    		IExpressionNode right = mae.getRightOperandNode();
    		if (right.getNodeID() == ASTNodeID.IdentifierID)
    		{
    			return ((IdentifierNode)left).getName() + "." + ((IdentifierNode)right).getName();
    		}
    	}
    	else if (left.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    	{
    		IExpressionNode right = mae.getRightOperandNode();
    		if (right.getNodeID() == ASTNodeID.IdentifierID)
    		{
    			String l = buildChain((MemberAccessExpressionNode)left);
    			if (l == null) return null;
    			return l + "." + ((IdentifierNode)right).getName();
    		}    		
    	}
    	return null;
    }
}
