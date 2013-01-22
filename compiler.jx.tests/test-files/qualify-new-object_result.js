goog.provide('org.apache.flex.A');

goog.require('flash.events.EventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {flash.events.EventDispatcher}
 */
org.apache.flex.A = function() {
	goog.base(this);
}
goog.inherits(org.apache.flex.A, flash.events.EventDispatcher);

org.apache.flex.A.prototype.init = function() {
	var /** @type {spark.components.Button} */ btn = new spark.components.Button();
	this.addEventListener("click", function() {
	});
};