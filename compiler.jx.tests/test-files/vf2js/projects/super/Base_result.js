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
 * Base
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('Base');

goog.require('Super');
goog.require('org.apache.flex.utils.Language');



/**
 * @constructor
 * @extends {Super}
 */
Base = function() {
  Base.base(this, 'constructor');
};
goog.inherits(Base, Super);


/**
 * @expose
 * @return {string}
 * @override
 */
Base.prototype.get_text = function() {
  return "A" + Base.base(this, 'get_text');
};


/**
 * @expose
 * @param {string} value
 * @override
 */
Base.prototype.set_text = function(value) {
  if (value != Base.base(this, 'get_text')) {
    Base.base(this, 'set_text', "B" + value);
  }
};


/**
 * @expose
 */
Base.prototype.getNumber = function() {
  var /** @type {number} */ x = this.get_x();
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Base.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Base', qName: 'Base'}] };
