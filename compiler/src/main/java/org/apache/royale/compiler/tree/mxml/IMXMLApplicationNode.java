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

package org.apache.royale.compiler.tree.mxml;

/**
 * This AST node represents the root tag defining an MXML application.
 */
public interface IMXMLApplicationNode extends IMXMLDocumentNode
{
    /**
     * Gets the frame rate specified by the <code>frameRate</code> attribute on
     * the application tag.
     * 
     * @return The frame rate as an integer.
     */
    int getFrameRate();

    /**
     * Gets the page title specified by the <code>pageTitle</code> attribute on
     * the application tag.
     * 
     * @return The page title as a String.
     */
    String getPageTitle();

    /**
     * Gets the script recursion limit specified by the
     * <code>scriptRecursionLimit</code> attribute on the application tag.
     * 
     * @return The script recursion limit as an integer.
     */
    int getScriptRecursionLimit();

    /**
     * Gets the script time limit specified by the <code>scriptTimeLimit</code>
     * attribute on the application tag.
     * 
     * @return The script time limit as an integer.
     */
    int getScriptTimeLimit();
}
