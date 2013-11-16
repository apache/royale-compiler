/**
 * wildcard_import
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('wildcard_import');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.html.staticControls.Button');



/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
wildcard_import = function() {
  goog.base(this);
  
  /**
   * @private
   * @type {Array}
   */
  this.mxmldd;
  
  /**
   * @private
   * @type {Array}
   */
  this.mxmldp;
};
goog.inherits(wildcard_import, org.apache.flex.core.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
wildcard_import.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'wildcard_import', qName: 'wildcard_import' }] };


/**
 * @private
 */
wildcard_import.prototype.tmp = function() {
  var /** @type {Button} */ myButton;
  myButton = new org.apache.flex.html.staticControls.Button();
};



