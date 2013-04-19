goog.provide('models.MyModel');

/**
 * @constructor
 * @extends {org.apache.flex.events.EventDispatcher}
 */
models.MyModel = function() {
	goog.base(this);
}
goog.inherits(models.MyModel, org.apache.flex.events.EventDispatcher);

/**
 * @private
 * @type {string}
 */
models.MyModel.prototype._labelText;

/**
 * @expose
 * @return {string}
 */
models.MyModel.prototype.get_labelText = function() {
	return this._labelText;
};

/**
 * @expose
 * @param {string} value
 */
models.MyModel.prototype.set_labelText = function(value) {
	if (value != this._labelText) {
		this._labelText = value;
		this.dispatchEvent(new org.apache.flex.events.Event("labelTextChanged"));
	}
};

/**
 * @private
 * @type {Array}
 */
models.MyModel.prototype._strings = ["AAPL", "ADBE", "GOOG", "MSFT", "YHOO"];

/**
 * @expose
 * @return {Array}
 */
models.MyModel.prototype.get_strings = function() {
	return this._strings;
};

/**
 * @private
 * @type {Array}
 */
models.MyModel.prototype._cities = ["London", "Miami", "Paris", "Sydney", "Tokyo"];

/**
 * @expose
 * @return {Array}
 */
models.MyModel.prototype.get_cities = function() {
	return this._cities;
};