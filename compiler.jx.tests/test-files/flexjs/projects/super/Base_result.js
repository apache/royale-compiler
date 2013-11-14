goog.provide('Base');

goog.require('Super');
goog.require('org.apache.flex.utils.Language');



/**
 * @constructor
 * @extends {Super}
 */
Base = function() {
  goog.base(this);
};
goog.inherits(Base, Super);


/**
 * @expose
 * @return {string}
 * @override
 */
Base.prototype.get_text = function() {
  return "A" + goog.base(this, "get_text");
};


/**
 * @expose
 * @param {string} value
 * @override
 */
Base.prototype.set_text = function(value) {
  if (value != goog.base(this, "get_text")) {
    goog.base(this, "set_text", "B" + value);
  }
};
