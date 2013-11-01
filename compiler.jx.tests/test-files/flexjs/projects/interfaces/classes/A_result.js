goog.provide('classes.A');

goog.require('classes.C');

/**
 * @constructor
 * @extends {classes.C}
 */
classes.A = function() {
	goog.base(this);
}
goog.inherits(classes.A, classes.C);