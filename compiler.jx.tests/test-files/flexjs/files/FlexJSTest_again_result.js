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
 * @suppress {checkTypes|accessControls}
 */

goog.provide('FlexJSTest_again');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.core.SimpleCSSValuesImpl');
goog.require('MyInitialView');
goog.require('models.MyModel');
goog.require('controllers.MyController');
goog.require('org.apache.flex.net.HTTPService');
goog.require('org.apache.flex.collections.LazyCollection');
goog.require('org.apache.flex.collections.parsers.JSONInputParser');
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
  this.$ID0_;
  
  /**
   * @private
   * @type {MyInitialView}
   */
  this.$ID1_;
  
  /**
   * @private
   * @type {models.MyModel}
   */
  this.$ID2_;
  
  /**
   * @private
   * @type {controllers.MyController}
   */
  this.$ID3_;
  
  /**
   * @private
   * @type {org.apache.flex.net.HTTPService}
   */
  this.service_;
  
  /**
   * @private
   * @type {org.apache.flex.collections.LazyCollection}
   */
  this.collection_;
  
  /**
   * @private
   * @type {org.apache.flex.collections.parsers.JSONInputParser}
   */
  this.$ID4_;
  
  /**
   * @private
   * @type {StockDataJSONItemConverter}
   */
  this.$ID5_;
  
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
null, [org.apache.flex.net.HTTPService, 2, 'id', true, 'service', 'beads', null, [org.apache.flex.collections.LazyCollection, 3, 'id', true, 'collection', 'inputParser', false, [org.apache.flex.collections.parsers.JSONInputParser, 1, '_id', true, '$ID4', 0, 0, null], 'itemConverter', false, [StockDataJSONItemConverter, 1, '_id', true, '$ID5', 0, 0, null], 0, 0, null], 0, 0, null],
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
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
FlexJSTest_again.prototype.FLEXJS_REFLECTION_INFO = function () {
  return {
    variables: function () {
      return {
      };
    },
    accessors: function () {
      return {
        'service': { type: 'org.apache.flex.net.HTTPService', declaredBy: 'FlexJSTest_again'},
        'collection': { type: 'org.apache.flex.collections.LazyCollection', declaredBy: 'FlexJSTest_again'}
      };
    },
    methods: function () {
      return {
        '$EH0': { type: 'void', declaredBy: 'FlexJSTest_again'}
      };
    }
  };
};


/**
 * @export
 * @param {org.apache.flex.events.Event} event
 */
FlexJSTest_again.prototype.$EH0 = function(event)
{
  org.apache.flex.utils.Language.as(this.model, models.MyModel, true).labelText = 'Hello World';
};


Object.defineProperties(FlexJSTest_again.prototype, /** @lends {FlexJSTest_again.prototype} */ {
/** @export */
    service: {
    /** @this {FlexJSTest_again} */
    get: function() {
      return this.service_;
    },
    /** @this {FlexJSTest_again} */
    set: function(value) {
      if (value != this.service_) {
        this.service_ = value;
        this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'service', null, value));
      }
    }
  },
  /** @export */
    collection: {
    /** @this {FlexJSTest_again} */
    get: function() {
      return this.collection_;
    },
    /** @this {FlexJSTest_again} */
    set: function(value) {
      if (value != this.collection_) {
        this.collection_ = value;
        this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'collection', null, value));
      }
    }
  }
});

