/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flex.compiler.tools.patchfiles;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by christoferdutz on 16.03.16.
 */
public class CutOperation extends Operation {

    private Integer startCuttingLine = -1;
    private Integer stopCuttingLine = -1;

    public CutOperation() {
    }

    public Integer getStartCuttingLine() {
        return startCuttingLine;
    }

    public void setStartCuttingLine(Integer startCuttingLine) {
        this.startCuttingLine = startCuttingLine;
    }

    public Integer getStopCuttingLine() {
        return stopCuttingLine;
    }

    public void setStopCuttingLine(Integer stopCuttingLine) {
        this.stopCuttingLine = stopCuttingLine;
    }

    @Override
    public void perform(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            for (int lineNumber = 1; (line = reader.readLine()) != null; lineNumber++) {
                if((startCuttingLine > lineNumber) || (lineNumber > stopCuttingLine))  {
                    sb.append(line).append("\n");
                }
            }
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
        IOUtils.write(sb.toString(), new FileOutputStream(file), "UTF-8");
    }

}
