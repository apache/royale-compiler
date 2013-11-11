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
 * @private
 */
wildcard_import.prototype.tmp = function() {
  var /** @type {Button} */ myButton;
  myButton = new org.apache.flex.html.staticControls.Button();
};
