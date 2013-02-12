goog.provide('org.apache.flex.A');

/**
 * @constructor
 */
org.apache.flex.A = function() {
};

/**
 * @private
 * @type {number}
 */
org.apache.flex.A.prototype._a = -1;

/**
 * @type {number}
 */
org.apache.flex.A.prototype.a;

Object.defineProperty(
	org.apache.flex.A.prototype, 
	'a', 
	{get:function() {
		var self = this;
		return -1;
	}, configurable:true}
);

Object.defineProperty(
	org.apache.flex.A.prototype, 
	'a', 
	{set:function(value) {
		var self = this;
		self._a = value;
	}, configurable:true}
);