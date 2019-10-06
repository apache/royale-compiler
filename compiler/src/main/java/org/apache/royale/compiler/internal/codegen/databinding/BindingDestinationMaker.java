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

import static org.apache.royale.abc.ABCConstants.*;

import java.util.LinkedList;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.as.codegen.Binding;
import org.apache.royale.compiler.internal.as.codegen.InstructionListNode;
import org.apache.royale.compiler.internal.as.codegen.MXMLClassDirectiveProcessor;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.tree.as.DynamicAccessNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelRootNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;

/**
 * Utility class for analyze binding destinations and making
 * "destination functions" for them.
 * 
 * Note that a "destination function" is passed as an argument to the 
 * constructor of the SDK's mx.binding.Binding object.
 * 
 * A typical destination function looks like:
 *  function (_sourceFunctionReturnValue:*): void
 *  {
 *      some_complex_lvalue = _sourceFunctionReturnValue;
 *  }
 */
public class BindingDestinationMaker
{
    public static class Destination
    {
        public Destination(IExpressionNode destinationASTNode, boolean transformationRequired)
        {
            this.destinationASTNode = destinationASTNode;
            this.transformationRequired = transformationRequired;
        }
        public IExpressionNode destinationASTNode;
        boolean transformationRequired;
    }
    
    /**
     * Generate destination function from an IMXMLConcatenatedDataBindingNode 
     * or IMXMLDataBindingNode
     * 
     * Do this by walking up and down the tree, building up instruction list
     */
    public  static IExpressionNode makeDestinationFunctionInstructionList(IMXMLDataBindingNode dbnode,
            MXMLClassDirectiveProcessor host)
    {   
        IExpressionNode ret = null;
        final IASNode parent = dbnode.getParent();
        
      
        // We only know how to make dest functions in very specific cases  
        // Case 1: we are in the <fx:Model> tag
        if (parent instanceof IMXMLModelPropertyNode)
        {
            // search up until we find the model tag
            // keep an ordered list of the nodes that will contribute to the instruction list
            LinkedList<IASNode > nodes = new LinkedList<IASNode >(); 
            IASNode tempNode = parent;

            for (boolean done=false; !done; )
            {
                if (tempNode instanceof IMXMLModelNode)
                {
                    // we found the root. terminate
                    done = true;  
                }
                
                // Save the node in our array, if we care about it
                if (!(tempNode instanceof IMXMLModelRootNode))
                {
                    nodes.addFirst(tempNode);
                }
                tempNode = tempNode.getParent();
                if (tempNode == null)
                {
                    assert false;   // no root. tree is mal-formed
                    return null;
                }
            }
            
            // Now we can generate the instructions by going through our node list in order,
            // which is like traversing the model tree from Model tag down to leaf
            InstructionList insns = new InstructionList();
            insns.addInstruction(OP_getlocal0);
   
            int nodeIndex = 0;
            for (IASNode n : nodes)
            {
                boolean last = nodeIndex == (nodes.size()-1);
                generateOne(insns, n, last);
                ++nodeIndex;
            }

           ret = new InstructionListNode(insns);    // Wrap the IL in a node and return it
        }
        else if (parent instanceof IMXMLPropertySpecifierNode && dbnode instanceof IMXMLSingleDataBindingNode)
        {
            IMXMLPropertySpecifierNode psn = (IMXMLPropertySpecifierNode)parent;
            IDefinition d = psn.getDefinition();
            //it's possible for the definition to be null if we're dealing with
            //a dynamic property on a class like Object -JT
            if (d != null)
            {
                Binding b = host.getInstanceScope().getBinding(d);
                INamespaceReference ns = d.getNamespaceReference();
                if (ns != NamespaceDefinition.getPublicNamespaceDefinition())
                {
                    InstructionList insns = new InstructionList();
                    insns.addInstruction(OP_getlocal0);
                    insns.addInstruction(OP_getlocal1);
                    insns.addInstruction(OP_setproperty, b.getName());
                    ret = new InstructionListNode(insns);    // Wrap the IL in a node and return it
                }
            }
        }
        else if (parent instanceof IMXMLStringNode && dbnode instanceof IMXMLConcatenatedDataBindingNode)
        {
        	// this is a binding in a literal like a string, such as 'this is {SOME_VAR} times';
        	// we need to figure out how to set the evaluated value.
        	// if this binding is not in an array, then other code will use the effectiveID of the string
        	// and set the value.
        	// if it is in an array, then figure out the index in the array to set
        	if (parent.getParent() instanceof IMXMLArrayNode)
        	{
        		int index = -1;
        		int n = parent.getParent().getChildCount();
        		for (int i = 0; i < n; i++)
        		{
        			IASNode child = parent.getParent().getChild(i);
        			if (child == parent)
        			{
        				index = i;
        				break;
        			}
        		}
        		IdentifierNode arrayNode = new IdentifierNode(((IMXMLArrayNode)parent.getParent()).getEffectiveID());
        		arrayNode.setSourcePath(parent.getSourcePath());
        		arrayNode.setColumn(parent.getColumn());
        		arrayNode.setLine(parent.getLine());
        		NumericLiteralNode indexNode = new NumericLiteralNode(new Integer(index).toString());
        		indexNode.setSourcePath(parent.getSourcePath());
        		indexNode.setColumn(parent.getColumn());
        		indexNode.setLine(parent.getLine());
        		DynamicAccessNode mae = new DynamicAccessNode(arrayNode);
        		mae.setRightOperandNode(indexNode);
        		mae.setParent((NodeBase) dbnode.getParent());
        		arrayNode.setParent(mae);
        		indexNode.setParent(mae);
        		return mae;
        	}
        }
        return ret;   
    }
    
    /**
     * Generate the instructions for one level of the fx:Model tree
     * 
     */
    private static void generateOne(InstructionList insns, IASNode node, boolean lastOne)
    {
        if (node instanceof IMXMLModelPropertyNode)
            generateOneLevel(insns, (IMXMLModelPropertyNode)node, lastOne);
        else if (node instanceof IMXMLModelNode)
            generateModel(insns, (IMXMLModelNode)node, lastOne);
        else assert false;
    }
    
    private static void generateOneLevel(InstructionList insns, IMXMLModelPropertyNode node, boolean lastOne)
    { 
        if (!lastOne)
        {
            // get the next property in the destination chain
            insns.addInstruction(OP_getproperty, new Name(node.getName()));
            if (node.getIndex() >= 0)
            {
                // If it's an array, then get the correct element out of it
                String nodeIndexName = Integer.toString(node.getIndex());
                insns.addInstruction(OP_getproperty, new Name(nodeIndexName));
            }
        }
        else if (node.getIndex() < 0)
        {
            // last node, not array
            // get the passed in function parameter, and store it into
            // the final property in the model node chain
            insns.addInstruction(OP_getlocal1);    
            insns.addInstruction(OP_setproperty, new Name(node.getName()));
        }
        else
        {
            // last node, it's an array
            // get the array...
            insns.addInstruction(OP_getproperty, new Name(node.getName()));
            
            // get the passed in function parameter, and store it into
            // the correct element of the array
            insns.addInstruction(OP_getlocal1); 
            String nodeIndexName = Integer.toString(node.getIndex());
            insns.addInstruction(OP_setproperty, new Name(nodeIndexName));
        }
    }

    private static void generateModel(InstructionList insns, IMXMLModelNode model, boolean lastOne)
    {
        if (!lastOne)
        {
            String id = model.getEffectiveID();
            assert id != null;
            insns.addInstruction(OP_getproperty, new Name(id));
        }
        else
            assert false;// This would correspond to <Model><a>{}</a></Model>
                        // Since this is invalid, higher level code detects that
                        // and never calls us
    }
}
