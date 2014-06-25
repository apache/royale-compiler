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
 * models.MyModel
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('models.MyModel');



/**
 * @constructor
 * @extends {org.apache.flex.events.EventDispatcher}
 */
models.MyModel = function() {
  models.MyModel.base(this, 'constructor');
};
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


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
models.MyModel.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyModel', qName: 'models.MyModel'}] };

