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

// this is a subset of the one in royale-typedefs/js.  It is only used to build a test version

// webkit_notifications
/**
 * @constructor
 */
function ExtendableEvent() {}

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
* @constructor
*/
function dialogPolyfill() {};

/**
* @param {!Element} element to upgrade, if necessary
*/
dialogPolyfill.registerDialog = function(element) {};

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
 * @export
 * This gets mapped to org.apache.royale.utils.Language.trace() by the compiler
 * @param {...} rest
 */
function trace(rest) {}

/**
 * @type {!Console}
 * @const
 */
var console;


/**
 * @type {number}
 * @const
 */
Array.CASEINSENSITIVE;

/**
 * @type {number}
 * @const
 */
Array.DESCENDING;

/**
 * @type {number}
 * @const
 */
Array.UNIQUESORT;

/**
 * @type {number}
 * @const
 */
Array.RETURNINDEXEDARRAY;

/**
 * @type {number}
 * @const
 */
Array.NUMERIC;


/**
 * @param {number} index The index.
 * @param {Object} element The Object.
 */
Array.prototype.insertAt = function(index, element) {};

/**
 * @param {number} index The index.
 */
Array.prototype.removeAt = function(index) {};

/**
 * @param {Object} fieldName The field name or array of field names.
 * @param {Object=} opt_options The bitmask of options.
 * @return {Array} The sorted Array.
 */
Array.prototype.sortOn = function(fieldName, opt_options) {};


/**
 * @type {number}
 * @const
 */
int.MAX_VALUE;


/**
 * @type {number}
 * @const
 */
int.MIN_VALUE;


/**
 * @type {number}
 * @const
 */
uint.MAX_VALUE;


/**
 * @type {number}
 * @const
 */
uint.MIN_VALUE;

// additions to the Date prototype to allow AS code to use these properties

/**
 * @type {number}
 */
Date.prototype.date;

/**
 * @type {number}
 */
Date.prototype.dateUTC;

/**
 * @type {number}
 */
Date.prototype.day;

/**
 * @type {number}
 */
Date.prototype.dayUTC;

/**
 * @type {number}
 */
Date.prototype.fullYear;

/**
 * @type {number}
 */
Date.prototype.fullYearUTC;

/**
 * @type {number}
 */
Date.prototype.hours;

/**
 * @type {number}
 */
Date.prototype.hoursUTC;

/**
 * @type {number}
 */
Date.prototype.milliseconds;

/**
 * @type {number}
 */
Date.prototype.millisecondsUTC;

/**
 * @type {number}
 */
Date.prototype.minutes;

/**
 * @type {number}
 */
Date.prototype.minutesUTC;

/**
 * @type {number}
 */
Date.prototype.month;

/**
 * @type {number}
 */
Date.prototype.monthUTC;

/**
 * @type {number}
 */
Date.prototype.seconds;

/**
 * @type {number}
 */
Date.prototype.secondsUTC;

/**
 * @type {number}
 */
Date.prototype.time;

/**
 * @type {number}
 */
Date.prototype.timezoneOffset;

/**
 * @param {string} type
 * @param {EventListener|function(!Event):*} listener
 * @param {(boolean|!AddEventListenerOptions)=} opt_options
 * @return {undefined}
 * @see https://dom.spec.whatwg.org/#dom-eventtarget-addeventlistener
 */
BaseAudioContext.prototype.addEventListener = function(type, listener, opt_options) {
};

/**
 * @param {string} type
 * @param {EventListener|function(!Event):*} listener
 * @param {(boolean|!EventListenerOptions)=} opt_options
 * @return {undefined}
 * @see https://dom.spec.whatwg.org/#dom-eventtarget-removeeventlistener
 */
BaseAudioContext.prototype.removeEventListener = function(
    type, listener, opt_options) {};
	
/**
 * @param {!Event} evt
 * @return {boolean}
 * @see https://dom.spec.whatwg.org/#dom-eventtarget-dispatchevent
 */
BaseAudioContext.prototype.dispatchEvent = function(evt) {};




