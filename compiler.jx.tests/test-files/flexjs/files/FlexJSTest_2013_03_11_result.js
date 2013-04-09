goog.provide('FlexJSTest_2013_03_11');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.core.SimpleCSSValuesImpl');
goog.require('MyInitialView');
goog.require('models.MyModel');
goog.require('controllers.MyController');
goog.require('org.apache.flex.net.HTTPService');
goog.require('org.apache.flex.net.dataConverters.LazyCollection');
goog.require('org.apache.flex.net.JSONInputParser');
goog.require('StockDataJSONItemConverter');

/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
FlexJSTest_2013_03_11 = function() {
	goog.base(this);
	
	/**
	 * @private
	 * @type {org.apache.flex.core.SimpleCSSValuesImpl}
	 */
	this.$ID0;
	
	/**
	 * @private
	 * @type {MyInitialView}
	 */
	this.$ID1;
	
	/**
	 * @private
	 * @type {models.MyModel}
	 */
	this.$ID2;
	
	/**
	 * @private
	 * @type {controllers.MyController}
	 */
	this.$ID3;
	
	/**
	 * @private
	 * @type {org.apache.flex.net.HTTPService}
	 */
	this.service;
	
	/**
	 * @private
	 * @type {org.apache.flex.net.dataConverters.LazyCollection}
	 */
	this.collection;
	
	/**
	 * @private
	 * @type {org.apache.flex.net.JSONInputParser}
	 */
	this.$ID4;
	
	/**
	 * @private
	 * @type {StockDataJSONItemConverter}
	 */
	this.$ID5;
	
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
goog.inherits(FlexJSTest_2013_03_11, org.apache.flex.core.Application);

/**
 * @this {FlexJSTest_2013_03_11}
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
FlexJSTest_2013_03_11.prototype.$EH0 = function(event)
{
	var self = this;
	this.model/** Cast to models.MyModel */.set_labelText('Hello World');
};

/**
 * @this {FlexJSTest_2013_03_11}
 * @return {org.apache.flex.net.HTTPService}
 */
FlexJSTest_2013_03_11.prototype.get_service = function()
{
	return this.service;
};

/**
 * @this {FlexJSTest_2013_03_11}
 * @param {org.apache.flex.net.HTTPService} value
 */
FlexJSTest_2013_03_11.prototype.set_service = function(value)
{
	if (value != this.service)
		this.service = value;
};

/**
 * @this {FlexJSTest_2013_03_11}
 * @return {org.apache.flex.net.dataConverters.LazyCollection}
 */
FlexJSTest_2013_03_11.prototype.get_collection = function()
{
	return this.collection;
};

/**
 * @this {FlexJSTest_2013_03_11}
 * @param {org.apache.flex.net.dataConverters.LazyCollection} value
 */
FlexJSTest_2013_03_11.prototype.set_collection = function(value)
{
	if (value != this.collection)
		this.collection = value;
};

/**
 * @override
 * @this {FlexJSTest_2013_03_11}
 * @return {Array} the Array of UI element descriptors.
 */
FlexJSTest_2013_03_11.prototype.get_MXMLDescriptor = function()
{
	if (this.mxmldd == undefined)
	{
		/** @type {Array} */
		var arr = goog.base(this, 'get_MXMLDescriptor');
		/** @type {Array} */
		var data = [
];
	
		if (arr)
			this.mxmldd = arr.concat(data);
		else
			this.mxmldd = data;
	}
	return this.mxmldd;
};

/**
 * @override
 * @this {FlexJSTest_2013_03_11}
 * @return {Array} the Array of UI element descriptors.
 */
FlexJSTest_2013_03_11.prototype.get_MXMLProperties = function()
{
	if (this.mxmldp == undefined)
	{
		/** @type {Array} */
		var arr = goog.base(this, 'get_MXMLProperties');
		/** @type {Array} */
		var data = [
5,
'model',
false,
[models.MyModel, 1, '_id', true, '$ID2', 0, 0, null],
'valuesImpl',
false,
[org.apache.flex.core.SimpleCSSValuesImpl, 1, '_id', true, '$ID0', 0, 0, null],
'initialView',
false,
[MyInitialView, 1, '_id', true, '$ID1', 0, 0, null],
'controller',
false,
[controllers.MyController, 1, '_id', true, '$ID3', 0, 0, null],
'beads',
null, [org.apache.flex.net.HTTPService, 2, 'id', true, 'service', 'beads', null, [org.apache.flex.net.dataConverters.LazyCollection, 3, 'id', true, 'collection', 'inputParser', false, [org.apache.flex.net.JSONInputParser, 1, '_id', true, '$ID4', 0, 0, null], 'itemConverter', false, [StockDataJSONItemConverter, 1, '_id', true, '$ID5', 0, 0, null], 0, 0, null], 0, 0, null],
0,
1,
'initialize',
this.$EH0
];
	
		if (arr)
			this.mxmldp = arr.concat(data);
		else
			this.mxmldp = data;
	}
	return this.mxmldp;
};

