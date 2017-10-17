package net.h34t.temporize;

import java.io.File;

/**
 * File that represents an un-compiled source template.
 */
public class TemplateFile {

    private File templateDirectory;
    private File templateFile;

    public TemplateFile(File templateDirectory, File templateFile) {
        this.templateDirectory = templateDirectory;
        this.templateFile = templateFile;
    }

    public File getFile() {
        return this.templateFile;
    }

    public String getPackageName() {
        return templateFile.getParentFile().getPath()
                .substring(templateDirectory.getPath().length() + 1)
                .replaceAll("/", ".");

    }

    public String getClassName() {
        String name = this.templateFile.getName();
        return Utils.ucFirst(name.substring(0, name.indexOf(".")));
    }

    public File getOutputDirectory(File outputBase) {
        return new File(outputBase, getPackageName().replaceAll("\\.", "/"));
    }

    public File getOutputFile(File outputBase) {
        return new File(getOutputDirectory(outputBase), getClassName() + ".java");
    }
}
