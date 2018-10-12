package net.h34t.temporize;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "temporize")
public class TemporizeMojo extends AbstractMojo {

    @Parameter(property = "temporize.inputpath")
    private String inputPath;

    @Parameter(property = "temporize.inputpath")
    private String outputPath;

    @Parameter(property = "temporize.modifier")
    private String modifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("Hello, world.");

        Temporize.main(
                inputPath,
                outputPath,
                modifier);
    }
}
