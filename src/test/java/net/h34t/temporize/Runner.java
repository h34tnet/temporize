package net.h34t.temporize;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Runner {

    @Ignore
    @Test
    public void testRunner() throws IOException {
        String mod = "mod.Modifiers";

        generate(new File("tpl"), "index.Stocks", new File("tpl-gen"), mod + ".*");
    }

    public void generate(File templateBaseDirectory, String fullName, File outputDirectory, String modifiers) throws IOException {
        File filePath = new File(fullName.replaceAll("\\.", "/"));
        String packageName = filePath.getParent();
        String fileName = filePath.getName();

        File file = new File(new File(templateBaseDirectory, packageName), fileName + ".html");

        List<Token> tokens = Parser.FULL.parse(file);
        ASTNode root = new ASTBuilder().build(tokens);
        Template tpl = new Compiler().compile(packageName, fileName, modifiers, root, include -> {
            try {
                generate(templateBaseDirectory, include, outputDirectory, modifiers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        File packageDir = new File(outputDirectory, packageName);

        if (packageDir.exists() || packageDir.mkdirs()) {
            try (FileWriter fw = new FileWriter(new File(packageDir, tpl.className + ".java"))) {
                fw.write(tpl.code);
            }
        } else throw new IOException("Failed generating the output directory");
    }
}
