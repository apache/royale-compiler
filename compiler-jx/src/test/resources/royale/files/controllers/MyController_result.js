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
 * controllers.MyController
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('controllers.MyController');

goog.require('MyInitialView');
goog.require('RoyaleTest_again');
goog.require('models.MyModel');
goog.require('org.apache.royale.core.Application');
goog.require('org.apache.royale.events.Event');
goog.require('org.apache.royale.core.IDocument');
goog.require('org.apache.royale.utils.Language');



/**
 * @constructor
 * @implements {org.apache.royale.core.IDocument}
 * @param {org.apache.royale.core.Application=} app
 */
controllers.MyController = function(app) {
  app = typeof app !== 'undefined' ? app : null;
  if (app) {
    this.app = org.apache.royale.utils.Language.as(app, RoyaleTest_again);
    app.addEventListener("viewChanged", org.apache.royale.utils.Language.closure(this.viewChangeHandler, this, 'viewChangeHandler'));
  }
};


/**
 * @private
 * @type {string}
 */
controllers.MyController.prototype.queryBegin = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22";


/**
 * @private
 * @type {string}
 */
controllers.MyController.prototype.queryEnd = "%22)%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";


/**
 * @private
 * @type {RoyaleTest_again}
 */
controllers.MyController.prototype.app = null;


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.viewChangeHandler = function(event) {
  this.app.initialView.addEventListener("buttonClicked", org.apache.royale.utils.Language.closure(this.buttonClickHandler, this, 'buttonClickHandler'));
  this.app.initialView.addEventListener("listChanged", org.apache.royale.utils.Language.closure(this.listChangedHandler, this, 'listChangedHandler'));
  this.app.initialView.addEventListener("cityListChanged", org.apache.royale.utils.Language.closure(this.cityListChangeHandler, this, 'cityListChangeHandler'));
  this.app.initialView.addEventListener("transferClicked", org.apache.royale.utils.Language.closure(this.transferClickHandler, this, 'transferClickHandler'));
  this.app.initialView.addEventListener("comboBoxChanged", org.apache.royale.utils.Language.closure(this.comboBoxChangeHandler, this, 'comboBoxChangeHandler'));
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.buttonClickHandler = function(event) {
  var /** @type {string} */ sym = org.apache.royale.utils.Language.as(this.app.initialView, MyInitialView, true).symbol;
  this.app.service.url = this.queryBegin + sym + this.queryEnd;
  this.app.service.send();
  this.app.service.addEventListener("complete", org.apache.royale.utils.Language.closure(this.completeHandler, this, 'completeHandler'));
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.completeHandler = function(event) {
  org.apache.royale.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.royale.utils.Language.as(this.app.collection.getItemAt(0), String);
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.listChangedHandler = function(event) {
  org.apache.royale.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.royale.utils.Language.as(this.app.initialView, MyInitialView, true).symbol;
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.cityListChangeHandler = function(event) {
  org.apache.royale.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.royale.utils.Language.as(this.app.initialView, MyInitialView, true).city;
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.transferClickHandler = function(event) {
  org.apache.royale.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.royale.utils.Language.as(this.app.initialView, MyInitialView, true).inputText;
};


/**
 * @private
 * @param {org.apache.royale.events.Event} event
 */
controllers.MyController.prototype.comboBoxChangeHandler = function(event) {
  org.apache.royale.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.royale.utils.Language.as(this.app.initialView, MyInitialView, true).comboBoxValue;
};


/**
 * @param {Object} document
 * @param {string=} id
 */
controllers.MyController.prototype.setDocument = function(document, id) {
  id = typeof id !== 'undefined' ? id : null;
  this.app = org.apache.royale.utils.Language.as(document, RoyaleTest_again);
  this.app.addEventListener("viewChanged", org.apache.royale.utils.Language.closure(this.viewChangeHandler, this, 'viewChangeHandler'));
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
controllers.MyController.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'MyController', qName: 'controllers.MyController', kind: 'class' }], interfaces: [org.apache.royale.core.IDocument] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
controllers.MyController.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'MyController': { type: '', declaredBy: 'controllers.MyController', parameters: function () { return [ 'org.apache.royale.core.Application', true ]; }},
        'setDocument': { type: 'void', declaredBy: 'controllers.MyController', parameters: function () { return [ 'Object', false ,'String', true ]; }}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
controllers.MyController.prototype.ROYALE_COMPILE_FLAGS = 9;
