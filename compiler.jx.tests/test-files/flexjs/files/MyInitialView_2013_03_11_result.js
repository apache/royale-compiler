goog.provide('MyInitialView_2013_03_11');

goog.require('org.apache.flex.binding.ConstantBinding');
goog.require('org.apache.flex.binding.SimpleBinding');
goog.require('org.apache.flex.core.ViewBase');
goog.require('org.apache.flex.html.staticControls.CheckBox');
goog.require('org.apache.flex.html.staticControls.Label');
goog.require('org.apache.flex.html.staticControls.List');
goog.require('org.apache.flex.html.staticControls.RadioButton');
goog.require('org.apache.flex.html.staticControls.TextArea');
goog.require('org.apache.flex.html.staticControls.TextButton');
goog.require('org.apache.flex.html.staticControls.TextInput');

/**
 * @constructor
 * @extends {org.apache.flex.core.ViewBase}
 */
MyInitialView_2013_03_11 = function() {
    org.apache.flex.core.ViewBase.call(this);

    /**
     * @private
     * @type {org.apache.flex.binding.SimpleBinding}
     */
    this.$ID1;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.Label}
     */
    this.lbl;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.TextButton}
     */
    this.$ID2;

    /**
     * @private
     * @type {org.apache.flex.binding.ConstantBinding}
     */
    this.$ID4;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.List}
     */
    this.list;

    /**
     * @private
     * @type {org.apache.flex.binding.SimpleBinding}
     */
    this.$ID6;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.TextArea}
     */
    this.$ID7;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.TextInput}
     */
    this.input;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.TextButton}
     */
    this.$ID8;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.CheckBox}
     */
    this.checkbox;

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
     * @type {org.apache.flex.html.staticControls.RadioButton}
     */
    this.$ID13;

    /**
     * @private
     * @type {org.apache.flex.html.staticControls.RadioButton}
     */
    this.$ID14;

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
 * @return {Label}
 */
MyInitialView_2013_03_11.prototype.get_lbl = function()
{
    return this.lbl;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {Label} value
 */
MyInitialView_2013_03_11.prototype.set_lbl = function(value)
{
    if (value != this.lbl)
        this.lbl = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {List}
 */
MyInitialView_2013_03_11.prototype.get_list = function()
{
    return this.list;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {List} value
 */
MyInitialView_2013_03_11.prototype.set_list = function(value)
{
    if (value != this.list)
        this.list = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {TextInput}
 */
MyInitialView_2013_03_11.prototype.get_input = function()
{
    return this.input;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {TextInput} value
 */
MyInitialView_2013_03_11.prototype.set_input = function(value)
{
    if (value != this.input)
        this.input = value;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @return {CheckBox}
 */
MyInitialView_2013_03_11.prototype.get_checkbox = function()
{
    return this.checkbox;
};

/**
 * @this {MyInitialView_2013_03_11}
 * @param {CheckBox} value
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
'x',
true,
100,
'y',
true,
25,
'id',
true,
'lbl',
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', '_id', true, '$ID1', 0, 0, null],
0,
0,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'text',
true,
'OK',
'x',
true,
100,
'y',
true,
50,
'_id',
true,
'$ID2',
0,
1,
'click',
this.$EH0,
null,
org.apache.flex.html.staticControls.List,
6,
'x',
true,
200,
'y',
true,
25,
'width',
true,
100,
'height',
true,
200,
'id',
true,
'list',
'beads',
null, [org.apache.flex.binding.ConstantBinding, 4, 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'strings', 'destinationPropertyName', true, 'dataProvider', '_id', true, '$ID4', 0, 0, null],
0,
1,
'change',
this.$EH1,
null,
org.apache.flex.html.staticControls.TextArea,
6,
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
'_id',
true,
'$ID7',
'beads',
null, [org.apache.flex.binding.SimpleBinding, 5, 'eventName', true, 'labelTextChanged', 'sourceID', true, 'applicationModel', 'sourcePropertyName', true, 'labelText', 'destinationPropertyName', true, 'text', '_id', true, '$ID6', 0, 0, null],
0,
0,
null,
org.apache.flex.html.staticControls.TextInput,
3,
'x',
true,
320,
'y',
true,
110,
'id',
true,
'input',
0,
0,
null,
org.apache.flex.html.staticControls.TextButton,
4,
'text',
true,
'Transfer',
'x',
true,
320,
'y',
true,
138,
'_id',
true,
'$ID8',
0,
1,
'click',
this.$EH2,
null,
org.apache.flex.html.staticControls.CheckBox,
4,
'x',
true,
320,
'y',
true,
170,
'text',
true,
'Check Me',
'id',
true,
'checkbox',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
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
130,
'_id',
true,
'$ID9',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
7,
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
150,
'selected',
true,
true,
'_id',
true,
'$ID10',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
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
170,
'_id',
true,
'$ID11',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
7,
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
230,
'selected',
true,
true,
'_id',
true,
'$ID12',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
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
250,
'_id',
true,
'$ID13',
0,
0,
null,
org.apache.flex.html.staticControls.RadioButton,
6,
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
270,
'_id',
true,
'$ID14',
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
0,
0,
0
];

         if (arr)
             this.mxmldp = arr.concat(data);
         else
             this.mxmldp = data;
    }
    return this.mxmldp;
};

