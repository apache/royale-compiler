goog.provide('org.apache.flex.A');

goog.require('flash.events.EventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {flash.events.EventDispatcher}
 */
org.apache.flex.A = function() {
	var self = this;
	goog.base(this);
	self.init();
}
goog.inherits(org.apache.flex.A, flash.events.EventDispatcher);

/**
 * @private
 * @type {spark.components.Button}
 */
org.apache.flex.A.prototype._privateVar;

org.apache.flex.A.prototype.init = function() {
	var self = this;
	var /** @type {spark.components.Button} */ btn = new spark.components.Button();
	self._privateVar = new spark.components.Button();
	self.addEventListener("click", function() {
	});
};

org.apache.flex.A.prototype.start = function() {
	var self = this;
	var /** @type {string} */ localVar = self._privateVar.label;
	self.init();
	doIt();
};