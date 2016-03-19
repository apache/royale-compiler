package org.apache.flex.compiler.tools.patchfiles;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by christoferdutz on 16.03.16.
 */
public class ReplaceOperation extends Operation {

    private String token;
    private String value;

    public ReplaceOperation() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void perform(File file) throws IOException {
        String content = IOUtils.toString(new FileInputStream(file), "UTF-8");
        content = content.replaceAll(Pattern.quote(token), Matcher.quoteReplacement((value == null) ? "" : value));
        IOUtils.write(content, new FileOutputStream(file), "UTF-8");
    }

}
