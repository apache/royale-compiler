goog.provide('LocalFunction');

/**
 * @constructor
 */
LocalFunction = function() {
};

/**
 * @private
 * @type {string}
 */
LocalFunction.prototype.myMemberProperty = "got it: ";

/**
 * @private
 * @param {number} value
 */
LocalFunction.prototype.myMemberMethod = function(value) {
	function myLocalFunction(value) {
		return this.myMemberProperty + value;
	};
	org.apache.flex.utils.Language.trace("WOW! :: " + goog.bind(myLocalFunction, this)(value + 42));
};

/**
 * @expose
 */
LocalFunction.prototype.doIt = function() {
	this.myMemberMethod(624);
};