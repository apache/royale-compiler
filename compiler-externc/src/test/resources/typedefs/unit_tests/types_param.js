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

 
/**
 * @const
 */
var foo = {};

/**
 * @const
 */
foo.bar = {};

 /**
  * @constructor
  */
foo.bar.Baz = function () {};
 
 /**
  * @constructor
  */
function Foo () {}

/**
 * @param {string} arg1
 */
Foo.test1 = function (arg1) {};

/**
 * @param {foo.bar.Baz} arg1
 */
Foo.test2 = function (arg1) {};

/**
 * @param {{myNum: number, myObject}} arg1
 */
Foo.test3 = function (arg1) {};

/**
 * @param {?number} arg1
 */
Foo.test4 = function (arg1) {};

/**
 * @param {!Object} arg1
 */
Foo.test5 = function (arg1) {};

/**
 * @param {function(string, boolean)} arg1
 */
Foo.test6 = function (arg1) {};













