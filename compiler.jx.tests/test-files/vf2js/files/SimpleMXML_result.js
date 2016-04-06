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
 * SimpleMXML
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('SimpleMXML');

goog.require('spark.components.Application');
goog.require('spark.components.Button');




/**
 * @constructor
 * @extends {spark.components.Application}
 */
SimpleMXML = function() {
  SimpleMXML.base(this, 'constructor');
  
  /**
   * @private
   * @type {spark.components.Button}
   */
  this.$ID0;

  /**
   * @private
   * @type {spark.components.Button}
   */
  this.$ID1;
};
goog.inherits(SimpleMXML, spark.components.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
SimpleMXML.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'SimpleMXML', qName: 'SimpleMXML' }] };


/**
 * start
 *
 * @export
 */
SimpleMXML.prototype.start = function () {
};

