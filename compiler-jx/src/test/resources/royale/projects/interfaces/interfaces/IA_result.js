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
 * interfaces.IA
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('interfaces.IA');

goog.require('interfaces.IC');



/**
 * @interface
 * @extends {interfaces.IC}
 */
interfaces.IA = function() {
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
interfaces.IA.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'IA', qName: 'interfaces.IA', kind: 'interface' }], interfaces: [interfaces.IC] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
interfaces.IA.prototype.ROYALE_REFLECTION_INFO = function () {
  return {};
};
/**
 * @const
 * @type {number}
 */
interfaces.IA.prototype.ROYALE_COMPILE_FLAGS = 9;
