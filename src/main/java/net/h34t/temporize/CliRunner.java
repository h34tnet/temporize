package net.h34t.temporize;

import java.util.Arrays;

public class CliRunner {
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
}
