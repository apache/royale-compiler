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
package org.apache.flex.compiler.internal.codegen.mxml.vf2js;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.internal.test.VF2JSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

public class TestVF2JSMXMLNodes extends VF2JSTestBase
{

    @Test
    public void testSimpleButtonNode()
    {
        String code = "<s:Button />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button></Button>");
    }

    @Test
    public void testButtonNodeWithVF2JSAttribute()
    {
        String code = "<s:Button label=\"hello\" />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button label=\"hello\"></Button>");
    }

    @Test
    public void testButtonNodeWithNonVF2JSAttribute()
    {
        String code = "<s:Button string=\"bye\" />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        // (erikdebruin) The attribute doesn't exist in VF2JS, so it's ignored
        assertOut("<Button></Button>");
    }
    
    @Test
    public void testButtonNodeWithBothVF2JSAndNonVF2JSAttribute()
    {
        String code = "<s:Button label=\"hello\" string=\"bye\" />";
        
        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);
        
        mxmlBlockWalker.visitPropertySpecifier(node);
        
        // (erikdebruin) The attribute 'string' doesn't exist in VF2JS, so it's ignored
        assertOut("<Button label=\"hello\"></Button>");
    }

}
