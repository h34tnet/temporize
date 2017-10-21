package net.h34t.temporize;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Helper class that represents an un-compiled source template to simplify filename to classname conversions.
 */
public class TemplateFile {

    private static final String PATH_SEPARATOR_REGEXP = Pattern.quote(File.separator);
    private static final String PATH_SEPARATOR = File.separator;

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
                .replaceAll(PATH_SEPARATOR_REGEXP, ".");

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
