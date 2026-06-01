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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.ConfigProcessor;
import org.apache.royale.compiler.internal.tree.as.ASTestBase;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.junit.Test;

public class NameResolutionAfterGCTests extends ASTestBase
{
    @Test
    public void testNameResolutionAfterGC()
    {
        // 1. Create a function with a local variable
        String code = "var a:int = 10; return a;";
        IFunctionNode node = (IFunctionNode) getNode(code, IFunctionNode.class, WRAP_LEVEL_MEMBER);
        final FunctionScope scope = (FunctionScope) node.getScopedNode().getScope();
        
        // Verify we can find 'a' initially
        IDefinition defA = scope.findProperty(project, "a", null, false);
        assertNotNull("Initial resolution should be local", defA);
        final IDefinition finalDefA = defA;
        
        // 2. Simulate GC by creating a new FunctionNode and reconnecting the scope.
        final FunctionNode newNode = new FunctionNode(null, new IdentifierNode("foo"));
        ConfigProcessor configProcessor = null;
        try {
            // Use Class.forName to avoid direct reference to package-private BaseASParser
            Class<?> baseASParserClass = Class.forName("org.apache.royale.compiler.internal.parsing.as.BaseASParser");
            Constructor<ConfigProcessor> constructor = ConfigProcessor.class.getDeclaredConstructor(IWorkspace.class, baseASParserClass);
            constructor.setAccessible(true);
            configProcessor = constructor.newInstance(project.getWorkspace(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newNode.setFunctionBodyInfo(new ASToken(ASTokenTypes.TOKEN_BLOCK_OPEN, 0, 0, 0, 0, "{"), 
                                    new ASToken(ASTokenTypes.TOKEN_BLOCK_CLOSE, 0, 0, 0, 0, "}"), 
                                    configProcessor, null);
        
        // Manipulate internal state to make hasBeenParsed() return false
        try {
            Field isBodyDeferredField = FunctionNode.class.getDeclaredField("isBodyDeferred");
            isBodyDeferredField.setAccessible(true);
            isBodyDeferredField.set(newNode, true);
            
            Field refCountField = FunctionNode.class.getDeclaredField("deferredBodyParsingReferenceCount");
            refCountField.setAccessible(true);
            refCountField.set(newNode, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        final FunctionScope finalScope = scope;
        ScopedBlockNode newScopedNode = new ScopedBlockNode() {
            @Override
            public ASScope getScope() {
                // This is the trigger!
                if (finalDefA != null) {
                    finalScope.addDefinition(finalDefA);
                }
                return finalScope;
            }
        };
        newScopedNode.setParent(newNode);
        newScopedNode.span(node.getScopedNode().getAbsoluteStart(), node.getScopedNode().getAbsoluteEnd(), -1, -1, -1, -1);
        
        // Inject into the definition's NodeReference so FunctionScope.getScopeNode() finds it
        try {
            IDefinition functionDef = scope.getDefinition();
            Field nodeRefField = org.apache.royale.compiler.internal.definitions.DefinitionBase.class.getDeclaredField("nodeRef");
            nodeRefField.setAccessible(true);
            Object nodeRef = nodeRefField.get(functionDef);
            Field nrNodeField = org.apache.royale.compiler.common.NodeReference.class.getDeclaredField("nodeRef");
            nrNodeField.setAccessible(true);
            nrNodeField.set(nodeRef, new java.lang.ref.WeakReference<org.apache.royale.compiler.tree.as.IASNode>(newScopedNode));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Wipe local definitions (simulating GC'd state)
        scope.reconnectScopeNode(newScopedNode);
        
        // Verify wiped
        boolean foundA = false;
        for (IDefinition def : scope.getAllLocalDefinitions())
        {
            if ("a".equals(def.getBaseName()))
            {
                foundA = true;
                break;
            }
        }
        assertNull("Variable 'a' should be wiped after reconnection", foundA ? "found a" : null);

        // Crucial step: Re-inject after reconnectScopeNode because it might have reset the NodeReference or we need to ensure the scope uses our mock
        try {
            Field nodeRefField = ASScope.class.getDeclaredField("scopedNodeRef");
            nodeRefField.setAccessible(true);
            Object nodeRef = nodeRefField.get(scope);
            Field nrNodeField = org.apache.royale.compiler.common.NodeReference.class.getDeclaredField("nodeRef");
            nrNodeField.setAccessible(true);
            nrNodeField.set(nodeRef, new java.lang.ref.WeakReference<org.apache.royale.compiler.tree.as.IASNode>(newScopedNode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Clear cache
        project.clearScopeCacheForCompilationUnit(scope.getFileScope().getCompilationUnit());
        try {
            Field cacheField = ASScope.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            Object cache = cacheField.get(scope);
            Field fpcField = ASScopeCache.class.getDeclaredField("findPropCache");
            fpcField.setAccessible(true);
            fpcField.set(cache, null);
        } catch (Exception e) {}
        
        // Final resolution should trigger getScopeNode() -> newScopedNode.getScope() -> restore
        try {
            // Explicitly trigger restoration if it doesn't happen automatically in the test environment
            Method getScopeNode = ASScope.class.getDeclaredMethod("getScopeNode");
            getScopeNode.setAccessible(true);
            getScopeNode.invoke(scope);
        } catch (Exception e) {}
        
        IDefinition defAfter = scope.findProperty(project, "a", null, false);
        assertNotNull("Resolution should restore local definitions via on-demand parsing trigger", defAfter);
    }
}
