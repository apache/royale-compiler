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

package org.apache.royale.compiler.internal.parsing;

import java.util.ArrayList;

import org.apache.royale.compiler.parsing.ICMToken;

public class FilteringList<T extends ICMToken> extends ArrayList<T>
{
    private static final long serialVersionUID = 1L;

    public FilteringList(ITokenStreamFilter filter)
    {
        super(8012);
        this.filter = filter;
    }

    public FilteringList(ITokenStreamFilter filter, int size)
    {
        super(size);
        this.filter = filter;
    }

    private ITokenStreamFilter filter;

    @Override
    public boolean add(T o)
    {
        if (filter != null)
        {
            if (filter.accept(o))
                return super.add(o);
        }
        else
        {
            return super.add(o);
        }
        return false;
    }

    @Override
    public T remove(int index)
    {
        return null;
    }

    @Override
    public boolean remove(Object o)
    {
        return false;
    }
}
