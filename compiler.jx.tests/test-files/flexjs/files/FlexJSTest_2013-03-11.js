goog.provide('FlexJSTest');

goog.require('MyInitialView');
goog.require('StockDataJSONItemConverter');
goog.require('controllers.MyController');
goog.require('models.MyModel');
goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.core.SimpleCSSValuesImpl');
goog.require('org.apache.flex.net.HTTPService');
goog.require('org.apache.flex.net.JSONInputParser');
goog.require('org.apache.flex.net.dataConverters.LazyCollection');

/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
FlexJSTest = function() {
    org.apache.flex.core.Application.call(this);

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
     * @type {org.apache.flex.net.JSONInputParser}
     */
    this.$ID6;

    /**
     * @private
     * @type {StockDataJSONItemConverter}
     */
    this.$ID7;

    /**
     * @private
     * @type {org.apache.flex.net.dataConverters.LazyCollection}
     */
    this.collection;

    /**
     * @private
     * @type {org.apache.flex.net.HTTPService}
     */
    this.service;

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
goog.inherits(FlexJSTest, org.apache.flex.core.Application);



/**
 * @this {FlexJSTest}
 * @expose
 * @param {flash.events.Event} event
 */
FlexJSTest.prototype.$EH0 = function(event)
{
        this.model /* Cast to models.MyModel */.set_labelText("Hello World");
};

/**
 * @this {FlexJSTest}
 * @return {LazyCollection}
 */
FlexJSTest.prototype.get_collection = function()
{
    return this.collection;
};

/**
 * @this {FlexJSTest}
 * @param {LazyCollection} value
 */
FlexJSTest.prototype.set_collection = function(value)
{
    if (value != this.collection)
        this.collection = value;
};

/**
 * @this {FlexJSTest}
 * @return {HTTPService}
 */
FlexJSTest.prototype.get_service = function()
{
    return this.service;
};

/**
 * @this {FlexJSTest}
 * @param {HTTPService} value
 */
FlexJSTest.prototype.set_service = function(value)
{
    if (value != this.service)
        this.service = value;
};

/**
 * @override
 * @this {FlexJSTest}
 * @return {Array} the Array of UI element descriptors.
 */
FlexJSTest.prototype.get_MXMLDescriptor = function()
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
 * @this {FlexJSTest}
 * @return {Array} the Array of UI element descriptors.
 */
FlexJSTest.prototype.get_MXMLProperties = function()
{
    if (this.mxmldp == undefined)
    {
         /** @type {Array} */
         var arr = goog.base(this, 'get_MXMLProperties');
         /** @type {Array} */
         var data = [
5,
'model',
false, [models.MyModel, 1, '_id', true, '$ID2', 0, 0, null],
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
null, [org.apache.flex.net.HTTPService, 2, 'id', true, 'service', 'beads', null, [org.apache.flex.net.dataConverters.LazyCollection, 3, 'inputParser', false, [org.apache.flex.net.JSONInputParser, 1, '_id', true, '$ID6', 0, 0, null], 'itemConverter', false, [StockDataJSONItemConverter, 1, '_id', true, '$ID7', 0, 0, null], 'id', true, 'collection', 0, 0, null], 0, 0, null],
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

