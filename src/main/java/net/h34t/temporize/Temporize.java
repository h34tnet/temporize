package net.h34t.temporize;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class Temporize {

    private Log log;

    public Temporize setLog(Log log) {
        this.log = log;
        return this;
    }

    public void exec(String inDir, String outDir, String modifier) throws IOException {
        if (this.log == null)
            this.log = new SystemStreamLog();

        long st = System.nanoTime();

        Path inDirectory = Paths.get(inDir);
        Path outDirectory = Paths.get(outDir);

        // validate import directory
        if (!Files.exists(inDirectory) || !Files.isDirectory(inDirectory)) {
            throw new RuntimeException("Couldn't find input directory " + inDirectory.toString());
        }

        PathMatcher pm = inDirectory.getFileSystem().getPathMatcher("glob:**/*.temporize.*");

        // gather all template files and compile them
        List<CompiledTemplate> compiledTemplates = Files.find(inDirectory, 64, (f, a) -> pm.matches(f))
                .map(t -> {
                    TemplateFile tf = new TemplateFile(inDirectory, t);

                    log.info("processing " + tf.getFile().toString());

                    try {
                        String packageName = tf.createPackageName();
                        String className = tf.getClassName();

                        log.info(String.format("Class: %s.%s", packageName, className));

                        // parse source file
                        List<Token> tokens = Parser.FULL.parse(t);

                        // build AST
                        ASTNode root = new ASTBuilder().build(tokens);

                        // compile
                        Template tpl = new Compiler().compile(packageName, className, modifier, root,
                                inc -> log.info(" * Includes " + inc));

                        return new CompiledTemplate(tf, tpl);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        // validate and/or create output directory
        if (Files.exists(outDirectory)) {
            if (!Files.isDirectory(outDirectory))
                throw new RuntimeException("Output destination " + outDirectory.toString() + " is not a directory");

        } else if (!Files.exists(outDirectory)) {
            try {
                Files.createDirectories(outDirectory);
            } catch (IOException ioe) {
                throw new RuntimeException("Couldn't create output directory " + outDirectory.toString() + ": " + ioe.getMessage());
            }
        }

        Path temporizeDirectory = outDirectory.resolve("net/h34t/temporize");
        if (!Files.exists(temporizeDirectory)) {
            try {
                Files.createDirectories(temporizeDirectory);
            } catch (IOException ioe) {
                throw new IOException("Can't create temporize interface directory");
            }
        }

        // copy the TemporizeTemplate file from the packaged resources into the output directory
        // but only if it doesn't exist yet
        Path temporizeInterface = temporizeDirectory.resolve("TemporizeTemplate.java");

        if (!Files.exists(temporizeInterface)) {
            Files.copy(
                    Temporize.class.getClassLoader().getResourceAsStream("net/h34t/temporize/TemporizeTemplate.java"),
                    temporizeInterface);
        }

        // first, try to create all output directories
        compiledTemplates.stream()
                .map(ct -> ct.templateFile.getOutputDirectory(outDirectory))
                .distinct()
                .forEach(dir -> {
                    try {
                        Files.createDirectories(dir);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create output directory " + dir, e);
                    }
                });

        // finally, create all files
        compiledTemplates.forEach(t -> {
            try {
                Path file = t.templateFile.getOutputFile(outDirectory);
                Files.write(file, t.template.code.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new RuntimeException("Failed writing the output files", e);
            }
        });

        long et = System.nanoTime();

        log.info("Done. Took " + (et - st) / 1_000_000 + "ms");
    }

    private static class CompiledTemplate {

        final TemplateFile templateFile;
        final Template template;

        CompiledTemplate(TemplateFile templateFile, Template template) {
            this.templateFile = templateFile;
            this.template = template;
        }
    }
}
