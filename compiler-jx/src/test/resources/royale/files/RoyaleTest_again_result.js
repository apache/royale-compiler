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
 * RoyaleTest_again
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('RoyaleTest_again');

goog.require('org.apache.royale.core.Application');
goog.require('org.apache.royale.core.SimpleCSSValuesImpl');
goog.require('MyInitialView');
goog.require('models.MyModel');
goog.require('controllers.MyController');
goog.require('org.apache.royale.net.HTTPService');
goog.require('org.apache.royale.collections.LazyCollection');
goog.require('org.apache.royale.collections.parsers.JSONInputParser');
goog.require('StockDataJSONItemConverter');
goog.require('org.apache.royale.events.Event');



/**
 * @constructor
 * @extends {org.apache.royale.core.Application}
 */
RoyaleTest_again = function() {
  RoyaleTest_again.base(this, 'constructor');
  
  /**
   * @private
   * @type {org.apache.royale.core.SimpleCSSValuesImpl}
   */
  this.$ID_8_0;
  
  /**
   * @private
   * @type {MyInitialView}
   */
  this.$ID_8_1;
  
  /**
   * @private
   * @type {models.MyModel}
   */
  this.$ID_8_2;
  
  /**
   * @private
   * @type {controllers.MyController}
   */
  this.$ID_8_3;
  
  /**
   * @private
   * @type {org.apache.royale.net.HTTPService}
   */
  this.service_;
  
  /**
   * @private
   * @type {org.apache.royale.collections.LazyCollection}
   */
  this.collection_;
  
  /**
   * @private
   * @type {org.apache.royale.collections.parsers.JSONInputParser}
   */
  this.$ID_8_4;
  
  /**
   * @private
   * @type {StockDataJSONItemConverter}
   */
  this.$ID_8_5;
  
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

  this.generateMXMLAttributes([
    5,
    'model',
    false,
    [
      models.MyModel,
      1,
      '_id',
      true,
      '$ID_8_2',
      0,
      0,
      null
    ],
    'valuesImpl',
    false,
    [
      org.apache.royale.core.SimpleCSSValuesImpl,
      1,
      '_id',
      true,
      '$ID_8_0',
      0,
      0,
      null
    ],
    'initialView',
    false,
    [
      MyInitialView,
      1,
      '_id',
      true,
      '$ID_8_1',
      0,
      0,
      null
    ],
    'controller',
    false,
    [
      controllers.MyController,
      1,
      '_id',
      true,
      '$ID_8_3',
      0,
      0,
      null
    ],
    'beads',
    null,
    [
      org.apache.royale.net.HTTPService,
      2,
      'id',
      true,
      'service',
      'beads',
      null,
      [
        org.apache.royale.collections.LazyCollection,
        3,
        'id',
        true,
        'collection',
        'inputParser',
        false,
        [
          org.apache.royale.collections.parsers.JSONInputParser,
          1,
          '_id',
          true,
          '$ID_8_4',
          0,
          0,
          null
        ],
        'itemConverter',
        false,
        [
          StockDataJSONItemConverter,
          1,
          '_id',
          true,
          '$ID_8_5',
          0,
          0,
          null
        ],
        0,
        0,
        null
      ],
      0,
      0,
      null
    ],
    0,
    1,
    'initialize',
this.$EH_8_0
  ]);
  
};
goog.inherits(RoyaleTest_again, org.apache.royale.core.Application);




/**
 * @export
 * @param {org.apache.royale.events.Event} event
 */
RoyaleTest_again.prototype.$EH_8_0 = function(event)
{
  org.apache.royale.utils.Language.as(this.model, models.MyModel, true).labelText = 'Hello World';
};


Object.defineProperties(RoyaleTest_again.prototype, /** @lends {RoyaleTest_again.prototype} */ {
/** @export */
    service: {
    /** @this {RoyaleTest_again} */
    get: function() {
      return this.service_;
    },
    /** @this {RoyaleTest_again} */
    set: function(value) {
      if (value != this.service_) {
        this.service_ = value;
        this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(this, 'service', null, value));
      }
    }
  },
  /** @export */
    collection: {
    /** @this {RoyaleTest_again} */
    get: function() {
      return this.collection_;
    },
    /** @this {RoyaleTest_again} */
    set: function(value) {
      if (value != this.collection_) {
        this.collection_ = value;
        this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(this, 'collection', null, value));
      }
    }
  }
});
/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
RoyaleTest_again.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'RoyaleTest_again', qName: 'RoyaleTest_again', kind: 'class'  }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
RoyaleTest_again.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    accessors: function () {
      return {
        'service': { type: 'org.apache.royale.net.HTTPService', access: 'readwrite', declaredBy: 'RoyaleTest_again'},
        'collection': { type: 'org.apache.royale.collections.LazyCollection', access: 'readwrite', declaredBy: 'RoyaleTest_again'}
      };
    },
    methods: function () {
      return {
        'RoyaleTest_again': { type: '', declaredBy: 'RoyaleTest_again'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
RoyaleTest_again.prototype.ROYALE_COMPILE_FLAGS = 9;



