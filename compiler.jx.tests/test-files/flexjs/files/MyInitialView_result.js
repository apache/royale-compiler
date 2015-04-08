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
  this.lbl_;
  
  /**
   * @private
   * @type {org_apache_flex_binding_SimpleBinding}
   */
  this.$ID0_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID1_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID2_;
  
  /**
   * @private
   * @type {org_apache_flex_html_Label}
   */
  this.timerLabel_;
  
  /**
   * @private
   * @type {org_apache_flex_html_List}
   */
  this.cityList_;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
   */
  this.$ID3_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextArea}
   */
  this.$ID5_;
  
  /**
   * @private
   * @type {org_apache_flex_binding_SimpleBinding}
   */
  this.$ID4_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextInput}
   */
  this.input_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID6_;
  
  /**
   * @private
   * @type {org_apache_flex_html_CheckBox}
   */
  this.checkbox_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID7_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID8_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID9_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID10_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID11_;
  
  /**
   * @private
   * @type {org_apache_flex_html_RadioButton}
   */
  this.$ID12_;
  
  /**
   * @private
   * @type {org_apache_flex_html_DropDownList}
   */
  this.list_;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
   */
  this.$ID13_;
  
  /**
   * @private
   * @type {org_apache_flex_html_TextButton}
   */
  this.$ID14_;
  
  /**
   * @private
   * @type {org_apache_flex_html_ComboBox}
   */
  this.comboBox_;
  
  /**
   * @private
   * @type {org_apache_flex_binding_ConstantBinding}
   */
  this.$ID15_;
  
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


;


;


;


;


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
  this.timerLabel.text = this.timer.currentCount.toString();
};


Object.defineProperties(MyInitialView.prototype, /** @lends {MyInitialView.prototype} */ {
/** @expose */
comboBoxValue: {
get: /** @this {MyInitialView} */ function() {
  return String(this.comboBox.selectedItem);
}},
/** @expose */
inputText: {
get: /** @this {MyInitialView} */ function() {
  return this.input.text;
}},
/** @expose */
symbol: {
get: /** @this {MyInitialView} */ function() {
  return org_apache_flex_utils_Language.as(this.list.selectedItem, String);
}},
/** @expose */
city: {
get: /** @this {MyInitialView} */ function() {
  return org_apache_flex_utils_Language.as(this.cityList.selectedItem, String);
}}}
);/**
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


Object.defineProperties(MyInitialView.prototype, /** @lends {MyInitialView.prototype} */ {
/** @expose */
    lbl: {
    /** @this {MyInitialView} */
    get: function() {
      return this.lbl_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.lbl_) {
        this.lbl_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'lbl', null, value));
      }
    }
  },
  /** @expose */
    timerLabel: {
    /** @this {MyInitialView} */
    get: function() {
      return this.timerLabel_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.timerLabel_) {
        this.timerLabel_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'timerLabel', null, value));
      }
    }
  },
  /** @expose */
    cityList: {
    /** @this {MyInitialView} */
    get: function() {
      return this.cityList_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.cityList_) {
        this.cityList_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'cityList', null, value));
      }
    }
  },
  /** @expose */
    input: {
    /** @this {MyInitialView} */
    get: function() {
      return this.input_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.input_) {
        this.input_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'input', null, value));
      }
    }
  },
  /** @expose */
    checkbox: {
    /** @this {MyInitialView} */
    get: function() {
      return this.checkbox_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.checkbox_) {
        this.checkbox_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'checkbox', null, value));
      }
    }
  },
  /** @expose */
    list: {
    /** @this {MyInitialView} */
    get: function() {
      return this.list_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.list_) {
        this.list_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'list', null, value));
      }
    }
  },
  /** @expose */
    comboBox: {
    /** @this {MyInitialView} */
    get: function() {
      return this.comboBox_;
    },
    /** @this {MyInitialView} */
    set: function(value) {
      if (value != this.comboBox_) {
        this.comboBox_ = value;
        this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(this, 'comboBox', null, value));
      }
    }
  },
  'MXMLDescriptor': {
    /** @this {MyInitialView} */
    get: function() {
      {
        if (this.mxmldd == undefined)
        {
          /** @type {Array} */
          var arr = org_apache_flex_utils_Language.superGetter(MyInitialView,this, 'MXMLDescriptor');
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
      }
      }
    }
  });
  