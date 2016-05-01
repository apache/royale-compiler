package org.apache.flex.maven.flexjs;

import org.apache.flex.tools.FlexTool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;

import java.io.File;

/**
 * Created by christoferdutz on 30.04.16.
 */
public abstract class BaseCompileMojo extends BaseMojo {

    @Override
    protected String getToolGroupName() {
        return "Falcon";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_COMPC;
    }

}
