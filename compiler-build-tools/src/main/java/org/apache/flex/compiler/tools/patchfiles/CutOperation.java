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
