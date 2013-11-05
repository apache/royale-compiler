goog.provide('Test');

goog.require('classes.A');

goog.require('interfaces.IA');
goog.require('interfaces.IE');

goog.require('org.apache.flex.utils.Language');

/**
 * @constructor
 * @extends {classes.A}
 * @implements {interfaces.IA}
 * @implements {interfaces.IE}
 */
Test = function() {
	goog.base(this);
}
goog.inherits(Test, classes.A);

/**
 * @const
 */
Test.prototype.AFJS_INTERFACES = [interfaces.IA, interfaces.IE];
