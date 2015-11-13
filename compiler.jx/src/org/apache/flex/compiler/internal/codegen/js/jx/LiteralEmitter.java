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
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;

public class LiteralEmitter extends JSSubEmitter implements
        ISubEmitter<ILiteralNode>
{

    public LiteralEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILiteralNode node)
    {
        boolean isWritten = false;

        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
            	if (s.contains("'"))
            		write("\"" + s + "\"");
            	else
            		write("'" + s + "'");

                isWritten = true;
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", "\\\\n");
        }

        if (!isWritten)
        {
            write(s);
        }
    }
}
