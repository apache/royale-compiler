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
 * SomeSDKClass
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('SomeSDKClass');

goog.require('mx.core.mx_internal');
goog.require('bases.HelperBaseClass');
goog.require('org_apache_flex_utils_Language');



/**
 * @constructor
 */
SomeSDKClass = function() {
  this.number = 'Got it: ' + this.getString();

  this.helperBaseClass = new bases.HelperBaseClass();
};


/**
 * @private
 * @type {number}
 */
SomeSDKClass.prototype.number;


/**
 * @expose
 * @return {string}
 */
SomeSDKClass.prototype.getString = function() {
  return Helper.helperFunction();
};


/**
 * @expose
 * @return {string}
 */
SomeSDKClass.prototype.someFunction = function() {
  this.helperBaseClass.doSomething();
};


/**
 * @type {bases.HelperBaseClass}
 */
SomeSDKClass.prototype.helperBaseClass;


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
SomeSDKClass.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'SomeSDKClass', qName: 'SomeSDKClass'}] };



/**
 * @constructor
 * @extends {bases.HelperBaseClass}
 * @param {string} url
 */
Helper = function(url) {
  Helper.base(this, 'constructor', url);
  this.url_ = url;
};
goog.inherits(Helper, bases.HelperBaseClass);


/**
 * @expose
 * @return {string}
 */
Helper.helperFunction = function() {
  return "Hello world";
};


/**
 * @private
 * @type {string}
 */
Helper.prototype.url_;


/**
 * @expose
 * @return {string}
 */
Helper.prototype.get_url = function() {
  return this.url_;
};
