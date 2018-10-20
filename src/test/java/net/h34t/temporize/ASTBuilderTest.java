package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class ASTBuilderTest {
    @Test
    public void build() throws Exception {
        List<Token> tokens = Parser.FULL.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("testParse.html"));
        new ASTBuilder().build(tokens);
    }

    @Test
    public void buildVariable() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{$bar}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildFailEndIfOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{/if}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildFailElseOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{else}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildFailIfOnly() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}");
        new ASTBuilder().build(tokens);
    }

    @Test
    public void buildConditional() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}bar{/if}");
        new ASTBuilder().build(tokens);
    }

    @Test
    public void buildConditionalAlternative() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}foo{else}no foo{/if}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = RuntimeException.class)
    public void buildConditionalDoubleAlternative() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}foo{else}no foo{else}bar{/if}");
        new ASTBuilder().build(tokens);
    }

    @Test
    public void buildBlock() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{for $foo}{$bar}{/for}baz");
        new ASTBuilder().build(tokens);
        Assert.assertEquals(4, tokens.size());
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildMismatchedBranches() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{for $foo}{$bar}{/if}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.UnmatchedBlockException.class)
    public void buildMismatchedBranches2() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}{$bar}{/for}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void buildMismatchedBranches3() throws Exception {
        List<Token> tokens = Parser.FULL.parse("{if $foo}{for $block}{/if}{/for}");
        new ASTBuilder().build(tokens);
    }

    @Test(expected = ASTBuilder.MismatchedBranchException.class)
    public void testParseConditionalsMissingEndIf() throws IOException {
        new ASTBuilder().build(Parser.FULL.parse("{if $boo}boo{else}far"));
    }

    @Test(expected = ASTBuilder.UnmatchedBlockException.class)
    public void testParseForUnclosedBlock() throws IOException {
        new ASTBuilder().build(Parser.FULL.parse("{for $boo}boo"));
    }

    @Test(expected = ASTBuilder.UnmatchedBlockException.class)
    public void testParseForUnmatchedBlock() throws IOException {
        new ASTBuilder().build(Parser.FULL.parse("boo{/for}bar"));
    }
}