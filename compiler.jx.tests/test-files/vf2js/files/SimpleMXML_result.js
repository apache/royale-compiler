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
 * Simple
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('Simple');

goog.require('vf2js_s.components.Application');
goog.require('vf2js_mx.components.Button');
goog.require('vf2js_s.components.Button');




/**
 * @constructor
 * @extends {vf2js_s.components.Application}
 */
Simple = function() {
  goog.base(this);
  
  /**
   * @private
   * @type {vf2js_mx.components.Button}
   */
  this.$ID0;
  
  /**
   * @private
   * @type {vf2js_s.components.Button}
   */
  this.$ID1;
  
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
goog.inherits(Simple, vf2js_s.components.Application);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Simple.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Simple', qName: 'Simple' }] };


/**
 * @override
 * @return {Array} the Array of UI element descriptors.
 */
Simple.prototype.get_MXMLProperties = function()
{
  if (this.mxmldp == undefined)
  {
    /** @type {Array} */
    var arr = goog.base(this, 'get_MXMLProperties');
    /** @type {Array} */
    var data = [
2,
vf2js_mx.components.Button,
1,
'_id',
true,
'$ID0',
0,
0,
null,
vf2js_s.components.Button,
3,
'_id',
true,
'$ID1',
'label',
true,
'hello',
'x',
true,
100,
0,
0,
null0,
0
];
  
    if (arr)
      this.mxmldp = arr.concat(data);
    else
      this.mxmldp = data;
  }
  return this.mxmldp;
};

