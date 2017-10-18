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
 * This AST node represents an MXML {@code <fx:DesignLayer>} tag.
 * <ul>
 * <li>{@code <fx:DesignLayer>} is scoped to the 2009 language namespace.</li>
 * <li>Only {@code id}, {@code visible} and {@code alpha} are legal properties.</li>
 * <li>{@code visible} and {@code alpha} properties can have states.</li>
 * <li>These attributes are not supported: includeIn, excludeFrom,
 * itemCreationPolicy, and itemDestructionPolicy.</li>
 * <li>A {@code DesignLayer} tag without any attribute is a "no-op". It will be
 * skipped during code generation.</li>
 * <li>Layer children of optimized (stripped) layers will be hoisted up to
 * parent layers if applicable.</li>
 * </ul>
 * <p>
 * Although {@code <fx:DesignLayer>} tag is a MXML 2009 Language tag, it does
 * have a corresponding ActionScript class definition -
 * {@code mx.core.DesignLayer}.
 * <p>
 * The implementation of this AST node extends {@code IMXMLInstanceNode} so that
 * it can take advantage of the property definitions in the backing ActionScript
 * class. However, the generated code can not treat a {@code <fx:DesignLayer>}
 * tag as an instance of some sort of component container. Instead, all the
 * children components under the {@code DesignLayer} tag will be hoisted to be
 * the direct children of the {@code DesignLayer}'s closest non-DesignLayer
 * parent.
 * <p>
 * For example, given the MXML AST like the following:
 * 
 * <pre>
 * HGroup
 * {
 *     Label l1     
 *     DesignLayer
 *     {
 *         Button b1
 *         Button b2
 *     }
 *     Label l2
 * }
 * </pre>
 * 
 * The MXML code generator will make Button "b1" and "b2" children of the
 * HGroup, which means despite the AST shape, "b1" and "b2" will actually become
 * siblings of "l1" and "l2" at runtime.
 * 
 * @see <a href="http://opensource.adobe.com/wiki/display/flexsdk/Runtime+Design+Layers"></a>
 */
public interface IMXMLDesignLayerNode extends IMXMLInstanceNode
{
    /**
     * Flatten {@code <fx:DesignLayer>} tags by hoisting direct children of a
     * "DesignLayer" tag. For example:
     * 
     * <pre>
     * Group
     *     Button #1
     *     DesignLayer #1
     *         Label #1
     *         Label #2
     *     Button #2
     * </pre> {@code DesignLayer #1}'s hoisted child count is 2.
     * <p>
     * 
     * <pre>
     * Group
     *     DesignLayer #1
     *         Label #1
     *         DesignLayer #2
     *             Label #2
     *             Label #3
     *         Label #4
     *         DesignLayer #3
     *             HGroup
     *                 Label #5
     * </pre>
     * 
     * {@code DesignLayer #1}'s hoisted child count is 5. They are:
     * 
     * <pre>
     * Label #1
     * Label #2
     * Label #3
     * Label #4
     * HGroup
     * </pre>
     * 
     * Note that {@code Label #5} is a child of {@code HGroup}. It is not
     * hoisted up to the {@code Group} level.
     * 
     */
    int getHoistedChildCount();

    /**
     * If a {@code <fx:DesignLayer>} tag has no attributes or property child
     * nodes, it will be ignored during code generation.
     * 
     * @return True if this {@code <fx:DesignLayer>} node will be included in
     * the code generation.
     */
    boolean skipCodeGeneration();
}
