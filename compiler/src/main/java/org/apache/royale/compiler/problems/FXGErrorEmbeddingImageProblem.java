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

package org.apache.royale.compiler.problems;

/**
 * Problem generated when an error is occurred while embedding 
 * an image by an FXG file.
 */
public final class FXGErrorEmbeddingImageProblem extends FXGProblem
{
    public static final String DESCRIPTION = "Error '${error}' occurred while embedding image: '${image}'.";
    public static final int errorCode = 1368;
    public final String image;
    public final String error;
    
    public FXGErrorEmbeddingImageProblem(String filePath, int line, int column, String image, String error)
    {
        super(filePath, line, column);
        this.image = image;
        this.error = error;
    }
}
