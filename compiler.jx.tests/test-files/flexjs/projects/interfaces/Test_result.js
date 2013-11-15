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
};
goog.inherits(Test, classes.A);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Test.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Test', qName: 'Test'}], interfaces: [interfaces.IA, interfaces.IE] };
