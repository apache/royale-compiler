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

package org.apache.royale.compiler.internal.codegen.js.goog;

import com.google.javascript.jscomp.Region;
import com.google.javascript.jscomp.SourceFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 */
public class JarSourceFile extends SourceFile {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3821969963886712441L;
	private String fileName;
    private boolean isExtern;
    private String code;

    public JarSourceFile(String fileName, String code, boolean isExtern) {
        super(fileName, isExtern ? SourceKind.EXTERN : SourceKind.STRONG);
        this.fileName = fileName;
        this.isExtern = isExtern;
        this.code = code;
    }

    @Override
    public int getLineOffset(int lineno) {
        return super.getLineOffset(lineno);
    }

    @Override
    public String getCode() throws IOException {
        return code;
    }

    @Override
    public Reader getCodeReader() throws IOException {
        return new StringReader(code);
    }

    @Override
    public String getOriginalPath() {
        return fileName;
    }

    @Override
    public void setOriginalPath(String originalPath) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clearCachedSource() {
        // Ignore as we don't do caching.
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public boolean isExtern() {
        return isExtern;
    }

    @Override
    public int getLineOfOffset(int offset) {
        return super.getLineOfOffset(offset);
    }

    @Override
    public int getColumnOfOffset(int offset) {
        return super.getColumnOfOffset(offset);
    }

    @Override
    public String getLine(int lineNumber) {
        return super.getLine(lineNumber);
    }

    @Override
    public Region getRegion(int lineNumber) {
        return super.getRegion(lineNumber);
    }

    @Override
    public String toString() {
        return fileName;
    }

}
