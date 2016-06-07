package org.apache.flex.maven.flexjs;

/**
 * Created by christoferdutz on 01.05.16.
 */
public class Namespace {

    public static final String TYPE_DEFAULT = "default";
    public static final String TYPE_AS = "as";
    public static final String TYPE_JS = "js";

    private String type = "default";
    private String uri;
    private String manifest;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

}
