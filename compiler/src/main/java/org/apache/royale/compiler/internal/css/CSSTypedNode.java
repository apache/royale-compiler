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

package org.apache.royale.compiler.internal.css;

import org.apache.royale.compiler.css.ICSSNode;
import com.google.common.collect.ImmutableList;

/**
 * An "imaginary" CSS node with a node type. This is used to aggregate a group
 * of nodes.
 */
public class CSSTypedNode extends CSSNodeBase
{

    protected CSSTypedNode(final CSSModelTreeType type, final ImmutableList<? extends ICSSNode> childNodes)
    {
        super(null, null, type);
        if (childNodes != null)
            children.addAll(childNodes);
    }

}
