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
 * wildcard_import
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('wildcard_import');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.html.Button');




/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
wildcard_import = function() {
  wildcard_import.base(this, 'constructor');
  
  /**
   * @private
   * @type {Array}
   */
  this.mxmldd;
  
  /**
   * @private
   * @type {Array}
   */
  this.mxmldp;
};
goog.inherits(wildcard_import, org.apache.flex.core.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
wildcard_import.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'wildcard_import', qName: 'wildcard_import' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
wildcard_import.prototype.FLEXJS_REFLECTION_INFO = function () {
  return {
    variables: function () {
      return {
      };
    },
    accessors: function () {
      return {
      };
    },
    methods: function () {
      return {
      };
    }
  };
};


/**
 * @private
 */
wildcard_import.prototype.tmp = function() {
  var /** @type {org.apache.flex.html.Button} */ myButton;
  myButton = new org.apache.flex.html.Button();
};



