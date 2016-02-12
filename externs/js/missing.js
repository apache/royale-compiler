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
 * @type {Object}
 */
Object.prototype;

/**
 * @type {Object}
 */
Object.prototype.prototype;

/**
 * @constructor
 * @extends {Function}
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


/**
 * @export
 * This gets mapped to org.apache.flex.utils.Language.trace() by the compiler
 * @param {...} rest
 */
function trace(rest) {}

/**
 * @type {!Console}
 * @const
 */
var console;