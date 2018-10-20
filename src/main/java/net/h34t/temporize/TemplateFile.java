package net.h34t.temporize;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Helper class that represents an un-compiled source template to simplify filename to classname conversions.
 */
public class TemplateFile {

    /**
     * The base directory where all templates are
     */
    private final Path templateDirectory;

    /**
     * The input temporize template file
     */
    private final Path templateFile;

    private final String packageName;

    public TemplateFile(Path templateDirectory, Path templateFile) {
        this.templateDirectory = templateDirectory;
        this.templateFile = templateFile;
        this.packageName = createPackageName();
    }

    public Path getFile() {
        return this.templateFile;
    }

    public String getPackageName() {
        return packageName;
    }

    /**
     * Determines the package name depending on the directory structure.
     * <p>
     * I.e.: tpl/foo/bar/baz/Template.temporize.html will yield "foo.bar.baz",
     * where "tpl" is the templateDirectory
     *
     * @return the package name depending on the directory structure.
     */
    String createPackageName() {
        Path baseDirectory = templateFile.getParent();

        if (baseDirectory.equals(templateDirectory)) {
            throw new RuntimeException("Please move " + templateFile.getFileName().toString() + " into a subdirectory. This " +
                    "is needed to " +
                    "generate a package name.");
        }

        Path relativePath = templateDirectory.relativize(baseDirectory);

        return StreamSupport.stream(relativePath.spliterator(), false)
                .peek(part -> {
                    if (ReservedWords.RESERVED_WORDS.contains(part.toString())) {
                        throw new RuntimeException("Package contains reserved keyword: " + part.toString());
                    }
                })
                .map(Path::toString)
                .collect(Collectors.joining("."));
    }

    /**
     * Note: this discards everything after the first ".". This might lead to collisions when files have the same name
     * but a different ending.
     *
     * @return the file name turned into a className
     */
    public String getClassName() {
        String name = this.templateFile.getFileName().toString();
        String filename = Utils.ucFirst(name.substring(0, name.indexOf(".")));

        if (filename.isEmpty())
            throw new RuntimeException("Invalid template name");

        return filename;
    }

    /**
     * Determines the directories depending on the package name.
     *
     * @param outputBase the src directory where to store the java files
     * @return a File representing the directory where to store the java file
     */
    public Path getOutputDirectory(Path outputBase) {
        return outputBase.resolve(Paths.get("", packageName.split("\\.")));
    }

    /**
     * Turns the class name and path into a File.
     *
     * @param outputBase the src directory where to store the java files
     * @return a file representing the file path and name of the template
     */
    public Path getOutputFile(Path outputBase) {
        return getOutputDirectory(outputBase).resolve(getClassName() + ".java");
    }
}
