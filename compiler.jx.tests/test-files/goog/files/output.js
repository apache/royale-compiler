goog.provide('org.apache.flex.A');

goog.require('flash.events.IEventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {spark.components.Button}
 * @implements {flash.events.IEventDispatcher}
 */
org.apache.flex.A = function() {
	var self = this;
	goog.base(this);
	self.trace(typeof("a"));
}
goog.inherits(org.apache.flex.A, spark.components.Button);

/**
 * @const
 * @type {string}
 */
org.apache.flex.A.MY_CLASS_CONST = "myClassConst";

/**
 * @private
 * @type {ArgumentError}
 */
org.apache.flex.A.prototype._a = new ArgumentError();

/**
 * @const
 * @type {string}
 */
org.apache.flex.A.prototype.MY_INSTANCE_CONST = "myInstanceConst";