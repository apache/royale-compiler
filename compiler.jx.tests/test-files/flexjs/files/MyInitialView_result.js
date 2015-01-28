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

goog.require('org_apache_flex_core_ViewBase');
goog.require('org_apache_flex_html_Label');
goog.require('org_apache_flex_binding_SimpleBinding');
goog.require('org_apache_flex_html_TextButton');
goog.require('org_apache_flex_html_List');
goog.require('org_apache_flex_binding_ConstantBinding');
goog.require('org_apache_flex_html_TextArea');
goog.require('org_apache_flex_html_TextInput');
goog.require('org_apache_flex_html_CheckBox');
goog.require('org_apache_flex_html_RadioButton');
goog.require('org_apache_flex_html_DropDownList');
goog.require('org_apache_flex_html_ComboBox');
goog.require('org_apache_flex_events_CustomEvent');
goog.require('org_apache_flex_events_Event');
goog.require('org_apache_flex_utils_Timer');




/**
 * @constructor
 * @extends {org_apache_flex_core_ViewBase}
 */
MyInitialView = function() {
  MyInitialView.base(this, 'constructor');
  
  /**
   * @private
   * @type {org_apache_flex_html_Label}
   */
  this.lbl;
  
  /**
   * @private
   * @type {org_apache_flex_binding_SimpleBinding}
   */
  this.$ID0;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID1;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID2;
  
  /**
   * @private
   * @type {org_apache_flex_html_Label}
   */
  this.timerLabel;
  
  /**
   * @private
   * @type {org_apache_flex_html_List}
   */
  this.cityList;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
   */
  this.$ID3;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextArea}
   */
  this.$ID5;
  
  /**
   * @private
   * @type {org_apache_flex_binding_SimpleBinding}
   */
  this.$ID4;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextInput}
   */
  this.input;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID6;
  
  /**
   * @private
   * @type {org_apache_flex_html_CheckBox}
   */
  this.checkbox;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID7;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID8;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID9;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID10;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID11;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID12;
  
  /**
   * @private
   * @type {org_apache_flex_html_DropDownList}
   */
  this.list;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
   */
  this.$ID13;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID14;
  
  /**
   * @private
   * @type {org_apache_flex_html_ComboBox}
   */
  this.comboBox;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
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
goog.inherits(MyInitialView, org_apache_flex_core_ViewBase);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
MyInitialView.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyInitialView', qName: 'MyInitialView' }] };


/**
 * @private
 * @type {org_apache_flex_utils_Timer}
 */
MyInitialView.prototype.timer;


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_symbol = function() {
  return org_apache_flex_utils_Language.as(this.get_list().get_selectedItem(), String);
};


/**
 * @expose
 * @return {string}
 */
MyInitialView.prototype.get_city = function() {
  return org_apache_flex_utils_Language.as(this.get_cityList().get_selectedItem(), String);
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
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.startTimer = function(event) {
  this.timer = new org_apache_flex_utils_Timer(1000);
  this.timer.addEventListener('timer', goog.bind(this.timerHandler, this));
  this.timer.start();
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.timerHandler = function(event) {
  this.get_timerLabel().set_text(this.timer.get_currentCount().toString());
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH0 = function(event)
{
  this.startTimer(event);
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH1 = function(event)
{
  this.timer.removeEventListener('timer', goog.bind(this.timerHandler, this));
  this.timer.stop();
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH2 = function(event)
{
  this.dispatchEvent(new org_apache_flex_events_CustomEvent('cityListChanged'));
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH3 = function(event)
{
  this.dispatchEvent(new org_apache_flex_events_CustomEvent('transferClicked'));
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH4 = function(event)
{
  this.dispatchEvent(new org_apache_flex_events_CustomEvent('listChanged'));
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH5 = function(event)
{
  this.dispatchEvent(new org_apache_flex_events_CustomEvent('buttonClicked'));
};


/**
 * @expose
 * @param {org_apache_flex_events_Event} event
 */
MyInitialView.prototype.$EH6 = function(event)
{
  this.dispatchEvent(new org_apache_flex_events_CustomEvent('comboBoxChanged'));
};


/**
 * @expose
 * @return {org_apache_flex_html_Label}
 */
MyInitialView.prototype.get_lbl = function()
{
  return this.lbl;
};


/**
 * @expose
 * @param {org_apache_flex_html_Label} value
 */
MyInitialView.prototype.set_lbl = function(value)
{
  if (value != this.lbl) {
    this.lbl = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'lbl', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_Label}
 */
MyInitialView.prototype.get_timerLabel = function()
{
  return this.timerLabel;
};


/**
 * @expose
 * @param {org_apache_flex_html_Label} value
 */
MyInitialView.prototype.set_timerLabel = function(value)
{
  if (value != this.timerLabel) {
    this.timerLabel = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'timerLabel', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_List}
 */
MyInitialView.prototype.get_cityList = function()
{
  return this.cityList;
};


/**
 * @expose
 * @param {org_apache_flex_html_List} value
 */
MyInitialView.prototype.set_cityList = function(value)
{
  if (value != this.cityList) {
    this.cityList = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'cityList', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_TextInput}
 */
MyInitialView.prototype.get_input = function()
{
  return this.input;
};


/**
 * @expose
 * @param {org_apache_flex_html_TextInput} value
 */
MyInitialView.prototype.set_input = function(value)
{
  if (value != this.input) {
    this.input = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'input', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_CheckBox}
 */
MyInitialView.prototype.get_checkbox = function()
{
  return this.checkbox;
};


/**
 * @expose
 * @param {org_apache_flex_html_CheckBox} value
 */
MyInitialView.prototype.set_checkbox = function(value)
{
  if (value != this.checkbox) {
    this.checkbox = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'checkbox', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_DropDownList}
 */
MyInitialView.prototype.get_list = function()
{
  return this.list;
};


/**
 * @expose
 * @param {org_apache_flex_html_DropDownList} value
 */
MyInitialView.prototype.set_list = function(value)
{
  if (value != this.list) {
    this.list = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'list', null, value));
  }
};


/**
 * @expose
 * @return {org_apache_flex_html_ComboBox}
 */
MyInitialView.prototype.get_comboBox = function()
{
  return this.comboBox;
};


/**
 * @expose
 * @param {org_apache_flex_html_ComboBox} value
 */
MyInitialView.prototype.set_comboBox = function(value)
{
  if (value != this.comboBox) {
    this.comboBox = value;
    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'comboBox', null, value));
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
org_apache_flex_html_Label,
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
null, [org_apache_flex_binding_SimpleBinding, 5, '_id', true, '$ID0', 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org_apache_flex_html_TextButton,
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
org_apache_flex_html_TextButton,
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
org_apache_flex_html_Label,
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
org_apache_flex_html_List,
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
null, [org_apache_flex_binding_ConstantBinding, 4, '_id', true, '$ID3', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'cities', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH2,
null,
org_apache_flex_html_TextArea,
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
null, [org_apache_flex_binding_SimpleBinding, 5, '_id', true, '$ID4', 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org_apache_flex_html_TextInput,
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
org_apache_flex_html_TextButton,
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
org_apache_flex_html_CheckBox,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_RadioButton,
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
org_apache_flex_html_DropDownList,
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
null, [org_apache_flex_binding_ConstantBinding, 4, '_id', true, '$ID13', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'strings', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH4,
null,
org_apache_flex_html_TextButton,
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
org_apache_flex_html_ComboBox,
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
null, [org_apache_flex_binding_ConstantBinding, 4, '_id', true, '$ID15', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'cities', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
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


