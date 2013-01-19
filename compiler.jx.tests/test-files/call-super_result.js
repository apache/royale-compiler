goog.provide('org.apache.flex.A');

goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {spark.components.Button}
 */
org.apache.flex.A = function() {
	goog.base(this);
}
goog.inherits(org.apache.flex.A, spark.components.Button);

/**
 * @param {string} a
 * @param {number} b
 */
org.apache.flex.A.prototype.hasSuperCall = function(a, b) {
	goog.base(this, 'hasSuperCall', a, b, 100);
	var /** @type {string} */ result = myRegularFunctionCall(-1);
};