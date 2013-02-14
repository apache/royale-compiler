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

package com.acme {
public class A implements I {
  
  public function A(msg:String) {
    this.msg = msg;
  }

  private var _msg:int;

  public function get msg():String {
    return String(this._msg);
  }

  trace("Class A is initialized!");

  public function set msg(value:String):void {
    this._msg = parseInt(value, 10);
  }

  private function secret(n) {
    return msg + n;
  }

  public function foo(x) {
    return this.secret(A.bar(x));
  }

  public function baz() {
    var tmp = this.secret;
    return tmp("-bound");
  }

  public static function bar(x) {
    return x + 1;
  }

}
}
