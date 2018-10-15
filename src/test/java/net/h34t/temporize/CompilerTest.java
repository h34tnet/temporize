package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CompilerTest {
    @Test
    public void createSetter() throws Exception {
        String setter = Compiler.createSetter("Temporize", "String", "foo", 0);
        Assert.assertEquals("    public Temporize setFoo(String foo) {\n" +
                "        this.foo = foo;\n" +
                "        return this;\n" +
                "    }", setter);
    }

    @Test
    public void getVariables0() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{$value}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getNodesOf(root, ASTNode.Variable.class);

        Assert.assertEquals(1, nodes.size());
    }

    @Test
    public void getVariables1() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{if $value}{$value}{else}no value, {$name}{/if}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getNodesOf(root, ASTNode.Variable.class);

        Assert.assertEquals(2, nodes.size());
        Assert.assertEquals("value", nodes.get(0).name);
        Assert.assertEquals("name", nodes.get(1).name);
    }

    @Test
    public void getVariables2() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{if $value}{$value}{else}no value, {for $block}{$name}{/for}{/if}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getNodesOf(root, ASTNode.Variable.class);

        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("value", nodes.get(0).name);
    }

    @Test
    public void compileValid() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(
                        Parser.FULL.parse("{$var}{for $forloop}{/for}[include a.b.C as $bar}")), s -> {
                });
    }


    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(Parser.FULL.parse("{$var}{for $var}{/for}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition2() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(Parser.FULL.parse("{for $var}{/for}{$var}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition4() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(Parser.FULL.parse("{for $Var}{/for}{$var}")), s -> {
                });
    }


    @Test(expected = RuntimeException.class)
    public void checkForIncludeCollision() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(Parser.FULL.parse("{for $var}{/for}{include a.b.C as $var}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkIncludeForCollision() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(Parser.FULL.parse("{include a.b.C as $var}{for $var}{/for}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition5() throws Exception {
        new Compiler().compile(null, "Foo",
                new ASTBuilder().build(
                        Parser.FULL.parse("{include a.b.C as $var}{include c.d.E as $var}")), s -> {
                });
    }


    @Test
    public void checkCompilation() throws IOException {
        Template tpl = new Compiler().compile("net.h34t", "TestClass",
                new ASTBuilder().build(
                        Parser.FULL.parse("{if $foo}{$foo}{for $bar}{$baz}{/for}{else}nothing{/if}")), s -> {
                });

        Assert.assertEquals("TestClass", tpl.className);
        Assert.assertEquals("net.h34t", tpl.packageName);
        Assert.assertNotNull(tpl.code);
        Assert.assertFalse("Code is empty", tpl.code.trim().isEmpty());
    }

    @Test
    public void compileConditionalBooleans() throws IOException {
        Template tpl = new Compiler().compile("net.h34t", "TestClass",
                new ASTBuilder().build(Parser.FULL.parse("{if $foo}foo{/if}")), s -> {
                });

        Assert.assertEquals("TestClass", tpl.className);
        Assert.assertEquals("net.h34t", tpl.packageName);
        Assert.assertNotNull(tpl.code);
        Assert.assertFalse("Code is empty", tpl.code.trim().isEmpty());
    }

    @Test
    public void compileEmptyConditional() throws IOException {
        Template tpl = new Compiler().compile("net.h34t", "TestClass",
                new ASTBuilder().build(Parser.FULL.parse("{if $foo}{else}{/if}{$bar}")), s -> {
                });

        Assert.assertEquals("TestClass", tpl.className);
        Assert.assertEquals("net.h34t", tpl.packageName);
        Assert.assertNotNull(tpl.code);
        Assert.assertFalse("Code is empty", tpl.code.trim().isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void compileEvilTwinBlockAssignments() throws IOException {
        new Compiler().compile("foo", "Bar",
                new ASTBuilder().build(
                        Parser.FULL.parse("{for $foo}bar{/for}{for $Foo}bar{/for}")
                ), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void compileEvilTwinIncludeAssignments() throws IOException {
        new Compiler().compile("foo", "Bar",
                new ASTBuilder().build(
                        Parser.FULL.parse("{include a.b.C as $bar}{include a.b.C as $Bar}")
                ), s -> {
                });
    }

}