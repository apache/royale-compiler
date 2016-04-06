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
 * bases.HelperBaseClass
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('bases.HelperBaseClass');



/**
 * @constructor
 */
bases.HelperBaseClass = function() {};


/**
 * @export
 * @return {string}
 */
bases.HelperBaseClass.prototype.doSomething = function() {
  return 'doneSomething';
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
bases.HelperBaseClass.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'HelperBaseClass', qName: 'bases.HelperBaseClass'}] };
