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
 * SimpleMXML_Project
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('SimpleMXML_Project');

goog.require('spark.components.Application');
goog.require('example.Component');
goog.require('org.apache.flex.utils.Language');




/**
 * @constructor
 * @extends {spark.components.Application}
 */
SimpleMXML_Project = function() {
  SimpleMXML_Project.base(this, 'constructor');
  };
goog.inherits(SimpleMXML_Project, spark.components.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
SimpleMXML_Project.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'SimpleMXML_Project', qName: 'SimpleMXML_Project' }] };


/**
 * @private
 * @type {example.Component}
 */
SimpleMXML_Project.prototype.myComponent;


/**
 * start
 *
 * @expose
 */
SimpleMXML_Project.prototype.start = function () {
};