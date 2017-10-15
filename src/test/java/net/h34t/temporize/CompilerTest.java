package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompilerTest {
    @Test
    public void getVariables0() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{$value}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getVariables(root);

        Assert.assertEquals(1, nodes.size());
    }

    @Test
    public void getVariables1() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{if $value}{$value}{else}no value, {$name}{/if}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getVariables(root);

        Assert.assertEquals(2, nodes.size());
        Assert.assertEquals("value", nodes.get(0).name);
        Assert.assertEquals("name", nodes.get(1).name);
    }

    @Test
    public void getVariables2() throws Exception {
        ASTNode root = new ASTBuilder().build(Parser.FULL.parse("<html>\n" +
                "{if $value}{$value}{else}no value, {for $block}{$name}{/for}{/if}\n" +
                "</html>"));

        List<ASTNode.Variable> nodes = Compiler.getVariables(root);

        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("value", nodes.get(0).name);
    }
}