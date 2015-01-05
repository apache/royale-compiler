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
 * MyInitialView
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('MyInitialView');

goog.require('org.apache.flex.core.ViewBase');
goog.require('org.apache.flex.html.Label');
goog.require('org.apache.flex.binding.SimpleBinding');
goog.require('org.apache.flex.html.TextButton');
goog.require('org.apache.flex.html.List');
goog.require('org.apache.flex.binding.ConstantBinding');
goog.require('org.apache.flex.html.TextArea');
goog.require('org.apache.flex.html.TextInput');
goog.require('org.apache.flex.html.CheckBox');
goog.require('org.apache.flex.html.RadioButton');
goog.require('org.apache.flex.html.DropDownList');
goog.require('org.apache.flex.html.ComboBox');
goog.require('org.apache.flex.events.CustomEvent');
goog.require('org.apache.flex.events.Event');
goog.require('org.apache.flex.utils.Timer');




/**
 * @constructor
 * @extends {org.apache.flex.core.ViewBase}
 */
MyInitialView = function() {
  MyInitialView.base(this, 'constructor');
  
  /**
   * @private
   * @type {org.apache.flex.html.Label}
   */
  this.lbl;
  
  /**
   * @private
   * @type {org.apache.flex.binding.SimpleBinding}
   */
  this.$ID0;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextButton}
   */
  this.$ID1;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextButton}
   */
  this.$ID2;
  
  /**
   * @private
   * @type {org.apache.flex.html.Label}
   */
  this.timerLabel;
  
  /**
   * @private
   * @type {org.apache.flex.html.List}
   */
  this.cityList;
  
  /**
   * @private
   * @type {org.apache.flex.binding.ConstantBinding}
   */
  this.$ID3;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextArea}
   */
  this.$ID5;
  
  /**
   * @private
   * @type {org.apache.flex.binding.SimpleBinding}
   */
  this.$ID4;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextInput}
   */
  this.input;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextButton}
   */
  this.$ID6;
  
  /**
   * @private
   * @type {org.apache.flex.html.CheckBox}
   */
  this.checkbox;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID7;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID8;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID9;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID10;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID11;
  
  /**
   * @private
   * @type {org.apache.flex.html.RadioButton}
   */
  this.$ID12;
  
  /**
   * @private
   * @type {org.apache.flex.html.DropDownList}
   */
  this.list;
  
  /**
   * @private
   * @type {org.apache.flex.binding.ConstantBinding}
   */
  this.$ID13;
  
  /**
   * @private
   * @type {org.apache.flex.html.TextButton}
   */
  this.$ID14;
  
  /**
   * @private
   * @type {org.apache.flex.html.ComboBox}
   */
  this.comboBox;
  
  /**
   * @private
   * @type {org.apache.flex.binding.ConstantBinding}
   */
  this.$ID15;
  
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
goog.inherits(MyInitialView, org.apache.flex.core.ViewBase);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
MyInitialView.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyInitialView', qName: 'MyInitialView' }] };


/**
 * @private
 * @type {org.apache.flex.utils.Timer}
 */
MyInitialView.prototype.timer;


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_symbol = function() {
  return org.apache.flex.utils.Language.as(this.get_list().get_selectedItem(), String);
};


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_city = function() {
  return org.apache.flex.utils.Language.as(this.get_cityList().get_selectedItem(), String);
};


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_inputText = function() {
  return this.get_input().get_text();
};


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_comboBoxValue = function() {
  return String(this.get_comboBox().get_selectedItem());
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.startTimer = function(event) {
  this.timer = new org.apache.flex.utils.Timer(1000);
  this.timer.addEventListener('timer', goog.bind(this.timerHandler, this));
  this.timer.start();
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.timerHandler = function(event) {
  this.get_timerLabel().set_text(this.timer.get_currentCount().toString());
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH0 = function(event)
{
  this.startTimer(event);
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH1 = function(event)
{
  this.timer.removeEventListener('timer', goog.bind(this.timerHandler, this));
  this.timer.stop();
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH2 = function(event)
{
  this.dispatchEvent(new org.apache.flex.events.CustomEvent('cityListChanged'));
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH3 = function(event)
{
  this.dispatchEvent(new org.apache.flex.events.CustomEvent('transferClicked'));
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH4 = function(event)
{
  this.dispatchEvent(new org.apache.flex.events.CustomEvent('listChanged'));
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH5 = function(event)
{
  this.dispatchEvent(new org.apache.flex.events.CustomEvent('buttonClicked'));
};


/**
 * @expose
 * @param {org.apache.flex.events.Event} event
 */
MyInitialView.prototype.$EH6 = function(event)
{
  this.dispatchEvent(new org.apache.flex.events.CustomEvent('comboBoxChanged'));
};


/**
 * @expose
 * @return {org.apache.flex.html.Label}
 */
MyInitialView.prototype.get_lbl = function()
{
  return this.lbl;
};


/**
 * @expose
 * @param {org.apache.flex.html.Label} value
 */
MyInitialView.prototype.set_lbl = function(value)
{
  if (value != this.lbl) {
    this.lbl = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'lbl', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.Label}
 */
MyInitialView.prototype.get_timerLabel = function()
{
  return this.timerLabel;
};


/**
 * @expose
 * @param {org.apache.flex.html.Label} value
 */
MyInitialView.prototype.set_timerLabel = function(value)
{
  if (value != this.timerLabel) {
    this.timerLabel = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'timerLabel', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.List}
 */
MyInitialView.prototype.get_cityList = function()
{
  return this.cityList;
};


/**
 * @expose
 * @param {org.apache.flex.html.List} value
 */
MyInitialView.prototype.set_cityList = function(value)
{
  if (value != this.cityList) {
    this.cityList = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'cityList', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.TextInput}
 */
MyInitialView.prototype.get_input = function()
{
  return this.input;
};


/**
 * @expose
 * @param {org.apache.flex.html.TextInput} value
 */
MyInitialView.prototype.set_input = function(value)
{
  if (value != this.input) {
    this.input = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'input', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.CheckBox}
 */
MyInitialView.prototype.get_checkbox = function()
{
  return this.checkbox;
};


/**
 * @expose
 * @param {org.apache.flex.html.CheckBox} value
 */
MyInitialView.prototype.set_checkbox = function(value)
{
  if (value != this.checkbox) {
    this.checkbox = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'checkbox', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.DropDownList}
 */
MyInitialView.prototype.get_list = function()
{
  return this.list;
};


/**
 * @expose
 * @param {org.apache.flex.html.DropDownList} value
 */
MyInitialView.prototype.set_list = function(value)
{
  if (value != this.list) {
    this.list = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'list', null, value));
  }
};


/**
 * @expose
 * @return {org.apache.flex.html.ComboBox}
 */
MyInitialView.prototype.get_comboBox = function()
{
  return this.comboBox;
};


/**
 * @expose
 * @param {org.apache.flex.html.ComboBox} value
 */
MyInitialView.prototype.set_comboBox = function(value)
{
  if (value != this.comboBox) {
    this.comboBox = value;
    this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, 'comboBox', null, value));
  }
};


/**
 * @override
 * @return {Array} the Array of UI element descriptors.
 */
MyInitialView.prototype.get_MXMLDescriptor = function()
{
  if (this.mxmldd == undefined)
  {
    /** @type {Array} */
    var arr = MyInitialView.base(this, 'get_MXMLDescriptor');
    /** @type {Array} */
    var data = [
org.apache.flex.html.Label,
4,
'id',
true,
'lbl',
'x',
true,
100,
'y',
true,
25,
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, '_id', true, '$ID0', 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org.apache.flex.html.TextButton,
4,
'_id',
true,
'$ID1',
'text',
true,
'Let\'s Start Timer',
'x',
true,
100,
'y',
true,
75,
0,
1,
'click',
this.$EH0,
null,
org.apache.flex.html.TextButton,
4,
'_id',
true,
'$ID2',
'text',
true,
'Stop Timer',
'x',
true,
100,
'y',
true,
100,
0,
1,
'click',
this.$EH1,
null,
org.apache.flex.html.Label,
3,
'id',
true,
'timerLabel',
'x',
true,
100,
'y',
true,
125,
0,
0,
null,
org.apache.flex.html.List,
6,
'id',
true,
'cityList',
'x',
true,
200,
'y',
true,
75,
'width',
true,
100,
'height',
true,
75,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID3', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'cities', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH2,
null,
org.apache.flex.html.TextArea,
6,
'_id',
true,
'$ID5',
'x',
true,
320,
'y',
true,
25,
'width',
true,
150,
'height',
true,
75,
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, '_id', true, '$ID4', 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org.apache.flex.html.TextInput,
3,
'id',
true,
'input',
'x',
true,
320,
'y',
true,
110,
0,
0,
null,
org.apache.flex.html.TextButton,
4,
'_id',
true,
'$ID6',
'text',
true,
'Transfer',
'x',
true,
320,
'y',
true,
138,
0,
1,
'click',
this.$EH3,
null,
org.apache.flex.html.CheckBox,
4,
'id',
true,
'checkbox',
'x',
true,
320,
'y',
true,
170,
'text',
true,
'Check Me',
0,
0,
null,
org.apache.flex.html.RadioButton,
6,
'_id',
true,
'$ID7',
'groupName',
true,
'group1',
'text',
true,
'Apples',
'value',
true,
0,
'x',
true,
100,
'y',
true,
150,
0,
0,
null,
org.apache.flex.html.RadioButton,
7,
'_id',
true,
'$ID8',
'groupName',
true,
'group1',
'text',
true,
'Oranges',
'value',
true,
1,
'x',
true,
100,
'y',
true,
170,
'selected',
true,
true,
0,
0,
null,
org.apache.flex.html.RadioButton,
6,
'_id',
true,
'$ID9',
'groupName',
true,
'group1',
'text',
true,
'Grapes',
'value',
true,
2,
'x',
true,
100,
'y',
true,
190,
0,
0,
null,
org.apache.flex.html.RadioButton,
7,
'_id',
true,
'$ID10',
'groupName',
true,
'group2',
'text',
true,
'Red',
'value',
true,
16711680,
'x',
true,
100,
'y',
true,
250,
'selected',
true,
true,
0,
0,
null,
org.apache.flex.html.RadioButton,
6,
'_id',
true,
'$ID11',
'groupName',
true,
'group2',
'text',
true,
'Green',
'value',
true,
32768,
'x',
true,
100,
'y',
true,
270,
0,
0,
null,
org.apache.flex.html.RadioButton,
6,
'_id',
true,
'$ID12',
'groupName',
true,
'group2',
'text',
true,
'Blue',
'value',
true,
255,
'x',
true,
100,
'y',
true,
290,
0,
0,
null,
org.apache.flex.html.DropDownList,
6,
'id',
true,
'list',
'x',
true,
200,
'y',
true,
200,
'width',
true,
100,
'height',
true,
24,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID13', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'strings', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH4,
null,
org.apache.flex.html.TextButton,
4,
'_id',
true,
'$ID14',
'text',
true,
'OK',
'x',
true,
200,
'y',
true,
230,
0,
1,
'click',
this.$EH5,
null,
org.apache.flex.html.ComboBox,
5,
'id',
true,
'comboBox',
'x',
true,
320,
'y',
true,
200,
'width',
true,
100,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID15', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'cities', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH6,
null
];
  
    if (arr)
      this.mxmldd = arr.concat(data);
    else
      this.mxmldd = data;
  }
  return this.mxmldd;
};


