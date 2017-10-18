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

package org.apache.royale.compiler.css;

/**
 * Source type enumerations for {@link ICSSFontFace}.
 */
public enum FontFaceSourceType
{
    /**
     * The source value is a URL of the font filename. For example: <br>
     * <code>src: url("../assets/MyriadWebPro.ttf"); </code>
     */
    URL,

    /**
     * The source value is the system font name. For example: <br>
     * <code>src: local("Myriad Web Pro");</code>
     */
    LOCAL;
}
