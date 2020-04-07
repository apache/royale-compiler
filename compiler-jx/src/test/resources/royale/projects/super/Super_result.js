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
 * Super
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('Super');



/**
 * @constructor
 */
Super = function() {
};


/**
 * @private
 * @type {string}
 */
Super.prototype._text = '';


Super.prototype.get__text = function() {
  return this._text;
};


Super.prototype.set__text = function(value) {
  if (value != this._text) {
    this._text = value;
  }
};


Object.defineProperties(Super.prototype, /** @lends {Super.prototype} */ {
/**
  * @export
  * @type {string} */
text: {
get: Super.prototype.get__text,
set: Super.prototype.set__text}}
);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Super.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'Super', qName: 'Super', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
Super.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    accessors: function () {
      return {
        'text': { type: 'String', access: 'readwrite', declaredBy: 'Super'}
      };
    },
    methods: function () {
      return {
        'Super': { type: '', declaredBy: 'Super'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
Super.prototype.ROYALE_COMPILE_FLAGS = 9;
