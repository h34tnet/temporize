package net.h34t.temporize;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Temporize {

    public static void main(String... args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar temporize.jar tpl/ output/ path/to/Modifiers.java");
            System.out.println("       Compiles all templates with filename according to the pattern");
            System.out.println("       [name].temporize.[ending] into precompiled templates and saves");
            System.out.println("       the java source files to output.");

        } else {
            File inDirectory = new File(args[0]);
            File outDirectory = new File(args[1]);
            String modifier = args[2];

            if (!inDirectory.exists()) {
                System.err.println("Couldn't find input directory " + inDirectory.getName());
                System.exit(1);
            }


//            if (!modifierPath.exists() || !modifierPath.isFile()) {
//                System.err.println("Couldn't find modifier file " + modifierPath.getName());
//                System.exit(1);
//            }

            if ((!outDirectory.exists() && !outDirectory.mkdirs()) || !outDirectory.isDirectory()) {
                System.err.println("Couldn't create output directory " + outDirectory.getName());
                System.exit(1);
            }

            List<File> templates = findFilesRecursively(inDirectory);

            templates
                    .forEach(t -> {
                        TemplateFile tf = new TemplateFile(inDirectory, t);

                        System.out.println("processing " + tf.getFile().getPath());

                        try {

                            String packageName = tf.getPackageName();
                            String className = tf.getClassName();

                            List<Token> tokens = Parser.FULL.parse(t);
                            ASTNode root = new ASTBuilder().build(tokens);
                            Template tpl = new Compiler().compile(packageName, className, modifier, root, inc -> {
                                System.out.println("Include " + inc);
                            });

                            File packageOutDirectory = tf.getOutputDirectory(outDirectory);
                            if (!packageOutDirectory.exists() && !packageOutDirectory.mkdirs()) {
                                throw new IOException("Failed creating package directory " + packageOutDirectory.getPath());
                            }

                            try (FileWriter fw = new FileWriter(tf.getOutputFile(outDirectory))) {
                                fw.write(tpl.code);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
        }
    }

    private static List<File> findFilesRecursively(File directory) {
        List<File> templates = new ArrayList<>();

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().matches("^.*temporize.*$")) {
                    templates.add(file);

                } else if (file.isDirectory()) {
                    templates.addAll(findFilesRecursively(file));
                }
            }
        }

        return templates;
    }

}
