/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * FlexJSTest_again
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('FlexJSTest_again');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.core.SimpleCSSValuesImpl');
goog.require('MyInitialView');
goog.require('models.MyModel');
goog.require('controllers.MyController');
goog.require('org.apache.flex.net.HTTPService');
goog.require('org.apache.flex.net.dataConverters.LazyCollection');
goog.require('org.apache.flex.net.JSONInputParser');
goog.require('StockDataJSONItemConverter');
goog.require('org.apache.flex.events.Event');




/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
FlexJSTest_again = function() {
  FlexJSTest_again.base(this, 'constructor');
  
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

  this.generateMXMLAttributes
  ([5,
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
  ]);
  
};
goog.inherits(FlexJSTest_again, org.apache.flex.core.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
FlexJSTest_again.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'FlexJSTest_again', qName: 'FlexJSTest_again' }] };


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
FlexJSTest_again.prototype.$EH0 = function(event)
{
  org.apache.flex.utils.Language.as(this.model, models.MyModel, true).set_labelText('Hello World');
};


/**
 * @expose
 * @return {org.apache.flex.net.HTTPService}
 */
FlexJSTest_again.prototype.get_service = function()
{
  return this.service;
};


/**
 * @expose
 * @param {org.apache.flex.net.HTTPService} value
 */
FlexJSTest_again.prototype.set_service = function(value)
{
  if (value != this.service) {
    this.service = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'service', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.net.dataConverters.LazyCollection}
 */
FlexJSTest_again.prototype.get_collection = function()
{
  return this.collection;
};


/**
 * @expose
 * @param {org.apache.flex.net.dataConverters.LazyCollection} value
 */
FlexJSTest_again.prototype.set_collection = function(value)
{
  if (value != this.collection) {
    this.collection = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'collection', null, value));
  }
};



