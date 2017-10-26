package net.h34t.temporize;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Helper class that represents an un-compiled source template to simplify filename to classname conversions.
 */
public class TemplateFile {

    /**
     * the path separator (e.g. "/" on *nix and "\" on windows) encoded to be used in a regular expression.
     */
    private static final String PATH_SEPARATOR_REGEXP = Pattern.quote(File.separator);

    private final File templateDirectory;
    private final File templateFile;

    public TemplateFile(File templateDirectory, File templateFile) {
        this.templateDirectory = templateDirectory;
        this.templateFile = templateFile;
    }

    public File getFile() {
        return this.templateFile;
    }

    /**
     * Determines the package name depending on the directory structure.
     * <p>
     * I.e.: tpl/foo/bar/baz/Template.temporize.html will yield "foo.bar.baz",
     * where "tpl" is the templateDirectory
     *
     * @return the package name depending on the directory structure.
     */
    public String getPackageName() {
        return templateFile.getParentFile().getPath()
                .substring(templateDirectory.getPath().length() + 1)
                .replaceAll(PATH_SEPARATOR_REGEXP, ".");

    }

    /**
     * Note: this discards everything after the first ".".
     *
     * @return the File name turned into a className
     */
    public String getClassName() {
        String name = this.templateFile.getName();
        return Utils.ucFirst(name.substring(0, name.indexOf(".")));
    }

    /**
     * Determines the directories depending on the package name.
     *
     * @param outputBase the src directory where to store the java files
     * @return a File representing the directory where to store the java file
     */
    public File getOutputDirectory(File outputBase) {
        return new File(outputBase, getPackageName().replaceAll("\\.", "/"));
    }

    /**
     * Turns the class name and path into a File.
     *
     * @param outputBase the src directory where to store the java files
     * @return a file representing the file path and name of the template
     */
    public File getOutputFile(File outputBase) {
        return new File(getOutputDirectory(outputBase), getClassName() + ".java");
    }
}
