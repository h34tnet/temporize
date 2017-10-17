package net.h34t.temporize;

import java.io.File;
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
            File modifierPath = new File(args[2]);

            if (!inDirectory.exists()) {
                System.err.println("Couldn't find input directory " + inDirectory.getName());
                System.exit(1);
            }

            if (!modifierPath.exists() || !modifierPath.isFile()) {
                System.err.println("Couldn't find modifier file " + modifierPath.getName());
                System.exit(1);
            }

            if ((!outDirectory.exists() && !outDirectory.mkdirs()) || !outDirectory.isDirectory()) {
                System.err.println("Couldn't create output directory " + outDirectory.getName());
                System.exit(1);
            }

            List<File> templates = findFilesRecursively(inDirectory);

            templates.forEach(System.out::println);

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
