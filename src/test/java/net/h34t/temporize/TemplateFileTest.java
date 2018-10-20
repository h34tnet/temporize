package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TemplateFileTest {

    @Test
    public void getPackageName_good() {
        Path directory = Paths.get("tpl");
        Path file = Paths.get("tpl/foo/bar/baz/MyFile.temporize.html");

        String packageName = new TemplateFile(directory, file).getPackageName();

        Assert.assertEquals("foo.bar.baz", packageName);
    }

    @Test(expected = RuntimeException.class)
    public void getPackageName_nopackage() {
        Path directory = Paths.get("tpl");
        Path file = Paths.get("tpl/MyFile.temporize.html");

        new TemplateFile(directory, file);
    }

    @Test(expected = RuntimeException.class)
    public void getPackageName_invalid() {
        Path directory = Paths.get("tpl");
        Path file = Paths.get("MyFile.temporize.html");

        new TemplateFile(directory, file);
    }

    @Test(expected = RuntimeException.class)
    public void getPackageName_containsInvalidWord() {
        Path directory = Paths.get("tpl");
        Path file = Paths.get("tpl/foo/bar/package/MyFile.temporize.html");

        new TemplateFile(directory, file);
    }

    @Test
    public void getClassName() {
        Assert.assertEquals("MyFile", new TemplateFile(Paths.get("tpl"), Paths.get("tpl/foo/bar/baz/MyFile.temporize.html")).getClassName());
        Assert.assertEquals("X", new TemplateFile(Paths.get("tpl"),
                Paths.get("tpl/foo/bar/baz/X.temporize.txt")).getClassName());
    }

    @Test(expected = RuntimeException.class)
    public void getClassNameInvalid() {
        new TemplateFile(Paths.get("tpl"), Paths.get("tpl/foo/bar/baz/.temporize.html")).getClassName();
    }

    @Test
    public void getOutputDirectory() {
        TemplateFile tFile =
                new TemplateFile(Paths.get("tpl"), Paths.get("tpl/foo/bar/baz/Meow.temporize.html"));

        Path out = Paths.get("out");
        Path dir = tFile.getOutputDirectory(out);
        Path file = tFile.getOutputFile(out);

        Assert.assertEquals(Paths.get("out", "foo", "bar", "baz"), dir);
    }

}