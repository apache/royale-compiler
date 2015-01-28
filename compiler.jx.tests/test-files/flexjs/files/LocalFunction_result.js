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
 * LocalFunction
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('LocalFunction');



/**
 * @constructor
 */
LocalFunction = function() {
};


/**
 * @private
 * @type {string}
 */
LocalFunction.prototype.myMemberProperty = "got it: ";


/**
 * @private
 * @param {number} value
 */
LocalFunction.prototype.myMemberMethod = function(value) {
  function myLocalFunction(value) {
    return this.myMemberProperty + value;
  };
  org_apache_flex_utils_Language.trace("WOW! :: " + goog.bind(myLocalFunction, this)(value + 42));
};


/**
 * @expose
 */
LocalFunction.prototype.doIt = function() {
  this.myMemberMethod(624);
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
LocalFunction.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'LocalFunction', qName: 'LocalFunction'}] };

