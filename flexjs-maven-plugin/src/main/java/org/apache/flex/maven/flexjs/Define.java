package org.apache.flex.maven.flexjs;

/**
 * Created by christoferdutz on 07.06.16.
 */
public class Define {

    private String name;
    private String value;

    public Define() {
    }

    public Define(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
