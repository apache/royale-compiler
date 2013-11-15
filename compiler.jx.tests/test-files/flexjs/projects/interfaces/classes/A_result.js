goog.provide('classes.A');

goog.require('classes.C');



/**
 * @constructor
 * @extends {classes.C}
 */
classes.A = function() {
  goog.base(this);
};
goog.inherits(classes.A, classes.C);


/**
 * @const
 * @type {Object.<string, Array.<Object>>}
 */
classes.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'classes.A'}] };
