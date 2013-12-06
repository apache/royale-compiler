/**
 * interfaces.IE
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('interfaces.IE');



/**
 * @interface
 */
interfaces.IE = function() {
};


interfaces.IE.prototype.myMethod = function() {};


/**
 * @return {string}
 */
interfaces.IE.prototype.get_myProp = function() {};


/**
 * @param {string} value
 */
interfaces.IE.prototype.set_myProp = function(value) {};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
interfaces.IE.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'IE', qName: 'interfaces.IE'}] };
