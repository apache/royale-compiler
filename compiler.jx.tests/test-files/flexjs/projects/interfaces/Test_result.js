/**
 * Test
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('Test');

goog.require('classes.A');
goog.require('interfaces.IA');
goog.require('interfaces.IC');
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
  var /** @type {interfaces.IA} */ ia = org.apache.flex.utils.Language.as(this.doSomething(interfaces.IC), interfaces.IA);
};
goog.inherits(Test, classes.A);


/**
 * @expose
 * @param {interfaces.IC} ic
 * @return {interfaces.IC}
 */
Test.prototype.doSomething = function(ic) {
  return ic;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Test.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Test', qName: 'Test'}], interfaces: [interfaces.IA, interfaces.IE] };
