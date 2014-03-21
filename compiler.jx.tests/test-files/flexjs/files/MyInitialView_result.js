/**
 * MyInitialView
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('MyInitialView');

goog.require('org.apache.flex.core.ViewBase');
goog.require('org.apache.flex.html.staticControls.Label');
goog.require('org.apache.flex.binding.SimpleBinding');
goog.require('org.apache.flex.html.staticControls.TextButton');
goog.require('org.apache.flex.html.staticControls.List');
goog.require('org.apache.flex.binding.ConstantBinding');
goog.require('org.apache.flex.html.staticControls.TextArea');
goog.require('org.apache.flex.html.staticControls.TextInput');
goog.require('org.apache.flex.html.staticControls.CheckBox');
goog.require('org.apache.flex.html.staticControls.RadioButton');
goog.require('org.apache.flex.html.staticControls.DropDownList');
goog.require('org.apache.flex.html.staticControls.ComboBox');
goog.require('org.apache.flex.events.CustomEvent');
goog.require('org.apache.flex.events.Event');
goog.require('org.apache.flex.utils.Timer');




/**
 * @constructor
 * @extends {org.apache.flex.core.ViewBase}
 */
MyInitialView = function() {
  goog.base(this);
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.Label}
   */
  this.lbl;
  
  /**
   * @private
   * @type {org.apache.flex.binding.SimpleBinding}
   */
  this.$ID0;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextButton}
   */
  this.$ID1;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextButton}
   */
  this.$ID2;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.Label}
   */
  this.timerLabel;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.List}
   */
  this.cityList;
  
  /**
   * @private
   * @type {org.apache.flex.binding.ConstantBinding}
   */
  this.$ID3;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextArea}
   */
  this.$ID5;
  
  /**
   * @private
   * @type {org.apache.flex.binding.SimpleBinding}
   */
  this.$ID4;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextInput}
   */
  this.input;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextButton}
   */
  this.$ID6;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.CheckBox}
   */
  this.checkbox;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID7;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID8;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID9;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID10;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID11;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.RadioButton}
   */
  this.$ID12;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.DropDownList}
   */
  this.list;
  
  /**
   * @private
   * @type {org.apache.flex.binding.ConstantBinding}
   */
  this.$ID13;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.TextButton}
   */
  this.$ID14;
  
  /**
   * @private
   * @type {org.apache.flex.html.staticControls.ComboBox}
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
 * @return {org.apache.flex.html.staticControls.Label}
 */
MyInitialView.prototype.get_lbl = function()
{
  return this.lbl;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.Label} value
 */
MyInitialView.prototype.set_lbl = function(value)
{
  if (value != this.lbl)
    this.lbl = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.Label}
 */
MyInitialView.prototype.get_timerLabel = function()
{
  return this.timerLabel;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.Label} value
 */
MyInitialView.prototype.set_timerLabel = function(value)
{
  if (value != this.timerLabel)
    this.timerLabel = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.List}
 */
MyInitialView.prototype.get_cityList = function()
{
  return this.cityList;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.List} value
 */
MyInitialView.prototype.set_cityList = function(value)
{
  if (value != this.cityList)
    this.cityList = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.TextInput}
 */
MyInitialView.prototype.get_input = function()
{
  return this.input;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.TextInput} value
 */
MyInitialView.prototype.set_input = function(value)
{
  if (value != this.input)
    this.input = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.CheckBox}
 */
MyInitialView.prototype.get_checkbox = function()
{
  return this.checkbox;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.CheckBox} value
 */
MyInitialView.prototype.set_checkbox = function(value)
{
  if (value != this.checkbox)
    this.checkbox = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.DropDownList}
 */
MyInitialView.prototype.get_list = function()
{
  return this.list;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.DropDownList} value
 */
MyInitialView.prototype.set_list = function(value)
{
  if (value != this.list)
    this.list = value;
};


/**
 * @expose
 * @return {org.apache.flex.html.staticControls.ComboBox}
 */
MyInitialView.prototype.get_comboBox = function()
{
  return this.comboBox;
};


/**
 * @expose
 * @param {org.apache.flex.html.staticControls.ComboBox} value
 */
MyInitialView.prototype.set_comboBox = function(value)
{
  if (value != this.comboBox)
    this.comboBox = value;
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
    var arr = goog.base(this, 'get_MXMLDescriptor');
    /** @type {Array} */
    var data = [
org.apache.flex.html.staticControls.Label,
4,
'id',
true,
'lbl',
'y',
true,
25,
'x',
true,
100,
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, '_id', true, '$ID0', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'eventName', true, 'labelTextChanged', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'_id',
true,
'$ID1',
'text',
true,
'Let\'s Start Timer',
'y',
true,
75,
'x',
true,
100,
0,
1,
'click',
this.$EH0,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'_id',
true,
'$ID2',
'text',
true,
'Stop Timer',
'y',
true,
100,
'x',
true,
100,
0,
1,
'click',
this.$EH1,
null,
org.apache.flex.html.staticControls.Label,
3,
'id',
true,
'timerLabel',
'y',
true,
125,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.List,
6,
'id',
true,
'cityList',
'height',
true,
75,
'width',
true,
100,
'y',
true,
75,
'x',
true,
200,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID3', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'cities', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH2,
null,
org.apache.flex.html.staticControls.TextArea,
6,
'_id',
true,
'$ID5',
'height',
true,
75,
'width',
true,
150,
'y',
true,
25,
'x',
true,
320,
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, '_id', true, '$ID4', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'eventName', true, 'labelTextChanged', 'destinationPropertyName', true, 'text', 0, 0, null],
0,
0,
null,
org.apache.flex.html.staticControls.TextInput,
3,
'id',
true,
'input',
'y',
true,
110,
'x',
true,
320,
0,
0,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'_id',
true,
'$ID6',
'text',
true,
'Transfer',
'y',
true,
138,
'x',
true,
320,
0,
1,
'click',
this.$EH3,
null,
org.apache.flex.html.staticControls.CheckBox,
4,
'id',
true,
'checkbox',
'text',
true,
'Check Me',
'y',
true,
170,
'x',
true,
320,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
'_id',
true,
'$ID7',
'text',
true,
'Apples',
'groupName',
true,
'group1',
'value',
true,
0,
'y',
true,
150,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
7,
'_id',
true,
'$ID8',
'selected',
true,
true,
'text',
true,
'Oranges',
'groupName',
true,
'group1',
'value',
true,
1,
'y',
true,
170,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
'_id',
true,
'$ID9',
'text',
true,
'Grapes',
'groupName',
true,
'group1',
'value',
true,
2,
'y',
true,
190,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
7,
'_id',
true,
'$ID10',
'selected',
true,
true,
'text',
true,
'Red',
'groupName',
true,
'group2',
'value',
true,
16711680,
'y',
true,
250,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
'_id',
true,
'$ID11',
'text',
true,
'Green',
'groupName',
true,
'group2',
'value',
true,
32768,
'y',
true,
270,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
'_id',
true,
'$ID12',
'text',
true,
'Blue',
'groupName',
true,
'group2',
'value',
true,
255,
'y',
true,
290,
'x',
true,
100,
0,
0,
null,
org.apache.flex.html.staticControls.DropDownList,
6,
'id',
true,
'list',
'height',
true,
24,
'width',
true,
100,
'y',
true,
200,
'x',
true,
200,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID13', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'strings', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH4,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'_id',
true,
'$ID14',
'text',
true,
'OK',
'y',
true,
230,
'x',
true,
200,
0,
1,
'click',
this.$EH5,
null,
org.apache.flex.html.staticControls.ComboBox,
5,
'id',
true,
'comboBox',
'width',
true,
100,
'y',
true,
200,
'x',
true,
320,
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


