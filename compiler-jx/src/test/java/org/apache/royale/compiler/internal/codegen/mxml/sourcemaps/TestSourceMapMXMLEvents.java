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
package org.apache.royale.compiler.internal.codegen.mxml.sourcemaps;

import org.apache.royale.compiler.internal.test.RoyaleSourceMapTestBase;
import org.apache.royale.compiler.internal.test.RoyaleTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;

import org.junit.Test;

public class TestSourceMapMXMLEvents extends RoyaleSourceMapTestBase
{
    @Test
    public void testEvent()
    {
        String code = "<basic:Button click=\"event;\"/>";

        IMXMLEventSpecifierNode node = (IMXMLEventSpecifierNode) getNode(code, IMXMLEventSpecifierNode.class, RoyaleTestBase.WRAP_LEVEL_DOCUMENT);
        IMXMLDocumentNode dnode = (IMXMLDocumentNode) node
                .getAncestorOfType(IMXMLDocumentNode.class);
        mxmlBlockWalker.walk(dnode);
        ///event
        assertMapping(node, 0, 6, 74, 2, 74, 7);  // event
        //the start column in the ActionScript seems to be outside the quote
        //instead of inside. that seems like a bug. -JT
    }
}
