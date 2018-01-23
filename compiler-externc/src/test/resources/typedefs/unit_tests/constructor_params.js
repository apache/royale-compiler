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
 * A constructor with no args.
 *
 * @constructor
 */
function FooNoArgs() {}

/**
 * A constructor with arg and opt arg.
 *
 * @constructor
 * @param {number} arg1
 * @param {*=} opt_arg2
 */
function FooOptArgs(arg1, opt_arg2) {}

/**
 * A constructor with arg and var args.
 *
 * @constructor
 * @param {number} arg1
 * @param {...*} var_args
 */
function FooVarArgs(arg1, var_args) {}

/**
 * A constructor with arg, opt arg and var args.
 *
 * @constructor
 * @param {number} arg1 The arg 1.
 * @param {*=} opt_arg2 The arg  that is
 * wrapped by another
 * line in the comment.
 * @param {...*} var_args A var agr param.
 * @see http://foo.bar.com
 * @returns {FooVarArgs} Another instance.
 */
function FooOptVarArgs(arg1, opt_arg2, var_args) {}

/**
 * A constructor with no args.
 *
 * @constructor
 */
// this pattern results in warnings and may no longer be allowed by Closure Compiler
// AssignFooNoArgs = function () {};

/**
 * A constructor with no args.
 *
 * @constructor
 */
var VarAssignFooNoArgs = function () {};

/**
 * @const
 */
var FinalClass = {};

/**
 * A static method.
 */
FinalClass.bar = function () {};
