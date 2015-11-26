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

 
// webkit_notifications
/**
 * @constructor
 */
function ExtendableEvent() {}

// HACK @enum page_visibility
/**
 * @constructor
 */
function VisibilityState(description) {}


// chrome.js
/**
 * @constructor
 */
function HTMLEmbedElement() {};

/**
 * @type {!Window}
 * @const
 */
var window;

/**
 * @type {!HTMLDocument}
 */
Window.prototype.document;

/**
 * @constructor
 */
function Promise() {}

/**
 * @constructor
 */
function FontFaceSetLoadStatus() {}

/**
 * @constructor
 */
function FontFaceLoadStatus() {}

/**
 * @constructor
 */
function Navigator() {}


/**
 * @constructor
 */
function Screen() {}

/**
 * @constructor
 */
function uint() {}

/**
 * @param {number=} opt_radix Optional radix.
 * @return {string} The result.
 */
uint.prototype.toString = function(opt_radix) {}

/**
 * @constructor
 */
function int() {}

/**
 * @param {number=} opt_radix Optional radix.
 * @return {string} The result.
 */
int.prototype.toString = function(opt_radix) {}


/**
 * @constructor
 */
function Class() {}

/**
 * @constructor
 */
function JSON() {}

/**
 * @param {string} s The input.
 * @param {function=} opt_reviver Optional reviver.
 * @return {Object} The result.
 */
JSON.parse = function(s, opt_reviver) {}

/**
 * @param {Object} obj The input.
 * @param {function=} opt_replacer Optional reviver.
 * @param {string|number=} opt_space Optional space.
 * @return {string} The result.
 */
JSON.stringify = function(obj, opt_replacer, opt_space) {}

// gecko

/**
 * @constructor
 */
function History() {}

/**
 * @constructor
 */
function Location() {}

/**
 * @type {number}
 */
XMLHttpRequest.prototype.timeout;


/***** hack ****/
/* below are copies from es3.js, which is:
   Copyright 2008 The Closure Compiler Authors
   
   es3.js includes Mozilla-only static versions
   of these methods which confuses the externs compiler.  The externs compiler
   currently doesn't expect a class to have a static and instance method of the
   same name.  Last definition found wins so by re-declaring here the instance
   methods win out */
/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {T} obj
 * @param {number=} opt_fromIndex
 * @return {number}
 * @this {{length: number}|Array.<T>|string}
 * @nosideeffects
 * @template T
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/indexOf
 */
Array.prototype.indexOf = function(obj, opt_fromIndex) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {T} obj
 * @param {number=} opt_fromIndex
 * @return {number}
 * @this {{length: number}|Array.<T>|string}
 * @nosideeffects
 * @template T
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/lastIndexOf
 */
Array.prototype.lastIndexOf = function(obj, opt_fromIndex) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {?function(this:S, T, number, !Array.<T>): ?} callback
 * @param {S=} opt_thisobj
 * @return {boolean}
 * @this {{length: number}|Array.<T>|string}
 * @template T,S
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/every
 */
Array.prototype.every = function(callback, opt_thisobj) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {?function(this:S, T, number, !Array.<T>): ?} callback
 * @param {S=} opt_thisobj
 * @return {!Array.<T>}
 * @this {{length: number}|Array.<T>|string}
 * @template T,S
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/filter
 */
Array.prototype.filter = function(callback, opt_thisobj) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {?function(this:S, T, number, !Array.<T>): ?} callback
 * @param {S=} opt_thisobj
 * @this {{length: number}|Array.<T>|string}
 * @template T,S
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/forEach
 */
Array.prototype.forEach = function(callback, opt_thisobj) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {?function(this:S, T, number, !Array.<T>): R} callback
 * @param {S=} opt_thisobj
 * @return {!Array.<R>}
 * @this {{length: number}|Array.<T>|string}
 * @template T,S,R
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/map
 */
Array.prototype.map = function(callback, opt_thisobj) {};

/**
 * Available in ECMAScript 5, Mozilla 1.6+.
 * @param {?function(this:S, T, number, !Array.<T>): ?} callback
 * @param {S=} opt_thisobj
 * @return {boolean}
 * @this {{length: number}|Array.<T>|string}
 * @template T,S
 * @see http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/some
 */
Array.prototype.some = function(callback, opt_thisobj) {};

/**** end hack **/