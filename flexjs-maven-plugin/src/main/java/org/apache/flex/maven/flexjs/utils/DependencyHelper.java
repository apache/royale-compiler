package org.apache.flex.maven.flexjs.utils;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.*;
import org.eclipse.aether.RepositorySystemSession;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by christoferdutz on 18.05.16.
 *
 * TODO: Change this to a real component ... statics suck.
 */
public class DependencyHelper {

    public static List<Artifact> getAllLibraries(MavenProject project, RepositorySystemSession repositorySystemSession,
                                                 ProjectDependenciesResolver projectDependenciesResolver) throws MojoExecutionException {
        DefaultDependencyResolutionRequest dependencyResolutionRequest =
                new DefaultDependencyResolutionRequest(project, repositorySystemSession);
        DependencyResolutionResult dependencyResolutionResult;

        try {
            dependencyResolutionResult = projectDependenciesResolver.resolve(dependencyResolutionRequest);
        } catch (DependencyResolutionException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        List<Artifact> artifacts = new LinkedList<Artifact>();
        if (dependencyResolutionResult.getDependencyGraph() != null
                && !dependencyResolutionResult.getDependencyGraph().getChildren().isEmpty()) {
            RepositoryUtils.toArtifacts(artifacts, dependencyResolutionResult.getDependencyGraph().getChildren(),
                    Collections.singletonList(project.getArtifact().getId()), null);
        }
        return artifacts;
    }

}
