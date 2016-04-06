/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/**
 * @enum {string}
 * @see http://dev.w3.org/csswg/css-font-loading/#enumdef-fontfaceloadstatus
 */
var FontFaceLoadStatus = {
 ERROR: 'error',
 LOADED: 'loaded',
 LOADING: 'loading',
 UNLOADED: 'unloaded'
};

/**
 * @enum
 * @see http://dev.w3.org/csswg/css-font-loading/#enumdef-fontfacesetloadstatus
 */
var FontFaceSetLoadStatus = {
 FOO_LOADED: 'loaded',
 FOO_LOADING: 'loading'
};

/** @const */
var foo = {};
/** @const */
foo.bar = {};
/** @const */
foo.bar.baz = {};

/**
 * @enum
 */
foo.bar.baz.QualifiedEnum = {
 One: '1',
 Two: '2'
};








