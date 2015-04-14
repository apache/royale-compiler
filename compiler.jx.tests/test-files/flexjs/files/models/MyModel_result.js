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

goog.provide('models_MyModel');



/**
 * @constructor
 * @extends {org_apache_flex_events_EventDispatcher}
 */
models_MyModel = function() {
  models_MyModel.base(this, 'constructor');
};
goog.inherits(models_MyModel, org_apache_flex_events_EventDispatcher);


/**
 * @private
 * @type {string}
 */
models_MyModel.prototype._labelText;


/**
 * @private
 * @type {Array}
 */
models_MyModel.prototype._strings = ["AAPL", "ADBE", "GOOG", "MSFT", "YHOO"];


/**
 * @private
 * @type {Array}
 */
models_MyModel.prototype._cities = ["London", "Miami", "Paris", "Sydney", "Tokyo"];


Object.defineProperties(models_MyModel.prototype, /** @lends {models_MyModel.prototype} */ {
/** @expose */
labelText: {
get: /** @this {models_MyModel} */ function() {
  return this._labelText;
},
set: /** @this {models_MyModel} */ function(value) {
  if (value != this._labelText) {
    this._labelText = value;
    this.dispatchEvent(new org_apache_flex_events_Event("labelTextChanged"));
  }
}},
/** @expose */
cities: {
get: /** @this {models_MyModel} */ function() {
  return this._cities;
}},
/** @expose */
strings: {
get: /** @this {models_MyModel} */ function() {
  return this._strings;
}}}
);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
models_MyModel.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyModel', qName: 'models_MyModel'}] };

