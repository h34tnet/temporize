package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CompilerTest {
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
        new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{$var}{for $forloop}{/for}[include a.b.C as $bar}")), s -> {
                });
    }


    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition() throws Exception {
        new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{$var}{for $var}{/for}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition2() throws Exception {
        new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{for $var}{/for}{$var}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkForIncludeCollision() throws Exception {
        new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{for $var}{/for}{include a.b.C as $var}")), s -> {
                });
    }

    @Test(expected = RuntimeException.class)
    public void checkIncludeForCollision() throws Exception {
        System.out.println(new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{include a.b.C as $var}{for $var}{/for}")), s -> {
                }).code);
    }

    @Test(expected = RuntimeException.class)
    public void checkDoubleDefinition5() throws Exception {
        new Compiler().compile(null, "Foo", "a.b.C",
                new ASTBuilder().build(Parser.FULL.parse("{include a.b.C as $var}{include c.d.E as $var}")), s -> {
                });
    }


    @Test
    public void checkCompilation() throws IOException {
        Template tpl = new Compiler().compile("net.h34t", "TestClass", null,
                new ASTBuilder().build(Parser.FULL.parse("{if $foo}{$foo}{for $bar}{$baz}{/for}{else}nothing{/if}")), s -> {
                });

        Assert.assertEquals("TestClass", tpl.className);
        Assert.assertEquals("net.h34t", tpl.packageName);
        Assert.assertNotNull(tpl.code);
        Assert.assertFalse("Code is empty", tpl.code.trim().isEmpty());
    }

}