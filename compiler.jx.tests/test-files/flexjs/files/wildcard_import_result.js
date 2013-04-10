goog.provide('wildcard_import');

goog.require('org.apache.flex.core.Application');
goog.require('org.apache.flex.html.staticControls.Button');

/**
 * @constructor
 * @extends {org.apache.flex.core.Application}
 */
wildcard_import = function() {
	goog.base(this);
	
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


wildcard_import.prototype.tmp = function() {
	var self = this;
	var /** @type {Button} */ self.myButton;
	self.myButton = new org.apache.flex.html.staticControls.Button();
};

/**
 * @override
 * @this {wildcard_import}
 * @return {Array} the Array of UI element descriptors.
 */
wildcard_import.prototype.get_MXMLDescriptor = function()
{
	if (this.mxmldd == undefined)
	{
		/** @type {Array} */
		var arr = goog.base(this, 'get_MXMLDescriptor');
		/** @type {Array} */
		var data = [
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
 * @this {wildcard_import}
 * @return {Array} the Array of UI element descriptors.
 */
wildcard_import.prototype.get_MXMLProperties = function()
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

