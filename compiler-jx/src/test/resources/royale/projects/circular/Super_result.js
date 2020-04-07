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

goog.require('Base');



/**
 * @constructor
 */
Super = function() {
};


/**
 * @private
 * @type {Base}
 */
Super.isItCircular = null;


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
/**
 * Provide reflection support for distinguishing dynamic fields on class object (static)
 * @const
 * @type {Array<string>}
 */
Super.prototype.ROYALE_INITIAL_STATICS = Object.keys(Super);
