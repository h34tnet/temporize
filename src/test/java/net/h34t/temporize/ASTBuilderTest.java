package net.h34t.temporize;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ASTBuilderTest {
    @Test
    public void build() throws Exception {
        List<Token> tokens = Parser.FULL.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("testParse.html"));

        ASTNode root = new ASTBuilder().build(tokens);
        System.out.println(root.print(0));

        // assertTrue(true);
    }

    @Test
    public void buildVariable() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{$bar}");

        ASTNode root = new ASTBuilder().build(tokens);
        System.out.println(root.print(0));

        assertTrue(true);
    }

    @Test(expected = Exception.class)
    public void buildFailEndIfOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{/if}");
        ASTNode root = new ASTBuilder().build(tokens);
    }

    @Test(expected = Exception.class)
    public void buildFailElseOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{else}");
        ASTNode root = new ASTBuilder().build(tokens);
    }

    @Test(expected = Exception.class)
    public void buildFailIfOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}");
        ASTNode root = new ASTBuilder().build(tokens);
    }

    @Test
    public void buildConditional() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}bar{/if}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));

        assertTrue(true);
    }

    @Test
    public void buildConditionalAlternative() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}foo{else}no foo{/if}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));

        assertTrue(true);
    }

    @Test(expected = RuntimeException.class)
    public void buildConditionalDoubleAlternative() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}foo{else}no foo{else}bar{/if}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));

        assertTrue(true);
    }

    @Test
    public void buildBlock() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{for $foo}{$bar}{/for}baz");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));

        assertTrue(true);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildMismatchedBranches() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{for $foo}{$bar}{/if}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildMismatchedBranches2() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}{$bar}{/for}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildMismatchedBranches3() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}{for $block}{/if}{/for}");

        ASTNode root = new ASTBuilder().build(tokens);

        System.out.println(root.print(0));
    }

}