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
 * @constructor
 */
function ImportMethodSignature() {};

/**
 * @param {foo.Quux|foo.Bar} bar
 * @param {number} value
 * @param {foo.Baz?} baz
 */
ImportMethodSignature.myMethod = function(bar, value, baz) {};

/**
 * @param {foo.Bar} bar
 * @param {number} value
 * @param {foo.Baz?} baz
 * @return {foo.Qux}
 */
ImportMethodSignature.prototype.myMethodWithReturnType = function(bar, value, baz) {};

/**
 * @param {foo.Bar} bar
 * @param {number} value
 * @param {foo.Baz?} baz
 * @return {foo.Quuux|foo.Bar}
 */
ImportMethodSignature.myMethodWithUnionReturnType = function(bar, value, baz) {};

/**
 * @const
 */
var foo = {};

/**
 * @constructor
 */
foo.Bar = function() {};

/**
 * @constructor
 */
foo.Baz = function() {};

/**
 * @constructor
 */
foo.Qux = function() {};

/**
 * @constructor
 */
foo.Quux = function() {};

/**
 * @constructor
 */
foo.Quuux = function() {};
