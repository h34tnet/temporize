package net.h34t.temporize;

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

    public static void main(String... args) {
        boolean verbose = false;

        try {
            if (args.length < 2) {
                System.out.println("Usage: java -jar temporize.jar tpl/ output/ path/to/Modifiers.java");
                System.out.println("       Compiles all templates with a file name according to the pattern");
                System.out.println("       [name].temporize.[ending] into precompiled templates and saves");
                System.out.println("       the java source files to output.");

            } else {
                long st = System.nanoTime();

                File inDirectory = new File(args[0]);
                File outDirectory = new File(args[1]);
                String modifier = args.length > 2 ? args[2] : null;

                verbose = Arrays.stream(args).anyMatch(a -> a.equals("--verbose"));

                if (!inDirectory.exists()) {
                    System.err.println("Couldn't find input directory " + inDirectory.getName());
                    System.exit(1);
                }

                if ((!outDirectory.exists() && !outDirectory.mkdirs()) || !outDirectory.isDirectory()) {
                    System.err.println("Couldn't create output directory " + outDirectory.getName());
                    System.exit(1);
                }

                File temporizeDirectory = new File(outDirectory, "temporize");
                if (!temporizeDirectory.exists()) {
                    if (!temporizeDirectory.mkdir())
                        throw new IOException("Can't create temporize interface directory");
                }

                // copy the TemporizeTemplate file from the packaged resources into the output directory
                File temporizeInterface = new File(temporizeDirectory, "TemporizeTemplate.java");

                try (InputStream is = Temporize.class.getClassLoader().getResourceAsStream("TemporizeTemplate.java");
                     OutputStream os = new FileOutputStream(temporizeInterface)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }


                List<File> templates = findFilesRecursively(inDirectory);

                templates
                        .forEach(t -> {
                            TemplateFile tf = new TemplateFile(inDirectory, t);

                            System.out.println("processing " + tf.getFile().getPath());

                            try {

                                String packageName = tf.getPackageName();
                                String className = tf.getClassName();

                                System.out.printf("Class: %s.%s%n", packageName, className);

                                List<Token> tokens = Parser.FULL.parse(t);
                                ASTNode root = new ASTBuilder().build(tokens);
                                Template tpl = new Compiler().compile(packageName, className, modifier, root, inc -> {
                                    System.out.println(" * Includes " + inc);
                                });

                                File packageOutDirectory = tf.getOutputDirectory(outDirectory);
                                if (!packageOutDirectory.exists() && !packageOutDirectory.mkdirs()) {
                                    throw new IOException("Failed creating package directory " + packageOutDirectory.getPath());
                                }

                                try (FileWriter fw = new FileWriter(tf.getOutputFile(outDirectory))) {
                                    fw.write(tpl.code);
                                }

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        });

                long et = System.nanoTime();

                System.out.println("Done. Took " + (et - st) / 1000000 + "ms");

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

}
