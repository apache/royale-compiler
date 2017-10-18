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

package {

import com.acme.A;
import com.acme.B;
import com.acme.I;
import com.acme.sub.IOther;
import com.acme.sub.ISub;

//noinspection JSUnusedGlobalSymbols
public class HelloWorld {

  //noinspection JSUnusedGlobalSymbols
  public function HelloWorld() {
    trace(B.now);
    trace(B.nowPlusOne());

    var b:B = new B('hello ');
    trace("b = new B('hello '):", b);
    trace("b.foo(3):", b.foo(3));
    trace("b.baz():", b.baz());
    trace("b is A:", b is A);
    trace("b is B:", b is B);
    trace("b is I:", b is I);
    trace("b is ISub:", b is ISub);
    trace("b is IOther:", b is IOther);

    var a:A = new A('123');
    trace("a = new A('123'):", a);
    trace("a is A:", a is A);
    trace("a is B:", a is B);
    trace("a is I:", a is I);
    trace("a is ISub:", a is ISub);
    trace("a is IOther:", a is IOther);
  }
}
}
