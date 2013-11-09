goog.provide('StockDataJSONItemConverter');

goog.require('org.apache.flex.net.JSONItemConverter');

/**
 * @constructor
 * @extends {org.apache.flex.net.JSONItemConverter}
 */
StockDataJSONItemConverter = function() {
	goog.base(this);
}
goog.inherits(StockDataJSONItemConverter, org.apache.flex.net.JSONItemConverter);

/**
 * @expose
 * @param {string} data
 * @return {Object}
 * @override
 */
StockDataJSONItemConverter.prototype.convertItem = function(data) {
	var /** @type {Object} */ obj = goog.base(this, 'convertItem', data);
	if (obj["query"]["count"] == 0)
		return "No Data";
	obj = obj["query"]["results"]["quote"];
	return obj;
};