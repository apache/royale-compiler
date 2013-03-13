goog.provide('MyInitialView_2013_03_11');

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

/**
 * @constructor
 * @extends {org.apache.flex.core.ViewBase}
 */
MyInitialView_2013_03_11 = function() {
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
     * @type {org.apache.flex.html.staticControls.List}
     */
    this.list;

    /**
     * @private
     * @type {org.apache.flex.binding.ConstantBinding}
     */
    this.$ID2;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.TextArea}
     */
    this.$ID3;

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
    this.$ID5;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.CheckBox}
     */
    this.checkbox;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.RadioButton}
     */
    this.$ID6;

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
     * @type {Array}
     */
    this.mxmldd;

    /**
     * @private
     * @type {Array}
     */
    this.mxmldp;
};
goog.inherits(MyInitialView_2013_03_11, org.apache.flex.core.ViewBase);

/**
 * @this {MyInitialView_2013_03_11}
 * @expose
 * @return {string}
 */
MyInitialView_2013_03_11.prototype.get_symbol = function()
{
        return this.list.get_selectedItem() /* as String */;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @expose
 * @return {string}
 */
MyInitialView_2013_03_11.prototype.get_inputText = function()
{
        return this.input.get_text();
};

/**
 * @this {MyInitialView_2013_03_11}
 * @expose
 * @param {flash.events.MouseEvent} event
 */
MyInitialView_2013_03_11.prototype.$EH0 = function(event)
{
        this.dispatchEvent(org.apache.flex.FlexGlobal.newObject(flash.events.Event, ["buttonClicked"]));
};

/**
 * @this {MyInitialView_2013_03_11}
 * @expose
 * @param {flash.events.Event} event
 */
MyInitialView_2013_03_11.prototype.$EH1 = function(event)
{
        this.dispatchEvent(org.apache.flex.FlexGlobal.newObject(flash.events.Event, ["listChanged"]));
};

/**
 * @this {MyInitialView_2013_03_11}
 * @expose
 * @param {flash.events.MouseEvent} event
 */
MyInitialView_2013_03_11.prototype.$EH2 = function(event)
{
        this.dispatchEvent(org.apache.flex.FlexGlobal.newObject(flash.events.Event, ["transferClicked"]));
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {org.apache.flex.html.staticControls.Label}
 */
MyInitialView_2013_03_11.prototype.get_lbl = function()
{
    return this.lbl;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {org.apache.flex.html.staticControls.Label} value
 */
MyInitialView_2013_03_11.prototype.set_lbl = function(value)
{
    if (value != this.lbl)
        this.lbl = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {org.apache.flex.html.staticControls.List}
 */
MyInitialView_2013_03_11.prototype.get_list = function()
{
    return this.list;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {org.apache.flex.html.staticControls.List} value
 */
MyInitialView_2013_03_11.prototype.set_list = function(value)
{
    if (value != this.list)
        this.list = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {org.apache.flex.html.staticControls.TextInput}
 */
MyInitialView_2013_03_11.prototype.get_input = function()
{
    return this.input;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {org.apache.flex.html.staticControls.TextInput} value
 */
MyInitialView_2013_03_11.prototype.set_input = function(value)
{
    if (value != this.input)
        this.input = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {org.apache.flex.html.staticControls.CheckBox}
 */
MyInitialView_2013_03_11.prototype.get_checkbox = function()
{
    return this.checkbox;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {org.apache.flex.html.staticControls.CheckBox} value
 */
MyInitialView_2013_03_11.prototype.set_checkbox = function(value)
{
    if (value != this.checkbox)
        this.checkbox = value;
};

/**
 * @override
 * @this {MyInitialView_2013_03_11}
 * @return {Array} the Array of UI element descriptors.
 */
MyInitialView_2013_03_11.prototype.get_MXMLDescriptor = function()
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
'OK',
'y',
true,
50,
'x',
true,
100,
0,
1,
'click',
this.$EH0,
null,
org.apache.flex.html.staticControls.List,
6,
'id',
true,
'list',
'height',
true,
200,
'width',
true,
100,
'y',
true,
25,
'x',
true,
200,
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, '_id', true, '$ID2', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'strings', 'destinationPropertyName', true, 'dataProvider', 0, 0, null],
0,
1,
'change',
this.$EH1,
null,
org.apache.flex.html.staticControls.TextArea,
6,
'_id',
true,
'$ID3',
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
'$ID5',
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
this.$EH2,
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
'$ID6',
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
130,
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
'$ID7',
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
150,
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
'$ID8',
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
170,
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
'$ID9',
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
230,
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
'$ID10',
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
'Blue',
'groupName',
true,
'group2',
'value',
true,
255,
'y',
true,
270,
'x',
true,
100,
0,
0,
null
];

         if (arr)
             this.mxmldd = arr.concat(data);
         else
             this.mxmldd = data;
    }
    return this.mxmldd;
};

/**
 * @override
 * @this {MyInitialView_2013_03_11}
 * @return {Array} the Array of UI element descriptors.
 */
MyInitialView_2013_03_11.prototype.get_MXMLProperties = function()
{
    if (this.mxmldp == undefined)
    {
         /** @type {Array} */
         var arr = goog.base(this, 'get_MXMLProperties');
         /** @type {Array} */
         var data = [
];

         if (arr)
             this.mxmldp = arr.concat(data);
         else
             this.mxmldp = data;
    }
    return this.mxmldp;
};
