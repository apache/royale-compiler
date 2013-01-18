goog.provide('org.apache.flex.A');

goog.require('flash.events.IEventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {spark.components.Button}
 * @implements {flash.events.IEventDispatcher}
 */
org.apache.flex.A = function() {
	goog.base(this);
	trace(typeof("a"));
}
goog.inherits(org.apache.flex.A, spark.components.Button);

/**
 * @private
 * @type {ArgumentError}
 */
org.apache.flex.A.prototype._a = new ArgumentError();