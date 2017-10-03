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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Michael Schmalle
 */
public class TestParenthesis extends ASTestBase
{
    @Test
    public void testParentheses_1()
    {
        IVariableNode node = (IVariableNode) getNode("var a = (a + b);",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:* = (a + b)");
    }

    @Test
    public void testParentheses_2()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a = ((a + b) - (c + d)) * e;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:* = ((a + b) - (c + d)) * e");
    }

    @Test
    public void testParentheses_3()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "a = (a + b) - c + d * e;", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = (a + b) - c + d * e");
    }

    @Test
    public void testParentheses_4()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "a = ((a + b) - (c + d)) * e;", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = ((a + b) - (c + d)) * e");
    }

    @Test
    public void testParentheses_Strings1()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "a = '' + '' + '' + ''", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = '' + '' + '' + ''");
    }

    @Test
    public void testParentheses_Strings2()
    {
        // this is a whacked test but is just proves the logic that for now, 
        // we only leave out parens for String literals on the right hand side
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "a = '' + 2 + '' + '' * 4 ", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = '' + 2 + '' + '' * 4");
    }
    
    @Test
    public void testParentheses_Ternary()
    {
        IReturnNode node = (IReturnNode) getNode(
        		"return \"a \" + (a < b ? \"<\" : \">=\") + \" b\";", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertOut("return \"a \" + (a < b ? \"<\" : \">=\") + \" b\"");
    }

}
