package net.h34t.temporize;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate-templates", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TemporizeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "temporize.inputpath", required = true)
    private String inputPath;

    @Parameter(property = "temporize.outputPath", defaultValue = "${project.build.directory}/generated-sources/temporize")
    private String outputPath;

    @Parameter(property = "temporize.modifier", required = true)
    private String modifier;

    @Override
    public void execute() throws MojoFailureException {
        getLog().info("Temporize template compilation");
        project.addCompileSourceRoot(outputPath);
        try {
            new Temporize()
                    .setLog(getLog())
                    .exec(
                            inputPath,
                            outputPath,
                            modifier);
        } catch (Exception e) {
            throw new MojoFailureException("An error occurred", e);
        }
    }
}
