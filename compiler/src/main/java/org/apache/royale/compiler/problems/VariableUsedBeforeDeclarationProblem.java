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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  Error if we can detect a variable is used before its declaration.
 *  var foo = bar;
 *  var bar = 0;
 *  
 *  This check is needed when there is also a bar property on the class
 *  containing the method with the error.
 *  
 *  private var bar:int = 0;
 *  public function baz()
 *  {
 *      var foo:int = bar;
 *      var bar:int = 1;
 *  }
 *  
 *  In the code above, it appears that SWF code will set foo to this.bar.
 *  I think if a findProp is generated it might know the local variable
 *  is not in scope?
 *  But in JS code, foo is set to undefined because at least Safari
 *  will reference the local variable before it has been initialized.
 *  
 *  So, we will generate an error since this code will not work in
 *  the browser.
 */
public final class VariableUsedBeforeDeclarationProblem extends SemanticProblem
{
    public static final String DESCRIPTION =
        "Variable ${declName} used before declaration.";

    public static final int errorCode = 1555;

    public VariableUsedBeforeDeclarationProblem(IASNode site, String declName)
    {
        super(site);
        this.declName = declName;
    }
    
    public final String declName;
}
