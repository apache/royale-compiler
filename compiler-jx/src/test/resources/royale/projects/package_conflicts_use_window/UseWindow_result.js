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
 * UseWindow
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('UseWindow');

goog.require('mypackage.TestClass');



/**
 * @constructor
 */
UseWindow = function() {
  this.testClass = new mypackage.TestClass();
};


/**
 * @private
 * @type {mypackage.TestClass}
 */
UseWindow.prototype.testClass = null;


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
UseWindow.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'UseWindow', qName: 'UseWindow', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
UseWindow.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'UseWindow': { type: '', declaredBy: 'UseWindow'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
UseWindow.prototype.ROYALE_COMPILE_FLAGS = 9;
