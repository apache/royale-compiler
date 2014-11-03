package org.apache.flex.swf.tags;

import org.apache.flex.swf.TagType;

/**
 * Represents a <code>EnableTelemetry</code> tag in a SWF file.
 * <p>
 * The EnableTelemetry tag instructs the flash runtime to provide advanced telemetry options.
 */
public class EnableTelemetryTag extends Tag {

    /**
     * Constructor.
     */
    public EnableTelemetryTag() {
        super(TagType.EnableTelemetry);
    }

    /**
     * Constructor with initialization.
     */
    public EnableTelemetryTag(String password) {
        this();
        this.password = password;
    }


    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
