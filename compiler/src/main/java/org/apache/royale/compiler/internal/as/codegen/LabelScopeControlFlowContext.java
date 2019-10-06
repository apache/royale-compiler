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

package org.apache.royale.compiler.internal.as.codegen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.abc.semantics.Label;
import org.apache.royale.compiler.internal.tree.as.ConditionalNode;
import org.apache.royale.compiler.internal.tree.as.IfNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.SwitchNode;
import org.apache.royale.compiler.internal.tree.as.TerminalNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link ControlFlowContext} sub-class for a syntactic region of a function the
 * determines the visibility of labels referenced by goto statements. goto
 * statements can not reference labels in a a in a loop, with statement, try,
 * catch, or finally unless the construct containing the label is an ancestor of
 * the goto statement.
 */
class LabelScopeControlFlowContext extends ControlFlowContext
{
    /**
     * @param controlFlowTreeNode Syntax tree node for which this context is being created.
     */
    public LabelScopeControlFlowContext(IASNode controlFlowTreeNode)
    {
        super(controlFlowTreeNode);
        aetLabelMap = new HashMap<String, Label>();
    }
    
    
    private Multimap<String, LabeledStatementNode> labelNameToLabeledStatments;
    private final Map<String, Label> aetLabelMap;
    
    private Multimap<String, LabeledStatementNode> getLabelMap()
    {
        if (labelNameToLabeledStatments != null)
            return labelNameToLabeledStatments;
        labelNameToLabeledStatments = LinkedListMultimap.<String, LabeledStatementNode>create();
        populateLabelMap(labelNameToLabeledStatments, controlFlowTreeNode);
        return labelNameToLabeledStatments;
    }
    
    /**
     * Scans the tree looking for labels that can be referenced from goto
     * statements in the syntactic region for which this context was created.
     * 
     * @param labelMap Map to populate with found labels
     * @param node Node to search from.
     */
    @SuppressWarnings("incomplete-switch")
	private static void populateLabelMap(Multimap<String, LabeledStatementNode> labelMap, IASNode node)
    {
        ASTNodeID nodeID = node.getNodeID();
        
        switch (nodeID)
        {
            case IfStatementID:
                {
                    IfNode ifNode = (IfNode) node;
                    int nIfNodeChildren = ifNode.getChildCount();
                    for (int i = 0; i < nIfNodeChildren; ++i)
                    {
                        IASNode ifNodeChild = ifNode.getChild(i);
                        if (ifNodeChild instanceof ConditionalNode)
                        {
                            ConditionalNode conditionalNode = (ConditionalNode) ifNodeChild;
                            IASNode conditionalBlockNode = conditionalNode.getContentsNode();
                            assert conditionalBlockNode != null;
                            populateLabelMap(labelMap, conditionalBlockNode);
                        }
                        else if (ifNodeChild instanceof TerminalNode)
                        {
                            TerminalNode terminalNode = (TerminalNode) ifNodeChild;
                            IASNode terminalBlockNode = terminalNode.getContentsNode();
                            assert terminalBlockNode != null;
                            populateLabelMap(labelMap, terminalBlockNode);
                        }
                    }
                }
                break;
            case LabledStatementID:
                {
                    LabeledStatementNode labelNode = (LabeledStatementNode) node;
                    String labelName = labelNode.getLabel();
                    if (labelName != null)
                        labelMap.put(labelName, labelNode);
                    populateLabelMap(labelMap, labelNode.getLabeledStatement());
                }
                break;
            case FileID:
            case BlockID:
            case MXMLEventSpecifierID:
                {
                    int childCount = node.getChildCount();
                    for (int i = 0; i < childCount; ++i)
                    {
                        IASNode child = node.getChild(i);
                        assert child != null;
                        populateLabelMap(labelMap, child);
                    }
                }
                break;
            case SwitchID:
                {
                    SwitchNode switchNode = (SwitchNode)node;
                    populateLabelMap(labelMap, switchNode.getStatementContentsNode());
                }
                break;
            case ConditionalID:
                {
                    ConditionalNode conditionalNode = (ConditionalNode)node;
                    populateLabelMap(labelMap, conditionalNode.getStatementContentsNode());
                }
                break;
        }
    }
    
    /**
     * Finds all the {@link LabeledStatementNode}'s in this context with the
     * specified label name.
     * 
     * @param label Name of the {@link LabeledStatementNode}'s to search for.
     * @return All the {@link LabeledStatementNode}'s in this context with the
     * specified label name.
     */
    Collection<LabeledStatementNode> getLabelNodes(String label)
    {
        Multimap<String, LabeledStatementNode> labelMap = getLabelMap();
        return labelMap.get(label);
    }
    
    @Override
    Label getGotoLabel(String label)
    {
        assert !getLabelNodes(label).isEmpty() : "Don't try to get AET labels for labels not in this context!";
        Label result = aetLabelMap.get(label);
        if (result != null)
            return result;
        result = new Label(label);
        aetLabelMap.put(label, result);
        return result;
    }

    @Override
    boolean hasGotoLabel(String label, boolean allowDuplicates)
    {
        Multimap<String, LabeledStatementNode> labelMap = getLabelMap();
        if (allowDuplicates)
            return labelMap.containsKey(label);
        else
            return labelMap.get(label).size() == 1;
    }
}
