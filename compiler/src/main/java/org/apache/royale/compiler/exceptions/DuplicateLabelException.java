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

package org.apache.royale.compiler.exceptions;

/**
 *  Checked exception thrown by the control-flow management
 *  logic when a new label duplicates an active label.
 *  The control-flow manager doesn't have access to the syntactic elements
 *  that will make a good diagnostic, so callers catch this exception
 *  and emit a diagnostic in its syntactic context.
 */
public class DuplicateLabelException extends Exception
{
    private static final long serialVersionUID = -3064893050053426634L;
    
    public DuplicateLabelException(Object criterion)
    {
        this.criterion = criterion;
    }
    
    public Object criterion;
}

