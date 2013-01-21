goog.provide('org.apache.flex.A');

goog.require('flash.events.IEventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {spark.components.Button}
 * @implements {flash.events.IEventDispatcher}
 * @param {string} z
 */
org.apache.flex.A = function(z) {
	goog.base(this, z);
}
goog.inherits(org.apache.flex.A, spark.components.Button);

/**
 * @param {string} a
 * @param {number} b
 * @return {string}
 */
org.apache.flex.A.prototype.hasSuperCall = function(a, b) {
	goog.base(this, 'hasSuperCall', a, b, 100);
	var /** @type {string} */ result = myRegularFunctionCall(-1);
	return result;
};