package net.h34t.temporize;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Temporize {

    private Log log;

    public static void main(String... args) {
        boolean verbose = false;

        try {
            if (args.length < 2) {
                System.out.println("Usage: java -jar temporize.jar tpl/ output/ path/to/Modifiers.java");
                System.out.println("       Compiles all templates with a file name according to the pattern");
                System.out.println("       [name].temporize.[ending] into precompiled templates and saves");
                System.out.println("       the java source files to output.");

            } else {
                String inDir = args[0];
                String outDir = args[1];
                String modifier = args.length > 2 ? args[2] : null;
                verbose = Arrays.asList(args).contains("--verbose");

                new Temporize().exec(inDir, outDir, modifier);
            }

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());

            if (verbose)
                e.printStackTrace();

            System.exit(1);
        }
    }

    private static List<File> findFilesRecursively(File directory) {
        List<File> templates = new ArrayList<>();

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().matches("^.+\\.temporize\\..*$")) {
                    templates.add(file);

                } else if (file.isDirectory()) {
                    templates.addAll(findFilesRecursively(file));
                }
            }
        }

        return templates;
    }

    public Temporize setLog(Log log) {
        this.log = log;
        return this;
    }

    public void exec(String inDir, String outDir, String modifier) throws IOException {
        if (this.log == null)
            this.log = new SystemStreamLog();

        long st = System.nanoTime();

        File inDirectory = new File(inDir);
        File outDirectory = new File(outDir);


        if (!inDirectory.exists()) {
            log.error("Couldn't find input directory " + inDirectory.getName());
            System.exit(1);
        }

        if ((!outDirectory.exists() && !outDirectory.mkdirs()) || !outDirectory.isDirectory()) {
            log.error("Couldn't create output directory " + outDirectory.getName());
            System.exit(1);
        }

        File temporizeDirectory = new File(outDirectory, "temporize");
        if (!temporizeDirectory.exists()) {
            if (!temporizeDirectory.mkdir())
                throw new IOException("Can't create temporize interface directory");
        }

        // copy the TemporizeTemplate file from the packaged resources into the output directory
        // but only if it doesn't exist yet
        File temporizeInterface = new File(temporizeDirectory, "TemporizeTemplate.java");

        if (!temporizeInterface.exists()) {
            try (InputStream is = Temporize.class.getClassLoader().getResourceAsStream("TemporizeTemplate.java");
                 OutputStream os = new FileOutputStream(temporizeInterface)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }

        // gather all template files
        List<File> templates = findFilesRecursively(inDirectory);

        // compile each template
        templates
                .forEach(t -> {
                    TemplateFile tf = new TemplateFile(inDirectory, t);

                    log.info("processing " + tf.getFile().getPath());

                    try {
                        String packageName = tf.getPackageName();
                        String className = tf.getClassName();

                        log.info(String.format("Class: %s.%s", packageName, className));

                        // parse source file
                        List<Token> tokens = Parser.FULL.parse(t);

                        // build AST
                        ASTNode root = new ASTBuilder().build(tokens);

                        // compile
                        Template tpl = new Compiler().compile(packageName, className, modifier, root,
                                inc -> log.info(" * Includes " + inc));

                        // create the output directory
                        File packageOutDirectory = tf.getOutputDirectory(outDirectory);
                        if (!packageOutDirectory.exists() && !packageOutDirectory.mkdirs()) {
                            throw new IOException("Failed creating package directory " + packageOutDirectory.getPath());
                        }

                        // write the template to a java source file
                        try (FileWriter fw = new FileWriter(tf.getOutputFile(outDirectory))) {
                            fw.write(tpl.code);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        long et = System.nanoTime();

        log.info("Done. Took " + (et - st) / 1_000_000 + "ms");
    }

}
