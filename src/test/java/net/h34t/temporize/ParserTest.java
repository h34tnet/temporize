package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ParserTest {

    static InputStream getResource(String name) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(name);
    }

    @Test
    public void testParseFile() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testParse.html");
        List<Token> tokens = Parser.FULL.parse(is);
        testTokens(tokens);

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);
    }

    @Test
    public void testParseString() throws Exception {
        List<Token> tokens = Parser.FULL.parse("<html>\n{$value}\n</html>");
        testTokens(tokens);

        Assert.assertEquals(3, tokens.size());

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);

        Assert.assertEquals(Token.Literal.class, tokens.get(0).getClass());
        Assert.assertEquals("<html>\n", tokens.get(0).contents);

        Assert.assertEquals(Token.Variable.class, tokens.get(1).getClass());
        Assert.assertEquals("{$value}", tokens.get(1).contents);
        Assert.assertEquals("value", ((Token.Variable) tokens.get(1)).variableName);

        Assert.assertEquals(Token.Literal.class, tokens.get(2).getClass());
        Assert.assertEquals("\n</html>", tokens.get(2).contents);
    }

    @Test
    public void testParseStream() throws Exception {
        try (InputStream is = new ByteArrayInputStream("<html>\n{$value}\n</html>".getBytes())) {
            List<Token> tokens = Parser.FULL.parse(is);
            testTokens(tokens);
        }
    }

    @Test
    public void testParseLine() throws Exception {
        List<Token> tokens = Parser.FULL.parseLine("<html>{$value}</html>", 0);
        testTokens(tokens);
    }

    @Test
    public void testVariableModifiers() throws Exception {
        List<Token> tokens = Parser.FULL.parse("<html>{$value|foo|bar}</html>");

        Assert.assertTrue(tokens.get(1) instanceof Token.Variable);

        Token.Variable v = (Token.Variable) tokens.get(1);

        Assert.assertEquals("value", v.variableName);
        Assert.assertEquals("foo", v.modifiers[0]);
        Assert.assertEquals("bar", v.modifiers[1]);
        Assert.assertEquals(2, v.modifiers.length);
    }

    private void testTokens(List<Token> tokens) {
        Assert.assertEquals(3, tokens.size());
        Assert.assertTrue(tokens.get(0) instanceof Token.Literal);
        Assert.assertTrue(tokens.get(1) instanceof Token.Variable);
        Assert.assertTrue(tokens.get(2) instanceof Token.Literal);
        Assert.assertEquals("value", ((Token.Variable) tokens.get(1)).variableName);
    }

    @Test
    public void testParseConditionals() throws IOException {

        List<Token> tokens = Parser.FULL
                .parse(getResource("testParseConditionals.html"));

        Assert.assertEquals(9, tokens.size());
    }

    @Test
    public void testDefaultModifier() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{*$hello}");

        Assert.assertEquals(1, tokens.size());
        Assert.assertEquals(Token.Variable.class, tokens.get(0).getClass());

        Token.Variable var = (Token.Variable) tokens.get(0);
        Assert.assertEquals("hello", var.variableName);
        Assert.assertEquals("def", var.modifiers[0]);
    }

    @Test
    public void testDefaultModifier2() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{*$hello|boo}");

        Assert.assertEquals(1, tokens.size());
        Assert.assertEquals(Token.Variable.class, tokens.get(0).getClass());

        Token.Variable var = (Token.Variable) tokens.get(0);
        Assert.assertEquals("hello", var.variableName);
        Assert.assertEquals("def", var.modifiers[0]);
        Assert.assertEquals("boo", var.modifiers[1]);

    }
}