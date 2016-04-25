package org.apache.flex.maven.flexjs;

import org.apache.flex.maven.flexjs.trust.TrustHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.io.File;

/**
 * This little mojo simply adds the maven build output directory to the
 * list of trusted sources for the FlashPlayer.
 *
 * Created by christoferdutz on 17.04.16.
 */
@Mojo(name="trust",defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class TrustMojo
        extends AbstractMojo
{

    @Parameter(defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Inject
    private TrustHandler securityHandler;

    public void execute()
            throws MojoExecutionException {
        securityHandler.trustDirectory(outputDirectory);
    }

}
