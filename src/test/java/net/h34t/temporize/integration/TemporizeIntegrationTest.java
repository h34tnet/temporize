package net.h34t.temporize.integration;

import net.h34t.temporize.ASTBuilder;
import net.h34t.temporize.Temporize;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TemporizeIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test_successful() throws IOException {
        String inDir = "src/test/resources/it/test-good";
        String outDir = folder.newFolder("out").toString();
        String modifier = "net.h34t.it.Modifier";

        new Temporize().exec(
                inDir,
                outDir,
                modifier
        );

        Path outDirectory = Paths.get(outDir);

        PathMatcher pm = outDirectory.getFileSystem().getPathMatcher("glob:**/*.java");

        Set<Path> files = Files.find(outDirectory, 32, (f, a) -> pm.matches(f))
                .map(outDirectory::relativize)
                .collect(Collectors.toSet());

        // two templates and the interface
        Set<Path> expectedFiles = new LinkedHashSet<>();
        expectedFiles.add(Paths.get("foo/bar/Test.java"));
        expectedFiles.add(Paths.get("foo/bar/baz/Inner.java"));
        expectedFiles.add(Paths.get("net/h34t/temporize/TemporizeTemplate.java"));

        Assert.assertEquals(expectedFiles, files);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void test_fail_missing_closing_if() throws IOException {
        String inDir = "src/test/resources/it/test-bad-missing-if";
        String outDir = folder.newFolder("out").toString();
        String modifier = "net.h34t.it.Modifier";

        new Temporize().exec(
                inDir,
                outDir,
                modifier
        );
    }

    @Test(expected = RuntimeException.class)
    public void test_fail_reserved_path() throws IOException {
        String inDir = "src/test/resources/it/test-bad-reservedname";
        String outDir = folder.newFolder("out").toString();
        String modifier = "net.h34t.it.Modifier";

        new Temporize().exec(
                inDir,
                outDir,
                modifier);
    }

    @Test
    public void test_write_only_after_successful_compilation() throws IOException {
        String inDir = "src/test/resources/it/test-bad-missing-if";
        String outDir = folder.newFolder("out").toString();
        String modifier = "net.h34t.it.Modifier";

        try {
            new Temporize().exec(
                    inDir,
                    outDir,
                    modifier);

            Assert.fail("This should throw a MismatchedBranchException");

        } catch (ASTBuilder.MismatchedBranchException ignored) {
        }

        Files.list(Paths.get(outDir))
                .forEach(p -> System.out.println(p.toString()));

        // now assert that the outDir is empty
        Assert.assertEquals(0, Files.list(Paths.get(outDir))
                .count());
    }

    @Test
    public void test_successful_compile() throws IOException {
        String inDir = "src/test/resources/it/test-good";
        String outDir = folder.newFolder("out").toString();

        new Temporize().exec(
                inDir,
                outDir,
                null);

        Path outDirectory = Paths.get(outDir);

        PathMatcher pm = outDirectory.getFileSystem().getPathMatcher("glob:**/*.java");

        List<File> files = Files.find(outDirectory, 32, (f, a) -> pm.matches(f))
                .map(Path::toFile)
                .collect(Collectors.toList());

        boolean result = TestCompiler.test(files);

        Assert.assertTrue("Compilation failed", result);
    }
}
