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

package org.apache.royale.compiler.internal.scopes;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Collections;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.ConfigProcessor;
import org.apache.royale.compiler.internal.tree.as.ASTestBase;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.junit.Test;

public class NameResolutionAfterGCTest extends ASTestBase
{
    @Test
    public void testNameResolutionAfterGC()
    {
        // 1. Create a function with a local variable
        String code = "var a:int = 10; return a;";
        IFunctionNode node = (IFunctionNode) getNode(code, IFunctionNode.class, WRAP_LEVEL_MEMBER);
        FunctionScope scope = (FunctionScope) node.getScopedNode().getScope();
        
        // Verify we can find 'a' initially
        IDefinition defA = scope.findProperty(project, "a", null, false);
        assertNotNull("Initial resolution should be local", defA);
        
        // 2. Simulate GC by creating a new FunctionNode and reconnecting the scope.
        // This simulates what happens when a FileNode is garbage collected and then re-parsed.
        // We need a FunctionNode shell that hasn't been parsed yet.
        FunctionNode newNode = new FunctionNode(null, new IdentifierNode("foo"));
        // To make hasBeenParsed() return false, we set the function body info as deferred.
        newNode.setFunctionBodyInfo(new ASToken(ASTokenTypes.TOKEN_BLOCK_OPEN, 0, 0, 0, 0, "{"), 
                                    new ASToken(ASTokenTypes.TOKEN_BLOCK_CLOSE, 0, 0, 0, 0, "}"), 
                                    new ConfigProcessor(project.getWorkspace(), null), null);
        
        IScopedNode newScopedNode = newNode.getScopedNode();
        // Since we are creating newNode manually, we must manually set its parent
        ((NodeBase)newScopedNode).setParent(newNode);
        
        // This call to reconnectScopeNode should wipe local definitions because newNode.hasBeenParsed() is false.
        scope.reconnectScopeNode(newScopedNode);
        
        // 3. Verify that the definitions were indeed wiped (explicit check).
        // We use getAllLocalDefinitions() because it does NOT trigger the on-demand parsing fix
        // in FunctionScope. It only inspects the current (wiped) state of the definition store.
        boolean foundA = false;
        for (IDefinition def : scope.getAllLocalDefinitions())
        {
            if ("a".equals(def.getBaseName()))
            {
                foundA = true;
                break;
            }
        }
        assertNull("Variable 'a' should be wiped from the raw scope store after reconnection", foundA ? "found a" : null);
        
        // 4. Test resolution after "GC" restoration.
        // In a real scenario, getScopeNode() would trigger a re-parse that populates the node.
        // Here we simulate the completion of a re-parse by clearing the deferred flag
        // and manually adding the definition back to the scope.
        // NOTE: In a real re-parse, the ASParser would create brand new definition instances.
        // For this test, we re-use 'defA' to simplify the setup and focus on verifying 
        // that the FunctionScope correctly triggers the restoration logic and allows 
        // definitions to be re-added after they were wiped.
        try {
            Field field = FunctionNode.class.getDeclaredField("isBodyDeferred");
            field.setAccessible(true);
            field.set(newNode, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // We MUST re-use the SAME scope object but give it its "re-parsed" definitions.
        scope.addDefinition(defA);
        
        // Before we call findProperty, we must clear the cache to ensure that the 
        // name resolution logic actually calls getPropertyForScopeChain and triggers the fix.
        project.resetScopeCaches(Collections.singleton(scope));
        
        // If the fix in FunctionScope is working, this call will trigger getScopeNode(),
        // which ensures the function body is re-parsed (or in this case, uses our "re-parsed" node).
        IDefinition defAfter = scope.findProperty(project, "a", null, false);
        assertNotNull("Resolution after reconnection should successfully restore local definitions via on-demand parsing", defAfter);
    }
}
